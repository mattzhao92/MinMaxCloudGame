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

public class ApplyNextMoveServlet extends HttpServlet{
	private static final long serialVersionUID = -977766106447860017L;
	private Gson gson = new Gson();
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		ApplyNextMoveInput request = gson.fromJson(reader, ApplyNextMoveInput.class);
		ArrayList<String> boardsNextLevel = GameModel.getValidMovesForPlayer(request.nextPlayerID, request.currentBoard);
		ApplyNextMoveResponse response = new ApplyNextMoveResponse(boardsNextLevel);
		resp.getWriter().println(gson.toJson(response, ApplyNextMoveResponse.class));
	}
}
