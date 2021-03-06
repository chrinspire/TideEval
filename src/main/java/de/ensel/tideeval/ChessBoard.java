/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static de.ensel.tideeval.ChessBasics.*;
import static java.lang.Math.*;
import static java.text.MessageFormat.*;

public class ChessBoard {

    /**
     * configure here which debug messages should be printed
     */
    public static final boolean DEBUGMSG_DISTANCE_PROPAGATION = false;
    public static final boolean DEBUGMSG_CLASH_CALCULATION = false;
    public static final boolean DEBUGMSG_CBM_ERRORS = false;
    public static final boolean DEBUGMSG_TESTCASES = true;
    public static final boolean DEBUGMSG_BOARD_INIT = false;

    // controls the debug messages for the verification method of creating and comparing each board's properties
    // with a freshly created board (after each move)
    public static final boolean DEBUGMSG_BOARD_COMPARE_FRESHBOARD = false;  // full output
    public static final boolean DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL = false || DEBUGMSG_BOARD_COMPARE_FRESHBOARD;  // output only verification problems

    public static final boolean DEBUGMSG_BOARD_MOVES = false || DEBUGMSG_BOARD_COMPARE_FRESHBOARD;

    //const automatically activates the additional creation and compare with a freshly created board
    // do not change here, only via the DEBUGMSG_* above.
    public static final boolean DEBUG_BOARD_COMPARE_FRESHBOARD = DEBUGMSG_BOARD_COMPARE_FRESHBOARD || DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL;

    private int whiteKingPos;
    private int blackKingPos;
    private int currentDistanceCalcLimit;

    static final int MAX_INTERESTING_NROF_HOPS = 6;

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

    public static final int NO_PIECE_ID = -1;

    ChessPiece getPieceAt(int pos) {
        int pceID = getPieceIdAt(pos);
        if (pceID == NO_PIECE_ID)
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
        if (boardSquares[pos].getPieceID()== NO_PIECE_ID || getPieceAt(pos)==null )   // Todo-Option:  use assert(getPiecePos!=null)
            return false;
        return (getPieceAt(pos).color() == col);
    }

    public int distanceToKing(int pos, boolean kingCol) {
        int dx, dy;
        // Achtung, Implementierung passt sich nicht einer ver??nderten Boardgr????e an.
        if (kingCol == WHITE) {
            dx = abs((pos & 7) - (whiteKingPos & 7));
            dy = abs((pos >> 3) - (whiteKingPos >> 3));
        } else {
            dx = abs((pos & 7) - (blackKingPos & 7));
            dy = abs((pos >> 3) - (blackKingPos >> 3));
        }
        return max(dx, dy);
    }


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

    protected static final int EVAL_INSIGHT_LEVELS = 9;

    private static final String[] evalLabels = {
            "game state",
            "piece values",
            "basic mobility",
            "max.clashes",
            "new mobility",
            "attacks on opponent side",
            "attacks on opponent king",
            "defends on own king",
            "Mix Eval"
            //, "2xmobility + max.clash"
    };

    static String getEvaluationLevelLabel(int level) {
        return evalLabels[level];
    }

        // [EVAL_INSIGHT_LEVELS];

