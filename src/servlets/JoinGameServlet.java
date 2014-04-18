package servlets;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Json.*;
import Model.PlayerMap;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.devrel.samples.ttt.PMF;
import com.google.gson.Gson;

public class JoinGameServlet  extends HttpServlet{

	//private MemcacheService syncCache = (MemcacheService) MemcacheServiceFactory.getMemcacheService();
	private Gson gson = new Gson();
	private ChannelService channelService = ChannelServiceFactory.getChannelService();
	private Random random = new Random();


	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String playerName = req.getParameter("playername");
		if (playerName == null || playerName == "") {
			return;
		}

		boolean gameStarted = false; //= (boolean) syncCache.get("gameStarted");
		if (gameStarted) {
			JoinGameServletResponse jsonresp = new JoinGameServletResponse("error", "game has already started");
			resp.getWriter().println(gson.toJson(jsonresp, JoinGameServletResponse.class));
		} else {
			//HashMap<String, GameInfo> players = (HashMap<String, GameInfo>) syncCache.get("listOfPlayers");


			String channelKey = playerName + random.nextDouble() * 100;
			String token = channelService.createChannel(channelKey);
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			GameInfo gameInfo = new GameInfo(token, true);
			
			Transaction tx = datastore.beginTransaction();
			Entity newPlayer = null;
		    Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");

		    // Run an ancestor query to ensure we see the most up-to-date
		    // view of the Greetings belonging to the selected Guestbook.
		  
		    Query query = new Query("Player", playerKey).addSort("name", Query.SortDirection.DESCENDING);
		    List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
		   
	    	System.out.println("000000 >>>>>>>>>>> player list has size "+ playerList.size());
		   		
	    	if (playerList.size() == 0) {
	    		gameInfo.setEnableMosue(false);
	    	}
	    	
			try {
				newPlayer = new Entity("Player", playerKey);
				newPlayer.setProperty("name", playerName);
				newPlayer.setProperty("token", token);
				datastore.put(newPlayer);
			} catch (Exception e) {
				e.printStackTrace();
			}
			tx.commit();
			
			JoinGameServletResponse jsonresp = new JoinGameServletResponse("success", gameInfo.toJson());
			resp.getWriter().println(gson.toJson(jsonresp, JoinGameServletResponse.class));
		}
	}

}
