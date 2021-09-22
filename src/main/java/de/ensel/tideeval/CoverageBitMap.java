/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;

abstract class CoverageBitMap {

    // Idea: Do not save too much memory,
    //       but make the difference between White+Black coverage computable
    // Use "bit piles" to count number of same pieces covering a field.
    // 13 bits:                                     PP_NNB_RRQQ_LTTK
    static final int NOTCOVERED = 0;
    static final int COVER_MASK          = 0b11_111_1111_1111;
    static final int PAWN_COVER_MASK     = 0b11_000_0000_0000;
    static final int PAWN_COVER_1        = 0b01_000_0000_0000;
    static final int PAWN_SHIFT = 11;
    static final int KNIGHT_COVER_MASK   = 0b00_110_0000_0000;
    static final int KNIGHT_COVER_1      = 0b00_010_0000_0000;
    static final int KNIGHT_SHIFT = 9;
    static final int BISHOP_COVER_MASK   = 0b00_001_0000_0000;
    static final int BISHOP_COVER_1      = 0b00_001_0000_0000;
    static final int BISHOP_SHIFT = 8;
    static final int ROOK_COVER_MASK     = 0b00_000_1100_0000;
    static final int ROOK_COVER_1        = 0b00_000_0100_0000;
    static final int ROOK_SHIFT = 6;
    static final int QUEEN_COVER_MASK    = 0b00_000_0011_0000;  // max 3 Queens...
    static final int QUEEN_COVER_1       = 0b00_000_0001_0000;
    static final int QUEEN_SHIFT = 4;
    static final int BISHOP_BQ_COVER_MASK= 0b00_000_0000_1000;
    static final int BISHOP_BQ_COVER_1   = 0b00_000_0000_1000;
    static final int BISHOP_BQ_SHIFT = 3;
    static final int ROOK_BQ_COVER_MASK  = 0b00_000_0000_0110;
    static final int ROOK_BQ_COVER_1     = 0b00_000_0000_0010;
    static final int ROOK_BQ_SHIFT = 1;
    static final int KING_COVER_MASK     = 0b00_000_0000_0001;
    static final int KING_COVER_1        = 0b00_000_0000_0001;
    static final int CBM_LENGTH = 13;

    // additional Piece types
    static final int ROOK_BEHIND_QUEEN = 8;
    static final int BISHOP_BEHIND_QUEEN = 9;

    static final int[] pceTypeCoverMask = { NOTCOVERED, KING_COVER_MASK, QUEEN_COVER_MASK, ROOK_COVER_MASK, BISHOP_COVER_MASK, KNIGHT_COVER_MASK, PAWN_COVER_MASK, COVER_MASK, ROOK_BQ_COVER_MASK, BISHOP_BQ_COVER_MASK };
    static final int[] pceTypeCover1 = { NOTCOVERED, KING_COVER_1,    QUEEN_COVER_1,    ROOK_COVER_1,    BISHOP_COVER_1,    KNIGHT_COVER_1,    PAWN_COVER_1,    0,          ROOK_BQ_COVER_1,    BISHOP_BQ_COVER_1 };
    static final int[] pceTypeShift =  { NOTCOVERED, 0,               QUEEN_SHIFT,      ROOK_SHIFT,      BISHOP_SHIFT,      KNIGHT_SHIFT,      PAWN_SHIFT,      0,          ROOK_BQ_SHIFT,      BISHOP_BQ_SHIFT };


    // has a significant deficiency:
    // Rook, Bishop or Queen cannot be marked as attacking through such pieces of opposite color (although they are in beating direction:
    // e.g. R->r->q : R cannot beet q directly, but after q gets between of something else, and r beats that oen back, then R also attacks the r (on the original q-square)
    // This situation cannot be coded with the current bitmap and thus needs to be avoided. Also the ClashResolve-Method cannot cope with that.
    // But this hopefully does not matter much. the middle "r" (in the example above) will be marked as pinned, so the evaluation and conclusions should still be ok.
    static int addPceTypeToCoverage(int pceType, int/*CBM*/ oldcoveredvalue) {
        if (pceType<0) {
            System.err.println("*** Fehler: Versuche gegnerische Figur zu CBM hinzufügen.");
            return oldcoveredvalue;
        } else if (pceType==EMPTY ) {
            return oldcoveredvalue;
        }
        int coverMask = pceTypeCoverMask[pceType];
        int cover1 = pceTypeCover1[pceType];
        if ( (oldcoveredvalue & coverMask) == coverMask ) {  // already full...
            debugPrint(DEBUGMSG_CBM_ERRORS,String.format("*** Fehler: Versuche zu viele Figuren zu CBM hinzufügen: %d zu %s.\n", pceType, cbmToFullString(oldcoveredvalue) ) );
            return oldcoveredvalue;
        }
        return oldcoveredvalue + cover1;
    }

