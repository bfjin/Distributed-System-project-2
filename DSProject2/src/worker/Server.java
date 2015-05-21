package worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

//	private SSLServerSocket serverSocket;
	private ServerSocket serverSocket;

	public static void main(String args[]) {
		new Server(4444);
	}

	public Server(int serverPort) {
		try {
//			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
//					.getDefault();
//			serverSocket = (SSLServerSocket) sslserversocketfactory
//					.createServerSocket(serverPort);
			serverSocket = new ServerSocket(serverPort);
			
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
