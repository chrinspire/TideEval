/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import de.ensel.chessgui.ChessEngine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.MAX_INTERESTING_NROF_HOPS;
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
        chessBoard = new ChessBoard(chessBasicRes.getString("chessboard.initialName"),fen);
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
        boardInfo.put("Attack balance on opponent side, king area / defend own king:", ""
                + chessBoard.evaluateOpponentSideAttack() + ", "
                + chessBoard.evaluateOpponentKingAreaAttack() + " / "
                + chessBoard.evaluateOwnKingAreaDefense());
        boardInfo.put("Evaluation (overall - piece values, max clashes, mobility:", ""
                + chessBoard.boardEvaluation()+" - "
                + chessBoard.boardEvaluation(1) + ", "
                + chessBoard.evaluateMaxClashes() + ", "
                + chessBoard.boardEvaluation(4));
        return boardInfo;
    }

    @Override
    public HashMap<String,String> getSquareInfo(String square, String squareFrom) {
        HashMap<String,String> squareInfo = new HashMap<>();
        int pos = coordinateString2Pos(square);
        int squareFromPos = coordinateString2Pos(squareFrom);
        int squareFromPceId = chessBoard.getPieceIdAt(squareFromPos);
        // basic square name (is now in headline)
        // does it contain a chess piece?
        ChessPiece pce = chessBoard.getPieceAt(pos);
        final String pceInfo;
        if (pce!=null) {
            pceInfo = pce.toString();
            squareInfo.put("Square's piece mobility:", "" + pce.getMobilities() + " "+Arrays.toString(pce.getRawMobilities()) );
            squareInfo.put("Square's piece last update:", "" + (pce==null ? "-" : pce.getLatestUpdate() ) );
        }
        else
            pceInfo = chessBasicRes.getString("pieceCharset.empty");
        // squareInfo.put("Piece:",pceInfo);
        Square sq = chessBoard.getBoardSquares()[pos];
        //squareInfo.put("SquareId:",""+pos+" = "+ squareName(pos));
        squareInfo.put("Base Value:",""+(pce==null ? "0" : pce.getBaseValue()));
        squareInfo.put("t_LatestClashUpdate:", ""+sq.getLatestClashResultUpdate());
        if (squareFromPceId!=NO_PIECE_ID) {
            VirtualPieceOnSquare vPce = sq.getvPiece(squareFromPceId);
            squareInfo.put("* Sel. piece's Uncond. Distance:", "" + sq.getUnconditionalDistanceToPieceIdIfShortest(squareFromPceId));
            int d = sq.getDistanceToPieceId(squareFromPceId);
            squareInfo.put("* Sel. piece's Distance:", "" + ( sq.hasNoGoFromPieceId(squareFromPceId) ? -d : d ) );
            squareInfo.put("* Sel. piece's update age on square:", "" + (chessBoard.getUpdateClock() - vPce.getLatestChange()) );
            squareInfo.put("* Sel.d piece's shortest cond. in-path from: ", "" + vPce.getShortestInPathDirDescription() );
            int relEval = vPce.getRelEval();
            squareInfo.put("* Result if sel. piece moves on square:", "" + (relEval==NOT_EVALUATED?0:relEval) );
            squareInfo.put("* Chances on square:", "" + vPce.getClosestChanceReachout() );
        }

        // information specific to this square
        squareInfo.put("Attacks by white:",""+ sq.countDirectAttacksWithColor(WHITE) );
        squareInfo.put("Attacks by black:",""+ sq.countDirectAttacksWithColor(BLACK) );
        squareInfo.put("ClashResults:","" + Arrays.toString(sq.getClashes()) );
        squareInfo.put("Clash Eval (Overall):",""+sq.clashEval());
        squareInfo.put("Clash Eval (Direct):",""+sq.clashEval(1));
        squareInfo.put("Clash Future Eval:",""+ sq.warningLevel() + " " + Arrays.toString(sq.futureClashEval() ) );
        squareInfo.put("Coverage by White:",""+sq.getClosestChanceReachout(WHITE) + " " + sq.getClosestChanceMove(WHITE)
                + " "+sq.getCoverageInfoByColorForLevel(WHITE, 1)
                +" "+sq.getCoverageInfoByColorForLevel(WHITE, 2)
                +( MAX_INTERESTING_NROF_HOPS>3 ? (" "+sq.getCoverageInfoByColorForLevel(WHITE, 3)) : "") );
        squareInfo.put("Coverage by Black:",""+sq.getClosestChanceReachout(BLACK) + " " + sq.getClosestChanceMove(BLACK) + " "
                +sq.getCoverageInfoByColorForLevel(BLACK, 1)
                +" "+sq.getCoverageInfoByColorForLevel(BLACK, 2)
                +( MAX_INTERESTING_NROF_HOPS>3 ? (" "+sq.getCoverageInfoByColorForLevel(BLACK, 3)) : "") );
        squareInfo.put("Latest Update:",""+sq.getLatestClashResultUpdate());

        // distance info for alle pieces in relation to this square
        for (Iterator<ChessPiece> it = chessBoard.getPiecesIterator(); it.hasNext(); ) {
            ChessPiece p = it.next();
            if (p != null) {
                int pID = p.getPieceID();
                int distance = sq.getDistanceToPieceId(pID);
                if (distance<ConditionalDistance.INFINITE_DISTANCE)
                    squareInfo.put("z " + p + " ("+pID+") Distance: ",
                            "" + sq.getConditionalDistanceToPieceId(pID)
                                    + " relEval=" + (sq.getvPiece(pID).getRelEval()==NOT_EVALUATED? "n.e." : sq.getvPiece(pID).getRelEval())
//                                    + " from: " + sq.getvPiece(pID).getReducedPathDescription()
                              + " " + sq.getvPiece(pID).getShortestInPathDirDescription()
                              //      + ":" + sq.getvPiece(pID).getBriefPathDescription()
                              + " " + sq.getvPiece(pID).getClosestChanceReachout()
                              + ":" + sq.getvPiece(pID).getChances()
                              //+ " " + sq.getvPiece(pID).getDistanceDebugDetails()
                    );
            }
        }
        return squareInfo;
    }
}
