package Json;

import java.util.ArrayList;

/**
 * Input to callback posts to the AI URL
 *
 */
public class GetMoveInput {

	/**
	 * the player ID
	 */
	public Long playerID;

	/**
	 * List of valid moves for the player
	 */
	public ArrayList<String> ValidMoves;

	/**
	 * The current depth of the min max computation tree
	 */
	public int TreeDepth;

	/**
	 * the URL of the game
	 */
	public String GameURL;

}
