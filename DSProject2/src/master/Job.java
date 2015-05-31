package master;

import gui.JobTable;

import java.io.File;
import java.util.UUID;

import common.Util;

public class Job {
	
	private String id;
	private String name;
	private File runnableFile;
	private File inputFile;
	private File resultFile;
	private int timeLimit;
	private int memoryLimit;
	JobTable jobTable;
	
	//0 = Disconnected, 1 = Running, 2 = Finished, 3 = Failed
	private int status;

	
	public Job(String name, File runnableFile, File inputFile) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.runnableFile = runnableFile;
		this.inputFile = inputFile;
		
		String folderPath = "Result" + File.separator + id;
		new File(folderPath).mkdirs();
		folderPath += File.separator;
		resultFile = Util.createFile("result.txt", folderPath);
		
		timeLimit = -1;
		memoryLimit = -1;
	}
	
	/**
	 * Set the jobTable
	 * @param jobTable 
	 */
	public void setJobTable(JobTable jobTable) {
		this.jobTable = jobTable;
	}
	
	/**
	 * @return the job name
	 */
	public String getJobName() {
		return name;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return the runnableFile
	 */
	public File getRunnableFile() {
		return runnableFile;
	}

	/**
	 * @return the intputFile
	 */
	public File getInputFile() {
		return inputFile;
	}
	
	/**
	 * @return the resultFile
	 */
	public File getResultFile() {
		return resultFile;
	}
	
	
	/**
	 * Set the status
	 * @param status 
	 */
	public void setStatus(int status) {
		this.status = status;
		if (this.jobTable != null) {
			jobTable.updateTable();
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the timeLimit
	 */
	public int getTimeLimit() {
		return timeLimit;
	}

	/**
	 * Set the timeLimist
	 * @param timeLimit the timeLimit to set
	 */
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	/**
	 * @return the memoryLimit
	 */
	public int getMemoryLimit() {
		return memoryLimit;
	}

	/**
	 * Set the memoryLimit
	 * @param memoryLimit the memoryLimit to set
	 */
	public void setMemoryLimit(int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}
}
