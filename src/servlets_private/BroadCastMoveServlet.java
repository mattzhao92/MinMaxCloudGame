package servlets_private;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.*;

import Json.*;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

/**
 * Servlet that Broadcasts a move to all players
 *
 */
public class BroadCastMoveServlet extends HttpServlet{

	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = 5499819133155527536L;
	private Gson gson = new Gson();
	private ChannelService channelService = ChannelServiceFactory.getChannelService();

	/**
	 * Post request handler that broadcasts a message from request req to all players
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));

		BroadCastMessage broadcastmsg = gson.fromJson(reader, BroadCastMessage.class);
		System.out.println("broadcastmsg "+ broadcastmsg.playername + " " + broadcastmsg.board);

		String fromPlayer = broadcastmsg.playername;

		//Get the player list from the datastore
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
		Query query = new Query("Player", playerKey);
		List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

		//Broadcast message to every player in player list besides the sender
		for (Entity entity: playerList) {

			String playername = (String) entity.getProperty("playerName");
			if (!playername.equals(fromPlayer)) {
				String token = (String) entity.getProperty("token");
				System.out.println("broadCasting to player: "+playername);

				SocketMessage packet = new SocketMessage("updateView", broadcastmsg.board, true);
				ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));
				channelService.sendMessage(message);
			}
		}		
	}
}
