package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Master {

	private ArrayList<Worker> workers;
	private ArrayList<Job> jobs;

	public Master() {
		workers = new ArrayList<Worker>();
		jobs = new ArrayList<Job>();

		workers.add(new Worker(this, "127.0.0.1", 4444));

		Thread listenThread = new Thread(() -> listen(4445));
		listenThread.setDaemon(true);
		listenThread.start();
	}

	public void addJob(Job job) {
		jobs.add(job);
		Worker worker = selectWorker(workers);
		worker.sendJob(job);
	}

	public void addWorker(String address, int port) {
		Worker worker = new Worker(this, address, port);
		workers.add(worker);
	}

	private Worker selectWorker(ArrayList<Worker> workers) {
		int min = -1;
		Worker selected = null;
		for (Worker worker : workers) {
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
			serverSocket = new ServerSocket(serverPort);
			while (true) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}