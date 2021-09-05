package de.ensel.gui.forgotToSave.board;

import de.ensel.gui.forgotToSave.control.ChessGuiBasics;
import de.ensel.gui.forgotToSave.control.Chessgame;
import de.ensel.gui.forgotToSave.control.ControlPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * This panel is responsible for accepting user commandos and displaying information about the game from the chess engine.
 */
public class InfoPanel extends JPanel {

    /**
     * logic attributes:
     */
    private final Chessgame chessgame;

    /**
     * panels:
     */
    private ControlPanel controlPanel;
    private JTextPane infoHeader;
    private JTextField commandInputField;
    private JList<String> lastCommandsTextBox;
    private List<String> lastTextCommands = new LinkedList<>();
    private DataTable boardData = new DataTable( "___Board  Data___", false, this);
    private DataTable squareData = new DataTable("___Square Data___", true, this);

    /**
     * Constructor, creating new InfoPanel
     * @param chessGame the ChessGame it belongs to (relevant for chess engine, movement and figure placement)
     */
    public InfoPanel(Chessgame chessGame){
        this.chessgame = chessGame;
        controlPanel = new ControlPanel(this);
        infoHeader = new JTextPane();
        infoHeader.setEditable(false);
        infoHeader.setText(ChessGuiBasics.STANDARD_INFO_HEADER);
        infoHeader.setMaximumSize(new Dimension(ChessGuiBasics.BOARD_PIXEL_SIZE,60));
        commandInputField = new JTextField();
        commandInputField.setMaximumSize(new Dimension(ChessGuiBasics.BOARD_PIXEL_SIZE,60));
        lastCommandsTextBox = new JList<>();
        lastCommandsTextBox.setMaximumSize(new Dimension(ChessGuiBasics.BOARD_PIXEL_SIZE,60));
        lastCommandsTextBox.setFixedCellWidth(0);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        //TODO add controlPanel when implemented: this.add(controlPanel);
        this.add(infoHeader);
        this.add(commandInputField);
        this.add(lastCommandsTextBox);
        this.add(boardData.getPanel());
        this.add(squareData.getPanel());
        setupAllListeners();
        this.setMaximumSize(new Dimension(ChessGuiBasics.BOARD_PIXEL_SIZE,10000));
        this.validate();
    }

    /**
     * Helper method to set up the button listeners for Undo, Restart and Forfeit buttons.
     */
    private void setupAllListeners() {
        commandInputField.addActionListener(e -> executeInputFieldCommand());
    }

    /**
     * 1. reads the command written into the commandInputField
     * 2. and forwards it to evaluateTextCommand()
     */
    private void executeInputFieldCommand() {
        String command = commandInputField.getText();
        commandInputField.setText("");
        addAsLastCommand(command);
        evaluateTextCommand(command);
    }

    /**
     * Adds a text into the last commands.
     * The last commands array can't be bigger than 3!
     * @param command command to add
     */
    private void addAsLastCommand(String command) {
        lastTextCommands.add(0,command);
        if(lastTextCommands.size() > 3)
            lastTextCommands.remove(3);
        lastCommandsTextBox.setListData(lastTextCommands.toArray(new String[0]));
    }

    /**
     * Evaluates and executes the according action to the inputted command
     * @param command command to evaluate
     */
    private void evaluateTextCommand(String command) {
        String[] parted = command.toLowerCase(Locale.ROOT).split(" ");
        command = parted[0];
        String attribute;
        // commands with attribute
        if (parted.length > 1) {
            attribute = parted[1];
            switch (command) {
                case "" -> noValidInput();
                case "sqri", "squareinfo" -> displaySquareInfo(attribute);
                default -> {doMove(command); return;}
            }
        }
        // commands without attribute
        else {
            switch (command) {
                case "" -> noValidInput();
                case "info", "boardinfo" -> displayBoardInfo();
                case "move", "enginemove" -> letChessEngineMove();
                case "get", "getboard" -> setBoardFromEngine();
                case "reset", "resetboard" -> resetBoard();
                default -> {doMove(command); return;}
            }
        }
        infoHeader.setText(ChessGuiBasics.STANDARD_INFO_HEADER);
    }

    public void resetBoard() {
        chessgame.getBoardPanel().setStandardBoard();
    }

    private void noValidInput() {
        infoHeader.setText(">no valid input<");
    }

    public void displayBoardInfo() {
        boardData.resetTable();
        HashMap<String,String> data = chessgame.getChessEngine().getBoardInfo();
        data.forEach((key,value) -> boardData.addRow(key,value));
        //for (String row : data) {
        //    boardData.addRow(row, "");
        //}
    }

    public void displaySquareInfo(String square) {
        squareData.resetTable();
        HashMap<String,String> data = chessgame.getChessEngine().getSquareInfo(square);
        data.forEach((key,value) -> squareData.addRow(key,value));
    }

    private void colorChessboard() {
        //TODO
    }

    private void letChessEngineMove() {
        //TODO
    }

    private void doMove(String move) {
        noValidInput();
    }

    private boolean moveIsIllegal(String move) {
        return false;
    }

    private void setBoardFromEngine() {
        //TODO
    }

    public Chessgame getChessgame() {
        return chessgame;
    }

}
