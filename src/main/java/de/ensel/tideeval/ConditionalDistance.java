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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.MAX_INTERESTING_NROF_HOPS;

/**
 * Class used to store distances for a Figure coming from one direction.
 * it stores an unconditional an a conditional distance.
 * as Condition it stores a move with wither real from and to positions or ANY - matching always.
 * So a condition can be "Piece moves away from e4:  "e4,ANY"
 */
public class ConditionalDistance {
    public static final int INFINITE_DISTANCE = Integer.MAX_VALUE/2-1;  // some room for accidental overflow errors, in case there is a bug in catching then explicitly (sorry)
    public static final int FREE = -2;  // for no nogo

    private int dist;

    private final ArrayList<MoveCondition> conds = new ArrayList<>(2);

    /**
     * distance has a no-go to move along that path (meaning it cannot go there without being beaten at the square).
     * nogo stores the first square on a path that is a no-go.
     * If nogo!=FREE then the distance is actually not valid.
     * (for now not a List, but just the first square that invalidated the path)
     */
    private int nogo = FREE;

    /**
     * holding the predecessor vPce (the square so to speak), where this distance comes from.
     * could be more than one equally distant predecessors, So Set is used.
     */
    private Set<VirtualPieceOnSquare> lastMoveOrigins;

    /** kind of the default Constructor, but one param back to it's origin.
     *  generates an infinite distance with no conditions
     */
    public ConditionalDistance(final VirtualPieceOnSquare lastMoveOrigin) {
        reset();
        setSingleLastMoveOrigin(lastMoveOrigin);
    }

    public ConditionalDistance(final VirtualPieceOnSquare lastMoveOrigin, final int dist) {
        setSingleLastMoveOrigin(lastMoveOrigin);
        setDistance(dist);
        resetConditions();
    }

    /**
     * Contructs new ConditionalDistance with already exactly one condition
     * @param lastMoveOrigin the origin, to be able to trace back to real Piece
     * @param dist distance
     * @param fromCond ANY or pos from where a piece needs to move away from
     * @param toCond ANY or pos where a piece needs to move to
     * @param colorCond what color needs to fulfil the condition
     */
    public ConditionalDistance(final VirtualPieceOnSquare lastMoveOrigin, final int dist,
                               final int fromCond, final int toCond, final boolean colorCond) {
        setDistanceWithSingleCondition(lastMoveOrigin, dist, fromCond,toCond, colorCond, FREE);
    }

    /**
     * Contructs new ConditionalDistance with already exactly one condition
     * @param lastMoveOrigin the origin, to be able to trace back to real Piece
     * @param dist distance
     * @param fromCond ANY or pos from where a piece needs to move away from
     * @param toCond ANY or pos where a piece needs to move to
     * @param colorCond what color needs to fulfil the condition
     * @param nogo last square that produced nogo
     */
    public ConditionalDistance(final VirtualPieceOnSquare lastMoveOrigin, final int dist,
                               final int fromCond, final int toCond, final boolean colorCond, final int nogo) {
        setDistanceWithSingleCondition(lastMoveOrigin, dist, fromCond,toCond, colorCond, nogo);
    }

    /**
     * Contructs new ConditionalDistance as copy of another plus an increase and an additional condition with color restriction to pieces from that color
     * @param baseDistance distance that is used as origin = copy source
     * @param inc increment of distance compared to baseDistance
     * @param fromCond ANY or square from where a piece needs to move away from
     * @param toCond ANY or square where a piece needs to move to
     * @param colorCond boolean expressing the color that the piece matching the condition has to have.
     */
    public ConditionalDistance(final ConditionalDistance baseDistance, final int inc,
                               final int fromCond, final int toCond, final boolean colorCond) {
        updateFrom(baseDistance);
        inc(inc);
        if (fromCond!=ANY || toCond!=ANY)
            conds.add(new MoveCondition(fromCond,toCond,colorCond));
    }

