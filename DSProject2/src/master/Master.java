package master;

import java.io.File;
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

		Thread listenThread = new Thread(() -> listen(5555));
		listenThread.setDaemon(true);
		listenThread.start();
	}

	public void addJob(String name, File runnableFile, File inputFile) {
		Job job = new Job(name, runnableFile, inputFile);
		jobs.add(job);
		Worker worker = selectWorker(workers);
		worker.send(job);
	}

	public void addWorker(String address, int port) {
		Worker worker = new Worker(this, address, port);
		workers.add(worker);
	}

	private Worker selectWorker(ArrayList<Worker> workers) {
		// TODO select a worker
		return workers.get(0);
	}

	public Job findJobById(String jobId) {
		for (Job job : jobs) {
			if (job.getId().equals(jobId))
				return job;
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
					if (worker.getAddress().equals(address))
						worker.setReceiveSocket(workerSocket);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
