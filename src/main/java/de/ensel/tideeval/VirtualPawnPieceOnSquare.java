/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ConditionalDistance.ANY;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;

public class VirtualPawnPieceOnSquare extends VirtualOneHopPieceOnSquare {

    public VirtualPawnPieceOnSquare(ChessBoard myChessBoard, int newPceID, int pceType, int myPos) {
        super(myChessBoard, newPceID, pceType, myPos);
    }


    @Override
    protected void resetMovepathBackTo(int frompos) {
        updatesOpenFromPos = frompos;
    }


    @Override
    public void setAndPropagateDistance(final ConditionalDistance suggestedDistance) {
        // for a pawn we recalc the correct distance ourselves from the predecessor squares
        // TODO? if suggestedDistance==0 set all backward distances to INFINITE
        if (suggestedDistance.dist()==0 || suggestedDistance.dist()==Integer.MAX_VALUE) {
            rawMinDistance.updateFrom(suggestedDistance);
            minDistance=null;
        }
        // do not start here, but where the pawn came from i.e.
        if (updatesOpenFromPos>-1)
            ((VirtualPawnPieceOnSquare)(myChessBoard.getBoardSquares()[updatesOpenFromPos].getvPiece(myPceID)))
                .setAndPropagateDistance(new ConditionalDistance());
        else  // unless it is a new piece
            recalcAndPropagatePawnDistance();
        updatesOpenFromPos = -1;
    }

    @Override
    protected void propagateDistanceChangeToAllOneHopNeighbours() {
        recalcAndPropagatePawnDistance();
    }

    @Override
    protected void propagateResetIfUSWToAllNeighbours() {
        // not needed in this completely new way for pawns,
        // its calc-algorithm can always cope with decreasing and increasing distances
    }

    public void setUpdatesOpenFromPos(int updatesOpenFromPos) {
        this.updatesOpenFromPos = updatesOpenFromPos;
    }

    int updatesOpenFromPos = -1;

    protected void recalcAndPropagatePawnDistance() {
        if ( recalcSquarePawnDistance()
                || rawMinDistance==null || rawMinDistance.dist()==0 || rawMinDistance.isInfinite()
        )
            recalcNeighboursAndPropagatePawnDistance();
    }

    @Override
    protected int recalcRawMinDistanceFromNeighbours() {
        return recalcSquarePawnDistance() ? 1 : 0;
    }

    /**
     * recalculate the vPiece's (i.e. the square's) distance from its pawn from the 3 or 4 squares
     * it can be reached from and stores it in rawMinDistance.
     * @return boolean if distance changed
     */
    protected boolean recalcSquarePawnDistance() {
        // recalc (unless we are 0, i.e. the Piece itself is here at my square)
        if (myPos==myPiece().getPos())  // propagation reached the pawn itself...
            return true;
        if (rawMinDistance!=null && rawMinDistance.dist()==0)
            return false;   // assert(false) not possible, because method can be called "behind" a pawn
        ConditionalDistance minimum = recalcSquareStraightPawnDistance();
        if (minimum==null)
            minimum = recalcSquareBeatingPawnDistance();
        else
            minimum.reduceIfSmaller( recalcSquareBeatingPawnDistance() );
        if (rawMinDistance!=null && rawMinDistance.distEquals(minimum)) {
            // nothing changed
            return false;
        }
        setLatestChangeToNow();
        rawMinDistance = minimum;
        minDistance = null;
        return true;
    }

