package common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class Util {

	public static String fileToByteString(File file) {
		byte[] bytes = new byte[(int) file.length()];
		try {
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));
			bis.read(bytes);
			bis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return new String(Base64.encodeBase64(bytes), "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static File bytesStringToFile(File file, String string) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bos.write(Base64.decodeBase64(string));
			bos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	public static void sendFile(DataOutputStream out, File file) {
		System.out.println("Send File: " + file.getPath());
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static File receiveFile(DataInputStream in, File file) {
		System.out.println("Receive File: " + file.getPath());

		try {
			byte[] bytearray = new byte[8192];
			FileOutputStream fos = new FileOutputStream(file);
			int bytesRead = 0;
			int totalBytesRead = 0;
			long totalFileLength = in.readLong();
			do {
				//System.out.println(bytesRead);
				bytesRead = in.read(bytearray, 0, bytearray.length);
				//System.out.println(totalBytesRead);
				if(bytesRead >= 0) {
					totalBytesRead += bytesRead;
					fos.write(bytearray, 0, bytesRead);
				}
			} while (totalBytesRead < totalFileLength);
			System.out.println("totalBytesRead =  " + totalBytesRead);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void send(DataOutputStream out, String message, String jobId) {
		try {
			String data = new JobInstruction(message, jobId).toJson();
			out.writeUTF(data);
			System.out.println("Send: " + data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Instruction receive(DataInputStream in) {
		try {
			String data = in.readUTF();
			// TODO deal with receive file
			if (data.length() < 1000) {
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
			}
			return new Instruction("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
}
