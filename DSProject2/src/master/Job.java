package master;

import java.io.File;

public class Job {
	
	Master master;
	File runnableFile;
	File inputFile;

	public Job(Master master, File runnableFile, File inputFile) {
		this.master = master;
		this.runnableFile = runnableFile;
		this.inputFile = inputFile;
	}

}
