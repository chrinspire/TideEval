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
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.FinalChessBoardEvalTest.*;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.*;

class ChessBoardTest {

    // choose the one best move
    @ParameterizedTest
    @CsvSource({
            //temporary/debug tests
            //interresting for future move selection - best move remains best, even if second best h2h3 is chosen: "r2qkb1r/pp2pppp/2p2n2/3P4/Q3PPn1/2N5/PP3P1P/R1B1KB1R w KQkq - 0 11, d5c6|h2h3|f2f3"
            //"rn1qkb1r/p1p2ppb/1p2pn1p/4N3/2pP2P1/1Q5P/PP1NPP2/R1B1KB1R w KQkq - 0 9, b3c4"  // check is too tempting, it chooses b3b5, which is ok, but the the position is a little worse as a result
        //"5rk1/p2qppb1/3p2pp/8/4P1b1/1PN1BPP1/P1Q4K/3R4 b - - 0 24, g4f3"
            //"rnb1kbnr/pp3ppp/3qp3/2p1P3/3p4/P4N2/NPPP1PPP/R1BQKB1R b KQkq - 0 6, d6c7"
            //"r1bqkbnr/ppp2ppp/2n5/3pp3/Q7/2N1PN2/PPPP1PPP/R1B1KB1R b KQkq - 1 5, d5d6"
            //"r1bqkbnr/ppp2ppp/2n1p3/3p4/6Q1/2N1PN2/PPPP1PPP/R1B1KB1R b KQkq - 1 4 moves e6e5 g4a4, d5d6"
            //"r3kb1r/pp3ppp/3p4/N3p3/1n1pn2B/5N2/PPQ2PPP/R4RK1 b kq - 0 14, b4c2"  // just take the queen back!
            //"r1b1kbnr/3n1ppp/p3p3/q1pp4/Np1P4/1B2PN2/PPPB1PPP/R2QK2R  b KQkq - 1 9, c5c4"
            //"3k4/1p6/r3P3/p7/7P/8/nP4P1/5RK1 b - - 0 33, a6e6"
            //"r1b1k2r/pp4pp/2p2b2/P2pp3/5BB1/2NQ2P1/1qP1PP1P/RR4K1 b kq - 1 16, b2a1|b2b1" // queen is hoplessly lost, but can take a rook with her
            //"r5r1/1pp2p2/1n1k1p2/2RP4/2p1P1Bp/p1R4P/P3KPP1/8 b - - 10 32 moves d6c5, a1a1" // Bug was no move, so any is fine :-) - was not reproduceable
            //"N1b4r/pp1p1k1p/4p1pQ/2p2p2/P7/3qP1PB/1P1n1P1P/2n3KR b - - 1 28, d2f3"  // was no move, but not reproduceable
            //"N1b4r/pp1p1k1p/4p1pQ/2p2p2/P7/3qP1P1/1P1n1PBP/2n3KR w - - 0 28 moves g2h3, d2f3"
            //BUG: Queen move h4h6 leads to problem with (ill)legal pawn move and thus illegal suggestion h7h5
            //"r1b2k1r/ppNp3p/4p1p1/2p2p2/P6Q/1n1qP1P1/1P1n1PBP/2B3KR w - - 4 26 moves h4h6 f8f7 c7a8 b3c1 g2h3, d2f3" // NOT h7h5
/*TODO: Bug            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 moves d2d4 g8f6 d1d3 d7d5 b1c3 c7c5 d4c5 b8c6 c1f4 c6b4 d3d2 f6e4 d2d4 b4c2 e1d1 c2d4 c3e4 d5e4 g2g4 d8a5 a2a4" +
                    " d4b3 a1a2 b3c5 b2b4, a5b4" // bug: was "a4b4" - move for the wrong color??
 */
//bug fixed, but anyway another bad move :-)            "r3qrk1/4bppp/1Q1ppn2/p7/b2P4/5N2/1P2PPPP/R1B1KB1R w KQ - 0 16, a1a1" // NOT a1a4
            //"r2r3k/pp6/2nPbNpp/4p3/2P2p2/2P5/P3PPPP/3RKB1R w K - 4 20, a1a1" // no not block covering of pawn ba f6d5
            //?"rnb2k1r/p4ppp/2pP4/1p1Q4/3P4/2N5/P3PPPP/R3KB1R w KQ - 0 17, a1a1"
            //"6k1/6pp/4p3/1N6/3P3P/p3K3/8/8 b - - 0 36, a1a1"
            //"r4rk1/ppqn1ppp/4b3/2pp2b1/2P5/1PQ3P1/P3PPBP/R1B2RK1 w - - 0 15, c1g5"
/*TODO:Bug*/ //            "r1b1kb1r/1ppp1ppp/5n2/p1q1p3/2B1P3/3QN3/PPPP1PPP/R1B2RK1 b kq - 5 11, a1a1" // NOT b7b5 - problem with clasheval for pawns?
            //BUg? "1k6/5p1p/3P4/p6P/6K1/p2q2P1/8/8 b - - 0 39, d3d6|a3a2" // do not let opponents pawn promote
            //"8/p3kp1p/1P6/4p2p/2K1P3/8/8/8 b - - 0 41, a7b6"
            //"2r3k1/pp3pp1/7p/3bP3/P7/5P2/1Rn1r1PP/2R4K w - - 4 28, a1a1"
            //"r3n1k1/p1p2p1p/8/RPn5/2Pr2pP/6P1/4PPR1/1N2K1N1 b - - 28 34, a1a1"
/*FUTURE*/            // ok, bug fixed, but FUTURE: chosen e5f4 still not a good move, as it trapps B in 2 moves...:
            //"r2qkbnr/pb4pp/1p1pp3/2p1Bp2/1P6/P1N5/2PPPPPP/RQ2KB1R w KQkq - 0 9, e5g3" // do not let attacked B be simply taken
/*TODO*/    //"3r3k/6p1/3bQ1pp/4p3/4P3/4N2P/PP3rPq/R1BK2R1 w - - 7 35, g1f1|g1e1"  // Bug was e3g4, kinda solved, but d1e1 still bat as it is counter attacking instead of saving the attacked piece
            //OK,fixed: "r1bqkbnr/ppp2ppp/3p4/6B1/8/2N2P2/PPP2PPP/R2QKB1R b KQkq - 0 6, d8g5"
            //fixed: do not always move out with queen first...
                //"r1bqk2r/pppp1ppp/2nbpn2/8/3P4/P3PN2/1PP2PPP/RNBQKB1R w KQkq - 1 5, a1a1"
                //same position via moves: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 moves e2e3 b8c6 d2d4 e7e6 g1f3 g8f6 a2a3 f8d6, a1a1"
//TODO!:XRay-Bug:            "r4rk1/1p3pp1/p1ppbnq1/4b3/2Q1P1P1/P1N1BB2/R1P5/4K1R1 w - - 2 29, a1a1"
            //"4k2r/pp3ppp/8/3n4/P2r4/5P2/1P3P1P/R1B1R1K1 b k - 3 23, a1a1" // cover king instead of moving away: brought up relEval problem at king position. solved be 0.29z2,z3 and z4 (but z2+z3 not used, because slightly worse overall)
//TODO: to much blocking benefit in too close future level: "Benefit 88@0 for blocking-move by vPce(15=weißer Turm) on [c5]"
                // "r3r3/1ppk1p1p/6p1/p5P1/8/4n1N1/1P3P1P/2R1K2R w K - 0 26, a1a1"
            //"r1b1kb1r/pp1ppppp/5n2/q1p5/3n4/N5P1/PPPPPPBP/R1B1QKNR b kq - 9 6, a1a1"  // NOT giving away knight with d4e2
            // ? "5r2/6k1/1p1N2P1/p3n3/2P4p/1P2P3/P5RK/8 w - - 5 45, a1a1"
            // only partly improved: defend vs. attack and loose: "r3kb1r/p2npppp/8/2pp4/5N2/3P4/P2B1PPP/1RR3K1 b kq - 3 16, a1a1"
/*TODO!!!*/  //          "8/8/8/1q6/8/K3k3/8/7q b - - 0 1, h1a1|h1a8"
            //"r1bq1rk1/pppn1pbp/3p2p1/8/2PQP1n1/2N2NP1/PP3PBP/R1B2RK1 w - - 1 10, a1a1"
            //? "1r4k1/1p1r1p1p/pBn1bbp1/4p3/2P1P3/5P2/PP4PP/R1N1KB1R w KQ - 2 17, c1b3|b6e3"
            // improved, but still tightly chosing wrong king move: "r4rk1/1pp2ppp/p2pbn2/5B2/2P2P2/1PN2nP1/PB1P3P/R3K2R w KQ - 0 19, e1f2"
            //DEL "k4r2/1pB4p/5pp1/Pb6/3P3P/8/5PP1/2R1R1K1 b - - 0 29, a1a1" // was no move / out of time -> but seems to work
            //"r1bqk2r/ppppbppp/2n2n2/4p1N1/2B1P3/3P4/PPP2PPP/RNBQ1RK1 b kq - 4 6, a1a1"
            // "r1b1k2r/p4p1p/1npp2p1/1p1p1BP1/1P1P4/2P2P2/P1N1P2P/2R1K1NR b Kkq - 0 18, a1a1"
       // not any more? "r1b2rk1/1ppp1ppp/p7/2b1n3/4n3/P1P1P1PP/2P1B1P1/R1B1K1NR w KQ - 1 13,a1a1" // NOT e2a6 - kill own L
       // Future: strange moving away / checking / non-real-contribution        //"r1b3nr/pp3ppp/3N1k2/2p2N2/1n6/8/PPP1PPPP/R3KB1R w KQ - 4 13, f5e3" // not good: d6e4
            // TODO pawns: "r1b2rk1/ppppbpp1/2n1p3/4P2p/3P1P2/3q4/PPP1BN2/RNB1K2R w Q - 0 14, e2d3|f2d3"  // take with b, not pawn
            // f. TODO move decision, take back calculation: "r1b2r2/p1p2pk1/2n1p1p1/Pp2P2p/2N1PP2/P7/8/R1BBK2R w Q b6 0 23, a1a1"  // c1h5
        // ? "2kr3r/p1p1pp1p/1pn1q1p1/6P1/P2P4/2n1P2P/1P4P1/R1BQR1K1 w - - 0 20, a1a1"
        // done? "r1bqkbnr/pppp1ppp/n7/8/3PB3/8/PPP1QPPP/RNB1K1NR  b KQkq - 6 6, a1a1" // Abzugschach
        // "r4r1k/1ppb3p/4pp1R/p3n3/4q3/P3B3/2P2PP1/R2QKB2 w Q - 2 21, a1a1"  // fut.do not test nr 4
        // "r1bq1rk1/ppp2ppp/2np4/3NP3/P7/8/1PP1K1PP/R1BQ1B1R w - - 0 12, a1a1"  // attack queen behind king
        // "4r3/p4p1k/8/2P5/p7/Pb1NP2P/1K1b4/6R1 b - - 0 31, e8e3|d2e3" // Just take it (back): from  https://lichess.org/HdTf7W6w/black#61
        // solved  "2k4r/pp3p1p/4pp1b/8/4NP2/8/PPP3PP/2K4R b - - 0 18, h6f4" // Just take it (back)
        // not fully solved:
        // ok: "2k2b1r/Bp3ppp/p1N5/3N1b2/8/6P1/PP2PP1P/2KR3R b - - 0 20, b7c6"  // just take back, from https://lichess.org/fzgVKvgY/black#39
        // ok: "8/8/5k2/3np3/6p1/2pK4/2p5/8 w - - 0 63, d3c2"  // just take back, from https://lichess.org/dpGDKlmk/white#124
        // ok: "5k2/p4p2/7P/1P2pK1P/P4b2/2P5/8/8 b - - 0 44, f4h6|f8g8"  // just take it + do not run away from covering promotion, from https://lichess.org/baqG7cnk/black#87
        // ok: "r1b1k2r/pppp1ppp/6n1/6B1/3Pq3/P1P3Q1/3K1PP1/R4B1R b kq - 1 15, a1a1" // NOT d7d6
/*problematic king endgame behaviour: from game https://lichess.org/ZiFKfoP5/black#136
            //"8/5K2/p7/2k5/2P4P/6P1/P7/8 b - - 0 69, c5c4"
            // "5K2/8/p7/2P5/7P/k5P1/P7/8 b - - 0 71, a3a2"
 */
            //Abw. von online-Zug: "r2k2nr/p1p2ppp/4p3/8/Q1P5/4bPP1/PR1N2qP/2BK3R w - - 4 16, a1a1"
            //Abw. von online-Zug: "r2k2nr/p1p2ppp/4p3/8/Q1P5/4bPP1/PR1N1q1P/2BK3R b - - 3 15 moves f2g2, a1a1"
        // ok, bug in Abzugsschach fixed, prefered c4c5, because it thought this was an Abzugschach... "r2k1bnr/p1p2ppp/4p3/8/Q1p3q1/1P3PP1/P2N1P1P/R1B1K2R b KQ - 0 11 moves g4d4 a1b1 f8c5 b3c4 d4f2 e1d1 c5e3 b1b2 f2g2, h1e1"
            // same position, but never had the error, just because different 1 condition was stored, that avoided the bug: "r2k1bnr/p1p2ppp/4p3/8/Q1pq4/1P3PP1/P2N1P1P/R1B1K2R w KQ - 1 12 moves a1b1 f8c5 b3c4 d4f2 e1d1 c5e3 b1b2 f2g2, a1a1"
/*todo*/ //    "8/k3pR2/8/2N5/5P2/2n1r3/2K5/8 b - - 0 56, a1a1"  // not c3e4
          //?  "rnb2rk1/pp3ppp/3qpn2/1NppN3/3P4/3Q4/PPP1PPPP/2KR1B1R b - - 1 9, a1a1"
        // "3r2k1/pp3ppp/2n5/2b2q2/8/P3PNP1/1PP2P1P/R1BQ1RK1 w - - 0 15, a1a1"
         // ok: pawn promotion, when to far away "8/8/7k/8/6Bp/7P/PP2K1P1/8 w - - 34 66, a1a1" // go pawns + why 3fold-rep?, from https://lichess.org/g1JwF395#117
         //   "r3kb1r/ppp2ppp/2q5/3pP3/1n1P4/PB2PQ2/1P1K2PP/RN5R b kq - 0 15, a1a1"
            //"1rbq1rk1/1pp2pbp/p1np1np1/3Pp3/2P1P3/2N1BP2/PP1Q2PP/R1N1KB1R b KQ - 0 10, a1a1"
            //"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR  w KQkq - 0 1, a1a1"
            //"rnb1kb1r/pp1p1ppp/2p1p3/q3P3/2PPnB2/8/PP1N1PPP/R2QKBNR w - - 0 4, a1a1"
            // solved inconsistencies: "rnbqkb1r/pp1p1ppp/2p1p3/4P3/2PPnB2/5N2/PP3PPP/R2QKBNR w - - 0 4 moves f3d2 d8a5, a1a1"
        // "1rbq4/p1p1bk2/1p3n1p/6p1/3P4/2N1P1B1/PP3PPP/R2QK2R  w KQ - 0 16, a1a1"
    // enable backrank mate
       //ok "3r2r1/2p5/1p2k2p/1R2n3/p7/3BP3/2P2PPP/6K1 w - - 2 33, d3e2|h2h3|g2g3|f2f4|b5b1"   // do not: move B out of the way and enable backrank mate
        // ok, but should be better  "7r/3k2pp/1p3n2/p7/3N4/bP1rB3/5PPP/R5KR w - - 0 34, a1a1" // leave behind defence of backrank mate
        /*TODO!*/ //
        //    "1r5k/p2P1p2/3b3p/3R3P/3K2P1/8/8/8 b - - 6 64, d6e7|d6c7" // NOT b8b6, uncovering promotion square, from https://lichess.org/syojTOC4/black#127
    // unsolved bad move, knight-move of opponent is largely overrated:  "r1b1k2r/pppp1ppp/2nb1n2/2q5/2P1p1P1/2N3RN/PP1PPP1P/R1BQKB2 w Qkq - 5 9, a1a1" // https://lichess.org/VkKp3byJ#16
        // ok: "r6r/1k3p2/4p1p1/p7/P1p1bP2/2P1P3/3K1R1P/R7 w - - 0 39, a1a1" // NOT f2f1, from  https://lichess.org/tIlSPag2#76
        // ok: "6k1/p5pp/2N5/5b2/3p4/4r3/K7/8 b - - 3 49, d4d3|e3e4" // Do not enable fork with, from https://lichess.org/CBsJsaod/black#97
    // ok:simple mate:       "8/8/8/1q6/8/K3k3/8/7q b - - 0 1, h1a1|h1a8"
//            "r1rq2k1/p2n1pBp/3Q2p1/8/2P2p2/R2BP2P/1P4P1/5RK1 b - - 0 23, g8g7"  // just take l back
    // almost mate-in-1, luft or move a away inbetweener for coverer
        //"1rn3k1/p4ppp/2p1p3/P7/1PK5/6P1/4PP1P/3R4  b - - 0 24, g8f8" // cover mating square by moving away
//      "1r4k1/p4ppp/2p1p3/P7/1PK5/6P1/4PP1P/3R4  b - - 0 24, g8f8" // do not b8b5 testcase from below + check bug, why d1d8 is not the primary move for R towards k, but one longer (also with NoGo) via g7.
    //"2r5/2p1nkpp/b3q3/1NPp4/1P1P1p2/5P2/4P1BP/3QK2R w K - 3 28, b5c3" //NOT b5a7 - do not go to a trap with N - but hard to see, N still seems to have an exit via xr on c8.
        //"3r3r/ppp1kpp1/8/4Pb1p/1n2NP2/4R2P/PP2P1P1/4KB1R w K - 3 17, e1f2|e4c3"
    //TODO!! moveBUG: ok with "8/n2k4/3np3/2p5/6P1/1Pp1b3/PB2B3/5K2 w - - 0 55, b2c3"  // simply take back, (NOT b2a3) as in https://lichess.org/lZUkqkuN#108
    // but not after "position fen 8/3k4/3np3/1np5/1p4P1/1P2b3/PBP1B3/5K2 b - - 5 53 moves b5a7 c2c3 b4c3, b2c3"
    //ok "r1bqk2r/ppp1bppp/n3pn2/1N1p4/Q1PP1B2/P7/1P2PPPP/R3KBNR  w KQkq - 4 7, b5c7"        // Double-Check: vPce(15=N) on [c7] should be realChecker, but on a7 it should be not. Bug is: both are not... fixed with 0.48h11
    //ok "5bk1/R4pp1/6p1/3p4/3Pn3/1Q5P/5PPB/2r1N1K1  b - - 2 37, c1e1" // mateIn1 by taking
    //ok"2k5/1p3r1p/p1p3p1/2n1pp2/P1P5/3Q3P/4BqP1/3R3K  w - - 2 34, d3d8"  // mateIn1 Nr 25
    //ok "2k2r2/1p1r2Bp/p7/n5p1/2bPB1Q1/P7/1P4PP/2R1N1KR  b - - 5 32, f8f1"  // mateIn1 covered by a king-pinned-piece...
    //ok "5r1k/p1p3pp/b5r1/2p5/2P1p1q1/1PB5/P2P2PP/R4RK1 w - - 0 27, f1f8"  // simple? mateIn1
    //ok"r1b2rk1/1ppq1pp1/p1np4/4p1P1/PPB1P1n1/1QPP4/6P1/1NB1KR1R b K - 2 14, a1a1" // was castelling bug: found wrong rook first.
    //ok "7k/b1p3rp/p1p5/3p4/1PP2R2/P1B1N3/3Pq3/7K  w - - 0 34, f4f8" // from puzzle, mate because opponents r is king-pinned
    //ok"rnbqkbn1/pp4p1/3pp3/2p2pNr/4NQ2/3P4/PPP1PPPP/R3KB1R b KQq - 1 8, e6e5|d8e7|d6d5|g8h6|b8c6" // NOT f5e4, taking the N gives way to be mated in 1
    //ok "4k3/3q4/2pBp3/2Pp2rp/1P6/P6P/5QP1/5K2  w - - 0 33, f2f8" //simple mateIn1
//TODO!! works in direct FEN but not by reaching with a move!
//   "2r3k1/pQ2Bppp/4p3/2P5/8/P1P1P3/5qPP/4R2K b - - 0 27, f2e1" // simple mateIn1 by taking protective pce
// ok, "2r3k1/pQ2Bppp/4p3/2P5/8/P1P1P3/5qPP/2R1r2K moves c1e1 b - - 0 27, f2e1" // simple mateIn1 by taking protective pce
    //ok "2k5/pp3bp1/1Rp2p2/3p3p/3r3P/P4P2/1QPq2PB/4R1K1  b - - 0 28, d2e1" // mateIn1 with Queen
    //ok "r3qrk1/p1p2p1p/1pN5/6b1/6Q1/1n6/PBPP1PPP/6K1 w - - 2 22, g4g5"  // mateIn1 by taking + 2nd piece
//TODO!! works in direct FEN but not by reaching with a move!
//   "8/2k5/1Rq4p/7P/8/4PPP1/1R4K1/8 b - - 0 48 moves c6b6 b2b6, c7b6"
//   ok,"8/2k5/1R5p/7P/8/4PPP1/6K1/8 b - - 0 49, c7b6" // just take rook (back)!, https://lichess.org/WhNd31xq/black#97
//TODO: to much for pin possibility Re1-n36-ke8, because it needs to check for blockers (similar to check-blocking)
//  "4kb1r/1pp2pp1/4nB1p/p1P4Q/1P3P2/P2P3N/7P/R4KNq b k - 0 24, g7f6"  // just take L back, https://lichess.org/ZKmNz9bR/black#47
    //ok "r1b4k/1p3n1p/3B1q2/P2Q1p2/3N4/1P2P3/3P1K2/6R1 b - - 0 31, h7h6|c8e6|h7h5"  // NOT f7d6 which enables mateIn1 Qd5g8
    // not solved, made it actually worse: "1rbq1rk1/1pp2pbp/p2p1np1/4n3/2P1P3/2N1BP2/PP1Q2PP/R1N1KB1R w KQ - 0 11, c1b3"  // was c4a4, became c1d3 wich uncovers the p c4
//TODO "1rb2rk1/1ppq1pbp/p1np2p1/7n/N1P1P3/2Q1BP2/PP4PP/R1N1KB1R w KQ - 6 14, c3d2|c3c2"  // NOT e4e5, but move Q away
   //"1rbq1rk1/1pp2pbp/p2p1np1/8/4P3/2NNnP2/PP2Q1PP/R3KB1R w KQ - 0 13, e2e3"
    //ok "r1bq1rk1/pp2npbp/5np1/3Pp3/1p2N3/2N2B2/P4PPP/R1BQR1K1 b - - 1 15, b4c3"  // just take the N!
    // TODO: "r1b1qrk1/5pbp/p1p2np1/1p2N3/4N3/4QP2/PP4PP/3RKB1R w K b6 0 18, e8e5" // just take the n again
    // TODO: "7k/5p1p/r2p3P/8/2PnPK2/5n2/PR6/8 b - - 3 61, a6a8"  // NOT d4b5 , blocking mate with the wrong piece
    // TODO: "1n3q1r/r2pk2p/b2NpBp1/2pn4/Q3N3/8/PP2PPPP/R3KB1R b KQ - 0 15, d5f6"  // NOT qf8f6 - why taking with the wrong piece + loosing q?
    //FUTURE?TODO: "r1b5/1p2k2p/p2ppb2/B6p/P3PP2/2N3P1/1PPR3P/4KR2 w - - 0 30, NOT b2b4" // tricky - takes away 2 defenders at once and looses the Nc3 and more...
//    "1r1qr1k1/2p1b2p/p1b2p2/1p1n1QpR/3P4/1B4NP/PP3PP1/R1B3K1 b - - 1 20, e7d6|a6a5"  // NOT e8f8 which makes it mateIn1
    //ok  "4k2r/ppp4p/4b3/2b1P3/6p1/2P1P1P1/P6P/R1r2RK1 w k - 0 27, a1a1"  // if ok, it should not show the error message "no king move found"
    //"1r1qr1k1/2p1b2p/p1b2p2/1p1n1QpR/3P4/1B4NP/PP3PP1/R1B3K1 b - - 1 20, a1a1"
//out of bounds bug:
    //"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 moves c2c4 g8f6 e2e3 g7g6 g1f3 f8g7 h2h3 c7c5 f1e2 e8g8 b1c3 b8c6 d2d4 c5d4 e3d4 d7d5 e1g1 d5c4 e2c4 c6a5 c4d3 a5c6 d3b5 a7a6 b5c4 b7b5 c4e2 b5b4 c3a4 a6a5 a4c5 f6d5 c5d3 c6d4 f3e5 d4e2 d1e2 c8a6 e2f3 a6d3 f3f7 f8f7, e5d3"  // java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 64	at de.ensel.tideeval.ChessBoard.basicMoveFromTo(ChessBoard.java:2067) https://lichess.org/No2rTuyg#40
    //"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 moves c2c4 g8f6 e2e3 g7g6 g1f3 f8g7 h2h3 c7c5 f1e2 e8g8 b1c3 b8c6 d2d4 c5d4 e3d4 d7d5 e1g1 d5c4 e2c4 c6a5 c4d3 a5c6 d3b5 a7a6 b5c4 b7b5 c4e2 b5b4 c3a4 a6a5 a4c5 f6d5 c5d3 c6d4 f3e5 d4e2 d1e2 c8a6 e2f3 a6d3, e5d3"  // NOT f3f7 from game https://lichess.org/No2rTuyg#40
    //"r2q1rk1/4ppbp/6p1/p2nN3/1p6/3b1Q1P/PP3PP1/R1B2RK1 w - - 0 21, e5d3"  // NOT f3f7 from game https://lichess.org/No2rTuyg#40
            // also here: position startpos moves e2e3 e7e5 b1c3 d7d5 f1b5 c7c6 b5d3 g8f6 g1f3 e5e4 d3f1 e4f3 d1f3 c8g4 f3f4 d8b6 f4e5 g4e6 f1d3 b8a6 e1g1 a6c5 d3e2 b6b4 d2d4 c5a4 e3e4 a4c3 b2c3 b4c3 c1g5 c3a1 f1a1
//todo still: "r3kb1r/ppqn1ppp/2p5/4n3/3BQ3/4P3/P1PP1PPP/1R3RK1 b kq - 5 20   , f8e7|c6c5|e8d8|e8c8"  // need to detox pin
    //hmm "rnbq1rk1/p1p1bp2/1p2pB2/4N2p/2pP4/2N1P3/PP3PP1/R2QKB1R b KQ - 0 12, e7f6" // take back B - or prevent r from trap? - anyway mate in 9 even after e7f6
    //not yet detecting avoiding trap:  "rnbq1rk1/p1p1bp2/1p2pn2/3pN2p/2PP3B/2N1P3/PP3PP1/R2QKB1R b KQ - 0 11, a1a1"  // NOT d5c4, position used to avoid trap of ra8 by moving pd5 out of the way
    //ok "rqb1k1nr/1p1p1ppp/pQ1b4/3Np1B1/4P3/8/PPP1BPPP/R4RK1 b kq - 5 11, f7f6"  // NOT d6c7 - it avoids mateIn1 but still leads to mate later -> needs new feature of blocking of other opponents piece covering the mating square
    //"2r3k1/ppb3p1/3q3p/8/2Pp2Q1/3P4/PP3P1P/R1B2RK1 b - - 0 23, d6h2" // clear mate in 1
    // FUTURE going into trap: "1rbq1rk1/1pp2pbp/p1np1np1/3Pp3/2P1P3/2N1BP2/PP1Q2PP/R1N1KB1R b KQ - 0 10, c6e7|c6a7" // NOT c6b4 whre it is trapped, difficult via mobility, as all other (safe) squares have also no mobility
    // FUTURE: "r1b2r2/p2p2kp/3b2n1/1p1Pp3/2p1P1Q1/2P1BP1P/Pq6/RN2KBNR w KQ - 1 18, h3h4"  //NOT e3h6, giving away B because R cannot be saved? reason is that check seems to hinder qb2xRa1, but actually is only postponing it for one move.
    // FUTURE: "r1b2r2/p2p3p/3b2nk/1p1Pp3/2p1P1Q1/2P2P1P/Pq6/RN2KBNR w KQ - 0 19, h3h4"  // similar, NOT f1c4 giving away B because R cannot be saved
//    "r1b2r2/p2p3p/3b2nk/3Pp3/2p1P1Q1/2P2P1P/P3N3/1q2KR2 w - - 0 22, e1f2" // NOT e2c1
    //ok: "1rbq1rk1/1pp2pbp/p2p1np1/3Pp3/1nP1P3/P1N1BP2/1P1Q2PP/R1N1KB1R b KQ - 0 11, b4d5|b4c6|a6a5" // 1 n is as good as lost - why sac a 2nd n?
//TODO: "5b1r/rpk2p1p/p4p2/5b2/3R4/P4P2/1PP3PP/4KBNR b K - 0 16, f8c5|h7h5|f8g7"  // NOT bf5xf2 which runs into a fork d4c4+
//ok "4r1r1/ppk1np1p/2n3p1/q2QB3/P3P3/2p3PB/1bP1PP1P/1R2K2R b - - 1 25, c7b6"  // save king by moving out of check - NOT take B with n and loose q
    "3rr1k1/1p3p1p/p1n2pP1/2B5/8/1N4R1/PPP3PP/5RK1 w - - 0 27, a1a1"
    })
    void DEBUG_ChessBoardGetBestMove_isBestMove_Test(String fen, String expectedBestMove) {
        doAndTestPuzzle(fen,expectedBestMove, "Simple Test", true);
    }


