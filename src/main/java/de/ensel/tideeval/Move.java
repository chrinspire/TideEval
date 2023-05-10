/*
 * Copyright (c) 2022.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.Objects;

/** simple class to express a Chess move from a square position (0-63) to another one.
 *  Optionally the from or to position can be set to the placeholder ANY from ChessBasics.
 */
public class Move {
    protected int from;
    protected int to;

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

    @Override
    public String toString() {
        return "" +
                ChessBasics.squareName( from) +
                "-" + ChessBasics.squareName(to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return from == move.from && to == move.to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
