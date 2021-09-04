package de.ensel.gui.forgotToSave.board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

/**
 * Data table to display chessboard data
 */
class DataTable {
    private final InfoPanel infoPanel;
    private final boolean clickable;

    private final JLabel title;
    private final JPanel panel;
    private final List<Row> rows;

    public DataTable(String titleText, boolean clickable, InfoPanel infoPanel) {
        this.clickable = clickable;
        this.infoPanel = infoPanel;
        title = new JLabel();
        panel = new JPanel();
        rows = new LinkedList<>();
        title.setText(titleText);
        panel.setMaximumSize(new Dimension(1000, 1000));
        panel.add(title);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    }

    /**
     * add a new row with name and data cell
     *
     * @param name new row name
     * @param data data for row
     */
    public void addRow(String name, String data) {
        addRow(new Row(name, data, this));
    }

    public void addRow(Row row) {
        rows.add(row);
        panel.add(row.getPanel());
        panel.validate();
    }

    /**
     * change the data in one row
     *
     * @param row     row number
     * @param newData new data for the row
     */
    public void changeDataInRow(int row, String newData) {
        if (row < rows.size())
            rows.get(row).getDataPane().setText(newData);
    }

    /**
     * change the data in all rows with the given row name
     *
     * @param name    row name
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

    public boolean isClickable() {
        return clickable;
    }

    public JPanel getPanel() {
        return panel;
    }

    public List<Row> getRows() {
        return rows;
    }

    public InfoPanel getInfoPanel() {
        return infoPanel;
    }

    /**
     * One Row to realize the data table for the board and the squares
     */
    public static class Row {

        private final DataTable dataTable;

        private final JPanel panel;
        private final JTextPane namePane;
        private final JTextPane dataPane;

        public Row(String name, String data, DataTable dataTable) {
            this.panel = new JPanel();
            this.namePane = new JTextPane();
            this.dataPane = new JTextPane();
            this.dataTable = dataTable;
            namePane.setEditable(false);
            dataPane.setEditable(false);
            panel.setMaximumSize(new Dimension(1000, 200));
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            namePane.setText(name);
            dataPane.setText(data);
            namePane.addMouseListener(new Row.PanelListener(this));
            panel.add(namePane);
            panel.add(dataPane);
        }

        /**
         * color all squares according to this row
         */
        public void colorSquares(){
            if (dataTable.isClickable()) {
                dataTable.getInfoPanel().getChessgame().getBoardPanel().paintSquaresByKey(namePane.getText());
            }
        }

        /**
         * Getter
         */
        public JPanel getPanel() {
            return panel;
        }
        public JTextPane getNamePane() {
            return namePane;
        }
        public JTextPane getDataPane() {
            return dataPane;
        }
        public DataTable getDataTable() {
            return dataTable;
        }

        /**
         * PanelListener to detect mouse activities in a row
         */
        private static class PanelListener implements MouseListener {

            private final Row row;

            public PanelListener(Row row) {
                this.row = row;
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                row.colorSquares();
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                row.getNamePane().setBackground(row.getNamePane().getBackground().darker());
                row.getDataPane().setBackground(row.getDataPane().getBackground().darker());
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                row.getNamePane().setBackground(row.getNamePane().getBackground().brighter());
                row.getDataPane().setBackground(row.getDataPane().getBackground().brighter());
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
