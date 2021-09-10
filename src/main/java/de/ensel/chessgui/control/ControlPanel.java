package de.ensel.chessgui.control;

import de.ensel.chessgui.board.InfoPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel (on the top right) to provide basic button controls
 * TODO implement, then add to InfoPanel (uncomment command)
 */
public class ControlPanel extends JPanel {

    private InfoPanel infoPanel;

    private JButton resetButton;

    public ControlPanel(InfoPanel infoPanel) {
        this.infoPanel = infoPanel;
        resetButton = new JButton("Reset Board");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infoPanel.resetBoard();
            }
        });
        resetButton.setVisible(true);
    }
}
