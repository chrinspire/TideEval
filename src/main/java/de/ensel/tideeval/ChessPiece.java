/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.MAX_INTERESTING_NROF_HOPS;

public class ChessPiece {
    final ChessBoard myChessBoard;
    private final int myPceTypeNr;
    private final int myPceID;

    ChessPiece(ChessBoard myChessBoard, int pceTypeNr, int pceID) {
        this.myChessBoard = myChessBoard;
        myPceTypeNr = pceTypeNr;
        myPceID = pceID;
    }

    @Override
    public String toString() {
        return pieceColorAndName(myPceTypeNr);
    }

    public boolean color() {
        return colorOfPieceTypeNr(myPceTypeNr);
    }

    int getBaseValue() {
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
            mobilityCountForHops[sq.getDistanceToPieceID(myPceID)]++;
        }
        return mobilityCountForHops;
    }

}