    // from games against SF0ply
    @ParameterizedTest
    @CsvSource({
        "4r1k1/2p2p1p/pp2bbp1/4p3/2PrP3/1P1N1P2/P2K2PP/2R2B1R w - - 0 20 , g2g4"  // bug from v0.46r
        , "r1b1k2r/pppp1ppp/6n1/6B1/3Pq3/P1P3Q1/3K1PP1/R4B1R b kq - 1 15, d7d6"  // not d7d6, which allows q-k-pin
    })
    void DEBUG_ChessBoardGetBestMove_SF0ply_doNot_Test(String fen, String notExpectedBestMove) {
        //doAndTestPuzzle(fen,expectedBestMove, "Simple  Test", true);
        ChessBoard.DEBUGMSG_MOVEEVAL = true;
        ChessBoard.DEBUGMSG_MOVESELECTION = true;
        ChessBoard board = new ChessBoard("CBGBM", fen);
        Move bestMove = board.getBestMove();
        String notExpectedMoveString = (new Move(notExpectedBestMove)).toString();
        System.out.println("" + board.getBoardName() + ": " + board.getBoardFEN() + " -> " + bestMove + " (should not be " + notExpectedMoveString+")");
        assertNotEquals( notExpectedMoveString, bestMove.toString() );
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;

    }

