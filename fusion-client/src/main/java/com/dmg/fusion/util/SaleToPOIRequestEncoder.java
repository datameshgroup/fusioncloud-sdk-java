package com.dmg.fusion.util;

import java.util.logging.Logger;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import au.com.dmg.fusion.request.SaleToPOIRequest;

public class SaleToPOIRequestEncoder implements Encoder.Text<SaleToPOIRequest> {

	private final static Logger LOGGER = Logger.getLogger(SaleToPOIRequestEncoder.class.getName());

	@Override
	public void init(EndpointConfig ec) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public String encode(SaleToPOIRequest saleToPOIRequest) throws EncodeException {
		LOGGER.info("Encoding request to server...");

		return "{\"SaleToPOIRequest\":" + saleToPOIRequest.toJson() + "}";
	}

}
