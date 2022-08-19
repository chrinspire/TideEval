/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ChessBasics.ANY;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
import static java.lang.Math.abs;

public abstract class VirtualPieceOnSquare implements Comparable<VirtualPieceOnSquare> {
    protected final ChessBoard myChessBoard;
    protected final int myPceID;
    protected final int myPceType;
    protected final int myPos;

    private int relEval;  // is in board perspective like all evals! (not relative to the color, just relative as seen from the one piece)

    public int getRelEval() {
        return relEval;
    }

    public void setRelEval(int relEval) {
        if (this.relEval==relEval)
            return;
        this.relEval = relEval;
        //distances need potentially to be recalculated, as a bad relEval can influence if a piece can really go here, resp. the NoGo-Flag
        //ConditionalDistance oldSugg = suggestionTo1HopNeighbour;
        minDistsDirty();
        // hmm, was thought of as a optimization, but is at best equal or even a little % slower
        // if (oldSugg==null || !minDistanceSuggestionTo1HopNeighbour().cdEquals(oldSugg)) {
            // if we cannot tell or suggestion has changed, trigger updates
            latestChange = getOngoingUpdateClock();
            propagateDistanceChangeToAllNeighbours();
        //}
    }

    protected ConditionalDistance rawMinDistance;   // distance in hops from corresponding real piece.
                                                    // it does not take into account if this piece is in the way of another of the same color
    protected ConditionalDistance minDistance;  // == null if "dirty" (after change of rawMinDistance) other ==rawMinDistance oder +1/+n, if same color Piece is on square
    protected ConditionalDistance suggestionTo1HopNeighbour;  // what would be suggested to a "1-hop-neighbour",
                                                // this is also ==null if "dirty" and used for all types of pieces, even sliding
    /**
     * "timestamp" when the rawMinDistance of this vPce was changed the last "time" (see ChessBoard: boardmoves+fineTicks)
     */
    protected long latestChange;

    // propagate "values" / chances/threats/protections/pinnings in backward-direction
    //private final int[] valueInDir;  // must probably be changed later, because it depends on the Piece that comes that way, but lets try to keep this factor out

    public VirtualPieceOnSquare(ChessBoard myChessBoard, int newPceID, int pceType, int myPos) {
        this.myChessBoard = myChessBoard;
        this.myPceType = pceType;
        this.myPos = myPos;
        myPceID = newPceID;
        latestChange = 0;
        //valueInDir = new int[MAXMAINDIRS];
        resetDistances();
        //resetValues();
        relEval = NOT_EVALUATED;
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
        return myChessBoard.getPiece(myPceID).getLatestUpdate();
    }


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
        myChessBoard.getPiece(myPceID).startNextUpdate();
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
        propagateDistanceChangeToAllNeighbours();   //0, Integer.MAX_VALUE );
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
        myChessBoard.getPiece(myPceID).endUpdate();

