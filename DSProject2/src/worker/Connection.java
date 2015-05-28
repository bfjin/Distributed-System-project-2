package worker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import common.AddJobInstruction;
import common.Instruction;
import common.Util;

class Connection extends Thread {
	private DataInputStream receiveIn;
	private DataOutputStream receiveOut;
	private Socket receiveSocket;

	private DataInputStream sendIn;
	private DataOutputStream sendOut;
	private Socket sendSocket;

	private ReentrantLock sendLock;
	private ReentrantLock receiveLock;

	public Connection(Socket socket) {
		setDaemon(true);
		sendLock = new ReentrantLock();
		receiveLock = new ReentrantLock();
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
			receiveLock.lock();
			String message = inst.getMessage();
			if (message.equals("AddJob")) {
				AddJobInstruction addJobInstruction = (AddJobInstruction) inst;
				addJob(addJobInstruction);
			}
			if (message.equals("RequestWorkLoad")) {
				Util.send(receiveOut, Listener.workload + "");
				receiveLock.unlock();
			}
		}
	}

	private void addJob(AddJobInstruction inst) {
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

		Util.send(receiveOut, "File Received");
		receiveLock.unlock();

		Thread doJobThrad = new Thread(() -> doJob(inst, runnableFile,
				inputFile, outputFile, errorFile));
		doJobThrad.setDaemon(true);
		doJobThrad.start();
		Listener.workload++;
	}

	public void doJob(AddJobInstruction inst, File runnableFile,
			File inputFile, File outputFile, File errorFile) {

		String javaExePath = Paths
				.get(System.getProperty("java.home"), "bin", "java")
				.toAbsolutePath().toString();
		int memoryLimit = inst.getMemoryLimit();
		ProcessBuilder builder = null;
		if (memoryLimit != -1) {
			String memoryLimitArg = "-xmx" + memoryLimit + "m";
			builder = new ProcessBuilder(javaExePath, "-jar",
					runnableFile.getPath(), inputFile.getPath(),
					outputFile.getPath(), memoryLimitArg);
		} else {
			builder = new ProcessBuilder(javaExePath, "-jar",
					runnableFile.getPath(), inputFile.getPath(),
					outputFile.getPath());
		}
		builder.redirectError(errorFile);
		try {
			Process p = builder.start();
			int timeLimit = inst.getTimeLimit();
			boolean finished = true;
			if (timeLimit != -1)
				finished = p.waitFor(timeLimit, TimeUnit.MILLISECONDS);
			else
				p.waitFor();

			sendLock.lock();
			if (!finished || p.exitValue() != 0) {
				Util.send(sendOut, "Failed", inst.getJobId());
				String reply = Util.receive(sendIn).getMessage();
				if (reply.equals("Ready To Receive Result"))
					Util.sendFile(sendOut, errorFile);
			} else {
				Util.send(sendOut, "Done", inst.getJobId());
				String reply = Util.receive(sendIn).getMessage();
				if (reply.equals("Ready To Receive Result"))
					Util.sendFile(sendOut, outputFile);
			}
			String reply = Util.receive(sendIn).getMessage();
			if (reply.equals("File Received")) {
				sendLock.unlock();
				Listener.workload--;
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
