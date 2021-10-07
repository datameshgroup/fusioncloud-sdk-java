package com.dmg.fusion.exception;

public class NotConnectedException extends RuntimeException {

	private static final long serialVersionUID = 3906348029921710950L;

	public NotConnectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotConnectedException(String message) {
		super(message);
	}

	public NotConnectedException(Throwable cause) {
		super(cause);
	}

}
