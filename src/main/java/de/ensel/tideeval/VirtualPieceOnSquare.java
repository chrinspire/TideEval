/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.ensel.tideeval.ChessBasics.*;

public class VirtualPieceOnSquare implements Comparable {
    private int rel_eval;
    private int distance;
    static private int DISTANCE_NOT_SET = Integer.MAX_VALUE;

    List<VirtualPieceOnSquare> singleneighbours;
    VirtualPieceOnSquare[] slidingneighbours;

    public VirtualPieceOnSquare() {
        singleneighbours = new ArrayList<VirtualPieceOnSquare>();
        slidingneighbours = new VirtualPieceOnSquare[MAXMAINDIRS];
        rel_eval = NOT_EVALUATED;
        distance = DISTANCE_NOT_SET;
    }

    @Override
    public int compareTo(@NotNull Object other) {
        if (this.rel_eval > ((VirtualPieceOnSquare)other).rel_eval)
            return 1;
        else if (this.rel_eval < ((VirtualPieceOnSquare)other).rel_eval)
            return -1;
        //else
        return 0;
    }

    public void addSingleNeighbour(VirtualPieceOnSquare newvPiece) {
        singleneighbours.add( newvPiece );
    }

    public void addSlidingNeighbour(VirtualPieceOnSquare neighbourPce, int direction) {
        slidingneighbours[convertMainDir2DirIndex(direction)] = neighbourPce;
    }

    public void setDistance(int distance) {
         setPaththroughDistance(distance, -1);
    }

    private void setPaththroughDistance(int distance, int paththroughDirIndex) {
        if (this.distance < distance)  // Achtung: Fehlerquelle - kann das wirklich immer für die ganze Reihe abgebrochen werden?
            return;
        this.distance = distance;
        // inform neighbours
        for (VirtualPieceOnSquare n:singleneighbours)
            n.setDistance(distance+1);
        for (int dirIndex=0; dirIndex<MAXMAINDIRS; dirIndex++) {
            VirtualPieceOnSquare n = slidingneighbours[dirIndex];
            if (n != null) {
                if (dirIndex==paththroughDirIndex)
                    n.setPaththroughDistance(distance, paththroughDirIndex);
                else
                    n.setPaththroughDistance(distance + 1, dirIndex);
            }
        }
    }

    public int getDistance() {
        return distance;
    }
}
