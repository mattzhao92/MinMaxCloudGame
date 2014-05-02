package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import Json.ApplyNextMoveInput;
import Json.ApplyNextMoveResponse;
import Model.GameModel;

/**
 * Servlet that applies the move of the next player
 *
 */
public class ApplyNextMoveServlet extends HttpServlet{
	
	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = -977766106447860017L;
	private Gson gson = new Gson();
	
	/**
	 * Handles the post request and applies the move in the request
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		ApplyNextMoveInput request = gson.fromJson(reader, ApplyNextMoveInput.class);
		
		// get all valid moves for the next player and return the resulting game board for each move
		ArrayList<String> boardsNextLevel = GameModel.getValidMovesForPlayer(request.nextPlayerID, request.currentBoard);
		ApplyNextMoveResponse response = new ApplyNextMoveResponse(boardsNextLevel, request.TreeDepth);
		resp.getWriter().println(gson.toJson(response, ApplyNextMoveResponse.class));
	}
}