    // solved - not a real test case: this test is "positive", if the unwanted 3-fold-repetition happens...
    void ChessBoardGetBestMove_Avoid3foldRepetition_Test() {
        String blackMove1 = "b8a7";
        String blackMove2 = "a7b8";
        String whiteMove1 = "d8a5";
        String whiteMove2 = "a5d8";
        String fen = "1k1Q4/1p5p/2p3r1/8/P2P4/4B1P1/4P1PP/R2K3B b - - 5 30 moves " + blackMove1;

        doAndTestPuzzle(fen,whiteMove1, "3fold-rep-0", false);
        fen += " " + whiteMove1 + " " + blackMove2;
        doAndTestPuzzle(fen,whiteMove2, "3fold-rep-0b", false);
        fen += " " + whiteMove2 + " " + blackMove1;
        doAndTestPuzzle(fen,whiteMove1, "3fold-rep-1", true);
        fen += " " + whiteMove1 + " " + blackMove2;
        doAndTestPuzzle(fen, whiteMove2, "3fold-rep-1b", false);
        fen += " " + whiteMove2 + " " + blackMove1;
        doAndTestPuzzle(fen, whiteMove1, "3fold-rep-2", false);
        fen += " " + whiteMove1 + " " + blackMove2;
        doAndTestPuzzle(fen, whiteMove2, "3fold-rep-2b", false);
        fen += " " + whiteMove2 + " " + blackMove1;
        doAndTestPuzzle(fen, whiteMove1, "3fold-rep-3", true);
    }


