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

import static org.junit.jupiter.api.Assertions.*;

class ChessGameReaderTest {

    @Test
    void chessGameReader_Test() {
        // good case
        ChessGameReader cgr = new ChessGameReader("1. e4 0.24 1... c5 0.32 2. Nf3 0.0 2... Nf6 0.44 { } 0-1");
        assertEquals( "e4", cgr.getNextMove());
        assertEquals(   24, cgr.getNextEval());
        assertEquals( "c5", cgr.getNextMove());
        assertEquals(   32, cgr.getNextEval());
        assertEquals("Nf3", cgr.getNextMove());
        assertEquals(    0, cgr.getNextEval());
        assertEquals("Nf6", cgr.getNextMove());
        assertEquals(   44, cgr.getNextEval());
        assertFalse(cgr.hasNext() );
        // bad case
        cgr = new ChessGameReader("1. e4 0.24 1... c5");  // ends to early
        assertEquals("e4", cgr.getNextMove());
        assertEquals(  24, cgr.getNextEval());
        assertEquals("c5", cgr.getNextMove());
        assertEquals(   0, cgr.getNextEval());
        assertFalse(cgr.hasNext() );
    }

}