package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.TakeTurn;

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
import com.google.gson.Gson;
import com.google.appengine.api.datastore.*;
import com.google.devrel.samples.ttt.Cell;
import com.google.devrel.samples.ttt.CellContainer;

import Json.GameInfo;
import Json.SocketMessage;
import Json.StatusResponse;
import Json.JoinSubGameMessage;
import Model.GameModel;
import Model.UrlPost;

/**
 * Servlet that allows a player to join a sub game
 *
 */
public class JoinSubGameServlet extends HttpServlet {

	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = -8974201704590761219L;
    public static final Logger log = Logger.getLogger(JoinSubGameServlet.class.getName());

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
		long score = request.playerScore;
		
		log.info("player has a score " + score);

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
			newPlayer.setProperty("isAI", isAI);
			newPlayer.setProperty("AIUrl", AIUrl);
			newPlayer.setProperty("gameURL", gameUrl);
			newPlayer.setProperty("score", score);
			
			System.out.println("PlayerName: "+ playerName);
			System.out.println("PlayerID: "+playerID);
			System.out.println("token: "+token);
			System.out.println("isAI: "+isAI);
			System.out.println("AIURL: "+AIUrl);
			System.out.println("gameURL " + gameUrl);
			//System.out.println("JoinSubGame isAI "+ isAI);
			datastore.put(newPlayer);
		} catch (Exception e) {
			errorOccured = true;
			e.printStackTrace();
		}
		tx.commit();

		tx = datastore.beginTransaction();
		Query query = new Query("Board", GameModel.boardKey);
		List<Entity> boardList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

		if (boardList == null || boardList.size() != 1) {
			String msg = "We dont have a board "+ boardList.size();
			StatusResponse status = new StatusResponse("fail",msg);
			resp.getWriter().println(gson.toJson(status, StatusResponse.class));
			System.err.println(msg);
			return;
		}

		String board = ((Text) boardList.get(0).getProperty("board")).getValue();
		CellContainer container = gson.fromJson(board, CellContainer.class);

		for (int i = 0; i < container.cells.size(); i++) {
			Cell cell =  container.cells.get(i);
			if (cell.playerName.equals("None") && !((cell.x == 0 && cell.y == 0) || (cell.x == 0 && cell.y == 1))) {
				cell.playerName = playerName;
				break;
			}
		}

		datastore.delete(boardList.get(0).getKey());
		tx.commit();
		String newBoard = CellContainer.toJson(container);
		GameModel.storeCurrentBoard(newBoard);


		query = new Query("Player", playerKey);
		List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

		//Broadcast message to every player in player list besides the sender
		for (Entity entity: playerList) {

			String playername = (String) entity.getProperty("playerName");
			token = (String) entity.getProperty("token");
			System.out.println("broadCasting to player: "+playername);

			SocketMessage packet = new SocketMessage("updateView", newBoard, true);
			ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));
			channelService.sendMessage(message);
		}	
		
		UrlPost postUtil = new UrlPost();
		TakeTurn tf = new TakeTurn(playerID, score);
		String response = postUtil.sendCallbackPost(gson.toJson(tf, TakeTurn.class), GameModel.turnControlPath +"/turnFinished");
		System.out.println("taketurnfinished before redirect " + response);
		
		
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
