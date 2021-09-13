/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.MAX_INSIGHT_LEVELS;
import static de.ensel.tideeval.ChessBoard.NOPIECE;
import static de.ensel.tideeval.Distance.INFINITE_DISTANCE;
import static org.junit.jupiter.api.Assertions.*;
import static java.lang.Math.abs;

        /* template for scenario visualisations
        8 ░░  ░░  ░░  ░░
        7   ░░  ░░  ░░  ░░
        6 ░░  ░░  ░░  ░░
        5   ░░  ░░  ░░  ░░
        4 ░░  ░░  ░░  ░░
        3   ░░  ░░  ░░  ░░
        2 ░░  ░░  ░░  ░░
        1   ░░  ░░  ░░  ░░
          A B C D E F G H
        or:
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5    ░░░   ░░░   ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3    ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1    ░░░   ░░░   ░░░   ░░░
        A  B  C  D  E  F  G  H    */


class ChessBoardTest {

    /**
     * Path for test-sets / files
     */
    private static final String TESTSETS_PATH = "./out/test/TideEval/de/ensel/tideeval/";

    @Test
    @Disabled("Not Implemented")
    void doMove() {
    }

    @Test
    @Disabled("Not Implemented")
    void testDoMove() {
    }

    @SuppressWarnings("GrazieInspection")
    @Test
    void chessBoardBasicFigurePlacement_Test() {
        ChessBoard board = new ChessBoard("TestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int rookW1pos = A1SQUARE;
        board.spawnPieceAt(ROOK,rookW1pos);
        /*
        8 ░░░ r1░2░   ░░░   ░░░ 3
        7    ░x░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5    ░░░   ░░░   ░░░   ░░░
        4 ░░░ d ░░░   ░░░   ░░░
        3    ░d░   ░░░ d ░░░   ░░░
        2 ░░░ d ░░░   ░░░   ░░░
        1 dR1░dx   ░░░ d ░x░dR2░dx
           A  B  C  D  E  F  G  H    */
        // test if pieces are there
        int rookW1Id = board.getPieceIdAt(rookW1pos);
        assertEquals( pieceColorAndName(ROOK),       board.getPieceFullName(rookW1Id));
        // and nothing there next to it (see "x")
        assertEquals( null, board.getPieceAt(rookW1pos+RIGHT) );
        assertEquals( NOPIECE, board.getPieceIdAt(rookW1pos+RIGHT) );
        // test distances to pieces stored at squares
        // dist from rookW1
        assertEquals( 0, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW1pos,         rookW1Id));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW1pos+RIGHT,   rookW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW1pos+UPRIGHT, rookW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW1pos+UPRIGHT+UP,  rookW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW1pos+UPRIGHT+2*UP,rookW1Id));

        // test if two more pieces are there
        int rookW2pos = 62;
        int rookB1pos = 1;
        board.spawnPieceAt(ROOK,rookW2pos);
        board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        int rookW2Id = board.getPieceIdAt(rookW2pos);
        int rookB1Id = board.getPieceIdAt(rookB1pos);
        assertEquals( pieceColorAndName(ROOK),       board.getPieceFullName(rookW2Id));
        assertEquals( pieceColorAndName(ROOK_BLACK), board.getPieceFullName(rookB1Id));
        // nothing there (see "x")
        assertEquals( NOPIECE, board.getPieceIdAt(rookW2pos+LEFT) );
        assertEquals( NOPIECE, board.getPieceIdAt(rookW2pos+RIGHT) );
        assertEquals( NOPIECE, board.getPieceIdAt(rookB1pos+DOWN) );
        // test distances to pieces stored at squares
        // dist from rookW2
        assertEquals( 0, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW2pos,         rookW2Id));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW2pos+RIGHT,   rookW2Id));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW2pos+2*LEFT,  rookW2Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW2pos+2*UPLEFT,rookW2Id));
        // these distances only work, when other own piece is moving away
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW2pos, rookW1Id));
        assertEquals( 2, board.getShortestConditionalDistanceToPosFromPieceId(rookW2pos, rookW1Id));
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW2pos+RIGHT,   rookW1Id));
        assertEquals( 2, board.getShortestConditionalDistanceToPosFromPieceId(rookW2pos+RIGHT,   rookW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW1pos, rookW2Id));
        // at square 2
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookB1pos+RIGHT,rookW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookB1pos+RIGHT,rookW2Id));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(rookB1pos+RIGHT,rookB1Id));
        // at square 3
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(7,rookW1Id));

        /* add two pieces -> they should block some of the ways and increase the distances
        8 ░d░dr1░░░ d ░b1 d ░d░
        7    ░░░   ░d░   ░░░   ░░░
        6 ░░░   ░░░ b2░░░   ░░░
        5    ░░░   ░░░   ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3    ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░ d
        1 dR1░░░   ░░░   ░░░ R2░░░
           A  B  C  D  E  F  G  H    */
        // test if distances are updated

        int bishopB1pos = 4;
        int bishopB2pos = 4+DOWNLEFT+DOWN;
        board.spawnPieceAt(BISHOP_BLACK,bishopB1pos);
        board.spawnPieceAt(BISHOP_BLACK,bishopB2pos);
        // test if pieces are there
        int bishopB1Id = board.getPieceIdAt(bishopB1pos);
        int bishopB2Id = board.getPieceIdAt(bishopB2pos);
        assertEquals( pieceColorAndName(BISHOP_BLACK),board.getPieceFullName(bishopB1Id));
        assertEquals( pieceColorAndName(BISHOP_BLACK),board.getPieceFullName(bishopB2Id));
        // test distances to pieces stored at squares
        // dist from rookW1
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB2pos+UP,   rookW1Id));  // still 2
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos+RIGHT,rookW1Id));  // still 2
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos+LEFT, rookW1Id));  // increased to 3
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos,      rookW1Id));  // still 2, by taking bishop
        // dist from rookW2
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB2pos+UP,   rookW2Id));  // still 2
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos+RIGHT,rookW2Id));  // still 2
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos+LEFT, rookW2Id));  // increased to 3
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos,      rookW2Id));  // still 2, by taking bishop
        // dist from rookB1
        assertEquals( 2, board.getShortestConditionalDistanceToPosFromPieceId(bishopB1pos+RIGHT,rookB1Id));  // increased to 2, after moving bishop
        // dist from bishopB1
        assertEquals(INFINITE_DISTANCE, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos+RIGHT,bishopB1Id));  // wrong square color
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos+4*LEFT,      bishopB1Id));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos+3*DOWNRIGHT, bishopB1Id));
        // dist from bishopB2
        assertEquals(INFINITE_DISTANCE, board.getShortestUnconditionalDistanceToPosFromPieceId(bishopB1pos+2*RIGHT,bishopB2Id));  // wrong square color
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookB1pos, bishopB2Id));  //  2, but only after moving rook away
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(rookW1pos, bishopB2Id));  // still 2, but only after moving bishop

        /* add two kings -> they should block some of the ways and increase the distances,
                            but also are interesting with long distances accross the board...
        8 ░4░ r1░k░ 3 ░b1 2 ░░░
        7    ░░░   ░1░   ░░░   ░░░
        6 ░░░   ░░░ b2░░░   ░░░
        5    ░░░   ░░░   ░░░   ░5░
        4 ░░░   ░░░   ░░░   ░░░
        3    ░░░ K ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1  R1░░░   ░░░   ░░░ R2░░░
           A  B  C  D  E  F  G  H    */
        // test if distances are updated

        int kingWpos = rookW1pos+2*UPRIGHT;
        int kingBpos = rookB1pos+1;
        board.spawnPieceAt(KING,kingWpos);
        board.spawnPieceAt(KING_BLACK,kingBpos);
        // test if pieces are there
        int kingWId = board.getPieceIdAt(kingWpos);
        int kingBId = board.getPieceIdAt(kingBpos);
        assertEquals( pieceColorAndName(KING),board.getPieceFullName(kingWId));
        assertEquals( pieceColorAndName(KING_BLACK),board.getPieceFullName(kingBId));
        // test distances to pieces stored at squares
        // dist from rookW1
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookW1Id));
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, rookW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookW1Id));
        // dist from rookW2
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW2Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookW2Id));
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, rookW2Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookW2Id));
        // dist from rookB1
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookB1Id));  // now 3, (way around or king+bishop move away)
        assertEquals( 3, board.getShortestConditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookB1Id));  // now 3, but only after moving king and bishop
        // dist from bishopB1
        assertEquals(INFINITE_DISTANCE, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/ bishopB1pos+RIGHT,bishopB1Id));  // wrong square color
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*4*/ bishopB1pos+4*LEFT,      bishopB1Id));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(/*5*/ bishopB1pos+3*DOWNRIGHT, bishopB1Id));
        // dist from bishopB2
        assertEquals(INFINITE_DISTANCE, board.getShortestUnconditionalDistanceToPosFromPieceId(/*4*/ bishopB1pos+4*LEFT,bishopB2Id));  // wrong square color
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*R1*/ rookW1pos, bishopB2Id));  // now 3, after taking K or moving around
        // dist from KingW
        assertEquals( 4, board.getShortestUnconditionalDistanceToPosFromPieceId(/*1*/  bishopB2pos+UP,   kingWId));
        assertEquals( 5, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,kingWId));
        assertEquals( 5, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingWId));
        // dist from KingB
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(/*1*/  bishopB2pos+UP,   kingBId));
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,kingBId));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));

        /* add two queens
        8 ░4░ r1░k░3q ░b1 2 ░░░
        7    ░░░   ░1░   ░░░   ░░░
        6 ░░░   ░░░ b2░░░   ░░░
        5    ░░░   ░░░   ░░░   ░5░
        4 ░░░   ░░░   ░░░   ░░░
        3    ░░░ K ░░░   ░░░   ░░░
        2 ░░░ Q ░░░   ░░░   ░░░
        1  R1░░░   ░░░   ░░░ R2░░░
           A  B  C  D  E  F  G  H    */
        // test if distances are updated

        int queenWpos = rookW1pos+UPRIGHT;
        int queenBpos = rookB1pos+2;
        board.spawnPieceAt(QUEEN,queenWpos);
        board.spawnPieceAt(QUEEN_BLACK,queenBpos);
        // test if pieces are there
        int queenWId = board.getPieceIdAt(queenWpos);
        int queenBId = board.getPieceIdAt(queenBpos);
        assertEquals( pieceColorAndName(QUEEN),board.getPieceFullName(queenWId));
        assertEquals( pieceColorAndName(QUEEN_BLACK),board.getPieceFullName(queenBId));
        // test distances to pieces stored at squares
        // dist from rookW1 - unverändert
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookW1Id));
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, rookW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookW1Id));
        // dist from rookW2 - unverändert
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW2Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookW2Id));
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, rookW2Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookW2Id));
        // dist from rookB1
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookB1Id));  //  3, but only by way around
        assertEquals( 4, board.getShortestUnconditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookB1Id));  // 4 after moving king, queen and bishop, or on way around+moving bishop
        // dist from bishopB2
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*R1*/ rookW1pos, bishopB2Id));  // now 3, after moving around K and taking Q
        // dist from KingB
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,kingBId));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));  // only after moving q away

        /* add two knights
        8 ░4░ r1░k░3q ░b1 2 ░░░
        7    ░░░ n ░1░   ░░░   ░░░
        6 ░░░   ░░░ b2░░░   ░░░
        5    ░░░   ░░░   ░░░   ░5░
        4 ░░░   ░░░ N ░░░   ░░░
        3    ░░░ K ░░░   ░░░   ░░░
        2 ░░░ Q ░░░   ░░░   ░░░
        1  R1░░░   ░░░   ░░░ R2░░░
           A  B  C  D  E  F  G  H    */
        // test if distances are updated

        int knightWpos = kingWpos+UPRIGHT;
        int knightBpos = kingBpos+DOWN;
        board.spawnPieceAt(KNIGHT,knightWpos);
        board.spawnPieceAt(KNIGHT_BLACK,knightBpos);
        // test if pieces are there
        int knightWId = board.getPieceIdAt(knightWpos);
        int knightBId = board.getPieceIdAt(knightBpos);
        assertEquals( pieceColorAndName(KNIGHT),board.getPieceFullName(knightWId));
        assertEquals( pieceColorAndName(KNIGHT_BLACK),board.getPieceFullName(knightBId));
        // test distances to pieces stored at squares
        // dist from rookW1
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW1Id));  // now 3
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookW1Id));
        // dist from rookB1
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookB1Id));  //  3, but only by way around
        assertEquals( 4, board.getShortestUnconditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookB1Id));  // 4 after moving king, queen and bishop, or on way around+moving bishop
        // dist from bishopB2
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*R1*/ rookW1pos, bishopB2Id));  // now 3, after moving around K and taking Q
        // dist from KingB
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,kingBId));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));  // only after moving q away
        // dist from N
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*5*/  bishopB1pos+3*DOWNRIGHT,knightWId));
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  A1SQUARE, knightWId));  // only after moving q away
        // dist from n
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,knightBId));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(/*3*/  bishopB2pos+RIGHT, knightBId));  // only after moving q away

        /* add pawns
        8 ░4░ r1░k░3q ░b1 2 ░░░
        7    ░░░ n ░1░   ░p░   ░░░
        6 ░░░   ░░░ b2░░░   ░p.
        5    ░░░   ░░░ . ░.░   ░5░
        4 ░░░   ░░░ N.░.░   ░.░
        3    ░░░ K ░░░ . ░P░   ░░░
        2 ░░░ Q ░░░   ░P░   ░░░
        1  R1░░░   ░░░   ░░░ R2░░░
           A  B  C  D  E  F  G  H    */
        // test if distances are updated

        int pW1pos = queenWpos+3*RIGHT;
        int pW2pos = kingWpos+3*RIGHT;
        int pB1pos = bishopB1pos+DOWNRIGHT;
        int pB2pos = bishopB1pos+2*DOWNRIGHT;
        board.spawnPieceAt(PAWN,pW1pos);
        board.spawnPieceAt(PAWN,pW2pos);
        board.spawnPieceAt(PAWN_BLACK,pB1pos);
        board.spawnPieceAt(PAWN_BLACK,pB2pos);
        // test if pieces are there
        int pW1Id = board.getPieceIdAt(pW1pos);
        int pW2Id = board.getPieceIdAt(pW2pos);
        int pB1Id = board.getPieceIdAt(pB1pos);
        int pB2Id = board.getPieceIdAt(pB2pos);
        assertEquals( pieceColorAndName(PAWN),board.getPieceFullName(pW1Id));
        assertEquals( pieceColorAndName(PAWN_BLACK),board.getPieceFullName(pB1Id));
        // test distances to pieces stored at squares
        // dist from rookW1
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookW1Id));   // now 3, via a5 - TODO: testcase via pW1 has to move away, but can't because can only move straight...
        // dist from rookB1
        assertEquals( 4, board.getShortestUnconditionalDistanceToPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookB1Id));  //  now 4
        // dist from bishopB1
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*5*/  bishopB1pos+3*DOWNRIGHT, bishopB1Id));  // now 3, after moving both pB or moving around
        // dist from pW1 -> "."
        assertEquals( 0, board.getShortestUnconditionalDistanceToPosFromPieceId(/*.*/  pW1pos,pW1Id));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(/*.*/  pW1pos+UP,pW1Id));
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(/*.*/  pW1pos+2*UP,pW1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*.*/  pW1pos+3*UP,pW1Id));
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*.*/  knightWpos, pW1Id));  // knight needs to walk away // TODO: PAWN cannot take towards an empty square... so actuiallo correct is D_NOT-SET
        assertEquals(INFINITE_DISTANCE, board.getShortestUnconditionalDistanceToPosFromPieceId(/*.*/  knightWpos+2*LEFT, pW1Id));  // not reachable
        // dist from pW2 -> "."
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(/*.*/  pW2pos+2*UP+UPRIGHT,pW2Id));
        // dist from pBx -> "."
        assertEquals( 1, board.getShortestUnconditionalDistanceToPosFromPieceId(/*.*/  pB1pos+2*DOWN,pB1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(/*.*/  pB2pos+2*DOWN,pB2Id));
    }

    @Test
    void boardEvaluation_Test() {
        String[] testSetFiles = {
                "T_13xx.cts", "T_16xx.cts", "T_22xx.cts", "T_22xxVs11xx.cts",
                "V_13xx.cts", "V_16xx.cts", "V_22xx.cts", "V_22xxVs11xx.cts",
        };
        int[] expectedDeltaAvg = { 1000, 800 };
        int overLimit = 0;
        for ( String ctsFilename: testSetFiles ) {
            System.out.println();
            System.out.println("Testing Set " + ctsFilename + ": ");
            int evalDeltaAvg[] = boardEvaluation_Test_Set(ctsFilename);
            // check the result of every insight-level for this test-set
            System.out.print("Evaluation deltas: " );
            for (int i=0; i<ChessBoard.MAX_INSIGHT_LEVELS; i++) {
                System.out.print("" + evalDeltaAvg[i] + ((i<ChessBoard.MAX_INSIGHT_LEVELS-1) ? ", " : "") );
                if ( evalDeltaAvg[i] > expectedDeltaAvg[i] || evalDeltaAvg[i] < -expectedDeltaAvg[i] )
                    overLimit++;
            }
            System.out.println(".");
        }

        // value in assertion is kind of %age of how many sets*InsightLevels where not fulfilled
        assertEquals(100, 100-(overLimit*100)/(testSetFiles.length*MAX_INSIGHT_LEVELS));
    }

    private int[] boardEvaluation_Test_Set(String ctsFilename) {
        int evalDeltaSum[] = new int[ChessBoard.MAX_INSIGHT_LEVELS];

        //Read file, iterate over testgames in there
        int testedPositionsCounter = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(TESTSETS_PATH + ctsFilename))) {
            String line;
            // read the contents of the file line per line = game per game
            while ((line = br.readLine()) != null) {
                testedPositionsCounter += boardEvaluation_Test_testOneGame(line, evalDeltaSum);
            }
        } catch (IOException e) {
            System.out.println("Error reading file "+ctsFilename);
            e.printStackTrace();
        }
        // devide all sums by nr of positions evaluated
        if (testedPositionsCounter>0) {
            for (int i = 0; i < ChessBoard.MAX_INSIGHT_LEVELS; i++)
                evalDeltaSum[i] /= testedPositionsCounter;
        }
        System.out.println("Finished test of "+testedPositionsCounter+" positions from Test set "+ctsFilename+".");
        return evalDeltaSum;
    }

    /**
     * itarates over moves in one game and compares the delta of the evaluation with the eval in the game-string
     * @param ctsOneGameLine - String: something like "1. e4 0.24 1... c5 0.32 Nf3 0.0 2... Nf6 0.44"
     * @param evalDeltaSum int[]: to add of the evaluation deltas for each level to
     * @return
     */
    private int boardEvaluation_Test_testOneGame(final String ctsOneGameLine, int evalDeltaSum[]) {
        // begin with start postition
        ChessBoard chessBoard = new ChessBoard("Test ", FENPOS_INITIAL);
        ChessGameReader cgr = new ChessGameReader(ctsOneGameLine);
        // skip evaluation of some moves by just making the moves
        for (int i = 0; i < 10 && cgr.hasNext(); i++) {
            chessBoard.doMove(cgr.getNextMove());
            cgr.getNextEval();
        }

        // while über alle Züge in der partie
        int testedPositionsCounter = 0;
        boolean moveValid=true;
        while( cgr.hasNext() && (moveValid=chessBoard.doMove(cgr.getNextMove())) ) {
            testedPositionsCounter++;
            int expectedEval = cgr.getNextEval();
            if (expectedEval==OPPONENT_IS_CHECKMATE)
                expectedEval = isWhite(chessBoard.getTurnCol()) ? BLACK_IS_CHECKMATE : WHITE_IS_CHECKMATE;
            for (int i = 0; i < ChessBoard.MAX_INSIGHT_LEVELS; i++) {
                evalDeltaSum[i] += abs(expectedEval - chessBoard.boardEvaluation(i + 1));
            }
        }
        if (!moveValid)
            System.out.println(" *** Spiel abgebrochen wg. fehlerhaftem Zug ***");
        return testedPositionsCounter;
    }

    @Test
    public void boardEvaluation_Simple_Test() {
        boardEvaluation_SingleBoard_Test( FENPOS_INITIAL, 0, 50);
        boardEvaluation_SingleBoard_Test( FENPOS_EMPTY, 0, 10);
    }

    private void boardEvaluation_SingleBoard_Test(String fen, int expectedEval, int tolerance) {
        int evalDeltaSum[] = new int[ChessBoard.MAX_INSIGHT_LEVELS];
        //TODO: Read file, iterate over test-boards in there
        ChessBoard chessBoard = new ChessBoard("Test " + fen, fen );
        System.out.println("Testing " + chessBoard.getShortBoardName() );
        int overLimit = 0;
        for (int i=1; i<=ChessBoard.MAX_INSIGHT_LEVELS; i++) {
            int eval = chessBoard.boardEvaluation(i);
            System.out.println("eval on level " + i + " is: " + eval + " -> delta: " + (eval-expectedEval) );
            if ( abs( eval - expectedEval ) > tolerance )
                overLimit++;
        }
        assertTrue(overLimit==0 );
    }

    @Test
    void doMove_String_Test() {
        // Test 1
        ChessBoard chessBoard = new ChessBoard("MoveTest " , FENPOS_INITIAL );
        assertEquals(32, chessBoard.getPieceCounter() );
        assertTrue( chessBoard.doMove("e4")       );
        assertEquals(32, chessBoard.getPieceCounter() );
        assertTrue(     chessBoard.doMove("e5")   );
        assertTrue( chessBoard.doMove("d4")       );
        assertTrue(     chessBoard.doMove("exd4") );
        assertEquals(31, chessBoard.getPieceCounter() );
        assertTrue( chessBoard.doMove("c3")       );
        assertTrue(     chessBoard.doMove("d6?")  );
        assertTrue( chessBoard.doMove("Bc4?!")    );
        assertTrue(     chessBoard.doMove("dxc3") );
        assertEquals(30, chessBoard.getPieceCounter() );
        assertTrue( chessBoard.doMove("Nf3")      );
        assertTrue(     chessBoard.doMove("cxb2") );
        assertTrue( chessBoard.doMove("Bxb2")     );
        assertEquals(28, chessBoard.getPieceCounter() );
        assertTrue(     chessBoard.doMove("a6?!") );
        // check king+rook position after castelling
        assertEquals( KING, chessBoard.getPieceTypeAt(coordinateString2Pos("e1")));
        assertEquals( coordinateString2Pos("e1"), chessBoard.getWhiteKingPos() );
        assertEquals( ROOK, chessBoard.getPieceTypeAt(coordinateString2Pos("h1")));
        assertTrue( chessBoard.doMove("O-O")      );
        assertEquals( EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("e1")));
        assertEquals( EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("h1")));
        assertEquals( KING, chessBoard.getPieceTypeAt(coordinateString2Pos("g1")));
        assertEquals( coordinateString2Pos("g1"), chessBoard.getWhiteKingPos() );
        assertEquals( ROOK, chessBoard.getPieceTypeAt(coordinateString2Pos("f1")));

        assertEquals(28, chessBoard.getPieceCounter() );
        assertTrue(     chessBoard.doMove("Be6?") );
        assertTrue( chessBoard.doMove("Bxe6")     );
        assertTrue(     chessBoard.doMove("fxe6") );
        assertTrue( chessBoard.doMove("Qb3?")     );
        assertTrue(     chessBoard.doMove("b6?")  );
        assertTrue( chessBoard.doMove("Qxe6+")    );
        assertTrue(     chessBoard.doMove("Qe7")  );
        assertTrue( chessBoard.doMove("Qd5")      );
        assertTrue(     chessBoard.doMove("c6")   );
        assertTrue( chessBoard.doMove("Qh5+")     );
        assertTrue(     chessBoard.doMove("Qf7")  );
        assertTrue( chessBoard.doMove("Qh3")      );
        assertTrue(     chessBoard.doMove("Nd7")  );
        assertTrue( chessBoard.doMove("Nc3?")     );
        assertTrue(     chessBoard.doMove("Ngf6?!"));
        assertTrue( chessBoard.doMove("Rfe1?")    );

        // check king+rook position after castelling
        assertEquals( KING_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("e8")));
        assertEquals( coordinateString2Pos("e8"), chessBoard.getBlackKingPos() );
        assertEquals( ROOK_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("a8")));
        assertTrue(     chessBoard.doMove("O-O-O"));
        assertEquals( EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("e8")));
        assertEquals( EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("a8")));
        assertEquals( KING_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("c8")));
        assertEquals( coordinateString2Pos("c8"), chessBoard.getBlackKingPos() );
        assertEquals( ROOK_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("d8")));

        assertTrue( chessBoard.doMove("a4??")     );
        assertTrue(     chessBoard.doMove("a5??") );
        assertTrue( chessBoard.doMove("Ba3?")     );
        assertTrue(     chessBoard.doMove("Kb7?!"));
        assertTrue( chessBoard.doMove("Rab1?")    );
        assertTrue(     chessBoard.doMove("Ne5")  );
        String newFen = chessBoard.getBoardFEN();  // TODO
        assertEquals(EMPTY,      chessBoard.getPieceTypeAt(coordinateString2Pos("e2") ));
        assertEquals(PAWN,       chessBoard.getPieceTypeAt(coordinateString2Pos("e4") ));
        assertEquals(PAWN_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("a5") ));
        assertEquals(KING_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("b7") ));
        assertEquals(KING,       chessBoard.getPieceTypeAt(coordinateString2Pos("g1") ));
        assertEquals(EMPTY,      chessBoard.getPieceTypeAt(coordinateString2Pos("f1") ));
        assertEquals(ROOK,       chessBoard.getPieceTypeAt(coordinateString2Pos("e1") ));

        // Test 2
        // Problem was:  still ok:  d4(d2d4) e6(e7e6) c4(c2c4) c6(c7c6) e4(e2e4) Nf6?(g8f6) e5(e4e5) Ne4(f6e4) Bf4?(c1f4) Qa5+?!(d8a5) Nd2(b1d2) Bb4?(f8b4) 
        //  Problem 1:  Nf3(d2f3) - should have been the other Knight, as this one is pinned!
        //  then Problem 2: O-O?(e8g8) - would have been illegal if Ng1 is still there...
        //  then seems ok: Be2?!(f1e2) Be7?!(b4e7) O-O(e1g1) 
        //  but Problem 3: Qd8?? - is a legal move, why was it not recognized?
        //  then: Nxe4 - ok, does not exist after P1... 
        //  and seems ok: d5(d7d5) cxd5(c4d5) cxd5(c6d5) 
        //  might be Folgeproblem, but still strange how a knight could move 2 squares straight or diagonal...: Nc3(f3c3) f6(f7f6) Re1(f1e1) fxe5(f6e5) Bxe5(f4e5) Nc6(b8c6) Bg3(e5g3) Qa5 Qd2 e5(e6e5) dxe5(d4e5) Nxe5(c6e5) Nxe5(c3e5) Qb6(a5b6) Nxd5(e5d5) Qe6(b6e6) Nxe7+(d5e7) Qxe7(e6e7) Bc4+(e2c4) Be6(c8e6) Bxe6+(c4e6) Qxe6 Nd7(e5d7)**** Fehler: Fehlerhafter Zug: auf e5 steht keine Figur auf Board Test .
        chessBoard = new ChessBoard("MoveTest " , FENPOS_INITIAL );
        assertTrue( chessBoard.doMove("d4"));
        assertTrue(     chessBoard.doMove("e6"));
        assertTrue( chessBoard.doMove("c4"));
        assertTrue(     chessBoard.doMove("c6"));
        assertTrue( chessBoard.doMove("e4"));
        assertTrue(     chessBoard.doMove("Nf6?"));
        assertTrue( chessBoard.doMove("e5"));
        assertTrue(     chessBoard.doMove("Ne4"));
        assertTrue( chessBoard.doMove("Bf4?"));
        assertTrue(     chessBoard.doMove("Qa5+?!"));
        assertTrue( chessBoard.doMove("Nd2"));
        assertTrue(     chessBoard.doMove("Bb4?"));
        assertTrue( chessBoard.doMove("Nf3"));
        // check if correct Knight has moved - tricky, due to pinned knight on d2
        assertEquals(KNIGHT, chessBoard.getPieceTypeAt(coordinateString2Pos("f3")));
        //TODO!!:  Here is the current problem that needs a general king-pin-soltion first.
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("g1")));
        assertEquals(KNIGHT, chessBoard.getPieceTypeAt(coordinateString2Pos("d2")));
        assertTrue( chessBoard.doMove("O-O?"));
        assertTrue( chessBoard.doMove("Be2?!"));
        assertTrue( chessBoard.doMove("Be7?!"));
        assertTrue( chessBoard.doMove("O-O"));
        assertTrue( chessBoard.doMove("Qd8??"));
        // check if queen has moved correctly
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("a5")));
        assertEquals(QUEEN_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("d8")));
        assertTrue( chessBoard.doMove("Nxe4"));
        assertTrue( chessBoard.doMove("d5"));
        assertTrue( chessBoard.doMove("cxd5"));
        assertTrue( chessBoard.doMove("cxd5"));
        assertTrue( chessBoard.doMove("Nc3"));
        assertTrue( chessBoard.doMove("f6"));
        assertTrue( chessBoard.doMove("Re1"));
        assertTrue( chessBoard.doMove("fxe5"));
        assertTrue( chessBoard.doMove("Bxe5"));
        assertTrue( chessBoard.doMove("Nc6"));
        assertTrue( chessBoard.doMove("Bg3"));
        assertTrue( chessBoard.doMove("Qa5"));
        assertTrue( chessBoard.doMove("Qd2"));
        assertTrue( chessBoard.doMove("e5"));
        assertTrue( chessBoard.doMove("dxe5"));
        assertTrue( chessBoard.doMove("Nxe5"));
        assertTrue( chessBoard.doMove("Nxe5"));
        assertTrue( chessBoard.doMove("Qb6"));
        assertTrue( chessBoard.doMove("Nxd5"));
        assertTrue( chessBoard.doMove("Qe6"));
        assertTrue( chessBoard.doMove("Nxe7+"));
        assertTrue( chessBoard.doMove("Qxe7"));
        assertTrue( chessBoard.doMove("Bc4+"));
        assertTrue( chessBoard.doMove("Be6"));
        assertTrue( chessBoard.doMove("Bxe6+"));
        assertTrue( chessBoard.doMove("Qxe6"));
        assertTrue( chessBoard.doMove("Nd7"));
    }

    @Test
    void isPinnedByKing_Test() {
        ChessBoard board = new ChessBoard("PinnedKingTestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int kingWpos = A1SQUARE;
        int kingWId = board.spawnPieceAt(KING,kingWpos);
        int knightW1pos = kingWpos+2*UP;
        int knightW1Id = board.spawnPieceAt(KNIGHT,knightW1pos);
        int rookB1pos = knightW1pos+2*UP;
        int rookB1Id = board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5  t ░░░   ░░░   ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3  N ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1  K ░░░   ░░░   ░░░   ░░░
           A  B  C  D  E  F  G  H    */
        // test distances to pieces stored at squares
        // dist from kingW
        assertEquals( 3, board.getShortestUnconditionalDistanceToPosFromPieceId(kingWpos,   rookB1Id));
        assertEquals( 1, board.getShortestConditionalDistanceToPosFromPieceId(kingWpos,     rookB1Id));
        //assertEquals( 1, board.(kingWpos,     rookB1Id));
        assertEquals( 2, board.getShortestConditionalDistanceToPosFromPieceId(knightW1pos,  rookB1Id));
        assertEquals( 2, board.getShortestUnconditionalDistanceToPosFromPieceId(knightW1pos,rookB1Id));
    }
}


