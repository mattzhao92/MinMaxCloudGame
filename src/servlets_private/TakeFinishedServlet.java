package servlets_private;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.TakeTurn;
import Json.SocketMessage;
import Json.StatusResponse;
import Json.TakeTurnFinishedInput;
import Model.GameModel;
import Model.UrlPost;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;

public class TakeFinishedServlet extends HttpServlet{

	private static final long serialVersionUID = 7486734051193470255L;
	private Gson gson = new Gson();
	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	
	private void redirectBrowserToPortal(Long user) {
		ChannelService channelService = ChannelServiceFactory
				.getChannelService();
		// String channelKey = getChannelKey(user);

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
		Query query = new Query("Player", playerKey);
		List<Entity> playerList = datastore.prepare(query).asList(
				FetchOptions.Builder.withLimit(5));

		String token = null;
		boolean isAI = false;
		String AIURL = "";
		System.out.println("myPlayerID " + user);
		for (Entity entity : playerList) {
			System.out.println("otherPlayerID "
					+ (Long) entity.getProperty("playerID"));
			Long otherPlayerID = (Long) entity.getProperty("playerID");
			if (user.equals(otherPlayerID)) {
				System.out.println("I match");
				isAI = (boolean) entity.getProperty("isAI");
				AIURL = (String) entity.getProperty("AIURL");
				token = (String) entity.getProperty("token");
			}
		}

		if (AIURL == null) AIURL = "";
		System.out.println("found the token for user " + user + ": " + token);

		
		Key newIDsKey = KeyFactory.createKey("PortalList", "MyPortalList");
	    query = new Query("Portal", newIDsKey);
	    List<Entity> portalList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));
	    
		
		System.out.println("redirectBrowserToPortal "+ portalList.size());
		if (portalList.size() > 0) {
			
			System.out.println(portalList.get(0).getProperty("inboundPortNumber"));
			
			String portalID = String.valueOf(portalList.get(0)
					.getProperty("inboundPortNumber"));
			
			Map<String, String> state = new HashMap<String, String>();
			state.put("redirectURL", GameModel.turnControlPath
					+ "/redirectToPortal");
			state.put("redirectURLData", "portalID=" + portalID+"&isAI="+isAI +"&AIURL="+AIURL+"&playerID="+user);
			
			SocketMessage packet = new SocketMessage("portalMove",gson.toJson(state), false);
			ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));
			channelService.sendMessage(message);
		
		}
	}

	
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{

		System.out.println("TakeFinishedServlet");
		final Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		TakeTurnFinishedInput request = gson.fromJson(reader, TakeTurnFinishedInput.class);

		final Long playerID;
		final Long currScore = 0L;
		Key lastTurnKey = KeyFactory.createKey("LastTurn", "MyLastTurn");

		Query query = new Query("lastTurn", lastTurnKey);
		List<Entity> turnList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));

		System.out.println("lastTurn List "+ turnList.size());
		System.out.println("x,y: " + request.x + ", " + request.y);

		if (turnList.size() == 1){
			Entity lastTurn = turnList.get(0);
			playerID = (Long) lastTurn.getProperty("playerID");
		} else {
			System.out.println("About to return");
			return;
		}
		
		final UrlPost postUtil = new UrlPost();

		if (request.x == 0 && request.y == 0) {
			System.out.println("1111111111111111");
			redirectBrowserToPortal(playerID);
		} else {
			TakeTurn tf = new TakeTurn(playerID, currScore);
			postUtil.sendPost(gson.toJson(tf, TakeTurn.class), GameModel.turnControlPath +"/turnFinished");
		}
		// At this point, we can send back a packet with status "ok"
		StatusResponse response = new StatusResponse("ok", "sent turn finished to TC");
		resp.getWriter().println(gson.toJson(response, StatusResponse.class));
	}
}
