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

import java.util.*;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBasics.EVAL_TENTH;
import static de.ensel.tideeval.ChessBoard.*;

public class EvaluatedMove extends Move {
    int[] eval = new int[MAX_INTERESTING_NROF_HOPS + 1];

    EvaluatedMove(final int fromCond, final int toCond) {
        super(fromCond, toCond);
        Arrays.fill(eval, 0);
    }

    EvaluatedMove(Move move, int[] eval) {
        super(move);
        this.eval = Arrays.copyOf(eval, eval.length);
    }

    EvaluatedMove(EvaluatedMove evMove) {
        super(evMove);
        this.eval = Arrays.copyOf(evMove.eval, evMove.eval.length);
    }

    void initEval(int initEval) {
        Arrays.fill(eval, initEval);
    }

    /**
     * sets an eval on a certain future level
     * beware: range is unchecked
     * @param evalValue
     * @param futureLevel the future level from 0..max
     */
    void setEval(int evalValue, int futureLevel) {
        eval[futureLevel] = evalValue;
    }

    /**
     * adds or substracts to/from an eval on a certain future level
     * beware: is unchecked
     * @param evalValue
     * @param futureLevel the future level from 0..max
     */
    void addEval(int evalValue, int futureLevel) {
        eval[futureLevel] += evalValue;
    }

    void addEval(int[] eval) {
        for (int i=0; i<this.eval.length; i++)
            this.eval[i] += eval[i];
    }

    void subtractEval(int[] eval) {
        for (int i=0; i<this.eval.length; i++)
            this.eval[i] -= eval[i];
    }

    @Override
    public String toString() {
        return "" + super.toString()
                + "=" + Arrays.toString(eval);
    }

    boolean isBetterForColorThan(boolean color, EvaluatedMove other) {
        int i = 0;
        debugPrintln(DEBUGMSG_MOVESELECTION, "  comparing move eval " + this + " at "+i + " ");
        int comparethreshold = pieceBaseValue(PAWN)-(EVAL_TENTH<<1); // 80
        boolean probablyBetter = false;
        while (i < other.eval.length) {
            if (isWhite(color) ? eval[i] > other.eval[i] + comparethreshold
                               : eval[i] < other.eval[i] - comparethreshold) {
                debugPrintln(DEBUGMSG_MOVESELECTION, "!=" + i + " " + Arrays.toString(eval) +".");
                return true;
            }
            if (isWhite(color) ? eval[i] > other.eval[i] + (comparethreshold >> 1)
                               : eval[i] < other.eval[i] - (comparethreshold >> 1)) {
                probablyBetter = true;
                /*if (i > 0)         // *0.75
                    comparethreshold -= comparethreshold >> 2; */
                debugPrintln(DEBUGMSG_MOVESELECTION, "?:" + i + " " + Arrays.toString(eval) + ".");
                i++;
                continue;
            }

            if (isWhite(color) ? eval[i] < other.eval[i] - (comparethreshold) // - lowthreshold
                                    : eval[i] > other.eval[i] + (comparethreshold) ) {
                debugPrintln(DEBUGMSG_MOVESELECTION, "stopping, seems worse in compare at i=" + i + " " + Arrays.toString(eval) + ".");
                probablyBetter = false;
                break;
            }
            debugPrintln(DEBUGMSG_MOVESELECTION, "similar, cont i=" + i + " " + Arrays.toString(eval) + ".");
            i++;  // same evals on the future levels so far, so continue comparing
        }
        return probablyBetter;
    }

    static List<EvaluatedMove> addEvaluatedMoveToSortedListOfCol(EvaluatedMove evMove, List<EvaluatedMove> sortedMoves, boolean color, int maxEntries) {
        int i;
        for (i=0; i<sortedMoves.size(); i++) {
            if (evMove.isBetterForColorThan(color, sortedMoves.get(i))) {
                sortedMoves.add(i, evMove);
                break;
            }
        }
        if (i==sortedMoves.size() && i<maxEntries)
            sortedMoves.add(i, evMove);
        else {
            while (sortedMoves.size() > maxEntries) {
                sortedMoves.remove(maxEntries);
            }
        }
        return sortedMoves;
    }

    public int[] getEval() {
        return eval;
    }
}
