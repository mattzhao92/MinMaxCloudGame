package Json;

public class SocketMessage {
	public String type = "default";
	public String content;
	public Boolean lockScreen;
	
	public SocketMessage(String typeIn, String contentIn, Boolean lockScreenIn) {
		this.type = typeIn;
		this.content = contentIn;
		this.lockScreen = lockScreenIn;
	}
	
}
