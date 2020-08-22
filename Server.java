package il.co.ilrd.chat;

import java.nio.channels.SelectableChannel;

public interface Server {
	public void sendMessage(String groupName, String message, String name);
	
	public void addToGroup(String groupName, String name, SelectableChannel channel);
	
	public void removeFromGroup(String groupName, String name);
	
}
