package Json;

import java.util.ArrayList;

public class ApplyNextMoveResponse {
	public ArrayList<String> boardsNextLevel;
	public int TreeDepth;
	
	public ApplyNextMoveResponse(ArrayList<String> validMovesIn, int TreeDepthIn) {
		this.boardsNextLevel = validMovesIn;
		this.TreeDepth = TreeDepthIn;

	}
}
