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

import de.ensel.chessgui.control.ChessGuiBasics;
import de.ensel.chessgui.control.Chessgame;

import javax.swing.*;
import java.awt.*;

import static de.ensel.chessbasics.ChessBasics.FENPOS_STARTPOS;

/**
 * Controls and gui elements for the whole game board are located here.
 */
public class BoardPanel extends JPanel {

    /**
     * logic attributes:
     * - chessgame            -> control class
     * - chessboard           -> array of all squares
     */
    private final Chessgame chessgame;
    private final SquarePanel[] chessboard;

    /**
     * Constructor, generating a new board
     */
    public BoardPanel(Chessgame chessgame) {
        this.chessgame = chessgame;
        chessboard = new SquarePanel[ChessGuiBasics.BOARD_SIZE * ChessGuiBasics.BOARD_SIZE];
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setVisible(true);
        // add ranks to board
        for (int rank = 0; rank < ChessGuiBasics.BOARD_SIZE; rank++) {
            JPanel newRank = new JPanel();
            newRank.setLayout(new BoxLayout(newRank, BoxLayout.PAGE_AXIS));
            // add squares to rank and squareList
            for (int file = 0; file < ChessGuiBasics.BOARD_SIZE; file++) {
                SquarePanel newSquare = new SquarePanel(chessgame,this, rank + file * 8);
                chessboard[rank + file * 8] = newSquare;
                newRank.add(newSquare);
            }
            newRank.setMaximumSize(new Dimension(ChessGuiBasics.BOARD_PIXEL_SIZE / ChessGuiBasics.BOARD_SIZE, ChessGuiBasics.BOARD_PIXEL_SIZE));
            newRank.setVisible(true);
            newRank.validate();
            this.add(newRank);
        }
        this.validate();
        setStandardBoard();
    }

    /**
     * Set up standard board
     */
    public void setStandardBoard() {
        setBoardWithFenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        chessgame.getChessEngine().setBoard(FENPOS_STARTPOS);
    }

    /**
     * Set up any board from a (simple) fen-string
     * @param fen fen-string of wanted board
     */
    public void setBoardWithFenString(String fen) {
        char[] fenArray = fen.split(" ")[0].toCharArray();
        int file = 0;
        int rank = 0;
        for (char c : fenArray) {
            // evaluate next char
            switch (c) {
                case '/' -> {
                    file++;
                    rank = 0;
                    continue;
                }
                case '1', '2', '3', '4', '5', '6', '7', '8' -> {
                    for (int i = 0; i < c - 48; i++) {
                        chessboard[rank + file * 8].setFigureAndRepaint(Piece.EMPTY);
                        rank = ++rank % 8;
                    }
                    continue;
                }
                default -> chessboard[rank + file * 8].setFigureAndRepaint(Piece.getFigureFromAsciiSymbol(c));
            }
            rank = ++rank % 8;
        }
        this.repaint();
    }

    /**
     * Makes both given squares blink red two times to signal an error (e.g. false move)
     * @param originIndex index of first square
     * @param destinationIndex index of second square (== first square if only one should blink)
     */
    public void markIllegalMove(int originIndex, int destinationIndex){
        Thread errorBlinker = new Thread(() -> {
            SquarePanel errorFrom = chessboard[originIndex];
            SquarePanel errorTo = chessboard[destinationIndex];
            for (int i = 0; i < 2; i++) {
                errorFrom.colorBackground(ChessGuiBasics.ERROR_COLOR);
                errorTo.colorBackground(ChessGuiBasics.ERROR_COLOR);
                try {
                    Thread.sleep(150);
                } catch (Exception ignored) {
                    System.err.println("WAIT FAILED");
                }
                resetBoardBackground();
                try {
                    Thread.sleep(150);
                } catch (Exception ignored) {
                    System.err.println("WAIT FAILED");
                }
            }
        });
        errorBlinker.start();
    }

    /**
     * reset the background in every square
     */
    public void resetBoardBackground(){
        for (SquarePanel squarePanel : chessboard) {
            squarePanel.resetBackground();
            if (squarePanel.getIndex() == chessgame.getCurrentMouseSquareIndex()) {
                squarePanel.darkenBackground();
            }
        }
    }

    /**
     * Change background of one square to a color
     * @param index index of square
     * @param color new background color
     */
    public void colorSquareAtIndex(int index, Color color) {
        chessboard[index].setBackground(color);
    }

    /**
     * Get the square on a given coordinate
     * @param index index of square
     * @return according square
     */
    public SquarePanel getSquareAtIndex(int index) {
        if (index < 0 || index > chessboard.length) {
            return null;
        }
        return chessboard[index];
    }

    /**
     * Getter
     */
    public SquarePanel[] getChessboard() {
        return chessboard;
    }
}