        // TODO: Think&Check if this also works, if a piece was taken here
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"] ");
    }

    public void pieceHasMovedAway() {
        // inform neighbours that something has changed here
        // start propagation
        minDistsDirty();
        myChessBoard.getPiece(myPceID).startNextUpdate();  //todo: think if startNextUpdate needs to be called one level higher, since introduction of board-wide hop-wise distance calculation
        if (rawMinDistance!=null && !rawMinDistance.isInfinite())
           propagateDistanceChangeToAllNeighbours(); // 0, Integer.MAX_VALUE);
        myChessBoard.getPiece(myPceID).endUpdate();  // todo: endUpdate necessary?
    }

    // fully set up initial distance from this vPces position
    public void myOwnPieceHasSpawnedHere() {  //replaces myOwnPieceHasMovedHereFrom(int frompos) for spawn case. the normal case is replaced by orhestration viw chessPiece
        // one extra piece
        // treated just like sliding neighbour, but with no matching "from"-direction
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, "[" + pieceColorAndName(myChessBoard.getPiece(myPceID).getPieceType())
                + "(" + myPceID + "): propagate own distance: ");

        myChessBoard.getPiece(myPceID).startNextUpdate();
        rawMinDistance = new ConditionalDistance(0);  //needed to stop the reset-bombs below at least here
        minDistsDirty();
        setAndPropagateDistance(new ConditionalDistance(0));  // , 0, Integer.MAX_VALUE );
        myChessBoard.getPiece(myPceID).endUpdate();
    }

    // fully set up initial distance from this vPces position
    public void myOwnPieceHasMovedHereFrom(int frompos) {
        assert(frompos!=NOWHERE);
        // a piece moved  (around the corner or for non-sliding neighbours
        // treated just like sliding neighbour, but with no matching "from"-direction
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, "[" + pieceColorAndName(myChessBoard.getPiece(myPceID).getPieceType())
                + "(" + myPceID + "): propagate own distance: ");

        myChessBoard.getPiece(myPceID).startNextUpdate();
        rawMinDistance = new ConditionalDistance(0);  //needed to stop the reset-bombs below at least here
        minDistsDirty();
        resetMovepathBackTo(frompos);
        //TODO: the currently necessary reset starting from the frompos is very costly. Try
        // to replace it with propagaten that is able to correct dist values in both directions
        myChessBoard.getBoardSquares()[frompos].getvPiece(myPceID).resetDistances();
        myChessBoard.getBoardSquares()[frompos].getvPiece(myPceID).propagateResetIfUSWToAllNeighbours();
        setAndPropagateDistance(new ConditionalDistance(0));  // , 0, Integer.MAX_VALUE );

        myChessBoard.getPiece(myPceID).endUpdate();
    }


    protected void recalcRawMinDistanceFromNeighboursAndPropagate() {
        if ( recalcRawMinDistanceFromNeighbours()!=0 )
            propagateDistanceChangeToAllNeighbours();   // Todo!: recalcs twice, because this propagate turns into a recalcAndPropagete for Pawns... must be revised
        else
            propagateDistanceChangeToUninformedNeighbours();
    }


    protected void resetMovepathBackTo(int frompos) {
        // most Pieces do nothing special here
    }

    public String getShortestInPathDirDescription() {
        return TEXTBASICS_NOTSET;
    }

    public int getShortestConditionalInPathDirIndex() {
        return MULTIPLE;
    }

    protected void resetDistances() {
        setLatestChangeToNow();
        if (rawMinDistance==null)
            rawMinDistance = new ConditionalDistance();
        else
            rawMinDistance.reset();
        minDistsDirty();
    }

    protected void minDistsDirty() {
        minDistance = null;
        suggestionTo1HopNeighbour = null;
        // TODO: check idea:
        //  if ("dist"==1)
        //      myPiece().bestMoveRelEvalDirty();
    }

    protected abstract void propagateDistanceChangeToAllNeighbours();

    protected abstract void propagateDistanceChangeToUninformedNeighbours();

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
        return myChessBoard.getPiece(myPceID);
    }

    /**
     * mySquarePiece()
     * @return reference to the piece sitting on my square, or null if empty
     */
    ChessPiece mySquarePiece() {
        return myChessBoard.getPieceAt(myPos);
    }

    boolean mySquareIsEmpty() {
        return myChessBoard.isSquareEmpty(myPos);
    }

    // setup basic neighbourhood network
    public void addSingleNeighbour(VirtualPieceOnSquare newVPiece) {
        ((VirtualOneHopPieceOnSquare)this).addSingleNeighbour( (VirtualOneHopPieceOnSquare)newVPiece );
    }

    public void addSlidingNeighbour(VirtualPieceOnSquare neighbourPce, int direction) {
        ((VirtualSlidingPieceOnSquare)this).addSlidingNeighbour( (VirtualSlidingPieceOnSquare)neighbourPce, direction );
    }


    public static int increaseIfPossible(int i, int plus) {
        if (i+plus<i)
            return Integer.MAX_VALUE;
        return i+plus;
    }

    /**
     * tells the distance after moving away from here, considering if a Piece is in the way here
     * @return a "safe"=new ConditionalDistance
     */
    public ConditionalDistance minDistanceSuggestionTo1HopNeighbour() {
        // Todo: Increase 1 more if Piece is pinned to the king
        if (rawMinDistance==null) {
            // should normally not happen, but in can be the case for completely unset squares
            // e.g. a vPce of a pawn behind the line it could ever reach
            return new ConditionalDistance();
        }
        // good case: it's already calculated
        if (suggestionTo1HopNeighbour!=null)
            return suggestionTo1HopNeighbour;

        if (rawMinDistance.dist()==0)
            suggestionTo1HopNeighbour = new ConditionalDistance(1);  // almost nothing is closer than my neighbour  // TODO. check if my piece can move away at all (considering king pins e.g.)
        else {
            // TODO: the following doesn't work yet, because breadth propagation calls are already qued after the relEval is calculated
            int inc = 0; //(getRelEval()==0 || getRelEval()==NOT_EVALUATED) ? 0 : MAX_INTERESTING_NROF_HOPS-1;

            if (rawMinDistance.isInfinite())
                suggestionTo1HopNeighbour = new ConditionalDistance(INFINITE_DISTANCE);  // can't get further away than infinite...

            // one hop from here is +1 or +2 if this piece first has to move away
            else if (myChessBoard.hasPieceOfColorAt(myPiece().color(), myPos)) {
                // one of my same colored pieces are in the way
                int penalty = movingMySquaresPieceAwayDistancePenalty();
                if (penalty<INFINITE_DISTANCE) {
                    inc += penalty + 1;
                    suggestionTo1HopNeighbour = new ConditionalDistance(rawMinDistance, inc, myPos, ANY, myPiece().color());
                } else
                    suggestionTo1HopNeighbour = new ConditionalDistance();
                // because own piece is in the way, we can only continue under the condition that it moves away
            } else {
                // square is free (or of opposite color and to be beaten)
                inc += 1; // so finally here return the "normal" case -> "my own Distance + 1"
                suggestionTo1HopNeighbour = new ConditionalDistance(rawMinDistance, inc);
                if (!evalIsOkForColByMin(getRelEval(), myPiece().color(), EVAL_TENTH))
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
            rawMinDistance = new ConditionalDistance();
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
            rawMinDistance = new ConditionalDistance();
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
        if ( !evalIsOkForColByMin( getRelEval(), myPiece().color(),EVAL_TENTH ) )
            minDistance.setNoGo(myPos);
        return minDistance;
    }

    public int movingMySquaresPieceAwayDistancePenalty() {
        // looks if this square is blocked by own color (but other) piece and needs to move away first
        if (myChessBoard.hasPieceOfColorAt( myPiece().color(), myPos )) {
            // make further calculation depending on whether mySquarePiece can move away
            final ChessPiece pieceHere = myChessBoard.getPieceAt(myPos);
            if ( pieceHere.canMoveAwayReasonably() )
                return 1;
            // it has no good place to go, so it will probably not go away.
            return 1; //1=deactivated, instead of better approaches (that do not work in the overall update mechanism,
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
        return "vPce("+myPceID+") on ["+ squareName( myPos)+"] "
                + rawMinDistance + " away from "
                + pieceColorAndName(myChessBoard.getPiece(myPceID).getPieceType()) +
                '}';
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
        /*if (this.myPiece().getValue()
            > other.myPiece().getValue())
            return -1;
        if (this.myPiece().getValue()
                < other.myPiece().getValue())
            return 1;
        return 0;*/
    }

    @Override
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
        return myChessBoard.getPiece(myPceID).getPos();
    }

    public boolean isConditional() {
        return !rawMinDistance.isUnconditional();
    }


    /*  zum Vergleich: Minimum mit Streams implementiert, allerdings haben wir nun die komplexeren, mehrdimensionalen Distances, für die das Minimum "gemerged" werden muss
    List<VirtualPieceOnSquare> destinations = new ArrayList<>();
    public VirtualPieceOnSquare getBestNeighbour() {
        return destinations.parallelStream()
                .reduce((a,b)-> a.compareTo(b) > 0 ? a : b )
                .get();
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
