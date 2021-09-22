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
    private final Distance[] suggestedDistanceFromSlidingNeighbours;
    private int uniqueShortestWayDir;   // if only one shortest way to this square exists, this is the direction back to the piece
    private int uniqueShortestConditionalWayDir;    // dito, but only if under a condition there is a even shorter way
    // it also shows where the minimal suggestedDist was. it is -1 if there is more than one way here
    private int[] latestUpdateFromSlidingNeighbour;

    public VirtualSlidingPieceOnSquare(ChessBoard myChessBoard, int newPceID, int pceType, int myPos) {
        super(myChessBoard, newPceID, pceType, myPos);
        slidingNeighbours = new VirtualSlidingPieceOnSquare[MAXMAINDIRS];
        suggestedDistanceFromSlidingNeighbours = new Distance[MAXMAINDIRS];
        latestUpdateFromSlidingNeighbour = new int[MAXMAINDIRS];
        uniqueShortestWayDir = NONE;
        uniqueShortestConditionalWayDir = NONE;
        resetSlidingDistances();
    }

    private void resetSlidingDistances() {
        for (int i = 0; i < MAXMAINDIRS; i++) {
            suggestedDistanceFromSlidingNeighbours[i] = new Distance();
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
        setAndPropagateDistanceObeyingPassthrough(distance, -1,  Integer.MAX_VALUE );  //minDist, maxDist );
/*      // TODO!! - splitting does not works - ends up in endles loop or misses necessary updates
        myChessBoard.getPiece(myPceID).endUpdate();
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," /2ff: ");
        myChessBoard.getPiece(myPceID).startNextUpdate();
        setAndPropagateDistanceObeyingPassthrough(distance, 2,  Integer.MAX_VALUE );  //minDist, maxDist );  */
    }


    @Override
    protected void propagateDistanceChangeToAllNeighbours() {

        propagateDistanceChangeToSlidingNeighboursExceptDir(-1, -1, Integer.MAX_VALUE);
        /* a spit in two phases made the local updates was even worse in the test szenario (BasicPiecePlacementTest)
           so we do not split here.
        myChessBoard.getPiece(myPceID).endUpdate();
        myChessBoard.getPiece(myPceID).startNextUpdate();
        propagateDistanceChangeToSlidingNeighboursExceptDir(-1, 2, Integer.MAX_VALUE);
        */
    }


    // set up initial distance from this vPces position - restricted to distance depth change - the subclass internal version...
    private void setAndPropagateDistanceObeyingPassthrough(final Distance distance, int minDist, int maxDist ) {
        setAndPropagateDistanceObeyingPassthrough(distance, FROMNOWHERE, minDist, maxDist );
    }

    //private enum UpdateMode { SHORTENING, INCREASEING }

    private void setAndPropagateDistanceObeyingPassthrough(final Distance suggestedDistance,
                                                           final int passingThroughInDirIndex,
                                                           int minDist,    // assume that distances below minDist are already correct, but still run "over" them to get propagation going
                                                           int maxDist     // stop at dist >= maxDist (leaves it open for later call with minDist=this value)
                                                           //                                                             final Distance trustBelow   // lower Limit if Update of distance is "locally increasing" i.e. only partial for squares >= that limit
    ) {
        assert(suggestedDistance.getShortestDistanceEvenUnderCondition()>=0);
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," {"+squareName(myPos)+"_"+ suggestedDistance);
        if (suggestedDistance.dist()==0) {
            // I carry my own piece, i.e. distance=0.  test is needed, otherwise I'd act as if I'd find my own piece here in my way...
            rawMinDistance = suggestedDistance;  //new Distance(0);
            minDistance = rawMinDistance;
            assert(passingThroughInDirIndex == FROMNOWHERE);
            propagateDistanceChangeToAllNeighbours(minDist, maxDist);
            return;
        }
        //else
        if (suggestedDistance.getShortestDistanceEvenUnderCondition() > maxDist) {
            // over max, we stop here
            return;
        }
        //else...
        if (passingThroughInDirIndex!=FROMNOWHERE ) {
            //  the distance changed from a certain, specified direction  "passingThroughInDirIndex"
            int neededPropagationDir = updateRawMinDistances(suggestedDistance,
                    oppositeDirIndex(passingThroughInDirIndex), minDist );
            //if ( updateSuggestedDistanceInPassthroughDirIndex(suggestedDistance, passingThroughInDirIndex) ) {
            // update rawMin first, so the propagate methods will calculate the right propagation value
            if (rawMinDistance.getShortestDistanceEvenUnderCondition()<minDist) {
                // I am still in a range that is already set
                // I change nothing, but get the propagation rolling
                propagateDistanceChangeToOutdatedNeighbours(minDist, maxDist);
                return;
            }
            if (rawMinDistance.getShortestDistanceEvenUnderCondition()==minDist) {   // TODO!! - splitting does not works - ends up in endles loop or misses necessary updates
                // I am at the border of what was already set
                neededPropagationDir = ALLDIRS;
            }

            switch(neededPropagationDir) {
                case NONE:
                    return;
                case ALLDIRS:
                    debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"|");
                    // and, if new distance is different from what it was, also tell all other neighbours
                    propagateDistanceChangeToSlidingNeighboursExceptDir(-1, minDist, maxDist);
                    break;
                default:
                    // here, only the passthroughSuggestion has changed, so inform opposite neighbour
                    // only in one special case, it is unsure if the information needs to be reflected
                    // back to the neighbour, because he might be wrong... (this is signales by -dir)
                    if (neededPropagationDir < 0) {
                        neededPropagationDir = -neededPropagationDir-1;   // the calculation is like the 2s compliment to avoid -0=+0
                        propagateDistanceChangeToSlidingNeighbourInDir(oppositeDirIndex(neededPropagationDir), minDist, maxDist);
                    }
                    propagateDistanceChangeToSlidingNeighbourInDir(neededPropagationDir, minDist, maxDist);
            }
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"}");
            return;
        }
        // we should never end up here
        assert(false);
    }

    protected void propagateDistanceChangeToAllNeighbours(final int minDist, final int maxDist) {
        propagateDistanceChangeToSlidingNeighboursExceptDir(-1, minDist, maxDist);
    }

    protected void propagateDistanceChangeToOutdatedNeighbours(final int minDist, final int maxDist) {
        propagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(-1, latestUpdate(), minDist, maxDist);
    }

    /** inform one neighbour:
     * propagate my distance to the neighbour in direction "passingThroughInDirIndex" (if there is one),
     * but only if this neighbour has a lower update"age" than "updateAgeLimit".
     * (maxDist is checked, minDist is simply passed through)
     * @param passingThroughInDirIndex
     * @param updateAgeLimit
     * @param minDist
     * @param maxDist
     */
    private void propagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(final int passingThroughInDirIndex, final int updateAgeLimit, final int minDist, final int maxDist) {
        // inform one (opposite) neighbour
        VirtualSlidingPieceOnSquare n = slidingNeighbours[passingThroughInDirIndex];
        if (n != null && n.latestUpdateFromSlidingNeighbour[oppositeDirIndex(passingThroughInDirIndex)]<updateAgeLimit) {
            Distance suggestion = getSuggestionToPassthroughIndex(passingThroughInDirIndex);
            //if (suggestion.getDistanceUnderCondition()<=maxDist)
            /*** experimenting with breadth search propagation will follow later: ***/
            if (FEATURE_TRY_BREADTHSEARCH)
                myPiece().quePropagation(suggestion.getShortestDistanceEvenUnderCondition(), ()->{ n.setAndPropagateDistanceObeyingPassthrough(
                    suggestion,
                    passingThroughInDirIndex,
                    minDist, maxDist); } );
            else
                n.setAndPropagateDistanceObeyingPassthrough(
                        suggestion,
                        passingThroughInDirIndex,
                        minDist, maxDist);
        }
    }

    private void propagateDistanceChangeToSlidingNeighbourInDir(final int passingThroughInDirIndex, final int minDist, final int maxDist) {
        propagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(passingThroughInDirIndex,Integer.MAX_VALUE,minDist, maxDist);
    }


    private void propagateDistanceChangeToSlidingNeighboursExceptDir(final int excludeDirIndex, final int minDist, final int maxDist) {
        propagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(excludeDirIndex, Integer.MAX_VALUE, minDist, maxDist);
    }

    private void propagateDistanceChangeToSlidingNeighboursExceptDirAndFresher(final int excludeDirIndex, final int updateAgeLimit, final int minDist, final int maxDist) {
        // old: break if max. propagation depth is reached (or exceeded)
        //if (rawMinDistance.dist()>maxDist)
        //    return;
        // for the slidingNeighbours, we need to check from which direction the figure is coming from
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (dirIndex!=excludeDirIndex)
                propagateDistanceChangeToSlidingNeighbourInDirExceptFresherThan(dirIndex, updateAgeLimit,minDist, maxDist);
    }

    /**
     *  calculate the suggestion to a neighbour, possibly using a passthrough
     * @param passthroughDirIndex
     * @return
     */
    private Distance getSuggestionToPassthroughIndex(int passthroughDirIndex) {
        int fromDir = oppositeDirIndex(passthroughDirIndex);

        if ( // I am at my own square
             rawMinDistance.dist()==0
             // or if even under a condition there is no way, yet
             ||   suggestedDistanceFromSlidingNeighbours[fromDir].getShortestDistanceEvenUnderCondition() == INFINITE_DISTANCE
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
                    increaseIfPossible(suggestedDistanceFromSlidingNeighbours[fromDir].dist(), penalty) );
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
                        suggestedDistanceFromSlidingNeighbours[fromDir].dist() ) );
            } else {
                // passthrough possible
                suggestion.reduceIfSmaller(suggestedDistanceFromSlidingNeighbours[fromDir]);
            }
        }
        return suggestion;
    }



    /**
     * Updates the overall minimum distance
     * @param updatedDistance
     * @return int what needs to be updated. NONE for nothing, ALLDIRS for all,
     *         below that >0 tells a single direction,  -
     *         1 to -8 is used for two update directions on one axis: so the equiv. to (-n-1) and the opposite direction of that.
     */
    private int updateRawMinDistances(final Distance updatedDistance,
                                      final int fromDir, final int minDist ) {
        setLastUpdateToNow();
        latestUpdateFromSlidingNeighbour[fromDir] = latestUpdate();
        if (rawMinDistance.dist()==0)
            return NONE;  // there is nothing closer than myself...
        if (updatedDistance.isSmaller(rawMinDistance)) {     // (1a)(7)(10)(4)
            // the new distance is smaller than the minimum, so we already found the new minimum
            if (updatedDistance.hasSmallerConditionalDistance(rawMinDistance)) {
                uniqueShortestConditionalWayDir = fromDir;
                suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            }
            else if (uniqueShortestConditionalWayDir!=fromDir
                    && updatedDistance.hasEqualConditionalDistance(rawMinDistance)) {
                uniqueShortestConditionalWayDir = MULTIPLE;
                suggestedDistanceFromSlidingNeighbours[fromDir].reduceIfSmaller(updatedDistance);
            } else
                suggestedDistanceFromSlidingNeighbours[fromDir].reduceIfSmaller(updatedDistance);
            rawMinDistance.reduceIfSmaller(updatedDistance);
            minDistance = null;
            uniqueShortestWayDir = fromDir;
            // TODO!!! in many cases of this method:  handle how to update uniqueShortestConditionalWayDir = calcUniqueShortestConditionalWayDir();
            return ALLDIRS;
        }
        if (updatedDistance.hasSmallerConditionalDistance(rawMinDistance)) {
            // the new distance is not smaller, but the stored condition has a shorter way
            suggestedDistanceFromSlidingNeighbours[fromDir].reduceIfSmaller(updatedDistance);
            rawMinDistance.reduceIfSmaller(updatedDistance);
            minDistance = null;
            uniqueShortestConditionalWayDir = fromDir;
            return ALLDIRS;
        }
        if (updatedDistance.equals(rawMinDistance)) {           // (8)(10)(5)
            // the same suggestion value that we already have als minimum
            if (uniqueShortestWayDir==fromDir)                  // (1b)
                return NONE;     // even the direction matches th known shortestWay, so nothing to do
            suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            uniqueShortestWayDir = MULTIPLE;
            if (updatedDistance.hasSmallerConditionalDistance(rawMinDistance) )
                uniqueShortestConditionalWayDir = fromDir;
            else if (uniqueShortestConditionalWayDir==fromDir && updatedDistance.hasEqualConditionalDistance(rawMinDistance))
                uniqueShortestConditionalWayDir = MULTIPLE;
            return oppositeDirIndex(fromDir);  //because this value is new from this direction, we better pass it on
        }
        // from here on, the new suggestion is in any case not the minimum
        if ( suggestedDistanceFromSlidingNeighbours[fromDir].   // (extra case?)
                equals(updatedDistance) ) {
            // the same suggestion value that we already had from this direction
            return NONE;
        }
        if (rawMinDistance.getShortestDistanceEvenUnderCondition()<=minDist) {
            // if we reached the "safe" squares (and the suggestion was not smaller) we cen stop, it cannot
            // be a vaild "reset-scenario" with overriding longer distances.
            // and an propagateToAll-call was or will be started from here anyway.
            return NONE;
        }
        if ( updatedDistance.isSmallerOrEqual(suggestedDistanceFromSlidingNeighbours[fromDir]) ) {
            // a smaller suggestion value than we already had from this direction
            suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            if ( getSuggestionToPassthroughIndex(fromDir).isSmallerOrEqual(updatedDistance) )
                return -oppositeDirIndex(fromDir)-1;
            return oppositeDirIndex(fromDir);
        }
        if ( uniqueShortestWayDir==fromDir) {   // (2)(3)(1c)
            // an real (but increasing!) update coming in from the known single shortest path
            // we have to check was has become of the former shortest in-path and recalc the unique shortest path
            suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            int minUpdated = recalcRawMinDistance();
            uniqueShortestWayDir = calcUniqueShortestWayDir();
            uniqueShortestConditionalWayDir = calcUniqueShortestConditionalWayDir();
            if (minUpdated!=0)
                return oppositeDirIndex(fromDir);
            return NONE;
        }
        if ( uniqueShortestWayDir==MULTIPLE
                && suggestedDistanceFromSlidingNeighbours[fromDir].equals(rawMinDistance) ) {       // (9)
            // an real update coming in from known one of the shortest pathes
            // this means, we don not yet have to worry, because another shorter path exists
            // but we need to check and flag, if the remaining in-path is now the only one left = unique
            suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            uniqueShortestWayDir = calcUniqueShortestWayDir();
            uniqueShortestConditionalWayDir = calcUniqueShortestConditionalWayDir();
            if ( getSuggestionToPassthroughIndex(fromDir).isSmallerOrEqual(updatedDistance) ) {
                // the neighbour seems to be outdated, let me inform him.
                return -oppositeDirIndex(fromDir)-1;
            }
            return oppositeDirIndex(fromDir)-1;
        }
        // an real update from a neighbour that was Further away (so far - and still is)
        suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
        //  needed to complete edge cases after "reset-bomb"
        //  if updateDistance is worse than the recommendation towards that fromPos from here would be,
        //  then tell this  back to this fromPosNeighbour, so it does not propagate its too high value to many theres, which needs to be corrected late again
        // BUT, it can be wrong! because if can have old distance information from unreachable neighbours.
        // BUTBUT, this cannot happen any more due to the resetPropagation that runs beforehand.
        if ( getSuggestionToPassthroughIndex(fromDir).isSmallerOrEqual(updatedDistance) )
            return -oppositeDirIndex(fromDir)-1;
        return oppositeDirIndex(fromDir);
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
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            propagateResetIfUSWTo1SlidingNeighbour(dirIndex);
    }

    private void propagateResetIfUSWTo1SlidingNeighbour(int toDir) {
        VirtualSlidingPieceOnSquare n = slidingNeighbours[toDir];
        if (n != null)
            n.propagateResetIfUSW(oppositeDirIndex(toDir));
    }

    private void propagateResetIfUSW(int fromDir ) {
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," R"+squareName(myPos));
        if (rawMinDistance.dist()==0 && fromDir!=FROMNOWHERE) {
            // I carry my own piece, i.e. distance=0. No reset necessary from here.
            return;
        }
        if ( fromDir==uniqueShortestWayDir ) {  // "reset-bomb" only if the neighbour was my only predecessor in the path
            // forget the dist-infos that I got from my neighbours
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"*");
            resetDistances();
            resetSlidingDistances();  // TODO: test if a reset of the single fromDir (instead of all dirs) was sufficient (dito for conditional distances)
            resetSlidingConditionalDistances();
            // and tell them to do the same, if i am their predecessor in the path
            uniqueShortestWayDir = NONE;
            uniqueShortestConditionalWayDir = NONE;
            propagateResetIfUSWToAllNeighbours();
        }
        else if ( uniqueShortestWayDir==MULTIPLE
                && suggestedDistanceFromSlidingNeighbours[fromDir].equals(rawMinDistance) ) {
            // only propagate reset to opposite neighbour because we were at least among several shortest inpaths
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"-");
            suggestedDistanceFromSlidingNeighbours[fromDir] = new Distance();
            uniqueShortestWayDir = calcUniqueShortestWayDir();
            uniqueShortestConditionalWayDir = calcUniqueShortestConditionalWayDir();
            propagateResetIfUSWTo1SlidingNeighbour(oppositeDirIndex(fromDir));
        }
        // additionaly check the similar case, if they apply for the conditional distances
        else if ( fromDir==uniqueShortestConditionalWayDir ) {  // "reset-bomb" only if the neighbour was my only predecessor in the path
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"x");
            resetConditionalDistances();        // TODO!!!: this must be transfered to the SIngleHopPieces as well!
            resetSlidingConditionalDistances();
            uniqueShortestConditionalWayDir = NONE;
            // and tell them to do the same, if i am their predecessor in the path
            propagateResetIfUSWToAllNeighbours();
        }
        else if ( uniqueShortestConditionalWayDir==MULTIPLE
                && suggestedDistanceFromSlidingNeighbours[fromDir].getShortestDistanceEvenUnderCondition() == rawMinDistance.getShortestDistanceEvenUnderCondition() ) {
            // only propagate reset to opposite neighbour because we were at least among several shortest conditional inpaths
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"~");
            suggestedDistanceFromSlidingNeighbours[fromDir].resetConditionalDistance();
            uniqueShortestWayDir = calcUniqueShortestWayDir();
            uniqueShortestConditionalWayDir = calcUniqueShortestConditionalWayDir();
            propagateResetIfUSWTo1SlidingNeighbour(oppositeDirIndex(fromDir));
        }
        else {
            // it was just a message from a neighbour that is further away than other neighbours
            // at least forget the old input from fromDir
            // (same implementation part as in setAndPropagateDistanceObeyingPassthrough();
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,".");
            suggestedDistanceFromSlidingNeighbours[fromDir].reset();
        }
    }

    /**
     * searches in all suggestions from neighbours for the correct fromDirection where the shortest in-path is coming from.
     * @return MULTIPLE, if more than one shortest in-path was found.  returns the fromDir otherwise.
     */
    private int calcUniqueShortestWayDir() {
        int newUSWD = -1;
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (slidingNeighbours[dirIndex]!=null
                    && suggestedDistanceFromSlidingNeighbours[dirIndex].equals(rawMinDistance)) {
                // we found (one of) the shortest in-paths
                if (newUSWD>-1)
                    return MULTIPLE;   // but this is already the second, so we have multiple shortest in-paths
                newUSWD = dirIndex;
            }
        return newUSWD;
    }

    private int calcUniqueShortestConditionalWayDir() {
        int newUSWD = -1;
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (slidingNeighbours[dirIndex]!=null
                    && suggestedDistanceFromSlidingNeighbours[dirIndex].hasEqualConditionalDistance(rawMinDistance) ) {
                // we found (one of) the shortest conditional in-paths
                if (newUSWD>-1)
                    return MULTIPLE;   // but this is already the second, so we have multiple shortest conditional in-paths
                newUSWD = dirIndex;
            }
        return newUSWD;
    }

}
