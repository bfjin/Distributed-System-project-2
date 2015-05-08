package master;

import java.util.ArrayList;

public class Worker {
	
	private ArrayList<Job> jobs;
	private String address;
	private int port;
	private int status;
	
	public Worker(String address, int port) {
		jobs = new ArrayList<Job>();
	}

	public void doJob(Job job) {
		jobs.add(job);
	}

}
