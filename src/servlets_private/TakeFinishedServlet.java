package servlets_private;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
import com.google.appengine.api.datastore.Transaction;
import com.google.devrel.samples.ttt.Cell;
import com.google.devrel.samples.ttt.CellContainer;
import com.google.gson.Gson;

public class TakeFinishedServlet extends HttpServlet{

	private static final long serialVersionUID = 7486734051193470255L;
	private Gson gson = new Gson();
    public static final Logger log = Logger.getLogger(TakeFinishedServlet.class.getName());
	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	
	private void redirectBrowserToPortal(Long user, long score) {
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
		String playerName = "";
		System.out.println("myPlayerID " + user);
		for (Entity entity : playerList) {
			System.out.println("otherPlayerID "
					+ (Long) entity.getProperty("playerID"));
			Long otherPlayerID = (Long) entity.getProperty("playerID");
			if (user.equals(otherPlayerID)) {
				System.out.println("I match");
				playerName = (String) entity.getProperty("playerName");
				isAI = (boolean) entity.getProperty("isAI");
				AIURL = (String) entity.getProperty("AIUrl");
				token = (String) entity.getProperty("token");
			}
		}

		//if (AIURL == null) AIURL = "";

		
		Key newIDsKey = KeyFactory.createKey("PortalList", "MyPortalList");
	    query = new Query("Portal", newIDsKey);
	    List<Entity> portalList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));
	    
		
		System.out.println("redirectBrowserToPortal "+ portalList.size());
		if (portalList.size() > 0) {
			
			System.out.println(portalList.get(0).getProperty("inboundPortNumber"));
			System.out.println(portalList.get(0).getProperty("outboundPortNumber"));
			
			String portalID = String.valueOf(portalList.get(0)
					.getProperty("outboundPortNumber"));
			
			Map<String, String> state = new HashMap<String, String>();
			state.put("redirectURL", GameModel.turnControlPath
					+ "/redirectToPortal");
			
			state.put("redirectURLData", "portalID=" + portalID+"&isAI="+isAI +"&AIURL="+"\""+AIURL+"\""+"&playerName=\""+playerName+"\"&playerID="+user+"&gameUrl="+"\""+GameModel.gameServerPath+"\""+"&playerScore="+score);
			
			SocketMessage packet = new SocketMessage("portalMove",gson.toJson(state), false);
			ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));
			channelService.sendMessage(message);
		
		}
		
		for (Entity entity : playerList) {
			Long otherPlayerID = (Long) entity.getProperty("playerID");
			if (user.equals(otherPlayerID)) {
				datastore.delete(entity.getKey());
			}
		}
	}

	public int computeDifference(String oldBoard, String newBoard, Long playerID){
		int ret = 0;
		CellContainer oldBoardContainer = gson.fromJson(oldBoard, CellContainer.class);
		ArrayList<Cell> oldBoardCells = oldBoardContainer.cells;
		CellContainer newBoardContainer = gson.fromJson(newBoard, CellContainer.class);
		ArrayList<Cell> newBoardCells = newBoardContainer.cells;
		for(int i = 0; i < oldBoardCells.size(); i++){
			Cell oldCell = oldBoardCells.get(i);
			Cell newCell = newBoardCells.get(i);
			if(oldCell.playerName.equals("None") && !newCell.playerName.equals("None")){
				ret = newCell.val;
				break;
			}
		}
		
		return ret;
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
		
		String oldBoard = GameModel.getCurrentBoard();
		int diff = computeDifference(oldBoard, request.board, playerID);
		GameModel.storeCurrentBoard(request.board);
		
		

		
		final UrlPost postUtil = new UrlPost();

		Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
		query = new Query("Player", playerKey);
		List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

		System.out.println("myPlayerID "+ playerID);
		long score = 0;
		//find the player entity that matches the playerID in the request
		for (Entity entity: playerList) {
			if((entity.getProperty("playerID").toString()).equals(playerID.toString())) {
				score = (long)entity.getProperty("score") + diff;
			}
		}
		
		
		if (request.x == 0 && (request.y == 0 || request.y == 1)) {
			System.out.println(">>>>> 1111111111111111");
			log.info("redirectBrowserToPortal score " + score);
			
			redirectBrowserToPortal(playerID, score);
		} else {
			log.info("turnFinished score " + score);

			TakeTurn tf = new TakeTurn(playerID, score);
			postUtil.sendPost(gson.toJson(tf, TakeTurn.class), GameModel.turnControlPath +"/turnFinished");
		}
		
		// At this point, we can send back a packet with status "ok"
		StatusResponse response = new StatusResponse("ok", "sent turn finished to TC");
		resp.getWriter().println(gson.toJson(response, StatusResponse.class));
	}
}
