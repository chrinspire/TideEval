/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.ArrayList;
import java.util.List;

public class ToRelationSet {
    private final List<VirtualPieceOnSquare> destinations = new ArrayList<>();

    public VirtualPieceOnSquare getBestNeighbour() {
        return destinations.parallelStream()
                .reduce((a,b)-> a.compareTo(b) > 0 ? a : b )
                .get();
    }
}
