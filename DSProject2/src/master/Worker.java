package master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Worker {

	private ArrayList<Job> jobs;
	private String address;
	private int port;
	private boolean running;
	private SSLSocket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public Worker(String address, int port) {
		jobs = new ArrayList<Job>();
		connect();
	}

	public void connect() {
		try {
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			socket = (SSLSocket) sslsocketfactory.createSocket(address, port);
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
			out.writeUTF(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String receive() {
		try {
			return in.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

}
