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

*/



}
