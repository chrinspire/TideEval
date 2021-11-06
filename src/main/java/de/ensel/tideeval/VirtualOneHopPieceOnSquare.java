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
    public void setAndPropagateDistance(final ConditionalDistance distance) {
        setAndPropagateOneHopDistance(distance);
    }

    @Override
    protected void propagateDistanceChangeToAllNeighbours() {
        propagateDistanceChangeToAllOneHopNeighbours();
    }

    @Override
    protected void propagateDistanceChangeToUninformedNeighbours() {
        ConditionalDistance suggestion = minDistanceSuggestionTo1HopNeighbour();
        for (VirtualOneHopPieceOnSquare n: singleNeighbours)
            if (n.getRawMinDistanceFromPiece().isInfinite())
                myPiece().quePropagation(
                        suggestion.dist(),
                        ()-> n.setAndPropagateOneHopDistance(suggestion));
    }

    /**
     * checks suggested Distance, if it is smaller:  if so, then update and propagates that knowledge to the neighbours
     * @param suggestedDistance distance suggested to this vPce
     */
    protected void setAndPropagateOneHopDistance(final @NotNull ConditionalDistance suggestedDistance) {
        assert(suggestedDistance.dist()>=0);
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," {"+squareName(myPos)+"_"+ suggestedDistance);
        if (suggestedDistance.dist()==0) {
            // I carry my own piece, i.e. distance=0.  test is needed, otherwise I'd act as if I'd find my own piece here in my way...
            rawMinDistance = suggestedDistance;  //new Distance(0);
            minDistance = null;
            propagateDistanceChangeToAllOneHopNeighbours();
            return;
        }
        //else
        /*if (suggestedDistance.getShortestDistanceEvenUnderCondition() > myChessBoard.currentDistanceCalcLimit()) {
            // over max, update myself, but stop
            updateRawMinDistances(suggestedDistance);
            return;
        } */
        //else...
        int neededPropagationDir = updateRawMinDistances(suggestedDistance);
        /*if (rawMinDistance.getShortestDistanceEvenUnderCondition()<minDist) {
            // I am still in a range that is already set
            // I change nothing, but get the propagation rolling
            propagateDistanceChangeToOutdatedNeighbours(minDist, maxDist);
            return;
        }
        if (rawMinDistance.getShortestDistanceEvenUnderCondition()==minDist) {
            // I am at the border of what was already set
            neededPropagationDir = ALLDIRS;
        }*/
        switch(neededPropagationDir) {
            case NONE:
                break;
            case ALLDIRS:
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"|");
                // and, if new distance is different from what it was, also tell all other neighbours
                propagateDistanceChangeToAllOneHopNeighbours();
                break;
            case BACKWARD_NONSLIDING:
                break;   // TODO: nothing necessary here? and who is the one who called me?
            default:
                assert (false);  // should not occur for 1hop-pieces
        }
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"}");
    }


    protected void propagateDistanceChangeToAllOneHopNeighbours() {    // final int minDist, final int maxDist) {
        propagateDistanceChangeToOutdatedOneHopNeighbours(Long.MAX_VALUE);
    }

    protected void propagateDistanceChangeToOutdatedNeighbours() {  // final int minDist, final int maxDist) {
        propagateDistanceChangeToOutdatedOneHopNeighbours(getLatestChange());
    }

    protected void propagateDistanceChangeToOutdatedOneHopNeighbours(final long updateLimit) {   // final int minDist, final int maxDist, long updateLimit) {
        // the direct "singleNeighbours"
        //int ret = NONE;
        // first we check if another neighbour is even closer... to avoid unnecessary long recursions and
        // to cover the cases where after a resetPropagation we run into the "border" of the old and better values

        //TODO: Check: do we still need this?  I think since breadth propagation, we dont any more, but it makes bugs without.
        //recalcRawMinDistanceFromNeighbours();
        ConditionalDistance suggestion = minDistanceSuggestionTo1HopNeighbour();
        for (VirtualOneHopPieceOnSquare n: singleNeighbours) {
            if (n.getLatestChange()<updateLimit) { // only if it was not visited, yet
                myPiece().quePropagation(
                            suggestion.dist(),
                            ()-> n.setAndPropagateOneHopDistance(suggestion));
                //n.setAndPropagateOneHopDistance(suggestion);
                // TODO: see above, this also depends on where a own mySquarePiece can move to - maybe only in the way?
            }
        }
    }


    @Override
    protected int recalcRawMinDistanceFromNeighbours() {
        return recalcRawMinDistanceFromOneHopNeighbours();
    }

    /**
     * updates rawMinDistance from Neighbours
     * @return 0: value did not change;  +1: value increased;  -1: value decreased;
     */
    private int recalcRawMinDistanceFromOneHopNeighbours() {
        if (rawMinDistance.dist()==0)
            return 0;  // there is nothing closer than myself...
        //rawMinDistance = (IntStream.of(suggestedDistanceFromNeighbours)).min().getAsInt();
        ConditionalDistance minimum = null;
        //Todo: Optimize: first find neighbour with minimum dist, then copy it - instead of multiple copy of distances with all conditions...
        for(VirtualOneHopPieceOnSquare n : singleNeighbours) {
            if (n!=null) {
                if (minimum==null)
                    minimum = n.minDistanceSuggestionTo1HopNeighbour();
                else
                    minimum.reduceIfSmaller(n.minDistanceSuggestionTo1HopNeighbour());
            }
        }
        if (minimum==null) {
            //TODO: thies piece has no(!) neighbour... this is e.g. (only case?) a pawn that has reached the final rank.
            return 0;
        }
        if (rawMinDistance.distEquals(minimum))
            return 0;
        if (rawMinDistance.reduceIfSmaller(minimum)) {
            minDistance = null;
            return -1;
        }
        rawMinDistance.updateFrom(minimum);
        minDistance = null;
        return +1;
    }

    /**
     * Updates the overall minimum distance
     * @param suggestedDistance the new distance-value propagated from my neighbour (or "0" if Piece came to me)
     * @return int what needs to be updated. NONE for nothing, ALLDIRS for all, below that >0 tells a single direction
     */
    protected int updateRawMinDistances(final ConditionalDistance suggestedDistance) {
        if (rawMinDistance.dist()==0)
            return NONE;  // there is nothing closer than myself...
        if (suggestedDistance.distIsSmaller(rawMinDistance)) {
            // the new distance is smaller than the minimum, so we already found the new minimum
            if (rawMinDistance.reduceIfSmaller(suggestedDistance)) {
                setLatestChangeToNow();
                minDistance = null;
            }
            return ALLDIRS;
        }
        if (suggestedDistance.distEquals(rawMinDistance)) {
            return NONE;
        }
        // from here on, the new suggestion is in any case not the minimum. it is worse than what I already know
        if (minDistanceSuggestionTo1HopNeighbour().distIsSmaller(suggestedDistance)) {
            // the neighbour seems to be outdated, let me inform him.
            return BACKWARD_NONSLIDING; // there is no real fromDir, so -1 is supposed to mean "a" direction backward
        }
        return NONE;
    }

    @Override
    protected void propagateResetIfUSWToAllNeighbours() {
        propagateResetIfUSWToAllNeighbours(rawMinDistance);
    }

    protected void propagateResetIfUSWToAllNeighbours(ConditionalDistance onlyAbove) {
        for (VirtualOneHopPieceOnSquare n : singleNeighbours)
            n.propagateResetIfUSW(onlyAbove);
    }

    private void propagateResetIfUSW(ConditionalDistance onlyAbove ) {
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," r"+squareName(myPos));
        if ( !onlyAbove.isInfinite() && rawMinDistance.distIsSmallerOrEqual(onlyAbove)
                || rawMinDistance.isInfinite()
                || rawMinDistance.dist()==0) {
            // we are under the reset-limit -> our caller was not our predecessor on the shortest in-path
            // or we are at a square that was already visited
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,".");
            //we reached the end of the reset. from here we should propagate back the correct value
            // there, we need to get update from best neighbour (but not now, only later with breadth propagation).
            myPiece().quePropagation(
                    0,
                    ()-> this.recalcRawMinDistanceFromNeighboursAndPropagate());
            return;
        }
        // propagate on
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"*");
        ConditionalDistance nextLimit = new ConditionalDistance(rawMinDistance);
        resetDistances();
        propagateResetIfUSWToAllNeighbours(nextLimit);
    }

}
