package model.board;

import java.awt.Point;
import java.util.Random;

import model.*;

public class GameGridBoard extends ABoardModel {
    private int IN_ROW= 3;
    public GameGridBoard(int nRows, int nCols)  {
        super(nRows, nCols);
    }

	synchronized public void reset()  {
		super.reset();
		Random rand = new Random();
		for(int i = 0; i < cells.length; i++){
			for(int j = 0; j < cells[i].length; j++){
				cells[i][j] = rand.nextInt(10) + 5;
			}
		}

		
	}
	
    /**
     * Changes the state of the board according to the input.
     * @param winner Which player is the winner {-1, 0, +1} where 0 = no winner or draw.
     */
    synchronized private void chgState(int winner)  {
        if (winner == -1) {
            state = Player0WonState.Singleton;
        }
        else if (winner == 1) {
            state = Player1WonState.Singleton;
        }
        else  {// winner == 0 -> no winner, but perhaps a draw
        	System.out.println("in winner check");
            map(winner, new IBoardLambda<Void>() {
                public boolean apply(int player, IBoardModel host, 
                                     int row, int col, int value, Void... nu) {
                    state = NonTerminalState.Singleton;
                    return false;
                }

                public void noApply(int player, IBoardModel host, Void... nu) {
                    state = DrawState.Singleton;
                }
            });
        }
    }

 	Point old_pos;
	int old_value;
	Point new_pos;
	int new_value;
	public synchronized IUndoMove makeMove(final int row, final int col, int player,
                                           ICheckMoveVisitor chkMoveVisitor,
                                           IBoardStatusVisitor<Void, Void> statusVisitor) {
        if (isValidMove(player,row,col)) {
       
        	for(int i = 0; i < cells.length; i++)
        		for(int j = 0; j < cells[i].length; j++)
        			if(cells[i][j] == player){
        			 	old_pos = new Point(i, j);
        			 	old_value = cells[i][j];
        				cells[i][j] = 0;
        			}
        	final Point new_pos = new Point(row, col);
        	final int new_pos_value = cells[row][col];
            cells[row] [col] = player;
            pprint(cells);
            chgState(winCheck(row, col));
            chkMoveVisitor.validMoveCase();
            execute(statusVisitor);
            return new IUndoMove() {
                public void apply(IUndoVisitor undoVisitor) {
                    undoMove(old_pos, old_value, new_pos, new_pos_value, undoVisitor);
                }
            };
        }
        chkMoveVisitor.invalidMoveCase();
        return new IUndoMove() {
            public void apply(IUndoVisitor undoVisitor) {
                // no-op
            }
        };
    }

	public void pprint(int[][] matrix){
		for (int i = 0; i < matrix.length; i++) {
		    for (int j = 0; j < matrix[i].length; j++) {
		        System.out.print(matrix[i][j] + " ");
		    }
		    System.out.print("\n");
		}
	}
    /**
     * Undoes the move at (row,col).
     * @param row
     * @param col
     * @param undoVisitor The appropriate method of the visitor is called after the undo is performed.
     */
    private synchronized void undoMove(Point old_pos, int old_value, Point new_pos, int new_pos_value, IUndoVisitor undoVisitor)  {
        
    	if(old_value != -5){
    		if(this.isValidMove(old_value, old_pos.x, old_pos.y))
    			cells[old_pos.x][old_pos.y] = old_value;
    	}
        cells[new_pos.x][new_pos.y]= new_pos_value; 
        state = NonTerminalState.Singleton;
    }



    /**
     * Checks for a winner when a token is placed at (row, col).
     * @param row
     * @param col
     * @return The winner {-1, 0, +1} where 0 = no winner or draw.
     */
    private int winCheck(int row, int col){
    	int count = 0;
        int player = cells[row][col];
        for(int i = 0; i < cells.length; i++)
        	for(int j = 0; j < cells[i].length; j++){
        		if(cells[i][j] == 0)
        			count++;
        	}
        System.out.println("count: " + count + " | cells[0]length*cells.length -1:" + (cells[0].length * cells.length - 2));
        if(count == cells[0].length * cells.length - 2)
        	return 1;
        
        return 0;
    }

    protected boolean isValidMove(int player, int row, int col){
        if(cells[row][col] == -2 || cells[row][col] == -1){
        	return false;
        }
        else
        	return true;
    }
}
