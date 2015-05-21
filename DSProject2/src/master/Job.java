package master;

import java.io.File;

public class Job {

	private String name;
	private File runnableFile;
	private File inputFile;
	
	// 0 = Disconnected, 1 = Running, 2 = Finished, 3 = Failed
	private int status;

	public Job(String name, File runnableFile, File inputFile) {
		this.name = name;
		this.runnableFile = runnableFile;
		this.inputFile = inputFile;
		setStatus(0);
	}
	
	public String getJobName(){
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

	public void setStatus(int status) {
		this.status = status;
	}
}