    public static boolean containsPceTypeInCoverage(int pceType, int/*CBM*/ cbm) {
        if (pceType<0) {
            System.err.println("*** Fehler: Versuche gegnerische Figur in CBR zu suchen.");
            return false;
        } else if (pceType==EMPTY ) {
            return false;
        }
        int coverMask = pceTypeCoverMask[pceType];
        return (cbm & coverMask) != 0;
    }

    public static int removePceTypeFromCoverageSaferWay(int pceType, int/*CBM*/ oldcoveredvalue) {
        // like removeFigureNrFromCoverage, but removes BbQ/RbQ if it is called with B/R but B/R is already empty.
        // this is encessary for cases, where (unlike in clashCalculation) it cannot be known if the pceType is behind queen or not, like in calcMySupportFromPlayerPerspective

        if (pceType==BISHOP && ((oldcoveredvalue & pceTypeCoverMask[BISHOP])==0)
                && ((oldcoveredvalue & pceTypeCoverMask[BISHOP_BEHIND_QUEEN])!=0)) {
            return removePceTypeFromCoverage( BISHOP_BEHIND_QUEEN , oldcoveredvalue);
        } else if (pceType==ROOK && ((oldcoveredvalue & pceTypeCoverMask[ROOK])==0)
                && ((oldcoveredvalue & pceTypeCoverMask[ROOK_BEHIND_QUEEN]) != 0)) {
            return removePceTypeFromCoverage( ROOK_BEHIND_QUEEN , oldcoveredvalue);
        }
        return removePceTypeFromCoverage( pceType , oldcoveredvalue);
    }


    public static int removePceTypeFromCoverage(int pceType, int/*CBM*/ oldcoveredvalue) {
        if (pceType<0) {
            System.err.format("*** Fehler: Versuche gegnerische Figur aus CBM zu entfernen: %d aus %s.\n", pceType, cbmToFullString(oldcoveredvalue) );
            return oldcoveredvalue;
        } else if (pceType==EMPTY ) {
            return oldcoveredvalue;
        }
        int coverMask = pceTypeCoverMask[pceType];
        int cover1 = pceTypeCover1[pceType];
        if ( (oldcoveredvalue & coverMask) == 0 ) {  // already empty ...
            // TODO!!!: dies kommt oft vor für:  *** Fehler: Versuche zu viele Figuren zu CBM hinzufügen: 4 zu [0000100000000]
            //  System.out.format("*** Fehler: Versuche zu viele Figuren aus CBM zu entfernen: %d von %s.\n", pceType, toFullString(oldcoveredvalue) );
            return oldcoveredvalue;
        }
        int resCBM = oldcoveredvalue - cover1;   // this is the whole magic of removing a figure from the bit mask :-)
        if (pceType == QUEEN ) {
            // a Queen was removed, check if bishop-behind queen or rock-behind-queen needs to be counted as a normal bishop/rook now.
            //System.out.format("*** Entferne: %s aus %s ergibt %s. ", Figure.giveFigureNameByNr(pceType), toFullString(oldcoveredvalue), toFullString(resCBM) );
            int hiddenPieceCoverage =  ( resCBM & (pceTypeCoverMask[BISHOP_BEHIND_QUEEN]) ) >> BISHOP_BQ_SHIFT;
            //System.out.format(" hiddenPieceCoverage= %d from %s.  ", hiddenPieceCoverage, toFullString(resCBM) );
            if (hiddenPieceCoverage>0 && ( (resCBM & pceTypeCoverMask[BISHOP]) == 0 ) )  // es gibt mind einen BbQ, zähle das bei den Bishops dazu... daher prüfe vorher ob der 0 ist, denn es kann nicht 2 Bs eines Spielers mit derselben Feldfarbe geben
                resCBM = (resCBM + (hiddenPieceCoverage<<BISHOP_SHIFT) ) & (~(pceTypeCoverMask[BISHOP_BEHIND_QUEEN])) ;
            else {
                // es gab keine Läufer, versuche es nochmal mit Türmen.  (Wenn es Läufer gab, dann lass die Türme hinten bis zum Entfernen der nächsten Dame. Wg. den verschiedenen Richtungen kann die Dame eh nicht beide Fälle auf einmal freigeben)
                hiddenPieceCoverage = ( resCBM & (pceTypeCoverMask[ROOK_BEHIND_QUEEN]) ) >> ROOK_BQ_SHIFT;
                //System.out.format(" hiddenPieceCoverage= %d from %s. ", hiddenPieceCoverage, toFullString(resCBM) );
                if (hiddenPieceCoverage>0)
                    resCBM = (resCBM + (hiddenPieceCoverage<<ROOK_SHIFT) ) & (~(pceTypeCoverMask[ROOK_BEHIND_QUEEN]));
            }
            //System.out.format(" resCBM= %s.  ", toFullString(resCBM) );
        }
        return resCBM;
    }


