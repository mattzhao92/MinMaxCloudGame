package Json;

import java.util.ArrayList;

/**
 * Response packet for apply next move servlet
 * contains boards resulting from next players move
 *
 */
public class ApplyNextMoveResponse {

	/**
	 * Resulting board from each valid move of the next player
	 */
	public ArrayList<String> boardsNextLevel;
	
	/**
	 * Current depth of min max tree
	 */
	public int TreeDepth;
	
	/**
	 * Construct the response
	 * @param validMovesIn
	 * @param TreeDepthIn
	 */
	public ApplyNextMoveResponse(ArrayList<String> validMovesIn, int TreeDepthIn) {
		this.boardsNextLevel = validMovesIn;
		this.TreeDepth = TreeDepthIn;

	}
}
