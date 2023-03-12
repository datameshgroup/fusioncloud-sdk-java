package au.com.dmg.fusion.util;

import java.security.SecureRandom;

public class MessageHeaderUtil {

	private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static SecureRandom random = new SecureRandom();

	public static String generateServiceID() {
		return generateServiceID(10);
	}

	public static String generateServiceID(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
		return sb.toString();
	}

}
