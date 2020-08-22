package il.co.ilrd.chat;

public class ClientInfo {
	String name = null;
	Connection connection = null;;
	
	public ClientInfo(String name, Connection connection) {
		this.name = name;
		this.connection = connection;
	}	
}