    // solved - not a real test case: this test is "positive", if the unwanted 3-fold-repetition happens...
    void ChessBoardGetBestMove_Avoid3foldRepetition2_Test() {
        String blackMove1 = "d8d1";
        String blackMove2 = "d1d8";
        String whiteMove1 = "e3e2";
        String whiteMove2 = "e2e3";
        String fen = "3r4/5p2/2p4p/p1p1kb2/1n6/1P6/5K2/8 w - - 0 45 moves f2e3";

        doAndTestPuzzle(fen,blackMove1, "3fold-rep-0", false);
        fen += " " + blackMove1 + " " + whiteMove1;
        doAndTestPuzzle(fen,blackMove2, "3fold-rep-0b", false);
        fen += " " + blackMove2 + " " + whiteMove2;
        doAndTestPuzzle(fen,blackMove1, "3fold-rep-1", false);
        fen += " " + blackMove1 + " " + whiteMove1;
        doAndTestPuzzle(fen,blackMove2, "3fold-rep-1b", false);
        fen += " " + blackMove2 + " " + whiteMove2;
        doAndTestPuzzle(fen,blackMove1, "3fold-rep-2", false);
        fen += " " + blackMove1 + " " + whiteMove1;
        doAndTestPuzzle(fen,blackMove2, "3fold-rep-2b", false);
        fen += " " + blackMove2 + " " + whiteMove2;
    }



// choose the one best move
    @ParameterizedTest
    @CsvSource({
            // bug was illegal move d2e2 -> but reproducable with fen string, but with one moves sequence:
            "r1b2rk1/pp3ppp/n2p1n2/3N1N2/2P3P1/4PQ2/Pq1K3P/R4B1R w - - 0 15, d2e1"
            // square e2 does not update from 1 NoGo&if{e3-any (weiß)} to 1 NoGo&if{d2-any (weiß)}
            , "r1b2rk1/pp3ppp/n2p1n2/3NqN2/2P3P1/4PQ2/PP1K3P/R4B1R b - - 0 14 moves e5b2, d2e1"
    })
    void TMP_ChessBoard_doMoveDistUpdate_Test(String fen, String expectedBestMove) {
        doAndTestPuzzle(fen,expectedBestMove, "Simple  Test", true);
    }


//    Test cases for treating hanging pieces behind king,
//            8/6kp/1p4p1/2p4P/P1P5/r4K2/8/8  b - - 1 46
//            8/6kp/1p4p1/2p4P/P1P5/r4K2/8/8  w - - 1 47
//            5rk1/p2p1ppp/b7/b3P3/3P2P1/p7/P1K4P/1rR5  b - - 0 32
//            1n2k2r/6pp/2pbp3/1p1p4/3P4/2PB1P2/3K3P/r6q  w k - 1 25
//            1n2k2r/6pp/2pbp3/1p1p4/3P4/2PB1P2/3K3P/r2q4  w k - 10 30
//            3r4/6k1/8/p4P1B/P2pq1PP/1PpR4/2P2B2/R4K2  b - - 1 63


    @ParameterizedTest
    @CsvSource({
            // bug was bad move after this position, which was not reproducable with direct fen string, but with one moves sequence:
            "rnb1k2r/p1pp1p1p/8/1Q6/P3Pb2/8/1PP1NPp1/RN2KB2 b Qkq - 1 16 moves g2f1 e1f1 a7a6, b5d5"
            , "rnb1k2r/p1pp1p1p/8/1Q6/P3Pb2/8/1PP1NP2/RN2Kq2 w Qkq - 0 17 moves e1f1 a7a6, b5d5"
            , "rnb1k2r/p1pp1p1p/8/1Q6/P3Pb2/8/1PP1NP2/RN3K2 b kq - 0 17 moves a7a6, b5d5"
            // now - after bug was fixed (always resetting clashevals) - the obove 3 lead to the best move, but the direct fen string leads to variations.
            // TODO!!!: other BIG BUG: dist of Q on a8 is wrong here:
            , "rnb1k2r/2pp1p1p/p7/1Q6/P3Pb2/8/1PP1NP2/RN3K2 w kq - 0 18, b5d5"
    })
    void TMP_chessBoard_doMove_UpdateBug_Test(String fen, String expectedBestMove) {
        doAndTestPuzzle(fen,expectedBestMove, "Simple  Test", true);
    }


    @Test
    void ChessBoard_PawnRelEval_Test() {
        // check if covering a pawn (on c4) that is heavily attacked by opponent (black) light pieces
        // would be correctly covered (at least positively considered) by its neighbour pawn d2
        // step 1:  // check relEval, if pawn can move to d3
        ChessBoard board = new ChessBoard("relEvalTest",
                "r1bqkb1r/p1pppppp/1p1n4/4n3/2P4P/P5PN/1P1PPPB1/RNBQK2R  b KQkq - 1 7");
        int pW1Pos = coordinateString2Pos("d2");
        int testPos = coordinateString2Pos("d3");
        int pW1Id = board.getPieceIdAt(pW1Pos);

        boolean dbgME = DEBUGMSG_MOVEEVAL;
        boolean dbgMS = DEBUGMSG_MOVESELECTION;
        DEBUGMSG_MOVEEVAL = true;
        DEBUGMSG_MOVESELECTION = true;
        DEBUGFOCUS_SQ = testPos;
        DEBUGFOCUS_VP = pW1Id;
        board.completeCalc();

        int pW1TestRelEval = board.getBoardSquare(testPos).getvPiece(pW1Id).getRelEval();
        debugPrintln(DEBUGMSG_MOVEEVAL, "Test result: " + board.getPiece(pW1Id) + " relEval at "
            + squareName(testPos) + "=" + pW1TestRelEval + ".");
        assert( abs(pW1TestRelEval) < EVAL_TENTH );

        DEBUGMSG_MOVEEVAL = dbgME;
        DEBUGMSG_MOVESELECTION = dbgMS;
    }

    @Test
    void ChessBoard_PawnClashCalc_Test() {
        // check if covering a pawn (on c4) that is heavily attacked by opponent (black) light pieces
        // would be correctly covered (at least positively considered) by its neighbour pawn d2
        // Step 2: check if d2-pawn is considered to cover c4 by d2d3
        int pW1Pos = coordinateString2Pos("d2");
        int testPos = coordinateString2Pos("c4");
        int pW1Id = 22; //board.getPieceIdAt(pW1Pos);

        boolean dbgME = DEBUGMSG_MOVEEVAL;
        boolean dbgMS = DEBUGMSG_MOVESELECTION;
        DEBUGMSG_MOVEEVAL = true;
        DEBUGMSG_MOVESELECTION = true;
        DEBUGFOCUS_SQ = testPos;
        DEBUGFOCUS_VP = pW1Id;

        ChessBoard board = new ChessBoard("relEvalTest",
                "r1bqkb1r/pppppppp/3n4/4n3/2P4P/P5PN/1P1PPP2/RNBQKB1R  w KQk - 4 9");
        board.completeCalc();

        int pW1TestRelEval = board.getBoardSquare(testPos).getvPiece(pW1Id).getRelEval();
        debugPrintln(DEBUGMSG_MOVEEVAL, "- intermediate result: " + board.getPiece(pW1Id) + " relEval at "
            + squareName(testPos) + "=" + pW1TestRelEval + ".");
        assertTrue( pW1TestRelEval < -EVAL_HALFAPAWN ); /// should be -100

        Move m = board.getBestMove();
        debugPrintln(DEBUGMSG_MOVEEVAL, "Move result: " + m + ".");
        assertTrue( m.toString().matches("b2b3|d2d3"));
        DEBUGMSG_MOVEEVAL = dbgME;
        DEBUGMSG_MOVESELECTION = dbgMS;
    }

    @Test
    void ChessBoard_QueenAdditionalAttack_Test() {
        int qWPos = coordinateString2Pos("d6");
        int testPos = coordinateString2Pos("f2");
        int qWId = 6; //board.getPieceIdAt(pW1Pos);

        boolean dbgME = DEBUGMSG_MOVEEVAL;
        boolean dbgMS = DEBUGMSG_MOVESELECTION;
        DEBUGMSG_MOVEEVAL = true;
        DEBUGMSG_MOVESELECTION = true;
        DEBUGFOCUS_SQ = testPos;
        DEBUGFOCUS_VP = qWId;

        ChessBoard board = new ChessBoard("relEvalTest",
                "2r3k1/ppb3p1/3q3p/8/2Pp2Q1/3P4/PP3P1P/R1B2RK1  b - - 0 23");
        board.completeCalc();

        int qWTestRelEval = board.getBoardSquare(testPos).getvPiece(qWId).getRelEval();
        debugPrintln(DEBUGMSG_MOVEEVAL, "- intermediate result: "
                + board.getPiece(qWId) + " relEval at "
                + squareName(testPos) + "=" + qWTestRelEval + ".");
        debugPrintln(DEBUGMSG_MOVEEVAL, ".");
        assertTrue( qWTestRelEval < -pieceBaseValue(ROOK) ); // losing the queen for little counter benefit

        // Todo: add the test we originally wanted ;-) is coming closer to f2 seen as a benefit
        // or is there still the bug, that it is accounted as +something (benefiting white)

        Move m = board.getBestMove();
        debugPrintln(DEBUGMSG_MOVEEVAL, "Move result: " + m + ".");
        // best move is actually checkmate!
        assertTrue( m.toString().matches("d6h2"));
        DEBUGMSG_MOVEEVAL = dbgME;
        DEBUGMSG_MOVESELECTION = dbgMS;
    }



    @Test
    void chessBoard_VirtualPieceOnSquare_getShortestPredecessors_Test1() {
        ChessBoard board = new ChessBoard("TestBoard", "r4rk1/1b1nbppp/1pq1pn2/p1p5/3P1B2/P1NQ1NP1/1P2PPBP/R2R2K1 w - - 4 16");
//Todo: Bug? currently Actual:[a4, e6]
        checkPredecessorsAndNeighboursOfTarget(board,
                "b7", "b3",
                "[a4, c4]",
                "[]");  // nothing, because it is out of reach. Only has a result for MAX...=7
    }

