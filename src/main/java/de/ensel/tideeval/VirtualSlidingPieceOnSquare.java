/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.Distance.ANY;
import static de.ensel.tideeval.Distance.INFINITE_DISTANCE;

public class VirtualSlidingPieceOnSquare extends VirtualPieceOnSquare {

    // array of slinging neighbours, one per direction-index (see ChessBasics)
    private final VirtualSlidingPieceOnSquare[] slidingNeighbours;
    // .. and the corresponding distances suggested by these neighbours.
    private final Distance[] suggestedDistanceFromSlidingNeighbours = new Distance[MAXMAINDIRS];

    private int uniqueShortestWayDirIndex;   // if only one shortest way to this square exists, this is the direction back to the piece
    private int uniqueShortestConditionalWayDirIndex;    // dito, but only if under a condition there is a even shorter way
    // it also shows where the minimal suggestedDist was. it is -1 if there is more than one way here
    private final long[] latestUpdateFromSlidingNeighbour = new long[MAXMAINDIRS];

    public VirtualSlidingPieceOnSquare(ChessBoard myChessBoard, int newPceID, int pceType, int myPos) {
        super(myChessBoard, newPceID, pceType, myPos);
        slidingNeighbours = new VirtualSlidingPieceOnSquare[MAXMAINDIRS];
        uniqueShortestWayDirIndex = NONE;
        uniqueShortestConditionalWayDirIndex = NONE;
        resetSlidingDistances();
    }

    protected void resetSlidingDistances() {
        for (int i = 0; i < MAXMAINDIRS; i++) {
            if (suggestedDistanceFromSlidingNeighbours[i]==null)
                suggestedDistanceFromSlidingNeighbours[i] = new Distance();
            else
                suggestedDistanceFromSlidingNeighbours[i].reset();
            latestUpdateFromSlidingNeighbour[i]=0;
        }
    }

    private void resetSlidingConditionalDistances() {
        for (int i = 0; i < MAXMAINDIRS; i++) {
            suggestedDistanceFromSlidingNeighbours[i].resetConditionalDistance();
            latestUpdateFromSlidingNeighbour[i]=0;
        }
    }

    @Override
    public void addSlidingNeighbour(VirtualPieceOnSquare neighbourPce, int direction) {
        slidingNeighbours[convertMainDir2DirIndex(direction)] = (VirtualSlidingPieceOnSquare) neighbourPce;
    }

    // set up initial distance from this vPces position - restricted to distance depth change
    @Override
    public void setAndPropagateDistance(final Distance distance) {   // }, int minDist, int maxDist ) {
        setAndPropagateDistanceObeyingPassthrough(distance);  //minDist, maxDist );
    }

    @Override
    protected void propagateDistanceChangeToAllNeighbours() {
        propagateDistanceChangeToSlidingNeighboursExceptDir(-1);
    }


    // set up initial distance from this vPces position - restricted to distance depth change - the subclass internal version...
    private void setAndPropagateDistanceObeyingPassthrough(final Distance distance) {
        setAndPropagateDistanceObeyingPassthrough(distance, FROMNOWHERE);
    }

