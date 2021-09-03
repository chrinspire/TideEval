package de.ensel.gui.forgotToSave.board;

import de.ensel.gui.forgotToSave.control.Chessgame;

import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {

    /**
     * static attributes:
     * - BOARD_SIZE         -> length of one sie of the board
     * - BOARD_PIXEL_SIZE   -> size of board in window
     * - COLOR_1            -> first color of the board
     * - COLOR_2            -> second color of the board
     */
    public static final int BOARD_SIZE = 8;
    public static final int BOARD_PIXEL_SIZE = 800;
    public static final Color COLOR_1 = new Color(0xFFFFFF);
    public static final Color COLOR_2 = new Color(0x17912E);
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

    /**
     * Constructor, generating a new board
     */
    public BoardPanel(Chessgame chessgame) {
        this.chessgame = chessgame;
        squarePanels = new SquarePanel[BOARD_SIZE][BOARD_SIZE];
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setVisible(true);
        // add ranks to board
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            JPanel newRank = new JPanel();
            newRank.setLayout(new BoxLayout(newRank, BoxLayout.PAGE_AXIS));
            // add squares to rank and squareList
            for (int file = 0; file < BOARD_SIZE; file++) {
                SquarePanel newSquare = new SquarePanel(this, rank, file);
                squarePanels[rank][file] = newSquare;
                newRank.add(newSquare);
            }
            newRank.setMaximumSize(new Dimension(BOARD_PIXEL_SIZE / BOARD_SIZE,BOARD_PIXEL_SIZE));
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
        moveTo.setFigureAndRepaint(moveFrom.getPiece());
        moveFrom.setFigureAndRepaint(Piece.EMPTY);
        chessgame.getChessEngine().doMove(coordinatesToMove(moveFrom.getRank(), moveFrom.getFile(), moveTo.getRank(), moveTo.getFile()));
        chessgame.getInfoPanel().displayBoardInfo();
    }

    private String coordinatesToMove(int fromRank, int fromFile, int toRank, int toFile) {
        return ""+ rankToLetter(fromRank)+(fromFile * -1 + 8)+ rankToLetter(toRank)+(toFile * -1 + 8);
    }

    private char rankToLetter(int rank) {
        switch (rank) {
            case 0 -> {return 'a';}
            case 1 -> {return 'b';}
            case 2 -> {return 'c';}
            case 3 -> {return 'd';}
            case 4 -> {return 'e';}
            case 5 -> {return 'f';}
            case 6 -> {return 'g';}
            case 7 -> {return 'h';}
            default -> {return ' ';}
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
