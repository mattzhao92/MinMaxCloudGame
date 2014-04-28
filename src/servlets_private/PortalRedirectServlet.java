package servlets_private;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.PortalRedirect;
import request.PortalRequest;
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

public class PortalRedirectServlet extends HttpServlet {

	private static final long serialVersionUID = -1157474598962538944L;
	
	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private ChannelService channelService = ChannelServiceFactory.getChannelService();

	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		PortalRedirect request = gson.fromJson(reader, PortalRedirect.class);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
	    Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");

	    // Run an ancestor query to ensure we see the most up-to-date
	    // view of the Greetings belonging to the selected Guestbook.
	  
	    Query query = new Query("Player", playerKey);
	    List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
	   
    	for (Entity entity: playerList) {
			Long playerId = (Long) entity.getProperty("playerID");
			if (playerId == request.playerID) {
				String token = (String) entity.getProperty("token");


				String data = gson.toJson(request, PortalRedirect.class);
				SocketMessage packet = new SocketMessage("redirect", data, false);
				ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));
				channelService.sendMessage(message);
			}
    	}
	}

}
