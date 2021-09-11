/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.ArrayList;
import java.util.List;

import static de.ensel.tideeval.ChessBasics.*;

public class VirtualOneHopPieceOnSquare extends VirtualPieceOnSquare {

    public VirtualOneHopPieceOnSquare(ChessBoard myChessBoard, int newPceID, int myPos) {
        super(myChessBoard, newPceID, myPos);
        singleNeighbours = new ArrayList<>();
    }

    // all non-sliding neighbours (one-hop neighbours) are kept in one ArrayList
    private final List<VirtualOneHopPieceOnSquare> singleNeighbours;

    @Override
    public void addSingleNeighbour(VirtualPieceOnSquare newVPiece) {
        this.singleNeighbours.add( (VirtualOneHopPieceOnSquare)newVPiece );
    }


    // set up initial distance from this vPces position - restricted to distance depth change
    @Override
    public void setAndPropagateDistance(final Distance distance) {  //}, int minDist, int maxDist ) {
        setAndPropagateDistance(distance, -1,  Integer.MAX_VALUE );  //minDist, maxDist );
        // TODO!! - splitting does not works - ends up in endles loop or misses necessary updates
        /*
        myChessBoard.getPiece(myPceID).endUpdate();
        System.out.print(" \\1ff: ");
        myChessBoard.getPiece(myPceID).startNextUpdate();
        setAndPropagateDistance(distance, 1,  Integer.MAX_VALUE );  //minDist, maxDist ); */
    }

    @Override
    protected void propagateDistanceChangeToAllNeighbours() {
        propagateDistanceChangeToAllNeighbours(-1, Integer.MAX_VALUE);
    }

    private void setAndPropagateDistance(final Distance suggestedDistance,
                                         int minDist,    // assume that distances below minDist are already correct, but still run "over" them to get propagation going
                                         int maxDist     // stop at dist >= maxDist (leaves it open for later call with minDist=this value)
                                         //                                                             final Distance trustBelow   // lower Limit if Update of distance is "locally increasing" i.e. only partial for squares >= that limit
    ) {
        assert(suggestedDistance.getDistanceUnderCondition()>=0);
        System.out.print(" {"+squareName(myPos)+"_"+ suggestedDistance);
        if (suggestedDistance.dist()==0) {
            // I carry my own piece, i.e. distance=0.  test is needed, otherwise I'd act as if I'd find my own piece here in my way...
            rawMinDistance = suggestedDistance;  //new Distance(0);
            minDistance = rawMinDistance;
            propagateDistanceChangeToAllNeighbours(minDist, maxDist);
            return;
        }
        //else
        if (suggestedDistance.getDistanceUnderCondition() > maxDist) {
            // over max, we stop here
            return;
        }
        //else...
        int neededPropagationDir = updateRawMinDistances(suggestedDistance, minDist );
        if (rawMinDistance.getDistanceUnderCondition()<minDist) {
            // I am still in a range that is already set
            // I change nothing, but get the propagation rolling
            propagateDistanceChangeToOutdatedNeighbours(minDist, maxDist);
            return;
        }
        if (rawMinDistance.getDistanceUnderCondition()==minDist) {
            // I am at the border of what was already set
            neededPropagationDir = ALLDIRS;
        }
        switch(neededPropagationDir) {
            case NONE:
                return;
            case ALLDIRS:
                System.out.print("|");
                // and, if new distance is different from what it was, also tell all other neighbours
                propagateDistanceChangeToAllNeighbours(minDist, maxDist);
                break;
            default:
                assert (false);  // should not occur for 1hop-pieces
        }
        System.out.print("}");
    }


    protected void propagateDistanceChangeToAllNeighbours(final int minDist, final int maxDist) {
        // the direct "singleNeighbours"
        for (VirtualOneHopPieceOnSquare n: singleNeighbours) {
            Distance suggestion = minDistanceSuggestionTo1HopNeighbour();   // is always recalculated freshly, as it might have changed vie other recursion ways
            /*** breadth search propagation will follow later:
             myPiece().quePropagation(suggestion.getDistanceUnderCondition(),
                    ()->{ n.setAndPropagateDistance(suggestion, minDist, maxDist); } );   ***/
            n.setAndPropagateDistance(suggestion, minDist, maxDist);
            // TODO: see above, this also depends on where a own mySquarePiece can move to - maybe only in the way?
        }
    }

