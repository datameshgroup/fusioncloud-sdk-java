package au.com.dmg.fusion.exception;

import java.nio.ByteBuffer;

import javax.websocket.DecodeException;

public class SecurityTrailerValidationException extends DecodeException {

	private static final long serialVersionUID = 1400750113630691261L;

	public SecurityTrailerValidationException(ByteBuffer bb, String message) {
		super(bb, message);
	}

	public SecurityTrailerValidationException(ByteBuffer bb, String message, Throwable cause) {
		super(bb, message, cause);
	}

	public SecurityTrailerValidationException(String encodedString, String message) {
		super(encodedString, message);
	}

	public SecurityTrailerValidationException(String encodedString, String message, Throwable cause) {
		super(encodedString, message, cause);
	}

}
