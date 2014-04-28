package servlets_private;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.PortalRequest;
import request.TakeTurn;
import Json.StatusResponse;
import Json.TakeTurnFinishedInput;
import Model.GameModel;
import Model.UrlPost;

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
		Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		TakeTurnFinishedInput request = gson.fromJson(reader, TakeTurnFinishedInput.class);
		
		Long playerID = -1L;
		Long currScore = 100L;
	    Key lastTurnKey = KeyFactory.createKey("LastTurn", "MyLastTurn");
	 
	    Query query = new Query("lastTurn", lastTurnKey);
	    List<Entity> turnList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));
	    
	    System.out.println("lastTurn List "+ turnList.size());
	    if (turnList.size() == 1){
	    	Entity lastTurn = turnList.get(0);
	    	playerID = (Long) lastTurn.getProperty("playerID");
	    } else {
	    	return;
	    }
	    
	    if (request.x == 3 && request.y == 2) {
	    	UrlPost postUtil = new UrlPost();
	    	postUtil.sendPost(gson.toJson(new PortalRequest(0, playerID), PortalRequest.class), GameModel.turnControlPath + "/getGameURLFromPortal");
	    }
	    else {
	    	TakeTurn tf = new TakeTurn(playerID, currScore);
	    	UrlPost postUtil = new UrlPost();
	    	postUtil.sendPost(gson.toJson(tf, TakeTurn.class), GameModel.turnControlPath +"/turnFinished");
	    }
		
		// At this point, we can send back a packet with status "ok"
		StatusResponse response = new StatusResponse("ok", "sent turn finished to TC");
		resp.getWriter().println(gson.toJson(response, StatusResponse.class));
	}

}
