package de.ensel.gui.forgotToSave.control;

import java.awt.*;

public abstract class ChessGuiBasics {

    // --- STATICS ---

    /**
     * CHESS attributes:
     * - BOARD_SIZE             -> length of one sie of the board
     */
    public static final int BOARD_SIZE = 8;

    /**
     * GUI attributes:
     * - STANDARD_WINDOW_SIZE   -> start size of the window
     * - BOARD_PIXEL_SIZE       -> size of board in window
     * - COLOR_1                -> first color of the board
     * - COLOR_2                -> second color of the board
     */
    public static final Dimension STANDARD_WINDOW_SIZE = new Dimension(1600,850);
    public static final int BOARD_PIXEL_SIZE = 800;
    public static final Color COLOR_1 = new Color(0xFFFFFF);
    public static final Color COLOR_2 = new Color(0x17912E);
    public static final Color ERROR_COLOR = new Color(0xC91818);

    /**
     * TEXT attributes: TODO move to Resource Bundle
     * - WINDOW_TITLE           -> title of the whole window
     * - STANDARD_INFO_HEADER   -> standard text for header over the command input field
     */
    public static final String WINDOW_TITLE = "Chess-Engine Gui v.0.1";
    public static final String STANDARD_INFO_HEADER = "Enter Command:";


    // --- CONVERTERS ---

    /**
     * Converts the given coordinates (files, ranks) to an executable moveString
     * @param fromRank rank of origin square
     * @param fromFile file of origin square
     * @param toRank rank of destination square
     * @param toFile file of destination square
     * @return move-string
     */
    public static String coordinatesToMove(int fromRank, int fromFile, int toRank, int toFile) {
        return ""+ rankToLetter(fromRank)+(fromFile * -1 + 8)+ rankToLetter(toRank)+(toFile * -1 + 8);
    }

    /**
     * Converts the given rank to the according letter (a-h)
     * @param rank rank-number
     * @return rank-letter
     */
    public static char rankToLetter(int rank) {
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
}
