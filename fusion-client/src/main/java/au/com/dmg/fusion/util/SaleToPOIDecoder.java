package au.com.dmg.fusion.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.ConfigurationException;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.squareup.moshi.JsonDataException;

import au.com.dmg.fusion.Message;
import au.com.dmg.fusion.MessageHeader;
import au.com.dmg.fusion.SaleToPOI;
import au.com.dmg.fusion.exception.SecurityTrailerValidationException;

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
			throw new DecodeException(s, e.getMessage());
		}

		SaleToPOI messageWrapper = message.getRequest() != null ? message.getRequest()
				: message.getResponse() != null ? message.getResponse() : null;

		MessageHeader messageHeader = messageWrapper.getMessageHeader();
		if (messageHeader == null) {
			throw new SecurityTrailerValidationException(s, "Message header cannot be empty.");
		}

		try {
			SecurityTrailerUtil.validateSecurityTrailer(messageWrapper.getSecurityTrailer(),
					messageHeader.getMessageCategory(),
					messageHeader.getMessageType(), s);

		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
				| InvalidKeySpecException e) {

			LOGGER.log(Level.SEVERE, e.getMessage());
			throw new SecurityTrailerValidationException(s, e.getMessage());
		}

		return message.getRequest() == null ? message.getResponse() : message.getRequest();
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

}
