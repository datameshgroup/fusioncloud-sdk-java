package au.com.dmg.fusion.config;

import javax.naming.ConfigurationException;
import java.net.URI;
import java.net.URISyntaxException;
import au.com.dmg.fusion.util.Util;

public class FusionClientConfig {

	public static FusionClientConfig instance = null;
	public String serverDomain;
	public String kekValue;
	public String keyIdentifier;
	public String keyVersion;
	public String providerIdentification;
	public String applicationName;
	public String softwareVersion;
	public String certificationCode;
	public String saleID;
	public String poiID;
	public boolean isTestEnvironment;
	public String socketProtocol;

	public FusionClientConfig(boolean isTestEnvironment){
		this.isTestEnvironment = isTestEnvironment;
		this.serverDomain = isTestEnvironment ? "wss://www.cloudposintegration.io/nexodev" : "wss://nexo.datameshgroup.io:5000";
		this.keyIdentifier = isTestEnvironment ? "SpecV2TestMACKey" : "SpecV2ProdMACKey";
		this.keyVersion = isTestEnvironment ? "20191122164326" : "20191122164326";
		this.socketProtocol = "TLSv1.2";
	}

	public static FusionClientConfig getInstance() throws ConfigurationException {
		if (instance == null) {
			throw new ConfigurationException("Fusion client config values have not been initialized.");
		}

		return instance;
	}
	public void init()
			throws ConfigurationException {
		if (instance != null) {
			throw new ConfigurationException("Fusion client config already exists");
		}
		instance = new FusionClientConfig(isTestEnvironment);
	}

	public static boolean isInitialised() {
		return instance != null;
	}

	public URI getServerDomain() throws URISyntaxException {
		URI test = new URI(this.serverDomain);
		return test;
	}

	public String getSocketProtocol() {
		return socketProtocol;
	}

	public String getEnv(){
		String env = isTestEnvironment ? "DEV" : "PROD";
		return env;
	}
}
