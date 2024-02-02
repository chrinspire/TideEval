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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Math.*;

public class ChessBasics {

    public static final ResourceBundle chessBasicRes = ResourceBundle.getBundle("de.ensel.chessTexts");

    ////// B/W
    public static final boolean WHITE = true;
    public static final boolean BLACK = false;
    public static final int CIWHITE = 0;
    public static final int CIBLACK = 1;
    public static final int ANYWHERE = -1;
    public static final int[] CASTLING_KINGSIDE_KINGTARGET = new int[2];
    public static final int[] CASTLING_KINGSIDE_ROOKTARGET = new int[2];
    public static final int[] CASTLING_QUEENSIDE_KINGTARGET = new int[2];
    public static final int[] CASTLING_QUEENSIDE_ROOKTARGET = new int[2];
    static {
        CASTLING_KINGSIDE_KINGTARGET[CIWHITE]  = coordinateString2Pos("g1");
        CASTLING_KINGSIDE_ROOKTARGET[CIWHITE]  = coordinateString2Pos("f1");
        CASTLING_QUEENSIDE_KINGTARGET[CIWHITE] = coordinateString2Pos("c1");
        CASTLING_QUEENSIDE_ROOKTARGET[CIWHITE] = coordinateString2Pos("d1");
        CASTLING_KINGSIDE_KINGTARGET[CIBLACK]  = coordinateString2Pos("g8");
        CASTLING_KINGSIDE_ROOKTARGET[CIBLACK]  = coordinateString2Pos("f8");
        CASTLING_QUEENSIDE_KINGTARGET[CIBLACK] = coordinateString2Pos("c8");
        CASTLING_QUEENSIDE_ROOKTARGET[CIBLACK] = coordinateString2Pos("d8");
    }

    public static boolean isWhite(boolean col) {
        return col;  // actually correct is: col==WHITE;
    }
    public static boolean isBlack(boolean col) {
        return col==BLACK;
    }
    public static boolean isWhite(int colIndex) {
        return colIndex==CIWHITE;
    }
    public static boolean colorFromColorIndex(int colIndex) {
        return colIndex==CIWHITE;
    }
    public static int colorIndex(boolean col) {
        return col ? CIWHITE : CIBLACK;
    }
    public static boolean opponentColor(boolean col) {
        return !col;
    }
    public static int opponentColorIndex(int colIndex) {
        return colIndex ^= 1;  // XOR
    }
    public static final String colorNameWhite= chessBasicRes.getString("colorname_white");
    public static final String colorNameBlack= chessBasicRes.getString("colorname_black");
    public static String colorName(boolean color) {
        return color ? colorNameWhite : colorNameBlack;
    }
    public static String colorName(int colorIndex) {
        switch (colorIndex) {
            case -1:
                return "";
            case 0:
                return colorNameWhite;
            case 1:
                return colorNameBlack;
            default:
                return "error";
        }
    }

    // ******* CONSTs for Evaluation

    // eval is not set
    public static final int NOT_EVALUATED = Integer.MIN_VALUE + 25000;  // value was chosen out of reach of MIN_VALUE+high evaluation

    // absolute evaluation in centipawns with pro-white = pos,  pro-black=neg
    public static final int WHITE_IS_CHECKMATE = -99999;
    public static final int BLACK_IS_CHECKMATE = 99999;

    /**
     * checkmateEval() returns the posEval for a checkmate against given color
     * @param color boolean ChessBasics color
     * @return evaluation, either WHITE_IS_CHECKMATE or black...
     */
    public static int checkmateEval(boolean color) {
        return isWhite(color) ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE;
    }

    public static boolean isCheckmateEvalFor(int eval, boolean color) {
        return isWhite(color) ?  eval < WHITE_IS_CHECKMATE + CHECK_IN_N_DELTA*20
                              :  eval > BLACK_IS_CHECKMATE - CHECK_IN_N_DELTA*20;
    }

    // relative evaluation in centipawns with pro-my-color = pos,  pro-opponent=neg
    public static final int OPPONENT_IS_CHECKMATE = 111111;
    public static final int IM_CHECKMATE = -111111;
    public static final int CHECK_IN_N_DELTA=10;
    //static final int CHECKMATE=BLACK_IS_CHECKMATE-(CHECK_IN_N_DELTA<<4)-1;

    // *******  CONSTs concerning rules

    // Nr of squares - careful, cannot be simply changed, some parts of code rely on this for performance reasons. Esp. the file,rank->pos calculation.
    public static final int NR_SQUARES = 64;
    public static final int NR_RANKS = 8;  // 1-8
    public static final int NR_FILES = 8;  // a-h
    public static final int MAX_PIECES = 32+16+16;  //32 at the beginning + 2x16 promoted pawns.
    public static final int A1SQUARE = NR_SQUARES-NR_FILES;
    // max nr of moves without pawn move or taking a piece
    public static final int MAX_BORING_MOVES = 50;     // should be: 50;
    // starting position
    public static final String FENPOS_STARTPOS = chessBasicRes.getString("fen.stdChessStartingPosition");
    public static final String FENPOS_EMPTY = chessBasicRes.getString("fen.emptyChessBoard");

