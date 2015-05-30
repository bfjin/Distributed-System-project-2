package master;

import gui.WorkerTable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;

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
	private Thread receiveThread;
	
	private Job currentJob;
	
	public int getWorkerID() {
		return workerID;
	}

	public Worker(Master master, String address, int port, WorkerTable workerTable) {
		this.master = master;
		this.address = address;
		this.port = port;
		this.workerTable = workerTable;
		lock = new ReentrantLock();
		connect();
		workerID = id;
		id ++;
		
		//receiveThread = new Thread(() -> receiveData());
		//receiveThread.setDaemon(true);
		//receiveThread.start();
	}

	public void connect() {
		try {

//			SSLSocketFactory sslSocketFactory = 
//					(SSLSocketFactory) SSLSocketFactory.getDefault();
//			Socket socket = sslSocketFactory.createSocket(address, port);
			@SuppressWarnings("resource")
			Socket socket = new Socket(address, port);

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
		lock.lock();
		Util.send(out, "AddJob", job.getId(), job.getTimeLimit(),
				job.getMemoryLimit());
		currentJob = job;	
	}
	
	public int getWorkLoad() {		
		lock.lock();
		Util.send(out, "RequestWorkLoad");
		String reply = Util.receive(in).getMessage();
		lock.unlock();
		return Integer.parseInt(reply);
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
			System.out.println("aaa");	
			Instruction inst = Util.receive(in);
			lock.lock();
			String message = inst.getMessage();
			System.out.println("eee");	
			if (message.equals("Done")) {
				System.out.println("bbb");	
				Job job = master
						.findJobById(((JobInstruction) inst).getJobId());
				job.setStatus(2);
				File resultFile = job.getResultFile();
				Util.send(out, "Ready To Receive Result", job.getId());
				Util.receiveFile(in, resultFile);
				Util.send(out, "File Received", job.getId());
				lock.unlock();
				System.out.println("ccc");	
			} else if (message.equals("Failed")) {				
				Job job = master
						.findJobById(((JobInstruction) inst).getJobId());
				job.setStatus(3);
				File resultFile = job.getResultFile();
				Util.send(out, "Ready To Receive Result", job.getId());
				Util.receiveFile(in, resultFile);
				Util.send(out, "File Received", job.getId());
				lock.unlock();
				System.out.println("ddd");	
			}
			else if (message.equals("Ready To Receive Runnable File")){
				Util.sendFile(out, currentJob.getRunnableFile());
			}
			else if (message.equals("Ready To Receive Input File")){
				Util.sendFile(out, currentJob.getRunnableFile());
			}
			else if (message.equals("File Received")){
				currentJob.setStatus(1);
				lock.unlock();
				currentJob = null;
			}
			else {
				System.out.println("Unexpected message:  " + message);
			}

		}
	}

}
