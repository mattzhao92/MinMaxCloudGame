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
import Json.PortalRedirectToClient;
import Json.SocketMessage;
import Json.StatusResponse;
import Json.TakeTurnFinishedInput;
import Model.GameModel;
import Model.ICallback;
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
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;

public class TakeFinishedServlet extends HttpServlet{

	private static final long serialVersionUID = 7486734051193470255L;
	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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
			final ChannelService channelService = ChannelServiceFactory.getChannelService();
			ICallback callback = new ICallback() {
				@Override
				public void execute(String response) {
					// parse to packet to be forwarded
					PortalRedirect request = gson.fromJson(response, PortalRedirect.class);

					
					Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");

					Query query = new Query("Player", playerKey);
					List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

					for (Entity entity: playerList) {
						Long playerId = (Long) entity.getProperty("playerID");
						if (playerId == playerID) {
							String token = (String) entity.getProperty("token");
							String playerName = (String) entity.getProperty("playerName");
							
							//TODO add isAI to Player table in db
							String aiUrl = (String)entity.getProperty("AIUrl");
							boolean isAI = true;
							if (aiUrl == null) isAI = false;
							PortalRedirectToClient prtc = new PortalRedirectToClient("ok", playerID,
									request.gameURL, request.inboundPortalID, playerName, isAI, aiUrl);

							System.out.println("TakeFinishedServlet sending reidrect packets");
							String data = gson.toJson(prtc, PortalRedirectToClient.class);
							SocketMessage packet = new SocketMessage("redirect", data, false);
							ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));
							channelService.sendMessage(message);
						}
					}

				}
			};
			postUtil.sendPost(gson.toJson(new PortalRequest(GameModel.getOutboundID(), playerID), PortalRequest.class), GameModel.turnControlPath + "/getGameURLFromPortal",callback);

		} else {
			TakeTurn tf = new TakeTurn(playerID, currScore);
			postUtil.sendPost(gson.toJson(tf, TakeTurn.class), GameModel.turnControlPath +"/turnFinished", null);
		}
		// At this point, we can send back a packet with status "ok"
		StatusResponse response = new StatusResponse("ok", "sent turn finished to TC");
		resp.getWriter().println(gson.toJson(response, StatusResponse.class));
	}
}
