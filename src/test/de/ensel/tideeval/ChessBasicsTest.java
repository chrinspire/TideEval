/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static de.ensel.tideeval.ChessBasics.*;
import static org.junit.jupiter.api.Assertions.*;

class ChessBasicsTest {

    @Test
    void isWhite_Test() {
        assertTrue(isWhite(WHITE));
        assertFalse(isWhite(BLACK));
    }

    @Test
    void colorIndex_Test() {
        assertEquals(0, colorIndex(WHITE));
        assertEquals(1, colorIndex(BLACK));
    }

    @Test
    void colorName_Test() {
        assertNotNull(chessBasicRes.getString("colorname_white"));
        assertNotNull(chessBasicRes.getString("colorname_black"));
        assertEquals(chessBasicRes.getString("colorname_white"), colorName(WHITE));
        assertEquals(chessBasicRes.getString("colorname_black"), colorName(BLACK));
    }

    @Test
    void isQueen_Test() {
        assertTrue(isQueen(QUEEN));
        assertTrue(isQueen(QUEEN_BLACK));
        assertFalse(isQueen(ROOK));
        assertFalse(isQueen(KNIGHT_BLACK));
    }

    @Test
    void givePieceName_Test() {
        assertNotNull(chessBasicRes.getString("pieceName.knight"));
        assertNotNull(chessBasicRes.getString("pieceName.bishop"));
        assertEquals(chessBasicRes.getString("pieceName.knight"), givePieceName(KNIGHT));
        assertEquals(chessBasicRes.getString("pieceName.bishop"), givePieceName(BISHOP_BLACK));
    }

    /*@Test
    void givePieceColorAndName() {
    }*/

    @Test
    void isPieceTypeNrWhite_Test() {
        assertTrue(isPieceTypeNrWhite(QUEEN));
        assertTrue(isPieceTypeNrWhite(KING));
        assertFalse(isPieceTypeNrWhite(PAWN_BLACK));
        assertFalse(isPieceTypeNrWhite(KNIGHT_BLACK));
    }

    @Test
    void isPieceTypeNrBlack_Test() {
        assertTrue(isPieceTypeNrBlack(QUEEN_BLACK));
        assertTrue(isPieceTypeNrBlack(KING_BLACK));
        assertFalse(isPieceTypeNrBlack(PAWN));
        assertFalse(isPieceTypeNrBlack(KNIGHT));
    }

    @Test
    void colorOfPieceTypeNr_Test() {
        assertEquals(BLACK,colorOfPieceTypeNr(QUEEN_BLACK));
        assertEquals(BLACK,colorOfPieceTypeNr(KING_BLACK));
        assertEquals(WHITE,colorOfPieceTypeNr(PAWN));
        assertEquals(WHITE,colorOfPieceTypeNr(KNIGHT));
    }

    @Test
    void colorlessPieceTypeNr_Test() {
        assertEquals(QUEEN,colorlessPieceTypeNr(QUEEN_BLACK));
        assertEquals(KING,colorlessPieceTypeNr(KING_BLACK));
        assertEquals(PAWN,colorlessPieceTypeNr(PAWN));
        assertEquals(KNIGHT,colorlessPieceTypeNr(KNIGHT));
    }

    /*@Test
    void convertMainDir2DirIndex() {
    }

    @Test
    void convertDirIndex2MainDir() {
    }*/

    @Test
    @SuppressWarnings("nls")
    void squareName_Test() {
        squareName_singleTest("a1", A1SQUARE);
        squareName_singleTest("b1", A1SQUARE + RIGHT);
        squareName_singleTest("b2", A1SQUARE + UPRIGHT);
    }

    private void squareName_singleTest(String expected, int testPos) {
        assertTrue( expected.equalsIgnoreCase( squareName( testPos ) ) );
    }

    @Test
    @SuppressWarnings("nls")
    void coordinateString2Pos_Test() {
        assertEquals(A1SQUARE, coordinateString2Pos("a1b1", 0 ));
        assertEquals(A1SQUARE, coordinateString2Pos("b2a1", 2 ));
    }


    @Test
    void isFirstFile_Test() {
        for(int pos=0; pos<A1SQUARE; pos+=NR_FILES)
            assertTrue(ChessBasics.isFirstFile(pos));
        for(int pos=1; pos<A1SQUARE; pos+=NR_FILES)
            assertFalse(ChessBasics.isFirstFile(pos));
        for(int pos=NR_FILES-1; pos<(NR_SQUARES-NR_FILES); pos+=NR_FILES)
            assertFalse(isFirstFile(pos));
    }

