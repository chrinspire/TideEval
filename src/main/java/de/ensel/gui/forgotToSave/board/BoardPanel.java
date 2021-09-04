package de.ensel.gui.forgotToSave.board;

import de.ensel.gui.forgotToSave.control.ChessGuiBasics;
import de.ensel.gui.forgotToSave.control.Chessgame;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class BoardPanel extends JPanel {

    /**
     * logic attributes:
     * - squarePanels       -> array of all squares
     * - moveFrom           -> remember origin square for move
     * - moveTo             -> remember destination square for move
     */
    private Chessgame chessgame;
    private final SquarePanel[][] squarePanels;
    private SquarePanel moveFrom;
    private SquarePanel moveTo;
    private String currentColorKey;

    /**
     * Constructor, generating a new board
     */
    public BoardPanel(Chessgame chessgame) {
        this.chessgame = chessgame;
        this.currentColorKey = "";
        squarePanels = new SquarePanel[ChessGuiBasics.BOARD_SIZE][ChessGuiBasics.BOARD_SIZE];
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setVisible(true);
        // add ranks to board
        for (int rank = 0; rank < ChessGuiBasics.BOARD_SIZE; rank++) {
            JPanel newRank = new JPanel();
            newRank.setLayout(new BoxLayout(newRank, BoxLayout.PAGE_AXIS));
            // add squares to rank and squareList
            for (int file = 0; file < ChessGuiBasics.BOARD_SIZE; file++) {
                SquarePanel newSquare = new SquarePanel(this, rank, file);
                squarePanels[rank][file] = newSquare;
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
    }

    /**
     * Set up any board from a (simple) fen-string
     * @param fen fen-string of wanted board
     */
    public void setBoardWithFenString(String fen) {
        char[] fenArray = fen.toCharArray();
        int file = 0;
        int rank = 0;
        for (char c : fenArray) {
            // evaluate next char
            switch (c) {
                case '/' -> {file++;rank=0;continue;}
                case '1', '2', '3', '4', '5', '6', '7', '8' -> {
                    for (int i = 0; i < c - 48; i++) {
                        squarePanels[rank][file].setFigureAndRepaint(Piece.EMPTY);
                        rank = ++rank % 8;
                    }
                    continue;
                }
                default -> squarePanels[rank][file].setFigureAndRepaint(Piece.getFigureFromAsciiSymbol(c));
            }
            rank = ++rank % 8;
        }
        this.repaint();
    }

    /**
     * Paint background of al squares according to the given key.
     * Resets the painting if the same key is requested, that is already displayed.
     * @param key key to evaluate the value from
     */
    public void paintSquaresByKey(String key) {
        if (currentColorKey.equals(key)) {
            currentColorKey = "";
            for (SquarePanel[] row : squarePanels) {
                for (SquarePanel squarePanel: row) {
                    squarePanel.resetBackground();
                }
            }
        }
        else {
            currentColorKey = key;
            for (SquarePanel[] row : squarePanels) {
                for (SquarePanel squarePanel : row) {
                    squarePanel.colorByKey(key, chessgame.getChessEngine());
                }
            }
        }
    }

    /**
     * Set a new origin for the next move
     * @param rank origin rank
     * @param file origin file
     */
    public void setMoveOrigin(int rank, int file) {
        moveFrom = squarePanels[rank][file];
    }

    /**
     * Set a new destination for next move
     * @param rank destination rank
     * @param file destination file
     */
    public void setMoveDestination(int rank, int file) {
        moveTo = squarePanels[rank][file];
    }

    /**
     * Execute move according to current moveFrom and move To.
     * Warning: Should only be used after setMoveOrigin() and setMoveDestination().
     */
    public void executeMove() {
        // if origin and destination are the same (move on same square), display information for given square
        if (moveFrom == moveTo) {
            this.chessgame.getInfoPanel().displaySquareInfo(moveFrom.getSquareString());
        }
        // otherwise, execute the move
        else {
            moveTo.setFigureAndRepaint(moveFrom.getPiece());
            moveFrom.setFigureAndRepaint(Piece.EMPTY);
            chessgame.getChessEngine().doMove(ChessGuiBasics.coordinatesToMove(moveFrom.getRank(), moveFrom.getFile(), moveTo.getRank(), moveTo.getFile()));
            chessgame.getInfoPanel().displayBoardInfo();
            /* if special moves (castling, en passant) are implemented in the ChessEngine,
             * use these to display the right board after special move by uncommenting this command:
             * setBoardWithFenString(chessgame.getChessEngine().getBoard());
             */
        }
    }

    /**
     * Get the square on a given coordinate
     * @param rank rank of square
     * @param file file of square
     * @return according square
     */
    public SquarePanel getSquareOnCoordinate(int rank, int file) {
        return squarePanels[rank][file];
    }
}
