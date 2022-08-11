package de.ensel.chessgui.control;

import de.ensel.chessgui.ChessEngine;
import de.ensel.chessgui.chessboard.SquarePanel;
import de.ensel.chessgui.sidepanel.InfoPanel;
import de.ensel.chessgui.chessboard.BoardPanel;
import de.ensel.tideeval.ChessBasics;
import de.ensel.tideeval.ChessBoardController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller class for the chess gui.
 * Processes input and output of the system.
 */
public class Chessgame {

    /**
     * logic attributes,
     * everything non-graphical:
     * - ChessEngine            -> chess engine to use for analysis
     * - moveOriginIndex        -> index of a set moveOrigin square
     * - currentMouseSquareIndex-> current square the mouse is on (also used as moveDestination)
     * - currentColorKey        -> key the board is currently colored by
     * - currentColoringSquare  -> square the coloring command came from
     * - boardInfo              -> information about the board
     * - squareInfoArray        -> array of all the square information
     */
    private ChessEngine chessEngine;
    private int moveOriginIndex;
    private int currentMouseSquareIndex;
    private String currentColorKey;
    private int currentColoringSquareIndex;
    private HashMap<String,String> boardInfo;
    private final ArrayList<HashMap<String,String>> squareInfoArray;

    /**
     * graphic attributes,
     * mainly all frames and panels:
     * - window                 -> main application window
     * - boardPanel             -> game board
     * - infoPanel              -> side panel for input and information display
     */
    private final JFrame window;
    private final BoardPanel boardPanel;
    private final InfoPanel infoPanel;

    /**
     * Constructor, initializing the game (windows and logic)
     */
    public Chessgame() {
        chessEngine = new ChessBoardController();
        chessEngine.setBoard(ChessBasics.FENPOS_INITIAL);
        squareInfoArray = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            squareInfoArray.add(null);
        }
        // initialize window
        window = new JFrame(ChessGuiBasics.WINDOW_TITLE);
        boardPanel = new BoardPanel(this);
        infoPanel = new InfoPanel(this);
        Container contentPanel = window.getContentPane();
        window.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));
        window.add(boardPanel);
        window.add(infoPanel);
        window.setSize(ChessGuiBasics.STANDARD_WINDOW_SIZE);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.validate();
    }

    /**
     * starts the Chessgame
     * @param args ignored
     */
    public static void main(String[] args) {
        System.err.println("\nWarning: Implementation (v.1.2) might be buggy. Weird things can happen.");
        Chessgame game = new Chessgame();
    }

    /**
     * Sets the visual and the chessEngine board to the new board
     * @param fen fen-string of the new board
     */
    public void setBoardFromFen(String fen) {
        chessEngine.setBoard(fen);
        boardPanel.setBoardWithFenString(chessEngine.getBoard());
    }

    /**
     * updates the board info if null
     */
    private void updateOldBoardData(){
        boardInfo = chessEngine.getBoardInfo();
        infoPanel.displayHashmap(infoPanel.getBoardData(), boardInfo);
        System.out.println("update");
    }

    /**
     * Updates the square info if null (outdated or missing).
     * @param squareIndex index of square to update
     * @param originSquareIndex optional index of second square (e.g. for distance)
     */
    private void updateOldSquareInfo(int squareIndex, int originSquareIndex) {
        squareInfoArray.set(squareIndex, chessEngine.getSquareInfo(ChessGuiBasics.indexToSquare(squareIndex), ChessGuiBasics.indexToSquare(originSquareIndex)));
    }

    /**
     * updates square info for all squares
     * @param originSquareIndex optional index of second square (e.g. for distance)
     */
    private void updateAllSquareInfo(int originSquareIndex) {
        for (int i = 0; i < 64; i++) {
            updateOldSquareInfo(i,originSquareIndex);
        }
    }

    /**
     * Removes all board and square information.
     */
    private void markInformationAsOutdated() {
        boardInfo = null;
        for (HashMap<String, String> squareInfo : squareInfoArray) {
            squareInfo = null;
        }
    }

    /**
     * Sets all square backgrounds according to the data of a given key
     * @param key key to evaluate from
     */
    public void paintAllSquaresByKey(String key) {
        currentColorKey = key;
        if (currentColorKey == null) {
            boardPanel.resetBoardBackground();
        }
        else {
            for (int i = 0; i < ChessGuiBasics.BOARD_SIZE * ChessGuiBasics.BOARD_SIZE; i++) {
                Color squareColor = Color.white;
                if (squareInfoArray.get(i).get(key) == null) {
                    squareColor = ChessGuiBasics.getColorFromKeyValue("0");
                } else {
                    int v = 0;
                    try {
                        v = Integer.parseInt(squareInfoArray.get(i).get(key).split("\\s")[0] );
                    } catch (NumberFormatException e) {
                        v = 0;
                    }
                    if (v == 0)
                       squareColor = SquarePanel.getColorFromCoordinate(i);
                    else
                        squareColor = ChessGuiBasics.getColorFromKeyValue(squareInfoArray.get(i).get(key).split("\\s")[0]);
                }
                if (i == currentMouseSquareIndex) {
                    squareColor = squareColor.darker();
                }
                boardPanel.colorSquareAtIndex(i, squareColor);
            }
        }
        paintDataTableAccordingToCurrentKey();
    }

    /**
     * highlights the row with the current key
     */
    private void paintDataTableAccordingToCurrentKey() {
        infoPanel.highlightRowsInAllTables(currentColorKey);
    }

    /**
     * Executes a move [this.executeMove(int,int)] according to the moveOrigin and currentMouseSquare
     */
    public void executeMoveFromMarkedSquares() {
        executeMove(moveOriginIndex,currentMouseSquareIndex);
    }

    /**
     * Executes a move:
     * 1. informs chess-engine
     * 2. updates board to fit the chess-engine board
     */
    private void executeMove(int originIndex, int destinationIndex) {
        // if origin and destination are the same (move on same square), display information for given square
        if (originIndex == destinationIndex) {
            currentColoringSquareIndex = originIndex;
            updateOldSquareInfo(originIndex,originIndex);
            infoPanel.displayHashmap(infoPanel.getSquareData(), squareInfoArray.get(originIndex));
            infoPanel.displayTitle(infoPanel.getSquareData(), ChessGuiBasics.indexToSquare(originIndex) + "(*" + boardPanel.getSquareAtIndex(originIndex).getPiece().getAsciiSymbol()+ ")");
        }
        // otherwise, execute the move
        else {
            // if move illegal: error
            if (!chessEngine.doMove(ChessGuiBasics.coordinatesToMove(originIndex, destinationIndex))) {
                boardPanel.markIllegalMove(originIndex,destinationIndex);
            }
            updateOldBoardData();
        }
        boardPanel.setBoardWithFenString(chessEngine.getBoard());
        updateAllSquareInfo(originIndex);
        updateOldBoardData();
        paintAllSquaresByKey(currentColorKey);
    }

    /**
     * Setters
     */
    public void setChessEngine(ChessEngine chessEngine) {
        this.chessEngine = chessEngine;
    }
    public void setCurrentMouseSquareIndex(int currentMouseSquareIndex) {
        this.currentMouseSquareIndex = currentMouseSquareIndex;
    }
    public void setMoveOriginIndex(int moveOriginIndex) {
        this.moveOriginIndex = moveOriginIndex;
    }

    /**
     * Getters
     */
    public ChessEngine getChessEngine() {
        return chessEngine;
    }
    public int getCurrentMouseSquareIndex() {
        return currentMouseSquareIndex;
    }
}
