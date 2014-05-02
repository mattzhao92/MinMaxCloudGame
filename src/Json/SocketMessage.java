package Json;

/**
 * Message to send inside a Channel Message packet
 *
 */
public class SocketMessage {
	
	/**
	 * The type of this message
	 */
	public String type = "default";
	
	/**
	 * The content of this message
	 */
	public String content;
	
	/**
	 * Something
	 */
	public Boolean lockScreen;
	
	/**
	 * Constructor
	 * @param typeIn
	 * @param contentIn
	 * @param lockScreenIn
	 */
	public SocketMessage(String typeIn, String contentIn, Boolean lockScreenIn) {
		this.type = typeIn;
		this.content = contentIn;
		this.lockScreen = lockScreenIn;
	}
	
}
