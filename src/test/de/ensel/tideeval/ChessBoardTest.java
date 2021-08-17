/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.NOPIECE;
import static de.ensel.tideeval.VirtualPieceOnSquare.DISTANCE_NOT_SET;
import static org.junit.jupiter.api.Assertions.*;


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
        int rookW2pos = 62;
        int rookB1pos = 1;
        board.spawnPieceAt(ROOK,rookW1pos);
        board.spawnPieceAt(ROOK,rookW2pos);
        board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        /*
        8 ░░░ r1░D░   ░░░   ░░░
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
        int rookW2Id = board.getPieceIdAt(rookW2pos);
        int rookB1Id = board.getPieceIdAt(rookB1pos);
        assertEquals( pieceColorAndName(ROOK),       board.getPieceFullName(rookW1Id));
        assertEquals( pieceColorAndName(ROOK),       board.getPieceFullName(rookW2Id));
        assertEquals( pieceColorAndName(ROOK_BLACK), board.getPieceFullName(rookB1Id));
        // nothing there (see "x")
        assertEquals( null, board.getPieceAt(rookW1pos+RIGHT) );
        assertEquals( NOPIECE, board.getPieceIdAt(rookW1pos+RIGHT) );
        assertEquals( NOPIECE, board.getPieceIdAt(rookW2pos+LEFT) );
        assertEquals( NOPIECE, board.getPieceIdAt(rookW2pos+RIGHT) );
        assertEquals( NOPIECE, board.getPieceIdAt(rookB1pos+DOWN) );
        // test distances to pieces stored at squares
        // dist from rookW1
        assertEquals( 0, board.getDistanceAtPosFromPieceId(rookW1pos,         rookW1Id));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(rookW1pos+RIGHT,   rookW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookW1pos+UPRIGHT, rookW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookW1pos+UPRIGHT+UP,  rookW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookW1pos+UPRIGHT+2*UP,rookW1Id));
        // dist from rookW2
        assertEquals( 0, board.getDistanceAtPosFromPieceId(rookW2pos,         rookW2Id));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(rookW2pos+RIGHT,   rookW2Id));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(rookW2pos+2*LEFT,  rookW2Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookW2pos+2*UPLEFT,rookW2Id));
        // these distances only work, when other own piece is moving away
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookW2pos, rookW1Id));
        //TODO: needs update instead of init-implementation:
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookW2pos+RIGHT,   rookW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookW1pos, rookW2Id));
        // at square D
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookB1pos+RIGHT,rookW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookB1pos+RIGHT,rookW2Id));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(rookB1pos+RIGHT,rookB1Id));
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
        assertEquals( 2, board.getDistanceAtPosFromPieceId(bishopB2pos+UP,   rookW1Id));  // still 2
        assertEquals( 2, board.getDistanceAtPosFromPieceId(bishopB1pos+RIGHT,rookW1Id));  // still 2
        assertEquals( 3, board.getDistanceAtPosFromPieceId(bishopB1pos+LEFT, rookW1Id));  // increased to 3
        assertEquals( 2, board.getDistanceAtPosFromPieceId(bishopB1pos,      rookW1Id));  // still 2, by taking bishop
        // dist from rookW2
        assertEquals( 2, board.getDistanceAtPosFromPieceId(bishopB2pos+UP,   rookW2Id));  // still 2
        assertEquals( 2, board.getDistanceAtPosFromPieceId(bishopB1pos+RIGHT,rookW2Id));  // still 2
        assertEquals( 3, board.getDistanceAtPosFromPieceId(bishopB1pos+LEFT, rookW2Id));  // increased to 3
        assertEquals( 2, board.getDistanceAtPosFromPieceId(bishopB1pos,      rookW2Id));  // still 2, by taking bishop
        // dist from rookB1
        assertEquals( 2, board.getDistanceAtPosFromPieceId(bishopB1pos+RIGHT,rookB1Id));  // increased to 2, after moving bishop
        // dist from bishopB1
        assertEquals( DISTANCE_NOT_SET, board.getDistanceAtPosFromPieceId(bishopB1pos+RIGHT,bishopB1Id));  // wrong square color
        assertEquals( 2, board.getDistanceAtPosFromPieceId(bishopB1pos+4*LEFT,      bishopB1Id));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(bishopB1pos+3*DOWNRIGHT, bishopB1Id));
        // dist from bishopB2
        assertEquals( DISTANCE_NOT_SET, board.getDistanceAtPosFromPieceId(bishopB1pos+2*RIGHT,bishopB2Id));  // wrong square color
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookB1pos, bishopB2Id));  //  2, but only after moving rook away
        assertEquals( 2, board.getDistanceAtPosFromPieceId(rookW1pos, bishopB2Id));  // still 2, but only after moving bishop

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
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookW1Id));
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*3*/  bishopB1pos+LEFT, rookW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,      rookW1Id));
        // dist from rookW2
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW2Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookW2Id));
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*3*/  bishopB1pos+LEFT, rookW2Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,      rookW2Id));
        // dist from rookB1
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookB1Id));  // now 3, (way around or king+bishop move away)
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,      rookB1Id));  // now 3, but only after moving king and bishop
        // dist from bishopB1
        assertEquals( DISTANCE_NOT_SET, board.getDistanceAtPosFromPieceId(/*2*/ bishopB1pos+RIGHT,bishopB1Id));  // wrong square color
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*4*/ bishopB1pos+4*LEFT,      bishopB1Id));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(/*5*/ bishopB1pos+3*DOWNRIGHT, bishopB1Id));
        // dist from bishopB2
        assertEquals( DISTANCE_NOT_SET, board.getDistanceAtPosFromPieceId(/*4*/ bishopB1pos+4*LEFT,bishopB2Id));  // wrong square color
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*R1*/ rookW1pos, bishopB2Id));  // now 3, after taking K or moving around
        // dist from KingW
        assertEquals( 4, board.getDistanceAtPosFromPieceId(/*1*/  bishopB2pos+UP,   kingWId));
        assertEquals( 5, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,kingWId));
        assertEquals( 5, board.getDistanceAtPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingWId));
        // dist from KingB
        assertEquals( 1, board.getDistanceAtPosFromPieceId(/*1*/  bishopB2pos+UP,   kingBId));
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,kingBId));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));

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
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookW1Id));
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*3*/  bishopB1pos+LEFT, rookW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,      rookW1Id));
        // dist from rookW2 - unverändert
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW2Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookW2Id));
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*3*/  bishopB1pos+LEFT, rookW2Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,      rookW2Id));
        // dist from rookB1
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookB1Id));  //  3, but only by way around
        assertEquals( 4, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,      rookB1Id));  // 4 after moving king, queen and bishop, or on way around+moving bishop
        // dist from bishopB2
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*R1*/ rookW1pos, bishopB2Id));  // now 3, after moving around K and taking Q
        // dist from KingB
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,kingBId));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));  // only after moving q away

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
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*1*/  bishopB2pos+UP,   rookW1Id));  // now 3
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,      rookW1Id));
        // dist from rookB1
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookB1Id));  //  3, but only by way around
        assertEquals( 4, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,      rookB1Id));  // 4 after moving king, queen and bishop, or on way around+moving bishop
        // dist from bishopB2
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*R1*/ rookW1pos, bishopB2Id));  // now 3, after moving around K and taking Q
        // dist from KingB
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,kingBId));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));  // only after moving q away
        // dist from N
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*5*/  bishopB1pos+3*DOWNRIGHT,knightWId));
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*3*/  A1SQUARE, knightWId));  // only after moving q away
        // dist from n
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,knightBId));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(/*3*/  bishopB2pos+RIGHT, knightBId));  // only after moving q away

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
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*b1*/ bishopB1pos,      rookW1Id));   // now 3, via a5 - TODO: testcase via pW1 has to move away, but can't because can only move straight...
        // dist from rookB1
        assertEquals( 4, board.getDistanceAtPosFromPieceId(/*2*/  bishopB1pos+RIGHT,rookB1Id));  //  now 4
        // dist from bishopB1
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*5*/  bishopB1pos+3*DOWNRIGHT, bishopB1Id));  // now 3, after moving both pB or moving around
        // dist from pW1 -> "."
        assertEquals( 0, board.getDistanceAtPosFromPieceId(/*.*/  pW1pos,pW1Id));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(/*.*/  pW1pos+UP,pW1Id));
        assertEquals( 1, board.getDistanceAtPosFromPieceId(/*.*/  pW1pos+2*UP,pW1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*.*/  pW1pos+3*UP,pW1Id));
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*.*/  knightWpos, pW1Id));  // knight needs to walk away // TODO: PAWN cannot take towards an empty square... so actuiallo correct is D_NOT-SET
        assertEquals( DISTANCE_NOT_SET, board.getDistanceAtPosFromPieceId(/*.*/  knightWpos+2*LEFT, pW1Id));  // not reachable
        // dist from pW2 -> "."
        assertEquals( 3, board.getDistanceAtPosFromPieceId(/*.*/  pW2pos+2*UP+UPRIGHT,pW2Id));
        // dist from pBx -> "."
        assertEquals( 1, board.getDistanceAtPosFromPieceId(/*.*/  pB1pos+2*DOWN,pB1Id));
        assertEquals( 2, board.getDistanceAtPosFromPieceId(/*.*/  pB2pos+2*DOWN,pB2Id));

    }
}