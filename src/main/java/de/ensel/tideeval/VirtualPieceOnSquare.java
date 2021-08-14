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
    private final ChessBoard myChessBoard;
    private final int myPceID;
    private int rel_eval;
    private int distance;   // distance in hops from corresponding real piece

    static private int DISTANCE_NOT_SET = Integer.MAX_VALUE;

    List<VirtualPieceOnSquare> singleneighbours;
    VirtualPieceOnSquare[] slidingneighbours;

    public VirtualPieceOnSquare(ChessBoard myChessBoard, int newPceID) {
        this.myChessBoard = myChessBoard;
        myPceID = newPceID;
        singleneighbours = new ArrayList<>();
        slidingneighbours = new VirtualPieceOnSquare[MAXMAINDIRS];
        rel_eval = NOT_EVALUATED;
        distance = DISTANCE_NOT_SET;
    }

    /**
     * myPiece()
     * @return backward reference to my corresponding real piece on the Board
     */
    private ChessPiece myPiece() {
        return myChessBoard.getPiece(myPceID);
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

    public void setInitialDistance(int distance) {
         setInitialDistanceObeyingPaththrough(distance, FROMNOWHERE);
    }

    private void setInitialDistanceObeyingPaththrough(int distance, int pathingThroughInDirIndex) {
        if (this.distance < distance)  // Achtung: Fehlerquelle - kann das wirklich immer für die ganze Reihe abgebrochen werden?
            return;
        this.distance = distance;
        // inform neighbours
        // this is simple for the direct "singleneighbours"
        for (VirtualPieceOnSquare n:singleneighbours)
            n.setInitialDistance(distance+1);
        // for the slidingneighbours, we need to check from which direction the figure is coming from
        for (int dirIndex=0; dirIndex<MAXMAINDIRS; dirIndex++) {
            VirtualPieceOnSquare n = slidingneighbours[dirIndex];
            if (n != null) {
                if (dirIndex==pathingThroughInDirIndex) {
                    // is this is following the same direction as the call came from, we do not need to increase the hops
                    n.setInitialDistanceObeyingPaththrough(distance, pathingThroughInDirIndex);
                }
                else {
                    // piece is taking "a corner" so this will need one hop (i.e. an intermediate stop here)
                    n.setInitialDistanceObeyingPaththrough(distance + 1, dirIndex);
                }
            }
        }
    }

    public int getDistance() {
        return distance;
    }
}