    // *******  about PIECES

    //general or WHITE piece types
    public static final int EMPTY = 0;
    public static final int KING  = 1;
    public static final int ROOK  = 2;
    public static final int BISHOP= 4;
    public static final int QUEEN = ROOK | BISHOP;
    public static final int KNIGHT= 8;
    public static final int PAWN  = 16;
    /*public static final int OWN_PIECE = 7;
    public static final int OPPONENT_PIECE = -7;
    static final int NR_ROOK_BEHIND_QUEEN = 8;
    static final int NR_BISHOP_BEHIND_QUEEN = 9;*/
    //BLACK piece types
    private static final int WHITE_FILTER = (PAWN<<1)-1;
    public static final int BLACK_PIECE = (PAWN<<1);
    public static final int KING_BLACK  = BLACK_PIECE + KING;
    public static final int QUEEN_BLACK = BLACK_PIECE + QUEEN;
    public static final int ROOK_BLACK  = BLACK_PIECE + ROOK;
    public static final int BISHOP_BLACK= BLACK_PIECE + BISHOP;
    public static final int KNIGHT_BLACK= BLACK_PIECE + KNIGHT;
    public static final int PAWN_BLACK  = BLACK_PIECE + PAWN;

    public static int positivePieceBaseValue(int pceTypeNr) {
        return pieceBaseValue(colorlessPieceType(pceTypeNr));
    }

    public static final int SINGLEBISHOP_BASEVALUE = 300;

    public static int pieceBaseValue(int pceTypeNr) {
        return switch (pceTypeNr) {
                case EMPTY ->        0;
                case KING ->        1200;
                case QUEEN ->        940;
                case ROOK ->         530;
                case BISHOP ->       SINGLEBISHOP_BASEVALUE + (EVAL_TENTH<<1);
                case KNIGHT ->       290;
                case PAWN ->         108;
                case KING_BLACK -> -1200;
                case QUEEN_BLACK -> -940;
                case ROOK_BLACK ->  -530;
                case BISHOP_BLACK-> -(SINGLEBISHOP_BASEVALUE + (EVAL_TENTH<<1));
                case KNIGHT_BLACK-> -290;
                case PAWN_BLACK ->  -108;
                default -> 0;
            };
    }

    public static int singleLightPieceBaseValue(int pceTypeNr) {
        return switch (pceTypeNr) {
            case BISHOP ->       SINGLEBISHOP_BASEVALUE;
            case KNIGHT ->       SINGLEBISHOP_BASEVALUE; // like bishop
            case BISHOP_BLACK-> -SINGLEBISHOP_BASEVALUE;
            case KNIGHT_BLACK-> -SINGLEBISHOP_BASEVALUE;
            default -> 0;
        };
    }


    public static int reversePieceBaseValue(int pceTypeNr) {
        switch (pceTypeNr) {
            case EMPTY -> {
                return 0;
            }
            case KING -> {
                return 10;
            }
            case KING_BLACK -> {
                return -10;
            }
        }
        if (isPieceTypeWhite(pceTypeNr))
            return pieceBaseValue(PAWN) + ( pieceBaseValue(QUEEN)-pieceBaseValue(pceTypeNr) );
        // formula for black is actually identical, but just in case someone wants to use different base values for black and white (above)
        return pieceBaseValue(PAWN_BLACK) + ( pieceBaseValue(QUEEN_BLACK)-pieceBaseValue(pceTypeNr) );
    }

    // for comparisons
    public static final int EVAL_TENTH = pieceBaseValue(PAWN)/10;  // a tenth od a PAWN
    public static final int EVAL_DELTAS_I_CARE_ABOUT = EVAL_TENTH;
    public static final int EVAL_HALFAPAWN = (positivePieceBaseValue(PAWN)>>1);

    /** evalIsOkForColByMin checks if a squares local evaluation (board perspective)
     * is significantly close to zero (by min) or even good for its own color
     * @param eval local squares result for my piece
     * @param col color of my piece
     * @param min what i tolerate, even against my favour.
     *            if min<0 then I must have at least -min benefit.
     * @return boolean result if ok for my piece to go there (false should result in a nogo flag)
     *  Be aware, if relEval is still NOT_EVALUATED this returns also true.
     */
    public static boolean evalIsOkForColByMin(final int eval, final boolean col, final int min) {
        if (eval==NOT_EVALUATED)
            return true;
        if (min>0)
            return abs(eval)<min
                || (col==BLACK && eval<0)
                || (col==WHITE && eval>0);
        return (col==BLACK && eval<min)
                || (col==WHITE && eval>-min);
    }

    /**
     * same as method with 3 params, with min set to EVAL_TENTH
     */
    public static boolean evalIsOkForColByMin(final int eval, final boolean col) {
        return eval==NOT_EVALUATED
                || abs(eval)<EVAL_DELTAS_I_CARE_ABOUT
                || (col==BLACK && eval<0)
                || (col==WHITE && eval>0);
    }


