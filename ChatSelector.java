package il.co.ilrd.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;


public class ChatSelector {
	private ChatServer chatServer = null;
	private Selector selector = null;
	private ServerSocketChannel serverSocketChannel = null;
	private ByteBuffer buffer = ByteBuffer.allocate(1024);
	private SelectionKey key = null;
	private boolean flag = false;
	private Thread thread = null;
	private static final char stx = 0x02;
	private static final char etx = 0x03;
	
	public ChatSelector(ChatServer chatServer) throws IOException {
		this.chatServer = chatServer;
		selector = Selector.open();
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(chatServer.getPort()));
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	public void startChat() throws IOException {
		thread = new Thread(new SelectorThread());
		flag = true;
		thread.start();		
	}
	
	public void stopChat() {
		flag = false;
	}
	
	void registerClient() throws IOException {
		 SocketChannel client = serverSocketChannel.accept();
	     client.configureBlocking(false);
	     client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}
	
	void parseProtocol(SelectionKey key) throws IOException {			
		char id;
		int position = 4;
		int length = 0;
		
		SocketChannel channel = (SocketChannel)key.channel();
		channel.read(buffer);
		buffer.flip();
		buffer.clear();
		String str = Charset.forName("UTF-8").decode(buffer).toString();
			
		if (str.isEmpty()) {
			return;
		}

		if(str.charAt(0) != stx || (str.charAt(str.length() - 2) != etx)) {
			char[] arr = {stx, 0x15, etx};
			String errorMessage = arr.toString();
			channel.write(Charset.forName("UTF-8").encode(errorMessage));
			return;
		} 
		else {			
			id = str.charAt(1);
			length = str.charAt(3);
			String groupName = str.substring(position, position + length);
			position += length;
			length = str.charAt(position++);
			String name = str.substring(position, position + length);
			
			switch (str.charAt(2)) {
			case 'R':
				chatServer.addToGroup(groupName, name, channel);					
				break;
			case 'U':
				chatServer.removeFromGroup(groupName, name);	
				key.cancel();
				break;
			case 'M':
				position += length;
				length = str.charAt(position++);
				String message = str.substring(position, position + length);
				chatServer.sendMessage(groupName, message, name);					
				break;
			default:
				throw new IOException();
			}
		}

		char[] ACK = {stx, id, 0x06, etx};
		String ackMessage = ACK.toString();
		channel.write(Charset.forName("UTF-8").encode(ackMessage));
	}
	
	class SelectorThread implements Runnable {
		@Override
		public void run() {
			while(flag){
				try {
					selector.select();
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iter = selectedKeys.iterator();
					while (iter.hasNext()) {
						key = iter.next();
						if (key.isAcceptable()) {
							registerClient();
						}
						else if (key.isReadable()) {
							parseProtocol(key);
						}
						
						iter.remove();
					}   	
				} catch (IOException e) {}
			}
		}		
	}
}