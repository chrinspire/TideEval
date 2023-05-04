/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static org.junit.jupiter.api.Assertions.*;
import static de.ensel.tideeval.ChessBoardTest.checkRelEvalOnSquareOfVPce;

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
        board.completeCalc();
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
        board.completeCalc();
        assertTrue(board.doMove("Na3"));
        knightW1pos += KNIGHT_DIR_LELEUP;
        // we need a black piece to move, so the knight can move back,,
        pawnB1Id = board.spawnPieceAt(PAWN_BLACK,15);
        board.completeCalc();
        assertTrue(board.doMove("h5"));
        // but then the rook pins the knight to the king
        rookB1pos = kingWpos+4*UP;
        rookB1Id = board.spawnPieceAt(ROOK_BLACK,rookB1pos);
        board.completeCalc();

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
        board.completeCalc();
        assertEquals(0,board.getBoardSquares()[knightW1pos].clashEval());

        //but a black bishop now attacks additionally
        int bishopB1pos = rookB1pos+2*RIGHT;
        int bishopB1Id = board.spawnPieceAt(BISHOP_BLACK,bishopB1pos);
        board.completeCalc();
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
           a  b  c  d  e  f  g  h    */  //a3: [-280, +320, -320, +530]

        //knight is now covered by a pawn, so it should be safe
        int pW1pos = kingWpos+UPRIGHT;
        int pW1Id = board.spawnPieceAt(PAWN,pW1pos);
        board.completeCalc();
        debugPrintln(DEBUGMSG_TESTCASES, board.getBoardFEN() );
        // expected==0, because black will not take
        // but expected==-70 if code for bishop behind pawn etc. is not active
        assertEquals(0,board.getBoardSquares()[knightW1pos].clashEval());

        //but a black pawn now attacks additionally
        int pB1pos = knightW1pos+UPRIGHT;
        int pB1Id = board.spawnPieceAt(PAWN_BLACK,pB1pos);
        board.completeCalc();
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
        board.completeCalc();
        // correct+wanted: expected==0, because black will not take
        // but expected==-70 if code for bishop behind pawn etc. is not active
        // should work, but fails due to open issue: see todo in CD, line 326, which leads to incorrect d-correction for white bishop after pawn takes
        // may be temporarily fixed with return 1 in movingMySquaresPieceAwayDistancePenalty(), if piece canNOT move away reasonably.
        assertEquals(-70,board.getBoardSquares()[knightW1pos].clashEval());
    }


    @Test
    void chessBoardSquaresVPS_getFirstMovesToHere_Test() {
        ChessBoard board = new ChessBoard("TestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int rookW1pos = A1SQUARE;
        board.spawnPieceAt(ROOK, rookW1pos);
        int rookW1Id = board.getPieceIdAt(rookW1pos);
        int rookW2pos = 62;
        int rookB1pos = 1;
        board.spawnPieceAt(ROOK, rookW2pos);
        board.spawnPieceAt(ROOK_BLACK, rookB1pos);
        int rookW2Id = board.getPieceIdAt(rookW2pos);
        int rookB1Id = board.getPieceIdAt(rookB1pos);
        board.completeCalc();
        /*
        8 ░x░rB1░░░   ░░░   ░x░ 0
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5  W ░B░ 0 ░░░   ░░░ w ░0░
        4 ░░░   ░░░   ░░░   ░░░
        3    ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1 RW1░W░ W ░░░   ░░░RW2░W░
           A  B  C  D  E  F  G  H    */
        assertEquals("[a1-d1]", board.getBoardSquares()
                [coordinateString2Pos("d1")].getvPiece(rookW1Id).getFirstMovesToHere().toString());
        assertEquals("[a1-a3, a1-d1]", board.getBoardSquares()
                [coordinateString2Pos("d3")].getvPiece(rookW1Id).getFirstMovesToHere().stream().map(m->m.toString()).sorted().collect(Collectors.toList()).toString());
        assertEquals("[a1-h1]", board.getBoardSquares()
                [coordinateString2Pos("h1")].getvPiece(rookW1Id).getFirstMovesToHere().toString());
        assertEquals("[a1-a7, a1-b1]", board.getBoardSquares()
                [coordinateString2Pos("h8")].getvPiece(rookW1Id).getFirstMovesToHere().stream().map(m->m.toString()).sorted().collect(Collectors.toList()).toString());
        assertEquals("[b8-b3, b8-d8]", board.getBoardSquares()
                [coordinateString2Pos("d3")].getvPiece(rookB1Id).getFirstMovesToHere().stream().map(m->m.toString()).sorted().collect(Collectors.toList()).toString());

        /* add two kings -> they should block some of the ways and increase the distances,
                            but also are interesting with long distances accross the board...
        8 ░░░ r1░k░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5    ░░░   ░░░   ░░░   ░░░
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
        assertEquals("[c3-d3]", board.getBoardSquares()
                [coordinateString2Pos("d3")].getvPiece(kingWId).getFirstMovesToHere().stream().map(m->m.toString()).sorted().collect(Collectors.toList()).toString());
        assertEquals("[c3-d2, c3-d3, c3-d4]", board.getBoardSquares()
                [coordinateString2Pos("h1")].getvPiece(kingWId).getFirstMovesToHere().stream().map(m->m.toString()).sorted().collect(Collectors.toList()).toString());
        assertEquals("[c8-b7, c8-c7, c8-d7]", board.getBoardSquares()
                [coordinateString2Pos("d3")].getvPiece(kingBId).getFirstMovesToHere().stream().map(m->m.toString()).sorted().collect(Collectors.toList()).toString());
        if (MAX_INTERESTING_NROF_HOPS<7)
            assertEquals("[]", board.getBoardSquares()
                    [coordinateString2Pos("h1")].getvPiece(kingBId).getFirstMovesToHere().toString() );
        assertEquals("[c8-b7, c8-c7, c8-d7]", board.getBoardSquares()
                [coordinateString2Pos("g2")].getvPiece(kingBId).getFirstMovesToHere().stream().map(m->m.toString()).sorted().collect(Collectors.toList()).toString());
        assertEquals("[c8-c7, c8-d7]", board.getBoardSquares()
                [coordinateString2Pos("g3")].getvPiece(kingBId).getFirstMovesToHere().stream().map(m->m.toString()).sorted().collect(Collectors.toList()).toString());
        //ToDo-Bug? why is h4 not working, but producing an empty result?
        // assertEquals("[Move{c8-c7}, Move{c8-d7}]", board.getBoardSquares()
        //      [coordinateString2Pos("g3")].getvPiece(kingBId).getFirstMovesToHere().stream().map(m->m.toString()).sorted().collect(Collectors.toList()).toString());

    }

    @Test
    void chessBoardSquaresUpdateClashResultAndRelEvals4SlidingPieces_Test() {
        ChessBoard board = new ChessBoard("TestBoard", FENPOS_EMPTY);
        // put a few pieces manually:
        int rookW1pos = A1SQUARE;
        board.spawnPieceAt(ROOK, rookW1pos);
        int rookW1Id = board.getPieceIdAt(rookW1pos);
        int rookW2pos = 62;
        int rookB1pos = 1;
        board.spawnPieceAt(ROOK, rookW2pos);
        board.spawnPieceAt(ROOK_BLACK, rookB1pos);
        int rookW2Id = board.getPieceIdAt(rookW2pos);
        int rookB1Id = board.getPieceIdAt(rookB1pos);
        board.completeCalc();
        /*
        8 ░x░rB1░░░   ░░░   ░x░ 0
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5  W ░B░ 0 ░░░   ░░░ w ░0░
        4 ░░░   ░░░   ░░░   ░░░
        3    ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1 RW1░W░ W ░░░   ░░░RW2░W░
           A  B  C  D  E  F  G  H    */

        // clashes should be 0 everywhere now
        for (String c : new String[]{ "a5", "b5","c5", "g5","h5",
                "a1", "b1", "c1", "g1",
                "a8", "b8", "g8", "h8" }) {
            checkSquareDirectClashResult(0, board, coordinateString2Pos(c));
        }

        // check relEvals where rookB1Id can go
        for (String c : new String[]{ "b5", "c5",   "h5", "b8", "h8" })
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookB1Id );

        // check relEvals where rookB1Id canNOT go
        for (String c : new String[]{"a5", "g5",   "b1", "c1",    "a8", "g8" })
            checkRelEvalOnSquareOfVPce(-board.getPieceAt(rookB1pos).getValue(),
                    board, coordinateString2Pos(c), rookB1Id );
        // TODO: testcase for "a1", "g1" -> would be 0 (rook exchange), but NoGo on every the way

        // check relEvals where rookW1Id can go
        for (String c : new String[]{"a5", "c5", "h5",  "b1",  "g8"})
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookW1Id );
        for (String c : new String[]{"g5",   "a1", "c1", "h1" })
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookW1Id );
        // TODO: testcase+decision for "g1" -> can WR1 go on place of WR2?

        // check relEvals where rookW1Id cannot go
        for (String c : new String[]{ "b5",   "a8", "h8" })
            checkRelEvalOnSquareOfVPce(-board.getPieceAt(rookW1pos).getValue(),
                    board, coordinateString2Pos(c), rookW1Id );
