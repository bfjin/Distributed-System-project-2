/***
 * Subject                      Distributed System
 * Author: 						Bofan Jin, Fei Tang, Kimple Ke, Roger Li
 * Date of last modification: 	31/05/2015
 ***/

package master;

import gui.JobTable;
import gui.WorkerTable;

import java.util.ArrayList;

/**
 * Master class is for adding workers and jobs
 * */
public class Master {

	private ArrayList<WorkerConnection> workerConnections;
	private ArrayList<Job> jobs;
	private JobTable jobTable;
	private WorkerTable workerTable;

	public Master() {
		workerConnections = new ArrayList<WorkerConnection>();
		jobs = new ArrayList<Job>();
	}

	/**
	 * adds a job
	 * 
	 * @param job
	 *            job to be done
	 */
	public boolean addJob(Job job) {
		jobs.add(job);
		WorkerConnection workerConnection = selectWorker(workerConnections);
		if (workerConnection == null)
			return false;
		System.out.println("Job send");
		workerConnection.sendJob(job);
		if (getJobTable() != null)
			getJobTable().updateTable();
		return true;
	}

	/**
	 * adds the worker to the list
	 * 
	 * @param address
	 *            ip address of the worker
	 * @param port
	 *            port number of the worker
	 */
	public void addWorker(String address, int port) {
		WorkerConnection workerConnection = new WorkerConnection(this, address,
				port, getWorkerTable());
		workerConnections.add(workerConnection);
		getWorkerTable().updateTable();
	}

	/**
	 * select the worker with a minimum workload
	 * 
	 * @param workerConnections
	 *            array list of workers
	 */
	private WorkerConnection selectWorker(
			ArrayList<WorkerConnection> workerConnections) {
		int min = Integer.MAX_VALUE;
		WorkerConnection selected = null;
		for (WorkerConnection workerConnection : workerConnections) {
			int workload = workerConnection.getWorkLoad();
			if (workload < min) {
				min = workload;
				selected = workerConnection;
			}
			System.err.println("workerid = " + workerConnection.getWorkerID());
			System.err.println("workload = " + workload);
		}
		return selected;
	}

	/**
	 * return the job with the job id, null if cannot find
	 * 
	 * @param jobId
	 *            job id
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
	public ArrayList<WorkerConnection> getWorkers() {
		return workerConnections;
	}

	/**
	 * @return the jobTable
	 */
	public JobTable getJobTable() {
		return jobTable;
	}

	/**
	 * Set the jobTable
	 * 
	 * @param jobTable
	 *            the jobTable to set
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
	 * 
	 * @param workerTable
	 *            the workerTable to set
	 */
	public void setWorkerTable(WorkerTable workerTable) {
		this.workerTable = workerTable;
	}

}
