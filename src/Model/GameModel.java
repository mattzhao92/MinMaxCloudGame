package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import request.ExceptionStringify;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.devrel.samples.ttt.Cell;
import com.google.devrel.samples.ttt.CellContainer;
import com.google.gson.Gson;

/**
 * The Game Model class
 *
 */
public class GameModel {
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	public static Key boardKey = KeyFactory.createKey("BoardKey", "MyBoard");
    public static final Logger log = Logger.getLogger(GameModel.class.getName());

    
	public static String turnControlPath = "https://1-dot-turncontrol.appspot.com";
	public static String gameServerPath = "https://1-dot-gameserver4052.appspot.com";

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
			Query query = new Query("Board", GameModel.boardKey);
			List<Entity> boardList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

			for (Entity e : boardList) {
				datastore.delete(e.getKey());
			}

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
	 * get the string representing the board from the datastore
	 * As a "Board", boardKey entity
	 * @param board
	 */
	public static String getCurrentBoard() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key boardKey = GameModel.boardKey;
		Query query = new Query("Board", boardKey);
		List<Entity> boardList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

		//Send the board to resp
		System.out.println("getCurrentBoard: board list size "+boardList.size());
		if (boardList.size() == 1) {
			String board = ((Text) boardList.get(0).getProperty("board")).getValue();
			System.out.println("getCurrentBoard: returning board "+board);
			return board;
		} else {
			return null;
		}
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
		if(r == 0 && c == 0){
			if(bad(cells[r+1][c]) && bad(cells[r][c+1]))
				return true;
		}
		if(r == cells.length - 1 && c == 0){
			if(bad(cells[r-1][c]) &&  bad(cells[r][c+1]))
				return true;
		}
		if(r == 0 && c == cells[r].length - 1){
			if(bad(cells[r][c-1]) && bad(cells[r+1][c]))
				return true;
		}
		if(r == cells.length - 1 && c == cells[r].length - 1){
			if(bad(cells[r][c-1]) && bad(cells[r-1][c]))
				return true;
		}

		if(r > 0 && r < cells.length - 1 && c == 0){
			if(bad(cells[r-1][c]) && bad(cells[r+1][c]) && bad(cells[r][c+1]))
				return true;
		}

		if(r > 0 && r < cells.length - 1 && c == cells[r].length - 1){
			if(bad(cells[r-1][c]) && bad(cells[r+1][c]) && bad(cells[r][c-1]))
				return true;
		}

		if(r == 0 && c > 0 && c < cells[r].length - 1){
			if(bad(cells[r][c-1]) && bad(cells[r][c+1]) && bad(cells[r+1][c])){
				return true;
			}
		}

		if(r == cells.length - 1 && c > 0 && c < cells[r].length - 1){
			if(bad(cells[r][c-1]) && bad(cells[r][c+1]) && bad(cells[r-1][c]))
				return true;
		}

		if(r > 0 && r < cells.length - 1 && c > 0 && c < cells[r].length - 1){
			if(bad(cells[r-1][c]) && bad(cells[r+1][c]) && bad(cells[r][c-1]) && bad(cells[r][c+1]))
				return true;
		}
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
	protected static boolean isValidMove(String[][] cells, int newRow, int newCol){
		if(cells[newRow][newCol].equals("None"))
			return true;

		return false;
	}

