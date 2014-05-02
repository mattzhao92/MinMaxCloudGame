package servlets_private;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.http.*;

import com.google.devrel.samples.ttt.Board;
import com.google.devrel.samples.ttt.Cell;
import com.google.devrel.samples.ttt.CellContainer;

/**
 * Servlet that makes a random move on the supplied game board
 *
 */
public class GetRandomMoveServlet extends HttpServlet {

	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = -6738682736055424826L;

	/**
	 * Handles the post request and makes a random move on the sent board
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{

		//Get the current game board from the request
		CellContainer cellContainer = CellContainer.fromJson(req.getInputStream());
		ArrayList<Cell> cells = cellContainer.cells;

		//Find all unoccupied cells on the game board
		ArrayList<Cell> unoccupiedCells = new ArrayList<Cell>();
		for (Cell cell : cells ) {
			if (cell.val <= 10) {
				unoccupiedCells.add(cell);
			}
		}

		//Choose a random unoccupied cell and move to it by changing it's value on the board
		Random randomG = new Random();
		Cell randomfreeCell = unoccupiedCells.get(randomG.nextInt(unoccupiedCells.size()));
		randomfreeCell.val = randomfreeCell.val + 20;

		//Send the modified board back
		CellContainer updateContainer = new CellContainer(cells);
		Board updated = new Board(CellContainer.toJson(updateContainer));
		resp.getWriter().println(updated.getState());
	}
}
