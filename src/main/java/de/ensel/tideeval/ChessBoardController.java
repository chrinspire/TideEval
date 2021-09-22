/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import de.ensel.chessgui.ChessEngine;
import de.ensel.chessgui.board.Piece;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.NO_PIECE_ID;

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
        //boardInfo.put("Nr. of moves & turn:", ""+chessBoard.getFullMoves()  );
        boardInfo.put("FEN:", chessBoard.getBoardFEN());
        boardInfo.put("Game state:", chessBoard.getGameState()+
                ( chessBoard.isGameOver() ? "" : (" turn: " + colorName(chessBoard.getTurnCol()) + "" ) ) );
        boardInfo.put("Piece value:", ""+chessBoard.boardEvaluation(1));
        boardInfo.put("Evaluation:", ""+chessBoard.boardEvaluation());
        return boardInfo;
    }

    @Override
    public HashMap<String,String> getSquareInfo(String square, String squareFrom) {
        HashMap<String,String> squareInfo = new HashMap<>();
        int pos = coordinateString2Pos(square);
        int squareFromPos = coordinateString2Pos(squareFrom);
        int squareFromPceId = chessBoard.getPieceIdAt(squareFromPos);
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
        Square sq = chessBoard.getBoardSquares()[pos];
        squareInfo.put("SquareId:",""+pos);
        squareInfo.put("Piece:",pceInfo);
        squareInfo.put("Base Value:",""+(pce==null ? "0" : pce.getBaseValue()));
        if (squareFromPceId!=NO_PIECE_ID) {
            squareInfo.put("Direct distance:", "" + sq.getShortestUnconditionalDistanceToPieceID(squareFromPceId));
            squareInfo.put("Conditional Distance:", "" + sq.getShortestConditionalDistanceToPieceID(squareFromPceId));
        }
        squareInfo.put("ClashResults:",""+ Arrays.toString(sq.evaluateClashes()) );
        squareInfo.put("ClashResult:",""+sq.clashEval());
        for (Iterator<ChessPiece> it = chessBoard.getPiecesIterator(); it.hasNext(); ) {
            ChessPiece p = it.next();
            if (p != null) {
                int pID = p.getPieceID();
                squareInfo.put("C.Distance for ("+pID+") " + p + ": ", "" + chessBoard.getBoardSquares()[pos].getShortestConditionalDistanceToPieceID(pID));
            }
        }
        return squareInfo;
    }
}