 /*   //public static final String[] FIGURE_NAMES = {"none", "König", "Dame", "Turm", "Läufer", "Springer", "Bauer", "eine Figure", "Turm der hinter einer Dame war", "Läufer der hinter einer Dame war"};
    private static final String[] pieceNames;
    static {
        pieceNames = new String[BLACK_PIECE*2];
        pieceNames[EMPTY]       = chessBasicRes.getString("empty");
        pieceNames[KING]        = chessBasicRes.getString("pieceName.king");
        pieceNames[QUEEN]       = chessBasicRes.getString("pieceName.queen");
        pieceNames[ROOK]        = chessBasicRes.getString("pieceName.rook");
        pieceNames[BISHOP]      = chessBasicRes.getString("pieceName.bishop");
        pieceNames[KNIGHT]      = chessBasicRes.getString("pieceName.knight");
        pieceNames[PAWN]        = chessBasicRes.getString("pieceName.pawn");
        pieceNames[KING_BLACK]  = chessBasicRes.getString("pieceName.king");
        pieceNames[QUEEN_BLACK] = chessBasicRes.getString("pieceName.queen");
        pieceNames[ROOK_BLACK]  = chessBasicRes.getString("pieceName.rook");
        pieceNames[BISHOP_BLACK]= chessBasicRes.getString("pieceName.bishop");
        pieceNames[KNIGHT_BLACK]= chessBasicRes.getString("pieceName.knight");
        pieceNames[PAWN_BLACK]  = chessBasicRes.getString("pieceName.pawn");
    } */

    private static final String pieceFenChars = chessBasicRes.getString("pieceCharset.fen");
    public static char fenCharFromPceType(int pceType) {
        return // pieceFenChars.charAt(pceType);
                switch (pceType) {
                    case EMPTY ->        pieceFenChars.charAt(0);
                    case KING ->         pieceFenChars.charAt(1);
                    case QUEEN ->        pieceFenChars.charAt(2);
                    case ROOK ->         pieceFenChars.charAt(3);
                    case BISHOP ->       pieceFenChars.charAt(4);
                    case KNIGHT ->       pieceFenChars.charAt(5);
                    case PAWN ->         pieceFenChars.charAt(6);
                    case KING_BLACK ->   pieceFenChars.charAt(9);
                    case QUEEN_BLACK ->  pieceFenChars.charAt(10);
                    case ROOK_BLACK ->   pieceFenChars.charAt(11);
                    case BISHOP_BLACK -> pieceFenChars.charAt(12);
                    case KNIGHT_BLACK -> pieceFenChars.charAt(13);
                    case PAWN_BLACK ->   pieceFenChars.charAt(14);
                    default -> '/';
                };

    }

    public static int getPceTypeFromPromoteChar(char promoteToChar) {
        int promoteToFigNr;
        switch (promoteToChar) {
            case 'q', 'Q', 'd', 'D', ' ' -> promoteToFigNr = QUEEN;
            case 'n', 'N', 's', 'S' -> promoteToFigNr = KNIGHT;
            case 'b', 'B', 'l', 'L' -> promoteToFigNr = BISHOP;
            case 'r', 'R', 't', 'T' -> promoteToFigNr = ROOK;
            default -> {
                promoteToFigNr = QUEEN;
            }
        }
        return promoteToFigNr;
    }


    public static int pceTypeFromPieceSymbol(char c) {
        int pceTypeNr = switch (c) {
            case 'q', 'Q', 'd', 'D' -> QUEEN;
            case 'n', 'N', 's', 'S' -> KNIGHT;
            case 'b', 'B', 'l', 'L' -> BISHOP;
            case 'r', 'R', 't', 'T' -> ROOK;
            case 'k', 'K'           -> KING;
            case 'p', 'P', 'o', '*' -> PAWN;
            default -> EMPTY;
        };
        /*if (isWhite(getTurnCol()))*/
        if (Character.isUpperCase(c) || c=='*')
            return pceTypeNr;
        // black
        return BLACK_PIECE + pceTypeNr;
    }

    public static boolean isQueen(int pceType) {
        return (pceType&WHITE_FILTER)==QUEEN;
    }

    public static boolean isPawn(int pceType) {
        return (pceType&WHITE_FILTER)==PAWN;
    }

    public static boolean isKing(int pceType) {
        return (pceType&WHITE_FILTER)==KING;
    }

    public static boolean isRook(int pceType) {
        return (pceType&WHITE_FILTER)==ROOK;
    }

    public static boolean isSlidingPieceType(int pceType) {
        int type = colorlessPieceType(pceType);
        return (type==ROOK || type==BISHOP || type==QUEEN);
    }

    public static boolean isLightPieceType(int pceType) {
        int type = colorlessPieceType(pceType);
        return (type==BISHOP || type==KNIGHT);
    }

    public static String pieceNameForType(int pceType) {
        return //pieceNames[pceType];
            switch (pceType) {
                case EMPTY ->        chessBasicRes.getString("empty");
                case KING ->         chessBasicRes.getString("pieceName.king");
                case QUEEN ->        chessBasicRes.getString("pieceName.queen");
                case ROOK ->         chessBasicRes.getString("pieceName.rook");
                case BISHOP ->       chessBasicRes.getString("pieceName.bishop");
                case KNIGHT ->       chessBasicRes.getString("pieceName.knight");
                case PAWN ->         chessBasicRes.getString("pieceName.pawn");
                case KING_BLACK ->   chessBasicRes.getString("pieceName.king");
                case QUEEN_BLACK ->  chessBasicRes.getString("pieceName.queen");
                case ROOK_BLACK ->   chessBasicRes.getString("pieceName.rook");
                case BISHOP_BLACK -> chessBasicRes.getString("pieceName.bishop");
                case KNIGHT_BLACK -> chessBasicRes.getString("pieceName.knight");
                case PAWN_BLACK ->   chessBasicRes.getString("pieceName.pawn");
                default -> new String("unknown");
            };
    }

