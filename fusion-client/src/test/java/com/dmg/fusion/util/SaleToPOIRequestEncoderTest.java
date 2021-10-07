package com.dmg.fusion.util;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.websocket.EncodeException;

import org.junit.Before;
import org.junit.Test;

import au.com.dmg.fusion.MessageHeader;
import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.data.MessageClass;
import au.com.dmg.fusion.data.MessageType;
import au.com.dmg.fusion.data.SaleCapability;
import au.com.dmg.fusion.data.TerminalEnvironment;
import au.com.dmg.fusion.request.SaleTerminalData;
import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.request.loginrequest.LoginRequest;
import au.com.dmg.fusion.request.loginrequest.SaleSoftware;
import au.com.dmg.fusion.request.paymentrequest.carddata.KEKIdentifier;
import au.com.dmg.fusion.securitytrailer.AuthenticatedData;
import au.com.dmg.fusion.securitytrailer.EncapsulatedContent;
import au.com.dmg.fusion.securitytrailer.KEK;
import au.com.dmg.fusion.securitytrailer.KeyEncryptionAlgorithm;
import au.com.dmg.fusion.securitytrailer.MACAlgorithm;
import au.com.dmg.fusion.securitytrailer.Recipient;
import au.com.dmg.fusion.securitytrailer.SecurityTrailer;

public class SaleToPOIRequestEncoderTest {

	private SaleToPOIRequestEncoder encoder;

	@Before
	public void init() {
		this.encoder = new SaleToPOIRequestEncoder();
	}

	@Test
	public void testEncode() throws EncodeException {
		// Login Request
		SaleSoftware saleSoftware = new SaleSoftware.Builder()//
				.providerIdentification("BlackLabel")//
				.applicationName("BlackLabel")//
				.softwareVersion("1.0.0")//
				.certificationCode("CertificationCode")//
				.build();

		SaleTerminalData saleTerminalData = new SaleTerminalData.Builder()//
				.terminalEnvironment(TerminalEnvironment.Attended)//
				.saleCapabilities(Arrays.asList(SaleCapability.CashierStatus, SaleCapability.PrinterReceipt))//
				.build();

		LoginRequest request = new LoginRequest.Builder()//
				.dateTime("2021-09-16T07:28:24+08:00")//
				.saleSoftware(saleSoftware)//
				.saleTerminalData(saleTerminalData)//
				.operatorLanguage("en")//
				.build();

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

		SaleToPOIRequest saleToPOIRequest = new SaleToPOIRequest.Builder()//
				.messageHeader(messageHeader)//
				.request(request)//
				.securityTrailer(securityTrailer)//
				.build();

		String jsonRequest = encoder.encode(saleToPOIRequest);

		assertTrue(jsonRequest.startsWith("{\"SaleToPOIRequest\":"));
		assertTrue(jsonRequest.endsWith("}"));
	}

}
