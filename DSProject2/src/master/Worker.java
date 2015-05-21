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

	public void send(String data) {
		try {
			byte[] bytes = data.getBytes("utf-8");
			out.writeInt(bytes.length);
			out.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String receive() {
		try {
			int length = in.readInt();
			byte[] bytes = new byte[length];
			in.read(bytes);
			return new String(bytes, "utf-8").trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
