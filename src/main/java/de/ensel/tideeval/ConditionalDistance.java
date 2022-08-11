/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.MAX_INTERESTING_NROF_HOPS;

/**
 * Class used to store distances for a Figure coming from one direction.
 * it stores an unconditional an a conditional distance.
 * as Condition it stores a move with wither real from and to positions or ANY - matching always.
 * So a condition can be "Piece moves away from e4:  "e4,ANY"
 */
public class ConditionalDistance {
    public static final int INFINITE_DISTANCE = Integer.MAX_VALUE;
    public static final int FREE = -2;  // for no nogo

    private int dist;
    private final ArrayList<Condition> conds = new ArrayList<>(2);

    /**
     * distance has a no-go to move along that path (meaning it cannot go there without being beaten at the square).
     * nogo stores the first square on a path that is a no-go.
     * If nogo!=FREE then the distance is actually not valid.
     * (for now not a List, but just the first square that invalidated the path)
     */
    private int nogo = FREE;

    public ConditionalDistance() {
        reset();
    }

    public ConditionalDistance(final int dist) {
        setDistance(dist);
        resetConditions();
    }


    /**
     * Contructs new ConditionalDistance with already exactly one condition
     * @param dist distance
     * @param fromCond ANY or pos from where a piece needs to move away from
     * @param toCond ANY or pos where a piece needs to move to
     * @param colorCond what color needs to fulfil the condition
     */
    public ConditionalDistance(final int dist,
                               final int fromCond, final int toCond, final boolean colorCond) {
        setDistanceWithSingleCondition(dist, fromCond,toCond, colorCond, FREE);
    }

    /**
     * Contructs new ConditionalDistance with already exactly one condition
     * @param dist distance
     * @param fromCond ANY or pos from where a piece needs to move away from
     * @param toCond ANY or pos where a piece needs to move to
     * @param colorCond what color needs to fulfil the condition
     * @param nogo FREE or pos that is the reason for (one) NoGo.
     */
    public ConditionalDistance(final int dist,
                               final int fromCond, final int toCond,
                               final boolean colorCond,
                               final int nogo
                               ) {
        setDistanceWithSingleCondition(dist, fromCond,toCond, colorCond, nogo);
    }



    /**
     * Contructs new ConditionalDistance as copy of another plus an increase and an additional condition with color restriction to pieces from that color
     * @param baseDistance distance
     * @param fromCond ANY or square from where a piece needs to move away from
     * @param toCond ANY or square where a piece needs to move to
     * @param colorCond boolean expressing the color that the piece matching the condition has to have.
     */
    public ConditionalDistance(final ConditionalDistance baseDistance, final int inc,
                               final int fromCond, final int toCond, final boolean colorCond) {
        updateFrom(baseDistance);
        inc(inc);
        if (fromCond!=ANY || toCond!=ANY)
            conds.add(new Condition(fromCond,toCond,colorCond));
    }

    /**
     * Contructs new ConditionalDistance as copy of another plus an increase
     * @param baseDistance distance
     */
    public ConditionalDistance(final ConditionalDistance baseDistance, final int inc) {
        updateFrom(baseDistance);
        inc(inc);
    }

    public ConditionalDistance(final ConditionalDistance baseDistance) {
        updateFrom(baseDistance);
    }


    public void updateFrom(ConditionalDistance baseDistance) {
        setDistance(baseDistance.dist);
        resetConditions();
        for( Condition c : baseDistance.conds )
            conds.add(new Condition(c));
        this.nogo = baseDistance.nogo;
    }

    public void reset() {
        dist = INFINITE_DISTANCE;
        resetConditions();
    }

    public void resetConditions() {
        conds.clear();
        nogo = FREE;
    }

    public int getFromCond(int ci) {
        if (ci==0 && conds.size()==0)
            return ANY;
        assert(conds.size()>ci);
        return conds.get(ci).fromCond;
    }

    public int getToCond(int ci) {
        if (ci==0 && conds.size()==0)
            return ANY;
        assert(conds.size()>ci);
        return conds.get(ci).toCond;
    }

    public void addCondition(int fromCond, int toCond) {
        this.conds.add(new Condition(fromCond, toCond));
    }

