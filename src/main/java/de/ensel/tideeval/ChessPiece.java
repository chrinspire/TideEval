/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.*;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;

public class ChessPiece {
    final ChessBoard myChessBoard;
    private final int myPceType;
    private final int myPceID;
    private int myPos;
    private long latestUpdate;   // virtual "time"stamp (=consecutive number) for last/ongoing update.

    public long getLatestUpdate() {
        return latestUpdate;
    }

    public long startNextUpdate() {
        latestUpdate = myChessBoard.nextUpdateClockTick();
        return latestUpdate;
    }

    public void endUpdate() {
    }

    public int getPieceType() {
        return myPceType;
    }

    public int getPieceID() {
        return myPceID;
    }


    ChessPiece(ChessBoard myChessBoard, int pceTypeNr, int pceID, int pcePos) {
        this.myChessBoard = myChessBoard;
        myPceType = pceTypeNr;
        myPceID = pceID;
        myPos = pcePos;
        latestUpdate = 0;
    }

    @Override
    public String toString() {
        return pieceColorAndName(myPceType);
    }

    public boolean color() {
        return colorOfPieceType(myPceType);
    }

    int getBaseValue() {
        return getPieceBaseValue(myPceType);
    }

    public int getValue() {
        // Todo calc real/better value of piece
        return getPieceBaseValue(myPceType);
    }
    /**
     * getSimpleMobilities()
     * @return int[] for mobility regarding hopdistance i (not considering whether there is chess at the moment)
     * - i.e. result[0] reflects how many moves the piece can make fro where it stands.
     * - result[1] is how many squares can be reached in 2 hops
     *   (so one move of the above + one more OR because another figure moves out of the way)
     */
    int[] getSimpleMobilities() {
        // TODO: discriminate between a) own figure in the way (which i can control) or uncovered opponent (which I can take)
        // and b) opponent blocking the way (but which also "pins" him there to keep it up)
        int[] mobilityCountForHops = new int[MAX_INTERESTING_NROF_HOPS];
        for( Square sq : myChessBoard.getBoardSquares() ) {
            int distance = sq.getShortestUnconditionalDistanceToPieceID(myPceID);
            if (distance!=0 && distance<=MAX_INTERESTING_NROF_HOPS)
                mobilityCountForHops[distance-1]++;
        }
        return mobilityCountForHops;
    }

    public int getPos() {
        return myPos;
    }

    public void setPos(int pos) {
        myPos = pos;
    }

    /**
     * die() piece is EOL - clean up
     */
    public void die() {
        // little to clean up here...
        myPos = -1;
    }

    public boolean isWhite() {
        return ChessBasics.isPieceTypeWhite(myPceType);
    }


    /**** started to build am ordered que here - to implement a breadth search for propagation ****/

    private static final int QUE_MAX_DEPTH = MAX_INTERESTING_NROF_HOPS+3;
    private final List<List<Runnable>> searchPropagationQues = new ArrayList<>();
    {
        // prepare List of HashSets
        for (int i=0; i<QUE_MAX_DEPTH+1; i++) {
            searchPropagationQues.add(new ArrayList<>());
        }
    }

    void quePropagation(final int queIndex, final Runnable function) {
        searchPropagationQues.get(Math.min(queIndex, QUE_MAX_DEPTH)).add(function);
    }

    /**
     * Execute one stored function call from the que with lowest available index
     * it respects the board-wide currentDistanceCalcLimit() and stops with false
     * if it has reached the limit
     * returns if one propagation was executed or not.
     */
    public boolean queCallNext() {
        List<Runnable> searchPropagationQue;
        for (int i = 0, quesSize = Math.min(myChessBoard.currentDistanceCalcLimit(), searchPropagationQues.size());
             i < quesSize; i++) {
            searchPropagationQue = searchPropagationQues.get(i);
            if (searchPropagationQue != null && searchPropagationQue.size() > 0 ) {
                //System.out.print(" (L"+i+")");
                searchPropagationQue.get(0).run();
                searchPropagationQue.remove(0);
                return true;  // end loop, we only work on one at a time.
            }
        }
        return false;
    }

    /**
     * executes all open distance calculations and propagations up to the
     * boards currentDistanceCalcLimit()
     */
    public void continueDistanceCalc() {
        int n = 0;
        while (queCallNext())
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, " " + (n++));
        if (n>0)
            debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, " done: " + n);
    }

    public boolean pawnCanTheoreticallyReach(int p) {
        //TODO: should be moved to a subclass e.g. PawnChessPiece
        assert(colorlessPieceType(myPceType)==PAWN);
        int deltaFiles = Math.abs( fileOf(myPos) - fileOf(p));
        int deltaRanks;
        if (this.isWhite())
            deltaRanks = rankOf(p)-rankOf(myPos);
        else
            deltaRanks = rankOf(myPos)-rankOf(p);
        return (deltaFiles<=deltaRanks);
    }
}
