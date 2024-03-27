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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinalChessBoardEvalTest {
    /**
     * Path for test-sets / files
     * Path for test-sets / files
     */
    private static final String TESTSETS_PATH = "./out/test/TideEval/de/ensel/tideeval/";


    private int countNrOfBoardEvals = 0;
    private static final int SKIP_OPENING_MOVES = 10;
    private static final int MIN_NROF_PIECES = 6;
    // check one of the levels more thorougly
    // private static final int CHECK_EVAL_LEVEL = 5;
    private static final int[] countEvalSame = new int[EVAL_INSIGHT_LEVELS];
    private static final int[] countEvalRightTendency = new int[EVAL_INSIGHT_LEVELS];
    private static final int[] countEvalRightTendencyButTooMuch = new int[EVAL_INSIGHT_LEVELS];
    private static final int[] countEvalWrongTendency = new int[EVAL_INSIGHT_LEVELS];
    private static final long[] sumEvalRightTendency = new long[EVAL_INSIGHT_LEVELS];
    private static final long[] sumEvalRightTendencyButTooMuch = new long[EVAL_INSIGHT_LEVELS];
    private static final long[] sumEvalWrongTendency = new long[EVAL_INSIGHT_LEVELS];
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
---
    2021-11-07 09:30  (only T_ in 49.5 Sec -> about 360 Evals/sec, only 9 aborted games (is still 28 on T_+V_ files)
    Testing Set T_13xx.cts:
     *** Test abgebrochen wg. fehlerhaftem Zug *** 8/8/8/1K1p4/6k1/8/PPB5/7p  b - - 1 48
    **** Fehler: Fehlerhafter Zug: e4 -> d3 nicht möglich auf Board Testboard 1. e4 0.24 1... c5 0.32 2....
     *** Test abgebrochen wg. fehlerhaftem Zug *** r1bqkb1r/1p2pppp/5n2/p1p1n1N1/P2Pp3/1BP5/1P3PPP/RNBQ1RK1  b kq d3 0 9
    Finished test of 4136 positions from Test set T_13xx.cts.        Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 288,  max.clashes: 275,  new mobility: 288,  attacks on opponent side: 298,  attacks on opponent king: 298,  defends on own king: 300,  Mix Eval: 255.
    Testing Set T_16xx.cts:
    **** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board Testboard 1. e4 0.24 1... e6 0.13 2....
     *** Test abgebrochen wg. fehlerhaftem Zug ***    rnb1kqnr/5pb1/1pp1p2p/p2pP1p1/P2PB3/1PN1BN2/2PQ1PPP/R3K2R  w KQkq d6 0 13
    **** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board Testboard 1. e4 0.24 1... e6 0.13 2....
     *** Test abgebrochen wg. fehlerhaftem Zug ***    r1b1kb1r/p1qnnpp1/2p1p2p/1p1pP3/3P4/2N2N2/PPP1BPPP/R1BQR1K1  b kq - 5 9
    Finished test of 4567 positions from Test set T_16xx.cts.       Evaluation deltas:  game state: 394,  piece values: 285,  basic mobility: 270,  max.clashes: 268,  new mobility: 270,  attacks on opponent side: 282,  attacks on opponent king: 282,  defends on own king: 284,  Mix Eval: 245.
    Testing Set T_22xx.cts:
     4643 (43) 7886 (73) 8247 (77) 6290 (58) 8456 (79) 7993 (74) 8086 (75) 8010 (74) 7143 (66).
    **** Fehler: Fehlerhafter Zug: a5 -> b6 nicht möglich auf Board Testboard 1. e4 0.24 1... g6 0.4 2.....
     *** Test abgebrochen wg. fehlerhaftem Zug ***    1rb1k2r/p1qn1pbp/2pp1np1/Pp2p3/3PP3/1BN1BN1P/1PP2PP1/R2QK2R  w KQk b6 0 11
    **** Fehler: Fehlerhafter Zug: g5 -> f6 nicht möglich auf Board Testboard 1. d4 0.0 1... Nf6 0.19 2....
     *** Test abgebrochen wg. fehlerhaftem Zug ***    r4rk1/2pqn1bp/6p1/2pPppP1/4P3/2NQ1N2/PP3P2/R3K1R1  w Q f6 0 20
    **** Fehler: Fehlerhafter Zug: d5 -> e6 nicht möglich auf Board Testboard 1. Nf3 0.13 1... g6 0.48 ....
     *** Test abgebrochen wg. fehlerhaftem Zug ***    rn1r4/1p1b2bk/p2p1npp/2pPpp2/2P1P3/2N3P1/PP3PBP/1RBQ1RK1  w - e6 0 14
    Finished test of 5492 positions from Test set T_22xx.cts.       Evaluation deltas:  game state: 290,  piece values: 229,  basic mobility: 221,  max.clashes: 212,  new mobility: 221,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 228,  Mix Eval: 200.
    Testing Set T_22xxVs11xx.cts:
     *** Test abgebrochen wg. fehlerhaftem Zug ***    5b1P/pp6/2kpBp2/2p2P2/2n5/4P3/PP2KP2/7R  w - - 1 33
    **** Fehler: Fehlerhafter Zug: a4 -> b3 nicht möglich auf Board Testboard 1. e3 0.0 1... d5 0.0 2. ....
     *** Test abgebrochen wg. fehlerhaftem Zug ***    r5k1/1p3pp1/2p2n1p/3pq3/pPPNr3/P3P2P/2Q2PP1/2RR2K1  b - b3 0 22
    Finished test of 3378 positions from Test set T_22xxVs11xx.cts. Evaluation deltas:  game state: 540,  piece values: 345,  basic mobility: 322,  max.clashes: 322,  new mobility: 323,  attacks on opponent side: 342,  attacks on opponent king: 343,  defends on own king: 345,  Mix Eval: 290.
    Total Nr. of board evaluations: 17573
    Thereof within limits: 78%
    Quality of level basic mobility (2):    (same as basic piece value: 393)        - improvements: 10567 (-42)      - totally wrong: 6062 (33); - overdone: 551 (26)
    Quality of level max.clashes (3):       (same as basic piece value: 11892)      - improvements: 3709 (-145)      - totally wrong: 1678 (95); - overdone: 294 (96)
    Quality of level new mobility (4):      (same as basic piece value: 585)        - improvements: 10336 (-45)      - totally wrong: 6023 (36); - overdone: 629 (28)
    Quality of level attacks on opponent side (5):  (same as basic piece value: 948)- improvements: 10372 (-7)       - totally wrong: 6195 (6);  - overdone: 58 (5)
    Quality of level attacks on opponent king (6):  (same as basic piece value: 1157)-improvements: 10050 (-6)       - totally wrong: 6325 (5);  - overdone: 41 (3)
    Quality of level defends on own king (7):(same as basic piece value: 953)       - improvements: 8658 (-7)        - totally wrong: 7898 (7);  - overdone: 64 (5)
    Quality of level Mix Eval (8):          (same as basic piece value: 123)        - improvements: 11155 (-96)      - totally wrong: 5304 (57); - overdone: 991 (58)
    ---
    now much slower after addinh NoGo calculation, but a little bit improved quality:
    2022-08-11: 1 min 34 sec --> 187/Sec. (on "my VM"...)
    // with deactivated updateRelEval() it is only 1:01 => 288/Sec
    // and also deactivated evaluateClashes() only 40 sec! => 440/Sec
    // with only deactivated evaluateClashes: 40 sec
    Only 8 "**** Fehler" left - all with a pawn-beating-move seeming not possible.
    Finished test of 4136 positions from Test set T_13xx.cts.  Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 283,  max.clashes: 275,  new mobility: 282,  attacks on opponent side: 297,  attacks on opponent king: 296,  defends on own king: 301,  Mix Eval: 246.
    Finished test of 4593 positions from Test set T_16xx.cts.  Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 267,  max.clashes: 267,  new mobility: 266,  attacks on opponent side: 278,  attacks on opponent king: 279,  defends on own king: 284,  Mix Eval: 236.
    Finished test of 5492 positions from Test set T_22xx.cts.  Evaluation deltas:  game state: 290,  piece values: 229,  basic mobility: 220,  max.clashes: 212,  new mobility: 219,  attacks on opponent side: 226,  attacks on opponent king: 227,  defends on own king: 228,  Mix Eval: 195.
    Finished test of 3378 positions from Test set T_22xxVs11xx.cts.  Evaluation deltas:  game state: 540,  piece values: 345,  basic mobility: 317,  max.clashes: 322,  new mobility: 316,  attacks on opponent side: 336,  attacks on opponent king: 338,  defends on own king: 345,  Mix Eval: 274.
    Total Nr. of board evaluations: 17599
    Thereof within limits: 81%
    Quality of level basic mobility (2):  (same as basic piece value: 382)                - improvements: 10931 (-46)   - totally wrong: 5724 (35); - overdone: 562 (27)
    Quality of level max.clashes (3):  (same as basic piece value: 11906)                 - improvements: 3719 (-145)   - totally wrong: 1679 (95); - overdone: 295 (96)
    Quality of level new mobility (4):  (same as basic piece value: 524)                  - improvements: 10720 (-52)   - totally wrong: 5686 (39); - overdone: 669 (31)
    Quality of level attacks on opponent side (5):  (same as basic piece value: 330)      - improvements: 10222 (-19)   - totally wrong: 6825 (15); - overdone: 222 (11)
    Quality of level attacks on opponent king (6):  (same as basic piece value: 418)      - improvements: 10050 (-16)   - totally wrong: 6970 (12); - overdone: 161 (11)
    Quality of level defends on own king (7):  (same as basic piece value: 937)           - improvements: 8326 (-10)    - totally wrong: 8228 (10); - overdone: 108 (8)
    Quality of level Mix Eval (8):  (same as basic piece value: 89)                       - improvements: 11415 (-109)  - totally wrong: 4968 (63); - overdone: 1127 (59)
    ---
    quality optimization that also brings speed: do not pretend pqwns can easily move=beat diagonally, if there is no
    opponent piece and even none that could (directly) move there. -> This reduced the number of moves for pawns and thus
    pawns involved in clashes. Speeds up the above result from 1:34 to 1:16.
    Effect is even better, when re-activating the "bishop-behind-pawn-etc" machanism in clash evaluation.
    this brought slowdown by +1 min ! now, after the above + pre-sorting out pieces with no conditions to
    fulfill, it slows it down only by 15 sec (instead of 1 min!) to 1:31.
    2022-08-15: 1 min 31 sec --> still 187/sec.
    Still 8 "***" errors.
    *** Test abgebrochen wg. fehlerhaftem Zug *** 8/8/8/1K1p4/6k1/8/PPB5/7p  b - - 1 48 **** Fehler: Fehlerhafter Zug: e4 -> d3 nicht möglich auf Board Testboard 1. e4 0.24 1... c5 0.32 2....
    *** Test abgebrochen wg. fehlerhaftem Zug *** r1bqkb1r/1p2pppp/5n2/p1p1n1N1/P2Pp3/1BP5/1P3PPP/RNBQ1RK1  b kq d3 0 9
    **** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board Testboard 1. e4 0.24 1... e6 0.13 2....
    *** Test abgebrochen wg. fehlerhaftem Zug *** rnb1kqnr/5pb1/1pp1p2p/p2pP1p1/P2PB3/1PN1BN2/2PQ1PPP/R3K2R  w KQkq d6 0 13 **** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board Testboard 1. e4 0.24 1... e6 0.13 2....
    **** Fehler: Fehlerhafter Zug: a5 -> b6 nicht möglich auf Board Testboard 1. e4 0.24 1... g6 0.4 2.....
    *** Test abgebrochen wg. fehlerhaftem Zug *** 1rb1k2r/p1qn1pbp/2pp1np1/Pp2p3/3PP3/1BN1BN1P/1PP2PP1/R2QK2R  w KQk b6 0 11 **** Fehler: Fehlerhafter Zug: g5 -> f6 nicht möglich auf Board Testboard 1. d4 0.0 1... Nf6 0.19 2....
    *** Test abgebrochen wg. fehlerhaftem Zug *** r4rk1/2pqn1bp/6p1/2pPppP1/4P3/2NQ1N2/PP3P2/R3K1R1  w Q f6 0 20 **** Fehler: Fehlerhafter Zug: d5 -> e6 nicht möglich auf Board Testboard 1. Nf3 0.13 1... g6 0.48 ....
    *** Test abgebrochen wg. fehlerhaftem Zug *** rn1r4/1p1b2bk/p2p1npp/2pPpp2/2P1P3/2N3P1/PP3PBP/1RBQ1RK1  w - e6 0 14
    *** Test abgebrochen wg. fehlerhaftem Zug ***  5b1P/pp6/2kpBp2/2p2P2/2n5/4P3/PP2KP2/7R  w - - 1 33  **** Fehler: Fehlerhafter Zug: a4 -> b3 nicht möglich auf Board Testboard 1. e3 0.0 1... d5 0.0 2. ....
    *** Test abgebrochen wg. fehlerhaftem Zug ***  r5k1/1p3pp1/2p2n1p/3pq3/pPPNr3/P3P2P/2Q2PP1/2RR2K1  b - b3 0 22
    Testing Set T_13xx.cts:  44179 (981) 21312 (473) 18274 (406) 20362 (452) 17062 (379) 20943 (465) 21127 (469) 20915 (464) 14617 (324).   Finished test of 4136 positions from Test set T_13xx.cts.   Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 284,  max.clashes: 275,  new mobility: 282,  attacks on opponent side: 296,  attacks on opponent king: 297,  defends on own king: 300,  Mix Eval: 247.
    Testing Set T_16xx.cts:  62741 (922) 23413 (344) 21184 (311) 21975 (323) 21949 (322) 23172 (340) 23531 (346) 23106 (339) 19751 (290).   Finished test of 4593 positions from Test set T_16xx.cts. Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 266,  max.clashes: 267,  new mobility: 265,  attacks on opponent side: 280,  attacks on opponent king: 281,  defends on own king: 283,  Mix Eval: 238.
    Testing Set T_22xx.cts:  4643 (43) 7886 (73) 7986 (74) 6308 (58) 7716 (72) 7707 (72) 7900 (73) 7946 (74) 5951 (55).  Finished test of 5492 positions from Test set T_22xx.cts.  Evaluation deltas:  game state: 290,  piece values: 229,  basic mobility: 220,  max.clashes: 212,  new mobility: 221,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 228,  Mix Eval: 200.
    Testing Set T_22xxVs11xx.cts:  12616 (1802) 3545 (506) 2463 (351) 3373 (481) 2069 (295) 3367 (481) 3434 (490) 3315 (473) 1344 (192).     Finished test of 3378 positions from Test set T_22xxVs11xx.cts.  Evaluation deltas:  game state: 540,  piece values: 345,  basic mobility: 318,  max.clashes: 322,  new mobility: 318,  attacks on opponent side: 340,  attacks on opponent king: 342,  defends on own king: 345,  Mix Eval: 282.
    Total Nr. of board evaluations: 17599
    Thereof within limits: 81%
    Quality of level basic mobility (2):    (same as basic piece value: 396)        - improvements: 10820 (-47)  - totally wrong: 5794 (35); - overdone: 589 (28)
    Quality of level max.clashes (3):       (same as basic piece value: 11844)      - improvements: 3768 (-144)  - totally wrong: 1696 (93); - overdone: 291 (98)
    Quality of level new mobility (4):      (same as basic piece value: 438)        - improvements: 10258 (-61)  - totally wrong: 6022 (48); - overdone: 881 (41)
    Quality of level attacks on opponent side (5):(same as basic piece value: 670)  - improvements: 10442 (-11)  - totally wrong: 6379 (9);  - overdone: 108 (7)
    Quality of level attacks on opponent king (6):(same as basic piece value: 815)  - improvements: 10214 (-8)   - totally wrong: 6493 (6);  - overdone: 77 (5)
    Quality of level defends on own king (7):(same as basic piece value: 771)       - improvements: 8817 (-8)    - totally wrong: 7920 (7);  - overdone: 91 (6)
    Quality of level Mix Eval (8):          (same as basic piece value: 86)         - improvements: 10897 (-114) - totally wrong: 5354 (67); - overdone: 1262 (65)
    ---
    2022-08-17 while experimenting with isColorLikelyToComeHere()-chekcs for pawn-beating moves
    Testing Set T_13xx.cts:  44179 (981) 21312 (473) 18371 (408) 20362 (452) 18110 (402) 20890 (464) 21127 (469) 21022 (467) 16047 (356). Finished test of 4136 positions from Test set T_13xx.cts. Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 284,  max.clashes: 275,  new mobility: 285,  attacks on opponent side: 297,  attacks on opponent king: 298,  defends on own king: 300,  Mix Eval: 249.
    Testing Set T_16xx.cts:  62741 (922) 23413 (344) 20833 (306) 21975 (323) 21189 (311) 23100 (339) 23423 (344) 23012 (338) 18663 (274). Finished test of 4593 positions from Test set T_16xx.cts. Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 267,  max.clashes: 267,  new mobility: 269,  attacks on opponent side: 281,  attacks on opponent king: 281,  defends on own king: 284,  Mix Eval: 242.
    Testing Set T_22xx.cts:  4643 (43) 7886 (73) 8571 (80) 6308 (58) 8488 (79) 8004 (74) 8116 (75) 8008 (74) 7366 (68). Finished test of 5492 positions from Test set T_22xx.cts. Evaluation deltas:  game state: 290,  piece values: 229,  basic mobility: 220,  max.clashes: 212,  new mobility: 220,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 228,  Mix Eval: 198.
    Testing Set T_22xxVs11xx.cts: 12616 (1802) 3545 (506) 2508 (358) 3373 (481) 2545 (363) 3407 (486) 3481 (497) 3354 (479) 1946 (278). Finished test of 3378 positions from Test set T_22xxVs11xx.cts. Evaluation deltas:  game state: 540,  piece values: 345,  basic mobility: 317,  max.clashes: 321,  new mobility: 320,  attacks on opponent side: 340,  attacks on opponent king: 342,  defends on own king: 345,  Mix Eval: 284.
    Total Nr. of board evaluations: 17599 with 37565077 propagation que calls.
    Thereof within limits: 78%
    Quality of level basic mobility (2):  (same as basic piece value: 390) - improvements: 10945 (-46)   - totally wrong: 5719 (34); - overdone: 545 (27)
    Quality of level max.clashes (3):  (same as basic piece value: 11839)  - improvements: 3770 (-145)   - totally wrong: 1697 (93); - overdone: 293 (98)
    Quality of level new mobility (4):  (same as basic piece value: 608)   - improvements: 10497 (-47)   - totally wrong: 5879 (35); - overdone: 615 (28)
    Quality of level attacks on opponent side (5):  (same as basic piece value: 676)   - improvements: 10506 (-10)   - totally wrong: 6321 (8); - overdone: 96 (6)
    Quality of level attacks on opponent king (6):  (same as basic piece value: 919)   - improvements: 10198 (-8)   - totally wrong: 6429 (6); - overdone: 53 (5)
    Quality of level defends on own king (7):  (same as basic piece value: 919)   - improvements: 8689 (-7)   - totally wrong: 7923 (7); - overdone: 68 (6)
    Quality of level Mix Eval (8):  (same as basic piece value: 109)   - improvements: 11306 (-100)   - totally wrong: 5173 (57); - overdone: 1011 (57)
    boardEvaluation_Test() finished with 37281496 propagation que calls + 2299856 mobility updates.
    ---
    big slowdown (previous commit, not this), but much better NoG-calculation
    Calc up to 6 with "T_13xx.cts", "T_16xx.cts", "T_22xx.cts", "T_22xxVs11xx.cts"
    2023-04-22 4 min 59 sec - 5 min 32 sec - still 8 "***" errors.
    Testing Set T_13xx.cts: 44179 (981) 21312 (473) 18700 (415) 20362 (452) 18634 (414) 20968 (465) 21128 (469) 21056 (467) 16284 (361). Finished test of 4136 positions from Test set T_13xx.cts.       Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 284,  max.clashes: 275,  new mobility: 286,  attacks on opponent side: 296,  attacks on opponent king: 297,  defends on own king: 301,  Mix Eval: 251.
    Testing Set T_16xx.cts: 62741 (922) 23413 (344) 20784 (305) 21975 (323) 21321 (313) 23115 (339) 23436 (344) 23050 (338) 18673 (274). Finished test of 4593 positions from Test set T_16xx.cts.       Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 268,  max.clashes: 266,  new mobility: 270,  attacks on opponent side: 280,  attacks on opponent king: 281,  defends on own king: 284,  Mix Eval: 243.
    Testing Set T_22xx.cts:  4643 (43) 7886 (73) 8888 (83) 6308 (58) 8554 (79) 7938 (74) 8126 (75) 7982 (74) 7522 (70).                  Finished test of 5492 positions from Test set T_22xx.cts.       Evaluation deltas:  game state: 290,  piece values: 229,  basic mobility: 221,  max.clashes: 212,  new mobility: 222,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 228,  Mix Eval: 200.
    Testing Set T_22xxVs11xx.cts: 12616 (1802) 3545 (506) 2427 (346) 3373 (481) 2317 (331) 3361 (480) 3439 (491) 3341 (477) 1617 (231).  Finished test of 3378 positions from Test set T_22xxVs11xx.cts. Evaluation deltas:  game state: 540,  piece values: 345,  basic mobility: 318,  max.clashes: 322,  new mobility: 320,  attacks on opponent side: 340,  attacks on opponent king: 341,  defends on own king: 345,  Mix Eval: 285.
    Total Nr. of board evaluations: 17599
    Thereof within limits: 78%
    Quality of level basic mobility (2):  (same as basic piece value: 408)  - improvements: 10714 (-46)  - totally wrong: 5890 (35); - overdone: 587 (27)
    Quality of level max.clashes (3):  (same as basic piece value: 11854)   - improvements: 3760 (-145)  - totally wrong: 1694 (93); - overdone: 291 (98)
    Quality of level new mobility (4):  (same as basic piece value: 533)    - improvements: 10223 (-49)  - totally wrong: 6154 (39); - overdone: 689 (30)
    Quality of level attacks on opponent side (5):  (same as basic piece value: 574)  - improvements: 10403 (-12)  - totally wrong: 6505 (9); - overdone: 117 (7)
    Quality of level attacks on opponent king (6):  (same as basic piece value: 832)  - improvements: 10183 (-9)   - totally wrong: 6508 (7); - overdone: 76 (5)
    Quality of level defends on own king (7):  (same as basic piece value: 880)       - improvements: 8609 (-8)    - totally wrong: 8035 (8); - overdone: 75 (6)
    Quality of level Mix Eval (8):  (same as basic piece value: 107)        - improvements: 11071 (-103) - totally wrong: 5323 (61); - overdone: 1098 (58)
    boardEvaluation_Test() finished with 126394443 propagation que calls + 2299856 mobility updates.
                                         ^^^^^^^^^ = prev * 3.4   - at 3.6x longer time consumption, and almost same, but even slighly worse evaluation :-(
    /// less cases for quicker comparison:
    Calc up to 6 with "T_16xx.cts" only
    2023-04-23: 1 min 12 sec - 1 "*** Test abgebrochen" errors / 2 "**** Fehler"
    Finished test of 4593 positions from Test set T_16xx.cts.
    Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 268,  max.clashes: 266,  new mobility: 270,  attacks on opponent side: 280,  attacks on opponent king: 281,  defends on own king: 284,  Mix Eval: 243.
    Total Nr. of board evaluations: 4593
    Thereof within limits: 100%
    Quality of level Mix Eval (8):  (same as basic piece value: 23)
    - improvements: 2896 (-102)
    - totally wrong: 1411 (64); - overdone: 263 (56)
    boardEvaluation_Test() finished with 32905871 propagation que calls + 592304 mobility updates
    --- after a little less recalcs with only +/-2 variation:
    boardEvaluation_Test() finished with 32830341 propagation que calls + 592304 mobility updates.

    --- much better after introducing check if clashUpdates are necessary at all:
    2023-04-23: 35 sec - 1 "*** Test abgebrochen" errors / 2 "**** Fehler"
    Finished test of 4593 positions from Test set T_16xx.cts.
    Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 268,  max.clashes: 267,  new mobility: 270,  attacks on opponent side: 280,  attacks on opponent king: 281,  defends on own king: 284,  Mix Eval: 243.
    Total Nr. of board evaluations: 4593
    Thereof within limits: 100%
    Quality of level Mix Eval (8):  (same as basic piece value: 27)
      - improvements: 2893 (-101)
      - totally wrong: 1409 (64); - overdone: 264 (57)
    boardEvaluation_Test() finished with 17924751 propagation que calls + 592304 mobility updates.
    --> so the std. test with "T_13xx.cts", "T_16xx.cts","T_22xx.cts", "T_22xxVs11xx.cts" is much better again:
    2023-04-23: 2 min 9 sec
    Total Nr. of board evaluations: 17599
    Thereof within limits: 78%
    Quality of level Mix Eval (8):  (same as basic piece value: 98) - improvements: 11053 (-103)  - totally wrong: 5351 (60); - overdone: 1097 (58)
    boardEvaluation_Test() finished with 68784654 propagation que calls + 2299856 mobility updates.

    --- 2023-05-01: 2 min - 2 min 51 sec (for all 4 Testsets) --> commit
    Testing Set T_13xx.cts: 44179 (981) 21312 (473) 18374 (408) 20362 (452) 18070 (401) 20934 (465) 21133 (469) 21041 (467) 15976 (355).        Finished test of 4136 positions from Test set T_13xx.cts.       Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 284,  max.clashes: 275,  new mobility: 286,  attacks on opponent side: 297,  attacks on opponent king: 297,  defends on own king: 300,  Mix Eval: 251.
    Testing Set T_16xx.cts: 62741 (922) 23413 (344) 20967 (308) 21925 (322) 21573 (317) 23141 (340) 23431 (344) 23077 (339) 19073 (280).        Finished test of 4593 positions from Test set T_16xx.cts.       Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 267,  max.clashes: 267,  new mobility: 269,  attacks on opponent side: 281,  attacks on opponent king: 281,  defends on own king: 284,  Mix Eval: 242.
    Testing Set T_22xx.cts: 4643 (43) 7886 (73) 8447 (78) 6432 (60) 8286 (77) 7947 (74) 8097 (75) 8011 (74) 7281 (68).                          Finished test of 5492 positions from Test set T_22xx.cts.       Evaluation deltas:  game state: 290,  piece values: 229,  basic mobility: 220,  max.clashes: 212,  new mobility: 220,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 228,  Mix Eval: 199.
    Testing Set T_22xxVs11xx.cts: 12616 (1802) 3545 (506) 2499 (357) 3373 (481) 2609 (372) 3404 (486) 3473 (496) 3359 (479) 2004 (286).         Finished test of 3378 positions from Test set T_22xxVs11xx.cts. Evaluation deltas:  game state: 540,  piece values: 345,  basic mobility: 316,  max.clashes: 322,  new mobility: 320,  attacks on opponent side: 340,  attacks on opponent king: 341,  defends on own king: 345,  Mix Eval: 285.
    Total Nr. of board evaluations: 17599
    Thereof within limits: 78%
    Quality of level basic mobility (2):  (same as basic piece value: 418)      - improvements: 10838 (-47)     - totally wrong: 5761 (35); - overdone: 582 (28)
    Quality of level max.clashes (3):  (same as basic piece value: 11932)       - improvements: 3723 (-145)     - totally wrong: 1658 (94); - overdone: 286 (99)
    Quality of level new mobility (4):  (same as basic piece value: 588)        - improvements: 10535 (-46)     - totally wrong: 5837 (36); - overdone: 639 (28)
    Quality of level attacks on opponent side (5): (same as basic piece value: 659) - improvements: 10444 (-11) - totally wrong: 6392 (8);  - overdone: 104 (6)
    Quality of level attacks on opponent king (6):  (same as basic piece value: 898) - improvements: 10243 (-8) - totally wrong: 6400 (6);  - overdone: 58 (5)
    Quality of level defends on own king (7):  (same as basic piece value: 965)  - improvements: 8621 (-8)      - totally wrong: 7934 (8);  - overdone: 79 (6)
    Quality of level Mix Eval (8):  (same as basic piece value: 115)            - improvements: 11298 (-100)    - totally wrong: 5160 (58); - overdone: 1026 (58)
    boardEvaluation_Test() finished with 37374538 propagation que calls + 2299856 mobility updates.

    --- 2023-05-06: 1 min 33
    Testing Set T_13xx.cts: 44179 (981) 21312 (473) 18406 (409) 20362 (452) 18026 (400) 21006 (466) 21171 (470) 21030 (467) 16001 (355).        Finished test of 4136 positions from Test set T_13xx.cts.       Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 284,  max.clashes: 275,  new mobility: 285,  attacks on opponent side: 297,  attacks on opponent king: 298,  defends on own king: 300,  Mix Eval: 250.
    Testing Set T_16xx.cts: 62741 (922) 23413 (344) 21042 (309) 21925 (322) 21573 (317) 23104 (339) 23406 (344) 23101 (339) 19009 (279).        Finished test of 4593 positions from Test set T_16xx.cts.       Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 267,  max.clashes: 267,  new mobility: 268,  attacks on opponent side: 281,  attacks on opponent king: 281,  defends on own king: 284,  Mix Eval: 242.
    Testing Set T_22xx.cts: 4643 (43) 7886 (73) 8425 (78) 6425 (60) 8396 (78) 7957 (74) 8080 (75) 7996 (74) 7355 (68).                          Finished test of 5492 positions from Test set T_22xx.cts.       Evaluation deltas:  game state: 290,  piece values: 229,  basic mobility: 220,  max.clashes: 212,  new mobility: 220,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 228,  Mix Eval: 198.
    Testing Set T_22xxVs11xx.cts: 12616 (1802) 3545 (506) 2496 (356) 3373 (481) 2569 (367) 3415 (487) 3473 (496) 3344 (477) 1960 (280).         Finished test of 3378 positions from Test set T_22xxVs11xx.cts. Evaluation deltas:  game state: 540,  piece values: 345,  basic mobility: 316,  max.clashes: 322,  new mobility: 319,  attacks on opponent side: 340,  attacks on opponent king: 341,  defends on own king: 345,  Mix Eval: 284.
    Total Nr. of board evaluations: 17599 - Thereof within limits: 78%
    Quality of level basic mobility (2):  (same as basic piece value: 401)      - improvements: 10868 (-47)     - totally wrong: 5753 (35); - overdone: 577 (28)
    Quality of level max.clashes (3):  (same as basic piece value: 11925)       - improvements: 3727 (-145)     - totally wrong: 1661 (94); - overdone: 286 (98)
    Quality of level new mobility (4):  (same as basic piece value: 586)        - improvements: 10545 (-47)     - totally wrong: 5825 (36); - overdone: 643 (28)
    Quality of level attacks on opponent side (5):  (same as basic piece value: 682)- improvements: 10378 (-11)  - totally wrong: 6444 (8); - overdone: 95 (6)
    Quality of level attacks on opponent king (6):  (same as basic piece value: 900)- improvements: 10205 (-8)  - totally wrong: 6440 (6); - overdone: 54 (5)
    Quality of level defends on own king (7):  (same as basic piece value: 923)  - improvements: 8707 (-7)  - totally wrong: 7892 (7); - overdone: 77 (6)
    Quality of level Mix Eval (8):  (same as basic piece value: 121)             - improvements: 11356 (-99)  - totally wrong: 5120 (57); - overdone: 1002 (58)
    boardEvaluation_Test() finished with 37423238 propagation que calls + 2299856 mobility updates.

    --- 2023-05-19: 2 min 30 - but important bugs fixed ;-\ --> commit
    usual 8 "*** Test..." errors.
    Testing Set T_13xx.cts:  44179 (981) 21312 (473) 18439 (409) 21203 (471) 17970 (399) 20957 (465) 21165 (470) 21050 (467) 16710 (371).       Finished test of 4136 positions from Test set T_13xx.cts.       Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 284,  max.clashes: 275,  new mobility: 285,  attacks on opponent side: 297,  attacks on opponent king: 298,  defends on own king: 300,  Mix Eval: 250.
    Testing Set T_16xx.cts:  62741 (922) 23413 (344) 20892 (307) 22050 (324) 21421 (315) 23162 (340) 23428 (344) 23067 (339) 19043 (280).       Finished test of 4593 positions from Test set T_16xx.cts.       Evaluation deltas:  game state: 392,  piece values: 284,  basic mobility: 267,  max.clashes: 267,  new mobility: 269,  attacks on opponent side: 281,  attacks on opponent king: 281,  defends on own king: 284,  Mix Eval: 243.
    Testing Set T_22xx.cts:   4643 (43) 7886 (73) 8500 (79) 6256 (58) 8550 (79) 7869 (73) 8054 (75) 8034 (75) 7235 (67).                        Finished test of 5492 positions from Test set T_22xx.cts.       Evaluation deltas:  game state: 290,  piece values: 229,  basic mobility: 220,  max.clashes: 212,  new mobility: 221,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 228,  Mix Eval: 199.
    Testing Set T_22xxVs11xx.cts:  12616 (1802) 3545 (506) 2480 (354) 3155 (450) 2561 (365) 3406 (486) 3468 (495) 3347 (478) 1679 (239).        Finished test of 3378 positions from Test set T_22xxVs11xx.cts. Evaluation deltas:  game state: 540,  piece values: 345,  basic mobility: 316,  max.clashes: 322,  new mobility: 320,  attacks on opponent side: 340,  attacks on opponent king: 342,  defends on own king: 345,  Mix Eval: 284.
    Total Nr. of board evaluations: 17599 - Thereof within limits: 78%
    Quality of level basic mobility (2):  (same as basic piece value: 378)      - improvements: 10886 (-47)   - totally wrong: 5746 (35); - overdone: 589 (28)
    Quality of level max.clashes (3):  (same as basic piece value: 11833)       - improvements: 3747 (-155)   - totally wrong: 1698 (113); - overdone: 321 (114)
    Quality of level new mobility (4):  (same as basic piece value: 604)        - improvements: 10544 (-46)   - totally wrong: 5812 (36); - overdone: 639 (28)
    Quality of level attacks on opponent side (5):  (same as basic piece value: 666) - improvements: 10395 (-11) - totally wrong: 6431 (8); - overdone: 107 (6)
    Quality of level attacks on opponent king (6):  (same as basic piece value: 965) - improvements: 10154 (-8) - totally wrong: 6427 (6); - overdone: 53 (5)
    Quality of level defends on own king (7):  (same as basic piece value: 907) - improvements: 8661 (-7)     - totally wrong: 7960 (7); - overdone: 71 (6)
    Quality of level Mix Eval (8):  (same as basic piece value: 108)            - improvements: 11261 (-104)  - totally wrong: 5168 (65); - overdone: 1062 (65)
    boardEvaluation_Test() finished with 36730254 propagation que calls + 2299856 mobility updates.
    --- 2023-05-28: 3 min 0 sec - with 10% increase of necessary propagations for/after bug fixes
    eval very similar.
    error-message "Error in from-condition of ... points to empty square". which shows up 4129 times
    boardEvaluation_Test() finished with 39315286 propagation que calls + 2299856 mobility updates.
--- 2023-06-06:
    3 min 35 - may be slower or now includes some of the move selection per code? (to be checked)
    inkl. en-passant-"swindle" -> eliminates alle 8x "*** Test..." errors (still has 7 **** Fehler: Fehlerhafter Zug".
Testing Set T_13xx.cts:
Finished test of 4166 positions from Test set T_13xx.cts.
Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 282,  max.clashes: 274,  new mobility: 284,  attacks on opponent side: 296,  attacks on opponent king: 297,  defends on own king: 300,  Mix Eval: 248.
Testing Set T_16xx.cts:
Finished test of 4662 positions from Test set T_16xx.cts.
Evaluation deltas:  game state: 393,  piece values: 284,  basic mobility: 266,  max.clashes: 267,  new mobility: 269,  attacks on opponent side: 281,  attacks on opponent king: 282,  defends on own king: 284,  Mix Eval: 242.
Testing Set T_22xx.cts:
Finished test of 5596 positions from Test set T_22xx.cts.
Evaluation deltas:  game state: 293,  piece values: 230,  basic mobility: 220,  max.clashes: 213,  new mobility: 221,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 229,  Mix Eval: 199.
Testing Set T_22xxVs11xx.cts:
Finished test of 3404 positions from Test set T_22xxVs11xx.cts.
Evaluation deltas:  game state: 539,  piece values: 344,  basic mobility: 314,  max.clashes: 320,  new mobility: 319,  attacks on opponent side: 339,  attacks on opponent king: 340,  defends on own king: 343,  Mix Eval: 283.
Total Nr. of board evaluations: 17828
Thereof within limits: 78%
Quality of level basic mobility (2):  (same as basic piece value: 358)
  - improvements: 11120 (-47)
  - totally wrong: 5754 (35); - overdone: 596 (28)
Quality of level max.clashes (3):  (same as basic piece value: 11949)
  - improvements: 3827 (-155)
  - totally wrong: 1726 (112); - overdone: 326 (112)
Quality of level new mobility (4):  (same as basic piece value: 612)
  - improvements: 10689 (-47)
  - totally wrong: 5891 (36); - overdone: 636 (30)
Quality of level attacks on opponent side (5):  (same as basic piece value: 693)
  - improvements: 10620 (-10)
  - totally wrong: 6409 (8); - overdone: 106 (6)
Quality of level attacks on opponent king (6):  (same as basic piece value: 990)
  - improvements: 10314 (-8)
  - totally wrong: 6471 (6); - overdone: 53 (5)
Quality of level defends on own king (7):  (same as basic piece value: 886)
  - improvements: 8849 (-7)
  - totally wrong: 8017 (7); - overdone: 76 (5)
Quality of level Mix Eval (8):  (same as basic piece value: 110)
  - improvements: 11418 (-104)
  - totally wrong: 5234 (64); - overdone: 1066 (65)
boardEvaluation_Test() finished with 39758763 propagation que calls + 2900990 mobility updates.
----
2023-06-08
Testing Set T_13xx.cts:
Finished test of 4166 positions from Test set T_13xx.cts.
Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 282,  max.clashes: 274,  new mobility: 284,  attacks on opponent side: 296,  attacks on opponent king: 297,  defends on own king: 300,  Mix Eval: 248.
Testing Set T_16xx.cts:
Finished test of 4662 positions from Test set T_16xx.cts.
Evaluation deltas:  game state: 393,  piece values: 284,  basic mobility: 266,  max.clashes: 267,  new mobility: 269,  attacks on opponent side: 281,  attacks on opponent king: 282,  defends on own king: 284,  Mix Eval: 242.
Testing Set T_22xx.cts:
Finished test of 5596 positions from Test set T_22xx.cts.
Evaluation deltas:  game state: 293,  piece values: 230,  basic mobility: 220,  max.clashes: 213,  new mobility: 221,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 229,  Mix Eval: 199.
Testing Set T_22xxVs11xx.cts:
Finished test of 3404 positions from Test set T_22xxVs11xx.cts.
Evaluation deltas:  game state: 539,  piece values: 344,  basic mobility: 314,  max.clashes: 320,  new mobility: 319,  attacks on opponent side: 339,  attacks on opponent king: 340,  defends on own king: 343,  Mix Eval: 283.
Total Nr. of board evaluations: 17828
Thereof within limits: 78%
Quality of level basic mobility (2):  (same as basic piece value: 383)
  - improvements: 11111 (-47)
  - totally wrong: 5743 (35); - overdone: 591 (28)
Quality of level max.clashes (3):  (same as basic piece value: 11949)
  - improvements: 3827 (-155)
  - totally wrong: 1726 (112); - overdone: 326 (112)
Quality of level new mobility (4):  (same as basic piece value: 619)
  - improvements: 10684 (-47)
  - totally wrong: 5888 (36); - overdone: 637 (29)
Quality of level attacks on opponent side (5):  (same as basic piece value: 691)
  - improvements: 10618 (-10)
  - totally wrong: 6413 (8); - overdone: 106 (6)
Quality of level attacks on opponent king (6):  (same as basic piece value: 987)
  - improvements: 10324 (-8)
  - totally wrong: 6464 (6); - overdone: 53 (5)
Quality of level defends on own king (7):  (same as basic piece value: 885)
  - improvements: 8849 (-7)
  - totally wrong: 8018 (7); - overdone: 76 (5)
Quality of level Mix Eval (8):  (same as basic piece value: 103)
  - improvements: 11428 (-104)
  - totally wrong: 5230 (64); - overdone: 1067 (64)
boardEvaluation_Test() finished with 39632893 propagation que calls + 2900990 mobility updates.

    2023-06-16:
Testing Set T_13xx.cts:
Finished test of 4166 positions from Test set T_13xx.cts.
Evaluation deltas:  game state: 452,  piece values: 300,  basic mobility: 282,  max.clashes: 274,  new mobility: 284,  attacks on opponent side: 296,  attacks on opponent king: 297,  defends on own king: 300,  Mix Eval: 248.
Testing Set T_16xx.cts:
Finished test of 4662 positions from Test set T_16xx.cts.
Evaluation deltas:  game state: 393,  piece values: 284,  basic mobility: 267,  max.clashes: 267,  new mobility: 269,  attacks on opponent side: 281,  attacks on opponent king: 282,  defends on own king: 284,  Mix Eval: 243.
Testing Set T_22xx.cts:
Finished test of 5596 positions from Test set T_22xx.cts.
Evaluation deltas:  game state: 293,  piece values: 230,  basic mobility: 220,  max.clashes: 213,  new mobility: 221,  attacks on opponent side: 227,  attacks on opponent king: 228,  defends on own king: 229,  Mix Eval: 199.
Testing Set T_22xxVs11xx.cts:
Finished test of 3404 positions from Test set T_22xxVs11xx.cts.
Evaluation deltas:  game state: 539,  piece values: 344,  basic mobility: 314,  max.clashes: 320,  new mobility: 319,  attacks on opponent side: 339,  attacks on opponent king: 340,  defends on own king: 343,  Mix Eval: 283.
Total Nr. of board evaluations: 17828
Thereof within limits: 78%
Quality of level basic mobility (2):  (same as basic piece value: 394)
  - improvements: 11069 (-47)
  - totally wrong: 5771 (35); - overdone: 594 (28)
Quality of level max.clashes (3):  (same as basic piece value: 11949)
  - improvements: 3827 (-155)
  - totally wrong: 1726 (112); - overdone: 326 (112)
Quality of level new mobility (4):  (same as basic piece value: 603)
  - improvements: 10670 (-46)
  - totally wrong: 5922 (36); - overdone: 633 (28)
Quality of level attacks on opponent side (5):  (same as basic piece value: 670)
  - improvements: 10603 (-10)
  - totally wrong: 6452 (8); - overdone: 103 (6)
Quality of level attacks on opponent king (6):  (same as basic piece value: 1001)
  - improvements: 10312 (-8)
  - totally wrong: 6463 (6); - overdone: 52 (5)
Quality of level defends on own king (7):  (same as basic piece value: 887)
  - improvements: 8851 (-7)
  - totally wrong: 8018 (7); - overdone: 72 (6)
Quality of level Mix Eval (8):  (same as basic piece value: 108)
  - improvements: 11424 (-104)
  - totally wrong: 5237 (64); - overdone: 1059 (64)
boardEvaluation_Test() finished with 39195690 propagation que calls + 2900990 mobility updates.

    2023-07-06: 5 min 16 - 2x"*** T", 11x ****
/usr/lib/jvm/java-1.17.0-openjdk-amd64/bin/java -ea -Didea.test.cyclic.buffer.size=1048576 -javaagent:/opt/intellij-idea-community/lib/idea_rt.jar=39747:/opt/intellij-idea-community/bin -Dfile.encoding=UTF-8 -classpath /home/christian/.m2/repository/org/junit/platform/junit-platform-launcher/1.7.0/junit-platform-launcher-1.7.0.jar:/opt/intellij-idea-community/lib/idea_rt.jar:/opt/intellij-idea-community/plugins/junit/lib/junit5-rt.jar:/opt/intellij-idea-community/plugins/junit/lib/junit-rt.jar:/home/christian/IdeaProjects/TideEval/out/test/TideEval:/home/christian/IdeaProjects/TideEval/out/production/TideEval:/home/christian/.m2/repository/org/jetbrains/annotations/20.1.0/annotations-20.1.0.jar:/home/christian/.m2/repository/org/junit/jupiter/junit-jupiter/5.7.0/junit-jupiter-5.7.0.jar:/home/christian/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.7.0/junit-jupiter-api-5.7.0.jar:/home/christian/.m2/repository/org/apiguardian/apiguardian-api/1.1.0/apiguardian-api-1.1.0.jar:/home/christian/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar:/home/christian/.m2/repository/org/junit/platform/junit-platform-commons/1.7.0/junit-platform-commons-1.7.0.jar:/home/christian/.m2/repository/org/junit/jupiter/junit-jupiter-params/5.7.0/junit-jupiter-params-5.7.0.jar:/home/christian/.m2/repository/org/junit/jupiter/junit-jupiter-engine/5.7.0/junit-jupiter-engine-5.7.0.jar:/home/christian/.m2/repository/org/junit/platform/junit-platform-engine/1.7.0/junit-platform-engine-1.7.0.jar com.intellij.rt.junit.JUnitStarter -ideVersion5 -junit5 de.ensel.tideeval.FinalChessBoardEvalTest,boardEvaluation_Test

Testing Set T_13xx.cts:
 *** Test abgebrochen wg. fehlerhaftem Zug ***
**** Fehler: Fehlerhafter Zug: f7 -> f5 nicht möglich auf Board 2rq1r2/pp3pk1/3p2pb/3N4/P1pPP3/5Q1P/1P4P1/R4RK1  b - - 0 21.

2rq1r2/pp3pk1/3p2pb/3N4/P1pPP3/5Q1P/1P4P1/R4RK1  b - - 0 21
**** Fehler: Fehlerhafter Zug: e4 -> d3 nicht möglich auf Board r1bqkb1r/1p2pppp/5n2/p1p1n1N1/P2Pp3/1BP5/1P3PPP/RNBQ1RK1  b kq d3 0 9.


Finished test of 4154 positions from Test set T_13xx.cts.
Evaluation deltas:  game state: 453,  piece values: 300,  basic mobility: 282,  max.clashes: 274,  new mobility: 300,  attacks on opponent side: 296,  attacks on opponent king: 297,  defends on own king: 299,  Mix Eval: 264,  pceVals + best move[0]: 322,  pceVals + best move[0]+[1]/4: 323.

Testing Set T_16xx.cts:
**** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board rnb1kqnr/5pb1/1pp1p2p/p2pP1p1/P2PB3/1PN1BN2/2PQ1PPP/R3K2R  w KQkq d6 0 13.

**** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board rnbqkbnr/pp3pp1/2p1p2p/3pP3/2B5/5N2/PPPP1PPP/RNBQK2R  w KQkq d6 0 5.


Finished test of 4662 positions from Test set T_16xx.cts.
Evaluation deltas:  game state: 393,  piece values: 284,  basic mobility: 266,  max.clashes: 267,  new mobility: 284,  attacks on opponent side: 281,  attacks on opponent king: 282,  defends on own king: 284,  Mix Eval: 259,  pceVals + best move[0]: 302,  pceVals + best move[0]+[1]/4: 305.

Testing Set T_22xx.cts:
**** Fehler: Fehlerhafter Zug: f2 -> f4 nicht möglich auf Board R4b2/1p3rkp/2pp2p1/8/1PPpB2P/3P2P1/3n1PK1/2N5  w - - 1 31.
 *** Test abgebrochen wg. fehlerhaftem Zug ***

R4b2/1p3rkp/2pp2p1/8/1PPpB2P/3P2P1/3n1PK1/2N5  w - - 1 31
**** Fehler: Fehlerhafter Zug: a5 -> b6 nicht möglich auf Board 1rb1k2r/p1qn1pbp/2pp1np1/Pp2p3/3PP3/1BN1BN1P/1PP2PP1/R2QK2R  w KQk b6 0 11.

**** Fehler: Fehlerhafter Zug: g5 -> f6 nicht möglich auf Board r4rk1/2pqn1bp/6p1/2pPppP1/4P3/2NQ1N2/PP3P2/R3K1R1  w Q f6 0 20.

**** Fehler: Fehlerhafter Zug: d5 -> e6 nicht möglich auf Board rn1r4/1p1b2bk/p2p1npp/2pPpp2/2P1P3/2N3P1/PP3PBP/1RBQ1RK1  w - e6 0 14.


Finished test of 5539 positions from Test set T_22xx.cts.
Evaluation deltas:  game state: 296,  piece values: 231,  basic mobility: 222,  max.clashes: 214,  new mobility: 231,  attacks on opponent side: 229,  attacks on opponent king: 230,  defends on own king: 231,  Mix Eval: 209,  pceVals + best move[0]: 253,  pceVals + best move[0]+[1]/4: 254.

Testing Set T_22xxVs11xx.cts:
**** Fehler: Fehlerhafter Zug: a4 -> b3 nicht möglich auf Board r5k1/1p3pp1/2p2n1p/3pq3/pPPNr3/P3P2P/2Q2PP1/2RR2K1  b - b3 0 22.


Finished test of 3404 positions from Test set T_22xxVs11xx.cts.
Evaluation deltas:  game state: 539,  piece values: 344,  basic mobility: 314,  max.clashes: 320,  new mobility: 344,  attacks on opponent side: 339,  attacks on opponent king: 340,  defends on own king: 343,  Mix Eval: 308,  pceVals + best move[0]: 362,  pceVals + best move[0]+[1]/4: 364.
Total Nr. of board evaluations: 17759
Thereof within limits: 64%
Quality of level basic mobility (2):  (same as basic piece value: 382)
  - improvements: 11057 (-47)
  - totally wrong: 5735 (35); - overdone: 585 (28)
Quality of level max.clashes (3):  (same as basic piece value: 11918)
  - improvements: 3811 (-156)
  - totally wrong: 1710 (113); - overdone: 320 (113)
Quality of level new mobility (4):  (same as basic piece value: 17759)
  - improvements: 0 (-)
  - totally wrong: 0 (-); - overdone: 0 (-)
Quality of level attacks on opponent side (5):  (same as basic piece value: 664)
  - improvements: 10567 (-10)
  - totally wrong: 6431 (8); - overdone: 97 (6)
Quality of level attacks on opponent king (6):  (same as basic piece value: 992)
  - improvements: 10263 (-8)
  - totally wrong: 6449 (6); - overdone: 55 (4)
Quality of level defends on own king (7):  (same as basic piece value: 921)
  - improvements: 8808 (-7)
  - totally wrong: 7958 (7); - overdone: 72 (6)
Quality of level Mix Eval (8):  (same as basic piece value: 259)
  - improvements: 11312 (-72)
  - totally wrong: 5652 (46); - overdone: 536 (92)
Quality of level pceVals + best move[0] (9):  (same as basic piece value: 1919)
  - improvements: 9570 (-19)
  - totally wrong: 5943 (44); - overdone: 327 (866)
Quality of level pceVals + best move[0]+[1]/4 (10):  (same as basic piece value: 563)
  - improvements: 10184 (-21)
  - totally wrong: 6608 (48); - overdone: 404 (714)
boardEvaluation_Test() finished with 38144739 propagation que calls + 0 mobility updates.

---

Testing Set T_13xx.cts:
Finished test of 4154 positions from Test set T_13xx.cts.
Evaluation deltas:  game state: 453,  piece values: 300,  basic mobility: 282,  max.clashes: 274,  new mobility: 300,  attacks on opponent side: 296,  attacks on opponent king: 297,  defends on own king: 299,  Mix Eval: 264,  pceVals + best move[0]: 316,  pceVals + best move[0]+[1]/4: 333.
Testing Set T_16xx.cts:
Finished test of 4662 positions from Test set T_16xx.cts.
Evaluation deltas:  game state: 393,  piece values: 284,  basic mobility: 266,  max.clashes: 266,  new mobility: 284,  attacks on opponent side: 281,  attacks on opponent king: 282,  defends on own king: 284,  Mix Eval: 258,  pceVals + best move[0]: 303,  pceVals + best move[0]+[1]/4: 332.
Testing Set T_22xx.cts:
Finished test of 5539 positions from Test set T_22xx.cts.
Evaluation deltas:  game state: 296,  piece values: 231,  basic mobility: 222,  max.clashes: 213,  new mobility: 231,  attacks on opponent side: 229,  attacks on opponent king: 230,  defends on own king: 231,  Mix Eval: 208,  pceVals + best move[0]: 243,  pceVals + best move[0]+[1]/4: 255.
Testing Set T_22xxVs11xx.cts:
Finished test of 3404 positions from Test set T_22xxVs11xx.cts.
Evaluation deltas:  game state: 539,  piece values: 344,  basic mobility: 314,  max.clashes: 318,  new mobility: 344,  attacks on opponent side: 339,  attacks on opponent king: 340,  defends on own king: 343,  Mix Eval: 305,  pceVals + best move[0]: 362,  pceVals + best move[0]+[1]/4: 377.
Total Nr. of board evaluations: 17759
Thereof within limits: 64%
Quality of level basic mobility (2):  (same as basic piece value: 357)
  - improvements: 11020 (-48)
  - totally wrong: 5793 (35); - overdone: 589 (28)
Quality of level max.clashes (3):  (same as basic piece value: 11943)
  - improvements: 3818 (-156)
  - totally wrong: 1682 (111); - overdone: 316 (104)
Quality of level new mobility (4):  (same as basic piece value: 17759)
  - improvements: 0 (-)
  - totally wrong: 0 (-); - overdone: 0 (-)
Quality of level attacks on opponent side (5):  (same as basic piece value: 678)
  - improvements: 10608 (-10)
  - totally wrong: 6373 (8); - overdone: 100 (6)
Quality of level attacks on opponent king (6):  (same as basic piece value: 975)
  - improvements: 10334 (-8)
  - totally wrong: 6394 (6); - overdone: 56 (5)
Quality of level defends on own king (7):  (same as basic piece value: 832)
  - improvements: 8915 (-7)
  - totally wrong: 7939 (7); - overdone: 73 (6)
Quality of level Mix Eval (8):  (same as basic piece value: 257)
  - improvements: 11365 (-73)
  - totally wrong: 5607 (45); - overdone: 530 (86)
Quality of level pceVals + best move[0] (9):  (same as basic piece value: 2049)
  - improvements: 8912 (-22)
  - totally wrong: 6477 (43); - overdone: 321 (623)
Quality of level pceVals + best move[0]+[1]/4 (10):  (same as basic piece value: 646)
  - improvements: 9603 (-24)
  - totally wrong: 7088 (66); - overdone: 422 (870)
boardEvaluation_Test() finished with 41285237 propagation que calls + 0 mobility updates.
--
    2023-08-11 v29q
Testing Set T_13xx.cts:
 *** Test abgebrochen wg. fehlerhaftem Zug ***
1r5k/p1p3pp/b1p5/8/1b6/1PN1P3/P1P1NPPP/6KR  w - - 1 18
**** Fehler: Fehlerhafter Zug: e4 -> d3 nicht möglich auf Board r1bqkb1r/1p2pppp/5n2/p1p1n1N1/P2Pp3/1BP5/1P3PPP/RNBQ1RK1  b kq d3 0 9.
Board: r1bqkb1r/1p2pppp/5n2/p1p1n1N1/P2Pp3/1BP5/1P3PPP/RNBQ1RK1  b kq d3 0 9
Finished test of 4077 positions from Test set T_13xx.cts.
Evaluation deltas:  game state: 454,  piece values: 299,  basic mobility: 282,  max.clashes: 273,  new mobility: 299,  attacks on opponent side: 295,  attacks on opponent king: 296,  defends on own king: 299,  Mix Eval: 263,  pceVals + best move[0]: 309,  pceVals + best move[0]+[1]/4: 317.
Testing Set T_16xx.cts:
**** Fehler: Fehlerhafter Zug: f7 -> f5 nicht möglich auf Board 8/5p2/2p3kp/BpNb2p1/8/P1P4P/5PP1/5RK1  b - - 2 30.
Board: 8/5p2/2p3kp/BpNb2p1/8/P1P4P/5PP1/5RK1  b - - 2 30
 *** Test abgebrochen wg. fehlerhaftem Zug ***
8/5p2/2p3kp/BpNb2p1/8/P1P4P/5PP1/5RK1  b - - 2 30
**** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board rnb1kqnr/5pb1/1pp1p2p/p2pP1p1/P2PB3/1PN1BN2/2PQ1PPP/R3K2R  w KQkq d6 0 13.
Board: rnb1kqnr/5pb1/1pp1p2p/p2pP1p1/P2PB3/1PN1BN2/2PQ1PPP/R3K2R  w KQkq d6 0 13
**** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board rnbqkbnr/pp3pp1/2p1p2p/3pP3/2B5/5N2/PPPP1PPP/RNBQK2R  w KQkq d6 0 5.
Board: rnbqkbnr/pp3pp1/2p1p2p/3pP3/2B5/5N2/PPPP1PPP/RNBQK2R  w KQkq d6 0 5
Finished test of 4646 positions from Test set T_16xx.cts.
Evaluation deltas:  game state: 391,  piece values: 285,  basic mobility: 267,  max.clashes: 267,  new mobility: 285,  attacks on opponent side: 282,  attacks on opponent king: 282,  defends on own king: 284,  Mix Eval: 259,  pceVals + best move[0]: 299,  pceVals + best move[0]+[1]/4: 315.
Testing Set T_22xx.cts:
**** Fehler: Fehlerhafter Zug: f2 -> f4 nicht möglich auf Board R4b2/1p3rkp/2pp2p1/8/1PPpB2P/3P2P1/3n1PK1/2N5  w - - 1 31.
Board: R4b2/1p3rkp/2pp2p1/8/1PPpB2P/3P2P1/3n1PK1/2N5  w - - 1 31
 *** Test abgebrochen wg. fehlerhaftem Zug ***
R4b2/1p3rkp/2pp2p1/8/1PPpB2P/3P2P1/3n1PK1/2N5  w - - 1 31
**** Fehler: Fehlerhafter Zug: a5 -> b6 nicht möglich auf Board 1rb1k2r/p1qn1pbp/2pp1np1/Pp2p3/3PP3/1BN1BN1P/1PP2PP1/R2QK2R  w KQk b6 0 11.
Board: 1rb1k2r/p1qn1pbp/2pp1np1/Pp2p3/3PP3/1BN1BN1P/1PP2PP1/R2QK2R  w KQk b6 0 11
**** Fehler: Fehlerhafter Zug: g5 -> f6 nicht möglich auf Board r4rk1/2pqn1bp/6p1/2pPppP1/4P3/2NQ1N2/PP3P2/R3K1R1  w Q f6 0 20.
Board: r4rk1/2pqn1bp/6p1/2pPppP1/4P3/2NQ1N2/PP3P2/R3K1R1  w Q f6 0 20
**** Fehler: Fehlerhafter Zug: d5 -> e6 nicht möglich auf Board rn1r4/1p1b2bk/p2p1npp/2pPpp2/2P1P3/2N3P1/PP3PBP/1RBQ1RK1  w - e6 0 14.
Board: rn1r4/1p1b2bk/p2p1npp/2pPpp2/2P1P3/2N3P1/PP3PBP/1RBQ1RK1  w - e6 0 14
Finished test of 5539 positions from Test set T_22xx.cts.
Evaluation deltas:  game state: 296,  piece values: 231,  basic mobility: 222,  max.clashes: 213,  new mobility: 231,  attacks on opponent side: 229,  attacks on opponent king: 230,  defends on own king: 231,  Mix Eval: 207,  pceVals + best move[0]: 241,  pceVals + best move[0]+[1]/4: 249.
Testing Set T_22xxVs11xx.cts:
**** Fehler: Fehlerhafter Zug: a4 -> b3 nicht möglich auf Board r5k1/1p3pp1/2p2n1p/3pq3/pPPNr3/P3P2P/2Q2PP1/2RR2K1  b - b3 0 22.
Board: r5k1/1p3pp1/2p2n1p/3pq3/pPPNr3/P3P2P/2Q2PP1/2RR2K1  b - b3 0 22
Finished test of 3404 positions from Test set T_22xxVs11xx.cts.
Evaluation deltas:  game state: 539,  piece values: 344,  basic mobility: 314,  max.clashes: 318,  new mobility: 344,  attacks on opponent side: 338,  attacks on opponent king: 340,  defends on own king: 343,  Mix Eval: 305,  pceVals + best move[0]: 355,  pceVals + best move[0]+[1]/4: 364.
Total Nr. of board evaluations: 17666
Thereof within limits: 64%
Quality of level basic mobility (2):  (same as basic piece value: 400)
  - improvements: 10930 (-48)
  - totally wrong: 5744 (35); - overdone: 592 (28)
Quality of level max.clashes (3):  (same as basic piece value: 11873)
  - improvements: 3798 (-156)
  - totally wrong: 1687 (109); - overdone: 308 (106)
Quality of level new mobility (4):  (same as basic piece value: 17666)
  - improvements: 0 (-)
  - totally wrong: 0 (-); - overdone: 0 (-)
Quality of level attacks on opponent side (5):  (same as basic piece value: 645)
  - improvements: 10573 (-10)
  - totally wrong: 6350 (8); - overdone: 98 (6)
Quality of level attacks on opponent king (6):  (same as basic piece value: 950)
  - improvements: 10281 (-8)
  - totally wrong: 6379 (6); - overdone: 56 (4)
Quality of level defends on own king (7):  (same as basic piece value: 844)
  - improvements: 8826 (-7)
  - totally wrong: 7921 (7); - overdone: 75 (5)
Quality of level Mix Eval (8):  (same as basic piece value: 276)
  - improvements: 11285 (-73)
  - totally wrong: 5583 (45); - overdone: 522 (87)
Quality of level pceVals + best move[0] (9):  (same as basic piece value: 2059)
  - improvements: 9355 (-16)
  - totally wrong: 6057 (32); - overdone: 195 (816)
Quality of level pceVals + best move[0]+[1]/4 (10):  (same as basic piece value: 777)
  - improvements: 9968 (-19)
  - totally wrong: 6605 (46); - overdone: 316 (847)
boardEvaluation_Test() finished with 132332389 propagation que calls + 0 mobility updates.
Actual   :63.63636363636363

024-03-27 v0.48h63n
Testing Set T_13xx.cts:
 *** Test abgebrochen wg. fehlerhaftem Zug ***
1r5k/p1p3pp/b1p5/8/1b6/1PN1P3/P1P1NPPP/6KR w - - 1 18
**** Fehler: Fehlerhafter Zug: e4 -> d3 nicht möglich auf Board r1bqkb1r/1p2pppp/5n2/p1p1n1N1/P2Pp3/1BP5/1P3PPP/RNBQ1RK1 b kq d3 0 9 (1 NoGo&if{any-d3 (weiß)}).
Board: r1bqkb1r/1p2pppp/5n2/p1p1n1N1/P2Pp3/1BP5/1P3PPP/RNBQ1RK1 b kq d3 0 9
Finished test of 4077 positions from Test set T_13xx.cts.
Evaluation deltas:  game state: 454,  piece values: 299,  basic mobility: 282,  max.clashes: 273,  new mobility: 299,  attacks on opponent side: 295,  attacks on opponent king: 296,  defends on own king: 299,  Mix Eval: 263,  pceVals + best move[0]: 343,  pceVals + best move[0]+[1]/4: 342.
Testing Set T_16xx.cts:
**** Fehler: Fehlerhafter Zug: g8h8 schlägt eigene Figur auf r5kr/2q2p1p/1p2p3/p1p1N1Q1/3p2P1/2PP3P/PP6/RN3RK1 b - - 1 18.
Board: r5kr/2q2p1p/1p2p3/p1p1N1Q1/3p2P1/2PP3P/PP6/RN3RK1 b - - 1 18
 *** Test abgebrochen wg. fehlerhaftem Zug ***
r5kr/2q2p1p/1p2p3/p1p1N1Q1/3p2P1/2PP3P/PP6/RN3RK1 b - - 1 18
**** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board rnb1kqnr/5pb1/1pp1p2p/p2pP1p1/P2PB3/1PN1BN2/2PQ1PPP/R3K2R w KQkq d6 0 13 (1 NoGo&if{any-d6 (schwarz)}).
Board: rnb1kqnr/5pb1/1pp1p2p/p2pP1p1/P2PB3/1PN1BN2/2PQ1PPP/R3K2R w KQkq d6 0 13
**** Fehler: Fehlerhafter Zug: e5 -> d6 nicht möglich auf Board rnbqkbnr/pp3pp1/2p1p2p/3pP3/2B5/5N2/PPPP1PPP/RNBQK2R w KQkq d6 0 5 (1 NoGo&if{any-d6 (schwarz)}).
Board: rnbqkbnr/pp3pp1/2p1p2p/3pP3/2B5/5N2/PPPP1PPP/RNBQK2R w KQkq d6 0 5
Finished test of 4654 positions from Test set T_16xx.cts.
Evaluation deltas:  game state: 392,  piece values: 283,  basic mobility: 265,  max.clashes: 264,  new mobility: 283,  attacks on opponent side: 280,  attacks on opponent king: 280,  defends on own king: 283,  Mix Eval: 256,  pceVals + best move[0]: 344,  pceVals + best move[0]+[1]/4: 343.
Testing Set T_22xx.cts:
**** Fehler: Fehlerhafter Zug: a5 -> b6 nicht möglich auf Board 1rb1k2r/p1qn1pbp/2pp1np1/Pp2p3/3PP3/1BN1BN1P/1PP2PP1/R2QK2R w KQk b6 0 11 (1 NoGo&if{any-b6 (schwarz)}).
Board: 1rb1k2r/p1qn1pbp/2pp1np1/Pp2p3/3PP3/1BN1BN1P/1PP2PP1/R2QK2R w KQk b6 0 11
**** Fehler: Fehlerhafter Zug: g5 -> f6 nicht möglich auf Board r4rk1/2pqn1bp/6p1/2pPppP1/4P3/2NQ1N2/PP3P2/R3K1R1 w Q f6 0 20 (1 NoGo&if{any-f6 (schwarz)}).
Board: r4rk1/2pqn1bp/6p1/2pPppP1/4P3/2NQ1N2/PP3P2/R3K1R1 w Q f6 0 20
**** Fehler: Fehlerhafter Zug: d5 -> e6 nicht möglich auf Board rn1r4/1p1b2bk/p2p1npp/2pPpp2/2P1P3/2N3P1/PP3PBP/1RBQ1RK1 w - e6 0 14 (1 NoGo&if{any-e6 (schwarz)}).
Board: rn1r4/1p1b2bk/p2p1npp/2pPpp2/2P1P3/2N3P1/PP3PBP/1RBQ1RK1 w - e6 0 14
Finished test of 5596 positions from Test set T_22xx.cts.
Evaluation deltas:  game state: 293,  piece values: 228,  basic mobility: 219,  max.clashes: 210,  new mobility: 228,  attacks on opponent side: 226,  attacks on opponent king: 227,  defends on own king: 228,  Mix Eval: 205,  pceVals + best move[0]: 278,  pceVals + best move[0]+[1]/4: 278.
Testing Set T_22xxVs11xx.cts:
**** Fehler: Fehlerhafter Zug: a4 -> b3 nicht möglich auf Board r5k1/1p3pp1/2p2n1p/3pq3/pPPNr3/P3P2P/2Q2PP1/2RR2K1 b - b3 0 22 (1 NoGo&if{any-b3 (weiß)}).
Board: r5k1/1p3pp1/2p2n1p/3pq3/pPPNr3/P3P2P/2Q2PP1/2RR2K1 b - b3 0 22
Finished test of 3404 positions from Test set T_22xxVs11xx.cts.
Evaluation deltas:  game state: 539,  piece values: 342,  basic mobility: 314,  max.clashes: 316,  new mobility: 342,  attacks on opponent side: 337,  attacks on opponent king: 339,  defends on own king: 342,  Mix Eval: 304,  pceVals + best move[0]: 396,  pceVals + best move[0]+[1]/4: 395.
Total Nr. of board evaluations: 17731
Thereof within limits: 66%
Quality of level basic mobility (2):  (same as basic piece value: 381)
  - improvements: 11047 (-47)
  - totally wrong: 5715 (35); - overdone: 588 (27)
Quality of level max.clashes (3):  (same as basic piece value: 11918)
  - improvements: 3805 (-159)
  - totally wrong: 1681 (111); - overdone: 327 (104)
Quality of level new mobility (4):  (same as basic piece value: 17731)
  - improvements: 0 (-)
  - totally wrong: 0 (-); - overdone: 0 (-)
Quality of level attacks on opponent side (5):  (same as basic piece value: 666)
  - improvements: 10640 (-11)
  - totally wrong: 6320 (8); - overdone: 105 (6)
Quality of level attacks on opponent king (6):  (same as basic piece value: 870)
  - improvements: 10387 (-8)
  - totally wrong: 6410 (6); - overdone: 64 (5)
Quality of level defends on own king (7):  (same as basic piece value: 893)
  - improvements: 8811 (-7)
  - totally wrong: 7953 (7); - overdone: 74 (6)
Quality of level Mix Eval (8):  (same as basic piece value: 304)
  - improvements: 11289 (-74)
  - totally wrong: 5567 (45); - overdone: 571 (82)
Quality of level pceVals + best move[0] (9):  (same as basic piece value: 2546)
  - improvements: 9131 (-15)
  - totally wrong: 5875 (84); - overdone: 179 (3171)
Quality of level pceVals + best move[0]+[1]/4 (10):  (same as basic piece value: 993)
  - improvements: 9931 (-17)
  - totally wrong: 6557 (78); - overdone: 250 (2276)
boardEvaluation_Test() finished with 91051418 propagation que calls + 0 mobility updates.
org.opentest4j.AssertionFailedError:
Expected :100.0
Actual   :65.9090909090909
boardEvaluation_Simple_Test() finished with 17034 propagation que calls + 0 mobility updates.

Process finished with exit code 255

    */
    @Test
    void boardEvaluation_Test() {
        String[] testSetFiles = {
                   "T_13xx.cts" ,
                "T_16xx.cts"
                   , "T_22xx.cts", "T_22xxVs11xx.cts"
                // , "V_13xx.cts", "V_16xx.cts", "V_22xx.cts", "V_22xxVs11xx.cts"
        };
        long startcntProp = ChessPiece.debug_propagationCounter;
        long startcntMob  = ChessPiece.debug_updateMobilityCounter;
        int[] expectedDeltaAvg = { 600, 400, 350, 300, 300, 280, 300, 300, 280, 200, 200 };
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
        debugPrintln(true, "boardEvaluation_Test() finished with " + (ChessPiece.debug_propagationCounter -startcntProp) + " propagation que calls + " + (ChessPiece.debug_updateMobilityCounter -startcntMob) + " mobility updates." );

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
                //System.out.println();
                //System.out.println("...tested "+testedPositionsCounter+" positions from Test set "+ctsFilename+"...");
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
                FENPOS_STARTPOS);
        ChessGameReader cgr = new ChessGameReader(ctsOneGameLine);
        int[] evalDeltaSum = new int[EVAL_INSIGHT_LEVELS];
        // skip evaluation of some moves by just making the moves
        debugPrint(DEBUGMSG_TESTCASES||true, "");
        for (int i = 0; i < SKIP_OPENING_MOVES && cgr.hasNext(); i++) {
            String move = cgr.getNextMove();
            /* extra test - e.g. for inconsistency checks on move selections:
            chessBoard.getBestMove();
            EvaluatedMove bm = chessBoard.bestMove;
            debugPrintln(DEBUGMSG_TESTCASES||true, " test: " + move +" | " + bm);
            if (!bm.isBasicallyALegalMove())
                chessBoard.internalErrorPrintln("Illegal Move " + bm + " chosen!!");
             */
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
        long startcntProp = ChessPiece.debug_propagationCounter;
        long startcntMob  = ChessPiece.debug_updateMobilityCounter;
        boardEvaluation_SingleBoard_Test(FENPOS_STARTPOS, 0, 50);
        boardEvaluation_SingleBoard_Test( FENPOS_EMPTY, 0, 10);
        boardEvaluation_SingleBoard_Test( "rnbqk1nr/p1p2ppp/1p6/3p4/3P4/1P6/P1P2PPP/RNBQK1NR  w KQkq - 0 2", 0, 50);
        // 2022-08-17: boardEvaluation_Simple_Test() fnished with 23765 propagation que calls.
        debugPrintln(true, "boardEvaluation_Simple_Test() finished with " + (ChessPiece.debug_propagationCounter -startcntProp) + " propagation que calls + " + (ChessPiece.debug_updateMobilityCounter -startcntMob) + " mobility updates." );
    }

    void boardEvaluation_SingleBoard_Test(String fen, int expectedEval, int tolerance) {
        int[] evalDeltaSum = new int[EVAL_INSIGHT_LEVELS];
        //TODO: Read file, iterate over test-boards in there
        ChessBoard chessBoard = new ChessBoard("Test " + fen, fen );
        boardEvaluation_SingleBoard_Test(chessBoard, expectedEval, tolerance);
    }

    static void boardEvaluation_SingleBoard_Test(ChessBoard chessBoard, int expectedEval, int tolerance) {
        debugPrintln(DEBUGMSG_TESTCASES, "Testing " + chessBoard.getShortBoardName() );
        int overLimit = 0;
        for (int i = 0; i<ChessBoard.EVAL_INSIGHT_LEVELS; i++) {
            int eval = chessBoard.boardEvaluation(i);
            debugPrintln(DEBUGMSG_TESTCASES, "Evaluation " + getEvaluationLevelLabel(i) + "(" + i + ") is: " + eval + " -> delta: " + (eval- expectedEval) );
            if ( i>0 && abs( eval - expectedEval) > tolerance)
                overLimit++;
        }
        assertEquals(0, overLimit );
    }

}
