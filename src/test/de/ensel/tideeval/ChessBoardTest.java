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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.FinalChessBoardEvalTest.*;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
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
    void chessBoardBasicFigurePlacement_Test() {
        ChessBoard board = new ChessBoard("TestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int rookW1pos = A1SQUARE;
        board.spawnPieceAt(ROOK,rookW1pos);
        board.completeCalc();
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
        board.completeCalc();
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
        checkUnconditionalDistance( 1 // was 2+conditional, until decision to directly count covering an own piece as 1 for easier conflict calculatin...
                , board, rookW2pos, rookW1Id);
        checkUnconditionalDistance( 1 // was 2+conditional until decision concerning direct own piece coverage ...
                , board, rookW1pos, rookW2Id);

        //checkUnconditionalDistance( 3, board, rookW2pos+RIGHT,   rookW1Id);
        checkCondDistance( 2, board, rookW2pos+RIGHT,   rookW1Id);  // under the cond that the white rook goes away
        checkUnconditionalDistance( 1, board, rookW1pos, rookW2Id);
        // at square 2
        checkUnconditionalDistance( 2, board,rookB1pos+RIGHT,rookW1Id);
        checkUnconditionalDistance( 2, board,rookB1pos+RIGHT,rookW2Id);
        checkUnconditionalDistance( 1, board,rookB1pos+RIGHT,rookB1Id);
        // at square 3 - thought it's 3
        // remarkable: then I found it should actually be 2 because the black rook does not need to be taken (or passed by
        // via Ta1-a7-h7-h8), it could also move away in between my 2 moves... - voila -> 2 under condition: Ta1-a8-(t moves away)-h8.
        // ... but then again - since NoGo-squares are detected and considered as NoGo :-) this 2 is no longer possible
        // as a8 is a NoGo and r (which threatens that square) is still there. - so 3 is correct again..., but unconditional now
        // however: the target square h8 also is a NoGo... so there is no way without NoGo and the 2+condition is shorter than the Nogo+3+NoCondition. Thus 2+Cond should be expected...
        // be aware :-): if this condition (rb8 goes away) arises, then the nogo on the 3-dist move (e.g. Ta3-Th3-Th8)
        //               should also not occur... so finally the dist==3 without Nogo seems to win, but it now has a condition and is longer,
        //               so 2+cond remains the best...?  Or is 3 correct, because a NoGo on the last move dows not count, because the piece nevertheless unconditionally covers the square h8? (But this is not implemented like this at the moment...
    // TODO: What is correct here??
        //so no: checkUnconditionalDistance( 3,board,7,rookW1Id);
        //and not: checkCondDistance( 2,board,7,rookW1Id);

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
        board.completeCalc();
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
        checkUnconditionalDistance( 1, board, rookB1pos, bishopB2Id);  //  2, but only after moving rook away
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
        board.completeCalc();
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
        //the condDist is shorter, so no: checkUnconditionalDistance( 3, board, /*b1*/ bishopB1pos,rookB1Id);
        checkCondDistance( 2, board, /*b1*/ bishopB1pos,rookB1Id);
        // dist from bishopB1
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*2*/ bishopB1pos+RIGHT,bishopB1Id);  // wrong square color
        checkUnconditionalDistance( 2, board,/*4*/ bishopB1pos+4*LEFT,      bishopB1Id);
        checkUnconditionalDistance( 1, board,/*5*/ bishopB1pos+3*DOWNRIGHT, bishopB1Id);
        // dist from bishopB2
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*4*/ bishopB1pos+4*LEFT,bishopB2Id);  // wrong square color
        checkCondDistance( 2, board,/*R1*/ rookW1pos, bishopB2Id);  //  2, after moving K away
        // dist from KingW
        checkUnconditionalDistance( 4, board,/*1*/  bishopB2pos+UP,   kingWId);