    public ConditionalDistance(final VirtualPieceOnSquare lastMoveOrigin,
                               final ConditionalDistance baseDistance, final int inc,
                               final int fromCond, final int toCond, final boolean colorCond) {
        updateFrom(baseDistance);
        inc(inc);
        if (fromCond!=ANY || toCond!=ANY)
            conds.add(new MoveCondition(fromCond,toCond,colorCond));
        setSingleLastMoveOrigin(lastMoveOrigin);
    }

    /**
     * Contructs new ConditionalDistance as copy of another
     * @param baseDistance distance
     */
    public ConditionalDistance(final ConditionalDistance baseDistance) {
        updateFrom(baseDistance);
    }

    /**
     * Contructs new ConditionalDistance as copy of another plus an increase
     * @param baseDistance distance
     */
    public ConditionalDistance(final ConditionalDistance baseDistance, final int inc) {
        updateFrom(baseDistance);
        inc(inc);
    }

    public ConditionalDistance(final VirtualPieceOnSquare lastMoveOrigin,
                               final ConditionalDistance baseDistance, final int inc) {
        updateFrom(baseDistance);
        inc(inc);
        setSingleLastMoveOrigin(lastMoveOrigin);
    }

    public void updateFrom(ConditionalDistance baseDistance) {
        setDistance(baseDistance.dist);
        resetConditions();
        for( MoveCondition c : baseDistance.conds )
            conds.add(new MoveCondition(c));
        this.nogo = baseDistance.nogo;
        setLastMoveOrigins(baseDistance.lastMoveOrigins);
    }

    public void reset() {
        dist = INFINITE_DISTANCE;
        resetConditions();
    }

    public void resetConditions() {
        conds.clear();
        nogo = FREE;
    }

    /**
     * get from-field of condition nr condi
     * @param condi
     * @return  returns position from whre a piece needs to move to enable this distance or ANY if this is not a from condition
     */
    public int getFromCond(final int condi) {
        if (condi==0 && conds.size()==0)
            return ANY;
        assert(conds.size()>condi);
        return conds.get(condi).from();
    }

    public List<Integer> getFromConds() {
        List<Integer> result = new ArrayList<>();
        for (MoveCondition c : conds)
            if (c.from() !=ANY)
                result.add(c.from());
        return result;
    }

    public int getToCond(final int ci) {
        if (ci==0 && conds.size()==0)
            return ANY;
        assert(conds.size()>ci);
        return conds.get(ci).to;
    }

    public void addCondition(final int fromCond, final int toCond) {
        this.conds.add(new MoveCondition(fromCond, toCond));
    }

    public void addCondition(final int fromCond,
                             final int toCond,
                             final boolean colorCond) {
        this.conds.add(new MoveCondition(fromCond, toCond, colorCond));
    }


    public void inc() {
        if (dist>=MAX_INTERESTING_NROF_HOPS)
            dist = INFINITE_DISTANCE;
        else // if (dist<INFINITE_DISTANCE)
            dist++;
    }

    public void inc(final int inc) {
        assert(inc>=0);
        if ( inc>MAX_INTERESTING_NROF_HOPS
                || dist>MAX_INTERESTING_NROF_HOPS
                || dist+inc>MAX_INTERESTING_NROF_HOPS)
            dist = INFINITE_DISTANCE;
        else
            dist += inc;
    }

