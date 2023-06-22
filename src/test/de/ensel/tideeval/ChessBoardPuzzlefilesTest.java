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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Arrays;

import static de.ensel.tideeval.ChessBoardTest.doAndTestPuzzle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ChessBoardPuzzlefilesTest {

    // Puzzles from DBs
    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_410-499-mateIn1.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle4xx_mateIn1_Test(String puzzleId, String fen, String moves,
                                                             String rating, String ratingDeviation, String popularity,
                                                             String nbPlays,
                                                             String themes, String gameUrl, String openingTags) {
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;
        doAndTestPuzzle(fen, moves, themes);
    }

    // Puzzles from DBs
    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_410-499-mateIn1.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle4xx_AvoidMateIn1_Test(String puzzleId, String fen, String moves,
                                                             String rating, String ratingDeviation, String popularity,
                                                             String nbPlays,
                                                             String themes, String gameUrl, String openingTags) {
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;
        ChessBoard board = new ChessBoard("Avoid MateIn1: " + themes, fen);
        // assume that the first move don in lichess 1mate puzzles are a blunder and lead to the 1mate, so we see of engine avoids this move
        // (however it is unsure if there is a better and mate avoiding move at all...)
        Move bestMove = board.getBestMove();
        String notExpectedMoveString = (new Move(moves.substring(0, 4))).toString();
        System.out.println("" + board.getBoardName() + ": " + board.getBoardFEN() + " -> " + bestMove + " (should not be " + notExpectedMoveString+")");
        assertNotEquals( notExpectedMoveString, bestMove.toString() );
    }

    // Puzzles from DBs
    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_410-499-NOTmateIn1.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle4xx_NOTmateIn1_Test(String puzzleId, String fen, String moves,
                                                                String rating, String ratingDeviation, String popularity,
                                                                String nbPlays,
                                                                String themes, String gameUrl, String openingTags) {
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;
        doAndTestPuzzle(fen, moves, themes);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_2k-5xx.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle2k5xxTest(String puzzleId, String fen, String moves,
                                                      String rating, String ratingDeviation, String popularity,
                                                      String nbPlays,
                                                      String themes, String gameUrl, String openingTags) {
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;
        doAndTestPuzzle(fen, moves, themes);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_2k-9xx.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle2k9xxTest(String puzzleId, String fen, String moves,
                                                      String rating, String ratingDeviation, String popularity,
                                                      String nbPlays,
                                                      String themes, String gameUrl, String openingTags) {
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;
        doAndTestPuzzle(fen, moves, themes);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_2k-12xx.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle2k12xxTest(String puzzleId, String fen, String moves,
                                                       String rating, String ratingDeviation, String popularity,
                                                       String nbPlays,
                                                       String themes, String gameUrl, String openingTags) {
        doAndTestPuzzle(fen, moves, themes);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_2k-16xx.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle2k16xxTest(String puzzleId, String fen, String moves,
                                                       String rating, String ratingDeviation, String popularity,
                                                       String nbPlays,
                                                       String themes, String gameUrl, String openingTags) {
        doAndTestPuzzle(fen, moves, themes);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_2k-20xx.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle2k20xxTest(String puzzleId, String fen, String moves,
                                                       String rating, String ratingDeviation, String popularity,
                                                       String nbPlays,
                                                       String themes, String gameUrl, String openingTags) {
        doAndTestPuzzle(fen, moves, themes);
    }

    /* results:
    2023-06-01:
        lichess_db_puzzle_230601_410-499.csv:  3537 failed,  2830 passed - 54 sec
        lichess_db_puzzle_230601_5xx.csv: 18946 failed, 14815 passed - 4 min 37 sec
        lichess_db_puzzle_230601_2k-410-499.csv: 1065 failed, 935 passed - 20 sec
        lichess_db_puzzle_230601_2k-5xx.csv:     1117 failed, 883 passed - 21 sec
        lichess_db_puzzle_230601_2k-9xx.csv:     1443 failed, 557 passed - 24 sec
        lichess_db_puzzle_230601_2k-12xx.csv:    1541 failed, 459 passed - 24 sec
        lichess_db_puzzle_230601_2k-16xx.csv:    1603 failed, 397 passed - 24 sec
        lichess_db_puzzle_230601_2k-20xx.csv:    1615 failed, 385 passed - 24 sec
     after enabling calcBestMove() to obey checks, king-pins etc.:
        lichess_db_puzzle_230601_2k-410-499.csv: 922 failed, 1078 passed - 16 sec
        lichess_db_puzzle_230601_2k-5xx.csv:     977 failed, 1023 passed - 15 sec
        lichess_db_puzzle_230601_2k-9xx.csv:     1363 failed, 637 passed - 16 sec
        lichess_db_puzzle_230601_2k-12xx.csv:    1437 failed, 563 passed - 19 sec
        lichess_db_puzzle_230601_2k-16xx.csv:    1537 failed, 463 passed - 24 sec
        lichess_db_puzzle_230601_2k-20xx.csv:    1540 failed, 460 passed - 19 sec
     2023-06-03: -> commit+push
        lichess_db_puzzle_230601_2k-410-499.csv: 935 failed, 1065 passed - 17 sec
        lichess_db_puzzle_230601_2k-5xx.csv:    1022 failed,  978 passed - 17 sec
        lichess_db_puzzle_230601_2k-9xx.csv:    1497 failed,  603 passed - 18 sec
        lichess_db_puzzle_230601_2k-12xx.csv:   1494 failed,  506 passed - 19 sec
        lichess_db_puzzle_230601_2k-16xx.csv:   1583 failed,  417 passed - 24 sec
        lichess_db_puzzle_230601_2k-20xx.csv:   1595 failed,  405 passed - 20 sec
     2023-06-05: -first games on lichess !!
        lichess_db_puzzle_230601_2k-410-499.csv: 917 failed, 1083 passed - 20 sec
        lichess_db_puzzle_230601_2k-5xx.csv:    xx failed,  xx passed - 18 sec
        lichess_db_puzzle_230601_2k-9xx.csv:    xx failed,  xx passed - 20 sec
        lichess_db_puzzle_230601_2k-12xx.csv:   xx failed,  xx passed - 20 sec
        lichess_db_puzzle_230601_2k-16xx.csv:   xx failed,  xx passed - 27 sec
        lichess_db_puzzle_230601_2k-20xx.csv:   xx failed,  xx passed - 29 sec
     2023-06-06:
        lichess_db_puzzle_230601_2k-410-499.csv: 876 failed, 1124 passed - 22 sec
        lichess_db_puzzle_230601_2k-9xx.csv:    1309 failed,  691 passed - 29 sec
     2024-06-08:
        lichess_db_puzzle_230601_2k-410-499.csv: 899 failed, 1101 passed - 24 sec
new:    lichess_db_puzzle_230601_410-499-mateIn1.csv:    1582 failed, 2150 passed - 45 sec
new:    lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 1428 failed, 1207 passed - 31 sec
        lichess_db_puzzle_230601_2k-9xx.csv:    1360 failed,  640 passed - 29 sec
        lichess_db_puzzle_230601_2k-20xx.csv:   1538 failed,  462 passed - 29 sec
    2023-06-10:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    1609 failed, 2123 passed - 46 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 1396 failed, 1239 passed - 29 sec
        lichess_db_puzzle_230601_2k-5xx.csv:              982 failed, 1018 passed - 25 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             1342 failed,  658 passed - 25 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            1416 failed,  584 passed - 29 sec
        lichess_db_puzzle_230601_2k-16xx.csv:            1506 failed,  494 passed - 31 sec
        lichess_db_puzzle_230601_2k-20xx.csv:            1520 failed,  480 passed - 30 sec
Ok-Ok-Ok: Forget the history above, I now noticed only now that the puzzle cvs files first contain a move that
needs to be done and THEN the puzzle starts... :-o
--> completely new results in time and quality...:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    1784 failed, 1948 passed - 70 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 1736 failed,  899 passed - 77 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             1212 failed,  788 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             1427 failed,  573 passed - 42 sec
        lichess_db_puzzle_230601_2k-20xx.csv:            1581 failed,  419 passed - 45 sec
+ I added a first mateInOne check (which does not yet seem to be perfect, but already does a good job :-)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    917 failed, 2815 passed - 70 sec
                                        AvoidMateIn1:   2037 failed, 1695 passed - 49 sec   // 1644 are passed even without mate-detection in Square.calcCheckBlockingOptions()
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 800 failed, 1835 passed - 44 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             606 failed, 1394 passed - 39 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1204 failed,  796 passed - 45 sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1397 failed,  603 passed - 49 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1512 failed,  488 passed - 50 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1553 failed,  447 passed - 45 sec
    2023-06-16: some more corrections
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    875 failed, 2857 passed - 111 sec
                                        AvoidMateIn1:   2045 failed, 1687 passed - 65 sec   // 1644 are passed even without mate-detection in Square.calcCheckBlockingOptions()
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 753 failed, 1882 passed - xx sec
        lichess_db_puzzle_230601_2k-5xx.csv:             590 failed, 1410 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1100 failed,  900 passed - 65 sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1331 failed,  669 passed - 49 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1456 failed,  544 passed - 68 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1540 failed,  460 passed - 58 sec
        all: passed 10593 of 20099
    2023-06-17am: some more corrections
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    891 failed, 2841 passed - xx sec
                                        AvoidMateIn1:   1992 failed, 1740 passed - 49 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 756 failed, 1879 passed - 44 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             574 failed, 1426 passed - 39 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1492 failed,  508 passed - 50 sec
    2023-06-18am: pawn beating corrections
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    868 failed, 2864 passed - xx sec
                                        AvoidMateIn1:   2042 failed, 1690 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 742 failed, 1893 passed - 56 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             562 failed, 1438 passed - 46 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1009 failed,  991 passed - xx sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1268 failed,  732 passed - 54 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1417 failed,  583 passed - 56 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1502 failed,  498 passed - 58 sec
    2023-06-21am:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    751 failed, 2981 passed - 80 sec
                                        AvoidMateIn1:   2041 failed, 1691 passed - 50 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 710 failed, 1925 passed - 51 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             508 failed, 1492 passed - 49 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1100 failed,  900 passed - 65 sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1233 failed,  767 passed - 50 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1397 failed,  603 passed - 68 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1481 failed,  519 passed - 54 sec

    2023-06-22pm:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    690 failed, 3042 passed - xx sec
                                        AvoidMateIn1:   2156 failed, 1576 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 669 failed, 1966 passed - 59 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             481 failed, 1519 passed - 50 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1014 failed,  986 passed - 56 sec
    2023-06-23am: after improvements in calcCheckBlockingOptions()
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    305 failed, 3427 passed - 93 sec
                                        AvoidMateIn1:   2066 failed, 1666 passed - 55 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 438 failed, 2197 passed - 55 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             312 failed, 1688 passed - 50 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             942 failed, 1058 passed - 54 sec
*/



}
