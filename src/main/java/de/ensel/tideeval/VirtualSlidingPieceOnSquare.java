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

import java.util.*;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ChessBasics.ANYWHERE;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;


public class VirtualSlidingPieceOnSquare extends VirtualPieceOnSquare {

    // array of slinging neighbours, one per direction-index (see ChessBasics)
    private final VirtualSlidingPieceOnSquare[] slidingNeighbours;
    // .. and the corresponding distances suggested by these neighbours.
    private final ConditionalDistance[] suggDistFromSlidingNeighbours = new ConditionalDistance[MAXMAINDIRS];

    private int uniqueShortestWayDirIndex;   // if only one shortest way to this square exists, this is the direction back to the piece
    // it also shows where the minimal suggestedDist was. it is -1 if there is more than one way here
    private final long[] latestUpdateFromSlidingNeighbour = new long[MAXMAINDIRS];

    public VirtualSlidingPieceOnSquare(ChessBoard myChessBoard, int newPceID, int pceType, int myPos) {
        super(myChessBoard, newPceID, pceType, myPos);
        slidingNeighbours = new VirtualSlidingPieceOnSquare[MAXMAINDIRS];
        uniqueShortestWayDirIndex = NONE;
        resetSlidingDistances();
    }

    protected void resetSlidingDistances() {
        for (int i = 0; i < MAXMAINDIRS; i++) {
            if (suggDistFromSlidingNeighbours[i]==null)
                suggDistFromSlidingNeighbours[i] = new ConditionalDistance(this);
            else
                suggDistFromSlidingNeighbours[i].reset();
            latestUpdateFromSlidingNeighbour[i]=0;
        }
    }

    @Override
    public void addSlidingNeighbour(VirtualPieceOnSquare neighbourPce, int direction) {
        slidingNeighbours[convertMainDir2DirIndex(direction)] = (VirtualSlidingPieceOnSquare) neighbourPce;
    }

    // set up initial distance from this vPces position - restricted to distance depth change
    @Override
    public void setAndPropagateDistance(final ConditionalDistance distance) {   // }, int minDist, int maxDist ) {
        setAndPropagateDecreasingDistanceObeyingPassthrough(distance);  //minDist, maxDist );
    }

    @Override
    protected void quePropagateDistanceChangeToAllNeighbours() {
        quePropagateDistanceChangeToSlidingNeighboursExceptDir(-1);
    }