	/**
	 * Converts from an ArrayList of Cell objects to a two dimensional string matrix
	 * @param cells
	 * @return
	 */
	public static String[][] convertFromArrayListToMatrix(ArrayList<Cell> cells){
		String[][] cellMatrix = new String[4][4];
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < 4; j++){
				cellMatrix[i][j] = cells.get(i*j).playerName;
			}
		}
		return cellMatrix;
	}

	public static int getPayoff(Long playerID, String board){
		//System.out.println("in inner getPayoff: " + playerID + " | board: " + board);
		int ret = 0;
		try{
			Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
			Query query = new Query("Player", playerKey);
			List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));
			String playerName = null;
			for (Entity existingEntity : playerList) {
				if(((Long)existingEntity.getProperty("playerID")).equals(playerID)){
					playerName = (String)existingEntity.getProperty("playerName");
				}
			}
			if(playerName == null)
				return 0;
			Gson gson = new Gson();
			//System.out.println("getValidMovesForPlayer " + board);
			CellContainer cellContainer = gson.fromJson(board, CellContainer.class);
			ArrayList<Cell> cells = cellContainer.cells;

			//System.out.println("player name: " + playerName);
			HashMap<String, Integer> payoffs = new HashMap<String, Integer>();
			for (Cell cell : cells ) {
				if (!cell.playerName.equals("None")) {
					int value = 0;
					
					if (cell.x == 0 || (cell.y == 0 || cell.y == 1)) {
						long portalID = 0;
						
						Key newIDsKey = KeyFactory.createKey("PortalList", "MyPortalList");

					    Query portalIDquery = new Query("Portal", newIDsKey);
					    List<Entity> newIDsList = datastore.prepare(portalIDquery).asList(FetchOptions.Builder.withLimit(500));
					   
					    if (newIDsList.size() != 1) {
					    	throw new RuntimeException ("new portal id record can be found!");
					    }
				
						Entity portalIDs = newIDsList.get(0);
						portalID = (long) portalIDs.getProperty("outboundPortNumber");
	
						// make a get request to GetScoreForPortalServlet
						UrlPost httpUtil = new UrlPost();
						
						log.info("GameModel: GetScoreForPortalServlet portalID " + portalID + " playerID " + playerID);

						String portalMoveScoreStr = httpUtil.sendGet("?portalID="+portalID +"&playerID="+playerID, GameModel.gameServerPath+"/getScoreForPortal");
						log.info("GameModel: portalMoveScoreStr is "+portalMoveScoreStr);
						int portalMoveScore = Integer.parseInt(portalMoveScoreStr);
						value = portalMoveScore;
					}
					
					if (cell.playerName.equals(playerName)){
						ret+= value;//cell.val;
					}
					else if(!cell.playerName.equals("None")){
						payoffs.put(cell.playerName, payoffs.get(cell.playerName) + value);
					}
				}

			}
			int max = -1;
			for(Integer val : payoffs.values()){
				if(val > max)
					max = val;
			}
			ret = ret - max;
			//System.out.println("returning w/: " + ret);
		}
		catch(Exception e){
			ExceptionStringify es = new ExceptionStringify(e);
			//System.out.println(es.run());
		}
		return ret;
	}

	/**
	 * Get a list of all valid moves on the board for a specific player
	 * @param playerID
	 * @param board
	 * @return
	 */
	public static ArrayList<String> getValidMovesForPlayer(Long playerID, String board) {
		Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
		Query query = new Query("Player", playerKey);
		List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));
		String playerName = null;
		for (Entity existingEntity : playerList) {
			if(((Long)existingEntity.getProperty("playerID")).equals(playerID)){
				playerName = (String)existingEntity.getProperty("playerName");
			}
		}
		if(playerName == null)
			return new ArrayList<String>();
		Gson gson = new Gson();
		//System.out.println("getValidMovesForPlayer " + board);
		CellContainer cellContainer = gson.fromJson(board, CellContainer.class);
		ArrayList<Cell> cells = cellContainer.cells;

		Cell currPos = new Cell();
		for (Cell cell : cells ) {
			if (cell.playerName.equals(playerName)){
				currPos = cell;
			}
		}

		ArrayList<String> validMoves = new ArrayList<String>();
		for (Cell cell : cells ) {
			if(cell.playerName.equals("None")){
				ArrayList<Cell> newBoard = new ArrayList<Cell>();
				for(Cell c : cells){
					newBoard.add(c.clone());
				}
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
