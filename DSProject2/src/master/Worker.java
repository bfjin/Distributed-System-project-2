package master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

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
	
	private SSLServerSocket getServerSocket(int thePort){
	    SSLServerSocket socket = null;
	    try{
		    String key="SSLKey";  //certificate name
		    
		    char keyStorePass[]="12345678".toCharArray();  //证书密码
		    
		    char keyPassword[]="12345678".toCharArray();  //证书别称所使用的主要密码
		    
		    KeyStore ks=KeyStore.getInstance("JKS");  //创建JKS密钥库
		    
		    ks.load(new FileInputStream(key),keyStorePass);
		    
		    //创建管理JKS密钥库的X.509密钥管理器
		    KeyManagerFactory kmf=KeyManagerFactory.getInstance("SunX509");
		    
		    kmf.init(ks,keyPassword);
		    
		    SSLContext sslContext=SSLContext.getInstance("SSLv3");
		    
		    sslContext.init(kmf.getKeyManagers(),null,null);
		    
		    //根据上面配置的SSL上下文来产生SSLServerSocketFactory,与通常的产生方法不同
		    SSLServerSocketFactory factory=sslContext.getServerSocketFactory();
		    
		    socket = (SSLServerSocket)factory.createServerSocket(thePort);
		    
	    }
	    catch(Exception e){
	    	System.out.println(e);
	    }
	    
	    return(socket);
    }

}
