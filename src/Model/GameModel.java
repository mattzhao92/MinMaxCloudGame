package Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.devrel.samples.ttt.Board;
import com.google.devrel.samples.ttt.Cell;
import com.google.devrel.samples.ttt.CellContainer;
import com.google.gson.Gson;

public class GameModel {
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	public static Key boardKey = KeyFactory.createKey("BoardKey", "MyBoard");
	public static String turnControlPath = "http://localhost:8887";
	public static String gameServerPath = "http://localhost:8886";
	
	//public static String turnControlPath = "http://localhost:8887";
	//public static String gameServerPath = "http://localhost:8888";
	
	//public static String turnControlPath = "https://1-dot-striped-buckeye-555.appspot.com";
	//public static String gameServerPath = "https://app405cloudgame.appspot.com";
	
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
	
	public static boolean bad(String theirPlayerName){
        if(theirPlayerName.equals("None"))
            return true;
        return false;
    }
	
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
	    
	    protected static boolean isValidMove(String[][] cells, int newRow, int newCol){
	        if(cells[newRow][newCol].equals("None"))
	            return true;
	        
	        return false;
	    }
	
	    
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
		int ret = 0;
		
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
	    System.out.println("getValidMovesForPlayer " + board);
		CellContainer cellContainer = gson.fromJson(board, CellContainer.class);
		ArrayList<Cell> cells = cellContainer.cells;
		
		Cell currPos = new Cell();
		for (Cell cell : cells ) {
			if (cell.playerName.equals(playerName)){
				ret+= cell.val;
			}
		}
		
		return ret;
	}
	
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
	    System.out.println("getValidMovesForPlayer " + board);
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
