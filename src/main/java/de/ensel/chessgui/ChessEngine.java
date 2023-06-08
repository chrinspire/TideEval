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

package de.ensel.chessgui;

import java.util.HashMap;

public interface ChessEngine {
    /**
     * Informs ChessEngine of a move made on the board
     * @param move move to execute
     */
    boolean doMove(String move);

    /**
     * Get a move from the ChessEngine
     * @return any move, null if for any reason no move can be returned
     */
    String getMove();
    // TODO: must be divided in two functions:
    //  -> go(...t.b.d.-parameters for e.g. remaining time + increment
    //  -> stop()

    /**
     * Informs ChessEngine of a new Board
     * @param fen FEN-String of the new chess board
     */
    void setBoard(String fen);

    /**
     * Gets the current board from the ChessEngine as:
     * @return FEN-String of chess board
     */
    String getBoard();

    /**
     * Gets general board information to display
     * @return general board information e.g. to display in UI
     */
    // TODO: replace Info-String by table of fieldname+value pairs
    HashMap<String,String> getBoardInfo();

    /**
     * what is the evaluation of the board?
     * @return evaluation in centipawns
     */
    int getBoardEvaluation();

    /**
     * Gets the square information from one square
     * @param square square to get information from
     * @param squareFrom optional square (e.g. for distance)
     * @return Hashmap with keys and the according data
     */
    // TODO: replace Info-String by table of fieldname+value pairs
    HashMap<String,String> getSquareInfo(String square, String squareFrom);

    boolean setParam(String paramName, String value);

    // TODO: Callback-possibility for ChessEngine to UI, esp. for "info"s and end of calculation "bestmove".
}
