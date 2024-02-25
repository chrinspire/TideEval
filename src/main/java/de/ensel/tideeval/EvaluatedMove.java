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

import static de.ensel.tideeval.ChessBoard.*;

public class EvaluatedMove extends Move {
    private final Evaluation eval;

    private boolean isCheckGiving = false;

    EvaluatedMove(final int from, final int to) {
        super(from, to);
        eval = new Evaluation(to);
    }

    EvaluatedMove(final Move move, final int[] rawEval) {
        super(move);
        eval = new Evaluation(rawEval, move.to());
    }

    EvaluatedMove(final int from, final int to, final Evaluation eval) {
        super(from, to);
        this.eval = new Evaluation(eval);
    }

    EvaluatedMove(final Move move, final Evaluation eval) {
        super(move);
        this.eval = new Evaluation(eval);
    }

    EvaluatedMove(final Move move, final int[] rawEval, final int target) {
        super(move);
        eval = new Evaluation(rawEval, target);
    }

    EvaluatedMove(final Move move, final int target) {
        super(move);
        eval = new Evaluation(target);
    }

    EvaluatedMove(final EvaluatedMove evMove) {
        super(evMove);
        eval = new Evaluation(evMove.eval());
        this.isCheckGiving = evMove.isCheckGiving;
    }

    EvaluatedMove(final Move m) {
        super(m);
        eval = new Evaluation(m.to());
    }

    /**
     * adds or substracts to/from an eval on a certain future level (passthrough to Evaluation)
     * beware: is unchecked
     * @param evalValue
     * @param futureLevel the future level from 0..max
     */
    void addEval(int evalValue, int futureLevel) {
        eval.addEval(evalValue,futureLevel);
    }


    void addEval(Evaluation addEval) {
        eval.addEval(addEval);
    }

    @Deprecated
    void addRawEval(int[] eval) {
        for (int i = 0; i< this.eval.getRawEval().length; i++)
            this.eval.addEval(eval[i],i);
    }

    void addEvalAt(int eval, int futureLevel) {
        this.eval.addEval(eval,futureLevel);
    }

    void subtractEvalAt(int eval, int futureLevel) {
        this.eval.addEval(-eval,futureLevel);
    }

    /**
     * calcs and stores the max of this eval and the given other eval individually on all levels
     * @param meval the other evaluation
     */
    public void incEvaltoMaxFor(Evaluation meval, boolean color) {
        eval.maxEvalPerFutureLevelFor(meval, color);
    }

    @Override
    public String toString() {
        return "" + super.toString()
                + "=" + eval.toString();
    }

    boolean isBetterForColorThan(boolean color, EvaluatedMove other) {
        boolean probablyBetter = eval.isBetterForColorThan( color, other.eval());
        if (DEBUGMSG_MOVEEVAL_COMPARISON) {
            debugPrintln(DEBUGMSG_MOVEEVAL_COMPARISON, "=> " + probablyBetter + ". ");
        }
        return probablyBetter;
    }


    /**
     * See if evMove is among the best, i.e. best or max maxTopEntries-st best, in the list sortedTopMoves.
     * If yes it is put there, else into restMoves. If sortedTopMoves grows too large, the too many lower ones are also moved to restMoves.
     * @param evMove move to be sorted in
     * @param sortedTopMoves top moves so far. sortedTopMoves needs to be sorted from the beginning (or empty).
     * @param color to determine which evaluations are better (higher or lower)
     * @param maxTopEntries the max nr of entries that should be in sortedTopMoves
     * @param restMoves is not sorted.
     * @return true if evMove is a new top move, false otherwise
     */
    static boolean addEvaluatedMoveToSortedListOfCol(EvaluatedMove evMove,
                                                     List<EvaluatedMove> sortedTopMoves,
                                                     boolean color, int maxTopEntries,
                                                     List<EvaluatedMove> restMoves) {
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
                return false;
            }
        }
        //it was best!!
        sortedTopMoves.add(0, evMove);
        // move lower rest if top list became too big
        while (sortedTopMoves.size() > maxTopEntries) {
            restMoves.add(
                sortedTopMoves.remove(maxTopEntries) );
        }
        return true;
    }

    public Evaluation eval() {
        return eval;
    }

    @Deprecated
    public int[] getRawEval() {
        return eval.getRawEval();
    }

    public int getEvalAt(int futureLevel) {
        return eval.getEvalAt(futureLevel);
    }

    public boolean isCheckGiving() {
        return isCheckGiving;
    }

    public void setIsCheckGiving() {
        isCheckGiving = true;
    }

    public void setIsCheckGiving(boolean isCheckGiving) {
        this.isCheckGiving = isCheckGiving;
    }

    @Deprecated
    public void setEval(int[] eval) {
        this.eval.copyFromRaw(eval);
    }

    public void initEval(int initValue) {
        eval.initEval(initValue);
    }

    public void changeEvalHalfWayTowards(int deltaToDraw) {
        eval.changeEvalHalfWayTowards(deltaToDraw);
    }


 /*   @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvaluatedMove)) return false;
        if (!super.equals(o)) return false;
        EvaluatedMove that = (EvaluatedMove) o;
        return getTarget() == that.getTarget() && isCheckGiving() == that.isCheckGiving() && Arrays.equals(getEval(), that.getEval());
    } */

    /*@Override
    public Integer hashId() {
        return super.hashId() + (getTarget()<<16);
    }*/

}
