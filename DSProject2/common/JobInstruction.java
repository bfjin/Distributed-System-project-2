package common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JobInstruction extends Instruction {

	protected String jobId;

	public JobInstruction(String msg, String jobId) {
		super(msg);
		this.jobId = jobId;
		this.type = "JobInstruction";
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
		obj.put("Type", type);
		return obj.toJSONString();
	}
	
	public static JobInstruction fromJson(String json) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(json);
			return new JobInstruction((String) obj.get("Message"),
					(String) obj.get("JobId"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
