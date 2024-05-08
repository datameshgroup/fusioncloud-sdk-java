package au.com.dmg.fusion.client;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import au.com.dmg.fusion.request.transactionstatusrequest.TransactionStatusRequest;
import au.com.dmg.fusion.response.TransactionStatusResponse;
import au.com.dmg.fusion.securitytrailer.SecurityTrailer;
import au.com.dmg.fusion.util.*;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

import au.com.dmg.fusion.SaleToPOI;
import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.exception.FusionException;
import au.com.dmg.fusion.request.Request;
import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.response.SaleToPOIResponse;

@ClientEndpoint(encoders = { SaleToPOIRequestEncoder.class, SaleToPOIResponseEncoder.class }, decoders = {
		SaleToPOIDecoder.class })
public class FusionClient {

	private final static String fusionCloudVersion = "1.0.8";

	private final static Logger LOGGER = Logger.getLogger(FusionClient.class.getName());

	Session userSession = null;
	private MessageHandler messageHandler;
	private ErrorHandler errorHandler;

	private final NexoMessageParser messageParser;

	private String lastTxnServiceID;
	private String lastMessageRefServiceID;

	private URI uri;

	private boolean useTestEnvironment = false;

	private final BlockingQueue<SaleToPOI> responseQueue;

	private String saleID;
	private String poiID;
	private String kek;
	private String customURL;

	public FusionClient(boolean useTestEnvironment) {

		this.useTestEnvironment = useTestEnvironment;

		responseQueue = new LinkedBlockingQueue<>();
		messageParser = new NexoMessageParser(useTestEnvironment);

		lastTxnServiceID = null;
		lastMessageRefServiceID = null;
		customURL = null;
	}

	public static interface MessageHandler {
		public void handleMessage(SaleToPOI message);
	}

	public static interface ErrorHandler {
		public void handleError(Session session, Throwable t) throws Exception;
	}
	public void setSettings(String saleID, String poiID, String kek)
	{
		setSettings(saleID, poiID, kek, null);
	}

	public void setSettings(String saleID, String poiID, String kek, String customURL)
	{
		this.saleID = saleID;
		this.poiID = poiID;
		this.kek = kek;
		SecurityTrailerUtil.KEK = kek; //used by the SaleToPOIDecoder when processing a message
		this.customURL = customURL;
	}

	public String getVersion(){
		LOGGER.info("Fusion Cloud Version: " + fusionCloudVersion);
		return fusionCloudVersion;
	}

	public void connect() throws FusionException{
		connect(0, 0);
	}

	public void connect(long sendTimeout, long maxSessionIdleTimeout)
			throws FusionException {
		LOGGER.info("(Version: " + fusionCloudVersion + ") Connecting to websocket server...");

		try {
			URI serverDomain = getServerDomain();
			ClientManager cm = ClientManager.createClient();

			TrustManager[] trustManagers = addCertificate();
			setProtocolVersion(trustManagers);
			cm.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR,
					new SSLEngineConfigurator(SSLContext.getDefault(), true, false, false));

			this.uri = serverDomain;

			cm.setAsyncSendTimeout(sendTimeout);
			cm.setDefaultMaxSessionIdleTimeout(maxSessionIdleTimeout);
			cm.connectToServer(this, serverDomain);
		} catch (Exception ex) {
			throw new FusionException("(Version: " + fusionCloudVersion + ") Error when connecting to WebSocket server" + ex.getMessage(), false);
		}
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
					responseQueue.put(message);
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

	public SaleToPOI readMessage() throws FusionException {
		SaleToPOI saleToPOI;
		try
		{
			boolean checkNextMessage;
			do
			{
				saleToPOI =  null;
				checkNextMessage = false;
				// Check if we are connected
				if (!isConnected())
				{
					throw new FusionException("Connection is closed!", false);
				}

				Optional<SaleToPOI> optResponse = Optional
						.ofNullable(responseQueue.poll(250, TimeUnit.MILLISECONDS));

				if (optResponse.isPresent())
				{
					saleToPOI = optResponse.get();
					LOGGER.info("RX:" + saleToPOI);
					checkNextMessage = !validateMessage(saleToPOI);
				}

			} while(checkNextMessage);
		}
		catch (Exception e) //InterruptedException handling is required for poll.  Handle other errors as well
		{
			throw new FusionException("An error occurred while polling the response queue: " + e.getMessage());
		}
		
		return saleToPOI;
	}

	public void sendMessage(Request message) throws FusionException {
		sendMessage(message, MessageHeaderUtil.generateServiceID());
	}

	public void sendMessage(Request message, String serviceID) throws FusionException {
		sendMessage(messageParser.BuildSaleToPOIMessage(serviceID, saleID, poiID, message));
	}