    /**
     * calculates board evaluation according to several "insight levels"
     * @param levelOfInsight: 1 - sum of lain standard figure values,
     *                        2 - take figure position into account
     *                       -1 - take best algorithm currently implemented
     * @return board evaluation in centipawns (+ for white, - for an advantage of black)
     */
    public int boardEvaluation(int levelOfInsight) {
        if (levelOfInsight>=EVAL_INSIGHT_LEVELS || levelOfInsight<0)
            levelOfInsight = EVAL_INSIGHT_LEVELS-1;
        int[] eval = new int[EVAL_INSIGHT_LEVELS];
        // first check if its over...
        checkAndEvaluateGameOver();
        if (isGameOver()) {                         // gameOver
            if (isCheck(WHITE))
                eval[0] = WHITE_IS_CHECKMATE;
            else if (isCheck(BLACK))
                eval[0] = BLACK_IS_CHECKMATE;
            eval[0] = 0;
        }
        else if (isWhite(getTurnCol()))       // game is running
            eval[0] = +1;
        else
            eval[0] = -1;
        if (levelOfInsight==0)
            return eval[0];
        // even for gameOver we try to calculate the other evaluations "as if"
        int l=0;
        eval[++l] = evaluateAllPiecesBasicValueSum(); /*1*/
        if (levelOfInsight==l)
            return eval[l];
        eval[++l] = evaluateAllPiecesBasicMobility();
        if (levelOfInsight==l)
            return eval[1] + eval[l];
        eval[++l] = evaluateMaxClashes();
        if (levelOfInsight==l)
            return eval[1] + eval[l];
        eval[++l] = evaluateAllPiecesMobility();
        if (levelOfInsight==l)
            return eval[1] + eval[l];
        eval[++l] = evaluateOpponentSideAttack();
        if (levelOfInsight==l)
            return eval[1] + eval[l];
        eval[++l] = evaluateOpponentKingAreaAttack();
        if (levelOfInsight==l)
            return eval[1] + eval[l];
        eval[++l] = evaluateOwnKingAreaDefense();
        if (levelOfInsight==l)
            return eval[1] + eval[l];
        eval[++l] = (int)(eval[3]*1.2)+eval[4]+eval[5]+eval[6]+eval[7];
        if (levelOfInsight==l)
            return eval[1] + eval[l];

        // hier one should not be able to end up, according to the parameter restriction/correction at the beginning
        // - but javac does not see it like that...
        assert(false);
        return 0;
    }

    public int boardEvaluation() {
        // for a game that has ended, the official evaluation is in level 0 (so that the others remain available "as if")
        if (isGameOver())
            return boardEvaluation(0);
        return boardEvaluation(EVAL_INSIGHT_LEVELS-1);
    }

    private int evaluateAllPiecesBasicValueSum() {
        /*error: return getPiecesStream()
                .filter(Objects::nonNull)
                .mapToInt(pce -> pce.getBaseValue() )
                .sum(); */
        //or old fashioned :-)
        int pceValSum = 0;
        for (ChessPiece pce: piecesOnBoard)
            if (pce!=null)
                pceValSum += pce.getBaseValue();
        return pceValSum;

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
        // sum first three levels up into one value, but weight later hops lesser
        int mobSum = mobSumPerHops[0];
        for (int i=1; i<=2; i++)  // MAX_INTERESTING_NROF_HOPS
            mobSum += mobSumPerHops[i]>>(i+1);   // rightshift, so hops==2 counts quater, hops==3 counts only eightth...
        return (int)(mobSum*0.9);
    }

    private int evaluateAllPiecesMobility() {
        // this is not using streams, but a loop, as the return-type int[] is to complex to "just sum up"
        int mobSum = 0;
        // init mobility sum per hop
        for (ChessPiece pce: piecesOnBoard) {
            if (pce!=null) {
                //add this pieces mobility to overall sum
                if (pce.isWhite())
                    mobSum += pce.getMobilities()*EVALPARAM_CP_PER_MOBILITYSQUARE;
                else
                    mobSum -= pce.getMobilities()*EVALPARAM_CP_PER_MOBILITYSQUARE;
            }
        }
        return (int)(mobSum);
    }


    /* sum of clashes brings no benefit:
    of board evaluations: 17421
    Quality of level clash sum (3):  (same as basic piece value: 6769)
     - improvements: 5687 (-112)
     - totally wrong: 4167 (164); - overdone: 798 (142)
    private int evaluateSumOfDirectClashResultsOnOccupiedSquares() {
        int clashSumOnHopLevel1 = 0;
        for (ChessPiece p: piecesOnBoard) {
            if (p==null)
                continue;
            Square s = boardSquares[p.getPos()];
            clashSumOnHopLevel1 += s.clashEval(1);
        }
        return clashSumOnHopLevel1;
    } */

