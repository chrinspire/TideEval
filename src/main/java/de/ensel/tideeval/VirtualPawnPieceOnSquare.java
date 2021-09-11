/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import static de.ensel.tideeval.ChessBasics.ALLDIRS;
import static de.ensel.tideeval.ChessBasics.NONE;

public class VirtualPawnPieceOnSquare extends VirtualOneHopPieceOnSquare {

    public VirtualPawnPieceOnSquare(ChessBoard myChessBoard, int newPceID, int myPos) {
        super(myChessBoard, newPceID, myPos);
    }

    /**
     * Updates the overall minimum distance
     * @param updateDistance the new distance-value propagated from my neighbour (or "0" if Piece came to me)
     * @return int what needs to be updated. NONE for nothing, ALLDIRS for all, below that >0 tells a single direction
     */
    @Override
    protected int updateRawMinDistances(final Distance updateDistance, int minDist ) {
        setLastUpdateToNow();
        if (rawMinDistance.dist()==0)
            return NONE;  // there is nothing closer than myself...
        if (updateDistance.isSmaller(rawMinDistance)) {
            // the new distance is smaller than the minimum, so we already found the new minimum
            rawMinDistance.reduceIfSmaller(updateDistance);
            minDistance = null;
            return ALLDIRS;
        }
        // because a pawn is never able to move the same way back, an update is always simple and never leads to a "reset-bomb"
        return NONE;
    }


}
