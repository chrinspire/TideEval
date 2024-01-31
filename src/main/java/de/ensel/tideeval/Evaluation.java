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

import java.util.Arrays;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ChessBoard.debugPrintln;
import static java.lang.Math.*;

/** provides an Evaluation for a Piece going towards a target.
 * The evaluation representation is an array of ints (typically used as centipawns) in board
 * perspective for [now, in one move, in 2 moves,...] called future levels.
 * If the target equals the square the Evaluation is used on, it typically means "if the Piece goes here",
 * otherwise if the piece goes here to achieve something at the target (like covering). This helps to not
 * add up evaluations where the same goal can be achieved alternatively via several ways.
 */
public class Evaluation {
    public static final int MAX_EVALDEPTH = ChessBoard.MAX_INTERESTING_NROF_HOPS + 1;
    private int[] rawEval;

    private final int target;

    //// Constructors
    public Evaluation(int target) {
        this.target = target;
        rawEval = new int[MAX_EVALDEPTH];
    }

    public Evaluation(Evaluation oeval) {
        this.target = oeval.target;
        this.rawEval = Arrays.copyOf(oeval.rawEval, MAX_EVALDEPTH);
    }

    public Evaluation(int eval, int futureLevel, int target) {
        this.target = target;
        this.rawEval = new int[MAX_EVALDEPTH];
        setEval(eval, futureLevel);
    }

    @Deprecated
    public Evaluation(int[] rawEval, int target) {
        this.target = target;
        this.rawEval = Arrays.copyOf(rawEval, MAX_EVALDEPTH);
    }

    @Deprecated
    public int[] getRawEval() {
        return rawEval;
    }

    @Deprecated
    public void copyFromRaw(int[] rawEval) {
        this.rawEval = Arrays.copyOf(rawEval, MAX_EVALDEPTH);
    }


    ////
    boolean isBetterForColorThan(boolean color, Evaluation oEval) {
        int i = 0;
        //if (DEBUGMSG_MOVESELECTION)
        //    debugPrint(DEBUGMSG_MOVESELECTION, "  comparing move eval " + this + " at "+i + " with " + oEval +": ");
        int comparethreshold = 36; // 23 -> 34 -> 51
        int bias = isWhite(color) ? -4 : +4;
        boolean probablyBetter = false;
        boolean probablyALittleBetter = true;
        while (i < MAX_EVALDEPTH) {
            if (i==1)
                comparethreshold += 12;
            else if (i==2)
                comparethreshold += 8;
            else if (i==3)
                comparethreshold += 9;
            if (isWhite(color) ? rawEval[i] + bias - oEval.rawEval[i] > comparethreshold
                    : rawEval[i] + bias - oEval.rawEval[i] < -comparethreshold) {
                if (DEBUGMSG_MOVEEVAL_COMPARISON)
                    debugPrint(DEBUGMSG_MOVEEVAL_COMPARISON, " done@" + i + " ");
                probablyBetter = true;
                break;
            }
            else if (isWhite(color) ? rawEval[i] + bias - oEval.rawEval[i] < -(comparethreshold>>1) // - lowthreshold
                    : rawEval[i] + bias - oEval.rawEval[i] > (comparethreshold>>1) ) {
                if (DEBUGMSG_MOVEEVAL_COMPARISON)
                    debugPrint(DEBUGMSG_MOVEEVAL_COMPARISON, " done, worse@" + i + " ");
                probablyBetter = false;
                probablyALittleBetter = false;
                break;
            }
            else if (isWhite(color) ? rawEval[i] + bias - oEval.rawEval[i] > (comparethreshold >> 1)
                    : rawEval[i] + bias - oEval.rawEval[i] < -(comparethreshold >> 1)) {
                probablyBetter = true;
                // tighten comparethreshold more if it was almost a full hit and leave it almost the same if it was close to similar
                // u76-u115: comparethreshold -= (comparethreshold>>2);
                comparethreshold -= ( abs(rawEval[i]- oEval.rawEval[i]) - (comparethreshold>>1) );
                if (DEBUGMSG_MOVEEVAL_COMPARISON)
                    debugPrint(DEBUGMSG_MOVEEVAL_COMPARISON, " positive /");
            }
            else if ( probablyALittleBetter
                    && (isWhite(color) ? rawEval[i] + bias - oEval.rawEval[i] < 0
                    : rawEval[i] + bias - oEval.rawEval[i] > 0) ) {
                probablyALittleBetter = false;
            }
            bias += (bias>>3) + rawEval[i]- oEval.rawEval[i];
            if (DEBUGMSG_MOVEEVAL_COMPARISON)
                debugPrint(DEBUGMSG_MOVEEVAL_COMPARISON, " similar@=" + i + " (bias="+bias+") " ); // + " " + Arrays.toString(eval) + ".");
            i++;  // almost same evals on the future levels so far, so continue comparing
        }
        if ( i >= MAX_EVALDEPTH && probablyALittleBetter == true ) {
            if (DEBUGMSG_MOVEEVAL_COMPARISON)
                debugPrint(DEBUGMSG_MOVEEVAL_COMPARISON, "-> almost same but slighly better ");
            probablyBetter = true;
        }
        if (DEBUGMSG_MOVEEVAL_COMPARISON) {
            debugPrintln(DEBUGMSG_MOVEEVAL_COMPARISON, "=> " + probablyBetter + ". ");
            DEBUGMSG_MOVEEVAL_COMPARISON = false;
            boolean oppositeComparison = oEval.isBetterForColorThan(color, this);
            DEBUGMSG_MOVEEVAL_COMPARISON = true;
            if (probablyBetter && oppositeComparison)
                debugPrintln(DEBUGMSG_MOVEEVAL_COMPARISON, " X!X: "
                        + oEval + " isBetterFor " + colorName(color) + " than " + this
                        + " - but opposite comparison should not also be true!");
            else if (!probablyBetter && !oppositeComparison)
                debugPrintln(DEBUGMSG_MOVEEVAL_COMPARISON, " X!X: "
                        + oEval + " isNOTBetterFor " + colorName(color) + " than " + this
                        + " - but opposite comparison should not also be false!");
        }
        return probablyBetter;
    }


