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


}
