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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class EvaluatedMovesCollection extends AbstractCollection<EvaluatedMove> {
    /**
     * chances (or risks) for a certain move of the Piece - more or less a collection of the chances of its vPces with d==1.
     * All variants of the moves (like for different targets etc) are "collapsed" i.e. summed up.
     * for fast access, the Hashmap key is the to-square.
     * K: to, E: EvaluatedMove to move my Piece from myPos to to.
     */
    private HashMap<Integer, EvaluatedMove> evMoves;   // stores real moves (i.e. d==1) and the chances they have on certain future-levels (thus the Array of relEvals)public EvaluatedMovesCollection() {

    private final boolean color;  // color is needed to know how to aggregate move evaluations (board perspective:

    public EvaluatedMovesCollection(boolean color) {
        this.evMoves = new HashMap<>(8);;
        this.color = color;
    }
    // larger number are better for white, smaller is better for black)

    Collection<EvaluatedMove> getAllEvMoves() {
        if (evMoves == null || evMoves.isEmpty())
            return null;
        return evMoves.values();
    }

    /**
     * get the one EvaluatedMove that matches the discriminator
     * @param discriminator - same value used to store the value
     * @return the one (single or aggregated) EvaluatedMove
     */
    EvaluatedMove getEvMove(int discriminator) {
        return evMoves.get(discriminator);
    }

    /**
     * defaults to em.to() as discriminator
     * @param em
     * @return
     */
    public boolean addMax(EvaluatedMove em) {
        return addMax(em, em.to());
    }

    public boolean addMax(EvaluatedMove em, int discriminator) {
        EvaluatedMove existingEm = evMoves.get(discriminator);
        if ( existingEm == null ) {
            // not found -> this is a new move
            evMoves.put(discriminator, em);
        }
        else {
            // we already had an evaluation for the same move-to
            existingEm.incEvaltoMaxFor(em.eval(), color());
        }
        return true;
    }


    @Override
    /**
     * defaults to em.to() as discriminator
     * @param em
     * @return
     */
    public boolean add(EvaluatedMove em) {
        return add(em, em.to());
    }

    public boolean add(EvaluatedMove em, int discriminator) {
        if (em==null)
            return false;

        EvaluatedMove existingEm = evMoves.get(discriminator);
        if ( existingEm == null ) {
            // not found -> this is a new move
            evMoves.put(discriminator, em);
        }
        else {
            // we already had an evaluation for the same move-to
            existingEm.addEval(em.eval());
        }
        return true;
    }

    @Override
    public Iterator<EvaluatedMove> iterator() {
        return evMoves.values().iterator();
    }

    @Override
    public int size() {
        return evMoves.size();
    }

    //// specialized getters

    protected boolean color() {
        return color;
    }

}