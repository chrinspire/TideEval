/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ClashBitRepresentation.calcBiasedClashResultFromBoardPerspective;
import static de.ensel.tideeval.CoverageBitMap.*;
import static de.ensel.tideeval.Distance.ANY;
import static de.ensel.tideeval.Distance.INFINITE_DISTANCE;

public class Square {
    final ChessBoard myChessBoard;
    private final int myPos; // mainly for debugging and output
    private int myPieceID;
    private final List<VirtualPieceOnSquare> vPieces;  // TODO: change to plain old []
    VirtualPieceOnSquare getvPiece(int pid) {
        return vPieces.get(pid);
    }
    /*public void setvPiece(VirtualPieceOnSquare vPiece) {
        this.vPiece = vPiece;
    }*/

    /*
    boolean hasWhitePiece();
    boolean hasBlackPiece();
    boolean hasExactPieceType(int figType);
    boolean hasPieceOfAnyColor(int figType);

    boolean hasNoWhitePiece();
    boolean hasNoBlackPiece();
    boolean isEmptySquare();

    boolean hasOwnPiece(boolean myColor);
    boolean hasNoOpponentPiece(boolean myColor);
    boolean hasOwnBishop(boolean myColor);
    boolean hasOwnPawn(boolean myColor);

    boolean hasPawn();

    boolean hasOpponentPiece(boolean myColor);
    boolean hasNoOwnPiece(boolean myColor);
    boolean hasOpponentPieceTypeNr(boolean myColor, int pieceNr);
    //boolean hasOpponentPieceWithMatchingCD(int pos, boolean myColor, int wantedCD);
    */

    Square(ChessBoard myChessBoard, int myPos) {
        this.myChessBoard = myChessBoard;
        this.myPos = myPos;
        myPieceID = NO_PIECE_ID;
        vPieces = new ArrayList<>(ChessBasics.MAX_PIECES);
    }

    void prepareNewPiece(int newPceID) {
        vPieces.add(newPceID, VirtualPieceOnSquare.generateNew( myChessBoard, newPceID, getMyPos() ));
    }

    void spawnPiece(int pid) {
        //the Piece had not existed so far, so prefill the move-net
        movePieceHere(pid);
    }

