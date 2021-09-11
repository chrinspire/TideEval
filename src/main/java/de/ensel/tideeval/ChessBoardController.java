/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import de.ensel.chessgui.ChessEngine;

import java.util.HashMap;

import static de.ensel.tideeval.ChessBasics.*;

public class ChessBoardController implements ChessEngine {
    ChessBoard chessBoard;

    @Override
    public boolean doMove(String move) {
        return chessBoard.doMove(move);
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
    public HashMap<String,String > getBoardInfo() {
        HashMap<String,String> boardInfo = new HashMap<>();
        boardInfo.put("BoardInfo of:", chessBoard.getBoardName().toString());
        boardInfo.put("Nr. of moves:", ""+chessBoard.getFullMoves());
        boardInfo.put("Turn:", colorName(chessBoard.getTurnCol()));
        boardInfo.put("Game state:", chessBoard.getGameState());
        boardInfo.put("Evaluation:", ""+chessBoard.boardEvaluation());
        return boardInfo;
    }

    @Override
    public HashMap<String,String> getSquareInfo(String square, String squareFrom) {
        HashMap<String,String> squareInfo = new HashMap<>();
        int pos = coordinateString2Pos(square);
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
        squareInfo.put("SquareId:",""+pos);
        squareInfo.put("Piece:",pceInfo);
        squareInfo.put("Base Value:",""+(chessBoard.getPieceAt(pos) == null ? "0" : chessBoard.getPieceAt(pos).getBaseValue()));
        squareInfo.put("Distance:",""+chessBoard.getBoardSquares()[pos].getShortestConditionalDistanceToPieceID(chessBoard.getPieceIdAt(coordinateString2Pos(squareFrom))));
        return squareInfo;
    }
}
