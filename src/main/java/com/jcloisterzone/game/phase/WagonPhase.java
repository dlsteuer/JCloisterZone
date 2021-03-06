package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.feature.visitor.IsOccupiedOrCompleted;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.WagonCapability;


public class WagonPhase extends Phase {

    final WagonCapability wagonCap;


    public WagonPhase(Game game) {
        super(game);
        wagonCap = game.getCapability(WagonCapability.class);
    }


    @Override
    public boolean isActive() {
        return game.hasCapability(WagonCapability.class);
    }

    @Override
    public void enter() {
        if (!existsLegalMove()) next();
    }

    @Override
    public void pass() {
        enter();
    }

    @Override
    public void deployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType) {
        if (!meepleType.equals(Wagon.class)) {
            logger.error("Illegal figure type.");
            return;
        }
        Meeple m = getActivePlayer().getMeepleFromSupply(Wagon.class);
        m.deployUnoccupied(getBoard().get(p), loc);
        enter();
    }

    @Override
    public Player getActivePlayer() {
        Player p = wagonCap.getWagonPlayer();
        return p == null ? game.getTurnPlayer() : p;
    }

    private boolean existsLegalMove() {
        Map<Player, Feature> rw = wagonCap.getReturnedWagons();
        while (!rw.isEmpty()) {
            int pi = game.getTurnPlayer().getIndex();
            while(! rw.containsKey(game.getAllPlayers()[pi])) {
                pi++;
                if (pi == game.getAllPlayers().length) pi = 0;
            }
            Player player = game.getAllPlayers()[pi];
            Feature f = rw.remove(player);
            List<FeaturePointer> wagonMoves = prepareWagonMoves(f);
            if (!wagonMoves.isEmpty()) {
                wagonCap.setWagonPlayer(player);
                game.post(new SelectActionEvent(getActivePlayer(), new MeepleAction(Wagon.class).addAll(wagonMoves), true));
                return true;
            }
        }
        return false;
    }

    private List<FeaturePointer> prepareWagonMoves(Feature source) {
        return source.walk(new FindUnoccupiedNeighbours());
    }

    private class FindUnoccupiedNeighbours implements FeatureVisitor<List<FeaturePointer>> {

        private List<FeaturePointer> wagonMoves = new ArrayList<>();

        @Override
        public boolean visit(Feature feature) {
            if (feature.getNeighbouring() != null) {
                for (Feature nei : feature.getNeighbouring()) {
                    if (nei.walk(new IsOccupiedOrCompleted())) continue;
                    wagonMoves.add(new FeaturePointer(getTile().getPosition(), nei.getLocation()));
                }
            }
            return true;
        }

        public List<FeaturePointer> getResult() {
            return wagonMoves;
        }
    }

}
