/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;

public class VirtualOneHopPieceOnSquare extends VirtualPieceOnSquare {

    public VirtualOneHopPieceOnSquare(ChessBoard myChessBoard, int newPceID, int pceType, int myPos) {
        super(myChessBoard, newPceID, pceType, myPos);
        singleNeighbours = new ArrayList<>();
    }

    // all non-sliding neighbours (one-hop neighbours) are kept in one ArrayList
    protected final List<VirtualOneHopPieceOnSquare> singleNeighbours;

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
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," \\1ff: ");
        myChessBoard.getPiece(myPceID).startNextUpdate();
        setAndPropagateDistance(distance, 1,  Integer.MAX_VALUE );  //minDist, maxDist ); */
    }

    @Override
    protected void propagateDistanceChangeToAllNeighbours() {
        propagateDistanceChangeToAllNeighbours(-1, Integer.MAX_VALUE);
    }

    /**
     * checks suggested Distance, if it is smaller==an update and propagates that knowledge to the neighbours
     * @param suggestedDistance
     * @param minDist
     * @param maxDist
     */
    protected void setAndPropagateDistance(final @NotNull Distance suggestedDistance,
                                         int minDist,    // assume that distances below minDist are already correct, but still run "over" them to get propagation going
                                         int maxDist     // stop at dist >= maxDist (leaves it open for later call with minDist=this value)
                                         //                                                             final Distance trustBelow   // lower Limit if Update of distance is "locally increasing" i.e. only partial for squares >= that limit
    ) {
        //not needed: @return ALLDIR if I had to update my meighbours; NONE if I terminated the recursion,
        //                    BACKWARD_NONSLIDING if I was suo much closer that the caller should re-think his opinion and better ask me again...
        assert(suggestedDistance.getShortestDistanceEvenUnderCondition()>=0);
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," {"+squareName(myPos)+"_"+ suggestedDistance);
        if (suggestedDistance.dist()==0) {
            // I carry my own piece, i.e. distance=0.  test is needed, otherwise I'd act as if I'd find my own piece here in my way...
            rawMinDistance = suggestedDistance;  //new Distance(0);
            minDistance = rawMinDistance;
            propagateDistanceChangeToAllNeighbours(minDist, maxDist);
            return;
        }
        //else
        if (suggestedDistance.getShortestDistanceEvenUnderCondition() > maxDist) {
            // over max, we stop here
            return;
        }
        //else...
        int neededPropagationDir = updateRawMinDistances(suggestedDistance, minDist );
        if (rawMinDistance.getShortestDistanceEvenUnderCondition()<minDist) {
            // I am still in a range that is already set
            // I change nothing, but get the propagation rolling
            propagateDistanceChangeToOutdatedNeighbours(minDist, maxDist);
            return;
        }
        if (rawMinDistance.getShortestDistanceEvenUnderCondition()==minDist) {
            // I am at the border of what was already set
            neededPropagationDir = ALLDIRS;
        }
        switch(neededPropagationDir) {
            case NONE:
                break;
            case ALLDIRS:
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"|");
                // and, if new distance is different from what it was, also tell all other neighbours
                propagateDistanceChangeToAllNeighbours(minDist, maxDist);
                break;
            case BACKWARD_NONSLIDING:
                break;
            default:
                assert (false);  // should not occur for 1hop-pieces
        }
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"}");
    }


    protected void propagateDistanceChangeToAllNeighbours(final int minDist, final int maxDist) {
        propagateDistanceChangeToOutdatedNeighbours(minDist, maxDist, Integer.MAX_VALUE);
    }

    protected void propagateDistanceChangeToOutdatedNeighbours(final int minDist, final int maxDist) {
        propagateDistanceChangeToOutdatedNeighbours(minDist, maxDist, latestUpdate());
    }

    protected void propagateDistanceChangeToOutdatedNeighbours(final int minDist, final int maxDist, int updateLimit) {
        // the direct "singleNeighbours"
        //int ret = NONE;
        // first we check if another neighbour is even closer... to avoid unnecessary long recursions and
        // to cover the cases where after a resetPropagation we run into the "border" of the old and better values
        recalcRawMinDistance();
        for (VirtualOneHopPieceOnSquare n: singleNeighbours) {
            if (n.latestUpdate()<updateLimit) { // only if it was not visited, yet
                Distance suggestion = minDistanceSuggestionTo1HopNeighbour();
                /*** experimenting with breadth search propagation: ***/
                if (FEATURE_TRY_BREADTHSEARCH)
                    myPiece().quePropagation(suggestion.getShortestDistanceEvenUnderCondition(),
                 ()-> n.setAndPropagateDistance(suggestion, minDist, maxDist));
                else
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
        // from here on, the new suggestion is in any case not the minimum. it is worse than what I already know
        if (minDistanceSuggestionTo1HopNeighbour().isSmaller(updateDistance)) {
            // the neighbour seems to be outdated, let me inform him.
            return BACKWARD_NONSLIDING; // there is no real fromDir, so -1 is supposed to mean "a" direction backward
        }
        return NONE;

        /***
        if (rawMinDistance.getDistanceUnderCondition()<=minDist) {
            // if we reached the "safe" squares (and the suggestion was not smaller), we can stop. It cannot
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
         ***/
    }


    @Override
    protected void propagateResetIfUSWToAllNeighbours() {
        propagateResetIfUSWToAllNeighbours(rawMinDistance);
    }

    protected void propagateResetIfUSWToAllNeighbours(Distance onlyAbove) {
        for (VirtualOneHopPieceOnSquare n : singleNeighbours)
            n.propagateResetIfUSW(onlyAbove);
    }

    private void propagateResetIfUSW(Distance onlyAbove ) {
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," r"+squareName(myPos));
        if (rawMinDistance.isSmallerOrEqual(onlyAbove)
                || rawMinDistance.isInfinite() ) {
            // we are under the reset-limit -> our caller was not our predecessor on the shortest in-path
            // or we are at a square that was already visited
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,".");
            return;
        }
        // propagate on
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"*");
        Distance nextLimit = new Distance(rawMinDistance);
        resetDistances();
        propagateResetIfUSWToAllNeighbours(nextLimit);
    }

}
