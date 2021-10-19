package au.com.dmg.fusion.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

import com.squareup.moshi.JsonDataException;

import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.data.MessageType;

public class SaleToPOIStringTest {

	private String message;
	private String expectedMessageHeader = "{\"MessageClass\":\"Service\",\"MessageCategory\":\"Payment\",\"MessageType\":\"Response\",\"ServiceID\":\"Vbr9kR6UTj\",\"SaleID\":\"BlackLabelUAT1\",\"POIID\":\"BLBPOI01\"}";
	private String expectedBody = "{\"Response\":{\"Result\":\"Failure\",\"ErrorCondition\":\"Cancel\",\"AdditionalResponse\":\"User Cancelled\"},\"SaleData\":{\"OperatorLanguage\":\"en\",\"SaleTransactionID\":{\"TimeStamp\":\"2021-10-15T06:26:21.544Z\",\"TransactionID\":\"transactionID14:26:21+08:00\"}},\"POIData\":{\"POITransactionID\":{\"TransactionID\":\"61691f0dcd083c13f0e07524\",\"TimeStamp\":\"2021-10-15T17:26:52.728+11:00\"},\"POIReconciliationID\":\"6155595ccd6798510c3b9bab\"},\"PaymentResult\":{\"PaymentType\":\"Normal\",\"PaymentInstrumentData\":{\"PaymentInstrumentType\":\"Card\",\"CardData\":{\"EntryMode\":\"Tapped\",\"PaymentBrand\":\"Card\",\"MaskedPAN\":\"\"}},\"AmountsResp\":{\"Currency\":\"AUD\",\"AuthorizedAmount\":1000,\"TotalFeesAmount\":0,\"CashBackAmount\":0,\"SurchargeAmount\":0,\"TipAmount\":0},\"OnlineFlag\":true},\"PaymentReceipt\":[{\"DocumentQualifier\":\"SaleReceipt\",\"IntegratedPrintFlag\":true,\"RequiredSignatureFlag\":false,\"OutputContent\":{\"OutputFormat\":\"XHTML\",\"OutputXHTML\":\"PHAgaWQ9InJlY2VpcHQtaW5mbyI+MTUvMTAvMjAyMSAxNzoyNjo1Mjxici8+TWVyY2hhbnQgSUQ6IEJMQk1JRDAwMTxici8+VGVybWluYWwgSUQ6IEJMQjAwMDAxPC9wPjxwIGlkPSJyZWNlaXB0LWRldGFpbHMiPjxiPlB1cmNoYXNlIFRyYW5zYWN0aW9uPC9iPjxici8+QW1vdW50OiAkMTAwMC4wMDxici8+Q2FyZDogIChUKTxici8+Q3JlZGl0IEFjY291bnQ8L3A+PHAgaWQ9InJlY2VpcHQtcmVzdWx0Ij48Yj5DYW5jZWxsZWQ8L2I+PC9wPg==\"}}]}";

	@Before
	public void init() throws UnsupportedEncodingException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("SaleToPOIPaymentResponse.json").getFile());

		this.message = new String(Files.readAllBytes(file.toPath()), "UTF-8");
	}

	@Test
	public void testFromJson() throws JsonDataException, IOException {
		SaleToPOIString stps = SaleToPOIString.fromJson(message, MessageCategory.Payment, MessageType.Response);
		assertEquals(expectedMessageHeader, stps.getMessageHeader());
		assertEquals(expectedBody, stps.getBody());
	}

}
