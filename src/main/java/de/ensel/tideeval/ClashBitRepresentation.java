/*
 * Copyright (c) 2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.NO_PIECE_ID;
import static de.ensel.tideeval.CoverageBitMap.KING_COVER_1;

class ClashBitRepresentation {
    // derived from the coverage bit-representation in 13 bits per color (see CoverageBitMap)
    // a concat of the black.white representation is  used to represent and resolve clashes.
    // x    unused Idea, because it makes code too complex:
    // x    However, number of possible combinations for White*Black are (depending on optimizations): 648^2=0,4M | 1536^2=2,25M | 22bit=4,2M, | 24bit=16,8M
    // x    even *2, because it is necessary to know if it is white or blacks turn.

    // CLASH_BIT_REPRESENTATION - CBR

    //
    static final int verbosityLevel = 0;
    private static final boolean CACHE_ACTIVATED = true; // default: true;

    //
    private static final int cacheSize = 67108864; // 2^(2*13) = 67.108.864
    private static final short[] clashEvalResultCache = new short[cacheSize];  // Array of Evaluations
    private static final short[] clashPceTypeResultCache = new short[cacheSize];  // Array of pceTypes (of piece remaining after evaluation)
    private static int countCacheHits = 0;
    private static int countCacheMisses = 0;
    /*clashBitRep*/

    static final int CBM_LENGTH = 13;

    // Initialize cache
    static {
        if (CACHE_ACTIVATED) {
            if (verbosityLevel > 1)
                System.out.println("Initializing Cache with size " + cacheSize);
            long startTime = System.nanoTime();
            clashEvalResultCache[0] = 0;
            for (int i = 1; i < cacheSize; i++) {
                clashEvalResultCache[i] = Short.MIN_VALUE;
                clashPceTypeResultCache[i] = NO_PIECE_ID;
            }
        /*for (int i = 0; i < 8192; i++) {
            for (int j = 0; j < 8192; j++)
                calcClashResultFromCurrentPlayerPerspective(-SOMEOFMINE,i,j);
            System.out.println(ClashBitRepresentation.getCacheStatistics());
        }*/
            long elapsedTime = System.nanoTime() - startTime;
            if (verbosityLevel > 0)
                System.out.println("************** Zeit für Cache-Initialisierung: " + elapsedTime / 1000000 + " Millisekunden");
        }
    }

    private static void addResultToCache(int result, int playerCBM, int oppCBM, int /*pceType*/ remainingPceType) {
        if (!CACHE_ACTIVATED)
            return;
        if (verbosityLevel > 1)
            System.out.println("Füge ein: Cache" + CoverageBitMap.cbmToFullString(playerCBM) + CoverageBitMap.cbmToFullString(oppCBM) + "=" + result);
        int cbr = (playerCBM << CBM_LENGTH) | oppCBM;
        clashEvalResultCache[cbr] = (short) result;
        clashPceTypeResultCache[cbr] = (short) remainingPceType;
        // BLI: if one CBM==0, add all same results, which have same CBM but additional (unused) pieces in other CBM;
    }

    private static int getEvalCacheEntry(int playerCBM, int oppCBM) {
        int cbr = (playerCBM << CBM_LENGTH) | oppCBM;
        if (clashEvalResultCache[cbr] == Short.MIN_VALUE)
            return Integer.MIN_VALUE+1;
        if (verbosityLevel > 1)
            System.out.println("Lese aus Cache" + CoverageBitMap.cbmToFullString(playerCBM) + CoverageBitMap.cbmToFullString(oppCBM) + "=" + clashEvalResultCache[cbr]);
        return clashEvalResultCache[cbr];
    }

    private static int getPceTypeCacheEntry(int playerCBM, int oppCBM) {
        int cbr = (playerCBM << CBM_LENGTH) | oppCBM;
        return clashPceTypeResultCache[cbr];
    }

    public static String getCacheStatistics() {
        return "Cache has " + countCacheMisses + " Entries and resulted in " + countCacheHits + " hits.";
    }


    // based on calcClashResultFromCurrentPlayerPerspective

    /**
     * calcs the rsult of a clash of Pieces on one square.
     * @param bias muss den piecenwert auf dem Feld (aus Board-Perspektive!) mit beinhalten + ggf. zusätzlicher bias .
     * @param pceOnFieldType wie in chessBasics vorgegeben, muss hier für gegnerische Figuren aber negativ sein.
     * @param whiteCBR  CBR (see CoveageBitMap :-) for white
     * @param blackCBR  CBR for black
     * @return result of the clash if both sides only go as far as is beneficial for them
     */
    public static int calcBiasedClashResultFromBoardPerspective(int bias, int/*NR*/pceOnFieldType, int/*CBR*/ whiteCBR, int/*CBR*/ blackCBR)  {
        if (verbosityLevel > 0)
            System.out.print("calcClashResultFromBoardPerspective( " + bias + ", " + givePieceName(pceOnFieldType)
                    + ", " + CoverageBitMap.cbmToFullString(whiteCBR) + "<->" + CoverageBitMap.cbmToFullString(blackCBR) + " ) ");
        int result;
        /*if (whiteCBR == 0 && blackCBR == 0) {
            if (beVerbose)
                System.out.print("calcClashResult() mit cbr 0 aufgerufen ");
            result = bias;
        }
        else */
        if (pceOnFieldType==NO_PIECE_ID ) { // d.h. auf dem Feld steht keine Figur = fiktive Bewertung, zu welchem Preis man einen Läufer opfern könnte (und davon für weiß+schwarz das Delta/4)
            if (verbosityLevel > 0)
                System.out.print("<-- es steht keine Figur auf dem Feld. Bewerte, wer es besser abdeckt:  ");
            result      = calcBiasedClashResultFromCurrentPlayerPerspective(-bias - getPositivePieceBaseValue(BISHOP),
                    whiteCBR, blackCBR);
            int resultB = calcBiasedClashResultFromCurrentPlayerPerspective(bias - getPositivePieceBaseValue(BISHOP),
                    blackCBR, whiteCBR);
            if (verbosityLevel > 0)
                System.out.print(" (" + result + " vs " + resultB + ")  ");
            if (result > resultB)
                result = (getPositivePieceBaseValue(QUEEN) + getPositivePieceBaseValue(BISHOP) - getPositivePieceBaseValue(CoverageBitMap.getSmallestPieceTypeFromCoverage(whiteCBR))) >> 5;
            else if (result < resultB)
                result = -((getPositivePieceBaseValue(QUEEN) + getPositivePieceBaseValue(BISHOP) - getPositivePieceBaseValue(CoverageBitMap.getSmallestPieceTypeFromCoverage(blackCBR))) >> 5);
            else
                result = 0;
            if (verbosityLevel > 0)
                System.out.println(" Bewertung des leeren Feldes: " + result);
            return result;
        } else if (isWhite(pceOnFieldType)) { // d.h. auf dem Feld steht eine weiße Figur
            if (verbosityLevel > 0)
                System.out.print("<-- es steht eine weiße Figur auf dem Feld. Was kann Schwarz machen?  ");
            result = -calcBiasedClashResultFromCurrentPlayerPerspective( -bias,
                    blackCBR, whiteCBR);
        } else { //schwarze Figur:   //  Das wird über den umgekehrten Weg einer weißen Figur berechnet.
            if (verbosityLevel > 0)
                System.out.print("<-- es steht eine schwarze Figur auf dem Feld. Schaue, was Weiß machen kann:  ");
            result = calcBiasedClashResultFromCurrentPlayerPerspective( bias,
                    whiteCBR, blackCBR);
        }
        if (verbosityLevel > 0)
            System.out.println("==> Ok, Bewertung: " + result);
        return result;
    }


    private static int calcClashResultFromCurrentPlayerPerspective(int/*NR*/pceOnFieldType, int/*CBR*/ playerCBR, int/*CBR*/ oppCBR)  {
        //BLI: refactor callers to directly use new method
        return calcBiasedClashResultFromCurrentPlayerPerspective(getPositivePieceBaseValue(pceOnFieldType), playerCBR, oppCBR);
    }

    private static int calcBiasedClashResultFromCurrentPlayerPerspective(int bias, int/*CBR*/ playerCBR, int/*CBR*/ oppCBR)  {
        // wird immer aufgerufen mit einer gegnerischen Figur am Feld!  (sonst könnte ich nicht schlagen!)
        // bias hat damit normal den negativen Wert der gegnerischen Figur, die darauf steht.  (Dis kann bei Bedarf rauf oder runter verschoben werden)
        //
        // turn; Farbe; wir nur für die textuelle Farb-Ausgabe verwendet. BLI:Könnte auch wegelassen werden
        // figOnFieldNr:  wie in class Piece vorgegeben, muss hier für gegnerische Figuren aber negativ sein.
        // cbr: siehe CBR :-)
        if (verbosityLevel > 1)
            System.out.print("calcBiasedClashResultFromCurrentPlayerPerspective( bias=" + bias
                    + ", " + CoverageBitMap.cbmToFullString(playerCBR) + "<->" + CoverageBitMap.cbmToFullString(oppCBR) + " ) ");
        /*if (figOnFieldNr > 0) { // d.h. auf dem Feld steht eine eigene Figur
            System.out.print("***** Fehler: es steht eine eigene Figur auf dem Feld. Hier kann man nichts nehmen ");
            return 0;
        }*/

        int result = Integer.MIN_VALUE+1;
        //int remainingFigNr;

        if (playerCBR==0) {   // hab nix mehr
            return -1;            // Gegner hat die letzte Figur drauf (also "gewonnen", aber hier im letzten Moment gerade kein Material gewonnen)
        } else if (playerCBR==KING_COVER_1 && oppCBR > 0) {   // habe nur noch nur König, aber Gegner hat noch was
            return -1;            // Gegner hat noch Figuren drauf aber ich nur den König, kann daher nicht schlagen.
        } else {
            int usedPceType = CoverageBitMap.getSmallestPieceTypeFromCoverage(playerCBR);
            if (usedPceType == 0) {
                System.err.println("**** Fehler: calcClashResult() mit playerCBR==0 Sollte an dieser Stelle nicht vorkommen!.");
                ChessBoard.internalErrorPrintln(String.format("Error in call to calcBiasedClashResultFromCurrentPlayerPerspective(%+d,%s,%s)", bias, CoverageBitMap.cbmToFullString(playerCBR), CoverageBitMap.cbmToFullString(oppCBR) ));
                //result = -1;
            } else {
                // mit gefundener nächster Figur "schlagen"
                //System.out.print(" ->" + toFullString(subCBR) + ".  ");
                //System.out.print(" -> Ziehe mit " + giveFigureNameByNr(usedPceType) + " und rufe nächste Bewertung auf:   ");
                if (CACHE_ACTIVATED) {
                    result = getEvalCacheEntry(playerCBR, oppCBR);
                    //remainingFigNr = getPceTypeCacheEntry(playerCBR, oppCBR);
                    if (result > Integer.MIN_VALUE+1)  // Berechnung bereits im Cache
                        countCacheHits++;
                    else
                        countCacheMisses++;
                    //System.out.println(" -> Result from Cache: " + result + " (" + countCacheHits + " hits, " + countCacheMisses + " misses)");
                }
                if (result <= Integer.MIN_VALUE+1 ) {
                    // neue Berechnung aufrufen (recursive call)
                    int subCBR = CoverageBitMap.removePceTypeFromCoverage(usedPceType, playerCBR);
                    result = -calcBiasedClashResultFromCurrentPlayerPerspective(-getPositivePieceBaseValue(usedPceType), oppCBR, subCBR);
                    if (result == 1)
                        addResultToCache(result, playerCBR, oppCBR, usedPceType);  // Gegner hat nicht mehr gezogen, meine Figur blieb stehen.
                    else
                        addResultToCache(result, playerCBR, oppCBR, -getPceTypeCacheEntry(oppCBR, subCBR));
                }
            }
            //System.out.print("=> erhalte Bewertung " + result + " nach " + "Schlagen von Figur " + giveFigureNameByNr(-figOnFieldNr) + " und speichere den Wert im Cache an Stelle " + toFullString(playerCBR)+ toFullString(oppCBR) + ". ");
        }

        if (result - bias < 0) {    // schlagen bleibt trotzdem schlecht, ich mache es also nicht...
            //System.out.print("=> erhalte Bewertung " + result + " nach " + "Schlagen von Figur " + giveFigureNameByNr(-figOnFieldNr) + ". Das sieht nicht gut aus, da ziehe ich lieber nicht.  ");
            result = -1;  // es wird mindestens -1 genommen, da bei schlechten Zügen besser nicht gezogen wird.
        } else
            result -= bias;   // bias ist normal negativ wg. dem Wert der gegnerischen Figur, d.h. das Schlagen verbessert hier den Wert.

        if (verbosityLevel > 1)
            System.out.println("==> Ok, bewerte das Ergebnis als " + result);

        return result;
    }

/*
    public static void main(String[] args) {
        boolean testsuccess = true;

        addResultToCache(123, 0b0000000010000, 0b0000000010000, PAWN );
        testsuccess &= TestClashRepresentation.myassert ( getEvalCacheEntry(0b0000000010000, 0b0000000010000), 123 );

        addResultToCache(-123, 0b0000000010000, 0b0000000010000, PAWN);
        testsuccess &= TestClashRepresentation.myassert ( getEvalCacheEntry(0b0000000010000, 0b0000000010000), -123 );

        System.out.format("\n\n%s", TestClashRepresentation.outputPrefix);
        System.out.print("Testergebnis: ");
        TestClashRepresentation.myassert(testsuccess, true);
        System.out.println();
    }
*/

}