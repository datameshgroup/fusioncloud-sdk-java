package au.com.dmg.fusion.client;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import au.com.dmg.fusion.util.Cert;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

import au.com.dmg.fusion.SaleToPOI;
import au.com.dmg.fusion.config.FusionClientConfig;
import au.com.dmg.fusion.config.*;
import au.com.dmg.fusion.exception.NotConnectedException;
import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.response.SaleToPOIResponse;
import au.com.dmg.fusion.util.SaleToPOIDecoder;
import au.com.dmg.fusion.util.SaleToPOIRequestEncoder;
import au.com.dmg.fusion.util.SaleToPOIResponseEncoder;

@ClientEndpoint(encoders = { SaleToPOIRequestEncoder.class, SaleToPOIResponseEncoder.class }, decoders = {
		SaleToPOIDecoder.class })
public class FusionClient {

	private final static Logger LOGGER = Logger.getLogger(FusionClient.class.getName());

	public FusionClientConfig fusionClientConfig;
	Session userSession = null;

	private MessageHandler messageHandler;

	private ErrorHandler errorHandler;

	private URI uri;

	private String env;

	public final BlockingQueue<SaleToPOIResponse> inQueueResponse;

	public final BlockingQueue<SaleToPOIRequest> inQueueRequest;

	public void init(FusionClientConfig fusionClientConfig) throws URISyntaxException, DeploymentException, IOException, KeyManagementException,
			NoSuchAlgorithmException, CertificateException, KeyStoreException, ConfigurationException {
		this.fusionClientConfig = fusionClientConfig;

		fusionClientConfig.init();
		KEKConfig.init(fusionClientConfig.kekValue, fusionClientConfig.keyIdentifier, fusionClientConfig.keyVersion);
		SaleSystemConfig.init(fusionClientConfig.providerIdentification, fusionClientConfig.applicationName, fusionClientConfig.softwareVersion, fusionClientConfig.certificationCode);

		connect(fusionClientConfig.getServerDomain());
//
//
//		createDefaultHeader()
//		createDefaultSecurityTrailer()

	}
	public FusionClient() {
		inQueueResponse = new LinkedBlockingQueue<>();
		inQueueRequest = new LinkedBlockingQueue<>();
	}

	public void connect(URI endpointURI) throws DeploymentException, IOException, KeyManagementException,
			NoSuchAlgorithmException, CertificateException, KeyStoreException, ConfigurationException {
		connect(endpointURI, 0, 0);
	}

	public void connect(URI endpointURI, long sendTimeout, long maxSessionIdleTimeout)
			throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException,
			KeyManagementException, DeploymentException, ConfigurationException {
		LOGGER.info("Connecting to websocket server...");

		ClientManager cm = ClientManager.createClient();

		TrustManager[] trustManagers = addCertificate();
		setProtocolVersion(trustManagers);
		cm.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR,
				new SSLEngineConfigurator(SSLContext.getDefault(), true, false, false));

		this.uri = endpointURI;

		cm.setAsyncSendTimeout(sendTimeout);
		cm.setDefaultMaxSessionIdleTimeout(maxSessionIdleTimeout);
		cm.connectToServer(this, endpointURI);
	}

	public void disconnect() throws IOException {
		LOGGER.info("Disconnecting from websocket server...");
		this.userSession.close();
	}

	@OnOpen
	public void onOpen(Session userSession) {
		LOGGER.info("Connection to websocket server is open");

		this.userSession = userSession;
		setMessageHandler(new FusionClient.MessageHandler() {
			@Override
			public void handleMessage(SaleToPOI message) {
				LOGGER.info("RX:" + message);
				try {
					if (message instanceof SaleToPOIRequest) {
						inQueueRequest.put((SaleToPOIRequest) message);
					} else if (message instanceof SaleToPOIResponse) {
						inQueueResponse.put((SaleToPOIResponse) message);
					}
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE, e.getMessage());
				}
			}
		});
		setErrorHandler(new FusionClient.ErrorHandler() {
			@Override
			public void handleError(Session session, Throwable t) {
				LOGGER.log(Level.SEVERE, t.getMessage());
			}
		});
	}

	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		LOGGER.info("Connection to websocket server is closed");

		this.userSession = null;
	}

	@OnMessage
	public void onMessage(SaleToPOI message) {
		LOGGER.info("Received message from websocket server");

		if (this.messageHandler != null) {
			this.messageHandler.handleMessage(message);
		}
	}

	@OnError
	public void error(Session session, Throwable t) throws Exception {
		LOGGER.info("An error occurred");

		if (this.errorHandler != null) {
			this.errorHandler.handleError(session, t);
		}
	}

	public void sendMessage(String message) throws NotConnectedException {
		LOGGER.info("Sending message to websocket server");

		if (!isConnected()) {
			throw new NotConnectedException("Connection is closed!");
		}
		this.userSession.getAsyncRemote().sendText(message);
	}

	public void sendMessage(SaleToPOIRequest message) throws NotConnectedException {
		LOGGER.info("Sending message to websocket server");

		if (!isConnected()) {
			throw new NotConnectedException("Connection is closed!");
		}
		LOGGER.info("TX:" + message);
		this.userSession.getAsyncRemote().sendObject(message);
	}

	public void sendMessage(SaleToPOIResponse message) throws NotConnectedException {
		LOGGER.info("Sending message to websocket server");

		if (!isConnected()) {
			throw new NotConnectedException("Connection is closed!");
		}
		this.userSession.getAsyncRemote().sendObject(message);

	}

	public void setMessageHandler(MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}

	public static interface MessageHandler {
		public void handleMessage(SaleToPOI message);
	}

	public void setErrorHandler(ErrorHandler errHandler) {
		this.errorHandler = errHandler;
	}

	public static interface ErrorHandler {
		public void handleError(Session session, Throwable t) throws Exception;
	}

	public boolean isConnected() {
		return userSession != null && userSession.isOpen();
	}

	private void setProtocolVersion(TrustManager[] trustManagers)
			throws NoSuchAlgorithmException, KeyManagementException, IOException, ConfigurationException {

		SSLContext context = SSLContext.getInstance(FusionClientConfig.getInstance().getSocketProtocol());
		context.init(null, trustManagers, null);
		SSLContext.setDefault(context);
	}

	private TrustManager[] addCertificate() throws CertificateException, KeyStoreException, NoSuchAlgorithmException,
			IOException, ConfigurationException {
		String cert = Cert.getCertificate(FusionClientConfig.getInstance().getEnv());
		InputStream fis = new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));

		X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509")
				.generateCertificate(new BufferedInputStream(fis));

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("unify-test", ca);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);

		return tmf.getTrustManagers();
	}

	public URI getURI() {
		return uri;
	}

}
