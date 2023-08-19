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

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ChessBasics.ANY;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
import static java.lang.Math.*;

public abstract class VirtualPieceOnSquare implements Comparable<VirtualPieceOnSquare> {
    protected final ChessBoard board;
    protected final int myPceID;
    protected final int myPceType;
    protected final int myPos;

    private int relEval;  // is in board perspective like all evals! (not relative to the color, just relative as seen from the one piece)
    private int relClashContrib;  // tells if Piece is needed in Clash or other benefit. relEval can be 0, but still has a contribution. if Pieves moved away instead, it would miss this contribution.

    protected ConditionalDistance rawMinDistance;   // distance in hops from corresponding real piece.
                                                    // it does not take into account if this piece is in the way of another of the same color
    protected ConditionalDistance minDistance;  // == null if "dirty" (after change of rawMinDistance) other ==rawMinDistance oder +1/+n, if same color Piece is on square
    protected ConditionalDistance suggestionTo1HopNeighbour;  // what would be suggested to a "1-hop-neighbour",
                                                // this is also ==null if "dirty" and used for all types of pieces, even sliding
    /**
     * "timestamp" when the rawMinDistance of this vPce was changed the last "time" (see ChessBoard: boardmoves+fineTicks)
     */
    protected long latestChange;

    /**
     * Array of "future levels" for HashMap collecting "first moves to here" creating a chance on my square on that "future level"
     */
    private List<HashMap<Move,Integer>> chances;

    private boolean isCheckGiving;

    private Set<VirtualPieceOnSquare> predecessors;
    private Set<VirtualPieceOnSquare> shortestReasonableUnconditionedPredecessors;
    private Set<Move> firstMovesWithReasonableShortestWayToHere;
    private int mobilityFromHere;    // a value, somehow summing mobilty up
    private int mobilityMapFromHere; // a 64-bitmap, one bit for each square


    public VirtualPieceOnSquare(ChessBoard myChessBoard, int newPceID, int pceType, int myPos) {
        this.board = myChessBoard;
        this.myPceType = pceType;
        this.myPos = myPos;
        myPceID = newPceID;
        latestChange = 0;
        //valueInDir = new int[MAXMAINDIRS];
        resetDistances();
        //resetValues();
        relEval = NOT_EVALUATED;
        relClashContrib = NOT_EVALUATED;
        resetChances();
    }

    public static VirtualPieceOnSquare generateNew(ChessBoard myChessBoard, int newPceID, int myPos) {
        int pceType = myChessBoard.getPiece(newPceID).getPieceType();
        if (isSlidingPieceType(pceType))
            return new VirtualSlidingPieceOnSquare(myChessBoard,newPceID, pceType, myPos);
        if (isPawn(pceType))
            return new VirtualPawnPieceOnSquare(myChessBoard,newPceID, pceType, myPos);
        return new VirtualOneHopPieceOnSquare(myChessBoard,newPceID, pceType, myPos);
    }

    boolean canCoverFromSavePlace() {
        boolean canCoverFromSavePlace = false;
        for (VirtualPieceOnSquare attackerAtLMO : getShortestReasonableUnconditionedPredecessors()) {
            if (attackerAtLMO == null || attackerAtLMO.getMinDistanceFromPiece().hasNoGo() )
                continue;
            if (attackerAtLMO.isASavePlaceToStay()) {
                canCoverFromSavePlace = true;
                debugPrint(DEBUGMSG_MOVEEVAL,"(save covering possible on " + squareName(attackerAtLMO.myPos) + ":) ");
                break;
            }
        }
        return canCoverFromSavePlace;
    }


    //////
    ////// general Piece/moving related methods



    /**
     * Where can a Piece go from here?
     * Similar to getPredecessorNeighbours(), but in the forward direction.
     * i.e. result is even identical for 1-hop-pieces, but for pawns completely the opposite...
     * For sliding pieces it only returns the direct sliding neighbours, not anything along the axis beyond the direct neighbour.
     * @return List of vPces that this vPce can reach.
     */
    protected abstract List<VirtualPieceOnSquare> getNeighbours();

    /**
     * where could my Piece come from? (incl. all options, even via NoGo)
     * For sliding pieces: does however not return all squares along the axes, just the squares from every direction
     * that provide the shortest way to come from that direction.
     * @return List of vPces (squares so to speak) that this vPce can come from
     */
    Set<VirtualPieceOnSquare> getPredecessors() {
        if (predecessors!=null)
            return predecessors;   // be aware, this is not a cache, it would cache to early, before distance calc is finished!
        return calcPredecessors();
    }

    abstract Set<VirtualPieceOnSquare> calcPredecessors();

    void rememberAllPredecessors() {
        predecessors = calcPredecessors();
        shortestReasonableUnconditionedPredecessors = calcShortestReasonableUnconditionedPredecessors();
        firstMovesWithReasonableShortestWayToHere = calcFirstMovesWithReasonableShortestWayToHere();
    }

    /**
     * Subset of getPredecessorNeighbours(), with only those predecessors that can reasonably be reached by the Piece
     * and where there is no condition possibly avoiding the last move.
     * @return List of vPces that this vPce can come from.
     */
    Set<VirtualPieceOnSquare> getShortestReasonableUnconditionedPredecessors() {
        if (shortestReasonableUnconditionedPredecessors!=null)
            return shortestReasonableUnconditionedPredecessors;   // be aware, this is not a cache, it would cache to early, before distance calc is finished!
        return calcShortestReasonableUnconditionedPredecessors();
    }



    abstract Set<VirtualPieceOnSquare> calcShortestReasonableUnconditionedPredecessors();

    /**
     * calc which 1st moves of my piece lead to here (on shortest ways) - obeying NoGos
     * @return */
    public Set<Move> getFirstMovesWithReasonableShortestWayToHere() {
        if (firstMovesWithReasonableShortestWayToHere !=null)
            return firstMovesWithReasonableShortestWayToHere;
        return calcFirstMovesWithReasonableShortestWayToHere();
    }

    /**
     * calc which 1st moves of my piece lead to here (on shortest ways) - obeying NoGos
     * @return */
    public Set<Move> calcFirstMovesWithReasonableShortestWayToHere() {
        final boolean localDebug = false; //DEBUGMSG_MOVEEVAL;
        debugPrint(localDebug, "getFirstMoveto:"+this.toString() + ": ");
        if (!getRawMinDistanceFromPiece().distIsNormal()) {
            return new HashSet<>();
        }
        Set<Move> res = new HashSet<>(8);
        if ( getRawMinDistanceFromPiece().dist()==1
                && !getRawMinDistanceFromPiece().hasNoGo() //!getMinDistanceFromPiece().hasNoGo()
              /*  || ( getRawMinDistanceFromPiece().dist()==2
                      && getRawMinDistanceFromPiece().nrOfConditions()==1) */ ) {
            res.add(new Move(myPiece().getPos(), myPos));  // a first "clean" move found
            if (localDebug)
                debugPrintln(localDebug, " found 1st move from "+ squareName(myPiece().getPos())
                    + " to " + squareName(myPos) + ": ");
        }
        else {
            // recursion necessary
            if (localDebug)
                debugPrintln(localDebug, " recursing down to " +
                    Arrays.toString(getShortestReasonableUnconditionedPredecessors()
                            .stream()
                            .map(vPce -> squareName(vPce.myPos))
                            .sorted(Comparator.naturalOrder())
                            .collect(Collectors.toList()).toArray()));
            for ( VirtualPieceOnSquare vPce : getShortestReasonableUnconditionedPredecessors() )  // getPredecessors() ) //
                if ( vPce!=this ){
                    Set<Move> firstMovesToHere = vPce.getFirstMovesWithReasonableShortestWayToHere();
                    res.addAll(firstMovesToHere );
                }
        }
        return res;
    }

    private Set<Move> getMoveOrigin(VirtualPieceOnSquare vPce) {
        Set<Move> firstMovesToHere = vPce.getFirstMovesWithReasonableShortestWayToHere();
        if (firstMovesToHere==null) {
            firstMovesToHere = new HashSet<>();
            if ( rawMinDistance.dist()==1
                    || ( rawMinDistance.dist()==2 && !(rawMinDistance.nrOfConditions()==1) ) )
                firstMovesToHere.add(new Move(myPiece().getPos(), myPos));  // a first "clean" move found
        }
        return firstMovesToHere;
    }

