/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static de.ensel.tideeval.ChessBasics.squareName;

/**
 * Class used to store distances for a Figure coming from one direction.
 * it stores an unconditional an a conditional distance.
 * as Condition it stores a move with wither real from and to positions or ANY - matching always.
 * So a condition can be "Piece moves away from e4:  "e4,ANY"
 */
public class ConditionalDistance {
    public static final int ANY = -1;
    public static final int INFINITE_DISTANCE = Integer.MAX_VALUE;

    private int dist;
    private ArrayList<Condition> conds = new ArrayList<>(2);

    public ConditionalDistance() {
        reset();
    }

    public ConditionalDistance(int d) {
        dist = d;
        resetConditions();
    }

    /**
     * Contructs new ConditionalDistance with already exactly one condition
     * @param dist distance
     * @param fromCond ANY or square from where a piece needs to move away from
     * @param toCond ANY or square where a piece needs to move to
     */
    public ConditionalDistance(final int dist, final int fromCond, final int toCond) {
        setDistanceWithSingleCondition(dist, fromCond,toCond);
    }

    /**
     * Contructs new ConditionalDistance as copy of another plus an increase and an additional condition
     * @param baseDistance distance
     * @param fromCond ANY or square from where a piece needs to move away from
     * @param toCond ANY or square where a piece needs to move to
     */
    public ConditionalDistance(final ConditionalDistance baseDistance, final int inc, final int fromCond, final int toCond) {
        updateFrom(baseDistance);
        if (dist<INFINITE_DISTANCE)
            dist+=inc;
        if (fromCond!=ANY || toCond!=ANY)
            conds.add(new Condition(fromCond,toCond));
    }

    /**
     * Contructs new ConditionalDistance as copy of another plus an increase
     * @param baseDistance distance
     */
    public ConditionalDistance(final ConditionalDistance baseDistance, final int inc) {
        updateFrom(baseDistance);
        if (dist<INFINITE_DISTANCE)
            dist+=inc;
    }

    public ConditionalDistance(final ConditionalDistance baseDistance) {
        updateFrom(baseDistance);
    }

    public void updateFrom(ConditionalDistance baseDistance) {
        this.dist = baseDistance.dist;
        resetConditions();
        for( Condition c : baseDistance.conds )
            conds.add(new Condition(c));
    }

    public void reset() {
        dist = INFINITE_DISTANCE;
        resetConditions();
    }

    public void resetConditions() {
        conds.clear();
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

    static class Condition {
        public int fromCond = ANY;
        public int toCond = ANY;
        Condition(final int fromCond, final int toCond) {
            this.fromCond = fromCond;
            this.toCond = toCond;
        }
        Condition(final Condition baseCondition) {
            this.fromCond = baseCondition.fromCond;
            this.toCond = baseCondition.toCond;
        }
        @Override
        public String toString() {
            return "if{" + (fromCond==ANY ? "any" : squareName(fromCond))
                    +'-' + (toCond==ANY ? "any" : squareName(toCond)) + '}';
        }
        public String getConditionDescription() {
            return "from " + (fromCond==ANY ? ( toCond!=ANY ? "opponent" :"any") : squareName(fromCond) )
                    + " to " + (toCond==ANY ? "any" : squareName(toCond) );
        }

    }

    public int dist() {
        return dist;
    }
    public void setDistance(final int dist) {
        this.dist = dist;
    }

    public void setDistanceWithSingleCondition(final int dist, final int fromCond, final int toCond) {
        this.dist = dist;
        resetConditions();
        if (fromCond!=ANY || toCond!=ANY)
            this.conds.add(new Condition(fromCond, toCond));
    }

    public boolean distEquals(final ConditionalDistance o) {
        return (o!=null && dist==o.dist && conds.size()==o.conds.size());
    }

    /**
     * compare two distances, if they have the same value, the one with less conditions is considered smaller
     * @param o other ConditionalDistance to compare this one with
     * @return boolean comparison
     */
    public boolean distIsSmallerOrEqual(@NotNull final ConditionalDistance o) {
        return ( dist<o.dist
                || (dist==o.dist && conds.size()<=o.conds.size() ) );
    }

    public boolean distIsSmaller(@NotNull final ConditionalDistance o) {
        return ( dist<o.dist
                || (dist == o.dist && conds.size()<o.conds.size() ) );
    }


    public boolean matches(final int testFrompos, final int testTopos) {
        for( Condition c : conds )
            if ( (c.fromCond==ANY || testFrompos==c.fromCond)
                    && (c.toCond==ANY || testTopos==c.toCond) )
                return true;
        return false;
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
     * reduces this distance if parameter distance is smaller - so this distance becomes the minimum.
     * also takes the new values (i.e. takes away the conditions) if the new d has the same value, but no or fewer conditions.
     * @param d : distance to compare this with
     * @return boolean if something has changed
     */
    public boolean reduceIfSmaller(ConditionalDistance d) {
        if (d==null)
            return false;
        boolean hasChanged = false;
        if ( d.distIsSmaller(this) ) {
            updateFrom(d);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder( (dist==INFINITE_DISTANCE)?"X":""+dist);
        if (conds.size()>0) {
            for( Condition c : conds )
                res.append(" ").append(c);
        }
        return res.toString();
    }

    public boolean isInfinite() {
        return (dist==INFINITE_DISTANCE);
    }

    public boolean isUnconditional() {
        return conds==null || conds.size()==0;
    }

}
