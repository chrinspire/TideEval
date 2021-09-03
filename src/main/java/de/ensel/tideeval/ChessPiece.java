/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import javax.swing.*;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.MAX_INTERESTING_NROF_HOPS;

public class ChessPiece {
    final ChessBoard myChessBoard;
    private final int myPceTypeNr;
    private final int myPceID;
    private int myPos;

    public int getPieceTypeNr() {
        return myPceTypeNr;
    }

    public int getPieceID() {
        return myPceID;
    }


    ChessPiece(ChessBoard myChessBoard, int pceTypeNr, int pceID, int pcePos) {
        this.myChessBoard = myChessBoard;
        myPceTypeNr = pceTypeNr;
        myPceID = pceID;
        myPos = pcePos;
    }

    @Override
    public String toString() {
        return pieceColorAndName(myPceTypeNr);
    }

    public boolean color() {
        return colorOfPieceTypeNr(myPceTypeNr);
    }

    int getBaseValue() {
        if (isPieceTypeNrBlack(myPceTypeNr))
            return -getPieceBaseValue(myPceTypeNr);
        return getPieceBaseValue(myPceTypeNr);
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
            int distance = sq.getDistanceToPieceID(myPceID);
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
        // actually nothing to clean up here...
    }

    public boolean isWhite() {
        return ChessBasics.isPieceTypeNrWhite(myPceTypeNr);
    }
}
