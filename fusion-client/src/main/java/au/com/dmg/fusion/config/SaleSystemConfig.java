package au.com.dmg.fusion.config;

import javax.naming.ConfigurationException;

import au.com.dmg.fusion.util.Util;

public class SaleSystemConfig {

	private static SaleSystemConfig instance = null;

	private SaleSystemConfig(String providerIdentification, String applicationName, String softwareVersion,
			String certificationCode) {
		this.providerIdentification = providerIdentification;
		this.applicationName = applicationName;
		this.softwareVersion = softwareVersion;
		this.certificationCode = certificationCode;
	}

	private String providerIdentification;
	private String applicationName;
	private String softwareVersion;
	private final String certificationCode;

	public String getProviderIdentification() {
		return providerIdentification;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public String getCertificationCode() {
		return certificationCode;
	}

	public static SaleSystemConfig getInstance() throws ConfigurationException {
		if (instance == null) {
			throw new ConfigurationException("Sale system config values have not been initialized.");
		}

		return SaleSystemConfig.instance;
	}

	public static void init(String providerIdentification, String applicationName, String softwareVersion,
			String certificationCode) throws ConfigurationException {
		if (instance != null) {
			throw new ConfigurationException("Sale system config already exists");
		}

		if (Util.isStringNullEmptyBlank(providerIdentification) || Util.isStringNullEmptyBlank(applicationName)
				|| Util.isStringNullEmptyBlank(softwareVersion) || Util.isStringNullEmptyBlank(certificationCode)) {
			throw new ConfigurationException(
					"Certificate location, server domain and socket protocol values are required to initialize the Fusion Client config.");
		}

		instance = new SaleSystemConfig(providerIdentification, applicationName, softwareVersion, certificationCode);
	}

	public static boolean isInitialised() {
		return instance != null;
	}

}
