package master;

import java.io.File;
import java.util.ArrayList;

public class Master {
	
	private ArrayList<Worker> workers;
	private ArrayList<Job> jobs;
	
	public Master() {
		workers = new ArrayList<Worker>();
		jobs = new ArrayList<Job>();
		
		workers.add(new Worker("127.0.0.1", 4444));
	}
	
	public void addJob(File runnableFile, File inputFile){
		Job job = new Job(this, runnableFile, inputFile);
		jobs.add(job);
		Worker worker = findWorker(workers);
		worker.doJob(job);
	}

	public void addWorker(String address, int port) {
		Worker worker = new Worker(address, port);		
		workers.add(worker);
	}
	
	private Worker findWorker(ArrayList<Worker> workers) {
		// TODO round robin
		return workers.get(0);
	}

}
