package de.ensel.chessgui.control;

import java.awt.*;

import static java.lang.Math.*;
import static java.lang.Math.max;

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
    public static final Color COLOR_NEUTRAL = new Color(0xFFFFFF);
    public static final Color COLOR_1 = new Color(0xFFFFFF);
    public static final Color COLOR_2 = new Color(0x17912E);
    public static final Color ERROR_COLOR = new Color(0x9D2F2F);
    public static final Color MARKED_COLOR = new Color(0x5DAB6D);

    /**
     * TEXT attributes: TODO move to Resource Bundle
     * - WINDOW_TITLE           -> title of the whole window
     * - STANDARD_INFO_HEADER   -> standard text for header over the command input field
     */
    public static final String WINDOW_TITLE = "Chess-Engine Gui v.0.1";
    public static final String STANDARD_INFO_HEADER = "Enter Command:";
    public static final String STANDARD_BOARD_INFO_PANEL_TITLE = "Board Data";
    public static final String STANDARD_SQUARE_INFO_PANEL_TITLE = "Square Data";


    // --- CONVERTERS ---

    /**
     * Converts the given coordinates (files, ranks) to an executable moveString
     * @param originIndex index of origin square
     * @param destinationIndex index of destination square
     * @return move-string
     */
    public static String coordinatesToMove(int originIndex, int destinationIndex) {
        return ""+ rankToLetter(originIndex % 8)+((originIndex / 8) * -1 + 8)+ rankToLetter(destinationIndex % 8)+((destinationIndex / 8) * -1 + 8);
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

    public static String indexToSquare(int index) {
        return rankToLetter(index % 8) + "" + (-1 * (index / 8 - 8));
    }

    public static int SquareToIndex(String square) {
        return 0;
    }

    /**
     * Get color from a given value
     * @param value value from key
     * @return square color
     */
    public static Color getColorFromKeyValue(String value) {
        int v = 0;
        if (value!=null)
            try {
                v = Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                v = value.hashCode();
            }

        if (v == 0  || v>=Integer.MAX_VALUE-3) {
            return new Color(110, 110, 110);
        }
        if (v > 0 && v <= 10) {
            return new Color(200 - 15*v,90 - 5*v,255 - v * 14);
        }


        if (v > 0 && v <= 200) {
            return new Color(100 + v/4,100 + v/3,40 - v/20);
        }
        if (v < 0 && v >= -200) {
            v = -v;
            return new Color(40 - v/20,100 + v/3,100 + v/4);
        }

        if (v > 0 && v <= 2000) {
            return new Color(150 + v/20,150 + v/20,40 - v/50 +(v%20));
        }
        if (v < 0 && v >= -2000) {
            v = -v;
            return new Color(40 - v/50 +(v%20),150 + v/20,150 + v/20);
        }

        if (v > 2000) {
            int n = (int)sqrt((v-2000d)/2d);
            return new Color(255-((v/4)%20),max(0, 255-n), min(255, n/4));
        }
        //if (v < -2000) {
        int n = (int)sqrt(-(v+2000d)/2d);
        return new Color(min(255, n/4) ,max(0, 255-n), 255-((v/4)%20));
        //}
    }
}
