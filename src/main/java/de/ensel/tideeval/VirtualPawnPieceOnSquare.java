/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import static de.ensel.tideeval.ChessBasics.*;
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
        ConditionalDistance origSuggestionToNeighbour = minDistanceSuggestionTo1HopNeighbour();
        ConditionalDistance minimum = recalcSquareStraightPawnDistance();
        if (minimum==null)
            minimum = recalcSquareBeatingPawnDistance();
        else
            minimum.reduceIfSmaller( recalcSquareBeatingPawnDistance() );
        rawMinDistance = minimum;
        ConditionalDistance newSuggestionToNeighbour = minDistanceSuggestionTo1HopNeighbour();
        if (rawMinDistance!=null && rawMinDistance.distEquals(minimum)
            // nothing changed in the suggestions, but my own sqaure could have changed a piece, so check minDistance
            && origSuggestionToNeighbour.distEquals(newSuggestionToNeighbour)
            && origSuggestionToNeighbour.conditionEquals(newSuggestionToNeighbour)
        ) {
            return false;
        }
        setLatestChangeToNow();
        minDistance = null;
        return true;
    }

    protected ConditionalDistance recalcSquareStraightPawnDistance() {
        ConditionalDistance minimum=null;
        if (hasLongPawnPredecessor(myPiece().color(), myPos)) {
            // square might be reachable by two square-move
            int midPos = getLongPawnMoveMidPos(myPiece().color(), myPos);
            int startPos = getLongPawnPredecessorPos(myPiece().color(), myPos);
            assert(midPos>-1);
            assert(startPos>-1);
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) myChessBoard.getBoardSquares()[startPos]
                    .getvPiece(myPceID);
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
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) myChessBoard.getBoardSquares()[startPos]
                    .getvPiece(myPceID);
            ConditionalDistance suggestion = neighbour.minDistanceSuggestionTo1HopNeighbour();
            if (minimum==null)
                minimum = suggestion;
            else
                minimum.reduceIfSmaller(suggestion);
        }
        if (minimum==null)
            return null;
        if (minimum.isInfinite())
            return minimum;
        if (myChessBoard.hasPieceOfColorAt(opponentColor(myPiece().color()), myPos )) {
            // opponent is in the way, it needs to move away first
            // TODO: Check if dist needs to inc, if opponent has to move away.
            minimum.addCondition(myPos,ANY);
            minimum.inc();
        }
        else if (myChessBoard.hasPieceOfColorAt(myPiece().color(), myPos )) {
            // my own piece is in the way, it needs to move away first
            minimum = new ConditionalDistance(minimum,
                    movingMySquaresPieceAwayDistancePenalty()+1,
                    myPos,ANY);
        }
        return minimum;
    }

    protected ConditionalDistance recalcSquareBeatingPawnDistance() {
        int penalty = 0;
        int moveAwayFromCond = ANY;
        int moveHereCond = ANY;

        // set the list of relevant predecessors and get minimum of their distance suggestion
        final int[] beatingPredecessorDirs = getBeatingPawnPredecessorDirs(myPiece().color(), rankOf(myPos));
        ConditionalDistance minimum = getMinimumBeatingSuggestionOfPredecessors(beatingPredecessorDirs);
        if (myChessBoard.hasPieceOfColorAt(opponentColor(myPiece().color()), myPos )) {
            // opponent is in the way, this is great, so it can be beaten...
            // nothing else to do
        }
        else if (myChessBoard.hasPieceOfColorAt(myPiece().color(), myPos )
            // if my own piece is in the way, it needs to be beaten by an opponent first
            || (myChessBoard.isSquareEmpty(myPos))
            // or square is empty, so an opponent needs to move here first
        ) {
            // similar to sliding pieces that need to move out of the way, here a piece to come here.
            // do not count the first opponent coming to be beaten as distance, but later do count (this is not very precise...)
            if (!minimum.isUnconditional())
                minimum.inc();
            minimum.addCondition(ANY,myPos);
        }
        return minimum;
    }

    protected ConditionalDistance getMinimumBeatingSuggestionOfPredecessors(int[] predecessorDirs) {
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
        if (minimum==null)
            return new ConditionalDistance();
        return minimum;
    }


    /**
     * tells the distance after moving away from here,
     * careful: othen than for other pieces, the method does not consider if a Piece is in the way here,
     * because it cannot know here where the piece came from: from a beating or a moving position...
     * @return a "safe"=new ConditionalDistance
     */
    @Override
    public ConditionalDistance minDistanceSuggestionTo1HopNeighbour() {
        // Todo: Increase 1 more if Piece is pinned to the king
        if (rawMinDistance==null) {
            // should normally not happen, but in can be the case for completely unset squares
            // e.g. a vPce of a pawn behind the line it cuold ever reach
            return new ConditionalDistance();
        }
        if (rawMinDistance.dist()==0)
            return new ConditionalDistance(1);  // almost nothing is closer than my neighbour
        if (rawMinDistance.dist()==INFINITE_DISTANCE)
            return new ConditionalDistance(INFINITE_DISTANCE);  // can't get further away than infinite...

        // one hop from here is +1 or +2 if this piece first has to move away
        int inc = 1;
        //if (myChessBoard.hasPieceOfColorAt(opponentColor(myPiece().color()), myPos )) {
            // opponent is already there, so pawn can beat it directly
            return new ConditionalDistance(rawMinDistance, inc, ANY,ANY);
        //} //else
        // square is free
        // or one of my same colored pieces is in the way.
        // in both cases this pawn can only go here if an opponent gos to that square (resp. beats that piece)
        //return new ConditionalDistance(rawMinDistance, inc, ANY,myPos);
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
