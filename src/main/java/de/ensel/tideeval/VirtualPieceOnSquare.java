/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.ensel.tideeval.ChessBasics.*;
import static java.lang.Math.min;

public class VirtualPieceOnSquare implements Comparable {
    private final ChessBoard myChessBoard;
    private final int myPceID;
    private final int myPos;
    private int rel_eval;
    private int rawMinDistance;   // distance in hops from corresponding real piece.
                                  // "raw" means, it does not take into account if this square is blocked by this piece itself

    static final int DISTANCE_NOT_SET = Integer.MAX_VALUE;

    private final List<VirtualPieceOnSquare> singleNeighbours;
    private final VirtualPieceOnSquare[] slidingNeighbours;
    private final int[] suggestedDistanceFromNeighbours;

    public VirtualPieceOnSquare(ChessBoard myChessBoard, int newPceID,  int myPos) {
        this.myChessBoard = myChessBoard;
        this.myPos = myPos;
        myPceID = newPceID;
        singleNeighbours = new ArrayList<>();
        slidingNeighbours = new VirtualPieceOnSquare[MAXMAINDIRS];
        suggestedDistanceFromNeighbours = new int[MAXMAINDIRS];
        resetDistances();
        rel_eval = NOT_EVALUATED;
    }

    private void resetDistances() {
        for (int i = 0; i < MAXMAINDIRS; i++)
            suggestedDistanceFromNeighbours[i] = DISTANCE_NOT_SET;
        rawMinDistance = DISTANCE_NOT_SET;
    }

    /**
     * myPiece()
     * @return backward reference to my corresponding real piece on the Board
     */
    private ChessPiece myPiece() {
        return myChessBoard.getPiece(myPceID);
    }

    /**
     * mySquarePiece()
     * @return reference to the piece sitting on my square, or null if empty
     */
    private ChessPiece mySquarePiece() {
        return myChessBoard.getPieceAt(myPos);
    }

    @Override
    public int compareTo(@NotNull Object other) {
        if (this.rel_eval > ((VirtualPieceOnSquare)other).rel_eval)
            return 1;
        else if (this.rel_eval < ((VirtualPieceOnSquare)other).rel_eval)
            return -1;
        //else
        return 0;
    }

    // setup basic neighbourhood network
    public void addSingleNeighbour(VirtualPieceOnSquare newVPiece) {
        singleNeighbours.add( newVPiece );
    }

    public void addSlidingNeighbour(VirtualPieceOnSquare neighbourPce, int direction) {
        slidingNeighbours[convertMainDir2DirIndex(direction)] = neighbourPce;
    }

    // set up initial distance from spawning position
    public void setDistance(final int distance) {
        // one extra hop (around the corner or for non-sliding neighbours
        // treated just like sliding neighbour, but with no matching "from"-direction
         setInitialDistanceObeyingPassthrough(distance, FROMNOWHERE);
    }

    private void setInitialDistanceObeyingPassthrough(final int suggestedDistance, int passingThroughInDirIndex) {
        assert(suggestedDistance>=0);
        if (suggestedDistance==0) {
            // I carry my own piece, i.e. distance=0.  test is needed, otherwise I'd act as if I'd find my own piece here in my way...
            rawMinDistance = 0;
            assert(passingThroughInDirIndex == FROMNOWHERE);
        }
        else if (passingThroughInDirIndex!=FROMNOWHERE ) { 
            //  from a certain direction the distance changed
            int oldRawMinDistance = rawMinDistance;
            int oldSuggestionToOppositeNeighbour =         // remember how the latest suggestion to opposite neighbour was
                    getSuggestionToPassthroughIndex(passingThroughInDirIndex);
            updateSuggestedDistanceInPassthroughDirIndex(suggestedDistance, passingThroughInDirIndex);
            //  if new distance is different from what it was, tell my neighbours
            int newSuggestionToOppositeNeighbour = getSuggestionToPassthroughIndex(passingThroughInDirIndex);
            if (rawMinDistance !=oldRawMinDistance) {
                // a greater change has happened, inform all
                informAllSlidingNeighbours();    // doch nicht: Except(oppositeDirIndex(passingThroughInDirIndex));
            }
            else if (newSuggestionToOppositeNeighbour!=oldSuggestionToOppositeNeighbour) {
                // no greater change, but still, the passthroughSuggestion has changed, so inform opposite neighbour
                informSlidingNeighbour(passingThroughInDirIndex);
            }
            return;  //TODO: This only works for initial calculation, needs different implementation for updates
        }
        // from here on passingThroughInDirIndex==FROMNOWHERE
        else if (rawMinDistance <= suggestedDistance) { // Achtung: Fehlerquelle? - kann das wirklich immer für die ganze Reihe abgebrochen werden?
            // it was already closer via another way. -> I do not need to tell any of my neighbours
            return;  //TODO for singleNeighbours: This only works for initial calculation, needs different implementation for updates
        }
        //else
        // ok, I set the new (and shorter) distance
        //TODO for singleNeighbours: This only works for initial calculation, needs different implementation for updates
        updateRawMinDistance(suggestedDistance);
        informAllNeighbours();
    }

