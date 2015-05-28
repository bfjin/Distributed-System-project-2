package worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener {

//	private SSLServerSocket serverSocket;
	private ServerSocket serverSocket;
	public static int workload; 

	public static void main(String args[]) {
		new Listener(4444);
	}

	public Listener(int serverPort) {
		workload = 0;
		try {
//			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
//					.getDefault();
//			serverSocket = (SSLServerSocket) sslserversocketfactory
//					.createServerSocket(serverPort);
			serverSocket = new ServerSocket(serverPort);
			System.out.println("Server Started");
			while (true) {
				Socket clientSocket = serverSocket.accept();
				Connection c = new Connection(clientSocket);
				c.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
