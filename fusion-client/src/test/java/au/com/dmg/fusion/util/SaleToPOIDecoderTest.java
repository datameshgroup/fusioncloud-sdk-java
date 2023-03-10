package au.com.dmg.fusion.util;

import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.response.SaleToPOIResponse;
import org.junit.Before;
import org.junit.Test;

import javax.naming.ConfigurationException;
import javax.websocket.DecodeException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SaleToPOIDecoderTest {

	private au.com.dmg.fusion.util.SaleToPOIDecoder decoder;

	@Before
	public void init() throws UnsupportedEncodingException, ConfigurationException {
		SecurityTrailerUtil.KEK = "44DACB2A22A4A752ADC1BBFFE6CEFB589451E0FFD83F8B21";
		this.decoder = new au.com.dmg.fusion.util.SaleToPOIDecoder();
	}

	@Test
	public void testDecodeRequest() throws DecodeException, UnsupportedEncodingException, IOException {
		Path path = Paths.get("build/resources/test/SaleToPOIRequest.json");

		String saleToPOIRequestString = new String(Files.readAllBytes(path.toAbsolutePath()), "UTF-8");
		assertTrue(saleToPOIRequestString.startsWith("{\"SaleToPOIRequest\":"));

		SaleToPOIRequest saleToPOIRequest = (SaleToPOIRequest) decoder.decode(saleToPOIRequestString);

		assertFalse(saleToPOIRequest == null);
		assertFalse(saleToPOIRequest.toJson().startsWith("{\"SaleToPOIRequest\":"));
	}

	@Test
	public void testDecodeResponse() throws DecodeException, UnsupportedEncodingException, IOException {
		Path path = Paths.get("build/resources/test/SaleToPOIResponse.json");

		String saleToPOIResponseString = new String(Files.readAllBytes(path.toAbsolutePath()), "UTF-8");
		assertTrue(saleToPOIResponseString.startsWith("{\"SaleToPOIResponse\":"));

		SaleToPOIResponse saleToPOIResponse = (SaleToPOIResponse) decoder.decode(saleToPOIResponseString);

		assertFalse(saleToPOIResponse == null);
		assertFalse(saleToPOIResponse.toJson().startsWith("{\"SaleToPOIResponse\":"));
	}

}
