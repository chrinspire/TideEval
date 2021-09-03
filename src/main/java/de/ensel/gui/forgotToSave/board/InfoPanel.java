package de.ensel.gui.forgotToSave.board;

import de.ensel.gui.forgotToSave.control.Chessgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * This panel is responsible for accepting user commandos and displaying information about the game from the chess engine.
 */
public class InfoPanel extends JPanel {

    /**
     * static attributes:
     */
    private static final String STANDARD_INFO_HEADER = "Enter Command:";

    /**
     * logic attributes:
     */
    private final Chessgame chessgame;

    /**
     * panels:
     */
    private JTextPane infoHeader;
    private JTextField commandInputField;
    private JList<String> lastCommandsTextBox;
    private List<String> lastTextCommands = new LinkedList<>();
    private DataTable boardData = new DataTable( "___Board  Data___");
    private DataTable squareData = new DataTable("___Square Data___");

    /**
     * Constructor, creating new InfoPanel
     * @param chessGame the ChessGame it belongs to (relevant for chess engine, movement and figure placement)
     */
    public InfoPanel(Chessgame chessGame){
        this.chessgame = chessGame;
        infoHeader = new JTextPane();
        infoHeader.setEditable(false);
        infoHeader.setText(STANDARD_INFO_HEADER);
        infoHeader.setMaximumSize(new Dimension(BoardPanel.BOARD_PIXEL_SIZE,60));
        commandInputField = new JTextField();
        commandInputField.setMaximumSize(new Dimension(BoardPanel.BOARD_PIXEL_SIZE,60));
        lastCommandsTextBox = new JList<>();
        lastCommandsTextBox.setMaximumSize(new Dimension(BoardPanel.BOARD_PIXEL_SIZE,60));
        lastCommandsTextBox.setFixedCellWidth(0);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(infoHeader);
        this.add(commandInputField);
        this.add(lastCommandsTextBox);
        this.add(boardData.getPanel());
        this.add(squareData.getPanel());
        setupAllListeners();
        this.setMaximumSize(new Dimension(BoardPanel.BOARD_PIXEL_SIZE,10000));
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
        infoHeader.setText(STANDARD_INFO_HEADER);
    }

    private void resetBoard() {
        chessgame.getBoardPanel().setStandardBoard();
    }

    private void noValidInput() {
        infoHeader.setText(">no valid input<");
    }

    public void displayBoardInfo() {
        String[] data = chessgame.getChessEngine().getBoardInfo().split("\n");
        boardData.resetTable();
        for (String row : data) {
            boardData.addRow(row, "");
        }
    }

    private void displaySquareInfo(String square) {
        String[] data = chessgame.getChessEngine().getSquareInfo(square).split("\n");
        squareData.resetTable();
        for (String row : data) {
            squareData.addRow(row, "");
        }
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

    /**
     * Data table to display chessboard data
     */
    private static class DataTable {
        private final JLabel title;
        private final JPanel panel;
        private final List<Row> rows;
        public DataTable() {
            title = new JLabel();
            panel = new JPanel();
            rows = new LinkedList<>();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        }
        public DataTable(String titleText) {
            title = new JLabel();
            panel = new JPanel();
            rows = new LinkedList<>();
            title.setText(titleText);
            panel.setMaximumSize(new Dimension(1000,1000));
            panel.add(title);
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        }

        /**
         * add a new row with name and data cell
         * @param name new row name
         * @param data data for row
         */
        public void addRow(String name, String data) {
            addRow(new Row(name, data));
        }
        public void addRow(Row row) {
            rows.add(row);
            panel.add(row.getPanel());
            panel.validate();
        }

        /**
         * change the data in one row
         * @param row row number
         * @param newData new data for the row
         */
        public void changeDataInRow(int row, String newData) {
            if(row < rows.size())
                rows.get(row).getDataPane().setText(newData);
        }

        /**
         * change the data in all rows with the given row name
         * @param name row name
         * @param newData new data for the row
         */
        public void changeDataByName(String name, String newData) {
            rows.stream()
                    .filter(row -> row.getNamePane().getText().equals(name))
                    .forEach(row -> row.getDataPane().setText(newData));
        }

        /**
         * remove all rows from table
         */
        public void resetTable() {
            rows.clear();
            panel.removeAll();
            panel.add(title);
            panel.validate();
        }

        public JPanel getPanel() {
            return panel;
        }
        public List<Row> getRows() {
            return rows;
        }
    }

    /**
     * One Row to realize the data table for the board and the squares
     */
    public static class Row {
        private final JPanel panel;
        private final JTextPane namePane;
        private final JTextPane dataPane;

        public Row(String name, String data) {
            this.panel = new JPanel();
            this.namePane = new JTextPane();
            this.dataPane = new JTextPane();
            namePane.setEditable(false);
            dataPane.setEditable(false);
            panel.setMaximumSize(new Dimension(1000, 200));
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            namePane.setText(name);
            dataPane.setText(data);
            namePane.addMouseListener(new Row.PanelListener());
            panel.add(namePane);
            panel.add(dataPane);
        }

        public JPanel getPanel() {
            return panel;
        }

        public JTextPane getNamePane() {
            return namePane;
        }

        public JTextPane getDataPane() {
            return dataPane;
        }

        /**
         * PanelListener to detect mouse activities in a row
         */
        private static class PanelListener implements MouseListener {
            @Override
            public void mouseClicked(MouseEvent event) {
                Object source = event.getSource();
                if (source instanceof JPanel panelPressed) {
                    panelPressed.setBackground(Color.blue);
                }
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                System.out.println("Mouse entered row");
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {

            }
        }
    }
}
