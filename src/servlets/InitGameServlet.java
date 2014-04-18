package servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Json.*;
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
	private MemcacheService syncCache = (MemcacheService) MemcacheServiceFactory.getMemcacheService();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{

		Board newBoard = new Board();
		syncCache.put("boardState", newBoard.getState());
		syncCache.put("listOfPlayers", new HashMap<String, GameInfo>());
		syncCache.put("gameStarted", false);
		
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction tx = datastore.beginTransaction();
	    Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");

	    Query query = new Query("Player", playerKey).addSort("name", Query.SortDirection.DESCENDING);
	    List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

	    System.out.println("whyyyyyy  >>>>>>>>>>> player list has size "+ playerList.size());
	    
	    for (Entity existingEntity : playerList) {
	    	datastore.delete(existingEntity.getKey());
	    }
	   		
	    tx.commit();
		
		resp.getWriter().println(newBoard.getState());
	}
}