    public static @NotNull String pieceColorAndName(int pceType) {
        return colorName(colorOfPieceType(pceType))
                + (isQueen(pceType) ? chessBasicRes.getString("langPostfix.femaleAttr")
                                      : chessBasicRes.getString("langPostfix.maleAttr"))
                + " "
                + pieceNameForType(pceType);
    }

    public static boolean isPieceTypeWhite(int pceType) {
        return (pceType & BLACK_PIECE) == 0;
    }

    public static boolean isPieceTypeBlack(int pceType) {
        return (pceType & BLACK_PIECE) != 0;
    }

    public static boolean colorOfPieceType(int pceType) {
        return (pceType & BLACK_PIECE)==0;
    }

    public static int colorIndexOfPieceType(int pceType) {
        return colorIndex((pceType & BLACK_PIECE)==0 );
    }

    public static int colorlessPieceType(int pceType) {
        return  isPieceTypeWhite(pceType) ? pceType : (pceType&WHITE_FILTER);
    }

    // ******* about MOVING

    // directions of moving (as seen of white player)
    public static final int UP    = -NR_FILES;
    public static final int DOWN  = +NR_FILES;
    public static final int LEFT  = -1;
    public static final int RIGHT = +1;
    public static final int UPLEFT    = UP + LEFT;
    public static final int UPRIGHT   = UP + RIGHT;
    public static final int DOWNLEFT  = DOWN+LEFT;
    public static final int DOWNRIGHT = DOWN+RIGHT;

    public static final int MAXMAINDIRS = 8;
    public static final int ALLDIRS = 17;
    public static final int NONE = -ALLDIRS-1;
    public static final int MULTIPLE = -ALLDIRS-2;
    public static final int BACKWARD_NONSLIDING = -ALLDIRS-3;
    public static final int FROMNOWHERE = -NR_SQUARES;  // a direction

    public static final int NOWHERE = -NR_SQUARES-1;  // a position
    public static final int POS_UNSET = NOWHERE-1;
    private static final int[] MAINDIRS = {UPLEFT, UP, UPRIGHT,        LEFT, RIGHT,     DOWNLEFT, DOWN, DOWNRIGHT};
    //                                          -9 -8 -7                -1    +1                +7 +8 +9
    private static final int[] MAINDIRINDEXES = {0, 1, 2, 0, 0, 0, 0, 0, 3, 0, 4, 0, 0, 0, 0, 0, 5, 6, 7};

    public static int convertMainDir2DirIndex(final int dir) {
        return MAINDIRINDEXES[dir + 9];
    }

    public static int convertDirIndex2MainDir(final int d) {
        return MAINDIRS[d];
    }

    public static int oppositeDirIndex(final int dirindex) {
        return (MAXMAINDIRS-1)-dirindex;
    }

    public static final int[] ROYAL_DIRS = { RIGHT, LEFT, UPRIGHT, DOWNLEFT, UPLEFT, DOWNRIGHT, DOWN, UP };
    public static final int[] HV_DIRS = { RIGHT, LEFT, DOWN, UP };
    public static final int[] DIAG_DIRS = { UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT };
    public static final int[] NODIRS = {};

    public static boolean isRookDir(final int dir) {
        for (int d : HV_DIRS)
            if (dir == d)
                return true;
        return false;
    }

    public static boolean isBishopDir(final int dir) {
        for (int d : DIAG_DIRS)
            if (dir == d)
                return true;
        return false;
    }

    public static boolean isCorrectSlidingPieceDirFromTo(final int pceType, final int from, final int to) {
        // should be extended to all piece types at some point :-)
        int dir = calcDirFromTo(from, to);
        switch (pceType) {
            case QUEEN, QUEEN_BLACK:
                return isRookDir(dir) || isBishopDir(dir);
            case ROOK, ROOK_BLACK:
                return isRookDir(dir);
            case BISHOP, BISHOP_BLACK:
                return isBishopDir(dir);
            case KING:
            case KING_BLACK:
            case KNIGHT:
            case KNIGHT_BLACK:
            case PAWN:
            case PAWN_BLACK:
            case EMPTY:
            default:
                return false;
        }
    }


    private static final int[] WPAWN_ALL_DIRS = { UPLEFT, UP, UPRIGHT };
    private static final int WPAWN_STRAIGHT_DIR = UP;
    private static final int WPAWN_LONG_DIR = 2*UP;
    private static final int[] WPAWN_BEATING_DIRS = { UPLEFT, UPRIGHT };
    private static final int[] WPAWN_ALL_DIRS_INCL_LONG = { UPLEFT, UP, UPRIGHT, 2*UP };  // careful, order of 2-square-move in this array is relevant for pawn algorithm

