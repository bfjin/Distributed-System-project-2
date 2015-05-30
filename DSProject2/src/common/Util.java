package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class Util {

	//public static int masterSocket = 4445;
	public static int workerSocket = 4444;

	public static void sendFile(DataOutputStream out, File file) {
		System.out.println("Sending File: " + file.getPath());
		try {
			byte [] bytearray  = new byte [8192];
			FileInputStream fin = new FileInputStream(file);
			int len = 0;
			int totalBytesSent = 0;
			out.writeLong(file.length());
			while ((len = fin.read(bytearray)) > -1) {
				out.write(bytearray, 0, len);
				totalBytesSent += len;				
			}
			System.err.println("totalBytesSent = " + totalBytesSent);		
			fin.close();
		} catch (IOException e) {
			System.err.println("Failed when sending file");
			e.printStackTrace();
		}
	}

	public static File receiveFile(DataInputStream in, File file) {
		System.out.println("Receiving File: " + file.getPath());

		try {
			byte[] bytearray = new byte[8192];
			FileOutputStream fos = new FileOutputStream(file);
			int bytesRead = 0;
			int totalBytesRead = 0;
			long totalFileLength = in.readLong();
			do {
				bytesRead = in.read(bytearray, 0, bytearray.length);
				if(bytesRead >= 0) {
					totalBytesRead += bytesRead;
					fos.write(bytearray, 0, bytesRead);
				}
			} while (totalBytesRead < totalFileLength);
			System.out.println("totalBytesRead =  " + totalBytesRead);
			fos.close();
		} catch (IOException e) {
			System.err.println("Failed when receiving file");
			e.printStackTrace();
		}
		return file;
	}

	public static void send(DataOutputStream out, String message) {
		try {
			String data = new Instruction(message).toJson();
			out.writeUTF(data);
			System.out.println("Send: " + data);
		} catch (IOException e) {
			System.err.println("Failed when sending message");
			e.printStackTrace();
		}
	}

	public static void send(DataOutputStream out, String message, String jobId) {
		try {
			String data = new JobInstruction(message, jobId).toJson();
			out.writeUTF(data);
			System.out.println("Send: " + data);
		} catch (IOException e) {
			System.err.println("Failed when sending message");
			e.printStackTrace();
		}
	}

	public static void send(DataOutputStream out, String message, String jobId,
			int timeLimit, int memoryLimit) {
		try {
			String data = new AddJobInstruction(message, jobId, timeLimit,
					memoryLimit).toJson();
			out.writeUTF(data);
			System.out.println("Send: " + data);
		} catch (IOException e) {
			System.err.println("Failed when sending message");
			e.printStackTrace();
		}
	}

	public static Instruction receive(DataInputStream in) {
		try {
			System.err.println(in);
			String data = in.readUTF();
			// TODO deal with receive file
			//if (data.length() < 1000) {
				System.out.println("Receive: " + data);
				String type = Instruction.getTypefromJson(data);
				if (type.equals("Instruction")){
					return Instruction.fromJson(data);
				}
				if (type.equals("JobInstruction")){
					return JobInstruction.fromJson(data);
				}
				if (type.equals("AddJobInstruction")){
					return AddJobInstruction.fromJson(data);
				}
			//}
			return new Instruction("");
		} catch (IOException e) {
			System.err.println("Failed when receive instruction");
			e.printStackTrace();
			return null;
		}
	}

	public static File createFile(String fileName, String folderPath) {
		File file = new File(folderPath + fileName);
		try {
			file.createNewFile();
			return file;
		} catch (IOException e) {
			System.err.println("Failed when creating file");
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 public static SSLServerSocket getServerSocket(String certName, int thePort){
	    SSLServerSocket socket = null;
	    try
	    {
		    //String key="SSLKey";  //要使用的证书名
		    
		    char keyStorePass[]="password".toCharArray();  //证书密码
		    
		    char keyPassword[]="password".toCharArray();  //证书别称所使用的主要密码
		    
		    KeyStore ks=KeyStore.getInstance("JKS");  //创建JKS密钥库
		    
		    ks.load(new FileInputStream(certName),keyStorePass);
		    
		    //创建管理JKS密钥库的X.509密钥管理器
		    KeyManagerFactory kmf=KeyManagerFactory.getInstance("SunX509");
		    
		    kmf.init(ks,keyPassword);
		    
		    KeyStore tks = KeyStore.getInstance("JKS");  
		    
            tks.load(new FileInputStream(certName), keyStorePass);
            
            TrustManagerFactory tmf = TrustManagerFactory  
                    .getInstance("SunX509");  
            tmf.init(tks);  
            
            
		    SSLContext sslContext = SSLContext.getInstance("SSL");
		    
		    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),null);
		    
		    //根据上面配置的SSL上下文来产生SSLServerSocketFactory,与通常的产生方法不同
		    SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
		    socket = (SSLServerSocket)factory.createServerSocket(thePort);

	    
	    }catch(Exception e){
	    	System.out.println(e);
	    }
	    return socket;
	 }
	 */
	public static SSLServerSocket getServerSocket(int thePort){
	    SSLServerSocket socket = null;
	    try{
		    String key="keystore";  //要使用的证书名
		    
		    char keyStorePass[]="password".toCharArray();  //证书密码
		    
		    char keyPassword[]="password".toCharArray();  //证书别称所使用的主要密码
		    
		    KeyStore ks=KeyStore.getInstance("JKS");  //创建JKS密钥库
		    
		    ks.load(new FileInputStream(key),keyStorePass);
		    
		    //创建管理JKS密钥库的X.509密钥管理器
		    KeyManagerFactory kmf=KeyManagerFactory.getInstance("SunX509");
		    
		    kmf.init(ks,keyPassword);
		    
		    SSLContext sslContext=SSLContext.getInstance("SSLv3");

		    
		    sslContext.init(kmf.getKeyManagers(),null,null);
		    
		    //根据上面配置的SSL上下文来产生SSLServerSocketFactory,与通常的产生方法不同
		    SSLServerSocketFactory factory=sslContext.getServerSocketFactory();
		    
		    socket = (SSLServerSocket)factory.createServerSocket(thePort);
		    
	    } catch(Exception e){
	    	System.out.println(e);
	    }
	    
	    return socket;
	}
	

}
