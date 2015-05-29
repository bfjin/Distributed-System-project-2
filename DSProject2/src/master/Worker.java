package master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import common.Instruction;
import common.JobInstruction;
import common.Util;

public class Worker {

	private Master master;
	private String address;
	private int port;
	private boolean running;

	private SSLSocket sendSocket;
	private DataInputStream sendIn;
	private DataOutputStream sendOut;

	private Socket receiveSocket;
	private DataInputStream receiveIn;
	private DataOutputStream receiveOut;
	
	private ReentrantLock sendLock;
	private ReentrantLock receiveLock;

	private static int id = 1;
	private int workerID;
	
	public int getWorkerID() {
		return workerID;
	}

	public Worker(Master master, String address, int port) {
		this.master = master;
		this.address = address;
		this.port = port;
		sendLock = new ReentrantLock();
		receiveLock = new ReentrantLock();
		connect();
		workerID = id;
		id ++;
	}

	public void connect() {
		try {
			java.lang.System.setProperty("javax.net.ssl.trustStore", "certif");
			java.lang.System.setProperty("javax.net.ssl.trustStorePassword", "123456");			
			SSLSocketFactory sslSocketFactory = 
					(SSLSocketFactory) SSLSocketFactory.getDefault();
			sendSocket = 
					(SSLSocket) sslSocketFactory.createSocket(address, port);
			sendIn = new DataInputStream(sendSocket.getInputStream());
			sendOut = new DataOutputStream(sendSocket.getOutputStream());
			running = true;
		} catch (UnknownHostException e) {
			System.err.println("Worker not found");
			running = false;
		} catch (IOException e) {
			System.err.println("Failed to establish connection");
			e.printStackTrace();
		}
	}

	public void sendJob(Job job) {
		sendLock.lock();
		Util.send(sendOut, "AddJob", job.getId(), job.getTimeLimit(),
				job.getMemoryLimit());
		String reply = Util.receive(sendIn).getMessage();
		if (reply.equals("Ready To Receive Runnable File")){
			Util.sendFile(sendOut, job.getRunnableFile());
		}
		reply = Util.receive(sendIn).getMessage();
		if (reply.equals("Ready To Receive Input File")){
			Util.sendFile(sendOut, job.getInputFile());	
		}
		reply = Util.receive(sendIn).getMessage();
		if (reply.equals("File Received")){
			job.setStatus(1);
			sendLock.unlock();
		}		
	}
	
	public int getWorkLoad() {
		Util.send(sendOut, "RequestWorkLoad");
		String reply = Util.receive(sendIn).getMessage();
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

	public Socket getReceiveSocket() {
		return receiveSocket;
	}

	public void setReceiveSocket(Socket receiveSocket) {
		this.receiveSocket = receiveSocket;
		try {
			receiveIn = new DataInputStream(receiveSocket.getInputStream());
			receiveOut = new DataOutputStream(receiveSocket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Failed to create Data stream");
			e.printStackTrace();
		}
		Thread receiveThread = new Thread(() -> receiveData());
		receiveThread.setDaemon(true);
		receiveThread.start();
	}

	private void receiveData() {
		while (true) {
			System.out.println("aaa");	
			Instruction inst = Util.receive(receiveIn);
			receiveLock.lock();
			String message = inst.getMessage();
			System.out.println("eee");	
			if (message.equals("Done")) {
				System.out.println("bbb");	
				Job job = master
						.findJobById(((JobInstruction) inst).getJobId());
				job.setStatus(2);
				File resultFile = job.getResultFile();
				Util.send(receiveOut, "Ready To Receive Result");
				Util.receiveFile(receiveIn, resultFile);
				Util.send(receiveOut, "File Received");
				receiveLock.unlock();
				System.out.println("ccc");	
				try {
					java.awt.Desktop.getDesktop().edit(resultFile);
				} catch (IOException e) {
					System.err.println("Failed to create result file");
					e.printStackTrace();
				}
			} else if (message.equals("Failed")) {				
				Job job = master
						.findJobById(((JobInstruction) inst).getJobId());
				job.setStatus(3);
				File resultFile = job.getResultFile();
				Util.send(receiveOut, "Ready To Receive Result");
				Util.receiveFile(receiveIn, resultFile);
				Util.send(receiveOut, "File Received");
				receiveLock.unlock();
				System.out.println("ddd");	
				try {
					java.awt.Desktop.getDesktop().edit(resultFile);
				} catch (IOException e) {
					System.err.println("Failed to create result file");
					e.printStackTrace();
				}
			}

		}
	}

}
