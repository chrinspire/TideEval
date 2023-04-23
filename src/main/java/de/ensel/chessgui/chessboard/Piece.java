package de.ensel.chessgui.chessboard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.Objects;

/**
 * One chess Figure
 */
public enum Piece {
    EMPTY (null, ' '),
    WHITE_PAWN (getImageFromPath("pieces/white_pawn.png"), '*'),
    WHITE_BISHOP (getImageFromPath("pieces/white_bishop.png"), 'B'),
    WHITE_KNIGHT (getImageFromPath("pieces/white_knight.png"), 'N'),
    WHITE_ROOK (getImageFromPath("pieces/white_rook.png"), 'R'),
    WHITE_QUEEN (getImageFromPath("pieces/white_queen.png"), 'Q'),
    WHITE_KING (getImageFromPath("pieces/white_king.png"), 'K'),
    BLACK_PAWN (getImageFromPath("pieces/black_pawn.png"), 'o'),
    BLACK_BISHOP (getImageFromPath("pieces/black_bishop.png"), 'b'),
    BLACK_KNIGHT (getImageFromPath("pieces/black_knight.png"), 'n'),
    BLACK_ROOK (getImageFromPath("pieces/black_rook.png"), 'r'),
    BLACK_QUEEN (getImageFromPath("pieces/black_queen.png"), 'q'),
    BLACK_KING (getImageFromPath("pieces/black_king.png"), 'k');

    private final Image image;
    private final char asciiSymbol;

    Piece(Image image, char asciiSymbol) {
        this.image = image;
        this.asciiSymbol = asciiSymbol;
    }

    public Image getImage() {
        return image;
    }

    public char getAsciiSymbol() {
        return asciiSymbol;
    }

    public static Piece getFigureFromAsciiSymbol(char asciiSymbol) {
        switch (asciiSymbol) {
            case 'o', 'P', '♙' -> {return Piece.WHITE_PAWN;}
            case 'L', 'B', '♗' -> {return Piece.WHITE_BISHOP;}
            case 'S', 'N', '♘' -> {return Piece.WHITE_KNIGHT;}
            case 'T', 'R', '♖' -> {return Piece.WHITE_ROOK;}
            case 'D', 'Q', '♕' -> {return Piece.WHITE_QUEEN;}
            case 'K', '♔' -> {return Piece.WHITE_KING;}
            case '*', 'p', '♟' -> {return Piece.BLACK_PAWN;}
            case 'l', 'b', '♝' -> {return Piece.BLACK_BISHOP;}
            case 's', 'n', '♞' -> {return Piece.BLACK_KNIGHT;}
            case 't', 'r', '♜' -> {return Piece.BLACK_ROOK;}
            case 'd', 'q', '♛' -> {return Piece.BLACK_QUEEN;}
            case 'k', '♚' -> {return Piece.BLACK_KING;}
            default -> {return Piece.EMPTY;}
        }
    }

    public static Image getImageFromPath(String path) {
        try {
            return ImageIO.read(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(path)));
        }
        catch (Exception ignored){
            return null;
        }
    }

}