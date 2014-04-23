package Model;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;

public class GameModel {
	public static Key boardKey = KeyFactory.createKey("BoardKey", "MyBoard");

	
	public static void storeCurrentBoard(String board) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction tx = datastore.beginTransaction();
		Entity newBoard = null;
		try {
			newBoard = new Entity("Board", boardKey);
			newBoard.setProperty("board", board);
			datastore.put(newBoard);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tx.commit();
	}
}