    @Test
    void chessBoard_VirtualPieceOnSquare_getShortestPredecessors_Test2() {
        /*ChessBoard board = new ChessBoard("TestBoard", "");
        int kingBPos = coordinateString2Pos("e8");
        int kingBId = board.getBoardSquare(kingBPos).getPieceID();
        int queenWPos = coordinateString2Pos("b3");
        int queenWId = board.getBoardSquare(queenWPos).getPieceID();
         */
        ChessBoard board = new ChessBoard("TestBoard", "rn1qkb1r/p1p2ppb/1p2pn1p/4N3/2pP2P1/1Q5P/PP1NPP2/R1B1KB1R w KQkq - 0 9");

        checkPredecessorsAndNeighboursOfTarget(board,
                "b3", "e8",
                "[a4, b5, e3]",  // unsorted former output was: [d8, h8, b5, e3, f7]
                "[a4, b5]");
    }

    private static void checkPredecessorsAndNeighboursOfTarget(ChessBoard board, String from, String to,
                                                               String predecessorNeighboursExpected ,
                                                               String shortestPredecessorsExpected ) {
        int targetPos = coordinateString2Pos(to);
        int pcePos = coordinateString2Pos(from);
        int pceId = board.getBoardSquare(pcePos).getPieceID();
        VirtualPieceOnSquare vPceAtTarget = board.getBoardSquare(targetPos).getvPiece(pceId);

        System.out.println("checking " + vPceAtTarget + ": " );

        Set<VirtualPieceOnSquare> predecessorNeighbours = vPceAtTarget.getPredecessors();
        String predecessorNeighboursActual = Arrays.toString(predecessorNeighbours
                .stream()
                .map(vPce -> squareName(vPce.myPos))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList()).toArray());

        System.out.println(" getPredecessor: " + predecessorNeighboursActual+".");

        Set<VirtualPieceOnSquare> shortestPredecessors = vPceAtTarget.getShortestReasonableUnconditionedPredecessors();
        String shortestPredecessorsActual = Arrays.toString(shortestPredecessors
                .stream()
                .map(vPce -> squareName(vPce.myPos))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList()).toArray());

        System.out.print(" getShortestReasonableUnconditionedPredecessors: " + shortestPredecessorsActual+".");

        if (predecessorNeighboursExpected!=null)
            assertEquals(predecessorNeighboursExpected, predecessorNeighboursActual );
        if (shortestPredecessorsExpected!=null)
            assertEquals(shortestPredecessorsExpected, shortestPredecessorsActual );
    }


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
        //assertEquals( pieceColorAndName(ROOK),       board.getPieceFullName(rookW1Id));
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
        //System.out.println("1 ---- ");
        debugPrintln(DEBUGMSG_TESTCASES, board.getBoardFEN() );
        board.completeCalc();
        //System.out.println("2 ---- ");
        int rookW2Id = board.getPieceIdAt(rookW2pos);
        int rookB1Id = board.getPieceIdAt(rookB1pos);
        assertEquals( pieceColorAndName(ROOK) + " on " + squareName(rookW2pos),
                board.getPieceFullName(rookW2Id));
        assertEquals( pieceColorAndName(ROOK_BLACK) + " on " + squareName(rookB1pos),
                board.getPieceFullName(rookB1Id));
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
        assertEquals( pieceColorAndName(BISHOP_BLACK) + " on " + squareName(bishopB1pos),board.getPieceFullName(bishopB1Id));
        assertEquals( pieceColorAndName(BISHOP_BLACK) + " on " + squareName(bishopB2pos),board.getPieceFullName(bishopB2Id));
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
        assertEquals( pieceColorAndName(KING) + " on " + squareName(kingWpos),board.getPieceFullName(kingWId));
        assertEquals( pieceColorAndName(KING_BLACK) + " on " + squareName(kingBpos),board.getPieceFullName(kingBId));
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
        assertEquals( pieceColorAndName(QUEEN) + " on " + squareName(queenWpos),board.getPieceFullName(queenWId));
        assertEquals( pieceColorAndName(QUEEN_BLACK) + " on " + squareName(queenBpos),board.getPieceFullName(queenBId));
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
        assertEquals( pieceColorAndName(KNIGHT) + " on " + squareName(knightWpos),board.getPieceFullName(knightWId));
        assertEquals( pieceColorAndName(KNIGHT_BLACK) + " on " + squareName(knightBpos),board.getPieceFullName(knightBId));
        // test distances to pieces stored at squares
        // dist from rookW1
        checkUnconditionalDistance( 2, board,/*b1*/ bishopB1pos,      rookW1Id);
        checkCondDistance( 2, board,/*1*/  bishopB2pos+UP,   rookW1Id);  // *chg*
        // dist from rookB = 3: rb4 + rf4{Nd4-any} + rf8
        checkCondDistance( 3, board,/*2*/  bishopB1pos+RIGHT,rookB1Id);  //  3, but only by way around and under the condition that the white knight moves away (otherwise nogo...)
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
        assertEquals( pieceColorAndName(PAWN) + " on " + squareName(pW1pos),board.getPieceFullName(pW1Id));
        assertEquals( pieceColorAndName(PAWN_BLACK) + " on " + squareName(pB1pos),board.getPieceFullName(pB1Id));
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
//TODO: what is the right result? recap which conditional moves are counted and what should checkUnconditional do...         checkUnconditionalDistance(3, board,/*.*/  bishopB2pos, pW1Id);  // but, it can beat a black piece diagonally left
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

        // without "killable=nogo" it is
        checkCondDistance(5, board,/*.*/  bishopB1pos, pW1Id);  // not straigt, but via beating others...
        // with "killable=nogo" it needs to be:  checkNoGoDistance(5, board,/*.*/  bishopB1pos, pW1Id);  // not straigt, but via beating others...

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
                        + board.getBoardSquare(pos).getvPiece(pceId).getPathDescription() );
        }
        assertEquals( expected, actual);
        assertTrue( board.getBoardSquare(pos).getvPiece(pceId).getRawMinDistanceFromPiece().isUnconditional() );
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
                    + "(expected: "+expected+")" + " via" + board.getBoardSquare(pos).getvPiece(pceId).getFirstUncondMovesToHere() );
            debugPrintln(true, "Board: " + board.getBoardFEN() );
            //if ( board.getBoardSquare(pos).getvPiece(pceId) instanceof VirtualOneHopPieceOnSquare )
            debugPrintln(true, "path to : "
                    + board.getBoardSquare(pos).getvPiece(pceId).getPathDescription() );
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
        ChessBoard board = new ChessBoard("MoveTest1 ", FENPOS_STARTPOS);
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

