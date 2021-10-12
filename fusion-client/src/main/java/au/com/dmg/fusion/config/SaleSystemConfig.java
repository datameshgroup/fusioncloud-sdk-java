package au.com.dmg.fusion.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SaleSystemConfig {

	private static SaleSystemConfig instance = null;
	private static final String CONFIG_LOCATION = System.getProperty("config.location");

	private SaleSystemConfig() throws IOException {
		Properties prop = new Properties();

		InputStream input = new FileInputStream(CONFIG_LOCATION);
		prop.load(input);

		this.providerIdentification = prop.getProperty("provider.identification");
		this.applicationName = prop.getProperty("application.name");
		this.softwareVersion = prop.getProperty("software.version");
		this.certificationCode = prop.getProperty("certification.code");
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

	public static SaleSystemConfig getInstance() throws IOException {
		if (SaleSystemConfig.instance == null) {
			SaleSystemConfig.instance = new SaleSystemConfig();
		}

		return SaleSystemConfig.instance;
	}

}
