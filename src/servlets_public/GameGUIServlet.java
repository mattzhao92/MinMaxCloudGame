package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Json.JoinSubGameMessage;

import com.google.gson.Gson;

public class GameGUIServlet extends HttpServlet{

	private static final long serialVersionUID = -5695796009771973102L;
	private Gson gson = new Gson();
	
	private static String getStringFromInputStream(InputStream is) {
		 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
 
		String line;
		try {
 
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line+"\n");
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		String jsonString = URLDecoder.decode(req.getParameter("data"), "UTF-8");
		JoinSubGameMessage request = gson.fromJson(jsonString, JoinSubGameMessage.class);
	    ServletContext context = getServletContext();

		String indexhtml = getStringFromInputStream(context.getResourceAsStream("/WEB-INF/index.html"));
		indexhtml = indexhtml.replace("\"REPLACE_PLAYER_NAME\"", "\""+request.playerName+"\"");
		indexhtml = indexhtml.replace("\"REPLACE_TOKEN\"", "\""+request.token+"\"");
		indexhtml = indexhtml.replace("\"REPLACE_ENABLE_MOUSE\"", "false");
		resp.getWriter().println(indexhtml);
	}
}
