package de.ensel.gui.forgotToSave.board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        setSize(new Dimension(BoardPanel.BOARD_PIXEL_SIZE / BoardPanel.BOARD_SIZE, BoardPanel.BOARD_PIXEL_SIZE / BoardPanel.BOARD_SIZE));
        setMaximumSize(new Dimension(BoardPanel.BOARD_PIXEL_SIZE / BoardPanel.BOARD_SIZE, BoardPanel.BOARD_PIXEL_SIZE / BoardPanel.BOARD_SIZE));
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
     * get right background color for a square on a given coordinate
     * @param rank rank of square
     * @param file file of square
     * @return according color
     */
    public static Color getColorFromCoordinate(int rank, int file) {
        return rank % 2 == 0 ? (file % 2 == 0 ? BoardPanel.COLOR_1 : BoardPanel.COLOR_2) : (file % 2 == 0 ? BoardPanel.COLOR_2 : BoardPanel.COLOR_1);
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
            if(square.getBackground() == Color.CYAN.darker().darker()) {
                square.setBackground(square.standardBackgroundColor.darker());
            }
            else {
                square.setBackground(Color.CYAN);
            }
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
            square.setBackground(square.getBackground().darker());
            square.getBoard().setMoveDestination(square.rank, square.file);
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
            square.setBackground(square.getBackground().brighter());
        }

        @Override
        public void mousePressed(MouseEvent arg0) {
            square.getBoard().setMoveOrigin(square.rank, square.file);
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            //square.getBoard().setMoveDestination(square.getRank() + (event.getX() / square.getWidth()), square.getFile() + (event.getY() / square.getHeight()));
            square.getBoard().executeMove();
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
}
