/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.ArrayList;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.Distance.ANY;
import static de.ensel.tideeval.Distance.INFINITE_DISTANCE;

public class VirtualSlidingPieceOnSquare extends VirtualPieceOnSquare {

    // array of slinging neighbours, one per direction-index (see ChessBasics)
    private final VirtualSlidingPieceOnSquare[] slidingNeighbours;
    // .. and the corresponding distances suggested by these neighbours.
    private final Distance[] suggestedDistanceFromSlidingNeighbours;
    private int uniqueShortestWayDir;   // if only one shortest way to this square exists, this is the direction back to the piece
    // it also shows where the minimal suggestedDist was. it is -1 if there is more than one way here
    private int latestUpdateFromSlidingNeighbour[];

    public VirtualSlidingPieceOnSquare(ChessBoard myChessBoard, int newPceID, int myPos) {
        super(myChessBoard, newPceID, myPos);
        slidingNeighbours = new VirtualSlidingPieceOnSquare[MAXMAINDIRS];
        suggestedDistanceFromSlidingNeighbours = new Distance[MAXMAINDIRS];
        latestUpdateFromSlidingNeighbour = new int[MAXMAINDIRS];
        uniqueShortestWayDir = MULTIPLE;
        resetSlidingDistances();
    }

    void resetSlidingDistances() {
        for (int i = 0; i < MAXMAINDIRS; i++) {
            suggestedDistanceFromSlidingNeighbours[i] = new Distance();
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
        System.out.print(" /2ff: ");
        myChessBoard.getPiece(myPceID).startNextUpdate();
        setAndPropagateDistanceObeyingPassthrough(distance, 2,  Integer.MAX_VALUE );  //minDist, maxDist );  */
    }


    @Override
    protected void propagateDistanceChangeToAllNeighbours() {
        propagateDistanceChangeToSlidingNeighboursExceptDir(-1, -1, Integer.MAX_VALUE);
        /* a spit in two phases made the local updates even worse in the test szenario (BasicPiecePlacementTest)
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
        assert(suggestedDistance.getDistanceUnderCondition()>=0);
        System.out.print(" {"+squareName(myPos)+"_"+ suggestedDistance);
        if (suggestedDistance.dist()==0) {
            // I carry my own piece, i.e. distance=0.  test is needed, otherwise I'd act as if I'd find my own piece here in my way...
            rawMinDistance = suggestedDistance;  //new Distance(0);
            minDistance = rawMinDistance;
            assert(passingThroughInDirIndex == FROMNOWHERE);
            propagateDistanceChangeToAllNeighbours(minDist, maxDist);
            return;
        }
        //else
        if (suggestedDistance.getDistanceUnderCondition() > maxDist) {
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
            if (rawMinDistance.getDistanceUnderCondition()<minDist) {
                // I am still in a range that is already set
                // I change nothing, but get the propagation rolling
                propagateDistanceChangeToOutdatedNeighbours(minDist, maxDist);
                return;
            }
            if (rawMinDistance.getDistanceUnderCondition()==minDist) {   // TODO!! - splitting does not works - ends up in endles loop or misses necessary updates
                // I am at the border of what was already set
                neededPropagationDir = ALLDIRS;
            }

            switch(neededPropagationDir) {
                case NONE:
                    return;
                case ALLDIRS:
                    System.out.print("|");
                    // and, if new distance is different from what it was, also tell all other neighbours
                    propagateDistanceChangeToSlidingNeighboursExceptDir(-1, minDist, maxDist);
                    break;
                default:
                    // here, only the passthroughSuggestion has changed, so inform opposite neighbour
                    // only in one special case, it is unsure if the information needs to be reflected
                    // back to the neighbour, because he might be wrong... (this is signales by -dir)
                    if (neededPropagationDir < 0) {
                        neededPropagationDir = -neededPropagationDir;
                        propagateDistanceChangeToSlidingNeighbourInDir(oppositeDirIndex(neededPropagationDir), minDist, maxDist);
                    }
                    propagateDistanceChangeToSlidingNeighbourInDir(neededPropagationDir, minDist, maxDist);
            }
            System.out.print("}");
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
            /*** breadth search propagation will follow later:
            myPiece().quePropagation(suggestion.getDistanceUnderCondition(), ()->{ n.setAndPropagateDistanceObeyingPassthrough(
                    suggestion,
                    passingThroughInDirIndex,
                    minDist, maxDist); } );  ***/
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
                        ||   suggestedDistanceFromSlidingNeighbours[fromDir].getDistanceUnderCondition() == INFINITE_DISTANCE
                        // if an opponent's piece actually hinders passthrough, it can be taken (=+1 hop without passthrough-option)
                        || myChessBoard.hasPieceOfColorAt(opponentColor(myPiece().color()), myPos)  ){
            return minDistanceSuggestionTo1HopNeighbour();
        }
        // now this is either the same (take a corner after the shortest distance)
        Distance suggestion = minDistanceSuggestionTo1HopNeighbour();
        // or stay on the passthrough towards opposite neighbour:
        // but this might have a penalty if own figure is in the way:
        int penalty = movingOwnPieceFromSquareDistancePenalty();
        if (penalty==0) {
            // passthrough possible
            suggestion.reduceIfSmaller(suggestedDistanceFromSlidingNeighbours[fromDir]);
        }
        else {
            // own piece in the way
            Distance d = new Distance(INFINITE_DISTANCE,
                    myPos, ANY,
                    increaseIfPossible(suggestedDistanceFromSlidingNeighbours[fromDir].dist(), penalty) );
            // TODO:  this should not overwrite an existing condition! Chains of conditions are necessery to introduce...
            suggestion.reduceIfSmaller(d);
            // TODO: Scheint nicht falsch, aber könnte effizienter implementiert werden, wenn die Annahme stimmt, dass das d wg. der penalty eh niemals kleiner sein kann als die suggestion (die auch die selbe penalty enthält und ansonsten das minimum aus den verschiedenen Richtungen ist.
        }
        return suggestion;
    }



