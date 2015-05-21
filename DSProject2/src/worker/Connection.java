package worker;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class Connection extends Thread {
	private DataInputStream in;
	private DataOutputStream out;
	private Socket clientSocket;
	private JSONParser parser;

	private static final String runnableFileName = "runnable.jar";
	private static final String inputFileName = "input.txt";
	private static final String outputFileName = "output.txt";

	public Connection(Socket socket) {
		setDaemon(true);
		parser = new JSONParser();
		try {
			clientSocket = socket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			String data = receive();
			JSONObject obj = null;

			try {
				obj = (JSONObject) parser.parse(data);
			} catch (ParseException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			if (obj != null) {
				String id = UUID.randomUUID().toString();

				File runnableFile = new File(id + "\\" + runnableFileName);
				runnableFile.createNewFile();
				bytesStringToFile(runnableFile,
						(String) obj.get("RunnableFile"));

				File inputFile = new File(id + "\\" + inputFileName);
				inputFile.createNewFile();
				bytesStringToFile(inputFile, (String) obj.get("InputFile"));

				File outputFile = new File(id + "\\" + outputFileName);
				outputFile.createNewFile();

				boolean success = doJob(runnableFile.getPath(),
						inputFile.getPath(), outputFile.getPath());
				// Handle Reply
				send(success + "");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean doJob(String runnableFilePath, String inputFilePath,
			String outputFilePath) {
		String javaExePath = Paths
				.get(System.getProperty("java.home"), "bin", "java")
				.toAbsolutePath().toString();

		ProcessBuilder builder = new ProcessBuilder(javaExePath, "-jar",
				runnableFilePath, inputFilePath, outputFilePath);
		builder.redirectErrorStream(true);
		builder.inheritIO();
		try {
			Process p = builder.start();
			p.waitFor();
			return p.exitValue() == 0;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return false;
	}

	private static File bytesStringToFile(File file, String string) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bos.write(Base64.decodeBase64(string));
			bos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	public void send(String data) {
		try {
			byte[] bytes = data.getBytes("utf-8");
			out.writeInt(bytes.length);
			out.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String receive() {
		try {
			int length = in.readInt();
			byte[] bytes = new byte[length];
			in.read(bytes);
			return new String(bytes, "utf-8").trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
