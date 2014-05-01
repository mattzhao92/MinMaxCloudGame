package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import Json.ApplyNextMoveInput;
import Json.JoinSubGameMessage;

public class ApplyNextMoveServlet extends HttpServlet{
	private Gson gson = new Gson();
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		ApplyNextMoveInput request = gson.fromJson(reader, ApplyNextMoveInput.class);
		
		
	
		
	}
}
