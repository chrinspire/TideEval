/*
 *     TideEval - Wired New Chess Algorithm
 *     Copyright (C) 2023 Christian Ensel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
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
    protected void quePropagateDistanceChangeToAllNeighbours() {
        quePropagateDistanceChangeToAllOneHopNeighbours();
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
            quePropagateDistanceChangeToAllOneHopNeighbours();
            return;
        }
        int neededPropagationDir = updateRawMinDistances(suggestedDistance);
        switch(neededPropagationDir) {
            case NONE:
                break;
            case ALLDIRS:
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"|");
                // and, if new distance is different from what it was, also tell all other neighbours
                quePropagateDistanceChangeToAllOneHopNeighbours();
                break;
            case BACKWARD_NONSLIDING:
                break;   // TODO: nothing necessary here? seems it works without. and who is the one who called me, to call him back?
            default:
                assert (false);  // should not occur for 1hop-pieces
        }
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"}");
    }

    private void doNowPropagateDistanceChangeToAllOneHopNeighbours() {    // final int minDist, final int maxDist) {
        minDistsDirty();  // force re-calc
        ConditionalDistance suggestion = minDistanceSuggestionTo1HopNeighbour();
        for (VirtualOneHopPieceOnSquare n: singleNeighbours) {
            n.setAndPropagateOneHopDistance(suggestion);
        }
    }

    protected void quePropagateDistanceChangeToAllOneHopNeighbours() {    // final int minDist, final int maxDist) {
        myPiece().quePropagation(
                minDistanceSuggestionTo1HopNeighbour().dist(),
                this::doNowPropagateDistanceChangeToAllOneHopNeighbours);
    }

    private void doNowPropagateDistanceChangeToUninformedNeighbours() {
        minDistsDirty();  // force re-calc
        ConditionalDistance suggestion = minDistanceSuggestionTo1HopNeighbour();
        for (VirtualOneHopPieceOnSquare n: singleNeighbours)
            if (n.getRawMinDistanceFromPiece().isInfinite())
                 n.setAndPropagateOneHopDistance(suggestion);
    }

    @Override
    protected void quePropagateDistanceChangeToUninformedNeighbours() {
        myPiece().quePropagation(
                minDistanceSuggestionTo1HopNeighbour().dist(),
                this::doNowPropagateDistanceChangeToUninformedNeighbours);
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
                minimum.addLastMoveOrigins( rawMinDistance.getLastMoveOrigins() );  // conserve previous move origins
                updateRawMinDistanceFrom(minimum);
                return +1;
            }
            rawMinDistance.addLastMoveOrigins( minimum.getLastMoveOrigins() );
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
            if ( rawMinDistance.addLastMoveOrigins( suggestedDistance.getLastMoveOrigins() ) ) // add the origins of the equivalently good new suggestion
                return NONE;  // TODO:later Should return ALLDIRS here, to propagate knowledge about move origins, but performance impact needs to be checked first + if loops can come up ore are successfully caught by the if here. Also, move origins are not propagated on, yet, so it does not matter now, does it?
            return NONE;
        }
        // from here on, the new suggestion is in any case not the minimum. it is worse than what I already know
        if (minDistanceSuggestionTo1HopNeighbour().cdIsSmallerThan(suggestedDistance)) {
        /*    if (suggestedDistance.cdEqualDistButNogo(rawMinDistance)) {
                ConditionalDistance onlyAbove = rawMinDistance;
                rawMinDistance = new ConditionalDistance(suggestedDistance);
                minDistsDirty();
                //rawMinDistance.updateFrom(suggestedDistance);
                minDistanceSuggestionTo1HopNeighbour();
                propagateResetIfUSWToAllNeighbours(onlyAbove);
                return ALLDIRS;
            }*/
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
            // we are below the reset-limit -> our caller was not our predecessor on the shortest in-path
            // or we are at a square that was already visited
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,".");
            //we reached the end of the reset. from here we should propagate back the correct value
            // there, we need to get update from best neighbour (but not now, only later with breadth propagation).
            //propagateDistanceChangeToAllOneHopNeighbours();
            myPiece().quePropagation(
                    0,
                    this::recalcRawMinDistanceFromNeighboursAndPropagate);
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
                if (!onepathcheck)
                    return false;  // we found one clear path
            }
        return true;
    }

    @Override
    Set<VirtualPieceOnSquare> calcPredecessors() {
        // TODO: and for castling
        // Todo: ond for pawn promotions
        Set<VirtualPieceOnSquare> res = new HashSet<>(8);
        for (VirtualPieceOnSquare n : getNeighbours())
            if (n!=null && n!=this && n.getRawMinDistanceFromPiece().cdIsSmallerThan(getRawMinDistanceFromPiece()))
                res.add(n);
        return res;
    }

    /**
     * @returns same list as predecessors for 1hop pieces
     */
    Set<VirtualPieceOnSquare> calcDirectAttackVPcs() {
        // nothing to do directAttackSquares are equal to predecessors for one hop pieces
        return getPredecessors();
    }

    @Override
    Set<VirtualPieceOnSquare> calcShortestReasonableUnconditionedPredecessors() {
        //TODO? clarify: what happend to unconditioned here?
        return getPredecessors().stream()
                .filter(n->n.minDistanceSuggestionTo1HopNeighbour().cdIsSmallerOrEqualThan(rawMinDistance))
                .filter(n->!n.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                .collect(Collectors.toSet());
    }

    @Override
    Set<VirtualPieceOnSquare> calcShortestReasonablePredecessors() {
        return getPredecessors().stream()
                .filter(n->n.minDistanceSuggestionTo1HopNeighbour().cdIsSmallerOrEqualThan(rawMinDistance))
                .filter(n->!n.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                .collect(Collectors.toSet());
    }

}