    /**
     * Updates the overall minimum distance
     * @param updatedDistance
     * @return int what needs to be updated. NONE for nothing, ALLDIRS for all, below that >0 tells a single direction
     */
    private int updateRawMinDistances(final Distance updatedDistance,
                                      final int fromDir, final int minDist ) {
        setLastUpdateToNow();
        latestUpdateFromSlidingNeighbour[fromDir] = latestUpdate();
        if (rawMinDistance.dist()==0)
            return NONE;  // there is nothing closer than myself...
        if (updatedDistance.isSmaller(rawMinDistance)) {
            // the new distance is smaller than the minimum, so we already found the new minimum
            suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            rawMinDistance.reduceIfSmaller(updatedDistance);
            minDistance = null;
            uniqueShortestWayDir = fromDir;
            return ALLDIRS;
        }
        if (updatedDistance.equals(rawMinDistance)) {
            // the same suggestion value that we already have als minimum
            if (uniqueShortestWayDir==fromDir)   // even the direction matches th known shortestWay
                return NONE;
            suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            uniqueShortestWayDir = MULTIPLE;
            return oppositeDirIndex(fromDir);  //because this value is new from this direction, we better pass it on
        }
        // from here on, the new suggestion is in any case not the minimum
        if ( suggestedDistanceFromSlidingNeighbours[fromDir].equals(updatedDistance) ) {
            // the same suggestion value that we already had from this direction
            return NONE;
        }
        if (rawMinDistance.getDistanceUnderCondition()<=minDist) {
            // if we reached the "safe" squares (and the suggestion was not smaller) we cen stop, it cannot
            // be a vaild "reset-scenario" with overriding longer distances.
            // and an propagateToAll-call was or will be started from here anyway.
            return NONE;
        }
        if ( updatedDistance.isSmallerOrEqual(suggestedDistanceFromSlidingNeighbours[fromDir]) ) {
            // a smaller suggestion value than we already had from this direction
            suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            return oppositeDirIndex(fromDir);
        }
        if ( uniqueShortestWayDir==fromDir) {   // "rest-bomb":
            // an real (but incrasing!) update coming in from known single the shortest path
            // sorry, I need to forget evething at this moment, it might all be outdated because I might have
            // been the shortest path to all of them
            resetDistances();
            resetSlidingDistances();
            //latestUpdate = myChessBoard.getPiece(myPceID).latestUpdate();
            suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            rawMinDistance.updateFrom(updatedDistance);
            uniqueShortestWayDir = fromDir;
            return ALLDIRS;
        }
        if ( uniqueShortestWayDir==MULTIPLE
                && suggestedDistanceFromSlidingNeighbours[fromDir].equals(rawMinDistance) ) {
            // an real update coming in from known one of the shortest pathes
            // this means, we don not yet have to worry, because another shorter path exists
            // but we need to check and flag, if the remaining in-path is now the only one left = unique
            suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
            uniqueShortestWayDir = refindUniqueShortestWayDir();
            return -oppositeDirIndex(fromDir);
        }
        // an real update from a neighbour that was Further away (so far - and still is)
        suggestedDistanceFromSlidingNeighbours[fromDir].updateFrom(updatedDistance);
        //  needed to complete edge cases after "reset-bomb"
        //  if updateDistance is worse than the recommendation towards that fromPos from here would be,
        //  then tell this  back to this fromPosNeighbour, so it does not propagate its too high value to many theres, which needs to be corrected late again
        if ( getSuggestionToPassthroughIndex(fromDir).isSmallerOrEqual(updatedDistance) )
            return -oppositeDirIndex(fromDir);
        return oppositeDirIndex(fromDir);
    }


    private int refindUniqueShortestWayDir() {
        int newUSWD = -1;
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (slidingNeighbours[dirIndex]!=null && suggestedDistanceFromSlidingNeighbours[dirIndex].equals(rawMinDistance)) {
                // we found (one of) the shortest in-paths
                if (newUSWD>-1)
                    return MULTIPLE;   // but this is already the second, so we have multiple shortest in-paths
                newUSWD = dirIndex;
            }
        return newUSWD;
    }

}
