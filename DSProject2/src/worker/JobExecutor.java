package worker;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import common.AddJobInstruction;
import common.Util;

public class JobExecutor extends Thread {

	private String jobId;
	private DataOutputStream out;
	private AddJobInstruction instruction;
	private File runnableFile;
	private File inputFile;
	private File outputFile;
	private File errorFile;
	private boolean error;

	public JobExecutor(DataOutputStream out, AddJobInstruction inst,
			File runnableFile, File inputFile, File outputFile, File errorFile,
			ReentrantLock lock) {
		this.out = out;
		this.instruction = inst;
		this.runnableFile = runnableFile;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.errorFile = errorFile;
		this.jobId = instruction.getJobId();
	}

	@Override
	public void run() {
		String javaExePath = Paths
				.get(System.getProperty("java.home"), "bin", "java")
				.toAbsolutePath().toString();
		int memoryLimit = instruction.getMemoryLimit();
		ProcessBuilder builder = null;
		if (memoryLimit != -1) {
			String memoryLimitArg = "-xmx" + memoryLimit + "m";
			builder = new ProcessBuilder(javaExePath, "-jar",
					runnableFile.getPath(), inputFile.getPath(),
					outputFile.getPath(), memoryLimitArg);
		} else {
			builder = new ProcessBuilder(javaExePath, "-jar",
					runnableFile.getPath(), inputFile.getPath(),
					outputFile.getPath());
		}
		builder.redirectError(errorFile);
		try {

			Process p = builder.start();
			int timeLimit = instruction.getTimeLimit();
			boolean finished = true;
			if (timeLimit != -1) {
				finished = p.waitFor(timeLimit, TimeUnit.MILLISECONDS);
			} else {
				p.waitFor();
			}
			Connection.lock.lock();
			try {
				if (!finished || p.exitValue() != 0) {
					Util.send(out, "Failed", instruction.getJobId());
					error = true;
				} else {
					Util.send(out, "Done", instruction.getJobId());
					error = false;
				}
				Connection.fileReceive.awaitUninterruptibly();
			} finally {
				Connection.lock.unlock();
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
	public void sendOutputFile() {
		if (error) {
			Util.sendFile(out, errorFile);
		} else {
			Util.sendFile(out, outputFile);
		}
	}

	/**
	 * @return the jobId
	 */
	public String getJobId() {
		return jobId;
	}
}
