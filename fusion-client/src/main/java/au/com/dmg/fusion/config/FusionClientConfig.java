package au.com.dmg.fusion.config;

import javax.naming.ConfigurationException;

import au.com.dmg.fusion.util.Util;

public class FusionClientConfig {

	private static FusionClientConfig instance = null;

	private FusionClientConfig(String serverDomain, String socketProtocol, String env) {
		this.serverDomain = serverDomain;
		this.socketProtocol = socketProtocol;
		this.env = env;
	}

	private String serverDomain;
	private String socketProtocol;
	private String env;

	
	public String getServerDomain() {
		return serverDomain;
	}

	public String getSocketProtocol() {
		return socketProtocol;
	}

	public String getEnv(){return env;}

	public static FusionClientConfig getInstance() throws ConfigurationException {
		if (instance == null) {
			throw new ConfigurationException("Fusion client config values have not been initialized.");
		}

		return instance;
	}

	public static void init(String serverDomain, String socketProtocol, String env)
			throws ConfigurationException {
		if (instance != null) {
			throw new ConfigurationException("Fusion client config already exists");
		}

		if (Util.isStringNullEmptyBlank(serverDomain)) {
			throw new ConfigurationException(
					"Server domain and socket protocol values are required to initialize the Fusion Client config.");
		}

		if (Util.isStringNullEmptyBlank(socketProtocol)) {
			socketProtocol = "TLSv1.2"; // default value
		}

		if (Util.isStringNullEmptyBlank(env)) {
			env = "DEV"; // default value
		}

		instance = new FusionClientConfig(serverDomain, socketProtocol, env);
	}

	public static boolean isInitialised() {
		return instance != null;
	}

}