//TODO-Bug:  "b8" -> works here, but shuold not, as NoGo on all ways there.

        // check relEvals where rookW2Id can go
        for (String c : new String[]{"a5", "c5", "h5",  "b1",  "a8"})
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookW2Id );
        for (String c : new String[]{"g5",   "c1", "h1" })
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookW2Id );

        // check relEvals where rookW2Id cannot go
        for (String c : new String[]{ "b5",   "g8", "h8" })
            checkRelEvalOnSquareOfVPce(-board.getPieceAt(rookW2pos).getValue(),
                    board, coordinateString2Pos(c), rookW2Id );


        /* add two pieces -> they should block some of the ways and increase the distances
        8 ░x░rB1░░░   bB1   ░x░ 0
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░bB2░░░   ░░░
        5  W ░B░ B ░░░   ░░░ w ░B░
        4 ░░░   ░░░   ░░░   ░░░
        3    ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1 RW1░W░ W ░░░   ░░░RW2░W░
           a  b  c  d  e  f  g  h    */
        // test if distances are updated

        int bishopB1pos = 4;
        int bishopB2pos = 4+DOWNLEFT+DOWN;
        board.spawnPieceAt(BISHOP_BLACK,bishopB1pos);
        board.spawnPieceAt(BISHOP_BLACK,bishopB2pos);
        board.completeCalc();

        // clashes should be 0 everywhere now
        for (String c : new String[]{ "a5", "b5","c5", "g5","h5",
                "a1", "b1", "c1", "g1",
                "d6",
                "a8", "b8", "e8", "g8", "h8" }) {
            checkSquareDirectClashResult(0, board, coordinateString2Pos(c));
        }

        // check relEvals where rookB1Id can go
        for (String c : new String[]{ "b5", "c5",  "a3", "g3",  "g6",  "h5", "b8", "h8" })
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookB1Id );

        // check relEvals where rookB1Id canNOT go
        for (String c : new String[]{ "a5", "g5",   "b1", "c1",    "a8", "g8" })
            checkRelEvalOnSquareOfVPce(-board.getPieceAt(rookB1pos).getValue(),
                    board, coordinateString2Pos(c), rookB1Id );
        // TODO: testcase for "a1", "g1" -> would be 0 (rook exchange), but NoGo on every way

        // check relEvals where rookW1Id can go
        for (String c : new String[]{"a5",  "b1",  "g8", "h8"})
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookW1Id );
        for (String c : new String[]{"g5",   "a1", "c1", "h1" })
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookW1Id );
        // TODO: testcase+decision for "g1" -> can WR1 go on place of WR2?
