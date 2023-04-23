/*
 * Copyright (c) 2023.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

public class MovenetDistance {
    public MovenetDistance(ConditionalDistance movenetDist) {
        this.movenetDist = movenetDist;
    }

    public ConditionalDistance movenetDist() {
        return movenetDist;
    }

    private ConditionalDistance movenetDist;
}
