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
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.max;

public abstract class VirtualPieceOnSquare implements Comparable<VirtualPieceOnSquare> {
    protected final ChessBoard board;
    protected final int myPceID;
    protected final int myPceType;
    protected final int myPos;

    private int relEval;  // is in board perspective like all evals! (not relative to the color, just relative as seen from the one piece)
    private int relClashContrib;  // if Piece is involved in Clash, relEval can be 0, but still has a contribution. if Pieves moved away instead, it would miss this contribution.


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

    // propagate "values" / chances/threats/protections/pinnings in backward-direction
    //private final int[] valueInDir;  // must probably be changed later, because it depends on the Piece that comes that way, but lets try to keep this factor out

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

    protected void setLatestChangeToNow() {
        latestChange = getOngoingUpdateClock();
    }

    protected long getOngoingUpdateClock() {
        return board.getPiece(myPceID).getLatestUpdate();
    }

    public int getRelEval() {
        return relEval;
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
            latestChange = getOngoingUpdateClock();
            if (oldSugg != null && oldSugg.cdIsSmallerThan(minDistanceSuggestionTo1HopNeighbour()))
                myPiece().quePropagation(
                        0,
                        this::propagateResetIfUSWToAllNeighbours);
            quePropagateDistanceChangeToAllNeighbours();
        }
    }

    public void setClashContrib(int relClashContrib) {
        this.relClashContrib = relClashContrib;
    }

    public int getClashContrib() {
        return relClashContrib == NOT_EVALUATED ? 0 : relClashContrib;
    }


