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

import java.util.Objects;

import static de.ensel.chessbasics.ChessBasics.*;

/**
 * provides storage and calculation regarding conditions for distances to be(come) valid
 */
class MoveCondition extends Move {
    public final int colIndexCond;

    MoveCondition(final int fromCond, final int toCond) {
        super(fromCond, toCond);
        colIndexCond = ANYWHERE;
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
                + (from() == ANYWHERE ? "any" : squareName(from()))
                + '-' + (to() == ANYWHERE ? "any" : squareName(to()))
                + (colIndexCond == ANYWHERE ? "" : " (" + colorName(colIndexCond) + ')')
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
