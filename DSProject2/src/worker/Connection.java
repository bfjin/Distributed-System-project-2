package worker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLSocket;

import common.AddJobInstruction;
import common.Instruction;
import common.Util;

class Connection extends Thread {
	private DataInputStream in;
	private DataOutputStream out;
	private ReentrantLock lock;
	private Listener worker;

	public Connection(Socket clientSocket, Listener worker) {
		setDaemon(true);
		this.worker = worker;
		lock = new ReentrantLock();
		try {
/*
			receiveSocket = socket;
			receiveIn = new DataInputStream(receiveSocket.getInputStream());
			receiveOut = new DataOutputStream(receiveSocket.getOutputStream());

			String masterAddress = receiveSocket.getInetAddress()
					.getHostAddress();

			System.out.println("masterAddress = " + masterAddress);
			
			//SSLSocketFactory sslSocketFactory = 
			//		(SSLSocketFactory) SSLSocketFactory.getDefault();
			//sendSocket = sslSocketFactory.createSocket(masterAddress, Util.masterSocket);
			sendSocket = new Socket(masterAddress, Util.masterSocket);

			
			sendIn = new DataInputStream(sendSocket.getInputStream());
			sendOut = new DataOutputStream(sendSocket.getOutputStream());
			*/

			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Failed to establish connection");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			while (lock.isLocked()) {
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Instruction inst = Util.receive(in);
			lock.lock();
			String message = inst.getMessage();
			if (message.equals("AddJob")) {
				AddJobInstruction addJobInstruction = (AddJobInstruction) inst;
				addJob(addJobInstruction);
			} else if (message.equals("RequestWorkLoad")) {
				Util.send(out, worker.getWorkload() + "");
				lock.unlock();
			}
			else {
				System.out.println("Unexpected message:  " + message);
			}
		}		
	}

	private void addJob(AddJobInstruction inst) {
		String jobId = inst.getJobId();
		System.out.println("Job Id: " + jobId);
		String folderPath = "Execution" + File.separator + jobId;
		new File(folderPath).mkdirs();
		folderPath += File.separator;

		File runnableFile = Util.createFile("runnable.jar", folderPath);
		File inputFile = Util.createFile("input.txt", folderPath);
		File outputFile = Util.createFile("output.txt", folderPath);
		File errorFile = Util.createFile("error.txt", folderPath);

		Util.send(out, "Ready To Receive Runnable File");
		Util.receiveFile(in, runnableFile);

		Util.send(out, "Ready To Receive Input File");
		Util.receiveFile(in, inputFile);

		Util.send(out, "File Received");
		lock.unlock();

		Thread doJobThrad = new Thread(() -> doJob(inst, runnableFile,
				inputFile, outputFile, errorFile));
		doJobThrad.setDaemon(true);
		doJobThrad.start();
		worker.setWorkload(worker.getWorkload() + 1);
	}

	public void doJob(AddJobInstruction inst, File runnableFile,
			File inputFile, File outputFile, File errorFile) {
		System.out.println("aaa");
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
			if (timeLimit != -1) {
				System.out.println("bbb2");
				finished = p.waitFor(timeLimit, TimeUnit.MILLISECONDS);
			} else {
				// This take like 1 minute or longer
				// BEAWARE
				System.out.println("bbb1");
				p.waitFor();
			}
			System.out.println("bbb0");
			lock.lock();
			interrupt();
			if (!finished || p.exitValue() != 0) {
				Util.send(out, "Failed", inst.getJobId());
				String reply = Util.receive(in).getMessage();
				if (reply.equals("Ready To Receive Result")) {
					Util.sendFile(out, errorFile);
				}
			} else {
				Util.send(out, "Done", inst.getJobId());
				String reply = Util.receive(in).getMessage();
				if (reply.equals("Ready To Receive Result")) {
					Util.sendFile(out, outputFile);
				}
			}
			String reply = Util.receive(in).getMessage();
			System.out.println("xxx");
			if (reply.equals("File Received")) {
				lock.unlock();
				worker.setWorkload(worker.getWorkload() - 1);
				System.out.println("zzz");
			}
			System.out.println("zzz11");
		} catch (InterruptedException e) {
			System.err.println("Job interrupted");
			e.printStackTrace();
		} catch (IOException e1) {
			System.err.println("Connection down");
			e1.printStackTrace();
		}

	}
}
