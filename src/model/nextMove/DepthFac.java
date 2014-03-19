package model.nextMove;

import model.IBoardModel;


public class DepthFac implements IAccFactory {
    private int _maxDepth=0;
    private IAccFactory _accFac;
    private IBoardModel ibm;

    public DepthFac(IAccFactory accFac, int maxDepth, IBoardModel ibm) {
        _accFac = accFac;
        _maxDepth = maxDepth;
        ibm = ibm;
    }

    public AAccumulator makeAcc(int player) {
        return new DepthAcc(player, _accFac.makeAcc(player), _maxDepth, ibm);
    }
}