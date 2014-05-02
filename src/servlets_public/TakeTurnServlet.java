package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servlets_private.TakeFinishedServlet;
import Json.GetMoveInput;
import Json.SocketMessage;
import Json.StatusResponse;
import Json.TakeTurnFinishedInput;
import Json.TakeTurnServletInput;
import Model.GameModel;
import Model.UrlPost;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.Text;

/**
 * Servlet that receives a post request allowing a player to take their turn
 *
 */
public class TakeTurnServlet extends HttpServlet{

	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = 4570152921495514409L;

	/**
	 * Handler for a post request that indicates a player is allowed to take their turn
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{

		System.out.println("TakeTurnServlet");

		Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		TakeTurnServletInput request = gson.fromJson(reader, TakeTurnServletInput.class);


		Long playerID = request.playerID;
		Long currScore = request.currentScore;

		// get current state of the board
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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

		// get the channel id corresponding to that player, if that player cannot be found
		// return a packet with status "fail"
		String token = null;

		Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
		query = new Query("Player", playerKey);
		List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

		System.out.println("myPlayerID "+ playerID);

		//find the player entity that matches the playerID in the request
		for (Entity entity: playerList) {
			System.out.println("otherPlayerID "+ (Long) entity.getProperty("playerID"));
			Long otherPlayerID = (Long) entity.getProperty("playerID");

			if (playerID.equals(otherPlayerID)) {

				//the next player is AI, so tell AI they should take their turn
				if((Boolean)entity.getProperty("isAI")){
					UrlPost postUtil = new UrlPost();
					GetMoveInput gmi = new GetMoveInput();
					gmi.playerID = playerID;
					gmi.gameURL = "https://inlaid-agility-567.appspot.com";
					gmi.treeDepth = 0;
					gmi.validMoves = GameModel.getValidMovesForPlayer(playerID, board);
					String data = postUtil.sendCallbackPost(gson.toJson(gmi), (String)entity.getProperty("AIUrl"));
					GameModel.storeCurrentBoard(data);
					TakeTurnFinishedInput ttfi = new TakeTurnFinishedInput();
					ttfi.board = data;
					TakeFinishedServlet tfs = new TakeFinishedServlet();
					tfs.doModPost(ttfi, req, resp);
					return;
				}
				else
					//next player is a human, store their token
					token = (String) entity.getProperty("token");
			}


		}

		if (token == null) {
			System.out.println("token is null");
			StatusResponse status = new StatusResponse("fail","player does not exist in the database");
			resp.getWriter().println(gson.toJson(status, StatusResponse.class));
			return;
		}

		// send him the current board along with a message saying hey, you can move now
		SocketMessage packet = new SocketMessage("updateView", board, true);
		ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));


		Transaction tx = datastore.beginTransaction();
		Key lastTurnKey = KeyFactory.createKey("LastTurn", "MyLastTurn");

		//delete the old store of the playerID that took the last turn
		query = new Query("lastTurn", lastTurnKey);
		List<Entity> turnList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));
		for (Entity deleteMe : turnList) {
			datastore.delete(deleteMe.getKey());
		}

		//store this playerID as the last player ot take a turn
		Entity lastTurn;
		try {
			lastTurn = new Entity("lastTurn", lastTurnKey);
			lastTurn.setProperty("playerID", playerID);
			datastore.put(lastTurn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tx.commit();


		// sending an updateview packet to that player
		System.out.println("sending to client a updateview packet, tokenId: "+token);
		ChannelService service = ChannelServiceFactory.getChannelService();
		service.sendMessage(message);

	}
}