    void movePieceHere(int pid) {
        //an existing Piece must correct its move-net
        myPieceID = pid;
        vPieces.get(pid).myOwnPieceHasMovedHere( );
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," ---  and "+myPieceID+": correct the other pieces' distances: " );
        for (VirtualPieceOnSquare vPce : vPieces) {
            // tell all other pieces that something new is here - and possibly in the way...
            if (vPce !=null && vPce.getPieceID()!=pid)
                vPce.pieceHasArrivedHere(pid);
        }
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," :"+myPieceID+"done.]     " );
    }


    public void removePiece(int pceID) {
        vPieces.set(pceID,null);
    }


    void pieceMoved1Closer(int pid) {
        //the Piece had a hop-distance of one and now moved on my square
        if (myPieceID != NO_PIECE_ID) {
            // this piece is beaten...
            // TODO:  Hat das Entfernen Auswirkungen auf die Daten der Nachbarn? oder wird das durch das einsetzen der neuen Figur gelöst?
            myChessBoard.removePiece(myPieceID);
        } else {
            // TODO: does not work for rook, when casteling - why not?
            //  assert (vPieces.get(pid).realMinDistanceFromPiece() == 1);
        }
        movePieceHere(pid);
    }

    void emptySquare() {
        int oldPceId = myPieceID;
        myPieceID = NO_PIECE_ID;
        for (VirtualPieceOnSquare vPce : vPieces) {
            // tell all pieces that something has disappeared here - and possibly frees the way...
            if (vPce!=null) {
                if (vPce.getPieceID()==oldPceId)
                    vPce.resetDistances();
                vPce.pieceHasMovedAway();
            }
        }
        //TODO: find more efficient way, when the other pieces recalculate their distances,
        // the piece should already be placed "in the way" at the new square
    }

    int getMyPos() {
        return myPos;
    }

    int getPieceID() {
        return myPieceID;
    }

    int getShortestUnconditionalDistanceToPieceID(int pceId) {
        VirtualPieceOnSquare vPce = vPieces.get(pceId);
        if (vPce==null)
            return INFINITE_DISTANCE;
        return vPce.getMinDistanceFromPiece().dist();
    }

    int getShortestConditionalDistanceToPieceID(int pceId) {
        VirtualPieceOnSquare vPce = vPieces.get(pceId);
        if (vPce==null)
            return INFINITE_DISTANCE;
        return vPce.getMinDistanceFromPiece().getShortestDistanceEvenUnderCondition();
    }

    public Distance getDistanceToPieceID(int pceId) {
        VirtualPieceOnSquare vPce = vPieces.get(pceId);
        if (vPce==null)
            return null;
        return vPce.getMinDistanceFromPiece();
    }

    /**
     * determines which Pieces of that color cover from an attack of the opposite color.
     * @param color:  normally the same color than my own Piece (but square could also be empty)
     * @return List<Integer> with positions of the covering pieces
     */
    public List<Integer> coveredByOfColor(boolean color) {
        List<Integer> result =  new ArrayList<>();
        for (VirtualPieceOnSquare vPce : vPieces) {
            if (vPce != null) {
                Distance d = vPce.getMinDistanceFromPiece();
                //TODO: Check if latest changes of pawn-distance semantics play a role here: suggestion: compare with algorithm for collection ob coverage bitmaps
                if (d.dist()==2 && d.getShortestDistanceEvenUnderCondition()==1)
                    result.add(d.getFromCond());   // the pos is stored in the fromCondition from where the piece needs to disappear from, so that vPce covers the wanted square.
            }
        }
        return result;
    }

    // stores the cbms on each hop-depth
    int[] whiteCBMPerHops  = new int[MAX_INTERESTING_NROF_HOPS];
    int[] blackCBMPerHops  = new int[MAX_INTERESTING_NROF_HOPS];

    String getCoverageInfoByColorForLevel(boolean color, int level) {
        if (isWhite(color))
            return cbmToFullString(whiteCBMPerHops[level]);
        return cbmToFullString(blackCBMPerHops[level]);
    }


    public final int[] getClashes() {
        if (clashResultPerHops==null || !areClashResultsUpToDate() )
            clashResultPerHops = evaluateClashes();
        return clashResultPerHops;
    }

    public int[] evaluateClashes() {
        int[] clashResultPerHops = new int[MAX_INTERESTING_NROF_HOPS];
        clashResultsLastUpdate = myChessBoard.nextUpdateClockTick();
        for(int i=0; i<MAX_INTERESTING_NROF_HOPS; i++) {
            clashResultPerHops[i] = 0;
            whiteCBMPerHops[i] = 0;
            blackCBMPerHops[i] = 0;
        }

        // run over all vPieces on this square and create CoverageBitMaps to calculate the clashes
        // todo: should not be done here, but continiously while pieces come closer or move away...
        debugPrint(DEBUGMSG_CLASH_CALCULATION, "evaluating " + this + ": ");
        for (VirtualPieceOnSquare vPce : vPieces) {
            if (vPce != null
                    && ( colorOfPieceType(vPce.getPieceType())== colorOfPieceType(myPieceType())   // same color coverage is always counted
                        || (  colorlessPieceType(vPce.getPieceType())== colorlessPieceType(myPieceType())    // but never add my own piece typ from the opponent, because it cannot attack me without myself beating it first.
                              && vPce.getMinDistanceFromPiece().getShortestDistanceEvenUnderCondition()==1 )        // except it is already there in attack range
                        || (    colorlessPieceType(vPce.getPieceType())!= colorlessPieceType(myPieceType() ) )
                        )
                    && ! (   // one exception: a pawn can only beat, but not "run into" a piece
                         colorlessPieceType(vPce.getPieceType())==PAWN
                                 && fileOf(myPos)==fileOf(vPce.myPiece().getPos())
                    )
              //                  && ( ! (colorlessPieceTypeNr(vPce.getPieceType())==QUEEN   // similarly a queen cannot attack a rook or a bishop  if it is the attack direction with the same distance backwards
              //                          && myChessBoard.getBoardSquares()[vPce.myPos].getvPiece(myPieceID).rawMinDistance.dist()==vPce.getMinDistanceFromPiece().getShortestDistanceEvenUnderCondition() ) ) ) )
            ) {
                int d = vPce.getRawMinDistanceFromPiece().getShortestDistanceEvenUnderCondition();
                int fromCond = vPce.getRawMinDistanceFromPiece().getFromCond();
                // if the distance is conditional so that a piece of different color needs to move away, this does
                // not count here (as it is the opponent to decide). We count the unconditional distance instead
                if (fromCond!=ANY
                        && d<vPce.getRawMinDistanceFromPiece().dist()
                        && myChessBoard.getPieceAt(fromCond)!=null   // todo: check why this happens!?
                        && myChessBoard.getPieceAt(fromCond).color()
                               !=colorOfPieceType(vPce.getPieceType())
                    || // in the same way do not count cond.distances whree the opponent needs to move a piece to beat in the way
                        colorlessPieceType(vPce.getPieceType())==PAWN
                        && d>1
                        && vPce.getRawMinDistanceFromPiece().getToCond()!=ANY
                )
                    d = vPce.getRawMinDistanceFromPiece().dist();
                //if (myPieceID!=NO_PIECE_ID && colorOfPieceTypeNr(vPce.getPieceType())==myPiece().color())
                //    d--;   // counteracts the distance-calulation assuming that the piece has to move out of the way first, which ist not true here in clash scenarions
                debugPrint(DEBUGMSG_CLASH_CALCULATION, "adding " + vPce + ": ");
                if (d>0 && d<INFINITE_DISTANCE)
                    for (int i=d; i<MAX_INTERESTING_NROF_HOPS; i++) { // this piece counts on all levels from d downward.
                        if (isWhite(vPce.myPceType)) {
                            whiteCBMPerHops[i] = addPceTypeToCoverage(vPce.getPieceType(), whiteCBMPerHops[i]);
                            debugPrint(DEBUGMSG_CLASH_CALCULATION, " w" + i + ":" + CoverageBitMap.cbmToFullString(whiteCBMPerHops[i]));
                        } else {
                            blackCBMPerHops[i] = addPceTypeToCoverage(colorlessPieceType(vPce.getPieceType()), blackCBMPerHops[i]);
                            debugPrint(DEBUGMSG_CLASH_CALCULATION, " b" + i + ":" + CoverageBitMap.cbmToFullString(blackCBMPerHops[i]));
                        }
                    }
                debugPrintln(DEBUGMSG_CLASH_CALCULATION, "");
            }
        }
        // calculate clashes on this square on all levels
        for (int i=1; i<MAX_INTERESTING_NROF_HOPS; i++) {
            if (myPieceID!=NO_PIECE_ID)
                clashResultPerHops[i] = calcBiasedClashResultFromBoardPerspective(
                        myPiece().getValue(),
                        myPieceType(),
                        whiteCBMPerHops[i],
                        blackCBMPerHops[i]);
            else
                clashResultPerHops[i] = (int) (0.1f * calcBiasedClashResultFromBoardPerspective(
                                        0,
                        myPieceType(),
                                        whiteCBMPerHops[i],
                                        blackCBMPerHops[i]));
            debugPrintln(DEBUGMSG_CLASH_CALCULATION, "Clashcalc at " + squareName(myPos)
                    + " for " + i
                    + " maxHops: white=" + CoverageBitMap.cbmToFullString(whiteCBMPerHops[i])
                    + " against black=" + CoverageBitMap.cbmToFullString(blackCBMPerHops[i])
                    + " result: " + clashResultPerHops[i] );
        }
        return clashResultPerHops;
    }

    private int myPieceType() {
        if (myPieceID==NO_PIECE_ID)
            return EMPTY;
        return myChessBoard.getPiece(myPieceID).getPieceType();
    }

    public ChessPiece myPiece() {
        return myChessBoard.getPiece(myPieceID);
    }

    @Override
    public String toString() {
        return "Square{" +
                "" + squareName(myPos) +
                (myPieceID==NO_PIECE_ID ? " is empty" : " with " + myPiece()) +
                '}';
    }

    private int[] clashResultPerHops = null;
    private long clashResultsLastUpdate = -1;

    public long getLatestClashResultUpdate() {
        return clashResultsLastUpdate;
    }

    private boolean areClashResultsUpToDate() {
        long maxLatestUpdate = vPieces.stream().filter(Objects::nonNull)
                .mapToLong(VirtualPieceOnSquare::getLatestChange)
                .max().getAsLong();
        return clashResultsLastUpdate >= maxLatestUpdate;
    }

    public int clashEval() {
        getClashes();  // assures clashes are calculated if outdated
        int eval=0;
        for (int i=0; i<MAX_INTERESTING_NROF_HOPS; i++) {
            eval += clashResultPerHops[i]>>i;
        }
        return eval;
    }

    public int clashEval(int level) {
        getClashes();  // assures clashes are calculated if outdated
        return clashResultPerHops[level];
    }
}