    @Test
    void isLastFile_Test() {
        for(int pos=NR_FILES-1; pos<A1SQUARE; pos+=NR_FILES)
            assertTrue(isLastFile(pos));
        for(int pos=0; pos<A1SQUARE; pos+=NR_FILES)
            assertFalse(isLastFile(pos));
        for(int pos=NR_FILES-2; pos<A1SQUARE; pos+=NR_FILES)
            assertFalse(isLastFile(pos));
    }

    @ParameterizedTest
    @ValueSource(ints = {A1SQUARE, A1SQUARE+1, NR_SQUARES-2, NR_SQUARES-1})
        // Tests 4 true samples - assumes board size of NR_FILES>=2
    void isFirstRank_Test_True(int pos) {
        assertTrue(isFirstRank(pos));
    }

    @Test
    void isFirstRank_Test_False() {
        // run from "a8" diagonally over board, up to almost the first rank "1" to make false-samples
        for(int pos=0; pos<(A1SQUARE-NR_FILES-1); pos+=NR_FILES+1)
            assertFalse(isFirstRank(pos));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, NR_FILES-2, NR_FILES-1})
        // Tests 4 true samples - assumes board size of NR_FILES>=2
    void isLastRank_Test_True(int pos) {
        assertTrue(isLastRank(pos));
    }

    @Test
    void isLastRank_Test_False() {
        // run from "a1" diagonally over board, up to almost the last rank "1" to make false-samples
        for(int pos=A1SQUARE; pos>(2*NR_FILES-1); pos-=NR_FILES-1)
            assertFalse(isLastRank(pos));
    }

    @Test
    void firstFileInRank_Test() {
        for (int rank=0; rank<NR_RANKS; rank++) {
            int startpos = rank*NR_FILES;
            assertEquals(startpos, firstFileInRank(startpos));
            assertEquals(startpos, firstFileInRank(startpos+RIGHT));
            assertEquals(startpos, firstFileInRank(startpos + NR_FILES-1));
            assertEquals(startpos, firstFileInRank(startpos + NR_FILES-1 + 2*LEFT));
        }
    }

    @Test
    void lastFileInRank_Test() {
        for (int rank=0; rank<NR_RANKS; rank++) {
            int startpos = rank*NR_FILES;
            assertEquals(startpos+NR_FILES-1, lastFileInRank(startpos));
            assertEquals(startpos+NR_FILES-1, lastFileInRank(startpos+RIGHT));
            assertEquals(startpos+NR_FILES-1, lastFileInRank(startpos + NR_FILES-1));
            assertEquals(startpos+NR_FILES-1, lastFileInRank(startpos + NR_FILES-1 + 2*LEFT));
        }
    }

    @Test
    void firstRankInFile_Test() {
        for (int file=0; file<NR_FILES; file++) {
            assertEquals(A1SQUARE+file, firstRankInFile(A1SQUARE+file));
            assertEquals(A1SQUARE+file, firstRankInFile(file));
            assertEquals(A1SQUARE+file, firstRankInFile(file + 2 * DOWN));
            assertEquals(A1SQUARE+file, firstRankInFile(A1SQUARE + file + UP));
        }
    }

    @Test
    void lastRankInFile_Test() {
        for (int file=0; file<NR_FILES; file++) {
            assertEquals(file, lastRankInFile(A1SQUARE+file));
            assertEquals(file, lastRankInFile(file));
            assertEquals(file, lastRankInFile(file + 2 * DOWN));
            assertEquals(file, lastRankInFile(A1SQUARE + file + UP));
        }
    }

    @Test
    void rankOf_Test() {
        assertEquals(0, rankOf(A1SQUARE));
        assertEquals(0, rankOf(NR_SQUARES-1));
        assertEquals(NR_RANKS-1, rankOf(NR_FILES-1));
        assertEquals(NR_RANKS-2, rankOf(NR_FILES));
    }

    @Test
    void fileOf_Test() {
        assertEquals(0, fileOf(A1SQUARE));
        assertEquals(NR_FILES-1, fileOf(NR_SQUARES-1));
        assertEquals(NR_FILES-1, fileOf(NR_FILES-1));
        assertEquals(0, fileOf(NR_FILES));
    }
}