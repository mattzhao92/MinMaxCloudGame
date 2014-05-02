package Model;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.devrel.samples.ttt.Cell;
import com.google.devrel.samples.ttt.CellContainer;

/**
 * The Game Model class
 *
 */
public class GameModel {
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	public static Key boardKey = KeyFactory.createKey("BoardKey", "MyBoard");

	public static String turnControlPath = "http://localhost:8887";
	public static String gameServerPath = "http://localhost:8886";

	//public static String turnControlPath = "https://1-dot-striped-buckeye-555.appspot.com";
	//public static String gameServerPath = "https://app405cloudgame.appspot.com";


	/**
	 * Stores the string representing the board into the datastore
	 * As a "Board", boardKey entity
	 * @param board
	 */
	public static void storeCurrentBoard(String board) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction tx = datastore.beginTransaction();
		Entity newBoard = null;
		try {
			newBoard = new Entity("Board", boardKey);
			com.google.appengine.api.datastore.Text text = new com.google.appengine.api.datastore.Text(board);
			newBoard.setProperty("board", text);
			datastore.put(newBoard);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tx.commit();
	}

	/**
	 * Convenience method
	 * Equivalent to theirPlayerName.equals("None");
	 * @param theirPlayerName
	 * @return
	 */
	public static boolean bad(String theirPlayerName){
		if(theirPlayerName.equals("None"))
			return true;
		return false;
	}

	/**
	 * Checks if a player at row 'r' and col 'c' has any valid adjacent moves
	 * @param cells
	 * @param r
	 * @param c
	 * @return
	 */
	public static boolean checkIfStuck(String cells[][], int r, int c){
		
		//Check if stuck in upper left corner
		if(r == 0 && c == 0){
			if(bad(cells[r+1][c]) && bad(cells[r][c+1]))
				return true;
		}
		
		//Check if stuck in bottom left corner
		if(r == cells.length - 1 && c == 0){
			if(bad(cells[r-1][c]) &&  bad(cells[r][c+1]))
				return true;
		}
		
		//Check if stuck in top right corner
		if(r == 0 && c == cells[r].length - 1){
			if(bad(cells[r][c-1]) && bad(cells[r+1][c]))
				return true;
		}
		
		//Check if stuck in bottom right corner
		if(r == cells.length - 1 && c == cells[r].length - 1){
			if(bad(cells[r][c-1]) && bad(cells[r-1][c]))
				return true;
		}

		//Check if stuck in left-most column but not in corner
		if(r > 0 && r < cells.length - 1 && c == 0){
			if(bad(cells[r-1][c]) && bad(cells[r+1][c]) && bad(cells[r][c+1]))
				return true;
		}

		//Check if stuck in right-most column but not in corner
		if(r > 0 && r < cells.length - 1 && c == cells[r].length - 1){
			if(bad(cells[r-1][c]) && bad(cells[r+1][c]) && bad(cells[r][c-1]))
				return true;
		}
		
		//Check if stuck in top row but not in corner
		if(r == 0 && c > 0 && c < cells[r].length - 1){
			if(bad(cells[r][c-1]) && bad(cells[r][c+1]) && bad(cells[r+1][c])){
				return true;
			}
		}

		//Check if stuck in bottom row but not in corner
		if(r == cells.length - 1 && c > 0 && c < cells[r].length - 1){
			if(bad(cells[r][c-1]) && bad(cells[r][c+1]) && bad(cells[r-1][c]))
				return true;
		}

		//Check if we're stuck somewhere in the middle
		if(r > 0 && r < cells.length - 1 && c > 0 && c < cells[r].length - 1){
			if(bad(cells[r-1][c]) && bad(cells[r+1][c]) && bad(cells[r][c-1]) && bad(cells[r][c+1]))
				return true;
		}
		
		//We must not be stuck
		return false;
	}

	/**
	 * Check if a given move is valid based on a player's current location
	 * @param cells
	 * @param oldRow	the current row of the player
	 * @param oldCol	the current column of the player
	 * @param newRow	the row of the proposed move
	 * @param newCol	the column of the proposed move
	 * @return
	 */
	protected static boolean isValidMove(String[][] cells, int oldRow, int oldCol, int newRow, int newCol){
		
		//Check if proposed move location is available
		if(cells[newRow][newCol].equals("None"))
			return false;
		
		//If player is stuck, any move is valid
		if(checkIfStuck(cells, oldRow, oldCol))
			return true;

		//Check if move is adjacent to current location
		int xDiff = Math.abs(newRow - oldRow);
		int yDiff = Math.abs(newCol - oldCol);
		
		if (yDiff + xDiff == 1) {
			return true;
		}

		return false;
	}

	/**
	 * Converts from an ArrayList of Cell objects to a two dimensional string matrix
	 * @param cells
	 * @return
	 */
	public static String[][] convertFromArrayListToMatrix(ArrayList<Cell> cells){
		String[][] cellMatrix = new String[3][3];
		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 3; j++){
				cellMatrix[i][j] = cells.get(i*j).playerName;
			}
		}
		return cellMatrix;
	}

	/**
	 * Get a list of all valid moves on the board for a specific player
	 * @param playerID
	 * @param board
	 * @return
	 */
	public static ArrayList<String> getValidMovesForPlayer(Long playerID, String board) {
		
		//Get the list of players from the datastore 
		Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
		Query query = new Query("Player", playerKey);
		List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));
		
		//Find the playerName associated with the paramater playerID
		String playerName = null;
		for (Entity existingEntity : playerList) {
			if(((Long)existingEntity.getProperty("playerID")).equals(playerID)){
				playerName = (String)existingEntity.getProperty("playerName");
			}
		}
		
		//If no playerName found, no valid moves to return
		if(playerName == null)
			return new ArrayList<String>();
		
		
		CellContainer cellContainer = CellContainer.fromJson(board);
		ArrayList<Cell> cells = cellContainer.cells;

		//Find the current position of the player
		Cell currPos = new Cell();
		for (Cell cell : cells ) {
			if (cell.playerName.equals(playerName)){
				currPos = cell;
			}
		}

		//Find all valid move options for the player
		String[][] cellsMatrix = convertFromArrayListToMatrix(cells);
		ArrayList<String> validMoves = new ArrayList<String>();
		
		for (Cell cell : cells ) {
			if(isValidMove(cellsMatrix, currPos.x, currPos.y, cell.x, cell.y)){
				//If this cell is a valid move, create a new board to represent that move
				ArrayList<Cell> newBoard = cells;
				for(Cell subCell : newBoard){
					if(subCell.x == cell.x && subCell.y == cell.y){
						subCell.playerName = playerName;
					}
				}
				validMoves.add(CellContainer.toJson(new CellContainer(newBoard)));
			}
		}

		return validMoves;
	}
}
