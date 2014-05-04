package servlets_private;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.*;

import Json.*;
import Model.PlayerMap;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.devrel.samples.ttt.PMF;
import com.google.gson.Gson;

public class BroadCastMoveServlet extends HttpServlet{
	private Gson gson = new Gson();
	private MemcacheService syncCache = (MemcacheService) MemcacheServiceFactory.getMemcacheService();
	private ChannelService channelService = ChannelServiceFactory.getChannelService();

	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));

		BroadCastMessage broadcastmsg = gson.fromJson(reader, BroadCastMessage.class);
		System.out.println("broadcastmsg "+ broadcastmsg.playername + " " + broadcastmsg.board);

		String fromPlayer = broadcastmsg.playername;

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
	    Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");

	    // Run an ancestor query to ensure we see the most up-to-date
	    // view of the Greetings belonging to the selected Guestbook.
	  
	    Query query = new Query("Player", playerKey);
	    List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
	   
    	for (Entity entity: playerList) {
			String playername = (String) entity.getProperty("playerName");
			if (!playername.equals(fromPlayer)) {
				String token = (String) entity.getProperty("token");
				//System.out.println("broadCasting to player: "+playername);

				SocketMessage packet = new SocketMessage("updateView", broadcastmsg.board, false);
				ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));
				channelService.sendMessage(message);
				//channelService.sendMessage(message);
			}
    	}		
	}
}
