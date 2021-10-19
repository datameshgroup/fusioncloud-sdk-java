package au.com.dmg.fusion.model;

import java.io.IOException;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonReader.Token;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;

import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.data.MessageType;

public class SaleToPOIString {

	private String messageHeader;
	private String body;
	private String messageType;

	public String getMessageHeader() {
		return messageHeader;
	}

	public String getBody() {
		return body;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageHeader(String messageHeader) {
		this.messageHeader = messageHeader;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public static SaleToPOIString fromJson(String jsonString, MessageCategory messageCategory, MessageType messageType)
			throws IOException, JsonDataException {
		Moshi moshi = new Moshi.Builder().add(new SaleToPOIStringAdapter(messageCategory, messageType)).build();

		JsonAdapter<SaleToPOIString> jsonAdapter = moshi.adapter(SaleToPOIString.class);
		return jsonAdapter.fromJson(jsonString);
	}

	@Override
	public String toString() {
		return "SaleToPOIString [messageHeader=" + messageHeader + ", body=" + body + ", messageType=" + messageType
				+ "]";
	}

}

class SaleToPOIStringAdapter extends JsonAdapter<SaleToPOIString> {

	private MessageCategory messageCategory;
	private MessageType messageType;

	public SaleToPOIStringAdapter(MessageCategory messageCategory, MessageType messageType) {
		super();
		this.messageCategory = messageCategory;
		this.messageType = messageType;
	}

	@FromJson
	@Override
	public SaleToPOIString fromJson(JsonReader reader) throws IOException {
		SaleToPOIString saleToPOIString = new SaleToPOIString();

		while (reader.hasNext()) {
			Token token = reader.peek();
			if (token == Token.BEGIN_OBJECT) {
				reader.beginObject();
				while (reader.hasNext()) {
					String fieldName = reader.nextName();
					if (fieldName.equals("MessageHeader")) {
						saleToPOIString.setMessageHeader(reader.nextSource().readUtf8());
					} else if (fieldName.equals(String.format("%s%s", messageCategory, messageType))) {
						saleToPOIString.setBody(reader.nextSource().readUtf8());
					} else {
						reader.skipValue();
					}
				}

			}
			reader.endObject();
		}

		return saleToPOIString;
	}

	@ToJson
	@Override
	public void toJson(JsonWriter writer, SaleToPOIString value) throws IOException {
		Moshi moshi = new Moshi.Builder().build();
		JsonAdapter<SaleToPOIString> jsonAdapter = moshi.adapter(SaleToPOIString.class);

		writer.value(jsonAdapter.toJson(value));
	}
}
