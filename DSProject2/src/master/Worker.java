package master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import common.Util;

public class Worker {

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

	public Worker(String address, int port) {
		this.address = address;
		this.port = port;
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

	public void send(Job job) {
		Util.send(sendOut, "AddJob");
		String reply = Util.receive(sendIn);
		if (reply.equals("Ready To Receive Runnable File"))
		Util.sendFile(sendOut, job.getRunnableFile());
		reply = Util.receive(sendIn);
		if (reply.equals("Ready To Receive Input File"))
		Util.sendFile(sendOut, job.getInputFile());
		job.setStatus(1);		
	}
	
	public boolean isRunning() {
		return running;
	}

	public String getAddress() {
		return address;
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
		Thread listenThread = new Thread(() -> receiveData());
		listenThread.setDaemon(true);
		listenThread.start();
	}

	private void receiveData() {
		while (true) {
			String data = Util.receive(receiveIn);	
		}			
	}

}