    /**
     * checks if this distance has stored any condition that needs a certain colors help to become possible
     * (currently this is only derived from the colIndexCond - this method relies on that this field is only filled
     * if an opponent needs to come there (e.g. to enable a pawn to beat it and thus move there)
     * @return if such a condition exists (somewhere on the way)
     */
    public boolean needsHelpFrom(boolean color) {
        int ci = colorIndex(color);
        for (MoveCondition cond : conds)
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
    public int countHelpNeededFromColorExceptOnPos(final boolean color, final int exceptPos) {
        int ci = colorIndex(color);
        int cnt = 0;
        for (MoveCondition cond : conds)
            if (cond.colIndexCond==ci && cond.to !=exceptPos)
                cnt++;
        return cnt;
    }

    /**
     * if dist is >0 and <= MAX_INTERESTING...
     * @return
     */
    public boolean distIsNormal() {
        return  dist()>0
                && dist()<=MAX_INTERESTING_NROF_HOPS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConditionalDistance other = (ConditionalDistance) o;
        return this.cdEquals(other)
                && this.conditionsEqual(other);
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
        for (MoveCondition c : conds) {
            if (c.colIndexCond == myColIndex)
                d--;  // Todo!: This is wromg if more than 1 was added. It seems the inc needs to be stored with the condition...
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
            this.dist = INFINITE_DISTANCE;
        else
            this.dist = dist;
    }

    private void setDistanceWithSingleCondition(final VirtualPieceOnSquare lastMoveOrigin,
                                                final int dist,
                                                final int fromCond,
                                                final int toCond,
                                                final boolean colorCond, final int nogo ) {
        setSingleLastMoveOrigin(lastMoveOrigin);
        setDistance(dist);
        resetConditions();
        if (fromCond!=ANY || toCond!=ANY)
            this.conds.add(new MoveCondition(fromCond, toCond, colorCond));
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
                    && nrOfConditions()<o.nrOfConditions() ) );
    }

