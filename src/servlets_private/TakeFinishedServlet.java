package servlets_private;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

/**
 * Servlet that communicates to the turn control that the player finished their turn
 * @author CShuford
 *
 */
public class TakeFinishedServlet extends HttpServlet{

	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = 7486734051193470255L;

	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	Gson gson = new Gson();

	/**
	 * Handles a post request and tells the turn control the player's
	 * turn has been completed
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		System.out.println("TakeFinishedServlet");

		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		TakeTurnFinishedInput request = gson.fromJson(reader, TakeTurnFinishedInput.class);
		doModPost(request, req, resp);
	}

	/**
	 * Helper method that does the post request
	 * @param request
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	public void doModPost(TakeTurnFinishedInput request, HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{

		// save the board 
		Transaction tx = datastore.beginTransaction();
		Query query = new Query("Board", GameModel.boardKey);
		List<Entity> boardList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

		for (Entity entity: boardList) {
			datastore.delete(entity.getKey());
		}
		tx.commit();

		// creating a new board and store it in the database
		GameModel.storeCurrentBoard(request.board);


		Long playerID = -1L;
		Long currScore = 100L;
		Key lastTurnKey = KeyFactory.createKey("LastTurn", "MyLastTurn");

		query = new Query("lastTurn", lastTurnKey);
		List<Entity> turnList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));

		System.out.println("lastTurn List "+ turnList.size());
		if (turnList.size() == 1){
			Entity lastTurn = turnList.get(0);
			playerID = (Long) lastTurn.getProperty("playerID");
		} else {
			return;
		}

		TakeTurn tf = new TakeTurn(playerID, currScore);
		UrlPost postUtil = new UrlPost();
		postUtil.sendPost(gson.toJson(tf, TakeTurn.class), GameModel.turnControlPath +"/turnFinished");


		// At this point, we can send back a packet with status "ok"
		StatusResponse response = new StatusResponse("ok", "sent turn finished to TC");
		resp.getWriter().println(gson.toJson(response, StatusResponse.class));
	}

}
