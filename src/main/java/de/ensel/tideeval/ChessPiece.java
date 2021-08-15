/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import static de.ensel.tideeval.ChessBasics.colorOfPieceTypeNr;
import static de.ensel.tideeval.ChessBasics.pieceColorAndName;

public class ChessPiece {
    private final int myPceTypeNr;
    private final int myPceID;

    ChessPiece(int pceTypeNr, int pceID) {
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
}
