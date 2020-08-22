package il.co.ilrd.chat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SocketConnection implements Connection {

	private SocketChannel socketChannel = null;
	private ByteBuffer buffer = ByteBuffer.allocate(1024); 
	
	public SocketConnection(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}
	@Override
	public void send(String message) {
		try {
			buffer = Charset.forName("UTF-8").encode(message);
			socketChannel.write(buffer);
		} catch (IOException e) {}
	}
}
