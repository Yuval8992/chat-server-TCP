package il.co.ilrd.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class User {
	private String group;
	private String name;
	private int ID = 0;
	private static final char stx = 0x02;
	private static final char etx = 0x03;
	private String message = new String();
	private Socket socket = null;
	private PrintWriter writer = null;
	private InputStreamReader reader = null;
	private BufferedReader readBuffer = null;
	private char[] arr = new char[1024];
	private boolean flag = true;
		
	public User(String group, String name, int port) throws UnknownHostException, IOException {
		this.group = group;
		this.name = name;
		socket = new Socket("localhost", port);
		writer = new PrintWriter(socket.getOutputStream(), true);
		reader = new InputStreamReader(socket.getInputStream());
		readBuffer = new BufferedReader(reader);
		writer.println(createRegisterMessage());
	}
	
	public void start() throws IOException {
		new Thread(new sendMessage()).start();
		
		while (true) {
			if (readBuffer.ready()) {
				readBuffer.read(arr);
				System.out.println(arr);
				Arrays.fill(arr, '\u0000');
			}
		}
	}

	private String createMessage(String message) {
		int nameLength = name.length();
		int groupLength = group.length();
		int messageLength = message.length();
		++ID;
		return stx + "" + (char)ID + 'M' + (char)groupLength + group + (char)nameLength + name + 
				(char)messageLength + message + etx; 
	}
	
	private String createRegisterMessage() {
		int nameLength = name.length();
		int groupLength = group.length();
		++ID;
		return stx + "" + (char)ID + "R" + (char)groupLength + group + (char)nameLength + name + etx;
	}
	
	private String createUnregisterMessage() {
		int nameLength = name.length();
		int groupLength = group.length();
		++ID;
		return stx + "" + (char)ID + "U" + (char)groupLength + group + (char)nameLength + name + etx;
	}
	
	public class sendMessage implements Runnable {
		Scanner input = new Scanner(System.in);

		@Override
		public void run() {
			while(flag) {
				message = input.nextLine();
				
				if (message.equals(new String("U"))) {
					writer.println(createUnregisterMessage());
					flag = false;
				}
				else {
					writer.println(createMessage(message));
					message = "";						
				}
			}
		}	
	}
}






