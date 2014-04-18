package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.http.*;

import com.google.devrel.samples.ttt.Board;
import com.google.devrel.samples.ttt.Cell;
import com.google.devrel.samples.ttt.CellContainer;

public class GetRandomMoveServlet extends HttpServlet {

	private static final long serialVersionUID = -6738682736055424826L;

	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		CellContainer cellContainer = CellContainer.fromJson(req.getInputStream());
		ArrayList<Cell> cells = cellContainer.cells;
		
		ArrayList<Cell> unoccupiedCells = new ArrayList<Cell>();
		for (Cell cell : cells ) {
			if (cell.val <= 10) {
				unoccupiedCells.add(cell);
			}
		}
		Random randomG = new Random();
	    Cell randomfreeCell = unoccupiedCells.get(randomG.nextInt(unoccupiedCells.size()));
		randomfreeCell.val = randomfreeCell.val + 20;
		
		CellContainer updateContainer = new CellContainer(cells);
		Board updated = new Board(CellContainer.toJson(updateContainer));
	    resp.getWriter().println(updated.getState());
	}
}
