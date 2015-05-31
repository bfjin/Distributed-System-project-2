/***
 * Subject                      Distributed System
 * Author: 						Bofan Jin, Fei Tang, Kimple Ke, Roger Li
 * Date of last modification: 	31/05/2015
 ***/

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

/**
 * Worker class is a representation under the master that does the
 * connection and communication with the actual worker on the cloud
 * */
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

	/**
	 * Connect to a given address and port number using SSL connection
	 */
	public void connect() {
		try {

			SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			Socket socket = sslSocketFactory.createSocket(address, port);
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
			running = false;
			System.err.println("Failed to establish connection");
			e.printStackTrace();
		}
	}

	/**
	 * Send a job to a worker
	 * 
	 * @param job
	 *            job to be send
	 */
	public void sendJob(Job job) {
		lock.lock();
		try {
			Util.send(out, "AddJob", job.getId(), job.getTimeLimit(),
					job.getMemoryLimit());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		job.setStatus(1);
		currentJob = job;
	}

	/**
	 * Communicate with the cloud workers
	 */
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
				
				try {
					Util.send(out, "Ready To Receive Result", job.getId());
					Util.receiveFile(in, resultFile);
					Util.send(out, "File Received", job.getId());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				lock.unlock();
				if (master.getJobTable() != null)
					master.getJobTable().updateTable();
			} else if (message.equals("Failed")) {
				Job job = master
						.findJobById(((JobInstruction) inst).getJobId());
				job.setStatus(3);
				File resultFile = job.getResultFile();
				lock.lock();

				try {
					Util.send(out, "Ready To Receive Result", job.getId());
					Util.receiveFile(in, resultFile);
					Util.send(out, "File Received", job.getId());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				lock.unlock();
				if (master.getJobTable() != null)
					master.getJobTable().updateTable();
			} else if (message.equals("Ready To Receive Runnable File")) {
				try {
					Util.sendFile(out, currentJob.getRunnableFile());
				} catch (IOException e) {
					System.err.println("Failed when sending file");
					e.printStackTrace();
				}
			} else if (message.equals("Ready To Receive Input File")) {
				try {
					Util.sendFile(out, currentJob.getInputFile());
				} catch (IOException e) {
					System.err.println("Failed when sending file");
					e.printStackTrace();
				}
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

	/**
	 * @return the workload of the worker
	 */
	public int getWorkLoad() {
		lock.lock();


		try {
			Util.send(out, "RequestWorkLoad");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return workload;
	}

	/**
	 * @return the status of the worker
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @return the worker address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
}
