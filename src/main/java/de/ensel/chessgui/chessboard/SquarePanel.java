package de.ensel.chessgui.chessboard;

import de.ensel.chessgui.control.ChessGuiBasics;
import de.ensel.chessgui.control.Chessgame;

import javax.swing.*;
import java.awt.*;

import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * One square on the board
 */
public class SquarePanel extends JPanel {

    /**
     * logic attributes:
     */
    private BoardPanel board;
    private Piece piece;
    private final int index;

    /**
     * graphical attributes:
     */
    private Color standardBackgroundColor;
    private JLabel image;

    /**
     * Constructor for a new square
     * @param boardPanel board the square belongs to
     * @param index index of square
     */
    public SquarePanel(Chessgame chessgame, BoardPanel boardPanel, int index) {
        // set attributes
        this.piece = Piece.EMPTY;
        this.board = boardPanel;
        this.index = index;
        this.standardBackgroundColor = getColorFromCoordinate(index);
        this.image = new JLabel();
        this.image.setSize(new Dimension(80,80));
        this.image.setVisible(true);
        // add panels
        add(image);
        // set panel properties
        setSize(new Dimension(ChessGuiBasics.BOARD_PIXEL_SIZE / ChessGuiBasics.BOARD_SIZE, ChessGuiBasics.BOARD_PIXEL_SIZE / ChessGuiBasics.BOARD_SIZE));
        setMaximumSize(new Dimension(ChessGuiBasics.BOARD_PIXEL_SIZE / ChessGuiBasics.BOARD_SIZE, ChessGuiBasics.BOARD_PIXEL_SIZE / ChessGuiBasics.BOARD_SIZE));
        setBackground(standardBackgroundColor);
        addMouseListener(new SquareListener(chessgame, this));
        // finish and paint panel
        validate();
        updateGraphicalElements();
    }

    /**
     * Change the piece on the square and update the image
     * @param piece new piece on board
     */
    public void setFigureAndRepaint(Piece piece) {
        this.piece = piece;
        updateGraphicalElements();
    }

    /**
     * Update Image on square
     */
    private void updateGraphicalElements() {
        if(piece.getImage() == null) {
            image.setIcon(null);
            image.setText(String.valueOf(piece.getAsciiSymbol()));
        }
        else {
            image.setIcon(new ImageIcon(piece.getImage().getScaledInstance(this.getWidth() - 17, this.getHeight()- 17, Image.SCALE_SMOOTH)));
        }
        this.repaint();
    }

    /**
     * Paint the background to a certain color
     * @param color new background color
     */
    public void colorBackground(Color color) {
        setBackground(color);
    }

    /**
     * Reset the background color
     */
    public void resetBackground() {
        setBackground(standardBackgroundColor);
    }

    /**
     * Darkens the current background
     */
    public void darkenBackground() {
        setBackground(getBackground().darker());
    }

    /**
     * Get right standard background color for a square on a given coordinate
     * @param index index of square
     * @return according color
     */
    public static Color getColorFromCoordinate(int index) {
        return (index % 8) % 2 == 0 ? ((index / 8) % 2 == 0 ? ChessGuiBasics.COLOR_1 : ChessGuiBasics.COLOR_2) : ((index / 8) % 2 == 0 ? ChessGuiBasics.COLOR_2 : ChessGuiBasics.COLOR_1);
    }

    /**
     * returns coordinate as String
     * @return coordinate-string
     */
    public String getSquareString() {
        return ""+ ChessGuiBasics.rankToLetter((index % 8))+((index / 8) * -1 + 8);
    }

    @Override
    public String toString() {
        return "Square ["+(index % 8)+","+(index / 8)+"]: "+ piece;
    }

    /**
     * Getter
     */
    public Piece getPiece() {
        return piece;
    }
    public Color getStandardBackgroundColor() {
        return standardBackgroundColor;
    }
    public BoardPanel getBoard() {
        return board;
    }
    public int getIndex() {
        return index;
    }
    public JLabel getImage() {
        return image;
    }

}
