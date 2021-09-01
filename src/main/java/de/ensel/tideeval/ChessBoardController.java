/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import de.ensel.gui.ChessEngine;

import static de.ensel.tideeval.ChessBasics.*;

public class ChessBoardController implements ChessEngine {
    ChessBoard chessBoard;

    @Override
    public void doMove(String move) {
        chessBoard.doMove(move);
    }

    @Override
    public String getMove() {
        if (chessBoard.isGameOver())
            return null;
        //TODO: chessBoard.go();
        // needs to be replaced by async functions, see interface
        return null;
    }

    @Override
    public void setBoard(String fen) {
        chessBoard = new ChessBoard(chessBasicRes.getString("chessboard.initialName"));
    }

    @Override
    public String getBoard() {
        return chessBoard.getBoardFEN();
    }

    @Override
    public String getBoardInfo() {
        return "BoardInfo of " + chessBoard.getBoardName()
                + ": \nNr. of moves: " + chessBoard.getFullMoves()
                + "\nTurn: " + colorName(chessBoard.getTurnCol())
                + "\n" + chessBoard.getGameState()
                + "\nEvaluation: " + chessBoard.boardEvaluation();
    }

    @Override
    public String getSquareInfo(String field) {
        int pos = coordinateString2Pos(field);
        // basic square name
        final String squareName = squareName(pos) + ": ";
        // does it contain a chess piece?
        ChessPiece pce = chessBoard.getPieceAt(pos);
        final String pceInfo;
        if (pce!=null) {
            pceInfo = pce.toString();
        } else {
            pceInfo = chessBasicRes.getString("pieceCharset.empty");
        }
        return squareName + pceInfo;
    }
}
