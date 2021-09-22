/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import static java.lang.Math.min;

/**
 * Class used to store distances for a Figure coming from one direction.
 * it stores an unconditional an a conditional distance.
 * as Condition it stores a move with wither real from and to positions or ANY - matching always.
 * So a condition can be "Piece moves away from e4:  "e4,ANY"
 */
public class Distance {
    public static final int ANY = -1;
    public static final int INFINITE_DISTANCE = Integer.MAX_VALUE;

    private int distUncond;
    private int fromCond;
    private int toCond;
    private int distCond;

    public int dist() {
        return distUncond;
    }
    public void setUnconditionalDistance(int dist) {
        distUncond = dist;
    }

    public int getShortestDistanceEvenUnderCondition() {
        return min(distUncond,distCond);
    }
    public void setDistanceUnderCondition(int fromCond, int toCond, int dist) {
        this.fromCond = fromCond;
        this.toCond = toCond;
        this.distCond = dist;
    }

    public int getFromCond() {
        return fromCond;
    }

    public int getToCond() {
        return toCond;
    }

    public Distance(int distanceUncoditional, int frompos, int topos, int distanceConditional) {
        this.distUncond = distanceUncoditional;
        this.fromCond = frompos;
        this.toCond = topos;
        this.distCond = distanceConditional;
    }

    public Distance(int distanceUncoditional) {
        this.distUncond = distanceUncoditional;
        fromCond = ANY;
        toCond = ANY;
        distCond = INFINITE_DISTANCE;
    }

    public Distance() {
        reset();
    }

    public Distance(Distance newDistance) {
        this.distUncond = newDistance.distUncond;
        this.fromCond   = newDistance.fromCond;
        this.toCond     = newDistance.toCond;
        this.distCond   = newDistance.distCond;
    }

    public void reset() {
        distUncond = INFINITE_DISTANCE;
        resetConditionalDistance();
    }

    public void resetConditionalDistance() {
        fromCond = ANY;
        toCond = ANY;
        distCond = INFINITE_DISTANCE;
    }

    public boolean matches(int testFrompos, int testTopos) {
        return (fromCond ==ANY || testFrompos== fromCond)
                && (toCond ==ANY || testTopos== toCond);
    }

    public boolean equals(final Distance o) {
        return (o!=null && distCond==o.distCond && fromCond==o.fromCond && toCond==o.toCond && distUncond==o.distUncond);
    }

    public boolean isSmallerOrEqual(@NotNull final Distance o) {
        return (  distUncond < o.dist()
                || ( distUncond <= o.dist()
                   && distCond <= o.getShortestDistanceEvenUnderCondition() ) );
    }

    public boolean isSmaller(@NotNull final Distance o) {
        return ( distUncond < o.dist() ) // && distCond <= o.getDistanceUnderCondition() )
                || ( distUncond <= o.dist() && distCond < o.getShortestDistanceEvenUnderCondition() );
    }

    public boolean isAtLeast2Smaller(@NotNull final Distance o) {
        return ( distUncond < o.dist()-1 ) // && distCond <= o.getDistanceUnderCondition() )
                || ( distUncond <= o.dist()-1 && distCond < o.getShortestDistanceEvenUnderCondition()-1 );
    }

    public void updateFrom(Distance newDistance) {
        this.distUncond = newDistance.distUncond;
        this.fromCond   = newDistance.fromCond;
        this.toCond     = newDistance.toCond;
        this.distCond   = newDistance.distCond;
    }

    /**
     * reduces this distance if parameter distance is smaller - so this distance becomes the minimum.
     * @param d : distance to compare this with
     * @return boolean is something has changed
     */
    public boolean reduceIfSmaller(Distance d) {
        if (d==null)
            return false;
        boolean hasChanged = false;
        int candidateDistance = d.dist();
        if ( candidateDistance < distUncond ) {
            distUncond = candidateDistance;
            hasChanged = true;
        }
        candidateDistance = d.getShortestDistanceEvenUnderCondition();
        if ( candidateDistance < distCond ) {
            setDistanceUnderCondition(
                    d.getFromCond(),
                    d.getToCond(),
                    candidateDistance);
            return true;
        }
        return hasChanged;
    }

    @Override
    public String toString() {
        return ( (distUncond==Integer.MAX_VALUE?"X":distUncond)
                +((getFromCond()==ANY || distCond==distUncond)?"":("/"+distCond )) );
    }

    public boolean isInfinite() {
        return (distUncond==INFINITE_DISTANCE && distCond==INFINITE_DISTANCE);
    }

    public boolean hasSmallerConditionalDistance(Distance o) {
        return ( distCond < o.getShortestDistanceEvenUnderCondition() );
    }

    public boolean hasEqualConditionalDistance(Distance o) {
        return distCond == o.getShortestDistanceEvenUnderCondition();
    }

    public boolean hasCondition() {
        return fromCond!=ANY && toCond!=ANY;
    }

    /*public Distance plus1Hop() {
        return new Distance(distUncond+1,fromCond,toCond);
    }*/

}
