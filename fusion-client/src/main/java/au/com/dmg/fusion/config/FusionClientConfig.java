package au.com.dmg.fusion.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FusionClientConfig {

	private static FusionClientConfig instance = null;
	private static final String CONFIG_LOCATION = System.getProperty("config.location");

	private FusionClientConfig() throws IOException {
		Properties prop = new Properties();

		InputStream input = new FileInputStream(CONFIG_LOCATION);
		prop.load(input);

		this.certificateLocation = prop.getProperty("certificate.location");
		this.serverDomain = prop.getProperty("server.domain");
		this.socketProtocol = prop.getProperty("socket.protocol");
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

	public static FusionClientConfig getInstance() throws IOException {
		if (instance == null) {
			instance = new FusionClientConfig();
		}

		return instance;
	}

}
