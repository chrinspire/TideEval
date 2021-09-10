package de.ensel.chessgui.board;

import de.ensel.chessgui.control.ChessGuiBasics;
import de.ensel.chessgui.control.Chessgame;
import de.ensel.tideeval.ChessBasics;

import javax.swing.*;
import java.awt.*;

/**
 * Controls and gui elements for the whole game board are located here.
 */
public class BoardPanel extends JPanel {

    /**
     * logic attributes:
     * - squarePanels           -> array of all squares
     */
    private Chessgame chessgame;
    private final SquarePanel[][] squarePanels;

    /**
     * state attributes:
     * - moveFrom               -> origin square for move
     * - mouseSquare            -> square the mouse is on
     * - currentColorKey        -> current key the board is colored by
     * - currentColoringSquare  -> square the coloring command came from
     */
    private SquarePanel moveFrom;
    private SquarePanel mouseSquare;
    private String currentColorKey;
    private SquarePanel currentColoringSquare;

    /**
     * Constructor, generating a new board
     */
    public BoardPanel(Chessgame chessgame) {
        this.chessgame = chessgame;
        this.currentColorKey = "";
        this.currentColoringSquare = null;
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
        chessgame.getChessEngine().setBoard(ChessBasics.FENPOS_INITIAL);
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
            repaintSquaresByKey();
        }
        else {
            currentColorKey = key;
            repaintSquaresByKey();
        }
    }

    /**
     * Repaint squares according to set key
     */
    public void repaintSquaresByKey() {
        if (currentColorKey.equals("")) {
            for (SquarePanel[] row : squarePanels) {
                for (SquarePanel squarePanel : row) {
                    squarePanel.resetBackground();
                    if (squarePanel == mouseSquare) {
                        squarePanel.darkenBackground();
                    }
                }
            }
        }
        else {
            for (SquarePanel[] row : squarePanels) {
                for (SquarePanel squarePanel : row) {
                    squarePanel.colorByKey(currentColorKey, currentColoringSquare, chessgame.getChessEngine());
                    if (squarePanel == mouseSquare) {
                        squarePanel.darkenBackground();
                    }
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
        mouseSquare = squarePanels[rank][file];
    }

    /**
     * Execute move according to current moveFrom and move To.
     * Warning: Should only be used after setMoveOrigin() and setMoveDestination().
     */
    public void moveAndUpdate() {
        // if origin and destination are the same (move on same square), display information for given square
        if (moveFrom == mouseSquare) {
            currentColoringSquare = moveFrom;
            chessgame.getInfoPanel().displaySquareInfo(moveFrom.getSquareString());
        }
        // otherwise, execute the move
        else {
            // if move legal: execute
            if (chessgame.getChessEngine().doMove(ChessGuiBasics.coordinatesToMove(moveFrom.getRank(), moveFrom.getFile(), mouseSquare.getRank(), mouseSquare.getFile()))) {
                mouseSquare.setFigureAndRepaint(moveFrom.getPiece());
                moveFrom.setFigureAndRepaint(Piece.EMPTY);
                chessgame.getInfoPanel().displayBoardInfo();
                /* if special moves (castling, en passant) are implemented in the ChessEngine,
                 * use these to display the right board after special move by uncommenting the following line:
                 */
                setBoardWithFenString(chessgame.getChessEngine().getBoard());
                repaintSquaresByKey();
            }
            // illegal move according to chessEngine: error
            else {
                Thread errorBlinker = new Thread(() -> {
                    SquarePanel errorFrom = moveFrom;
                    SquarePanel errorTo = mouseSquare;
                    for (int i = 0; i < 2; i++) {
                        errorFrom.colorBackground(ChessGuiBasics.ERROR_COLOR);
                        errorTo.colorBackground(ChessGuiBasics.ERROR_COLOR);
                        try {
                            Thread.sleep(150);
                        } catch (Exception ignored) {
                            System.out.println("WAIT FAILED");
                        }
                        currentColorKey = "";
                        repaintSquaresByKey();
                        try {
                            Thread.sleep(150);
                        } catch (Exception ignored) {
                            System.out.println("WAIT FAILED");
                        }
                    }
                });
                errorBlinker.start();
            }
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

    /**
     * Getter
     */
    public SquarePanel[][] getSquarePanels() {
        return squarePanels;
    }
    public SquarePanel getMoveFrom() {
        return moveFrom;
    }
    public SquarePanel getMouseSquare() {
        return mouseSquare;
    }
    public String getCurrentColorKey() {
        return currentColorKey;
    }
    public SquarePanel getCurrentColoringSquare() {
        return currentColoringSquare;
    }
}
