/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.FEATURE_TRY_BREADTHSEARCH;
import static de.ensel.tideeval.ChessBoard.NO_PIECE_ID;
import static de.ensel.tideeval.Distance.ANY;
import static de.ensel.tideeval.Distance.INFINITE_DISTANCE;

public class VirtualPawnPieceOnSquare extends VirtualOneHopPieceOnSquare {

    public VirtualPawnPieceOnSquare(ChessBoard myChessBoard, int newPceID, int pceType, int myPos) {
        super(myChessBoard, newPceID, pceType, myPos);
    }

    @Override
    public void setAndPropagateDistance(final @NotNull Distance suggestedDistance) {
        // for a pawn we recalc the correct distance ourselves from the predecessor squares
        // TODO? if suggestedDistance==0 set all backward distances to INFINITE
        if (suggestedDistance.dist()==0) {
            rawMinDistance.updateFrom(suggestedDistance);
            minDistance=null;
        }
        recalcAndPropagatePawnDistance();
    }

    @Override
    protected void propagateDistanceChangeToAllNeighbours() {
        // for a pawn we ignore all the parameters...
        // and recalc the correct distance ourselves from the predecessor squares
        // TODO? if suggestedDistance==0 set all backward distances to INFINITE
        recalcAndPropagatePawnDistance();
    }

    @Override
    protected void propagateResetIfUSWToAllNeighbours() {
        // not needed for pawn, its calc-algorithm can always cope with decreasing and increasing distances
    }

