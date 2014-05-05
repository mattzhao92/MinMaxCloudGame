package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Json.GetPayoffInput;
import Json.GetPayoffOutput;
import Json.GetScoreForPortal;
import Model.GameModel;

import com.google.gson.Gson;

/**
 * Servlet that provides the payoff for a board input
 *
 */
public class GetScoreForPortalServlet extends HttpServlet{
	private static final long serialVersionUID = -977766106447860017L;
	private Gson gson = new Gson();
	
	/**
	 * Handler for post request that responds with the payoff
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		GetScoreForPortal request = gson.fromJson(reader, GetScoreForPortal.class);
		int score = 5;
		//System.out.println("payoff: " + payoff);
		resp.getWriter().println(gson.toJson(score));
	}
}
