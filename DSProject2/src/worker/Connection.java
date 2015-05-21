package worker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.UUID;

import common.Util;

class Connection extends Thread {
	private DataInputStream in;
	private DataOutputStream out;
	private Socket clientSocket;

	private static final String runnableFileName = "runnable.jar";
	private static final String inputFileName = "input.txt";
	private static final String outputFileName = "output.txt";

	public Connection(Socket socket) {
		setDaemon(true);
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
			String data = Util.receive(in);			
			if (data.equals("AddJob"))
			{
				String jobId = UUID.randomUUID().toString();
				System.out.println("Job Id: " + jobId);
				String folderPath = "Jobs\\" + jobId;
				new File(folderPath).mkdirs();
				folderPath += "\\";
				
				File runnableFile = new File(folderPath + runnableFileName);
				runnableFile.createNewFile();
				File inputFile = new File(folderPath + inputFileName);
				inputFile.createNewFile();	
				File outputFile = new File(folderPath + outputFileName);
				outputFile.createNewFile();
				
				Util.send(out, "Ready To Receive Runnable File");
				Util.receiveFile(in, runnableFile);
				
				Util.send(out, "Ready To Receive Input File");
				Util.receiveFile(in, inputFile);

				boolean success = doJob(runnableFile.getPath(),
						inputFile.getPath(), outputFile.getPath());				
				
				if (success)
					Util.send(out, "Done");
				else
					Util.send(out, "Failed");
				// Handle Output File
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
			return p.waitFor() == 0;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return false;
	}

}
