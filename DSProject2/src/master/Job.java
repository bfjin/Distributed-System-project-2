package master;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

public class Job extends Thread {

	private String name;
	private File runnableFile;
	private File inputFile;
	private Worker worker;
	private int status;

	public Job(String name, File runnableFile, File inputFile) {
		setDaemon(true);
		this.name = name;
		this.runnableFile = runnableFile;
		this.inputFile = inputFile;
	}

	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		JSONObject obj = new JSONObject();
		obj.put("Name", name);
		obj.put("RunnableFile", fileToByteString(runnableFile));
		obj.put("InputFile", fileToByteString(inputFile));
		String data = obj.toJSONString();
		worker.send(data);		
		worker.receive();
	}

	public String fileToByteString(File file) {
		byte[] bytes = new byte[(int) file.length()];
		try {
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));
			bis.read(bytes);
			bis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return new String(Base64.encodeBase64(bytes), "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