    public void addCondition(int fromCond, int toCond, boolean colorCond) {
        this.conds.add(new Condition(fromCond, toCond, colorCond));
    }



    public void setNoGo(int nogo) {
        this.nogo = nogo;
    }

    public boolean hasNoGo() {
        return nogo!=FREE;
    }

    public void inc() {
        if (dist>=MAX_INTERESTING_NROF_HOPS)
            dist = INFINITE_DISTANCE;
        else // if (dist<INFINITE_DISTANCE)
            dist++;
    }

    public void inc(int inc) {
        assert(inc>=0);
        if (dist>MAX_INTERESTING_NROF_HOPS
                || dist+inc>MAX_INTERESTING_NROF_HOPS)
            dist = INFINITE_DISTANCE;
        else
            dist += inc;
    }

    private boolean hasFewerOrEqualConditionsThan(ConditionalDistance o) {
        return this.conds.size() <= o.conds.size();
    }

    private boolean hasFewerConditionsThan(ConditionalDistance o) {
        return this.conds.size() < o.conds.size();
    }

    /**
     * checks if this distance has stored any condition that needs a certain colors help to become possible
     * (currently this is only derived from the colIndexCond - this method relies on that this field is only filled
     * if an opponent needs to come there (e.g. to enable a pawn to beat it and thus move there)
     * @return if such a condition exists (somewhere on the way)
     */
    public boolean needsHelpFrom(boolean color) {
        int ci = colorIndex(color);
        for (Condition cond : conds)
            if (cond.colIndexCond==ci)
                return true;
        return false;
    }

    /**
     * same as needsHelpFrom(col), but returns Nr of helps needed and is able to ignore one square (e.g.
     * used to exclude the final destination itself, which would still be covered, although not directly
     * reachable by a pawn)
     * @return if such a condition exists (somewhere on the way)
     */
    public int countHelpNeededFromColorExceptOnPos(boolean color, int exceptPos) {
        int ci = colorIndex(color);
        int cnt = 0;
        for (Condition cond : conds)
            if (cond.colIndexCond==ci && cond.toCond!=exceptPos)
                cnt++;
        return cnt;
    }

    static class Condition {
        public int fromCond;
        public int toCond;
        public int colIndexCond = ANY;

        Condition(final int fromCond, final int toCond) {
            this.fromCond = fromCond;
            this.toCond = toCond;
        }
        Condition(final int fromCond, final int toCond, final boolean colorCond) {
            this.fromCond = fromCond;
            this.toCond = toCond;
            this.colIndexCond = colorIndex(colorCond);
        }
        Condition(final Condition baseCondition) {
            this.fromCond = baseCondition.fromCond;
            this.toCond = baseCondition.toCond;
            this.colIndexCond = baseCondition.colIndexCond;
        }

        @Override
        public String toString() {
            return "if{"
                    + (colIndexCond==ANY ? "" : colorName(colIndexCond)+':')
                    + (fromCond==ANY ? "any" : squareName(fromCond))
                    +'-' + (toCond==ANY ? "any" : squareName(toCond)) + '}';
        }

        public String getConditionDescription() {
            return "from " + (fromCond==ANY ? ( toCond!=ANY ? "opponent" :"any") : squareName(fromCond) )
                    + " to " + (toCond==ANY ? "any" : squareName(toCond) );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Condition other = (Condition) o;
            return this.toCond==other.toCond
                    && this.fromCond==other.fromCond
                    && this.colIndexCond==other.colIndexCond;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConditionalDistance other = (ConditionalDistance) o;
        ConditionalDistance me = (ConditionalDistance)this;
        return me.cdEquals(other)
                && me.conditionsEqual(other);
    }

    /** returns the "pure" distance 1:1 as stored - i.e. ignoring the conditions
     * and whether they are fulfilled or not or if it needs a move to fulfill them or not
     * @return distance as stored
     */
    public int dist() {
        return dist;
    }

