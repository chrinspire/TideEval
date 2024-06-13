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

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static de.ensel.tideeval.ChessBasics.ANYWHERE;


public class EvalPerTargetAggregation extends AbstractCollection<Evaluation> {
    /**
     * K: target, E: Evaluation if myPiece continues to or covers target.
     */
    private HashMap<Integer, Evaluation> evalPerTarget;   // stores real moves (i.e. d==1) and the chances they have
            // on certain future-levels (thus the Array of relEvals within Evaluation)

    private final boolean color;  // color is needed to know how to aggregate move evaluations (board perspective:
                                  // where larger numbers are better for white, smaller is better for black)

    private Evaluation aggregatedEval = null;  // this is calculated as the sum of all perTarget Entries. It is cached once calculated in the getter, but reset with every change

    //// Constructor

    public EvalPerTargetAggregation(boolean color) {
        this.evalPerTarget = new HashMap<>(8);
        this.color = color;
    }

    public EvalPerTargetAggregation(EvalPerTargetAggregation o) {
        this.evalPerTarget = new HashMap<>(8);
        for (Map.Entry<Integer, Evaluation> e : o.evalPerTarget.entrySet()) {
            evalPerTarget.put(e.getKey(), new Evaluation(e.getValue()) );  // copy values
        }
        this.color = o.color;
    }

    public EvalPerTargetAggregation(int target, Evaluation eval, boolean color) {
        this.evalPerTarget = new HashMap<>(8);
        if (eval != null)
            evalPerTarget.put(target, new Evaluation(eval) );  // copy values
        this.color = color;
    }



    //// manipulation

    public boolean addMax(Evaluation eval, int target) {
        if (eval==null)
            return false;
        int origSize = this.size();
        Evaluation existingEval = getOrAddEvalForTarget(target);
        existingEval.maxEvalPerFutureLevelFor(eval, color());
        aggregatedEval = null;
        return origSize != this.size();
    }

    /**
     * defaults to eval.target() as discriminator
     * @param eval
     * @return whether new eval was added
     */
    @Override
    public boolean add(final Evaluation eval) {
        return add(eval, eval.getTarget());
    }

    public boolean add(final Evaluation eval, final int target) {
        if (eval==null)
            return false;
        final int origSize = this.size();
        getOrAddEvalForTarget(target)
                .addEval(eval);
        aggregatedEval = null;
        return origSize != this.size();
    }

    public boolean add(final int benefit, final int futureLevel, final int target) {
        final int origSize = this.size();
        getOrAddEvalForTarget(target)
                .addEval(benefit, futureLevel);
        aggregatedEval = null;
        return origSize != this.size();
    }

    /** aggregates another aggregation of evaluations into this one.
     * New targets are just taken over, same targets are maxed with existing one.
     * @param moreChances
     */
    public void aggregateIn(final EvalPerTargetAggregation moreChances) {
        aggregateIn(moreChances, false);
    }

    public void aggregateIn(final EvalPerTargetAggregation moreChances, boolean quarterOfPositivesOnly) {
        if (moreChances==null)
            return;
        for (Map.Entry<Integer, Evaluation> e : moreChances.evalPerTarget.entrySet()) {
            Evaluation existingEval = evalPerTarget.get(e.getKey());
            Evaluation eval = e.getValue();
            if (quarterOfPositivesOnly) {
                if (eval.isGoodForColor(color()))
                    eval = new Evaluation(eval).devideBy(4);
                else
                    eval = null;
            }
            if (eval != null ) {
                if (existingEval == null) {
                    // not found -> this is a new Evaluation
                    evalPerTarget.put(e.getKey(), eval);
                } else {
                    // same target, lat's take max
                    existingEval.maxEvalPerFutureLevelFor(eval, color());
                    // TODO!!! - needed to fix "swallowed" negative benfits=fees by max
                    // e.g. in "1r1qr1k1/2p1b2p/p1b2p2/1p1n1QpR/3P4/1B4NP/PP3PP1/R1B3K1 b - - 1 20, e7d6|a6a5"  // NOT e8f8 which makes it mateIn1
                    //  existingEval.incEvaltoMaxOrDecreaseFor(e.getValue(), color());  // 48h44p
                }
            }
        }
    }

    public void timeWarp(int futureLevelDelta) {
        for (Map.Entry<Integer, Evaluation> e : evalPerTarget.entrySet()) {
            e.getValue().timeWarp(futureLevelDelta);
        }
    }


    @Override
    public Iterator<Evaluation> iterator() {
        return evalPerTarget.values().iterator();
    }

    @Override
    public int size() {
        return evalPerTarget.size();
    }


    //// specialized getter

    public boolean color() {
        return color;
    }

    /**
     * get the one Evaluation that matches the target (=discriminator)
     * @param target - same value used to store the value
     * @return the one (single or aggregated) Evaluation
     */
    Evaluation getEvMove(int target) {
        return evalPerTarget.get(target);
    }


    ////

    /**
     * picks the Evaluation for a target. If it does not exist, it makes a new one (all 0 eval) and returns this.
     *
     * @param target - the already existing or new target discriminator
     * @return an Evaluation - always exists, is never null,  but may be fresh (an all 0 evaluation)
     */
    private Evaluation getOrAddEvalForTarget(int target) {
        Evaluation existingEval = evalPerTarget.get(target);
        if ( existingEval == null ) {
            // not found -> this is a new Evaluation
            Evaluation newEval = new Evaluation(target);
            evalPerTarget.put(target, newEval);
            return newEval;
        }
        return existingEval;
    }

    /**
     * smurf all evaluations (for all targets) together to one
     * @return one single evaluation, aggregating (actually just adding) all internally stored ones
     */
    public Evaluation getAggregatedEval() {
        if ( aggregatedEval != null ) {
            return aggregatedEval;
        }
        aggregatedEval = new Evaluation(ANYWHERE);
        for (Evaluation ev : this) {
            aggregatedEval.addEval(ev);
        }
        return aggregatedEval;
    }

    /** filter Evaluations to only those referring to target fTarget.
     *
     * @param fTarget the selector
     * @return new, similar but filtered EvalPerTargetAggregation
     */
    public EvalPerTargetAggregation filterTarget(final int fTarget) {
        return new EvalPerTargetAggregation(fTarget, evalPerTarget.get(fTarget), color());
    }
}