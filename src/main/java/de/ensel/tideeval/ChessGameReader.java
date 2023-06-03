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

import java.util.Scanner;

import static de.ensel.tideeval.ChessBasics.*;

public class ChessGameReader {
    final Scanner game;
    enum ItemType { MoveNr, Move, Eval, ItsOver }
    ItemType nextExpected;

    public ChessGameReader(final String gameString) {
        this.game = new Scanner(gameString);
        nextExpected = ItemType.MoveNr;
        // we start by skipping=ignoring the first moveNr
        skipMoveNr();
    }

    public String getNextMove() {
        assert(nextExpected==ItemType.Move);
        String move;
        if (game.hasNext()) {
            move = game.next();
            nextExpected = ItemType.Eval;
            return move;
        }
        else {
            // should not happen
            // we can quit or ignore the syntax error in the string... assert(false);
            nextExpected=ItemType.ItsOver;
            return null;
        }
    }

    public int getNextEval() {
        assert(nextExpected==ItemType.Eval);
        int eval;
        if (game.hasNext()) {
            String nxt = game.next();
            if (nxt.charAt(0)=='#') {
                if (nxt.charAt(1) == '-')
                    eval = WHITE_IS_CHECKMATE+(int)(Double.valueOf(nxt.substring(2))*CHECK_IN_N_DELTA);
                else
                    eval = BLACK_IS_CHECKMATE-(int)(Double.valueOf(nxt.substring(1))*CHECK_IN_N_DELTA);
            } else if (nxt.charAt(0)=='{') {
                nextExpected=ItemType.ItsOver;
                return OPPONENT_IS_CHECKMATE;
                //TODO: Could be stalemate or Remis! needs to be checked with previous move-symbol?
            } else {
                eval = (int)(Double.valueOf(nxt) * 100.0);
            }
            nextExpected = ItemType.MoveNr;
            skipMoveNr();
            return eval;
        }
        else {
            // should not happen
            // we can quit or ignore the syntax error in the string... assert(false);
            nextExpected=ItemType.ItsOver;
            return 0;
        }
    }

    private void skipMoveNr() {
        assert(nextExpected==ItemType.MoveNr);
        if (game.hasNext()) {
            String nxt = game.next();
            // didn't work:  game.skip("([0-9]+)((\\.)+)");
            if (nxt.matches("([0-9]+).+")) {
                nextExpected = ItemType.Move;
            } else {
                nextExpected = ItemType.ItsOver;
            }
        }
        else {
            // should not happen
            assert(false);
            nextExpected=ItemType.ItsOver;
        }
    }

    public boolean hasNext() {
        if (nextExpected==ItemType.ItsOver)
            return false;
        if (game.hasNext())
            return true;
        nextExpected = ItemType.ItsOver;
        return false;
    }
}
