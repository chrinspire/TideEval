/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.text.MessageFormat.*;

public class ChessBoard {

    // static Board position check functions
    static boolean isFirstFile(int pos) {
        return ((pos & 0b0111) == 0); // Achtung, Impementierung passt sich nicht einer verändert Boardgröße an.
    }
    static boolean isLastFile(int pos) {
        return ((pos & 0b0111) == 0b0111); // Achtung, Impementierung passt sich nicht einer verändert Boardgröße an.
    }
    static boolean isFirstRank(int pos) {
        return (pos >= ChessBasics.NR_SQUARES- ChessBasics.NR_FILES);
    }
    static boolean isLastRank(int pos) {
        return (pos < ChessBasics.NR_FILES);
    }

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
        return ChessBasics.isWhite(col) ? whiteKingChecks > 0
                            : blackKingChecks > 0;
    }

    /*public int getPieceNrCounterForColor(int figNr, boolean whitecol) {
        return whitecol ? countOfWhiteFigNr[abs(figNr)]
                        : countOfBlackFigNr[abs(figNr)];
    }*/
    //int getPieceNrCounter(int pieceNr);

    private int countOfWhiteFigures;
    private int countOfBlackFigures;
    public int getPieceCounter()  {
        return countOfWhiteFigures + countOfBlackFigures;
    }
    public int getPieceCounterForColor(boolean whitecol)  {
        return whitecol ? countOfWhiteFigures
                        : countOfBlackFigures;
    }

    private int nextFreePceID;

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
    };
    //boolean accessibleForKing(int pos, boolean myColor);
    //boolean coveredByMe(int pos, boolean color);
    //boolean coveredByMeExceptOne(int pos, boolean color, int pieceNr);

    public int distanceToKing(int pos, boolean kingCol) {
        int dx, dy;
        // Achtung, Implementierung passt sich nicht einer veränderten Boardgröße an.
        if (kingCol == ChessBasics.WHITE) {
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

    //long getBoardHash();
    //long getBoardAfterMoveHash(int frompos, int topos);


    public String doMove(@NotNull String move) {
        int startpos=0;
        while (startpos<move.length() && move.charAt(startpos)==' ')
            startpos++;
        move = move.substring(startpos);
        if (move.isEmpty())
            return "";
        int frompos = (move.charAt(0) - 'a') + 8 * (7 - (move.charAt(1) - '1'));
        int topos = (move.charAt(2) - 'a') + 8 * (7 - (move.charAt(3) - '1'));
        char promoteToChar = move.length()>4 ? move.charAt(4) : 'q';
        int promoteToFigNr = 0;
        switch (promoteToChar) {
            case'q':
            case'Q':
            case' ':
                promoteToFigNr = ChessBasics.QUEEN;
                break;
            case'n':
            case'N':
            case's':
            case'S':
                promoteToFigNr = ChessBasics.KNIGHT;
                break;
            case'b':
            case'B':
            case'l':
            case'L':
                promoteToFigNr = ChessBasics.BISHOP;
                break;
            case'r':
            case'R':
            case't':
            case'T':
                promoteToFigNr = ChessBasics.ROOK;
                break;
            default:
                promoteToFigNr = ChessBasics.QUEEN;
                System.err.println(format(ChessBasics.chessBasicRes.getString("errormessage.moveParsingError")+" {0}", promoteToChar));
        }
        //System.out.format(" %c,%c %c,%c = %d,%d-%d,%d = %d-%d\n", input.charAt(0), input.charAt(1), input.charAt(2), input.charAt(3), (input.charAt(0)-'A'), input.charAt(1)-'1', (input.charAt(2)-'A'), input.charAt(3)-'1', frompos, topos);
        return doMove(frompos, topos, promoteToFigNr);
    }

    String doMove(int frompos, int topos, int promoteToPieceNr) {
        return null;
    }

    StringBuffer getBoardName() {
        return null;
    }

    String getShortBoardName() {
        return null;
    }

    String getBoardFEN() {
        return null;
    }
    //StringBuffer[] getBoard8StringsFromPieces();


    ////// Contructors
    public ChessBoard() {
        initChessBoard(new StringBuffer(ChessBasics.chessBasicRes.getString("chessboard.initalName")), ChessBasics.INITIAL_FEN_POS);
    }
    public ChessBoard(String boardName) {
        initChessBoard(new StringBuffer(boardName), ChessBasics.INITIAL_FEN_POS);
    }
    public ChessBoard(String boardName, String fenBoard ) {
        initChessBoard(new StringBuffer(boardName), fenBoard);
    }
    private void initChessBoard(StringBuffer boardName, String fenBoard) {
        this.boardName = boardName;
        countBoringMoves = 0;
        whiteKingsideCastleAllowed = true;  /// s.o.
        whiteQueensideCastleAllowed = true;
        blackKingsideCastleAllowed = true;
        blackQueensideCastleAllowed = true;
        enPassantCol = -1;    // -1 = not possible,   0 to 7 = possible to beat pawn of opponent on col A-H
        turn = ChessBasics.WHITE;
        fullMoves = 0;

        whiteKingPos = -1;
        blackKingPos = -1;

        boardSquares = new Square[ChessBasics.NR_SQUARES];
        for(int p = 0; p< ChessBasics.NR_SQUARES; p++) {
            boardSquares[p] = new Square(p);
        }

        countOfWhiteFigures = 0;
        countOfBlackFigures = 0;
        nextFreePceID = 0;
        // TODO: read fenBoard and initialize Squares.
        // here put 1 figure manually:
        spawnPieceAt(ChessBasics.ROOK,56);
        spawnPieceAt(ChessBasics.ROOK,63);
        spawnPieceAt(ChessBasics.ROOK_BLACK,1);
        checkAndEvaluateGameOver();
    }

    private void spawnPieceAt(int pceTypeNr, int pos) {
        final int newPceID = nextFreePceID++;
        if ( ChessBasics.isPieceTypeNrWhite(pceTypeNr) )  {
            countOfWhiteFigures++;
        } else {
            countOfBlackFigures++;
        }
        // tell all squares about this new piece
        for(int p = 0; p< ChessBasics.NR_SQUARES; p++) {
            boardSquares[p].prepareNewPiece(newPceID,pceTypeNr);
        }
        // construct net of neighbours for this new piece
        for(int p = 0; p< ChessBasics.NR_SQUARES; p++) {
            switch (ChessBasics.colorlessPieceTypeNr(pceTypeNr)) {
                case ChessBasics.ROOK: {
                    if ( !isFirstFile(p) )
                        establishSlidingNeighbourship4PieceID(newPceID, p, ChessBasics.LEFT);
                    if ( !isLastFile(p) )
                        establishSlidingNeighbourship4PieceID(newPceID, p, ChessBasics.RIGHT);
                    if ( !isFirstRank(p) )
                        establishSlidingNeighbourship4PieceID(newPceID, p, ChessBasics.DOWN);
                    if ( !isLastRank(p) )
                        establishSlidingNeighbourship4PieceID(newPceID, p, ChessBasics.UP);
                }
                default:
                    internalError(ChessBasics.chessBasicRes.getString("errormessage.notImplemented"));
            }
        }
        // finally add the new piece at its place
        boardSquares[pos].spawnPiece(newPceID);
    }

    private void establishSingleNeighbourship4PieceID(int pid, int pos, int neighboursDir) {
        boardSquares[pos].getvPiece(pid).addSingleNeighbour(boardSquares[pos+neighboursDir].getvPiece(pid));
    }

    private void establishSlidingNeighbourship4PieceID(int pid, int pos, int neighboursDir) {
        boardSquares[pos].getvPiece(pid).addSlidingNeighbour(boardSquares[pos+neighboursDir].getvPiece(pid), neighboursDir);
    }


    private int firstPosInRank(int pos) {
        return (pos/ ChessBasics.NR_FILES)* ChessBasics.NR_FILES;
    }

    private int lastPosInRank(int pos) {
        return firstPosInRank(pos)+7;
    }

    private void internalError(String s) {
        System.out.println( ChessBasics.chessBasicRes.getString("errormessage.errorPrefix") + s );
    }

    Square[] boardSquares;

    private boolean turn;
    public boolean isTurn() {
        return turn;
    }
    //void setTurn(boolean turn);

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

    private StringBuffer boardName;
    //private long boardFigureHash;
}
