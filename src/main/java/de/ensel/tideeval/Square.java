/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.ArrayList;
import java.util.List;

import static de.ensel.tideeval.ChessBoard.NOPIECE;

public class Square {
    final ChessBoard myChessBoard;
    private final int myPos; // mainly for debugging and output
    private int myPieceID;
    private final List<VirtualPieceOnSquare> vPieces;  // TODO: change to plain old []
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
        myPieceID = NOPIECE;
        vPieces = new ArrayList<>(ChessBasics.MAX_PIECES);
    }

    public void prepareNewPiece(int newPceID) {
        vPieces.add(newPceID, new VirtualPieceOnSquare( myChessBoard, newPceID, getMyPos() ));
    }

    public void spawnPiece(int pid) {
        //the Piece had not existed so far, so prefill the move-net
        myPieceID = pid;
        vPieces.get(pid).setInitialDistance(0);
        for (VirtualPieceOnSquare vPce : vPieces) {
            // tell all pieces that something new is here - and possibly in the way...
            vPce.pieceHasArrivedHere(pid);
        }
    }

    public int getMyPos() {
        return myPos;
    }

    public int getMyPieceID() {
        return myPieceID;
    }

    public int getDistanceToPieceID(int pceId) {
        return vPieces.get(pceId).realMinDistanceFromPiece();
    }

}