    private static final int[] BPAWN_ALL_DIRS = { DOWNLEFT, DOWN, DOWNRIGHT };
    private static final int BPAWN_STRAIGHT_DIR = DOWN;
    private static final int BPAWN_LONG_DIR = 2*DOWN;
    private static final int[] BPAWN_BEATING_DIRS = { DOWNLEFT, DOWNRIGHT };
    private static final int[] BPAWN_ALL_DIRS_INCL_LONG = { DOWNLEFT, DOWN, DOWNRIGHT, 2*DOWN };

    public static final int KNIGHT_DIR_UPUPLEFT = UP+UPLEFT;
    public static final int KNIGHT_DIR_UPUPRIGHT = UP+UPRIGHT;
    public static final int KNIGHT_DIR_DNDNLEFT = DOWN+DOWNLEFT;
    public static final int KNIGHT_DIR_DNDNRIGHT = DOWN+DOWNRIGHT;
    public static final int KNIGHT_DIR_LELEUP = LEFT+UPLEFT;
    public static final int KNIGHT_DIR_LELEDOWN = LEFT+DOWNLEFT;
    public static final int KNIGHT_DIR_REREUP = RIGHT+UPRIGHT;
    public static final int KNIGHT_DIR_REREDOWN = RIGHT+DOWNRIGHT;
    public static final int[] KNIGHT_DIRS = { LEFT+UPLEFT, UP+UPLEFT, UP+UPRIGHT, RIGHT+UPRIGHT, LEFT+DOWNLEFT, RIGHT+DOWNRIGHT, DOWN+DOWNLEFT, DOWN+DOWNRIGHT };

    public static int[] getAllPawnDirs(boolean col, int fromRank) {
        if (isWhite(col)) {
            return (fromRank==1)
                    ? WPAWN_ALL_DIRS_INCL_LONG
                    : fromRank==NR_RANKS-1 ? NODIRS : WPAWN_ALL_DIRS;
        }
        return (fromRank==NR_RANKS-2)
                ? BPAWN_ALL_DIRS_INCL_LONG
                : fromRank==0 ? NODIRS : BPAWN_ALL_DIRS;
    }

    public static int[] getAllPawnPredecessorDirs(boolean col, int fromRank) {
        if (isWhite(col)) {
            return (fromRank==3)
                    ? BPAWN_ALL_DIRS_INCL_LONG
                    : fromRank==1 ? NODIRS : BPAWN_ALL_DIRS;
        }
        return (fromRank==NR_RANKS-4)
                ? WPAWN_ALL_DIRS_INCL_LONG
                : fromRank==NR_RANKS-2 ? NODIRS : WPAWN_ALL_DIRS;
    }

    public static boolean hasLongPawnPredecessor(boolean color, int pos) {
        return (isWhite(color) && rankOf(pos)==3
                || !isWhite(color) && rankOf(pos)==NR_RANKS-4);
    }

    /**
     * calculates origin(predecessor) position for a position that is able to be reached by a long (2-square) move of a pawn
     * @param color - color of pqwn
     * @param pos - target position
     * @return position of origin square (on rank 1 rsp. NR-Ranks-2); -1 in error case = not possible
     */
    public static int getLongPawnPredecessorPos(boolean color, int pos) {
        if (isWhite(color) && rankOf(pos)==3)
             return pos+BPAWN_LONG_DIR;
        if (!isWhite(color) && rankOf(pos)==NR_RANKS-4)
            return pos+WPAWN_LONG_DIR;
        return -1;
    }

    /**
     * same as getLongPawnPredecessorPos(), but returns tha mid pos that the pawn jumps over
     * @param color - color of pqwn
     * @param pos - target position
     * @return position of square that is jumped over (on rank 2 rsp. NR-Ranks-3); -1 in error case = not possible
     */
    public static int getLongPawnMoveMidPos(boolean color, int pos) {
        if (isWhite(color) && rankOf(pos)==3)
            return pos+DOWN;
        if (!isWhite(color) && rankOf(pos)==NR_RANKS-4)
            return pos+UP;
        return -1;
    }

    public static int getSimpleStraightPawnPredecessorPos(boolean color, int pos) {
        if (isWhite(color)) {
            return (rankOf(pos)<=1 ? -1 : pos+BPAWN_STRAIGHT_DIR);
        }
        return (rankOf(pos)>=NR_RANKS-2 ? -1 : pos+WPAWN_STRAIGHT_DIR);
    }

    public static int[] getBeatingPawnPredecessorDirs(boolean col, int fromRank) {
        if (isWhite(col)) {
            return (fromRank==1 ? NODIRS : BPAWN_BEATING_DIRS);
        }
        return (fromRank==NR_RANKS-2 ? NODIRS : WPAWN_BEATING_DIRS);
    }

