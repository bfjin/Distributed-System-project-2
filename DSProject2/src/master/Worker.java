package master;

import gui.WorkerTable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLSocketFactory;

import common.Instruction;
import common.JobInstruction;
import common.Util;

public class Worker {

	private Master master;
	private String address;
	private int port;
	private boolean running;

	private DataInputStream in;
	private DataOutputStream out;

	private ReentrantLock lock;

	private static int id = 1;
	private int workerID;

	private WorkerTable workerTable;

	private Job currentJob;
	
	private int workload;

	public int getWorkerID() {
		return workerID;
	}

	public Worker(Master master, String address, int port,
			WorkerTable workerTable) {
		this.master = master;
		this.address = address;
		this.port = port;
		this.workerTable = workerTable;
		lock = new ReentrantLock();
		connect();
		workerID = id;
		id++;

		Thread receiveThread = new Thread(() -> receiveData());
		receiveThread.setDaemon(true);
		receiveThread.start();
	}

	public void connect() {
		try {

			SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			Socket socket = sslSocketFactory.createSocket(address, port);
			// @SuppressWarnings("resource")
			// Socket socket = new Socket(address, port);

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			running = true;
			if (workerTable != null) {
				workerTable.updateTable();
			}
		} catch (UnknownHostException e) {
			System.err.println("Worker not found");
			running = false;
			if (workerTable != null) {
				workerTable.updateTable();
			}
		} catch (IOException e) {
			System.err.println("Failed to establish connection");
			e.printStackTrace();
		}
	}

	public void sendJob(Job job) {
		System.err.println("Worker locked");
		lock.lock();
		Util.send(out, "AddJob", job.getId(), job.getTimeLimit(),
				job.getMemoryLimit());
		job.setStatus(1);
		currentJob = job;
	}

	public int getWorkLoad() {
		lock.lock();
		Util.send(out, "RequestWorkLoad");
		while (lock.isLocked()) {
			//Wait until lock is unlocked
		}
		return workload;
	}

	public boolean isRunning() {
		return running;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	private void receiveData() {
		while (true) {
			Instruction inst = Util.receive(in);
			String message = inst.getMessage();
			if (message.equals("Done")) {
				Job job = master
						.findJobById(((JobInstruction) inst).getJobId());
				job.setStatus(2);
				File resultFile = job.getResultFile();
				lock.lock();
				Util.send(out, "Ready To Receive Result", job.getId());
				Util.receiveFile(in, resultFile);
				Util.send(out, "File Received", job.getId());
				lock.unlock();
				if (master.getJobTable() != null)
					master.getJobTable().updateTable();
			} else if (message.equals("Failed")) {
				Job job = master
						.findJobById(((JobInstruction) inst).getJobId());
				job.setStatus(3);
				File resultFile = job.getResultFile();
				lock.lock();
				Util.send(out, "Ready To Receive Result", job.getId());
				Util.receiveFile(in, resultFile);
				Util.send(out, "File Received", job.getId());
				System.err.println("Unlocked");
				lock.unlock();
				if (master.getJobTable() != null)
					master.getJobTable().updateTable();
			} else if (message.equals("Ready To Receive Runnable File")) {
				Util.sendFile(out, currentJob.getRunnableFile());
			} else if (message.equals("Ready To Receive Input File")) {
				Util.sendFile(out, currentJob.getInputFile());
			} else if (message.equals("File Received")) {
				lock = new ReentrantLock();
				currentJob = null;
			} else if (message.startsWith("Current Workload: ")) {
				workload = Integer.parseInt(message.substring(18));
				lock = new ReentrantLock();
			} else {
				System.err.println("Unexpected message:  " + message);
			}
		}
	}

}
