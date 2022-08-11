/*
 * Copyright (c) 2022.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

/** simple class to express a Chess move from a square position (0-63) to another one.
 *  Optionally the from or to position can be set to the placeholder ANY from ChessBasics.
 */
public class Move {
    private int from;
    private int to;

    public Move(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int from() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int to() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}