    public static List<Integer> getAllPawnPredecessorPositions(boolean col, int fromPos) {
        List<Integer> result = new ArrayList<>(4);
        if (isWhite(col)) {
            if (isFirstRank(fromPos))
                return result;
            result.add(fromPos+DOWN);
            if (rankOf(fromPos)==rankOf(A1SQUARE+3*UP) )
                result.add(fromPos+2*DOWN);
            if (!isFirstFile(fromPos))
                result.add(fromPos+DOWNLEFT);
            if (!isLastFile(fromPos))
                result.add(fromPos+DOWNRIGHT);
        } else {
            if (isLastRank(fromPos))
                return result;
            result.add(fromPos + UP);
            if (rankOf(fromPos) == rankOf(A1SQUARE+4*UP) )
                result.add(fromPos + 2 * UP);
            if (!isFirstFile(fromPos))
                result.add(fromPos + UPLEFT);
            if (!isLastFile(fromPos))
                result.add(fromPos + UPRIGHT);
        }
        return result;
    }
    public static List<Integer> getAllPawnAttackPositions(boolean col, int fromPos) {
        List<Integer> result = new ArrayList<>(2);
        if (isWhite(col)) {
            if (isFirstRank(fromPos))
                return result;
            if (!isFirstFile(fromPos))
                result.add(fromPos+DOWNLEFT);
            if (!isLastFile(fromPos))
                result.add(fromPos+DOWNRIGHT);
        } else {
            if (isLastRank(fromPos))
                return result;
            if (!isFirstFile(fromPos))
                result.add(fromPos + UPLEFT);
            if (!isLastFile(fromPos))
                result.add(fromPos + UPRIGHT);
        }
        return result;
    }

    public static String dirIndexDescription(int dirIndex) {
        return switch (dirIndex) {
            case NONE     -> chessBasicRes.getString("direction.unset");
            case ALLDIRS  -> chessBasicRes.getString("direction.all");
            case MULTIPLE -> chessBasicRes.getString("direction.multiple");
            case BACKWARD_NONSLIDING -> chessBasicRes.getString("direction.backwardNonsliding");
            default ->  ( dirIndex<0 ? "-" : "") +
                    switch (convertDirIndex2MainDir(dirIndex<0 ? -dirIndex-1 : dirIndex)) {
                        case UP    -> chessBasicRes.getString("direction.up");
                        case DOWN  -> chessBasicRes.getString("direction.down");
                        case LEFT  -> chessBasicRes.getString("direction.left");
                        case RIGHT -> chessBasicRes.getString("direction.right");
                        case UPLEFT  -> chessBasicRes.getString("direction.upleft");
                        case UPRIGHT -> chessBasicRes.getString("direction.upright");
                        case DOWNLEFT -> chessBasicRes.getString("direction.downleft");
                        case DOWNRIGHT-> chessBasicRes.getString("direction.downright");
                        default -> chessBasicRes.getString("direction.error");
                    };
        };
    }
    // ******* Squares
    @Contract(pure = true)
    public static @NotNull String squareName(final int pos) {
        if (pos == ANYWHERE)
            return "**";
        return (char) ((int) 'a' + fileOf(pos)) + String.valueOf((char) ((int) '1' + rankOf(pos)));
    }

    /**
     * calcs "raw" rank of a position on a board.
     * Just like pos it is numeric and starts at 0 - BUT: 0 equals to rank "1", 7 to rank "8"
     * @param pos : int from 0-NR_SQUARES (typically 64-1=63) starting from the side of black player (0=typically "a8")
     * @return rank : int representing rank from the perspective of white player
     */
    public static int rankOf(int pos) {
        // Achtung, Implementierung passt sich nicht einer verändert Boardgröße an.
        return 7- (pos >> 3);
    }

    /**
     * calcs "raw" file of a position on a board.
     * Just like pos it is numeric and starts at 0 - 0 equals to file "a", 7 to file "h"
     * @param pos : int from 0-NR_SQUARES (typically 64-1=63) starting from the side of black player (0=typically "a8")
     * @return file : int representing files, typically 0-7 meaning a-h.
     */
    public static int fileOf(int pos) {
        // Achtung, Implementierung passt sich nicht einer verändert Boardgröße an.
        return (pos & 7);
    }

    /**
     * tells if two positions are on the same file.
     * @param pos1
     * @param pos2
     * @return true if on the same file - if one pos is invalid or ANYWHERE (<0) then this is always false
     */
    public static boolean onSameFile(int pos1, int pos2) {
        // Achtung, Implementierung passt sich nicht einer verändert Boardgröße an.
        return fileOf(pos1) == fileOf(pos2)
                && pos1 >= 0
                && pos2 >= 0;
    }

    public static int coordinateString2Pos(@NotNull String coordinate) {
        return coordinateString2Pos(coordinate,0);
    }

    public static int coordinateString2Pos(@NotNull String move, final int coordinateIndexInString) {
        return (move.charAt(coordinateIndexInString) - 'a')
                + NR_FILES * ((NR_FILES-1) - (move.charAt(coordinateIndexInString+1) - '1'));
    }

    public static int fileRank2Pos(final int file, final int rank) {
        return (NR_RANKS-1-rank) * NR_FILES + file;
    }

    // static Board position check functions
    public static boolean isFirstFile(int pos) {
        // Achtung, Implementierung passt sich nicht einer verändert Boardgröße an.
        return ((pos & 0b0111) == 0);
    }

    public static boolean isFileChar(char f) {
        return (f>='a' && f<('a'+NR_FILES));
    }

