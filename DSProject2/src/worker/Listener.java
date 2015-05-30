package worker;

import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import common.Util;

public class Listener {

	private SSLServerSocket serverSocket;
	private int workload; 

	public static void main(String args[]) {
		new Listener(Util.workerSocket);
	}

	public Listener(int serverPort) {
		setWorkload(0);
		try {

			serverSocket = Util.getServerSocket(serverPort);
			//serverSocket.setNeedClientAuth(true);  
			System.out.println("Worker Started");
			while (true) {			
				SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
				System.out.println("A new connection is detected");
				Connection c = new Connection(clientSocket, this);
				c.start();
			}
		} catch (IOException e) {
			System.err.println("Worker failed to launch.");
			e.printStackTrace();
		}
	}

	public int getWorkload() {
		return workload;
	}

	public void setWorkload(int workload) {
		this.workload = workload;
	}
}