    //// getter
    public int getEvalAt(int futureLevel) {
        return rawEval[futureLevel];
    }

    public int getTarget() {
        return target;
    }

    //// setter + advanced setter
    public void initEval(int initEval) {
        Arrays.fill(rawEval, initEval);
    }

    /**
     * sets an eval on a certain future level
     * beware: range is unchecked
     * @param evalValue
     * @param futureLevel the future level from 0..max
     * @return itself (but changed)
     */
    public Evaluation setEval(int evalValue, int futureLevel) {
        rawEval[futureLevel] = evalValue;
        return this;
    }

    /**
     * adds or substracts to/from an eval on a certain future level
     * beware: is unchecked
     * @param evalValue
     * @param futureLevel the future level from 0..max
     */
    public Evaluation addEval(int evalValue, int futureLevel) {
        rawEval[futureLevel] += evalValue;
        return this;
    }

    public Evaluation addEval(Evaluation addEval) {
        if (addEval != null) {
            for (int i = 0; i < MAX_EVALDEPTH; i++)
                this.rawEval[i] += addEval.rawEval[i];
        }
        return this;
    }

    public Evaluation subtractEval(Evaluation addEval) {
        if (addEval != null) {
            for (int i = 0; i < MAX_EVALDEPTH; i++)
                this.rawEval[i] -= addEval.rawEval[i];
        }
        return this;
    }

    public Evaluation incEvaltoMaxFor(Evaluation meval, boolean color) {
        if (meval != null) {
            for (int i = 0; i < MAX_EVALDEPTH; i++) {
                this.rawEval[i] = maxFor(meval.rawEval[i], rawEval[i], color);
            }
        }
        return this;
    }

    public Evaluation decEvaltoMinFor(Evaluation meval, boolean color) {
        if (meval != null) {
            for (int i = 0; i < MAX_EVALDEPTH; i++) {
                this.rawEval[i] = minFor(meval.rawEval[i], rawEval[i], color);
            }
        }
        return this;
    }

    public Evaluation onlyBeneficialFor(boolean color) {
        for (int i = 0; i < MAX_EVALDEPTH; i++) {
            if ( !evalIsOkForColByMin(rawEval[i], color, 0) )
                rawEval[i] = 0;
        }
        return this;
    }

    public Evaluation changeEvalHalfWayTowards(int towardsValue) {
        for (int i = 0; i < MAX_EVALDEPTH; i++)
            rawEval[i] = (towardsValue + rawEval[i]) >> 1;
        return this;
    }

    /** shifts evaluation timewise - note: the values at the arrays borders can fall off the edge of their world.
     * Shift left followed by shift right does not result in the original evaluation, but clears the value at futurelevel 0.
     * @param futureLevelDelta negative value = shift left, e.g. -1: every eval comes one move closer.
     *                        positive value = shift right, e.g. +1: every eval is postponed one move into the future.
     * @return this (but changed)
     */
    public Evaluation timeWarp(int futureLevelDelta) {
        if (futureLevelDelta<0) {
            if (futureLevelDelta<-MAX_EVALDEPTH)
                futureLevelDelta = -MAX_EVALDEPTH;
            for (int i = 0; i < MAX_EVALDEPTH; i++) {
                rawEval[i] = i >= MAX_EVALDEPTH+futureLevelDelta ? 0 : rawEval[i-futureLevelDelta];
            }
        }
        else if (futureLevelDelta>0) {
            if (futureLevelDelta>MAX_EVALDEPTH)
                futureLevelDelta = MAX_EVALDEPTH;
            for (int i = MAX_EVALDEPTH-1; i>=0; i--) {
                rawEval[i] = i < futureLevelDelta ? 0 : rawEval[i-futureLevelDelta];
            }
        }
        return this;
    }

    /** devide all evals on all future levels by div
     * @param div divisor
     * @return this (but changed)
     */
    public Evaluation devideBy(int div) {
        int shiftBy = switch (div) {   // hoping this is faster than calculating
            case 2 -> 1;
            case 4 -> 2;
            case 8 -> 3;
            default -> 0;
        };
        for (int i = 0; i < MAX_EVALDEPTH; i++) {
            rawEval[i] = (shiftBy>0) ? (rawEval[i]>>shiftBy)    // has anyone tried if this is really faster :-) or are these just my old 8088-without-FPU instincts?
                                     : (rawEval[i] / div);
        }
        return this;
    }

    ////
    @Override
    public String toString() {
        int firstEntry = MAX_EVALDEPTH;
        for (int i = 0; i < MAX_EVALDEPTH; i++) {
            if (rawEval[i] != 0) {
                if (firstEntry == MAX_EVALDEPTH)
                    firstEntry = i;
                else {
                    firstEntry = -1;  // meaning multiple entries found
                    break;
                }
            }
        }
        if (firstEntry == -1)
            return "" + Arrays.toString(rawEval) + (target==ANYWHERE ? "" : "$" + squareName(getTarget()));
        if (firstEntry == MAX_EVALDEPTH)
            return "[]";
        return "["+rawEval[firstEntry]+"@"+firstEntry+"]" + (target==ANYWHERE ? "" : "$" + squareName(getTarget()));
    }

}