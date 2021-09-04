/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class ChessBasics {

    public static final ResourceBundle chessBasicRes = ResourceBundle.getBundle("de.ensel.chessTexts");

    ////// B/W
    public static final boolean WHITE = true;
    public static final boolean BLACK = false;
    public static boolean isWhite(boolean col) {
        return col;  // actually correct is: col==WHITE;
    }
    public static boolean isBlack(boolean col) {
        return col==BLACK;
    }
    public static int colorIndex(boolean col) {
        return col ? 0 : 1;
    }
    public static boolean opponentColor(boolean col) {
        return !col;
    }
    private static final String colorNameWhite= chessBasicRes.getString("colorname_white");
    private static final String colorNameBlack= chessBasicRes.getString("colorname_black");
    public static String colorName(boolean col) {
        return col ? colorNameWhite : colorNameBlack;
    }


    // ******* CONSTs for Evaluation

    // eval is not set
    static final int NOT_EVALUATED = Integer.MIN_VALUE + 25000;  // value was chosen out of reach of MIN_VALUE+high evaluation

    // absolute evaluation in centipawns with pro-white = pos,  pro-black=neg
    static final int WHITE_IS_CHECKMATE = -99999;
    static final int BLACK_IS_CHECKMATE = 99999;

    // relative evaluation in centipawns with pro-my-color = pos,  pro-opponent=neg
    static final int OPPONENT_IS_CHECKMATE = 111111;
    static final int IM_CHECKMATE = -111111;

    static final int CHECK_IN_N_DELTA=10;
    //static final int CHECKMATE=BLACK_IS_CHECKMATE-(CHECK_IN_N_DELTA<<4)-1;


    // *******  CONSTs concerning rules

    // Nr of squares - careful, cannot be simply changed, some parts of code rely on this for performance reasons. Esp. the file,rank->pos calculation.
    static final int NR_SQUARES = 64;
    static final int NR_RANKS = 8;  // 1-8
    static final int NR_FILES = 8;  // a-h
    static final int MAX_PIECES = 32+16+16;  //32 at the beginning + 2x16 promoted pawns.
    static final int A1SQUARE = NR_SQUARES-NR_FILES;
    // max nr of moves without pawn move or taking a piece
    static final int MAX_BORING_MOVES = 50;     // should be: 50;
    // starting position
    public static final String FENPOS_INITIAL = chessBasicRes.getString("fen.stdChessStartingPosition");
    static final String FENPOS_EMPTY = chessBasicRes.getString("fen.emptyChessBoard");

    // *******  about PIECES

    //general or WHITE piece types
    static final int EMPTY = 0;
    static final int KING  = 1;
    static final int QUEEN = 2;
    static final int ROOK  = 3;
    static final int BISHOP= 4;
    static final int KNIGHT= 5;
    static final int PAWN  = 6;
    /*public static final int OWN_PIECE = 7;
    public static final int OPPONENT_PIECE = -7;
    static final int NR_ROOK_BEHIND_QUEEN = 8;
    static final int NR_BISHOP_BEHIND_QUEEN = 9;*/
    //BLACK piece types
    private static final int WHITE_FILTER = 7;
    public static final int BLACK_PIECE = 8;
    public static final int KING_BLACK  = BLACK_PIECE + 1;
    public static final int QUEEN_BLACK = BLACK_PIECE + 2;
    public static final int ROOK_BLACK  = BLACK_PIECE + 3;
    public static final int BISHOP_BLACK= BLACK_PIECE + 4;
    public static final int KNIGHT_BLACK= BLACK_PIECE + 5;
    public static final int PAWN_BLACK  = BLACK_PIECE + 6;

    private static final int[] PIECE_BASE_VALUE = {0, 1200, 940, 530, 320, 290, 100, 1, 510, 305};
    public static int getPieceBaseValue(int pceTypeNr) {
        return PIECE_BASE_VALUE[colorlessPieceTypeNr(pceTypeNr)];
    }
    //public static final String[] FIGURE_NAMES = {"none", "König", "Dame", "Turm", "Läufer", "Springer", "Bauer", "eine Figure", "Turm der hinter einer Dame war", "Läufer der hinter einer Dame war"};
    public static final String[] figureNames;
    static {
        figureNames = new String[BLACK_PIECE*2];
        figureNames[EMPTY]       = chessBasicRes.getString("empty");
        figureNames[KING]        = chessBasicRes.getString("pieceName.king");
        figureNames[QUEEN]       = chessBasicRes.getString("pieceName.queen");
        figureNames[ROOK]        = chessBasicRes.getString("pieceName.rook");
        figureNames[BISHOP]      = chessBasicRes.getString("pieceName.bishop");
        figureNames[KNIGHT]      = chessBasicRes.getString("pieceName.knight");
        figureNames[PAWN]        = chessBasicRes.getString("pieceName.pawn");
        figureNames[KING_BLACK]  = chessBasicRes.getString("pieceName.king");
        figureNames[QUEEN_BLACK] = chessBasicRes.getString("pieceName.queen");
        figureNames[ROOK_BLACK]  = chessBasicRes.getString("pieceName.rook");
        figureNames[BISHOP_BLACK]= chessBasicRes.getString("pieceName.bishop");
        figureNames[KNIGHT_BLACK]= chessBasicRes.getString("pieceName.knight");
        figureNames[PAWN_BLACK]  = chessBasicRes.getString("pieceName.pawn");
    }
    public static final char[] figureFenNames;
    static {
        figureFenNames = new char[BLACK_PIECE * 2];
        figureFenNames[EMPTY]   = ' ';
        figureFenNames[KING]    = 'K';
        figureFenNames[QUEEN]    = 'Q';
        figureFenNames[ROOK]    = 'R';
        figureFenNames[BISHOP]    = 'B';
        figureFenNames[KNIGHT]    = 'N';
        figureFenNames[PAWN]    = 'P';
        figureFenNames[KING_BLACK]    = 'k';
        figureFenNames[QUEEN_BLACK]    = 'q';
        figureFenNames[ROOK_BLACK]    = 'r';
        figureFenNames[BISHOP_BLACK]    = 'b';
        figureFenNames[KNIGHT_BLACK]    = 'n';
        figureFenNames[PAWN_BLACK]    = 'p';
    }
    //"Turm der hinter einer Dame war", "Läufer der hinter einer Dame war";

    static final String figureFENCharSet = chessBasicRes.getString("pieceCharset.fen");
    static final String figureCharSet = chessBasicRes.getString("pieceCharset.display");

    public static boolean isQueen(int pceTypeNr) {
        return (pceTypeNr&WHITE_FILTER) == QUEEN;
    }

    public static String givePieceName(int pceTypeNr) {
        return figureNames[pceTypeNr];
    }

    public static @NotNull String pieceColorAndName(int pceTypeNr) {
        return colorName(colorOfPieceTypeNr(pceTypeNr))
                + (isQueen(pceTypeNr) ? chessBasicRes.getString("langPostfix.femaleAttr")
                                      : chessBasicRes.getString("langPostfix.maleAttr"))
                + " "
                + figureNames[pceTypeNr];
    }

    public static boolean isPieceTypeNrWhite(int pceTypeNr) {
        return (pceTypeNr & BLACK_PIECE) == 0;
    }
    public static boolean isPieceTypeNrBlack(int pceTypeNr) {
        return (pceTypeNr & BLACK_PIECE) != 0;
    }
    public static boolean colorOfPieceTypeNr(int pceTypeNr) {
        return (pceTypeNr & BLACK_PIECE)==0;
    }

    public static int colorlessPieceTypeNr(int pceTypeNr) {
        return  isPieceTypeNrWhite(pceTypeNr) ? pceTypeNr : (pceTypeNr&WHITE_FILTER);
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
    public static final int FROMNOWHERE = -NR_SQUARES;
    private static final int[] MAINDIRS = {UPLEFT, UP, UPRIGHT,        LEFT, RIGHT,     DOWNLEFT, DOWN, DOWNRIGHT};
    //                                          -9 -8 -7                -1    +1                +7 +8 +9
    private static final int[] MAINDIRINDEXES = {0, 1, 2, 0, 0, 0, 0, 0, 3, 0, 4, 0, 0, 0, 0, 0, 5, 6, 7};

    static int convertMainDir2DirIndex(final int dir) {
        return MAINDIRINDEXES[dir + 9];
    }

    static int convertDirIndex2MainDir(final int d) {
        return MAINDIRS[d];
    }

    static int oppositeDirIndex(final int dirindex) {
        return (MAXMAINDIRS-1)-dirindex;
    }

    static final int[] ROYAL_DIRS = { RIGHT, LEFT, UPRIGHT, DOWNLEFT, UPLEFT, DOWNRIGHT, DOWN, UP };
    static final int[] HV_DIRS = { RIGHT, LEFT, DOWN, UP };
    static final int[] DIAG_DIRS = { UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT };
    static final int[] WPAWN_DIRS = { UPLEFT, UP, UPRIGHT };
    static final int[] WPAWN_LONG_DIR = { 2*UP };
    static final int[] BPAWN_DIRS = { DOWNLEFT, DOWN, DOWNRIGHT };
    static final int[] BPAWN_LONG_DIR = { 2*DOWN };
    static final int KNIGHT_DIR_UPUPLEFT = UP+UPLEFT;
    static final int KNIGHT_DIR_UPUPRIGHT = UP+UPRIGHT;
    static final int KNIGHT_DIR_DNDNLEFT = DOWN+DOWNLEFT;
    static final int KNIGHT_DIR_DNDNRIGHT = DOWN+DOWNRIGHT;
    static final int KNIGHT_DIR_LELEUP = LEFT+UPLEFT;
    static final int KNIGHT_DIR_LELEDOWN = LEFT+DOWNLEFT;
    static final int KNIGHT_DIR_REREUP = RIGHT+UPRIGHT;
    static final int KNIGHT_DIR_REREDOWN = RIGHT+DOWNRIGHT;
    static final int[] KNIGHT_DIRS = { LEFT+UPLEFT, UP+UPLEFT, UP+UPRIGHT, RIGHT+UPRIGHT, LEFT+DOWNLEFT, RIGHT+DOWNRIGHT, DOWN+DOWNLEFT, DOWN+DOWNRIGHT };

    // ******* Squares
    @Contract(pure = true)
    public static @NotNull String squareName(final int pos) {
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

    public static int coordinateString2Pos(@NotNull String coordinate) {
        return coordinateString2Pos(coordinate,0);
    }

    public static int coordinateString2Pos(@NotNull String move, final int coordinateIndexInString) {
        return (move.charAt(coordinateIndexInString) - 'a') + NR_FILES * ((NR_FILES-1) - (move.charAt(coordinateIndexInString+1) - '1'));
    }

    public static int fileRank2Pos(final int file, final int rank) {
        return (NR_RANKS-1-rank) * NR_FILES + file;
    }

    // static Board position check functions
    public static boolean isFirstFile(int pos) {
        // Achtung, Implementierung passt sich nicht einer verändert Boardgröße an.
        return ((pos & 0b0111) == 0);
    }

    public static boolean isRankChar(char r) {
        return (r>='a' && r<('a'+NR_RANKS));
    }

    public static boolean isFileChar(char f) {
        return (f>='1' && f<('1'+NR_FILES));
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
}