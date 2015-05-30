package master;

import gui.JobTable;
import gui.WorkerTable;

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
	JobTable jobTable;
	WorkerTable workerTable;

	public Master() {
		workers = new ArrayList<Worker>();
		jobs = new ArrayList<Job>();

		workers.add(new Worker(this, "127.0.0.1", Util.workerSocket, workerTable));
		//workers.add(new Worker(this, "146.118.97.88", Util.workerSocket, workerTable));
		
		Thread listenThread = new Thread(() -> listen(Util.masterSocket));
		listenThread.setDaemon(true);
		listenThread.start();
	}
	
	public void setJobTable(JobTable jobTable) {
		this.jobTable = jobTable;
	}
	
	public void setWorkerTable(WorkerTable workerTable) {
		this.workerTable = workerTable;
	}

	public void addJob(Job job) {
		jobs.add(job);
		Worker worker = selectWorker(workers);
		System.err.println("workerid = " + worker.getWorkerID());
		worker.sendJob(job);
		jobTable.updateTable();
	}

	public void addWorker(String address, int port) {
		Worker worker = new Worker(this, address, port, workerTable);
		workers.add(worker);
		//workerTable.updateTable();
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
			serverSocket = new ServerSocket (serverPort);
			//serverSocket = Util.getServerSocket(serverPort);
			//serverSocket.setNeedClientAuth(true);  
			while (true) {
				//SSLSocket workerSocket = (SSLSocket) serverSocket.accept();
				Socket workerSocket = serverSocket.accept();
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
