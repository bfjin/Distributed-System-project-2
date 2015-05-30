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
			Instruction inst = Util.receive(in);
			lock.lock();
			String message = inst.getMessage();
			if (message.equals("AddJob")) {
				AddJobInstruction addJobInstruction = (AddJobInstruction) inst;
				addJob(addJobInstruction);
			} else if (message.equals("RequestWorkLoad")) {
				Util.send(out, worker.getWorkload() + "");
				lock.unlock();
			} else {
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

		JobExecutor jobExecutor = new JobExecutor(out, inst, runnableFile,
				inputFile, outputFile, errorFile, lock);
		jobExecutor.start();

		worker.setWorkload(worker.getWorkload() + 1);
	}

}
