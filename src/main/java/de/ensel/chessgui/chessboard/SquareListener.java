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
