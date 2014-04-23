package servlets_private;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.devrel.samples.ttt.Board;

public class GetBoardServlet extends HttpServlet{

	private static final long serialVersionUID = -894912701106818294L;
	private MemcacheService syncCache = (MemcacheService) MemcacheServiceFactory.getMemcacheService();

	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		String boardState = (String) syncCache.get("boardState");
		if (boardState == null)
			boardState = "";
		resp.getWriter().println(boardState);
	}
	
	  public Board getBoard() {
		  Board newBoard = new Board();
		  return newBoard;
	  }
}
