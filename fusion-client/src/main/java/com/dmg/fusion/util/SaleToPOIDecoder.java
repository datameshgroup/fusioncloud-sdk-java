package com.dmg.fusion.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.squareup.moshi.JsonDataException;

import au.com.dmg.fusion.Message;
import au.com.dmg.fusion.SaleToPOI;

public class SaleToPOIDecoder implements Decoder.Text<SaleToPOI> {

	private final static Logger LOGGER = Logger.getLogger(SaleToPOIDecoder.class.getName());

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public SaleToPOI decode(String s) throws DecodeException {
		LOGGER.info("Decoding response from server...");

		Message message = null;
		try {
			message = Message.fromJson(s);
		} catch (JsonDataException | IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}

		return message.getRequest() == null ? message.getResponse() : message.getRequest();
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

}
