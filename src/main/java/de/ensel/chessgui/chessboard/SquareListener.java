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

package de.ensel.chessgui.chessboard;

import de.ensel.chessgui.control.Chessgame;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * SquareListener to detect mouse activities in a square
 */
class SquareListener extends MouseAdapter {

    /**
     * logic attributes:
     * - chessgame  -> chessgame to send the commandos to
     * - square     -> square the listener belongs to
     */
    private final Chessgame chessgame;
    private final SquarePanel square;

    public SquareListener(Chessgame chessgame, SquarePanel square) {
        this.chessgame = chessgame;
        this.square = square;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        square.setBackground(square.getBackground().darker());
        chessgame.setCurrentMouseSquareIndex(square.getIndex());
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        square.setBackground(square.getBackground().brighter());
        chessgame.setCurrentMouseSquareIndex(-1);
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
        chessgame.setMoveOriginIndex(square.getIndex());
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        chessgame.executeMoveFromMarkedSquares();
    }
}
