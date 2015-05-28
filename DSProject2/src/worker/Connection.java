package worker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;

import common.Instruction;
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

			String masterAddress = receiveSocket.getInetAddress()
					.getHostAddress();
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
		while (true) {
			Instruction inst = Util.receive(receiveIn);
			String message = inst.getMessage();
			if (message.equals("AddJob")) {
				addJob(inst);
			}
		}
	}

	private void addJob(Instruction inst) {
		String jobId = inst.getJobId();
		System.out.println("Job Id: " + jobId);
		String folderPath = "Execution\\" + jobId;
		new File(folderPath).mkdirs();
		folderPath += "\\";

		File runnableFile = Util.createFile("runnable.jar", folderPath);
		File inputFile = Util.createFile("input.txt", folderPath);
		File outputFile = Util.createFile("output.txt", folderPath);
		File errorFile = Util.createFile("error.txt", folderPath);

		Util.send(receiveOut, "Ready To Receive Runnable File");
		Util.receiveFile(receiveIn, runnableFile);

		Util.send(receiveOut, "Ready To Receive Input File");
		Util.receiveFile(receiveIn, inputFile);

		Thread doJobThrad = new Thread(() -> doJob(jobId, runnableFile,
				inputFile, outputFile, errorFile));
		doJobThrad.setDaemon(true);
		doJobThrad.start();
	}

	public void doJob(String jobId, File runnableFile, File inputFile,
			File outputFile, File errorFile) {

		String javaExePath = Paths
				.get(System.getProperty("java.home"), "bin", "java")
				.toAbsolutePath().toString();

		ProcessBuilder builder = new ProcessBuilder(javaExePath, "-jar",
				runnableFile.getPath(), inputFile.getPath(),
				outputFile.getPath());
		builder.redirectError(errorFile);
		try {
			Process p = builder.start();
			int exit = p.waitFor();

			// Handle Output File
			if (exit == 0){
				Util.send(sendOut, "Done", jobId);
				String reply = Util.receive(sendIn).getMessage();
				if (reply.equals("Ready To Receive Result")){
					Util.sendFile(sendOut, outputFile);
				}
			}
			else {
				Util.send(sendOut, "Failed", jobId);
				String reply = Util.receive(sendIn).getMessage();
				if (reply.equals("Ready To Receive Result")){
					Util.sendFile(sendOut, errorFile);
				}
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
