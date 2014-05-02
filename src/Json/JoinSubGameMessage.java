package Json;

import com.google.gson.Gson;

/**
 * Input to calls to join sub game servlet
 *
 */
public class JoinSubGameMessage {
	
	/**
	 * playerID joining the sub game
	 */
	public Long playerID;
	
	/**
	 * playerName joining the sub game
	 */
	public String playerName;
	
	/**
	 * if this is an AI player or not
	 */
	public boolean isAI;
	
	/**
	 * if this is an AI player, the url of the AI server
	 */
	public String AIUrl; //same with playerServer URL
	
	/**
	 * url of the game
	 */
	public String gameURL;
	
	/**
	 * the player's token
	 */
	public String token;
	
	/**
	 * Get the JSON string for this packet
	 * @return
	 */
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this, JoinSubGameMessage.class);
	}
}
