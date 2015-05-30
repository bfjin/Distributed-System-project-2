package master;

import gui.JobTable;
import gui.WorkerTable;

import java.util.ArrayList;

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
		//jobTable.updateTable();
	}

	public void addWorker(String address, int port) {
		Worker worker = new Worker(this, address, port, workerTable);
		workers.add(worker);
		//workerTable.updateTable();
	}

	// something wrong here
	private Worker selectWorker(ArrayList<Worker> workers) {
//		int min = -1;
//		Worker selected = null;
//		for (Worker worker : workers) {
//			System.err.println(worker.getWorkLoad());
//			int workload = worker.getWorkLoad();
//			if (workload > min) {
//				min = workload;
//				selected = worker;
//			}
//		}
//		return selected;
		return workers.get(0);
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
}