    /**
     * calc which 1st moves of my piece lead to here (on shortest ways) - obeying NoGos
     * @return */
    public Set<Move> getFirstUncondMovesToHere() {
        //debugPrintln(true, "getFirstMoveto:"+this.toString() );
        switch (getRawMinDistanceFromPiece().dist()) {
            case 0 -> {
                return null;
            }
            case INFINITE_DISTANCE -> {
                return new HashSet<>();
            }
        }
        Set<Move> res = new HashSet<>(8);
        for ( VirtualPieceOnSquare vPce : getShortestReasonableUnconditionedPredecessors() ) {
            res.addAll(getUncondMoveOrigin(vPce));
        }
        return res;
    }

    private Set<Move> getUncondMoveOrigin(VirtualPieceOnSquare vPce) {
        /*if (vPce==null) {
            Set<Move> s = new HashSet<>();
            s.add(new Move(myPiece().getPos(), myPos));  // a first move found
            return s;
        }*/
        Set<Move> res = vPce.getFirstUncondMovesToHere();
        if (res==null) {
            res = new HashSet<>();
            if ( rawMinDistance.nrOfConditions()==0 && rawMinDistance.dist()==1 )
                res.add(new Move(myPiece().getPos(), myPos));  // a first "clean" move found
            // otherwise it is a conditional move found and cannot be directly moved.
        }
        return res;
    }



    //////
    ////// handling of Distances

    /** update methods for rawMinDistance - should never be set directly (as usual :-)
     * @param baseDistance copies the values from that CD
     */
    protected void updateRawMinDistanceFrom(ConditionalDistance baseDistance) {
        rawMinDistance.updateFrom(baseDistance);
        minDistsDirty();
        setLatestChangeToNow();
    }

    /**
     * just the same as the setter updateRMD(), but only updates if the given value is smaller
     *
     * @param baseDistance the value to compare and copy if smaller
     * @return boolean whether  value has changed
     */
    protected boolean reduceRawMinDistanceIfCdIsSmaller(ConditionalDistance baseDistance) {
        if (rawMinDistance.reduceIfCdIsSmaller(baseDistance)) {
            minDistsDirty();
            setLatestChangeToNow();
            return true;
        }
        return false;
    }

    public void pieceHasArrivedHere(int pid) {
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION,"");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," ["+myPceID+":" );
        setLatestChangeToNow();
        // inform neighbours that something has arrived here
        board.getPiece(myPceID).startNextUpdate();
        if (pid==myPceID) {
            //my own Piece is here - but I was already told and distance set to 0
            assert (rawMinDistance.dist()==0);
            return;
        }
        // here I should update my own minDistance - necessary for same colored pieces that I am in the way now,
        // but this is not necessary as minDistance is safed "raw"ly without this influence and later it is calculated on top, if it is made "dirty"==null .
        // reset values from this square onward (away from piece)
        resetDistances();
        /* already overridden: if (colorlessPieceType(myPceType)==BISHOP || colorlessPieceType(myPceType)==ROOK
                || colorlessPieceType(myPceType)==QUEEN )  // todo: - not nice here...
            ((VirtualSlidingPieceOnSquare)this).resetSlidingDistances(); */
        propagateResetIfUSWToAllNeighbours();
        // start propagation of new values
        quePropagateDistanceChangeToAllNeighbours();   //0, Integer.MAX_VALUE );
        /* ** experimenting with breadth search propagation ** */
        // no experimental feature any more, needed for pawns (and empty lists for others)
        // if (FEATURE_TRY_BREADTHSEARCH) {
            // continue one propagation by one until no more work is left.
        // distance propagation is not executed here any more any, but centrally hop-wise for all pieces
            /*int n = 0;
            while (myPiece().queCallNext())
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, " " + (n++));
            debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, " done: " + n);*/
        //}
        /*debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," // and complete the propagation for 2+: ");
        latestUpdate = myChessBoard.getPiece(myPceID).startNextUpdate();
        propagateDistanceChangeToOutdatedNeighbours(2, Integer.MAX_VALUE );
        */
        board.getPiece(myPceID).endUpdate();

        // TODO: Think&Check if this also works, if a piece was taken here
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"] ");
    }

    public void pieceHasMovedAway() {
        // inform neighbours that something has changed here
        // start propagation
        minDistsDirty();
        //setLatestChangeToNow();
        board.getPiece(myPceID).startNextUpdate();  //todo: think if startNextUpdate needs to be called one level higher, since introduction of board-wide hop-wise distance calculation
        if (rawMinDistance!=null && !rawMinDistance.isInfinite())
           quePropagateDistanceChangeToAllNeighbours(); // 0, Integer.MAX_VALUE);
        board.getPiece(myPceID).endUpdate();  // todo: endUpdate necessary?
    }

    // fully set up initial distance from this vPces position
    public void myOwnPieceHasSpawnedHere() {  //replaces myOwnPieceHasMovedHereFrom(int frompos) for spawn case. the normal case is replaced by orhestration viw chessPiece
        // one extra piece
        // treated just like sliding neighbour, but with no matching "from"-direction
        if (DEBUGMSG_DISTANCE_PROPAGATION) {
            debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, "[" + pieceColorAndName(board.getPiece(myPceID).getPieceType())
                    + "(" + myPceID + "): propagate own distance: ");
        }
        board.getPiece(myPceID).startNextUpdate();
        rawMinDistance = new ConditionalDistance(this,0);  //needed to stop the reset-bombs below at least here
        minDistsDirty();
        //initializeLocalMovenet(null);
        setAndPropagateDistance(new ConditionalDistance(this,0));  // , 0, Integer.MAX_VALUE );
        board.getPiece(myPceID).endUpdate();
    }

//    private void initializeLocalMovenet(VirtualPieceOnSquare closerNeighbour) {
//        if (closerNeighbour==null) {
            // I own/am the real piece
//            movenetCachedDistance = new MovenetDistance(new ConditionalDistance(0) );
//        }
        /* else {
            movenetCachedDistance = new MovenetDistance(new ConditionalDistance(
                            closerNeighbour.movenetDistance().movenetDist(),
                            1 ));
        } */
//        movenetNeighbours = getNeighbours();
//    }

    /**
     *  fully set up initial distance from this vPces position
     * @param frompos position where piece comes from.
     */
    public void myOwnPieceHasMovedHereFrom(int frompos) {
        assert(frompos!=NOWHERE);
        // a piece moved  (around the corner or for non-sliding neighbours
        // treated just like sliding neighbour, but with no matching "from"-direction
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
        if (DEBUGMSG_DISTANCE_PROPAGATION)
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, "[" + pieceColorAndName(board.getPiece(myPceID).getPieceType())
                + "(" + myPceID + "): propagate own distance: ");

        board.getPiece(myPceID).startNextUpdate();
        rawMinDistance = new ConditionalDistance(this,0);  //needed to stop the reset-bombs below at least here
        minDistsDirty();
        resetMovepathBackTo(frompos);
        //TODO: the currently necessary reset starting from the frompos is very costly. Try
        // to replace it with propagaten that is able to correct dist values in both directions
        VirtualPieceOnSquare vPceAtFrompos = board.getBoardSquare(frompos).getvPiece(myPceID);
        vPceAtFrompos.resetDistances();
        vPceAtFrompos.propagateResetIfUSWToAllNeighbours();
        setAndPropagateDistance(new ConditionalDistance(this,0));  // , 0, Integer.MAX_VALUE );

        board.getPiece(myPceID).endUpdate();
    }


    protected void recalcRawMinDistanceFromNeighboursAndPropagate() {
        if ( recalcRawMinDistanceFromNeighbours()!=0 )
            quePropagateDistanceChangeToAllNeighbours();   // Todo!: recalcs twice, because this propagate turns into a recalcAndPropagate for Pawns... must be revised
        else
            quePropagateDistanceChangeToUninformedNeighbours();
    }


    protected void resetMovepathBackTo(int frompos) {
        // most Pieces do nothing special here
    }

    public String getShortestInPathDirDescription() {
        return TEXTBASICS_NOTSET;
    }

    protected void resetDistances() {
        setLatestChangeToNow();
        if (rawMinDistance==null)
            rawMinDistance = new ConditionalDistance(this);
        else
            rawMinDistance.reset();
        minDistsDirty();
        resetChances();
    }

    protected void minDistsDirty() {
        minDistance = null;
        suggestionTo1HopNeighbour = null;
        // TODO: check idea:
        //  if ("dist"==1)
        //      myPiece().bestMoveRelEvalDirty();
    }

    protected abstract void quePropagateDistanceChangeToAllNeighbours();

    protected abstract void quePropagateDistanceChangeToUninformedNeighbours();

    // not needed on higher level:  protected abstract void propagateDistanceChangeToOutdatedNeighbours();  //final int minDist, final int maxDist);

    // set up initial distance from this vPces position - restricted to distance depth change
    public abstract void setAndPropagateDistance(final ConditionalDistance distance);  //, final int minDist, final int maxDist );

    protected abstract int recalcRawMinDistanceFromNeighbours();

    protected abstract void propagateResetIfUSWToAllNeighbours();

    /**
     * myPiece()
     * @return backward reference to my corresponding real piece on the Board
     */
    public ChessPiece myPiece() {
        return board.getPiece(myPceID);
    }

    /**
     * mySquarePiece()
     * @return reference to the piece sitting on my square, or null if empty
     */
    ChessPiece mySquarePiece() {
        return board.getPieceAt(myPos);
    }

    boolean mySquareIsEmpty() {
        return board.isSquareEmpty(myPos);
    }

    // setup basic neighbourhood network
    public void addSingleNeighbour(VirtualPieceOnSquare newVPiece) {
        ((VirtualOneHopPieceOnSquare)this).addSingleNeighbour( (VirtualOneHopPieceOnSquare)newVPiece );
    }

    public void addSlidingNeighbour(VirtualPieceOnSquare neighbourPce, int direction) {
        ((VirtualSlidingPieceOnSquare)this).addSlidingNeighbour( (VirtualSlidingPieceOnSquare)neighbourPce, direction );
    }

    /**
     * tells the distance after moving away from here, considering if a Piece is in the way here
     * @return a "safe"=new ConditionalDistance
     */
