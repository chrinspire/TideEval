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

import static de.ensel.chessbasics.ChessBasics.*;
import static de.ensel.chessbasics.ChessBasics.ANYWHERE;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
import static java.lang.Math.abs;

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
            ((VirtualPawnPieceOnSquare)(board.getBoardSquare(updatesOpenFromPos).getvPiece(myPceID)))
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
        /*if (getPieceID()==ChessBoard.DEBUGFOCUS_VP) {
            System.err.println("");
            System.err.print("p.recalcAndProp-"+squareName(getMyPos())+":"+(getRawMinDistanceFromPiece()==null?" - ":(getRawMinDistanceFromPiece().isInfinite()?"X":getRawMinDistanceFromPiece().dist()))+": ");
        }*/
        if ( recalcAllPawnDists() || rawMinDistance.dist()==0
               // || rawMinDistance==null  || rawMinDistance.isInfinite()
        ) {
            int quePriority = minDistanceSuggestionTo1HopNeighbour().dist();
            if (quePriority==INFINITE_DISTANCE)
                quePriority=0;  // resets/unreachables must be propagated immediately
            myPiece().quePropagation(
                    quePriority,
                    this::doPropagatePawnDistanceToNeighbours);
        }
    }

    @Override
    protected int recalcRawMinDistanceFromNeighbours() {
        return recalcAllPawnDists() ? 1 : 0;
    }

    /**
     * recalculate the vPiece's (i.e. the square's) distance from its pawn from the 3 or 4 squares
     * it can be reached from and stores it in rawMinDistance.
     * Also calculates MinDistanceSuggestionTo1HopNeighbour and stores it in suggestionTo1HopNeighbour.
     * @return boolean if distance changed
     */
    protected boolean recalcAllPawnDists() {
        // recalc (unless we are 0, i.e. the Piece itself is here at my square)
        /*if (getPieceID() == ChessBoard.DEBUGFOCUS_VP) {
            //System.err.print(", " + squareName(getMyPos()) + ":" + (getRawMinDistanceFromPiece() == null ? "-" : (getRawMinDistanceFromPiece().isInfinite() ? "X" : getRawMinDistanceFromPiece().dist())) + "");
            System.err.print("" + rankOf(getMyPos())+ "");
        }*/
        if (myPos == myPiece().getPos()) { // propagation reached the pawn itself...
            // experimental to solve a pawn update problem...
            if (rawMinDistance != null && rawMinDistance.dist()==0)
                return false;
            setLatestChangeToNow();
            rawMinDistance = new ConditionalDistance(this, 0);
            minDistsDirty();
            getMinDistanceFromPiece();
            suggestionTo1HopNeighbour = new ConditionalDistance( minDistance, +1);
            /*if (getPieceID()==ChessBoard.DEBUGFOCUS_VP)
                System.err.print("P");*/
            return true;
        }
        //if (rawMinDistance!=null && rawMinDistance.dist()==0)
        //    return false;   // assert(false) not possible, because method can be called "behind" a pawn
        ConditionalDistance origSuggestionToNeighbour = suggestionTo1HopNeighbour; // not minDistanceSuggestionTo1HopNeighbour(); because this would recalculate if it is not even calculated yet, leading to old==new value and falsely no propagaion being done

        ConditionalDistance straightSugg2Here = getSquareStraightPawnSuggestionFromPredecessors();  // s
        ConditionalDistance takingSugg2Here = getSquareTakingPawnSuggestionFromPredecessors();      // t
        int moveAwayPenalty = mySquareIsEmpty() ? 0 : mySquarePiece().movingAwayDistPenalty();
        final boolean takingHasNoGo = takingSugg2Here == null || takingSugg2Here.isInfinite() || takingSugg2Here.hasNoGo();
        final boolean straightHasNoGo = straightSugg2Here == null || straightSugg2Here.isInfinite() || straightSugg2Here.hasNoGo();

        // first determine rawMinDistance
        minDistance = null;
        if ( !canPawnOfColorReachPos(color(), getMyPiecePos(), getMyPos()) ) {
            rawMinDistance =  new ConditionalDistance(this);
            suggestionTo1HopNeighbour = new ConditionalDistance(this);
            return !(origSuggestionToNeighbour == null || origSuggestionToNeighbour.isInfinite());
        }
        if ( takingSugg2Here == null || takingSugg2Here.isInfinite() ) {
            if ( straightSugg2Here == null || straightSugg2Here.isInfinite() ) {
                rawMinDistance =  new ConditionalDistance(this);
                suggestionTo1HopNeighbour = new ConditionalDistance(this);
                return !(origSuggestionToNeighbour == null || origSuggestionToNeighbour.isInfinite());
            }
            rawMinDistance = straightSugg2Here;
        }
        else {
            rawMinDistance = takingSugg2Here;
        }
        if (straightSugg2Here != null) {
            if (mySquareIsEmpty()) {
                // take min of t and s
                if (rawMinDistance == null || rawMinDistance.isInfinite()
                        || straightSugg2Here.cdIsSmallerThan(takingSugg2Here)) {
                    rawMinDistance = straightSugg2Here;
                }
            } else {
                if ( takingHasNoGo && !straightHasNoGo ) {
                    // taking has nogo but straight has not, so let's take straight + condition and penalty
                    rawMinDistance = straightSugg2Here.inc(moveAwayPenalty);
                    rawMinDistance.addCondition(myPos, ANYWHERE, mySquarePiece().color());
                    if (!mySquarePiece().canMoveAwayReasonably())
                        rawMinDistance.setNoGo(myPos);
                }
            }
        }

        // now determine minDistance
        getMinDistanceFromPiece();

        // now determine suggestionTo1HopNeighbour
        suggestionTo1HopNeighbour = new ConditionalDistance( minDistance, +1);
        final boolean noGoIsSame = takingHasNoGo == straightHasNoGo;
        if ( !board.hasPieceOfColorAt(myOpponentsColor(), myPos)                    // there was nothing to take,
             && ( (!takingHasNoGo && straightHasNoGo))                              // but only straight has NoGo
                  || (noGoIsSame && takingSugg2Here != null && straightSugg2Here != null // or NoGos are same, but taking is closer
                      && takingSugg2Here.cdIsSmallerThan(straightSugg2Here)) ) { // or NoGos are same, but taking is closer
            suggestionTo1HopNeighbour.addCondition(ANYWHERE, myPos,myOpponentsColor());
            if (!opponentPieceIsLikelyToComeHere())
                suggestionTo1HopNeighbour.setNoGo(myPos);
        }
        else if ( board.hasPieceOfColorAt(color(), myPos)
                    && noGoIsSame && takingSugg2Here != null && straightSugg2Here != null
                    && !takingSugg2Here.cdIsSmallerThan(straightSugg2Here) ) {
            // special case, where nogos are equal and straight is same or closer, but own piece is in the way
            suggestionTo1HopNeighbour.addCondition(ANYWHERE, myPos,myOpponentsColor());
            if (!opponentPieceIsLikelyToComeHere())
                suggestionTo1HopNeighbour.setNoGo(myPos);
            // either taking is quicker, so it is like above with a condition
            // or see if moving straight after moving away penalty is quicker without a condition
            ConditionalDistance straightSugg2 = straightSugg2Here.inc(moveAwayPenalty+1);
            suggestionTo1HopNeighbour.reduceIfCdIsSmaller(straightSugg2);
        }


        if ( // nothing changed in the suggestions, but my own square could have changed a piece, so check minDistance
            origSuggestionToNeighbour != null && origSuggestionToNeighbour.equals(suggestionTo1HopNeighbour)
            //&& origSuggestionToNeighbour.conditionsEqual(newSuggestionToNeighbour)
        ) {
            /*if (getPieceID()==ChessBoard.DEBUGFOCUS_VP)
                System.err.print("=");*/
            return false;
        }
        setLatestChangeToNow();
        /*if (getPieceID()==ChessBoard.DEBUGFOCUS_VP)
            System.err.print("-"+squareName(getMyPos())+"->"+(getRawMinDistanceFromPiece().isInfinite()?"X":getRawMinDistanceFromPiece().dist()));
         */
        return true;
    }

    protected ConditionalDistance getSquareStraightPawnSuggestionFromPredecessors() {
        ConditionalDistance minimum=null;
        if (hasLongPawnPredecessor(myPiece().color(), myPos)) {
            // square might be reachable by two square-move
            int midPos = getLongPawnMoveMidPos(myPiece().color(), myPos);
            int startPos = getLongPawnPredecessorPos(myPiece().color(), myPos);
            assert(midPos>-1);
            assert(startPos>-1);
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) board.getBoardSquare(startPos)
                    .getvPiece(myPceID);
            minimum = new ConditionalDistance( neighbour.minDistanceSuggestionTo1HopNeighbour() );
            if (!minimum.isInfinite()
                    && !board.isSquareEmpty(midPos)) {
                // if middle square on the way is occupied by a piece, this one has to move away, too.
                int midPenalty=board.getPieceAt(midPos).movingAwayDistPenalty();
                // we need to make the suggestion conditional + penalty
                minimum.inc(midPenalty);
                minimum.addCondition(midPos, ANYWHERE, board.getPieceAt(midPos).color());
            }
        }
        int startPos = getSimpleStraightPawnPredecessorPos(myPiece().color(), myPos);
        if (startPos>-1) { // if ==-1, then it is pawn starting position, but as I do not carry the Piece myself, I must be out of reach...
            VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) board.getBoardSquare(startPos)
                    .getvPiece(myPceID);
            ConditionalDistance suggestion = new ConditionalDistance( neighbour.minDistanceSuggestionTo1HopNeighbour() );
            if (minimum==null)
                minimum = new ConditionalDistance(suggestion);
            else
                minimum.reduceIfCdIsSmaller(suggestion);
        }
        return minimum;
    }

    protected ConditionalDistance getSquareTakingPawnSuggestionFromPredecessors() {
        // set the list of relevant predecessors and get minimum of their distance suggestion
        final int[] beatingPredecessorDirs = getBeatingPawnPredecessorDirs(myPiece().color(), rankOf(myPos));
        ConditionalDistance minimum = new ConditionalDistance(this);
        for (int predecessorDir : beatingPredecessorDirs) {
            if (neighbourSquareExistsInDirFromPos(predecessorDir, myPos)) {
                VirtualPawnPieceOnSquare neighbour = (VirtualPawnPieceOnSquare) board
                        .getBoardSquare(myPos+predecessorDir).getvPiece(myPceID);
                ConditionalDistance suggestion = new ConditionalDistance(neighbour.minDistanceSuggestionTo1HopNeighbour());
                minimum.reduceIfCdIsSmaller(suggestion);
            }
        }
        return minimum;
    }

    @Override
    public ConditionalDistance minDistanceSuggestionTo1HopNeighbour() {
        if (rawMinDistance == null) {
            return new ConditionalDistance(this);
        }
        if (suggestionTo1HopNeighbour != null)
            return suggestionTo1HopNeighbour;
        if (rawMinDistance.dist() == 0)
            suggestionTo1HopNeighbour = new ConditionalDistance(this, 1);  // almost nothing is closer than my neighbour  // TODO. check if my piece can move away at all (considering king pins e.g.)
        else if (rawMinDistance.isInfinite())
            suggestionTo1HopNeighbour = new ConditionalDistance(this);
        else {
            /*if (getPieceID()==ChessBoard.DEBUGFOCUS_VP) {
                System.err.println("");
                System.err.print("p.minDistSuggTo1HopN-"+squareName(getMyPos())+":"+(getRawMinDistanceFromPiece()==null?" - ":(getRawMinDistanceFromPiece().isInfinite()?"X":getRawMinDistanceFromPiece().dist()))+": ");
            }*/
            // this can happen after all minDists have been calculated, but minDist and sugg got invalidated e.g. by a setRelEval()
            //recalcAndPropagatePawnDistance();  82d
            recalcAllPawnDists();  //82e
        }
        return suggestionTo1HopNeighbour;
    }

    protected Set<VirtualPieceOnSquare> calcPredecessors() {  // where could it come from
        return getAllPawnPredecessorPositions(color(),myPos).stream()
                .map(p-> board.getBoardSquare(p).getvPiece(myPceID))
                .collect(Collectors.toSet());
    }

    Set<VirtualPieceOnSquare> calcDirectAttackVPcs() {
        return getAllPawnAttackPositions(color(),getMyPos()).stream()
                .map(p-> board.getBoardSquare(p).getvPiece(myPceID))
                .collect(Collectors.toSet());
    }

    private boolean opponentPieceIsLikelyToComeHere() {
        return board.getBoardSquare(myPos).isColorLikelyToComeHere(myOpponentsColor());
    }

    public int furthestLastTakingRankOnTheWayHere() {
        if (getMinDistanceFromPiece().dist() == 0
                || getMinDistanceFromPiece().isInfinite()
                || getMinDistanceFromPiece().hasNoGo())
            return NOWHERE;
        if (board.hasPieceOfColorAt(myOpponentsColor(), getMyPos()))
            return rankOf(getMyPos());
        int fLTR = rankOf(getMyPiecePos());
        for(VirtualPieceOnSquare p : getShortestReasonablePredecessors()) {
            int pfLTR = ((VirtualPawnPieceOnSquare)p).furthestLastTakingRankOnTheWayHere();
            if (pfLTR != NOWHERE)
                fLTR = minFor(pfLTR, fLTR,color());
        }
        if (fLTR == rankOf(getMyPiecePos()))
            return NOWHERE;
        return fLTR;
    }


    /*@Override
    public ConditionalDistance minDistanceSuggestionTo1HopNeighbour() {
        return super.minDistanceSuggestionTo1HopNeighbour();
    }*/

    /**
     * makes corrections of minDistanceSuggestionTo1HopNeighbour (to here)  dependent on if this was a taking pawn move or not.
     * Needed because normally taking needs to assume that a current own piece was taken by opponent or moved away,
     * but for pawn moving away does not help...  in the same way a pawn moving straight is blocked by an opponent,
     * all other pieces are not.
     * @param taking true is the move to come here was taking.
     * @return the distance suggested from here on
     */

    public ConditionalDistance pawnMinDistanceSuggestionCorrection(ConditionalDistance suggestion, boolean taking) {
        if (suggestion == null) {
            return new ConditionalDistance(this);
        }
        if (suggestion.isInfinite()) {
            return suggestion;
        }
        if ( taking && (mySquareIsEmpty()
                        || board.hasPieceOfColorAt(myPiece().color(), myPos))  ) {
            // square has own piece or is empty, but this is a taking move, so:
            suggestion.addCondition(ANYWHERE, myPos, myOpponentsColor() );
            if ( !opponentPieceIsLikelyToComeHere() ) {
                //no opponent will come here, so further ways are nogo
                suggestion.setNoGo(myPos);
                // do not suggestion.inc(); because then covering a square would become d==2, but d==1 is expected.
            }
        }
        else if (!taking && !mySquareIsEmpty()) {
            // because own piece is in the straight way, we can only continue under the condition that it moves away
            int penalty = movingMySquaresPieceAwayDistancePenalty();
            if (penalty < INFINITE_DISTANCE)
                suggestion.inc(penalty);
            else
                suggestion = new ConditionalDistance(this);
            suggestion.addCondition(myPos, ANYWHERE, mySquarePiece().color());
            if (!mySquarePiece().canMoveAwayReasonably()) {
                // the piece at my square has no reasonable move, so further ways are nogo
                suggestion.setNoGo(myPos);
            }
        }
        // else if square is free and moving straight or has piece of opposite color to be beaten, nothing changes
        checkNsetNoGoOrEnablingCondition(suggestion);
        return suggestion;
    }


