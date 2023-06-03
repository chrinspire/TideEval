package de.ensel.chessgui.sidepanel;

import de.ensel.chessgui.control.ChessGuiBasics;
import de.ensel.chessgui.control.Chessgame;
import de.ensel.tideeval.ChessBasics;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

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
    private final JTextPane infoHeader;
    private final JTextField commandInputField;
    private final JList<String> lastCommandsTextBox;
    private final List<String> lastTextCommands = new LinkedList<>();
    private final DataTable boardData = new DataTable( ChessGuiBasics.STANDARD_BOARD_INFO_PANEL_TITLE, false, this);
    private final DataTable squareData = new DataTable(ChessGuiBasics.STANDARD_SQUARE_INFO_PANEL_TITLE, true, this);

    /**
     * Constructor, creating new InfoPanel
     * @param chessGame the ChessGame it belongs to (relevant for chess engine, movement and figure placement)
     */
    public InfoPanel(Chessgame chessGame){
        this.chessgame = chessGame;
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
     * Sets the list contents and displays them.
     * @param infoMap
     */
    public void displayHashmap(DataTable table, HashMap<String,String> infoMap) {
        table.resetTable();
        List<String> dataList = new ArrayList<>();
        infoMap.forEach((key, value) -> dataList.add(key+"\n"+value));
        dataList.sort(String::compareTo);
        dataList.sort(String::compareTo);
        dataList.forEach(keyValuePair -> {
            String[] pair = keyValuePair.split("\n");
            table.addRow(pair[0],pair[1]);
        });
    }

    /**
     * changes title of given DataTable
     * @param dataTable datatable to change title in
     * @param title new title (see DataTable.changeTitleExtra() for details)
     */
    public void displayTitle(DataTable dataTable, String title) {
        dataTable.changeTitleExtra(title);
    }

    /**
     * Calls row-highlighting on all relevant DataTables
     * @param key key of the row to highlight
     */
    public void highlightRowsInAllTables(String key) {
        boardData.highlightRow(key);
        squareData.highlightRow(key);
    }

    /// Input field methods:

    /**
     * 1. reads the command written into the commandInputField
     * 2. and forwards it to evaluateTextCommand()
     */
    private void executeInputFieldCommand() {
        String command = commandInputField.getText();
        commandInputField.setText("");

        addAsLastCommand( evaluateTextCommand(command) );
    }

    /**
     * Adds a text into the last commands.
     * The last commands array can't be bigger than 3!
     * @param command command to add
     */
    public void addAsLastCommand(String command) {
        lastTextCommands.add(0,command);
        if(lastTextCommands.size() > 3)
            lastTextCommands.remove(3);
        lastCommandsTextBox.setListData(lastTextCommands.toArray(new String[0]));
    }

    /**
     * Evaluates and executes the according action to the inputted command
     * @param commandline command to evaluate
     */
    private String evaluateTextCommand(String commandline) {
        String[] parted = commandline.split(" ");
        String command = parted[0].toLowerCase(Locale.ROOT);
        String attribute = null;
        if (parted.length > 1) {
            attribute = parted[1];
        }
        switch (command) {
            case "" -> noValidInput();
            case "move", "enginemove" -> letChessEngineMove();
            case "automove" -> {
                if ( attribute==null || attribute.length()==0)
                    chessgame.setAutoMove( !chessgame.isAutoMove() );  // toggle
                else if ( parted[1].equalsIgnoreCase("on") )
                    chessgame.setAutoMove(true);
                else if ( parted[1].equalsIgnoreCase("off") )
                    chessgame.setAutoMove(false);
                else
                    commandline += " -> ??";
            }
            case "reset", "resetboard" -> resetBoard();
            case "set" -> {
                if ( !chessgame.getChessEngine().setParam(parted[1], parted[2]) )
                    commandline += " -> failed";
            }
            case "position" -> {
                String subcommand = parted[1].toLowerCase(Locale.ROOT);
                switch (subcommand) {
                    case "startpos" -> resetBoard();
                    case "fen" -> chessgame.setBoardFromFen(commandline.split(" ", 3)[2]);
                }
            }
            default -> chessgame.setBoardFromFen(commandline);
        }
        infoHeader.setText(ChessGuiBasics.STANDARD_INFO_HEADER);
        return commandline;
    }

    public void resetBoard() {
        chessgame.setBoardFromFen(ChessBasics.FENPOS_INITIAL);
    }

    private void noValidInput() {
        infoHeader.setText(">no valid input<");
    }

    private void letChessEngineMove() {
        chessgame.executeEngineMove();
    }

    /**
     * Getter
     */
    public Chessgame getChessgame() {
        return chessgame;
    }
    public DataTable getBoardData() {
        return boardData;
    }
    public DataTable getSquareData() {
        return squareData;
    }
}
