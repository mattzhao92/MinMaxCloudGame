package model.nextMove;

import model.GameModel;
import model.IBoardModel;
import model.board.GameGridBoard;


public class DepthAcc extends ADepthAcc {
	
	private GameModel gm;
	
    public DepthAcc(int mP, AAccumulator acc, int maxD, GameModel gm){
        super(mP, acc, maxD);
        this.gm = gm;
        System.out.println("gm: " + gm);
    }

    public AAccumulator makeOpposite() {
        return new DepthAcc(_modelPlayer, _acc.makeOpposite(), _maxDepth, gm) {
            public int getDepth() {
                return 1 + DepthAcc.this.getDepth();
            }

            public boolean isNotDone() {
                //System.out.println("Depth is " + getDepth());
                if(getDepth() >= getMaxDepth()) {
                  if(getDepth() == getMaxDepth())
                  {
                    //_acc._val = ((int)(3.0*Math.random()))-1;

//                    int[] payOffVal = {0, 0};
//                    _boardModel.mapAll(_modelPlayer, _payOff, payOffVal);
                	  int payoff = payoff(_modelPlayer);
                    _acc.updateBest(_acc.getMove().y, _acc.getMove().x, payoff);
//                    _acc._val = payOffVal[0]-payOffVal[1];
                    //System.out.println("Payoff = " + _acc._val);

                    }
                    return false;
                }
                else
                    return _acc.isNotDone();
            }
        };
    }

    private int payoff(int player){
    	System.out.println("player: " + player + " | gm : " + gm);
    	GameGridBoard i = (GameGridBoard) gm.getBoardModel();
    	System.out.println("ggb: " + i);
      return i.getPayoff(player);
    }
}