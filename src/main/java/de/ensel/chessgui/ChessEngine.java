package de.ensel.chessgui;

import java.util.HashMap;

public interface ChessEngine {
    /**
     * Informs ChessEngine of a move made on the board
     * @param move move to execute
     */
    boolean doMove(String move);

    /**
     * Get a move from the ChessEngine
     * @return any move, null if for any reason no move can be returned
     */
    String getMove();
    // TODO: must be divided in two functions:
    //  -> go(...t.b.d.-parameters for e.g. remaining time + increment
    //  -> stop()

    /**
     * Informs ChessEngine of a new Board
     * @param fen FEN-String of the new chess board
     */
    void setBoard(String fen);

    /**
     * Gets the current board from the ChessEngine as:
     * @return FEN-String of chess board
     */
    String getBoard();

    /**
     * Gets general board information to display
     * @return
     */
    // TODO: replace Info-String by table of fieldname+value pairs
    HashMap<String,String> getBoardInfo();

    /**
     * Gets information for one field to display
     * @return
     */
    // TODO: replace Info-String by table of fieldname+value pairs
    HashMap<String,String> getSquareInfo(String square, String squareFrom);

    // TODO: Callback-possibility for ChessEngine to UI, esp. for "info"s and end of calculation "bestmove".
}
