/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.junit.jupiter.api.Test;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.DEBUGMSG_TESTCASES;
import static de.ensel.tideeval.ChessBoard.debugPrintln;
import static org.junit.jupiter.api.Assertions.*;

class SquareTest {

    @Test
    void clashEval_Test() {
        ChessBoard board = new ChessBoard("SquareClashTestBoard1", FENPOS_EMPTY);
        // put a few pieces manually:
        int kingWpos = A1SQUARE;
        int kingWId = board.spawnPieceAt(KING,kingWpos);
        int knightW1pos = kingWpos+2*UP;
        int knightW1Id = board.spawnPieceAt(KNIGHT,knightW1pos);
        // we need a black piece to move, so the knight can move back,
        int pawnB1Id = board.spawnPieceAt(PAWN_BLACK,22);
        // but then the rook pins the knight to the king
        int rookB1pos = kingWpos+4*UP;
        int rookB1Id = board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        board.completeDistanceCalc();
        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░p░
        5  r ░░░   ░░░   ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3  N ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1  K ░░░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */
        assertEquals(0,     board.getBoardSquares()[rookB1pos].clashEval());
        assertEquals(-290,  board.getBoardSquares()[knightW1pos].clashEval());
        assertEquals(0,     board.getBoardSquares()[kingWpos].clashEval());

        // now the very same board, but set up via 2 piece movements
        board = new ChessBoard("SquareClashTestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        kingWId = board.spawnPieceAt(KING,kingWpos);
        knightW1pos = kingWpos+KNIGHT_DIR_REREUP;
        knightW1Id = board.spawnPieceAt(KNIGHT,knightW1pos);
        board.completeDistanceCalc();
        assertTrue(board.doMove("Na3"));
        knightW1pos += KNIGHT_DIR_LELEUP;
        // we need a black piece to move, so the knight can move back,,
        pawnB1Id = board.spawnPieceAt(PAWN_BLACK,15);
        board.completeDistanceCalc();
        assertTrue(board.doMove("h5"));
        // but then the rook pins the knight to the king
        rookB1pos = kingWpos+4*UP;
        rookB1Id = board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        board.completeDistanceCalc();

        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░p░
        5  r ░░░   ░░░   ░░░   ░p░
        4 ░░░   ░░░   ░░░   ░░░
        3  N ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1  K ░░░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */

        assertEquals(0,     board.getBoardSquares()[rookB1pos].clashEval());
        assertEquals(-290,  board.getBoardSquares()[knightW1pos].clashEval());
        assertEquals(0,     board.getBoardSquares()[kingWpos].clashEval());

        //knight is now covered by a bishop, so it should be safe
        int bishopW1pos = kingWpos+2*RIGHT;
        int bishopW1Id = board.spawnPieceAt(BISHOP,bishopW1pos);
        board.completeDistanceCalc();
        assertEquals(0,board.getBoardSquares()[knightW1pos].clashEval());

        //but a black bishop now attacks additionally
        int bishopB1pos = rookB1pos+2*RIGHT;
        int bishopB1Id = board.spawnPieceAt(BISHOP_BLACK,bishopB1pos);
        board.completeDistanceCalc();
        assertEquals(-290,board.getBoardSquares()[knightW1pos].clashEval());

        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5  r ░░░ b ░░░   ░░░   ░p░
        4 ░░░   ░░░   ░░░   ░░░
        3  N ░░░   ░░░   ░░░   ░░░
        2 ░░░ P ░░░   ░░░   ░░░
        1  K ░░░ B ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */

        //knight is now covered by a pawn, so it should be safe
        int pW1pos = kingWpos+UPRIGHT;
        int pW1Id = board.spawnPieceAt(PAWN,pW1pos);
        board.completeDistanceCalc();
        debugPrintln(DEBUGMSG_TESTCASES, board.getBoardFEN() );
        // expected==0, because black will not take
        // but expected==-70 if code for bishop behind pawn etc. is not active
        assertEquals(0,board.getBoardSquares()[knightW1pos].clashEval());

        //but a black pawn now attacks additionally
        int pB1pos = knightW1pos+UPRIGHT;
        int pB1Id = board.spawnPieceAt(PAWN_BLACK,pB1pos);
        board.completeDistanceCalc();
        assertEquals(-290,board.getBoardSquares()[knightW1pos].clashEval());

        // but black pawn moves away again
        assertTrue(board.doMove("b4b3"));
        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5  t ░░░ b ░░░   ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3  N ░p░   ░░░   ░░░   ░░░
        2 ░░░ P ░░░   ░░░   ░░░
        1  K ░░░ B ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */
        board.completeDistanceCalc();
        // expected==0, because black will not take
        // but expected==-70 if code for bishop behind pawn etc. is not active
        assertEquals(0,board.getBoardSquares()[knightW1pos].clashEval());
    }
}