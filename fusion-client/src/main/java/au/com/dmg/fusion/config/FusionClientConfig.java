package au.com.dmg.fusion.config;

import javax.naming.ConfigurationException;

import au.com.dmg.fusion.util.Util;

public class FusionClientConfig {

	private static FusionClientConfig instance = null;

	private FusionClientConfig(String certificateLocation, String serverDomain, String socketProtocol) {
		this.certificateLocation = certificateLocation;
		this.serverDomain = serverDomain;
		this.socketProtocol = socketProtocol;
	}

	private String certificateLocation;
	private String serverDomain;
	private String socketProtocol;

	public String getCertificateLocation() {
		return certificateLocation;
	}

	public String getServerDomain() {
		return serverDomain;
	}

	public String getSocketProtocol() {
		return socketProtocol;
	}

	public static FusionClientConfig getInstance() throws ConfigurationException {
		if (instance == null) {
			throw new ConfigurationException("Fusion client config values have not been initialized.");
		}

		return instance;
	}

	public static void init(String certificateLocation, String serverDomain, String socketProtocol)
			throws ConfigurationException {
		if (instance != null) {
			throw new ConfigurationException("Fusion client config already exists");
		}

		if (Util.isStringNullEmptyBlank(certificateLocation) || Util.isStringNullEmptyBlank(serverDomain)) {
			throw new ConfigurationException(
					"Certificate location, server domain and socket protocol values are required to initialize the Fusion Client config.");
		}

		if (Util.isStringNullEmptyBlank(socketProtocol)) {
			socketProtocol = "TLSv1.2"; // default value
		}

		instance = new FusionClientConfig(certificateLocation, serverDomain, socketProtocol);
	}

	public static boolean isInitialised() {
		return instance != null;
	}

}
