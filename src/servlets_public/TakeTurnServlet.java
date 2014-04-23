package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import post.TurnFinishedPost;
import request.TakeTurn;
import Json.JoinSubGameInput;
import Json.SocketMessage;
import Json.StatusResponse;
import Json.TakeTurnServletInput;
import Model.GameModel;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;

public class TakeTurnServlet extends HttpServlet{
	private Gson gson = new Gson();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		System.out.println("TakeTurnServlet");

		Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		TakeTurnServletInput request = gson.fromJson(reader, TakeTurnServletInput.class);
	
		
		Long playerID = Long.parseLong(request.playerID);
		Long currScore = Long.parseLong(request.currentScore);
		
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
	    
	    String board = (String) boardList.get(0).getProperty("board");
		
		// get the channel id corresponding to that player, if that player cannot be found
	    // return a packet with status "fail"
	    String token = null;
	    
	    Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
	    query = new Query("Player", playerKey).addSort("name", Query.SortDirection.DESCENDING);
	    List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
	   
    	for (Entity entity: playerList) {
			Long otherPlayerID = (Long) entity.getProperty("playerID");
			if (playerID.equals(otherPlayerID)) {
				token = (String) entity.getProperty("token");
			}
    	}
    	
    	if (token == null) {
    		StatusResponse status = new StatusResponse("fail","player does not exist in the database");
			resp.getWriter().println(gson.toJson(status, StatusResponse.class));
			return;
    	}
	    
	    // send him the current board along with a message saying hey, you can move now
		SocketMessage packet = new SocketMessage("updateView", board, true);
		ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));
		
//		TakeTurn tf = new TakeTurn(playerID, currScore + 1);
//		TurnFinishedPost tfp = new TurnFinishedPost();
//		String result = tfp.run(tf);
//		
//		// At this point, we can send back a packet with status "ok"
//		resp.getWriter().println(result);
		
	}
}
