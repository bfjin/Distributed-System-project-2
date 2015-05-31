/***
 * Subject                      Distributed System
 * Author: 						Bofan Jin, Fei Tang, Kimple Ke, Roger Li
 * Date of last modification: 	31/05/2015
 ***/

package worker;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import common.AddJobInstruction;
import common.Util;

/**
 * JobExecutor class is where the job gets to be done
 * */
public class JobExecutor extends Thread {

	private String jobId;	
	private DataOutputStream out;
	private AddJobInstruction instruction;
	private File runnableFile;
	private File inputFile;
	private File outputFile;
	private File errorFile;
	private boolean error;

	public JobExecutor(DataOutputStream out, AddJobInstruction inst, File runnableFile,
			File inputFile, File outputFile, File errorFile, ReentrantLock lock) {
		this.out = out;
		this.instruction = inst;
		this.runnableFile = runnableFile;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.errorFile = errorFile;
		this.jobId = instruction.getJobId();
	}

	@Override
	/**
	 * Do the job
	 */
	public void run() {
		String javaExePath = Paths
				.get(System.getProperty("java.home"), "bin", "java")
				.toAbsolutePath().toString();
		int memoryLimit = instruction.getMemoryLimit();
		ProcessBuilder builder = null;
		// if memoryLimit has been set ...
		if (memoryLimit != -1) {
			// set it
			String memoryLimitArg = "-xmx" + memoryLimit + "m";
			builder = new ProcessBuilder(javaExePath, "-jar",
					runnableFile.getPath(), inputFile.getPath(),
					outputFile.getPath(), memoryLimitArg);
		// else leave it 
		} else {
			builder = new ProcessBuilder(javaExePath, "-jar",
					runnableFile.getPath(), inputFile.getPath(),
					outputFile.getPath());
		}
		// if anything goes wrong, make a error file
		builder.redirectError(errorFile);
		try {
			// do work
			Process p = builder.start();
			int timeLimit = instruction.getTimeLimit();
			boolean finished = true;
			// if timelimit has been set
			if (timeLimit != -1) {
				// wait for maximum timelimit of miliseconds
				finished = p.waitFor(timeLimit, TimeUnit.MILLISECONDS);
			// else leave it
			} else {
				p.waitFor();
			}
			MasterConnection.lock.lock();
			interrupt();
			if (!finished || p.exitValue() != 0) {
				Util.send(out, "Failed", instruction.getJobId());
				error = true;
			} else {
				Util.send(out, "Done", instruction.getJobId());
				error = false;
			}
		} catch (InterruptedException e) {
			System.err.println("Job interrupted");
			e.printStackTrace();
		} catch (IOException e1) {
			System.err.println("Connection down");
			e1.printStackTrace();
		}
	}
	
	/**
	 * Send output file back to the master.
	 */
	public void sendOutputFile(){
		if (error){
			try {
				Util.sendFile(out, errorFile);
			} catch (IOException e) {
				System.err.println("Failed when sending file");
				e.printStackTrace();
			}
		}
		else {
			try {
				Util.sendFile(out, outputFile);
			} catch (IOException e) {
				System.err.println("Failed when sending file");
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the jobId
	 */
	public String getJobId() {
		return jobId;
	}
}