    public static boolean isRankChar(char r) {
        return (r>='1' && r<('1'+NR_RANKS));
    }


    public static boolean isLastFile(int pos) {
        // Achtung, Implementierung passt sich nicht einer verändert Boardgröße an.
        return ((pos & 0b0111) == 0b0111);
    }

    public static boolean isFirstRank(int pos) {
        return (pos >= NR_SQUARES - NR_FILES);
    }

    public static boolean isLastRank(int pos) {
        return (pos < NR_FILES);
    }

    public static int firstFileInRank(int pos) {
        return (pos/ NR_FILES)* NR_FILES;
    }

    public static int lastFileInRank(int pos) {
        return firstFileInRank(pos)+NR_FILES-1;
    }

    public static int firstRankInFile(int pos) {
        return A1SQUARE + fileOf(pos);
    }

    public static int lastRankInFile(int pos) {
        return fileOf(pos);
    }

    public static boolean squareColor(int pos) {
        return ((rankOf(pos)+fileOf(pos))%2)==0;   // true for white
    }

    public static boolean isSameSquareColor(int p1, int p2) {
        return ((rankOf(p1)+fileOf(p1))%2)==((rankOf(p2)+fileOf(p2))%2);   // true for white
    }

    public static boolean isPromotionRankForColor(int pos, boolean color) {
        // actually the color parameter and if are not necessary if this is checked for a pawn move, as the pawns
        // never reach their own first rank anyway... but to be generic/complete for other use cases
        return isWhite(color) ? isLastRank( pos)
                              : isFirstRank(pos);
    }

    public static int promotionDistanceForColor(int pos, boolean color) {
        return isWhite(color) ? (NR_RANKS-1) - rankOf(pos)
                              : rankOf(pos);
    }

    public static boolean neighbourSquareExistsInDirFromPos(int dir, int pos) {
        // designed to work only for "direct" directions, i.e. to the neighbouring fields.  (e.g. +1, but not for a two-hop +2)
        return ! (   ( isFirstFile(pos) && ( dir==LEFT  || dir==UPLEFT   || dir==DOWNLEFT ) )
                  || ( isLastFile(pos)  && ( dir==RIGHT || dir==UPRIGHT  || dir==DOWNRIGHT) )
                  || ( isFirstRank(pos) && ( dir==DOWN  || dir==DOWNLEFT || dir==DOWNRIGHT) )
                  || ( isLastRank(pos)  && ( dir==UP    || dir==UPLEFT   || dir==UPRIGHT  ) ) );
    }

    public static boolean knightMoveInDirFromPosStaysOnBoard(int dir, int pos) {
        // designed to work only for "direct"=one hop knight moves
        final int rank = rankOf(pos);
        final int file = fileOf(pos);
        switch (dir) {
            case KNIGHT_DIR_UPUPLEFT  -> { return rank<NR_RANKS-2 && file>0; }
            case KNIGHT_DIR_UPUPRIGHT -> { return rank<NR_RANKS-2 && file<NR_FILES-1; }
            case KNIGHT_DIR_DNDNLEFT  -> { return rank>1          && file>0; }
            case KNIGHT_DIR_DNDNRIGHT -> { return rank>1          && file<NR_FILES-1; }
            case KNIGHT_DIR_LELEUP    -> { return rank<NR_RANKS-1 && file>1; }
            case KNIGHT_DIR_LELEDOWN  -> { return rank>0          && file>1; }
            case KNIGHT_DIR_REREUP    -> { return rank<NR_RANKS-1 && file<NR_FILES-2; }
            case KNIGHT_DIR_REREDOWN  -> { return rank>0          && file<NR_FILES-2; }
        }
        return false;
        // TODO: throw illegalMoveException
    }

    /** checks whether it is possible to move in one hop from one place to another and in what direction
     * that would be. Does not work for Knights! returns NONE for knight jumps.
     *
     * @param frompos the starting point
     * @param topos the endpoint
     * @return the (possibly sliding) direction or NONE if not possible.
     */
    public static int calcDirFromTo(int frompos, int topos) {
        int fileDelta = fileOf(topos) - fileOf(frompos);
        int rankDelta = rankOf(topos) - rankOf(frompos);
        if (fileDelta==0) {
            if (rankDelta<0)
                return DOWN;
            else if (rankDelta>0)
                return UP;
            else
                return NONE;
        }
        else if (rankDelta==0) {
            if (fileDelta<0)
                return LEFT;
            else // is if (fileDelta>0), because both==0 is already covered above
                return RIGHT;
        }
        else if (rankDelta==fileDelta) {
            if (fileDelta<0)
                return DOWNLEFT;
            else // if (fileDelta>0)
                return UPRIGHT;
        }
        else if (rankDelta==-fileDelta) {
            if (fileDelta<0)
                return UPLEFT;
            else //if (fileDelta>0)
                return DOWNRIGHT;
        }
        return NONE;
    }

    public static int calcDirIndexFromTo(final int frompos, final int topos) {
        return convertMainDir2DirIndex(calcDirFromTo(frompos, topos));
    }

