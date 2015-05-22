package common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Instruction {

	private String message;
	private String jobId;

	public Instruction(String msg, String jobId) {
		this.message = msg;
		this.jobId = jobId;
	}

	public Instruction(String msg) {
		this(msg, null);
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the jobId
	 */
	public String getJobId() {
		return jobId;
	}

	@SuppressWarnings("unchecked")
	public String toJson() {
		JSONObject obj = new JSONObject();
		obj.put("Message", message);
		obj.put("JobId", jobId);
		return obj.toJSONString();
	}

	public static Instruction fromJson(String json) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(json);
			return new Instruction((String) obj.get("Message"),
					(String) obj.get("JobId"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
}