    protected Distance recalcSquareStraightPawnDistance() {
        int penalty = 0;
        // set the list of relevant predecessors
        if (!myChessBoard.isSquareEmpty(myPos)) {
            // I am on a square that carries a Piece of my own color
            // at the moment we cannot go there directly, but with an penalty - i.e. if the piece moves away
            // OR here is an opponents Piece that I unfortunately cannot beat (moving straight), but it could also move away
            penalty++;  // TODO: evaluate real costs of moving away (it might also not be possible)
        }
        //else
        //   empty square: no penalty
        // check special double square pawn move
        Distance minimum=null;
        if (hasLongPawnPredecessor(myPiece().color(), myPos)) {
            // square might be reachable by two square-move
            int midPos = getLongPawnMoveMidPos(myPiece().color(), myPos);
            int startPos = getLongPawnPredecessorPos(myPiece().color(), myPos);
            assert(midPos>-1);
            assert(startPos>-1);
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) myChessBoard.getBoardSquares()[startPos].getvPiece(myPceID);
            minimum = neighbour.minDistanceSuggestionTo1HopNeighbour();
            if (minimum!=null && minimum.getShortestDistanceEvenUnderCondition()<INFINITE_DISTANCE
                    && !myChessBoard.isSquareEmpty(midPos)) {
                // if middle square on the way is occupied by a piece, this one has to move away, too.
                int midPenalty=1;   // TODO(same): evaluate real costs of moving away (it might also not be possible)
                // we need to make the suggestion conditional + penalty
                minimum = new Distance(
                        INFINITE_DISTANCE,
                        midPos,    //TODO: should not overwrite the other condition, but chains of conditions is not yet supportet
                        ANY,
                        increaseIfPossible(minimum.getShortestDistanceEvenUnderCondition(), midPenalty) );
            }
        }
        int startPos = getSimpleStraightPawnPredecessorPos(myPiece().color(), myPos);
        if (startPos>-1) { // if ==-1, then it is pawn starting position, but as I do not carry the Piece myself, I must be out of reach...
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) myChessBoard.getBoardSquares()[startPos].getvPiece(myPceID);
            Distance suggestion = neighbour.minDistanceSuggestionTo1HopNeighbour();
            if (minimum == null)
                minimum = suggestion;
            else
                minimum.reduceIfSmaller(suggestion);
        }

        if (penalty==0 || minimum==null) {
            return minimum;
        }
        return new Distance(
                INFINITE_DISTANCE,
                myPos,    //TODO: should not overwrite the other condition, but chains of conditions is not yet supportet
                ANY,
                increaseIfPossible(minimum.getShortestDistanceEvenUnderCondition(), penalty) );
    }

    protected Distance getMinimumOfPredecessors(int[] predecessorDirs) {
        //TODO-low: check is this can be reused on super-class level
        Distance minimum = null;
        for (int predecessorDir : predecessorDirs) {
            if (neighbourSquareExistsInDirFromPos(predecessorDir, myPos)) {
                VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) myChessBoard.getBoardSquares()[myPos + predecessorDir].getvPiece(myPceID);
                Distance suggestion = neighbour.minDistanceSuggestionTo1HopNeighbour();
                if (minimum == null)
                    minimum = suggestion;
                else
                    minimum.reduceIfSmaller(suggestion);
            }
        }
        return minimum;
    }

    protected Distance recalcSquareBeatingPawnDistance() {
        int penalty = 0;
        int moveAwayFromCond = ANY;
        // set the list of relevant predecessors
        if (myChessBoard.isSquareEmpty(myPos)
                || myChessBoard.hasPieceOfColorAt(myPiece().color(), myPos) ) {
            // I am on a square that carries no Piece or my own Piece
            // one can only move here, if another opponents piece can move here
            // TODO: implement this case - for now it is left as not possible
            return null;  // TODO!! Cover own piece (needs something like distance==1), but do not continue propagation
            //penalty++;  // TODO: evaluate real costs of moving away (it might also not be possible)
            //moveAwayFromCond = myPos;
        }
        // else
            // here is an opponents Piece that I can beat, nothing special to do...

        final int[] beatingPredecessorDirs = getBeatingPawnPredecessorDirs(myPiece().color(), rankOf(myPos));
        Distance minimumSuggestion = getMinimumOfPredecessors(beatingPredecessorDirs);

        if (penalty==0 && moveAwayFromCond==ANY
                || minimumSuggestion==null) {
            return minimumSuggestion;
        }
        // we need to make the suggestion conditional + penalty
        // (but actually, at the moment, there is no szenario where e need it)
        return new Distance(
                increaseIfPossible(minimumSuggestion.dist(), penalty),
                moveAwayFromCond,
                ANY,
                increaseIfPossible(minimumSuggestion.getShortestDistanceEvenUnderCondition(), penalty)
        );
    }


    protected boolean recalcSquarePawnDistance() {
        // recalc (unless we are 0, i.e. the Piece itself is here at my square)
        if (rawMinDistance!=null && rawMinDistance.dist()==0)
            return false;
        Distance minimum = recalcSquareStraightPawnDistance();
        if (minimum==null)
            minimum = recalcSquareBeatingPawnDistance();
        else
            minimum.reduceIfSmaller( recalcSquareBeatingPawnDistance() );
        if (rawMinDistance!=null && rawMinDistance.equals(minimum)) {
            // nothing changed
            return false;
        }
        rawMinDistance = minimum;
        minDistance = null;
        return true;
    }

    protected void recalcAndPropagatePawnDistance() {
        if (rawMinDistance==null || rawMinDistance.dist()==0
                || recalcSquarePawnDistance() )
            recalcNeighboursAndPropagatePawnDistance();
    }

    protected void recalcNeighboursAndPropagatePawnDistance() {
        // if my result changed, do propagation:
        // first tell neighbours to correct their own distance
        final int[] neighbourDirs = getAllPawnDirs(myPiece().color(), rankOf(myPos));
        for (int neighbourDir : neighbourDirs) {
            if (neighbourSquareExistsInDirFromPos(neighbourDir, myPos)) {
                VirtualPawnPieceOnSquare n = (VirtualPawnPieceOnSquare) myChessBoard.getBoardSquares()[myPos + neighbourDir].getvPiece(myPceID);
                n.recalcSquarePawnDistance();
            }
        }
        // then on that basis start breadth propagation
        for (int neighbourDir : neighbourDirs) {
            if (neighbourSquareExistsInDirFromPos(neighbourDir, myPos)) {
                VirtualPawnPieceOnSquare n = (VirtualPawnPieceOnSquare) myChessBoard.getBoardSquares()[myPos + neighbourDir].getvPiece(myPceID);
                //breadth search is mandatory here  - so no if (FEATURE_TRY_BREADTHSEARCH)
                myPiece().quePropagation(
                        minDistanceSuggestionTo1HopNeighbour().getShortestDistanceEvenUnderCondition(),
                        () -> n.recalcNeighboursAndPropagatePawnDistance());
            }
        }
    }


}
