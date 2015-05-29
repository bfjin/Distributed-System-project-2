package common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Instruction {

	protected String message;
	protected String type;

	public Instruction(String msg) {
		this.message = msg;
		this.type = "Instruction";
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
	public String toJson() {
		JSONObject obj = new JSONObject();
		obj.put("Message", message);
		obj.put("Type", type);
		return obj.toJSONString();
	}

	public static Instruction fromJson(String json) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(json);
			return new Instruction((String) obj.get("Message"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static String getTypefromJson(String json) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(json);
			return (String) obj.get("Type");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
