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
import static de.ensel.tideeval.ConditionalDistance.ANY;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;

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
        coverageOfColorPerHops = new ArrayList<>(MAX_INTERESTING_NROF_HOPS);
        for (int h=0; h<MAX_INTERESTING_NROF_HOPS; h++) {
            coverageOfColorPerHops.add(new ArrayList<>(2));
            coverageOfColorPerHops.get(h).add(new ArrayList<>()); // for white
            coverageOfColorPerHops.get(h).add(new ArrayList<>()); // for black
        }
    }

    void prepareNewPiece(int newPceID) {
        vPieces.add(newPceID, VirtualPieceOnSquare.generateNew( myChessBoard, newPceID, getMyPos() ));
    }

    void spawnPiece(int pid) {
        //the Piece had not existed so far, so prefill the move-net
        movePieceHereFrom(pid, FROMNOWHERE);
    }

    void movePieceHereFrom(int pid, int frompos) {
        //a new or existing Piece must correct its move-net
        if (myPieceID != NO_PIECE_ID) {
            // this piece is beaten...
            // TODO:  Hat das Entfernen Auswirkungen auf die Daten der Nachbarn? oder wird das durch das einsetzen der neuen Figur gelöst?
            myChessBoard.removePiece(myPieceID);
        } else {
            // TODO: does not work for rook, when castling - why not?
            //  assert (vPieces.get(pid).realMinDistanceFromPiece() == 1);
        }
        myPieceID = pid;
        vPieces.get(pid).myOwnPieceHasMovedHereFrom(frompos);
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
        clashResultPerHops = null;
    }

    void emptySquare() {
        /*VirtualPieceOnSquare vPce = getvPiece(myPieceID);
        if (vPce!=null)
            vPce.resetDistances();*/
        myPieceID = NO_PIECE_ID;
    }

    void pieceHasMovedAway() {
        for (VirtualPieceOnSquare vPce : vPieces) {
            // tell all pieces that something has disappeared here - and possibly frees the way...
            if (vPce!=null)
                vPce.pieceHasMovedAway();
        }
    }
    //TODO: find more efficient way, when the other pieces recalculate their distances,
    // the piece should already be placed "in the way" at the new square


    int getMyPos() {
        return myPos;
    }

    int getPieceID() {
        return myPieceID;
    }

    int getUnconditionalDistanceToPieceIdIfShortest(int pceId) {
        VirtualPieceOnSquare vPce = vPieces.get(pceId);
        if (vPce==null || !vPce.getMinDistanceFromPiece().isUnconditional())
            return INFINITE_DISTANCE;
        return vPce.getMinDistanceFromPiece().dist();
    }

    int getDistanceToPieceId(int pceId) {
        VirtualPieceOnSquare vPce = vPieces.get(pceId);
        if (vPce==null)
            return INFINITE_DISTANCE;
        return vPce.getMinDistanceFromPiece().dist();
    }

    public ConditionalDistance getConditionalDistanceToPieceId(int pceId) {
        VirtualPieceOnSquare vPce = vPieces.get(pceId);
        if (vPce==null)
            return null;
        return vPce.getMinDistanceFromPiece();
    }

    /**
     * determines which Pieces of that color block from (i.e. are in the way of) an attack of the opposite color.
     * @param color:  normally the same color than my own Piece (but square could also be empty)
     * @return List<Integer> with positions of the covering pieces
     */
    public List<Integer> getPositionsOfPiecesThatBlockWayAndAreOfColor(boolean color) {
        List<Integer> result =  new ArrayList<>();
        for (VirtualPieceOnSquare vPce : vPieces) {
            if (vPce != null ) {
                ConditionalDistance d = vPce.getMinDistanceFromPiece();
                //TODO: Check if latest changes of pawn-distance semantics play a role here: suggestion: compare with algorithm for collection of coverage
                if (d.dist()==1 && d.hasExactlyOneFromToAnywhereCondition()) {
                    int blockingPiecePos = d.getFromCond(0);
                    if (myChessBoard.getPieceAt(blockingPiecePos).color()==color)
                        result.add(blockingPiecePos);   // the pos is stored in the fromCondition from where the piece needs to disappear from, so that vPce covers the wanted square.
                }
            }
        }
        return result;
    }

    /* trying new implementation without GlubschFishes CBM code
    // stores the cbms on each hop-depth
    int[] whiteCBMPerHops  = new int[MAX_INTERESTING_NROF_HOPS];
    int[] blackCBMPerHops  = new int[MAX_INTERESTING_NROF_HOPS];
    */
    List<List<List<VirtualPieceOnSquare>>> coverageOfColorPerHops;

    String getCoverageInfoByColorForLevel(boolean color, int level) {
        StringBuilder s = new StringBuilder(20);
        s.append(level).append(":");
        for (VirtualPieceOnSquare vPce:coverageOfColorPerHops.get(level).get(colorIndex(color)))
            s.append(giveFENChar(vPce.getPieceType()));
        return s.toString();
    }


    public final int[] getClashes() {
        //TODO: update time mchannism needs to be checked and corrected:
        if (clashResultPerHops==null || !areClashResultsUpToDate() )
            clashResultPerHops = evaluateClashes();
        return clashResultPerHops;
    }

    /**
     * evaluates the local clash, if the square carries a piece
     * @return int[] with the clash result, where [0] is the result of all pieces with dist==0,
     *  the rest [i] is not calculated, yet --> TODO
     */
    public int[] evaluateClashes() {
        int[] clashResultPerHops = new int[MAX_INTERESTING_NROF_HOPS];
        clashResultsLastUpdate = myChessBoard.nextUpdateClockTick();
        for(int h=0; h<MAX_INTERESTING_NROF_HOPS; h++) {
            clashResultPerHops[h] = 0;
            coverageOfColorPerHops.get(h).get(0).clear(); // for white
            coverageOfColorPerHops.get(h).get(1).clear(); // for black
        }

        // run over all vPieces on this square and correctly pre-ordered vPce-Lists to calculate the clashes
        debugPrint(DEBUGMSG_CLASH_CALCULATION, "evaluating " + this + ": ");
        for (VirtualPieceOnSquare vPce : vPieces) {
            if (vPceCoversOrAttacks(vPce)) {
                /*  old: before dist-simplifacation
                int d = vPce.getRawMinDistanceFromPiece().dist();
                if (d>0 && d<MAX_INTERESTING_NROF_HOPS) {
                    int fromCond = vPce.getRawMinDistanceFromPiece().getFromCond(0);  //todo: deal with array of conditions
                    // if the distance is conditional so that a piece of different color needs to move away, this does
                    // not count here (as it is the opponent to decide). We count the unconditional distance instead
                    if (fromCond!=ANY
                            && d<vPce.getRawMinDistanceFromPiece().dist()
                            && myChessBoard.getPieceAt(fromCond)!=null   // todo: check why this happens!?
                            && myChessBoard.getPieceAt(fromCond).color()
                                   !=colorOfPieceType(vPce.getPieceType())
                        || // in the same way do not count cond.distances where the opponent needs to move a piece to beat in the way
                            colorlessPieceType(vPce.getPieceType())==PAWN
                            && d>1
                            && vPce.getRawMinDistanceFromPiece().getToCond(0)!=ANY  // todo: deal with array of conditions
                    ) {
                        d = vPce.getRawMinDistanceFromPiece().dist();
                    }

                    //if (myPieceID!=NO_PIECE_ID && colorOfPieceTypeNr(vPce.getPieceType())==myPiece().color())
                    //    d--;   // counteracts the distance-calculation assuming that the piece has to move out
                    //    of the way first, which ist not true here in clash scenarios
                    if (d>0 && d<MAX_INTERESTING_NROF_HOPS) {
                        debugPrint(DEBUGMSG_CLASH_CALCULATION, "adding " + vPce + " at " + d + " ");
                        coverageOfColorPerHops.get(d).get(colorIndex(colorOfPieceType(vPce.getPieceType()))).add(vPce);
                    }
                }
                */
                ConditionalDistance cd = vPce.getRawMinDistanceFromPiece();
                int d = cd.dist();
                // todo: deal with array of conditions
                if (    d>0 && d<MAX_INTERESTING_NROF_HOPS
                        && ( cd.isUnconditional()
                            || // in the same way do not count cond.distances where the opponent needs to move a piece to beat in the way
                            colorlessPieceType(vPce.getPieceType())==PAWN
                                && vPce.getRawMinDistanceFromPiece().getToCond(0)!=ANY  // or a pawn can come here after an opponent moves in the way to be taken
                            )
                ) {
                    debugPrint(DEBUGMSG_CLASH_CALCULATION, "adding " + vPce + " at " + d + " ");
                    coverageOfColorPerHops.get(d).get(colorIndex(colorOfPieceType(vPce.getPieceType()))).add(vPce);
                }
            }
        }
        // sort in order of ascending piece value
        for(int h=0; h<MAX_INTERESTING_NROF_HOPS; h++) {
            coverageOfColorPerHops.get(h).get(0).sort(VirtualPieceOnSquare::compareTo); // for white
            coverageOfColorPerHops.get(h).get(1).sort(VirtualPieceOnSquare::compareTo); // for black
        }
        // calculate clashes on this square on FIRST LEVEL for now not on all levels
        for (int i=1; i<=1 /*MAX_INTERESTING_NROF_HOPS*/; i++) {
            if (myPieceID==NO_PIECE_ID)
                clashResultPerHops[i] = 0;  // TODO: think, wha vlue could make sense here
            else
                clashResultPerHops[i] = calcClashResult();
        }
        return clashResultPerHops;
    }

    private int calcClashResult() {
        assert(myPieceID!=NO_PIECE_ID);
        // start simulation with my own piece on the square and the opponent to decide whether to take it or not
        boolean turn = opponentColor( colorOfPieceType(myPieceType()) );
        VirtualPieceOnSquare currentVPceOnSquare = getvPiece(myPieceID);
        List<VirtualPieceOnSquare> whites = coverageOfColorPerHops.get(1).get(colorIndex(WHITE));
        List<VirtualPieceOnSquare> blacks = coverageOfColorPerHops.get(1).get(colorIndex(BLACK));
        return calcClashResultExcludingOne(turn,currentVPceOnSquare,whites,blacks, null);
    }

     /**
     * calculates the clash result if a piece vPceOnSquare is on a square directly (d==1) covered
     * by whites and blacks. it excludes the one excludeVPce. this is usefule to calc as if that pce had
     * moved here, to check if that is possible.
     */
    private static int calcClashResultExcludingOne(boolean turn,
                                       VirtualPieceOnSquare vPceOnSquare,
                                       List<VirtualPieceOnSquare> whites,
                                       List<VirtualPieceOnSquare> blacks,
                                       VirtualPieceOnSquare excludeVPce  ) {
        // start simulation with my own piece on the square and the opponent to decide whether to take it or not
        int resultIfTaken = -vPceOnSquare.myPiece().getValue();
        VirtualPieceOnSquare assassin;
        if (isWhite(turn)) {
            if (whites.size()==0)
                return 0;
            assassin = whites.get(0); //.poll();
            if (assassin==excludeVPce) {
                if (whites.size()==1)
                    return 0;
                assassin = whites.get(1); //.poll();
                whites = whites.subList(2, whites.size() );
            } else
                whites = whites.subList(1, whites.size() );
        } else {
            if (blacks.size()==0)
                return 0;
            assassin = blacks.get(0); //.poll();
            if (assassin==excludeVPce) {
                if (blacks.size()==1)
                    return 0;
                assassin = blacks.get(1); //.poll();
                blacks = blacks.subList(2, blacks.size() );
            } else
                blacks = blacks.subList(1, blacks.size() );
        }

        resultIfTaken += calcClashResultExcludingOne(!turn,assassin,whites,blacks, excludeVPce);
        if ( isWhite(turn) && resultIfTaken<0
                || !isWhite(turn) && resultIfTaken>0)
            return 0;  // do not take, it's not worth it
        return resultIfTaken;
    }

    void updateRelEvals() {
        getClashes();  // makes sure clash-lists are updated
        //TODO: go through all vPces and calc what happens if piece would go there
        for (VirtualPieceOnSquare vPce:vPieces)
            if (vPce!=null) {
                if (vPce.getMinDistanceFromPiece().dist()<=MAX_INTERESTING_NROF_HOPS)
                    updateRelEval(vPce);
                else
                   vPce.setRelEval(NOT_EVALUATED);
            }
    }

    private void updateRelEval(VirtualPieceOnSquare vPce) {
        //already called: getClashes();  // makes sure clash-lists are updated
        boolean turn = opponentColor( colorOfPieceType(vPce.getPieceType()) );
        int currentResult = 0;
        if (myPieceID!=NO_PIECE_ID) {
            VirtualPieceOnSquare currentVPceOnSquare = getvPiece(myPieceID);
            if (colorOfPieceType(currentVPceOnSquare.getPieceType())==colorOfPieceType(vPce.getPieceType()) ) {
                // cannot move on a square already occupied by my own piece
                vPce.setRelEval(NOT_EVALUATED);
                return;
            }
            currentResult = -currentVPceOnSquare.myPiece().getValue();
        }
        List<VirtualPieceOnSquare> whites = coverageOfColorPerHops.get(1).get(colorIndex(WHITE));
        List<VirtualPieceOnSquare> blacks = coverageOfColorPerHops.get(1).get(colorIndex(BLACK));
        currentResult += calcClashResultExcludingOne(turn,vPce,whites,blacks, vPce);
        vPce.setRelEval(currentResult);
    }

    private boolean vPceCoversOrAttacks(VirtualPieceOnSquare vPce) {
        return vPce != null
                && (  // same color coverage is always counted
                      colorOfPieceType(vPce.getPieceType())==colorOfPieceType(myPieceType())
                      || (  // but never add my own piece typ from the opponent, because it cannot attack me without myself beating it first.
                            colorlessPieceType(vPce.getPieceType())==colorlessPieceType(myPieceType())
                            // except it is already there in attack range
                            && vPce.getMinDistanceFromPiece().dist()==1 )
                      || (  // but add all opponents of different piece type
                            colorlessPieceType(vPce.getPieceType())!=colorlessPieceType(myPieceType() )
                            // TODO: but do not add queen or king on hv-dirs with dist>2 if I have a rook
                            //       and also not a queen or king on diagonal dirs with dit <2 if I have a bishop
                              //                  && ( ! (colorlessPieceTypeNr(vPce.getPieceType())==QUEEN   // similarly a queen cannot attack a rook or a bishop  if it is the attack direction with the same distance backwards
                              //                          && myChessBoard.getBoardSquares()[vPce.myPos].getvPiece(myPieceID).rawMinDistance.dist()==vPce.getMinDistanceFromPiece().getShortestDistanceEvenUnderCondition() ) ) ) )
                         )

                    )
                && ! (   // one exception: a pawn can only beat, but not "run into" a piece
                     colorlessPieceType(vPce.getPieceType())==PAWN
                             && fileOf(myPos)==fileOf(vPce.myPiece().getPos())
                );

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
        for (int i=1; i<MAX_INTERESTING_NROF_HOPS; i++) {
            eval += clashResultPerHops[i]>>(i-1);
        }
        return eval;
    }

    public int clashEval(int level) {
        getClashes();  // assures clashes are calculated if outdated
        return clashResultPerHops[level];
    }
}
