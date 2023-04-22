/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ChessBasics.ANY;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
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
                suggDistFromSlidingNeighbours[i] = new ConditionalDistance();
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
    protected void propagateDistanceChangeToAllNeighbours() {
        propagateDistanceChangeToSlidingNeighboursExceptDir(-1);
    }

    @Override
    protected void propagateDistanceChangeToUninformedNeighbours() {
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++) {
            VirtualSlidingPieceOnSquare n = slidingNeighbours[dirIndex];
            if (n != null
                    && n.suggDistFromSlidingNeighbours[oppositeDirIndex(dirIndex)].isInfinite() ) {
                ConditionalDistance suggestion = getSuggestionToPassthroughIndex(dirIndex);
                int finalDirIndex = dirIndex;
                myPiece().quePropagation(
                        suggestion.dist(),
                        () -> n.setAndPropagateAnyDistanceObeyingPassthrough(  //setAndPropagateDecreasingDistanceObeyingPassthrough(
                                this, //suggestion,
                                finalDirIndex));
            }
        }
        /*for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (suggestedDistanceFromSlidingNeighbours[dirIndex].isInfinite())
                propagateDistanceChangeToSlidingNeighbourInDir(
                        dirIndex,
                        Long.MAX_VALUE
                );*/
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
            propagateDistanceChangeToAllNeighbours();
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
                    propagateDistanceChangeToSlidingNeighboursExceptDir(-1);
                    break;
                default:
                    // here, only the passthroughSuggestion has changed, so inform opposite neighbour
                    // only in one special case, it is unsure if the information needs to be reflected
                    // back to the neighbour, because he might be wrong... (this is signales by -dir)
                    if (neededPropagationDir < 0) {
                        neededPropagationDir = -neededPropagationDir-1;   // the calculation is like the 2s compliment to avoid -0=+0
                        propagateDistanceChangeToSlidingNeighbourInDir(oppositeDirIndex(neededPropagationDir));
                    }
                    propagateDistanceChangeToSlidingNeighbourInDir(neededPropagationDir);
            }
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"}");
            return;
        }
        // we should never end up here
        assert(false);
    }

    private void setAndPropagateAnyDistanceObeyingPassthrough(final VirtualSlidingPieceOnSquare callingNeighbour,
                                                              final int passingThroughInDirIndex ) {
        ConditionalDistance suggestedDistance = callingNeighbour.getSuggestionToPassthroughIndex(passingThroughInDirIndex);
        int fromDirIndex = oppositeDirIndex(passingThroughInDirIndex);
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
        if (passingThroughInDirIndex==FROMNOWHERE) {
            // I carry my own piece, i.e. distance=0. ot another definite set of distance (like after moving away to 1)
            rawMinDistance = suggestedDistance;  //new Distance(0);
            minDistsDirty();
            if (suggestedDistance.dist()==0)
                uniqueShortestWayDirIndex = NONE;
            propagateIncreasingDistanceChangeToAllSlidingNeighbours();
            return;
        }
        int fromDirIndex = oppositeDirIndex(passingThroughInDirIndex);
        if (rawMinDistance.dist()==0) {
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            return;  // there is nothing closer than myself...
        }
        assert(passingThroughInDirIndex!=FROMNOWHERE);
        //  the distance changed from a certain, specified direction  "passingThroughInDirIndex"
        int neededPropagationDir; // = updateRawMinDistanceWithIncreasingSuggestionFromDirIndex(suggestedDistance,oppositeDirIndex(passingThroughInDirIndex));
////// was in extra method:
        if ( suggDistFromSlidingNeighbours[fromDirIndex].equals(suggestedDistance )
            //&& suggDistFromSlidingNeighbours[fromDirIndex].conditionsEqual(suggestedDistance )
            ) {
            // the same suggestion value that we already had from this direction
            return;
        }
        latestUpdateFromSlidingNeighbour[fromDirIndex] = getOngoingUpdateClock();

        if ( uniqueShortestWayDirIndex==MULTIPLE
            && rawMinDistance.cdEquals(suggDistFromSlidingNeighbours[fromDirIndex])
        ) {
            // an update coming in from one of several shortest paths known so. update and recalc
            if (suggestedDistance.cdIsSmallerThan(rawMinDistance)) {
                // new suggestion is even smaller than the currently smallest.
                // This is a conflict with this being an "Increasing" method !?
                // It turns out, that this can happen due to not-yet-updated neighbours injecting their shorter (old) way via slinding
                // for now, we stop it here and hope for an update once this old neighbour is updated.
                //rawMinDistance.updateFrom(suggestedDistance);
                minDistsDirty();
                //uniqueShortestWayDirIndex = passingThroughInDirIndex;
                //neededPropagationDir = ALLDIRS;
                neededPropagationDir = NONE;
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
//////
        switch(neededPropagationDir) {
            case NONE:
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,".i}");
                return;
            case ALLDIRS:
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"*");
                // and, if new distance is different from what it was, also tell all other neighbours
                propagateIncreasingDistanceChangeToAllSlidingNeighbours();
                break;
            default:
                // here, only the passthroughSuggestion has changed, so inform opposite neighbour
                // only in one special case, it is unsure if the information needs to be reflected
                // back to the neighbour, because he might be wrong... (this is signales by -dir)
                if (neededPropagationDir < 0) {
                    neededPropagationDir = -neededPropagationDir-1;   // the calculation is like the 2s compliment to avoid -0=+0
                    propagateIncreasingDistanceChangeToSlidingNeighbourInDir(oppositeDirIndex(neededPropagationDir));
                }
                propagateIncreasingDistanceChangeToSlidingNeighbourInDir(neededPropagationDir);
        }
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"i}");
    }

    protected void propagateDistanceChangeToOutdatedNeighbours(final int minDist, final int maxDist) {
        propagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(-1, getOngoingUpdateClock());
    }

    /** inform one neighbour:
     * propagate my distance to the neighbour in direction "passingThroughInDirIndex" (if there is one)
     * @param passingThroughInDirIndex neighbour to be informed is in that direction
     * @param updateAgeLimit but only if this neighbour has a lower update"age" than "updateAgeLimit"
     */
    private void propagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(final int passingThroughInDirIndex, final long updateAgeLimit) {
        // inform one (opposite) neighbour
        // TODO!: check: update limit is propably only working correctly for the slidingNeigbourUpdateTimes, since update-time is only remembered if rawMinDistance really changes
        VirtualSlidingPieceOnSquare n = slidingNeighbours[passingThroughInDirIndex];
        if (n != null
                && n.latestUpdateFromSlidingNeighbour[oppositeDirIndex(passingThroughInDirIndex)]
                    <=updateAgeLimit) {
            ConditionalDistance suggestion = getSuggestionToPassthroughIndex(passingThroughInDirIndex);
            myPiece().quePropagation(
                        suggestion.dist(),
                        ()-> n.setAndPropagateAnyDistanceObeyingPassthrough( // setAndPropagateDecreasingDistanceObeyingPassthrough(
                                this, //suggestion,
                                passingThroughInDirIndex));
            //n.setAndPropagateDistanceObeyingPassthrough(suggestion,passingThroughInDirIndex);
        }
    }

    private void propagateIncreasingDistanceChangeToSlidingNeighbourInDir(final int passingThroughInDirIndex) {
        // inform one (opposite) neighbour
        VirtualSlidingPieceOnSquare n = slidingNeighbours[passingThroughInDirIndex];
        if (n != null) {
            ConditionalDistance suggestion = getSuggestionToPassthroughIndex(passingThroughInDirIndex);
            // start with depth first propagation...  (do not want to mix up with breadth propagation of decreasing values for now...)
            //TODO: think about and handle no-go squares also for increasing distances
            // originally, without Queing it was:
            //  n.setAndPropagateIncreasingDistanceObeyingPassthrough(suggestion, passingThroughInDirIndex);
            // but old (not-yet-updated) sliding-suggestions could interfer with nogo-1-hop-suggestions and trigger an endless update-loop (self-feeding a sliding row cont. up and down)
            // it is not nice, to prevent this by breadth-search-approach only, but currently, there ist no better idea...
            myPiece().quePropagation(
                        suggestion.dist(),
                        ()-> n.setAndPropagateIncreasingDistanceObeyingPassthrough( // setAndPropagateDecreasingDistanceObeyingPassthrough(
                                suggestion,
                                passingThroughInDirIndex));
        }
    }

    private void propagateDistanceChangeToSlidingNeighbourInDir(final int passingThroughInDirIndex) {
        propagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(passingThroughInDirIndex,Integer.MAX_VALUE);
    }


    private void propagateDistanceChangeToSlidingNeighboursExceptDir(final int excludeDirIndex) {
        propagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(excludeDirIndex, Long.MAX_VALUE);
    }

    private void propagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(final int excludeDirIndex, final long updateAgeLimit) {
        // for the slidingNeighbours, we need to check from which direction the figure is coming from
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (dirIndex!=excludeDirIndex)
                propagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(
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

    protected void propagateIncreasingDistanceChangeToAllSlidingNeighbours() {
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            propagateIncreasingDistanceChangeToSlidingNeighbourInDir(dirIndex);
    }



    /**
     *  calculate the suggestion to a neighbour, possibly using a passthrough
     * @param passthroughDirIndex direction for which the passthrough is checked
     * @return the distance to suggest to the next neighbour in that direction
     */
    private ConditionalDistance getSuggestionToPassthroughIndex(int passthroughDirIndex) {
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
        if (myChessBoard.hasPieceOfColorAt( myPiece().color(), myPos )) {
            // own piece in the way
            int penalty = movingMySquaresPieceAwayDistancePenalty();
            if (penalty!=INFINITE_DISTANCE) {
                ConditionalDistance d = new ConditionalDistance(
                        suggDistFromSlidingNeighbours[fromDirIndex],
                        penalty,
                        myPos, ANY, myPiece().color());
                suggestion.reduceIfCdIsSmaller(d);
                // TODO: Scheint nicht falsch, aber könnte effizienter implementiert werden, wenn die Annahme stimmt,
                //  dass das d wg. der penalty eh niemals kleiner sein kann als die suggestion (die auch die selbe penalty
                //  enthält und ansonsten das minimum aus den verschiedenen Richtungen ist.
            }
        }
        else {
            boolean opponentColor = myOpponentsColor();
            if (myChessBoard.hasPieceOfColorAt( opponentColor, myPos )) {
                // an opponent Piece is in the way here - this needs penalty in some cases:
                // do not count the first opponent moving away as distance, but later do count (this is not very precise...)
                int inc = suggDistFromSlidingNeighbours[fromDirIndex].isUnconditional() ? 0 : 1;
                // and it additionally needs the condition that the piece moves away to allow passthrough
                ConditionalDistance d = new ConditionalDistance(
                        suggDistFromSlidingNeighbours[fromDirIndex],
                         inc,
                        myPos, ANY, opponentColor  //TODO: topos-condition must not be ANY, but "anywhere except in that direction"
                );
                suggestion.reduceIfCdIsSmaller( d );
            } else {
                // passthrough possible
                suggestion.reduceIfCdIsSmaller(suggDistFromSlidingNeighbours[fromDirIndex]);
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
              //  || ( suggestedDistance.distEquals(rawMinDistance)
              //      && (suggestedDistance.hasFewerConditionsThan(rawMinDistance)) )
        ) {     // (1a)(7)(10)(4)
            // the new distance is smaller than the minimum, so we already found the new minimum
            suggDistFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            //if (rawMinDistance.reduceIfSmaller(suggestedDistance)) {

            updateRawMinDistanceFrom(suggestedDistance);

            //}
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
            return oppositeDirIndex(fromDirIndex);  //because this value is new from this direction, we better pass it on
        }
        // from here on, the new suggestion is in any case not the minimum, but might be smaller than the previous from this direction still
        if ( suggestedDistance.cdIsSmallerThan(suggDistFromSlidingNeighbours[fromDirIndex])
              //  || ( suggestedDistance.distEquals(suggestedDistanceFromSlidingNeighbours[fromDirIndex])
              //  && (suggestedDistance.hasFewerConditionsThan(suggestedDistanceFromSlidingNeighbours[fromDirIndex])) )
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
        ConditionalDistance minimum = new ConditionalDistance();
        // finds new minimum and is at the same like calcUniqueShortestWayDir() but does not rely on the not yet correct rawMinDist.
        uniqueShortestWayDirIndex = NONE;
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (slidingNeighbours[dirIndex]!=null) {
                if (minimum.reduceIfCdIsSmaller(suggDistFromSlidingNeighbours[dirIndex]))
                    uniqueShortestWayDirIndex = dirIndex;  // we found (one of) the shortest in-paths
                else if (uniqueShortestWayDirIndex>=0
                        && minimum.cdEquals(suggDistFromSlidingNeighbours[dirIndex]) )
                        //&& suggDistFromSlidingNeighbours[dirIndex].hasFewerOrEqualConditionsThan(minimum)) // must be same nr. of conditions
                    uniqueShortestWayDirIndex = MULTIPLE;   // again, but as this is already the second, we have multiple shortest in-paths
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
        ConditionalDistance minimum = new ConditionalDistance();
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++) {
            if (slidingNeighbours[dirIndex]!=null) {
                // get fresh update from all neighbours and recalc minimum.
                suggDistFromSlidingNeighbours[dirIndex] = slidingNeighbours[dirIndex].getSuggestionToPassthroughIndex(oppositeDirIndex(dirIndex));
                minimum.reduceIfCdIsSmaller(suggDistFromSlidingNeighbours[dirIndex]);
            }
        }
        // TODO-OPTI:  using calcUniqueShortestWayDir() is inefficient here, as we could calculate it also already in the loop above
        if (rawMinDistance.cdEquals(minimum)) {
            uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
            return 0;
        }
        if (minimum==null) {
            //TODO: thies piece has no(!) neighbour... this is e.g. (only case?) a pawn that has reached the final rank.
            assert(false); // should not happen for sliding figures
            return 0;
        }
        if (reduceRawMinDistanceIfCdIsSmaller(minimum)) {
            uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
            return -1;
        }
        updateRawMinDistanceFrom(minimum);
        uniqueShortestWayDirIndex = calcUniqueShortestWayDirIndex();
        return +1;
    }

    @Override
    public void pieceHasArrivedHere(int pid) {
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION,"");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," ["+myPceID+":" );
        setLatestChangeToNow();
        // inform neighbours that something has arrived here
        myChessBoard.getPiece(myPceID).startNextUpdate();
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

        myChessBoard.getPiece(myPceID).endUpdate();
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"] ");
    }

    @Override
    protected List<VirtualPieceOnSquare> getNeighbours() {
        return Collections.unmodifiableList( Arrays.asList(slidingNeighbours) );
    }

    @Override
    protected void propagateResetIfUSWToAllNeighbours() {
        assert(false);  // should not b called, is replaced by IncreasingPropagation
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


    @Override
    // fully set up initial distance from this vPces position
    public void myOwnPieceHasMovedHereFrom(int frompos) {
        // one extra piece or a new hop (around the corner or for non-sliding neighbours
        // treated just like sliding neighbour, but with no matching "from"-direction
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, "[" + pieceColorAndName(myChessBoard.getPiece(myPceID).getPieceType())
                + "(" + myPceID + "): propagate own distance: ");

        myChessBoard.getPiece(myPceID).startNextUpdate();
        rawMinDistance = new ConditionalDistance(0);  //needed to stop the reset-bombs below at least here
        minDistsDirty();

        if (frompos!=NOWHERE) {
            // correct neighbourSuggestions on backwards way in both directions
            int backDir = calcDirFromTo(myPos,frompos);
            int fromDirIndex = oppositeDirIndex(convertMainDir2DirIndex(backDir));
            VirtualSlidingPieceOnSquare vPce = this;
            int correctionPos=myPos;
            do {
                correctionPos+=backDir;
                vPce.suggDistFromSlidingNeighbours[convertMainDir2DirIndex(backDir)] = new ConditionalDistance(2);
                vPce = (VirtualSlidingPieceOnSquare)
                        (myChessBoard.getBoardSquares()[correctionPos].getvPiece(myPceID));
                vPce.suggDistFromSlidingNeighbours[fromDirIndex] = new ConditionalDistance(1);
                vPce.uniqueShortestWayDirIndex = fromDirIndex;
            } while (correctionPos!=frompos);
            // do the propagation from the new position first
            setAndPropagateDistance(new ConditionalDistance(0));  // , 0, Integer.MAX_VALUE );
            // and then correct increasing distances from the frompos
            vPce.setAndPropagateIncreasingDistanceObeyingPassthrough(
                            new ConditionalDistance(1),
                            FROMNOWHERE);
        }
        else
            setAndPropagateDistance(new ConditionalDistance(0));  // , 0, Integer.MAX_VALUE );

        myChessBoard.getPiece(myPceID).endUpdate();
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
                    ()-> this.recalcRawMinDistanceFromNeighboursAndPropagate());
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
                if (onepathcheck==false)
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

    /** checks if this vPce could reach a position in dist==1 after conditions are fulfilled. Useful
     * to select 2nd row clash candidates.  maPiece, targetpos and conditions must be on one sliding path.
     * Used for pre-selection, so it does not need to work precisely, but quickly. could tell true in false cases...
     * but never false for possibly 2nd row candiates.
     *
     * @return boolean true if fulfilled
     */
    public boolean fulfilledConditionsCouldMakeDistIs1() {
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
