package au.com.dmg.fusion.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

	private Crypto() {
	}

	private static final String ENCODING = "UTF-8";

	private static final String SECRET_KEY_ALGORITHM = "DESede";

	private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256";

	private static final String CIPHER_MODE = "DESede/CBC/NoPadding";

	public static byte[] generate16ByteKey() {
		final SecureRandom random = new SecureRandom();
		final byte[] randomKey = new byte[16];
		random.nextBytes(randomKey);
		return randomKey;
	}

	public static byte[] generateEncryptedKey(byte[] randomKey, String masterKey)
			throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

		return encrypt(randomKey, masterKey);
	}

	public static byte[] encrypt(byte[] value, String key) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {

		byte[] keyBytes = hexStringToByteArray(key);

		// add 8 bytes to satisfy key length requirement
		if (keyBytes.length == 16) {
			byte[] tmpKey = new byte[24];
			System.arraycopy(keyBytes, 0, tmpKey, 0, 16);
			System.arraycopy(keyBytes, 0, tmpKey, 16, 8);
			keyBytes = tmpKey;
		}

		IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[8]);
		SecretKey secretKey = new SecretKeySpec(keyBytes, SECRET_KEY_ALGORITHM);
		Cipher cipherEncrpyt = Cipher.getInstance(CIPHER_MODE);
		cipherEncrpyt.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

		return cipherEncrpyt.doFinal(value);
	}

	public static String generateMAC(String request, String key) throws UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

		byte[] hashResults = getSHA(request);
		byte[] hashAppendedBytes = append8Bytes(hashResults);
		byte[] encrypt = encrypt(hashAppendedBytes, key);
		byte[] last8bytes = getLast8Bytes(encrypt);

		return byteArrayToHexString(last8bytes);
	}

	private static byte[] getSHA(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM).digest(input.getBytes(ENCODING));
	}

	private static byte[] append8Bytes(byte[] hash) {
		byte[] resultArray = new byte[hash.length + 8];
		System.arraycopy(hash, 0, resultArray, 0, hash.length);
		resultArray[32] = (byte) ((Character.digit('8', 16) << 4) + Character.digit('0', 16));

		for (int i = 33; i < 40; i++) {
			resultArray[i] = Byte.parseByte("00");
		}

		return resultArray;
	}

	private static byte[] getLast8Bytes(byte[] encrypt) {
		byte[] byteArray = new byte[8];

		System.arraycopy(encrypt, encrypt.length - 8, byteArray, 0, 8);

		return byteArray;
	}

	public static String byteArrayToHexString(final byte[] byteArray) {
		final StringBuilder hex = new StringBuilder(byteArray.length * 2);

		for (final byte b : byteArray) {
			hex.append(String.format("%02x", 0xff & b));
		}

		return hex.toString();
	}

	public static byte[] hexStringToByteArray(String hex) {
		byte[] byteArray = new byte[hex.length() / 2];

		for (int i = 0; i < byteArray.length; i++) {
			int index = i * 2;
			int dataBytes = Integer.parseInt(hex.substring(index, index + 2), 16) & 0xff;
			byteArray[i] = (byte) dataBytes;
		}

		return byteArray;
	}
}
