/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VirtualOneHopPieceOnSquare extends VirtualPieceOnSquare {

    // all non-sliding neighbours (one-hop neighbours) are kept in one ArrayList
    protected final List<VirtualOneHopPieceOnSquare> singleNeighbours;

    public VirtualOneHopPieceOnSquare(ChessBoard myChessBoard, int newPceID, int pceType, int myPos) {
        super(myChessBoard, newPceID, pceType, myPos);
        singleNeighbours = new ArrayList<>(MAXMAINDIRS);
    }

    @Override
    protected List<VirtualPieceOnSquare> getNeighbours() {
        return Collections.unmodifiableList(singleNeighbours);
    }

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
            minDistsDirty();
            propagateDistanceChangeToAllOneHopNeighbours();
            return;
        }
        int neededPropagationDir = updateRawMinDistances(suggestedDistance);
        switch(neededPropagationDir) {
            case NONE:
                break;
            case ALLDIRS:
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"|");
                // and, if new distance is different from what it was, also tell all other neighbours
                propagateDistanceChangeToAllOneHopNeighbours();
                break;
            case BACKWARD_NONSLIDING:
                break;   // TODO: nothing necessary here? seems it works without. and who is the one who called me, to call him back?
            default:
                assert (false);  // should not occur for 1hop-pieces
        }
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"}");
    }


    protected void propagateDistanceChangeToAllOneHopNeighbours() {    // final int minDist, final int maxDist) {
        ConditionalDistance suggestion = minDistanceSuggestionTo1HopNeighbour();
        for (VirtualOneHopPieceOnSquare n: singleNeighbours) {
            myPiece().quePropagation(
                    suggestion.dist(),
                    () -> n.setAndPropagateOneHopDistance(suggestion));
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
        ConditionalDistance minimum = new ConditionalDistance(this);
        //Todo: Optimize: first find neighbour with minimum dist, then copy it - instead of multiple copy of distances with all conditions...
        for(VirtualOneHopPieceOnSquare n : singleNeighbours) {
            if (n!=null)
                minimum.reduceIfCdIsSmaller(n.minDistanceSuggestionTo1HopNeighbour());
        }
        if (minimum.isInfinite()) {
            //TODO?: this piece has no(!) neighbour... this is e.g. (only case?) a pawn that has reached the final rank.
            return 0;
        }
        if (rawMinDistance.cdEquals(minimum)) {
            if (!rawMinDistance.equals(minimum)) { // same dist, but different conditions - we update, but this case is a potential source for a bug later
                updateRawMinDistanceFrom(minimum);
            }
            return 0;
        }
        if (reduceRawMinDistanceIfCdIsSmaller(minimum))
            return -1;

        updateRawMinDistanceFrom(minimum);
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
        if (reduceRawMinDistanceIfCdIsSmaller(suggestedDistance)) {
            // the new distance is smaller than the minimum, so we already found the new minimum
            return ALLDIRS;
        }
        if (suggestedDistance.cdEquals(rawMinDistance)) {
            return NONE;
        }
        // from here on, the new suggestion is in any case not the minimum. it is worse than what I already know
        if (minDistanceSuggestionTo1HopNeighbour().cdIsSmallerThan(suggestedDistance)) {
            // the neighbour seems to be outdated, let me inform him.
            return BACKWARD_NONSLIDING; // there is no real fromDir, so -20 is supposed to mean "a" direction backward
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
        if ( !onlyAbove.isInfinite() && rawMinDistance.cdIsSmallerOrEqualThan(onlyAbove)
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

    @Override
    public boolean isUnavoidableOnShortestPath(int pos, int maxdepth) {
        // simplest case: I am on my own way -> yes
        if (pos == myPos)
            return true;
        assertNotNull(rawMinDistance);
        if (rawMinDistance.dist()==0
                || rawMinDistance.dist()==INFINITE_DISTANCE
                || maxdepth==0 )
            return false;  // the search has ended, no pos was passed.

        // ok, we have to check the neighbours, but only those with minimum distance
        for (VirtualOneHopPieceOnSquare n : singleNeighbours )
            //careful here: this comparison must be consistent with the condition in "CD.reduceIfSmall" used in recalcRawMinDistance()
            if (n!=null
                    && n.minDistanceSuggestionTo1HopNeighbour().cdEquals(rawMinDistance)
                    //&& n.minDistanceSuggestionTo1HopNeighbour().hasFewerOrEqualConditionsThan(rawMinDistance)
            ) {
                // we are at (one of) the shortest in-paths
                boolean onepathcheck = n.isUnavoidableOnShortestPath(pos,maxdepth-1);
                if (onepathcheck==false)
                    return false;  // we found one clear path
            }
        return true;
    }

    @Override
    List<VirtualPieceOnSquare> getMoveOrigins() {
        return getPredecessorNeighbours().stream()
                .filter(n->n.minDistanceSuggestionTo1HopNeighbour().cdIsSmallerOrEqualThan(rawMinDistance))
                .collect(Collectors.toList());
    }

}