/*    public MovenetDistance movenetDistance() {
        return movenetCachedDistance;
    }
*/

    //////
    ////// general Piece/moving related methods

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
        board.getPiece(myPceID).startNextUpdate();  //todo: think if startNextUpdate needs to be called one level higher, since introduction of board-wide hop-wise distance calculation
        if (rawMinDistance!=null && !rawMinDistance.isInfinite())
           quePropagateDistanceChangeToAllNeighbours(); // 0, Integer.MAX_VALUE);
        board.getPiece(myPceID).endUpdate();  // todo: endUpdate necessary?
    }

    // fully set up initial distance from this vPces position
    public void myOwnPieceHasSpawnedHere() {  //replaces myOwnPieceHasMovedHereFrom(int frompos) for spawn case. the normal case is replaced by orhestration viw chessPiece
        // one extra piece
        // treated just like sliding neighbour, but with no matching "from"-direction
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, "[" + pieceColorAndName(board.getPiece(myPceID).getPieceType())
                + "(" + myPceID + "): propagate own distance: ");

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

    protected abstract List<VirtualPieceOnSquare> getNeighbours();

    protected List<VirtualPieceOnSquare> getPredecessorNeighbours() {  // where could it come from
        return getNeighbours(); // needs to be overridden for pawns
        // TODO: and for castelling
        // Todo: ond for pawn promotions
    }

    // fully set up initial distance from this vPces position
    public void myOwnPieceHasMovedHereFrom(int frompos) {
        assert(frompos!=NOWHERE);
        // a piece moved  (around the corner or for non-sliding neighbours
        // treated just like sliding neighbour, but with no matching "from"-direction
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, "[" + pieceColorAndName(board.getPiece(myPceID).getPieceType())
                + "(" + myPceID + "): propagate own distance: ");

        board.getPiece(myPceID).startNextUpdate();
        rawMinDistance = new ConditionalDistance(this,0);  //needed to stop the reset-bombs below at least here
        minDistsDirty();
        resetMovepathBackTo(frompos);
        //TODO: the currently necessary reset starting from the frompos is very costly. Try
        // to replace it with propagaten that is able to correct dist values in both directions
        board.getBoardSquares()[frompos].getvPiece(myPceID).resetDistances();
        board.getBoardSquares()[frompos].getvPiece(myPceID).propagateResetIfUSWToAllNeighbours();
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
                    suggestionTo1HopNeighbour = new ConditionalDistance(this, rawMinDistance, inc, myPos, ANY, myPiece().color());
                } else
                    suggestionTo1HopNeighbour = new ConditionalDistance(this);
                // because own piece is in the way, we can only continue under the condition that it moves away
            } else {
                // square is free (or of opposite color and to be beaten)
                inc += 1; // so finally here return the "normal" case -> "my own Distance + 1"
                suggestionTo1HopNeighbour = new ConditionalDistance( this, rawMinDistance, inc);
                if (!evalIsOkForColByMin(getRelEval(), myPiece().color()))
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
        if ( !evalIsOkForColByMin( getRelEval(), myPiece().color() ) )
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
            return 2; //1=deactivated, instead of better approaches (that do not work in the overall update mechanism,
            // due to order problems):
            // - INFINITE_DISTANCE
            // - or calc. of how many moves it  takes to free the Piece,
            // - or 2 as a simplification of that calculation;
        }
        //else
        return 0;
    }

    public int getPieceID() {
        return myPceID;
    }

    public int getPieceType() {
        return myPceType;
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
        return Integer.compare(abs(this.myPiece().getValue()), abs(other.myPiece().getValue()));
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
    public boolean color() {
        return colorOfPieceType(myPceType);
    }

    protected boolean myOpponentsColor() {
        return opponentColor(myPiece().color());
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

    public int getPiecePos() {
        return board.getPiece(myPceID).getPos();
    }

    public boolean isConditional() {
        return !rawMinDistance.isUnconditional();
    }

    public boolean isUnconditional() {
        return rawMinDistance.isUnconditional();
    }

    public String getPathDescription() {
        if (getRawMinDistanceFromPiece().dist()==0)
            return "-" + myPiece().symbol()+squareName(myPos);
        if (getRawMinDistanceFromPiece().dist()>=INFINITE_DISTANCE)
            return "[INF]";
        String tome =  "-" + squareName(myPos)
                +"(D"+getRawMinDistanceFromPiece()+")";
                //.dist()+"/"+getRawMinDistanceFromPiece().nrOfConditions()
        return  "[" + getShortestPredecessors().stream()
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
                return "[" + getShortestPredecessors().stream()
                        .map(n -> n.getBriefPathDescription() + tome)
                        .collect(Collectors.joining("||"))
                        + "]";
        }
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
        for ( VirtualPieceOnSquare vPce : getShortestPredecessors() ) {
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

    /**
     * calc which 1st moves of my piece lead to here (on shortest ways) - obeying NoGos
     * @return */
    public Set<Move> getFirstMovesToHere() {
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
        for ( VirtualPieceOnSquare vPce : getShortestPredecessors() ) {
            res.addAll(getMoveOrigin(vPce));
        }
        return res;
    }

    private Set<Move> getMoveOrigin(VirtualPieceOnSquare vPce) {
        Set<Move> res = vPce.getFirstMovesToHere();
        if (res==null) {
            res = new HashSet<>();
            if ( rawMinDistance.dist()==1
                 || ( rawMinDistance.dist()==2 && !(rawMinDistance.nrOfConditions()==1) ) )
                res.add(new Move(myPiece().getPos(), myPos));  // a first "clean" move found
        }
        return res;
    }



    abstract List<VirtualPieceOnSquare> getShortestPredecessors();

    void resetChances() {
        chances = new ArrayList<>(MAX_INTERESTING_NROF_HOPS+1);
        for (int i = 0; i <= MAX_INTERESTING_NROF_HOPS; i++) {
            chances.add(i, new HashMap<>());
        }
    }

    public void addMoveAwayChance(final int benefit, final int inOrderNr, final Move m) {
        if (inOrderNr > MAX_INTERESTING_NROF_HOPS+1 || abs(benefit) < 2)
            return;
        assert(myPos==m.from());
        debugPrintln(DEBUGMSG_MOVEEVAL," Adding MoveAwayChance of " + benefit + "@"+inOrderNr+" for "+m+" of "+this+" on square "+ squareName(myPos)+".");
        addChance(benefit,inOrderNr,m);   // stored as normal chance, but only at the piece origin.
    }

    /**
     * add Chance of possible approaching (to eventually win) an opponents square (just the "upper hand"
     * or even with piece on it) with a certain benefit (relative eval, as always in board perspective)
     * in a suspected move distance
     * @param benefit
     * @param inOrderNr
     */
    public void addChance(final int benefit, final int inOrderNr) {
        if (inOrderNr>MAX_INTERESTING_NROF_HOPS || !getRawMinDistanceFromPiece().distIsNormal())
            return;
        // add chances for all first move options to here
        Set<Move> firstMovesToHere = getFirstMovesToHere();
        assert(firstMovesToHere!=null);
        for (Move m : firstMovesToHere) {   // was getFirstUncondMovesToHere(), but it locks out enabling moves if first move has a condition
            if ( !myPiece().isBasicallyALegalMoveForMeTo(m.to()) ) {
                // impossible move, square occupied. Still move needs to be entered in chance list, so that moving away from here also gets calculated
                addChance( 2 * checkmateEval(color()) , 0, m);
            }
            else {
                debugPrintln(DEBUGMSG_MOVEEVAL, "->" + m + "(" + benefit + "@" + inOrderNr + ")");
                addChance( benefit , inOrderNr, m);
                Square toSq = board.getBoardSquares()[m.to()];
                /* Option:Solved differently in loop over allsquares now
                ConditionalDistance toSqRmd = toSq.getvPiece(myPceID).getRawMinDistanceFromPiece();
                if ((toSqRmd.dist() == 1 || toSqRmd.dist() == 2) && toSqRmd.nrOfConditions() == 1) {
                    // add chances for condition of this "first" (i.e. second after condition) move, that make me come one step closer
                    int fromCond = getRawMinDistanceFromPiece().getFromCond(0);
                    if (fromCond != -1)
                        addChances2PieceThatNeedsToMove(benefit - (benefit >> 2), inOrderNr, fromCond);
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
                    addChances2PieceThatNeedsToMove(benefit>>1, inOrderNr, fromCond);
            } */
    }

    void addChances2PieceThatNeedsToMove(final int benefit, int inOrderNr, final Integer fromCond) {
        ChessPiece piece2Bmoved = board.getPieceAt(fromCond);
        if (piece2Bmoved==null) {
            if (DEBUGMSG_MOVEEVAL)
                System.err.println("Error in from-condition of " + this + ": points to empty square " + squareName(fromCond));
        }
        else {
            if (color() != piece2Bmoved.color() && inOrderNr>0)
                inOrderNr--;
            piece2Bmoved.addMoveAwayChance2AllMovesUnlessToBetween(benefit, inOrderNr, myPiece().getPos(), myPos);
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
