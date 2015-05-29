package worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import common.Util;

public class Listener {

//	private SSLServerSocket serverSocket;
	private ServerSocket serverSocket;
	private int workload; 

	public static void main(String args[]) {
		new Listener(Util.workerSocket);
	}

	public Listener(int serverPort) {
		setWorkload(0);
		try {
//			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
//					.getDefault();
//			serverSocket = (SSLServerSocket) sslserversocketfactory
//					.createServerSocket(serverPort);
			serverSocket = new ServerSocket(serverPort);

			System.out.println("Worker Started");
			while (true) {			
				Socket clientSocket = serverSocket.accept();
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
