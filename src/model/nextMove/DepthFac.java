package model.nextMove;

import model.GameModel;
import model.IBoardModel;


public class DepthFac implements IAccFactory {
    private int _maxDepth=0;
    private IAccFactory _accFac;
    private GameModel gameModel;

    public DepthFac(IAccFactory accFac, int maxDepth, GameModel gameModel) {
        _accFac = accFac;
        _maxDepth = maxDepth;
        this.gameModel = gameModel;
        System.out.println("game mode: " + gameModel);
    }

    public AAccumulator makeAcc(int player) {
        return new DepthAcc(player, _accFac.makeAcc(player), _maxDepth, gameModel);
    }
}