    public static int countPceTypeInCoverage(int pceType, int/*CBM*/ cbm) {
        if (pceType<0) {
            System.err.format("*** Fehler: Versuche gegnerische Figur in CBM zu zählen: %d aus %s.\n", pceType, cbmToFullString(cbm) );
            return -1;
        } else if (pceType==EMPTY ) {
            return -1;
        }
        int figMask = pceTypeCoverMask[pceType];
        int figShift = pceTypeShift[pceType];
        int count = ((cbm & figMask)>>figShift);
        if (  pceType == BISHOP ) {
            figMask = pceTypeCoverMask[BISHOP_BEHIND_QUEEN];
            figShift = pceTypeShift[BISHOP_BEHIND_QUEEN];
            count += ((cbm & figMask)>>figShift);
        }
        else if (  pceType == ROOK ) {
            figMask = pceTypeCoverMask[ROOK_BEHIND_QUEEN];
            figShift = pceTypeShift[ROOK_BEHIND_QUEEN];
            count += ((cbm & figMask)>>figShift);
        }
        return count;
    }


    public static int getSmallestPieceTypeFromCoverage(int/*CBM*/ cbm) {
        for (int i=PAWN; i>KING; i--)
            if ( (cbm & pceTypeCoverMask[i]) > 0 )
                return i;
        if ( (cbm & pceTypeCoverMask[BISHOP_BEHIND_QUEEN]) > 0 )
            return 0; // should not happen. It is removed after Queen-move. BISHOP_BEHIND_QUEEN;
        if ( (cbm & pceTypeCoverMask[ROOK_BEHIND_QUEEN]) > 0 )
            return 0; // should not happen. It is removed after Queen-move. ROOK_BEHIND_QUEEN;
        if ( (cbm & pceTypeCoverMask[KING]) > 0 )
            return KING;
        if ( cbm > 0)
            System.err.println("*** Fehler: keine Figur in CBM gefunden, obwohl diese nicht leer scheint.");
        return 0;  // nothing found, CBR must be 0
    }



    public static String cbmToPureString(int cbm1) {
        //String.format("%32s", Integer.toBinaryString(cmbl)).replace(' ', '0')
        //return Integer.toBinaryString(cbm1);
        return String.format("%13s", Integer.toBinaryString(cbm1)).replace(' ', '0');
    }

    public static String cbmToFullString(int cbm1) {
        return "["+ cbmToPureString(cbm1)+"]";
    }

    public static String cbmToFullString(int cbm1, boolean col) {
        return "["+ (isWhite(col) ? "w" : "b") +  cbmToPureString(cbm1) + "]";
    }

    /*** public static String cbmToString(int cbm1, boolean col) {
        //todo: use Letters fro ChessBasics
        if ( cbm1 == NOTCOVERED ) {
            return " ";
        }
        if ( col ) {
            switch (cbm1) {
                case PAWN_COVER_1:   return "o";
                case KNIGHT_COVER_1: return "N";
                case BISHOP_COVER_1: return "B";
                case ROOK_COVER_1:   return "R";
                case QUEEN_COVER_1:  return "Q";
                case KING_COVER_1:   return "K";
                default: return "X";
            }
        } else {
            switch (cbm1) {
                case PAWN_COVER_1:   return "*";
                case KNIGHT_COVER_1: return "n";
                case BISHOP_COVER_1: return "b";
                case ROOK_COVER_1:   return "r";
                case QUEEN_COVER_1:  return "q";
                case KING_COVER_1:   return "k";
                default: return "x";
            }

        }
    }***/
}
