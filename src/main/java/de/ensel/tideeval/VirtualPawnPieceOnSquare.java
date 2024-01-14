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

import java.util.Set;
import java.util.stream.Collectors;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBasics.ANYWHERE;
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
        if (suggestedDistance.dist()==0 || suggestedDistance.isInfinite() )
            updateRawMinDistanceFrom(suggestedDistance);

        // do not start here, but where the pawn came from i.e.
        if (updatesOpenFromPos>-1)
            ((VirtualPawnPieceOnSquare)(board.getBoardSquares()[updatesOpenFromPos].getvPiece(myPceID)))
                .setAndPropagateDistance(new ConditionalDistance(this));
        else  // unless it is a new piece
            recalcAndPropagatePawnDistance();
        updatesOpenFromPos = -1;
    }

    @Override
    protected void quePropagateDistanceChangeToAllOneHopNeighbours() {
        ////recalcNeighboursAndPropagatePawnDistance();
        //// why was this here instead?  this lead to double-recalc and no propagation...: recalcAndPropagatePawnDistance();
        // answer: then another piece arrives, first the resetBomb is called, then this here. So first there needs to be a recalc before propagation...
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
        if (myPos==myPiece().getPos()) { // propagation reached the pawn itself...
            // experimental to solve a pawn update problem...
                setLatestChangeToNow();
                rawMinDistance = new ConditionalDistance(this, 0);
                minDistsDirty();
            return true;
        }
        if (rawMinDistance!=null && rawMinDistance.dist()==0)
            return false;   // assert(false) not possible, because method can be called "behind" a pawn
        ConditionalDistance origSuggestionToNeighbour = minDistanceSuggestionTo1HopNeighbour();
        ConditionalDistance minimum = recalcSquareStraightPawnDistance();
        if (minimum==null || minimum.isInfinite())
            minimum = recalcSquareBeatingPawnDistance();
        else
            minimum.reduceIfCdIsSmaller( recalcSquareBeatingPawnDistance() );
        rawMinDistance = minimum;
        suggestionTo1HopNeighbour = null; // otherwise it would not recalculate it.
        ConditionalDistance newSuggestionToNeighbour = minDistanceSuggestionTo1HopNeighbour();
        if ( // nothing changed in the suggestions, but my own square could have changed a piece, so check minDistance
            origSuggestionToNeighbour.equals(newSuggestionToNeighbour)
            //&& origSuggestionToNeighbour.conditionsEqual(newSuggestionToNeighbour)
        ) {
            return false;
        }
        setLatestChangeToNow();
        minDistsDirty();
        suggestionTo1HopNeighbour = newSuggestionToNeighbour;  // we resetted too much in the last line, but here we restore the already known value...
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
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) board.getBoardSquares()[startPos]
                    .getvPiece(myPceID);
            minimum = new ConditionalDistance( neighbour.minDistanceSuggestionTo1HopNeighbour() );
            if (!minimum.isInfinite()
                    && !board.isSquareEmpty(midPos)) {
                // if middle square on the way is occupied by a piece, this one has to move away, too.
                int midPenalty=1;   // TODO(same): evaluate real costs of moving away (it might also not be possible)
                // we need to make the suggestion conditional + penalty
                minimum.inc(midPenalty);
                minimum.addCondition(midPos, ANYWHERE, board.getPieceAt(midPos).color());
            }
        }
        int startPos = getSimpleStraightPawnPredecessorPos(myPiece().color(), myPos);
        if (startPos>-1) { // if ==-1, then it is pawn starting position, but as I do not carry the Piece myself, I must be out of reach...
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) board.getBoardSquares()[startPos]
                    .getvPiece(myPceID);
            ConditionalDistance suggestion = neighbour.minDistanceSuggestionTo1HopNeighbour();
            if (minimum==null)
                minimum = new ConditionalDistance(suggestion);
            else
                minimum.reduceIfCdIsSmaller(suggestion);
        }
        if (minimum==null)
            return null;
        if (minimum.isInfinite())
            return minimum;
        boolean opponentColor = myOpponentsColor();
        if (board.hasPieceOfColorAt(opponentColor, myPos )) {
            // opponent is in the way, it needs to move away first
            // TODO: Check if dist needs to inc, if opponent has to move away.
            minimum.addCondition(myPos, ANYWHERE,opponentColor);
            minimum.inc();
        }
        else if (board.hasPieceOfColorAt(myPiece().color(), myPos )) {
            // my own colored piece is in the way, it needs to move away first
            int penalty = movingMySquaresPieceAwayDistancePenalty();
            if (penalty==INFINITE_DISTANCE)
                return new ConditionalDistance(this);
            minimum.addCondition(myPos, ANYWHERE, myPiece().color());
            minimum.inc(1 + penalty);
        }
        return minimum;
    }

    protected ConditionalDistance recalcSquareBeatingPawnDistance() {
        // set the list of relevant predecessors and get minimum of their distance suggestion
        final int[] beatingPredecessorDirs = getBeatingPawnPredecessorDirs(myPiece().color(), rankOf(myPos));
        ConditionalDistance minimum = getMinimumBeatingSuggestionOfPredecessors(beatingPredecessorDirs);
        if (board.hasPieceOfColorAt(myOpponentsColor(), myPos )) {
            // opponent is in the way, this is great, so it can be beaten...
            // nothing else to do
        }
        else {
            boolean ownPieceIsInTheWay = board.hasPieceOfColorAt(myPiece().color(), myPos );
            if ( ownPieceIsInTheWay  // if my own piece is in the way, it needs to be beaten by an opponent first
                || mySquareIsEmpty()    // or square is empty, so an opponent needs to move here first
            ){
                // similar to sliding pieces that need to move out of the way, here a piece has to come here.
                // do not count the first opponent coming to be beaten as distance, but later do count (this is not very precise...)
                if (!minimum.isUnconditional())
                    minimum.inc();
                minimum.addCondition(ANYWHERE, myPos, myOpponentsColor());
                if (!ownPieceIsInTheWay && !opponentPieceIsLikelyToComeHere())
                    minimum.setNoGo(myPos);  // should/could set to infinite, but still if it cannot go there it covers it  -so let's handle it with the NoGo flag
            }
        }
        return minimum;
    }

    protected Set<VirtualPieceOnSquare> calcPredecessors() {  // where could it come from
        return getAllPawnPredecessorPositions(color(),myPos).stream()
                .map(p-> board.getBoardSquare(p).getvPiece(myPceID))
                .collect(Collectors.toSet());
    }


    private boolean opponentPieceIsLikelyToComeHere() {
        return board.getBoardSquare(myPos).isColorLikelyToComeHere(myOpponentsColor());
    }

    protected ConditionalDistance getMinimumBeatingSuggestionOfPredecessors(int[] predecessorDirs) {
        //TODO-low: check if this can be reused on super-class level
        ConditionalDistance minimum = new ConditionalDistance(this);
        for (int predecessorDir : predecessorDirs) {
            if (neighbourSquareExistsInDirFromPos(predecessorDir, myPos)) {
                VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) board
                        .getBoardSquare(myPos+predecessorDir).getvPiece(myPceID);
                ConditionalDistance suggestion = neighbour.minDistanceSuggestionTo1HopNeighbour();
                minimum.reduceIfCdIsSmaller(suggestion);
            }
        }
        return minimum;
    }


    /*
     * tells the distance after moving away from here,
     * careful: othen than for other pieces, the method does not consider if a Piece is in the way here,
     * because it cannot know here where the piece came from: from a beating or a moving position...
     * @return a "safe"=new ConditionalDistance
     */
