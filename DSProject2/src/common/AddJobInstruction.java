/***
 * Subject                      Distributed System
 * Author: 						Bofan Jin, Fei Tang, Kimple Ke, Roger Li
 * Date of last modification: 	31/05/2015
 ***/

package common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * AddJobInstruction class is a child class of JobInstruction
 * */
public class AddJobInstruction extends JobInstruction {

	private int timeLimit;
	private int memoryLimit;

	public AddJobInstruction(String msg, String jobId, int timeLimit,
			int memorylimit) {
		super(msg, jobId);
		this.timeLimit = timeLimit;
		this.memoryLimit = memorylimit;
		this.type = "AddJobInstruction";
	}

	/**
	 * @return the timeLimit
	 */
	public int getTimeLimit() {
		return timeLimit;
	}

	/**
	 * @return the memoryLimit
	 */
	public int getMemoryLimit() {
		return memoryLimit;
	}

	@SuppressWarnings("unchecked")
	public String toJson() {
		JSONObject obj = new JSONObject();
		obj.put("Message", message);
		obj.put("JobId", jobId);
		obj.put("TimeLimit", timeLimit + "");
		obj.put("MemoryLimit", memoryLimit + "");
		obj.put("Type", type);
		return obj.toJSONString();
	}

	public static AddJobInstruction fromJson(String json) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(json);
			return new AddJobInstruction((String) obj.get("Message"),
					(String) obj.get("JobId"), Integer.parseInt((String) obj
							.get("TimeLimit")), Integer.parseInt((String) obj
							.get("MemoryLimit")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
