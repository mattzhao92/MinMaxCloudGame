package Model;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;

public class GameModel {
	public static Key boardKey = KeyFactory.createKey("BoardKey", "MyBoard");

	public static String turnControlPath = "http://localhost:8888";
	public static String gameServerPath = "http://localhost:8887";
	
	//public static String turnControlPath = "https://1-dot-striped-buckeye-555.appspot.com";
	//public static String gameServerPath = "https://app405cloudgame.appspot.com";
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
	
	public static int getInboundID() {
		return 0;
	}
	
	public static int getOutboundID() {
		return 1;
	}
}
