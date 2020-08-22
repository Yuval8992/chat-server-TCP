package il.co.ilrd.chat;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;

public class ChatServer implements Server{
	private HashMap<String, LinkedList<ClientInfo>> map = new HashMap<>();
	private int port = 0;
		
	public ChatServer(int port) throws IOException {
		this.port = port;
	}
	
	@Override
	public void addToGroup(String groupName, String name, SelectableChannel channel) {
		SocketChannel socketChannel = (SocketChannel)channel;
		ClientInfo client = new ClientInfo(name, new SocketConnection(socketChannel));
		if (!map.containsKey(groupName)) {
			map.put(groupName, new LinkedList<>());
		} 
		map.get(groupName).add(client);
	}
	
	@Override
	public void removeFromGroup(String groupName, String name) {
		for (ClientInfo c : map.get(groupName)) {
			if (c.name.equals(name)) {
				map.get(groupName).remove(c);				
			}
		}
	}
	
	@Override
	public void sendMessage(String groupName, String message, String name) {
		for (ClientInfo c : map.get(groupName)) {
			if (!c.name.equals(name)) {
				c.connection.send(message);				
			}
		}
	}

	public int getPort() {
		return port;
	}
}