    protected ConditionalDistance recalcSquareStraightPawnDistance() {
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
        ConditionalDistance minimum=null;
        if (hasLongPawnPredecessor(myPiece().color(), myPos)) {
            // square might be reachable by two square-move
            int midPos = getLongPawnMoveMidPos(myPiece().color(), myPos);
            int startPos = getLongPawnPredecessorPos(myPiece().color(), myPos);
            assert(midPos>-1);
            assert(startPos>-1);
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) myChessBoard.getBoardSquares()[startPos].getvPiece(myPceID);
            minimum = neighbour.minDistanceSuggestionTo1HopNeighbour();
            if (minimum!=null && minimum.dist()<INFINITE_DISTANCE
                    && !myChessBoard.isSquareEmpty(midPos)) {
                // if middle square on the way is occupied by a piece, this one has to move away, too.
                int midPenalty=1;   // TODO(same): evaluate real costs of moving away (it might also not be possible)
                // we need to make the suggestion conditional + penalty
                minimum = new ConditionalDistance( minimum,
                        midPenalty,
                        midPos,    //TODO: should not overwrite the other condition, but chains of conditions is not yet supportet
                        ANY );
            }
        }
        int startPos = getSimpleStraightPawnPredecessorPos(myPiece().color(), myPos);
        if (startPos>-1) { // if ==-1, then it is pawn starting position, but as I do not carry the Piece myself, I must be out of reach...
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) myChessBoard.getBoardSquares()[startPos].getvPiece(myPceID);
            ConditionalDistance suggestion = neighbour.minDistanceSuggestionTo1HopNeighbour();
            if (minimum==null)
                minimum = suggestion;
            else
                minimum.reduceIfSmaller(suggestion);
        }

        if (penalty==0 || minimum==null) {
            return minimum;
        }
        return new ConditionalDistance( minimum,
                penalty,
                myPos,
                ANY );
    }

    protected ConditionalDistance getMinimumOfPredecessors(int[] predecessorDirs) {
        //TODO-low: check is this can be reused on super-class level
        ConditionalDistance minimum = null;
        for (int predecessorDir : predecessorDirs) {
            if (neighbourSquareExistsInDirFromPos(predecessorDir, myPos)) {
                VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) myChessBoard
                        .getBoardSquares()[myPos+predecessorDir].getvPiece(myPceID);
                ConditionalDistance suggestion = neighbour.minDistanceSuggestionTo1HopNeighbour();
                if (minimum==null)
                    minimum = suggestion;
                else
                    minimum.reduceIfSmaller(suggestion);
            }
        }
        return minimum;
    }

    protected ConditionalDistance recalcSquareBeatingPawnDistance() {
        int penalty = 0;
        int moveAwayFromCond = ANY;
        int moveHereCond = ANY;

        // set the list of relevant predecessors and get minimum of their distance suggestion
        final int[] beatingPredecessorDirs = getBeatingPawnPredecessorDirs(myPiece().color(), rankOf(myPos));
        ConditionalDistance minimumSuggestion = getMinimumOfPredecessors(beatingPredecessorDirs);

        // dist semantics: also squares that are covered (although pawn cannot move there) are marked as distance==1
        if (myChessBoard.isSquareEmpty(myPos) ) {
            // I am on a square that carries no Piece
            // first check if this nevertheless is a en-passant beatable pawn
            if (myPos==myChessBoard.getEnPassantPosForTurnColor(colorOfPieceType(myPceID))
                    && minimumSuggestion.dist()==1) {
                // it is possible to beat by en-passant in the first move
                debugPrint(DEBUGMSG_BOARD_MOVES, "/"+squareName(myPos)+"/");
            } else {
                // Pawn can only move here, if another opponents piece moves here
                // TODO: implement check if opponent can move here... (needs to be sure that opponents
                //  distances are alread evaluated sufficiantly!)  - for now it is left as if it was possible
                //penalty++;  // TODO: evaluate real costs of moving away (it might also not be possible)
                //moveAwayFromCond = myPos;
                moveHereCond = myPos;  // Todo: Enable Distance-Condition to express that this is only valid for an opponents Piece
            }
        }
        else if (myChessBoard.hasPieceOfColorAt(myPiece().color(), myPos) ) {
            // I am on a square that carries my own Piece
            // Pawn can only move here, if another opponents piece takes here
            // TODO: implement check if opponent can move here... (needs to be sure that opponents
            //  distances are alread evaluated sufficiantly!)  - for now it is left as if it was possible
            //! here no penalty++ -- weggaihn nutzd nix
            moveHereCond = myPos;  // Todo: Enable Distance-Condition to express that this is only valid for an opponents Piece
        }
        // else
            // here is an opponents Piece that I can beat, nothing special to do...

        if (penalty==0 && moveAwayFromCond==ANY && moveHereCond==ANY
                || minimumSuggestion==null) {
            return minimumSuggestion;
        }
        // we need to make the suggestion conditional + penalty
        // (but actually, at the moment, there is no scenario where e need it)
        return new ConditionalDistance( minimumSuggestion,
                penalty,
                moveAwayFromCond,
                moveHereCond
        );
    }


    protected void recalcNeighboursAndPropagatePawnDistance() {
        // if my result changed, do propagation:
        // first tell neighbours to correct their own distance
        final int[] neighbourDirs = getAllPawnDirs(myPiece().color(), rankOf(myPos));
        final boolean[] neighbourUpdated = new boolean[neighbourDirs.length];
        for (int i = 0; i < neighbourDirs.length; i++) {
            if (neighbourSquareExistsInDirFromPos(neighbourDirs[i], myPos)) {
                VirtualPawnPieceOnSquare n = (VirtualPawnPieceOnSquare) myChessBoard
                        .getBoardSquares()[myPos+neighbourDirs[i]].getvPiece(myPceID);
                neighbourUpdated[i] = n.recalcSquarePawnDistance();
            }
            else
                neighbourUpdated[i] = false;
        }
        // then on that basis start breadth propagation where necessary
        for (int i = 0; i < neighbourDirs.length; i++) {
            if (neighbourUpdated[i]) {  //neighbourSquareExistsInDirFromPos(neighbourDir, myPos)) {
                VirtualPawnPieceOnSquare n = (VirtualPawnPieceOnSquare) myChessBoard
                        .getBoardSquares()[myPos+neighbourDirs[i]].getvPiece(myPceID);
                //breadth search is mandatory here  - so no if (FEATURE_TRY_BREADTHSEARCH)
                int quePriority = n.getRawMinDistanceFromPiece().dist();
                if (quePriority==INFINITE_DISTANCE)
                    quePriority=0;  // resets/unreachables must be propagated immediately
                myPiece().quePropagation(
                        quePriority,
                        n::recalcNeighboursAndPropagatePawnDistance);
            }
        }
    }


}