/* experimental change, but brought bugs:
   public ConditionalDistance minDistanceSuggestionTo1HopNeighbour() {
        // Todo: Increase 1 more if Piece is pinned to the king
        if (rawMinDistance==null) {
            // should normally not happen, but in can be the case for completely unset squares
            // e.g. a vPce of a pawn behind the line it could ever reach
            return new ConditionalDistance();
        }

        if (suggestionTo1HopNeighbour!=null)  // good case: it's already calculated
            return suggestionTo1HopNeighbour;
        if (rawMinDistance.isInfinite())
            suggestionTo1HopNeighbour = new ConditionalDistance(INFINITE_DISTANCE);  // can't get further away than infinite...

        // TODO: the following increment doesn't work yet, because breadth propagation calls are already qued after the relEval is calculated
        //(getRelEval()==0 || getRelEval()==NOT_EVALUATED) ? 0 : MAX_INTERESTING_NROF_HOPS-1;

        // standard case: neighbour is one hop from here is
        suggestionTo1HopNeighbour = new ConditionalDistance(
                rawMinDistance,
                1,
                myPiece(),
                myPos,
                 ANY //to here unknown neighbour
                 );

        // TODO. check if my piece can move away at all (considering king pins e.g.)
        if (rawMinDistance.dist()==0) // that it here, as almost nothing is closer than my neighbour
            return suggestionTo1HopNeighbour;

        if (myChessBoard.hasPieceOfColorAt(myPiece().color(), myPos)) {
            // one of my same colored pieces are in the way: +1 more as this piece first has to move away
            int penalty = movingMySquaresPieceAwayDistancePenalty();
            suggestionTo1HopNeighbour.inc(penalty );
            // because own piece is in the way, we can only continue under the condition that it moves away
            suggestionTo1HopNeighbour.addCondition( mySquarePiece(), myPos, ANY);
        } else {
            // square is free (or of opposite color and to be beaten)
        }
        if (!evalIsOkForColByMin(getRelEval(), myPiece().color(), EVAL_TENTH))
            suggestionTo1HopNeighbour.setNoGo(myPos);
        return suggestionTo1HopNeighbour;
    }
*/
    public ConditionalDistance minDistanceSuggestionTo1HopNeighbour() {
        // Todo: Increase 1 more if Piece is pinned to the king
        if (rawMinDistance==null) {
            // should normally not happen, but in can be the case for completely unset squares
            // e.g. a vPce of a pawn behind the line it could ever reach
            return new ConditionalDistance(this);
        }
        // good case: it's already calculated
        if (suggestionTo1HopNeighbour!=null)
            return suggestionTo1HopNeighbour;

        if (rawMinDistance.dist()==0)
            suggestionTo1HopNeighbour = new ConditionalDistance(this,1);  // almost nothing is closer than my neighbour  // TODO. check if my piece can move away at all (considering king pins e.g.)
        else {
            // TODO: the following doesn't work yet, because breadth propagation calls are already qued after the relEval is calculated
            int inc = 0; //(getRelEval()==0 || getRelEval()==NOT_EVALUATED) ? 0 : MAX_INTERESTING_NROF_HOPS-1;

            if (rawMinDistance.isInfinite())
                suggestionTo1HopNeighbour = new ConditionalDistance(this);  // can't get further away than infinite...

                // one hop from here is +1 or +2 if this piece first has to move away
            else if (board.hasPieceOfColorAt(myPiece().color(), myPos)) {
                // one of my same colored pieces are in the way
                int penalty = movingMySquaresPieceAwayDistancePenalty();
                if (penalty<INFINITE_DISTANCE) {
                    inc += penalty + 1;
                    suggestionTo1HopNeighbour = new ConditionalDistance(this,
                            rawMinDistance, inc,
                            myPos, ANY, myPiece().color());
                    if (!evalIsOkForColByMin(getRelEvalOrZero(), myPiece().color()))
                        suggestionTo1HopNeighbour.setNoGo(myPos);
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


    long getLatestChange() {
        return latestChange;
    }

    public ConditionalDistance getRawMinDistanceFromPiece() {
        if (rawMinDistance==null) { // not set yet at all
            rawMinDistance = new ConditionalDistance(this);
            minDistsDirty();
        }
        return rawMinDistance;
    }

    public ConditionalDistance getMinDistanceFromPiece() {
        // check if we already created the response object
        if (minDistance!=null)
            return minDistance;
        // no, its null=="dirty", we need a new one...
        // Todo: Increase 1 more if Piece is pinned to the king
        if (rawMinDistance==null) { // not set yet at all
            rawMinDistance = new ConditionalDistance(this);
            minDistsDirty();
        }
        minDistance = new ConditionalDistance(rawMinDistance);
        /* current decision: we do not use penalty or inc for mindistance if an own colored piece is in the way
         * because for threat/clash calculation it counts how the piece can get here.
         * (However, these movingAwayPenalties or other increases are calculated in the suggestions to further neighbours
         * old code was:
        else if (rawMinDistance.dist()==0
                || (rawMinDistance.dist()==INFINITE_DISTANCE) )
            minDistance=new ConditionalDistance(rawMinDistance);  // almost nothing is closer than my neighbour
        else {
            int inc = 0; // (getRelEval()==0 || getRelEval()==NOT_EVALUATED) ? 0 : MAX_INTERESTING_NROF_HOPS-1;
            // one hop from here is +1 or +2 if this piece first has to move away
            int penalty = movingMySquaresPieceAwayDistancePenalty();
            if (penalty>0)  // my own color piece, it needs to move away first
                minDistance = new ConditionalDistance(rawMinDistance,
                        penalty+inc,
                        myPos,
                        ANY);
            else  {
                // square is free or opponents piece is here, but then I can beat it
                minDistance = new ConditionalDistance(rawMinDistance, inc);
            }
        }
        */
        if ( !evalIsOkForColByMin( getRelEvalOrZero(), myPiece().color() ) )
            minDistance.setNoGo(myPos);
        return minDistance;
    }

    public int movingMySquaresPieceAwayDistancePenalty() {
        // TODO: this also depends on where a own mySquarePiece can move to - maybe only in the way?
        // looks if this square is blocked by own color (but other) piece and needs to move away first
        if (board.hasPieceOfColorAt( myPiece().color(), myPos )) {
            // make further calculation depending on whether mySquarePiece can move away
            if ( mySquarePiece().canMoveAwayReasonably() )
                return 1;
            // it has no good place to go, so it will probably not go away.
            return 3; //1=deactivated, instead of better approaches (that do not work in the overall update mechanism,
            // due to order problems):
            // - INFINITE_DISTANCE
            // - or calc. of how many moves it  takes to free the Piece,
            // - or 2 as a simplification of that calculation;
        }
        //else
        return 0;
    }

    @Override
    public String toString() {
        return "vPce("+myPceID+"="
                +(board.getPiece(myPceID)==null
                    ? "null!?"
                    : pieceColorAndName(board.getPiece(myPceID).getPieceType()) )
                +") on ["+ squareName( myPos)+"] "
                + rawMinDistance + " away from origin {"+ squareName( myPiece().getPos()) + "}";
    }

    public String getDistanceDebugDetails() {
        return  "";
        // (id=" + myPceID + ")" + ", latestChange=" + latestChange";
    }

    @Override
    public int compareTo(@NotNull VirtualPieceOnSquare other) {
        /* do not consider distance for std comparison:
        if ( this.getMinDistanceFromPiece().getShortestDistanceEvenUnderCondition()
                > other.getMinDistanceFromPiece().getShortestDistanceEvenUnderCondition() )
            return 2;
        if ( this.getMinDistanceFromPiece().getShortestDistanceEvenUnderCondition()
                < other.getMinDistanceFromPiece().getShortestDistanceEvenUnderCondition() )
            return -2;
        // distance is equal, so */
        // compare piece value
        return Integer.compare(abs(getValue()), abs(other.getValue()));
    }

    public int getValue() {
        return myPiece().getValue();
    }


/*    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VirtualPieceOnSquare other = (VirtualPieceOnSquare) o;
        boolean equal = compareWithDebugMessage(this + ".Piece Type", myPceType, other.myPceType);
        equal &= compareWithDebugMessage(this + ".myPos", myPos, other.myPos);
        equal &= compareWithDebugMessage(this + "Relative Eval", relEval, other.relEval);
        equal &= compareWithDebugMessage(this + ".RawMinDistance", rawMinDistance, other.rawMinDistance);
        equal &= compareWithDebugMessage(this + ".minDistanceFromPiece", getMinDistanceFromPiece(), other.getMinDistanceFromPiece());
        equal &= compareWithDebugMessage(this + ".minDistanceSuggestionTo1HopNeighbour", minDistanceSuggestionTo1HopNeighbour(), other.minDistanceSuggestionTo1HopNeighbour());
        return equal;
    }
*/

    public String getPathDescription() {
        if (getRawMinDistanceFromPiece().dist()==0)
            return "-" + myPiece().symbol()+squareName(myPos);
        if (getRawMinDistanceFromPiece().dist()>=INFINITE_DISTANCE)
            return "[INF]";
        String tome =  "-" + squareName(myPos)
                +"(D"+getRawMinDistanceFromPiece()+")";
                //.dist()+"/"+getRawMinDistanceFromPiece().nrOfConditions()
        return  "[" + getShortestReasonableUnconditionedPredecessors().stream()
                .map(n-> "(" + n.getPathDescription()+ tome + ")")
                .collect(Collectors.joining( " OR "))
                + "]";
    }

    public String getBriefPathDescription() {
        debugPrintln(true,this.toString() );
        switch (getRawMinDistanceFromPiece().dist()) {
            case 0:
                return "-" + myPiece().symbol() + squareName(myPos);
            case INFINITE_DISTANCE:
                return "[INF]";
            default:
                String tome = "-" + squareName(myPos)
                        + "'" + getRawMinDistanceFromPiece().dist()
                        + "C" + getRawMinDistanceFromPiece().nrOfConditions();
                return "[" + getShortestReasonableUnconditionedPredecessors().stream()
                        .map(n -> n.getBriefPathDescription() + tome)
                        .collect(Collectors.joining("||"))
                        + "]";
        }
    }

    void resetChances() {
        chances = new ArrayList<>(MAX_INTERESTING_NROF_HOPS+1);
        for (int i = 0; i <= MAX_INTERESTING_NROF_HOPS; i++) {
            chances.add(i, new HashMap<>());
        }
        clearCheckGiving();
        predecessors = null;
        shortestReasonableUnconditionedPredecessors = null;
        firstMovesWithReasonableShortestWayToHere = null;
        mobilityFromHere = 0;
        mobilityMapFromHere = 0;
    }

    public void addMoveAwayChance(final int benefit, final int inOrderNr, final Move m) {
        if (inOrderNr > MAX_INTERESTING_NROF_HOPS+1 || abs(benefit) < 2)
            return;
        assert(myPos==m.from());
        if (DEBUGMSG_MOVEEVAL && abs(benefit)>4)
            debugPrintln(DEBUGMSG_MOVEEVAL," Adding MoveAwayChance of " + benefit + "@"+inOrderNr+" for "+m+" of "+this+" on square "+ squareName(myPos)+".");
        addChance(benefit,inOrderNr,m);   // stored as normal chance, but only at the piece origin.
    }

    /**
     * add Chance of possible approaching (to eventually win) an opponents square (just the "upper hand"
     * or even with piece on it) with a certain benefit (relative eval, as always in board perspective)
     * in a suspected move distance
     * @param benefit
     * @param inFutureLevel
     */
    public void addChance(final int benefit, final int inFutureLevel) {
        if (inFutureLevel>MAX_INTERESTING_NROF_HOPS || !getRawMinDistanceFromPiece().distIsNormal())
            return;
        // add chances for all first move options to here
        Set<Move> firstMovesToHere = getFirstMovesWithReasonableShortestWayToHere();
        assert(firstMovesToHere!=null);
        for (Move m : firstMovesToHere) {   // was getFirstUncondMovesToHere(), but it locks out enabling moves if first move has a condition
            if ( !myPiece().isBasicallyALegalMoveForMeTo(m.to()) ) {
                // impossible move, square occupied. Still move needs to be entered in chance list, so that moving away from here also gets calculated
                addChance( 2 * checkmateEval(color()) , 0, m);
            }
            else {
                if (DEBUGMSG_MOVEEVAL && abs(benefit)>4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, "->" + m + "(" + benefit + "@" + inFutureLevel + ")");
                addChance( benefit , inFutureLevel, m);
                if ( evalIsOkForColByMin( benefit, myPiece().color(), -EVAL_DELTAS_I_CARE_ABOUT)
                     && abs(benefit)<(BLACK_IS_CHECKMATE+QUEEN) ) {
                    //TODO: always search for all counter moves here after every addChance is ineffective.
                    // Should be done later collectively after all Chances are calculated
                    // a positive move - see who can cover this square
                    Square toSq = board.getBoardSquare(m.to());
                    // iterate over all opponents who could sufficiently cover my target square.
                    if (toSq.isSquareEmpty() ) {   // but only to this if square is empty, because otherwise (clash) this is already calculated by "close future chances"
                        int myattacksAfterMove = toSq.countDirectAttacksWithColor(color());
                        if (!(colorlessPieceType(getPieceType()) == PAWN && fileOf(m.to()) == fileOf(m.from())))  // not a straight moving pawn
                            myattacksAfterMove--;   // all moves here (except straight pawn) take away one=my cover from the square.
                        for (VirtualPieceOnSquare opponentAtTarget : toSq.getVPieces()) {
                            if (opponentAtTarget != null
                                    && opponentAtTarget.color() != color()
                                    && !opponentAtTarget.getRawMinDistanceFromPiece().isInfinite()
                                    && opponentAtTarget.getRawMinDistanceFromPiece().dist() > 1   // if it is already covering it, no need to bring it closer...
                            ) {
                                // loop over all positions from where the opponent can attack/cover this square
                                for (VirtualPieceOnSquare opponentAtLMO : opponentAtTarget.getShortestReasonableUnconditionedPredecessors()) {
                                    if (opponentAtLMO != null) {
                                        ConditionalDistance oppAtLMORmd = opponentAtLMO.getRawMinDistanceFromPiece();
                                        int defendBenefit;
                                        int opponendDefendsAfterMove = toSq.countDirectAttacksWithColor(opponentAtTarget.color()) + 1;  // one opponent was brought closer
                                        // TODO! real check if covering is possible/significant and choose benefit accordingly
                                        // here just a little guess...
                                        if (opponendDefendsAfterMove > myattacksAfterMove)
                                            defendBenefit = abs(benefit) >> 1;
                                        else
                                            defendBenefit = abs(benefit) >> 3;
                                        // not anymore, because of forking square coverage with higher benefit: limit benefit to the attacking pieces value (as long as we do not use real significance/clash calculation here)
                                        // defendBenefit = min(defendBenefit, positivePieceBaseValue(getPieceType()));
                                        if (!oppAtLMORmd.isUnconditional()  // is conditional and esp. the last part has a condition (because it has more conditions than its predecessor position)
                                                && oppAtLMORmd.nrOfConditions() > oppAtLMORmd.oneLastMoveOrigin().getRawMinDistanceFromPiece().nrOfConditions())
                                            defendBenefit >>= 2;
                                        int defendInFutureLevel = opponentAtLMO.getStdFutureLevel()
                                                - (opponentAtLMO.color() == board.getTurnCol() ? 1 : 0);
                                        if (defendInFutureLevel <= 0)
                                            defendInFutureLevel = 0;
                                        if (defendInFutureLevel > MAX_INTERESTING_NROF_HOPS + 1
                                                || getRawMinDistanceFromPiece().dist() < oppAtLMORmd.dist() - 3
                                                || defendInFutureLevel > inFutureLevel)
                                            continue;
                                        if (getRawMinDistanceFromPiece().dist() < oppAtLMORmd.dist())
                                            defendBenefit >>= 1;
                                        if (opponentAtLMO.getRawMinDistanceFromPiece().hasNoGo())
                                            defendBenefit >>= 3;  // could almost continue here
                                        if (opponentAtLMO.getMinDistanceFromPiece().hasNoGo())
                                            defendBenefit >>= 1;  // and will not survive there myself
                                        if (this.getMinDistanceFromPiece().hasNoGo())
                                            defendBenefit >>= 2;  // if the piece dies there anyway, extra coverage is hardly necessary
                                        if (isKing(opponentAtLMO.getPieceType())) {
                                            if (oppAtLMORmd.dist() > 1 || isQueen(getPieceType()))
                                                continue;
                                            if (oppAtLMORmd.dist() > 2)
                                                defendBenefit >>= 2;
                                            else
                                                defendBenefit >>= 1;
                                        }
                                        /* was an idea, but actually we award and fee only the first moves, so we are already too late to cover here, if the first move (futurelevel=0) happens...
                                        if (defendInFutureLevel > inFutureLevel)   // defender is too late...
                                            defendBenefit /= 4 + defendInFutureLevel - inFutureLevel;
                                        */
                                        if (defendInFutureLevel > 0 )   // defender is too late...
                                            defendBenefit /= 4 + defendInFutureLevel - (min(inFutureLevel,defendInFutureLevel)>>1);
                                        if (isBlack(opponentAtLMO.color()))
                                            defendBenefit = -defendBenefit;
                                        if (abs(defendBenefit) > 1)
                                            opponentAtLMO.addRawChance(defendBenefit, max(inFutureLevel, defendInFutureLevel));
                                    }
                                }

                            }
                        }
                    }
                    // and see who can block the firstmove
                    if (inFutureLevel<2) {
                        toSq.getvPiece(getPieceID()).addBenefitToBlockers(m.from(), inFutureLevel, -benefit >> 2);
                    }
                }
                if (DEBUGMSG_MOVEEVAL && abs(benefit)>4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, ".");
                /* Option:Solved differently in loop over allsquares now
                ConditionalDistance toSqRmd = toSq.getvPiece(myPceID).getRawMinDistanceFromPiece();
                if ((toSqRmd.dist() == 1 || toSqRmd.dist() == 2) && toSqRmd.nrOfConditions() == 1) {
                    // add chances for condition of this "first" (i.e. second after condition) move, that make me come one step closer
                    int fromCond = getRawMinDistanceFromPiece().getFromCond(0);
                    if (fromCond != -1)
                        addChances2PieceThatNeedsToMove(benefit - (benefit >> 2), inFutureLevel, fromCond);
                } */
            }
        }
        // add chances for other moves on the way fulfilling conditions, that make me come one step closer
        // Todo: add conditions from all shortest paths, this here covers only one, as conditions are only stored along one of the shortests paths
        //  Partially solved above by addChances2PieceThatNeedsToMove fir first moves.
                /* Option:Solved differently in loop over allsquares now
        if (getRawMinDistanceFromPiece().dist()>1)
            for (Integer fromCond : rawMinDistance.getFromConds() ) {
                if (fromCond!=-1)
                    addChances2PieceThatNeedsToMove(benefit>>1, inFutureLevel, fromCond);
            } */
    }

    /**
     * like addChance() but not "calling" other opponents pieces to cover here if benefit is significant
     * @param benefit
     * @param inOrderNr
     */
    public void addRawChance(final int benefit, final int inOrderNr) {
        if (inOrderNr>MAX_INTERESTING_NROF_HOPS || !getRawMinDistanceFromPiece().distIsNormal())
            return;
        // add chances for all first move options to here
        Set<Move> firstMovesToHere = getFirstMovesWithReasonableShortestWayToHere();
        assert(firstMovesToHere!=null);
        for (Move m : firstMovesToHere) {   // was getFirstUncondMovesToHere(), but it locks out enabling moves if first move has a condition
            if ( !myPiece().isBasicallyALegalMoveForMeTo(m.to()) ) {
                // impossible move, square occupied. Still move needs to be entered in chance list, so that moving away from here also gets calculated
                addChance( 2 * checkmateEval(color()) , 0, m);
            }
            else {
                if (abs(benefit)>4)
                    debugPrint (DEBUGMSG_MOVEEVAL, " +raw->" + m + "(" + benefit + "@" + inOrderNr + ") ");
                addChance( benefit , inOrderNr, m);
            }
        }

    }


    /**
     * adds Chances to piece2Bmoved, but also threats that come up, when a piece in my
     * way (at piece2movedPos) moves away
     * @param benefit
     * @param inOrderNr
     * @param piece2BmovedPos
     */
    void addChances2PieceThatNeedsToMove(int benefit, int inOrderNr, final Integer piece2BmovedPos) {
        ChessPiece piece2Bmoved = board.getPieceAt(piece2BmovedPos);
        if (piece2Bmoved==null) {
            if (DEBUGMSG_MOVEEVAL)
                board.internalErrorPrintln("Error in from-condition of " + this + ": points to empty square " + squareName(piece2BmovedPos));
        }
        else {
            if (color() != piece2Bmoved.color() && inOrderNr>0)
                inOrderNr--;
            // find matching lastMoveOrigins, which are blocked by this piece
            for (VirtualPieceOnSquare lmo : getPredecessors()) {
                if (calcDirFromTo(myPos, lmo.myPos) == calcDirFromTo(myPos, piece2BmovedPos)) {
                    // origin is in the same direction
                    Set<Move> firstMoves = lmo.getFirstMovesWithReasonableShortestWayToHere();
                    if ( (firstMoves==null || firstMoves.size()==0) || lmo.getMinDistanceFromPiece().dist()==1)
                        firstMoves.add( new Move(lmo.myPos, myPos));  // there is no lmo of the lmo, it is a 1-dist move
                    if (firstMoves.size()==1 && lmo.getRawMinDistanceFromPiece().dist() >= 1)
                        benefit >>= 1;  // only one move leads to here, we also look at the first move and the other half is given out below
                    piece2Bmoved.addMoveAwayChance2AllMovesUnlessToBetween(
                            benefit,
                            inOrderNr,
                            lmo.myPos,
                            myPos, // to the target position
                            lmo.getRawMinDistanceFromPiece().dist() >= 1
                                    && piece2Bmoved.color() != color()  // an opponents piece moving to the hop/turning point
                                                                        // before my target is also kind of moving out of
                                                                        // the way, as it can be beaten  (unless it beats me)
                    );
                    // if there is only one way to get here, the following part works in the same way for the first move
                    // to here (but any hop in between is neglected, still)
                    // thus, TODO: exclusion needs to be extended to previous moves on the way, works only for the last part (or 1-move distance)
                    if (firstMoves.size()!=1 || lmo.getRawMinDistanceFromPiece().dist() < 1)
                        continue;
                    piece2Bmoved.addMoveAwayChance2AllMovesUnlessToBetween(
                            benefit,
                            inOrderNr,
                            myPiece().getPos(),
                            firstMoves.iterator().next().to() , // to the target position
                            piece2Bmoved.color() != color()  // only exclude blocking my own color. An opponents piece moving to = beating my piece point also gets credit
                    );
                }

            }
            // TODO: Check if toPos here should really be exclusive or rather inclusive, because if the p2Bmoved is
            // moving just there (propably beating) then the benefit for the other piece if most probably gone.
        }
    }

    private void addChance(int benefit, int inOrderNr, Move m) {
        if (inOrderNr<0 || inOrderNr>chances.size()) {
            if (DEBUGMSG_MOVEEVAL)
                System.err.println("Error in addChance for " + this + ": invalid inOrderNr in benefit " + benefit + "@" + inOrderNr);
            return;
        }
        Integer chanceSumUpToNow = chances.get(inOrderNr).get(m);
        if (chanceSumUpToNow==null) {
            chances.get(inOrderNr).put(m, benefit);
        }
        else {
                chances.get(inOrderNr).replace(m, chanceSumUpToNow + benefit);
        }
    }

    public List<HashMap<Move, Integer>> getChances() {
        return chances;
    }

    public int getClosestChanceReachout() {
        int i = 0;
        while (chances.get(i).size()==0) {
            i++;
            if (i > MAX_INTERESTING_NROF_HOPS)
                return MAX_INTERESTING_NROF_HOPS + 1;
        }
        return i;  //(MAX_INTERESTING_NROF_HOPS+1-i)*100;
    }

    int getBestChanceOnLevel(int inFutureLevel) {
        if (inFutureLevel<0 || inFutureLevel>chances.size()) {
            if (DEBUGMSG_MOVEEVAL)
                System.err.println("Error in getBestChanceOnLevel for " + this + ": invalid inFutureLevel @" + inFutureLevel);
            return 0;
        }
        int max = 0;
        for (Integer c : chances.get(inFutureLevel).values() ) {
            if (isWhite(color()) ? c.intValue() > max
                    : c.intValue() < max)
                max = c.intValue();
        }
        return max;
    }


    /**
     * isUnavoidableOnShortestPath() finds out if the square pos has to be passed on
     * the way from thd piece to this current square/cPiece.*
     *
     * @param pos      that is checked, if it MUST be on the way to here
     * @param maxdepth remaining search depth limit - needed to cancel long possible
     *                 detours, e.g. due to MULTIPLE shortest paths. (! Also needed because
     *                 remaining bugs in dist-calculation unfortunately lets sometimes
     *                 exist circles in the shortest path, leading to endless recursions...)
     * @return true, if all ways between actual piece and here lead via pos.
     */
    abstract public boolean isUnavoidableOnShortestPath(int pos, int maxdepth);

    //// getter

    public int getPieceID() {
        return myPceID;
    }

    public int getPieceType() {
        return myPceType;
    }

    protected long getOngoingUpdateClock() {
        return board.getPiece(myPceID).getLatestUpdate();
    }

    public int getRelEval() {
        return relEval;
    }

    public int getRelEvalOrZero() {
        return hasRelEval() ? relEval : 0;
    }

    public int getClashContribOrZero() {
        return relClashContrib == NOT_EVALUATED ? 0 : relClashContrib;
    }

    public boolean hasRelEval() {
        return relEval != NOT_EVALUATED;
    }

    public boolean color() {
        return colorOfPieceType(myPceType);
    }

    protected boolean myOpponentsColor() {
        return opponentColor(myPiece().color());
    }

    public int getMyPiecePos() {
        return board.getPiece(myPceID).getPos();
    }

    public boolean isConditional() {
        return !rawMinDistance.isUnconditional();
    }

    public boolean isUnconditional() {
        return rawMinDistance.isUnconditional();
    }

    public boolean isCheckGiving() {
        return isCheckGiving;
    }

    public void clearCheckGiving() {
        isCheckGiving = false;
    }

    public int getMobility() {
        return mobilityFromHere;
    }

    public int getMobilityMap() {
        return mobilityMapFromHere;
    }

    //// setter


    protected void setLatestChangeToNow() {
        latestChange = getOngoingUpdateClock();
    }

    public void setRelEval(int relEval) {
        int oldRelEval = this.relEval;
        this.relEval = relEval;
        if (oldRelEval-2<=relEval && oldRelEval+2>=relEval)  // +/-2 is almost the same.
            return;
        //distances need potentially to be recalculated, as a bad relEval can influence if a piece can really go here, resp. the NoGo-Flag
        ConditionalDistance oldSugg = suggestionTo1HopNeighbour;
        minDistsDirty();
        // hmm, was thought of as an optimization, but is almost equal, as the propagation would anyway stop soon
        if (oldSugg==null || !minDistanceSuggestionTo1HopNeighbour().cdEquals(oldSugg)) {
            // if we cannot tell or suggestion has changed, trigger updates
            setLatestChangeToNow();
            if (oldSugg != null && ( oldSugg.cdIsSmallerThan(minDistanceSuggestionTo1HopNeighbour())
                                     || oldSugg.cdIsEqualButDifferentSingleCondition(minDistanceSuggestionTo1HopNeighbour()) )
            ) {
                myPiece().quePropagation(
                        0,
                        this::propagateResetIfUSWToAllNeighbours);
            }
            quePropagateDistanceChangeToAllNeighbours();
        }
    }

    /**
     * initial set of contribution of myPiece at myPos.
     * to be called in updateClashResultAndRelEvals.
     * in later phaases use addClashContrib()!
     * @param relClashContrib the contribution...
     */
    public void setClashContrib(int relClashContrib) {
        this.relClashContrib = relClashContrib;
    }

    public void addClashContrib(int relClashContrib) {
        this.relClashContrib += relClashContrib;
    }

    public void setCheckGiving() {
        isCheckGiving = true;
    }

    int getStdFutureLevel() {
        ConditionalDistance rmd = getRawMinDistanceFromPiece();
        int inFutureLevel = rmd.dist()
                + rmd.countHelpNeededFromColorExceptOnPos(opponentColor(color()), this.myPos);
        if (inFutureLevel <= 0)
            inFutureLevel = 0;
        return inFutureLevel;
    }


    public void addMobility(int mobility) {
        this.mobilityFromHere += mobility;
    }


    public void addMobilityMap(int mobMap) {
        this.mobilityMapFromHere |= mobMap;
    }

    /**
     * gives out benefit for blocking the way of an attacker to here.
     * Called on the vPce that likes to move here (or further via here)
     *
     * @param attackFromPos
     * @param futureLevel
     * @param benefit
     * @return nr of immeditate (d==1) real blocks by opponent found.
     */
    int addBenefitToBlockers(final int attackFromPos, int futureLevel, final int benefit) {
        if (futureLevel<0)  // TODO: may be deleted later, after stdFutureLevel is fixed to return one less (safely)
            futureLevel=0;
        int countBlockers = 0;
        // first find best blockers
        int closestDistInTimeWithoutNoGo = MAX_INTERESTING_NROF_HOPS+1;
        for (int pos : calcPositionsFromTo(attackFromPos, this.myPos)) {
            for (VirtualPieceOnSquare blocker : board.getBoardSquare(pos).getVPieces()) {
                if (blocker != null
                        && blocker.color() == opponentColor(color())
                        && blocker.getPieceID() != this.getPieceID()
                        && !isKing(blocker.getPieceType())
                        && blocker.getRawMinDistanceFromPiece().dist() < 3   //TODO?: make it generic for all future levels )
                        && blocker.getRawMinDistanceFromPiece().dist() > 0
                        && blocker.getRawMinDistanceFromPiece().isUnconditional()
                        && !blocker.getRawMinDistanceFromPiece().hasNoGo()
                ) {
                    int blockerFutureLevel = blocker.getStdFutureLevel() - 1;
                            // - (blocker.color()==board.getTurnCol() ? 1 : 0);
                    if ( pos==attackFromPos ) {
                        blockerFutureLevel--;   // we are close to a turning point on the way of the attacker, it is sufficient to cover the square

                        if (board.getPieceIdAt(pos) == getPieceID() ) {
                            if (blockerFutureLevel > 0)
                                continue; // it also makes no sense to chase away the piece that wants to move anyway
                        }
                        else if (blocker.color() != color() && blockerFutureLevel == 0) {
                            //TODO: give staying-bonus to blocker - it already blocks the turning point.
                            continue; // it makes no sense to move opponent blocker in the way where it can directly be taken
                        }
                    }
                    if (blockerFutureLevel<0)
                        blockerFutureLevel=0;
                    int finalFutureLevel = futureLevel - blockerFutureLevel;
                    if ( blocker.getRawMinDistanceFromPiece().dist() == 1  && blocker.getRawMinDistanceFromPiece().isUnconditional())
                        countBlockers++;
                    if (finalFutureLevel>=0
                            && blocker.getRawMinDistanceFromPiece().dist() < closestDistInTimeWithoutNoGo
                    ) { // not too late
                        closestDistInTimeWithoutNoGo = blocker.getRawMinDistanceFromPiece().dist();
                    }
                }
            }
        }
        // give benefit
        for (int p : calcPositionsFromTo(attackFromPos, this.myPos)) {
            for (VirtualPieceOnSquare blocker : board.getBoardSquare(p).getVPieces()) {
                if (blocker != null
                        && blocker.color() == opponentColor(color())
                        && blocker.getPieceID() != this.getPieceID()
                        && !isKing(blocker.getPieceType())
                        && blocker.getRawMinDistanceFromPiece().dist() < 3   //TODO?: make it generic for all future levels )
                        && blocker.getRawMinDistanceFromPiece().dist() > 0
                        && blocker.getRawMinDistanceFromPiece().isUnconditional()
                        && !blocker.getRawMinDistanceFromPiece().hasNoGo()
                ) {
                    int finalBenefit = ( abs(blocker.getValue()) <= abs(getValue()) )
                            ? (benefit-(benefit >> 3))
                            : (benefit >> 2);
                    if ( blocker.getRawMinDistanceFromPiece().dist() > closestDistInTimeWithoutNoGo )
                        finalBenefit >>= 1; // others are closer  - it will be diminished more further down bacause of future level being big
                    int blockerFutureLevel = blocker.getStdFutureLevel() - 1;
                            //- (blocker.color()==board.getTurnCol() ? 1 : 0);
                    if ( p==attackFromPos ) {
                        blockerFutureLevel--;   // we are close to a turning point on the way of the attacker, it is sufficient to cover the square

                        if (board.getPieceIdAt(p) == getPieceID() ) {
                            if (blockerFutureLevel > 0)
                                continue; // it also makes no sense to chase away the piece that wants to move anyway
                        }
                        else if (blocker.color() != color() && blockerFutureLevel == 0) {
                            //TODO: give staying-bonus to blocker - it already blocks the turning point.
                            continue; // it makes no sense to move opponent blocker in the way where it can directly be taken
                        }
                    }
                    if (blockerFutureLevel<0)
                        blockerFutureLevel=0;
                    int finalFutureLevel;
                    if ( blocker.getRawMinDistanceFromPiece().dist() > closestDistInTimeWithoutNoGo )
                        finalFutureLevel = max(futureLevel-1, blockerFutureLevel);
                    else
                        finalFutureLevel = futureLevel - blockerFutureLevel;

                    if (finalFutureLevel<0) { // coming too late
                        finalBenefit /= 3 + blockerFutureLevel - futureLevel;
                        finalFutureLevel = blockerFutureLevel - futureLevel;
                    }
                    else if (finalFutureLevel>0) // still time
                        finalBenefit >>= finalFutureLevel;

                    if (p!=attackFromPos && blocker.getMinDistanceFromPiece().hasNoGo())
                        finalBenefit >>= 2;   // a square "in between" must be safe to block.

                    if (DEBUGMSG_MOVEEVAL && abs(finalBenefit) > 4)
                        debugPrint(DEBUGMSG_MOVEEVAL, " Benefit " + finalBenefit + "@" + finalFutureLevel
                                + " for blocking-move by " + blocker + " @" + blockerFutureLevel + " to " + squareName(p)
                                + " against " + this + " @" + futureLevel + " coming from " + squareName(attackFromPos)+ ": ");
                    blocker.addRawChance(finalBenefit, finalFutureLevel);
                    debugPrintln(DEBUGMSG_MOVEEVAL, ".");
                }
            }
        }

        /*
                if (blocker != null
                        //&& blocker.color() == opponentColor(color())
                        && blocker.getPieceID() != this.getPieceID()
                        && !isKing(blocker.getPieceType())
                        && blocker.getRawMinDistanceFromPiece().dist() < 3   //TODO?: make it generic for all future levels )
                        && blocker.getRawMinDistanceFromPiece().isUnconditional()
                        && !blocker.getRawMinDistanceFromPiece().hasNoGo()
                ) {
                    int finalBenefit = ( abs(blocker.getValue()) <= abs(getValue()) )
                            ? benefit : (benefit >> 2);
                    int blockerFutureLevel = blocker.getStdFutureLevel()-1;
                    //TODO!: correctly treat pawns (not covering straight, but blocking etc.)
                    if ( pos==attackFromPos ) {
                        if (board.getPieceIdAt(pos) != getPieceID() ) {
                            // we are at a turning point on the way of the attacker
                            if ( blocker.color() != color() ) {
                                if (blockerFutureLevel==0 )
                                    continue; // it makes no sense to move opponent blocker in the away where it can be taken
                                else //if (blockerFutureLevel>0)
                                    blockerFutureLevel--;  // but it makes sense to cover that square (which even happens one earlier!)
                            }
                        }
                        else if (blockerFutureLevel > 0)
                            continue; // usually makes no sense to chase away the piece that wants to move anyway
                    }
                    int finalFutureLevel = max( futureLevel, blockerFutureLevel);
                    if ( blocker.getRawMinDistanceFromPiece().dist() == 1  && blocker.getRawMinDistanceFromPiece().isUnconditional())
                        countBlockers++;
                    if (finalFutureLevel>futureLevel) // coming too late
                        finalBenefit /= 3+finalFutureLevel-futureLevel;
                    if (blocker.color() == color()) {  // blocking myself takes away a chance, but is not as such negative - not easy to express in the evals...
                        if (finalFutureLevel == 0)
                            finalBenefit >>= 1;
                        else
                            finalBenefit >>= 3;
                    }
                    // not needed for blocking by covering: if (blocker.getMinDistanceFromPiece().hasNoGo()) finalBenefit >>= 2;
        if (DEBUGMSG_MOVEEVAL && abs(finalBenefit) > 4)
            debugPrint(DEBUGMSG_MOVEEVAL, " Benefit " + finalBenefit + "@" + finalFutureLevel
                    + " for blocking-move by " + blocker + " to " + squareName(pos) + " against " + this + " coming from " + squareName(attackFromPos)+ ": ");
        blocker.addRawChance(finalBenefit, finalFutureLevel);
        debugPrintln(DEBUGMSG_MOVEEVAL, ".");
    } */

        return countBlockers;
}

    /* 0.29z5 Discarded.
    unclear, why this "improved" and thought of as more precise version is clearly worse in the test games...
    try again:
    int BAD_addBenefitToBlockers(final int attackFromPos, int futureLevel, final int benefit) {
        if (futureLevel<0)  // TODO: may be deleted later, after stdFutureLevel is fixed to return one less (safely)
            futureLevel=0;
        int countBlockers = 0;
        // first find best blockers
        int closestDistinTimeWithoutNoGo = MAX_INTERESTING_NROF_HOPS+1;    // closest distance for a piece to be able to cover
        for (int pos : calcPositionsFromTo(attackFromPos, this.myPos)) {
            for (VirtualPieceOnSquare blocker : board.getBoardSquare(pos).getVPieces()) {
                if (blocker != null
                        && blocker.color() == opponentColor(color())
                        && blocker.getPieceID() != this.getPieceID()
                        && !isKing(blocker.getPieceType())
                        && blocker.getRawMinDistanceFromPiece().dist() < 4   //TODO?: make it generic for all future levels )
                        && blocker.getRawMinDistanceFromPiece().dist() > 0
                        && blocker.getRawMinDistanceFromPiece().isUnconditional()
                        && !blocker.getRawMinDistanceFromPiece().hasNoGo()
                ) {
                    int blockerFutureLevel = blocker.getStdFutureLevel();                     // - (blocker.color()==board.getTurnCol() ? 1 : 0);
                    if ( pos==attackFromPos && blocker.color() != board.getTurnCol() )
                        blockerFutureLevel--;   // we are close to a turning point on the way of the attacker, it is sufficient to cover the square
                    if (blockerFutureLevel<0)
                        blockerFutureLevel=0;
                    int finalFutureLevel = futureLevel-blockerFutureLevel+1;
                    if (blocker.getMinDistanceFromPiece().dist() < closestDistinTimeWithoutNoGo) { // new closest blocker, remember its distance
                        closestDistinTimeWithoutNoGo = blocker.getMinDistanceFromPiece().dist();
                    }
                }
            }
        }
        // give benefit
        for (Iterator<ChessPiece> it = board.getPiecesIterator(); it.hasNext(); ) {
            ChessPiece bPce = it.next();
            if (bPce==null)
                continue;
            int blockerBenefit = ( abs(bPce.getValue()) <= abs(myPiece().getValue()) )
                    ? (benefit-(benefit >> 3))
                    : (benefit >> 2);
            int maxBenefit = 0;
            int maxFL = 0;
            VirtualPieceOnSquare maxBlockerVPce = null;
            for (int p : calcPositionsFromTo(attackFromPos, this.myPos)) {
                VirtualPieceOnSquare blocker = board.getBoardSquare(p).getvPiece(bPce.getPieceID() );
                if (blocker != null
                        && blocker.color() == opponentColor(color())
                        && blocker.getPieceID() != this.getPieceID()
                        && !isKing(blocker.getPieceType())
                        && blocker.getRawMinDistanceFromPiece().dist() < 4   //TODO?: make it generic for all future levels )
                        && blocker.getRawMinDistanceFromPiece().dist() > 0
                        && blocker.getRawMinDistanceFromPiece().isUnconditional()
                        && !blocker.getRawMinDistanceFromPiece().hasNoGo()
                ) {
                    int finalBenefit = blockerBenefit;
                    int blockerFutureLevel = blocker.getStdFutureLevel();
                    if ( p==attackFromPos && blocker.color() != board.getTurnCol() ) {
                        // we are close to a turning point on the way of the attacker, it is sufficient to cover the square
                        blockerFutureLevel--;
                        // TODO: treat covering with more expensive pieces better - depend on real possible clash result there
                        if (blocker.getValue()-EVAL_TENTH > this.getValue())
                            finalBenefit >>= 2;
                    }
                    if (blockerFutureLevel<0)
                        blockerFutureLevel=0;
                    int finalFutureLevel = futureLevel-blockerFutureLevel+1;

                    if (finalFutureLevel<0) { // coming too late
                        finalBenefit /= 2 + blockerFutureLevel - futureLevel;
                        finalFutureLevel = blockerFutureLevel - futureLevel;
                    }
                    else if (finalFutureLevel>0) { // still time
                        finalBenefit -= finalBenefit>>2;  // *0.75
                        if (finalFutureLevel>1) // really still time :-)
                            finalBenefit = finalBenefit>>3 + (finalBenefit>>(finalFutureLevel-1));
                    }

                    if (blocker.getMinDistanceFromPiece().hasNoGo())
                        finalBenefit >>= 2;

                    if ( blocker.getMinDistanceFromPiece().dist() > closestDistinTimeWithoutNoGo+1 ) { // others are closer
                        finalBenefit /= blocker.getMinDistanceFromPiece().dist() - closestDistinTimeWithoutNoGo;
                    }

                    if ( isWhite(blocker.color()) && finalBenefit>maxBenefit
                         || isBlack(blocker.color()) && finalBenefit<maxBenefit
                    ) {  // remember best blocking square=vPce
                        if ( maxBlockerVPce==null  // count only one per piece
                                && blocker.getRawMinDistanceFromPiece().dist() == 1  && blocker.getRawMinDistanceFromPiece().isUnconditional())
                            countBlockers++;
                        maxBenefit = finalBenefit;
                        maxFL = finalFutureLevel;
                        maxBlockerVPce = blocker;
                    }
                    // reward all others also a little bit
                    finalBenefit >>= 2;  // 1/4
                    if (DEBUGMSG_MOVEEVAL && abs(finalBenefit) > 4)
                        debugPrint(DEBUGMSG_MOVEEVAL, " Benefit " + finalBenefit + "@" + finalFutureLevel
                                + " for blocking-move by " + blocker + " @" + blockerFutureLevel + " to " + squareName(p)
                                + " against " + this + " @" + futureLevel + " coming from " + squareName(attackFromPos)+ ": ");
                    //blocker.addRawChance(finalBenefit, finalFutureLevel);
                }
            }
            if (maxBlockerVPce != null) {
                maxBenefit -= maxBenefit>>2;  //  3/4  (a bot more than the rest of the above)
                if (DEBUGMSG_MOVEEVAL && abs(maxBenefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " + Benefit " + maxBenefit + "@" + maxFL
                            + " for best blocking-move by " + maxBlockerVPce + " . ");
                maxBlockerVPce.addRawChance(maxBenefit, maxFL);
            }

        }

        return countBlockers;
    }
    */


    public int additionalChanceWouldGenerateForkingDanger(int atPos, int evalForTakenOpponentHere) {
        if (rawMinDistance.dist()!=1) {
            // is at the moment only used and implemented at a dist==1
            System.err.println("Error in additionalChanceHereWouldGenerateForkingDanger(): must be called for dist==1 only.");
            return 0;
        }
        final int futureLevel = 1;
        // run over all squares=vPces reachable from here, to see the max benefit with different move axis
        int maxChanceHere = 0;
        final int MIN_SIGNIFICANCE = EVAL_HALFAPAWN; // 50
        int dir = calcDirFromTo(myPos, atPos);
        for (VirtualPieceOnSquare nVPce : getNeighbours()) {
            if (nVPce==null)
                continue;
            if (dir == calcDirFromTo(myPos, nVPce.myPos) )
                continue;
            Move m = new Move(getMyPiecePos(), myPos);
            Integer chanceHere = nVPce.getChances().get(futureLevel).get(m);
            if (chanceHere!=null
                    && evalIsOkForColByMin(chanceHere, color(), -MIN_SIGNIFICANCE)
                    && (isWhite(color()) ? chanceHere>maxChanceHere : chanceHere<maxChanceHere ) )
                maxChanceHere = chanceHere;
        }
        if (isWhite(color()))
            return min(maxChanceHere, evalForTakenOpponentHere);
        return max(maxChanceHere, evalForTakenOpponentHere);
    }

    /**
     * checks whether this vPce cannot be attacked (from current d==2) by lower rated piece
      * @return boolean
     */
    public boolean isASavePlaceToStay() {
        if (!evalIsOkForColByMin(getRelEvalOrZero(),color()))
            return false; // cannot be there anyway
        // todo check, if piece is part of a clash there
        for (VirtualPieceOnSquare attacker : board.getBoardSquare(myPos).getVPieces()) {
            if (attacker == null
                    || attacker.color()==color()
                    || attacker.getMinDistanceFromPiece().dist()!=2
                    || attacker.getMinDistanceFromPiece().hasNoGo()
                    || !attacker.getMinDistanceFromPiece().isUnconditional()
                    || abs(attacker.getValue()) > abs(getValue()) )
                continue;
            return false;
        }
        return true;
    }

/*
    public ConditionalDistance predictMoveInfluenceOnDistance() {
        Set firstMoves = new HashTree()
        if (getRawMinDistanceFromPiece().dist()==0)
            return "-" + myPiece().symbol()+squareName(myPos);
        String tome =  "-" + squareName(myPos)
                +"(D"+getRawMinDistanceFromPiece()+")";
        //.dist()+"/"+getRawMinDistanceFromPiece().nrOfConditions()
        int shortestNeighbourDistance = getPredecessorNeighbours().stream()
                .map(n->n.getMinDistanceFromPiece().dist() )
                .min(Comparator.naturalOrder()).orElse(0);
        return  "[" + getPredecessorNeighbours().stream()
                .filter(n->n.getMinDistanceFromPiece().dist()==shortestNeighbourDistance)
                .map(n-> "(" + n.getPathDescription()+ tome + ")")
                .collect(Collectors.joining( "\n OR "))
                + "]";
    }
*/

    //////
    ////// handling of ValueInDir

/*
    private void resetValues() {
        for (int i = 0; i < MAXMAINDIRS; i++)
            valueInDir[i] = 0;
    }
 */

/*
    void propagateMyValue(int value) {
        // TODO: this part with Values is still completely nnon-sens and need to be rethinked before implementation
        // first the direct "singleNeighbours"
        for (VirtualPieceOnSquare n: singleNeighbours) {
            n.propagateDistance(minDistanceSuggestionTo1HopNeighbour());
            // TODO: see above, this also depends on where a own mySquarePiece can move to - maybe only in the way?
        }
        // for the slidingNeighbours, we need to check from which direction the figure is coming from
        for (int dirIndex=0; dirIndex<MAXMAINDIRS; dirIndex++)
            tellDistanceChangeToSlidingNeighbourInDirXXX(dirIndex);
    }

    private void tellDistanceChangeToSlidingNeighbourInDirXXX(int passingThroughInDirIndex) {
        // inform one (opposite) neighbour
        VirtualPieceOnSquare n = slidingNeighbours[passingThroughInDirIndex];
        if (n != null)
            n.propagateDistanceObeyingPassthrough(
                    getSuggestionToPassthroughIndex(passingThroughInDirIndex),
                    passingThroughInDirIndex);
    }
*/

}
