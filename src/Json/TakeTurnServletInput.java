package Json;

/**
 * Input for calls to the take turn servlet
 *
 */
public class TakeTurnServletInput {
	
	/**
	 * Player that is being given their turn
	 */
	public Long playerID;
	
	/**
	 * Current score for the game
	 */
	public Long currentScore;
}
