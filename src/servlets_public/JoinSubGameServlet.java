package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

import Json.GameInfo;
import Json.StatusResponse;
import Json.JoinSubGameMessage;
import Model.GameModel;

/**
 * Servlet that allows a player to join a sub game
 *
 */
public class JoinSubGameServlet extends HttpServlet {

	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = -8974201704590761219L;
	
	private ChannelService channelService = ChannelServiceFactory.getChannelService();
	private Random random = new Random();
	private Gson gson = new Gson();

	/**
	 * Handler for a post request that allows the player to join a subgame
	 * and responds with the url to the game GUI
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		resp.addHeader("Access-Control-Allow-Origin", "*");
		System.out.println("JoinSubGameServlet");
		
		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		JoinSubGameMessage request = gson.fromJson(reader, JoinSubGameMessage.class);
		Long playerID = request.playerID;
		String playerName = request.playerName;
		boolean isAI = request.isAI;
		String AIUrl = request.AIUrl;
		String gameUrl = request.gameURL;
		
		// generate a channel id/token for the player
		String channelKey = ""+playerID + random.nextDouble() * 100;
		String token = channelService.createChannel(channelKey);
		request.token = token;
		
		// store player information into the database
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		GameInfo gameInfo = new GameInfo(token, false);
		
		Transaction tx = datastore.beginTransaction();
		Entity newPlayer = null;
	    
		boolean errorOccured = false;
		Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
		try {
			newPlayer = new Entity("Player", playerKey);
			newPlayer.setProperty("playerName", playerName);
			newPlayer.setProperty("playerID", playerID);
			newPlayer.setProperty("token", token);
			newPlayer.setProperty("AIUrl", AIUrl);
			newPlayer.setProperty("isAI", isAI);
			newPlayer.setProperty("gameURL", gameUrl);
			datastore.put(newPlayer);
		} catch (Exception e) {
			errorOccured = true;
			e.printStackTrace();
		}
		tx.commit();
	
		//respond with error or URL to game GUI
		StatusResponse jsonresp;
		if (errorOccured) {
			jsonresp = new StatusResponse("fail", "exception occured when storing player");
			resp.getWriter().println(gson.toJson(jsonresp, StatusResponse.class));

		} else {
			// changeme
			resp.getWriter().println(GameModel.gameServerPath+"/gameGUI?data=" + URLEncoder.encode(gson.toJson(request, JoinSubGameMessage.class), "UTF-8"));
		}
	}
}
