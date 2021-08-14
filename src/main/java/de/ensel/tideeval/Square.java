/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.ArrayList;
import java.util.List;

public class Square {
    ChessBoard myChessBoard;
    private int myPos; // mainly for debugging and output
    private int myPieceID ;
    private List<VirtualPieceOnSquare> vPieces;
    public VirtualPieceOnSquare getvPiece(int pid) {
        return vPieces.get(pid);
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

    public Square(ChessBoard myChessBoard, int myPos) {
        this.myChessBoard = myChessBoard;
        this.myPos = myPos;
        myPieceID = -1;
        vPieces = new ArrayList<>(ChessBasics.MAX_PIECES);
    }

    public void prepareNewPiece(int newPceID) {
        vPieces.add(newPceID, new VirtualPieceOnSquare( myChessBoard, newPceID ));
    }

    public void spawnPiece(int pid) {
        //the Piece had not existed so far, so prefill the move-net
        myPieceID = pid;
        vPieces.get(pid).setInitialDistance(0);
    }
}
