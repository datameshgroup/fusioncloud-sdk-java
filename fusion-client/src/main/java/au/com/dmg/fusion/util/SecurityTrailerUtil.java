package au.com.dmg.fusion.util;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import au.com.dmg.fusion.MessageHeader;
import au.com.dmg.fusion.config.KEKConfig;
import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.data.MessageType;
import au.com.dmg.fusion.exception.SecurityTrailerValidationException;
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

	public static SecurityTrailer generateSecurityTrailer(MessageHeader messageHeader, Request request, String KEK)
			throws InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
			IOException {

		// KEK encrypted key
		byte[] key = Crypto.generate16ByteKey();
		byte[] encryptedKey = Crypto.generateEncryptedKey(key, KEK);
		String encryptedHexKey = Crypto.byteArrayToHexString(encryptedKey).toUpperCase();

		// MAC
		String macBody = buildMACBody(messageHeader, request);
		String hexKey = Crypto.byteArrayToHexString(key);
		String MAC = Crypto.generateMAC(macBody, hexKey).toUpperCase();

		// Security Trailer
		KEK kek = new KEK("v4", //
				new KEKIdentifier(KEKConfig.getInstance().getKeyIdentifier(), KEKConfig.getInstance().getKeyVersion()), //
				new KeyEncryptionAlgorithm("des-ede3-cbc"), //
				encryptedHexKey);

		Recipient recipient = new Recipient(kek, //
				new MACAlgorithm("id-retail-cbc-mac-sha-256"), //
				new EncapsulatedContent("iddata"), //
				MAC);

		AuthenticatedData authenticatedData = new AuthenticatedData("v0", recipient);

		SecurityTrailer securityTrailer = new SecurityTrailer("id-ctauthData", authenticatedData);

		return securityTrailer;
	}

	public static void validateSecurityTrailer(SecurityTrailer securityTrailer, String KEK,
			MessageCategory messageCategory, MessageType messageType, String messageStr) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException,
			IllegalBlockSizeException, IOException, InvalidKeySpecException, SecurityTrailerValidationException {

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

		// Extract message header and body from message string
		Map<String, Object> map = createMapFromMessageString(messageStr, messageType);
		String messageHeader = mapToJson(String.class, Object.class,
				Collections.singletonMap("MessageHeader", map.get("MessageHeader")));
		String messageBody = mapToJson(String.class, Object.class,
				Collections.singletonMap(String.format("%s%s", messageCategory, messageType),
						map.get(String.format("%s%s", messageCategory, messageType))));

		// Generate MAC based on key and request/response body in the payload
		String hexKey = Crypto.byteArrayToHexString(key);
		String MAC = Crypto.generateMAC(String.format("%s,%s", messageHeader.substring(1, messageHeader.length() - 1),
				messageBody.substring(1, messageBody.length() - 1)), hexKey).toUpperCase();

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

	private static Map<String, Object> createMapFromMessageString(String messageStr, MessageType messageType)
			throws IOException {
		Map<String, Object> map = null;

		Moshi moshi = new Moshi.Builder().build();
		Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
		JsonAdapter<Map<String, Object>> adapter = moshi.adapter(type);

		if (messageType == MessageType.Request) {
			map = adapter.fromJson(messageStr.substring("\"SaleToPOIRequest\": ".length(), messageStr.length() - 1));
		} else if (messageType == MessageType.Response) {
			map = adapter.fromJson(messageStr.substring("\"SaleToPOIResponse\": ".length(), messageStr.length() - 1));
		}

		return map;
	}

	private static <K, V> String mapToJson(Class<K> key, Class<V> value, Object json) {
		Moshi moshi = new Moshi.Builder().build();
		return moshi.adapter(Types.newParameterizedType(Map.class, key, value)).toJson(json);
	}

}
