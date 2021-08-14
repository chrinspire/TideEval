/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import static de.ensel.tideeval.ChessBasics.pieceColorAndName;

public class ChessPiece {
    int myPceTypeNr;
    ChessPiece(int pceTypeNr) {
        myPceTypeNr = pceTypeNr;
    }

    @Override
    public String toString() {
        return pieceColorAndName(myPceTypeNr);
    }
}
