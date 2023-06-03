/*
 * Copyright (c) 2023.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.Objects;

import static de.ensel.tideeval.ChessBasics.*;

/**
 * provides storage and calculation regarding conditions for distances to be(come) valid
 */
class MoveCondition extends Move {
    public final int colIndexCond;

    MoveCondition(final int fromCond, final int toCond) {
        super(fromCond, toCond);
        colIndexCond = ANY;
    }

    MoveCondition(final int fromCond, final int toCond, final boolean colorCond) {
        super(fromCond, toCond);
        this.colIndexCond = colorIndex(colorCond);
    }

    MoveCondition(final MoveCondition baseCondition) {
        super(baseCondition.from(), baseCondition.to());
        this.colIndexCond = baseCondition.colIndexCond;
    }

    @Override
    public String toString() {
        return "if{"
                + (from() == ANY ? "any" : squareName(from()))
                + '-' + (to() == ANY ? "any" : squareName(to()))
                + (colIndexCond == ANY ? "" : " (" + colorName(colIndexCond) + ')')
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MoveCondition)) return false;
        if (!super.equals(o)) return false;
        MoveCondition condition = (MoveCondition) o;
        return colIndexCond == condition.colIndexCond;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), colIndexCond);
    }
}
