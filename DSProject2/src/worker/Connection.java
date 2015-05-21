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
	private DataInputStream receiveIn;
	private DataOutputStream receiveOut;
	private Socket receiveSocket;
	
	private DataInputStream sendIn;
	private DataOutputStream sendOut;
	private Socket sendSocket;

	public Connection(Socket socket) {
		setDaemon(true);
		try {
			receiveSocket = socket;
			receiveIn = new DataInputStream(receiveSocket.getInputStream());
			receiveOut = new DataOutputStream(receiveSocket.getOutputStream());
			
			String masterAddress = receiveSocket.getInetAddress().getHostAddress();
			sendSocket = new Socket(masterAddress, 5555);
			sendIn = new DataInputStream(sendSocket.getInputStream());
			sendOut = new DataOutputStream(sendSocket.getOutputStream());			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				String data = Util.receive(receiveIn);
				if (data.equals("AddJob")) {
					String jobId = UUID.randomUUID().toString();
					System.out.println("Job Id: " + jobId);
					String folderPath = "Jobs\\" + jobId;
					new File(folderPath).mkdirs();
					folderPath += "\\";

					File runnableFile = new File(folderPath + "runnable.jar");
					runnableFile.createNewFile();
					File inputFile = new File(folderPath + "input.txt");
					inputFile.createNewFile();
					File outputFile = new File(folderPath + "output.txt");
					outputFile.createNewFile();

					Util.send(receiveOut, "Ready To Receive Runnable File");
					Util.receiveFile(receiveIn, runnableFile);

					Util.send(receiveOut, "Ready To Receive Input File");
					Util.receiveFile(receiveIn, inputFile);

					Thread doJobThrad = new Thread(() -> doJob(
							runnableFile.getPath(), inputFile.getPath(),
							outputFile.getPath()));
					doJobThrad.setDaemon(true);
					doJobThrad.start();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void doJob(String runnableFilePath, String inputFilePath,
			String outputFilePath) {
		String javaExePath = Paths
				.get(System.getProperty("java.home"), "bin", "java")
				.toAbsolutePath().toString();

		ProcessBuilder builder = new ProcessBuilder(javaExePath, "-jar",
				runnableFilePath, inputFilePath, outputFilePath);
		builder.redirectErrorStream(true);
		try {
			Process p = builder.start();
			int exit = p.waitFor();
			
			// Handle Output File
			if (exit == 0)
				Util.send(sendOut, "Done");
			else {
				Util.send(sendOut, "Failed");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
	}

}
