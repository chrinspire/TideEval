/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
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
