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
import Json.GetPayoffInput;
import Json.GetPayoffOutput;
import Model.GameModel;

public class GetPayoffServlet extends HttpServlet{
	private static final long serialVersionUID = -977766106447860017L;
	private Gson gson = new Gson();
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		//System.out.println("in get payoff test");
		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		GetPayoffInput request = gson.fromJson(reader, GetPayoffInput.class);
		GetPayoffOutput gpo = new GetPayoffOutput();
		int payoff = GameModel.getPayoff(request.nextPlayerID, request.currentBoard);
		//System.out.println("payoff: " + payoff);
		gpo.number = payoff;
		resp.getWriter().println(gson.toJson(gpo));
	}
}