    public boolean cdIsEqualButDifferentSingleCondition(@NotNull final ConditionalDistance o) {
        if ( this.dist() != o.dist()
                || this.hasNoGo() != o.hasNoGo()
                || this.nrOfConditions() != o.nrOfConditions()
        )
            return false;
        // everything the same, even same nr of conditions
        // lets return true, if both have a single, but different condition (to encourage updates in these cases)
        if (nrOfConditions()!=1 || this.conds.get(0).equals(o.conds.get(0)) )
            return false;
        return true;
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
                   && nrOfConditions()<=o.nrOfConditions() );
    }

    public boolean cdEqualDistButNogo(final ConditionalDistance o) {
        if (this.nogo==FREE || o.nogo!=FREE)
            return false;
        return this.dist == o.dist;
    }


    public MoveCondition matches(final Move m) {
        for( MoveCondition c : conds )
            if ( (c.from() ==ANY || m.from()==c.from())
                    && (c.to() ==ANY || m.to()==c.to()) )
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
    public int movesFulfillConditions(final List<Move> moves) {
        if (nrOfConditions()==0)
            return 0;
        List<MoveCondition> cc = new ArrayList<>( conds);
        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);
            MoveCondition cm = matches(m);
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

    public boolean piecesMovesMayFulfillAllFromConds(List<VirtualPieceOnSquare> whites, List<VirtualPieceOnSquare> blacks) {
        if (nrOfConditions()==0)
            return true;
        for (int i=0; i<conds.size(); i++) {
            int fromCond = conds.get(i).from();
            if (fromCond==ANY
                    || atLeastOnePiecesMoveMayFulfillFromCond(whites, fromCond)
                    || atLeastOnePiecesMoveMayFulfillFromCond(blacks, fromCond)
            ) {
                continue;  // this condition might be fulfilled by one of the pieecs moving away
            } else
                return false;  // this condition could not be fulfilled by any piece
        }
        return true;
   }

    static private boolean atLeastOnePiecesMoveMayFulfillFromCond(List<VirtualPieceOnSquare> vPieces, int fromCond) {
        for (VirtualPieceOnSquare vPce : vPieces)
            if (vPce.getMyPiecePos() == fromCond)
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
        MoveCondition c = conds.get(0);
        return (c.from() ==ANY || testFrompos==c.from())
                    && (c.to() ==ANY || testTopos==c.to());
    }

    /**
     * checks if distance has a single (one-and-only) condition, that a piece (in the way) needs to move away
     * @return boolean if such a condition exists (is also false if there are no conditions)
     */
    public boolean hasExactlyOneFromToAnywhereCondition() {
        if (conds.size()!=1)
            return false;
        MoveCondition c = conds.get(0);
        return (c.from() !=ANY);  // should be irrelevant, if a specific toCond is set, so no --&& c.toCond==ANY;
    }


    /**
     * checks if distance has a single (one-and-only) condition, that a piece (from anywhere) needs to move to my square
     * (this is needed for pawns, so they can move somewhere by beating something)
     * @return boolean if such a condition exists (is also false if there are no conditions)
     */
    public boolean hasExactlyOneFromAnywhereToHereCondition() {
        if (conds.size()!=1)
            return false;
        MoveCondition c = conds.get(0);
        return (c.to !=ANY);  // should be irrelevant, if a specific toCond is set, so no --&& c.toCond==ANY;
    }

    /**
     * reduces this distance if parameter distance is smaller - so this distance becomes the minimum.
     * also takes the new values (incl. conditions) if the new d has the same value, but no or fewer conditions.
     * A distance with a nogo is always longer than a distance without nogo.
     * @param d : distance to compare this with
     * @return boolean if something has changed
     */
    public boolean reduceIfCdIsSmaller(ConditionalDistance d) {
        if ( d.cdIsSmallerThan(this)
            || d.cdIsEqualButDifferentSingleCondition(this)
        ) {
            updateFrom(d);
            return true;
        }
        return false;
    }

    /**
     * like reduceIfCdIsSmaller(), but adds lastMoveOrigins of d if distances are equal.
     * @param d : distance to compare this with
     * @return boolean if something has changed
     */
    public boolean reduceIfCdIsSmallerOrAddLastMOIfEqual(ConditionalDistance d) {
        if ( reduceIfCdIsSmaller(d) )
            return true;
        if ( cdEquals(d) ) {  // means: d and this are of EQUAL distance, so d's origins are also relevant
            addLastMoveOrigins(d.getLastMoveOrigins());
            return true;  // does this provoke too many updates?
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder( (dist==INFINITE_DISTANCE) ? "X"
                : (""+dist)+(hasNoGo()?" NoGo":" ok"));
        if (conds.size()>0) {
            for( MoveCondition c : conds )
                res.append("&").append(c);
        }
        return res.toString();
    }

    public boolean isInfinite() {
        return (dist>=INFINITE_DISTANCE);
    }

    public boolean isUnconditional() {
        return nrOfConditions()==0;  //conds==null || conds.size()==0;
    }

    public int nrOfConditions() {
        if ( conds==null )
            return 0;
        return conds.size();  //conds.stream().filter(c -> c.who==null ).count());
    }


    //// getter

    public VirtualPieceOnSquare oneLastMoveOrigin() {
        if (lastMoveOrigins==null || lastMoveOrigins.isEmpty())
            return null;
        return lastMoveOrigins.iterator().next();
    }

    public Set<VirtualPieceOnSquare> getLastMoveOrigins() {
        if (lastMoveOrigins==null)
            lastMoveOrigins = new HashSet<>(2);
        return lastMoveOrigins;
    }

    public void setLastMoveOrigins(Set<VirtualPieceOnSquare> lastMoveOrigins) {
        this.lastMoveOrigins = new HashSet<>(lastMoveOrigins);
    }

    public MoveCondition getConds(int nr) {
        return conds.get(nr);
    }

    public boolean hasNoGo() {
        return nogo!=FREE;
    }

    public int getNoGo() {
        return nogo;
    }


    /** returns the "pure" distance 1:1 as stored - i.e. ignoring the conditions
     * and whether they are fulfilled or not or if it needs a move to fulfill them or not
     * @return distance as stored
     */
    public int dist() {
        return dist;
    }


    //// setter

    public void setSingleLastMoveOrigin(VirtualPieceOnSquare lastMoveOrigin) {
        this.lastMoveOrigins = new HashSet<>(2);
        this.lastMoveOrigins.add(lastMoveOrigin);
    }

    /**
     * adds more move origins
     * @param moreLastMoveOrigins the set to add to my set
     * @return boolean if something was added (or everything already known)
     */
    public boolean addLastMoveOrigins(Set<VirtualPieceOnSquare> moreLastMoveOrigins) {
        int nr = this.lastMoveOrigins.size();
        this.lastMoveOrigins.addAll(moreLastMoveOrigins);
        return this.lastMoveOrigins.size() > nr;
    }

    public void setNoGo(final int nogo) {
        this.nogo = nogo;
    }

}