    int evaluateMaxClashes() {
        int clashMaxWhite=Integer.MIN_VALUE;
        int clashMinBlack=Integer.MAX_VALUE;
        for (ChessPiece p: piecesOnBoard) {
            if (p==null)
                continue;
            int clashResult = boardSquares[p.getPos()].clashEval(1);
            if (p.isWhite()) {
                clashMinBlack = min(clashMinBlack, clashResult);
            }
            else {
                clashMaxWhite = max(clashMaxWhite, clashResult);
            }
        }
        // for a first simple analysis we do not look at any dependencies of the clashes.
        // assumption: the color whose turn it is can either win at least the best clash
        // or hinder the opponent from its best clash (we calc it as reduction to 1/16th)
        // after that, the opponent does the same - but this for now is counted only half...
        debugPrintln(DEBUGMSG_CLASH_CALCULATION, String.format(" w: %d  b: %d ",clashMaxWhite, clashMinBlack));
        if (isWhite(getTurnCol())) {
            if (clashMaxWhite > -clashMinBlack)
                return    (clashMaxWhite>Integer.MIN_VALUE ? clashMaxWhite   : 0)
                        + (clashMinBlack<Integer.MAX_VALUE ? clashMinBlack/2 : 0);
            return (clashMaxWhite>Integer.MIN_VALUE ? clashMaxWhite/4 : 0); // + clashMinBlack/8;
        }
        // else blacks turn
        if (clashMaxWhite < -clashMinBlack)
            return    (clashMaxWhite>Integer.MIN_VALUE ? clashMaxWhite/2 : 0)
                    + (clashMinBlack<Integer.MAX_VALUE ? clashMinBlack   : 0);
        return  (clashMinBlack<Integer.MAX_VALUE ? clashMinBlack/4 : 0); // + clashMaxWhite/8;
    }

    int evaluateOpponentSideAttack() {
        int pos;
        int sum=0;
        for (pos=0; pos<NR_FILES*3; pos++)
            sum += boardSquares[pos].getAttacksValueforColor(WHITE) * (rankOf(pos)>=6?2:1);
        for (pos=NR_SQUARES-NR_FILES*3; pos<NR_SQUARES; pos++)
            sum -= boardSquares[pos].getAttacksValueforColor(BLACK) * (rankOf(pos)<=1?2:1);
        return sum;
    }

    int evaluateOpponentKingAreaAttack() {
        int pos;
        int sum[]={0,0,0,0};
        for (pos=0; pos<NR_SQUARES; pos++) {
            int dbk = distanceToKing(pos, BLACK);
            int dwk = distanceToKing(pos, WHITE);
            if (dbk<4)
                sum[dbk] += boardSquares[pos].getAttacksValueforColor(WHITE);
            if (dwk<4)
                sum[dwk] -= boardSquares[pos].getAttacksValueforColor(BLACK);
        }
        return sum[1]*2 + sum[2] + sum[3]/3;
    }