    private void setAndPropagateDistanceObeyingPassthrough(final Distance suggestedDistance,
                                                           final int passingThroughInDirIndex ) {
/*                                                           int minDist,    // assume that distances below minDist are already correct, but still run "over" them to get propagation going
                                                           int maxDist     // stop at dist >= maxDist (leaves it open for later call with minDist=this value)
                                                           // final Distance trustBelow   // lower Limit if Update of distance is "locally increasing" i.e. only partial for squares >= that limit
    ) { */
        assert(suggestedDistance.getShortestDistanceEvenUnderCondition()>=0);
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," {"+squareName(myPos)+"_"+ suggestedDistance);
        if (suggestedDistance.dist()==0) {
            // I carry my own piece, i.e. distance=0.  test is needed, otherwise I'd act as if I'd find my own piece here in my way...
            rawMinDistance = suggestedDistance;  //new Distance(0);
            minDistance = rawMinDistance;
            assert(passingThroughInDirIndex == FROMNOWHERE);
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
                    <updateAgeLimit) {
            Distance suggestion = getSuggestionToPassthroughIndex(passingThroughInDirIndex);
            //if (suggestion.getDistanceUnderCondition()<=maxDist)
            /* ** experimenting with breadth search propagation will follow later: ** */
            if (FEATURE_TRY_BREADTHSEARCH_ALSO_FOR_1HOP_AND_SLIDING
                    || suggestion.getShortestDistanceEvenUnderCondition()>myChessBoard.currentDistanceCalcLimit())
                myPiece().quePropagation(
                        suggestion.getShortestDistanceEvenUnderCondition(),
                        ()-> n.setAndPropagateDistanceObeyingPassthrough(
                            suggestion,
                            passingThroughInDirIndex));
            else
                n.setAndPropagateDistanceObeyingPassthrough(
                        suggestion,
                        passingThroughInDirIndex);
        }
    }

    private void propagateDistanceChangeToSlidingNeighbourInDir(final int passingThroughInDirIndex) {
        propagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(passingThroughInDirIndex,Integer.MAX_VALUE);
    }


    private void propagateDistanceChangeToSlidingNeighboursExceptDir(final int excludeDirIndex) {
        propagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(excludeDirIndex, Long.MAX_VALUE);
    }

    private void propagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(final int excludeDirIndex, final long updateAgeLimit) {
        // old: break if max. propagation depth is reached (or exceeded)
        //if (rawMinDistance.dist()>maxDist)
        //    return;
        // for the slidingNeighbours, we need to check from which direction the figure is coming from
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (dirIndex!=excludeDirIndex)
                propagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(
                        dirIndex,
                        updateAgeLimit
                );
    }

    /**
     *  calculate the suggestion to a neighbour, possibly using a passthrough
     * @param passthroughDirIndex direction for which the passthrough is checked
     * @return the distance to suggest to the next neighbour in that direction
     */
    private Distance getSuggestionToPassthroughIndex(int passthroughDirIndex) {
        int fromDirIndex = oppositeDirIndex(passthroughDirIndex);

        if ( // I am at my own square
             rawMinDistance.dist()==0
             // or if even under a condition there is no way, yet
             ||   suggestedDistanceFromSlidingNeighbours[fromDirIndex].getShortestDistanceEvenUnderCondition() == INFINITE_DISTANCE
        ) {
            return minDistanceSuggestionTo1HopNeighbour();
        }
        // now this is either the same (take a corner after the shortest distance)
        Distance suggestion = minDistanceSuggestionTo1HopNeighbour();
        // or stay on the passthrough towards opposite neighbour:
        // but this might have a penalty if own figure is in the way:
        int penalty = movingMySquaresPieceAwayDistancePenalty();
        if (penalty>0)  {
            // own piece in the way
            Distance d = new Distance(INFINITE_DISTANCE,
                    myPos, ANY,
                    increaseIfPossible(suggestedDistanceFromSlidingNeighbours[fromDirIndex].dist(), penalty) );
            // TODO:  this should not overwrite an existing condition! Chains of conditions are necessery to introduce...
            suggestion.reduceIfSmaller(d);
            // TODO: Scheint nicht falsch, aber könnte effizienter implementiert werden, wenn die Annahme stimmt,
            //  dass das d wg. der penalty eh niemals kleiner sein kann als die suggestion (die auch die selbe penalty
            //  enthält und ansonsten das minimum aus den verschiedenen Richtungen ist.
        }
        else {
            if (myChessBoard.hasPieceOfColorAt( opponentColor(myPiece().color()), myPos )) {
                // an opponent Piece is in the way here - this is no penalty for 1hop-calculation, but
                // it additionally allows passthrough under the condition that the piece moves away
                suggestion.reduceIfSmaller(new Distance(
                        suggestion.dist(),
                        myPos, ANY,  //TODO: topos-condition must not be ANY, but "anywhere except in that direction"
                        suggestedDistanceFromSlidingNeighbours[fromDirIndex].dist() ) );
            } else {
                // passthrough possible
                suggestion.reduceIfSmaller(suggestedDistanceFromSlidingNeighbours[fromDirIndex]);
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
    private int updateRawMinDistancesNonIncreasingly(final Distance suggestedDistance,
                                                     final int fromDirIndex) {
        latestUpdateFromSlidingNeighbour[fromDirIndex] = getOngoingUpdateClock();
        if (rawMinDistance.dist()==0)
            return NONE;  // there is nothing closer than myself...
        if (suggestedDistance.isSmaller(rawMinDistance)) {     // (1a)(7)(10)(4)
            // the new distance is smaller than the minimum, so we already found the new minimum
            if (suggestedDistance.hasCondition() && suggestedDistance.hasSmallerConditionalDistance(rawMinDistance)) {
                uniqueShortestConditionalWayDirIndex = fromDirIndex;
                suggestedDistanceFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            }
            else if (uniqueShortestConditionalWayDirIndex!=fromDirIndex
                    && suggestedDistance.hasCondition()
                    && suggestedDistance.hasEqualConditionalDistance(rawMinDistance)) {
                uniqueShortestConditionalWayDirIndex = MULTIPLE;
                suggestedDistanceFromSlidingNeighbours[fromDirIndex].reduceIfSmaller(suggestedDistance);
            } else
                suggestedDistanceFromSlidingNeighbours[fromDirIndex].reduceIfSmaller(suggestedDistance);
            if (rawMinDistance.reduceIfSmaller(suggestedDistance)) {
                setLatestChangeToNow();
                minDistance = null;
            }
            uniqueShortestWayDirIndex = fromDirIndex;
            // TODO!!! in many cases of this method:  handle how to update uniqueShortestConditionalWayDir = calcUniqueShortestConditionalWayDir();
            return ALLDIRS;
        }
        if (suggestedDistance.hasCondition() && suggestedDistance.hasSmallerConditionalDistance(rawMinDistance)) {
            // the new distance is not smaller, but the stored condition has a shorter way
            suggestedDistanceFromSlidingNeighbours[fromDirIndex].reduceIfSmaller(suggestedDistance);
            if (rawMinDistance.reduceIfSmaller(suggestedDistance)) {
                setLatestChangeToNow();
                minDistance = null;
            }
            uniqueShortestConditionalWayDirIndex = fromDirIndex;
            return ALLDIRS;
        }
        if (suggestedDistance.equals(rawMinDistance)) {           // (8)(10)(5)
            // the same suggestion value that we already have als minimum
            if (uniqueShortestWayDirIndex==fromDirIndex)                  // (1b)
                return NONE;     // even the direction matches th known shortestWay, so nothing to do
            suggestedDistanceFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            uniqueShortestWayDirIndex = MULTIPLE;
            if (suggestedDistance.hasCondition() && suggestedDistance.hasSmallerConditionalDistance(rawMinDistance) )
                uniqueShortestConditionalWayDirIndex = fromDirIndex;
            else if (uniqueShortestConditionalWayDirIndex!=fromDirIndex
                    && suggestedDistance.hasCondition()
                    && suggestedDistance.hasEqualConditionalDistance(rawMinDistance))
                uniqueShortestConditionalWayDirIndex = MULTIPLE;
            return oppositeDirIndex(fromDirIndex);  //because this value is new from this direction, we better pass it on
        }
        // from here on, the new suggestion is in any case not the minimum
        if ( suggestedDistanceFromSlidingNeighbours[fromDirIndex].   // (extra case?)
                equals(suggestedDistance) ) {
            // the same suggestion value that we already had from this direction
            return NONE;
        }
        /*if (rawMinDistance.getShortestDistanceEvenUnderCondition()<=minDist) {
            // if we reached the "safe" squares (and the suggestion was not smaller) we cen stop, it cannot
            // be a vaild "reset-scenario" with overriding longer distances.
            // and an propagateToAll-call was or will be started from here anyway.
            return NONE;
        }*/
        if ( suggestedDistance.isSmaller(suggestedDistanceFromSlidingNeighbours[fromDirIndex]) ) {
            // a smaller suggestion value than we already had from this direction
            suggestedDistanceFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            if ( getSuggestionToPassthroughIndex(fromDirIndex).isSmallerOrEqual(suggestedDistance)
                    || getSuggestionToPassthroughIndex(fromDirIndex).hasSmallerOrEqualConditionalDistance(suggestedDistance) )
                return -oppositeDirIndex(fromDirIndex)-1;
            return oppositeDirIndex(fromDirIndex);
        }
        if ( uniqueShortestWayDirIndex==fromDirIndex) {   // (2)(3)(1c)
            // a longer update coming in from the known single shortest path
            // this must be an old update-call
            return NONE;

            /* // we have to check was has become of the former shortest in-path and recalc the unique shortest path
            suggestedDistanceFromSlidingNeighbours[fromDirIndex].updateFrom(suggestedDistance);
            int minUpdated = recalcRawMinDistance();
            uniqueShortestWayDirIndex = calcUniqueShortestWayDir();
            uniqueShortestConditionalWayDirIndex = calcUniqueShortestConditionalWayDir();
            if (minUpdated!=0)
                return oppositeDirIndex(fromDirIndex);
            return NONE;*/
        }
        if ( uniqueShortestWayDirIndex==MULTIPLE
                && suggestedDistanceFromSlidingNeighbours[fromDirIndex].equals(rawMinDistance) ) {       // (9)
            // an longer update coming in from one of the shortest pathes known
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
     * @return 0: value did not change;  +1: value increased;  -1: value decreased;
     */
    private int recalcRawMinDistance() {
        if (rawMinDistance.dist()==0)
            return 0;  // there is nothing closer than myself...
        //rawMinDistance = (IntStream.of(suggestedDistanceFromNeighbours)).min().getAsInt();
        Distance minimum = new Distance();
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++) {
            if (slidingNeighbours[dirIndex]!=null)
                minimum.reduceIfSmaller(suggestedDistanceFromSlidingNeighbours[dirIndex]);
        }
        if (rawMinDistance.equals(minimum))
            return 0;
        if (minimum==null) {
            //TODO: thies piece has no(!) neighbour... this is e.g. (only case?) a pawn that has reached the final rank.
            assert(false); // should not happen for sliding figures
            return 0;
        }
        if (rawMinDistance.reduceIfSmaller(minimum))
            return -1;
        rawMinDistance.updateFrom(minimum);
        return +1;
    }


    protected void resetConditionalDistances() {
        if (rawMinDistance ==null)
            rawMinDistance = new Distance();
        else
            rawMinDistance.resetConditionalDistance();
        minDistance = null;
    }

    @Override
    protected void propagateResetIfUSWToAllNeighbours() {
        propagateResetIfUSWToAllNeighboursExceptDirIndex(NONE);
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
        if ( fromDirIndex==uniqueShortestWayDirIndex) {  // "reset-bomb" only if the neighbour was my only predecessor in the path
            // forget the dist-infos that I got from my neighbours
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"*");
            resetDistances();
            resetSlidingDistances();
            // TODO: test if a reset of the single fromDirIndex (instead of all dirs) was sufficient (dito for conditional distances)
            // and tell them to do the same, if i am their predecessor in the path
            uniqueShortestWayDirIndex = NONE;
            uniqueShortestConditionalWayDirIndex = NONE;
            propagateResetIfUSWToAllNeighboursExceptDirIndex(fromDirIndex);
        }
        else if ( uniqueShortestWayDirIndex==MULTIPLE
                && suggestedDistanceFromSlidingNeighbours[fromDirIndex].equals(rawMinDistance) ) {
            // only propagate reset to opposite neighbour because we were at least among several shortest inpaths
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"-");
            suggestedDistanceFromSlidingNeighbours[fromDirIndex] = new Distance();
            uniqueShortestWayDirIndex = calcUniqueShortestWayDir();
            uniqueShortestConditionalWayDirIndex = calcUniqueShortestConditionalWayDir();
            propagateResetIfUSWTo1SlidingNeighbour(oppositeDirIndex(fromDirIndex));
        }
        // additionaly check the similar case, if they apply for the conditional distances
        else if ( fromDirIndex==uniqueShortestConditionalWayDirIndex) {  // "reset-bomb" only if the neighbour was my only predecessor in the path
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"x");
            resetConditionalDistances();        // TODO!!!: this must be transfered to the SIngleHopPieces as well!
            resetSlidingConditionalDistances();
            uniqueShortestConditionalWayDirIndex = NONE;
            // and tell them to do the same, if i am their predecessor in the path
            propagateResetIfUSWToAllNeighboursExceptDirIndex(fromDirIndex);
        }
        else if ( uniqueShortestConditionalWayDirIndex==MULTIPLE
                && suggestedDistanceFromSlidingNeighbours[fromDirIndex].getShortestDistanceEvenUnderCondition() == rawMinDistance.getShortestDistanceEvenUnderCondition() ) {
            // only propagate reset to opposite neighbour because we were at least among several shortest conditional inpaths
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"~");
            suggestedDistanceFromSlidingNeighbours[fromDirIndex].resetConditionalDistance();
            uniqueShortestWayDirIndex = calcUniqueShortestWayDir();
            uniqueShortestConditionalWayDirIndex = calcUniqueShortestConditionalWayDir();
            propagateResetIfUSWTo1SlidingNeighbour(oppositeDirIndex(fromDirIndex));
        }
        else {
            // it was just a message from a neighbour that is further away than other neighbours
            // at least forget the old input from fromDirIndex
            // (same implementation part as in setAndPropagateDistanceObeyingPassthrough();
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,".");
            suggestedDistanceFromSlidingNeighbours[fromDirIndex].reset();
        }
    }


    /**
     * searches in all suggestions from neighbours for the correct fromDirection where the shortest in-path is coming from.
     * @return MULTIPLE, if more than one shortest in-path was found.  returns the fromDir otherwise.
     */
    private int calcUniqueShortestWayDir() {
        int newUSWD = NONE;
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (slidingNeighbours[dirIndex]!=null
                    && suggestedDistanceFromSlidingNeighbours[dirIndex].equals(rawMinDistance)) {
                // we found (one of) the shortest in-paths
                if (newUSWD>=0)
                    return MULTIPLE;   // but this is already the second, so we have multiple shortest in-paths
                newUSWD = dirIndex;
            }
        return newUSWD;
    }

    @Override
    public String getShortestInPathDirDescription() {
        return TEXTBASICS_FROM + " " + dirIndexDescription(uniqueShortestWayDirIndex)
                + " (" + TEXTBASICS_FROM + " " + dirIndexDescription(uniqueShortestConditionalWayDirIndex)
                + ")";
    }

    private int calcUniqueShortestConditionalWayDir() {
        int newUSWD = NONE;
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (slidingNeighbours[dirIndex]!=null
                    && suggestedDistanceFromSlidingNeighbours[dirIndex].hasEqualConditionalDistance(rawMinDistance) ) {
                // we found (one of) the shortest conditional in-paths
                if (newUSWD>=0)
                    return MULTIPLE;   // but this is already the second, so we have multiple shortest conditional in-paths
                newUSWD = dirIndex;
            }
        return newUSWD;
    }

}
