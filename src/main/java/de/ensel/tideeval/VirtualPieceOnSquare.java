/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.Distance.ANY;
import static de.ensel.tideeval.Distance.INFINITE_DISTANCE;
import static java.lang.Math.min;

public abstract class VirtualPieceOnSquare {
    protected final ChessBoard myChessBoard;
    protected final int myPceID;
    protected final int myPos;
    private int rel_eval;
    protected Distance rawMinDistance;   // distance in hops from corresponding real piece.
                                    // careful, this does not take into account if this piece is in the way of another of the same color
    protected Distance minDistance;  // == null if "dirty" (after change of rawMinDistance) other ==rawMinDistance oder +1, if same color Piece is on square
    private int latestUpdate;

    // propagate "values" / chances/threats/protections/pinnings in backward-direction
    //private final int[] valueInDir;  // must probably be changed later, because it depends on the Piece that comes that way, but lets try to keep this factor out

    public VirtualPieceOnSquare(ChessBoard myChessBoard, int newPceID,  int myPos) {
        this.myChessBoard = myChessBoard;
        this.myPos = myPos;
        myPceID = newPceID;
        latestUpdate = 0;
        //valueInDir = new int[MAXMAINDIRS];
        resetDistances();
        //resetValues();
        rel_eval = NOT_EVALUATED;
    }

    public static VirtualPieceOnSquare generateNew(ChessBoard myChessBoard, int newPceID, int myPos) {
        if (isSlidingPieceType(myChessBoard.getPiece(newPceID).getPieceType()))
            return new VirtualSlidingPieceOnSquare(myChessBoard,newPceID, myPos);
        if (isPawn(myChessBoard.getPiece(newPceID).getPieceType()))
            return new VirtualPawnPieceOnSquare(myChessBoard,newPceID, myPos);
        return new VirtualOneHopPieceOnSquare(myChessBoard,newPceID, myPos);
    }

    protected void setLastUpdateToNow() {
        latestUpdate = myChessBoard.getPiece(myPceID).latestUpdate();
    }


    //////
    ////// general Piece/moving related methods

    //////
    ////// handling of Distances

