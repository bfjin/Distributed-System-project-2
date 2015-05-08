package master;

import java.io.File;

public class Job {
	
	private Master master;
	private File runnableFile;
	private File inputFile;
	private Worker worker;
	private int status;

	public Job(Master master, File runnableFile, File inputFile) {
		this.master = master;
		this.runnableFile = runnableFile;
		this.inputFile = inputFile;
	}

}
