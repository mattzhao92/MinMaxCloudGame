package servlets_private;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model.GameModel;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.devrel.samples.ttt.Board;
import com.google.appengine.api.datastore.Text;

public class GetBoardServlet extends HttpServlet{

	private static final long serialVersionUID = -894912701106818294L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	    Key boardKey = GameModel.boardKey;
	    Query query = new Query("Board", boardKey);
	    List<Entity> boardList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
	    //System.out.println("GetBoardServlet: board list size "+boardList.size());
	    if (boardList.size() == 1) {
	    	String board = ((Text) boardList.get(0).getProperty("board")).getValue();
	    	//System.out.println("GetBoardServlet: returning board "+board);
	    	resp.getWriter().println(board);
	    } else {
	    	resp.getWriter().println("");
	    }
	}
	
	public Board getBoard() {
		  Board newBoard = new Board();
		  return newBoard;
	}
}
