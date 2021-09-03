/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
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