	public void sendMessage(SaleToPOIRequest saleToPOI) throws FusionException {
		LOGGER.info("Sending message to websocket server");

		if (!isConnected()) {
			LOGGER.info("Websocket connection is closed. Trying to connect..");
			try{
				this.connect();
			} catch (FusionException e){
				throw new FusionException("Connection is closed! " + e);
			}

		}

		if(saleToPOI.getMessageHeader().getMessageCategory() == MessageCategory.TransactionStatus){
			TransactionStatusRequest tsr = saleToPOI.getTransactionStatusRequest();
			if(tsr != null){
				lastMessageRefServiceID = tsr.getMessageReference().getServiceID();
				LOGGER.info("Request Message Reference ServiceID = " + lastMessageRefServiceID);
			}
		}
		if(saleToPOI.getMessageHeader().getMessageCategory() != MessageCategory.Abort){
			lastTxnServiceID = saleToPOI.getMessageHeader().getServiceID();
			LOGGER.info("Request ServiceID = " + lastTxnServiceID);
		}

		LOGGER.info("TX:" + saleToPOI);

		this.userSession.getAsyncRemote().sendObject(saleToPOI);

	}

	public void setMessageHandler(MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}

	public void setErrorHandler(ErrorHandler errHandler) {
		this.errorHandler = errHandler;
	}

	public boolean isConnected() {
		return userSession != null && userSession.isOpen();
	}

	public URI getURI() {
		return uri;
	}

	private void setProtocolVersion(TrustManager[] trustManagers)
			throws NoSuchAlgorithmException, KeyManagementException, IOException, ConfigurationException {

		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(null, trustManagers, null);
		SSLContext.setDefault(context);
	}

	private TrustManager[] addCertificate() throws CertificateException, KeyStoreException, NoSuchAlgorithmException,
			IOException, ConfigurationException {
		String cert = Cert.getCertificate(useTestEnvironment);
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

	private boolean validateMessage(SaleToPOI message) {

		if((lastTxnServiceID == null) || (lastTxnServiceID.length() == 0))
		{
			return true;
		}

		boolean messageValid = true;

		if(message instanceof SaleToPOIRequest){
			SaleToPOIRequest request = (SaleToPOIRequest) message;

			MessageCategory messageCategory = request.getMessageHeader().getMessageCategory();
			String currentServiceID = request.getMessageHeader().getServiceID();

			if ((currentServiceID == null) || (currentServiceID.length() == 0)) {
				LOGGER.info("No ServiceID received in " + messageCategory + ".  Expected value is " + lastTxnServiceID + " .  Will process the next message instead.");
				return false;
			}

			if (!currentServiceID.equals(lastTxnServiceID)) {
				LOGGER.info("Unexpected ServiceID " + currentServiceID + " received in " + messageCategory + ".  Expected value is " + lastTxnServiceID + " .  Will process the next message instead.");
				return false;
			}

		}

		else if (message instanceof SaleToPOIResponse) {
			SaleToPOIResponse response = (SaleToPOIResponse) message;

			MessageCategory messageCategory = response.getMessageHeader().getMessageCategory();
			//Don't verify ServiceID for EventNotification
			if (messageCategory == MessageCategory.Event) {
				return true;
			}

			String currentServiceID = response.getMessageHeader().getServiceID();
			if ((currentServiceID == null) || (currentServiceID.length() == 0)) {
				LOGGER.info("No ServiceID received in " + messageCategory + ".  Expected value is " + lastTxnServiceID + " .  Will process the next message instead.");
				return false;
			}

			if (!currentServiceID.equals(lastTxnServiceID)) {
				LOGGER.info("Unexpected ServiceID " + currentServiceID + " received in " + messageCategory + ".  Expected value is " + lastTxnServiceID + " .  Will process the next message instead.");
				return false;
			}

			if(messageCategory == MessageCategory.TransactionStatus)
			{
				currentServiceID = "";
				messageValid = false;
				TransactionStatusResponse tsResponse = response.getTransactionStatusResponse();
				if(tsResponse != null){
					if(tsResponse.getRepeatedMessageResponse() != null){
						if(tsResponse.getRepeatedMessageResponse().getMessageHeader() != null) {
							currentServiceID = tsResponse.getRepeatedMessageResponse().getMessageHeader().getServiceID();
						}
					}
					else if(tsResponse.getMessageReference() != null){ //Failure - In Progress
						currentServiceID = tsResponse.getMessageReference().getServiceID();
					}
					else{ //Message not found, does not have MessageReference
						return true; // Exit for NotFound
					}
					messageValid = lastMessageRefServiceID.equals(currentServiceID);
					if(!messageValid){
						LOGGER.info("Unexpected Message Reference ServiceID " + currentServiceID + " received in " + messageCategory + ".  Expected value is " + lastTxnServiceID + " .  Will process the next message instead.");
					}
				}
			}

		}
		return messageValid;
	}

	private URI getServerDomain() throws URISyntaxException{
		String serverURL;
		if((customURL != null) && (customURL.length() > 0))
		{
			serverURL = customURL;
		}
		else if(useTestEnvironment)
		{
			serverURL = "wss://www.cloudposintegration.io/nexouat1";
		}
		else
		{
			serverURL = "wss://nexo.datameshgroup.io:5000";
		}
		return new URI(serverURL);
	}
}
