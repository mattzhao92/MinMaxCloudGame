package Json;

/**
 * Format of the input for the BroadCastMove servlet
 * @author CShuford
 *
 */
public class BroadCastMessage {
	
	/**
	 * The player name that is sending the message
	 */
	public String playername;
	
	/**
	 * The board message the player is sending
	 */
	public String board;
	
	/**
	 * The playerID that is sending the message
	 */
	public String playerId;
}
