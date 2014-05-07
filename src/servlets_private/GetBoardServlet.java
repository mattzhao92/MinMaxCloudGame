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
import com.google.appengine.api.datastore.Transaction;
import com.google.devrel.samples.ttt.Board;
import com.google.appengine.api.datastore.Text;

/**
 * Servlet that provides the current game board
 *
 */
public class GetBoardServlet extends HttpServlet{

	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = -894912701106818294L;

	/**
	 * Handles a get request and sends the current board to resp
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{

		//Get the board from the datastore
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key boardKey = GameModel.boardKey;
		Query query = new Query("Board", boardKey);
		List<Entity> boardList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

		//Send the board to resp
		System.out.println("GetBoardServlet: board list size "+boardList.size());
		if (boardList.size() >= 1) {
			String board = ((Text) boardList.get(boardList.size()-1).getProperty("board")).getValue();
			System.out.println("GetBoardServlet: returning board "+board);
			resp.getWriter().println(board);
			
			// delete all the other boards
			Entity lastBoard = boardList.get(boardList.size()-1);
			
			Transaction tx = datastore.beginTransaction();
			try {
				for (Entity e : boardList) {
					datastore.delete(e.getKey());
				}
				datastore.put(lastBoard);
			} catch(Exception e) {
				e.printStackTrace();
			}
			tx.commit();
			
			
		} else {
			resp.getWriter().println("");
		}
	}

	/**
	 * Returns a new board
	 * @return
	 */
	public Board getBoard() {
		Board newBoard = new Board();
		return newBoard;
	}
}
