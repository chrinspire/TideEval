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

import static de.ensel.tideeval.ChessBoardTest.doAndTestPuzzle;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ChessBoardPuzzlefilesTest {

    // Puzzles from DBs
    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_410-499-mateIn1.csv",
            numLinesToSkip = 0)
    void PARTLY_ChessBoardGetBestMove_Puzzle4xx_mateIn1_Test(String puzzleId, String fen, String moves,
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
    void PARTLY_ChessBoardGetBestMove_Puzzle4xx_AvoidMateIn1_Test(String puzzleId, String fen, String moves,
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
    void PARTLY_ChessBoardGetBestMove_Puzzle4xx_NOTmateIn1_Test(String puzzleId, String fen, String moves,
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
    void PARTLY_ChessBoardGetBestMove_Puzzle2k5xxTest(String puzzleId, String fen, String moves,
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
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;
        doAndTestPuzzle(fen, moves, themes);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_2k-16xx.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle2k16xxTest(String puzzleId, String fen, String moves,
                                                       String rating, String ratingDeviation, String popularity,
                                                       String nbPlays,
                                                       String themes, String gameUrl, String openingTags) {
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;
        doAndTestPuzzle(fen, moves, themes);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "lichess_db_puzzle_230601_2k-20xx.csv",
            numLinesToSkip = 0)
    void FUTURE_ChessBoardGetBestMove_Puzzle2k20xxTest(String puzzleId, String fen, String moves,
                                                       String rating, String ratingDeviation, String popularity,
                                                       String nbPlays,
                                                       String themes, String gameUrl, String openingTags) {
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;
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
    2023-06-23pm: after improvements in calcCheckBlockingOptions()
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    328 failed, 3404 passed - 93 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 446 failed, 2635 passed - 55 sec
    2024-06-23Am: after improvements for/against pinning()
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    339 failed, 3393 passed - 82 sec
    2023-06-26am; 3/7 best moves + many more...
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 471 failed, 2160 passed - 55 sec
                                        AvoidMateIn1:   1820 failed, 1912 passed - 55 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1012 failed,  988 passed - 52 sec
    2023-06-26pm: several calc corrections
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    377 failed, 3355 passed - 101 sec
                                        AvoidMateIn1:   1976 failed, 1756 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 410 failed, 2225 passed - xx sec
        lichess_db_puzzle_230601_2k-5xx.csv:             314 failed, 1686 passed - 48 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             833 failed, 1167 passed - 64 sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1153 failed,  847 passed - 54 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1338 failed,  662 passed - 63 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1480 failed,  520 passed - 67 sec
     2023-06-29am: try to cover opponents best move targets
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    356 failed, 3376 passed - 110 sec
                                        AvoidMateIn1:   1833 failed, 1899 passed - 59 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 420 failed, 2215 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             846 failed, 1154 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1488 failed,  512 passed - 65 sec
    AllTests: 7765 failed, 12490 passed of 20255 tests
     2023-06-29pm: anti-draw evaluations (via board hashes) added
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    362 failed, 3370 passed - 97 sec
                                        AvoidMateIn1:   1848 failed, 1884 passed - 59 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 417 failed, 2218 passed - 62 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             851 failed, 1149 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1502 failed,  498 passed - 65 sec
     2023-07-04am - v0.24
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    279 failed, 3453 passed - 98 sec
                                        AvoidMateIn1:   1821 failed, 1911 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 320 failed, 2315 passed - 66 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             658 failed, 1342 passed - 60 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            965 failed, 1035 passed - 63 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1403 failed,  597 passed - 66 sec
     2023-07-05am - was never prod
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    223 failed, 3509 passed - 94 sec
                                        AvoidMateIn1:   1758 failed, 1974 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 278 failed, 2357 passed - 55 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             570 failed, 1430 passed - 57 sec
     2023-07-07pm - hmm, bugs fixed, but really improved?
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    291 failed, 3509 passed - 94 sec
                                        AvoidMateIn1:   1777 failed, 1955 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 299 failed, 2336 passed - 55 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             565 failed, 1435 passed - 57 sec
     2023-07-07pm - (online as, but not yet pushed) v.25 - added next best move benefit to checking moves
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    279 failed, 3453 passed - 94 sec
                                        AvoidMateIn1:   1776 failed, 1956 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 294 failed, 2341 passed - 55 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             561 failed, 1439 passed - 63 sec
slightly bevor last bugfix:
        lichess_db_puzzle_230601_2k-5xx.csv:             266 failed, 1734 passed - 60 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            873 failed, 1127 passed - 70 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1128 failed,  872 passed - 68 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1465 failed,  635 passed - 66 sec
     2023-07-09 - pushed v.25 - corrected fork(ish) calculation
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    279 failed, 3453 passed - xx sec
                                        AvoidMateIn1:   1788 failed, 1944 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 284 failed, 2351 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             561 failed, 1439 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1361 failed,  639 passed - xx sec
     2023-07-11 - v.26pre - handles multiple (equal) move origins
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    327 failed, 3405 passed - xx sec
                                        AvoidMateIn1:   1880 failed, 1852 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 293 failed, 2342 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             xx failed, 1439 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           xx failed,  639 passed - xx sec
     2023-07-11 - v.26 - some fixes - in sum a little worse! + much slower?
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    336 failed, 3396 passed - 260 sec !?!
                                        AvoidMateIn1:   1897 failed, 1835 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 341 failed, 2294 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             658 failed, 1342 passed - 156 sec // mit MAX 4P 11B
        lichess_db_puzzle_230601_2k-9xx.csv:             636 failed, 1364 passed - 166 sec // mit MAX 7P 20B
        lichess_db_puzzle_230601_2k-9xx.csv:             636 failed, 1364 passed - 130 sec // mit MAX 7P 20B - RemeberPredecessor activated
        lichess_db_puzzle_230601_2k-9xx.csv:             636 failed, 1364 passed - 120 sec // mit MAX 7P 20B - RemeberFirstMovesToHere activated
        lichess_db_puzzle_230601_2k-20xx.csv:           1352 failed,  648 passed - 184 sec
     2023-07-13am - v.27 - introduce new mobility + slighly take it into account in move benefits  // all with MAX 4P 11B
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    319 failed, 3414 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             618 failed, 1382 passed - 109 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1342 failed,  658 passed - 104 sec
     2023-07-14pm - v.28pre
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    321 failed, 3412 passed - xx sec
                                        AvoidMateIn1:   1863 failed, 1869 passed - 99 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 314 failed, 2321 passed - 93 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             274 failed, 1726 passed - 81 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             582 failed, 1418 passed - 95 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            897 failed, 1103 passed - 105 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1194 failed,  806 passed - 107 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1364 failed,  636 passed - 106 sec
     2023-07-15am - v.28
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    280 failed, 3452 passed - 150 sec
                                        AvoidMateIn1:   1871 failed, 1861 passed - 94 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 462 failed, 2171 passed - 93 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             337 failed, 1662 passed - 76 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             650 failed, 1350 passed - 86 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            897 failed, xx passed - 105 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1194 failed,  xx passed - 107 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1364 failed,  xx passed - 106 sec
     2023-07-16am - v.28ck2 - several evaluation+futirelevel corrections - but worse?
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    367?? 422? failed, 3365 passed - xx sec
                                        AvoidMateIn1:   1820 failed, 1912 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 582 failed, 2053 passed - 90 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             410 failed, 1590 passed - 76 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             730 failed, 1270 passed - 88 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            955 failed, 1045 passed - 102 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1202 failed,  798 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1375 failed,  625 passed - 102 sec
     2023-07-18pm - v.28ck5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    439 failed, 3293 passed - 148 sec
                                        AvoidMateIn1:   1868 failed, 1864 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             741 failed, 1259 passed - 98 sec
     2023-07-21am - v.29pre1              // with coveringVPce.addClashContrib(-benefit);   // and without
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    488 failed, 3732 passed - 154 sec    474 f
                                        AvoidMateIn1:   1843 failed, 1889 passed - 97 sec    1853 f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 590 failed, 2045 passed - 93 sec     587 failed
        lichess_db_puzzle_230601_2k-9xx.csv:             757 failed, 1243 passed - 98 sec     756 f
     same but with setShortest... statt getPredecessors in trapping-code                    // without
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    453 failed, 3732 passed - 154 sec    453 f
                                        AvoidMateIn1:   1867 failed, 1889 passed - 97 sec    1867 f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 566 failed, 2069 passed - 93 sec     566 failed
        lichess_db_puzzle_230601_2k-9xx.csv:             744 failed, 1243 passed - 98 sec     744 f
     2023-07-23am - v.29
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    445 failed, 3287 passed - xx sec
                                        AvoidMateIn1:   1866 failed, 1866 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 598 failed, 2037 passed - 92 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             728 failed, 1272 passed - xx sec
        lichess_db_puzzle_230601_2k-12xx.csv:            950 failed, 1050 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1328 failed,  672 passed - 89 sec
     2023-08-01 - v.29d (with !emptySquare instead of isEmptySquare in addChance...)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    396 failed, 3336 passed - 151 sec
                                        AvoidMateIn1:   1893 failed, 1839 passed - 79 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 637 failed, 1998 passed - 92 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             727 failed, 1273 passed - 80 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            946 failed, 1054 passed - 89 sec
     2023-08-01 - v.29e
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    406 failed, 3326 passed - 133 sec
                                        AvoidMateIn1:   1913 failed, 1819 passed - 77 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 562 failed, 2073 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             727 failed, 1273 passed - 79 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            942 failed, 1058 passed - 82 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1347 failed,  653 passed - 87 sec
     2023-08-01 - v.29h
        Score of 0.26 cs 0.29i: 20 - 24 - 36
        Score of SF14.1/0ply vs. 0.29h: 78 - 0 - 2
        Score of SF14.1/4ply/1600 vs. 0.29h: 324 - 31 - 45
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    391 failed, 3341 passed - 140 sec

     2023-08-02 - v.29i
        Score of 0.26 cs 0.29i: 21 - 25 - 34
        Score of SF14.1/0ply vs. 0.29i: 79 - 1 - 0
        Score of SF14.1/4ply/1600 vs. 0.29i: 328 - 31 - 41
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    399 failed, 3333 passed - 139 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 510 failed, 2125 passed - 77 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             713 failed, 1287 passed - 76 sec

     2023-08-08 - v.30pre1 - test with different moveEval-Comparison, considering futureLevels more
        better in non-mateIn1-puzzles,
            BUT Score of 0.29i cs 0.30pre1: 58 - 5 - 17
            AND Score of 0.26 cs 0.30pre1: 54 - 13 - 13
            AND Score of SF14.1/4ply/1600 vs. 0.30pre1: 382 - 9 - 9
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    441 failed, 3291 passed - 138 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 454 failed, 2181 passed - 82 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             674 failed, 1326 passed - 84 sec

     2023-08-08 - v.29j
        Score of 0.26 cs 0.29j: 15 - 30 - 35
        Score of SF14.1/0ply vs. 0.29j: 76 - 1 - 3
        Score of SF14.1/4ply/1600 vs. 0.29j: 324 - 23 - 53
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    398 failed, 3334 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 508 failed, 2127 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             724 failed, 1276 passed - xx sec
   2023-08-10 - v.29k
        Score of 0.26 vs. 0.29k: 17 - 30 - 33
        Score of SF14.1/0ply vs. 0.29k: 75 - 1- 4
        Score of SF14.1/4ply/1600 vs. 0.29k: 309 - 41 - 50
        resp. Score of SF14.1/4ply/1600 vs. 0.29k: 323 - 32 - 45
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    409 failed, 3732 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 522 failed, 2113 passed - 79 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             733 failed, 1267 passed - 78 sec
     2023-08-10 - v.29m
        Score of 0.26 vs. 0.29m: 20 - 29 - 31
        Score of SF14.1/0ply vs. 0.29m: 73 - 2 - 5
        Score of SF14.1/4ply/1600 vs. 0.29m: 313 - 40 - 47
        resp. Score of SF14.1/4ply/1600 vs. 0.29m:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    399 failed, 3333 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 522 failed, 2113 passed - 82 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             726 failed, 1267 passed - xx sec
     2023-08-10 - v.29p-pre
        Score of 0.26 vs TideEval 0.29p: 17 - 24 - 39  [0.456] 80
        Score of SF14.1/0ply vs. 0.29m: 73 - 3 - 4
        Score of SF14.1/4ply/1600 vs. 0.29m: 310 - 33 - 57
     2023-08-10 - v.29p
        Score of 0.26 vs TideEval 0.29p: 10 - 26 - 44  [0.400] 80
        Score of SF14.1/0ply vs. 0.29m?: 71 - 2 - 7
        Funfact: Score of Stockfish **11 64** vs TideEval 0.29p: 79 - 0 - 1  [0.994] 80
        Score of SF14.1/4ply/1600 vs. 0.29m?: 304 - 34 - 62
        lichess_db_puzzle_230601_2k-9xx.csv:             716 failed, 1284 passed - 87 sec
with changed mobility benefits:
        Score of TideEval 0.26 vs TideEval 0.29p: 26 - 22 - 32  [0.525] 80
        Score of SF14.1/0ply vs. 0.29m: 75 - 1 - 4
+ change in pawn-promotion-defence
        Score of TideEval 0.26 vs TideEval 0.29p: 25 - 23 - 32  [0.512] 80
        Score of SF14.1/0ply vs. 0.29p: 74 - 2 - 4
        Score of SF14.1/4ply/1600 vs. 0.29p: 318 - 32 - 50

     2023-08-10 - v.29q (change in pawn-promotion-defence, but undid mobility change)
        Score of 0.26 vs TideEval 0.29q:                13 - 25 - 42
        Score of SF14.1/0ply vs. 0.29q:                 68 - 4 - 8
        Score of SF14.1/4ply/1600 vs. 0.29q:           328 - 32 - 40
                                                       321 - 32 - 47
        Score of *SF11-64/0ply vs TideEval 0.29q:       79 - 0 - 1
        Score of *SF11-64/4ply/1600 vs TideEval 0.29q: 343 - 25 - 32
                                                       348 - 14 - 38
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    403 failed, 3329 passed - 143 sec
                                        AvoidMateIn1:   1914 failed, 1818 passed - 80 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 517 failed, 2118 passed - 98 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             717 failed, 1283 passed - xx sec

     2023-08-10 - v.29r with addBenefitBlocker changes (incl. same color and not moving on turning points)
        Score of 0.26 vs TideEval 0.29r:                24 - 26 - 30
        Score of SF14.1/0ply vs. 0.29r:                 75 - 0 - 5
        Score of SF14.1/4ply/1600 vs. 0.29r:           333 - 34 - 33
        Score of *SF11-64/0ply vs TideEval 0.29r:       79 - 0 - 1
        Score of *SF11-64/4ply/1600 vs TideEval 0.29r: 355 - 22 - 23
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    542 failed, - passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 583 failed, 2052 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             763 failed, - passed - xx sec

     2023-08-10 - v.29r without the above addBenefitBlocker changes
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    441 failed, - passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 589 failed, 2052 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             762 failed, - passed - xx sec

     2023-08-10 - v.29r with parts of the above addBenefitBlocker changes (no same color approach, which does make it worse it seems)
        Score of 0.26 vs TideEval 0.29r:                24 - 31 - 25
        Score of SF14.1/0ply vs. 0.29r:                 77 - 0 - 3
        Score of SF14.1/4ply/1600 vs. 0.29r:
        Score of *SF11-64/0ply vs TideEval 0.29r:       80 - 00 - 0
        Score of *SF11-64/4ply/1600 vs TideEval 0.29r:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    448 failed, - passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 584 failed, 2052 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             757 failed, 1243 passed - 79 sec

     2023-08-10 - v.29s (r + some undos+small corrections) hmm ->>2     -(>>2+>> 3)        ->>3         -0
        Score of 0.26 vs TideEval 0.29s:                20 - 25 - 35   21 - 21 - 38     20 - 26 - 34    21 - 21 - 38
        Score of SF14.1/0ply vs. 0.29s:                 77 -  1 - 2    77 -  1 - 2      76 -  2 - 2     75 -  3 - 2
        Score of SF14.1/4ply/1600 vs. 0.29s:           338 - 29 - 33  323 - 37 - 40    317 - 34 - 49   319 - 28 - 53
        Score of *SF11-64/0ply vs TideEval 0.29s:       80 -  0 - 0    80 -  0 - 0      80 -  0 - 0     80 -  0 - 0
        Score of *SF11-64/4ply/1600 vs TideEval 0.29s: 356 - 16 - 28  344 - 19 - 37    345 - 22 - 33   349 - 17 - 34
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      441            413               411           418 failed
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   522            517               523           523 failed
        lichess_db_puzzle_230601_2k-9xx.csv:               723            730               723           722 failed

    2023-08-10 - v.29t (unsing ->>4 = almost nothing, see to table above) + skip conditional additionalAttackers
        Score of 0.26 vs TideEval 0.29t:                 21 - 23 - 36  -> same    to compare with ->>2 18 - 25 - 37-> better
        Score of SF14.1/0ply vs. 0.29t:                  77 -  2 - 1   -> worse                        77 -  1 - 2 -> better
        Score of SF14.1/4ply/1600 vs. 0.29t:            310 - 35 - 55  -> better                      324 - 34 - 42-> worse
        Score of *SF11-64/0ply vs TideEval 0.29t:        80 -  0 - 0   -> same                         80 -  0 - 0 -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29t:  358 - 11 - 31  -> worse                       341 - 26 - 33-> better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        415 failed -> same                             419 -> same (4 worse)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     522 failed -> same (1 better)              => -(>> 3) will be used
        lichess_db_puzzle_230601_2k-9xx.csv:                 722 failed -> same (1 better)

    2023-08-10 - v.29u - special benefit for in uncovarable additional attacks
        Score of 0.26 vs TideEval 0.29t:                 21 - 25 - 34  -> 1 worse
        Score of SF14.1/0ply vs. 0.29t:                  76 -  2 - 2   -> 1 better
        Score of SF14.1/4ply/1600 vs. 0.29t:            322 - 32 - 46  -> 11 worse
        Score of *SF11-64/0ply vs TideEval 0.29t:        80 -  0 - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29t:   356 - 15 - 29  -> 2 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        419 failed -> same
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     517 failed -> same (5 better)
        lichess_db_puzzle_230601_2k-9xx.csv:                 724 failed -> same (2 worse)

    2023-08-10 - v.29v - reduce checking + hindering benefits as long as unclear if blockable
        Score of 0.26 vs TideEval 0.29v:                 21 - 26 - 33  -> same (0.5 worse)
        Score of SF14.1/0ply vs. 0.29v:                  77 -  1 - 2   -> same (0.5 worse)
        Score of SF14.1/4ply/1600 vs. 0.29v:            309 - 43 - 48  -> better
        Score of *SF11-64/0ply vs TideEval 0.29v:        80 -  0 - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29v:   353 - 22 - 25  -> same (0.5 worse)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        450 failed -> worse
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     608 failed -> worse
        lichess_db_puzzle_230601_2k-9xx.csv:                 761 failed -> worse

    2023-08-10 - v.29w - adhere contribution blocking
        Score of 0.26 vs TideEval 0.29w:                 19 - 28 - 33  -> 1 better
        Score of SF14.1/0ply vs. 0.29w:                  75 -  2 - 3   -> 1.5 better
        Score of SF14.1/4ply/1600 vs. 0.29w:            318 - 35 - 47  -> 5 worse
        Score of *SF11-64/0ply vs TideEval 0.29w:        80 -  0 - 0   -> same :-(
        Score of *SF11-64/4ply/1600 vs TideEval 0.29w:   359 - 16 - 25  -> 3 worse
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        449 failed -> same (1 better )
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     610 failed -> same (2 worse)
        lichess_db_puzzle_230601_2k-9xx.csv:                 767 failed -> 7 worse

    2023-08-10 - v.29x
        Score of *SF11-64/4ply/1600 vs TideEval 0.29x:   354 - 12 - 34  -> 11 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        441 failed -> better
        lichess_db_puzzle_230601_2k-9xx.csv:                 735 failed -> better

    2023-08-10 - v.29y
        Score of 0.26 vs TideEval 0.29y:                 19 - 23 - 38  -> 2.5 better
        Score of SF14.1/0ply vs. 0.29y:                  75 -  1 - 4   -> 0.5 better
        Score of SF14.1/4ply/1600 vs. 0.29y:            320 - 35 - 45  -> 2 worse
        Score of *SF11-64/0ply vs TideEval 0.29y:        79 -  0 - 1   -> finally 1 (again)...
        Score of *SF11-64/4ply/1600 vs TideEval 0.29y:  351 - 15 - 34  -> 1.5 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        441 failed -> same
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     x608 failed ->
        lichess_db_puzzle_230601_2k-9xx.csv:                 736 failed -> same (1 worse)
    2023-08-15 - v.29zpre
        Score of 0.26 vs TideEval 0.29zpre:              15 - 26 - 39  -> 2.5 better
        Score of SF14.1/0ply vs. 0.29zpre:               76 -  1 - 3   -> 1 worse
        Score of SF14.1/4ply/1600 vs. 0.29zpre:         323 - 33 - 44  -> 2 worse
                                                        318 - 29 - 53  -> 5 better
                                                        311 - 38 - 51  -> 14 better
                                                   avg. 317 - 34 - 49 -> 3.5 better
        Score of *SF11-64/0ply vs TideEval 0.29zpre:     80 -  0 - 0   -> worse (again)...
        Score of *SF11-64/4ply/1600 vs TideEval 0.29zpre:334- 27 - 39  -> 11 better
                                                         352- 16 - 32  -> 1.5 worse
                                                         350- 22 - 28  -> 2.5 worse
                                                   avg.  345- 22 - 33 -> 2.5 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        438 failed -> 3 better
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     516 failed -> better
        lichess_db_puzzle_230601_2k-9xx.csv:                 736 failed -> same
    2023-08-15 - v.29z1
        Score of 0.26 vs TideEval 0.29z1:                 19 - 29 - 32  -> 5.5 worse
        Score of SF14.1/0ply vs. 0.29z1:                  75 -  1 - 4   -> same
        Score of SF14.1/4ply/1600 vs. 0.29z1:            312 - 37 - 51  -> better
                                                         315 - 29 - 59  -> better
                                                     avg.313.5- 33- 55
        Score of *SF11-64/0ply vs TideEval 0.29z1:        80 -  0 - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z1:  350 - 13 - 37  -> 0.5 worse
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        451 failed -> 13 worse
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     551 failed -> 35 worse
        lichess_db_puzzle_230601_2k-9xx.csv:                 737 failed -> same (1 worse)

    2023-08-15 - v.29z2 - +change relEval on kings square -> chenged to z3, but same -> reverted
        Score of 0.26 vs TideEval 0.29z2:                 20 - 30 - 30  -> 2 worse
        Score of SF14.1/0ply vs. 0.29z2:                  75 -  1 - 4   -> same
        Score of SF14.1/4ply/1600 vs. 0.29z2:            331 - 24 - 45  -> 16 worse
                                                         318 - 30 - 52  -> 7 worse
        Score of *SF11-64/0ply vs TideEval 0.29z2:        80 -  0 - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z2:  347 - 12 - 41  -> 7 better
                                                         354 - 14 - 32  -> 5.5 worse
    2023-08-15 - v.29z4 still incl. z3 code
        Score of 0.26 vs TideEval 0.29z4:                 24 - 26 - 30  -> 2.5 worse compared to z1
        Score of SF14.1/0ply vs. 0.29z4:                  73 -  2 - 5   -> 1.5 better
        Score of SF14.1/4ply/1600 vs. 0.29z4:            338 - 27 - 35  ->  worse
        Score of *SF11-64/0ply vs TideEval 0.29z4:        80 -  x - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z4:  352 - 13 - 35  -> 2 worse
    => back to 0.29z1

    2023-08-10 - v.29z5 with BAD_addBenefitToBlockers()
        Score of 0.26 vs TideEval 0.29z5:                 24 - 21 - 35  -> 1 worse compared to z1
        Score of SF14.1/0ply vs. 0.29z5:                  77 -  2 - 1   -> 2.5 worse
        Score of SF14.1/4ply/1600 vs. 0.29z5:            330 - 28 - 42  -> 15 worse
        Score of *SF11-64/0ply vs TideEval 0.29z5:        78 -  0 - 2   -> 2 better!
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z5:  360 - 13 - 27  -> 10 worse
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        568 failed -> >100 worse
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     x608 failed ->
        lichess_db_puzzle_230601_2k-9xx.csv:                 x767 failed -> 30 worse

    2023-08-10 - v.29z5
        Score of 0.26 vs TideEval 0.29z5:                 24 - 19 - 37  -> same compared to z1
        Score of SF14.1/0ply vs. 0.29z5:                  74 -  1 - 5   -> 1 better
        Score of SF14.1/4ply/1600 vs. 0.29z5:            319 - 24 - 57  -> 2 worse
                                                         323 - 27 - 50  -> 5.5 worse
                                                         324 - 24 - 52  -> 5 worse
                                                    avg. 322 - 25 - 53
        Score of *SF11-64/0ply vs TideEval 0.29z5:        78 -  0 - 2   -> 2 better!
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z5:  354 - 11 - 35  -> 3 worse
                                                         344 - 15 - 41  -> 5 better
                                                         350 - 10 - 40  -> 1.5 better
                                                    avg. 349 - 12 - 39
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        671 failed -> >200 worse
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     539 failed -> 12 better
        lichess_db_puzzle_230601_2k-9xx.csv:                 810 failed -> 73 worse
   -> not really better, but calculation more consistand, so we leave it

    2023-08-10 - v.29z6 - baseline - warning of .29z7 below turned off
        Score of 0.26 vs TideEval 0.29z6:                 24 - 25 - 31  -> 3 worse compared to z1                  z1:   19 - 29 - 32
        Score of SF14.1/0ply vs. 0.29z6:                  73 -  1 - 6   -> 2 better                                      75 -  1 - 4
        Score of SF14.1/4ply/1600 vs. 0.29z6:            318 - 20 - 62  -> 8 better                                     312 - 37 - 51
                                                         319 - 18 - 63  -> 8 better                                     315 - 29 - 59
                                                                                                                   avg. 313.5- 33- 55
        Score of *SF11-64/0ply vs TideEval 0.29z6:        77 -  0 - 3   -> 3 better                                     80 -  0 - 0
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z6:  350 - 10 - 40  -> 1.5 better                              z1:  350 - 13 - 37
                                                         351 - 14 - 35  -> 1.5 worse
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        663 failed -> 211 worse                                        451 failed
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     532 failed -> 19 better                                        551 failed
        lichess_db_puzzle_230601_2k-9xx.csv:                 770 failed -> 33 worse                                         737 failed

    2023-08-10 - v.29z7 - warn about running piece into check fork   >>2                                >>4
        Score of 0.26 vs TideEval 0.29z7:                 23 - 27 - 30  -> same compared to z6     22 - 27 - 31  -> 1 better compared to z6
        Score of SF14.1/0ply vs. 0.29z7:                  77 -  0 - 3   -> 3.5 worse               71 -  1 - 8   -> 2 better
        Score of SF14.1/4ply/1600 vs. 0.29z7:            336 - 15 - 49  -> 20 worse               321 - 25 - 54  -> 5 worse
        Score of *SF11-64/0ply vs TideEval 0.29z7:        79 -  0 - 1   -> 2 worse                 77 -  0 - 3   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z7:  340 - 18 - 42  -> 6 better               347 - 14 - 39  -> 3 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        665 failed -> same (2 worse)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     533 failed -> same (1 worse)
        lichess_db_puzzle_230601_2k-9xx.csv:                 770 failed -> same

    2023-08-10 - v.29z10-13 - (z7 with >>4) +  loosing clash contribs is accounted only 1) 0 vs. 2) little 3) very little any more, if my move takes a piece that also has a contribution in the same clash
        Score of 0.26 vs TideEval 0.29z10:                19 - 26 - 35  -> 3.5 better comp.to z7>>4   20 - 31 - 29 -> -2.5 comp.to z10 21 - 32 - 27  -> -5  comp.to z10 23 - 28 - 29  -> -5 comp.to z10
        Score of SF14.1/0ply vs. 0.29z10:                 76 -  1 - 3   -> 2.5 worse                  77 -  1 - 2  -> -0.5             76 -  1 - 3   -> =               76 -  1 - 3   -> =
        Score of SF14.1/4ply/1600 vs. 0.29z10:           308 - 24 - 68  -> 13.5 better             318.5 -23.5- 58 -> -10            317. - 22 - 60. -> -9             322. - 22 - 55 -> -13
        Score of *SF11-64/0ply vs TideEval 0.29z10:       79 -  0 - 1   -> 2 worse                    79 -  0 - 1  -> same             79 -  0 - 1   -> =               79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z10: 355 - 11 - 34  -> 6.5 worse                 341 - 12 - 47 -> +13            351. - 12.- 36  -> +3.5           346 - 10 - 44  -> +9.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:       642 failed -> 23 better                      642  =                             642      -> =                    642      -> =
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:    517 failed -> 16 better                      519  -2                            517      -> =                    517      -> =
        lichess_db_puzzle_230601_2k-9xx.csv:                762 failed -> same (8 better)                766  -4                            772      -> -10                  763      -> -1

    2023-08-10 - v.29z14 - reduce benefit of additional caverage      + z15 reversePieceBenefit + reduce more if already "overcovered" + z16 do not fully loose pawnDoubleHopBenefits with omaxbenefits + z17 queens magic rect triangle
        Score of 0.26 vs TideEval 0.29z14:                 23 - 25 - 32  -> 1.5 better compared to z13  18 - 22 - 40 -> 6.5 better comp. to z14      14 - 26 - 40 -> +2  comp to z15       18 - 22 - 40 -> -2  comp to z16
        Score of SF14.1/0ply vs. 0.29z14:                  74 -  3 - 3   -> 1 better                    76 -  2 - 2  -> 1.5 worse                    74 -  3 - 3  -> +1.5                  74 -  1 - 5  -> +
        Score of SF14.1/4ply/1600 vs. 0.29z14:            321 - 20. - 58. -> 2.5 better                326. - 18 - 55. -> 1 worse                   323. - 24 - 52.-> =                   324 - 23 - 53 -> =
        Score of *SF11-64/0ply vs TideEval 0.29z14:        79 -  0 - 1   -> same                        79 -  0 - 1  -> same                         79 -  0 - 1  ->  =                    78 -  0 - 2  ->  +1 !!
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z14:  347. - 10 - 42.  -> 1.5 worse                347.- 13. - 39-> 1.5 worse                   345.- 12. - 42->  +2.5                351 - 10. - 38. ->  -4.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        x failed -> same                                                                            642      -> = comp to z14 above      642       -> = comp to z16
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     x failed -> same                                                                            522      -> -5                       526       -> -4
        lichess_db_puzzle_230601_2k-9xx.csv:                 x failed -> same                                                                            753      -> +10                      753       -> =

    2023-08-10 - v.29z18 - reduce knight mobility values                                                z17:
        Score of 0.26 vs TideEval 0.29z18:                 17 - 23 - 40  -> 0.5 better comp.to z17    18 - 22 - 40
        Score of SF14.1/0ply vs. 0.29z18:                  76 -  2 - 2   -> 2.5 worse                 74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. 0.29z18:            325 - 11 - 64 -> 5 better                  324 - 23 - 53
        Score of *SF11-64/0ply vs TideEval 0.29z18:        79 -  0 - 1   -> -1                        78 -  0 - 2
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z18:  346.- 11.- 42  -> 4.5 better                351 - 10.- 38.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        652 failed -> -10
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     526 failed -> =
        lichess_db_puzzle_230601_2k-9xx.csv:                 760 failed -> -7
        lichess_db_puzzle_230601_2k-12xx.csv:                967 failed

    2023-08-10 - v.29z19 - a little more castling motivation + king castle area clearance
        Score of 0.26 vs TideEval 0.29z19(here still w/o clearance):   21 - 19 - 40  -> -2 comp. to v0.29z18 (strange, everything else alsmost same)
        Score of 0.26 vs TideEval 0.29z19:                 21 - 19 - 40  -> -2 comp. to v0.29z18 still...
        Score of SF14.1/0ply vs. 0.29z19:                  76 -  2 - 2   -> -2.5
        Score of SF14.1/4ply/1600 vs. 0.29z19:            324 - 20.- 55. -> -4
        Score of *SF11-64/0ply vs TideEval 0.29z19:        79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z19:  341.- 10.- 48  -> +6
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        652 failed  -> =
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     x526 failed ->
        lichess_db_puzzle_230601_2k-9xx.csv:                 x760 failed ->

    2023-08-10 - v.30 - corrected king trapping benefit, where it is still unclear if really trapped
        Score of 0.26 vs TideEval 0.30:                 19 - 22 - 39  -> +0.5 comp. to v0.29z19
        Score of SF14.1/0ply vs. 0.30:                  76 -  2 - 2   -> =
        Score of SF14.1/4ply/1600 vs. 0.30:            321 - 25 - 54  -> +1
        Score of *SF11-64/0ply vs TideEval 0.30:        79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval 0.30:  351.- 14 - 34.  -> -12 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         397 failed -> +255 (!)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      501 failed -> +25
        lichess_db_puzzle_230601_2k-9xx.csv:                  682 failed -> +78
        lichess_db_puzzle_230601_2k-16xx.csv:                1176 failed

   2023-08-10 - v.40pre2 - reimplementation of chance collecting
        Score of 0.26 vs TideEval 0.40:                 15 - 23 - 42  -> +3.5 comp. to 0.30
        Score of SF14.1/0ply vs. 0.40:                  73 -  2 - 5   -> +3 (!)
        Score of SF14.1/4ply/1600 vs. 0.40:            325 - 25.- 49. -> -4
        Score of *SF11-64/0ply vs TideEval 0.40:        77 -  0 - 3   -> +2 (!)
        Score of *SF11-64/4ply/1600 vs TideEval 0.40:  333 - 16 - 51  -> +17.5 (!)

   2023-08-10 - v.41 - reimpl. ok now + activation of setEvalsForBlockingHere()
        Score of 0.26 vs TideEval:                      16 - 24 - 40  -> -1.5 (!) comp. to v0.40
        Score of SF14.1/0ply vs. TideEval:              76 -  2 - 2   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        315 - 25.- 59.  -> +10 (!)
                                                       313 - 34.- 52.  -> +7.5
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       345 - 17.- 37. -> -11.
                                                       351 - 19 - 30  -> -16. (!)
                                                       343 - 15 - 42  -> -10.
                                                  avg. 346.- 17 - 36.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         387 failed -> +10 comp. to 0.30
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      492 failed -> +9
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed -> +7
        lichess_db_puzzle_230601_2k-16xx.csv:                1168 failed -> +8

   2023-08-10 - v.42 + hanging pieces behind kings
        Score of 0.26 vs TideEval:                      14 - 26 - 40  -> +1 comp. to v0.41
        Score of SF14.1/0ply vs. TideEval:              76 -  2 - 2   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        313 - xx - 52.  ->
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       339.- 14.- 46. -> +8.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         383 failed -> +4 comp. to 0.41
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      494 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:                  679 failed -> -4

   2023-08-10 - v.42b + hanging pieces behind kings, but max benefit setEvalsForBlockingHere
        Score of 0.26 vs TideEval:                      17 - 26 - 37  -> -4.5 comp. to v0.43(!)
        Score of SF14.1/0ply vs. TideEval:              73 -  1 - 6   -> = +1
        Score of SF14.1/4ply/1600 vs. TideEval:        329.- 24 - 46.-> -12 (!! d.h. +12 für setEvalsForBlockingHere in v43 unten)
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> = -3
        Score of *SF11-64/4ply/1600 vs TideEval:       340.- 17.- 42. -> +6.

   2023-08-10 - v.42c + hanging pieces behind kings, but NO benefit setEvalsForBlockingHere
        Score of 0.26 vs TideEval:                      12 - 29 - 39  -> +3.5 comp. to v0.42b
        Score of SF14.1/0ply vs. TideEval:              74 -  2 - 4   -> -0.5
        Score of SF14.1/4ply/1600 vs. TideEval:        313 - 28 - 59  -> +14
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +3
        Score of *SF11-64/4ply/1600 vs TideEval:       346.- 15.- 38  -> -5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         385 failed ->
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      494 failed ->
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed ->

   2023-08-10 - v.43  still hanging pieces + corrected and reduced setEvalsForBlockingHere to exclude the piece with contribution itself + more
        Score of 0.26 vs TideEval:                      14 - 27 - 39  -> -1 comp. to v0.42c
        Score of SF14.1/0ply vs. TideEval:              73 -  2 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        317 - 29.- 53. -> = -5
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       347 - 19. - 33.  -> -3
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         383 failed -> +2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      496 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed -> =
        lichess_db_puzzle_230601_2k-16xx.csv:                x1168 failed ->

   2023-08-10 - v.44a - small improvements here and there (e.g. kings attacking+defending helpless pieces :-)
        Score of 0.26 vs TideEval:                      17 - 27 - 36  -> -3  comp. to v0.43
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -4 (!) , -7.5
        Score of SF14.1/4ply/1600 vs. TideEval:        320.- 26.- 51  -> -3
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       345.- 16. - 38 -> +2.5  , +5.5

   2023-08-10 - v.44c - fixed very old bug: pawn 2 sq move error after sq1 is freed -> much less "*** Test" Errors in FinalChessBoardEvalTest"
        Score of 0.26 vs TideEval:                      18 - 27 - 35  -> -1  comp. to v0.44a
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -4 (!) , -7.5
        Score of SF14.1/4ply/1600 vs. TideEval:        318 - 35 - 47  -> -2
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       340- 16. - 43. -> +5 / +2.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         374 failed -> +9 comp to v0.43
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      522 failed -> -26
        lichess_db_puzzle_230601_2k-9xx.csv:                  677 failed -> -2
        lichess_db_puzzle_230601_2k-16xx.csv:                1160 failed -> +8

   2023-08-10 - v.44d - little extra move pawn forward motivation
        Score of 0.26 vs TideEval:                      18 - 26 - 36  -> +0.5  comp. to v0.44c
        Score of SF14.1/0ply vs. TideEval:              75 -  0 - 5   -> +3 (!) , -7.5
        Score of SF14.1/4ply/1600 vs. TideEval:        316.- 27.- 56  -> -8
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       340 - 23. - 36. -> -3.5

   2023-08-10 - v.44d - little extra move pawn forward motivation + fee for direct pawn doubeling
        Score of 0.26 vs TideEval:                      20 - 27 - 33  -> -2.5  comp. to v0.44c
        Score of SF14.1/0ply vs. TideEval:              75 -  0 - 5   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        312 - 30 - 58  -> +3
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       343.- 19 - 37. -> -1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         372 failed -> +2 comp to v0.44c
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      524 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed -> +2

   2023-08-10 - v.44f - increase motivation for threatened pieaces to move away -relEval>>3  (from >>4)
        Score of 0.26 vs TideEval:                      16 - 26 - 38  -> +4.5  comp. to v0.44d
        Score of SF14.1/0ply vs. TideEval:              76 -  0 - 4   -> -1
        Score of SF14.1/4ply/1600 vs. TideEval:        312 - 35 - 53  -> -2.5
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       331 - 22 - 47  -> +11
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         372 failed -> = comp to v0.44d
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      526 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed -> =
  */
    /* 2023-08-10 - v.44g - increase motivation for threatened pieaces to move away more: -relEval>>2  (from >>3 / >>4)
        Score of 0.26 vs TideEval:                      20 - 22 - 38  -> -2  comp. to v0.44f
        Score of SF14.1/0ply vs. TideEval:              75 -  0 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval (4x400) 311 - 35.- 53. -> +2
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> +1 (finally again...)
        Score of *SF11-64/4ply/1600 vs TideEval:       344 - 17 - 39 -> -10
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         370 failed -> +2 comp to v0.44f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      533 failed -> -7
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed ->
        => not used
   2023-08-10 - v.44h - reduce king area benefits
        Score of 0.26 vs TideEval:                      11 - 34 - 35  -> +1  comp. to v0.44f
        Score of SF14.1/0ply vs. TideEval:              74 -  1 - 5   -> +1.5
        Score of SF14.1/4ply/1600 vs. TideEval:        314.- 41 - 44. -> -5
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> = +1
        Score of *SF11-64/4ply/1600 vs TideEval:       335.- 28 - 36.  -> -5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         426 failed -> -56 comp to v0.44f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      550 failed -> -24
        lichess_db_puzzle_230601_2k-9xx.csv:                  718 failed -> -43
        => not used */
    /*
   2023-08-10 - v.44i2 - changed king area benefits a little
        Score of 0.26 vs TideEval:                      13 - 35 - 32  -> -1.5   comp. to v0.44f
        Score of SF14.1/0ply vs. TideEval:              74 -  3 - 3   -> +0.5
        Score of SF14.1/4ply/1600 vs. TideEval: i1!:   307 - 43.- 53. -> +2.5
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       342 - 19.- 38.  -> -12.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         364 failed -> +8 comp to v0.44f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      533 failed -> -7
        lichess_db_puzzle_230601_2k-9xx.csv:                  668 failed -> +7

   2023-08-10 - v.44i3
        Score of 0.26 vs TideEval:                      19 - 27 - 34  -> -2   comp. to v0.44i2
        Score of SF14.1/0ply vs. TideEval:              74 -  1 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        301 - 42 - 57  -> +7.5
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +2
        Score of *SF11-64/4ply/1600 vs TideEval:       343.- 20 - 38. -> -2.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         428 failed -> -46 comp to v0.44i2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      543 failed -> -17
        lichess_db_puzzle_230601_2k-9xx.csv:                  668 failed -> +7
        (NOT used, understandably not beneficial:  LowTide max effect on king area benefits all belonging to the same target)

   2023-08-10 - v.44i4 i3 + 5%
        Score of 0.26 vs TideEval:                      18 - 31 - 31  -> -3   comp. to v0.44i2
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -1.5
        Score of SF14.1/4ply/1600 vs. TideEval:        310.- 33 - 57. -> +1
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       350.- 15 - 34. -> -6
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         359 failed -> +5 comp to v0.44i2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      534 failed -> -1
        lichess_db_puzzle_230601_2k-9xx.csv:                  666 failed -> +5

   2023-08-10 - v.44j - calc getKingAreaBenefit for both kings, not just attacking the opponent king
        Score of 0.26 vs TideEval:                      21 - 15 - 44  -> +2   comp. to v0.44i2
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> -3.5
        Score of SF14.1/4ply/1600 vs. TideEval:        318 - 26 - 56  -> -4
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       345 - 12 - 43  -> +1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         390 failed -> -26 comp to v0.44i2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      653 failed -> -122
        lichess_db_puzzle_230601_2k-9xx.csv:                  690 failed -> -22

   2023-08-10 - v.44j2 - j adapted for defence cases
        Score of 0.26 vs TideEval:                      19 - 23 - 38  -> -1.5   comp. to v0.44i2
        Score of SF14.1/0ply vs. TideEval:              73 -  0 - 7   -> +2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        315.- 29. - 55 -> -3
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +2 (!)
        Score of *SF11-64/4ply/1600 vs TideEval:       347.- 15.- 37.  -> -1.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         377 failed -> -13 comp to v0.44i2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      575 failed -> -42
        lichess_db_puzzle_230601_2k-9xx.csv:                  686 failed -> -18

   2023-08-10 - v.44j3 - j adapted for defence cases
        Score of 0.26 vs TideEval:                      19 - 23 - 38  -> = comp. to v0.44j2
        Score of SF14.1/0ply vs. TideEval:              77 -  1 - 2   -> -0.5
        Score of SF14.1/4ply/1600 vs. TideEval:        306 - 37 - 57 -> +6
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       341.- 18 - 40.  -> + 4.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         376 failed -> +1 comp to v0.44j2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      576 failed -> -1
        lichess_db_puzzle_230601_2k-9xx.csv:                  687 failed -> -1

   2023-08-10 - v.44k - correction of close future attacking benefits (no clashContrib after prev. additional attacker already got the same amount + relEval-based benefit)
        Score of 0.26 vs TideEval:                      18 - 24 - 38  -> +0.5  comp. to v0.44j3
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> +0.5
        Score of SF14.1/4ply/1600 vs. TideEval:        323.- 29 - 47. -> -14
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       351.- 15 - 33. -> -8.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         370 failed ->-6  comp to v0.44j3
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      555 failed -> +21
        lichess_db_puzzle_230601_2k-9xx.csv:                  684 failed -> +3

   2023-08-10 - v.44k3 - correction of close future attacking benefits (no clashContrib after prev. additional attacker already got the same amount + relEval-based benefit)
        Score of 0.26 vs TideEval:                      17 - 26 - 37  -> +0.5  comp. to v0.44j3
        Score of SF14.1/0ply vs. TideEval:              74 -  2 - 4   -> +2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        325 - 22.- 52. -> -12
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +2
        Score of *SF11-64/4ply/1600 vs TideEval:       346 - 13.- 40.  -> -2
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         421 failed -> -- comp to v0.44j3
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      571 failed -> =
        lichess_db_puzzle_230601_2k-9xx.csv:                  712 failed -> -

   2023-08-10 - v.44l - more benefit to pawns that seemt to be able to move straight to promotion - and a bit less to the others
        Score of 0.26 vs TideEval:                      17 - 34 - 29  -> -4  comp. to v0.44k3
        Score of SF14.1/0ply vs. TideEval:              73 -  2 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        308.- 41 - 50. -> +7
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 27 - 34.  > +1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         412 failed -> -36 comp to v0.44j3(!)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      530 failed -> +25
        lichess_db_puzzle_230601_2k-9xx.csv:                  676 failed -> +11

   2023-08-10 - v.44m - experiment: postpone first king attacking by 1 future level
        Score of 0.26 vs TideEval:                      17 - 30 - 33  -> +2   comp. to v0.44k3
        Score of SF14.1/0ply vs. TideEval:              74 -  2 - 4   -> -1
        Score of SF14.1/4ply/1600 vs. TideEval:        299.- 55. - 45 -> +2
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +2
        Score of *SF11-64/4ply/1600 vs TideEval:       345.- 25 - 29.  > -6
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         431 failed -> -19 comp to v0.44j3(!)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      534 failed -> -4
        lichess_db_puzzle_230601_2k-9xx.csv:                  686 failed -> -10

   2023-08-25 - v.45 - isChecking flag for indirect moving away check moves
        Score of 0.26 vs TideEval:                      13 - 35 - 32  -> +1.5   comp. to v0.44m
        Score of SF14.1/0ply vs. TideEval:              73 -  2 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        304 - 49.- 46. -> -1.5
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       340 - 31 - 29   > +2.5

   2023-08-25 - v.45a - same + little extra EVAL-score for those moves
        Score of 0.26 vs TideEval:                      12 - 36 - 32  -> +2  comp. to v0.44m
        Score of SF14.1/0ply vs. TideEval:              73 -  2 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        295.- 56 - 48. -> +1
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       336.- 34.- 29  -> +2
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         428 failed -> +3  comp to v0.44m

   2023-08-25 - v.46 - now thinks about likely2Bkilled, this changes Nogo calculation + Conditions instead of NoGos if target square is only slightly blocked by 1 opponent
        Score of 0.26 vs TideEval:                      27 - 30 - 23  -> -11.   comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              76 -  0 - 4   -> -2
        Score of SF14.1/4ply/1600 vs. TideEval:        325.- 36 - 38. -> -20 (!)
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +1
        Score of *SF11-64/4ply/1600 vs TideEval:       350.- 22.- 27  -> -8
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         436 failed -> -8  comp to v0.45a
                                        AvoidMateIn1:        1996 failed  ca. -180 (zu v.29)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      596 failed -> -62
        lichess_db_puzzle_230601_2k-9xx.csv:                  719 failed -> -33

   2023-08-25 - v.46a - now thinks about likely2Bkilled, this changes Nogo calculation - but here without change of Conditions instead of NoGos
        Score of 0.26 vs TideEval:                      14 - 40 - 26  -> -4 comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              76 -  1 - 3   -> -2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        321.- 46 - 32. -> -15
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       346.- 28.- 25  -> -7
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         419 failed -> +9  comp to v0.45a
                                        AvoidMateIn1:        2013 failed  ca. -200 (zu v.29)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      554 failed -> -20 (v.44m)
        lichess_db_puzzle_230601_2k-9xx.csv:                  733 failed -> -51

   2023-08-25 - v.46b - without "calcClash... recalculated straight moving pawn could also trigger a 2nd row piece"
        Score of 0.26 vs TideEval:                      15 - 41 - 24  -> -5.5 comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              75 -  1 - 4   -> -1.5
        Score of SF14.1/4ply/1600 vs. TideEval:        312 - 50.- 37. -> -13

   2023-08-25 - v.46c - without improved "fulfilledConditionsCouldMakeDistIs1()" and without using killedReasonablySure()
        Score of 0.26 vs TideEval:                      11 - 39 - 30  -> -0.5  comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        306.- 47.- 46  -> -8
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +1
        Score of *SF11-64/4ply/1600 vs TideEval:       339.- 31.- 29  -> -1.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         421 failed -> +7    comp to v0.45a
                                        AvoidMateIn1:        1989 failed  ?
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      524 failed -> +10  (v.44m)
        lichess_db_puzzle_230601_2k-9xx.csv:                  696 failed -> -10  (v.44m)
        => everything undone and more or less equal again. hmmm

   2023-08-26 - v.46d - correction of old_eval reg. 2nd row / moreWhites
        Score of 0.26 vs TideEval:                      10 - 41 - 29  -> =  comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        313.- 50 - 37. -> -15
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 32.- 29  -> -1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         420 failed -> +1    comp to v0.46c
                                        AvoidMateIn1:        1987 failed -> +2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      523 failed -> +1
        lichess_db_puzzle_230601_2k-9xx.csv:                  696 failed -> =

   2023-08-26 - v.45e - (named 45, as all 46 functions are inactive anyway...) correction of call to calcClashResultExcludingOne in futireClashEval, concerning moreWhites / 2nd row etc
        Score of 0.26 vs TideEval:                       9 - 43 - 28  -> =  comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        303 - 53.- 43. -> -6
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       332 - 34 - 34  -> +5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      420 failed -> +1   comp to v0.46c
                                        AvoidMateIn1:     1987 failed -> +2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   523 failed -> +1

   2023-08-26 - v.45f - 45e + corrected fulfilledConditionsCouldMakeDistIs1()
        Score of 0.26 vs TideEval:                       9 - 43 - 28  -> =  comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        305.- 52.- 42  -> -8
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 30 - 31. -> +0.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      420 failed -> +1    comp to v0.46c
                                        AvoidMateIn1:     1986 failed -> +3
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   523 failed -> +1
        lichess_db_puzzle_230601_2k-9xx.csv:               695 failed -> +1

   2023-08-26 - v.46i + less future clash benefit + use (above unsuccessful) check for leaving squares uncoverd for an immediate opponent move at least to give out contributions
        Score of 0.26 vs TideEval:                      18 - 28 - 34  -> -1.5  comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        305.- 52.- 42  -> -8
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 30 - 31. -> +0.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      420 failed -> +1  comp to v0.45f
                                        AvoidMateIn1:     1986 failed -> +3
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   523 failed -> +1
        lichess_db_puzzle_230601_2k-9xx.csv:               695 failed -> +1

   2023-08-26 - v.46l + use (above unsuccessful) check for leaving squares uncoverd for an immediate opponent move at least to give out contributions 100/200 for his possible check-giving moves
        Score of 0.26 vs TideEval:                       9 - 40 - 31  -> +1.5  comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        309.- 44.- 46  -> =
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       335.- 35.- 29 -> =

   2023-08-26 - v.46m + like 46l, but a little contributions for non-checking squares conquerable by opponent.
        Score of 0.26 vs TideEval:                                          comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              74 -  1 - 5   -> +2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        311.- 45 - 43. -> -2
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       336.- 35 - 28. -> -1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      425 failed -> -5  comp to v0.45f
                                        AvoidMateIn1:     1986 failed ->
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   527 failed -> -4
        lichess_db_puzzle_230601_2k-9xx.csv:               695 failed ->

   2023-08-26 - v.46n + like 46m but less benefit for future attackers fl>=2
        Score of 0.26 vs TideEval:                                          comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              74 -  2 - 4   -> +2
        Score of SF14.1/4ply/1600 vs. TideEval:        301 - 51.- 45. -> +4
        Score of *SF11-64/0ply vs TideEval:             79 -  1 - 0   -> -0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       336.- 35 - 28. ->
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      426 failed -> -6  comp to v0.45f
                                        AvoidMateIn1:     1986 failed ->
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   525 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:               695 failed ->

   2023-08-26 - v.46o + like 46n but no extra avoid conquer contrib if square is occupied
        Score of 0.26 vs TideEval:                                          comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              73 -  1 - 6   -> +3.5
        Score of SF14.1/4ply/1600 vs. TideEval:        311.- 41.- 47  -> -0.5
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       333.- 38 - 28. ->
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      426 failed -> -6  comp to v0.45f
                                        AvoidMateIn1:     1987 failed -> -1
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   524 failed -> -1
        lichess_db_puzzle_230601_2k-9xx.csv:               696 failed -> -1


   2023-08-26 - v.46p corrects hanging piece behing king
        Score of 0.26 vs TideEval:                      14 - 42 - 24  -> -5,5 (comp. to 46k)
        Score of SF14.1/0ply vs. TideEval:              72 -  1 - 7   -> +1 comp. to v0.45o
        Score of SF14.1/4ply/1600 vs. TideEval:        317.- 43 - 45. -> -4.5
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 31.- 30. -> -1.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      428 failed -> -2   comp to v0.45o
                                        AvoidMateIn1:     1987 failed -> =
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   530 failed -> -6
        lichess_db_puzzle_230601_2k-9xx.csv:               679 failed -> +17

   2023-08-26 - v.46r improves addBenefitToBlockers, also count already covering the hopping point + contribution for these
        Score of 0.26 vs TideEval:                 R    14 - 44 - 22  -> -6,5 (comp. to 46k)
        Score of SF14.1/0ply vs. TideEval:         R    74 -  1 - 5   -> +4 comp. to v0.45o
        Score of SF14.1/4ply/1600 vs. TideEval:        310 - 50.- 39. -> -3.5
        Score of *SF11-64/0ply vs TideEval:             77 -  2 - 1   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       325 - 37 - 38  -> +9

   2023-08-26 - v.46s - reduce benefit fir fl>1 for moving away / enabling other benfits cases
        Score of 0.26 vs TideEval:                      14 - 44 - 22  -> X-6,5   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              73 -  1 - 6   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        310.- 51 - 39. -> X-3.5
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       325 - 37 - 38  -> X+9
        lichess_db_puzzle_230601_2k-9xx.csv:               701 failed -> -22

   2023-08-26 - v.46t5 - count blockers for hanging piece behind king and change benefit + benefit blockers
        Score of 0.26 vs TideEval:                      19 - 39 - 22  -> -2.5   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> -4.5 (!)
        Score of SF14.1/4ply/1600 vs. TideEval:        313 - 50 - 37  -> -3
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> -0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       340 - 34.- 26  -> -13.5 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      400 failed -> +28   comp to v0.45p
                                        AvoidMateIn1:     1962 failed -> +25
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   453 failed -> +77
        lichess_db_puzzle_230601_2k-9xx.csv:               675 failed -> +4

   2023-08-26 - v.46t6 - like t5 + switch noGo for isKillable on again (only for empty squares)
        Score of 0.26 vs TideEval:                      17 - 41 - 22  -> -1.5   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              76 -  1 - 3   -> -2
        Score of SF14.1/4ply/1600 vs. TideEval:        328 - 41 - 31  -> -12.5  (!)
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       358.- 21 - 20. -> -14 (!)
    => not used for now. idea is interesting, but seems to induce other problems.

   2023-08-26 - v.46t7 - like t5 + less reduction for higher fl
        Score of 0.26 vs TideEval:                      16 - 43 - 21  -> -1.5   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              80 -  0 - 0   -> -2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        310.- 52 - 37.  -> -1
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       339.- 33.- 27 -> -13 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      403 failed -> +25   comp to v0.45p

   2023-08-26 - v.46t8 - like t5 + now completely without reduction for higher fl
        Score of 0.26 vs TideEval:                      17 - 40 - 23  -> -1   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        306.- 51 - 42. -> +3
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       344.- 28 - 27. -> -15 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      405 failed -> +23   comp to v0.45p
                                        AvoidMateIn1:     1955 failed -> +32
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   455 failed -> +75
        lichess_db_puzzle_230601_2k-9xx.csv:               687 failed -> -8

   2023-08-26 - v.46u2 - like t8 + repaired blockinf Benefits for straight pawns (not attacking)
        Score of 0.26 vs TideEval:                      17 - 40 - 23  -> -1   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              76 -  0 - 4   -> -.5
        Score of SF14.1/4ply/1600 vs. TideEval:        305 - 52.- 42. -> +4
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       341 - 34.- 24. -> -15 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      394 failed -> +34   comp to v0.45p
                                        AvoidMateIn1:     1961 failed -> +26
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   453 failed -> +77
        lichess_db_puzzle_230601_2k-9xx.csv:               689 failed -> -10

   2023-08-26 - v.46u3 - like u2 + more fixes in blocking Benefits
        Score of 0.26 vs TideEval:                      14 - 39 - 27  -> +2.5 comp. to v0.45r:  14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        297 - 65 - 38  -> +7                    310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       339 - 38.- 22. -> -8                    325 - 37 - 38
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      427 failed -> +1   comp to v0.45p:   428
                                        AvoidMateIn1:     1968 failed -> +19                   1987
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   546 failed -> -16                    530
        lichess_db_puzzle_230601_2k-9xx.csv:               721 failed -> -42                    679

    2023-08-30 v46u4: u3 with less blocker-benefit when >1 blockers
        Score of 0.26 vs TideEval:                      15 - 39 - 26  -> +1.5 comp. to v0.45r:  14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        307.- 58 - 34. -> =                     310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       337 - 34.- 28. -> -4                    325 - 37 - 38
        -> not used

    2023-08-30 v46u5: u3 with less (*0.75) attacker-benefit when >1 blockers  (*0.5 was a little worse than 0.75, *0.83 also a little worse)
        Score of 0.26 vs TideEval:                      14 - 39 - 27  -> +2.5 comp. to v0.45r:  14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        304 - 65 - 31  -> -1                    310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       325.- 43.- 32 -> -3                     325 - 37 - 38
                                        AvoidMateIn1:     1986 failed -> +1                    1987

    2023-08-30 v46u9: u5 + attackerBenefit at attacking square (not kingpos)
        Score of 0.26 vs TideEval:                      12 - 42 - 26  -> +3    comp. to v0.45r: 14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        299 - 60.- 40. -> +6                    310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       340 - 36.- 23. -> -15 (!)               325 - 37 - 38

    2023-08-30 v46v9: u9 + no 1/4*mate bonus for check move with seemingly no opponent moves left in final move decision, because this ruins 3-fold-repetition detection in those cases-
        Score of 0.26 vs TideEval:                      11 - 49 - 20  -> +.5    comp. to v0.45r: 14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        298 - 66 - 36  -> +4                    310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 37 - 24. -> -13. (!)               325 - 37 - 38
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      467 failed -> +1   comp to v0.45p:   428
                                        AvoidMateIn1:     2010 failed -> +23                   1987
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   545 failed -> -15                    530
        lichess_db_puzzle_230601_2k-9xx.csv:               715 failed -> -36                    679


*/



}
