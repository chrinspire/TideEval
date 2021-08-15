/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.NOPIECE;
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
        8 ░░░ r1░░░ d ░b1 d ░░░
        7    ░░░   ░d░   ░░░   ░░░
        6 ░░░   ░░░ b2░░░   ░░░
        5    ░░░   ░░░   ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3    ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1  R1░░░   ░░░   ░░░ R2░░░
           A  B  C  D  E  F  G  H    */
        // test if distances are updated

        int bishopB1pos = 4;
        int bishopB2pos = 4+DOWNLEFT+DOWN;
        board.spawnPieceAt(BISHOP_BLACK,bishopB1pos);
        board.spawnPieceAt(BISHOP_BLACK,bishopB2pos);
        // test if pieces are there
        int bishopB1Id = board.getPieceIdAt(bishopB1pos);
        int bishopB2Id = board.getPieceIdAt(bishopB2pos);
        assertEquals( pieceColorAndName(BISHOP_BLACK),board.getPieceFullName(bishopB1pos));
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
        assertEquals( 2, board.getDistanceAtPosFromPieceId(bishopB1pos,      rookB1Id));  // still 2, but only after moving bishop



    }
}