    /**
     * returns an array of positions between 2 squares, incl. fromPosIncl, but not toPosExcl
     * @param fromPosIncl
     * @param toPosExcl
     * @return returns empty int[0] for no solution.
     */
    public static int[] calcPositionsFromTo(final int fromPosIncl, final int toPosExcl) {
        int dir = calcDirFromTo(fromPosIncl, toPosExcl);
        if (dir==NONE)
            return new int[0];
        int len = distanceBetween(fromPosIncl,toPosExcl);
        int[] res = new int[len];
        int pos = fromPosIncl;
        for (int i = 0; i < res.length; i++) {
            res[i] = pos;
            pos += dir;
        }
        return res;
    }

    /**
     * tells if a position is on the way to another (in one slide)
     * @param checkpos the position to check
     * @param fromPosExcl beware: fromPosExcl is excluded from a true result
     * @param toPosExcl beware: toPosExcl is excluded from a true result
     * @return true if checkpos is in between.  false if somewhere else, even if on fromPosExcl or on toPosExcl
     */
    public static boolean isBetweenFromAndTo(final int checkpos, final int fromPosExcl, final int toPosExcl) {
        int maindir = calcDirFromTo(fromPosExcl, toPosExcl);
        return maindir!=NONE
                && calcDirFromTo(fromPosExcl, checkpos) == maindir
                && calcDirFromTo(checkpos, toPosExcl) == maindir;
    }

    /**
     * straight king-like hopping distance between 2 squares, not counting starting point
     * (e.g. neighbouring squares have distance 1)
     * @param fromPos
     * @param toPos
     * @return
     */
    public static int distanceBetween(final int fromPos, final int toPos) {
        int dx, dy;
        dx = abs( fileOf(fromPos) - fileOf(toPos));
        dy = abs( rankOf(fromPos) - rankOf(toPos));
        return max(dx, dy);
    }

    public static boolean dirsAreOnSameAxis(int dir1, int dir2) {
        return dir1 == dir2 || dir1 == -dir2;
    }


    /**
     * returns 0..3 according to the axis of the given direction.
     * @param dir
     * @return 0: E-W, 1: NE-SW, 2: N-S, 3: SE-NW
     */
    public static int convertDir2AxisIndex(int dir) {
        return convertMainDir2DirIndex( abs(dir) ) - 4;
    }


    /** general UI strings
         *
         */
    public static String TEXTBASICS_NOTSET = chessBasicRes.getString("text.notset");
    public static String TEXTBASICS_FROM = chessBasicRes.getString("text.from");

    //// here now even some bitmap helpers

    public static String bitmap2String(int cbm) {
        //String.format("%32s", Integer.toBinaryString(cmbl)).replace(' ', '0')
        //return Integer.toBinaryString(cbm);
        return String.format("%13s", Integer.toBinaryString(cbm)).replace(' ', '0');
    }

    public static int pos2bitmap(int pos) {
        return 1<<pos;
    }

    public static final int LAST_RANK_CBM;
    static {
        int res = 0;
        for (int i = 0; i < NR_RANKS; i++) {
            res += pos2bitmap(coordinateString2Pos(((char)((int)'a'+i)) + ("0"+NR_RANKS)) );
        }
        LAST_RANK_CBM = res;
        //System.out.println("TEST8: " + res + " = " + bitmap2String(res));
    }

    public static final int FIRST_RANK_CBM;
    static {
        int res = 0;
        for (int i = 0; i < NR_RANKS; i++) {
            res += pos2bitmap(coordinateString2Pos(((char)((int)'a'+i))+"1" ) );
        }
        FIRST_RANK_CBM = res;
        //System.out.println("TEST1: " + res + " = " + bitmap2String(res));
    }

    /** to be called with a one square distance-dir only
     *
     * @param pos legal starting pos
     * @param dir direction (UP,DOWN,LEFT,UPLEFT,...), no knightmoves
     * @return true if square pos+dir does not fall of the board
     */
    public static boolean plusDirIsStillLegal(int pos, int dir) {
        // N or S
        if (pos+dir < 0 || pos+dir >= NR_SQUARES)
            return false;
        // E
        if ( isFirstFile(pos) &&
                (dir==LEFT || dir==DOWNLEFT || dir==UPLEFT) )
            return false;
        // W
        if ( isLastFile(pos) &&
                (dir==RIGHT || dir==DOWNRIGHT || dir==UPRIGHT) )
            return false;
        return true;
    }

    /**
     * compares evaluations correctly depending on for which color it is better
     * @param eval1 first evaluation for which we want to know if it is better/higher
     * @param eval2 2nd eval, that the first is compared to
     * @param color the color of the player determines if a higher or a lower value (in board perspective) is better or a smaller one
     * @return
     */
    public static boolean isBetterThenFor(int eval1, int eval2, boolean color) {
        return isWhite(color) ? eval1 > eval2
                              : eval1 < eval2;
    }

    public static int maxFor(int eval1, int eval2, boolean color) {
        return isWhite(color) ? max(eval1,eval2)
                              : min(eval1,eval2);
    }

    public static int minFor(int eval1, int eval2, boolean color) {
        return isWhite(color) ? min(eval1,eval2)
                              : max(eval1,eval2);
    }

}