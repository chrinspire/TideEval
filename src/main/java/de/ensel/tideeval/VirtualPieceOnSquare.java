/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.Distance.ANY;
import static de.ensel.tideeval.Distance.INFINITE_DISTANCE;
import static java.lang.Math.abs;

public abstract class VirtualPieceOnSquare implements Comparable<VirtualPieceOnSquare> {
    protected final ChessBoard myChessBoard;
    protected final int myPceID;
    protected final int myPceType;
    protected final int myPos;

    private int relEval;

    public int getRelEval() {
        return relEval;
    }
    public void setRelEval(int relEval) {
        this.relEval = relEval;
    }

    protected Distance rawMinDistance;   // distance in hops from corresponding real piece.
                                    // careful, this does not take into account if this piece is in the way of another of the same color
    protected Distance minDistance;  // == null if "dirty" (after change of rawMinDistance) other ==rawMinDistance oder +1, if same color Piece is on square
    /**
     * "timestamp" when the rawMinDistance of this vPce was changed the last "time" (see ChessBoard: boardmoves+fineTicks)
     */
    private long latestChange;

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

    public void pieceHasArrivedHere(int pid) {
        setLatestChangeToNow();
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION,"");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," ["+myPceID+":" );
        if (pid==myPceID) {
            //my own Piece is here - but I was already told and distance set to 0
            assert (rawMinDistance.dist() == 0);
            return;
        }
        // here I should update my own minDistance - necessary for same colored pieces that I am in the way now,
        // but this is not necessary as minDistance is safed "raw"ly without this influence and later it is calculated on top, if it is made "dirty"==null .
        minDistance = null;
        // inform neighbours that something has arrived here
        latestChange = myChessBoard.getPiece(myPceID).startNextUpdate();
        // reset values from this square onward (away from piece)
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

        // TODO: Think&Check if this also works, if a piece has been beaten here
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"] ");
    }

    public void pieceHasMovedAway() {
        // inform neighbours that something has changed here
        // start propagation
        minDistance = null;
        myChessBoard.getPiece(myPceID).startNextUpdate();  //todo: think if startNextUpdate needs to be called one level higher, since introduction of board-wide hop-wise distance calculation
        if (rawMinDistance!=null && !rawMinDistance.isInfinite())
           propagateDistanceChangeToAllNeighbours(); // 0, Integer.MAX_VALUE);
        myChessBoard.getPiece(myPceID).endUpdate();  // todo: endUpdate necessary?
    }

    // fully set up initial distance from this vPces position
    public void myOwnPieceHasMovedHere(int frompos) {
        // one extra piece or a new hop (around the corner or for non-sliding neighbours
        // treated just like sliding neighbour, but with no matching "from"-direction
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, "");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, "[" + pieceColorAndName(myChessBoard.getPiece(myPceID).getPieceType())
                + "(" + myPceID + "): propagate own distance: ");

        myChessBoard.getPiece(myPceID).startNextUpdate();
        setDistance(new Distance(0));  // , 0, Integer.MAX_VALUE );
        if (frompos!=FROMNOWHERE) {
            resetMovepathBackTo(frompos);
            myChessBoard.getBoardSquares()[frompos].getvPiece(myPceID).propagateResetIfUSWToAllNeighbours();
        }
        setAndPropagateDistance(new Distance(0));  // , 0, Integer.MAX_VALUE );

        myChessBoard.getPiece(myPceID).endUpdate();
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
            rawMinDistance = new Distance();
        else
            rawMinDistance.reset();
        minDistance = null;
    }

    protected abstract void propagateDistanceChangeToAllNeighbours();  //final int minDist, final int maxDist);

    // not needed on higher level:  protected abstract void propagateDistanceChangeToOutdatedNeighbours();  //final int minDist, final int maxDist);

    // set up initial distance from this vPces position - restricted to distance depth change
    public abstract void setAndPropagateDistance(final Distance distance);  //, final int minDist, final int maxDist );

    /** sets, but does not propagate...
     * should normally not be called, but only in the right sequence with a later setAndPropagate
     * @param newDistance the new distance - will overwrite the vPieces current rawMinDistance
     */
    protected void setDistance(final @NotNull Distance newDistance) {
        rawMinDistance = newDistance;  //new Distance(0);
        minDistance = rawMinDistance;
    }

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

    public Distance minDistanceSuggestionTo1HopNeighbour() {
        // Todo: Increase 1 more if Piece is pinned to the king
        if (rawMinDistance==null) {
            // should normally not happen, but in can be the case for completely unset squares
            // e.g. a vPce of a pawn behind the line it cuold ever reach
            return new Distance(INFINITE_DISTANCE );
        }
        if (rawMinDistance.dist()==0)
            return new Distance(1);  // almost nothing is closer than my neighbour
        // one hop from here is +1 or +2 if this piece first has to move away

        if (myChessBoard.hasPieceOfColorAt( myPiece().color(), myPos )) {
            // own piece is in the way
            int inc = movingMySquaresPieceAwayDistancePenalty() + 1;
            if (rawMinDistance.dist() == INFINITE_DISTANCE &&
                rawMinDistance.getShortestDistanceEvenUnderCondition() == INFINITE_DISTANCE)
                return new Distance(INFINITE_DISTANCE);  // can't get further away than infinite...
            // because own piece is in the way, we can only continue under the condition that it moves away
            return new Distance(INFINITE_DISTANCE,
                    myPos,ANY,
                    increaseIfPossible(
                            Math.min( rawMinDistance.getShortestDistanceEvenUnderCondition(),   // todo: implement chain of conditions (here the old condition is simply forgotten...)
                                        rawMinDistance.dist() ),
                            inc));
        } else {
            // square is free
            int inc = 1;
            if (rawMinDistance.dist() == INFINITE_DISTANCE &&
                rawMinDistance.getShortestDistanceEvenUnderCondition() == INFINITE_DISTANCE)
                return new Distance(INFINITE_DISTANCE);  // can't get worse
            // finally here return the "normal" case -> "my own Distance + 1"
            return new Distance(increaseIfPossible(rawMinDistance.dist(), inc),
                    rawMinDistance.getFromCond(),
                    rawMinDistance.getToCond(),
                    increaseIfPossible(
                            rawMinDistance.getShortestDistanceOnlyUnderCondition(),
                            inc));
        }
    }

    long getLatestChange() {
        return latestChange;
    }

    public Distance getRawMinDistanceFromPiece() {
        if (rawMinDistance==null) { // not set yet at all
            rawMinDistance = new Distance(INFINITE_DISTANCE);
            minDistance=rawMinDistance;
        }
        return rawMinDistance;
    }

    public Distance getMinDistanceFromPiece() {
        // check if we already created the response object
        if (minDistance!=null)
            return minDistance;
        // no, its null=="dirty", we need a new one...
        // Todo: Increase 1 more if Piece is pinned to the king
        if (rawMinDistance==null) { // not set yet at all
            rawMinDistance = new Distance(INFINITE_DISTANCE);
            minDistance=rawMinDistance;
        }
        else if (rawMinDistance.dist()==0
                || (rawMinDistance.dist()==INFINITE_DISTANCE && rawMinDistance.getShortestDistanceEvenUnderCondition()==INFINITE_DISTANCE) )
            minDistance=rawMinDistance;  // almost nothing is closer than my neighbour
        else {
            // one hop from here is +1 or +2 if this piece first has to move away
            int penalty = movingMySquaresPieceAwayDistancePenalty();
            if (rawMinDistance.dist() == INFINITE_DISTANCE)
                minDistance = new Distance(INFINITE_DISTANCE,
                        rawMinDistance.getFromCond(),
                        rawMinDistance.getToCond(),
                        increaseIfPossible(
                                rawMinDistance.getShortestDistanceEvenUnderCondition(),
                                penalty));
            else if (penalty > 0)  // my own color piece, it needs to move away first
                minDistance = new Distance(INFINITE_DISTANCE,
                        myPos,
                        ANY,
                        increaseIfPossible(
                                rawMinDistance.getShortestDistanceEvenUnderCondition(),
                                penalty));
            else  {
                // square is free or opponents piece is here, but then I can beat it
                minDistance = new Distance(rawMinDistance );
            }
        }
        return minDistance;
    }

    public int movingMySquaresPieceAwayDistancePenalty() {
        // looks if this square is blocked by own color (but other) piece and needs to move away first
        if (myChessBoard.hasPieceOfColorAt( myPiece().color(), myPos )) {
            // TODO: make further calculation depending on whether mySquarePiece can move away
            // for now just assume it can move away, and this costs one move=>distance+1
            return 1;  // after taking the moving away nto account, it was already closer via another way.
            // TODO: somehow store this as a "condition" for the further distance calculations
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
        boolean equal = compareWithDebugMessage(this + "Piece Type", myPceType, other.myPceType);
        equal &= compareWithDebugMessage(this + "Piece Type", myPos, other.myPos);
        //still unused equal &= compareWithDebugMessage(this + "Relative Eval", relEval, other.relEval);
        equal &= compareWithDebugMessage(this + "Raw Minimal Distance", rawMinDistance, other.rawMinDistance);
        equal &= compareWithDebugMessage(this + "Minimal Distance", getMinDistanceFromPiece(), other.getMinDistanceFromPiece());
        return equal;
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
