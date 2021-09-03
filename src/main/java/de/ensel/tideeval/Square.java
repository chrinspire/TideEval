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
    VirtualPieceOnSquare getvPiece(int pid) {
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

    Square(ChessBoard myChessBoard, int myPos) {
        this.myChessBoard = myChessBoard;
        this.myPos = myPos;
        myPieceID = NOPIECE;
        vPieces = new ArrayList<>(ChessBasics.MAX_PIECES);
    }

    void prepareNewPiece(int newPceID) {
        vPieces.add(newPceID, new VirtualPieceOnSquare( myChessBoard, newPceID, getMyPos() ));
    }

    void spawnPiece(int pid) {
        //the Piece had not existed so far, so prefill the move-net
        myPieceID = pid;
        vPieces.get(pid).setDistance(0);
        for (VirtualPieceOnSquare vPce : vPieces) {
            // tell all pieces that something new is here - and possibly in the way...
            if (vPce !=null)
                vPce.pieceHasArrivedHere(pid);
        }
    }

    void propagateMyValue() {
        ChessPiece p = myChessBoard.getPiece(myPieceID);
        int value = p==null ? 0 : p.getBaseValue();
        //TODO: get better values (from beating-situation here or from what Piece is able to to etc...)
        for (VirtualPieceOnSquare vPce : vPieces) {
            // propaget value over all vpieces
            if (vPce !=null)
                vPce.propagateMyValue(value);
        }
    }

    public void removePiece(int pceID) {
        vPieces.set(pceID,null);
    }

    void pieceMovedCloser(int pid) {
        //the Piece had a hop-distance of one and now moved on my square
        if (myPieceID !=NOPIECE) {
            // this piece is beaten...
            // TODO:  Hatdas entfernen auswirkungen auf die Daten der Nachbarn? oder wird das durch das einsetzen der neuen Figur gelöst?
            myChessBoard.removePiece(myPieceID);
        } else {
            // TODO: does not work for rook, when casteling - why not?
            //  assert (vPieces.get(pid).realMinDistanceFromPiece() == 1);
        }
        spawnPiece(pid);
    }

    void emptySquare() {
        //the Piece had not existed so far, so prefill the move-net
        myPieceID = NOPIECE;
        for (VirtualPieceOnSquare vPce : vPieces) {
            // tell all pieces that something has disappeared here - and possibly frees the way...
            if (vPce!=null)
                vPce.pieceHasMovedAway();
        }
        //TODO: find more efficient way, when the other pieces recalculate their distances,
        // the piece should already be placed "in the way" at the new square
    }

    int getMyPos() {
        return myPos;
    }

    int getPieceID() {
        return myPieceID;
    }

    int getDistanceToPieceID(int pceId) {
        return vPieces.get(pceId).realMinDistanceFromPiece();
    }

}
