package com.dmg.fusion.client;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

import com.dmg.fusion.config.FusionClientConfig;
import com.dmg.fusion.exception.NotConnectedException;
import com.dmg.fusion.util.SaleToPOIDecoder;
import com.dmg.fusion.util.SaleToPOIRequestEncoder;
import com.dmg.fusion.util.SaleToPOIResponseEncoder;

import au.com.dmg.fusion.SaleToPOI;
import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.response.SaleToPOIResponse;

/**
 * Provides the necessary methods to establish connection and communicate with a
 * websocket server
 */
@ClientEndpoint(encoders = { SaleToPOIRequestEncoder.class, SaleToPOIResponseEncoder.class }, decoders = {
		SaleToPOIDecoder.class })
public class FusionClient {

	private final static Logger LOGGER = Logger.getLogger(FusionClient.class.getName());

	Session userSession = null;

	private MessageHandler messageHandler;

	private ErrorHandler errorHandler;

	private URI uri;

	public final BlockingQueue<SaleToPOIResponse> inQueueResponse;

	public final BlockingQueue<SaleToPOIRequest> inQueueRequest;

	public FusionClient() {
		inQueueResponse = new LinkedBlockingQueue<>();
		inQueueRequest = new LinkedBlockingQueue<>();
	}

	/**
	 * Constructs a ClientManager and automatically connects to the specified URI.
	 *
	 * @param serverUri the server URI to connect to
	 * @throws DeploymentException
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 */
	public void connect(URI endpointURI) throws DeploymentException, IOException, KeyManagementException,
			NoSuchAlgorithmException, CertificateException, KeyStoreException {
		connect(endpointURI, 0, 0);
	}

	/**
	 * Constructs a ClientManager and automatically connects to the specified URI.
	 *
	 * @param serverUri             the server URI to connect to
	 * @param sendTimeout           the send message timeout in milliseconds
	 * @param maxSessionIdleTimeout the amount of time in milliseconds a web socket
	 *                              session will be closed if it has been inactive
	 * @throws DeploymentException
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 */
	public void connect(URI endpointURI, long sendTimeout, long maxSessionIdleTimeout) throws CertificateException,
			KeyStoreException, NoSuchAlgorithmException, IOException, KeyManagementException, DeploymentException {
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

	/**
	 * Closes the websocket connection.
	 *
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		LOGGER.info("Disconnecting from websocket server...");
		this.userSession.close();
	}

	/**
	 * Callback after an opening handshake has been performed.
	 *
	 * @param userSession the current session
	 */
	@OnOpen
	public void onOpen(Session userSession) {
		LOGGER.info("Connection to websocket server is open");

		this.userSession = userSession;
		setMessageHandler(new FusionClient.MessageHandler() {
			@Override
			public void handleMessage(SaleToPOI message) {
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
				LOGGER.info(t.getMessage());
			}
		});
	}

	/**
	 * Callback after the websocket connection has been closed.
	 *
	 * @param userSession the session that is getting closed
	 * @param reason      the reason for closing the connection
	 */
	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		LOGGER.info("Connection to websocket server is closed");

		this.userSession = null;
	}

	/**
	 * Callback for Message Events. This method will be invoked when a message is
	 * received.
	 *
	 * @param message The SaleToPOI message
	 */
	@OnMessage
	public void onMessage(SaleToPOI message) {
		LOGGER.info("Received message from websocket server");

		if (this.messageHandler != null) {
			this.messageHandler.handleMessage(message);
		}
	}

	@OnError
	public void error(Session session, Throwable t) {
		LOGGER.info("An error occurred");

		if (this.errorHandler != null) {
			this.errorHandler.handleError(session, t);
		}
	}

	/**
	 * Sends <var>message</var> to the connected websocket server.
	 *
	 * @param message The string that will be sent to the server.
	 */
	public void sendMessage(String message) throws NotConnectedException {
		LOGGER.info("Sending message to websocket server");

		if (!isConnected()) {
			throw new NotConnectedException("Connection is closed!");
		}
		this.userSession.getAsyncRemote().sendText(message);
	}

	/**
	 * Sends <var>message</var> to the connected websocket server.
	 *
	 * @param message The SaleToPOIRequest that will be sent to the server.
	 */
	public void sendMessage(SaleToPOIRequest message) throws NotConnectedException {
		LOGGER.info("Sending message to websocket server");

		if (!isConnected()) {
			throw new NotConnectedException("Connection is closed!");
		}
		this.userSession.getAsyncRemote().sendObject(message);
	}

	/**
	 * Sends <var>message</var> to the connected websocket server.
	 *
	 * @param message The SaleToPOIResponse that will be sent to the server.
	 */
	public void sendMessage(SaleToPOIResponse message) throws NotConnectedException {
		LOGGER.info("Sending message to websocket server");

		if (!isConnected()) {
			throw new NotConnectedException("Connection is closed!");
		}
		this.userSession.getAsyncRemote().sendObject(message);

	}

	/**
	 * Sets message handler
	 *
	 * @param msgHandler
	 */
	public void setMessageHandler(MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}

	public static interface MessageHandler {
		public void handleMessage(SaleToPOI message);
	}

	/**
	 * Sets error handler
	 *
	 * @param errHandler
	 */
	public void setErrorHandler(ErrorHandler errHandler) {
		this.errorHandler = errHandler;
	}

	public static interface ErrorHandler {
		public void handleError(Session session, Throwable t);
	}

	/**
	 * Checks whether a <var>userSession</var> exists and if it is open.
	 */
	public boolean isConnected() {
		return userSession != null && userSession.isOpen();
	}

	private void setProtocolVersion(TrustManager[] trustManagers)
			throws NoSuchAlgorithmException, KeyManagementException, IOException {

		SSLContext context = SSLContext.getInstance(FusionClientConfig.getInstance().getSocketProtocol());
		context.init(null, trustManagers, null);
		SSLContext.setDefault(context);
	}

	private TrustManager[] addCertificate()
			throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException {

		FileInputStream fis = new FileInputStream(FusionClientConfig.getInstance().getCertificateLocation());
		X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509")
				.generateCertificate(new BufferedInputStream(fis));

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("unify-test", ca);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);

		return tmf.getTrustManagers();
	}

	/**
	 * Returns the <var>URI</var>.
	 */
	public URI getURI() {
		return uri;
	}

}
