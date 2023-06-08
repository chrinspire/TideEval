/*
 *     TideEval - Wired New Chess Algorithm
 *     Copyright (C) 2023 Christian Ensel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.Move.getMoves;
import static java.lang.Math.*;
import static java.text.MessageFormat.*;

public class ChessBoard {

    /**
     * configure here which debug messages should be printed
     * (not final, to be able to change it during debugging)
     */
    public static boolean DEBUGMSG_DISTANCE_PROPAGATION = false;
    public static boolean DEBUGMSG_CLASH_CALCULATION = false;
    public static boolean DEBUGMSG_CBM_ERRORS = false;
    public static boolean DEBUGMSG_TESTCASES = false;
    public static boolean DEBUGMSG_BOARD_INIT = false;
    public static boolean DEBUGMSG_FUTURE_CLASHES = false;
    public static boolean DEBUGMSG_MOVEEVAL = false;
    public static boolean DEBUGMSG_MOVESELECTION = false || DEBUGMSG_MOVEEVAL;

    // controls the debug messages for the verification method of creating and comparing each board's properties
    // with a freshly created board (after each move)
    public static final boolean DEBUGMSG_BOARD_COMPARE_FRESHBOARD = false;  // full output
    public static final boolean DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL = false || DEBUGMSG_BOARD_COMPARE_FRESHBOARD;  // output only verification problems

    public static final boolean DEBUGMSG_BOARD_MOVES = false || DEBUGMSG_BOARD_COMPARE_FRESHBOARD;

    //const automatically activates the additional creation and compare with a freshly created board
    // do not change here, only via the DEBUGMSG_* above.
    public static final boolean DEBUG_BOARD_COMPARE_FRESHBOARD = DEBUGMSG_BOARD_COMPARE_FRESHBOARD || DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL;

    public static int DEBUGFOCUS_SQ = coordinateString2Pos("c4");   // changeable globally, just for debug output and breakpoints+watches
    public static int DEBUGFOCUS_VP = 6;   // changeable globally, just for debug output and breakpoints+watches
    private final ChessBoard board = this;       // only exists to make naming in debug evaluations easier (unified across all classes)

    private int whiteKingPos;
    private int blackKingPos;
    private int currentDistanceCalcLimit;

    public static int MAX_INTERESTING_NROF_HOPS = 6;
    private int[] nrOfLegalMoves = new int[2];
    private Move bestMove;

    //private int[] kingChecks  = new int[2];
    private boolean gameOver;

    private Square[] boardSquares;
    String fenPosAndMoves;


    /*public int getPieceNrCounterForColor(int figNr, boolean whitecol) {
        return whitecol ? countOfWhiteFigNr[abs(figNr)]
                        : countOfBlackFigNr[abs(figNr)];
    }*/
    //int getPieceNrCounter(int pieceNr);


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

    public ChessBoard(String boardName, String fenBoard) {
        initChessBoard(new StringBuffer(boardName), fenBoard);
        if (fenBoard != FENPOS_INITIAL)   // sic. string-pointer compare ok+wanted here
            debugPrintln(DEBUGMSG_BOARD_INIT, "with [" + fenBoard + "] ");
    }

    private void initChessBoard(StringBuffer boardName, String fenBoard) {
        debugPrintln(DEBUGMSG_BOARD_INIT, "");
        debugPrint(DEBUGMSG_BOARD_INIT, "New Board " + boardName + ": ");
        this.boardName = boardName;
        setCurrentDistanceCalcLimit(0);
        updateBoardFromFEN(fenBoard);
        calcBestMove();
        checkAndEvaluateGameOver();
    }


    private void emptyBoard() {
        piecesOnBoard = new ChessPiece[MAX_PIECES];
        countOfWhitePieces = 0;
        countOfBlackPieces = 0;
        nextFreePceID = 0;
        boardSquares = new Square[NR_SQUARES];
        for (int p = 0; p < NR_SQUARES; p++) {
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


    /////
    ///// the Chess Pieces as such
    /////

    /**
     * keep all Pieces on Board
     */
    ChessPiece[] piecesOnBoard;
    private int nextFreePceID;
    public static final int NO_PIECE_ID = -1;
    private int countOfWhitePieces;
    private int countOfBlackPieces;

    public ChessPiece getPiece(int pceID) {
        assert (pceID < nextFreePceID);
        return piecesOnBoard[pceID];
    }

    /**
     * give ChessPiece at pos
     *
     * @param pos board position
     * @return returns ChessPiece or null if quare is empty
     */
    ChessPiece getPieceAt(int pos) {
        int pceID = getPieceIdAt(pos);
        if (pceID == NO_PIECE_ID)
            return null;
        return piecesOnBoard[pceID];
    }

    public int getPieceIdAt(int pos) {
        return boardSquares[pos].getPieceID();
    }

    boolean hasPieceOfColorAt(boolean col, int pos) {
        if (boardSquares[pos].getPieceID() == NO_PIECE_ID || getPieceAt(pos) == null)   // Todo-Option:  use assert(getPiecePos!=null)
            return false;
        return (getPieceAt(pos).color() == col);
    }

    public int distanceToKing(int pos, boolean kingCol) {
        if (kingCol == WHITE)
            return distanceBetween(pos, whiteKingPos);
        else
            return distanceBetween(pos, blackKingPos);
    }

    public boolean isCheck(boolean col) {
        return nrOfChecks(col) > 0;
    }

    public int nrOfChecks(boolean col) {
        int kingPos = getKingPos(col);
        if (kingPos < 0)
            return 0;  // king does not exist... should not happen, but is part of some test-positions
        Square kingSquare = getBoardSquares()[kingPos];
        return kingSquare.countDirectAttacksWithout2ndRowWithColor(opponentColor(col));
    }

    /////
    ///// the Chess Game as such
    /////

    private void checkAndEvaluateGameOver() {    // called to check+evaluate if there are no more moves left or 50-rules-move is violated
        if (countOfWhitePieces <= 0 || countOfBlackPieces <= 0) {
            gameOver = true;
        } else if (isCheck(getTurnCol()) && nrOfLegalMoves(getTurnCol()) == 0) {
            gameOver = true;
        } else
            gameOver = false;
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
     *
     * @param levelOfInsight: 1 - sum of lain standard figure values,
     *                        2 - take figure position into account
     *                        -1 - take best algorithm currently implemented
     * @return board evaluation in centipawns (+ for white, - for an advantage of black)
     */
    public int boardEvaluation(int levelOfInsight) {
        if (levelOfInsight >= EVAL_INSIGHT_LEVELS || levelOfInsight < 0)
            levelOfInsight = EVAL_INSIGHT_LEVELS - 1;
        int[] eval = new int[EVAL_INSIGHT_LEVELS];
        // first check if its over...
        checkAndEvaluateGameOver();
        if (isGameOver()) {                         // gameOver
            if (isCheck(WHITE))
                eval[0] = WHITE_IS_CHECKMATE;
            else if (isCheck(BLACK))
                eval[0] = BLACK_IS_CHECKMATE;
            eval[0] = 0;
        } else if (isWhite(getTurnCol()))       // game is running
            eval[0] = +1;
        else
            eval[0] = -1;
        if (levelOfInsight == 0)
            return eval[0];
        // even for gameOver we try to calculate the other evaluations "as if"
        int l = 0;
        eval[++l] = evaluateAllPiecesBasicValueSum(); /*1*/
        if (levelOfInsight == l)
            return eval[l];
        eval[++l] = evaluateAllPiecesBasicMobility();
        if (levelOfInsight == l)
            return eval[1] + eval[l];
        eval[++l] = evaluateMaxClashes();
        if (levelOfInsight == l)
            return eval[1] + eval[l];
        eval[++l] = evaluateAllPiecesMobility();
        if (levelOfInsight == l)
            return eval[1] + eval[l];
        eval[++l] = evaluateOpponentSideAttack();
        if (levelOfInsight == l)
            return eval[1] + eval[l];
        eval[++l] = evaluateOpponentKingAreaAttack();
        if (levelOfInsight == l)
            return eval[1] + eval[l];
        eval[++l] = evaluateOwnKingAreaDefense();
        if (levelOfInsight == l)
            return eval[1] + eval[l];
        eval[++l] = (int) (eval[3] * 1.2) + eval[4] + eval[5] + eval[6] + eval[7];
        if (levelOfInsight == l)
            return eval[1] + eval[l];

        // hier one should not be able to end up, according to the parameter restriction/correction at the beginning
        // - but javac does not see it like that...
        assert (false);
        return 0;
    }

    public int boardEvaluation() {
        // for a game that has ended, the official evaluation is in level 0 (so that the others remain available "as if")
        if (isGameOver())
            return boardEvaluation(0);
        return boardEvaluation(EVAL_INSIGHT_LEVELS - 1);
    }

    private int evaluateAllPiecesBasicValueSum() {
        /*error: return getPiecesStream()
                .filter(Objects::nonNull)
                .mapToInt(pce -> pce.getBaseValue() )
                .sum(); */
        //or old fashioned :-)
        int pceValSum = 0;
        for (ChessPiece pce : piecesOnBoard)
            if (pce != null)
                pceValSum += pce.baseValue();
        return pceValSum;

    }

    // idea: could become an adapdable parameter later
    private static int EVALPARAM_CP_PER_MOBILITYSQUARE = 4;

    private int evaluateAllPiecesBasicMobility() {
        // this is not using streams, but a loop, as the return-type int[] is to complex to "just sum up"
        int[] mobSumPerHops = new int[MAX_INTERESTING_NROF_HOPS];
        // init mobility sum per hop
        for (int i = 0; i < MAX_INTERESTING_NROF_HOPS; i++)
            mobSumPerHops[i] = 0;
        for (ChessPiece pce : piecesOnBoard) {
            if (pce != null) {
                int[] pceMobPerHops = pce.getSimpleMobilities();
                //add this pieces mobility per hop to overall the sub per hop
                if (isWhite(pce.color()))
                    for (int i = 0; i < MAX_INTERESTING_NROF_HOPS; i++)
                        mobSumPerHops[i] += pceMobPerHops[i] * EVALPARAM_CP_PER_MOBILITYSQUARE;
                else  // count black as negative
                    for (int i = 0; i < MAX_INTERESTING_NROF_HOPS; i++)
                        mobSumPerHops[i] -= pceMobPerHops[i] * EVALPARAM_CP_PER_MOBILITYSQUARE;
            }
        }
        // sum first three levels up into one value, but weight later hops lesser
        int mobSum = mobSumPerHops[0];
        for (int i = 1; i < Math.min(3, MAX_INTERESTING_NROF_HOPS); i++)  // MAX_INTERESTING_NROF_HOPS
            mobSum += mobSumPerHops[i] >> (i + 1);   // rightshift, so hops==2 counts quater, hops==3 counts only eightth...
        return (int) (mobSum * 0.9);
    }

    private int evaluateAllPiecesMobility() {
        // this is not using streams, but a loop, as the return-type int[] is to complex to "just sum up"
        int mobSum = 0;
        // init mobility sum per hop
        for (ChessPiece pce : piecesOnBoard) {
            if (pce != null) {
                //add this pieces mobility to overall sum
                if (pce.isWhite())
                    mobSum += pce.getMobilities() * EVALPARAM_CP_PER_MOBILITYSQUARE;
                else
                    mobSum -= pce.getMobilities() * EVALPARAM_CP_PER_MOBILITYSQUARE;
            }
        }
        return (int) (mobSum);
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
        int clashMaxWhite = Integer.MIN_VALUE;
        int clashMinBlack = Integer.MAX_VALUE;
        for (ChessPiece p : piecesOnBoard) {
            if (p == null)
                continue;
            int clashResult = boardSquares[p.getPos()].clashEval();
            if (p.isWhite()) {
                clashMinBlack = min(clashMinBlack, clashResult);
            } else {
                clashMaxWhite = max(clashMaxWhite, clashResult);
            }
        }
        // for a first simple analysis we do not look at any dependencies of the clashes.
        // assumption: the color whose turn it is can either win at least the best clash
        // or hinder the opponent from its best clash (we calc it as reduction to 1/16th)
        // after that, the opponent does the same - but this for now is counted only half...
        debugPrintln(DEBUGMSG_CLASH_CALCULATION, String.format(" w: %d  b: %d ", clashMaxWhite, clashMinBlack));
        if (isWhite(getTurnCol())) {
            if (clashMaxWhite > -clashMinBlack)
                return (clashMaxWhite > Integer.MIN_VALUE ? clashMaxWhite : 0)
                        + (clashMinBlack < Integer.MAX_VALUE ? clashMinBlack / 2 : 0);
            return (clashMaxWhite > Integer.MIN_VALUE ? clashMaxWhite / 4 : 0); // + clashMinBlack/8;
        }
        // else blacks turn
        if (clashMaxWhite < -clashMinBlack)
            return (clashMaxWhite > Integer.MIN_VALUE ? clashMaxWhite / 2 : 0)
                    + (clashMinBlack < Integer.MAX_VALUE ? clashMinBlack : 0);
        return (clashMinBlack < Integer.MAX_VALUE ? clashMinBlack / 4 : 0); // + clashMaxWhite/8;
    }

    int evaluateOpponentSideAttack() {
        int pos;
        int sum = 0;
        for (pos = 0; pos < NR_FILES * 3; pos++)
            sum += boardSquares[pos].getAttacksValueforColor(WHITE) * (rankOf(pos) >= 6 ? 2 : 1);
        for (pos = NR_SQUARES - NR_FILES * 3; pos < NR_SQUARES; pos++)
            sum -= boardSquares[pos].getAttacksValueforColor(BLACK) * (rankOf(pos) <= 1 ? 2 : 1);
        return sum;
    }

    int evaluateOpponentKingAreaAttack() {
        int pos;
        int[] sum = {0, 0, 0, 0};
        for (pos = 0; pos < NR_SQUARES; pos++) {
            int dbk = distanceToKing(pos, BLACK);
            int dwk = distanceToKing(pos, WHITE);
            if (dbk < 4)
                sum[dbk] += boardSquares[pos].getAttacksValueforColor(WHITE);
            if (dwk < 4)
                sum[dwk] -= boardSquares[pos].getAttacksValueforColor(BLACK);
        }
        return sum[1] * 2 + sum[2] + sum[3] / 3;
    }

    int evaluateOwnKingAreaDefense() {
        int pos;
        int[] sum = {0, 0, 0, 0};
        for (pos = 0; pos < NR_SQUARES; pos++) {
            int dbk = distanceToKing(pos, BLACK);
            int dwk = distanceToKing(pos, WHITE);
            if (dbk <= 3)
                sum[dbk] -= boardSquares[pos].getAttacksValueforColor(BLACK);
            if (dwk <= 3)
                sum[dwk] += boardSquares[pos].getAttacksValueforColor(WHITE);
        }
        return sum[1] + sum[2] + sum[3] / 4;
    }


    /**
     * triggers distance calculation for all pieces, stepwise up to toLimit
     * this eventually does the breadth distance propagation
     *
     * @param toLimit final value of currentDistanceCalcLimit.
     */
    private void continueDistanceCalcUpTo(int toLimit) {
        for (ChessPiece pce : piecesOnBoard)
            if (pce != null)
                pce.cleanupCalcStart();
        for (int currentLimit = 1; currentLimit <= toLimit; currentLimit++) {
            setCurrentDistanceCalcLimit(currentLimit);
            nextUpdateClockTick();
            for (ChessPiece pce : piecesOnBoard)
                if (pce != null)
                    pce.continueDistanceCalc();
            nextUpdateClockTick();
            // update calc, of who can go where safely
            for (Square sq : boardSquares)
                sq.updateClashResultAndRelEvals();
            // update mobility per Piece  (Todo-Optimization: might later be updated implicitly during dist-calc)
            for (ChessPiece pce : piecesOnBoard)
                if (pce != null)
                    pce.updateMobilityInklMoves();
            if (currentLimit == 2) {
                calcCheckBlockingSquares();
                // collect legal moves
                for (ChessPiece p : piecesOnBoard)
                    if (p != null) {
                        p.collectMoves();
                    }
            }
        }
        for (Square sq : boardSquares) {
            sq.calcFutureClashEval();
        }
    }

    private void calcCheckBlockingSquares() {
        for (Square sq : getBoardSquares())
            sq.resetBlocksChecks();
        for (int ci = 0; ci <= 1; ci++) { // for both colors
            boolean col = colorFromColorIndex(ci);
            int kingpos = getKingPos(col);
            if (kingpos < 0)
                continue;          // in some test-cases boards without kings are used, so skip this (instead of error/abort)
            List<ChessPiece> attackers = getBoardSquares()[kingpos].DirectAttacksWithout2ndRowWithColor(opponentColor(col));
            for (ChessPiece a : attackers) {
                for (int pos : a.allPosOnWayTo(kingpos))
                    boardSquares[pos].setBlocksCheckFor(col);
            }
        }
    }

    /**
     * triggers all open distance calculation for all pieces
     */
    void completeCalc() {
        continueDistanceCalcUpTo(MAX_INTERESTING_NROF_HOPS);
        findReasonableTargets();
    }

    private void findReasonableTargets() {
        for (Square sq : boardSquares) {
            // TODO: sq.findChances();
        }
    }

    //boolean accessibleForKing(int pos, boolean myColor);
    //boolean coveredByMe(int pos, boolean color);
    //boolean coveredByMeExceptOne(int pos, boolean color, int pieceNr);

    //Piece getPieceOnSquare(int pos);
    //int getPieceNrOnSquare(int pos);

    private boolean turn;
    private StringBuffer boardName;

    /**
     * Get (simple) fen string from the current board
     * TODO make it return "real" fen string
     *
     * @return String in FEN notation representing the current board and game status
     */
    String getBoardFEN() {
        StringBuilder fenString = new StringBuilder();
        for (int rank = 0; rank < NR_RANKS; rank++) {
            if (rank > 0)
                fenString.append("/");
            int spaceCounter = 0;
            for (int file = 0; file < NR_FILES; file++) {
                int pceType = getPieceTypeAt(rank * 8 + file);
                if (pceType == EMPTY) {
                    spaceCounter++;
                } else {
                    if (spaceCounter > 0) {
                        fenString.append(spaceCounter);
                        spaceCounter = 0;
                    }
                    fenString.append(fenCharFromPceType(pceType));
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
        return (turn == WHITE ? " w " : " b ")
                + (isWhiteKingsideCastleAllowed() ? "K" : "") + (isWhiteQueensideCastleAllowed() ? "Q" : "")
                + (isBlackKingsideCastleAllowed() ? "k" : "") + (isBlackQueensideCastleAllowed() ? "q" : "")
                + ((!isWhiteKingsideCastleAllowed() && !isWhiteQueensideCastleAllowed()
                && !isBlackKingsideCastleAllowed() && !isBlackQueensideCastleAllowed()) ? "- " : " ")
                + (getEnPassantFile() == -1 ? "- " : (Character.toString(getEnPassantFile() + 'a') + (turn == WHITE ? "6" : "3")) + " ")
                + countBoringMoves
                + " " + fullMoves;
    }


    /**
     * create a new Piece on the board
     *
     * @param pceType type of white or black piece - according to type constants in ChessBasics
     * @param pos     square position on board, where to spawn that piece
     * @return returns pieceID of the new Piece
     */
    int spawnPieceAt(final int pceType, final int pos) {
        final int newPceID = nextFreePceID++;
        assert (nextFreePceID <= MAX_PIECES);
        assert (pos >= 0 && pos < NR_SQUARES);
        if (isPieceTypeWhite(pceType)) {
            countOfWhitePieces++;
            if (pceType == KING)
                whiteKingPos = pos;
        } else {
            countOfBlackPieces++;
            if (pceType == KING_BLACK)
                blackKingPos = pos;
        }

        piecesOnBoard[newPceID] = new ChessPiece(this, pceType, newPceID, pos);
        // tell all squares about this new piece
        for (Square sq : boardSquares)
            sq.prepareNewPiece(newPceID);

        // construct net of neighbours for this new piece
        for (int p = 0; p < NR_SQUARES; p++) {
            switch (colorlessPieceType(pceType)) {
                case ROOK -> carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, HV_DIRS);
                case BISHOP -> {
                    if (isSameSquareColor(pos, p)) // only if square  has same square color than the bishop is standing on
                        carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, DIAG_DIRS); //TODO: leave out squares with wrong color for bishop
                }
                case QUEEN -> carefullyEstablishSlidingNeighbourship4PieceID(newPceID, p, ROYAL_DIRS);
                case KING -> carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, ROYAL_DIRS); //TODO: Kings must avoid enemy-covered squares and be able to castle...
                case KNIGHT -> carefullyEstablishKnightNeighbourship4PieceID(newPceID, p, KNIGHT_DIRS);
                case PAWN -> {
                    if (piecesOnBoard[newPceID].pawnCanTheoreticallyReach(p))
                        carefullyEstablishSingleNeighbourship4PieceID(newPceID, p, getAllPawnDirs(colorOfPieceType(pceType), rankOf(p)));
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
                .filter(d -> neighbourSquareExistsInDirFromPos(d, pos))    // be careful at the borders
                .forEach(d -> vPiece.addSlidingNeighbour(boardSquares[pos + d].getvPiece(pid), d));
    }

    private void carefullyEstablishSingleNeighbourship4PieceID(int pid, int pos, int[] neighbourDirs) {
        VirtualPieceOnSquare vPiece = boardSquares[pos].getvPiece(pid);
        Arrays.stream(neighbourDirs)
                .filter(d -> neighbourSquareExistsInDirFromPos(d, pos))    // be careful at the borders
                .forEach(d -> vPiece.addSingleNeighbour(boardSquares[pos + d].getvPiece(pid)));
    }

    private void carefullyEstablishKnightNeighbourship4PieceID(int pid, int pos, int[] neighbourDirs) {
        VirtualPieceOnSquare vPiece = boardSquares[pos].getvPiece(pid);
        Arrays.stream(neighbourDirs)
                .filter(d -> knightMoveInDirFromPosStaysOnBoard(d, pos))    // be careful at the borders
                .forEach(d -> vPiece.addSingleNeighbour(boardSquares[pos + d].getvPiece(pid)));
    }

    public void removePiece(int pceID) {
        piecesOnBoard[pceID] = null;
        for (Square sq : boardSquares)
            sq.removePiece(pceID);
    }

    protected void updateBoardFromFEN(String fenString) {
        if (fenString == null || fenString.length() == 0)
            fenString = FENPOS_INITIAL;
        Move[] movesToDo = null;
        if (fenPosAndMoves != null
                && fenString.startsWith(fenPosAndMoves)) {
            movesToDo = getMoves(fenString.substring(fenPosAndMoves.length()));
        } else {
            // it seems the fenString is a later position of my current position
            movesToDo = initBoardFromFEN(fenString);
        }
        if (movesToDo != null) {
            for (int i = 0; i < movesToDo.length; i++) {
                completeCalc();
                if (!doMove(movesToDo[i])) {
                    System.err.println("Error in fenstring moves: invalid move " + movesToDo[i] + " on " + this.getBoardFEN() + "");
                    // try manually
                    basicMoveTo(movesToDo[i].from(), movesToDo[i].to());
                }
            }
        }
        if (!fenString.equalsIgnoreCase(fenPosAndMoves)) {
            System.err.println("Inconsistency in fen string: " + fenPosAndMoves
                    + " instead of " + fenString);
            // still we continue...
        }
        fenPosAndMoves = fenString;
        completeCalc();
    }

    /**
     * inits empty chessboard with pieces and parameters from a FEN string
     *
     * @param fenString FEN String according to Standard with board and game attributes
     * @return returns list of (still open) moves that were appended to the fen string
     */
    private Move[] initBoardFromFEN(String fenString) {
        fenPosAndMoves = fenString;
        setDefaultBoardState();
        emptyBoard();
        int figNr;
        int i = 0;
        int rank = 0;
        int file = 0;
        int pos = 0;
        while (i < fenString.length() && rank < 8) {
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
                case 'K', '♔' -> figNr = KING;
                case 'k', '♚' -> figNr = KING_BLACK;
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
            } else {
                //spawn nothing // figuresOnBoard[pos] = null;
                pos += emptyfields;
                file += emptyfields;
                emptyfields = 0;
                //while (--emptyfields>0)
                //figuresOnBoard[pos++]=null;
            }
            if (file > 8)
                System.err.println("**** Überlange Zeile gefunden beim Parsen an Position " + i + " des FEN-Strings " + fenString);
            // kann nicht vorkommen if (rank>8)
            //    System.err.println("**** Zu viele Zeilen gefunden beim Parsen an Position "+i+" des FEN-Strings "+fenString);
            i++;
        }
        // set board params from fen appendix
        // TODO: implementation is quite old, should use split etc...
        while (i < fenString.length() && fenString.charAt(i) == ' ')
            i++;
        if (i < fenString.length()) {
            if (fenString.charAt(i) == 'w' || fenString.charAt(i) == 'W')
                turn = WHITE;
            else if (fenString.charAt(i) == 'b' || fenString.charAt(i) == 'B')
                turn = BLACK;
            else
                System.err.println("**** Fehler beim Parsen der Spieler-Angabe an Position " + i + " des FEN-Strings " + fenString);
            i++;
            while (i < fenString.length() && fenString.charAt(i) == ' ')
                i++;
            // castle indicators
            int nextSeperator = i;
            while (nextSeperator < fenString.length() && fenString.charAt(nextSeperator) != ' ')
                nextSeperator++;
            blackQueensideCastleAllowed = fenString.substring(i, nextSeperator).contains("q");
            blackKingsideCastleAllowed = fenString.substring(i, nextSeperator).contains("k");
            whiteQueensideCastleAllowed = fenString.substring(i, nextSeperator).contains("Q");
            whiteKingsideCastleAllowed = fenString.substring(i, nextSeperator).contains("K");
            // enPassant
            i = nextSeperator;
            while (i < fenString.length() && fenString.charAt(i) == ' ')
                i++;
            nextSeperator = i;
            while (nextSeperator < fenString.length() && fenString.charAt(nextSeperator) != ' ')
                nextSeperator++;
            if (fenString.substring(i, nextSeperator).matches("[a-h]([1-8]?)"))
                enPassantFile = fenString.charAt(i) - 'a';
            else {
                enPassantFile = -1;
                if (fenString.charAt(i) != '-')
                    System.err.println("**** Fehler beim Parsen der enPassant-Spalte an Position " + i + " des FEN-Strings " + fenString);
            }
            // halfMoveClock
            i = nextSeperator;
            while (i < fenString.length() && fenString.charAt(i) == ' ')
                i++;
            nextSeperator = i;
            while (nextSeperator < fenString.length() && fenString.charAt(nextSeperator) != ' ')
                nextSeperator++;
            if (fenString.substring(i, nextSeperator).matches("[0-9]+"))
                countBoringMoves = Integer.parseInt(fenString.substring(i, nextSeperator));
            else {
                countBoringMoves = 0;
                System.err.println("**** Fehler beim Parsen der halfMoveClock an Position " + i + " des FEN-Strings " + fenString);
            }
            // nr of full moves
            i = nextSeperator;
            while (i < fenString.length() && fenString.charAt(i) == ' ')
                i++;
            nextSeperator = i;
            while (nextSeperator < fenString.length() && fenString.charAt(nextSeperator) != ' ')
                nextSeperator++;
            if (fenString.substring(i, nextSeperator).matches("[0-9]+"))
                fullMoves = Integer.parseInt(fenString.substring(i, nextSeperator));
            else {
                fullMoves = 1;
            }

            // parse+collect appending move strings
            i = nextSeperator;
            while (i < fenString.length() && fenString.charAt(i) == ' ')
                i++;
            Move[] moves = getMoves(fenString.substring(i));
            return moves;
        }
        // else no further board parameters available, stay with defaults
        return null;
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
        if (enPassantFile == -1)
            return -1;
        return fileRank2Pos(enPassantFile, isWhite(color) ? 5 : 2);
    }

    int countBoringMoves;
    int fullMoves;

    public int getCountBoringMoves() {
        return countBoringMoves;
    }

    public int getFullMoves() {
        return fullMoves;
    }

    public String getMove() {
        calcBestMove();
        Move m = getBestMove();
        if (m == null || !m.isMove())
            return "-";
        else
            return m.toString();
    }

    public Move getBestMove() {
        return bestMove;
    }

    public void calcBestMove() {
        final int lowest = (getTurnCol() ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE);
        int[] bestEvalSoFar = new int[MAX_INTERESTING_NROF_HOPS + 1];
        int[] bestOpponentEval = new int[MAX_INTERESTING_NROF_HOPS + 1];
        Arrays.fill(bestEvalSoFar, lowest);
        Arrays.fill(bestOpponentEval, -lowest);
        Arrays.fill(nrOfLegalMoves, 0);
        // positive chances to moves
        for (ChessPiece p : piecesOnBoard)
            if (p != null)
                p.addVPceMovesAndChances();
        // map chances of moves to lost or prolonged chances for the same piece other moves
        for (ChessPiece p : piecesOnBoard)
            if (p != null)
                p.mapLostChances();
        // first for opponent, then for "me"
        for (ChessPiece p : piecesOnBoard)
            if (p != null && p.color() != getTurnCol())
                nrOfLegalMoves[colorIndex(opponentColor(getTurnCol()))] += p.selectBestMove();
        for (ChessPiece p : piecesOnBoard)
            if (p != null && p.color() == getTurnCol())
                nrOfLegalMoves[colorIndex(getTurnCol())] += p.selectBestMove();

        // Compare all moves returned by all my pieces and find the best.
        Move bestMoveSoFar = getBestMoveFor(bestEvalSoFar, getTurnCol());
        Move bestOpponentMove = getBestMoveFor(bestOpponentEval, opponentColor(getTurnCol()));
        debugPrintln(DEBUGMSG_MOVESELECTION, "=> My best move: "+ bestMoveSoFar + "(" + bestEvalSoFar[0] +"), opponents best move: " + bestOpponentMove + "(" +bestOpponentEval[0]+").");

        if (bestOpponentMove!=null && bestMoveSoFar!=null
                && evalIsOkForColByMin(bestOpponentEval[0] + bestEvalSoFar[0],
                        opponentColor(getTurnCol()), (positivePieceBaseValue(PAWN) >>2))
                && !moveIsHinderingMove(bestMoveSoFar, bestOpponentMove)
        ) {
            int[] alternativeEval = new int[MAX_INTERESTING_NROF_HOPS + 1];
            Arrays.fill(alternativeEval, lowest);
            Move alternativeMove = getBestMoveForAvoiding(alternativeEval, getTurnCol(), bestOpponentMove);
            if (alternativeMove!=null) {
                debugPrintln(DEBUGMSG_MOVESELECTION, "Choosing alternative "+alternativeMove+" to avoid " + bestOpponentMove + " after " + bestMoveSoFar + ".");
                bestMoveSoFar = alternativeMove;
                bestEvalSoFar = alternativeEval;
            }
            else {
                debugPrintln(DEBUGMSG_MOVESELECTION, "Not choosing an alternative to avoid " + bestOpponentMove + " after " + bestMoveSoFar + ".");

            }
        }

        bestMove = bestMoveSoFar;
        checkAndEvaluateGameOver();
    }

    private Move getBestMoveForAvoiding(int[] bestEvalSoFar, boolean col, Move bestOpponentMove) {
        Move bestMoveSoFar = null;
        int bestMovesCorrective = 0;
        for (ChessPiece p : piecesOnBoard) {
            if (p != null && p.color() == col) {
                //HashMap<Move, int[]> legalMovesAndChances = p.getLegalMovesAndChances();
                //if (legalMovesAndChances!=null) {
                // collect moves and their chances from all vPces
                debugPrintln(DEBUGMSG_MOVESELECTION, "---- checking " + p + " with stayEval=" + p.staysEval() + ": ");
                int threshold = (col ? pieceBaseValue(PAWN)
                        : pieceBaseValue(PAWN_BLACK));
                    /*
                    for (Map.Entry<Move, int[]> e : legalMovesAndChances.entrySet()) {
                        int[] pMoveEvals = e.getValue();
                        Move pMove = e.getKey(); */
                int[] pMoveEvals = p.getBestMoveEval();
                Move pMove = p.getBestMoveSoFar();
                if (pMove != null && (bestOpponentMove==null
                                        || moveIsHinderingMove(pMove, bestOpponentMove))
                ) {
                    nrOfLegalMoves[colorIndex(col)]++;
                    ChessPiece beatenPiece = board.getPieceAt(pMove.to());
                    int corrective = -p.staysEval()   // eval of moving - eval of staying
                            - ((beatenPiece != null && beatenPiece.canMove())
                            ? (beatenPiece.getBestMoveRelEval() - getBoardSquares()[beatenPiece.getPos()].getvPiece(p.getPieceID()).getClashContrib()) // the best move except its contribution that is already calculated in the stayEval...
                            : 0);  // eliminated effect of beaten Piece
                    int i = 0;
                    debugPrintln(DEBUGMSG_MOVESELECTION, "  Move " + pMove + " " + Arrays.toString(pMoveEvals) + "+" + corrective);
                    while (i < bestEvalSoFar.length) {
                        int moveRelEval = pMoveEvals[i] + corrective;
                        if (isWhite(col) ? moveRelEval > bestEvalSoFar[i] + threshold
                                : moveRelEval < bestEvalSoFar[i] + threshold) {
                            for (int j = 0; j < bestEvalSoFar.length; j++)
                                bestEvalSoFar[j] = pMoveEvals[j] + corrective;
                            bestMoveSoFar = pMove;
                            bestMovesCorrective = (corrective);
                            debugPrintln(DEBUGMSG_MOVESELECTION, "!=" + i + " " + Arrays.toString(bestEvalSoFar));
                            break;
                        }
                        if (isWhite(col) ? moveRelEval > bestEvalSoFar[i] + (threshold >> 2)
                                : moveRelEval < bestEvalSoFar[i] + (threshold >> 2)) {
                            for (int j = 0; j < bestEvalSoFar.length; j++)
                                bestEvalSoFar[j] = pMoveEvals[j] + corrective;
                            threshold <<= 2;
                            bestMoveSoFar = pMove;
                            debugPrintln(DEBUGMSG_MOVESELECTION, "?:" + i + " " + Arrays.toString(bestEvalSoFar));
                            i++;
                        } else if (isWhite(col) ? moveRelEval > bestEvalSoFar[i] - EVAL_TENTH
                                : moveRelEval < bestEvalSoFar[i] + EVAL_TENTH) {
                            i++;  // same evals on the future levels so far, so continue comparing
                        } else
                            break;
                    }
                }
                //}
            }
        }
        for (int j = 0; j < bestEvalSoFar.length; j++)
            bestEvalSoFar[j] = bestEvalSoFar[j] - bestMovesCorrective;
        return bestMoveSoFar;
    }

    private Move getBestMoveFor ( int[] bestEvalSoFar, boolean col) {
        return getBestMoveForAvoiding(bestEvalSoFar, col, null);
    }

    boolean doMove (Move m) {
        return doMove(m.from(), m.to(), m.promotesTo());
    }

    boolean doMove ( int frompos, int topos, int promoteToPceType){
        // sanity/range checks for move
        if (frompos < 0 || topos < 0
                || frompos >= NR_SQUARES || topos >= NR_SQUARES) { // || figuresOnBoard[frompos].getColor()!=turn  ) {
            internalErrorPrintln(String.format("Fehlerhafter Zug: %s%s ist außerhalb des Boards %s.\n", squareName(frompos), squareName(topos), getBoardFEN()));
            return false;
        }
        final int pceID = getPieceIdAt(frompos);
        final int pceType = getPieceTypeAt(frompos);
        if (pceID == NO_PIECE_ID) { // || figuresOnBoard[frompos].getColor()!=turn  ) {
            internalErrorPrintln(String.format("Fehlerhafter Zug: auf %s steht keine Figur auf Board %s.\n", squareName(frompos), getBoardFEN()));
            return false;
        }
        if (boardSquares[topos].getUnconditionalDistanceToPieceIdIfShortest(pceID) != 1
                && colorlessPieceType(pceType) != KING) { // || figuresOnBoard[frompos].getColor()!=turn  ) {
            // TODO: check king for allowed moves... excluded here, because castelling is not obeyed in distance calculation, yet.
            internalErrorPrintln(String.format("Fehlerhafter Zug: %s -> %s nicht möglich auf Board %s.\n", squareName(frompos), squareName(topos), getBoardFEN()));
            //TODO!!: this allow illegal moves, but for now overcomes the bug to not allow enpassant beating by the opponent...
            if (!(colorlessPieceType(pceType) == PAWN && fileOf(topos) == enPassantFile))
                return false;
        }
        final int toposPceID = getPieceIdAt(topos);
        final int toposType = getPieceTypeAt(topos);

        // take piece
        if (toposPceID != NO_PIECE_ID) {
            takePieceAway(topos);
        /*old code to update pawn-evel-parameters
        if (takenFigNr==NR_PAWN && toRow==getWhitePawnRowAtCol(toCol))
            refindWhitePawnRowAtColBelow(toCol,toRow+1);  // try to find other pawn in column where the pawn was beaten
        else if (takenFigNr==NR_PAWN_BLACK && toRow==getBlackPawnRowAtCol(toCol))
            refindBlackPawnRowAtColBelow(toCol,toRow-1);*/
        }
        if (colorlessPieceType(pceType) == PAWN || toposPceID != NO_PIECE_ID)
            countBoringMoves = 0;
        else
            countBoringMoves++;

        // en-passant
        // is possible to occur in two notations to left/right (then taken pawn has already been treated above) ...
        if (pceType == PAWN
                && rankOf(frompos) == 4 && rankOf(topos) == 4
                && fileOf(topos) == enPassantFile) {
            topos += UP;
            //setFurtherWhitePawnRowAtCol(toCol, toRow);     // if the moved pawn was the furthest, remember new position, otherwise the function will leave it
        } else if (pceType == PAWN_BLACK
                && rankOf(frompos) == 3 && rankOf(topos) == 3
                && fileOf(topos) == enPassantFile) {
            topos += DOWN;
            //setFurtherBlackPawnRowAtCol(toCol, toRow);     // if the moved pawn was the furthest, remember new position, otherwise the function will leave it
        }
        // ... or diagonally als normal
        else if (pceType == PAWN
                && rankOf(frompos) == 4 && rankOf(topos) == 5
                && fileOf(topos) == enPassantFile) {
            takePieceAway(topos + DOWN);
        /*setFurtherWhitePawnRowAtCol(toCol, toRow);     // if the moved pawn was the furthest, remember new position, otherwise the function will leave it
        if (toRow+1==getBlackPawnRowAtCol(toCol))
            setFurtherBlackPawnRowAtCol(toCol, NO_BLACK_PAWN_IN_COL);*/
        } else if (pceType == PAWN_BLACK
                && rankOf(frompos) == 3 && rankOf(topos) == 2
                && fileOf(topos) == enPassantFile) {
            takePieceAway(topos + UP);
        /*setFurtherBlackPawnRowAtCol(toCol, toRow);     // if the moved pawn was the furthest, remember new position, otherwise the function will leave it
        if (toRow-1==getWhitePawnRowAtCol(toCol))
            setFurtherWhitePawnRowAtCol(toCol, NO_WHITE_PAWN_IN_COL);*/
        }

        //boardMoves.append(" ").append(squareName(frompos)).append(squareName(topos));

        // check if this is a 2-square pawn move ->  then enPassent is possible for opponent at next move
        if (pceType == PAWN && rankOf(frompos) == 1 && rankOf(topos) == 3
                || pceType == PAWN_BLACK && rankOf(frompos) == 6 && rankOf(topos) == 4)
            enPassantFile = fileOf(topos);
        else
            enPassantFile = -1;

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

        // move
        basicMoveTo(pceType, pceID, frompos, topos);

        // promote to
        if (promoteToPceType > 0 && colorlessPieceType(promoteToPceType) != PAWN) {
            if (pceType == PAWN && isLastRank(topos)) {
                takePieceAway(topos);
                spawnPieceAt(promoteToPceType, topos);
                completeCalc();
            } else if (pceType == PAWN_BLACK && isFirstRank(topos)) {
                takePieceAway(topos);
                spawnPieceAt(isPieceTypeWhite(promoteToPceType) ? promoteToPceType + BLACK_PIECE : promoteToPceType, topos);
                completeCalc();
            } else {
                promoteToPceType = 0;
            }
        } else {
            promoteToPceType = 0;
        }

        turn = !turn;
        if (isWhite(turn))
            fullMoves++;

        //not here: calcBestMove();

        // in debug mode compare with freshly created board from same fenString
        if (DEBUG_BOARD_COMPARE_FRESHBOARD)
            this.equals(new ChessBoard("CmpBoard", this.getBoardFEN()));

        fenPosAndMoves += " " + squareName(frompos) + squareName(topos)
                + (promoteToPceType > 0 ? (fenCharFromPceType(promoteToPceType | BLACK_PIECE)) : "");

        return true;
    }


    public boolean doMove (@NotNull String move){
        int startpos = 0;
        // skip spaces
        while (startpos < move.length() && move.charAt(startpos) == ' ')
            startpos++;
        move = move.substring(startpos);
        if (isWhite(turn)) {
            debugPrint(DEBUGMSG_BOARD_MOVES, " " + getFullMoves() + ".");
        }
        debugPrint(DEBUGMSG_BOARD_MOVES, " " + move);
        // an empty move string is not a legal move
        if (move.isEmpty())
            return false;

        Move m = new Move(move);
        if (m.isMove()) {
            // primitive move string wa successfully interpreted
        } else if (move.charAt(0) >= 'a' && move.charAt(0) < ('a' + NR_RANKS)) {
            int promcharpos;
            if (move.charAt(1) == 'x') {
                // a pawn beats something
                if (move.length() == 3) {
                    // very short form like "cxd" is not supported, yet
                    return false;
                }
                // a pawn beats something, like "hxg4"
                m.setTo(coordinateString2Pos(move, 2));
                m.setFrom(fileRank2Pos(move.charAt(0) - 'a', rankOf(m.to()) + (isWhite(getTurnCol()) ? -1 : +1)));
                promcharpos = 4;
            } else {
                // simple pawn move, like "d4"
                m.setTo(coordinateString2Pos(move, 0));
                m.setFrom(m.to() + (isWhite(getTurnCol()) ? +NR_FILES : -NR_FILES));  // normally it should come from one square below
                if (isWhite(getTurnCol()) && rankOf(m.to()) == 3) {
                    // check if it was a 2-square move...
                    if (getPieceTypeAt(m.from()) == EMPTY)
                        m.setFrom(m.from() + NR_FILES);   // yes, it must be even one further down
                } else if (isBlack(getTurnCol()) && rankOf(m.to()) == NR_RANKS - 4) {
                    // check if it was a 2-square move...
                    if (getPieceTypeAt(m.from()) == EMPTY)
                        m.setFrom(m.from() - NR_FILES);   // yes, it must be even one further down
                }
                promcharpos = 2;
            }
            // promotion character indicates what a pawn should be promoted to
            if ((isBlack(getTurnCol()) && isFirstRank(m.to())
                    || isWhite(getTurnCol()) && isLastRank(m.to()))) {
                char promoteToChar = move.length() > promcharpos ? move.charAt(promcharpos) : 'q';
                if (promoteToChar == '=') // some notations use a1=Q isntead of a1Q
                    promoteToChar = move.length() > promcharpos + 1 ? move.charAt(promcharpos + 1) : 'q';
                m.setPromotesTo(getPceTypeFromPromoteChar(promoteToChar));
            }
        } else if (move.length() >= 3 && move.charAt(1) == '-' &&
                (move.charAt(0) == '0' && move.charAt(2) == '0'
                        || move.charAt(0) == 'O' && move.charAt(2) == 'O'
                        || move.charAt(0) == 'o' && move.charAt(2) == 'o')) {
            // castelling
            if (isWhite(getTurnCol()))
                m.setFrom(A1SQUARE + 4);
            else   // black
                m.setFrom(4);
            if (move.length() >= 5 && move.charAt(3) == '-' && move.charAt(4) == move.charAt(0))
                m.setTo(m.from() - 2);  // long castelling
            else
                m.setTo(m.from() + 2);  // short castelling
        } else {
            // must be a normal, non-pawn move
            int movingPceType = pceTypeFromPieceSymbol(move.charAt(0));
            if (movingPceType == EMPTY)
                internalErrorPrintln(format(chessBasicRes.getString("errorMessage.moveParsingError") + " <{0}>", move.charAt(0)));
            if (isBlack(getTurnCol()))
                movingPceType += BLACK_PIECE;
            int fromFile = -1;
            int fromRank = -1;
            if (isFileChar(move.charAt(2))) {
                // the topos starts only one character later, so there must be an intermediate information
                if (move.charAt(1) == 'x') {   // its beating something - actually we do not care if this is true...
                } else if (isFileChar(move.charAt(1)))  // a starting file
                    fromFile = move.charAt(1) - 'a';
                else if (isRankChar(move.charAt(1)))  // a starting rank
                    fromRank = move.charAt(1) - '1';
                m.setTo(coordinateString2Pos(move, 2));
            } else if (move.charAt(2) == 'x') {
                // a starting file or rank + a beating x..., like "Rfxf2"
                if (isFileChar(move.charAt(1)))      // a starting file
                    fromFile = move.charAt(1) - 'a';
                else if (isRankChar(move.charAt(1))) // a starting rank
                    fromRank = move.charAt(1) - '1';
                m.setTo(coordinateString2Pos(move, 3));
            } else {
                m.setTo(coordinateString2Pos(move, 1));
            }
            // now the only difficulty is to find the piece and its starting position...
            m.setFrom(-1);
            for (ChessPiece p : piecesOnBoard) {
                // check if this piece matches the type and can move there in one hop.
                // TODO!!: it can still take wrong piece that is pinned to its king...
                if (p != null && movingPceType == p.getPieceType()                                    // found Piece p that matches the wanted type
                        && (fromFile == -1 || fileOf(p.getPos()) == fromFile)       // no extra file is specified or it is correct
                        && (fromRank == -1 || rankOf(p.getPos()) == fromRank)       // same for rank
                        && boardSquares[m.to()].getDistanceToPieceId(p.getPieceID()) == 1   // p can move here directly (distance==1)
                        && moveIsNotBlockedByKingPin(p, m.to())                                         // p is not king-pinned or it is pinned but does not move out of the way.
                ) {
                    m.setFrom(p.getPos());
                    break;
                }
            }
            if (!m.isMove())
                return false;  // no matching piece found
        }
        debugPrint(DEBUGMSG_BOARD_MOVES, "(" + squareName(m.from()) + squareName(m.to()) + ")");
        return doMove(m);  //frompos, topos, promoteToFigNr);
    }

    /**
     * p is not king-pinned or it is pinned but does not move out of the way.
     */
    public boolean moveIsNotBlockedByKingPin (ChessPiece p,int topos){
        if (isKing(p.getPieceType()))
            return true;
        int sameColorKingPos = p.isWhite() ? whiteKingPos : blackKingPos;
        if (sameColorKingPos < 0)
            return true;  // king does not exist... should not happen, but is part of some test-positions
        if (!isPiecePinnedToPos(p, sameColorKingPos))
            return true;   // p is not king-pinned
        if (colorlessPieceType(p.getPieceType()) == KNIGHT)
            return false;  // a king-pinned knight can never move away in a way that it still avoids the chess
        // or it is pinned, but does not move out of the way.
        int king2PceDir = calcDirFromTo(sameColorKingPos, topos);
        int king2TargetDir = calcDirFromTo(sameColorKingPos, p.getPos());
        return king2PceDir == king2TargetDir;
        // TODO?:  could also be solved by more intelligent condition stored in the distance to the king
    }


    public boolean isPiecePinnedToPos (ChessPiece p,int pos){
        int pPos = p.getPos();
        List<Integer> listOfSquarePositionsCoveringPos = boardSquares[pos].getPositionsOfPiecesThatBlockWayAndAreOfColor(p.color());
        for (Integer covpos : listOfSquarePositionsCoveringPos)
            if (covpos == pPos)
                return true;
        return false;
    }

    public boolean posIsBlockingCheck ( boolean col, int to){
        return boardSquares[to].blocksCheckFor(col);
    }

    public int nrOfLegalMovesForPieceOnPos ( int pos){
        ChessPiece sqPce = boardSquares[pos].myPiece();
        if (sqPce == null)
            return -1;
        return sqPce.getLegalMovesAndChances().size();
    }


    int getPieceTypeAt ( int pos){
        int pceID = boardSquares[pos].getPieceID();
        if (pceID == NO_PIECE_ID || piecesOnBoard[pceID] == null)
            return EMPTY;
        return piecesOnBoard[pceID].getPieceType();
    }

    private void takePieceAway ( int topos){
        //decreasePieceNrCounter(takenFigNr);
        //updateHash(takenFigNr, topos);
        ChessPiece p = getPieceAt(topos);
        p.startNextUpdate();
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

    private void basicMoveTo ( final int pceType, final int pceID, final int frompos, final int topos){
        //updateHash(..., frompos);
        //updateHash(..., topos);
        if (pceType == KING)
            whiteKingPos = topos;
        else if (pceType == KING_BLACK)
            blackKingPos = topos;
        // re-place piece on board
        emptySquare(frompos);
        piecesOnBoard[pceID].setPos(topos);
        // tell the square
        setCurrentDistanceCalcLimit(0);
        boardSquares[topos].movePieceHereFrom(pceID, frompos);
        // tell all Pieces to update their vPieces (to recalc the distances)
        ChessPiece mover = piecesOnBoard[pceID];
        mover.updateDueToPceMove(frompos, topos);
        //continueDistanceCalcUpTo(1);  // to do at lease one round of recalc of relEvals an NoGos
        for (ChessPiece chessPiece : piecesOnBoard)
            if (chessPiece != null && chessPiece != mover)
                chessPiece.updateDueToPceMove(frompos, topos);
        // for Test: "deactivation of recalc eval in doMove-methods in ChessBoard
        //           for manual tests with full Board reconstruction of every position, instead of evolving evaluations per move (just to compare speed)"
        // deactivate the following (correct) code:
        completeCalc();
    /* setCurrentDistanceCalcLimit(0);
    boardSquares[frompos].pieceHasMovedAway();
    completeDistanceCalc();
     */

    }

    public boolean isSquareEmpty ( final int pos){
        return (boardSquares[pos].getPieceID() == NO_PIECE_ID);
    }

    private void emptySquare ( final int frompos){
        boardSquares[frompos].emptySquare();
    }

    private void basicMoveTo ( final int frompos, final int topos){
        int pceID = getPieceIdAt(frompos);
        int pceType = getPieceTypeAt(frompos);
        basicMoveTo(pceType, pceID, frompos, topos);
    }

    public String getPieceFullName ( int pceId){
        return getPiece(pceId).toString();
    }

    public int getDistanceToPosFromPieceId ( int pos, int pceId){
        return boardSquares[pos].getDistanceToPieceId(pceId);
    }

    public boolean isDistanceToPosFromPieceIdUnconditional ( int pos, int pceId){
        return boardSquares[pos].getConditionalDistanceToPieceId(pceId).isUnconditional();
    }

    public boolean isWayToPosFromPieceIdNoGo ( int pos, int pceId){
        return boardSquares[pos].getConditionalDistanceToPieceId(pceId).hasNoGo();
    }

    ConditionalDistance getDistanceFromPieceId ( int pos, int pceId){
        return boardSquares[pos].getConditionalDistanceToPieceId(pceId);
    }

    public String getGameState () {
        checkAndEvaluateGameOver();
        if (isGameOver()) {
            if (isCheck(WHITE))
                return chessBasicRes.getString("state.blackWins");
            if (isCheck(BLACK))
                return chessBasicRes.getString("state.whiteWins");
            return chessBasicRes.getString("state.remis");
            //TODO: check and return stalemate
        }
        if (getFullMoves() == 0)
            return chessBasicRes.getString("state.notStarted");
        return chessBasicRes.getString("state.ongoing");
    }

    public Iterator<ChessPiece> getPiecesIterator () {
        return Arrays.stream(piecesOnBoard).iterator();
    }

    // virtual non-linear, but continuously increasing "clock" used to remember update-"time"s and check if information is outdated
    private long updateClockFineTicks = 0;

    public int getNrOfPlys () {
        if (isWhite(turn))
            return fullMoves * 2;
        return fullMoves * 2 + 1;
    }

    public long getUpdateClock () {
        return getNrOfPlys() * 10000L + updateClockFineTicks;
    }

    public long nextUpdateClockTick () {
        ++updateClockFineTicks;
        return getUpdateClock();
    }

    public static void internalErrorPrintln (String s){
        System.err.println(chessBasicRes.getString("errormessage.errorPrefix") + s);
    }


    public static void debugPrint ( boolean doPrint, String s){
        if (doPrint)
            System.err.print(s);
    }

    public static void debugPrintln ( boolean doPrint, String s){
        if (doPrint)
            System.err.println(s);
    }


    public int currentDistanceCalcLimit () {
        return currentDistanceCalcLimit;
    }

    private void setCurrentDistanceCalcLimit ( int newLimit){
        currentDistanceCalcLimit = min(MAX_INTERESTING_NROF_HOPS, newLimit);
    }


    @Override
    public boolean equals (Object o){
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChessBoard other = (ChessBoard) o;
        debugPrint(DEBUGMSG_BOARD_COMPARE_FRESHBOARD, "Comparing Boards: " + this.getBoardName() + " with " + other.getBoardName() + ":  ");

        boolean equal = compareWithDebugMessage("White King Pos", whiteKingPos, other.whiteKingPos);
        equal &= compareWithDebugMessage("Black King Pos", blackKingPos, other.blackKingPos);
        equal &= compareWithDebugMessage("White King Checks", nrOfChecks(WHITE), other.nrOfChecks(WHITE));
        equal &= compareWithDebugMessage("Black King Checks", nrOfChecks(BLACK), other.nrOfChecks(BLACK));
        equal &= compareWithDebugMessage("Count White Pieces", countOfWhitePieces, other.countOfWhitePieces);
        equal &= compareWithDebugMessage("Count Black Pieces", countOfBlackPieces, other.countOfBlackPieces);
        equal &= compareWithDebugMessage("Game Over", gameOver, other.gameOver);
        equal &= compareWithDebugMessage("Turn", turn, other.turn);
        equal &= compareWithDebugMessage("White Kingside Castling Allowed", whiteKingsideCastleAllowed, other.whiteKingsideCastleAllowed);
        equal &= compareWithDebugMessage("White Queenside Castling Allowed", whiteQueensideCastleAllowed, other.whiteQueensideCastleAllowed);
        equal &= compareWithDebugMessage("Black Kingside Castling Allowed", blackKingsideCastleAllowed, other.blackKingsideCastleAllowed);
        equal &= compareWithDebugMessage("Black Queenside Castling Allowed", blackQueensideCastleAllowed, other.blackQueensideCastleAllowed);
        equal &= compareWithDebugMessage("EnPassant File", enPassantFile, other.enPassantFile);
        equal &= compareWithDebugMessage("Boring Moves", countBoringMoves, other.countBoringMoves);
        equal &= compareWithDebugMessage("Full Moves", fullMoves, other.fullMoves);
        for (int pos = 0; pos < NR_SQUARES; pos++) {
            int pceId = boardSquares[pos].getPieceID();
            if (pceId != NO_PIECE_ID) {
                // piece found, get id of same piece on other board
                int otherPceId = other.boardSquares[pos].getPieceID();
                // compare all vPieces with this PceID on all squares
                for (int vpos = 0; vpos < NR_SQUARES; vpos++) {
                    VirtualPieceOnSquare thisVPce = boardSquares[vpos].getvPiece(pceId);
                    VirtualPieceOnSquare otherVPce = other.boardSquares[vpos].getvPiece(otherPceId);
                    equal &= thisVPce.equals(otherVPce);
                    //equal &= compareWithDebugMessage(thisVPce+" myPCeId", thisVPce.myPceID, otherVPce.myPceID );
                }
            }
        }
        if (equal)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD, " --> ok");
        else
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL, " --> Problem on Board " + this.getBoardFEN());
        return equal;
    }

    static boolean compareWithDebugMessage (String debugMesg,int thisInt, int otherInt){
        boolean cmp = (thisInt == otherInt);
        if (!cmp)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL, debugMesg + ": " + thisInt + " != " + otherInt);
        return cmp;
    }

    static boolean compareWithDebugMessage (String debugMesg,boolean thisBoolean, boolean otherBoolean){
        boolean cmp = (thisBoolean == otherBoolean);
        if (!cmp)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL, debugMesg + ": " + thisBoolean + " != " + otherBoolean);
        return cmp;
    }

    static boolean compareWithDebugMessage (String debugMesg,
            ConditionalDistance thisDistance,
            ConditionalDistance otherDistance){
        boolean cmp = (thisDistance.dist() == otherDistance.dist()
                || thisDistance.dist() >= MAX_INTERESTING_NROF_HOPS && otherDistance.dist() >= MAX_INTERESTING_NROF_HOPS);
        if (!cmp)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL,
                    debugMesg + ".dist: " + thisDistance
                            + " != " + otherDistance);
        boolean cmp2 = (thisDistance.hasNoGo() == otherDistance.hasNoGo());
        if (!cmp2)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL,
                    debugMesg + ".nogo: " + thisDistance
                            + " != " + otherDistance);
        return cmp && cmp2;
    }

    static boolean compareWithDebugMessage (String debugMesg,int[] thisIntArray, int[] otherIntArray){
        boolean cmp = Arrays.equals(thisIntArray, otherIntArray);
        if (!cmp)
            debugPrintln(DEBUGMSG_BOARD_COMPARE_FRESHBOARD_NONEQUAL, debugMesg + ": " + Arrays.toString(thisIntArray) + " != " + Arrays.toString(otherIntArray));
        return cmp;
    }

    static boolean compareWithDebugMessage (String debugMesg,
                ConditionalDistance[]thisDistanceArray,
                ConditionalDistance[]otherDistanceArray){
        boolean cmp = true;
        for (int i = 0; i < thisDistanceArray.length; i++)
            cmp &= compareWithDebugMessage(debugMesg + "[" + i + "]",
                    thisDistanceArray[i],
                    otherDistanceArray[i]);
        return cmp;
    }

    //// getter

    public int getPieceCounter () {
        return countOfWhitePieces + countOfBlackPieces;
    }

    public int getPieceCounterForColor ( boolean whitecol){
        return whitecol ? countOfWhitePieces
                : countOfBlackPieces;
    }

    public boolean isGameOver () {
        return gameOver;
    }

    public int nrOfLegalMoves ( boolean col){
        return nrOfLegalMoves[colorIndex(col)];
    }

    public int getKingPos ( boolean col){
        return isWhite(col) ? whiteKingPos : blackKingPos;
    }

    public static int getMAX_INTERESTING_NROF_HOPS () {
        return MAX_INTERESTING_NROF_HOPS;
    }

    public boolean getTurnCol () {
        return turn;
    }

    StringBuffer getBoardName () {
        return boardName;
    }

    StringBuffer getShortBoardName () {
        return boardName;
    }

    public Square[] getBoardSquares () {
        return boardSquares;
    }

    /*Stream<Square> getBoardSquaresStream() {
        return Arrays.stream(boardSquares);
    }*/

    //// setter

    public static void setMAX_INTERESTING_NROF_HOPS ( int RECONST_MAX_INTERESTING_NROF_HOPS){
        MAX_INTERESTING_NROF_HOPS = RECONST_MAX_INTERESTING_NROF_HOPS;
    }

    //void setTurn(boolean turn);


    private boolean moveIsHinderingMove(Move m, Move m2bBlocked) {
        if (m.to()==m2bBlocked.from())
            return true;
        if (m.from()==m2bBlocked.to())
            return true;
        if (isBetweenFromAndTo(m.to(), m2bBlocked.from(), m2bBlocked.to()))
        return true;
        return false;
    }

}