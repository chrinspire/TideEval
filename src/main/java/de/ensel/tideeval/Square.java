/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.ArrayList;
import java.util.List;

public class Square {
    private int myPos; // mainly for debugging and output
    private int myPieceID ;
    private int myPieceTypeNr;
    private List<VirtualPieceOnSquare> vPiece;
    public VirtualPieceOnSquare getvPiece(int pid) {
        return vPiece.get(pid);
    }
    /*public void setvPiece(VirtualPieceOnSquare vPiece) {
        this.vPiece = vPiece;
    }*/

    /*
    boolean hasWhitePiece();
    boolean hasBlackPiece();
    boolean hasExactPieceType(int figType);
    boolean hasPieceOfAnyColor(int figType);

    boolean hasNoWhitePiece();
    boolean hasNoBlackPiece();
    boolean isEmptySquare();

    boolean hasOwnPiece(boolean myColor);
    boolean hasNoOpponentPiece(boolean myColor);
    boolean hasOwnBishop(boolean myColor);
    boolean hasOwnPawn(boolean myColor);

    boolean hasPawn();

    boolean hasOpponentPiece(boolean myColor);
    boolean hasNoOwnPiece(boolean myColor);
    boolean hasOpponentPieceTypeNr(boolean myColor, int pieceNr);
    //boolean hasOpponentPieceWithMatchingCD(int pos, boolean myColor, int wantedCD);
    */

    public Square(int myPos) {
        this.myPos = myPos;
        myPieceID = -1;
        myPieceTypeNr = ChessBasics.EMPTY;
        vPiece = new ArrayList<VirtualPieceOnSquare>(ChessBasics.MAX_PIECES);
    }

    public void prepareNewPiece(int newPceID, int pceTypeNr) {
        myPieceTypeNr = pceTypeNr;
        vPiece.add(newPceID, new VirtualPieceOnSquare());
    }

    public void spawnPiece(int pid) {
        //the Piece had not existed so far, so prefill the move-net
        myPieceID = pid;
        vPiece.get(pid).setDistance(0);
    }
}