//ToDo: MakeCheck for NoGo - as in the following case, there is no legal way to d6 (which is covered) - unless later the implementation would take beating during moving around into account...
//        checkUnconditionalDistance( 5, board,/*2*/  bishopB1pos+RIGHT,kingWId);
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
        board.completeCalc();
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
            assertEquals( 3, board.getDistanceToPosFromPieceId(/*b1*/ bishopB1pos,rookB1Id));  // 4 after moving king, queen and bishop, or on way around+moving bishop
        // dist from bishopB2
        // 1) le5, then first Queen moves away, 2) lf6 (is not NoGo!), then King moves out of the way, 3) lxTa1
        checkCondDistance( 3, board,/*R1*/ rookW1pos, bishopB2Id);  // King moves out of the way, Queen gets beaten, then Ta1.
        // dist from KingB
        checkUnconditionalDistance( 3, board,/*2*/  bishopB1pos+RIGHT,kingBId);

        assertEquals( 1, board.getDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));  // only after moving q away

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
        board.completeCalc();
        // test if pieces are there
        int knightWId = board.getPieceIdAt(knightWpos);
        int knightBId = board.getPieceIdAt(knightBpos);
        assertEquals( pieceColorAndName(KNIGHT),board.getPieceFullName(knightWId));
        assertEquals( pieceColorAndName(KNIGHT_BLACK),board.getPieceFullName(knightBId));
        // test distances to pieces stored at squares
        // dist from rookW1
        checkUnconditionalDistance( 2, board,/*b1*/ bishopB1pos,      rookW1Id);
        checkCondDistance( 2, board,/*1*/  bishopB2pos+UP,   rookW1Id);  // *chg*
        // dist from rookB = 3: rb4 + rf4{Nd4-any} + rf8
        checkCondDistance( 3, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  //  3, but only by way around and under the condition that the white kniht moves away (otherwise nogo...)
        //checkCondDistance( 4, board, /*b1*/ bishopB1pos,      rookB1Id);  // 4 after moving king, queen and bishop, or on way around+moving bishop
        // dist from bishopB2 - was thought to be 3 for a while?? what is correct?
        checkCondDistance( 4, board,/*R1*/ rookW1pos, bishopB2Id);
        // dist from N
        checkUnconditionalDistance( 3, board, /*5*/  bishopB1pos+3*DOWNRIGHT,knightWId);
        checkUnconditionalDistance( 2, board, /*3*/  A1SQUARE, knightWId);
        // dist from KingB
        checkUnconditionalDistance( 3, board,/*2*/  bishopB1pos+RIGHT,kingBId);
        assertEquals( 1, board.getDistanceToPosFromPieceId(/*3*/  bishopB1pos+LEFT, kingBId));  // only after moving q away
        // dist from N
        checkUnconditionalDistance( INFINITE_DISTANCE, board,/*3*/  A1SQUARE, knightWId);  // only after moving q away
        // dist from n
        checkUnconditionalDistance( 1, board, /*b1*/ bishopB1pos,knightBId);
        checkUnconditionalDistance( 1, board,/*3*/  bishopB2pos+RIGHT, knightBId);

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
        board.completeCalc();
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
        // dist from rookB1 (3 no longer possible because of f7 pawn: 3: rb4 + rf4{Nd4-any} + rf8)
        // but (super tricky!) updateClashResultAndRelEvals() already considers the option
        //      of a reasonable(==0) knight exchange on b5 (with no move/dist count), then rb5 is possible + rh5 + rh8 + rf8
        // Todo: This case of a necessary exchange happening should generate a Condition for the distance!
        checkCondDistance( 4, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  //  now 4
        // so not any more:
        //checkUnconditionalDistance( 4, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  //  now 4

        // dist from bishopB1
        checkCondDistance( 3, board,/*5*/  bishopB1pos+3*DOWNRIGHT, bishopB1Id);  // now 3, after moving both pB or moving around

        // dist from pW1 -> ".",b2,bB1,b1
        checkUnconditionalDistance( 0, board,/*.*/  pW1pos,pW1Id);
        checkUnconditionalDistance( 1, board,/*.*/  pW1pos+UP,pW1Id);
        checkUnconditionalDistance( 1, board,/*.*/  pW1pos+2*UP,pW1Id);
        checkUnconditionalDistance( 2, board,/*.*/  pW1pos+3*UP,pW1Id);
        //  well: the original thought was:
        //  knight would need to walk away, but even this does not help, pawn cannot go there diagonally, however, if the knight is taken, than it can -->3
        //  TODO?: Later this might be INFINITE again or a high number, considering how long an opponents Piece needed to move here to be eaten...
        //  but: as covering ons own piece also counts as "1" now, the correct expected value is 2:
        checkCondDistance( 2, board, /*.*/  knightWpos, pW1Id);
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*.*/  knightWpos+LEFT, pW1Id);  // not reachable
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*.*/  knightWpos+2*LEFT, pW1Id);  // not reachable
        checkUnconditionalDistance(INFINITE_DISTANCE, board,/*.*/  knightWpos+UP, pW1Id);  // not reachable
        // also tricky: needs the n to go to e5 to be beaten in 2 moves, then the b2 to go away (which counts as move, as it is the second condition), so it's 3 with only NoGos
        checkUnconditionalDistance(3, board,/*.*/  bishopB2pos, pW1Id);  // but, it can beat a black piece diagonally left
        //before introducing NoGo it was:
        //  checkUnconditionalDistance(4, board,/*.*/  pB1pos, pW1Id);  // and right
        //  checkUnconditionalDistance(5, board,/*.*/  bishopB1pos, pW1Id);  // not straigt, but via beating others...
        //
        // before introducing isColorLikelyToComeHere()
        //  checkCondDistance(4, board, pB1pos, pW1Id);  // first cond, that black moves something to f5 + final Cond. that pB1 goes away. works because f5 is not NoGo, but neutral
        //  assertEquals(1, board.getBoardSquares()[pB1pos].getvPiece(pW1Id)
        //                                 .getMinDistanceFromPiece().nrOfConditions() );
        //  assertEquals(/*f5*/29, board.getBoardSquares()[pB1pos].getvPiece(pW1Id)
        //          .getMinDistanceFromPiece().getToCond(0) );
        // in between isColorLikelyToComeHere() maked f5 NoGo, but this is not the case any more, f7f5 counts as a possible and non,loosing move for blackand leads to an option to beat here.
        // TODO: Extend test in the future to deal with move chains, because actually, after th f7-pawn has moved away, itis no longer thre to be beaten later to get to that square :-)
        checkCondDistance(4, board, pB1pos, pW1Id);  // no possible way left, but dist 4 with NoGo
        checkCondDistance(5, board,/*.*/  bishopB1pos, pW1Id);  // not straigt, but via beating others...

        // all in all th pW2 cann not really even start to move...
        checkUnconditionalDistance( INFINITE_DISTANCE, board,/*.*/  pW2pos+UP,pW1Id);  // no way, also not via pW2
        checkUnconditionalDistance( 4, board, pB1pos, pW2Id);  // via f4,f5,g6,f7 - all are Nogo(!)
        checkNoGoDistance( 5, board,/*2*/  pB1pos+UP,pW2Id);  // by beating pB2
        checkUnconditionalDistance( 5, board,/*.*/  bishopB1pos,pW2Id);  // f4,f5,g6,f7 - all are Nogo(!)
        checkUnconditionalDistance( 5, board,/*4*/  pB1pos+UPRIGHT,pW2Id);  //  by beating pB2+straight
        checkUnconditionalDistance( 3, board,/*.*/  pB2pos,pW2Id);
        // dist from pBx -> "."

        checkUnconditionalDistance( 1, board,/*.*/  pB1pos+2*DOWN,pB1Id);
        checkUnconditionalDistance( INFINITE_DISTANCE, board,/*.*/  pW2pos,pB1Id);   // cannot move straight on other pawn
        // tricky case: looks like "3+1=4 to move white opponent away (sideways)",
        //      but then does not work any more since it is no longer easily assumed that a pawn could be hoped to just move/beat himself away, if there is noone to beat
        //      but still :-) e2 can move to e4 and be beaten -> so 3, unless pB1 on e4 is seen as a Nogo (due to pW2)  in the end
        checkCondDistance( 4, board, /*.*/ pW2pos, pB1Id);
        checkCondDistance( 5, board, /*.*/ pW2pos+DOWN,pB1Id);    // and then also one further is possilble
        checkCondDistance( 5, board, pW1pos,pB1Id);    // and over to pW1
        checkUnconditionalDistance( 2, board,/*.*/  pB2pos+2*DOWN,pB2Id);
    }


    static public void checkRelEvalOnSquareOfVPce(int expected, ChessBoard board, int pos, int pceId) {
        VirtualPieceOnSquare vPce = board.getBoardSquares()[pos].getvPiece(pceId);
        int actual = vPce.getRelEval();
        if (expected!=actual ) {
            debugPrintln(true, "LAST INFO....: vPiece " + vPce + " has actual relEval=" + actual + " (expected: "+expected+")" );
            debugPrintln(true, "Board: " + board.getBoardFEN() );
            //if ( board.getBoardSquares()[pos].getvPiece(pceId) instanceof VirtualOneHopPieceOnSquare )
                debugPrintln(true, "path to : "
                        + board.getBoardSquares()[pos].getvPiece(pceId).getPathDescription() );
        }
        assertEquals( expected, actual);

    }

    static void checkUnconditionalDistance(int expected, ChessBoard board, int pos, int pceId) {
        if (MAX_INTERESTING_NROF_HOPS<expected)
            return;
        int actual = board.getDistanceToPosFromPieceId(pos, pceId);
        if (expected!=actual || !board.isDistanceToPosFromPieceIdUnconditional(pos,pceId) ) {
            debugPrintln(true, "LAST INFO....: " + board.getDistanceFromPieceId(pos, pceId) + " " + (board.isDistanceToPosFromPieceIdUnconditional(pos,pceId)?"Unconditional!":"") + "(expected: "+expected+")" );
            debugPrintln(true, "Board: " + board.getBoardFEN() );
            //if ( board.getBoardSquares()[pos].getvPiece(pceId) instanceof VirtualOneHopPieceOnSquare )
                debugPrintln(true, "path to : "
                        + board.getBoardSquares()[pos].getvPiece(pceId).getPathDescription() );
        }
        assertEquals( expected, actual);
        assertTrue( board.isDistanceToPosFromPieceIdUnconditional(pos,pceId) );
    }

    static void checkNogoDistance(int expected, ChessBoard board, int pos, int pceId) {
        if (MAX_INTERESTING_NROF_HOPS<expected)
            return;
        int actual = board.getDistanceToPosFromPieceId(pos, pceId);
        if (expected!=actual
                || !board.getBoardSquares()[pos].getvPiece(pceId).getMinDistanceFromPiece().hasNoGo() ) {
            debugPrintln(true, "LAST INFO....: " + squareName(pos) + ": " + board.getDistanceFromPieceId(pos, pceId)
                    + " " + (!board.isDistanceToPosFromPieceIdUnconditional(pos,pceId)?"Conditional!":"")
                    + "(expected: "+expected+" Nogo)" + " via" + board.getBoardSquares()[pos].getvPiece(pceId).getFirstUncondMovesToHere() );
            debugPrintln(true, "Board: " + board.getBoardFEN() );
            //if ( board.getBoardSquares()[pos].getvPiece(pceId) instanceof VirtualOneHopPieceOnSquare )
            debugPrintln(true, "path to : "
                    + board.getBoardSquares()[pos].getvPiece(pceId).getPathDescription() );
        }
        assertEquals( expected, actual);
        assertTrue( board.getBoardSquares()[pos].getvPiece(pceId)
                .getMinDistanceFromPiece().hasNoGo() );
    }

    static void checkCondDistance(int expected, ChessBoard board, int pos, int pceId) {
        if (MAX_INTERESTING_NROF_HOPS<expected)
            return;
        int actual = board.getDistanceToPosFromPieceId(pos, pceId);
        if (expected!=actual || board.isDistanceToPosFromPieceIdUnconditional(pos,pceId) ) {
            debugPrintln(true, "LAST INFO....: " + squareName(pos) + ": " + board.getDistanceFromPieceId(pos, pceId)
                    + " " + (!board.isDistanceToPosFromPieceIdUnconditional(pos,pceId)?"Conditional!":"")
                    + "(expected: "+expected+")" + " via" + board.getBoardSquares()[pos].getvPiece(pceId).getFirstUncondMovesToHere() );
            debugPrintln(true, "Board: " + board.getBoardFEN() );
            //if ( board.getBoardSquares()[pos].getvPiece(pceId) instanceof VirtualOneHopPieceOnSquare )
            debugPrintln(true, "path to : "
                    + board.getBoardSquares()[pos].getvPiece(pceId).getPathDescription() );
        }
        assertEquals(expected,actual);
        assertFalse(board.isDistanceToPosFromPieceIdUnconditional(pos,pceId));
    }

    private void checkNoGoDistance(int expected, ChessBoard board, int pos, int pceId) {
        if (MAX_INTERESTING_NROF_HOPS<expected)
            return;
        int actual = board.getDistanceToPosFromPieceId(pos, pceId);
        if (expected!=actual || !board.isWayToPosFromPieceIdNoGo(pos,pceId)) {
            debugPrintln(true, "LAST INFO....: " + board.getDistanceFromPieceId(pos, pceId) + " " + (!board.isDistanceToPosFromPieceIdUnconditional(pos,pceId)?"Conditional":"") + "(expected: "+expected+")" );
            debugPrintln(true, "Board: " + board.getBoardFEN() );
            debugPrintln(true, "path to : "
                    + board.getBoardSquares()[pos].getvPiece(pceId).getPathDescription() );
        }
        assertEquals(expected,actual);
        assertTrue(board.isWayToPosFromPieceIdNoGo(pos,pceId));
    }



    @Test
    void doMove_String_Test1() {
        // Test 1
        ChessBoard board = new ChessBoard("MoveTest1 ", FENPOS_INITIAL);
        assertEquals(32, board.getPieceCounter());
        // check Knight distance calc after moveing
        final int knightW1Id = board.getPieceIdAt(coordinateString2Pos("b1"));
        final int pawnBdId = board.getPieceIdAt(coordinateString2Pos("d7"));
        final int queenBId = board.getPieceIdAt(coordinateString2Pos("d8"));
        final int d5 = coordinateString2Pos("d5");

        assertTrue(board.doMove("Nc3"));                             // WHITE Nb1c3
        checkUnconditionalDistance(1, board, d5, knightW1Id);
        checkUnconditionalDistance(1, board, d5, knightW1Id);
        // and also check the pawns basic movement
        checkUnconditionalDistance(1, board, d5 + UP, pawnBdId);
        checkCondDistance(2, board, d5 + LEFT, pawnBdId);
        checkUnconditionalDistance(1, board, d5, pawnBdId);
        checkCondDistance(2, board, d5+UP, queenBId);
        checkCondDistance(2, board, d5+DOWN, queenBId);
        assertEquals(1, board.getDistanceFromPieceId(d5+DOWN,queenBId).nrOfConditions());
        assertEquals("d7", squareName(
                board.getDistanceFromPieceId(d5+DOWN,queenBId).getFromCond(0)));

        assertTrue(board.doMove("d5"));
        checkUnconditionalDistance(INFINITE_DISTANCE, board, d5 + UP, pawnBdId);
        assertEquals(INFINITE_DISTANCE, board.getDistanceToPosFromPieceId(d5 + LEFT, pawnBdId));
        checkUnconditionalDistance(0, board, d5, pawnBdId);
        //Todo-optional-testcase (bug already fixed was: should be reset to null like minDist:
        // vPce (id=3) on [d5] is 1 ok / null / 4 ok&if{d5-any (schwarz)} away from schwarze Dame relEval=940
        checkUnconditionalDistance(1, board, d5+UP, queenBId);
        checkUnconditionalDistance(1, board, d5, queenBId);
        checkCondDistance(2, board, d5+DOWN, queenBId);
        assertEquals(1, board.getDistanceFromPieceId(d5+DOWN,queenBId).nrOfConditions());
        assertEquals("d5", squareName(
                board.getDistanceFromPieceId(d5+DOWN,queenBId).getFromCond(0)));
        // go on with Knight
        assertTrue(board.doMove("Nb5"));
        checkUnconditionalDistance(2, board, d5, knightW1Id);
        // -->  "
        assertEquals(32, board.getPieceCounter());
        boardEvaluation_SingleBoard_Test(board, 0, 135);
    }

    @Test
    void doMove_String_Test1fen() {
        // Test 1
        ChessBoard board = new ChessBoard("MoveTest1 ", FENPOS_INITIAL + "moves b1c3 d7d5");
        assertEquals(32, board.getPieceCounter());
        // check Knight distance calc after moveing
        final int knightW1Id = board.getPieceIdAt(coordinateString2Pos("c3"));
        final int pawnBdId = board.getPieceIdAt(coordinateString2Pos("d5"));
        final int queenBId = board.getPieceIdAt(coordinateString2Pos("d8"));
        final int d5 = coordinateString2Pos("d5");

        checkUnconditionalDistance(INFINITE_DISTANCE, board, d5 + UP, pawnBdId);
        assertEquals(INFINITE_DISTANCE, board.getDistanceToPosFromPieceId(d5 + LEFT, pawnBdId));
        checkUnconditionalDistance(0, board, d5, pawnBdId);
        //Todo-optional-testcase (bug already fixed was: should be reset to null like minDist:
        // vPce (id=3) on [d5] is 1 ok / null / 4 ok&if{d5-any (schwarz)} away from schwarze Dame relEval=940
        checkUnconditionalDistance(1, board, d5+UP, queenBId);
        checkUnconditionalDistance(1, board, d5, queenBId);
        checkCondDistance(2, board, d5+DOWN, queenBId);
        assertEquals(1, board.getDistanceFromPieceId(d5+DOWN,queenBId).nrOfConditions());
        assertEquals("d5", squareName(
                board.getDistanceFromPieceId(d5+DOWN,queenBId).getFromCond(0)));
        // go on with Knight
        assertTrue(board.doMove("Nb5"));
        checkUnconditionalDistance(2, board, d5, knightW1Id);
        // -->  "
        assertEquals(32, board.getPieceCounter());
        boardEvaluation_SingleBoard_Test(board, 0, 135);
    }


    @Test
    void doMove_String_Test2() {
        ChessBoard chessBoard = new ChessBoard("MoveTest 2 ", FENPOS_INITIAL);
        assertEquals(32, chessBoard.getPieceCounter());
        // check Rook distance calc after moveing
        final int rookB1Id = chessBoard.getPieceIdAt(0);
        final int a3 = coordinateString2Pos("a3");
        final int a4 = coordinateString2Pos("a4");
        assertFalse(chessBoard.isDistanceToPosFromPieceIdUnconditional(a4, rookB1Id));
        assertEquals(2, chessBoard.getDistanceToPosFromPieceId(a4, rookB1Id));
        assertFalse(chessBoard.isDistanceToPosFromPieceIdUnconditional(a3, rookB1Id));
        assertEquals(2, chessBoard.getDistanceToPosFromPieceId(a3, rookB1Id));
        assertTrue(chessBoard.doMove("d4"));
        assertFalse(chessBoard.isDistanceToPosFromPieceIdUnconditional(a3, rookB1Id));
        assertEquals(2, chessBoard.getDistanceToPosFromPieceId(a3, rookB1Id));
        assertFalse(chessBoard.isDistanceToPosFromPieceIdUnconditional(a4, rookB1Id));
        assertEquals(2, chessBoard.getDistanceToPosFromPieceId(a4, rookB1Id));
        assertTrue(chessBoard.doMove("a5"));
        //if (MAX_INTERESTING_NROF_HOPS>3)
        //   assertEquals( 4, chessBoard.XXXgetShortestUnconditionalDistanceToPosFromPieceId(a4,rookB1Id));
        assertFalse(chessBoard.isDistanceToPosFromPieceIdUnconditional(a4, rookB1Id));
        assertEquals(2, chessBoard.getDistanceToPosFromPieceId(a4, rookB1Id));
        assertTrue(chessBoard.doMove("b4"));
        assertFalse(chessBoard.isDistanceToPosFromPieceIdUnconditional(a4, rookB1Id));
        assertEquals(2, chessBoard.getDistanceToPosFromPieceId(a4, rookB1Id));
        // -->  "
        assertEquals(32, chessBoard.getPieceCounter());
        boardEvaluation_SingleBoard_Test(chessBoard, 20, 170);
    }

    @Test
    void doMove_String_Test3() {
        // Test 3
        ChessBoard chessBoard = new ChessBoard("MoveTest 3", FENPOS_INITIAL);
        assertEquals(32, chessBoard.getPieceCounter());
        assertTrue(chessBoard.doMove("e4"));
        assertEquals(32, chessBoard.getPieceCounter());
        // check pawn distance calc after moveing
        assertEquals(INFINITE_DISTANCE, chessBoard.getDistanceToPosFromPieceId(
                coordinateString2Pos("d3"), 20));
        assertEquals(INFINITE_DISTANCE, chessBoard.getDistanceToPosFromPieceId(
                coordinateString2Pos("d4"), 20));
        checkUnconditionalDistance(1, chessBoard, coordinateString2Pos("e5"), 20);
        checkCondDistance(1, chessBoard, coordinateString2Pos("d5"), 20);
        checkCondDistance(1, chessBoard, coordinateString2Pos("f5"), 20);
        checkCondDistance(INFINITE_DISTANCE, chessBoard, coordinateString2Pos("g5"), 20);

        int knightB1Id = 1;
        checkUnconditionalDistance(2, chessBoard, coordinateString2Pos("e5"), knightB1Id);
        assertTrue(chessBoard.doMove("e5"));
        checkUnconditionalDistance(2, chessBoard, coordinateString2Pos("e5"), knightB1Id);
        assertTrue(chessBoard.doMove("d4"));
        assertTrue(chessBoard.doMove("exd4"));
        assertEquals(31, chessBoard.getPieceCounter());
        checkUnconditionalDistance(2, chessBoard, coordinateString2Pos("e5"), knightB1Id);
        assertTrue(chessBoard.doMove("c3"));
        assertTrue(chessBoard.doMove("d6?"));
        assertTrue(chessBoard.doMove("Bc4?!"));
        assertTrue(chessBoard.doMove("dxc3"));
        assertEquals(30, chessBoard.getPieceCounter());
        assertTrue(chessBoard.doMove("Nf3"));

        assertTrue(chessBoard.doMove("cxb2"));
        assertTrue(chessBoard.doMove("Bxb2"));
        assertEquals(28, chessBoard.getPieceCounter());
        assertTrue(chessBoard.doMove("a6?!"));
        // check king+rook position after castelling
        assertEquals(KING, chessBoard.getPieceTypeAt(coordinateString2Pos("e1")));
        assertEquals(coordinateString2Pos("e1"), chessBoard.getKingPos(WHITE));
        assertEquals(ROOK, chessBoard.getPieceTypeAt(coordinateString2Pos("h1")));
        assertTrue(chessBoard.doMove("O-O"));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("e1")));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("h1")));
        assertEquals(KING, chessBoard.getPieceTypeAt(coordinateString2Pos("g1")));
        assertEquals(coordinateString2Pos("g1"), chessBoard.getKingPos(WHITE));
        assertEquals(ROOK, chessBoard.getPieceTypeAt(coordinateString2Pos("f1")));

        assertEquals(28, chessBoard.getPieceCounter());
        assertTrue(chessBoard.doMove("Be6?"));
        assertTrue(chessBoard.doMove("Bxe6"));
        assertTrue(chessBoard.doMove("fxe6"));
        assertTrue(chessBoard.doMove("Qb3?"));
        assertTrue(chessBoard.doMove("b6?"));
        assertTrue(chessBoard.doMove("Qxe6+"));
        assertTrue(chessBoard.doMove("Qe7"));
        assertTrue(chessBoard.doMove("Qd5"));
        assertTrue(chessBoard.doMove("c6"));
        assertTrue(chessBoard.doMove("Qh5+"));
        assertTrue(chessBoard.doMove("Qf7"));
        assertTrue(chessBoard.doMove("Qh3"));
        debugPrintln(DEBUGMSG_TESTCASES, chessBoard.getBoardFEN());
        assertTrue(chessBoard.doMove("Nd7"));
        assertTrue(chessBoard.doMove("Nc3?"));
        assertTrue(chessBoard.doMove("Ngf6?!"));
        assertTrue(chessBoard.doMove("Rfe1?"));

        // check king+rook position after castelling
        assertEquals(KING_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("e8")));
        assertEquals(coordinateString2Pos("e8"), chessBoard.getKingPos(BLACK));
        assertEquals(ROOK_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("a8")));
        assertTrue(chessBoard.doMove("O-O-O"));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("e8")));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("a8")));
        assertEquals(KING_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("c8")));
        assertEquals(coordinateString2Pos("c8"), chessBoard.getKingPos(BLACK));
        assertEquals(ROOK_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("d8")));

        assertTrue(chessBoard.doMove("a4??"));
        assertTrue(chessBoard.doMove("a5??"));
        assertTrue(chessBoard.doMove("Ba3?"));
        assertTrue(chessBoard.doMove("Kb7?!"));
        assertTrue(chessBoard.doMove("Rab1?"));
        assertTrue(chessBoard.doMove("Ne5"));
        String newFen = chessBoard.getBoardFEN();  // TODO
        //assertEquals("3r1l1r/1k3qpp/1ppp1n2/p3n3/P3P3/L1N2N1Q/5PPP/1R2R1K1  w - - 4 19",newFen);
        assertEquals("3r1b1r/1k3qpp/1ppp1n2/p3n3/P3P3/B1N2N1Q/5PPP/1R2R1K1  w - - 4 19",newFen);
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("e2")));
        assertEquals(PAWN, chessBoard.getPieceTypeAt(coordinateString2Pos("e4")));
        assertEquals(PAWN_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("a5")));
        assertEquals(KING_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("b7")));
        assertEquals(KING, chessBoard.getPieceTypeAt(coordinateString2Pos("g1")));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("f1")));
        assertEquals(ROOK, chessBoard.getPieceTypeAt(coordinateString2Pos("e1")));
    }


    @Test
    void doMove_String_Test4() {
        // Test 4
        // Problem was:  still ok:  d4(d2d4) e6(e7e6) c4(c2c4) c6(c7c6) e4(e2e4) Nf6?(g8f6) e5(e4e5) Ne4(f6e4) Bf4?(c1f4) Qa5+?!(d8a5) Nd2(b1d2) Bb4?(f8b4) 
        //  Problem 1:  Nf3(d2f3) - should have been the other Knight, as this one is pinned!
        //  then Problem 2: O-O?(e8g8) - would have been illegal if Ng1 is still there...
        //  then seems ok: Be2?!(f1e2) Be7?!(b4e7) O-O(e1g1) 
        //  but Problem 3: Qd8?? - is a legal move, why was it not recognized?
        //  then: Nxe4 - ok, does not exist after P1... 
        //  and seems ok: d5(d7d5) cxd5(c4d5) cxd5(c6d5) 
        //  might be Folgeproblem, but still strange how a knight could move 2 squares straight or diagonal...: Nc3(f3c3) f6(f7f6) Re1(f1e1) fxe5(f6e5) Bxe5(f4e5) Nc6(b8c6) Bg3(e5g3) Qa5 Qd2 e5(e6e5) dxe5(d4e5) Nxe5(c6e5) Nxe5(c3e5) Qb6(a5b6) Nxd5(e5d5) Qe6(b6e6) Nxe7+(d5e7) Qxe7(e6e7) Bc4+(e2c4) Be6(c8e6) Bxe6+(c4e6) Qxe6 Nd7(e5d7)**** Fehler: Fehlerhafter Zug: auf e5 steht keine Figur auf Board Test .
        ChessBoard chessBoard = new ChessBoard("MoveTest4 " , FENPOS_INITIAL );
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
        // the Qe6 resuls in a very mean update difficulty for Bc1 on g6:
        // on depth-level 2: f5 tells g6 a "3 ok" (coming from not yet updated e4), because via e6 it is 2{if e6-any}+1 = also 3 but with condition and thus longer.
        // assumtion/unchecked: on depth-level 3 this will be corrected?
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
    void doMove_String_Test5() {
        // Test 5
        // 1. e4(e2e4) c6(c7c6) 2. Nf3(g1f3) d5(d7d5) 3. exd5(e4d5) cxd5(c6d5) 4. Bb5+(f1b5) Bd7(c8d7)
        // 5. Bxd7+(b5d7) Nxd7(b8d7) 6. O-O(e1g1) e5?(e7e5) 7. d3?(d2d3) Ngf6?(g8f6) 8. Re1(f1e1) Bd6(f8d6)
        // 9. b3?(b2b3) Bb4?(d6b4) 10. Bd2?!(c1d2) a5?(a7a5) 11. Nxe5(f3e5) Nxe5?(d7e5) 12. Rxe5+(e1e5) Kd7?(e8d7)
        // 13. Bxb4?(d2b4) axb4(a5b4) 14. Qe2?(d1e2) Re8(h8e8) 15. Rxe8(e5e8) Qxe8(d8e8) 16. Qxe8+?(e2e8) Kxe8?(d7e8) 17. a3?!(a2a3) b5?(b7b5) 18. a4?(a3a4) d4?(d5d4) 19. Nd2(b1d2) Nd5?(f6d5) 20. Nf3(d2f3) Nc3(d5c3) 21. Nxd4?(f3d4) bxa4(b5a4) 22. bxa4(b3a4) Rxa4(a8a4) 23. Rxa4(a1a4) Nxa4(c3a4) 24. g3(g2g3) Kd7(e8d7)
        // 25. f4(f2f4)**** Fehler: Fehlerhafter Zug: f2 -> f4 nicht möglich auf Board Testboard 1. e4 0.24 1... c6 0.13 2....
        ChessBoard board = new ChessBoard("MoveTest4 " , FENPOS_INITIAL );
        assertTrue( board.doMove("e4"));
            assertTrue( board.doMove("c6"));
        assertTrue( board.doMove("Nf3"));
           assertTrue( board.doMove("d5"));
        assertTrue( board.doMove("exd5"));
            assertTrue( board.doMove("cxd5"));
        assertTrue( board.doMove("Bb5+"));
            assertTrue( board.doMove("Bd7"));
        assertTrue( board.doMove("Bxd7+"));
            assertTrue( board.doMove("Nxd7"));
        assertTrue( board.doMove("0-0"));
            assertTrue( board.doMove("e5?"));
        assertTrue( board.doMove("d3?"));
            assertTrue( board.doMove("Ngf6?"));
        assertTrue( board.doMove("Re1"));
            assertTrue( board.doMove("Bd6"));
        assertTrue( board.doMove("b3?"));
            assertTrue( board.doMove("Bb4?"));
        assertTrue( board.doMove("Bd2?!"));
            assertTrue( board.doMove("a5?"));
        assertTrue( board.doMove("Nxe5"));
            assertTrue( board.doMove("Nxe5"));
        assertTrue( board.doMove("Rxe5+"));
            assertTrue( board.doMove("Kd7?"));
        assertTrue( board.doMove("Bxb4?"));
            assertTrue( board.doMove("axb4"));
        assertTrue( board.doMove("Qe2?"));
            assertTrue( board.doMove("Re8"));
        assertTrue( board.doMove("Rxe8"));
            assertTrue( board.doMove("Qxe8"));
        assertTrue( board.doMove("Qxe8+?"));
            assertTrue( board.doMove("Kxe8"));
        assertTrue( board.doMove("a3?!"));
            assertTrue( board.doMove("b5?"));
        assertTrue( board.doMove("a4?"));
            assertTrue( board.doMove("d4?"));
        assertTrue( board.doMove("Nd2"));
            assertTrue( board.doMove("Nd5?"));
        assertTrue( board.doMove("Nf3"));
            assertTrue( board.doMove("Nc3"));
        // the error below has/has it's origin here. The f4-dist==2 before the knight moves away.
        assertTrue( board.doMove("Nxd4?"));
        // It seems it is/was not corrected to 1 after the way is free
            assertTrue( board.doMove("bxa4"));
        assertTrue( board.doMove("bxa4"));
            assertTrue( board.doMove("Rxa4"));
        assertTrue( board.doMove("Rxa4"));
            assertTrue( board.doMove("Nxa4"));
        assertTrue( board.doMove("g3"));
        assertTrue( board.doMove("Kd7"));

        checkUnconditionalDistance(1, board, /*f4*/ 37, /*white f pawn*/ 21);
        checkUnconditionalDistance(2, board, /*f5*/ 29, /*white f pawn*/ 21);
        //und hier passiert(e) nun der Fehler: **** Fehler: Fehlerhafter Zug: f2 -> f4 nicht möglich auf Board Testboard
        assertTrue( board.doMove("f4"));
        checkUnconditionalDistance(0, board, /*f4*/ 37, /*white f pawn*/ 21);
        checkUnconditionalDistance(1, board, /*f5*/ 29, /*white f pawn*/ 21);
    }


    @Test
    void isPinnedByKing_Test() {
        ChessBoard board = new ChessBoard("PinnedKingTestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int kingWpos = A1SQUARE;
        int kingWId = board.spawnPieceAt(KING,kingWpos);
        int knightW1pos = kingWpos+2*UP;
        int knightW1Id = board.spawnPieceAt(KNIGHT,knightW1pos);
        board.completeCalc();

        // the knight can move to/cover the king in 2 hops
        checkUnconditionalDistance(2, board, kingWpos,  knightW1Id);
        // then it can move freely
        boolean legalMove = board.doMove("Nc2");
        assertTrue(legalMove);
        // we need a black piece to move, so the knight can move back,,
        int pawnB1Id = board.spawnPieceAt(PAWN_BLACK,15);
        board.completeCalc();
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
        board.completeCalc();
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
    void isPinnedByKing_movedThere_Test() {
        ChessBoard board = new ChessBoard("PinnedKingTestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int kingWpos = A1SQUARE+RIGHT;
        int kingWId = board.spawnPieceAt(KING,kingWpos);
        int knightW1pos = kingWpos+2*UP+LEFT;
        int knightW1Id = board.spawnPieceAt(KNIGHT,knightW1pos);
        board.completeCalc();

        // then it can move freely
        boolean legalMove = board.doMove("Nc2");
        assertTrue(legalMove);
        // we need a black piece to move, so the knight can move back,,
        int pawnB1Id = board.spawnPieceAt(PAWN_BLACK,15);
        board.completeCalc();
        assertTrue(board.doMove("h5"));
        //and move night back
        legalMove = board.doMove("Na3");
        assertTrue(legalMove);
        assertTrue(board.doMove("h4"));
        // but then the rook pins the knight to the king
        int rookB1pos = knightW1pos+2*UP;
        int rookB1Id = board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        board.completeCalc();

        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░.░
        6 ░░░   ░░░   ░░░   ░░░ .
        5  t ░░░   ░░░   ░░░   ░.░
        4 ░░░   ░░░   ░░░   ░░░ p
        3  N ░░░   ░░░   ░░░   ░v░
        2 ░░░   ░.░   ░░░   ░░░
        1  < ░K░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */

        checkUnconditionalDistance(1,board,knightW1pos,rookB1Id);
        checkUnconditionalDistance(3,board,kingWpos,rookB1Id);  // 3 as it needs to avoid the covered b5 square

        assertTrue(board.doMove("Ka1"));
        kingWpos += LEFT;

        // after King moved, it is pinned, so test cases are now like in the one above
        assertEquals( 1, board.getDistanceToPosFromPieceId(knightW1pos,  rookB1Id));
        checkCondDistance( 1, board, kingWpos,rookB1Id); // under the condition that knight moves away
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

    ///

    @Test
    void doMove_Update_ExBug_Test() {
        ChessBoard board = new ChessBoard("MoveTestExBug", FENPOS_INITIAL);
        final int queenB1Id = board.getPieceIdAt(coordinateString2Pos("d8"));
        final int d2 = coordinateString2Pos("d2");
        final int e1 = coordinateString2Pos("e1");
        assertTrue(board.doMove("d4"));
        checkCondDistance(3, board,d2, queenB1Id);
        assertEquals(1,board.getDistanceFromPieceId(d2, queenB1Id).nrOfConditions() );
        checkCondDistance(3, board,e1, queenB1Id);
        assertEquals(1,board.getDistanceFromPieceId(e1, queenB1Id).nrOfConditions() );
    }



    /////////////////////////////////////////////////////////////////////////////

    @Test
    void getBestMove_takeIt_Test() {
        ChessBoard board = new ChessBoard("TakeItTestBoard", FENPOS_EMPTY);
        int kingWpos    = A1SQUARE+RIGHT;
        int knightW1pos = kingWpos+2*UP+LEFT;
        int rookB1pos   = knightW1pos+2*UP;
        int kingWId = board.spawnPieceAt(KING,kingWpos);
        int knightW1Id = board.spawnPieceAt(KNIGHT,knightW1pos);
        int rookB1Id = board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        board.completeCalc();
        // move white (on a bad spot) so it is blacks turn
        assertTrue(board.doMove("Ka1"));
        kingWpos += LEFT;
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

        assertEquals( new Move(rookB1pos,coordinateString2Pos("a3")),board.getBestMove());
    }

    @Test
    void getBestMove_takeIt2_Test() {
        ChessBoard board = new ChessBoard("TakeItTestBoard", FENPOS_EMPTY);
        int kingWpos    = A1SQUARE+RIGHT;
        int knightW1pos = kingWpos+2*UP+LEFT;
        int rookB1pos   = knightW1pos+2*UP;
        int pawnW1pos   = coordinateString2Pos("e5");
        int pawnW1Id = board.spawnPieceAt(PAWN, pawnW1pos);
        int kingWId = board.spawnPieceAt(KING, kingWpos);
        int knightW1Id = board.spawnPieceAt(KNIGHT, knightW1pos);
        int rookB1Id = board.spawnPieceAt(ROOK_BLACK, rookB1pos);
        board.completeCalc();
        // move white (on a bad spot) so it is blacks turn
        board.getBestMove();  //just for debug output
        assertTrue(board.doMove("Ka1"));
        kingWpos += LEFT;
        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5  t ░░░   ░R░   ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3  N ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1  K ░░░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */
        // compared to other test here is also a pawn to take, but knight tastes better
        assertEquals( new Move(rookB1pos, knightW1pos),board.getBestMove());

        // but now add even tastier rook to take
        int rookW1pos   = coordinateString2Pos("d5");
        int rookW1Id = board.spawnPieceAt(ROOK, rookW1pos);
        board.completeCalc();
        assertEquals( new Move(rookB1pos, rookW1pos),board.getBestMove());
    }

    @Test
    void getBestMove_doNotMoveAwayWhenKingPinned_Test() {
        ChessBoard board = new ChessBoard("TakeItTestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int kingWpos    = A1SQUARE;
        int knightW1pos = kingWpos+3*UP;
        int rookB1pos   = knightW1pos+2*UP;
        int knightW1Id = board.spawnPieceAt(KNIGHT,knightW1pos);
        int kingWId = board.spawnPieceAt(KING,kingWpos);
        int rookB1Id = board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        board.completeCalc();
        // expect king to move away (on b1 or b2)
        Move m = board.getBestMove();
        assertTrue( m.from()==kingWpos && (m.to()==coordinateString2Pos("b1")
                                                    || m.to()==coordinateString2Pos("b2")) );
        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░t░   ░░░   ░░░   ░░░
        5    ░░░   ░░░   ░░░   ░░░
        4 ░N░   ░░░   ░░░   ░░░
        3    ░░░   ░░░   ░░░   ░░░
        2 ░░░ / ░░░   ░░░   ░░░
        1  K ░░░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */
    }

    @Test
    void FUTURE_getBestMove_doNotMoveAwayWhenKingPinned_Cover_Test() {
        ChessBoard board = new ChessBoard("TakeItTestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int kingWpos = A1SQUARE;
        int kingWId = board.spawnPieceAt(KING,kingWpos);
        int knightW1pos = kingWpos+2*UP;
        int knightW1Id = board.spawnPieceAt(KNIGHT,knightW1pos);
        int pawnB1Id = board.spawnPieceAt(PAWN_BLACK,15);
        int rookB1pos = knightW1pos+2*UP;
        int rookB1Id = board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        board.completeCalc();
        // expect king to cover knight (better on b2 to unpin or is a2 ok?)
        assertEquals( new Move(kingWpos,coordinateString2Pos("b2")),board.getBestMove());
        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5  t ░░░   ░░░   ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3  N ░░░   ░░░   ░░░   ░░░
        2 ░░░ / ░░░   ░░░   ░░░
        1  K ░░░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */
    }

    @Test
    void getBestMove_TakeOrBlock_Test() {
        ChessBoard board = new ChessBoard("TakeOrprotectTestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int wR = board.spawnPieceAt(ROOK, coordinateString2Pos("a1"));
        int wN1 = board.spawnPieceAt(KNIGHT, coordinateString2Pos("b1"));
        int wN2 = board.spawnPieceAt(KNIGHT, coordinateString2Pos("c3"));
        int wPa = board.spawnPieceAt(PAWN, coordinateString2Pos("a2"));
        int wPe = board.spawnPieceAt(PAWN, coordinateString2Pos("e3"));
        int bl = board.spawnPieceAt(BISHOP_BLACK, coordinateString2Pos("e5"));
        int bpe = board.spawnPieceAt(PAWN_BLACK, coordinateString2Pos("e4"));
        board.completeCalc();
        // expect N to NOT take p (and then loose R), but to stax and get l for R
        assertEquals( new Move("a2-a4"), board.getBestMove());
        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5    ░░░   ░░░ l ░░░   ░░░
        4 ░░░   ░░░   ░p░   ░░░
        3    ░░░ N ░░░ P ░░░   ░░░
        2 ░P░   ░░░   ░░░   ░░░
        1  R ░N░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */
    }

    void getBestMove_TakeOrProtect_Test() {
        ChessBoard board = new ChessBoard("TakeOrprotectTestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int wR = board.spawnPieceAt(ROOK, coordinateString2Pos("a1"));
        int wL = board.spawnPieceAt(BISHOP, coordinateString2Pos("b1"));
        int wN = board.spawnPieceAt(KNIGHT, coordinateString2Pos("c2"));
        int wPa = board.spawnPieceAt(PAWN, coordinateString2Pos("a2"));
        int wPe = board.spawnPieceAt(PAWN, coordinateString2Pos("e2"));
        int bl = board.spawnPieceAt(BISHOP_BLACK, coordinateString2Pos("e5"));
        int bpe = board.spawnPieceAt(PAWN_BLACK, coordinateString2Pos("e3"));
        board.completeCalc();
        // expect N to NOT take p (and then loose R), but to stax and get l for R
        assertEquals( new Move("a2-a4"), board.getBestMove());
        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5    ░░░   ░░░ l ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3    ░░░   ░░░ p ░░░   ░░░
        2 ░P░   ░N░   ░P░   ░░░
        1  R ░L░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */
    }


    // debug output to show bonus for check blocking:  "rnbqkbnr/pp1p1ppp/2p1p3/8/8/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1, egal"

    // simple checkmates
    @ParameterizedTest
    @CsvSource({
            //simple ones
            "8/8/2r2Q2/2k5/4K3/8/5b2/8 w - - 0 1, f6f2"
            // avoid mateIn1
              })
    void ChessBoardGetBestMove_isBestMove_doCheckmate_Test(String fen, String expectedBestMove) {
        doAndTestPuzzle(fen,expectedBestMove, "Simple  Test");
    }

    // choose the one best move
    @ParameterizedTest
    @CsvSource({
            //simple ones
/*Todo*/             "8/8/2r2Q2/2k5/4K3/8/5b2/8 w - - 0 1, f6f2",
            "8/8/2r2Q2/8/2k1K3/8/5b2/8 w - - 0 1, f6c6",
            "8/2r5/2k5/8/4KQ2/8/8/2b5 w - - 0 1, f4c1",
            "8/2r5/8/bk1N4/4K3/8/8/8 w - - 0 1, d5c7",
            "3r4/8/8/3Q2K1/8/8/n1k5/3r4 w - - 0 1, d5a2"
            // mateIn1
            , "8/8/8/1q6/8/K3k3/8/7q b - - 0 1, h1a1|h1a8"
            //Forks:
            , "8/8/8/k3b1K1/8/4N3/3P4/8 w - - 0 1, e3c4"
            , "8/8/8/k3b1K1/3p4/4N3/3P4/8 w - - 0 1, e3c4",
            //stop/escape check:
            "rnb1kbnr/pppp1ppp/8/4p3/7q/2N1P3/PPPPP1PP/R1BQKBNR  w KQkq - 2 3, g2g3",
            "8/3pk3/R7/1R2Pp1p/2PPnKr1/8/8/8 w - - 4 43, f4f5",  // f5  looks most attractive at the current first glance, but should be f4e3|f4f3 - and NOT f4f5 -> #1
            "r6k/pb4r1/1p1Qpn2/4Np2/3P4/4P1P1/P4P1q/3R1RK1 w - - 0 24, g1h2",
            "rnl1k2r/pppp1ppp/4p3/8/3Pn2q/5Pl1/PPP1P2P/RNLQKLNR  w KQkq - 0 7, h2g3",
            "r1lq1l1r/p1ppkppp/p4n2/1P3PP1/3N4/P4N2/2P1Q2P/R1L1K2R  b KQ - 4 17, e7d6|f6e4",
            "6k1/1b3pp1/p3p2p/Bp6/1Ppr2K1/P3R1PP/5n2/5B1R w - - 1 37, g4h5",  // https://lichess.org/bMwlzoVV
            "r1lq2r1/1p6/p3pl2/2p1N3/3PQ2P/2PLk3/PP4P1/5RK1  b - - 4 23, e3d2"
            , "r1bq3r/pp2kp1p/1n2p1p1/2Qp4/P1p5/2P2NPB/1PP1PP1P/R3K2R b KQ - 3 13, d8d6|e7e8" // NOT e7d7, where k locks the vulnerable knight and k is checkable by N https://lichess.org/eI3EmDF8/black#25
            , "3r3k/1bqpnBp1/p1n4R/1p6/4P3/8/PP1Q1PPP/2R3K1 b - - 0 22, g7h6", // not null! pg7xh6 not listed as valid move!
            // pawn endgames:
            "8/P7/8/8/8/8/p7/8 b - - 0 1, a2a1q"
            , "8/P7/8/8/8/8/p7/8 w - - 0 1, a7a8q"
            //// (ex)blunders from tideeval online games
            , "1rbqk2r/p1ppbp1p/2n1pnp1/4P3/1p1P1P2/2P1BN1P/PPQNB1P1/R4RK1 b - - 0 13, f6d5|f6h5"  // instead of blundering the knight with g6g5
            , "1rb2rk1/p1pp1pp1/1pn5/3p2p1/2B1Nb2/2P5/PP1N1PPP/R1B1K2R w KQ - 0 19, c4d5"  // bug was moving away with N and getting l beaten...
/*Todo!*/             , "rnbqkbnr/pp2ppp1/3p3p/2p3B1/8/2NP4/PPP1PPPP/R2QKBNR w KQkq - 0 4, g5d2|g5d1|g5e3"  // B is attacked - move it away!
            //Warum nicht einfach die Figur nehmen?
            , "5rk1/p2qppb1/3p2pp/8/4P1b1/1PN1BPP1/P1Q4K/3R4 b - - 0 24, g4f3" // lxP statt Zug auf Feld wo eingesperrt wird,  https://lichess.org/7Vi88ar2/black#79
            , "r4rk1/pbqnbppp/1p2pn2/2Pp4/8/1P1BPN1P/PBPNQPP1/R4RK1 b - - 0 11, d7c5|b6c5|c7c5|e7c5"  //  - sieht auch noch nach komischen Zug aus, der etwas decken will aber per Abzug einen Angriff frei gibt.   https://lichess.org/dhVlMZEC/black
            , "1r1qk1r1/p1p1bpp1/1p5p/4p3/1PQ4P/P3N1N1/1B1p1PP1/3K3R w - - 2 29, b2e5"   // https://lichess.org/ZGLMBHLF/white
/*Todo*/             , "r1bq1rk1/1p2bppp/p2p1n2/2p5/4PB2/2NQ4/PPP1BPPP/2KR3R w - - 0 11, f4d6"    // take it - in a slightly complex clash, but worth it https://lichess.org/as1rvv81#20 - was no bug in clashes/relEval on d6 with 2nd row. relEval==100 seems ok, but unclear why. Adding releval of -320@0 as result/benefit despite nogo for vPce(15=weißer Läufer) on [d6] 1 ok away from origin {f4} on square f4. ->f4d6(-320@0)
            , "r1b1kbnr/3n1ppp/p3p3/qppp4/3P4/1BN1PN2/PPPB1PPP/R2QK2R b KQkq - 1 8, c5c4" // would have trapped B - https://lichess.org/Cos4w11H/black#15
 /*Todo?*/           , "r1b1kbnr/3n1ppp/p3p3/q1pp4/Np1P4/1B2PN2/PPPB1PPP/R2QK2R b KQkq - 1 9, c5c4" // still same
    })
    void ChessBoardGetBestMove_isBestMoveTest(String fen, String expectedBestMove) {
        doAndTestPuzzle(fen,expectedBestMove, "Simple  Test");
    }
/*
7 moves:  f6e8=354/90/648/626///60/ f6e4=1224/90/-18/746//120/-60/ f6g8=-177/90/-18/746//120/-60/ f6g4=354/-45/-648/-233//120/-60/ f6d7=354/90/648/626///60/ f6h7=354/90/648/626///60/ f6h5=354/90/-18/725//120/-60/ therein for moving away:  f6e4=//-333/60//60/-60/ f6g8=//-333/60//60/-60/ f6g4=//-333/60//60/-60/ f6h5=//-333/60//60/-60/ f6d5=//-333/60//60/-60/
7 moves:  f6e8=354/90/45/120//// f6e4=1224/90/26043/6906//6786/6546/ f6g8=-177/90/26043/6906//6786/6546/ f6g4=354/-45/25863/6433//6786/6546/ f6d7=354/90/45/120//// f6h7=354/90/45/120//// f6h5=354/90/26043/6885//6786/6546/ therein for moving away:  f6e4=//12999/3393//3393/3273/ f6g8=//12999/3393//3393/3273/ f6g4=//12999/3393//3393/3273/ f6h5=//12999/3393//3393/3273/ f6d5=//12999/3393//3393/3273/
 */

    // do NOT choose a certain move
    @ParameterizedTest
    @CsvSource({
            //simple ones
            "8/2r5/1k6/8/4KQ2/8/8/2b5 w - - 0 1, f6-c1",
            "8/2r5/1k6/8/4KQ2/8/8/2b5 w - - 0 1, f6-c6",
            "8/2r5/2k5/8/4KQ2/8/8/2b5 w - - 0 1, f4-c7",
            "8/7K/8/k3b3/8/4p3/P1N1P3/RB6 w - - 0 1, c2-e3",
            "8/7K/8/k3b3/8/4p3/2N1P3/N7 w - - 0 1, c2-e3",
            "r1lqklr1/1ppppppp/p1n2n2/8/3PP3/1LN2N2/PPPL1PPP/R2QK1R1  w Qq - 0 18, c3-e2",
            "8/8/8/5Q2/1k1q4/2r2NK1/8/8 w - - 0 1, f3-d4",
            "r1lqkl1r/pppppppp/2n2n2/8/4P3/2N2N2/PPPP1PPP/R1LQKL1R  b KQkq e3 0 3, a8b8",
            "1rbqkbnr/p1p1pppp/1pnp4/3P4/4PB2/2N5/PPP2PPP/R2QKBNR b KQk - 0 5, d8d7" // was bug: wrongly calc what black queen can do to protect the knight
            // do not stale mate
            , "K7/8/7p/8/1q6/4k3/8/8 b - - 0 1, b4b6"  // e.g. not with the queen
            //// Bugs from TideEval games
            , "rql1k1nr/p3p2p/7l/Q1pNNp2/8/P7/1PP2PPP/R4RK1  b k - 5 18, c5b4",            // Bug was an illegal pawn move
            "2lqklnr/1p1npppp/r1pp4/2P5/3PP3/P1N2N2/5PPP/R1LQKL1R  b KQk - 0 10, a6-a1",  // was bug: suggested illegal move (one with unfulfilled condition)
            "r1b1k2r/ppppqppp/2n1pn2/3PP1B1/1b6/2N2N2/PPP2PPP/R2QKB1R b KQkq - 0 8, f6g8",  // IS bug: n moves away, but was pinned to queen
            "rnbqk2r/pppp1ppp/5n2/2bP4/1P6/P1N2N2/4PPPP/R1BQKB1R b KQkq - 0 8, b8a6" // https://lichess.org/hK7BbAmi/black
            , "3rkb1r/p1pq1p1p/1p2bnp1/2p1P3/5B2/P1N2N2/1PQ2PPP/R4RK1 b k - 0 20, d7e7"  // e6f5|f6d5|f6h5 https://lichess.org/LZyhujqK/black
            , "r3kb2/ppp2pp1/3qp3/3n2P1/1nQPB3/8/PPP1NP2/R1B1K3 w Qq - 5 15, c1f4" // was bug in sorting of coverage pieces -> so q came bevore n, which made L have releval of 0 on f4 and move there...
            , "r1bqk2r/p1pp1ppp/2nbp3/1p6/3Pn3/1NP2N2/PP2PPPP/R1BQKB1R w KQkq - 2 8, c1g5"  // prob. same bug as one line above
            , "r3k2r/pp3ppp/4b3/1P1p4/2P1n3/N4N2/P4PPP/R3R1K1 b kq - 0 19, d5c4" // moves away defender of ne4 - https://lichess.org/mGjWE4SA/black
            , "2r1k2r/pp3ppp/4b3/1P6/2p1R3/N4N2/P4PPP/2R3K1 b k - 2 21, f7f5"    // again! in same game
            , "rnbqk2r/p2p1ppp/2p1pn2/1p4B1/1b1PP3/2NB1N2/PPP2PPP/R2Q1RK1 b kq - 3 10, f6g4" // pinned to q https://lichess.org/n4SajnZ3/black
            , "r2qr1k1/ppp2pbp/2n2np1/2B1p3/2B1P1b1/2NP1N2/PPP3PP/R2QK2R w KQ - 8 11, f3g5" // pinned to queen - https://lichess.org/nSaDkrhq/white
            , "r1b1kb1r/5ppp/p3p3/1qNn2N1/1ppPB1nP/4P3/PP1B1PP1/R2QK2R b KQkq - 4 15, d5f6"  // pinned to rook - https://lichess.org/Cos4w11H/black#29
            , "rr6/p1p1kppp/2p1qn2/5Q2/2NPP3/3P4/PP3PPP/R3KB1R w KQ - 3 17, e4e5"  // takes cover from Q ... gone
            , "r1b1kb1r/ppp1pppp/3q1n2/8/2Qn4/P4N2/1P2PPPP/RNB1KB1R w KQkq - 0 7, c4f7" // needless big blunder looses queen !=
            , "rq2kb1r/p4ppp/Qp1p1n2/2p5/4p1bP/1NN1P1P1/PPPP1P2/R1B1K2R b Qkq - 1 15, a1h8"  // did nothing, should at least make ANY move :-) and it does - game https://lichess.org/d638Kk4Q/black#29 may be hat a liChessBot-bug?
    })
    void ChessBoardGetBestMove_notThisMoveTest(String fen, String notExpectedBestMove) {
        ChessBoard board = new ChessBoard("CBGBM", fen);
        Move bestMove = board.getBestMove();
        String notExpectedMoveString = (new Move(notExpectedBestMove)).toString();
        System.out.println("" + board.getBoardName() + ": " + board.getBoardFEN() + " -> " + bestMove + " (should not be " + notExpectedMoveString+")");
        assertNotEquals( notExpectedMoveString, bestMove.toString() );
    }

    // "rnbqkbnr/pp2p1pp/3p1p2/2p3B1/8/2NP4/PPP1PPPP/R2QKBNR w KQkq - 0 4, SAVE Bg6"

    //solved bug: checkmate, but knight moved: "3q1l1r/4k1p1/p1n1Qp1p/2P5/1p4P1/5P1N/PP5P/2KRR3  b - - 0 23"
    //            because e5 is marked as blocks check (although actually the queen is giving check only)


    // Future+check
    @ParameterizedTest
    @CsvSource({
            "1r6/3Q4/8/6K1/8/k7/6P1/1r6 w - - 0 1, d7-a7"
            //// blunders from games
            , "r2qkb1r/ppp1nppp/2n5/4pbP1/8/5p1N/PPPPP1BP/R1BQK1R1 w Qkq - 0 9, g2f3|e2f3"  // why not just take pf3? + strange debaug output on moving away benefit:
                    /*100@1 Benefit helping pieces freeing way of vPce(23) on [f3] 3 ok&if{e2-any (weiß)} away from weißer Läufer} to f3.
                    ->[indirectHelp:e2d3]
                    ->[indirectHelp:e2e4] */
            , "1r4r1/1p3p1p/2k1p1pP/3p1b2/P1q2P2/K5P1/5Q2/2R4R b - - 0 40, b7b5|f5d3"  // b7b5|f5d3 bug: makes illegal move with king pinned queen
            // probably requiring move simulation of best moves
            , "r2qkb1r/pppbpppp/2np1n2/8/Q1PP4/P4N2/1P2PPPP/RNB1KB1R b KQkq - 3 5, c6d4"  // n takes covered pawn, but white first needs to save queen  https://lichess.org/LZyhujqK/black
            , "r2k2nr/pp1b1p1p/5b2/4n1p1/4Q3/2Pp2P1/PP3P1P/R3KB1R b KQ - 1 18, d7c6"  // doppelbedrohung ist möglich L->q->t
            , "r1b1k1nr/ppp2ppp/2n1p3/b1q5/8/P1NP1N2/1PPB1PPP/R2QKB1R w KQkq - 1 8, b2b4"  // fork P->l+q possible (but wins only n or l for 2Ps)
            //mate with queen
            , "3rk2r/2K1pp1p/3p1n2/1q5p/3n4/p7/1b4b1/8 b k - 17 43, b5b3"  // TODO! problem: queen typically has several lastMoveOrigin()s, but only one is stored, for now.  so mate-detector misses some
            , "r1b1k3/pp2bp2/2p5/4R1r1/2BQ4/2N3pP/PPP3P1/2KR4 w q - 1 2, d4d8" //  up to now, it does not notice that b defending mate on e7 is kin-pinned! https://lichess.org/3h9pxw0G/black#49
            // etc.
            , "3r2k1/5ppp/3p4/p1pP2P1/P1Rb1B2/r7/4K3/1R6 w - - 3 31, f4d6" // take a piece, because covering piece also needs to cover back rank mate https://lichess.org/as1rvv81#60
            // mateIn1 - but not so easy
            , "r7/5ppp/3Qbk2/3P4/4P3/2PB1NK1/PP4Pn/R6R w - - 1 27, e4e5"  // harder to see, as moving away p sets bishop free to block the rest of the kings squares - was d6f8 which blundered queen heavily
            // do not get matedIn1
            , "rnbqkbn1/pp4p1/3pp3/2p2pNr/4NQ2/3P4/PPP1PPPP/R3KB1R b KQq - 1 8, f5e4" // taking the N gives way to be mated in 1
    })
    void FUTURE_ChessBoardGetBestMove_MoveTest(String fen, String expectedBestMove) {
        ChessBoard board = new ChessBoard("CBGBM", fen);
        Move bestMove = board.getBestMove();
        assertEquals( (new Move(expectedBestMove)).toString(), bestMove.toString() );
    }


    // FUTURE do NOT choose a certain move
    @ParameterizedTest
    @CsvSource({
            //// blunders from games
            "r1bqk1nr/pp2ppbp/2n3p1/2p5/4N3/2Pp2P1/PP1N1P1P/R1BQKB1R b KQkq - 3 8, c5c4"  // do not cover a pawn with a pawn, where it has a nogo...
            // allow opponent to fork
            , "rnbqkbnr/ppp2ppp/8/4p3/3pN3/5N2/PPPPPPPP/R1BQKB1R w KQkq - 0 4, a1a1"
            //
            , "r4r1k/1ppb3p/4pp1R/p3n3/4q3/P3B3/2P2PP1/R2QKB2 w Q - 2 21, g2g3"  // do NOT allow n to give check
            , "r2n2kr/4bppp/1P2pn2/p7/5B1P/1PNK1N2/P1P3P1/4R3 b - - 0 25, f6d5"  // d5 looks coverd, but isn't because of a pin of the pawn to the le7
            , "r3r1k1/p1p1qNbp/1p3np1/4p3/2BnP3/3PB3/PPP3PP/R2NK2R w KQ - 1 15, c2c3" // wrong counter action against fork nc2 - https://lichess.org/nSaDkrhq/white#28
            // do not move away
            , "rnbqk2r/1p3pp1/4pn2/p7/1b1P2N1/2N1BQ2/1PP3KP/R4R2 b q - 0 18, f6g4"  // do NOT move away n, because this enables a mateIn1
/*ToDo*/    , "r2q3r/pp3ppp/2k1p3/8/PP2N2P/4p3/1P1N1PP1/R1Q1K2R b KQ - 0 17, c6d5"  // dont ot run into mateIn1 https://lichess.org/vR81ZGlO/black
            , "r2r3k/pp6/2nPbNpp/4p3/2P2p2/2P5/P3PPPP/3RKB1R w K - 4 20, f6d5" // do not block a own coverage of T to P by moving in between - https://lichess.org/LizReIjS/white
            , "r1b1k1nr/3p1p2/p3pbp1/7p/1p1PP1P1/1N4K1/PPP1BP1P/R1B2R2 b kq - 0 19, g8h6" // because of fork after g4g5
    })
    void FUTURE_ChessBoardGetBestMove_notThisMoveTest(String fen, String notExpectedBestMove) {
        ChessBoard board = new ChessBoard("CBGBM", fen);
        Move bestMove = board.getBestMove();
        String notExpectedMoveString = (new Move(notExpectedBestMove)).toString();
        System.out.println("" + board.getBoardName() + ": " + board.getBoardFEN() + " -> " + bestMove + " (should not be " + notExpectedMoveString+")");
        assertNotEquals( notExpectedMoveString, bestMove.toString() );
    }





    ////// Puzzles from DBs
    @ParameterizedTest
    @CsvSource({
            "008Nz,6k1/2p2ppp/pnp5/B7/2P3PP/1P1bPPR1/r6r/3R2K1 b - - 1 29,d3e2 d1d8,462,108,93,647,backRankMate mate mateIn1 middlegame oneMove,https://lichess.org/HNU4zavC/black#58,",
            "008o6,Q5k1/p1p3p1/5rP1/8/3P4/7P/q3r3/B4RK1 b - - 1 34,f6f8 a8f8,486,87,75,110,endgame mate mateIn1 oneMove,https://lichess.org/1k4lXfEi/black#68,",
            "00FHX,2r3k1/5p1p/4pP2/3p3P/8/5P2/p1b3P1/2R3K1 b - - 0 30,c2b1 c1c8,413,93,100,477,endgame hangingPiece mate mateIn1 oneMove,https://lichess.org/rztVgThB/black#60,"
    })
    void FUTURE_ChessBoardGetBestMove_PuzzleTest1(String puzzleId, String fen, String moves,
                                               String rating, String ratingDeviation, String popularity,
                                               String nbPlays,
                                               String themes, String gameUrl, String openingTags) {
        doAndTestPuzzle(fen, moves, themes);
    }

    static void doAndTestPuzzle(String fen, String expectedMoves, String themes) {
        ChessBoard board = new ChessBoard(themes, fen);
        String[] splitt = expectedMoves.trim().split(" ", 2);
        if (splitt.length==2 && splitt[1]!=null && splitt[1].length()>0) {
            // if expected moves is a series of moves, then the very first is still before the puzzle and must be moved first...
            board.doMove(splitt[0]);
            expectedMoves = splitt[1];
        }
        else
            expectedMoves = splitt[0];
        // get calculated best move
        Move bestMove = board.getBestMove();
        if (bestMove==null) {
            System.out.println("Failed on board " + board.getBoardName() + ": " + board.getBoardFEN() + ": No move?");
            assertEquals(Arrays.toString(expectedMoves.split("\\|")) , "" );
        }

        // check if correct
        boolean found = false;
        for (String expectedString : expectedMoves.split("\\|")) {
            if (expectedString.length()>4)
                expectedString = (new Move(expectedString.substring(0, 5).trim())).toString();
            //System.out.println("opt="+expectedString+".");
            if (expectedString.equalsIgnoreCase(bestMove.toString())) {
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Failed on board " + board.getBoardName() + ": " + board.getBoardFEN() + ": "
                    + bestMove.toString() + " (expected: " + expectedMoves + ")");
            assertEquals(Arrays.toString(expectedMoves.split("\\|")) , bestMove.toString() );
        }
    }



    /*  bugs+futures:

    aus https://lichess.org/hcwmIDD1#16
    korrekter zu bei direkteingabe fen-string: r1b1k1nr/ppB2ppp/8/3pn3/1b6/1P2P3/P1PP1PPP/RN1K1B1R w kq - 0 9
    aber falscher Zug wenn fen 2 ply früher + züge:   r1b1k1nr/ppB2ppp/2n5/3pN3/1b6/1P2P3/P1PP1PPP/RN1qKB1R w KQkq - 0 8


    r1lqklr1/1ppppppp/p1n2n2/8/3PP3/1LN2N2/PPPL1PPP/R2QK1R1  w Qq - 0 18
        -> suggests Ne2 although then Pe4 is no longer coverd
        --> 5 moves:  c3-b5=-290/-39/39///// c3-b1=/-19//39//// c3-d5=/-39/-50/-14/-28/// c3-e2=/33/-33//39/// c3-a4=/-27/-6///39//

    rnl1klnr/pp1pp2p/q1p2p2/2P5/3P2p1/PNN3L1/1P2PPPP/R2QKL1R  b KQkq - 0 3
        -> suggests qb5 with 0 relEval although attacked by N.

    8/1k1l1r2/1p6/pP1P4/P1P2L2/7p/7K/3R4  b - - 0 59
    tf7xf4 kommt nicht in den möglichen Zügen des t vor.
    davor war:  nf2xLd1, Tf1xnd1
     */

/****** Blunders 11.06.2023

Ok https://lichess.org/WlcTrzQw/white#18
OK https://lichess.org/2jx8QQxi/black#43
OK https://lichess.org/p8lrn3Hd/white#12

Zeitüberschreitung:
Lala- r1b1kb1r/ppp2ppp/3p1n2/1P6/2P1q3/N2n4/PB2PPPP/R2QKBNR w KQkq - 0 9, d1d3 // https://lichess.org/FV5PlYVy/white
Ok- rnb1kb1r/pppp1ppp/5n2/q7/8/P1N2N2/1PP1PPPP/R1BQKB1R w KQkq - 4 6 // https://lichess.org/Du2qkMFw/white#10
Ok  // https://lichess.org/8Fg9ca9u/black#115

Gabel vermeiden:
?- N auf c6: 1r1q1rk1/p2nppbp/2ppb1p1/6B1/3N4/1PN1P1PP/P4P2/R2Q1RK1 b - - 1 15
- 1r1qk1r1/p1p2pp1/1p1b3p/4pN2/1P4QP/Pn1p2N1/1BRP1PP1/5K1R w - - 0 24, c2c3 // statt Tc2c4->Gabel  https://lichess.org/ZGLMBHLF/white
-

Warum nicht einfach die Figur nehmen?
Ok->T lxP statt Zug auf Feld wo eingesperrt wird: 5rk1/p2qppb1/3p2pp/8/4P1b1/1PN1BPP1/P1Q4K/3R4 b - - 0 24 https://lichess.org/7Vi88ar2/black#79
Ok->T  r4rk1/pbqnbppp/1p2pn2/2Pp4/8/1P1BPN1P/PBPNQPP1/R4RK1 b - - 0 11, d7c5|b6c5  - sieht auch noch nach komischen Zug aus, der etwas decken will aber per Abzug einen Angriff frei gibt.   https://lichess.org/dhVlMZEC/black
Ok->T 1r1qk1r1/p1p1bpp1/1p5p/4p3/1PQ4P/P3N1N1/1B1p1PP1/3K3R w - - 2 29, b2e5  // https://lichess.org/ZGLMBHLF/white

Gegners Mattdrohung nicht gesehen und nicht verhindert:
- r3nrk1/pbqnbppp/4p3/2pp3Q/3N4/1P1BP2P/PBPN1PP1/R4RK1 b - - 1 13, d7f6|g7g6   // https://lichess.org/dhVlMZEC/black

Sinnlos patt statt matt in 1
O- 3rk2r/2K1pp1p/3p1n2/1q5p/3n4/p7/1b4b1/8 b k - 17 43, NOT h8g8  // https://lichess.org/YVH4LpBj/black#86
O- 3rk2r/2K1pp1p/3p1n2/1q5p/3n4/p7/1b4b1/8 b k - 17 43, NOT d8d7  // https://lichess.org/YVH4LpBj/black#86 - many mateIn1, but d8d7 is not one of those :-)


 MattIn1
- r1b1k3/pp2bp2/2p5/4R1r1/2BQ4/2N3pP/PPP3P1/2KR4 w q - 1 2 //  Future Test, does not notice that b defending mate on e7 is kin-pinned! https://lichess.org/3h9pxw0G/black#49

Ganz mieser Patzer:
- falsch 3rk2r/2K1pp1p/3p1n2/1q5p/3n4/p7/1b4b1/8 b k - 17 43, NOT d8e7 f7f6|c5e7  // prob. Problem with alternative move

Diverse...
https://lichess.org/FJIV2mju/black#20


Todo:
- a move can be avoided also by pinning the piece2Bmoved
- not b2c1 at 1r1qk1r1/p1p1bpp1/1p5p/4p3/1PQ4P/P3N1N1/1B1p1PP1/3K3R w - - 2 29,
- Abzugschach https://lichess.org/BQveVz0r/black#34
- Bug in pawn movement?  (Testzeile #7 in NOTmateIn1)
     **** Fehler: Fehlerhafter Zug: f6 -> e5 nicht möglich auf Board 8/1k5p/p4p2/4BN2/2b5/4P3/6P1/3K4  b - - 0 41.
    Failed on board crushing endgame fork short: 8/1k5p/p4p2/4BN2/2b5/4P3/6P1/3K4  b - - 0 41: c4b3 (expected: f5d6 b7c6 d6c4)




 */



}



/*
    //@Test
    void ArrayList_Test() {
        List<Integer> al1 = new ArrayList<>();
        al1.add(3);
        al1.add(8);
        System.out.println("al1: " + al1 );
        al1.sort(Comparator.naturalOrder() );
        System.out.println("al1: " + al1 );

        al1.add(1);
        al1.add(4);
        List<Integer> al2 = al1;
        System.out.println("al1: " + al1 + "  al2: " + al2);
        al2.sort(Comparator.naturalOrder() );
        al2.add(2);
        System.out.println("al1: " + al1 + "  al2: " + al2 );

        al1.add(10);
        al1.add(7);
        List<Integer> al3 = new ArrayList<>(al1);
        System.out.println("al1: " + al1 + "  al2: " + al2 + "  al3: " + al3);
        al3.sort(Comparator.naturalOrder() );
        al3.add(9);
        System.out.println("al1: " + al1 + "  al2: " + al2 + "  al3: " + al3 );

        al3.get(0);
        al3.add(9);
        System.out.println("al1: " + al1 + "  al2: " + al2 + "  al3: " + al3 );

        List<Move> ml1 = new ArrayList<>();
        List<Move> ml2 = ml1;
        ml1.add(new Move(1,2));
        ml1.add(new Move(3,4));
        List<Move> ml3 = new ArrayList<>(ml1);
        System.out.println("m1: " + ml1 + "  al2: " + ml2 + "  nl3: " + ml3 );
        ml3.get(0).setFrom(10);
        ml1.get(1).setTo(20);
        System.out.println("m1: " + ml1 + "  al2: " + ml2 + "  nl3: " + ml3 );
        ml1.add(new Move(60,61));
        ml3.add(new Move(40,48));
        ml3.remove(1);
        System.out.println("m1: " + ml1 + "  al2: " + ml2 + "  nl3: " + ml3 );

        ml2 = ml2.subList(1,3);
        System.out.println("m1: " + ml1 + "  al2 now subList: " + ml2 + "  nl3: " + ml3 );

        ml2.add(new Move(41,49));    // works
     // ml1.add(new Move(41,49)); // throws ConcurrentModificationException
        System.out.println("m1: " + ml1 + "  al2: " + ml2 + "  nl3: " + ml3 );

        ml2.remove(0);    // works
     // ml1.remove(1); // throws ConcurrentModificationException
        System.out.println("m1: " + ml1 + "  al2: " + ml2 + "  nl3: " + ml3 );
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
        **
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
    }  */

/* Bug dev by 0 ???
> position startpos moves b1c3 e7e6 g1f3 b8c6 d2d4 d7d5 c1d2 g8f6 h2h4 f6e4 d2g5 e4g5 h4g5 f8b4 a2a3 b4a5 d1d3 h7h6 g5g6 c8d7 g6f7 e8f7 a1d1 f7e7 e1d2 d8f8 b2b4 f8f4 e2e3 f4d6 b4a5 d6a3 c3b1 a3a5 d2e2 e6e5 f3e5 d7e6 b1c3 a5a6 e2d2 a6a5 d3e2 a5b4 d1e1 h8f8 e1d1 a7a5 d1e1 a5a4 e1d1 a4a3 d1e1 a3a2 e1d1 a2a1q
        =new Board: + b1c3 e7e6 g1f3 b8c6 d2d4 d7d5 c1d2 g8f6 h2h4 f6e4 d2g5 e4g5 h4g5 f8b4 a2a3 b4a5 d1d3 h7h6 g5g6 c8d7 g6f7 e8f7 a1d1 f7e7 e1d2 d8f8 b2b4 f8f4 e2e3 f4d6 b4a5 d6a3 c3b1 a3a5 d2e2 e6e5 f3e5 d7e6 b1c3 a5a6 e2d2 a6a5 d3e2 a5b4 d1e1 h8f8 e1d1 a7a5 d1e1 a5a4 e1d1 a4a3 d1e1 a3a2 e1d1 a2a1q
        Error: / by zero
        Error: [Ljava.lang.StackTraceElement;@5cb0d902
        tail: tideeval_debug.out: Datei abgeschnitten
        Log started at: Tue Jun 06 19:10:35 CEST 2023
        > uci
        tail: tideeval_debug.out: Datei abgeschnitten
        Log started at: Tue Jun 06 19:10:35 CEST 2023
<- id name TideEval 0.1
<- id author Christian Ensel
<- uciok
        > ucinewgame
<- readyok
        > isready
<- readyok
        > position startpos moves b1c3 e7e6 g1f3 b8c6 d2d4 d7d5 c1d2 g8f6 h2h4 f6e4 d2g5 e4g5 h4g5 f8b4 a2a3 b4a5 d1d3 h7h6 g5g6 c8d7 g6f7 e8f7 a1d1 f7e7 e1d2 d8f8 b2b4 f8f4 e2e3 f4d6 b4a5 d6a3 c3b1 a3a5 d2e2 e6e5 f3e5 d7e6 b1c3 a5a6 e2d2 a6a5 d3e2 a5b4 d1e1 h8f8 e1d1 a7a5 d1e1 a5a4 e1d1 a4a3 d1e1 a3a2 e1d1 a2a1q
        =new Board: + b1c3 e7e6 g1f3 b8c6 d2d4 d7d5 c1d2 g8f6 h2h4 f6e4 d2g5 e4g5 h4g5 f8b4 a2a3 b4a5 d1d3 h7h6 g5g6 c8d7 g6f7 e8f7 a1d1 f7e7 e1d2 d8f8 b2b4 f8f4 e2e3 f4d6 b4a5 d6a3 c3b1 a3a5 d2e2 e6e5 f3e5 d7e6 b1c3 a5a6 e2d2 a6a5 d3e2 a5b4 d1e1 h8f8 e1d1 a7a5 d1e1 a5a4 e1d1 a4a3 d1e1 a3a2 e1d1 a2a1q
        Error: / by zero
        Error: [Ljava.lang.StackTraceElement;@5cb0d902
*/
