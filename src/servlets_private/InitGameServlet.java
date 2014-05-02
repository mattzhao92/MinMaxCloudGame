package servlets_private;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.ExceptionStringify;
import Json.*;
import Model.GameModel;
import Model.PlayerMap;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.datastore.Transaction;
import com.google.devrel.samples.ttt.Board;
import com.google.devrel.samples.ttt.PMF;

public class InitGameServlet extends HttpServlet {
	private static final long serialVersionUID = -7935086544921717020L;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Transaction tx = datastore.beginTransaction();
		//deleting existing boardState
		
	    Query query = new Query("Board", GameModel.boardKey);
	    List<Entity> boardList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
	   
    	for (Entity entity: boardList) {
			datastore.delete(entity.getKey());
    	}
		tx.commit();
		
    	// creating a new board and store it in the database
		Board newBoard = new Board();
		
		GameModel.storeCurrentBoard(newBoard.getState());
		
		 
	    // resetting gameStarted boolean
		tx = datastore.beginTransaction();
		Key gameStartedKey = KeyFactory.createKey("GameStarted", "MyGameStarted");
		Entity gameStart =  new Entity("GameStarted", gameStartedKey);	
		gameStart.setProperty("gameStarted", false);
		datastore.put(gameStart);
		tx.commit();
		
		// deleting existing players
		tx = datastore.beginTransaction();
	    Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
	    query = new Query("Player", playerKey);
	    List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));
	    for (Entity existingEntity : playerList) {
	    	datastore.delete(existingEntity.getKey());
	    }
	    tx.commit();
	    
		// deleting existing portals
		tx = datastore.beginTransaction();
		Key portalKey = KeyFactory.createKey("PortalList", "MyPortalList");
		query = new Query("Portal", portalKey);
		List<Entity> portalList = datastore.prepare(query).asList(
				FetchOptions.Builder.withLimit(500));
		for (Entity existingEntity : portalList) {
			datastore.delete(existingEntity.getKey());
		}
		tx.commit();
		resp.getWriter().println(newBoard.getState());
	}
}
