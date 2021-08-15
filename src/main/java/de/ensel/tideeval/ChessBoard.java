/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static de.ensel.tideeval.ChessBasics.*;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.text.MessageFormat.*;

public class ChessBoard {

    private int whiteKingPos;
    private int blackKingPos;
    public int getWhiteKingPos() {
        return whiteKingPos;
    }
    public int getBlackKingPos() {
        return blackKingPos;
    }

    private int whiteKingChecks;
    private int blackKingChecks;
    public boolean isCheck(boolean col) {
        return isWhite(col) ? whiteKingChecks > 0
                            : blackKingChecks > 0;
    }

    /*public int getPieceNrCounterForColor(int figNr, boolean whitecol) {
        return whitecol ? countOfWhiteFigNr[abs(figNr)]
                        : countOfBlackFigNr[abs(figNr)];
    }*/
    //int getPieceNrCounter(int pieceNr);

    /**
     * keep all Pieces on Board
     */
    ChessPiece[] piecesOnBoard;
    private int nextFreePceID;

    public ChessPiece getPiece(int pceID) {
        assert(pceID<nextFreePceID);
        return piecesOnBoard[pceID];
    }

    public static final int NOPIECE = -1;

    ChessPiece getPieceAt(int pos) {
        int pceID = getPieceIdAt(pos);
        if (pceID == NOPIECE)
            return null;
        return piecesOnBoard[pceID];
    }

    public int getPieceIdAt(int pos) {
        return boardSquares[pos].getMyPieceID();
    }

    private int countOfWhiteFigures;
    private int countOfBlackFigures;
    public int getPieceCounter()  {
        return countOfWhiteFigures + countOfBlackFigures;
    }
    public int getPieceCounterForColor(boolean whitecol)  {
        return whitecol ? countOfWhiteFigures
                        : countOfBlackFigures;
    }

    boolean hasPieceOfColorAt(boolean col, int pos) {
        if (boardSquares[pos].getMyPieceID()==NOPIECE)
            return false;
        return (getPieceAt(pos).color() == col);
    }


    private boolean gameOver;
    public boolean isGameOver() {
        return gameOver;
    }

    void checkAndEvaluateGameOver() {    // called to check+evaluate if there are no more moves left or 50-rules-move is violated
        if ( countOfWhiteFigures <= 0 || countOfBlackFigures <= 0 ) {
            gameOver = true;
            return;
        }
        gameOver = false;
        // TODO: real game status check...
    }
    //boolean accessibleForKing(int pos, boolean myColor);
    //boolean coveredByMe(int pos, boolean color);
    //boolean coveredByMeExceptOne(int pos, boolean color, int pieceNr);

    public int distanceToKing(int pos, boolean kingCol) {
        int dx, dy;
        // Achtung, Implementierung passt sich nicht einer veränderten Boardgröße an.
        if (kingCol == WHITE) {
            dx = abs((pos & 7) - (whiteKingPos & 7));
            dy = abs((pos >> 3) - (whiteKingPos >> 3));
        } else {
            dx = abs((pos & 7) - (blackKingPos & 7));
            dy = abs((pos >> 3) - (blackKingPos >> 3));
        }
        return max(dx, dy);
    }

    //Piece getPieceOnSquare(int pos);
    //int getPieceNrOnSquare(int pos);

    Square[] boardSquares;

    private boolean turn;
    public boolean isTurn() {
        return turn;
    }
    //void setTurn(boolean turn);


    private StringBuffer boardName;

    StringBuffer getBoardName() {
        return boardName;
    }

    StringBuffer getShortBoardName() {
        return boardName;
    }

    String getBoardFEN() {
        return null;  // TODO
    }
    //StringBuffer[] getBoard8StringsFromPieces();


    /**
     * Constructor
     * for a fresh ChessBoard in Starting-Position
     */
    public ChessBoard() {
        initChessBoard(new StringBuffer(chessBasicRes.getString("chessboard.initalName")), FENPOS_INITIAL);
    }
    public ChessBoard(String boardName) {
        initChessBoard(new StringBuffer(boardName), FENPOS_INITIAL);
    }
    public ChessBoard(String boardName, String fenBoard ) {
        initChessBoard(new StringBuffer(boardName), fenBoard);
    }
    private void initChessBoard(StringBuffer boardName, String fenBoard) {
        this.boardName = boardName;
        countBoringMoves = 0;
        whiteKingsideCastleAllowed = false;  /// s.o.
        whiteQueensideCastleAllowed = false;
        blackKingsideCastleAllowed = false;
        blackQueensideCastleAllowed = false;
        enPassantCol = -1;    // -1 = not possible,   0 to 7 = possible to beat pawn of opponent on col A-H
        turn = WHITE;
        fullMoves = 0;

        whiteKingPos = -1;
        blackKingPos = -1;

        boardSquares = new Square[NR_SQUARES];
        for(int p = 0; p< NR_SQUARES; p++) {
            boardSquares[p] = new Square(this, p);
        }

        piecesOnBoard = new ChessPiece[MAX_PIECES];
        countOfWhiteFigures = 0;
        countOfBlackFigures = 0;
        nextFreePceID = 0;

        // TODO: read fenBoard and initialize Squares.
        checkAndEvaluateGameOver();
    }

