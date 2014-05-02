package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Json.GetPayoffInput;
import Json.GetPayoffOutput;

import com.google.gson.Gson;

/**
 * Servlet that provides the payoff for a board input
 *
 */
public class GetPayoffServlet extends HttpServlet{
	private static final long serialVersionUID = -977766106447860017L;
	private Gson gson = new Gson();
	
	/**
	 * Handler for post request that responds with the payoff
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		
		GetPayoffInput request = gson.fromJson(reader, GetPayoffInput.class);
		GetPayoffOutput gpo = new GetPayoffOutput();
		gpo.payoff = 5;
		resp.getWriter().println(gson.toJson(gpo));
	}
}
