/*
 * Copyright (c) 2022.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.Objects;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBasics.coordinateString2Pos;

/** simple class to express a Chess move from a square position (0-63) to another one.
 *  Optionally the from or to position can be set to the placeholder ANY from ChessBasics.
 */
public class Move {
    protected int from;
    protected int to;
    protected int promotesTo;

    public Move(int from, int to) {
        this.from = from;
        this.to = to;
        promotesTo = EMPTY;
    }

    public Move(int from, int to, int promotesToPceTypee) {
        this.from = from;
        this.to = to;
        promotesTo = promotesToPceTypee;
    }

    /**
     * Allows only simple notation like FEN-moves a1b2 or with dash a1-b2.
     * Appended Piece type letters points out which piece to promote a pawn to.
     * @param move
     */
    public Move(String move) {
        if ( move.length()>=4
                && isFileChar( move.charAt(0)) && isRankChar(move.charAt(1) )
                && isFileChar( move.charAt(2)) && isRankChar(move.charAt(3) )
        ) {
            // move-string starts with a lower case letter + a digit and is at least 4 chars long
            // --> standard fen-like move-string, like "a1b2"
            from = coordinateString2Pos(move, 0);
            to = coordinateString2Pos(move, 2);
            char promoteToChar = move.length() > 4 ? move.charAt(4) : 'q';
            promotesTo= getPceTypeFromPromoteChar(promoteToChar);
            //System.out.format(" %c,%c %c,%c = %d,%d-%d,%d = %d-%d\n", input.charAt(0), input.charAt(1), input.charAt(2), input.charAt(3), (input.charAt(0)-'A'), input.charAt(1)-'1', (input.charAt(2)-'A'), input.charAt(3)-'1', frompos, topos);
        }
        else  if ( move.length()>=5
                && isFileChar( move.charAt(0)) && isRankChar(move.charAt(1) )
                && move.charAt(2)=='-'
                && isFileChar( move.charAt(3)) && isRankChar(move.charAt(4) )
        ) {
            // move-string starts with a lower case letter + a digit + a '-' and is at least 5 chars long
            // --> simple move-string, like "a1-b2"
            from = coordinateString2Pos(move, 0);
            to = coordinateString2Pos(move, 3);
            char promoteToChar = move.length() > 5 ? move.charAt(5) : 'q';
            promotesTo = getPceTypeFromPromoteChar(promoteToChar);
            //System.out.format(" %c,%c %c,%c = %d,%d-%d,%d = %d-%d\n", input.charAt(0), input.charAt(1), input.charAt(2), input.charAt(3), (input.charAt(0)-'A'), input.charAt(1)-'1', (input.charAt(2)-'A'), input.charAt(3)-'1', frompos, topos);
        }
        else {
            this.from = -64;
            this.to = -64;
            promotesTo = EMPTY;
        }
    }


    public int from() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int to() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "" +
                ChessBasics.squareName( from) +
                "-" + ChessBasics.squareName(to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return from == move.from && to == move.to && promotesTo == move.promotesTo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    public boolean isMove() {
        return from >= 0 && from < NR_SQUARES
               && to >= 0 && to < NR_SQUARES;
    }

    public void setPromotesTo(int pceType) {
        promotesTo = pceType;
    }

    public int promotesTo() {
        return promotesTo;
    }

    public int direction() {
        return calcDirFromTo(from,to);
    }
}