/*        {
    @Override
    public ConditionalDistance minDistanceSuggestionTo1HopNeighbour()
            // Todo: Increase 1 more if Piece is pinned to the king
            if (rawMinDistance==null) {
                // should normally not happen, but in can be the case for completely unset squares
                // e.g. a vPce of a pawn behind the line it could ever reach
                return new ConditionalDistance();
            }

            if (rawMinDistance.dist()==0)
                return new ConditionalDistance(1);  // almost nothing is closer than my neighbour  // TODO. check if my piece can move away at all (considering king pins e.g.)
            if (rawMinDistance.dist()==INFINITE_DISTANCE)
                return new ConditionalDistance(INFINITE_DISTANCE);  // can't get further away than infinite...

            // TODO: doesn't work yet, because breadth propagation calls are already qued after the relEval is calculated
            int inc = 0; //(getRelEval()==0 || getRelEval()==NOT_EVALUATED) ? 0 : MAX_INTERESTING_NROF_HOPS;

            // one hop from here is +1 or +2 if this piece first has to move away
            inc += 1;
            ConditionalDistance suggestion = new ConditionalDistance(rawMinDistance, inc);

            //TODO!!: the following code does the NoGo-propagation from this square - should be correct, but leads to a mch larger number of interrupted test games, due to illegal moves.
            // e.g. 1. d4(d2d4) e6(e7e6) 2. c4(c2c4) c6(c7c6) 3. e4(e2e4) Nf6?(g8f6) 4. e5(e4e5)**** Fehler: Fehlerhafter Zug: e4 -> e5 nicht möglich auf Board Testboard
            // same error does not occur without these two lines:
            if ( !evalIsOkForColByMin( getRelEval(), myPiece().color(),EVAL_TENTH ) )
                suggestion.setNoGo(myPos);

            return suggestion;

            //if (myChessBoard.hasPieceOfColorAt(myOpponentsColor(), myPos )) {
            // opponent is already there, so pawn can beat it directly
            //    return new ConditionalDistance(rawMinDistance, inc, ANY,ANY);
            //} //else
            // square is free
            // or one of my same colored pieces is in the way.
            // in both cases this pawn can only go here if an opponent gos to that square (resp. beats that piece)
            //return new ConditionalDistance(rawMinDistance, inc, ANY,myPos);
        }
    }


    public ConditionalDistance minDistanceSuggestionToBeatingNeighbour() {
        // Todo: Increase 1 more if Piece is pinned to the king
        if (rawMinDistance==null) {
            return new ConditionalDistance(this);
        }
        if (suggestionTo1HopNeighbour!=null)
            return suggestionTo1HopNeighbour;

        if (rawMinDistance.dist()==0)
            suggestionTo1HopNeighbour = new ConditionalDistance(this,1);  // almost nothing is closer than my neighbour  // TODO. check if my piece can move away at all (considering king pins e.g.)
        else {
            int inc = 0;

            if (rawMinDistance.isInfinite())
                suggestionTo1HopNeighbour = new ConditionalDistance(this);  // can't get further away than infinite...

            else if (board.hasPieceOfColorAt(myPiece().color(), myPos)) {
                // one of my same colored pieces are in the way
                int penalty = movingMySquaresPieceAwayDistancePenalty();
                if (penalty<INFINITE_DISTANCE && opponentPieceIsLikelyToComeHere()) {
                    inc += penalty + 1;
                    suggestionTo1HopNeighbour = new ConditionalDistance(this, rawMinDistance, inc, myPos, ANY, myPiece().color());
                } else
                    suggestionTo1HopNeighbour = new ConditionalDistance(this);
                // because own piece is in the way, we can only continue under the condition that it moves away
            } else {
                // square is free (or of opposite color and to be beaten)
                inc += 1; // so finally here return the "normal" case -> "my own Distance + 1"
                suggestionTo1HopNeighbour = new ConditionalDistance( this, rawMinDistance, inc);
                if (!evalIsOkForColByMin(getRelEvalOrZero(), myPiece().color()))
                    suggestionTo1HopNeighbour.setNoGo(myPos);
            }
        }
        return suggestionTo1HopNeighbour;
    }
*/

    protected void recalcNeighboursAndPropagatePawnDistance() {
        // if my result changed, do propagation:
        // first tell neighbours to correct their own distance
        final int[] neighbourDirs = getAllPawnDirs(myPiece().color(), rankOf(myPos));
        final boolean[] neighbourUpdated = new boolean[neighbourDirs.length];
        for (int i = 0; i < neighbourDirs.length; i++) {
            if (neighbourSquareExistsInDirFromPos(neighbourDirs[i], myPos)) {
                VirtualPawnPieceOnSquare n = (VirtualPawnPieceOnSquare) board
                        .getBoardSquares()[myPos+neighbourDirs[i]].getvPiece(myPceID);
                neighbourUpdated[i] = n.recalcSquarePawnDistance();
            }
            else
                neighbourUpdated[i] = false;
        }
        // then on that basis start breadth propagation where necessary
        for (int i = 0; i < neighbourDirs.length; i++) {
            if (neighbourUpdated[i]) {  //neighbourSquareExistsInDirFromPos(neighbourDir, myPos)) {
                VirtualPawnPieceOnSquare n = (VirtualPawnPieceOnSquare) board
                        .getBoardSquares()[myPos+neighbourDirs[i]].getvPiece(myPceID);
                int quePriority = n.getRawMinDistanceFromPiece().dist();
                if (quePriority==INFINITE_DISTANCE)
                    quePriority=0;  // resets/unreachables must be propagated immediately
                myPiece().quePropagation(
                        quePriority,
                        n::recalcNeighboursAndPropagatePawnDistance); // TODO!!: probably same change necessary as for other vPieces: call this method for this/self, not for neighbour, to work with the correct que priority
            }
        }
    }

    @Override
    public int getValue() {
        //int adv = ( ((NR_RANKS-2)-promotionDistanceForColor( myPos, color() ))
        //        * (pieceBaseValue(getPieceType()))
        //        / NR_RANKS );  // 0-max: 5/8 of PAWN = +/-63   // does not matter much.
        //... + adv;   was worse, see v0.48h5 vs better h4 without
        return pieceBaseValue(getPieceType());
    }

    public boolean lastMoveIsStraight() {
        for ( VirtualPieceOnSquare lmo : getRawMinDistanceFromPiece().getLastMoveOrigins() ) {
            if ( fileOf(lmo.getMyPos()) != fileOf(getMyPos()) )
                return false;
        }
        return true;
    }
}