    @Override
    protected void quePropagateDistanceChangeToUninformedNeighbours() {
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++) {
            VirtualSlidingPieceOnSquare n = slidingNeighbours[dirIndex];
            if ( n != null ) {
                ConditionalDistance suggestion = getSuggestionToPassthroughIndex(dirIndex);
                int finalDirIndex = dirIndex;
                myPiece().quePropagation(
                        suggestion.dist(),
                        () -> doNowPropagateDistanceChangeToOneUninformedNeighbours(finalDirIndex));
            }
        }
    }

    protected void doNowPropagateDistanceChangeToOneUninformedNeighbours(int dirIndex) {
        VirtualSlidingPieceOnSquare n = slidingNeighbours[dirIndex];
        if ( n.suggDistFromSlidingNeighbours[oppositeDirIndex(dirIndex)].isInfinite() ) {
                n.setAndPropagateAnyDistanceObeyingPassthrough( dirIndex );
        }
    }

    // set up initial distance from this vPces position - restricted to distance depth change - the subclass internal version...
    private void setAndPropagateDecreasingDistanceObeyingPassthrough(final ConditionalDistance distance) {
        setAndPropagateDecreasingDistanceObeyingPassthrough(distance, FROMNOWHERE);
    }

    private void setAndPropagateDecreasingDistanceObeyingPassthrough(final VirtualSlidingPieceOnSquare callingNeighbour,
                                                                     final int passingThroughInDirIndex ) {
        setAndPropagateDecreasingDistanceObeyingPassthrough(
                callingNeighbour.getSuggestionToPassthroughIndex(passingThroughInDirIndex),
                passingThroughInDirIndex );
    }

    private void setAndPropagateDecreasingDistanceObeyingPassthrough(final ConditionalDistance suggestedDistance,
                                                                     final int passingThroughInDirIndex ) {
        assert(suggestedDistance.dist()>=0);
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," {"+squareName(myPos)+"_"+ suggestedDistance);
        if (suggestedDistance.dist()==0) {
            // I carry my own piece, i.e. distance=0.  test is needed, otherwise I'd act as if I'd find my own piece here in my way...
            rawMinDistance = suggestedDistance;  //new Distance(0);
            minDistsDirty();
            uniqueShortestWayDirIndex = NONE;
            assert(passingThroughInDirIndex==FROMNOWHERE);
            quePropagateDistanceChangeToAllNeighbours();
            return;
        }
        //else
        /*if (suggestedDistance.getShortestDistanceEvenUnderCondition() > maxDist) {
            // over max, we stop here
            return;
        }*/
        //else...
        if (passingThroughInDirIndex!=FROMNOWHERE ) {
            //  the distance changed from a certain, specified direction  "passingThroughInDirIndex"
            int neededPropagationDir = updateRawMinDistancesNonIncreasingly(suggestedDistance,
                    oppositeDirIndex(passingThroughInDirIndex));
            //if ( updateSuggestedDistanceInPassthroughDirIndex(suggestedDistance, passingThroughInDirIndex) ) {
            // update rawMin first, so the propagate methods will calculate the right propagation value
            /*if (rawMinDistance.getShortestDistanceEvenUnderCondition()<minDist) {
                // I am still in a range that is already set
                // I change nothing, but get the propagation rolling
                propagateDistanceChangeToOutdatedNeighbours(minDist, maxDist);
                return;
            }
            if (rawMinDistance.getShortestDistanceEvenUnderCondition()==minDist) {   // TODO!! - splitting does not works - ends up in endles loop or misses necessary updates
                // I am at the border of what was already set
                neededPropagationDir = ALLDIRS;
            }*/

            switch(neededPropagationDir) {
                case NONE:
                    return;
                case ALLDIRS:
                    debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"|");
                    // and, if new distance is different from what it was, also tell all other neighbours
                    quePropagateDistanceChangeToSlidingNeighboursExceptDir(-1);
                    break;
                default:
                    // here, only the passthroughSuggestion has changed, so inform opposite neighbour
                    // only in one special case, it is unsure if the information needs to be reflected
                    // back to the neighbour, because he might be wrong... (this is signales by -dir)
                    if (neededPropagationDir < 0) {
                        neededPropagationDir = -neededPropagationDir-1;   // the calculation is like the 2s compliment to avoid -0=+0
                        quePropagateDistanceChangeToSlidingNeighbourInDir(oppositeDirIndex(neededPropagationDir));
                    }
                    quePropagateDistanceChangeToSlidingNeighbourInDir(neededPropagationDir);
            }
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"}");
            return;
        }
        // we should never end up here
        assert(false);
    }

    private void setAndPropagateAnyDistanceObeyingPassthrough(final int passingThroughInDirIndex ) {
        int fromDirIndex = oppositeDirIndex(passingThroughInDirIndex);
        ConditionalDistance suggestedDistance = slidingNeighbours[fromDirIndex]
                .getSuggestionToPassthroughIndex(passingThroughInDirIndex);
        if ( suggDistFromSlidingNeighbours[fromDirIndex].cdIsSmallerThan(suggestedDistance)
               // || suggDistFromSlidingNeighbours[fromDirIndex].distEquals(suggestedDistance)
               // && suggDistFromSlidingNeighbours[fromDirIndex].hasFewerConditionsThan(suggestedDistance)
        )
            setAndPropagateIncreasingDistanceObeyingPassthrough(suggestedDistance,passingThroughInDirIndex);
        else
            setAndPropagateDecreasingDistanceObeyingPassthrough(suggestedDistance,passingThroughInDirIndex);
    }


    private void setAndPropagateAnyDistanceObeyingPassthrough(final ConditionalDistance suggestedDistance,
                                                                     final int passingThroughInDirIndex ) {
        int fromDirIndex = oppositeDirIndex(passingThroughInDirIndex);
        if ( suggDistFromSlidingNeighbours[fromDirIndex].cdIsSmallerThan(suggestedDistance)
             // || suggDistFromSlidingNeighbours[fromDirIndex].distEquals(suggestedDistance)
             //    && suggDistFromSlidingNeighbours[fromDirIndex].hasFewerConditionsThan(suggestedDistance)
        )
            setAndPropagateIncreasingDistanceObeyingPassthrough(suggestedDistance,passingThroughInDirIndex);
        else
            setAndPropagateDecreasingDistanceObeyingPassthrough(suggestedDistance,passingThroughInDirIndex);
    }

    private void setAndPropagateIncreasingDistanceObeyingPassthrough(final ConditionalDistance suggestedDistance,
                                                                     final int passingThroughInDirIndex ) {
        assert(suggestedDistance.dist()>=0);
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," {i"+squareName(myPos)+"_"+ suggestedDistance);
        if (passingThroughInDirIndex == FROMNOWHERE) {
            // I carry my own piece, i.e. distance=0. ot another definite set of distance (like after moving away to 1)
            rawMinDistance = suggestedDistance;  //new Distance(0);
            minDistsDirty();
            if (suggestedDistance.dist()==0)
                uniqueShortestWayDirIndex = NONE;
            quePropagateIncreasingDistanceChangeToAllSlidingNeighbours();
            return;
        }
        int fromDirIndex = oppositeDirIndex(passingThroughInDirIndex);
        if (rawMinDistance.dist()==0) {
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            return;  // there is nothing closer than myself...
        }
        //assert(passingThroughInDirIndex!=FROMNOWHERE);
        //  the distance changed from a certain, specified direction  "passingThroughInDirIndex"
        int neededPropagationDir; // = updateRawMinDistanceWithIncreasingSuggestionFromDirIndex(suggestedDistance,oppositeDirIndex(passingThroughInDirIndex));
        if ( suggDistFromSlidingNeighbours[fromDirIndex].equals(suggestedDistance )
            //&& suggDistFromSlidingNeighbours[fromDirIndex].conditionsEqual(suggestedDistance )
            ) {
            // the same suggestion value that we already had from this direction
            return;
        }
        latestUpdateFromSlidingNeighbour[fromDirIndex] = getOngoingUpdateClock();

        if ( uniqueShortestWayDirIndex==NONE) {
            // this vPce was still completely unset
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            rawMinDistance.updateFrom(suggestedDistance);
            minDistsDirty();
            uniqueShortestWayDirIndex = fromDirIndex;
            neededPropagationDir = ALLDIRS;
        }
        else if ( uniqueShortestWayDirIndex==MULTIPLE
            && rawMinDistance.cdEquals(suggDistFromSlidingNeighbours[fromDirIndex])
        ) {
            // an update coming in from one of several shortest paths known, so. update and recalc
            if (suggestedDistance.cdIsSmallerThan(rawMinDistance)) {
                // new suggestion is even smaller than the currently smallest.
                // old argument ;-): This is a conflict with this being an "Increasing" method !?
                //      It turns out, that this can happen due to not-yet-updated neighbours injecting their shorter (old) way via slinding
                //      for now, we stop it here and hope for an update once this old neighbour is updated.
                      //rawMinDistance.updateFrom(suggestedDistance);
                      //uniqueShortestWayDirIndex = passingThroughInDirIndex;
                      //neededPropagationDir = ALLDIRS;
                neededPropagationDir = NONE;
                // but: this here is also called decreasingly, e.g. when a piece moves away, and dist to here got shorter
                //      observed after d4 (1st move) on d2, where d4 is reevaluated first, which increases distances, then square d2 is reevaluated decreasing again over the diagonal side ways
                //      check: Is there now a conflict with the old argument here? Hope that has been solved via the better que order (que/doNow)
                //rawMinDistance.updateFrom(suggestedDistance);
                //minDistsDirty();
                //uniqueShortestWayDirIndex = passingThroughInDirIndex;
                //neededPropagationDir = ALLDIRS;
                // but then: the old argument is better, why should the increasing method be call then. Go and find the bug in the calls to here.
            } else {
                suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
                // recalcing rawMinDist should not be necessary, because only one of multiple ways
                // was increasing:  recalcRawMinDistance();
                uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
                neededPropagationDir = passingThroughInDirIndex;
            }
        }
        else if (fromDirIndex==uniqueShortestWayDirIndex) {
            // bad news: the unique shortest in-path got longer
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            recalcRawMinDistance();
            uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
            neededPropagationDir = ALLDIRS;
        } else {
            // an update from an irrelevant direction
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            neededPropagationDir = passingThroughInDirIndex;
        }

        switch(neededPropagationDir) {
            case NONE:
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,".i}");
                return;
            case ALLDIRS:
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"*");
                // and, if new distance is different from what it was, also tell all other neighbours
                quePropagateIncreasingDistanceChangeToAllSlidingNeighbours();
                break;
            default:
                // here, only the passthroughSuggestion has changed, so inform opposite neighbour
                // only in one special case, it is unsure if the information needs to be reflected
                // back to the neighbour, because he might be wrong... (this is signales by -dir)
                if (neededPropagationDir < 0) {
                    neededPropagationDir = -neededPropagationDir-1;   // the calculation is like the 2s compliment to avoid -0=+0
                    quePropagateIncreasingDistanceChangeToSlidingNeighbourInDir(oppositeDirIndex(neededPropagationDir));
                }
                quePropagateIncreasingDistanceChangeToSlidingNeighbourInDir(neededPropagationDir);
        }
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"i}");
    }

    /** inform one neighbour:
     * propagate my distance to the neighbour in direction "passingThroughInDirIndex" (if there is one)
     * @param passingThroughInDirIndex neighbour to be informed is in that direction
     * @param updateAgeLimit but only if this neighbour has a lower update"age" than "updateAgeLimit"
     */
    private void quePropagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(final int passingThroughInDirIndex, final long updateAgeLimit) {
        // inform one (opposite) neighbour
        // TODO!: check: update limit is propably only working correctly for the slidingNeigbourUpdateTimes, since update-time is only remembered if rawMinDistance really changes
        VirtualSlidingPieceOnSquare n = slidingNeighbours[passingThroughInDirIndex];
        if (n != null
                && n.latestUpdateFromSlidingNeighbour[oppositeDirIndex(passingThroughInDirIndex)]
                    <=updateAgeLimit) {
            ConditionalDistance suggestion = getSuggestionToPassthroughIndex(passingThroughInDirIndex);
            myPiece().quePropagation( min(suggestion.dist(),   // que at either the correct new distance - or (if smaller) the previous, smaller dist, to be sure to update it in time!  //Todo!!: Check if same is necessary for other Piece-Types! probably yes!
                                      n.suggDistFromSlidingNeighbours[oppositeDirIndex(passingThroughInDirIndex)].dist() ) ,
                        ()-> doNowPropagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(
                                passingThroughInDirIndex, updateAgeLimit));
        }
    }

    private void doNowPropagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(final int passingThroughInDirIndex, final long updateAgeLimit) {
        // inform one (opposite) neighbour
        // TODO!: check: update limit is propably only working correctly for the slidingNeigbourUpdateTimes, since update-time is only remembered if rawMinDistance really changes
        VirtualSlidingPieceOnSquare n = slidingNeighbours[passingThroughInDirIndex];
        if (n.latestUpdateFromSlidingNeighbour[oppositeDirIndex(passingThroughInDirIndex)]
                <= updateAgeLimit) {
            n.setAndPropagateAnyDistanceObeyingPassthrough( passingThroughInDirIndex);
        }
    }

    private void quePropagateIncreasingDistanceChangeToSlidingNeighbourInDir(final int passingThroughInDirIndex) {
        // que in to inform one of my (opposite) neighbours
        if (slidingNeighbours[passingThroughInDirIndex] != null) {
            myPiece().quePropagation(
                    getSuggestionToPassthroughIndex(passingThroughInDirIndex).dist(),
                    ()->doNowPropagateIncreasingDistanceChangeToSlidingNeighbourInDir(passingThroughInDirIndex));
        }
    }

    private void doNowPropagateIncreasingDistanceChangeToSlidingNeighbourInDir(final int passingThroughInDirIndex) {
        // inform one (opposite) neighbour
        VirtualSlidingPieceOnSquare n = slidingNeighbours[passingThroughInDirIndex];
        ConditionalDistance suggestion = getSuggestionToPassthroughIndex(passingThroughInDirIndex);
            //TODO: think about and handle no-go squares also for increasing distances
            // originally, without Queing it was:
            //  n.setAndPropagateIncreasingDistanceObeyingPassthrough(suggestion, passingThroughInDirIndex);
            // but old (not-yet-updated) sliding-suggestions could interfer with nogo-1-hop-suggestions and trigger an endless update-loop (self-feeding a sliding row cont. up and down)
            // it is not nice, to prevent this by breadth-search-approach only, but currently, there ist no better idea...
        n.setAndPropagateIncreasingDistanceObeyingPassthrough(
                                suggestion,
                                passingThroughInDirIndex);
    }

    private void quePropagateDistanceChangeToSlidingNeighbourInDir(final int passingThroughInDirIndex) {
        quePropagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(passingThroughInDirIndex,Integer.MAX_VALUE);
    }


    private void quePropagateDistanceChangeToSlidingNeighboursExceptDir(final int excludeDirIndex) {
        quePropagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(excludeDirIndex, Long.MAX_VALUE);
    }

    private void quePropagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(final int excludeDirIndex, final long updateAgeLimit) {
        // for the slidingNeighbours, we need to check from which direction the figure is coming from
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (dirIndex!=excludeDirIndex)
                quePropagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(
                        dirIndex,
                        updateAgeLimit
                );
    }

    protected void propagateAnyChangeToAllSlidingNeighbours() {
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++) {
            // inform neighbours one by one
            VirtualSlidingPieceOnSquare n = slidingNeighbours[dirIndex];
            if (n != null) {
                ConditionalDistance suggestion = getSuggestionToPassthroughIndex(dirIndex);
                // start with depth first propagation...  (do not want to mix up with breadth propagation of decreasing values for now...)
                n.setAndPropagateAnyDistanceObeyingPassthrough(suggestion,dirIndex);
            }
        }
    }

    protected void quePropagateIncreasingDistanceChangeToAllSlidingNeighbours() {
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            quePropagateIncreasingDistanceChangeToSlidingNeighbourInDir(dirIndex);
    }



    /**
     *  calculate the suggestion to a neighbour, possibly using a passthrough
     * @param passthroughDirIndex direction for which the passthrough is checked
     * @return the distance to suggest to the next neighbour in that direction
     */
    ConditionalDistance getSuggestionToPassthroughIndex(int passthroughDirIndex) {
        int fromDirIndex = oppositeDirIndex(passthroughDirIndex);

        ConditionalDistance suggestion = new ConditionalDistance( minDistanceSuggestionTo1HopNeighbour() );
        if ( // I am at my own square
             rawMinDistance.dist()==0
             // or if there is no sliding way
             ||   suggDistFromSlidingNeighbours[fromDirIndex].dist() == INFINITE_DISTANCE
        ) {
            return suggestion;
        }
        // now this is either the same (take a corner after the shortest distance)
        // or stay on the passthrough towards opposite neighbour:
        // but this might have a penalty if own figure is in the way:
        if (board.hasPieceOfColorAt( myPiece().color(), myPos )) {
            // own piece in the way
            int penalty = movingMySquaresPieceAwayDistancePenalty();
            if (penalty!=INFINITE_DISTANCE) {
                ConditionalDistance d = new ConditionalDistance(
                        suggDistFromSlidingNeighbours[fromDirIndex],
                        penalty,
                        myPos, ANYWHERE, myPiece().color());
                suggestion.reduceIfCdIsSmallerOrAddLastMOIfEqual(d);  // if d=suggestion from sliding-source-direction
                                                            // neighbour and 1-hop-suggestion are of EQUAL distance,
                                                            // then the suggestion is enriched with d's move origins.
                // TODO: Scheint nicht falsch, aber könnte effizienter implementiert werden, wenn die Annahme stimmt,
                //  dass das d wg. der penalty eh niemals kleiner sein kann als die suggestion (die auch die selbe penalty
                //  enthält und ansonsten das minimum aus den verschiedenen Richtungen ist.
            }
        }
        else {
            boolean opponentColor = myOpponentsColor();
            if (board.hasPieceOfColorAt( opponentColor, myPos )) {
                // an opponent Piece is in the way here - this needs penalty in some cases:
                // do not count the first opponent moving away as distance, but later do count (this is not very precise...)
                int inc = suggDistFromSlidingNeighbours[fromDirIndex].isUnconditional() ? 0 : 1;
                // and it additionally needs the condition that the piece moves away to allow passthrough
                ConditionalDistance d = new ConditionalDistance(  // not necessary, is already part of the neighbour's suggestion:  slidingNeighbours[fromDirIndex],  // do not take this, but the origin from where it slides over this
                        suggDistFromSlidingNeighbours[fromDirIndex],
                         inc,
                        myPos, ANYWHERE, opponentColor  //TODO: topos-condition must not be ANY, but "anywhere except in that direction"
                );
                suggestion.reduceIfCdIsSmallerOrAddLastMOIfEqual( d );
            } else {
                // passthrough possible
                suggestion.reduceIfCdIsSmallerOrAddLastMOIfEqual(suggDistFromSlidingNeighbours[fromDirIndex]);
            }
        }
        return suggestion;
    }



    /**
     * Updates the overall minimum distance
     * @param suggestedDistance the suggested update for the distance (from a neighbour)
     * @param fromDirIndex tells from which direction it was suggested (needed for correct sliding through)
     * @return int what needs to be updated. NONE for nothing, ALLDIRS for all,
     *         below that >0 tells a single directionindex  (see ChessBasics)
     *         -1 to -8 (i.e. -DIRINDEX) is used for two update directions on one axis: so the equiv. to (-n-1) and the opposite direction of that.
     */
    private int updateRawMinDistancesNonIncreasingly(final ConditionalDistance suggestedDistance,
                                                     final int fromDirIndex) {
        latestUpdateFromSlidingNeighbour[fromDirIndex] = getOngoingUpdateClock();
        if (rawMinDistance.dist()==0) {
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            return NONE;  // there is nothing closer than myself... noo need to reporpagate from here, everyone knows ;-)
        }
        // the new suggestion is in any case not the minimum
        if ( suggDistFromSlidingNeighbours[fromDirIndex].cdEquals(suggestedDistance ) ) {
                //&& !suggestedDistance.hasFewerConditionsThan(suggestedDistanceFromSlidingNeighbours[fromDirIndex])  ) {
            // the same suggestion value that we already had from this direction
            return NONE;
        }
        if (suggestedDistance.cdIsSmallerThan(rawMinDistance)
                || suggestedDistance.cdIsEqualButDifferentSingleCondition(rawMinDistance)
        ) {     // (1a)(7)(10)(4)
            // the new distance is smaller than the minimum, so we already found the new minimum
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            updateRawMinDistanceFrom(suggestedDistance);
            uniqueShortestWayDirIndex = fromDirIndex;
            return ALLDIRS;
        }
        if (suggestedDistance.cdEquals(rawMinDistance)) {           // (8)(10)(5)
            // the same suggestion distance (and condition length) that we already have als minimum
            if (uniqueShortestWayDirIndex==fromDirIndex)                  // (1b)
                return NONE;     // even the direction matches the known shortestWay, so nothing to do
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            //if (!suggestedDistance.distIsReallySmaller(rawMinDistance)) // must be same nr. of conditions
            uniqueShortestWayDirIndex = MULTIPLE;
            rawMinDistance.addLastMoveOrigins(suggestedDistance.getLastMoveOrigins() );
            return oppositeDirIndex(fromDirIndex);  //because this value is new from this direction, we better pass it on
        }
        // from here on, the new suggestion is in any case not the minimum, but might be smaller than the previous from this direction still
        if ( suggestedDistance.cdIsSmallerThan(suggDistFromSlidingNeighbours[fromDirIndex])
                || suggestedDistance.cdIsEqualButDifferentSingleCondition(suggDistFromSlidingNeighbours[fromDirIndex])
        ) {
            // a smaller suggestion value than we already had from this direction
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            if ( getSuggestionToPassthroughIndex(fromDirIndex).cdIsSmallerOrEqualThan(suggestedDistance) )
                return -oppositeDirIndex(fromDirIndex)-1;  // tell also backwards, that the last suggestion might not have been the best
            return oppositeDirIndex(fromDirIndex);
        }
        if ( uniqueShortestWayDirIndex==fromDirIndex) {   // (2)(3)(1c)
            // a longer update coming in from the known single shortest path
            // this must be an old update-call
            return NONE;

            /* TODO; check if the following is still necessary or if this only happens for increases, which ae not possible (because covered by th reset-bombs)
            // we have to check was has become of the former shortest in-path and recalc the unique shortest path
            suggestedDistanceFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            int minUpdated = recalcRawMinDistance();
            uniqueShortestWayDirIndex = calcUniqueShortestWayDir();
            uniqueShortestConditionalWayDirIndex = calcUniqueShortestConditionalWayDir();
            if (minUpdated!=0)
                return oppositeDirIndex(fromDirIndex);
            return NONE;*/
        }
        if ( uniqueShortestWayDirIndex==MULTIPLE
                && suggDistFromSlidingNeighbours[fromDirIndex].cdEquals(rawMinDistance) ) {       // (9)
            // a longer update coming in from one of the shortest paths known
            // this must be an old update-call
            return NONE;
            /*// this means, we don not yet have to worry, because another shorter path exists
            // but we need to check and flag, if the remaining in-path is now the only one left = unique
            suggestedDistanceFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            uniqueShortestWayDirIndex = calcUniqueShortestWayDir();
            uniqueShortestConditionalWayDirIndex = calcUniqueShortestConditionalWayDir();
            if ( getSuggestionToPassthroughIndex(fromDirIndex).isSmallerOrEqual(suggestedDistance) ) {
                // the neighbour seems to be outdated, let me inform him.
                return -oppositeDirIndex(fromDirIndex)-1;
            }
            return oppositeDirIndex(fromDirIndex)-1; */
        }
        // an real update from a neighbour that was Further away (at the time of the call creation)
        // this must be an old update-call
        //  needed to complete edge cases after "reset-bomb"
        //  if updateDistance is worse than the recommendation towards that fromPos from here would be,
        //  then tell this  back to this fromPosNeighbour, so it can update its surroundings.
        /*if ( getSuggestionToPassthroughIndex(fromDirIndex).isSmallerOrEqual(suggestedDistance)
                ||  getSuggestionToPassthroughIndex(fromDirIndex).hasSmallerOrEqualConditionalDistance(suggestedDistance) )
            return fromDirIndex; */
        return NONE;
        //return oppositeDirIndex(fromDirIndex);
    }

    /**
     * updates rawMinDistance from stored Neighbour-suggestions
     */
    private void recalcRawMinDistance() {
        if (rawMinDistance.dist()==0)
            return;  // there is nothing closer than myself...
        //rawMinDistance = (IntStream.of(suggestedDistanceFromNeighbours)).min().getAsInt();
        ConditionalDistance minimum = new ConditionalDistance(this);
        // finds new minimum and is at the same like calcUniqueShortestWayDir() but does not rely on the not yet correct rawMinDist.
        uniqueShortestWayDirIndex = NONE;
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (slidingNeighbours[dirIndex]!=null) {
                if (minimum.reduceIfCdIsSmaller(suggDistFromSlidingNeighbours[dirIndex]))
                    uniqueShortestWayDirIndex = dirIndex;  // we found (one of) the shortest in-paths
                else if (uniqueShortestWayDirIndex>=0
                        && minimum.cdEquals(suggDistFromSlidingNeighbours[dirIndex]) ) {
                    uniqueShortestWayDirIndex = MULTIPLE;   // again, but as this is already the second, we have multiple shortest in-paths
                    minimum.addLastMoveOrigins( suggDistFromSlidingNeighbours[dirIndex].getLastMoveOrigins() );
                }
            }
        updateRawMinDistanceFrom(minimum);

        /* not needed for now:
        * @return 0: value did not change;  +1: value increased;  -1: value decreased;
        if (rawMinDistance.distEquals(minimum)
            && rawMinDistance.conditionEquals(minimum) )
            return 0;
        if (minimum==null) {
            //TODO: thies piece has no(!) neighbour... this is e.g. (only case?) a pawn that has reached the final rank.
            assert(false); // should not happen for sliding figures
            return 0;
        }
        minDistsDirty();
        if (rawMinDistance.reduceIfSmaller(minimum))
            return -1;
        rawMinDistance.updateFrom(minimum);
        return +1;
        */
    }

    @Override
    protected int recalcRawMinDistanceFromNeighbours() {
        return recalcRawMinDistanceFromSlidingNeighbours();
    }

    /**
     * updates rawMinDistance from stored Neighbour-suggestions
     * @return 0: value did not change;  +1: value increased;  -1: value decreased;
     */
    private int recalcRawMinDistanceFromSlidingNeighbours() {
        ConditionalDistance minimum = new ConditionalDistance(this);
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++) {
            if (slidingNeighbours[dirIndex]!=null) {
                // get fresh update from all neighbours and recalc minimum.
                suggDistFromSlidingNeighbours[dirIndex] = slidingNeighbours[dirIndex].getSuggestionToPassthroughIndex(oppositeDirIndex(dirIndex));
                minimum.reduceIfCdIsSmallerOrAddLastMOIfEqual(suggDistFromSlidingNeighbours[dirIndex]);
            }
        }
        // TODO-OPTI:  using calcUniqueShortestWayDir() is inefficient here, as we could calculate it also already in the loop above
        if (rawMinDistance.cdEquals(minimum)) {
            uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
            rawMinDistance.addLastMoveOrigins( minimum.getLastMoveOrigins() );
            return 0;
        }
        if (reduceRawMinDistanceIfCdIsSmaller(minimum)) {
            uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
            minDistsDirty();
            //resetChances();
            return -1;
        }
        updateRawMinDistanceFrom(minimum);
        uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
        minDistsDirty();
        //resetChances();
        return +1;
    }

    @Override
    public void pieceHasArrivedHere(int pid) {
        if (DEBUGMSG_DISTANCE_PROPAGATION) {
            debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, " [" + myPceID + ":");
        }
        setLatestChangeToNow();
        // inform neighbours that something has arrived here
        board.getPiece(myPceID).startNextUpdate();
        if (pid==myPceID) {
            //my own Piece is here - but I was already told and distance set to 0
            assert (rawMinDistance.dist()==0);
            return;
        }
        // here I should update my own minDistance - necessary for same colored pieces that I am in the way now,
        // but this is not necessary as minDistance is safed "raw"ly without this influence and later it is calculated on top, if it is made "dirty"==null .
        minDistsDirty();

        // inform neighbours that something has arrived here
        // recalc the possibly increasing values from this square onward (away from piece)
        propagateAnyChangeToAllSlidingNeighbours();   // todo: do this same change from DecreasingProp to AnyProp for oneHopPieces!

        board.getPiece(myPceID).endUpdate();
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"] ");
    }


    @Override
    protected void propagateResetIfUSWToAllNeighbours() {
        // should not b called, is replaced by IncreasingPropagation
        // experimental: do not rreset, but do a, incre3asing dist value upgrade
        //propagateResetIfUSWToAllNeighboursExceptDirIndex(NONE);
    }

    @Override
    protected void resetMovepathBackTo(int frompos) {
        resetDistances();
        resetSlidingDistances();
        if (frompos==myPos)
            return;  // place reached, recursion ends
        int backDir = calcDirIndexFromTo(myPos,frompos);
        VirtualSlidingPieceOnSquare n = slidingNeighbours[backDir];
        assert(n!=null);
        n.resetMovepathBackTo(frompos);
    }

    protected void propagateLastMoveOriginToDirectlyReachableNeighbours() {
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++) {
            VirtualSlidingPieceOnSquare n = slidingNeighbours[dirIndex];
            if (n != null)
                n.propagateLastMoveOriginToDirectlyReachableNeighbours(this, dirIndex);
        }
    }

    protected void propagateLastMoveOriginToDirectlyReachableNeighbours(
            VirtualPieceOnSquare moveOrigin,
            int dirIndex
    ) {
        if (moveOrigin.minDistanceSuggestionTo1HopNeighbour().dist()
                != getSuggestionToPassthroughIndex(dirIndex).dist() )
            return;
        suggDistFromSlidingNeighbours[dirIndex].setSingleLastMoveOrigin(moveOrigin); //TODO!!: we will have to transmit the whole Set of moveorigins here!
        VirtualSlidingPieceOnSquare n = slidingNeighbours[dirIndex];
        if (n != null)
            n.propagateLastMoveOriginToDirectlyReachableNeighbours(moveOrigin, dirIndex);
        //TODO!!: we will have to transmit the whole Set of moveorigins here!
    }

    @Override
    // fully set up initial distance from this vPces position
    public void myOwnPieceHasMovedHereFrom(int frompos) {
        // one extra piece or a new hop (around the corner or for non-sliding neighbours
        // treated just like sliding neighbour, but with no matching "from"-direction
        if (DEBUGMSG_DISTANCE_PROPAGATION) {
            debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, "[" + pieceColorAndName(board.getPiece(myPceID).getPieceType())
                    + "(" + myPceID + "): propagate own distance: ");
        }
        board.getPiece(myPceID).startNextUpdate();
        rawMinDistance = new ConditionalDistance(this,0);  //needed to stop the reset-bombs below at least here
        minDistsDirty();

        if (frompos!=NOWHERE) {
            // correct neighbourSuggestions on backwards way in both directions
            int backDir = calcDirFromTo(myPos,frompos);
            int fromDirIndex = oppositeDirIndex(convertMainDir2DirIndex(backDir));
            VirtualSlidingPieceOnSquare vPce = this;
            int correctionPos=myPos;
            do {
                correctionPos+=backDir;
                vPce.suggDistFromSlidingNeighbours[convertMainDir2DirIndex(backDir)]
                        = new ConditionalDistance(slidingNeighbours[convertMainDir2DirIndex(backDir)],2);
                vPce = (VirtualSlidingPieceOnSquare)
                        (board.getBoardSquares()[correctionPos].getvPiece(myPceID));
                vPce.suggDistFromSlidingNeighbours[fromDirIndex]
                        = new ConditionalDistance(this,1); // moveOrigin is always this, not slidingNeighbours[fromDirIndex]
                vPce.uniqueShortestWayDirIndex = fromDirIndex;
            } while (correctionPos!=frompos);
            propagateLastMoveOriginToDirectlyReachableNeighbours();
            // do the propagation from the new position first
            setAndPropagateDistance(new ConditionalDistance(this,0));  // , 0, Integer.MAX_VALUE );
            // and then correct increasing distances from the frompos
            vPce.setAndPropagateIncreasingDistanceObeyingPassthrough(
                            new ConditionalDistance(this,1),
                            FROMNOWHERE);
        }
        else
            setAndPropagateDistance(new ConditionalDistance(this,0));  // , 0, Integer.MAX_VALUE );

        board.getPiece(myPceID).endUpdate();
    }

    protected void propagateResetIfUSWToAllNeighboursExceptDirIndex(int fromDirIndex) {
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (dirIndex!=fromDirIndex)
                propagateResetIfUSWTo1SlidingNeighbour(dirIndex);
    }

    private void propagateResetIfUSWTo1SlidingNeighbour(int toDir) {
        VirtualSlidingPieceOnSquare n = slidingNeighbours[toDir];
        if (n != null)
            n.propagateResetIfUSW(oppositeDirIndex(toDir));
    }

    private void propagateResetIfUSW(int fromDirIndex ) {
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," R"+squareName(myPos));
        if (rawMinDistance.dist()==0 && fromDirIndex!=FROMNOWHERE
                || rawMinDistance.isInfinite()
        ) {
            // I carry my own piece, i.e. distance=0. No reset necessary from here.
            // or I am a square that could never be reached already before
            return;
        }
        if (fromDirIndex==uniqueShortestWayDirIndex) {  // "reset-bomb" only if the neighbour was my only predecessor in the path
            // forget the dist-infos that I got from my neighbours
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"*");
            resetDistances();
            //suggestedDistanceFromSlidingNeighbours[fromDirIndex].reset(); is not sufficient, as reset also goes to all neighbours
            resetSlidingDistances();
            uniqueShortestWayDirIndex = NONE;
            propagateResetIfUSWToAllNeighbours();
        }
        else if (uniqueShortestWayDirIndex==MULTIPLE
                && suggDistFromSlidingNeighbours[fromDirIndex].cdEquals(rawMinDistance) ) {
            // only propagate reset to opposite neighbour because we were at least among several shortest inpaths
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"-");
            suggDistFromSlidingNeighbours[fromDirIndex].reset();
            uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
            //do not propagate reset in sliding direction, but the correct value, i.e. for now: non-sliding 1-hop suggestion
            //doch nicht: propagateDistanceChangeToSlidingNeighbourInDir(oppositeDirIndex(fromDirIndex));
            propagateResetIfUSWTo1SlidingNeighbour(oppositeDirIndex(fromDirIndex));
            /*myPiece().quePropagation(
                    0,
                    ()-> this.recalcRawMinDistanceFromNeighboursAndPropagate()); */
        }
        else {
            // it was just a message from a neighbour that is further away than other neighbours
            // at least forget the old input from fromDirIndex
            // (same implementation part as in setAndPropagateDistanceObeyingPassthrough();
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,".");
            suggDistFromSlidingNeighbours[fromDirIndex].reset();
            //we reached the end of the reset. from here we should propagate back the correct value
            //doch nicht: propagateDistanceChangeToSlidingNeighbourInDir(fromDirIndex);
            // instead we need to get update from best neighbour (but not now, only later with breadth propagation.
            myPiece().quePropagation(
                    0,
                    this::recalcRawMinDistanceFromNeighboursAndPropagate);
        }
    }


    @Override
    public boolean isUnavoidableOnShortestPath(int pos, int maxdepth) {
        // simplest case: I am on my own way -> yes
        if (pos == myPos)
            return true;
        // or I'm home -> I came here without touching pos, so this is possible.
        if (rawMinDistance.dist()==0
                || rawMinDistance.isInfinite()
                || maxdepth==0 )
            return false;  // the search has ended, no pos was passed.

        // make sure I know my shortest inpath
        if (uniqueShortestWayDirIndex==NONE)
            uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
        if (uniqueShortestWayDirIndex==NONE)
            return false;  // still NONE, it seems it cannot be reached at all.
        assertNotNull(rawMinDistance);
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, " "+myPceID+"(on"+myPos+")");
        if (uniqueShortestWayDirIndex>=0) {
            // only one way in, let that neighbour check...
            return slidingNeighbours[uniqueShortestWayDirIndex].isUnavoidableOnShortestPath(pos,maxdepth-1);
        }
        // ok, now it's complicated, let's assume the worst and then check if one neighbour has a path without that pos on the way
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            //careful here: this comparison must be consistent with the condition in "CD.reduceIfSmall" used in recalcRawMinDistance()
            if (slidingNeighbours[dirIndex]!=null
                    && suggDistFromSlidingNeighbours[dirIndex].cdEquals(rawMinDistance)
                    //&& suggDistFromSlidingNeighbours[dirIndex].hasFewerOrEqualConditionsThan(rawMinDistance)
            ) {
                // we found (one of) the shortest in-paths
                boolean onepathcheck = slidingNeighbours[dirIndex].isUnavoidableOnShortestPath(pos,maxdepth-1);
                if (!onepathcheck)
                    return false;  // we found one clear path
            }
        return true;
    }

    /**
     * searches in all suggestions from neighbours for the correct fromDirection where the shortest in-path is coming from.
     * @return MULTIPLE, if more than one shortest in-path was found.  returns the fromDir otherwise.
     */
    private int calcUniqueShortestWayDirIndex() {
        int newUSWDI = NONE;
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            //careful here: this comparison must be consistent with the condition in "CD.reduceIfSmall" used in recalcRawMinDistance()
            if (slidingNeighbours[dirIndex]!=null
                    && suggDistFromSlidingNeighbours[dirIndex].cdEquals(rawMinDistance)
                    // && suggDistFromSlidingNeighbours[dirIndex].hasFewerOrEqualConditionsThan(rawMinDistance)
            ) {
                // we found (one of) the shortest in-paths
                if (newUSWDI>=0)
                    return MULTIPLE;   // but this is already the second, so we have multiple shortest in-paths
                newUSWDI = dirIndex;
            }
        //assert(newUSWDI!=NONE);
        return newUSWDI;
    }

    /** checks if this vPce could reach a position (=here) in dist==1 after conditions are fulfilled. Useful
     * to select 2nd row clash candidates.  myPiece, targetpos and conditions must be on one sliding path.
     * Used for pre-selection, so it does not need to work precisely, but quickly. could tell true in false cases...
     * but never false for possibly 2nd row candiates.
     *
     * @return boolean true if fulfilled
     */
  /*  public boolean fulfilledConditionsCouldMakeDistIs1() {
        if ( calcDirFromTo(myPiece().getPos(), myPos)==NONE )
            return false;  // not even reachable in one slide...
        for (int i = 0; i < rawMinDistance.nrOfConditions(); i++) {
            int fromCond = rawMinDistance.getFromCond(i);
            int toCond = rawMinDistance.getToCond(i);
            if ( fromCond!=ANY
                    && !isBetweenFromAndTo(fromCond, myPiece().getPos(), myPos)
                    && toCond!=ANY
                    && toCond!=myPos
                    && !isBetweenFromAndTo(toCond, myPiece().getPos(), myPos) )
                return false;
        }
        return true;
    }
   */
    public boolean fulfilledConditionsCouldMakeDistIs1() {
        if ( calcDirFromTo(myPiece().getPos(), myPos)==NONE )
            return false;  // not even reachable in one slide...
        int helpfulConditions = 0;
        for (int i = 0; i < rawMinDistance.nrOfConditions(); i++) {
            int fromCond = rawMinDistance.getFromCond(i);
            int toCond = rawMinDistance.getToCond(i);
            if ( !( fromCond!= ANYWHERE
                    && !isBetweenFromAndTo(fromCond, myPiece().getPos(), myPos)
                    && toCond!= ANYWHERE
                    && toCond!=myPos
                    && !isBetweenFromAndTo(toCond, myPiece().getPos(), myPos) ) )
                helpfulConditions++;
        }
        return (getRawMinDistanceFromPiece().dist() - helpfulConditions) <= 1;
    }

    @Override
    Set<VirtualPieceOnSquare> calcShortestReasonableUnconditionedPredecessors() {
        if (!rawMinDistance.distIsNormal())
            return new HashSet<>();
        Set<VirtualPieceOnSquare> res = new HashSet<>();
        //System.out.println("Checking shortest Predecessors for  "+ this);
        for (ConditionalDistance nSugg : suggDistFromSlidingNeighbours) {
            if (nSugg != null && !nSugg.isInfinite() ) {
                //System.out.println(" nSugg=" + nSugg + ":");
                for (VirtualPieceOnSquare lmoO : nSugg.getLastMoveOrigins() ) {
                    VirtualSlidingPieceOnSquare lmo = (VirtualSlidingPieceOnSquare)lmoO;
                    if (lmo.myPos != myPos) {  // it is not a predecessor, the fastest way is through myself? no
                        ConditionalDistance lastMOminDist = lmo.getSuggestionToPassthroughIndex(calcDirIndexFromTo(lmo.myPos, myPos));
                        //TODO!: Should use nSugg here, not lastMOminDist, but this runs into infinite loops in rare cases
                        // there seems to be a bug in sliding pieces distance calculation leaving vPces to point to each other as shortest predecessors,
                        // possibly related to reaching MAX_INTERESTING_NROF_HOPS:
                        // turns up e.g. with; MAX..==6 and checkPredecessorsAndNeighboursOfTarget(board, "b7", "b3", "[a2, a4, c2, d5]", "[d5]");
                        // but not with MAX...==7!
                        //System.out.print(" - lmo=" + lmo + " suggesting: " + lastMOminDist + ".");
                        if ( lmoO.getMinDistanceFromPiece().cdIsSmallerThan(getRawMinDistanceFromPiece())
                                && lastMOminDist.cdIsSmallerOrEqualThan(rawMinDistance)
                                && !lastMOminDist.hasNoGo()
                                && nSugg.nrOfConditions() - lastMOminDist.nrOfConditions() <= 0  // no additional conditions on the last move
                        ) {
                            //System.out.print(" -> added");
                            res.add(lmoO);
                        }
                        //System.out.println(". ");
                    }
                }
            }
        }
        return res;
    }

    @Override
    Set<VirtualPieceOnSquare> calcShortestReasonablePredecessors() {
        if (!rawMinDistance.distIsNormal())
            return new HashSet<>();
        Set<VirtualPieceOnSquare> res = new HashSet<>();
        //System.out.println("Checking shortest Predecessors for  "+ this);
        for (ConditionalDistance nSugg : suggDistFromSlidingNeighbours) {
            if (nSugg != null && !nSugg.isInfinite() ) {
                //System.out.println(" nSugg=" + nSugg + ":");
                for (VirtualPieceOnSquare lmoO : nSugg.getLastMoveOrigins() ) {
                    VirtualSlidingPieceOnSquare lmo = (VirtualSlidingPieceOnSquare)lmoO;
                    if (lmo.myPos != myPos) {  // it is not a predecessor, the fastest way is through myself? no
                        ConditionalDistance lastMOminDist = lmo.getSuggestionToPassthroughIndex(calcDirIndexFromTo(lmo.myPos, myPos));
                        //TODO!: Should use nSugg here, not lastMOminDist, but this runs into infinite loops in rare cases
                        // there seems to be a bug in sliding pieces distance calculation leaving vPces to point to each other as shortest predecessors,
                        // possibly related to reaching MAX_INTERESTING_NROF_HOPS:
                        // turns up e.g. with; MAX..==6 and checkPredecessorsAndNeighboursOfTarget(board, "b7", "b3", "[a2, a4, c2, d5]", "[d5]");
                        // but not with MAX...==7!
                        //System.out.print(" - lmo=" + lmo + " suggesting: " + lastMOminDist + ".");
                        if ( lmoO.getMinDistanceFromPiece().cdIsSmallerThan(getRawMinDistanceFromPiece())
                                && lastMOminDist.cdIsSmallerOrEqualThan(rawMinDistance)
                                && !lastMOminDist.hasNoGo()
                                //&& nSugg.nrOfConditions() - lastMOminDist.nrOfConditions() <= 0  // no additional conditions on the last move
                        ) {
                            //System.out.print(" -> added");
                            res.add(lmoO);
                        }
                        //System.out.println(". ");
                    }
                }
            }
        }
        return res;
    }

    @Override
    protected List<VirtualPieceOnSquare> getNeighbours() {
        /*just for debuggging
        List<VirtualPieceOnSquare> res = new ArrayList<>(8);
        System.out.println("sns of " + this + ": ");
        for (int n=0; n<slidingNeighbours.length; n++)
            if (slidingNeighbours[n]!=null && slidingNeighbours[n]!=this) {
                System.out.println(" sn=" + slidingNeighbours[n] + ". ");
                res.add(slidingNeighbours[n]);
            }
        return res;*/
        return Collections.unmodifiableList( Arrays.asList(slidingNeighbours) );
    }

    @Override
    public Set<VirtualPieceOnSquare> calcPredecessors() {
        if (!rawMinDistance.distIsNormal())
            return new HashSet<>();
        // size of 8 is exacly sufficient for all 1hop pieces,
        // but might be too small for slidigPieces on a largely empty board
        Set<VirtualPieceOnSquare> res = new HashSet<>(8);
        for (ConditionalDistance nSugg : suggDistFromSlidingNeighbours) {
            //ConditionalDistance lastMOminDist = nSugg.lastMoveOrigin().minDistanceSuggestionTo1HopNeighbour();
            if (nSugg != null ) {
                for (VirtualPieceOnSquare lmo : nSugg.getLastMoveOrigins() ) {
                    if (nSugg != null && lmo.getRawMinDistanceFromPiece().cdIsSmallerThan(getRawMinDistanceFromPiece())) {
                        if (lmo.myPos != myPos)
                            res.add(lmo);
                    }
                }
            }
        }
        return res;
    }


    @Override
    public String getShortestInPathDirDescription() {
        return TEXTBASICS_FROM + " " + dirIndexDescription(uniqueShortestWayDirIndex);
    }


    @Override
    public String getDistanceDebugDetails() {
        return
                ", Sugg.s=" + Arrays.toString(suggDistFromSlidingNeighbours) +
                ", lUpd.=" + Arrays.toString(latestUpdateFromSlidingNeighbour) +
                super.getDistanceDebugDetails();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        boolean equal = super.equals((VirtualPieceOnSquare) o);
        VirtualSlidingPieceOnSquare other = (VirtualSlidingPieceOnSquare) o;
        equal &= compareWithDebugMessage(this + ".Unique Shortest Way Dir Index",
                uniqueShortestWayDirIndex, other.uniqueShortestWayDirIndex);
        //equal &= compareWithDebugMessage(this + "Unique Shortest Conditional Way Dir Index", uniqueShortestConditionalWayDirIndex, other.uniqueShortestConditionalWayDirIndex);
        equal &= compareWithDebugMessage(this + ".SuggestedDistanceFromNeighbour",
                suggDistFromSlidingNeighbours, other.suggDistFromSlidingNeighbours);
        return equal;
    }

}

