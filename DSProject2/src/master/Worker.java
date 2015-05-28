package master;

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
	// private SSLSocket socket;
	private Socket sendSocket;
	private DataInputStream sendIn;
	private DataOutputStream sendOut;

	private Socket receiveSocket;
	private DataInputStream receiveIn;
	private DataOutputStream receiveOut;
	
	private ReentrantLock sendLock;
	private ReentrantLock receiveLock;

	public Worker(Master master, String address, int port) {
		this.master = master;
		this.address = address;
		this.port = port;
		sendLock = new ReentrantLock();
		receiveLock = new ReentrantLock();
		connect();
	}

	public void connect() {
		try {
			// SSLSocketFactory sslsocketfactory = (SSLSocketFactory)
			// SSLSocketFactory
			// .getDefault();
			// socket = (SSLSocket) sslsocketfactory.createSocket(address,
			// port);
			sendSocket = new Socket(address, port);
			sendIn = new DataInputStream(sendSocket.getInputStream());
			sendOut = new DataOutputStream(sendSocket.getOutputStream());
			running = true;
		} catch (UnknownHostException e) {
			running = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendJob(Job job) {
		sendLock.lock();
		Util.send(sendOut, "AddJob", job.getId(), job.getTimeLimit(),
				job.getMemoryLimit());
		String reply = Util.receive(sendIn).getMessage();
		if (reply.equals("Ready To Receive Runnable File"))
			Util.sendFile(sendOut, job.getRunnableFile());
		reply = Util.receive(sendIn).getMessage();
		if (reply.equals("Ready To Receive Input File"))
			Util.sendFile(sendOut, job.getInputFile());		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread receiveThread = new Thread(() -> receiveData());
		receiveThread.setDaemon(true);
		receiveThread.start();
	}

	private void receiveData() {
		while (true) {
			Instruction inst = Util.receive(receiveIn);
			receiveLock.lock();
			String message = inst.getMessage();
			if (message.equals("Done")) {
				Job job = master
						.findJobById(((JobInstruction) inst).getJobId());
				job.setStatus(2);
				File resultFile = job.getResultFile();
				Util.send(receiveOut, "Ready To Receive Result");
				Util.receiveFile(receiveIn, resultFile);
				Util.send(receiveOut, "File Received");
				receiveLock.unlock();
				try {
					java.awt.Desktop.getDesktop().edit(resultFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
				try {
					java.awt.Desktop.getDesktop().edit(resultFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

}
