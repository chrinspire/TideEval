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

import static de.ensel.tideeval.ChessBasics.colorName;
import static de.ensel.tideeval.ChessBasics.isWhite;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ChessBoard.debugPrintln;
import static java.lang.Math.*;

public class Evaluation {
    public static final int MAX_EVALDEPTH = ChessBoard.MAX_INTERESTING_NROF_HOPS + 1;
    private int[] rawEval;

    //// Constructors
    public Evaluation() {
        rawEval = new int[MAX_EVALDEPTH];
    }

    public Evaluation(Evaluation oeval) {
        this.rawEval = Arrays.copyOf(oeval.rawEval, MAX_EVALDEPTH);
    }

    public Evaluation(int eval, int futureLevel) {
        this.rawEval = new int[MAX_EVALDEPTH];
        setEval(eval, futureLevel);
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
                if (DEBUGMSG_MOVESELECTION)
                    debugPrint(DEBUGMSG_MOVESELECTION, " done@" + i + " ");
                probablyBetter = true;
                break;
            }
            else if (isWhite(color) ? rawEval[i] + bias - oEval.rawEval[i] < -(comparethreshold>>1) // - lowthreshold
                    : rawEval[i] + bias - oEval.rawEval[i] > (comparethreshold>>1) ) {
                if (DEBUGMSG_MOVESELECTION)
                    debugPrint(DEBUGMSG_MOVESELECTION, " done, worse@" + i + " ");
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
                if (DEBUGMSG_MOVESELECTION)
                    debugPrint(DEBUGMSG_MOVESELECTION, " positive /");
            }
            else if ( probablyALittleBetter
                    && (isWhite(color) ? rawEval[i] + bias - oEval.rawEval[i] < 0
                    : rawEval[i] + bias - oEval.rawEval[i] > 0) ) {
                probablyALittleBetter = false;
            }
            bias += (bias>>3) + rawEval[i]- oEval.rawEval[i];
            if (DEBUGMSG_MOVESELECTION)
                debugPrint(DEBUGMSG_MOVESELECTION, " similar@=" + i + " (bias="+bias+") " ); // + " " + Arrays.toString(eval) + ".");
            i++;  // almost same evals on the future levels so far, so continue comparing
        }
        if ( i >= MAX_EVALDEPTH && probablyALittleBetter == true ) {
            if (DEBUGMSG_MOVESELECTION)
                debugPrint(DEBUGMSG_MOVESELECTION, "-> almost same but slighly better ");
            probablyBetter = true;
        }
        if (DEBUGMSG_MOVESELECTION) {
            debugPrintln(DEBUGMSG_MOVESELECTION, "=> " + probablyBetter + ". ");
            DEBUGMSG_MOVESELECTION = false;
            boolean oppositeComparison = oEval.isBetterForColorThan(color, this);
            DEBUGMSG_MOVESELECTION = true;
            if (probablyBetter && oppositeComparison)
                debugPrintln(DEBUGMSG_MOVESELECTION, " X!X: "
                        + oEval + " isBetterFor " + colorName(color) + " than " + this
                        + " - but opposite comparison should not also be true!");
            else if (!probablyBetter && !oppositeComparison)
                debugPrintln(DEBUGMSG_MOVESELECTION, " X!X: "
                        + oEval + " isNOTBetterFor " + colorName(color) + " than " + this
                        + " - but opposite comparison should not also be false!");
        }
        return probablyBetter;
    }


    @Deprecated
    public Evaluation(int[] rawEval) {
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


    //// getter
    public int getEvalAt(int futureLevel) {
        return rawEval[futureLevel];
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
     */
    public void setEval(int evalValue, int futureLevel) {
        rawEval[futureLevel] = evalValue;
    }

    /**
     * adds or substracts to/from an eval on a certain future level
     * beware: is unchecked
     * @param evalValue
     * @param futureLevel the future level from 0..max
     */
    public void addEval(int evalValue, int futureLevel) {
        rawEval[futureLevel] += evalValue;
    }

    public void addEval(Evaluation addEval) {
        for (int i = 0; i < MAX_EVALDEPTH; i++)
            this.rawEval[i] += addEval.rawEval[i];
    }

    public void incEvaltoMaxFor(Evaluation meval, boolean color) {
        for (int i = 0; i < MAX_EVALDEPTH; i++) {
            this.rawEval[i] = isWhite(color) ? max(meval.rawEval[i], rawEval[i])
                                             : min(meval.rawEval[i], rawEval[i]);
        }
    }

    ////

    @Override
    public String toString() {
        return "" + Arrays.toString(rawEval);
    }
}