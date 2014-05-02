package Json;


/**
 * Format of the input for apply next move servlet requests
 *
 */
public class ApplyNextMoveInput {
	
	/**
	 * ID of the next player
	 */
	public Long nextPlayerID;
	
	/**
	 * Current board state
	 */
	public String currentBoard;
	
	/**
	 * Current depth of the min max computation tree
	 */
	public int TreeDepth;	
}
