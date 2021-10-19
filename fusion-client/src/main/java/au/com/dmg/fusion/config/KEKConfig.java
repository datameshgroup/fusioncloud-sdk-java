package au.com.dmg.fusion.config;

import javax.naming.ConfigurationException;

import au.com.dmg.fusion.util.Util;

public class KEKConfig {

	private static KEKConfig instance = null;

	public KEKConfig(String value, String keyIdentifier, String keyVersion) {
		this.value = value;
		this.keyIdentifier = keyIdentifier;
		this.keyVersion = keyVersion;
	}

	private String value;
	private String keyIdentifier;
	private String keyVersion;

	public String getValue() {
		return value;
	}

	public String getKeyIdentifier() {
		return keyIdentifier;
	}

	public String getKeyVersion() {
		return keyVersion;
	}

	public static KEKConfig getInstance() throws ConfigurationException {
		if (instance == null) {
			throw new ConfigurationException("KEK configuration values have not been initialized.");
		}

		return instance;
	}

	public static void init(String value, String keyIdentifier, String keyVersion) throws ConfigurationException {
		if (instance != null) {
			throw new ConfigurationException("KEK configuration already exists");
		}

		if (Util.isStringNullEmptyBlank(value) || Util.isStringNullEmptyBlank(keyIdentifier)
				|| Util.isStringNullEmptyBlank(keyVersion)) {
			throw new ConfigurationException(
					"KEK value, key identifier and key version values are required to initialize the KEK config.");
		}

		instance = new KEKConfig(value, keyIdentifier, keyVersion);
	}

	public static boolean isInitialised() {
		return instance != null;
	}

}
