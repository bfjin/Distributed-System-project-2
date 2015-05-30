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
	private ReentrantLock lock;
	private boolean error;

	public JobExecutor(DataOutputStream out, AddJobInstruction inst, File runnableFile,
			File inputFile, File outputFile, File errorFile, ReentrantLock lock) {
		this.out = out;
		this.instruction = inst;
		this.runnableFile = runnableFile;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.errorFile = errorFile;
		this.lock = lock;
		this.jobId = instruction.getJobId();
	}

	@Override
	public void run() {
		System.out.println("aaa");
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
				System.out.println("bbb2");
				finished = p.waitFor(timeLimit, TimeUnit.MILLISECONDS);
			} else {
				// This take like 1 minute or longer
				// BEAWARE
				System.out.println("bbb1");
				p.waitFor();
			}
			System.out.println("bbb0");
			lock.lock();
			interrupt();
			if (!finished || p.exitValue() != 0) {
				Util.send(out, "Failed", instruction.getJobId());
				error = true;
//				String reply = Util.receive(in).getMessage();
//				if (reply.equals("Ready To Receive Result")) {
//					
//				}
			} else {
				Util.send(out, "Done", instruction.getJobId());
				error = false;
//				String reply = Util.receive(in).getMessage();
//				if (reply.equals("Ready To Receive Result")) {
//					Util.sendFile(out, outputFile);
//				}
			}
//			String reply = Util.receive(in).getMessage();
//			System.out.println("xxx");
//			if (reply.equals("File Received")) {
//				lock.unlock();
//			//	worker.setWorkload(worker.getWorkload() - 1);
//				System.out.println("zzz");
//			}
			System.out.println("zzz11");
		} catch (InterruptedException e) {
			System.err.println("Job interrupted");
			e.printStackTrace();
		} catch (IOException e1) {
			System.err.println("Connection down");
			e1.printStackTrace();
		}
	}
	
	public void sendFile(){
		if (error)
			Util.sendFile(out, errorFile);
		else 
			Util.sendFile(out, outputFile);
	}
	
	public void fileReceived() {
		lock.unlock();
	//	worker.setWorkload(worker.getWorkload() - 1);
		System.out.println("zzz");
	}

	/**
	 * @return the jobId
	 */
	public String getJobId() {
		return jobId;
	}
}
