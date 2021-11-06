/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
import static java.lang.Math.min;
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
    void chessBoardBasicFigurePlacement_Test() {
        ChessBoard board = new ChessBoard("TestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int rookW1pos = A1SQUARE;
        board.spawnPieceAt(ROOK,rookW1pos);
        board.completeDistanceCalc();
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
        assertEquals(NO_PIECE_ID, board.getPieceIdAt(rookW1pos+RIGHT) );
        // test distances to pieces stored at squares
        // dist from rookW1
        assertEquals( 0, board.getDistanceToPosFromPieceId(rookW1pos,         rookW1Id));
        assertEquals( 1, board.getDistanceToPosFromPieceId(rookW1pos+RIGHT,   rookW1Id));
        assertEquals( 2, board.getDistanceToPosFromPieceId(rookW1pos+UPRIGHT, rookW1Id));
        assertEquals( 2, board.getDistanceToPosFromPieceId(rookW1pos+UPRIGHT+UP,  rookW1Id));
        assertEquals( 2, board.getDistanceToPosFromPieceId(rookW1pos+UPRIGHT+2*UP,rookW1Id));

        // test if two more pieces are there
        int rookW2pos = 62;
        int rookB1pos = 1;
        board.spawnPieceAt(ROOK,rookW2pos);
        board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        debugPrintln(DEBUGMSG_TESTCASES, board.getBoardFEN() );
        board.completeDistanceCalc();
        int rookW2Id = board.getPieceIdAt(rookW2pos);
        int rookB1Id = board.getPieceIdAt(rookB1pos);
        assertEquals( pieceColorAndName(ROOK),       board.getPieceFullName(rookW2Id));
        assertEquals( pieceColorAndName(ROOK_BLACK), board.getPieceFullName(rookB1Id));
        // nothing there (see "x")
        assertEquals(NO_PIECE_ID, board.getPieceIdAt(rookW2pos+LEFT) );
        assertEquals(NO_PIECE_ID, board.getPieceIdAt(rookW2pos+RIGHT) );
        assertEquals(NO_PIECE_ID, board.getPieceIdAt(rookB1pos+DOWN) );
        // test distances to pieces stored at squares
        // dist from rookW2
        checkUnconditionalDistance( 0, board, rookW2pos,         rookW2Id);
        checkUnconditionalDistance( 1, board, rookW2pos+RIGHT,   rookW2Id);
        checkUnconditionalDistance( 1, board, rookW2pos+2*LEFT,  rookW2Id);
        checkUnconditionalDistance( 2, board, rookW2pos+2*UPLEFT,rookW2Id);
        // these distances only work, when other own piece is moving away
        assertEquals( 2, board.getDistanceToPosFromPieceId(rookW2pos, rookW1Id));
        assertEquals( 2, board.getDistanceToPosFromPieceId(rookW2pos, rookW1Id));

        //checkUnconditionalDistance( 3, board, rookW2pos+RIGHT,   rookW1Id);
        checkCondDistance( 2, board, rookW2pos+RIGHT,   rookW1Id);
        checkCondDistance( 2, board, rookW1pos, rookW2Id);
        // at square 2
        checkUnconditionalDistance( 2, board,rookB1pos+RIGHT,rookW1Id);
        checkUnconditionalDistance( 2, board,rookB1pos+RIGHT,rookW2Id);
        checkUnconditionalDistance( 1, board,rookB1pos+RIGHT,rookB1Id);
        // at square 3 - thought its 3, but actually it's 2 because the black rook does not need to be taken, it can move away in between my 2 moves...  *chg*
        checkCondDistance( 2,board,7,rookW1Id);
        assertEquals(rookB1pos, board.getBoardSquares()[7].getvPiece(rookW1Id).getMinDistanceFromPiece().getFromCond(0));

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
        board.completeDistanceCalc();
        // test if pieces are there
        int bishopB1Id = board.getPieceIdAt(bishopB1pos);
        int bishopB2Id = board.getPieceIdAt(bishopB2pos);
        assertEquals( pieceColorAndName(BISHOP_BLACK),board.getPieceFullName(bishopB1Id));
        assertEquals( pieceColorAndName(BISHOP_BLACK),board.getPieceFullName(bishopB2Id));
        // test distances to pieces stored at squares
        // dist from rookW1
        checkUnconditionalDistance( 2, board,bishopB2pos+UP,   rookW1Id);  // still 2
        checkUnconditionalDistance( 2, board,bishopB1pos+RIGHT,rookW1Id);  // still 2
        checkCondDistance( 2, board,bishopB1pos+LEFT, rookW1Id);  // *chg* not increased to 3, but conditional
        checkUnconditionalDistance( 2, board,bishopB1pos,      rookW1Id);  // still 2, by taking bishop
        // dist from rookW2
        checkUnconditionalDistance( 2, board,bishopB2pos+UP,   rookW2Id);  // still 2
        checkUnconditionalDistance( 2, board,bishopB1pos+RIGHT,rookW2Id);  // still 2
        checkCondDistance( 2, board,bishopB1pos+LEFT, rookW2Id);  // increased to 3
        checkUnconditionalDistance( 2, board,bishopB1pos,      rookW2Id);  // still 2, by taking bishop
        // dist from rookB1
        assertEquals( 2, board.getDistanceToPosFromPieceId(bishopB1pos+RIGHT,rookB1Id));  // increased to 2, after moving bishop
        // dist from bishopB1
        checkUnconditionalDistance(INFINITE_DISTANCE, board,bishopB1pos+RIGHT,bishopB1Id);  // wrong square color
        checkUnconditionalDistance( 2, board,bishopB1pos+4*LEFT,      bishopB1Id);
        checkUnconditionalDistance( 1, board,bishopB1pos+3*DOWNRIGHT, bishopB1Id);
        // dist from bishopB2
        assertEquals(INFINITE_DISTANCE, board.getDistanceToPosFromPieceId(bishopB1pos+2*RIGHT,bishopB2Id));  // wrong square color
        assertEquals( 2, board.getDistanceToPosFromPieceId(rookB1pos, bishopB2Id));  //  2, but only after moving rook away
        checkUnconditionalDistance( 2, board,rookW1pos, bishopB2Id);  // still 2 by beating

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
        board.completeDistanceCalc();
        // test if pieces are there
        int kingWId = board.getPieceIdAt(kingWpos);
        int kingBId = board.getPieceIdAt(kingBpos);
        assertEquals( pieceColorAndName(KING),board.getPieceFullName(kingWId));
        assertEquals( pieceColorAndName(KING_BLACK),board.getPieceFullName(kingBId));
        // test distances to pieces stored at squares
        // dist from rookW1
        checkUnconditionalDistance( 2, board,/*1*/  bishopB2pos+UP,   rookW1Id);
        checkUnconditionalDistance( 2, board,/*2*/  bishopB1pos+RIGHT,rookW1Id);
        checkCondDistance( 2, board,/*3*/  bishopB1pos+LEFT, rookW1Id);
        checkUnconditionalDistance( 2, board,/*b1*/ bishopB1pos,      rookW1Id);
        // dist from rookW2
        checkUnconditionalDistance( 2, board,/*1*/  bishopB2pos+UP,   rookW2Id);
        checkUnconditionalDistance( 2, board,/*2*/  bishopB1pos+RIGHT,rookW2Id);
        checkCondDistance( 2, board,/*3*/  bishopB1pos+LEFT, rookW2Id);
        checkUnconditionalDistance( 2, board,/*b1*/ bishopB1pos,      rookW2Id);
        // dist from rookB1
        checkUnconditionalDistance( 3, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  // now 3, (way around or king+bishop move away)
        if (MAX_INTERESTING_NROF_HOPS>3)
            assertEquals( 3, board.getDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookB1Id));  // now 3, but only after moving king and bishop
        // dist from bishopB1
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*2*/ bishopB1pos+RIGHT,bishopB1Id);  // wrong square color
        checkUnconditionalDistance( 2, board,/*4*/ bishopB1pos+4*LEFT,      bishopB1Id);
        checkUnconditionalDistance( 1, board,/*5*/ bishopB1pos+3*DOWNRIGHT, bishopB1Id);
        // dist from bishopB2
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*4*/ bishopB1pos+4*LEFT,bishopB2Id);  // wrong square color
        checkCondDistance( 2, board,/*R1*/ rookW1pos, bishopB2Id);  //  2, after moving K away
        // dist from KingW
        checkUnconditionalDistance( 4, board,/*1*/  bishopB2pos+UP,   kingWId);
        checkUnconditionalDistance( 5, board,/*2*/  bishopB1pos+RIGHT,kingWId);
        checkUnconditionalDistance( 5, board,/*3*/  bishopB1pos+LEFT, kingWId);
        // dist from KingB
        checkUnconditionalDistance( 1, board,/*1*/  bishopB2pos+UP,   kingBId);
        checkUnconditionalDistance( 3, board,/*2*/  bishopB1pos+RIGHT,kingBId);
        checkUnconditionalDistance( 1, board,/*3*/  bishopB1pos+LEFT, kingBId);

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
        board.completeDistanceCalc();
        // test if pieces are there
        int queenWId = board.getPieceIdAt(queenWpos);
        int queenBId = board.getPieceIdAt(queenBpos);
        assertEquals( pieceColorAndName(QUEEN),board.getPieceFullName(queenWId));
        assertEquals( pieceColorAndName(QUEEN_BLACK),board.getPieceFullName(queenBId));
        // test distances to pieces stored at squares
        // dist from rookW1 - unverändert
        checkUnconditionalDistance( 2, board,/*1*/  bishopB2pos+UP,   rookW1Id);
        checkUnconditionalDistance( 2, board,/*2*/  bishopB1pos+RIGHT,rookW1Id);
        checkCondDistance( 2, board,/*3*/  bishopB1pos+LEFT, rookW1Id);
        checkUnconditionalDistance( 2, board,/*b1*/ bishopB1pos,      rookW1Id);
        // dist from rookW2 - unverändert
        checkUnconditionalDistance( 2, board,/*1*/  bishopB2pos+UP,   rookW2Id);
        checkUnconditionalDistance( 2, board,/*2*/  bishopB1pos+RIGHT,rookW2Id);
        checkCondDistance( 2, board,/*3*/  bishopB1pos+LEFT, rookW2Id);
        checkUnconditionalDistance( 2, board,/*b1*/ bishopB1pos,      rookW2Id);

        // dist from rookB1
        checkUnconditionalDistance( 3, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  //  3, but only by way around
        if (MAX_INTERESTING_NROF_HOPS>3)
            assertEquals( 4, board.getDistanceToPosFromPieceId(/*b1*/ bishopB1pos,      rookB1Id));  // 4 after moving king, queen and bishop, or on way around+moving bishop
        // dist from bishopB2
        checkUnconditionalDistance( 3, board,/*R1*/ rookW1pos, bishopB2Id);
        // dist from KingB
        checkUnconditionalDistance( 3, board,/*2*/  bishopB1pos+RIGHT,kingBId);

        assertEquals( 2, board.getDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));  // only after moving q away

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
        board.completeDistanceCalc();
        // test if pieces are there
        int knightWId = board.getPieceIdAt(knightWpos);
        int knightBId = board.getPieceIdAt(knightBpos);
        assertEquals( pieceColorAndName(KNIGHT),board.getPieceFullName(knightWId));
        assertEquals( pieceColorAndName(KNIGHT_BLACK),board.getPieceFullName(knightBId));
        // test distances to pieces stored at squares
        // dist from rookW1
        checkUnconditionalDistance( 2, board,/*b1*/ bishopB1pos,      rookW1Id);
        checkCondDistance( 2, board,/*1*/  bishopB2pos+UP,   rookW1Id);  // *chg*
        // dist from rookB1
        checkUnconditionalDistance( 3, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  //  3, but only by way around
        checkCondDistance( 4, board, /*b1*/ bishopB1pos,      rookB1Id);  // 4 after moving king, queen and bishop, or on way around+moving bishop
        // dist from bishopB2
        checkUnconditionalDistance( 3, board,/*R1*/ rookW1pos, bishopB2Id);
        // dist from N
        checkUnconditionalDistance( 3, board,/*5*/  bishopB1pos+3*DOWNRIGHT,knightWId);
        assertEquals( 3, board.getDistanceToPosFromPieceId(/*3*/  A1SQUARE, knightWId));  // only after moving q away
        // dist from KingB
        checkUnconditionalDistance( 3, board,/*2*/  bishopB1pos+RIGHT,kingBId);
        assertEquals( 2, board.getDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));  // only after moving q away
        // dist from N
        checkUnconditionalDistance( INFINITE_DISTANCE, board,/*3*/  A1SQUARE, knightWId);  // only after moving q away
        // dist from n
        assertEquals( 2, board.getDistanceToPosFromPieceId(/*b1*/ bishopB1pos,knightBId));
        assertFalse( board.isDistanceToPosFromPieceIdUnconditional(/*b1*/ bishopB1pos,knightBId));
        checkUnconditionalDistance( 1, board,/*3*/  bishopB2pos+RIGHT, knightBId);  // only after moving q away

        /* add pawns
        8 ░4░ r1░k░3q ░b1 2 ░4░
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
        board.completeDistanceCalc();
        // test if pieces are there
        int pW1Id = board.getPieceIdAt(pW1pos);
        int pW2Id = board.getPieceIdAt(pW2pos);
        int pB1Id = board.getPieceIdAt(pB1pos);
        int pB2Id = board.getPieceIdAt(pB2pos);
        assertEquals( pieceColorAndName(PAWN),board.getPieceFullName(pW1Id));
        assertEquals( pieceColorAndName(PAWN_BLACK),board.getPieceFullName(pB1Id));
        // test distances to pieces stored at squares
        // dist from rookW1
        /* in clarification:
        checkUnconditionalDistance( 3, board, bishopB1pos,      rookW1Id);   // now 3, via a5 - TODO: testcase via pW1 has to move away, but can't because can only move straight...
        value is actually 2 with a lot of conditions: (all black pieces could move away...): vPce (id=0) on [f8] is 2 if{b8-any} if{c8-any} if{d8-any} if{e8-any} away from weißer Turm
        TODO: Although this is correct according to the current implementation semantics, it shuold be rethought...
        */
        // dist from rookB1
        checkUnconditionalDistance( 4, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  //  now 4
        // dist from bishopB1
        checkUnconditionalDistance( 3, board,/*5*/  bishopB1pos+3*DOWNRIGHT, bishopB1Id);  // now 3, after moving both pB or moving around

        // dist from pW1 -> ".",b2,bB1,b1
        checkUnconditionalDistance( 0, board,/*.*/  pW1pos,pW1Id);
        checkUnconditionalDistance( 1, board,/*.*/  pW1pos+UP,pW1Id);
        checkUnconditionalDistance( 1, board,/*.*/  pW1pos+2*UP,pW1Id);
        checkUnconditionalDistance( 2, board,/*.*/  pW1pos+3*UP,pW1Id);
        //  knight would need to walk away, but even this does not help, pawn cannot go there diagonally, however, if the knight is taken, than it can -->3
        // TODO?: Later this might be INFINITE again or a high number, considering how long an opponents Piece needed to move here to be eeten...
        if (MAX_INTERESTING_NROF_HOPS>3)
            assertEquals( 3, board.getDistanceToPosFromPieceId(/*.*/  knightWpos, pW1Id));
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*.*/  knightWpos+LEFT, pW1Id);  // not reachable
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*.*/  knightWpos+2*LEFT, pW1Id);  // not reachable
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*.*/  knightWpos+UP, pW1Id);  // not reachable
        checkUnconditionalDistance(3, board,/*.*/  bishopB2pos, pW1Id);  // but, it can beat a black piece diagonally left
        checkUnconditionalDistance(4, board,/*.*/  pB1pos, pW1Id);  // and right
        checkUnconditionalDistance(5, board,/*.*/  bishopB1pos, pW1Id);  // not straigt, but via beating others...
        checkUnconditionalDistance( INFINITE_DISTANCE, board,/*.*/  pW2pos+UP,pW1Id);  // no way, also not via pW2
        // dist from pW2 -> ".",2,3
        if (MAX_INTERESTING_NROF_HOPS>3)
            assertEquals( 4, board.getDistanceToPosFromPieceId(/*.*/  pB1pos,pW2Id));  // by beating pB2
        checkUnconditionalDistance( 5, board,/*2*/  pB1pos+UP,pW2Id);  // by beating pB2
        checkUnconditionalDistance( 5, board,/*.*/  bishopB1pos,pW2Id);  // by beating pB2+pB1
        checkUnconditionalDistance( 5, board,/*4*/  pB1pos+UPRIGHT,pW2Id);  //  by beating pB2+straight
        checkUnconditionalDistance( 3, board,/*.*/  pB2pos,pW2Id);
        // dist from pBx -> "."

        checkUnconditionalDistance( 1, board,/*.*/  pB1pos+2*DOWN,pB1Id);
        checkUnconditionalDistance( INFINITE_DISTANCE, board,/*.*/  pW2pos,pB1Id);   // cannot move straight on other pawn
        // tricky case: looks like "3+1=4 to move white opponent away (sideways)", but is 3 because pB1 could beat something on g4 and then beat back to file f on f3=pW2pos
        if (MAX_INTERESTING_NROF_HOPS>3) {
        assertEquals( 3, board.getDistanceToPosFromPieceId(/*.*/  pW2pos,pB1Id));
        assertEquals( 4, board.getDistanceToPosFromPieceId(/*.*/  pW2pos+DOWN,pB1Id));    // and then also one further is possilble
        }
        checkCondDistance( 4, board, pW1pos,pB1Id);    // and over to pW1
        checkUnconditionalDistance( 2, board,/*.*/  pB2pos+2*DOWN,pB2Id);
    }

    private void checkUnconditionalDistance(int expected, ChessBoard board, int pos, int pceId) {
        if (MAX_INTERESTING_NROF_HOPS<expected)
            return;
        assertEquals( expected, board.getDistanceToPosFromPieceId(pos, pceId));
        //instead of assertTrue(board.isDistanceToPosFromPieceIdUnconditional(pos,pceId) );
        if ( ! board.isDistanceToPosFromPieceIdUnconditional(pos,pceId)) {
            debugPrint(true, "not unconditional: " + board.getBoardSquares()[pos].getvPiece(pceId) );
            assert(false);
        }
    }

    private void checkCondDistance(int expected, ChessBoard board, int pos, int pceId) {
        if (MAX_INTERESTING_NROF_HOPS<expected)
            return;
        assertEquals( expected, board.getDistanceToPosFromPieceId(pos, pceId));
        assertFalse(board.isDistanceToPosFromPieceIdUnconditional(pos,pceId) );
    }


    private int countNrOfBoardEvals = 0;
    private static final int SKIP_OPENING_MOVES = 10;
    private static final int MIN_NROF_PIECES = 6;
    // check one of the levels more thorougly
    // private static final int CHECK_EVAL_LEVEL = 5;
    private static int[] countEvalSame = new int[EVAL_INSIGHT_LEVELS];
    private static int[] countEvalRightTendency = new int[EVAL_INSIGHT_LEVELS];
    private static int[] countEvalRightTendencyButTooMuch = new int[EVAL_INSIGHT_LEVELS];
    private static int[] countEvalWrongTendency = new int[EVAL_INSIGHT_LEVELS];
    private static long[] sumEvalRightTendency = new long[EVAL_INSIGHT_LEVELS];
    private static long[] sumEvalRightTendencyButTooMuch = new long[EVAL_INSIGHT_LEVELS];
    private static long[] sumEvalWrongTendency = new long[EVAL_INSIGHT_LEVELS];
    static {
        for (int i=0; i<EVAL_INSIGHT_LEVELS;i++) {
            countEvalSame[i] = 0;
            countEvalRightTendency[i] = 0;
            countEvalRightTendencyButTooMuch[i] = 0;
            countEvalWrongTendency[i] = 0;
            sumEvalRightTendency[i] = 0;
            sumEvalRightTendencyButTooMuch[i] = 0;
            sumEvalWrongTendency[i] = 0;
        }
    }
    /* snapshot of result on 25.09.2021
    --> (1 min 17 sec - 1 min 35) ^=  340 board-evals/sec
    Finished test of 3068 positions from Test set T_13xx.cts.       Evaluation deltas: 443, 293, 280, 291, 288.
    (Cache has 17102 Entries and resulted in 377033 hits.)
    Finished test of 3759 positions from Test set T_16xx.cts.       Evaluation deltas: 372, 267, 257, 268, 269.
    Finished test of 3917 positions from Test set T_22xx.cts.       Evaluation deltas: 274, 215, 216, 234, 246.
    Finished test of 2640 positions from Test set T_22xxVs11xx.cts. Evaluation deltas: 504, 342, 323, 319, 306.
    Finished test of 2921 positions from Test set V_13xx.cts.       Evaluation deltas: 467, 308, 291, 298, 293.
    Finished test of 3348 positions from Test set V_16xx.cts.       Evaluation deltas: 401, 282, 272, 277, 279.
    Finished test of 3884 positions from Test set V_22xx.cts.       Evaluation deltas: 272, 225, 226, 249, 257.
    Finished test of 2683 positions from Test set V_22xxVs11xx.cts. Evaluation deltas: 525, 326, 311, 304, 295.
    (Cache has 29395 Entries and resulted in 3301486 hits.)
    Total Nr. of board evaluations: 26220  (40 more, seems one little bug was eliminated as well)
    Thereof within limits: 75%                                                       { 500, 400, 300, 300, 280 };
    => taking these numbers as new baseline for later comparisons.
    ---
    test with tiight limits to see time contribution of overheads
    distancelimit:0 -> 1521 evals (+800x10 skipped moves) --> 5,5 sec.
    distancelimit:1 -> 28054 evals (+800x10 skipped moves) --> 16 sec.
    distancelimit:2 -> 28560 evals (+800x10 skipped moves) --> 28 sec. (75%)
    distancelimit:3 -> 29491 evals (+800x10 skipped moves) --> 37 sec.
    distancelimit:4 -> 29945 evals (+800x10 skipped moves) --> 51 sec.
    distancelimit:5 -> 29982 evals (+800x10 skipped moves) --> 54 sec.
    distancelimit:6 -> 29969 evals (+800x10 skipped moves) --> 56 sec. (80%)
    ---
    2.10.2021: div. Probleme mit den Distanzkorrekturen behoben
    --> (1 min 12 sec)
    Finished test of 3462 positions from Test set T_13xx.cts.       Evaluation deltas: 447, 294, 278, 284, 274, 282.
    (Cache has 15858 Entries and resulted in 299102 hits.)
    Finished test of 4087 positions from Test set T_16xx.cts.       Evaluation deltas: 382, 278, 265, 267, 259, 268.
    Finished test of 4656 positions from Test set T_22xx.cts.       Evaluation deltas: 277, 224, 220, 228, 231, 216.
    Finished test of 3059 positions from Test set T_22xxVs11xx.cts. Evaluation deltas: 540, 350, 326, 318, 298, 342.
    Finished test of 3621 positions from Test set V_13xx.cts.       Evaluation deltas: 463, 303, 288, 289, 282, 287.
    Finished test of 3771 positions from Test set V_16xx.cts.       Evaluation deltas: 406, 282, 272, 279, 274, 272.
    Finished test of 5083 positions from Test set V_22xx.cts.       Evaluation deltas: 297, 240, 235, 248, 247, 233.
    Finished test of 3247 positions from Test set V_22xxVs11xx.cts. Evaluation deltas: 532, 332, 312, 298, 283, 318.
    (Cache has 28359 Entries and resulted in 2680069 hits.)
    Total Nr. of board evaluations: 30986   (nur noch 162x "*** Test abgebrochen wg. fehlerhaftem Zug ***")
    Thereof within limits: 80%

    2.10.2021:  after allowing king-pinned pieces to move, if the move still covers the king
    --> (1 min 8 sec)  (ohne DEBUGMSG_BOARD_MOVES+INIT, nur _TESTCASES: 55 sec)
    Finished test of 3883 positions from Test set T_13xx.cts.       Evaluation deltas: 450, 293, 278, 284, 275, 281.
    (Cache has 16289 Entries and resulted in 327449 hits.)
    Finished test of 4370 positions from Test set T_16xx.cts.       Evaluation deltas: 395, 284, 269, 270, 261, 272.
    Finished test of 5174 positions from Test set T_22xx.cts.       Evaluation deltas: 284, 227, 222, 231, 233, 219.
    Finished test of 3287 positions from Test set T_22xxVs11xx.cts. Evaluation deltas: 543, 349, 325, 318, 297, 341.
    Finished test of 4065 positions from Test set V_13xx.cts.       Evaluation deltas: 487, 311, 295, 296, 288, 294.
    Finished test of 4184 positions from Test set V_16xx.cts.       Evaluation deltas: 426, 289, 278, 285, 278, 281.
    Finished test of 5612 positions from Test set V_22xx.cts.       Evaluation deltas: 311, 246, 240, 253, 252, 239.
    Finished test of 3533 positions from Test set V_22xxVs11xx.cts. Evaluation deltas: 542, 336, 314, 302, 285, 323.
    (Cache has 28922 Entries and resulted in 2871649 hits.)
    Total Nr. of board evaluations: 34108  (nur noch 54x  "*** Test abgebrochen wg. fehlerhaftem Zug ***")
    Thereof within limits: 78% (alte Vergleichsrechnung)                             { 600, 400, 300, 300, 280, 300 };
    Thereof within limits: 86% (neue Vergleichsrechnung)                             { 600, 400, 350, 300, 280, 300 };

    2.10.2021:  after fixing en-passant distance-calculation, to allow the en-passant moves:
    parms: skip:10, min-pces:10
    --> (1 min 29 sec)  (without DEBUGMSG_BOARD_MOVES+INIT, nur _TESTCASES: 1 min 02 sec => 557/Sec)
    Finished test of 3912 positions from Test set T_13xx.cts.       Evaluation deltas: 450, 294, 279, 286, 276, 281.
    (Cache has 16422 Entries and resulted in 330254 hits.)
    Finished test of 4465 positions from Test set T_16xx.cts.       Evaluation deltas: 394, 284, 269, 271, 263, 271.
    Finished test of 5275 positions from Test set T_22xx.cts.       Evaluation deltas: 287, 228, 222, 233, 234, 220.
    Finished test of 3313 positions from Test set T_22xxVs11xx.cts. Evaluation deltas: 542, 348, 325, 317, 296, 340.
    Finished test of 4065 positions from Test set V_13xx.cts.       Evaluation deltas: 487, 311, 295, 296, 289, 294.
    Finished test of 4220 positions from Test set V_16xx.cts.       Evaluation deltas: 428, 291, 280, 286, 279, 282.
    Finished test of 5741 positions from Test set V_22xx.cts.       Evaluation deltas: 310, 247, 240, 253, 251, 240.
    Finished test of 3584 positions from Test set V_22xxVs11xx.cts. Evaluation deltas: 545, 336, 314, 303, 286, 323.
    (Cache has 29038 Entries and resulted in 2905027 hits.)
    Total Nr. of board evaluations: 34575 (nur noch 37x  "*** Test abgebrochen wg. fehlerhaftem Zug ***")
    Thereof within limits: 86% (neue Vergleichsrechnung)                             { 600, 400, 350, 300, 280, 300 };
    -
    same for only T_-files:  (34 sec w/o INIT+MOVE-debugmsgs)
    [...]
    Finished test of 3313 positions from Test set T_22xxVs11xx.cts. Evaluation deltas: 542, 348, 325, 317, 296, 340.
    (Cache has 24394 Entries and resulted in 1440584 hits.)
    Total Nr. of board evaluations: 16965
    Thereof within limits: 88%
    -
    same with parms: skip:10, min-pces:6  (32 sec w/o...)  -> 540/sec
    Finished test of 3316 positions from Test set T_22xxVs11xx.cts. Evaluation deltas: 542, 348, 325, 317, 296, 340.
    (Cache has 24394 Entries and resulted in 1448079 hits.)
    Total Nr. of board evaluations: 17421  (just 56 more...)
    Thereof within limits: 88%
    ----

    (29.7-30.8 Sec) --> 570 Evals/Sec (at .._NROF_HOPS == 6, nur T_)
    Testing Set T_13xx.cts: 44179 (981) 21312 (473) 20392 (453) 20591 (457) 19206 (426).
    Finished test of 4050 positions from Test set T_13xx.cts.
    Evaluation deltas:  game state: 446,  piece values: 295,  mobility: 287,  max.clashes: 273,  mobility + max.clash: 263.
Testing Set T_16xx.cts: 62741 (922) 23413 (344) 22501 (330) 21972 (323) 20581 (302).
    Finished test of 4574 positions from Test set T_16xx.cts.
    Evaluation deltas:  game state: 396,  piece values: 286,  mobility: 278,  max.clashes: 273,  mobility + max.clash: 261.
Testing Set T_22xx.cts: 4643 (43) 7886 (73) 8025 (75) 9010 (84) 9200 (85).
    Finished test of 5481 positions from Test set T_22xx.cts.
    Evaluation deltas:  game state: 290,  piece values: 229,  mobility: 224,  max.clashes: 217,  mobility + max.clash: 210.
Testing Set T_22xxVs11xx.cts: 12616 (1802) 3545 (506) 2997 (428) 3372 (481) 2718 (388).
    Cache has 24394 Entries and resulted in 1448373 hits.
    Finished test of 3316 positions from Test set T_22xxVs11xx.cts.
    Evaluation deltas:  game state: 542,  piece values: 348,  mobility: 336,  max.clashes: 327,  mobility + max.clash: 311.
Total Nr. of board evaluations: 17421
Thereof within limits: 90%
Quality of level mobility (2):  (same as basic piece value: 364)
  - improvements: 10385 (-25)
  - totally wrong: 6359 (19); - overdone: 313 (15)
Quality of level max.clashes (3):  (same as basic piece value: 11609)
  - improvements: 3697 (-147)
  - totally wrong: 1780 (122); - overdone: 335 (117)
Quality of level mobility + max.clash (4):  (same as basic piece value: 273)
  - improvements: 11029 (-70)
  - totally wrong: 5500 (48); - overdone: 619 (66)

    comparison with MAX_INT_NROF_HOPS==3 , only on 4 Testtests;
    (9.4 Sec.)  --> 1680 Evals/sec.
Testing Set T_13xx.cts:  44179 (981) 21312 (473) 20835 (463) 20616 (458) 19764 (439).
    Finished test of 3801 positions from Test set T_13xx.cts.
    Evaluation deltas:  game state: 438,  piece values: 290,  mobility: 283,  max.clashes: 272,  mobility + max.clash: 262.
Testing Set T_16xx.cts:  42440 (866) 16783 (342) 16416 (335) 15468 (315) 14816 (302).
    Finished test of 4068 positions from Test set T_16xx.cts.
    Evaluation deltas:  game state: 366,  piece values: 273,  mobility: 266,  max.clashes: 261,  mobility + max.clash: 251.
Testing Set T_22xx.cts:  4643 (43) 7886 (73) 7894 (73) 8980 (83) 9088 (84).
    Finished test of 4779 positions from Test set T_22xx.cts.
    Evaluation deltas:  game state: 276,  piece values: 219,  mobility: 215,  max.clashes: 209,  mobility + max.clash: 203.
Testing Set T_22xxVs11xx.cts:  12616 (1802) 3545 (506) 3075 (439) 3372 (481) 2812 (401).
    Finished test of 3152 positions from Test set T_22xxVs11xx.cts.
    Evaluation deltas:  game state: 519,  piece values: 344,  mobility: 333,  max.clashes: 324,  mobility + max.clash: 310.
Cache has 7904 Entries and resulted in 231127 hits.
Total Nr. of board evaluations: 15800
Thereof within limits: 90%
Quality of level mobility (2):  (same as basic piece value: 375)
  - improvements: 9137 (-25)
  - totally wrong: 5982 (19); - overdone: 306 (15)
Quality of level max.clashes (3):  (same as basic piece value: 10353)
  - improvements: 3416 (-147)
  - totally wrong: 1679 (129); - overdone: 352 (155)
Quality of level mobility + max.clash (4):  (same as basic piece value: 276)
  - improvements: 9734 (-72)
  - totally wrong: 5158 (51); - overdone: 632 (89)

    === new clash implementation (!) based on priority ques (no mir GlubschFish bit encoding, sorry)
    (28,3 Sec.)  --> 1250 Evals/Sec (with NROF_HOPS==6, T_+V_, no MOVES in debugprint)
    Testing Set T_13xx.cts:     Finished test of 4050 positions from Test set T_13xx.cts.   Evaluation deltas:  game state: 446,  piece values: 295,  mobility: 287,  max.clashes: 274,  mobility + max.clash: 263.
    Testing Set T_16xx.cts:     Finished test of 4574 positions from Test set T_16xx.cts.   Evaluation deltas:  game state: 396,  piece values: 286,  mobility: 278,  max.clashes: 273,  mobility + max.clash: 261.
    Testing Set T_22xx.cts:     Finished test of 5481 positions from Test set T_22xx.cts.   Evaluation deltas:  game state: 290,  piece values: 229,  mobility: 224,  max.clashes: 218,  mobility + max.clash: 211.
    Testing Set T_22xxVs11xx.:  Finished test of 3316 positions from Test set T_22xxVs11xx. Evaluation deltas:  game state: 542,  piece values: 348,  mobility: 336,  max.clashes: 327,  mobility + max.clash: 312.
    Testing Set V_13xx.cts:     Finished test of 4091 positions from Test set V_13xx.cts.   Evaluation deltas:  game state: 490,  piece values: 313,  mobility: 304,  max.clashes: 289,  mobility + max.clash: 277.
    Testing Set V_16xx.cts:     Finished test of 4287 positions from Test set V_16xx.cts.   Evaluation deltas:  game state: 426,  piece values: 289,  mobility: 282,  max.clashes: 276,  mobility + max.clash: 267.
    Testing Set V_22xx.cts:     Finished test of 5871 positions from Test set V_22xx.cts.   Evaluation deltas:  game state: 316,  piece values: 252,  mobility: 247,  max.clashes: 244,  mobility + max.clash: 237.
    Testing Set V_22xxVs11xx.:  Finished test of 3595 positions from Test set V_22xxVs11xx. Evaluation deltas:  game state: 545,  piece values: 337,  mobility: 325,  max.clashes: 315,  mobility + max.clash: 300.
    Total Nr. of board evaluations: 35265  (with 38 broken tests)
    Thereof within limits: 90%
    Quality of level mobility (2):  (same as basic piece value: 801)
      - improvements: 21132 (-25)
      - totally wrong: 12777 (19); - overdone: 555 (15)
    Quality of level max.clashes (3):  (same as basic piece value: 23574)
      - improvements: 7370 (-149)
      - totally wrong: 3659 (126); - overdone: 662 (129)
    Quality of level mobility + max.clash (4):  (same as basic piece value: 607)
      - improvements: 22232 (-70)
      - totally wrong: 11259 (50); - overdone: 1167 (74)

    === switch from priority queue to ArrayList (so that it remains persistant for further local evaluations:)
        + huge extra evaluations for all squares x all pieces, who can go where with which clash result
    (42,5 Sec.) -> 830 Evals/Sec
    [...] Testing Set T_22xx.cts:   Finished test of 5481 positions from Test set T_22xx.cts.       Evaluation deltas:  game state: 290,  piece values: 229,  mobility: 224,  max.clashes: 218,  mobility + max.clash: 211.
    Testing Set T_22xxVs11xx.cts:   Finished test of 3316 positions from Test set T_22xxVs11xx.cts. Evaluation deltas:  game state: 542,  piece values: 348,  mobility: 336,  max.clashes: 325,  mobility + max.clash: 309.
    Testing Set V_22xx.cts:         Finished test of 5871 positions from Test set V_22xx.cts.       Evaluation deltas:  game state: 316,  piece values: 252,  mobility: 247,  max.clashes: 244,  mobility + max.clash: 236.
    Testing Set V_22xxVs11xx.cts:   Finished test of 3595 positions from Test set V_22xxVs11xx.cts. Evaluation deltas:  game state: 545,  piece values: 337,  mobility: 325,  max.clashes: 313,  mobility + max.clash: 298.
    Total Nr. of board evaluations: 35265  (with 38 broken tests)
    Thereof within limits: 90%
    Quality of level mobility (2):  (same as basic piece value: 801)
      - improvements: 21132 (-25)
      - totally wrong: 12777 (19); - overdone: 555 (15)
    Quality of level max.clashes (3):  (same as basic piece value: 22530)
      - improvements: 7918 (-160)
      - totally wrong: 3977 (147); - overdone: 840 (139)
    Quality of level mobility + max.clash (4):  (same as basic piece value: 591)
      - improvements: 22043 (-78)
      - totally wrong: 11269 (59); - overdone: 1362 (85)

     === Geschwindigkeitsvergleich ohne die Deltaberechnungen, dafür nach jedem Move über den FEN-String ein neues chessboard
     (1 min 50 Sec) --> 324 Evals/Sec.  (bei MAX_NROF-HOPS==3 ist der Vergleich: 75 Sec zu 28 Sec)
     slower (but maybe not as much as expected). but only 20 errors "*** Test.."
     all "*** Test abgebrochen wg. fehlerhaftem Zug ***":
    Testing Set T_13xx.cts:
    - 8/8/8/1K1p4/6k1/8/PPB5/7p  b - - 1 48   **** Fehler: Fehlerhafter Zug: e4 -> d3 nicht möglich auf Board Pos 6.
    - r1bqkb1r/1p2pppp/5n2/p1p1n1N1/P2Pp3/1BP5/1P3PPP/RNBQ1RK1  b kq d3 0 9
    Testing Set T_16xx.cts:
    - **** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board Pos 13.
    - rnb1kqnr/5pb1/1pp1p2p/p2pP1p1/P2PB3/1PN1BN2/2PQ1PPP/R3K2R  w KQkq d6 0 13
    Testing Set T_22xx.cts:
    - **** Fehler: Fehlerhafter Zug: a5 -> b6 nicht möglich auf Board Pos 9.
    - 1rb1k2r/p1qn1pbp/2pp1np1/Pp2p3/3PP3/1BN1BN1P/1PP2PP1/R2QK2R  w KQk b6 0 11
    - **** Fehler: Fehlerhafter Zug: g5 -> f6 nicht möglich auf Board Pos 27.
    - r4rk1/2pqn1bp/6p1/2pPppP1/4P3/2NQ1N2/PP3P2/R3K1R1  w Q f6 0 20
    - **** Fehler: Fehlerhafter Zug: d5 -> e6 nicht möglich auf Board Pos 15.
    - rn1r4/1p1b2bk/p2p1npp/2pPpp2/2P1P3/2N3P1/PP3PBP/1RBQ1RK1  w - e6 0 14
    Testing Set T_22xxVs11xx.cts:
    - 5b1P/pp6/2kpBp2/2p2P2/2n5/4P3/PP2KP2/7R  w - - 1 33
    - **** Fehler: Fehlerhafter Zug: a4 -> b3 nicht möglich auf Board Pos 32.
    - r5k1/1p3pp1/2p2n1p/3pq3/pPPNr3/P3P2P/2Q2PP1/2RR2K1  b - b3 0 22
    Testing Set V_13xx.cts:
    - 8/8/7p/3k2p1/pP4P1/3K1P1P/P7/8  b - b3 0 41
    Testing Set V_16xx.cts:
    - **** Fehler: Fehlerhafter Zug: e4 -> f3 nicht möglich auf Board Pos 12.
    - r2q1rk1/pp2nppp/2n1b3/1BPpP3/4pP2/2N5/PP4PP/R1BQ1RK1  b - f3 0 12
    - **** Fehler: Fehlerhafter Zug: e5 -> f6 nicht möglich auf Board Pos 11.
    - r1bqnrk1/pp2b1pp/2p1p3/3pPp2/5B2/2P1P3/PPQNBPPP/2KR3R  w - f6 0 12
    Testing Set V_22xx.cts:
    - *** Fehler: Fehlerhafter Zug: d4 -> c3 nicht möglich auf Board Pos 36.
    - *** Test abgebrochen wg. fehlerhaftem Zug ***
    - 8/p3pp2/6p1/2N1kb1p/1PPp4/5P2/P2K2PP/8  b - c3 0 24
    - *** Test abgebrochen wg. fehlerhaftem Zug ***
    - 2P5/6kp/5pp1/1p1Pn3/2r4P/6P1/5PBK/8  w - - 1 39
    - **** Fehler: Fehlerhafter Zug: e5 -> f6 nicht möglich auf Board Pos 49.
    - *** Test abgebrochen wg. fehlerhaftem Zug ***
    - 2r3k1/6pp/3Bp3/3nPp2/8/K7/2PR3P/8  w - f6 0 31
    - **** Fehler: Fehlerhafter Zug: g5 -> h6 nicht möglich auf Board Pos 53.
    - *** Test abgebrochen wg. fehlerhaftem Zug ***
    - 7r/p1p3k1/1npp1Np1/4p1Pp/4P2R/P1NP1q2/1P6/K1B4R  w - h6 0 33
    - **** Fehler: Fehlerhafter Zug: f4 -> g3 nicht möglich auf Board Pos 18.
    - *** Test abgebrochen wg. fehlerhaftem Zug ***
    - r1b1r1k1/1pp1np1p/p2b4/3p4/3P1pP1/2PB1P2/PPN1NK1P/R6R  b - g3 0 15
    - **** Fehler: Fehlerhafter Zug: e4 -> f3 nicht möglich auf Board Pos 22.
    - *** Test abgebrochen wg. fehlerhaftem Zug ***
    - r1q2rk1/pb1n1ppp/1p2p3/4P1N1/3NpP2/4Q3/PPP3PP/3R1RK1  b - f3 0 17
    - **** Fehler: Fehlerhafter Zug: d5 -> c6 nicht möglich auf Board Pos 25.
    - *** Test abgebrochen wg. fehlerhaftem Zug ***
    - r1bq1r1k/4n1p1/p2p1n1p/1ppP4/8/1BN1PN1P/PP4P1/R2QR1K1  w - c6 0 19
    Testing Set V_22xxVs11xx.cts:
    - *** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board skipped.
    - *** Test abgebrochen wg. fehlerhaftem Zug ***
    - rnbr2k1/2p2ppp/p2b4/1p1pP3/8/2P1PN2/PP2BPPP/RN1QK2R  b KQ - 1 9
    - **** Fehler: Fehlerhafter Zug: g5 -> h6 nicht möglich auf Board Pos 9.
    - *** Test abgebrochen wg. fehlerhaftem Zug ***
    - r1bq1rk1/2pnnpb1/pp2p1p1/3p2Pp/1P1P3P/P1P1P3/4BP2/RNBQK1NR  w KQ h6 0 11
    Testing Set T_13xx.cts: Finished test of 4136 positions from Test set T_13xx.cts.   Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 289,  max.clashes: 275,  mobility + max.clash: 261,  new mobility: 299.
    Testing Set T_16xx.cts: Finished test of 4593 positions from Test set T_16xx.cts.   Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 272,  max.clashes: 267,  mobility + max.clash: 251,  new mobility: 288.
    Testing Set T_22xx.cts: Finished test of 5492 positions from Test set T_22xx.cts.   Evaluation deltas:  game state: 290,  piece values: 229,  basic mobility: 223,  max.clashes: 212,  mobility + max.clash: 204,  new mobility: 238.
    Testing Set T_22xxVs11xx.cts:   Finished test of 3378 positions from Test set T_22xxVs11xx.cts. Evaluation deltas:  game state: 540,  piece values: 345,  basic mobility: 326,  max.clashes: 322,  mobility + max.clash: 298,  new mobility: 332.
    Testing Set V_13xx.cts: Finished test of 4122 positions from Test set V_13xx.cts.   Evaluation deltas:  game state: 490,  piece values: 314,  basic mobility: 301,  max.clashes: 287,  mobility + max.clash: 270,  new mobility: 308.
    Testing Set V_16xx.cts: Finished test of 4348 positions from Test set V_16xx.cts.   Evaluation deltas:  game state: 430,  piece values: 289,  basic mobility: 280,  max.clashes: 270,  mobility + max.clash: 258,  new mobility: 291.
    Testing Set V_22xx.cts: Finished test of 5943 positions from Test set V_22xx.cts.   Evaluation deltas:  game state: 315,  piece values: 249,  basic mobility: 244,  max.clashes: 233,  mobility + max.clash: 225,  new mobility: 261.
    Testing Set V_22xxVs11xx.cts:   Finished test of 3611 positions from Test set V_22xxVs11xx.cts. Evaluation deltas:  game state: 550,  piece values: 340,  basic mobility: 321,  max.clashes: 315,  mobility + max.clash: 291,  new mobility: 330.
    Total Nr. of board evaluations: 35623
    Thereof within limits: 86%
    Quality of level basic mobility (2):  (same as basic piece value: 846)
    - improvements: 20521 (-42)
    - totally wrong: 13173 (34); - overdone: 1083 (27)
    Quality of level max.clashes (3):  (same as basic piece value: 24219)
    - improvements: 7594 (-145)
    - totally wrong: 3269 (96); - overdone: 541 (101)
    Quality of level mobility + max.clash (4):  (same as basic piece value: 632)
    - improvements: 22071 (-86)
    - totally wrong: 11285 (50); - overdone: 1635 (49)
    Quality of level new mobility (5):  (same as basic piece value: 515)
    - improvements: 15540 (-68)
    - totally wrong: 17731 (58); - overdone: 1837 (40)

     */
    @Test
    void boardEvaluation_Test() {
        String[] testSetFiles = {
            "T_13xx.cts" , "T_16xx.cts",
            "T_22xx.cts", "T_22xxVs11xx.cts"
            // , "V_13xx.cts", "V_16xx.cts", "V_22xx.cts", "V_22xxVs11xx.cts"
        };
        int[] expectedDeltaAvg = { 600, 400, 350, 300, 300, 280, 300, 300, 280 };
        countNrOfBoardEvals = 0;
        int overLimit = 0;
        for ( String ctsFilename: testSetFiles ) {
            System.out.println();
            System.out.println("Testing Set " + ctsFilename + ": ");
            int[] evalDeltaAvg = boardEvaluation_Test_Set(ctsFilename);
            // check the result of every insight-level for this test-set
            System.out.print("Evaluation deltas: " );
            for (int i = 0; i<ChessBoard.EVAL_INSIGHT_LEVELS; i++) {
                System.out.print(" " + getEvaluationLevelLabel(i) + ": "  + evalDeltaAvg[i] + ((i<ChessBoard.EVAL_INSIGHT_LEVELS -1) ? ", " : "") );
                if ( evalDeltaAvg[i] > expectedDeltaAvg[i] || evalDeltaAvg[i] < -expectedDeltaAvg[i] )
                    overLimit++;
            }
            System.out.println(".");
        }
        System.out.println("Total Nr. of board evaluations: "+ countNrOfBoardEvals);
        System.out.println("Thereof within limits: "+ (100-(overLimit*100)/(testSetFiles.length* EVAL_INSIGHT_LEVELS))+"%");
        for (int i=2; i<EVAL_INSIGHT_LEVELS;i++) {
            System.out.print("Quality of level " + getEvaluationLevelLabel(i) + " ("+i+"): ");
            System.out.println(" (same as basic piece value: " + countEvalSame[i] +")");
            System.out.println("  - improvements: " + countEvalRightTendency[i] + " (" + (countEvalRightTendency[i]<=0?"-":sumEvalRightTendency[i]/countEvalRightTendency[i]) + ")");
            System.out.print("  - totally wrong: " + countEvalWrongTendency[i] + " (" + (countEvalWrongTendency[i]<=0?"-":sumEvalWrongTendency[i]/countEvalWrongTendency[i]) + ")");
            System.out.println("; - overdone: " + countEvalRightTendencyButTooMuch[i] + " (" + (countEvalRightTendencyButTooMuch[i]<=0?"-":sumEvalRightTendencyButTooMuch[i]/countEvalRightTendencyButTooMuch[i]) + ")");
        }

        // value in assertion is kind of %age of how many sets*InsightLevels where not fulfilled
        // 25.9. -> accepting deviation of 25.1% from { 500, 400, 300, 300, 280 } as a baseline for the current evaluation capabilities
        assertEquals(100.0f, 100-(overLimit*100.0)/(testSetFiles.length* EVAL_INSIGHT_LEVELS), 25.1);
        //assertTrue( countNrOfBoardEvals>220000 );
    }

    private int[] boardEvaluation_Test_Set(String ctsFilename) {
        int[] evalDeltaSum = new int[ChessBoard.EVAL_INSIGHT_LEVELS];

        //Read file, iterate over testgames in there
        int testedPositionsCounter = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(TESTSETS_PATH + ctsFilename))) {
            String line;
            // read the contents of the file line per line = game per game
            while ((line = br.readLine()) != null) {
                testedPositionsCounter += boardEvaluation_Test_testOneGame(line,
                        evalDeltaSum,
                        (testedPositionsCounter==0) );
            }
        } catch (IOException e) {
            System.out.println("Error reading file "+ctsFilename);
            e.printStackTrace();
        }
        // devide all sums by nr of positions evaluated
        if (testedPositionsCounter>0) {
            for (int i = 0; i < ChessBoard.EVAL_INSIGHT_LEVELS; i++)
                evalDeltaSum[i] /= testedPositionsCounter;
        }
        countNrOfBoardEvals += testedPositionsCounter;
        System.out.println();
        System.out.println("Finished test of "+testedPositionsCounter+" positions from Test set "+ctsFilename+".");
        return evalDeltaSum;
    }

    /**
     * iterates over moves in one game and compares the delta of the evaluation with the eval in the game-string
     * it skips SKIP_OPENING_MOVES number of moves at the beginning
     * and stops as soon as the expected eval is >2000 (because implementation does not yet cover mate scenarios)
     * and stops if less then 10 pieces are on the board (whish is not the focus os the algorithm at the moment)
     * @param ctsOneGameLine - String: something like "1. e4 0.24 1... c5 0.32 Nf3 0.0 2... Nf6 0.44"
     * @param totalEvalDeltaSum int[]: to add of the evaluation deltas for each level to
     * @return nr of testes positions
     */
    private int boardEvaluation_Test_testOneGame(final String ctsOneGameLine, int[] totalEvalDeltaSum, boolean debugOutput) {
        // begin with start postition
        ChessBoard chessBoard = new ChessBoard(
                "Testboard " + ctsOneGameLine.substring(0,min(25,ctsOneGameLine.length()))+"...",
                FENPOS_INITIAL);
        ChessGameReader cgr = new ChessGameReader(ctsOneGameLine);
        int[] evalDeltaSum = new int[EVAL_INSIGHT_LEVELS];
        // skip evaluation of some moves by just making the moves
        for (int i = 0; i < SKIP_OPENING_MOVES && cgr.hasNext(); i++) {
            String move = cgr.getNextMove();
            chessBoard.doMove(move);
            // Test: full Board reconstruction in new position, instead of evolving evaluations per move (just to compare speed)
            // -> also needs deactivation of recalc eval in doMove-methods in ChessBoard(!)
            //debugPrintln(1, chessBoard.getBoardFEN());
            //-->chessBoard = new ChessBoard("skipped", chessBoard.getBoardFEN());

            /* // Compare with freshly created board from same fenString
            ChessBoard freshBoard = new ChessBoard("Test Skip "+move, chessBoard.getBoardFEN());
            assertTrue(chessBoard.equals(freshBoard)); */

            cgr.getNextEval();
        }

        // while über alle Züge in der partie
        int testedPositionsCounter = 0;
        boolean moveValid=true;
        while( cgr.hasNext() ) {
            moveValid=chessBoard.doMove(cgr.getNextMove());
            if (!moveValid || chessBoard.getPieceCounter()<MIN_NROF_PIECES)
                break;

            // Test: full Board reconstruction in new position, instead of evolving evaluations per move (just to compare speed)
            // -> also needs deactivation of recalc eval in doMove-methods in ChessBoard(!)
            //debugPrintln(1, chessBoard.getBoardFEN());
            //-->chessBoard = new ChessBoard("Pos "+testedPositionsCounter, chessBoard.getBoardFEN());

            /*// Compare with freshly created board from same fenString
            ChessBoard freshBoard = new ChessBoard("Test Pos "+testedPositionsCounter, chessBoard.getBoardFEN());
            assertTrue(chessBoard.equals(freshBoard)); */

            int expectedEval = cgr.getNextEval();
            if (expectedEval==OPPONENT_IS_CHECKMATE)
                expectedEval = isWhite(chessBoard.getTurnCol()) ? BLACK_IS_CHECKMATE : WHITE_IS_CHECKMATE;
            if (debugOutput)
                debugPrint(DEBUGMSG_BOARD_MOVES, "  expected="+expectedEval+" ?= evaluated:");
            if (abs(expectedEval)>2000)
                break;
            testedPositionsCounter++;
            int basicPieceValueDeviation=0;
            for (int i = 0; i < EVAL_INSIGHT_LEVELS; i++) {
                int eval = chessBoard.boardEvaluation(i );
                int delta = eval - expectedEval;
                evalDeltaSum[i] += abs(delta);
                if (i==1) {  // basic piece value sum
                    basicPieceValueDeviation = delta;
                }
                if (i>1) {
                    if ( abs(delta)==abs(basicPieceValueDeviation) ) {
                        countEvalSame[i]++;
                    } else if ( abs(delta)<abs(basicPieceValueDeviation) ) {
                        countEvalRightTendency[i]++;
                        sumEvalRightTendency[i] += abs(delta)-abs(basicPieceValueDeviation);
                    } else if ( delta>0 && basicPieceValueDeviation<0
                            || delta<0 && basicPieceValueDeviation>0) {
                        countEvalRightTendencyButTooMuch[i]++;
                        sumEvalRightTendencyButTooMuch[i] += abs(delta)-abs(basicPieceValueDeviation);
                    } else {
                        countEvalWrongTendency[i]++;
                        sumEvalWrongTendency[i] += abs(delta)-abs(basicPieceValueDeviation);
                    }
                }
                if (debugOutput)
                    debugPrint(DEBUGMSG_BOARD_MOVES, "  "+ eval + " ("+delta+")");
            }
            if (debugOutput)
                debugPrintln(DEBUGMSG_BOARD_MOVES, ".");
        }
        if (testedPositionsCounter>0) {
            //debugPrint(DEBUGMSG_TESTCASES, " : " + testedPositionsCounter + " evals. ");
            for (int i = 0; i < EVAL_INSIGHT_LEVELS; i++) {
                if (debugOutput)
                    debugPrint(DEBUGMSG_TESTCASES, " " + evalDeltaSum[i] + " (" + evalDeltaSum[i] / testedPositionsCounter + ")");
                totalEvalDeltaSum[i] += evalDeltaSum[i];
            }
            if (debugOutput)
                debugPrintln(DEBUGMSG_TESTCASES, ".");
        }

        if (!chessBoard.isGameOver() && !moveValid) {
            System.out.println(" *** Test abgebrochen wg. fehlerhaftem Zug ***");
            System.out.println(chessBoard.getBoardFEN());
        }
        return testedPositionsCounter;
    }

    @Test
    public void boardEvaluation_Simple_Test() {
        boardEvaluation_SingleBoard_Test( FENPOS_INITIAL, 0, 50);
        boardEvaluation_SingleBoard_Test( FENPOS_EMPTY, 0, 10);
    }

    private void boardEvaluation_SingleBoard_Test(String fen, int expectedEval, int tolerance) {
        int[] evalDeltaSum = new int[EVAL_INSIGHT_LEVELS];
        //TODO: Read file, iterate over test-boards in there
        ChessBoard chessBoard = new ChessBoard("Test " + fen, fen );
        boardEvaluation_SingleBoard_Test(chessBoard, expectedEval, tolerance);
    }

    private void boardEvaluation_SingleBoard_Test(ChessBoard chessBoard, int expectedEval, int tolerance) {
        debugPrintln(DEBUGMSG_TESTCASES, "Testing " + chessBoard.getShortBoardName() );
        int overLimit = 0;
        for (int i = 0; i<ChessBoard.EVAL_INSIGHT_LEVELS; i++) {
            int eval = chessBoard.boardEvaluation(i);
            debugPrintln(DEBUGMSG_TESTCASES, "Evaluation " + getEvaluationLevelLabel(i) + "(" + i + ") is: " + eval + " -> delta: " + (eval- expectedEval) );
            if ( i>0 && abs( eval - expectedEval) > tolerance)
                overLimit++;
        }
        assertTrue(overLimit==0 );
    }

    @Test
    void doMove_String_Test() {
        // Test 4
        ChessBoard chessBoard = new ChessBoard("MoveTest4 " , FENPOS_INITIAL );
        assertEquals(32, chessBoard.getPieceCounter() );
        // check Knight distance calc after moveing
        final int knightW1Id = chessBoard.getPieceIdAt(coordinateString2Pos("b1"));
        final int pawnBdId = chessBoard.getPieceIdAt(coordinateString2Pos("d7"));
        final int d5 = coordinateString2Pos("d5");

        assertTrue( chessBoard.doMove("Nc3") );                             // WHITE Nb1c3
        checkUnconditionalDistance( 1, chessBoard,d5,knightW1Id);
        checkUnconditionalDistance( 1, chessBoard,d5,knightW1Id);
        // and also check the pawns basic movement
        checkUnconditionalDistance( 1, chessBoard,d5+UP,pawnBdId);
        checkCondDistance( 2, chessBoard, d5+LEFT,pawnBdId);
        checkUnconditionalDistance( 1, chessBoard,d5,pawnBdId);

        assertTrue(     chessBoard.doMove("d5"));                           // BLACK d7d5
        checkUnconditionalDistance( INFINITE_DISTANCE, chessBoard,d5+UP,pawnBdId);
        assertEquals( INFINITE_DISTANCE, chessBoard.getDistanceToPosFromPieceId(d5+LEFT,pawnBdId));
        checkUnconditionalDistance( 0, chessBoard,d5,pawnBdId);

        // go on with Knight
        assertTrue( chessBoard.doMove("Nb5") );
        checkUnconditionalDistance( 2, chessBoard,d5,knightW1Id);
        // -->  "
        assertEquals(32, chessBoard.getPieceCounter() );
        boardEvaluation_SingleBoard_Test( chessBoard,  0,  135);


        // Test 3
        chessBoard = new ChessBoard("MoveTest3 " , FENPOS_INITIAL );
        assertEquals(32, chessBoard.getPieceCounter() );
        // check Rook distance calc after moveing
        final int rookB1Id = chessBoard.getPieceIdAt(0);
        final int a3 = coordinateString2Pos("a3");
        final int a4 = coordinateString2Pos("a4");
        assertFalse( chessBoard.isDistanceToPosFromPieceIdUnconditional(a4,rookB1Id));
        assertEquals( 2, chessBoard.getDistanceToPosFromPieceId(a4,rookB1Id));
        assertFalse( chessBoard.isDistanceToPosFromPieceIdUnconditional(a3,rookB1Id));
        assertEquals( 2, chessBoard.getDistanceToPosFromPieceId(a3,rookB1Id));
        assertTrue( chessBoard.doMove("d4") );
        assertFalse( chessBoard.isDistanceToPosFromPieceIdUnconditional(a3,rookB1Id));
        assertEquals( 2, chessBoard.getDistanceToPosFromPieceId(a3,rookB1Id));
        assertFalse( chessBoard.isDistanceToPosFromPieceIdUnconditional(a4,rookB1Id));
        assertEquals( 2, chessBoard.getDistanceToPosFromPieceId(a4,rookB1Id));
        assertTrue( chessBoard.doMove("a5"));
        //if (MAX_INTERESTING_NROF_HOPS>3)
        //   assertEquals( 4, chessBoard.XXXgetShortestUnconditionalDistanceToPosFromPieceId(a4,rookB1Id));
        assertFalse( chessBoard.isDistanceToPosFromPieceIdUnconditional(a4,rookB1Id));
        assertEquals( 2, chessBoard.getDistanceToPosFromPieceId(a4,rookB1Id));
        assertTrue( chessBoard.doMove("b4") );
        assertFalse( chessBoard.isDistanceToPosFromPieceIdUnconditional(a4,rookB1Id));
        assertEquals( 2, chessBoard.getDistanceToPosFromPieceId(a4,rookB1Id));
        // -->  "
        assertEquals(32, chessBoard.getPieceCounter() );
        boardEvaluation_SingleBoard_Test( chessBoard,  0,  120);

        // Test 1
        chessBoard = new ChessBoard("MoveTest " , FENPOS_INITIAL );
        assertEquals(32, chessBoard.getPieceCounter() );
        assertTrue( chessBoard.doMove("e4")       );
        assertEquals(32, chessBoard.getPieceCounter() );
        // check pawn distance calc after moveing
        assertEquals( INFINITE_DISTANCE,chessBoard.getDistanceToPosFromPieceId(
                coordinateString2Pos("d3"),20));
        assertEquals( INFINITE_DISTANCE,chessBoard.getDistanceToPosFromPieceId(
                coordinateString2Pos("d4"),20));
        checkUnconditionalDistance(1,chessBoard, coordinateString2Pos("e5"),20);
        checkCondDistance(1,chessBoard, coordinateString2Pos("d5"),20);
        checkCondDistance(1,chessBoard,coordinateString2Pos("f5"),20);
        checkCondDistance( INFINITE_DISTANCE,chessBoard, coordinateString2Pos("g5"),20);

        int knightB1Id = 1;
        checkUnconditionalDistance(2,chessBoard,coordinateString2Pos("e5"),knightB1Id);
        assertTrue(     chessBoard.doMove("e5")   );
        checkCondDistance(3,chessBoard,coordinateString2Pos("e5"),knightB1Id);
        assertTrue( chessBoard.doMove("d4")       );
        assertTrue(     chessBoard.doMove("exd4") );
        assertEquals(31, chessBoard.getPieceCounter() );
        checkUnconditionalDistance(2,chessBoard,coordinateString2Pos("e5"),knightB1Id);
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
        debugPrintln(DEBUGMSG_TESTCASES, chessBoard.getBoardFEN() );
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
        assertEquals("3r1b1r/1k3qpp/1ppp1n2/p3n3/P3P3/B1N2N1Q/5PPP/1R2R1K1  w - - 4 19",
                newFen);
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
        //Here it is detected if there is a problem with the king-pin solution.
        assertEquals(KNIGHT, chessBoard.getPieceTypeAt(coordinateString2Pos("f3")));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("g1")));
        assertEquals(KNIGHT, chessBoard.getPieceTypeAt(coordinateString2Pos("d2")));
        assertTrue(     chessBoard.doMove("O-O?"));
        assertTrue( chessBoard.doMove("Be2?!"));

        assertTrue(     chessBoard.doMove("Be7?!"));
        assertTrue( chessBoard.doMove("O-O"));
        assertTrue(     chessBoard.doMove("Qd8??"));
        // check if queen has moved correctly
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("a5")));
        assertEquals(QUEEN_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("d8")));
        assertTrue( chessBoard.doMove("Nxe4"));
        assertTrue(     chessBoard.doMove("d5"));
        assertTrue( chessBoard.doMove("cxd5"));
        assertTrue(     chessBoard.doMove("cxd5"));
        assertTrue( chessBoard.doMove("Nc3"));
        assertTrue(     chessBoard.doMove("f6"));
        assertTrue( chessBoard.doMove("Re1"));
        assertTrue(     chessBoard.doMove("fxe5"));
        assertTrue( chessBoard.doMove("Bxe5"));
        assertTrue(     chessBoard.doMove("Nc6"));
        assertTrue( chessBoard.doMove("Bg3"));
        assertTrue(     chessBoard.doMove("Qa5"));
        assertTrue( chessBoard.doMove("Qd2"));
        assertTrue(     chessBoard.doMove("e5"));
        assertTrue( chessBoard.doMove("dxe5"));
        assertTrue(     chessBoard.doMove("Nxe5"));
        debugPrintln(DEBUGMSG_TESTCASES, chessBoard.getBoardFEN() );
        assertTrue( chessBoard.doMove("Nxe5"));
        assertTrue(     chessBoard.doMove("Qb6"));
        assertTrue( chessBoard.doMove("Nxd5"));
        assertTrue(     chessBoard.doMove("Qe6"));
        assertTrue( chessBoard.doMove("Nxe7+"));
        assertTrue(     chessBoard.doMove("Qxe7"));
        assertTrue( chessBoard.doMove("Bc4+"));
        assertTrue(     chessBoard.doMove("Be6"));
        assertTrue( chessBoard.doMove("Bxe6+"));
        assertTrue(     chessBoard.doMove("Qxe6"));
        assertTrue( chessBoard.doMove("Nd7"));
        // -->  "r4rk1/pp1N2pp/4q3/8/8/6B1/PP1Q1PPP/R3R1K1  b - - 1 23"
        // piece value sum == +710, but real evaluation is much better for white
        boardEvaluation_SingleBoard_Test( chessBoard,  1050,  450);
    }

    @Test
    void isPinnedByKing_Test() {
        ChessBoard board = new ChessBoard("PinnedKingTestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int kingWpos = A1SQUARE;
        int kingWId = board.spawnPieceAt(KING,kingWpos);
        int knightW1pos = kingWpos+2*UP;
        int knightW1Id = board.spawnPieceAt(KNIGHT,knightW1pos);
        board.completeDistanceCalc();

        // the knight can move to the king in 2 hops + the king must go away = 3
        assertFalse( board.isDistanceToPosFromPieceIdUnconditional(kingWpos,knightW1Id));
        assertEquals( 3, board.getDistanceToPosFromPieceId(kingWpos,  knightW1Id));
        // then it can move freely
        boolean legalMove = board.doMove("Nc2");
        assertTrue(legalMove);
        // we need a black piece to move, so the knight can move back,,
        int pawnB1Id = board.spawnPieceAt(PAWN_BLACK,15);
        board.completeDistanceCalc();
        assertTrue(board.doMove("h5"));
        //and move night back
        legalMove = board.doMove("Na3");
        assertTrue(legalMove);
        assertTrue(board.doMove("h4"));

        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5  t ░░░   ░░░   ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3  N ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1  K ░░░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */

        // but then the rook pins the knight to the king
        int rookB1pos = knightW1pos+2*UP;
        int rookB1Id = board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        board.completeDistanceCalc();
        // dist. to knight should be easy
        assertEquals( 1, board.getDistanceToPosFromPieceId(knightW1pos,  rookB1Id));
        //assertEquals( 1, board.XXXgetShortestUnconditionalDistanceToPosFromPieceId(knightW1pos,  rookB1Id));
        // but now test distances to kingW again...
        //assertEquals( 2, board.XXXgetShortestUnconditionalDistanceToPosFromPieceId(kingWpos,   rookB1Id)); // take knight + go to king...
        assertEquals( 1, board.getDistanceToPosFromPieceId(kingWpos,     rookB1Id)); // under the condition that knight moves away
        assertEquals( coordinateString2Pos("a3"),
                board.getDistanceFromPieceId(kingWpos,     rookB1Id).getFromCond(0)); // under the condition that knight moves away
        // if this all works, then the final test: moving the knight away must be an illegal move.
        legalMove = board.doMove("Nc2");
        assertFalse(legalMove);
    }

    @Test
    void doMove_isPinnedByKing_Test() {
        ChessBoard board = new ChessBoard("OnRookISPinnedTestBoard",
                "3q4/5pk1/p6p/3nr3/3Q4/7P/Pr3PP1/3R1RK1  b - - 1 25");
        //both rooks can move there, but one is king pinned
        /*
        8    ░░░   ░q░   ░░░   ░░░
        7 ░░░   ░░░   ░░░ p ░k░
        6  p ░░░   ░░░   ░░░   ░p░
        5 ░░░   ░░░ n ░r░   ░░░
        4    ░░░   ░Q░   ░░░   ░░░
        3 ░░░   ░░░   ░░░   ░░░ P
        2  P ░r░   ░░░ * ░P░ P ░░░
        1 ░░░   ░░░ R ░░░ R ░K░
           a  b  c  d  e  f  g  h    */
        int e2 = coordinateString2Pos("e2");
        int b2 = coordinateString2Pos("b2");
        int e5 = coordinateString2Pos("e5");
        assertEquals( EMPTY,board.getPieceTypeAt(e2));
        assertEquals( ROOK_BLACK,board.getPieceTypeAt(e5));
        assertEquals( ROOK_BLACK,board.getPieceTypeAt(b2));
        assertTrue(board.doMove("Re2"));
        assertEquals( ROOK_BLACK,board.getPieceTypeAt(e2));
        assertEquals( ROOK_BLACK,board.getPieceTypeAt(e5));
        assertEquals( EMPTY,board.getPieceTypeAt(b2));
    }


    //@Test
    void priotityQueue_Test() {
        /*  für Erik
        int meinwert = 10;
        while (true) {
            meinwert = meinwert + 1;
            if (meinwert==13)
                break;
            System.out.println("mein Wert ist " + meinwert);
        }
        System.out.println("mein Wert am Ende ist " + meinwert);
        */
        // sorry, not a real test, just to improve my understanding on how it behaves
        class PrItem implements Comparable<PrItem> {
            int value;
            PrItem(int v) {
                value = v;
            }
            @Override
            public String toString() {
                return "PrItem{" +
                        "value=" + value +
                        "} ";
            }
            @Override
            public int compareTo(@NotNull PrItem prItem) {
                if (prItem.value==this.value)
                    return 0;
                return this.value>prItem.value ? 1 : -1;
            }
        }
        List<PrItem> prItemList = new ArrayList<>();
        prItemList.add(new PrItem(5));
        prItemList.add(new PrItem(2));
        prItemList.add(new PrItem(8));
        PriorityQueue<PrItem> pq = new PriorityQueue<>(prItemList);
        pq.add(new PrItem(4));
        pq.add(new PrItem(9));
        pq.add(new PrItem(1));
        System.out.print("Iterator: ");
        for (PrItem pi : pq) {
            System.out.print(pi);
        }
        System.out.println(".");
        System.out.print("polls: ");
        while(!pq.isEmpty()) {
            PrItem pi = pq.poll();
            System.out.println(".");
            System.out.print("Polled " + pi + " remains: ");
            for (PrItem ipi : pq) {
                System.out.print(ipi);
            }
        }
        System.out.println(".");
    }
}


