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

public class GameModel {
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	public static Key boardKey = KeyFactory.createKey("BoardKey", "MyBoard");

	//public static String turnControlPath = "http://localhost:8887";
	//public static String gameServerPath = "http://localhost:8888";
	
	public static String turnControlPath = "https://1-dot-striped-buckeye-555.appspot.com";
	public static String gameServerPath = "https://app405cloudgame.appspot.com";
	
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
	    
	    protected static boolean isValidMove(String[][] cells, int oldRow, int oldCol, int newRow, int newCol){
	        if(cells[newRow][newCol].equals("None"))
	            return false;
	        //System.err.println("xDiff is: " + xDiff + " and yDiff is: " + yDiff);
	        if(checkIfStuck(cells, oldRow, oldCol))
	            return true;
	        
	        
	        int xDiff = Math.abs(newRow - oldRow);
	        int yDiff = Math.abs(newCol - oldCol);
	        
	        if (yDiff + xDiff == 1) {
	            return true;
	        }
	        
	        return false;
	    }
	
	    
	public static String[][] convertFromArrayListToMatrix(ArrayList<Cell> cells){
		String[][] cellMatrix = new String[3][3];
		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 3; j++){
				cellMatrix[i][j] = cells.get(i*j).playerName;
			}
		}
		return cellMatrix;
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
	    CellContainer cellContainer = CellContainer.fromJson(board);
		ArrayList<Cell> cells = cellContainer.cells;
		
		Cell currPos = new Cell();
		for (Cell cell : cells ) {
			if (cell.playerName.equals(playerName)){
				currPos = cell;
			}
		}
		
		String[][] cellsMatrix = convertFromArrayListToMatrix(cells);
		ArrayList<String> validMoves = new ArrayList<String>();
		for (Cell cell : cells ) {
			if(isValidMove(cellsMatrix, currPos.x, currPos.y, cell.x, cell.y)){
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
