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
			int length = (int) file.length();
			out.writeInt(length);
			byte[] bytes = new byte[length];
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));
			bis.read(bytes);
			out.write(bytes);
			bis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static File receiveFile(DataInputStream in, File file) {
		System.out.println("Receive File: " + file.getPath());
		try {
			int length = in.readInt();
			byte[] bytes = new byte[length];
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			in.read(bytes);
			bos.write(bytes);
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	public static void send(DataOutputStream out, String data) {
		try {
			out.writeUTF(data);
			System.out.println("Send: " + data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String receive(DataInputStream in) {
		try {
			String data = in.readUTF();
			System.out.println("Receive: " + data);
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
