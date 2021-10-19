package au.com.dmg.fusion.util;

import java.util.function.Predicate;

public class Util {

	private static Predicate<String> isNullEmptyBlank = s -> s == null || s.isEmpty() || s.trim().isEmpty();

	public static boolean isStringNullEmptyBlank(String s) {
		return isNullEmptyBlank.test(s);
	}

}
