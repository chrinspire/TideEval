/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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

    /////
    ///// the Chess Pieces as such
    /////

    /**
     * keep all Pieces on Board
     */
    ChessPiece[] piecesOnBoard;
    private int nextFreePceID;

    Stream<ChessPiece> getPiecesStream() {
        return Arrays.stream(piecesOnBoard);
    }
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
        return boardSquares[pos].getPieceID();
    }

    private int countOfWhitePieces;
    private int countOfBlackPieces;
    public int getPieceCounter()  {
        return countOfWhitePieces + countOfBlackPieces;
    }
    public int getPieceCounterForColor(boolean whitecol)  {
        return whitecol ? countOfWhitePieces
                        : countOfBlackPieces;
    }

    boolean hasPieceOfColorAt(boolean col, int pos) {
        if (boardSquares[pos].getPieceID()==NOPIECE || getPieceAt(pos)==null )   // Todo-Option:  use assert(getPiecePos!=null)
            return false;
        return (getPieceAt(pos).color() == col);
    }

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

    static final int MAX_INTERESTING_NROF_HOPS = 8;


    /////
    ///// the Chess Game as such
    /////

    private boolean gameOver;
    public boolean isGameOver() {
        return gameOver;
    }

    private void checkAndEvaluateGameOver() {    // called to check+evaluate if there are no more moves left or 50-rules-move is violated
        if ( countOfWhitePieces <= 0 || countOfBlackPieces <= 0 ) {
            gameOver = true;
            return;
        }
        gameOver = false;
        // TODO: real game status check...
    }

    protected static final int MAX_INSIGHT_LEVELS = 2;
    /**
     * calculates board evaluation according to several "insight levels"
     * @param levelOfInsight: 1 - sum of lain standard figure values,
     *                        2 - take figure position into account
     *                       -1 - take best algorithm currently implemented
     * @return board evaluation in centipawns (+ for white, - for an advantage of black)
     */
    public int boardEvaluation(int levelOfInsight) {
        if (levelOfInsight>MAX_INSIGHT_LEVELS || levelOfInsight<1)
            levelOfInsight=MAX_INSIGHT_LEVELS;
        int[] eval = new int[MAX_INSIGHT_LEVELS];
        eval[0] = evaluateAllPiecesBasicValueSum();
        eval[1] = evaluateAllPiecesBasicMobility();
        switch (levelOfInsight) {
            case 1 -> {
                return eval[0];
            }
            case 2 -> {
                return eval[0] + eval[1];
            }
        }
        // hier one should not be able to end up, according to the parameter restriction/correction at the beginning
        // - but javac does not see it like that...
        assert(false);
        return 0;
    }

    public int boardEvaluation() {
        return boardEvaluation(MAX_INSIGHT_LEVELS);
    }

    private int evaluateAllPiecesBasicValueSum() {
        return getPiecesStream()
                .filter(Objects::nonNull)
                .mapToInt(pce -> pce.getBaseValue() )
                .sum();
        /*or old fashioned :-)
        int pceValSum = 0;
        for (ChessPiece pce: piecesOnBoard)
            if (pce!=null)
                pceValSum += pce.getBaseValue();
        return pceValSum;
        */
    }

    // idea: could become an adapdable parameter later
    private static int EVALPARAM_CP_PER_MOBILITYSQUARE = 4;

    private int evaluateAllPiecesBasicMobility() {
        // this is not using streams, but a loop, as the return-type int[] is to complex to "just sum up"
        int[] mobSumPerHops = new int[MAX_INTERESTING_NROF_HOPS];
        // init mobility sum per hop
        for (int i=0; i<MAX_INTERESTING_NROF_HOPS; i++)
            mobSumPerHops[i]=0;
        for (ChessPiece pce: piecesOnBoard) {
            if (pce!=null) {
                int[] pceMobPerHops = pce.getSimpleMobilities();
                //add this pieces mobility per hop to overall the sub per hop
                if (isWhite(pce.color()))
                    for (int i=0; i<MAX_INTERESTING_NROF_HOPS; i++)
                        mobSumPerHops[i] += pceMobPerHops[i]*EVALPARAM_CP_PER_MOBILITYSQUARE;
                else  // count black as negative
                    for (int i=0; i<MAX_INTERESTING_NROF_HOPS; i++)
                        mobSumPerHops[i] -= pceMobPerHops[i]*EVALPARAM_CP_PER_MOBILITYSQUARE;
            }
        }
        // sum all level up into one value, but weight later hops lesser
        int mobSum = 0;
        for (int i=0; i<MAX_INTERESTING_NROF_HOPS; i++)
            mobSum += mobSumPerHops[i] >> i;   // rightshift, so hops==2 counts half, hops==3 counts only quater...
        return mobSum;
    }



    //boolean accessibleForKing(int pos, boolean myColor);
    //boolean coveredByMe(int pos, boolean color);
    //boolean coveredByMeExceptOne(int pos, boolean color, int pieceNr);

    //Piece getPieceOnSquare(int pos);
    //int getPieceNrOnSquare(int pos);

    private boolean turn;
    public boolean getTurnCol() {
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

    /**
     * Get (simple) fen string from the current board
     * TODO make it return "real" fen string
     * @return String in FEN notation representing the current board and game status
     */
    String getBoardFEN() {
        StringBuilder fenString = new StringBuilder();
        for (int rank = 0; rank < NR_RANKS; rank++) {
            if (rank>0)
                fenString.append("/");
            int spaceCounter = 0;
            for (int file = 0; file < NR_FILES; file++) {
                int pieceId = getPieceTypeAt(rank * 8 + file);
                if (pieceId == NOPIECE) {
                    spaceCounter++;
                }
                else {
                    if (spaceCounter > 0) {
                        fenString.append(spaceCounter);
                        spaceCounter = 0;
                    }
                    fenString.append(figureFenNames[pieceId]);
                }
            }
            if (spaceCounter > 0) {
                fenString.append(spaceCounter);
                spaceCounter = 0;
            }
        }
        return fenString + " " + getFENBoardPostfix();
    }
    //StringBuffer[] getBoard8StringsFromPieces();


    private String getFENBoardPostfix() {
        return (turn==WHITE?" w ":" b ")
                + (isWhiteKingsideCastleAllowed()?"K":"")+(isWhiteQueensideCastleAllowed()?"Q":"")
                + (isBlackKingsideCastleAllowed()?"k":"")+(isBlackQueensideCastleAllowed()?"q":"")
                + ( (!isWhiteKingsideCastleAllowed() && !isWhiteQueensideCastleAllowed()
                && !isBlackKingsideCastleAllowed() && !isBlackQueensideCastleAllowed()) ? "- ":" ")
                + (getEnPassantFile()==-1 ? "- " : (Character.toString(getEnPassantFile()+'a')+(turn==WHITE?"6":"3"))+" ")
                + countBoringMoves
                + " " + fullMoves;
    }


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
        System.out.println();
        System.out.print("New Board "+boardName+": ");
        this.boardName = boardName;
        initBoardFromFEN(fenBoard);
        checkAndEvaluateGameOver();
    }


    Square[] boardSquares;
    public Square[] getBoardSquares() {
        return boardSquares;
    }

    Stream<Square> getBoardSquaresStream() {
        return Arrays.stream(boardSquares);
    }

    /* Iterator<Square> getAllSquaresIterator() {
        return new Iterator<Square>() {
            private int i = 0;
            @Override
            public boolean hasNext() {
                return boardSquares.length > i;
            }
            @Override
            public Square next() {
                return boardSquares[i++];
            }
        };
    }; */

    private void emptyBoard() {
        piecesOnBoard = new ChessPiece[MAX_PIECES];
        countOfWhitePieces = 0;
        countOfBlackPieces = 0;
        nextFreePceID = 0;
        boardSquares = new Square[NR_SQUARES];
        for(int p = 0; p< NR_SQUARES; p++) {
            boardSquares[p] = new Square(this, p);
        }
    }

    private void setDefaultBoardState() {
        countBoringMoves = 0;
        whiteKingsideCastleAllowed = false;  /// s.o.
        whiteQueensideCastleAllowed = false;
        blackKingsideCastleAllowed = false;
        blackQueensideCastleAllowed = false;
        enPassantFile = -1;    // -1 = not possible,   0 to 7 = possible to beat pawn of opponent on col A-H
        turn = WHITE;
        fullMoves = 0;
        whiteKingPos = -1;
        blackKingPos = -1;
    }


    /**
     * create a new Piece on the board
     * @param pceType
     * @param pos
     * @return returns pieceID of the new Piece
     */
    int spawnPieceAt(final int pceType, final int pos) {
        final int newPceID = nextFreePceID++;
        assert(nextFreePceID<=MAX_PIECES);
        assert(pos>=0 && pos<NR_SQUARES);
        if ( isPieceTypeNrWhite(pceType) )  {
            countOfWhitePieces++;
            if (pceType==KING)
                whiteKingPos=pos;
        } else {
            countOfBlackPieces++;
            if (pceType==KING_BLACK)
                blackKingPos=pos;
        }

        piecesOnBoard[newPceID] = new ChessPiece( this,pceType, newPceID, pos);
        // tell all squares about this new piece
        for(Square sq : boardSquares )
            sq.prepareNewPiece(newPceID);

        // construct net of neighbours for this new piece
        for(int p = 0; p< NR_SQUARES; p++) {
            switch (colorlessPieceTypeNr(pceType)) {
                case ROOK   -> carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, HV_DIRS);
                case BISHOP -> {
                    if (isSameSquareColor(pos, p)) // only if square  has same square color than the bishop is standing on
                        carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, DIAG_DIRS); //TODO: leave out squares with wrong color for bishop
                }
                case QUEEN  -> carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, ROYAL_DIRS);
                case KING   -> carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, ROYAL_DIRS ); //TODO: Kings must avoid enemy-covered squares and be able to castle...
                case KNIGHT -> carefullyEstablishKnightNeighbourship4PieceID(newPceID, p, KNIGHT_DIRS);
                case PAWN -> {
                    // Todo: optimize, and do not establish impossible neighbourships (like from left/right of pawn)
                    if (pceType==PAWN) {
                        carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, WPAWN_DIRS);
                        if (rankOf(p)==1)
                            carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, WPAWN_LONG_DIR);
                    }
                    else { // ==PAWN_BLACK
                        carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, BPAWN_DIRS);
                        if (rankOf(p)==NR_RANKS-2)
                            carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, BPAWN_LONG_DIR);
                    }
                }
                default -> internalErrorPrintln(chessBasicRes.getString("errormessage.notImplemented"));
            }
        }
        // finally, add the new piece at its place
        boardSquares[pos].spawnPiece(newPceID);
        //updateHash
        return newPceID;
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

    public void removePiece(int pceID) {
        piecesOnBoard[pceID] = null;
        for(Square sq : boardSquares )
            sq.removePiece(pceID);
    }

    public void internalErrorPrintln(String s) {
        System.out.println( chessBasicRes.getString("errormessage.errorPrefix") + s );
    }

    public static final int DEBUGMSG_DISTANCE_PROPAGATION = 1001;
    public static void debugPrint(int topic, String s) {
        if (isDebugmsgTopicInterestin(topic))
            System.out.print( s );
    }

    public static void debugPrintln(int topic, String s) {
        if (isDebugmsgTopicInterestin(topic))
            System.out.println( s );
    }

    private static boolean isDebugmsgTopicInterestin(int topic) {
        return (topic!=DEBUGMSG_DISTANCE_PROPAGATION
                || (topic<100 && topic>3 ));
    }

    /**
     * inits empty chessboard with pieces and parameters from a FEN string
     * @param fenString FEN String according to Standard with board and game attributes
     */
    protected void initBoardFromFEN(String fenString) {
        setDefaultBoardState();
        emptyBoard();
        int figNr;
        int i=0;
        int rank=0;
        int file=0;
        int pos=0;
        while (i<fenString.length() && rank<8) {
            int emptyfields = 0;
            switch (fenString.charAt(i)) {
                case '*', 'p', '♟' -> figNr = PAWN_BLACK;
                case 'o', 'P', '♙' -> figNr = PAWN;
                case 'L', 'B', '♗' -> figNr = BISHOP;
                case 'l', 'b', '♝' -> figNr = BISHOP_BLACK;
                case 'T', 'R', '♖' -> figNr = ROOK;
                case 't', 'r', '♜' -> figNr = ROOK_BLACK;
                case 'S', 'N', '♘' -> figNr = KNIGHT;
                case 's', 'n', '♞' -> figNr = KNIGHT_BLACK;
                case 'K', '♔'      -> figNr = KING;
                case 'k', '♚'      -> figNr = KING_BLACK;
                case 'D', 'Q', '♕' -> figNr = QUEEN;
                case 'd', 'q', '♛' -> figNr = QUEEN_BLACK;
                case '/' -> {
                    if (file != 8)
                        internalErrorPrintln("**** Inkorrekte Felder pro Zeile gefunden beim Parsen an Position " + i + " des FEN-Strings " + fenString);
                    pos += 8 - file; // statt pos++, um ggf. den Input-Fehler zu korrigieren
                    file = 0;
                    i++;
                    rank++;
                    continue;
                }
                case ' ', '_' -> {  // signals end of board in fen notatio, but we also want to accept it as empty field
                    if (fenString.charAt(i) == ' ' && file == 8 && rank == 7) {
                        i++;
                        rank++;
                        pos++;
                        continue;
                    }
                    figNr = EMPTY;
                    emptyfields = 1;
                }
                default -> {
                    figNr = EMPTY;
                    if (fenString.charAt(i) >= '1' && fenString.charAt(i) <= '8')
                        emptyfields = fenString.charAt(i) - '1' + 1;
                    else {
                        internalErrorPrintln("**** Fehler beim Parsen an Position " + i + " des FEN-Strings " + fenString);
                    }
                }
            }
            if (figNr != EMPTY) {
                spawnPieceAt(figNr, pos);
                file++;
                pos++;
            }
            else {
                //spawn nothing // figuresOnBoard[pos] = null;
                pos+=emptyfields;
                file+=emptyfields;
                while (--emptyfields>0)
                    ;  //figuresOnBoard[pos++]=null;
            }
            if (file>8)
                System.err.println("**** Überlange Zeile gefunden beim Parsen an Position "+i+" des FEN-Strings "+fenString);
            // kann nicht vorkommen if (rank>8)
            //    System.err.println("**** Zu viele Zeilen gefunden beim Parsen an Position "+i+" des FEN-Strings "+fenString);
            i++;
        }
        // Todo!!! set board params from fen appendix
        while ( i<fenString.length() && fenString.charAt(i)==' ' )
            i++;
        if (i<fenString.length()) {
            if ( fenString.charAt(i)=='w' || fenString.charAt(i)=='W' )
                turn=WHITE;
            else if ( fenString.charAt(i)=='b' || fenString.charAt(i)=='B' )
                turn=BLACK;
            else
                System.err.println("**** Fehler beim Parsen der Spieler-Angabe an Position "+i+" des FEN-Strings "+fenString);
            i++;
            while ( i<fenString.length() && fenString.charAt(i)==' ' )
                i++;
            // castle indicators
            int nextSeperator=i;
            while ( nextSeperator<fenString.length() && fenString.charAt(nextSeperator)!=' ' )
                nextSeperator++;
            blackQueensideCastleAllowed = fenString.substring(i, nextSeperator).contains("q");
            blackKingsideCastleAllowed  = fenString.substring(i, nextSeperator).contains("k");
            whiteQueensideCastleAllowed = fenString.substring(i, nextSeperator).contains("Q");
            whiteKingsideCastleAllowed  = fenString.substring(i, nextSeperator).contains("K");
            // enPassant
            i=nextSeperator;
            while ( i<fenString.length() && fenString.charAt(i)==' ' )
                i++;
            nextSeperator=i;
            while ( nextSeperator<fenString.length() && fenString.charAt(nextSeperator)!=' ' )
                nextSeperator++;
            if ( fenString.substring(i,nextSeperator).matches("[a-h]([1-8]?)") )
                enPassantFile = fenString.charAt(i)-'a';
            else {
                enPassantFile = -1;
                if ( fenString.charAt(i)!='-' )
                    System.err.println("**** Fehler beim Parsen der enPassant-Spalte an Position "+i+" des FEN-Strings "+fenString);
            }
            // halfMoveClock
            i=nextSeperator;
            while ( i<fenString.length() && fenString.charAt(i)==' ' )
                i++;
            nextSeperator=i;
            while ( nextSeperator<fenString.length() && fenString.charAt(nextSeperator)!=' ' )
                nextSeperator++;
            if ( fenString.substring(i,nextSeperator).matches("[0-9]+") )
                countBoringMoves = Integer.parseInt(fenString.substring(i, nextSeperator));
            else {
                countBoringMoves = 0;
                System.err.println("**** Fehler beim Parsen der halfMoveClock an Position "+i+" des FEN-Strings "+fenString);
            }
            // full moves
            i=nextSeperator;
            while ( i<fenString.length() && fenString.charAt(i)==' ' )
                i++;
            nextSeperator=i;
            while ( nextSeperator<fenString.length() && fenString.charAt(nextSeperator)!=' ' )
                nextSeperator++;
            if ( fenString.substring(i,nextSeperator).matches("[0-9]+") )
                fullMoves = Integer.parseInt(fenString.substring(i, nextSeperator));
            else {
                fullMoves = 1;
            }
        }
        // else no further board parameters available, stay with defaults
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
    protected int enPassantFile;   // -1 = not possible,   0 to 7 = possible to beat pawn of opponent on col A-H
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
    public int getEnPassantFile() {
        return enPassantFile;
    }

    int countBoringMoves;
    int fullMoves;
    public int getCountBoringMoves() {
        return countBoringMoves;
    }
    public int getFullMoves() {
        return fullMoves;
    }

    boolean doMove(int frompos, int topos, int promoteToPceTypeNr) {
        // sanity/range checks for move
        if (frompos < 0 || topos < 0
                || frompos >= NR_SQUARES || topos >= NR_SQUARES) { // || figuresOnBoard[frompos].getColor()!=turn  ) {
            internalErrorPrintln(String.format("Fehlerhafter Zug: %s%s ist außerhalb des Boards %s.\n", squareName(frompos), squareName(topos), getBoardName()));
            return false;
        }
        int pceID = getPieceIdAt(frompos);
        int pceType = getPieceTypeAt(frompos);
        if (pceID == NOPIECE) { // || figuresOnBoard[frompos].getColor()!=turn  ) {
            internalErrorPrintln(String.format("Fehlerhafter Zug: auf %s steht keine Figur auf Board %s.\n", squareName(frompos), getBoardName()));
            return false;
        }
        if (boardSquares[topos].getShortestUnconditionalDistanceToPieceID(pceID) != 1
                && colorlessPieceTypeNr(pceType) != KING) { // || figuresOnBoard[frompos].getColor()!=turn  ) {
            // TODO: check king for allowed moves... excluded here, because castelling is not obeyed in distance calculation, yet.
            internalErrorPrintln(String.format("Fehlerhafter Zug: %s -> %s nicht möglich auf Board %s.\n", squareName(frompos), squareName(topos), getBoardName()));
            return false;
        }
        int toposPceID = getPieceIdAt(topos);
        int toposType = getPieceTypeAt(topos);

        // take figure
        if (toposPceID != NOPIECE) {
            takePieceAway(topos);
            /*old code to update pawn-evel-parameters
            if (takenFigNr==NR_PAWN && toRow==getWhitePawnRowAtCol(toCol))
                refindWhitePawnRowAtColBelow(toCol,toRow+1);  // try to find other pawn in column where the pawn was beaten
            else if (takenFigNr==NR_PAWN_BLACK && toRow==getBlackPawnRowAtCol(toCol))
                refindBlackPawnRowAtColBelow(toCol,toRow-1);*/
        }
        if (colorlessPieceTypeNr(pceType)==PAWN || toposPceID!=NOPIECE)
            countBoringMoves=0;
        else
            countBoringMoves++;

        // en-passant
        // is possible to occur in two notations to left/right (then taken pawn has already been treated above) ...
        if (pceType==PAWN
                && rankOf(frompos)==4 && rankOf(topos)==4
                && fileOf(topos)==enPassantFile) {
            topos+=UP;
            //setFurtherWhitePawnRowAtCol(toCol, toRow);     // if the moved pawn was the furthest, remember new position, otherwise the function will leave it
        } else if (pceType==PAWN_BLACK
                && rankOf(frompos)==3 && rankOf(topos)==3
                && fileOf(topos)==enPassantFile) {
            topos+=DOWN;
            //setFurtherBlackPawnRowAtCol(toCol, toRow);     // if the moved pawn was the furthest, remember new position, otherwise the function will leave it
        }
        // ... or diagonally als normal
        else if (pceType==PAWN
                && rankOf(frompos)==4 && rankOf(topos)==5
                && fileOf(topos)==enPassantFile)  {
            takePieceAway(topos+DOWN);
            /*setFurtherWhitePawnRowAtCol(toCol, toRow);     // if the moved pawn was the furthest, remember new position, otherwise the function will leave it
            if (toRow+1==getBlackPawnRowAtCol(toCol))
                setFurtherBlackPawnRowAtCol(toCol, NO_BLACK_PAWN_IN_COL);*/
        } else if (pceType==PAWN_BLACK
                && rankOf(frompos)==3 && rankOf(topos)==2
                && fileOf(topos)==enPassantFile) {
            takePieceAway(topos+UP);
            /*setFurtherBlackPawnRowAtCol(toCol, toRow);     // if the moved pawn was the furthest, remember new position, otherwise the function will leave it
            if (toRow-1==getWhitePawnRowAtCol(toCol))
                setFurtherWhitePawnRowAtCol(toCol, NO_WHITE_PAWN_IN_COL);*/
        }

        //boardMoves.append(" ").append(squareName(frompos)).append(squareName(topos));

        // check if this is a 2-square pawn move ->  then enPassent is possible for opponent at next move
        if (    pceType==PAWN       && rankOf(frompos)==1 && rankOf(topos)==3
                || pceType==PAWN_BLACK && rankOf(frompos)==6 && rankOf(topos)==4 )
            enPassantFile = fileOf(topos);
        else
            enPassantFile = -1;

        // move
        basicMoveTo(pceType, pceID, frompos, topos);

        // promote to
        if (toposType==PAWN && isLastRank(topos)) {
            takePieceAway(topos);
            spawnPieceAt(promoteToPceTypeNr, topos);
        } else if (toposType==PAWN_BLACK && isFirstRank(topos)) {
            takePieceAway(topos);
            spawnPieceAt(promoteToPceTypeNr>BLACK_PIECE ? promoteToPceTypeNr : promoteToPceTypeNr+BLACK_PIECE, topos);
        }

        // castelling:
        // i) also move rook  ii) update castelling rights
        // TODO: put castelling square numbers in constants in ChessBasics...
        if (pceType == KING_BLACK) {
            if (frompos == 4 && topos == 6)
                basicMoveTo(7, 5);
            else if (frompos == 4 && topos == 2)
                basicMoveTo(0, 3);
            blackKingsideCastleAllowed = false;
            blackQueensideCastleAllowed = false;
        } else if (pceType == KING) {
            if (frompos == 60 && topos == 62)
                basicMoveTo(63, 61);
            else if (frompos == 60 && topos == 58)
                basicMoveTo(56, 59);
            whiteKingsideCastleAllowed = false;
            whiteQueensideCastleAllowed = false;
        } else if (blackKingsideCastleAllowed && frompos == 7) {
            blackKingsideCastleAllowed = false;
        } else if (blackQueensideCastleAllowed && frompos == 0) {
            blackQueensideCastleAllowed = false;
        } else if (whiteKingsideCastleAllowed && frompos == 63) {
            whiteKingsideCastleAllowed = false;
        } else if (whiteQueensideCastleAllowed && frompos == 56) {
            whiteQueensideCastleAllowed = false;
        }

        turn = !turn;
        if (isWhite(turn))
            fullMoves++;

        return true;
    }


    public boolean doMove(@NotNull String move) {
        int startpos=0;
        // skip spaces
        while (startpos<move.length() && move.charAt(startpos)==' ')
            startpos++;
        move = move.substring(startpos);
        System.out.print(" "+move);
        // an empty move string is not a legal move
        if (move.isEmpty())
            return false;

        int frompos;
        int topos;
        int promoteToFigNr=EMPTY;
        if ( move.length()>=4
                && move.charAt(0)>='a' && move.charAt(0)<('a'+NR_RANKS)
                && move.charAt(1)>='1' && move.charAt(1)<('1'+NR_FILES) ) {
            // move-string starts with a lower case letter + a digit and is at least 4 chars long
            // --> standard fen-like move-string, like "a1b2"
            frompos = coordinateString2Pos(move, 0);
            topos = coordinateString2Pos(move, 2);
            char promoteToChar = move.length() > 4 ? move.charAt(4) : 'q';
            promoteToFigNr = getPromoteCharToPceTypeNr(promoteToChar);
            //System.out.format(" %c,%c %c,%c = %d,%d-%d,%d = %d-%d\n", input.charAt(0), input.charAt(1), input.charAt(2), input.charAt(3), (input.charAt(0)-'A'), input.charAt(1)-'1', (input.charAt(2)-'A'), input.charAt(3)-'1', frompos, topos);
        }
        // otherwise it must be a short form notation
        // if it starts with lower case letter --> must be a pawn movement
        else if ( move.charAt(0)>='a' && move.charAt(0)<('a'+NR_RANKS) ) {
            int promcharpos;
            if ( move.charAt(1)=='x') {
                // a pawn beats something
                if (move.length()==3) {
                    // very short form like "cxd" is not supported, yet
                    return false;
                }
                // a pawn beats something, like "hxg4"
                topos = coordinateString2Pos(move, 2);
                frompos = fileRank2Pos(move.charAt(0)-'a', rankOf(topos)+ (isWhite(getTurnCol()) ? -1 : +1));
                promcharpos = 4;
            } else {
                // simple pawn move, like "d4"
                topos = coordinateString2Pos(move, 0);
                frompos = topos + (isWhite(getTurnCol()) ? +NR_FILES : -NR_FILES);  // normally it should come from one square below
                if (isWhite(getTurnCol()) && rankOf(topos) == 3) {
                    // check if it was a 2-square move...
                    if (getPieceTypeAt(frompos) == EMPTY)
                        frompos += NR_FILES;   // yes, it must be even one further down
                } else if (isBlack(getTurnCol()) && rankOf(topos) == NR_RANKS - 4) {
                    // check if it was a 2-square move...
                    if (getPieceTypeAt(frompos) == EMPTY)
                        frompos -= NR_FILES;   // yes, it must be even one further down
                }
            }
            // promotion character indictats what a pawn should be promoted to
            promcharpos = 2;
            if ( (isBlack(getTurnCol()) && isFirstRank(topos)
                  || isWhite(getTurnCol()) && isLastRank(topos) ) ) {
                char promoteToChar = move.length() > promcharpos ? move.charAt(promcharpos) : 'q';
                promoteToFigNr = getPromoteCharToPceTypeNr(promoteToChar);
            }
        }
        else if ( move.length()>=3 && move.charAt(1)=='-' &&
                ( move.charAt(0)=='0' && move.charAt(2)=='0'
                || move.charAt(0)=='O' && move.charAt(2)=='O'
                || move.charAt(0)=='o' && move.charAt(2)=='o' ) ) {
            // castelling
            if ( isWhite(getTurnCol()) )
                frompos = A1SQUARE+4;
            else   // black
                frompos = 4;
            if ( move.length()>=5 && move.charAt(3)=='-' && move.charAt(4)==move.charAt(0) )
                topos = frompos-2;  // long castelling
            else
                topos = frompos+2;  // short castelling
        }
        else {
            // must be a normal, non-pawn move
            int movingPceType = char2PceTypeNr(move.charAt(0));
            /*if (isBlack(getTurnCol()))
                movingPceType += BLACK_PIECE;*/
            int fromFile = -1;
            int fromRank = -1;
            if ( isRankChar(move.charAt(2)) ) {
                // the topos starts only one character later, so there must be an intermediate information
                if ( move.charAt(1)=='x' ) {   // its beating something - actually we do not care if this is true...
                }
                else if ( isRankChar(move.charAt(1)) )  // a starting file
                    fromFile = move.charAt(1)-'a';
                else if ( isFileChar(move.charAt(1)) )  // a starting rank
                    fromRank = move.charAt(1)-'1';
                topos = coordinateString2Pos(move, 2);
            }
            else if ( move.charAt(2)=='x' ) {
                // a starting file or rank + a beating x..., like "Rfxf2"
                if ( isRankChar(move.charAt(1)) )      // a starting file
                    fromFile = move.charAt(1)-'a';
                else if ( isFileChar(move.charAt(1)) ) // a starting rank
                    fromRank = move.charAt(1)-'1';
                topos = coordinateString2Pos(move, 3);
            }
            else {
                topos = coordinateString2Pos(move, 1);
            }
            // now the only difficulty is to find the piece and its starting position...
            frompos = -1;
            for (ChessPiece p : piecesOnBoard) {
                // check if this piece matches the type and can move there in one hop.
                // TODO!!: it can still take wrong piece that is pinned to its king...
                if (p!=null) {
                    if (movingPceType == p.getPieceType()                                    // found Piece p that matches the wanted type
                            && (fromFile == -1 || fileOf(p.getPos()) == fromFile)       // no extra file is specified or it is correct
                            && (fromRank == -1 || rankOf(p.getPos()) == fromRank)       // same for rank
                            && boardSquares[topos].getShortestUnconditionalDistanceToPieceID(p.getPieceID()) == 1   // p can move here diectly (distance==1)
                            && !isPinnedByKing(p)                                         // p is not king-pinned
                            // TODO: ( ... || target-pos is blocking the way even after moving) - could also be solved by more intelligent condition stored in the distance to the king
                    ) {
                        frompos = p.getPos();
                        break;
                    }
                }
            }
            if (frompos==-1)
                return false;  // no matching piece found
        }
        System.out.print("("+squareName(frompos)+squareName(topos)+")");
        return doMove(frompos, topos, promoteToFigNr);
    }

    public boolean isPinnedByKing(ChessPiece p) {
        int sameColorKingPos = p.isWhite() ? whiteKingPos : blackKingPos;
        int pPos = p.getPos();
        for(Integer covpos : boardSquares[sameColorKingPos].coveredByOfColor(p.color()))
            if (covpos.intValue()==pPos)
                return true;
        return false;
    }

    private int getPromoteCharToPceTypeNr(char promoteToChar) {
        int promoteToFigNr;
        switch (promoteToChar) {
            case 'q', 'Q', 'd', 'D', ' ' -> promoteToFigNr = QUEEN;
            case 'n', 'N', 's', 'S' -> promoteToFigNr = KNIGHT;
            case 'b', 'B', 'l', 'L' -> promoteToFigNr = BISHOP;
            case 'r', 'R', 't', 'T' -> promoteToFigNr = ROOK;
            default -> {
                promoteToFigNr = QUEEN;
                System.err.println(format(chessBasicRes.getString("errorMessage.moveParsingError") + " {0}", promoteToChar));
            }
        }
        return promoteToFigNr;
    }

    private int char2PceTypeNr(char c) {
        int pceTypeNr;
        switch (c) {
            case 'q', 'Q', 'd', 'D' -> pceTypeNr = QUEEN;
            case 'n', 'N', 's', 'S' -> pceTypeNr = KNIGHT;
            case 'b', 'B', 'l', 'L' -> pceTypeNr = BISHOP;
            case 'r', 'R', 't', 'T' -> pceTypeNr = ROOK;
            case 'k', 'K' -> pceTypeNr = KING;
            case 'p', 'P', 'o', '*' -> pceTypeNr = PAWN;
            default -> {
                System.err.println(format(chessBasicRes.getString("errorMessage.moveParsingError") + " {0}", c));
                pceTypeNr = EMPTY;
            }
        }
        if (isWhite(getTurnCol()))
            return pceTypeNr;
        // black
        return BLACK_PIECE + pceTypeNr;
    }

    int getPieceTypeAt(int pos) {
        int pceID = boardSquares[pos].getPieceID();
        if (pceID==NOPIECE || piecesOnBoard[pceID]==null)
            return EMPTY;
        return piecesOnBoard[pceID].getPieceType();
    }

    private void takePieceAway(int topos) {
        //decreasePieceNrCounter(takenFigNr);
        //updateHash(takenFigNr, topos);
        ChessPiece p = getPieceAt(topos);
        if (p.isWhite())
            countOfWhitePieces--;
        else
            countOfBlackPieces--;
        p.die();
        emptySquare(topos);
    }

    private void basicMoveTo(int pceType, int pceID, int frompos, int topos) {
        //updateHash(..., frompos);
        //updateHash(..., topos);
        if (pceType==KING)
            whiteKingPos=topos;
        else if (pceType==KING_BLACK)
            blackKingPos=topos;
        piecesOnBoard[pceID].setPos(topos);
        emptySquare(frompos);
        boardSquares[topos].pieceMovedCloser(pceID);
    }

    private void emptySquare(int frompos) {
        boardSquares[frompos].emptySquare();
    }

    private void basicMoveTo(int frompos, int topos) {
        int pceID = getPieceIdAt(frompos);
        int pceType = getPieceTypeAt(frompos);
        basicMoveTo(pceType, pceID, frompos, topos);
    }

    public String getPieceFullName(int pceId) {
        return getPiece(pceId).toString();
    }

    public int getShortestUnconditionalDistanceToPosFromPieceId(int pos, int pceId) {
        return boardSquares[pos].getShortestUnconditionalDistanceToPieceID(pceId);
    }

    public int getShortestConditionalDistanceToPosFromPieceId(int pos, int pceId) {
        return boardSquares[pos].getShortestConditionalDistanceToPieceID(pceId);
    }

    public String getGameState() {
        checkAndEvaluateGameOver();
        if (isGameOver()) {
            if (isCheck(WHITE))
                return chessBasicRes.getString("state.blackWins");
            if (isCheck(BLACK))
                return chessBasicRes.getString("state.whiteWins");
            return chessBasicRes.getString("state.remis");
            //TODO: check and return stalemate
        }
        if (getFullMoves()==0)
            return chessBasicRes.getString("state.notStarted");
        return chessBasicRes.getString("state.ongoing");
    }

    public Iterator<ChessPiece> getPiecesIterator() {
        return Arrays.stream(piecesOnBoard).iterator();
    }
}
