/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.junit.jupiter.api.Test;

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
        //checkCondDistance( 4, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  //  now 4
        //ok, for now:
        checkUnconditionalDistance( 4, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  //  now 4
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
        checkNoGoDistance( 4, board, pB1pos, pW2Id);  // via g4(some black came here),g5,g6(moves away),g7
        checkNoGoDistance( 5, board,/*2*/  pB1pos+UP,pW2Id);  // by beating pB2
        checkNoGoDistance( 5, board,/*.*/  bishopB1pos,pW2Id);  // by beating pB2+pB1
        checkNoGoDistance( 5, board,/*4*/  pB1pos+UPRIGHT,pW2Id);  //  by beating pB2+straight
        checkNoGoDistance( 3, board,/*.*/  pB2pos,pW2Id);
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
        assertEquals(coordinateString2Pos("e1"), chessBoard.getWhiteKingPos());
        assertEquals(ROOK, chessBoard.getPieceTypeAt(coordinateString2Pos("h1")));
        assertTrue(chessBoard.doMove("O-O"));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("e1")));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("h1")));
        assertEquals(KING, chessBoard.getPieceTypeAt(coordinateString2Pos("g1")));
        assertEquals(coordinateString2Pos("g1"), chessBoard.getWhiteKingPos());
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
        assertEquals(coordinateString2Pos("e8"), chessBoard.getBlackKingPos());
        assertEquals(ROOK_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("a8")));
        assertTrue(chessBoard.doMove("O-O-O"));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("e8")));
        assertEquals(EMPTY, chessBoard.getPieceTypeAt(coordinateString2Pos("a8")));
        assertEquals(KING_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("c8")));
        assertEquals(coordinateString2Pos("c8"), chessBoard.getBlackKingPos());
        assertEquals(ROOK_BLACK, chessBoard.getPieceTypeAt(coordinateString2Pos("d8")));

        assertTrue(chessBoard.doMove("a4??"));
        assertTrue(chessBoard.doMove("a5??"));
        assertTrue(chessBoard.doMove("Ba3?"));
        assertTrue(chessBoard.doMove("Kb7?!"));
        assertTrue(chessBoard.doMove("Rab1?"));
        assertTrue(chessBoard.doMove("Ne5"));
        String newFen = chessBoard.getBoardFEN();  // TODO
        assertEquals("3r1l1r/1k3qpp/1ppp1n2/p3n3/P3P3/L1N2N1Q/5PPP/1R2R1K1  w - - 4 19",
                newFen);
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
    void FUTURE_getBestMove_doNotMoveAwayWhenKingPinned_Test() {
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

    /*  bugs:
    rnl1klnr/pp1pp2p/q1p2p2/2P5/3P2p1/PNN3L1/1P2PPPP/R2QKL1R  b KQkq - 0 3
        -> suggests qb5 with 0 relEval although attacked by N.

    8/1k1l1r2/1p6/pP1P4/P1P2L2/7p/7K/3R4  b - - 0 59
    tf7xf4 kommt nicht in den möglichen Zügen des t vor.
    davor war:  nf2xLd1, Tf1xnd1
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



