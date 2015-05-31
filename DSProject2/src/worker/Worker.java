/***
 * Subject                      Distributed System
 * Author: 						Bofan Jin, Fei Tang, Kimple Ke, Roger Li
 * Date of last modification: 	31/05/2015
 ***/

package worker;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLSocket;

import common.Util;

/**
 * Listener class represents a worker on the cloud server listens the
 * instruction sent from the master
 * */
public class Worker {
	
	private int workload; 

	public static void main(String args[]) {
		new Worker(Util.workerSocket);
	}

	public Worker(int serverPort) {
		setWorkload(0);
		try {			
			ServerSocket serverSocket = Util.getServerSocket(serverPort);
			System.out.println("Worker Started");
			while (true) {			
				SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
				System.out.println("A new connection is detected");
				MasterConnection c = new MasterConnection(clientSocket, this);
				c.start();
			}
		} catch (IOException e) {
			System.err.println("Worker failed to launch.");
			e.printStackTrace();
		}
	}

	/**
	 * @return the workload
	 */
	public int getWorkload() {
		return workload;
	}

	/**
	 * Set the workload
	 * @param workload
	 */
	public void setWorkload(int workload) {
		this.workload = workload;
	}
}