    /** starts at dist(), but subtracts one dist for all conditions that
     * originally added one distance (like having to move away a same colored piece)
     * it also subtracts one for the opponents move, except the first one.
     * @param myColor boolean color: the methods needs to know which color conditions are own and which are opponent)
     * @return distance after all conditions are fulfilled
     */
    public int distWhenAllConditionsFulfilled(final boolean myColor) {
        // Todo: not nice here: the matching algorithm what it was increased is implemented in a totally different class (vPieces)
        int myColIndex = colorIndex(myColor);
        int oppColIndex = colorIndex(opponentColor(myColor));
        int d = dist;
        boolean firstCondition = true;
        for (Condition c : conds) {
            if (c.colIndexCond == myColIndex)
                d--;
            else if (c.colIndexCond == oppColIndex) {
                if (!firstCondition)
                    d--;  // it is not counted the first time, but later.
            }
            firstCondition = false;
        }
        return d;
    }

    private void setDistance(final int dist) {
        if (dist>MAX_INTERESTING_NROF_HOPS || dist<0)
            this.dist=INFINITE_DISTANCE;
        else
            this.dist = dist;
    }

    private void setDistanceWithSingleCondition(final int dist,
                                                final int fromCond,
                                                final int toCond,
                                                final boolean colorCond, final int nogo ) {
        setDistance(dist);
        resetConditions();
        if (fromCond!=ANY || toCond!=ANY)
            this.conds.add(new Condition(fromCond, toCond, colorCond));
        this.nogo = nogo;
    }


    public boolean cdEquals(final ConditionalDistance o) {
        return (o!=null
                && this.dist==o.dist
                && this.conds.size()==o.conds.size()
                && this.hasNoGo()==o.hasNoGo() );
    }


    /**
     * compares all stored conditions in order and also compares nogo (even for same square value)
     * @param o other CD to compare with
     * @return boolean if equal
     */
    private boolean conditionsEqual(final ConditionalDistance o) {
        if (this.nogo!=o.nogo)
            return false;
        if (o.conds.size()!=this.conds.size())
            return false;
        for (int i=0; i<conds.size(); i++)
            if (!o.conds.get(i).equals(this.conds.get(i)))
                return false;
        return true;
    }

    /**
     * compares the pure distance-value, the conditions of two distances.
     * It ao obeys the nogo flag. A distance without nogo (i.e. nogo==FREE) is
     * always shorter than one with a nogo, except it is Infinite.
     * @param o other ConditionalDistance to compare this one with
     * @return boolean comparison if smaller (but not equal)
     */
    public boolean cdIsSmallerThan(@NotNull final ConditionalDistance o) {
        if (o.isInfinite() && !this.isInfinite())
            return true;
        if (!this.hasNoGo() && o.hasNoGo())
            return true;
        if (this.hasNoGo() && !o.hasNoGo())
            return false;
        // if nogo-flags are equal (in a boolean sense) then compare distances
        return ( dist<o.dist
                || (dist==o.dist
                && conds.size()<o.conds.size() ) );
    }

    /**
     * cdIsSmallerOrEqualThan() compares the pure distance-value (not the conditions) of two distances.
     * But it obeys the nogo flag. A distance without nogo (i.e. nogo==FREE) is always shorter than one with a nogo
     * @param o other ConditionalDistance to compare this one with
     * @return boolean comparison if smaller or equal
     */
    public boolean cdIsSmallerOrEqualThan(@NotNull final ConditionalDistance o) {
        if (o.isInfinite() && !this.isInfinite())
            return true;
        if (!this.hasNoGo() && o.hasNoGo())
            return true;
        if (this.hasNoGo() && !o.hasNoGo())
            return false;
        // if nogo-flags are equal (in a boolean sense) then compare distances
        return dist<o.dist
               || (dist==o.dist
                   && conds.size()<=o.conds.size() );
    }

    /*old
    private boolean distIsSmallerOrEqualThan(@NotNull final ConditionalDistance o) {
        if (!this.hasNoGo() && o.hasNoGo())
            return true;
        if (this.hasNoGo() && !o.hasNoGo())
            return false;
        // if nogo-flags are equal (in a boolean sense) then compare distances
        return dist<=o.dist;
        // not here:  || (dist==o.dist) ) && conds.size()<=o.conds.size() ) );
        // replaced by cdIsSmallerOrEqualThan to make all comparing methods work the same way incl. dist, conditions and nogo-flag
    }
    */


    public boolean matches(final int testFrompos, final int testTopos) {
        for( Condition c : conds )
            if ( (c.fromCond==ANY || testFrompos==c.fromCond)
                    && (c.toCond==ANY || testTopos==c.toCond) )
                return true;
        return false;
    }