    protected void propagateDistanceChangeToOutdatedNeighbours(final int minDist, final int maxDist) {
        // the direct "singleNeighbours"
        for (VirtualOneHopPieceOnSquare n: singleNeighbours) {
            if (n.latestUpdate()<latestUpdate()) { // only if it was not visited, yet
                Distance suggestion = minDistanceSuggestionTo1HopNeighbour();
                /*** breadth search propagation will follow later:
                myPiece().quePropagation(suggestion.getDistanceUnderCondition(),
                        ()->{ n.setAndPropagateDistance(suggestion, minDist, maxDist); } );  ***/
                n.setAndPropagateDistance(suggestion, minDist, maxDist);
                // TODO: see above, this also depends on where a own mySquarePiece can move to - maybe only in the way?
            }
        }
    }

    /**
     * updates rawMinDistance from Neighbours
     * @return 0: value did not change;  +1: value increased;  -1: value decreased;
     */
    private int recalcRawMinDistance() {
        if (rawMinDistance.dist()==0)
            return 0;  // there is nothing closer than myself...
        //rawMinDistance = (IntStream.of(suggestedDistanceFromNeighbours)).min().getAsInt();
        Distance minimum = null;
        for(VirtualOneHopPieceOnSquare n : singleNeighbours) {
            if (n!=null) {
                if (minimum==null)
                    minimum = new Distance(n.minDistanceSuggestionTo1HopNeighbour());
                else
                    minimum.reduceIfSmaller(n.minDistanceSuggestionTo1HopNeighbour());
            }
        }
        if (rawMinDistance.equals(minimum))
            return 0;
        if (minimum==null) {
            //TODO: thies piece has no(!) neighbour... this is e.g. (only case?) a pawn that has reached the final rank.
            return 0;
        }
        if (rawMinDistance.reduceIfSmaller(minimum))
            return -1;
        rawMinDistance.updateFrom(minimum);
        return +1;
    }

    /**
     * Updates the overall minimum distance
     * @param updateDistance the new distance-value propagated from my neighbour (or "0" if Piece came to me)
     * @return int what needs to be updated. NONE for nothing, ALLDIRS for all, below that >0 tells a single direction
     */
    protected int updateRawMinDistances(final Distance updateDistance, final int minDist ) {
        setLastUpdateToNow();
        if (rawMinDistance.dist()==0)
            return NONE;  // there is nothing closer than myself...
        if (updateDistance.isSmaller(rawMinDistance)) {
            // the new distance is smaller than the minimum, so we already found the new minimum
            rawMinDistance.reduceIfSmaller(updateDistance);
            minDistance = null;
            return ALLDIRS;
        }
        if (updateDistance.equals(rawMinDistance)) {
            return NONE;
        }
        // from here on, the new suggestion is in any case not the minimum
        if (rawMinDistance.getDistanceUnderCondition()<=minDist) {
            // if we reached the "safe" squares (and the suggestion was not smaller) we cen stop, it cannot
            // be a vaild "reset-scenario" with overriding longer distances.
            return NONE;
        }
        if (recalcRawMinDistance()<=0) {
            // although it got perhaps worse at the neighbour, nothing has changed here all in all
            // remark: the "<" seems actuallx not possible, but i can happen, because a very other neighbour could have even smaller distences by now.
            return NONE;
        }
        // rawMinDistance must have increased (as update was not smaller, and rawMinDistance did not stay the same)
        // this means the input neighbour was the shortest-path-in, but got longer...
        // sorry, I need to forget everything at this moment, it might all be outdated
        resetDistances();
        rawMinDistance.updateFrom(updateDistance);  // we also do not keep the calculated minimum, but propagate the higher input
        return ALLDIRS;
    }


}
