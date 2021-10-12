package au.com.dmg.fusion.util;

import java.util.logging.Logger;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import au.com.dmg.fusion.response.SaleToPOIResponse;

public class SaleToPOIResponseEncoder implements Encoder.Text<SaleToPOIResponse> {

	private final static Logger LOGGER = Logger.getLogger(SaleToPOIResponseEncoder.class.getName());

	@Override
	public void init(EndpointConfig ec) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public String encode(SaleToPOIResponse saleToPOIResponse) throws EncodeException {
		LOGGER.info("Encoding request to server...");

		return "{\"SaleToPOIResponse\":" + saleToPOIResponse.toJson() + "}";
	}

}
