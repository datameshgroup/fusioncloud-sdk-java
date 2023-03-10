package au.com.dmg.fusion.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.ConfigurationException;

import au.com.dmg.fusion.exception.FusionException;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;

import au.com.dmg.fusion.MessageHeader;
import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.data.MessageType;
import au.com.dmg.fusion.exception.SecurityTrailerValidationException;
import au.com.dmg.fusion.model.SaleToPOIString;
import au.com.dmg.fusion.request.Request;
import au.com.dmg.fusion.request.paymentrequest.carddata.KEKIdentifier;
import au.com.dmg.fusion.security.Crypto;
import au.com.dmg.fusion.securitytrailer.AuthenticatedData;
import au.com.dmg.fusion.securitytrailer.EncapsulatedContent;
import au.com.dmg.fusion.securitytrailer.KEK;
import au.com.dmg.fusion.securitytrailer.KeyEncryptionAlgorithm;
import au.com.dmg.fusion.securitytrailer.MACAlgorithm;
import au.com.dmg.fusion.securitytrailer.Recipient;
import au.com.dmg.fusion.securitytrailer.SecurityTrailer;

public class SecurityTrailerUtil {

	public static String KEK = null;

	public static SecurityTrailer generateSecurityTrailer(MessageHeader messageHeader, Request request, boolean useTestKeyIdentifier) throws FusionException{
		SecurityTrailer securityTrailer = null;
		try {
			// KEK encrypted key
			byte[] key = Crypto.generate16ByteKey();
			byte[] encryptedKey = Crypto.generateEncryptedKey(key, KEK);
			String encryptedHexKey = Crypto.byteArrayToHexString(encryptedKey).toUpperCase();

			// MAC
			String macBody = buildMACBody(messageHeader, request);
			String hexKey = Crypto.byteArrayToHexString(key);
			String MAC = Crypto.generateMAC(macBody, hexKey).toUpperCase();

			//KeyIdentifier = useTestKeyIdentifier ? "SpecV2TestMACKey" : "SpecV2ProdMACKey",
			String keyIdentifier;
			if(useTestKeyIdentifier)
			{
				keyIdentifier = "SpecV2TestMACKey";
			}
			else
			{
				keyIdentifier = "SpecV2ProdMACKey";
			}

			//KeyVersion = useTestKeyIdentifier ? "20191122164326.594" : "20191122164326.594",
			String keyVersion = "20191122164326.594";

			// Security Trailer
			KEK kek = new KEK("v4", //
					new KEKIdentifier(keyIdentifier, keyVersion), //
					new KeyEncryptionAlgorithm("des-ede3-cbc"), //
					encryptedHexKey);

			Recipient recipient = new Recipient(kek, //
					new MACAlgorithm("id-retail-cbc-mac-sha-256"), //
					new EncapsulatedContent("iddata"), //
					MAC);

			AuthenticatedData authenticatedData = new AuthenticatedData("v0", recipient);

			securityTrailer = new SecurityTrailer("id-ctauthData", authenticatedData);
		}
		catch (Exception ex)
		{
			throw new FusionException("An error occurred while generating a Security Trailer: " + ex.getMessage(), false);
		}
		return securityTrailer;
	}

	public static void validateSecurityTrailer(SecurityTrailer securityTrailer,
			MessageCategory messageCategory, MessageType messageType, String messageStr)
			throws SecurityTrailerValidationException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException,
			JsonDataException, IOException, InvalidKeySpecException {

		if (securityTrailer == null || securityTrailer.getAuthenticatedData() == null
				|| securityTrailer.getAuthenticatedData().getRecipient() == null
				|| securityTrailer.getAuthenticatedData().getRecipient().getKek() == null) {
			throw new SecurityTrailerValidationException(messageStr,
					"The following fields are necessary to perform MAC validation: 'AuthenticatedData', 'Recipient', 'KEK'");
		}

		// Decrypt key
		byte[] encryptedKey = Crypto
				.hexStringToByteArray(securityTrailer.getAuthenticatedData().getRecipient().getKek().getEncryptedKey());
		byte[] key = Crypto.crypt(Cipher.DECRYPT_MODE, encryptedKey, KEK);

		SaleToPOIString stps = null;
		if (messageType == MessageType.Request) {
			stps = SaleToPOIString.fromJson(
					messageStr.substring("\"SaleToPOIRequest\": ".length(), messageStr.length() - 1), messageCategory,
					messageType);
		} else if ((messageType == MessageType.Response) || (messageType == MessageType.Notification)) {
			stps = SaleToPOIString.fromJson(
					messageStr.substring("\"SaleToPOIResponse\": ".length(), messageStr.length() - 1), messageCategory,
					messageType);
		}

		// Generate MAC based on key and request/response body in the payload
		String hexKey = Crypto.byteArrayToHexString(key);
		String MAC = Crypto.generateMAC(String.format("\"MessageHeader\":%s,\"%s%s\":%s", stps.getMessageHeader(),
				messageCategory, messageType, stps.getBody()), hexKey).toUpperCase();

		// Encrypt key
		byte[] newEncryptedKey = Crypto.generateEncryptedKey(key, KEK);
		String encryptedHexKey = Crypto.byteArrayToHexString(newEncryptedKey).toUpperCase();

		if (!MAC.equalsIgnoreCase(securityTrailer.getAuthenticatedData().getRecipient().getMac())) {
			throw new SecurityTrailerValidationException(messageStr, "MAC Validation Error: expected "
					+ securityTrailer.getAuthenticatedData().getRecipient().getMac() + " , got " + MAC);
		}

		if (!encryptedHexKey
				.equalsIgnoreCase(securityTrailer.getAuthenticatedData().getRecipient().getKek().getEncryptedKey())) {
			throw new SecurityTrailerValidationException(messageStr,
					"Encrypted Key Validation Error: expected "
							+ securityTrailer.getAuthenticatedData().getRecipient().getKek().getEncryptedKey()
							+ " , got " + encryptedHexKey);
		}
	}

	private static String buildMACBody(MessageHeader messageHeader, Request request) {
		Moshi moshi = new Moshi.Builder().build();
		JsonAdapter<MessageHeader> jsonAdapter = moshi.adapter(MessageHeader.class);
		String macBody = String.format("\"MessageHeader\":%s,\"%sRequest\":%s", jsonAdapter.toJson(messageHeader),
				messageHeader.getMessageCategory(), request.toJson());

		return macBody;
	}

}
