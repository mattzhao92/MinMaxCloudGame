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
import Json.JoinSubGameInput;

public class JoinSubGameServlet extends HttpServlet {

	private ChannelService channelService = ChannelServiceFactory.getChannelService();
	private Random random = new Random();
	private Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		resp.addHeader("Access-Control-Allow-Origin", "*");
		System.out.println("JoinSubGameServlet");
		
//		JoinSubGameInput temp = new JoinSubGameInput();
//		temp.AIUrl = "http://AIString";
//		temp.isAI = false;
//		temp.playerID = "KyleID";
//		temp.playerName = "Kyle";
//		temp.token = "kyleToken";
//		if (true) {
//			resp.getWriter().println("http://localhost:8888/gameGUI?data=" + URLEncoder.encode(gson.toJson(temp, JoinSubGameInput.class), "UTF-8"));
//			return;
//		}
		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		JoinSubGameInput request = gson.fromJson(reader, JoinSubGameInput.class);
		String playerID = request.playerID;
		String playerName = request.playerName;
		boolean isAI = request.isAI;
		String AIUrl = request.AIUrl;
		String gameUrl = request.gameURL;
		
		// generate a channel id/token for the player
		String channelKey = playerID + random.nextDouble() * 100;
		String token = channelService.createChannel(channelKey);
		request.token = token;
		
		// storing <playerName, his token> into database
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
			newPlayer.setProperty("gameURL", gameUrl);
			datastore.put(newPlayer);
		} catch (Exception e) {
			errorOccured = true;
			e.printStackTrace();
		}
		tx.commit();
	
		StatusResponse jsonresp;
		if (errorOccured) {
			jsonresp = new StatusResponse("fail", "exception occured when storing player");
			resp.getWriter().println(gson.toJson(jsonresp, StatusResponse.class));

		} else {
			// changeme
			resp.getWriter().println("http://localhost:8888/gameGUI?data=" + URLEncoder.encode(gson.toJson(request, JoinSubGameInput.class), "UTF-8"));
		}
	}
}
