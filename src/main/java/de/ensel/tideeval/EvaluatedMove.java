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
import static java.lang.Math.abs;

public class EvaluatedMove extends Move {
    int[] eval = new int[MAX_INTERESTING_NROF_HOPS + 1];

    private boolean isCheckGiving;

    EvaluatedMove(final int from, final int to) {
        super(from, to);
        Arrays.fill(eval, 0);
        isCheckGiving = false;
    }

    EvaluatedMove(Move move, int[] eval) {
        super(move);
        this.eval = Arrays.copyOf(eval, eval.length);
        isCheckGiving = false;
    }

    EvaluatedMove(EvaluatedMove evMove) {
        super(evMove);
        this.eval = Arrays.copyOf(evMove.eval, evMove.eval.length);
        isCheckGiving = false;
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
        debugPrintln(DEBUGMSG_MOVESELECTION, "  comparing move eval " + this + " at "+i + " with " + other +".");
        int comparethreshold = (pieceBaseValue(PAWN)>>1)+(EVAL_TENTH); // 60
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
                // tighten comparethreshold more if it was almost a full hit and leave it almost the same if it was close to similar
                comparethreshold = comparethreshold - ( abs(eval[i]-other.eval[i]) - (comparethreshold>>1) );
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
            i++;  // olmost same evals on the future levels so far, so continue comparing
        }
        return probablyBetter;
    }

    static List<EvaluatedMove> addEvaluatedMoveToSortedListOfCol(EvaluatedMove evMove, List<EvaluatedMove> sortedMoves, boolean color, int maxEntries) {
        int i;
        for (i = sortedMoves.size() - 1; i >= 0; i--) {
            if (!evMove.isBetterForColorThan(color, sortedMoves.get(i))) {
                // not better, but it was better than the previous, so add below
                if (i < maxEntries)
                    sortedMoves.add(i + 1, evMove);
                // cut rest if it became too big
                while (sortedMoves.size() > maxEntries) {
                    sortedMoves.remove(maxEntries);
                }
                return sortedMoves;
            }
        }
        //it was best!!
        sortedMoves.add(0, evMove);
        // cut rest if it became too big
        while (sortedMoves.size() > maxEntries) {
                sortedMoves.remove(maxEntries);
        }
        return sortedMoves;
    }

    public int[] getEval() {
        return eval;
    }

    public boolean isCheckGiving() {
        return isCheckGiving;
    }

    public void setIsCheckGiving() {
        isCheckGiving = true;
    }

}
