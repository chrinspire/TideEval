/*
 *     TideEval - Wired New Chess Algorithm
 *     Copyright (C) 2023 Christian Ensel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.ensel.chessgui.sidepanel;

import de.ensel.chessgui.control.ChessGuiBasics;
import de.ensel.chessgui.control.Chessgame;

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
    private final String standardTitle;
    private final boolean clickable;

    private final JLabel title;
    private final JPanel panel;
    private final List<Row> rows;

    public DataTable(String titleText, boolean clickable, InfoPanel infoPanel) {
        this.clickable = clickable;
        this.standardTitle = titleText;
        this.infoPanel = infoPanel;
        title = new JLabel();
        panel = new JPanel();
        rows = new LinkedList<>();
        title.setText(standardTitle);
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

    /**
     * Highlight one row with the given key.
     * Remove highlights that don't equal the key.
     * Remove all highlights if key = null.
     * @param key key of the row to highlight
     */
    public void highlightRow(String key) {
        for(Row row : rows) {
            if (key == null || !row.getRowKey().equals(key)) {
                row.colorRow(ChessGuiBasics.COLOR_NEUTRAL);
            }
            else {
                row.colorRow(ChessGuiBasics.MARKED_COLOR);
            }
        }
    }

    /**
     * Adds extra text to the title, separated with a "-"
     * @param titleExtra extra text
     */
    public void changeTitleExtra(String titleExtra) {
        title.setText(standardTitle + " - " + titleExtra);
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

        private final JPanel panel;
        private final JTextPane namePane;
        private final JTextPane dataPane;

        public Row(String name, String data, DataTable dataTable) {
            this.panel = new JPanel();
            this.namePane = new JTextPane();
            this.dataPane = new JTextPane();
            namePane.setEditable(false);
            dataPane.setEditable(false);
            panel.setMaximumSize(new Dimension(1000, 200));
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            namePane.setText(name);
            dataPane.setText(data);
            namePane.addMouseListener(new RowListener(dataTable.getInfoPanel().getChessgame(), this));
            namePane.setMaximumSize(new Dimension(panel.getMaximumSize().width / 2 - 20,panel.getMaximumSize().height));
            dataPane.setMaximumSize(namePane.getMaximumSize());
            panel.add(namePane);
            panel.add(dataPane);
        }

        /**
         * compares key to key of row and colors the row accordingly
         * @param color new background color for row
         */
        public void colorRow(Color color) {
            getNamePane().setBackground(color);
            getDataPane().setBackground(color);
        }

        /**
         * get the key of the row
         * @return key
         */
        public String getRowKey() {
            return namePane.getText();
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

        /**
         * RowListener to detect mouse activities in a row
         */
        private static class RowListener implements MouseListener {

            private final Row row;
            private final Chessgame chessgame;

            public RowListener(Chessgame chessgame, Row row) {
                this.chessgame = chessgame;
                this.row = row;
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                chessgame.paintAllSquaresByKey(row.getRowKey());
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
