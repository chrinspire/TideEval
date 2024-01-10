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

package de.ensel.tideeval;

import org.jetbrains.annotations.Nullable;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBasics.coordinateString2Pos;
import static de.ensel.tideeval.ChessBoard.DEBUGMSG_MOVEEVAL;
import static de.ensel.tideeval.ChessBoard.debugPrint;

/** simple class to express a Chess move from a square position (0-63) to another one.
 *  Optionally the from or to position can be set to the placeholder ANY from ChessBasics.
 */
public class Move {
    protected int from;
    protected int to;
    protected int promotesTo;
    private boolean isBasicallyLegal = false;

    //// Constructors

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
            if ( move.length() > 4 )
                promotesTo = getPceTypeFromPromoteChar(move.charAt(4));
            else
                promotesTo = EMPTY;
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
            if ( move.length() > 5 )
                promotesTo = getPceTypeFromPromoteChar(move.charAt(5));
            else
                promotesTo = EMPTY;
            //System.out.format(" %c,%c %c,%c = %d,%d-%d,%d = %d-%d\n", input.charAt(0), input.charAt(1), input.charAt(2), input.charAt(3), (input.charAt(0)-'A'), input.charAt(1)-'1', (input.charAt(2)-'A'), input.charAt(3)-'1', frompos, topos);
        }
        else {
            this.from = -64;
            this.to = -64;
            promotesTo = EMPTY;
        }
    }

    public Move(Move origin) {
        this.from = origin.from;
        this.to = origin.to;
        this.promotesTo = origin.promotesTo;
        this.isBasicallyLegal = origin.isBasicallyLegal;
    }


    //// getter + simple information

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

    public boolean isMove() {
        return from >= 0 && from < NR_SQUARES
                && to >= 0 && to < NR_SQUARES;
    }

    public int promotesTo() {
        return promotesTo==EMPTY? QUEEN : promotesTo;
    }

    public int direction() {
        return calcDirFromTo(from,to);
    }


    //// setter

    public void setPromotesTo(int pceType) {
        promotesTo = pceType;
    }


    ////

    @Override
    public String toString() {
        return "" +
                ChessBasics.squareName( from)
            // for debugging only    + (isBasicallyALegalMove() ? "" : "'")
                + ChessBasics.squareName(to)
                + ( promotesTo!=EMPTY  ? Character.toLowerCase(fenCharFromPceType(promotesTo)) : "");
    }

    /**
     * std.equals(), hint: does not compare isLegal flag
     * @param o other move to compare with
     * @return true if members from, to and promotesTo are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return from == move.from && to == move.to && promotesTo == move.promotesTo;
    }

    @Override
    public int hashCode() {
        return hashId();
    }

    public Integer hashId() {
        return (from << 8) + to;
    }

    /**
     * move sequence factory :-)
     */
    @Nullable
    public static Move[] getMoves(String movesString) {
        String[] moveStrings = movesString.trim().split(" ");
        if (moveStrings==null || moveStrings.length==0 || moveStrings.length==1 && moveStrings[0].length()==0)
            return null;
        int start = moveStrings[0].equalsIgnoreCase("moves") ? 1 : 0;
        //System.out.println("Parsen der moves des FEN-Strings " + fenString
        //                    + ": "+ (moveStrings.length-start) + " moves expected");
        Move[] moves = new Move[moveStrings.length-start];
        for (int m = start; m< moveStrings.length; m++) {
            //System.out.println("<" +  moveStrings[m] + ">");
            if ( moveStrings[m]==null || moveStrings[m].length()==0) {
                start++;  // skip an empty one
                continue;
            } else if ( moveStrings[m].length()<4 || moveStrings[m].length()>5 ) {
                //System.err.println("**** Fehler beim Parsen der moves am Ende des FEN-Strings " + fenString);
                return null;
            }
            moves[m - start] = new Move(moveStrings[m]);
        }
        return moves;
    }


    public int dir() {
        return calcDirFromTo(from(), to());
    }

    public boolean isBasicallyLegal() {
        return isBasicallyLegal;
    }

    public void setBasicallyLegal() {
        isBasicallyLegal = true;
    }
}

