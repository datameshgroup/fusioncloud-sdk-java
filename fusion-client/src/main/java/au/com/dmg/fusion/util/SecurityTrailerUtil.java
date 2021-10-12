package au.com.dmg.fusion.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import au.com.dmg.fusion.config.KEKConfig;
import au.com.dmg.fusion.security.Crypto;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import au.com.dmg.fusion.MessageHeader;
import au.com.dmg.fusion.request.Request;
import au.com.dmg.fusion.request.paymentrequest.carddata.KEKIdentifier;
import au.com.dmg.fusion.securitytrailer.AuthenticatedData;
import au.com.dmg.fusion.securitytrailer.EncapsulatedContent;
import au.com.dmg.fusion.securitytrailer.KEK;
import au.com.dmg.fusion.securitytrailer.KeyEncryptionAlgorithm;
import au.com.dmg.fusion.securitytrailer.MACAlgorithm;
import au.com.dmg.fusion.securitytrailer.Recipient;
import au.com.dmg.fusion.securitytrailer.SecurityTrailer;

public class SecurityTrailerUtil {

	public static SecurityTrailer generateSecurityTrailer(MessageHeader messageHeader, Request request,
			String KEKString) throws InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
			IOException {

		// KEK encrypted key
		byte[] key = Crypto.generate16ByteKey();
		byte[] encryptedKey = Crypto.generateEncryptedKey(key, KEKString);
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

	private static String buildMACBody(MessageHeader messageHeader, Request request) {
		Moshi moshi = new Moshi.Builder().build();
		JsonAdapter<MessageHeader> jsonAdapter = moshi.adapter(MessageHeader.class);
		String macBody = String.format("\"MessageHeader\":%s,\"%sRequest\":%s", jsonAdapter.toJson(messageHeader),
				messageHeader.getMessageCategory(), request.toJson());

		return macBody;
	}

}
