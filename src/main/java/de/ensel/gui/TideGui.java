package de.ensel.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

public class TideGui extends JFrame{
    private JPanel mainPanel;
    private JLabel chessboard;
    private JTable dataTable;
    private JTextField commandTextField;
    private JButton commandButton;
    private JList lastCommandsList;

    private final List<String> pastCommands;
    private int[] board;

    public TideGui() {
        setContentPane(mainPanel);
        setTitle("TideEval");
        setSize(1400,1320);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        pastCommands = new LinkedList<>();
        board = new int[64];

        commandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = commandTextField.getText();
                evaluateCommand(command);
                commandTextField.setText("");
                pastCommands.add(0,command);
                if(pastCommands.size() > 3)
                    pastCommands.remove(3);
                lastCommandsList.setListData(pastCommands.toArray());
            }
        });
    }

    public static void main(String[] args) {
        TideGui frame = new TideGui();
    }

    public void setBoardFromFen(String fen) {
        //TODO
    }

    private void evaluateCommand(String command) {
        //TODO
    }
}
