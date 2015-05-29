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
		
		String folderPath = "Result\\" + id;
		new File(folderPath).mkdirs();
		folderPath += "\\";
		resultFile = Util.createFile("result.txt", folderPath);
		
		timeLimit = -1;
		memoryLimit = -1;
	}
	
	public void setJobTable(JobTable jobTable) {
		this.jobTable = jobTable;
	}
	
	public String getJobName() {
		return name;
	}

	public int getStatus() {
		return status;
	}

	public File getRunnableFile() {
		return runnableFile;
	}


	public File getInputFile() {
		return inputFile;
	}
	
	public File getResultFile() {
		return resultFile;
	}

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
	 * @param memoryLimit the memoryLimit to set
	 */
	public void setMemoryLimit(int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}
}
