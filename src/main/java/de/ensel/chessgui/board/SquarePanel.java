package de.ensel.chessgui.board;

import de.ensel.chessgui.ChessEngine;
import de.ensel.chessgui.control.ChessGuiBasics;
import de.ensel.tideeval.ChessBasics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.lang.Math.*;

/**
 * One square on the board
 */
class SquarePanel extends JPanel {

    /**
     * logic attributes:
     */
    private BoardPanel board;
    private Piece piece;
    private final int rank;
    private final int file;
    private boolean isMoused = false;

    /**
     * graphical attributes:
     */
    private Color standardBackgroundColor;
    private JLabel image;

    /**
     * Constructor for a new square
     * @param boardPanel board the square belongs to
     * @param rank rank of square
     * @param file file of square
     */
    public SquarePanel(BoardPanel boardPanel, int rank, int file) {
        // set attributes
        this.piece = Piece.EMPTY;
        this.board = boardPanel;
        this.rank = rank;
        this.file = file;
        this.standardBackgroundColor = getColorFromCoordinate(rank, file);
        this.image = new JLabel();
        this.image.setSize(new Dimension(80,80));
        this.image.setVisible(true);
        // add panels
        add(image);
        // set panel properties
        setSize(new Dimension(ChessGuiBasics.BOARD_PIXEL_SIZE / ChessGuiBasics.BOARD_SIZE, ChessGuiBasics.BOARD_PIXEL_SIZE / ChessGuiBasics.BOARD_SIZE));
        setMaximumSize(new Dimension(ChessGuiBasics.BOARD_PIXEL_SIZE / ChessGuiBasics.BOARD_SIZE, ChessGuiBasics.BOARD_PIXEL_SIZE / ChessGuiBasics.BOARD_SIZE));
        setBackground(standardBackgroundColor);
        addMouseListener(new SquareListener(this));
        // finish and paint panel
        validate();
        repaintSquare();
    }

    /**
     * Change the piece on the square and update the image
     * @param piece new piece on board
     */
    public void setFigureAndRepaint(Piece piece) {
        this.piece = piece;
        repaintSquare();
    }

    /**
     * Update Image on square
     */
    public void repaintSquare() {
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
     * Paint the background according to key-value of the square
     * @param key key to get value from
     */
    public void colorByKey(String key, SquarePanel commandFrom, ChessEngine chessEngine) {
        String squareInfo = chessEngine.getSquareInfo(
                        getSquareString(),
                        commandFrom.getSquareString())
                            .get(key);
        if (squareInfo==null)
            setBackground(getColorFromKeyValue("0"));
        else
            setBackground(getColorFromKeyValue(squareInfo.split("\\s")[0]));
    }

    /**
     * Get color from a given value
     * TODO do something
     * @param value value from key
     * @return square color
     */
    private Color getColorFromKeyValue(String value) {
        int v = 0;
        if (value!=null)
            try {
                v = Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                v = value.hashCode();
            }

        if (v == 0  || v>=Integer.MAX_VALUE-3) {
            return new Color(110, 110, 110);
        }
        if (v > 0 && v <= 10) {
            return new Color(200 - 15*v,90 - 5*v,255 - v * 14);
        }


        if (v > 0 && v <= 200) {
            return new Color(100 + v/4,100 + v/3,40 - v/20);
        }
        if (v < 0 && v >= -200) {
            v = -v;
            return new Color(40 - v/20,100 + v/3,100 + v/4);
        }

        if (v > 0 && v <= 2000) {
            return new Color(150 + v/20,150 + v/20,40 - v/50 +(v%20));
        }
        if (v < 0 && v >= -2000) {
            v = -v;
            return new Color(40 - v/50 +(v%20),150 + v/20,150 + v/20);
        }

        if (v > 2000) {
            int n = (int)sqrt((v-2000d)/2d);
            return new Color(255-((v/4)%20),max(0, 255-n), min(255, n/4));
        }
        //if (v < -2000) {
            int n = (int)sqrt(-(v+2000d)/2d);
            return new Color(min(255, n/4) ,max(0, 255-n), 255-((v/4)%20));
        //}
    }

    /**
     * Get right standard background color for a square on a given coordinate
     * @param rank rank of square
     * @param file file of square
     * @return according color
     */
    public static Color getColorFromCoordinate(int rank, int file) {
        return rank % 2 == 0 ? (file % 2 == 0 ? ChessGuiBasics.COLOR_1 : ChessGuiBasics.COLOR_2) : (file % 2 == 0 ? ChessGuiBasics.COLOR_2 : ChessGuiBasics.COLOR_1);
    }

    /**
     * returns coordinate as String
     * @return coordinate-string
     */
    public String getSquareString() {
        return ""+ ChessGuiBasics.rankToLetter(rank)+(file * -1 + 8);
    }

    /**
     * SquareListener to detect mouse activities in a square
     */
    private static class SquareListener extends MouseAdapter {

        /**
         * logic attributes:
         * - square     -> square the listener belongs to
         */
        private final SquarePanel square;

        public SquareListener(SquarePanel square) {
            this.square = square;
        }

        @Override
        public void mouseClicked(MouseEvent event) {
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
            square.setBackground(square.getBackground().darker());
            square.getBoard().setMoveDestination(square.rank, square.file);
            square.isMoused = true;
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
            square.setBackground(square.getBackground().brighter());
            square.isMoused = false;
        }

        @Override
        public void mousePressed(MouseEvent arg0) {
            square.getBoard().setMoveOrigin(square.rank, square.file);
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            //square.getBoard().setMoveDestination(square.getRank() + (event.getX() / square.getWidth()), square.getFile() + (event.getY() / square.getHeight()));
            square.getBoard().moveAndUpdate();
        }
    }

    @Override
    public String toString() {
        return "Square ["+rank+","+file+"]: "+ piece;
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
    public int getRank() {
        return rank;
    }
    public int getFile() {
        return file;
    }
    public JLabel getImage() {
        return image;
    }
    public boolean isMoused() {
        return isMoused;
    }
}
