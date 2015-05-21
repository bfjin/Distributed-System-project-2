package master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import common.Util;

public class Job extends Thread {

	private String name;
	private File runnableFile;
	private File inputFile;
	private Worker worker;
	// 0 = Disconnected, 1 = Running, 2 = Finished, 3 = Failed
	private int status;

	public Job(String name, File runnableFile, File inputFile) {
		setDaemon(true);
		this.name = name;
		this.runnableFile = runnableFile;
		this.inputFile = inputFile;
		status = 0;
	}

	public Worker getWorker() {	
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	@Override
	public void run() {	
		DataInputStream in = worker.getInputStream();
		DataOutputStream out = worker.getOutputStream();
		Util.send(out, "AddJob");
		String reply = Util.receive(in);
		if (reply.equals("Ready To Receive Runnable File"))
		Util.sendFile(out, runnableFile);
		reply = Util.receive(in);
		if (reply.equals("Ready To Receive Input File"))
		Util.sendFile(out, inputFile);
		status = 1;
		
		reply = Util.receive(in);
		if (reply.equals("Done"))
			status = 2;
		else 
			status = 3;		
		
		// TODO handle output file
	}
	
	public String getJobName(){
		return name;
	}

	public int getStatus() {
		return status;
	}
}
