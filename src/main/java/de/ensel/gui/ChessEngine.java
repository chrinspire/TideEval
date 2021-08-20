package de.ensel.gui;

public interface ChessEngine {
    /**
     * Informs ChessEngine of a move made on the board
     * @param move move to execute
     */
    void doMove(String move);

    /**
     * Get a move from the ChessEngine
     * @return any move, null if for any reason no move can be returned
     */
    String getMove();

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
    String getBoardInfo();

    /**
     * Gets information for one field to display
     * @return
     */
    String getSquareInfo(String field);
}