    public Condition matches(final Move m) {
        for( Condition c : conds )
            if ( (c.fromCond==ANY || m.from()==c.fromCond)
                    && (c.toCond==ANY || m.to()==c.toCond) )
                return c;
        return null;
    }

    /** similar to matches, but is only fulfilled, if all conditions are fulfilled by one or several moves
     * checks if "moves that happened up to now" trigger this Distance to become true (resp. be reduced, if it was
     * a piece of the same color and thus its move already counted in the distance...)
     * @param moves List<Move>s "moves that happened up to now"
     * @return int that tells after which move-nr (starting with 1) all conditions were fulfilled.
     *         if there are no conditions, it is always fulfilled, so it returns 0.
     *         returns <0 if it was not fully fulfilled. (-n where n is the number of remaining
     *         conditions)
     */
    public int movesFulfillConditions(List<Move> moves) {
        if (nrOfConditions()==0)
            return 0;
        List<Condition> cc = new ArrayList<>( conds);
        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);
            Condition cm = matches(m);
            if (cm != null) {
                cc.remove(cm);  // this condition matched, we take it out of the list.
                                    // Java-question: does this not break the original cond-list - is it a clean copy which's structure can be altered...
                if (cc.size()==0)  // it was the last match - nw all conditions are matched
                    return i+1;
            }
        }
        // we are through the list of moves, but unfulfilled conditions remain.
        return -cc.size();
        // TODO: rethink if conditional distance should really count other (own) moves, this makes this method much mor complicated...
    }

    /**
     * checks if a move fulfills a one-and-only condition, so that the distance becomes 1 unconditionally
     * @param testFrompos a piece goes away from here
     * @param testTopos and moves to there
     * @return boolean if it matched  (is also false if there are no conditions)
     */
    public boolean matchesOneAndOnlyCondition(final int testFrompos, final int testTopos) {
        if (conds.size()!=1)
            return false;
        Condition c = conds.get(0);
        return (c.fromCond==ANY || testFrompos==c.fromCond)
                    && (c.toCond==ANY || testTopos==c.toCond);
    }

    /**
     * checks if distance has a single (one-and-only) condition, that a piece (in the way) needs to move away
     * @return boolean if such a condition exists (is also false if there are no conditions)
     */
    public boolean hasExactlyOneFromToAnywhereCondition() {
        if (conds.size()!=1)
            return false;
        Condition c = conds.get(0);
        return (c.fromCond!=ANY);  // should be irrelevant, if a specific toCond is set, so no --&& c.toCond==ANY;
    }


    /**
     * checks if distance has a single (one-and-only) condition, that a piece (from anywhere) needs to move to my square
     * (this is needed for pawns, so they can move somewhere by beating something)
     * @return boolean if such a condition exists (is also false if there are no conditions)
     */
    public boolean hasExactlyOneFromAnywhereToHereCondition() {
        if (conds.size()!=1)
            return false;
        Condition c = conds.get(0);
        return (c.toCond!=ANY);  // should be irrelevant, if a specific toCond is set, so no --&& c.toCond==ANY;
    }

    /**
     * reduces this distance if parameter distance is smaller - so this distance becomes the minimum.
     * also takes the new values (incl. conditions) if the new d has the same value, but no or fewer conditions.
     * A distance with a nogo is always longer than a distance without nogo.
     * @param d : distance to compare this with
     * @return boolean if something has changed
     */
    public boolean reduceIfCdIsSmaller(ConditionalDistance d) {
        if ( d.cdIsSmallerThan(this) ) {
            updateFrom(d);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder( (dist==INFINITE_DISTANCE) ? "X"
                : (""+dist)+(hasNoGo()?" NoGo":" ok"));
        if (conds.size()>0) {
            for( Condition c : conds )
                res.append("&").append(c);
        }
        return res.toString();
    }

    public boolean isInfinite() {
        return (dist==INFINITE_DISTANCE);
    }

    public boolean isUnconditional() {
        return conds==null || conds.size()==0;
    }

    public int nrOfConditions() {
        if ( conds==null )
            return 0;
        return conds.size();
    }


}