//UNCLEAR since killable-flag:
        //checkUnconditionalDistance(1, board, d5, queenBId);
        checkNogoDistance(1, board, d5, queenBId);

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
        ChessBoard board = new ChessBoard("MoveTest1 ", FENPOS_STARTPOS + " moves b1c3 d7d5");
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
        ChessBoard chessBoard = new ChessBoard("MoveTest 2 ", FENPOS_STARTPOS);
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
        boardEvaluation_SingleBoard_Test(chessBoard, -20, 170);
    }

    @Test
    void doMove_String_Test3() {
        // Test 3
        ChessBoard chessBoard = new ChessBoard("MoveTest 3", FENPOS_STARTPOS);
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
        // check king+rook position after castling
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

        // check king+rook position after castling
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
        ChessBoard chessBoard = new ChessBoard("MoveTest4 " , FENPOS_STARTPOS);
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
        ChessBoard board = new ChessBoard("MoveTest4 " , FENPOS_STARTPOS);
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
    void doMove_TwoSqPawnAfterSquareFreed_Test() {
        ChessBoard board = new ChessBoard("MoveTest4 " , "3r2k1/p1p2ppp/B4n2/2b5/P3pP2/3b4/1P1PK1PP/R1B2R2 w - - 2 18 moves a6d3");
        assertTrue( board.doMove("a7a5"));
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
        // before isKillable-flag and Cond instead of Nogo:
        // checkUnconditionalDistance(3,board,kingWpos,rookB1Id);  // 3 as it needs to avoid the covered b5 square
        // checkUnconditionalDistance(3,board,kingWpos,rookB1Id);  // 3 as it needs to avoid the covered b5 square
//TODO: Bug here: currently dist is 3 NoGo, why? althought it seens 3 ok-pathes: [([([(-ra5-a4(D1 ok))]-b4(D2 ok))]-b1(D3 ok)) OR ([([(-ra5-a3(D1 ok))]-b3(D2 ok))]-b1(D3 ok)) OR ([([(-ra5-d5(D1 ok))]-d1(D2 ok))]-b1(D3 ok))]
        checkCondDistance(3, board,kingWpos,rookB1Id);  // 3 by moving around covered square. FUTURE: 2 with the condition that the knight moves away...

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
        ChessBoard board = new ChessBoard("MoveTestExBug", FENPOS_STARTPOS);
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
        debugPrintln(DEBUGMSG_MOVEEVAL,"----- before move.");
        assertTrue(board.doMove("Ka1"));
        debugPrintln(DEBUGMSG_MOVEEVAL,"----- after move.");
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
        // covering the pawn and attacking the knight is the best move here.
        assertEquals( new Move(knightW1pos, coordinateString2Pos("c4")),board.getBestMove());
        assertTrue(board.doMove("Ka1"));
        kingWpos += LEFT;
        // compared to other test here is also a pawn to take, but knight tastes better
        assertEquals( new Move(rookB1pos, knightW1pos),board.getBestMove());

        /*
        8 ░░░   ░░░   ░░░   ░░░
        7    ░░░   ░░░   ░░░   ░░░
        6 ░░░   ░░░   ░░░   ░░░
        5  t ░░░   +R░ P ░░░   ░░░
        4 ░░░   ░░░   ░░░   ░░░
        3  N ░░░   ░░░   ░░░   ░░░
        2 ░░░   ░░░   ░░░   ░░░
        1  K<░░░   ░░░   ░░░   ░░░
           a  b  c  d  e  f  g  h    */

        // but now add even tastier rook to take
        int rookW1pos   = coordinateString2Pos("d5");
        int rookW1Id = board.spawnPieceAt(ROOK, rookW1pos);
        //TODO!!!:Find Bug here, spawning is not considerd correctly, r moves through R:
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
        System.out.println("move: "+m);
        assertTrue( m.from()==kingWpos );
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
        // expect N to NOT take p (and then loose R), but to stay and get l for R
        assertEquals( new Move("a2-a4" /*or a2a4*/), board.getBestMove());
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
            //simple mateIn1s
            "8/8/2r2Q2/2k5/4K3/8/5b2/8 w - - 0 1, f6f2"
            , "8/8/8/1q6/8/K3k3/8/7q b - - 0 1, h1a1|h1a8"
            //mate with queen
            , "3rk2r/2K1pp1p/3p1n2/1q5p/3n4/p7/1b4b1/8 b k - 17 43, b5b3|f6d5|d4e6|b5b8|b5b7"  // TODO! problem: queen typically has several lastMoveOrigin()s, but only one is stored, for now.  so mate-detector misses some
            , "r1b1k3/pp2bp2/2p5/4R1r1/2BQ4/2N3pP/PPP3P1/2KR4 w q - 1 2, d4d8" //  up to now, it does not notice that b defending mate on e7 is kin-pinned! https://lichess.org/3h9pxw0G/black#49
            // mateIn1 - but with tricks
            , "r7/5ppp/3Qbk2/3P4/4P3/2PB1NK1/PP4Pn/R6R w - - 1 27, e4e5"  // harder to see, as moving away p sets bishop free to block the rest of the kings squares - was d6f8 which blundered queen heavily
            , "5bk1/R4pp1/6p1/3p4/3Pn3/1Q5P/5PPB/2r1N1K1  b - - 2 37, c1e1" // mateIn1 by taking
            , "2k5/1p3r1p/p1p3p1/2n1pp2/P1P5/3Q3P/4BqP1/3R3K  w - - 2 34, d3d8"  // mateIn1 Nr 25
            , "2k2r2/1p1r2Bp/p7/n5p1/2bPB1Q1/P7/1P4PP/2R1N1KR  b - - 5 32, f8f1"  // mateIn1 covered by a king-pinned-piece...
            , "5r1k/p1p3pp/b5r1/2p5/2P1p1q1/1PB5/P2P2PP/R4RK1 w - - 0 27, f1f8"  // simple, but but while mate threat of opponent - mateIn1
            , "7k/b1p3rp/p1p5/3p4/1PP2R2/P1B1N3/3Pq3/7K  w - - 0 34, f4f8" // from puzzle, mate because opponents r is king-pinned
            // avoid mateIn1
            , "rnbqkbn1/pp4p1/3pp3/2p2pNr/4NQ2/3P4/PPP1PPPP/R3KB1R b KQq - 1 8, e6e5|d8e7|d6d5|g8h6|b8c6" // NOT f5e4, taking the N gives way to be mated in 1
            , "r1b4k/1p3n1p/3B1q2/P2Q1p2/3N4/1P2P3/3P1K2/6R1 b - - 0 31, h7h6|c8e6|h7h5"  // NOT f7d6 which enables mateIn1 Qd5g8, from https://lichess.org/m2Rjzmxl/black#61
            , "r1b3k1/pp3p1p/3b2pB/4p3/1pB5/8/P4PPP/3R2K1 b - - 4 25, d6c7|d6e7"  // NOT d6c5 leading to matein1
            , "rqb1k1nr/1p1p1ppp/pQ1b4/3Np1B1/4P3/8/PPP1BPPP/R4RK1 b kq - 5 11, g8e7|f7f6"  // needed new feature: NOT d6c7 - it avoids mateIn1 but still leads to mate later -> needs new feature of blocking of other opponents piece covering the mating square
            // from NOTmateIn1 puzzles that are normally correct, but fail after considering all check moves for checking-flag instead of only ShortestUnconditionalPredecessors
            , "1k2r3/p2r1R2/2Q5/1p5p/P1P3p1/8/6PP/7K w - - 2 44 moves c6d7, e8e1"  // puzzle NOTmateIn1 Nr.1, contd. f7f1 e1f1
            , "8/8/8/1R3p2/1P6/6k1/r6p/7K w - - 2 50 moves b5f5, a2a1" // dito, + f5f1 a1f1
            , "8/R7/3P4/4p1p1/3rPp1k/5P2/5K2/8 b - - 0 46 moves d4d6, a7h7" // dito, + d6h6 h7h6
    })
    void ChessBoardMatingPuzzles_GetBestMove_isBestMove_doCheckmate_Test(String fen, String expectedBestMove) {
        doAndTestPuzzle(fen,expectedBestMove, "Simple  Test", false);
    }

    // double contribution scenario
    @ParameterizedTest
    @CsvSource({
            //simple ones
            "2r3nk/2p3pp/3p4/P1rbp3/2N5/1P2Q3/P5PP/6NK w - - 0 10, c4e5" // code works, but not sufficient to make the move top-1...
              })
    void ChessBoardGetBestMove_doubleContribExploit_Test(String fen, String expectedBestMove) {
        doAndTestPuzzle(fen,expectedBestMove, "Simple  Test", true);
    }



    // choose the one best move
    @ParameterizedTest
    @CsvSource({
            //simple ones
            "8/8/2r2Q2/2k5/4K3/8/5b2/8 w - - 0 1, f6f2",
            "8/8/2r2Q2/8/2k1K3/8/5b2/8 w - - 0 1, f6c6",
            "8/2r5/2k5/8/4KQ2/8/8/2b5 w - - 0 1, f4c1",
            "8/2r5/8/bk1N4/4K3/8/8/8 w - - 0 1, d5c7",
            "3r4/8/8/3Q2K1/8/8/n1k5/3r4 w - - 0 1, d5a2"
            , "8/2p5/3k1p1p/4p1p1/2Q1P1P1/1p3P2/1P5P/3K4 w - - 1 40, c4b4|c4b3"  // NOT c4c7(!?)
            , "r1b1k2r/pppp1ppp/2n5/2b5/4PqP1/2PB4/PPN2P1P/RN1QK2R w KQkq - 0 11, d1e2|h1f1" // NOT d1f3 - why just kill own queen?
            , "r2qkb1r/pp2pppp/2p2n2/3P4/Q3PPn1/2N5/PP3P1P/R1B1KB1R w KQkq - 0 11, d5c6|h2h3|f2f3"  // //interresting for future move selection - best move remains best, even if second best h2h3 is chosen

            //
            , "rnbqk2r/pp2Bpp1/2pb3p/3p4/3P4/2N2N2/PPP1BPPP/R2QK2R b KQkq - 0 8, d8e7|d6e7" // better dont take with king
            //Forks:
            , "8/8/8/k3b1K1/8/4N3/3P4/8 w - - 0 1, e3c4"
            , "8/8/8/k3b1K1/3p4/4N3/3P4/8 w - - 0 1, e3c4"
            , "8/3p4/1q3N2/8/1k6/8/3P4/2K5 w - - 0 1, f6d5" // make fork instead of greedily taking a pawn
            // do not allow opponent to fork
            , "r1bq1rk1/pp1nbpp1/4pn1p/3p2B1/P2N4/2NBP3/1PP2PPP/R2Q1RK1 w - - 0 10, g5h4|g5f6"  // NOT g5f4 as it enables a p fork on e5. but g5h4|g5f6 - from https://lichess.org/EizzUkMY#18
            , "3q1b1r/rbp2ppp/1pk2n2/p2p4/P7/2N2BP1/1PQPPP1P/R1B2KNR b - - 0 17, c6d7" // NOT b7a6, not sufficient to block the abzugschach with rook fork
            //stop/escape check:
            , "rnb1kbnr/pppp1ppp/8/4p3/7q/2N1P3/PPPPP1PP/R1BQKBNR  w KQkq - 2 3, g2g3"
/*TODO?*/   , "8/3pk3/R7/1R2Pp1p/2PPnKr1/8/8/8 w - - 4 43, f4f3|f4e3",  // f5  looks most attractive at the current first glance, but should be f4e3|f4f3 - and NOT f4f5 -> #1
            "r6k/pb4r1/1p1Qpn2/4Np2/3P4/4P1P1/P4P1q/3R1RK1 w - - 0 24, g1h2",
            "rnl1k2r/pppp1ppp/4p3/8/3Pn2q/5Pl1/PPP1P2P/RNLQKLNR  w KQkq - 0 7, h2g3",
            "r1lq1l1r/p1ppkppp/p4n2/1P3PP1/3N4/P4N2/2P1Q2P/R1L1K2R  b KQ - 4 17, e7d6|f6e4",
            "6k1/1b3pp1/p3p2p/Bp6/1Ppr2K1/P3R1PP/5n2/5B1R w - - 1 37, g4h5",  // https://lichess.org/bMwlzoVV
            "r1lq2r1/1p6/p3pl2/2p1N3/3PQ2P/2PLk3/PP4P1/5RK1  b - - 4 23, e3d2"
            , "3r3k/1bqpnBp1/p1n4R/1p6/4P3/8/PP1Q1PPP/2R3K1 b - - 0 22, g7h6" // not null! pg7xh6 not listed as valid move!
            , "3qk2r/2p1bpp1/1r6/pb1QPp1p/P2P4/2P2N1P/1P3PP1/R1B1K2R w KQk - 0 17 moves c3c4 e7b4, c1d2" // NOT 0-0, because it is check
            , "r1b1k2r/ppp2pp1/2n1p3/b6p/2BPq3/P1N1nN2/1PPQ1PPP/R3K2R w KQkq - 0 12, f2e3|d2e3"  // just take, from https://lichess.org/eTPndxVD/white#22
            // pawn endgames:
            , "8/P7/8/8/8/8/p7/8 b - - 0 1, a2a1q"
            , "8/P7/8/8/8/8/p7/8 w - - 0 1, a7a8q"
            // (ex)blunders from tideeval test games against local SF
            , "1r1q1rk1/2p2pbp/p1ppbnp1/4p3/1NP1P3/P1N1BP2/1P1Q2PP/R3KB1R b KQ - 3 14, d8d7|d8e8|e6d7" // cover forking square - NOT c6c5
            //// (ex)blunders from tideeval online games
            , "1rbqk2r/p1ppbp1p/2n1pnp1/4P3/1p1P1P2/2P1BN1P/PPQNB1P1/R4RK1 b - - 0 13, f6d5|f6h5"  // instead of blundering the knight with g6g5
            , "1rb2rk1/p1pp1pp1/1pn5/3p2p1/2B1Nb2/2P5/PP1N1PPP/R1B1K2R w KQ - 0 19, c4d5"  // bug was moving away with N and getting l beaten...
            , "rnbqkbnr/pp2ppp1/3p3p/2p3B1/8/2NP4/PPP1PPPP/R2QKBNR w KQkq - 0 4, g5d2|g5f4|g5c1"  // B is attacked - move it away!
            , "1k6/5r2/1p5p/7P/qb5R/4PNP1/1R2P1K1/8 b - - 1 45, f7b7|a4c6|f7f6|a4a5|b8a7"  // NOT f7f3, do not throw away quality - almost FUTURE, but solved :-)
            // X ray
            , "r1b2rk1/pp4pp/8/2Q2p2/8/P4N2/1PP1qPPP/R3R1K1 b - - 1 16, e2a6" // NOT e2e8, thinking, the R would not cover through q by X-RAY
            , "r4rk1/1p3pp1/p1ppbnq1/4b3/2Q1P1P1/P1N1BB2/R1P5/4K1R1 w - - 2 29, c4d3|c4b4"

            // do not leave behind
            , "rnqk3r/pp2ppbp/5np1/1Rp5/P6P/3Q2P1/1P2PP2/1NB1K1NR b K - 0 12, b8d7"  // not q e8d7 which looks like magical right triangle keeps protecting the b7 pawn, but isn't because of moving into check blocking            // fake checkmate wrongly acoiden :-)
            , "r1bqk2r/pppnbp2/4p1P1/3pPn2/3P1P1P/2N2Q2/PPP1NB2/3RKB1R b Kkq - 0 17, f7g6|f5h4"
            //Warum nicht einfach die Figur nehmen?
            , "5rk1/p2qppb1/3p2pp/8/4P1b1/1PN1BPP1/P1Q4K/3R4 b - - 0 24, g4f3" // lxP statt Zug auf Feld wo eingesperrt wird,  https://lichess.org/7Vi88ar2/black#79
            , "r4rk1/pbqnbppp/1p2pn2/2Pp4/8/1P1BPN1P/PBPNQPP1/R4RK1 b - - 0 11, d7c5|b6c5|c7c5|e7c5"  //  - sieht auch noch nach komischen Zug aus, der etwas decken will aber per Abzug einen Angriff frei gibt.   https://lichess.org/dhVlMZEC/black
            , "r2qkb1r/ppp2ppp/2n1bn2/4p3/Q7/2N2NP1/PP2pPBP/R1B2RK1 w kq - 0 9, c3e2|f1e1"  // NOT f3d2, but just take pawn or save rook and take pawn later
            // qa5c3 acceptable for now as q is in danger behind N , "r1b1kbnr/3n1ppp/p3p3/qppp4/3P4/1BN1PN2/PPPB1PPP/R2QK2R b KQkq - 1 8, c5c4" // would have trapped B - https://lichess.org/Cos4w11H/black#15
 /*Todo*/           , "r1b1kbnr/3n1ppp/p3p3/q1pp4/Np1P4/1B2PN2/PPPB1PPP/R2QK2R b KQkq - 1 9, c5c4" // still same
            , "rn2qk1r/1pp4p/3p1p2/p2b1N2/1b1P4/6P1/PPPBPPB1/R2QK3 w Q - 0 16, g2d5"  // do not take the other b first, although it could give check
            , "8/pp6/8/4N3/6P1/2R5/2k1K3/8 b - - 0 61, c2c3"  // blunder was c2b1??
            // best move not so clear: "1r1qk1r1/p1p1bpp1/1p5p/4p3/1PQ4P/P3N1N1/1B1p1PP1/3K3R w - - 2 29, b2e5"   // https://lichess.org/ZGLMBHLF/white
            , "r1b1k2r/ppppnppp/2N2q2/2b5/4P3/2P1B3/PP3PPP/RN1QKB1R b KQkq - 0 7, c5f3|f6c6"  // from gamesC#1 2-fold-clash with only one solution
    })
    void ChessBoardGetBestMove_isBestMoveTest(String fen, String expectedBestMove) {
        doAndTestPuzzle(fen,expectedBestMove, "Simple  Test", true);
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
            "1rbqkbnr/p1p1pppp/1pnp4/3P4/4PB2/2N5/PPP2PPP/R2QKBNR b KQk - 0 5, d8d7" // was bug: wrongly calc what black queen can do to protect the knight
            // do not stale mate
            , "K7/8/7p/8/1q6/4k3/8/8 b - - 0 1, b4b6"  // e.g. not with the queen
            // TODO!: do not get matted in one
            , "1r4k1/p4ppp/2p1p3/P7/1PK5/6P1/4PP1P/3R4 b - - 0 24, b8b5"  // r needs to stay to defend the back rank
            , "k3r3/pp4pp/3B1p2/3n4/8/3P4/5PPP/R5K1 w - - 6 27, a1a4" // same for R
            , "1k6/2p5/2b5/3r2p1/4p3/5p2/5P1B/2R3K1  w - - 0 38, c1c6" // same, but R needs to overcome urge to take free b
            // do not move away from covering a king fork
            , "r2qkb1r/1pp1ppp1/5n2/3p1b1p/1n6/2NP1NP1/P1PQPP1P/R3KB1R w KQkq - 0 9, d2e3"
            // do not move into a fork
            , "rnbqkbnr/ppp2ppp/8/4p3/3pN3/5N2/PPPPPPPP/R1BQKB1R w KQkq - 0 4, a1a1"
            //// Bugs from TideEval games
            , "rql1k1nr/p3p2p/7l/Q1pNNp2/8/P7/1PP2PPP/R4RK1  b k - 5 18, c5b4",            // Bug was an illegal pawn move
            "2lqklnr/1p1npppp/r1pp4/2P5/3PP3/P1N2N2/5PPP/R1LQKL1R  b KQk - 0 10, a6-a1",  // was bug: suggested illegal move (one with unfulfilled condition)
            "r1b1k2r/ppppqppp/2n1pn2/3PP1B1/1b6/2N2N2/PPP2PPP/R2QKB1R b KQkq - 0 8, f6g8",  // IS bug: n moves away, but was pinned to queen
            "rnbqk2r/pppp1ppp/5n2/2bP4/1P6/P1N2N2/4PPPP/R1BQKB1R b KQkq - 0 8, b8a6" // https://lichess.org/hK7BbAmi/black
            , "3rkb1r/p1pq1p1p/1p2bnp1/2p1P3/5B2/P1N2N2/1PQ2PPP/R4RK1 b k - 0 20, d7e7"  // e6f5|f6d5|f6h5 https://lichess.org/LZyhujqK/black
            , "r3kb2/ppp2pp1/3qp3/3n2P1/1nQPB3/8/PPP1NP2/R1B1K3 w Qq - 5 15, c1f4" // was bug in sorting of coverage pieces -> so q came before n, which made L have releval of 0 on f4 and move there...
            , "r1bqk2r/p1pp1ppp/2nbp3/1p6/3Pn3/1NP2N2/PP2PPPP/R1BQKB1R w KQkq - 2 8, c1g5"  // prob. same bug as one line above
            , "r3k2r/pp3ppp/4b3/1P1p4/2P1n3/N4N2/P4PPP/R3R1K1 b kq - 0 19, d5c4" // moves away defender of ne4 - https://lichess.org/mGjWE4SA/black
            , "2r1k2r/pp3ppp/4b3/1P6/2p1R3/N4N2/P4PPP/2R3K1 b k - 2 21, f7f5"    // again! in same game
            , "rnbqk2r/p2p1ppp/2p1pn2/1p4B1/1b1PP3/2NB1N2/PPP2PPP/R2Q1RK1 b kq - 3 10, f6g4" // pinned to q https://lichess.org/n4SajnZ3/black
            , "r2qr1k1/ppp2pbp/2n2np1/2B1p3/2B1P1b1/2NP1N2/PPP3PP/R2QK2R w KQ - 8 11, f3g5" // pinned to queen - https://lichess.org/nSaDkrhq/white
            , "r1b1kb1r/5ppp/p3p3/1qNn2N1/1ppPB1nP/4P3/PP1B1PP1/R2QK2R b KQkq - 4 15, d5f6"  // pinned to rook - https://lichess.org/Cos4w11H/black#29
            , "rr6/p1p1kppp/2p1qn2/5Q2/2NPP3/3P4/PP3PPP/R3KB1R w KQ - 3 17, e4e5"  // takes cover from Q ... gone
            , "r1b1kb1r/ppp1pppp/3q1n2/8/2Qn4/P4N2/1P2PPPP/RNB1KB1R w KQkq - 0 7, c4f7" // needless big blunder looses queen !=
            , "rq2kb1r/p4ppp/Qp1p1n2/2p5/4p1bP/1NN1P1P1/PPPP1P2/R1B1K2R b Qkq - 1 15, a1h8"  // did nothing, should at least make ANY move :-) and it does - game https://lichess.org/d638Kk4Q/black#29 may be hat a liChessBot-bug?
            , "rnbqkb1r/pppp3p/5p2/5p2/3N4/7R/PPPPPPP1/R1BQKB2 b Qkq - 0 7, d8e7"  // would move queen into king-pin by R
            , "3r2k1/Q1p2pp1/1p4bp/1BqpP3/P2N3P/2P3K1/1P4P1/R6R w - - 3 28, d4c6"  // d4c6 give complete way free for queen to attack
/*Todo*/            , "r1bq3r/pp2kp1p/1n2p1p1/2Qp4/P1p5/2P2NPB/1PP1PP1P/R3K2R b KQ - 3 13, e7d7" // NOT e7d7, but d8d6|e7e8 where k locks the vulnerable knight and k is checkable by N https://lichess.org/eI3EmDF8/black#25
            , "rnb1kb1r/pp1p1ppp/2p5/4p3/P1P1n1qP/1QN1P1PB/1P1P1P2/R1B1K1NR b KQkq - 2 8, g4e3" // NOT g4e3, Queen would still be dead - was bug in old_updateRelEval concering 2nr row attacks with no other direct attackers
            , "r2qkb1r/p4ppp/2p2n2/3p4/6b1/4PP2/PPPP3P/RNBQK2R b KQkq - 0 10, d8d7" //NOT d8d7 - do not leave b behind
            , "rnbqk1nr/pp1pppbp/6p1/2p5/P7/6PB/1PPPPP1P/RNBQK1NR b KQkq - 2 4, g7b2"  // NOT g7b2 , blundered bishop
            , "rnbqkb1r/pppp3p/5p2/5p2/3N4/7p/PPPPPPP1/R1BQKB1R w KQkq - 0 7, h1g1"  // NOT h1g1 - however, not taking, but e3 to free way of Q is actually the very best move here... (in the future)
            , "1r2r1k1/q1pb1p1p/5np1/N1p1p3/2B1P3/P4P1P/1N1Q2P1/3RK2R b K - 1 24, d7e6"  // b8b6|a7b6, NOT d7e6 which enables a fork a5c6


            // do  not take with too much loss
            , "r3qrk1/4bppp/1Q1ppn2/p7/b2P4/5N2/1P2PPPP/R1B1KB1R w KQ - 0 16, a1a4"  //sac quality for nothing
            , "rnbqk1nr/pp1pppbp/6p1/2p5/P7/6PB/1PPPPP1P/RNBQK1NR b KQkq - 2 4, g7b2" // NOT g7b2 - taking a covered pawn!? (happend due to overrating need to save Rh1 from being trapped

            //BUG: Queen move h4h6 leads to problem with (ill)legal pawn move and thus illegal suggestion h7h5
            , "r1b2k1r/ppNp3p/4p1p1/2p2p2/P6Q/1n1qP1P1/1P1n1PBP/2B3KR w - - 4 26 moves h4h6 f8f7 c7a8 b3c1 g2h3, h7h5" // d2f3 NOT h7h5
            //bug: move "away" on the same diagonal where the threat points to does not work...
            , "rnb1kb1r/pp1p1ppp/2p5/4p3/P1P1n1qP/1QN1P1PB/1P1P1P2/R1B1K1NR b KQkq - 2 8, g4e6"
            // king pins
            , "r2qr1k1/1b3ppp/p3p3/PpQ1P3/5P2/7P/1PK1BPP1/R6R w - - 1 19, NOT e2f3" // NOT e2f3, which is followed by pin ra8c8, Future: c5e3|c5d6|c5b4
            // bad 2-square pawn move at en passant possiblity for opponent
            , "5k1r/pp1r1pRp/4p3/3pP3/b1p1P2P/2P3R1/2PK1PP1/5B2 b - - 0 22, f7f5"
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
            , "6k1/5p2/4p1pp/8/8/5B2/6PP/1r3rQK w - - 0 2, h2h3|h2h4"  // NOT g1f1|g2g3|g2g4, will be mateIn1 - give Luft with the right piece  // from puzzle
            //// blunders from games
            , "r2qkb1r/ppp1nppp/2n5/4pbP1/8/5p1N/PPPPP1BP/R1BQK1R1 w Qkq - 0 9, g2f3|e2f3"  // why not just take pf3? + strange debaug output on moving away benefit:
                    /*100@1 Benefit helping pieces freeing way of vPce(23) on [f3] 3 ok&if{e2-any (weiß)} away from weißer Läufer} to f3.
                    ->[indirectHelp:e2d3]
                    ->[indirectHelp:e2e4] */
            , "1r4r1/1p3p1p/2k1p1pP/3p1b2/P1q2P2/K5P1/5Q2/2R4R b - - 0 40, b7b5|f5d3"  // b7b5|f5d3 bug: makes illegal move with king pinned queen
            , "rn2kbnr/pp1q1bpp/3p4/3N1p1Q/8/8/PPP1PPPP/R1B1KB1R w KQkq - 2 12, h5f3" // bug: does not use queen to move away AND cover knight - as because of the king-pin it thinks the knight is not threatened
            // probably requiring move simulation of best moves
            , "r2qkb1r/pppbpppp/2np1n2/8/Q1PP4/P4N2/1P2PPPP/RNB1KB1R b KQkq - 3 5, c6d4"  // n takes covered pawn, but white first needs to save queen  https://lichess.org/LZyhujqK/black
            , "r2k2nr/pp1b1p1p/5b2/4n1p1/4Q3/2Pp2P1/PP3P1P/R3KB1R b KQ - 1 18, d7c6"  // doppelbedrohung ist möglich L->q->t
            , "r1b1k1nr/ppp2ppp/2n1p3/b1q5/8/P1NP1N2/1PPB1PPP/R2QKB1R w KQkq - 1 8, b2b4"  // fork P->l+q possible (but wins only n or l for 2Ps)
            /*Future: "Abzug-zwischengewinn"*/  , "r1bq1rk1/1p2bppp/p2p1n2/2p5/4PB2/2NQ4/PPP1BPPP/2KR3R w - - 0 11, f4d6"    // take it - in a slightly complex clash, but worth it https://lichess.org/as1rvv81#20 - was no bug in clashes/relEval on d6 with 2nd row. relEval==100 seems ok, but unclear why. Adding releval of -320@0 as result/benefit despite nogo for vPce(15=weißer Läufer) on [d6] 1 ok away from origin {f4} on square f4. ->f4d6(-320@0) -> (strange: T on d1 hast dist==3 instead of 2, up to calcLevel of 3, so it is not counted in the clash at first, only later at currentlimit==4)
            // etc.
            , "3r2k1/5ppp/3p4/p1pP2P1/P1Rb1B2/r7/4K3/1R6 w - - 3 31, f4d6" // take a piece, because covering piece also needs to cover back rank mate https://lichess.org/as1rvv81#60
            // X-ray
            , "8/5p1k/6pp/Kp5b/5P2/P2P4/1r4P1/6R1 w - - 2 35, g2g4"  // trap L with P -> not considered, because R does not cover "through" P, although P move would fulfill the condition

    })
    void FUTURE_ChessBoardGetBestMove_MoveTest(String fen, String expectedBestMove) {
        ChessBoard board = new ChessBoard("CBGBM", fen);
        Move bestMove = board.getBestMove();
        assertEquals( (new Move(expectedBestMove)).toString(), bestMove.toString() );
    }


    // FUTURE do NOT choose a certain move
    @ParameterizedTest
    @CsvSource({
            "r1lqkl1r/pppppppp/2n2n2/8/4P3/2N2N2/PPPP1PPP/R1LQKL1R  b KQkq e3 0 3, a8b8"
            //// blunders from games
            , "r1bqk1nr/pp2ppbp/2n3p1/2p5/4N3/2Pp2P1/PP1N1P1P/R1BQKB1R b KQkq - 3 8, c5c4"  // do not cover a pawn with a pawn, where it has a nogo...
            // do not allow opponent to fork by moving away from covering a forking move
            , "r1b1kb1r/pp3pp1/3p3p/qN2p3/2PpP3/5N2/PP3PPP/2RQ1RK1 b kq - 1 15, a5a2" // f8e7, NOT a5a2 - do NOT move away from covering the forking square c7
            //
            , "r4r1k/1ppb3p/4pp1R/p3n3/4q3/P3B3/2P2PP1/R2QKB2 w Q - 2 21, g2g3"  // do NOT allow n to give check
            , "r2n2kr/4bppp/1P2pn2/p7/5B1P/1PNK1N2/P1P3P1/4R3 b - - 0 25, f6d5"  // d5 looks coverd, but isn't because of a pin of the pawn to the le7
            , "r3r1k1/p1p1qNbp/1p3np1/4p3/2BnP3/3PB3/PPP3PP/R2NK2R w KQ - 1 15, c2c3" // wrong counter action against fork nc2 - https://lichess.org/nSaDkrhq/white#28
            // do not move away
            , "rnbqk2r/1p3pp1/4pn2/p7/1b1P2N1/2N1BQ2/1PP3KP/R4R2 b q - 0 18, f6g4"  // do NOT move away n, because this enables a mateIn1
/*ToDo*/    , "r2q3r/pp3ppp/2k1p3/8/PP2N2P/4p3/1P1N1PP1/R1Q1K2R b KQ - 0 17, c6d5"  // dont run into mateIn1 https://lichess.org/vR81ZGlO/black
            , "r2r3k/pp6/2nPbNpp/4p3/2P2p2/2P5/P3PPPP/3RKB1R w K - 4 20, f6d5" // do not block a own coverage of T to P by moving in between - https://lichess.org/LizReIjS/white
            , "r1b1k1nr/3p1p2/p3pbp1/7p/1p1PP1P1/1N4K1/PPP1BP1P/R1B2R2 b kq - 0 19, g8h6" // because of fork after g4g5
            // do not move away and get mated in 1
            , "3r1rk1/1b3pp1/p1q1p2p/1p2P3/2pP4/P1P1b1BP/BP2NQP1/R5K1  w - - 1 26, f2e3"
            // king-pin overrated
            // = my move unpins from king and allows "unplanned" clash contribution
            , "rnb1kb1r/pp3ppp/8/8/3qP3/3N3P/PPP3P1/R2K4 b kq - 2 19, d4b2"  // NOT take pawn on square protected by a simultaneously unpinned knight - https://lichess.org/OinmOvs4/black#37
            , "2rq1b1r/pppb1k1p/4p1p1/4Pp1Q/2B5/P1P1P3/5PPP/R1B2RK1 w - - 0 15, h5f5" // double!! - NOT h5f5 give away Q

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
        doAndTestPuzzle(fen, expectedMoves, themes, false);
    }

    static void doAndTestPuzzle(String fen, String expectedMoves, String themes, boolean debugmoves) {
        ChessBoard.DEBUGMSG_MOVEEVAL = debugmoves;
        ChessBoard.DEBUGMSG_MOVESELECTION = debugmoves;
        ChessBoard.DEBUGMSG_MOVESELECTION2 = debugmoves;
        ChessBoard.DEBUGMSG_MOVEEVAL_AGGREGATION = debugmoves;
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
        System.out.println("Searching Best move for Board: " + board.getBoardName() + ": " + board.getBoardFEN() + " .");
        Move bestMove = board.getBestMove();
        ChessBoard.DEBUGMSG_MOVEEVAL = false;
        ChessBoard.DEBUGMSG_MOVESELECTION = false;
        ChessBoard.DEBUGMSG_MOVESELECTION2 = false;
        ChessBoard.DEBUGMSG_MOVEEVAL_AGGREGATION = false;

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

Data-Bug!?!
 inconsistency in best move depending on if position was reached via startpos+moves or via fen
 1)
 position startpos moves e2e4 e7e5 b1c3 g8f6 g1f3 b8c6 f1c4 f6e4 c3e4 d7d5 d1e2 d5c4 e2c4 f7f5 e4c5 d8e7 b2b4 e5e4 f3d4 e7f7 d4e6 c6e5 c4b3 e5d7 e6c7
 go
 bestmove e8d8
 2)
 position fen r1b1kb1r/ppNn1qpp/8/2N2p2/1P2p3/1Q6/P1PP1PPP/R1B1K2R b KQkq - 0 13
 go
 bestmove e8e7



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