    private void informAllNeighbours() {
        // first the direct "singleNeighbours"
        for (VirtualPieceOnSquare n: singleNeighbours) {
            n.setDistance(minDistanceSuggestionTo1HopNeighbour());
            // TODO: see above, this also depends on where a own mySquarePiece can move to - maybe only in the way?
        }
        informAllSlidingNeighbours();
    }

    private void informSlidingNeighbour(int passingThroughInDirIndex) {
        // inform one (opposite) neighbour
        VirtualPieceOnSquare n = slidingNeighbours[passingThroughInDirIndex];
        if (n != null)
            n.setInitialDistanceObeyingPassthrough(
                    getSuggestionToPassthroughIndex(passingThroughInDirIndex),
                    passingThroughInDirIndex);
    }

    private void informAllSlidingNeighbours() {
        // for the slidingNeighbours, we need to check from which direction the figure is coming from
        for (int dirIndex=0; dirIndex<MAXMAINDIRS; dirIndex++)
            informSlidingNeighbour(dirIndex);
        /*{
            VirtualPieceOnSquare n = slidingNeighbours[dirIndex];
            if (n != null)
                n.setInitialDistanceObeyingPassthrough(getSuggestionToPassthroughIndex(dirIndex), dirIndex);
        }*/
    }

    private void informAllSlidingNeighboursExcept(int excludeDirIndex) {
        // for the slidingNeighbours, we need to check from which direction the figure is coming from
        for (int dirIndex = 0; dirIndex < MAXMAINDIRS; dirIndex++)
            if (dirIndex!=excludeDirIndex)
                informSlidingNeighbour(dirIndex);
    }


    private void updateSuggestedDistanceInPassthroughDirIndex(int suggestedDistance, int passthroughInDirIndex) {
        // update my own distance-values by new information
        suggestedDistanceFromNeighbours[passthroughInDirIndex]= suggestedDistance;
        updateRawMinDistance(suggestedDistance);
    }

    private int getSuggestionToPassthroughIndex(int passthroughDirIndex) {
        if (suggestedDistanceFromNeighbours[passthroughDirIndex]==DISTANCE_NOT_SET
            ||  myChessBoard.hasPieceOfColorAt( opponentColor(myPiece().color()), myPos )  // an opponents piece actually hinders passthrough
        ) {
            return minDistanceSuggestionTo1HopNeighbour();
        }
        return min( // either take a corner after the shortest distance
                    minDistanceSuggestionTo1HopNeighbour(),
                    // or stay on the passthrough towards opposite neighbour
                    suggestedDistanceFromNeighbours[passthroughDirIndex]
                            + movingOwnPieceFromSquareDistancePenalty()
                );
    }

    private void updateRawMinDistance(int updatedDistance) {
        if (updatedDistance< rawMinDistance)
            rawMinDistance = updatedDistance;    // avoid iteration over all suggestions, if latest update was the minimum for sure.
        else
            recalcRawMinDistance();
    }

    private void recalcRawMinDistance() {
        if (rawMinDistance==0)
            return;  // there is nothing closer than myself...
        //rawMinDistance = (IntStream.of(suggestedDistanceFromNeighbours)).min().getAsInt();
        rawMinDistance = (Arrays.stream(suggestedDistanceFromNeighbours)).min().getAsInt();
    }

    public int realMinDistanceFromPiece() {
        if (rawMinDistance==0)
            return 0;  // there is nothing closer than myself...
        if (rawMinDistance==DISTANCE_NOT_SET)
            return DISTANCE_NOT_SET;  // can't get worse
        return rawMinDistance+movingOwnPieceFromSquareDistancePenalty();
    }

    public int minDistanceSuggestionTo1HopNeighbour() {
        if (rawMinDistance==0)
            return 1;  // almost nothing is closer than my neighbour
        if (rawMinDistance==DISTANCE_NOT_SET)
            return DISTANCE_NOT_SET;  // can't get worse
        return rawMinDistance+movingOwnPieceFromSquareDistancePenalty()+1;
    }

    public int movingOwnPieceFromSquareDistancePenalty() {
        // looks if this square is blocked by own (but other) piece and needs to move away first
        if (myChessBoard.hasPieceOfColorAt( myPiece().color(), myPos )) {
            // TODO: make further calculation depending on whether mySquarePiece can move away
            // for now just assume it can move away, and this costs one move=>distance+1
            return 1;  // after taking the moving away nto account, it was already closer via another way.
            // TODO: somehow store this as a "condition" for the further distance calculations
        }
        //else
        return 0;
    }

    public void pieceHasArrivedHere(int pid) {
        if (pid == myPceID) {
            //my own Piece is here - but I was already told and distance set to 0
            assert (rawMinDistance == 0);
            return;
        }
        
        // inform neighbours that something has arrived here
        informAllNeighbours();

        // TODO: (already ongoing) implementation is incorrect for passthrough movements, after this new piece disappears again.
        // we need testcases to detect this error and a more complex implementation remembering the minimum suggested
        //  distance coming in from each direction, to calculate passthroughs correctly...
    }

    public void pieceHasMovedAway() {
        // plan was to inform neighbours that something has changed here
        // but: we do not tell the neighbours yet, this will be triggered by placement of piece on new square
        //informAllNeighbours();
    }
}
