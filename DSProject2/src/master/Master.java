/***
 * Subject                      Distributed System
 * Author: 						Bofan Jin, Fei Tang, Kimple Ke, Roger Li
 * Date of last modification: 	31/05/2015
 ***/

package master;

import gui.JobTable;
import gui.WorkerTable;

import java.util.ArrayList;

import common.Util;

/**
 * Master class is for adding workers and jobs
 * */
public class Master {

	private ArrayList<Worker> workers;
	private ArrayList<Job> jobs;
	private JobTable jobTable;
	private WorkerTable workerTable;

	public Master() {
		workers = new ArrayList<Worker>();
		jobs = new ArrayList<Job>();

		//workers.add(new Worker(this, "127.0.0.1", Util.workerSocket,
		//		getWorkerTable()));
		 //workers.add(new Worker(this, "146.118.97.86", Util.workerSocket,
		 //workerTable));
//		 workers.add(new Worker(this, "146.118.97.88", Util.workerSocket,
//		 workerTable));
		 

		 workers.add(new Worker(this, "43.240.96.206", Util.workerSocket,
		 workerTable));
		 workers.add(new Worker(this, "43.240.96.207", Util.workerSocket,
		 workerTable));
	}

	public void addJob(Job job) {
		jobs.add(job);
		Worker worker = selectWorker(workers);
		System.out.println("Job send");
		worker.sendJob(job);
		if (getJobTable() != null)
			getJobTable().updateTable();
	}

	public void addWorker(String address, int port) {
		Worker worker = new Worker(this, address, port, getWorkerTable());
		workers.add(worker);
		getWorkerTable().updateTable();
	}

	/**
	 * select the worker with a minimum workload
	 * @param workers array list of workers
	 */
	private Worker selectWorker(ArrayList<Worker> workers) {
		int min = Integer.MAX_VALUE;
		Worker selected = null;
		for (Worker worker : workers) {
			int workload = worker.getWorkLoad();
			if (workload < min) {
				min = workload;
				selected = worker;
			}
			System.err.println("workerid = " + worker.getWorkerID());
			System.err.println("workload = " + workload);
		}
		return selected;
	}

	/**
	 * return the job with the job id, null if cannot find
	 * @param jobId job id
	 */
	public Job findJobById(String jobId) {
		for (Job job : jobs) {
			if (job.getId().equals(jobId)) {
				return job;
			}
		}
		return null;
	}

	/**
	 * @return the jobs
	 */
	public ArrayList<Job> getJobs() {
		return jobs;
	}

	/**
	 * @return the workers
	 */
	public ArrayList<Worker> getWorkers() {
		return workers;
	}

	/**
	 * @return the jobTable
	 */
	public JobTable getJobTable() {
		return jobTable;
	}

	/**
	 * Set the jobTable
	 * @param jobTable the jobTable to set
	 */
	public void setJobTable(JobTable jobTable) {
		this.jobTable = jobTable;
	}

	/**
	 * @return the workerTable
	 */
	public WorkerTable getWorkerTable() {
		return workerTable;
	}

	/**
	 * Set the workerTalbe
	 * @param workerTable the workerTable to set
	 */
	public void setWorkerTable(WorkerTable workerTable) {
		this.workerTable = workerTable;
	}

}