/*
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


    protected void doPropagatePawnDistanceToNeighbours() {
        final int[] neighbourDirs = getAllPawnDirs(myPiece().color(), rankOf(myPos));
        for (int i = 0; i < neighbourDirs.length; i++) {
            if ( //neighbourUpdated[i]) {  //neighbourSquareExistsInDirFromPos(neighbourDir, myPos)) {
                neighbourSquareExistsInDirFromPos(neighbourDirs[i], myPos)) {
                VirtualPawnPieceOnSquare n = (VirtualPawnPieceOnSquare) board
                        .getBoardSquare(myPos+neighbourDirs[i]).getvPiece(myPceID);
                n.recalcAndPropagatePawnDistance();
            }
        }
    }

    /*
    protected void recalcNeighboursAndPropagatePawnDistance() {
        // if my result changed, do propagation:
        // first tell neighbours to correct their own distance
        final int[] neighbourDirs = getAllPawnDirs(myPiece().color(), rankOf(myPos));
        final boolean[] neighbourUpdated = new boolean[neighbourDirs.length];
        for (int i = 0; i < neighbourDirs.length; i++) {
            if (neighbourSquareExistsInDirFromPos(neighbourDirs[i], myPos)) {
                VirtualPawnPieceOnSquare n = (VirtualPawnPieceOnSquare) board
                        .getBoardSquare(myPos+neighbourDirs[i]).getvPiece(myPceID);
                if (getPieceID()==ChessBoard.DEBUGFOCUS_VP) {
                    System.err.println(";");
                    System.err.println("");
                    System.err.print("p.recalcNeigAndPropPawnD-"+squareName(getMyPos())+":"+(getRawMinDistanceFromPiece()==null?" - ":(getRawMinDistanceFromPiece().isInfinite()?"X":getRawMinDistanceFromPiece().dist()))+": ");
                }
                neighbourUpdated[i] = n.recalcAllPawnDists();
            }
            else
                neighbourUpdated[i] = false;
        }
        // then on that basis start breadth propagation where necessary
        for (int i = 0; i < neighbourDirs.length; i++) {
            if (neighbourUpdated[i]) {  //neighbourSquareExistsInDirFromPos(neighbourDir, myPos)) {
                VirtualPawnPieceOnSquare n = (VirtualPawnPieceOnSquare) board
                        .getBoardSquare(myPos+neighbourDirs[i]).getvPiece(myPceID);
                int quePriority = n.getRawMinDistanceFromPiece().dist();
                if (quePriority==INFINITE_DISTANCE)
                    quePriority=0;  // resets/unreachables must be propagated immediately
                myPiece().quePropagation(
                        quePriority,
                        n::recalcAndPropagatePawnDistance);   // recalcNeighboursAndPropagatePawnDistance); // TODO!!: probably same change necessary as for other vPieces: call this method for this/self, not for neighbour, to work with the correct que priority
            }
        }
    }
*/
    @Override
    public int getValue() {
        //int adv = ( ((NR_RANKS-2)-promotionDistanceForColor( myPos, color() ))
        //        * (pieceBaseValue(getPieceType()))
        //        / NR_RANKS );  // 0-max: 5/8 of PAWN = +/-63   // does not matter much.
        //... + adv;   was worse, see v0.48h5 vs better h4 without

        int val = pieceBaseValue(getPieceType());

        final int fileDelta = abs(fileOf(getMyPos()) - fileOf(getMyPiecePos()));

        /*// motivating pawns to move forward, esp in endgames
        final int nrOfPiece = board.getPieceCounter();
        if (nrOfPiece < 21 && !rmd.hasNoGo()) {
            int forwardBenefit = (24 - nrOfPiece) >> 2;
            if (isBlack(vPce.color()))
                forwardBenefit = -forwardBenefit;
            if (DEBUGMSG_MOVEEVAL && abs(forwardBenefit) > 4)
                debugPrintln(DEBUGMSG_MOVEEVAL, " " + forwardBenefit + "@0 benefit for " + (isBeating ? "beating with" : "advancing") + " pawn to " + squareName(myPos) + ".");
            vPce.addChance(forwardBenefit, 0);
        }*/

        // avoid doubling pawns (when beating)
        int otherOwnPawns = board.getPawnCounterForColorInFileOfPos(color(), getMyPos() );
        if ( fileDelta>0 && otherOwnPawns > 0 )  {
            int doublePawnFee = EVAL_TENTH - (EVAL_TENTH >> 2) + (otherOwnPawns>1 ? (EVAL_TENTH>>1) : 0) ;
            if (isWhite(color()))
                doublePawnFee = -doublePawnFee;
            val += doublePawnFee;
        }

        // motivate to become a passed pawn (when beating) if possible
        if ( board.getPawnCounterForColorInFileOfPos(opponentColor(color()), getMyPos() ) == 0
                && (isFirstFile(getMyPos())
                    || board.getPawnCounterForColorInFileOfPos(opponentColor(color()), getMyPos()+LEFT ) == 0 )
                && (isLastFile(getMyPos())
                    || board.getPawnCounterForColorInFileOfPos(opponentColor(color()), getMyPos()+RIGHT ) == 0 )
        ) {
            // todo: needs to check if the there are opponent's pawns at the side, but still I am a passed pawn, because they are behind me...
            int passedPawnBenefit = EVAL_TENTH >> 1;
            if (isBlack(color()))
                passedPawnBenefit = -passedPawnBenefit;
            val += passedPawnBenefit;
        }

        return val;
    }

    public boolean lastPawnMoveIsStraight() {
        for (VirtualPieceOnSquare lmo : getRawMinDistanceFromPiece().getLastMoveOrigins()) {
            if ( fileOf(lmo.getMyPos()) != fileOf(getMyPos()) )
                return false;
        }
        return true;
    }

    public boolean isLastPawnMoveTakingFromReasonableOrigin() {
        for (VirtualPieceOnSquare lmo : getRawMinDistanceFromPiece().getLastMoveOrigins()) {
            if (fileOf(lmo.getMyPos()) != fileOf(getMyPos())
                 && lmo.getMinDistanceFromPiece().hasNoGo() )
                return false;
        }
        return true;
    }

    @Override
    boolean isStraightMovingPawn(final int fromPos) {
        return isPawn(getPieceType())              // pawn?
                && fileOf(getMyPos()) == fileOf(fromPos)   // straight?
                && getMyPos() != fromPos;                  // moving?
    }
}