    void spawnPieceAt(int pceTypeNr, int pos) {
        final int newPceID = nextFreePceID++;
        assert(nextFreePceID<=MAX_PIECES);
        if ( isPieceTypeNrWhite(pceTypeNr) )  {
            countOfWhiteFigures++;
        } else {
            countOfBlackFigures++;
        }
        piecesOnBoard[newPceID] = new ChessPiece(pceTypeNr, newPceID);
        // tell all squares about this new piece
        for(int p = 0; p< NR_SQUARES; p++) {
            boardSquares[p].prepareNewPiece(newPceID);
        }
        // construct net of neighbours for this new piece
        for(int p = 0; p< NR_SQUARES; p++) {
            switch (colorlessPieceTypeNr(pceTypeNr)) {
                case ROOK   -> carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, HV_DIRS);
                case BISHOP -> carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, DIAG_DIRS); //TODO: leave out squares with wrong color for bishop
                case QUEEN  -> carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, ROYAL_DIRS);
                case KING   -> carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, ROYAL_DIRS );
                //case KNIGHT -> carefullyEstablishKnightNeighbourship4PieceID(newPceID, p, KNIGHT_DIRS);
                default -> internalError(chessBasicRes.getString("errormessage.notImplemented"));
            }
        }
        // finally, add the new piece at its place
        boardSquares[pos].spawnPiece(newPceID);
    }

    /*private void establishSingleNeighbourship4PieceID(int pid, int pos, int neighboursDir) {
        boardSquares[pos].getvPiece(pid).addSingleNeighbour(boardSquares[pos+neighboursDir].getvPiece(pid));
    }*/

    private void carefullyEstablishSlidingNeighbourship4PieceID(int pid, int pos, int[] neighbourDirs) {
        VirtualPieceOnSquare vPiece = boardSquares[pos].getvPiece(pid);
        Arrays.stream(neighbourDirs)
                .filter(d -> neighbourSquareExistsInDirFromPos( d, pos))    // be careful at the borders
                .forEach(d -> vPiece.addSlidingNeighbour( boardSquares[pos+d].getvPiece(pid), d));
    }

    private void carefullyEstablishSingleNeighbourship4PieceID(int pid, int pos, int[] neighbourDirs ) {
        VirtualPieceOnSquare vPiece = boardSquares[pos].getvPiece(pid);
        Arrays.stream(neighbourDirs)
                .filter(d -> neighbourSquareExistsInDirFromPos( d, pos))    // be careful at the borders
                .forEach(d -> vPiece.addSingleNeighbour( boardSquares[pos+d].getvPiece(pid)));
    }

    private void carefullyEstablishKnightNeighbourship4PieceID(int pid, int pos, int[] neighbourDirs ) {
        VirtualPieceOnSquare vPiece = boardSquares[pos].getvPiece(pid);
        Arrays.stream(neighbourDirs)
                .filter(d -> knightMoveInDirFromPosStaysOnBoard( d, pos))    // be careful at the borders
                .forEach(d -> vPiece.addSingleNeighbour( boardSquares[pos+d].getvPiece(pid)));
    }


    private void internalError(String s) {
        System.out.println( chessBasicRes.getString("errormessage.errorPrefix") + s );
    }


    ///// Hash
    //private long boardFigureHash;
    //long getBoardHash();
    //long getBoardAfterMoveHash(int frompos, int topos);


    ///// MOVES

    protected boolean whiteKingsideCastleAllowed;
    protected boolean whiteQueensideCastleAllowed;
    protected boolean blackKingsideCastleAllowed;
    protected boolean blackQueensideCastleAllowed;
    protected int enPassantCol;   // -1 = not possible,   0 to 7 = possible to beat pawn of opponent on col A-H
    public boolean isWhiteKingsideCastleAllowed() {
        return whiteKingsideCastleAllowed;
    }
    public boolean isWhiteQueensideCastleAllowed() {
        return whiteQueensideCastleAllowed;
    }
    public boolean isBlackKingsideCastleAllowed() {
        return blackKingsideCastleAllowed;
    }
    public boolean isBlackQueensideCastleAllowed() {
        return blackQueensideCastleAllowed;
    }
    public int getEnPassantCol() {
        return enPassantCol;
    }

    int countBoringMoves;
    int fullMoves;
    public int getCountBoringMoves() {
        return countBoringMoves;
    }
    public int getFullMoves() {
        return fullMoves;
    }

    public String doMove(@NotNull String move) {
        int startpos=0;
        while (startpos<move.length() && move.charAt(startpos)==' ')
            startpos++;
        move = move.substring(startpos);
        if (move.isEmpty())
            return "";
        int frompos = coordinateString2Pos(move, 0);
        int topos = coordinateString2Pos(move, 2);
        char promoteToChar = move.length()>4 ? move.charAt(4) : 'q';
        int promoteToFigNr;
        switch (promoteToChar) {
            case 'q', 'Q', ' ' -> promoteToFigNr = QUEEN;
            case 'n', 'N', 's', 'S' -> promoteToFigNr = KNIGHT;
            case 'b', 'B', 'l', 'L' -> promoteToFigNr = BISHOP;
            case 'r', 'R', 't', 'T' -> promoteToFigNr = ROOK;
            default -> {
                promoteToFigNr = QUEEN;
                System.err.println(format(chessBasicRes.getString("errorMessage.moveParsingError") + " {0}", promoteToChar));
            }
        }
        //System.out.format(" %c,%c %c,%c = %d,%d-%d,%d = %d-%d\n", input.charAt(0), input.charAt(1), input.charAt(2), input.charAt(3), (input.charAt(0)-'A'), input.charAt(1)-'1', (input.charAt(2)-'A'), input.charAt(3)-'1', frompos, topos);
        return doMove(frompos, topos, promoteToFigNr);
    }

    String doMove(int frompos, int topos, int promoteToPieceNr) {
        return null;
    }

    public String getPieceFullName(int pceId) {
        return piecesOnBoard[pceId].toString();
    }

    public int getDistanceAtPosFromPieceId(int pos, int pceId) {
        return boardSquares[pos].getDistanceToPieceID(pceId);
    }
}