    int evaluateOwnKingAreaDefense() {
        int pos;
        int sum[]={0,0,0,0};
        for (pos=0; pos<NR_SQUARES; pos++) {
            int dbk = distanceToKing(pos, BLACK);
            int dwk = distanceToKing(pos, WHITE);
            if (dbk<=3)
                sum[dbk] -= boardSquares[pos].getAttacksValueforColor(BLACK);
            if (dwk<=3)
                sum[dwk] += boardSquares[pos].getAttacksValueforColor(WHITE);
        }
        return sum[1] + sum[2] + sum[3]/4;
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
                int pceType = getPieceTypeAt(rank * 8 + file);
                if (pceType == EMPTY) {
                    spaceCounter++;
                }
                else {
                    if (spaceCounter > 0) {
                        fenString.append(spaceCounter);
                        spaceCounter = 0;
                    }
                    fenString.append(giveFENChar(pceType));
                }
            }
            if (spaceCounter > 0) {
                fenString.append(spaceCounter);
            }
        }
        return fenString + " " + getFENBoardPostfix();
    }
    //StringBuffer[] getBoard8StringsFromPieces();


    String getFENBoardPostfix() {
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
        if (fenBoard!=FENPOS_INITIAL)   // sic. string-pointer compare ok+wanted here
            debugPrintln(DEBUGMSG_BOARD_INIT, "with ["+fenBoard + "] ");
    }

    private void initChessBoard(StringBuffer boardName, String fenBoard) {
        debugPrintln(DEBUGMSG_BOARD_INIT, "");
        debugPrint(DEBUGMSG_BOARD_INIT, "New Board "+boardName+": ");
        this.boardName = boardName;
        setCurrentDistanceCalcLimit(0);
        initBoardFromFEN(fenBoard);
        completeDistanceCalc();
        checkAndEvaluateGameOver();
    }

    /**
     * triggers distance calculation for all pieces, stepwise up to toLimit
     * this eventually does the breadth distance propagation
     * @param toLimit final value of currentDistanceCalcLimit.
     */
    private void continueDistanceCalcUpTo(int toLimit) {
        for (int currentLimit=1; currentLimit<=toLimit; currentLimit++) {
            setCurrentDistanceCalcLimit(currentLimit);
            for (ChessPiece pce : piecesOnBoard)
                if (pce!=null)
                    pce.continueDistanceCalc();
        }
    }

    /**
     * triggers all open distance calculation for all pieces
     */
    void completeDistanceCalc() {
        // make sure the first hops are all calculated
        continueDistanceCalcUpTo(1);
        // update calc, of who can go where safely
        for (Square sq:boardSquares)
            sq.updateRelEvals();
        // continue with distance calc
        continueDistanceCalcUpTo(MAX_INTERESTING_NROF_HOPS);
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
     * @param pceType type of white or black piece - according to type constants in ChessBasics
     * @param pos square position on board, where to spawn that piece
     * @return returns pieceID of the new Piece
     */
    int spawnPieceAt(final int pceType, final int pos) {
        final int newPceID = nextFreePceID++;
        assert(nextFreePceID<=MAX_PIECES);
        assert(pos>=0 && pos<NR_SQUARES);
        if ( isPieceTypeWhite(pceType) )  {
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
            switch (colorlessPieceType(pceType)) {
                case ROOK   -> carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, HV_DIRS);
                case BISHOP -> {
                    if (isSameSquareColor(pos, p)) // only if square  has same square color than the bishop is standing on
                        carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, DIAG_DIRS); //TODO: leave out squares with wrong color for bishop
                }
                case QUEEN  -> carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, ROYAL_DIRS);
                case KING   -> carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, ROYAL_DIRS ); //TODO: Kings must avoid enemy-covered squares and be able to castle...
                case KNIGHT -> carefullyEstablishKnightNeighbourship4PieceID(newPceID, p, KNIGHT_DIRS);
                case PAWN -> {
                    if (piecesOnBoard[newPceID].pawnCanTheoreticallyReach(p))
                        carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, getAllPawnDirs(colorOfPieceType(pceType),rankOf(p)));
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
                case '*', 'p', '???' -> figNr = PAWN_BLACK;
                case 'o', 'P', '???' -> figNr = PAWN;
                case 'L', 'B', '???' -> figNr = BISHOP;
                case 'l', 'b', '???' -> figNr = BISHOP_BLACK;
                case 'T', 'R', '???' -> figNr = ROOK;
                case 't', 'r', '???' -> figNr = ROOK_BLACK;
                case 'S', 'N', '???' -> figNr = KNIGHT;
                case 's', 'n', '???' -> figNr = KNIGHT_BLACK;
                case 'K', '???'      -> figNr = KING;
                case 'k', '???'      -> figNr = KING_BLACK;
                case 'D', 'Q', '???' -> figNr = QUEEN;
                case 'd', 'q', '???' -> figNr = QUEEN_BLACK;
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
                emptyfields=0;
                //while (--emptyfields>0)
                    //figuresOnBoard[pos++]=null;
            }
            if (file>8)
                System.err.println("**** ??berlange Zeile gefunden beim Parsen an Position "+i+" des FEN-Strings "+fenString);
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
    public int getEnPassantPosForTurnColor(boolean color) {
        if (enPassantFile==-1)
            return -1;
        return fileRank2Pos(enPassantFile, isWhite(color)? 5 : 2 );
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
            internalErrorPrintln(String.format("Fehlerhafter Zug: %s%s ist au??erhalb des Boards %s.\n", squareName(frompos), squareName(topos), getBoardName()));
            return false;
        }
        final int pceID = getPieceIdAt(frompos);
        final int pceType = getPieceTypeAt(frompos);
        if (pceID == NO_PIECE_ID) { // || figuresOnBoard[frompos].getColor()!=turn  ) {
            internalErrorPrintln(String.format("Fehlerhafter Zug: auf %s steht keine Figur auf Board %s.\n", squareName(frompos), getBoardName()));
            return false;
        }
        if (boardSquares[topos].getUnconditionalDistanceToPieceIdIfShortest(pceID) != 1
                && colorlessPieceType(pceType) != KING) { // || figuresOnBoard[frompos].getColor()!=turn  ) {
            // TODO: check king for allowed moves... excluded here, because castelling is not obeyed in distance calculation, yet.
            internalErrorPrintln(String.format("Fehlerhafter Zug: %s -> %s nicht m??glich auf Board %s.\n", squareName(frompos), squareName(topos), getBoardName()));
            return false;
        }
        final int toposPceID = getPieceIdAt(topos);
        final int toposType = getPieceTypeAt(topos);

        // take figure
        if (toposPceID != NO_PIECE_ID) {
            takePieceAway(topos);
            /*old code to update pawn-evel-parameters
            if (takenFigNr==NR_PAWN && toRow==getWhitePawnRowAtCol(toCol))
                refindWhitePawnRowAtColBelow(toCol,toRow+1);  // try to find other pawn in column where the pawn was beaten
            else if (takenFigNr==NR_PAWN_BLACK && toRow==getBlackPawnRowAtCol(toCol))
                refindBlackPawnRowAtColBelow(toCol,toRow-1);*/
        }
        if (colorlessPieceType(pceType)==PAWN || toposPceID!= NO_PIECE_ID)
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

        // castelling:
        // i) also move rook  ii) update castelling rights
        // TODO: put castelling square numbers in constants in ChessBasics...
        if (pceType==KING_BLACK) {
            if (frompos == 4 && topos == 6)
                basicMoveTo(7, 5);
            else if (frompos == 4 && topos == 2)
                basicMoveTo(0, 3);
            blackKingsideCastleAllowed = false;
            blackQueensideCastleAllowed = false;
        } else if (pceType==KING) {
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

        // move
        basicMoveTo(pceType, pceID, frompos, topos);

        // promote to
        if (promoteToPceTypeNr>0 && colorlessPieceType(promoteToPceTypeNr)!=PAWN) {
            if (toposType==PAWN && isLastRank(topos)) {
                takePieceAway(topos);
                spawnPieceAt(promoteToPceTypeNr, topos);
            } else if (toposType==PAWN_BLACK && isFirstRank(topos)) {
                takePieceAway(topos);
                spawnPieceAt(promoteToPceTypeNr > BLACK_PIECE ? promoteToPceTypeNr : promoteToPceTypeNr + BLACK_PIECE, topos);
            }
        }

        turn = !turn;
        if (isWhite(turn))
            fullMoves++;

        // in debug mode compare with freshly created board from same fenString
        if (DEBUG_BOARD_COMPARE_FRESHBOARD)
            this.equals( new ChessBoard("CmpBoard", this.getBoardFEN()) );

        return true;
    }


    public boolean doMove(@NotNull String move) {
        int startpos=0;
        // skip spaces
        while (startpos<move.length() && move.charAt(startpos)==' ')
            startpos++;
        move = move.substring(startpos);
        if (isWhite(turn)) {
            debugPrint(DEBUGMSG_BOARD_MOVES, " "+getFullMoves()+".");
        }
        debugPrint(DEBUGMSG_BOARD_MOVES, " "+move);
        // an empty move string is not a legal move
        if (move.isEmpty())
            return false;

        int frompos;
        int topos;
        int promoteToFigNr=EMPTY;
        if ( move.length()>=4
                && isFileChar( move.charAt(0)) && isRankChar(move.charAt(1) )
                && isFileChar( move.charAt(2)) && isRankChar(move.charAt(3) )
        ) {
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
                promcharpos = 2;
            }
            // promotion character indicates what a pawn should be promoted to
            if ( (isBlack(getTurnCol()) && isFirstRank(topos)
                  || isWhite(getTurnCol()) && isLastRank(topos) ) ) {
                char promoteToChar = move.length() > promcharpos ? move.charAt(promcharpos) : 'q';
                if (promoteToChar=='=') // some notations use a1=Q isntead of a1Q
                    promoteToChar = move.length() > promcharpos+1 ? move.charAt(promcharpos+1) : 'q';
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
            if ( isFileChar(move.charAt(2)) ) {
                // the topos starts only one character later, so there must be an intermediate information
                if ( move.charAt(1)=='x' ) {   // its beating something - actually we do not care if this is true...
                }
                else if ( isFileChar(move.charAt(1)) )  // a starting file
                    fromFile = move.charAt(1)-'a';
                else if ( isRankChar(move.charAt(1)) )  // a starting rank
                    fromRank = move.charAt(1)-'1';
                topos = coordinateString2Pos(move, 2);
            }
            else if ( move.charAt(2)=='x' ) {
                // a starting file or rank + a beating x..., like "Rfxf2"
                if ( isFileChar(move.charAt(1)) )      // a starting file
                    fromFile = move.charAt(1)-'a';
                else if ( isRankChar(move.charAt(1)) ) // a starting rank
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
                if (p!=null && movingPceType == p.getPieceType()                                    // found Piece p that matches the wanted type
                            && (fromFile == -1 || fileOf(p.getPos()) == fromFile)       // no extra file is specified or it is correct
                            && (fromRank == -1 || rankOf(p.getPos()) == fromRank)       // same for rank
                            && boardSquares[topos].getDistanceToPieceId(p.getPieceID()) == 1   // p can move here diectly (distance==1)
                            && moveIsNotBlockedByKingPin(p, topos)                                         // p is not king-pinned or it is pinned but does not move out of the way.
                    ) {
                        frompos = p.getPos();
                        break;
                }
            }
            if (frompos==-1)
                return false;  // no matching piece found
        }
        debugPrint(DEBUGMSG_BOARD_MOVES, "("+squareName(frompos)+squareName(topos)+")");
        return doMove(frompos, topos, promoteToFigNr);
    }

    /** p is not king-pinned or it is pinned but does not move out of the way.
     *
     */
    public boolean moveIsNotBlockedByKingPin(ChessPiece p, int topos) {
        int sameColorKingPos = p.isWhite() ? whiteKingPos : blackKingPos;
        if (!isPiecePinnedToPos(p,sameColorKingPos))
            return true;   // p is not king-pinned
        if (colorlessPieceType(p.getPieceType())==KNIGHT)
            return false;  // a king-pinned knight can never move away in a way that it still avoids the chess
        // or it is pinned, but does not move out of the way.
        int king2PceDir = calcDirFromTo(sameColorKingPos, topos);
        int king2TargetDir = calcDirFromTo(sameColorKingPos, p.getPos());
        return king2PceDir == king2TargetDir;
        // TODO?:  could also be solved by more intelligent condition stored in the distance to the king
    }


    public boolean isPiecePinnedToPos(ChessPiece p, int pos) {
        int pPos = p.getPos();
        List<Integer> listOfSquarePositionsCoveringPos = boardSquares[pos].getPositionsOfPiecesThatBlockWayAndAreOfColor(p.color());
        for(Integer covpos : listOfSquarePositionsCoveringPos )
            if (covpos==pPos)
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
                internalErrorPrintln(format(chessBasicRes.getString("errorMessage.moveParsingError") + " '{0}'", promoteToChar));
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
                internalErrorPrintln(format(chessBasicRes.getString("errorMessage.moveParsingError") + " <{0}>", c));
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
        if (pceID== NO_PIECE_ID || piecesOnBoard[pceID]==null)
            return EMPTY;
        return piecesOnBoard[pceID].getPieceType();
    }

    private void takePieceAway(int topos) {
        //decreasePieceNrCounter(takenFigNr);
        //updateHash(takenFigNr, topos);
        ChessPiece p = getPieceAt(topos);
        piecesOnBoard[p.getPieceID()] = null;
        if (p.isWhite())
            countOfWhitePieces--;
        else
            countOfBlackPieces--;
        for (Square s : boardSquares)
            s.removePiece(p.getPieceID());
        p.die();
        emptySquare(topos);
    }

    private void basicMoveTo(final int pceType, final int pceID, final int frompos, final int topos) {
        //updateHash(..., frompos);
        //updateHash(..., topos);
        if (pceType==KING)
            whiteKingPos=topos;
        else if (pceType==KING_BLACK)
            blackKingPos=topos;
        emptySquare(frompos);
        piecesOnBoard[pceID].setPos(topos);

        setCurrentDistanceCalcLimit(0);
        boardSquares[topos].movePieceHereFrom(pceID, frompos);

        // for Test: "deactivation of recalc eval in doMove-methods in ChessBoard
        //           for manual tests with full Board reconstruction of every position, instead of evolving evaluations per move (just to compare speed)"
        // deactivate the following (correct) code:
        completeDistanceCalc();

        setCurrentDistanceCalcLimit(0);
        boardSquares[frompos].pieceHasMovedAway();
        completeDistanceCalc();
    }

    public boolean isSquareEmpty(final int pos) {
        return (boardSquares[pos].getPieceID()==NO_PIECE_ID);
    }

    private void emptySquare(final int frompos) {
        boardSquares[frompos].emptySquare();
    }

    private void basicMoveTo(final int frompos, final int topos) {
        int pceID = getPieceIdAt(frompos);
        int pceType = getPieceTypeAt(frompos);
        basicMoveTo(pceType, pceID, frompos, topos);
    }

    public String getPieceFullName(int pceId) {
        return getPiece(pceId).toString();
    }

    public int XXXgetShortestUnconditionalDistanceToPosFromPieceId(int pos, int pceId) {
        // TODO: eliminate method and calls
        return getDistanceToPosFromPieceId(pos,pceId);
        //return boardSquares[pos].getShortestUnconditionalDistanceToPieceID(pceId);
    }

    public int getDistanceToPosFromPieceId(int pos, int pceId) {
        return boardSquares[pos].getDistanceToPieceId(pceId);
    }

    public boolean isDistanceToPosFromPieceIdUnconditional(int pos, int pceId) {
        return boardSquares[pos].getConditionalDistanceToPieceId(pceId).isUnconditional();
    }

    ConditionalDistance getDistanceFromPieceId(int pos, int pceId) {
        return boardSquares[pos].getConditionalDistanceToPieceId(pceId);
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

    // virtual non-linear, but continuously increasing "clock" used to remember update-"time"s and check if information is outdated
    private long updateClockFineTicks =0;

    public int getNrOfPlys() {
        if (isWhite(turn))
            return fullMoves*2;
        return fullMoves*2+1;
    }

    public long getUpdateClock() {
        return getNrOfPlys()*1000L + updateClockFineTicks;
    }

    public long nextUpdateClockTick() {
        ++updateClockFineTicks;
        return getUpdateClock();
    }

    public static void internalErrorPrintln(String s) {
        System.out.println( chessBasicRes.getString("errormessage.errorPrefix") + s );
    }


    public static void debugPrint(boolean doPrint, String s) {
        if (doPrint)
            System.out.print( s );
    }

    public static void debugPrintln(boolean doPrint, String s) {
        if (doPrint)
            System.out.println( s );
    }


    public int currentDistanceCalcLimit() {
        return currentDistanceCalcLimit;
    }

    private void setCurrentDistanceCalcLimit(int newLimit) {
        currentDistanceCalcLimit =min(MAX_INTERESTING_NROF_HOPS, newLimit);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChessBoard other = (ChessBoard) o;
        debugPrint(DEBUGMSG_BOARD_COMPARE_FRESHBOARD, "Comparing Boards: " + this.getBoardName() + " with " + other.getBoardName() + ":  ");

        boolean equal = compareWithDebugMessage("White King Pos", whiteKingPos, other.whiteKingPos);
        equal &= compareWithDebugMessage("Black King Pos", blackKingPos, other.blackKingPos);
        equal &= compareWithDebugMessage("White King Checks", whiteKingChecks , other.whiteKingChecks);
        equal &= compareWithDebugMessage("Black King Checks", blackKingChecks, other.blackKingChecks);
        equal &= compareWithDebugMessage("Count White Pieces", countOfWhitePieces, other.countOfWhitePieces);
        equal &= compareWithDebugMessage("Count Black Pieces", countOfBlackPieces, other.countOfBlackPieces);
        equal &= compareWithDebugMessage("GameO ver", gameOver, other.gameOver);
        equal &= compareWithDebugMessage("Turn", turn, other.turn);
        equal &= compareWithDebugMessage("White Kingside Castling Allowed", whiteKingsideCastleAllowed, other.whiteKingsideCastleAllowed);
        equal &= compareWithDebugMessage("White Queenside Castling Allowed", whiteQueensideCastleAllowed, other.whiteQueensideCastleAllowed);
        equal &= compareWithDebugMessage("Black Kingside Castling Allowed", blackKingsideCastleAllowed, other.blackKingsideCastleAllowed);
        equal &= compareWithDebugMessage("Black Queenside Castling Allowed", blackQueensideCastleAllowed, other.blackQueensideCastleAllowed);
        equal &= compareWithDebugMessage("EnPassant File", enPassantFile, other.enPassantFile);
        equal &= compareWithDebugMessage("Boring Moves", countBoringMoves, other.countBoringMoves);
        equal &= compareWithDebugMessage("Full Moves", fullMoves, other.fullMoves);
        for (int pos=0; pos<NR_SQUARES; pos++) {
            int pceId = boardSquares[pos].getPieceID();
            if (pceId!=NO_PIECE_ID) {
                // piece found, get id of same piece on other board
                int otherPceId = other.boardSquares[pos].getPieceID();
                // compare all vPieces with this PceID on all squares
                for (int vpos=0; vpos<NR_SQUARES; vpos++) {
                    VirtualPieceOnSquare thisVPce = boardSquares[vpos].getvPiece(pceId);
                    VirtualPieceOnSquare otherVPce = other.boardSquares[vpos].getvPiece(otherPceId);
                    equal &= thisVPce.equals(otherVPce);
                    //equal &= compareWithDebugMessage(thisVPce+" myPCeId", thisVPce.myPceID, otherVPce.myPceID );
                }
            }
        }
        if (equal)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD, " --> ok" );
        else
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL, " --> Problem on Board " +this.getBoardFEN() );
        return equal;
    }

    static boolean compareWithDebugMessage(String debugMesg, int thisInt, int otherInt) {
        boolean cmp = (thisInt==otherInt);
        if (!cmp)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL, debugMesg + ": " + thisInt + " != " + otherInt);
        return cmp;
    }

    static boolean compareWithDebugMessage(String debugMesg, boolean thisBoolean, boolean otherBoolean) {
        boolean cmp = (thisBoolean==otherBoolean);
        if (!cmp)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL, debugMesg + ": " + thisBoolean + " != " + otherBoolean);
        return cmp;
    }

    static boolean compareWithDebugMessage(String debugMesg,
                                           ConditionalDistance thisDistance,
                                           ConditionalDistance otherDistance) {
        boolean cmp = (thisDistance.dist() == otherDistance.dist()
            || thisDistance.dist()>=MAX_INTERESTING_NROF_HOPS && otherDistance.dist()>=MAX_INTERESTING_NROF_HOPS );
        if (!cmp)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL,
                    debugMesg + ": " + thisDistance
                               + " != " + otherDistance);
        return cmp;
    }

    static boolean compareWithDebugMessage(String debugMesg, int[] thisIntArray, int[] otherIntArray) {
        boolean cmp = Arrays.equals(thisIntArray, otherIntArray);
        if (!cmp)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL, debugMesg + ": " + Arrays.toString(thisIntArray) + " != " + Arrays.toString(otherIntArray)) ;
        return cmp;
    }

    static boolean compareWithDebugMessage(String debugMesg,
                                           ConditionalDistance[] thisDistanceArray,
                                           ConditionalDistance[] otherDistanceArray) {
        boolean cmp = true;
        for (int i = 0; i<thisDistanceArray.length; i++ )
            cmp &= compareWithDebugMessage(debugMesg+"["+i+"]",
                    thisDistanceArray[i],
                    otherDistanceArray[i]);
        return cmp;
    }

}
