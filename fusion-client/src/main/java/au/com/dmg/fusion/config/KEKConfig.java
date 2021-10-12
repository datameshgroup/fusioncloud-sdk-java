package au.com.dmg.fusion.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KEKConfig {

	private static KEKConfig instance = null;
	private static final String CONFIG_LOCATION = System.getProperty("config.location");

	public KEKConfig() throws IOException {
		Properties prop = new Properties();

		InputStream input = new FileInputStream(CONFIG_LOCATION);
		prop.load(input);

		this.keyIdentifier = prop.getProperty("key.identifier");
		this.keyVersion = prop.getProperty("key.version");
	}

	private String keyIdentifier;
	private String keyVersion;

	public String getKeyIdentifier() {
		return keyIdentifier;
	}

	public String getKeyVersion() {
		return keyVersion;
	}

	public static KEKConfig getInstance() throws IOException {
		if (instance == null) {
			instance = new KEKConfig();
		}

		return instance;
	}

}
