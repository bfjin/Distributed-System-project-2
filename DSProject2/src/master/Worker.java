package master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Worker {

	private String address;
	private int port;
	private boolean running;
	// private SSLSocket socket;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

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
			socket = new Socket(address, port);

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			running = true;
		} catch (UnknownHostException e) {
			running = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DataInputStream getInputStream() {
		return in;
	}

	public DataOutputStream getOutputStream() {
		return out;
	}

	public boolean isRunning() {
		return running;
	}
}