// TODO-bug?: testcase for "b8" -> would be 0 (take rook+bishop takes back), but NoGo on every way

        // check relEvals where rookW1Id cannot go
        for (String c : new String[]{ "b5", "c5", "h5",  "a8", "f8" })
            checkRelEvalOnSquareOfVPce(-board.getPieceAt(rookW1pos).getValue(),
                    board, coordinateString2Pos(c), rookW1Id );

        // check relEvals where rookW2Id can go
        for (String c : new String[]{"a5",  "b1",  "a8", "g8", "h8"})
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookW2Id );
        for (String c : new String[]{"g5",   "c1", "h1" })
            checkRelEvalOnSquareOfVPce(0, board, coordinateString2Pos(c), rookW2Id );
// TODO-bug?: testcase for "b8" -> would be 0 (take rook+bishop takes back), but NoGo on every way

        // check relEvals where rookW2Id cannot go
        for (String c : new String[]{ "b5", "c5", "e5", "h5" })
            checkRelEvalOnSquareOfVPce(-board.getPieceAt(rookW2pos).getValue(),
                    board, coordinateString2Pos(c), rookW2Id );

    }

    private void checkSquareDirectClashResult(int expected, ChessBoard board, int pos) {
        int actual = board.getBoardSquares()[pos].getClashes()[0];
        if (expected!=actual ) {
            debugPrintln(true, "LAST INFO....: "
                    + "(actual="+actual
                    +" != expected="+expected+" "+board.getBoardSquares()[pos].getClashes()[0]+")" );
        }
        assertEquals(expected, actual );
        //debugPrintln(true, "clashresult for " + squareName(pos) + " ok." );
    }


}