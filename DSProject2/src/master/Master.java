package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import common.Util;

public class Master {

	private ArrayList<Worker> workers;
	private ArrayList<Job> jobs;

	public Master() {
		workers = new ArrayList<Worker>();
		jobs = new ArrayList<Job>();

		workers.add(new Worker(this, "127.0.0.1", Util.workerSocket));
		workers.add(new Worker(this, "127.0.0.2", Util.workerSocket));

		Thread listenThread = new Thread(() -> listen(Util.masterSocket));
		listenThread.setDaemon(true);
		listenThread.start();
	}

	public void addJob(Job job) {
		jobs.add(job);
		Worker worker = selectWorker(workers);
		System.err.println("workerid = " + worker.getWorkerID());
		worker.sendJob(job);
	}

	public void addWorker(String address, int port) {
		Worker worker = new Worker(this, address, port);
		workers.add(worker);
	}

	// something wrong here
	private Worker selectWorker(ArrayList<Worker> workers) {
		int min = -1;
		Worker selected = null;
		for (Worker worker : workers) {
			System.err.println(worker.getWorkLoad());
			int workload = worker.getWorkLoad();
			if (workload > min) {
				min = workload;
				selected = worker;
			}
		}
		return selected;
	}

	public Job findJobById(String jobId) {
		for (Job job : jobs) {
			if (job.getId().equals(jobId)){
				return job;
			}
		}
		return null;
	}

	public ArrayList<Job> getJobs() {
		return jobs;
	}

	public ArrayList<Worker> getWorkers() {
		return workers;
	}

	public void listen(int serverPort) {
		ServerSocket serverSocket;
		try {
			java.lang.System.setProperty("javax.net.ssl.keyStore", "certif");
			java.lang.System.setProperty("javax.net.ssl.keyStorePassword", "123456");
			
			SSLServerSocketFactory sslServerSocketFactory = 
					(SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			serverSocket = (SSLServerSocket) sslServerSocketFactory
					.createServerSocket(serverPort);
			while (true) {
				SSLSocket workerSocket = (SSLSocket) serverSocket.accept();
				String address = workerSocket.getLocalAddress()
						.getHostAddress();
				for (Worker worker : workers) {
					if (worker.getAddress().equals(address)){
						worker.setReceiveSocket(workerSocket);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to establish connection with worker");
			e.printStackTrace();
		}
	}
}
