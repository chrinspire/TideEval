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

    /* plys much worse with:  - see results og 0.30pre1+2
    boolean isBetterForColorThan(boolean color, EvaluatedMove other) {
        return evalIsOkForColByMin( unifiedEvalForColor(color) - other.unifiedEvalForColor(color),
                color, 0);
    }

    int unifiedEvalForColor(boolean color) {
        int res = eval[0];
        for (int i = 1; i < eval.length; i++) {
            int e = eval[i];
            if (evalIsOkForColByMin(e, color))  // count  good ones in reduced manner
                e /= (i==1 ? 3 : i+3);
            else                                     // but negative things more
                e = (i==1 ? (e-(e>>2)) : e/(i+1));
            res += e;
        }
        return res;
    }
*/

    boolean isBetterForColorThan(boolean color, EvaluatedMove other) {
        int i = 0;
        if (DEBUGMSG_MOVESELECTION)
            debugPrint(DEBUGMSG_MOVESELECTION, "  comparing move eval " + this + " at "+i + " with " + other +": ");
        int comparethreshold = (pieceBaseValue(PAWN)>>1); // 50
        boolean probablyBetter = false;
        while (i < other.eval.length) {
            if (isWhite(color) ? eval[i] > other.eval[i] + comparethreshold
                               : eval[i] < other.eval[i] - comparethreshold) {
                if (DEBUGMSG_MOVESELECTION)
                    debugPrintln(DEBUGMSG_MOVESELECTION, " done@" + i + ".");
                return true;
            }
            if (isWhite(color) ? eval[i] > other.eval[i] + (comparethreshold >> 1)
                               : eval[i] < other.eval[i] - (comparethreshold >> 1)) {
                probablyBetter = true;
                // tighten comparethreshold more if it was almost a full hit and leave it almost the same if it was close to similar
                comparethreshold = comparethreshold - ( abs(eval[i]-other.eval[i]) - (comparethreshold>>1) );
                if (DEBUGMSG_MOVESELECTION)
                    debugPrint(DEBUGMSG_MOVESELECTION, " ?@" + i);
                i++;
                continue;
            }

            if (isWhite(color) ? eval[i] < other.eval[i] - (comparethreshold) // - lowthreshold
                                    : eval[i] > other.eval[i] + (comparethreshold) ) {
                if (DEBUGMSG_MOVESELECTION)
                    debugPrint(DEBUGMSG_MOVESELECTION, " done, worse@" + i );
                probablyBetter = false;
                break;
            }
            if (DEBUGMSG_MOVESELECTION)
                debugPrint(DEBUGMSG_MOVESELECTION, " similar@=" + i ); // + " " + Arrays.toString(eval) + ".");
            i++;  // almost same evals on the future levels so far, so continue comparing
        }
        if (DEBUGMSG_MOVESELECTION)
            debugPrintln(DEBUGMSG_MOVESELECTION, "=> "+probablyBetter+". ");
        return probablyBetter;
    }

    static void addEvaluatedMoveToSortedListOfCol(EvaluatedMove evMove, List<EvaluatedMove> sortedTopMoves, boolean color, int maxTopEntries, List<EvaluatedMove> restMoves) {
        int i;
        for (i = sortedTopMoves.size() - 1; i >= 0; i--) {
            if (!evMove.isBetterForColorThan(color, sortedTopMoves.get(i))) {
                // not better, but it was better than the previous, so add below
                if (i < maxTopEntries)
                    sortedTopMoves.add(i + 1, evMove);
                // move lower rest if top list became too big
                while (sortedTopMoves.size() > maxTopEntries) {
                    restMoves.add(
                        sortedTopMoves.remove(maxTopEntries) );
                }
                return;
            }
        }
        //it was best!!
        sortedTopMoves.add(0, evMove);
        // move lower rest if top list became too big
        while (sortedTopMoves.size() > maxTopEntries) {
            restMoves.add(
                sortedTopMoves.remove(maxTopEntries) );
        }
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
