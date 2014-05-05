package Json;

/**
 * Input for calls to get payoff servlet
 *
 */
public class GetPayoffInput {
	
	/**
	 * playerID making the request
	 */
	public Long nextPlayerID;
	
	/**
	 * board to get the payoff value for
	 */
	public String currentBoard;
}
