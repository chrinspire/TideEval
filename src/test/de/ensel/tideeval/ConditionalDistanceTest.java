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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.ensel.tideeval.ChessBasics.ANY;
import static de.ensel.tideeval.ChessBasics.WHITE;
import static de.ensel.tideeval.ChessBoard.debugPrintln;
import static de.ensel.tideeval.ConditionalDistance.FREE;
import static org.junit.jupiter.api.Assertions.*;

class ConditionalDistanceTest {

    @Test
    void set_has_NoGo_Test() {
        ConditionalDistance d = new ConditionalDistance((VirtualPieceOnSquare)null, 3,20,ANY, WHITE);
        assertFalse( d.hasNoGo() );

        d.setNoGo(18);
        assertTrue( d.hasNoGo() );

        d.setNoGo(FREE);
        assertFalse( d.hasNoGo() );

        ConditionalDistance d2 = new ConditionalDistance((VirtualPieceOnSquare)null, 3,20,ANY, WHITE, 22);
        assertTrue( d2.hasNoGo() );
        d2.setNoGo(FREE);
        assertFalse( d2.hasNoGo() );

        ConditionalDistance d3 = new ConditionalDistance((VirtualPieceOnSquare)null, 3,20,ANY, WHITE, FREE );
        assertFalse( d3.hasNoGo() );
    }

    @Test
    void inc_Test() {
        ConditionalDistance d = new ConditionalDistance((VirtualPieceOnSquare)null, 2,ANY,20, WHITE);
        assertEquals(2, d.dist());
        d.inc();
        assertEquals(3, d.dist());
    }

    @Test
    void hasFewerOrEqualConditionsThan() {
        ConditionalDistance d1 = new ConditionalDistance((VirtualPieceOnSquare)null, 1);
        ConditionalDistance d2 = new ConditionalDistance((VirtualPieceOnSquare)null, 2);
        ConditionalDistance d2c = new ConditionalDistance((VirtualPieceOnSquare)null, 2, 2, ANY, WHITE);
        ConditionalDistance d3 = new ConditionalDistance((VirtualPieceOnSquare)null, 3);
        // TODO
    }

    @Test
    void hasFewerConditionsThan_Test() {
        //TODO
    }

    @Test
    void needsHelpFrom_Test() {
        //TODO
    }

    @Test
    void countHelpNeededFromColorExceptOnPos_Test() {
        //TODO
    }

    @Test
    void cdIsSmallerThan_Test() {
        ConditionalDistance d1 = new ConditionalDistance((VirtualPieceOnSquare)null, 1);
        ConditionalDistance d2 = new ConditionalDistance((VirtualPieceOnSquare)null, 2);
        ConditionalDistance d2c = new ConditionalDistance((VirtualPieceOnSquare)null, 2, 2, ANY, WHITE);
        ConditionalDistance d3 = new ConditionalDistance((VirtualPieceOnSquare)null, 3);
        assertTrue(d1.cdIsSmallerThan(d2));
        assertTrue(d1.cdIsSmallerThan(d2c));
        assertTrue(d1.cdIsSmallerThan(d3));
        assertFalse(d2.cdIsSmallerThan(d1));
        assertFalse(d2.cdIsSmallerThan(d2));
        assertTrue(d2.cdIsSmallerThan(d2c)); // smaller taking nr of conditions into account
        assertTrue(d2.cdIsSmallerThan(d3));
        assertFalse(d2c.cdIsSmallerThan(d1));
        assertFalse(d2c.cdIsSmallerThan(d2));
        assertFalse(d2c.cdIsSmallerThan(d2c));
        assertTrue(d2c.cdIsSmallerThan(d3));
        assertFalse(d3.cdIsSmallerThan(d1));
        assertFalse(d3.cdIsSmallerThan(d2));
        assertFalse(d3.cdIsSmallerThan(d2c));
        assertFalse(d3.cdIsSmallerThan(d3));
        // now with a nogo
        d2.setNoGo(18);
        assertFalse(d2.cdIsSmallerThan(d1));
        assertFalse(d2.cdIsSmallerThan(d2));
        assertFalse(d2.cdIsSmallerThan(d2c)); // here changed due to nogo
        assertFalse(d2.cdIsSmallerThan(d3)); // here changed due to nogo
        assertTrue(d1.cdIsSmallerThan(d2));
        assertTrue(d2c.cdIsSmallerThan(d2)); // here changed due to nogo
        assertTrue(d3.cdIsSmallerThan(d2)); // here changed due to nogo
    }

    @Test
    void reduceIfSmaller_Test() {
        ConditionalDistance d1 = new ConditionalDistance((VirtualPieceOnSquare)null, 1);
        ConditionalDistance d2 = new ConditionalDistance((VirtualPieceOnSquare)null, 2);
        ConditionalDistance d2c = new ConditionalDistance((VirtualPieceOnSquare)null, 2, 2, ANY, WHITE);
        ConditionalDistance d3 = new ConditionalDistance((VirtualPieceOnSquare)null, 3);
        assertFalse( d1.reduceIfCdIsSmaller(d1) );
        assertFalse( d1.reduceIfCdIsSmaller(d2) );
        assertFalse( d1.reduceIfCdIsSmaller(d2c) );
        assertFalse( d1.reduceIfCdIsSmaller(d3) );
        assertEquals(d1.dist(), 1);
        assertFalse( d2.reduceIfCdIsSmaller(d2) );
        assertFalse( d2.reduceIfCdIsSmaller(d2c) );
        assertFalse( d2.reduceIfCdIsSmaller(d3) );
        assertEquals(d2.dist(), 2);
        assertTrue( d2.reduceIfCdIsSmaller(d1) );
        assertEquals(d2.dist(), 1);
        assertFalse( d2c.reduceIfCdIsSmaller(d3) );
        assertTrue( d3.reduceIfCdIsSmaller(d2c) );
        assertEquals(d3.dist(), 2);
        assertFalse(d3.isUnconditional());

        ConditionalDistance d2u = new ConditionalDistance((VirtualPieceOnSquare)null, 2);
        assertTrue( d2c.reduceIfCdIsSmaller(d2u) );
        assertEquals(d2c.dist(), 2);
        assertTrue(d2c.isUnconditional());

        // now with a nogo
        d2.setNoGo(18);
        assertEquals(d2.dist(), 1);
        assertFalse( d2c.reduceIfCdIsSmaller(d2) );
        assertEquals(d2c.dist(), 2);

        d2.inc();
        assertEquals(d2.dist(), 2);
        assertTrue( d2.reduceIfCdIsSmaller(d1) );
        assertEquals(d2.dist(), 1);
    }

    @Test
    void movesFulfillConditions_Test() {
        ConditionalDistance d1 = new ConditionalDistance((VirtualPieceOnSquare)null, 3);
        d1.addCondition(ANY, 20);
        assertEquals(1, d1.nrOfConditions());
        debugPrintln(true, "d1-before condition check: "+d1);
        List<Move> ml = new ArrayList<>(1);
        ml.add(new Move(18,20));
        assertEquals(1, d1.movesFulfillConditions(ml) );
        debugPrintln(true, "d1-after condition check: "+d1);
        assertEquals(1, d1.nrOfConditions());
        assertEquals(1, ml.size() );
        d1.addCondition(ANY, 20);
        assertEquals(2, d1.nrOfConditions());
        debugPrintln(true, "d1-before condition check: "+d1);
        assertEquals(-1, d1.movesFulfillConditions(ml) );
        debugPrintln(true, "d1-after condition check: "+d1);
        assertEquals(2, d1.nrOfConditions());
        assertEquals(1, ml.size() );
    }
}