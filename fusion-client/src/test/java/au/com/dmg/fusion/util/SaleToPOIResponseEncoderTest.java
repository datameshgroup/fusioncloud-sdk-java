package au.com.dmg.fusion.util;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.websocket.EncodeException;

import au.com.dmg.fusion.util.SaleToPOIResponseEncoder;
import org.junit.Before;
import org.junit.Test;

import au.com.dmg.fusion.MessageHeader;
import au.com.dmg.fusion.data.InfoQualify;
import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.data.MessageClass;
import au.com.dmg.fusion.data.MessageType;
import au.com.dmg.fusion.request.paymentrequest.carddata.KEKIdentifier;
import au.com.dmg.fusion.response.DisplayResponse;
import au.com.dmg.fusion.response.OutputResult;
import au.com.dmg.fusion.response.SaleToPOIResponse;
import au.com.dmg.fusion.securitytrailer.AuthenticatedData;
import au.com.dmg.fusion.securitytrailer.EncapsulatedContent;
import au.com.dmg.fusion.securitytrailer.KEK;
import au.com.dmg.fusion.securitytrailer.KeyEncryptionAlgorithm;
import au.com.dmg.fusion.securitytrailer.MACAlgorithm;
import au.com.dmg.fusion.securitytrailer.Recipient;
import au.com.dmg.fusion.securitytrailer.SecurityTrailer;

public class SaleToPOIResponseEncoderTest {

	private SaleToPOIResponseEncoder encoder;

	@Before
	public void init() {
		this.encoder = new SaleToPOIResponseEncoder();
	}

	@Test
	public void testEncode() throws EncodeException {
		// Display Response
		OutputResult outputResult = new OutputResult("device", InfoQualify.Status, "Success", null, null);
		DisplayResponse response = new DisplayResponse(Arrays.asList(outputResult));

		MessageHeader messageHeader = new MessageHeader.Builder()//
				.protocolVersion("3.1-dmg")//
				.messageClass(MessageClass.Service)//
				.messageCategory(MessageCategory.Login)//
				.messageType(MessageType.Request)//
				.serviceID("serviceIDLogin")//
				.saleID("saleID")//
				.POIID("poiID")//
				.build();

		// Security Trailer
		KEK kek = new KEK("v4", //
				new KEKIdentifier("SpecV2TestMACKey", //
						"202109259072824.028"), //
				new KeyEncryptionAlgorithm("des-ede3-cbc"), //
				"encryptedHexKey");

		Recipient recipient = new Recipient(kek, //
				new MACAlgorithm("id-retail-cbc-mac-sha-256"), //
				new EncapsulatedContent("iddata"), //
				"mac");

		AuthenticatedData authenticatedData = new AuthenticatedData("v0", recipient);

		SecurityTrailer securityTrailer = new SecurityTrailer("id-ctauthData", authenticatedData);

		SaleToPOIResponse saleToPOIResponse = new SaleToPOIResponse.Builder()//
				.messageHeader(messageHeader)//
				.response(response)//
				.securityTrailer(securityTrailer)//
				.build();

		String jsonRequest = encoder.encode(saleToPOIResponse);

		assertTrue(jsonRequest.startsWith("{\"SaleToPOIResponse\":"));
		assertTrue(jsonRequest.endsWith("}"));
	}

}
