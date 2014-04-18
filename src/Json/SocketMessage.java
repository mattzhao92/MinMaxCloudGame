package Json;

public class SocketMessage {
	public String type = "default";
	public String content;
	
	public SocketMessage(String typeIn, String contentIn) {
		this.type = typeIn;
		this.content = contentIn;
	}
	
}
