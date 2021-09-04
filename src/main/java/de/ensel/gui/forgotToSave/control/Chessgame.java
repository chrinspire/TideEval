package de.ensel.gui.forgotToSave.control;

import de.ensel.gui.ChessEngine;
import de.ensel.gui.forgotToSave.board.BoardPanel;
import de.ensel.gui.forgotToSave.board.InfoPanel;
import de.ensel.tideeval.ChessBasics;
import de.ensel.tideeval.ChessBoardController;

import javax.swing.*;
import java.awt.*;

public class Chessgame {

    /**
     * logic attributes,
     * everything non-graphical:
     * - ChessEngine            -> chess engine to use for analysis
     * - isWhitesMove           -> stores whose move it is
     */
    private ChessEngine chessEngine;
    private boolean isWhitesMove;

    /**
     * graphic attributes,
     * mainly all frames and panels:
     * - window                 -> main application window
     * - boardPanel              -> game board
     * - infoPanel              -> side panel for input and information display
     */
    private JFrame window;
    private BoardPanel boardPanel;
    private InfoPanel infoPanel;

    /**
     * Constructor, initializing the game (windows and logic)
     */
    public Chessgame() {
        chessEngine = new ChessBoardController();
        chessEngine.setBoard(ChessBasics.FENPOS_INITIAL);
        initializeWindow();
        startApplicationLoop();
    }

    /**
     * starts the Chessgame
     * @param args ignored
     */
    public static void main(String[] args) {
        System.err.println("\nWarning: Implementation (v.0.1) not ready yet. Weird things might happen.");
        Chessgame game = new Chessgame();
    }

    /**
     * initializes the game window
     */
    private void initializeWindow() {
        window = new JFrame(ChessGuiBasics.WINDOW_TITLE);
        boardPanel = new BoardPanel(this);
        infoPanel = new InfoPanel(this);
        Container contentPanel = window.getContentPane();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));
        contentPanel.add(boardPanel);
        contentPanel.add(infoPanel);
        window.setSize(ChessGuiBasics.STANDARD_WINDOW_SIZE);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.validate();
    }

    /**
     * Loop for repainting the window
     */
    private void startApplicationLoop() {
        Thread gameThread = new Thread(){
            @Override
            public void run(){
                boardPanel.repaint();
            }
        };
        gameThread.start();
    }

    /**
     * Getters
     */
    public ChessEngine getChessEngine() {
        return chessEngine;
    }
    public boolean isWhitesMove() {
        return isWhitesMove;
    }
    public JFrame getWindow() {
        return window;
    }
    public BoardPanel getBoardPanel() {
        return boardPanel;
    }
    public InfoPanel getInfoPanel() {
        return infoPanel;
    }
}
