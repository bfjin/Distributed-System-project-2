package worker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import common.AddJobInstruction;
import common.Instruction;
import common.JobInstruction;
import common.Util;

class Connection extends Thread {
	public static ReentrantLock lock;
	public static Condition fileReceive;
	private DataInputStream in;
	private DataOutputStream out;	
	private Listener worker;
	private ArrayList<JobExecutor> jobExecutors;

	public Connection(Socket clientSocket, Listener worker) {
		setDaemon(true);
		this.worker = worker;
		jobExecutors = new ArrayList<JobExecutor>();
		lock = new ReentrantLock();
		fileReceive = lock.newCondition();
		try {
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
			String message = inst.getMessage();
			if (message.equals("AddJob")) {
				AddJobInstruction addJobInstruction = (AddJobInstruction) inst;
				worker.setWorkload(worker.getWorkload() + 1);
				addJob(addJobInstruction);
			} else if (message.equals("RequestWorkLoad")) {
				Util.send(out, "Current Workload: " + worker.getWorkload());
			} else if (message.equals("Ready To Receive Result")) {
				JobInstruction jobInstruction = (JobInstruction) inst;
				String jobId = jobInstruction.getJobId();
				JobExecutor jobExecutor = findJobExcutorById(jobId);
				jobExecutor.sendOutputFile();
			} else if (message.equals("File Received")) {
				fileReceive.signal();
			} else {
				System.out.println("Unexpected message:  " + message);
			}
		}
	}

	/**
	 * Find a running jobExcutor given a job id
	 * @param jobId job id
	 */
	private JobExecutor findJobExcutorById(String jobId) {
		for (JobExecutor jobExecutor : jobExecutors)
			if (jobExecutor.getJobId().equals(jobId))
				return jobExecutor;
		return null;
	}

	/**
	 * Receive all the required file to run the job and make a jobExecutor to
	 * do the job.
	 * @param inst job instruction
	 */
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
		
		lock.lock();
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
		
	}

}