    public void pieceHasArrivedHere(int pid) {
        setLastUpdateToNow();
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION,"");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," ["+myPceID+":" );
        if (pid == myPceID) {
            //my own Piece is here - but I was already told and distance set to 0
            assert (rawMinDistance.dist() == 0);
            return;
        }
        // here I should update my own minDistance - necessary for same colored pieces that I am in the way now,
        // but this is not necessary as minDistance is safed "raw"ly without this influence and later it is calculated on top, if it is made "dirty"==null .
        minDistance = null;
        // inform neighbours that something has arrived here
        latestUpdate = myChessBoard.getPiece(myPceID).startNextUpdate();
        // reset values from this square onward (away from piece)
        propagateResetIfUSWToAllNeighbours();
        // start propagation of new values
        propagateDistanceChangeToAllNeighbours();   //0, Integer.MAX_VALUE );
        /*** breadth search propagation will follow later:
         // continue one by one
        int n=0;
        while (myPiece().queCallNext())
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," "+(n++));
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION," done: "+n);  ***/
        myChessBoard.getPiece(myPceID).endUpdate();

        /*debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," // and complete the propagation for 2+: ");
        latestUpdate = myChessBoard.getPiece(myPceID).startNextUpdate();
        propagateDistanceChangeToOutdatedNeighbours(2, Integer.MAX_VALUE );
        myChessBoard.getPiece(myPceID).endUpdate();
        */

        // TODO: Think&Check if this also works, if a piece has been beaten here
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"] ");
    }

    public void pieceHasMovedAway() {
        // inform neighbours that something has changed here
        // start propagation
        propagateDistanceChangeToAllNeighbours(); // 0, Integer.MAX_VALUE);
        /*** breadth search propagation will follow later:
         // continue one by one
        int n=0;
        while (myPiece().queCallNext())
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," "+(n++));
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION," done: "+n);  ***/
    }

    // fully set up initial distance from this vPces position
    public void myOwnPieceHasMovedHere() {
        // one extra piece or a new hop (around the corner or for non-sliding neighbours
        // treated just like sliding neighbour, but with no matching "from"-direction
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION,"");
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION,"["+pieceColorAndName(myChessBoard.getPiece(myPceID).getPieceType() )
                +"("+myPceID+"): propagate own distance: " );

        myChessBoard.getPiece(myPceID).startNextUpdate();
        setAndPropagateDistance(new Distance(0) );  // , 0, Integer.MAX_VALUE );
        myChessBoard.getPiece(myPceID).endUpdate();

        /*** breadth search propagation will follow later:
         // continue one by one
        int n=0;
        while (myPiece().queCallNext())
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," "+(n++));
        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION," done: "+n);  ***/

        debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION,"");
    }


    protected void resetDistances() {
        if (rawMinDistance ==null)
            rawMinDistance = new Distance();
        else
            rawMinDistance.reset();
        minDistance = null;
    }

    protected abstract void propagateDistanceChangeToAllNeighbours();  //final int minDist, final int maxDist);

    // not needed on higher level:  protected abstract void propagateDistanceChangeToOutdatedNeighbours();  //final int minDist, final int maxDist);

    // set up initial distance from this vPces position - restricted to distance depth change
    public abstract void setAndPropagateDistance(final Distance distance);  //, final int minDist, final int maxDist );

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
    private ChessPiece mySquarePiece() {
        return myChessBoard.getPieceAt(myPos);
    }

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
        if (rawMinDistance.dist()==0)
            return new Distance(1);  // almost nothing is closer than my neighbour
        // one hop from here is +1 or +2 if this piece first has to move away

        if (myChessBoard.hasPieceOfColorAt( myPiece().color(), myPos )) {
            // own piece is in the way
            int inc = movingOwnPieceFromSquareDistancePenalty() + 1;
            if (rawMinDistance.dist() == INFINITE_DISTANCE &&
                rawMinDistance.getDistanceUnderCondition() == INFINITE_DISTANCE)
                return new Distance(INFINITE_DISTANCE);  // can't get further away than infinite...
            // because own piece is in the way, we can only continue under the condition that it moves away
            return new Distance(INFINITE_DISTANCE,
                    myPos,ANY,
                    increaseIfPossible(
                            Math.min( rawMinDistance.getDistanceUnderCondition(),   // todo: implement chain of conditions (here the old condition is simply forgotten...)
                                        rawMinDistance.dist() ),
                            inc));
        } else {
            // square is free
            int inc = 1;
            if (rawMinDistance.dist() == INFINITE_DISTANCE &&
                rawMinDistance.getDistanceUnderCondition() == INFINITE_DISTANCE)
                return new Distance(INFINITE_DISTANCE);  // can't get worse
            // finally here return the "normal" case -> "my own Distance + 1"
            return new Distance(increaseIfPossible(rawMinDistance.dist(), inc),
                    rawMinDistance.getFromCond(),
                    rawMinDistance.getToCond(),
                    increaseIfPossible(
                            rawMinDistance.getDistanceUnderCondition(),
                            inc));
        }
    }

    protected int latestUpdate() {
        return latestUpdate;
    }

    /*
    public int realMinDistanceFromPiece() {
        if (minDistance.getUnconditionalDistance()==0)
            return 0;  // there is nothing closer than myself...
        if (minDistance.getUnconditionalDistance() == INFINITE_DISTANCE)
            return INFINITE_DISTANCE;  // can't get worse
        return minDistance.getUnconditionalDistance()+movingOwnPieceFromSquareDistancePenalty();
    }
    */

    public Distance getMinDistanceFromPiece() {
        // check if we already created the response object
        if (minDistance!=null)
            return minDistance;
        // no, its null=="dirty", we need a new one...
        // Todo: Increase 1 more if Piece is pinned to the king
        if (rawMinDistance.dist()==0
                || (rawMinDistance.dist()==INFINITE_DISTANCE && rawMinDistance.getDistanceUnderCondition()==INFINITE_DISTANCE) )
            minDistance=rawMinDistance;  // almost nothing is closer than my neighbour
        else {
            // one hop from here is +1 or +2 if this piece first has to move away
            int penalty = movingOwnPieceFromSquareDistancePenalty();
            if (rawMinDistance.dist() == INFINITE_DISTANCE)
                minDistance = new Distance(INFINITE_DISTANCE,
                        rawMinDistance.getFromCond(),
                        rawMinDistance.getToCond(),
                        increaseIfPossible(
                                rawMinDistance.getDistanceUnderCondition(),
                                penalty));
            else
                minDistance = new Distance(increaseIfPossible(rawMinDistance.dist(), penalty),
                    rawMinDistance.getFromCond(),
                    rawMinDistance.getToCond(),
                    increaseIfPossible(
                            rawMinDistance.getDistanceUnderCondition(),
                            penalty));
        }
        return minDistance;
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
