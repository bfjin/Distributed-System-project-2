package worker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLSocket;

import common.AddJobInstruction;
import common.Instruction;
import common.JobInstruction;
import common.Util;

class Connection extends Thread {
	private DataInputStream in;
	private DataOutputStream out;
	private ReentrantLock lock;
	private Listener worker;
	private ArrayList<JobExecutor> jobExecutors;

	public Connection(SSLSocket socket, Listener worker) {
		setDaemon(true);
		this.worker = worker;
		jobExecutors = new ArrayList<JobExecutor>();
		lock = new ReentrantLock();
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
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
			} else if (message.equals("Ready To Receive Result")) {
				JobInstruction jobInstruction = (JobInstruction) inst;
				String jobId = jobInstruction.getJobId();
				JobExecutor jobExecutor = findJobExcutorById(jobId);
				jobExecutor.sendFile();
			}
			else if (message.equals("File Received")) {
				// ToDO
			}
			else {
				System.out.println("Unexpected message:  " + message);
			}
		}
	}

	private JobExecutor findJobExcutorById(String jobId) {
		// TODO Auto-generated method stub
		return null;
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
		jobExecutors.add(jobExecutor);
		worker.setWorkload(worker.getWorkload() + 1);
	}

}
