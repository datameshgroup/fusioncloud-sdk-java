package au.com.dmg.fusion.util;

import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.response.SaleToPOIResponse;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.DecodeException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SaleToPOIDecoderTest {

	private SaleToPOIDecoder decoder;

	@Before
	public void init() throws UnsupportedEncodingException, IOException {
		this.decoder = new SaleToPOIDecoder();
	}

	@Test
	public void testDecodeRequest() throws DecodeException, UnsupportedEncodingException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("SaleToPOIRequest.json").getFile());

		String saleToPOIRequestString = new String(Files.readAllBytes(file.toPath()), "UTF-8");
		assertTrue(saleToPOIRequestString.startsWith("{\"SaleToPOIRequest\":"));

		SaleToPOIRequest saleToPOIRequest = (SaleToPOIRequest) decoder.decode(saleToPOIRequestString);

		assertFalse(saleToPOIRequest == null);
		assertFalse(saleToPOIRequest.toJson().startsWith("{\"SaleToPOIRequest\":"));
	}

	@Test
	public void testDecodeResponse() throws DecodeException, UnsupportedEncodingException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("SaleToPOIResponse.json").getFile());

		String saleToPOIResponseString = new String(Files.readAllBytes(file.toPath()), "UTF-8");
		assertTrue(saleToPOIResponseString.startsWith("{\"SaleToPOIResponse\":"));

		SaleToPOIResponse saleToPOIResponse = (SaleToPOIResponse) decoder.decode(saleToPOIResponseString);

		assertFalse(saleToPOIResponse == null);
		assertFalse(saleToPOIResponse.toJson().startsWith("{\"SaleToPOIResponse\":"));
	}

}