/* BIG BUG

> ucinewgame
> isready
<- readyok
> position startpos moves b2b3 e7e5 c1b2 d7d5 b2e5 b8c6 g1f3 c6e5 f3e5 g8f6 e2e3 f6e4 d1f3 c8f5 f3f5 e8e7 e5f7 d8b8 f5e5 e7f7 e5d5 f7f6 d5e4 f6f7 b1c3 f8c5 e4d5 f7g6 f1d3 g6h6 d5c5 h8e8 d3f5 h6g5 c3e4 g5h5 f5h7 h5h6 c5f5 e8e4 f5e4 h6g5 e4g6 g5h4 g2g3 h4h3 g6g7 b8d8 h7e4 a8c8 e4f5 h3g2 g7d7 d8d7 f5d7 g2h1 d7c8 h1h2 c8b7 h2h3
=new Board: + b2b3 e7e5 c1b2 d7d5 b2e5 b8c6 g1f3 c6e5 f3e5 g8f6 e2e3 f6e4 d1f3 c8f5 f3f5 e8e7 e5f7 d8b8 f5e5 e7f7 e5d5 f7f6 d5e4 f6f7 b1c3 f8c5 e4d5 f7g6 f1d3 g6h6 d5c5 h8e8 d3f5 h6g5 c3e4 g5h5 f5h7 h5h6 c5f5 e8e4 f5e4 h6g5 e4g6 g5h4 g2g3 h4h3 g6g7 b8d8 h7e4 a8c8 e4f5 h3g2 g7d7 d8d7 f5d7 g2h1 d7c8 h1h2 c8b7 h2h3
> go wtime 2096 btime 26880 winc 0 binc 0
=go go wtime 2096 btime 26880 winc 0 binc 0
<- bestmove e1g1
Error: Index -65 out of bounds for length 64
Error: [Ljava.lang.StackTraceElement;@67af833b



 */



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


