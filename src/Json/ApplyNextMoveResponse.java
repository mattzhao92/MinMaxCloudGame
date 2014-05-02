package Json;

import java.util.ArrayList;

public class ApplyNextMoveResponse {
	public ArrayList<String> boardsNextLevel;
	
	public ApplyNextMoveResponse(ArrayList<String> validMovesIn) {
		this.boardsNextLevel = validMovesIn;
	}
}
