/*
 * Copyright (c) 2021-2021.
 * Feel free to use code or algorithms, but always keep this copyright notice attached with my name -> Christian Ensel
 */

package de.ensel.tideeval;

import java.util.*;
import java.util.stream.Collectors;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Square {
    final ChessBoard myChessBoard;
    private final int myPos; // mainly for debugging and output
    private int myPieceID;  // the ID of the ChessPiece sitting directly on this square - if any, otherwise NO_PIECE_ID
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
        movePieceHereFrom(pid, NOWHERE);
        vPieces.get(pid).myOwnPieceHasSpawnedHere();
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," ---  and "+myPieceID+": correct the other pieces' distances: " );
        for (VirtualPieceOnSquare vPce : vPieces) {
            // tell all other pieces that something new is here - and possibly in the way...
            if (vPce !=null && vPce.getPieceID()!=pid)
                vPce.pieceHasArrivedHere(pid);
        }
        debugPrint(DEBUGMSG_DISTANCE_PROPAGATION," :"+myPieceID+"done.]     " );

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

    boolean isSquareEmpty() { return myPieceID == NO_PIECE_ID; }

    void pieceHasMovedAway() {
        for (VirtualPieceOnSquare vPce : vPieces) {
            // tell all pieces that something has disappeared here - and possibly frees the way...
            if (vPce!=null)
                vPce.pieceHasMovedAway();
        }
    }
    //TODO: find more efficient way, when the other pieces recalculate their distances,
    // the piece should already be placed "in the way" at the new square


    public void propagateLocalChange() {
        for (VirtualPieceOnSquare vPce : vPieces) {
            // tell all pieces that something has disappeared here - and possibly frees the way...
            if (vPce!=null) {
                vPce.setLatestChangeToNow();
                vPce.minDistsDirty();  // hmm, this method would better be private
                vPce.propagateDistanceChangeToAllNeighbours();
            }
        }
    }


    /** getter for myPos, i.e. the position of this square
     * @return position of this square
     */
    int getMyPos() {
        return myPos;
    }

    /**
     *
     * @return the ID of the ChessPiece sitting directly on this square - if any, otherwise NO_PIECE_ID
     */
    int getPieceID() {
        return myPieceID;
    }

    int getUnconditionalDistanceToPieceIdIfShortest(int pceId) {
        VirtualPieceOnSquare vPce = vPieces.get(pceId);
        if (vPce==null || !vPce.getMinDistanceFromPiece().isUnconditional() )
            return INFINITE_DISTANCE;
        return vPce.getMinDistanceFromPiece().dist();
    }

    public boolean hasNoGoFromPieceId(int pceId) {
        VirtualPieceOnSquare vPce = vPieces.get(pceId);
        if (vPce==null)
            return false;
        return vPce.getMinDistanceFromPiece().hasNoGo();
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
                    ChessPiece blockingPiece = myChessBoard.getPieceAt(blockingPiecePos);
                    if (blockingPiece!=null && blockingPiece.color()==color) // Problem ==null on 9th game move Qxd5 -> here square e1 vPce id=3, blockingPcePos==36==e4 is empty
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
        //TODO: update time mchannism is checked and ok, but could work more selective:
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
        //not any more :-) deactivated for debug reasons:

        // run over all vPieces on this square and correctly build the pre-ordered vPce-Lists
        // (that are later used to calculate the clashes)
        debugPrintln(DEBUGMSG_CLASH_CALCULATION, "");
        debugPrint(DEBUGMSG_CLASH_CALCULATION, "Evaluating " + this + ": ");
        for (VirtualPieceOnSquare vPce : vPieces) {
            int d = vPceCoverOrAttackDistance(vPce);
            if (d<MAX_INTERESTING_NROF_HOPS ) {  // implicitly contained && d!=INFINITE_DISTANCE
                // add this piece to the list of attackers/defenders
                debugPrint(DEBUGMSG_CLASH_CALCULATION, " +adding " + vPce + " at d=" + d + " ");
                // TODO:Experimental, use only 3 baskets for d==0, 1 and more -> needs to be cleanly changed everywhere from size MAX... to 3.
                coverageOfColorPerHops
                        .get(d)
                        .get(colorIndex(colorOfPieceType(vPce.getPieceType())))
                        .add(vPce);
            }
        }
        // sort in order of ascending piece value
        for(int h=1; h<MAX_INTERESTING_NROF_HOPS; h++) {
            coverageOfColorPerHops.get(h).get(0).sort(VirtualPieceOnSquare::compareTo); // for white
            coverageOfColorPerHops.get(h).get(1).sort(VirtualPieceOnSquare::compareTo); // for black
        }

        // calculate clashes on this square on FIRST LEVEL for now not on all levels
        for (int i=1; i<=1 ; i++) {  // was: <=MAX_INTERESTING_NROF_HOPS
            if (isSquareEmpty())
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

        List<VirtualPieceOnSquare> whiteOthers = new ArrayList<>();
        List<VirtualPieceOnSquare> blackOthers = new ArrayList<>();
        for(int h=2; h<MAX_INTERESTING_NROF_HOPS; h++) {
            whiteOthers.addAll(coverageOfColorPerHops
                    .get(h).get(colorIndex(WHITE))
                    .stream()
                    .filter(VirtualPieceOnSquare::isConditional )
                    .collect(Collectors.toList() )
            );
            blackOthers.addAll((Collection<? extends VirtualPieceOnSquare>) coverageOfColorPerHops
                    .get(h).get(colorIndex(BLACK))
                    .stream()
                    .filter(VirtualPieceOnSquare::isConditional )
                    .collect(Collectors.toList() )
            );
        }

        int res = calcClashResultExcludingOne(
                turn, currentVPceOnSquare,
                whites, blacks,
                null,
                whiteOthers, blackOthers,
                null);
        return res;
    }

     /**
     * calculates the clash result if a piece vPceOnSquare is on a square directly (d==1) covered
     * by whites and blacks. it excludes the one excludeVPce. this is usefule to calc as if that pce had
     * moved here, to check if that is possible.
     */
    private static int calcClashResultExcludingOne(final boolean turn,
                                                   final VirtualPieceOnSquare vPceOnSquare,
                                                   List<VirtualPieceOnSquare> whites,
                                                   List<VirtualPieceOnSquare> blacks,
                                                   final VirtualPieceOnSquare excludeVPce,
                                                   final List<VirtualPieceOnSquare> whiteOthers,
                                                   final List<VirtualPieceOnSquare> blackOthers,
                                                   List<Move> moves
    ) {
        boolean whitesIsCopy = false;
        boolean blacksIsCopy = false;
        if (moves==null)
            moves = new ArrayList<>();
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

        // pull more, mainly indirectly covering pieces into the clash
        // (like two rooks behind each other or a bishop diagonally behind a pawn
        // ToDO!! speed optimization, it works and testcases are updated, but duration of boardEvaluation_Test
        //  has doubled to 1:30-2 min since necessary changes in CD were made + increase to even 2:15-3 min(!!), when the following
        //  code was added to make use of the information - so the whole evaluation time has almost tripled just to tell
        //  that a bishop is behind a pawn and similar...
       moves.add(new Move( assassin.getPiecePos(),
                            vPceOnSquare.myPos));  // ToDo: Make+use getter for myPos
        for (Iterator<VirtualPieceOnSquare> iterator = whiteOthers.iterator(); iterator.hasNext(); ) {
            VirtualPieceOnSquare oVPce = iterator.next();
            ConditionalDistance oVPceMinDist = oVPce.getMinDistanceFromPiece();
            if (oVPceMinDist.movesFulfillConditions(moves) > 0
                && oVPceMinDist.distWhenAllConditionsFulfilled(WHITE)==1 ) {
                if (!whitesIsCopy) {
                    whites = new ArrayList<>(whites);  // before changing the original List for the first time, make and switch to a copy...
                    whitesIsCopy = true;
                }
                whites.add(oVPce);
                whites.sort(VirtualPieceOnSquare::compareTo); // for white
                // Todo!!: (here&black) sort sorts wrongly, as the compared value is the normal .dist, but should
                //  use the distWhenAllConditionsFulfilled
                //breaks the loop/list: whiteOthers.remove(oVPce);
                iterator.remove();
            }
        }
        for (Iterator<VirtualPieceOnSquare> iterator = blackOthers.iterator(); iterator.hasNext(); ) {
            VirtualPieceOnSquare oVPce = iterator.next();
            ConditionalDistance oVPceMinDist = oVPce.getMinDistanceFromPiece();
            if (oVPceMinDist.movesFulfillConditions(moves) > 0
                    && oVPceMinDist.distWhenAllConditionsFulfilled(BLACK)==1) {
                if (!blacksIsCopy) {
                    blacks = new ArrayList<>(blacks);  // before changing the original List for the first time, make and switch to a copy...
                    blacksIsCopy = true;
                }
                blacks.add(oVPce);
                blacks.sort(VirtualPieceOnSquare::compareTo); // for white
                //breaks the loop/list: blackOthers.remove(oVPce);
                iterator.remove();
            }
        }

        resultIfTaken += calcClashResultExcludingOne(
                !turn,assassin,
                whites, blacks,
                excludeVPce,
                whiteOthers, blackOthers,
                moves);
        if ( isWhite(turn) && resultIfTaken<0
                || !isWhite(turn) && resultIfTaken>0)
            return 0;  // do not take, it's not worth it
        return resultIfTaken;
    }

    /**
     * Evaluates the "relEval" of all vPieces - telling what would happen if these came here (first)
     */
    void updateRelEvals() {
        getClashes();  // makes sure clash-lists are updated
        for (VirtualPieceOnSquare vPce:vPieces)
            if (vPce!=null) {
                if (vPce.getMinDistanceFromPiece().dist()<=MAX_INTERESTING_NROF_HOPS)
                    updateRelEval(vPce);
                else
                   vPce.setRelEval(NOT_EVALUATED);
            }
    }

    /**
     * Evaluate the "relEval" of one of my vPieces - telling what would happen if that Piece came here (first of all)
     * @param evalVPce one virtual Piece to calculate
     */
    private void updateRelEval(VirtualPieceOnSquare evalVPce) {
        //already called: getClashes();  // makes sure clash-lists are updated

        // for debug reasons:  do nothing, just assume every clash result is 0:
        //evalVPce.setRelEval(0);
        //return;

        boolean turn = opponentColor( colorOfPieceType(evalVPce.getPieceType()) );
        int currentResult = 0;
        List<VirtualPieceOnSquare> whites = coverageOfColorPerHops.get(1).get(colorIndex(WHITE));
        List<VirtualPieceOnSquare> blacks = coverageOfColorPerHops.get(1).get(colorIndex(BLACK));
        boolean fuzzedWithKingInList = false;
        VirtualPieceOnSquare currentVPceOnSquare = null;
        if (myPieceID!=NO_PIECE_ID) {
            currentVPceOnSquare = getvPiece(myPieceID);
            if (colorOfPieceType(currentVPceOnSquare.getPieceType())==colorOfPieceType(evalVPce.getPieceType()) ) {
                // cannot move on a square already occupied by one of my own pieces
                // but TODO: check if that piece can move away at all
                // let's check what happens if the piece moves away
                // (a non pawn would then even cover that piece, but this is also currently not accounted, as we'd have to alter the now reusable whitesblacks-lists.)
                currentResult = 0 ;
            }
            else if ( colorlessPieceType(currentVPceOnSquare.getPieceType())==KING ) {
                // no value of the king? yes, it could not be the first piece taken, it would first
                // go away and then even cover the place (if possible of course, but let's better
                // assume, otherwise the kings place is always calculate as a save hopping point)
                currentResult = 0;
                if ( currentVPceOnSquare.getPieceType()==KING )
                    whites.add(currentVPceOnSquare);
                else
                    blacks.add(currentVPceOnSquare);
                fuzzedWithKingInList=true;
            }
            else
                currentResult = -currentVPceOnSquare.myPiece().getValue();
        }

        List<VirtualPieceOnSquare> whiteOthers = new ArrayList<>(coverageOfColorPerHops.get(2).get(colorIndex(WHITE)));
        List<VirtualPieceOnSquare> blackOthers = new ArrayList<>(coverageOfColorPerHops.get(2).get(colorIndex(BLACK)));
        // TODO-refactor: this code piece is duplicated
        for(int h=2; h<4/*MAX_INTERESTING_NROF_HOPS*/; h++) {
            whiteOthers.addAll(coverageOfColorPerHops
                    .get(h).get(colorIndex(WHITE))
                    .stream()
                    .filter(VirtualPieceOnSquare::isConditional )
                    .collect(Collectors.toList() )
            );
            blackOthers.addAll((Collection<? extends VirtualPieceOnSquare>) coverageOfColorPerHops
                    .get(h).get(colorIndex(BLACK))
                    .stream()
                    .filter(VirtualPieceOnSquare::isConditional )
                    .collect(Collectors.toList() )
            );
        }

        if ( isSquareEmpty()

                && colorlessPieceType(evalVPce.getPieceType())==PAWN
                && fileOf(evalVPce.getPiecePos())!=fileOf(getMyPos())  // only for beating scenarios -
            // TODO: this last check only works for dist==1, for others it is inherently imprecise, the last move has to be found out and taken to decide if it is a beating move
        ) {
            // treat PAWNs calculation on/towards an empty square special, because it can only go there if one of the opponents pieces goes there first...
            assertEquals(currentResult,0);
            turn = opponentColor(turn);
            VirtualPieceOnSquare firstMover = null;
                // first check if white can move a pawn there straightly, as this would not be in the covers-list.
            final int turnPawnPieceType = (isWhite(turn) ? PAWN : PAWN_BLACK);
            for( VirtualPieceOnSquare vPce : vPieces )
                if (vPce!=null
                    && vPce.getPieceType()==turnPawnPieceType
                    && fileOf(vPce.getPiecePos())==fileOf(getMyPos())
                    && vPce.getMinDistanceFromPiece().dist()<3  // hard coded, hmm, a pawn can come here in 2 straight moves (not 100% precise...)
                    // also accounted here, although it goes there to calculate, not to die ;-):
                    && evalIsOkForColByMin( vPce.getRelEval(), turn, EVAL_TENTH)
                ) {
                    firstMover = vPce;
                    break;
                }
            if (firstMover==null) {  // --> no straight pawn victim found,
                // we could take a Piece from the other attackers, but as this is a pawn guarding the square, that piece would always have a NoGo to get here and never do it.
                turn = opponentColor(turn);
                currentResult = checkmateEval(turn);  // a "random" very high bad value, so the piece will get a NoGo later in the algorithm
            } else
                currentResult += calcClashResultExcludingOne(turn,firstMover,  // the opponents pawn is now on the square
                        whites, blacks, null,   // the vPce is not excluded, it is now part of the clash (it had to be moved to ahead of the list, but as it is a pawn it is there (among pawns) anyway.
                        whiteOthers, blackOthers, null);
        } else
            currentResult += calcClashResultExcludingOne(turn,evalVPce,   // the vPce itself goes first
                    whites, blacks, evalVPce,    // and is thus excluded from the rest of the clash
                    whiteOthers, blackOthers, null);
        evalVPce.setRelEval(currentResult);

        if (fuzzedWithKingInList) {
            if (currentVPceOnSquare.getPieceType()==KING)
                whites.remove(currentVPceOnSquare);
            else
                blacks.remove(currentVPceOnSquare);
        }
    }


    int warningLevel() {
        if (clashEval(1)!=0)
            return 1;
        int[] fces = futureClashEval();
        int i=0;
        int indirectWarning = 0;
        while (i<fces.length-1)
            if (fces[i++]!=0) {
                if (fces[i]!=0)  // if also next level is !00, this means, we cannot protect the threat
                    return (i + 2);
                if (indirectWarning==0)
                    indirectWarning=-(i+1);
            }
        return indirectWarning;
    }

    /**
     * Evaluate "the next level"  concering local clashes: telling what would happen if additional Pieces (with
     * dist==2) are brought in the attack
     */
    int[] futureClashEval() {
        //must be already called: getClashes();  // makes sure clash-lists are updated
        if (isSquareEmpty()
)
            return new int[0];
        // start simulation with my own piece on the square and the opponent of that piece starting to decide whether to
        // bring in additional attackers
        boolean turn = opponentColor( colorOfPieceType(myPieceType()) );

        VirtualPieceOnSquare currentVPceOnSquare = getvPiece(myPieceID);
        List<VirtualPieceOnSquare> whites = new ArrayList<>(coverageOfColorPerHops.get(1).get(colorIndex(WHITE)));
        List<VirtualPieceOnSquare> blacks = new ArrayList<>(coverageOfColorPerHops.get(1).get(colorIndex(BLACK)));
        List<VirtualPieceOnSquare> whiteMoreAttackers = coverageOfColorPerHops.get(2).get(colorIndex(WHITE));
        List<VirtualPieceOnSquare> blackMoreAttackers = coverageOfColorPerHops.get(2).get(colorIndex(BLACK));
        int[] res = new int[Math.max(whiteMoreAttackers.size(),blackMoreAttackers.size())*2+1];
        int nr=0;
        int bNext = 0;
        int wNext=0;
        debugPrintln(DEBUGMSG_FUTURE_CLASHES, "future clashes on "+ this);
        while ( isWhite(turn) ? wNext<whiteMoreAttackers.size()
                                  : bNext<blackMoreAttackers.size()) {
            debugPrintln(DEBUGMSG_FUTURE_CLASHES, "");
            // bring additional pieces in
            if (isWhite(turn)) {
                debugPrint(DEBUGMSG_FUTURE_CLASHES, "White adds "+whiteMoreAttackers.get(wNext));
                whites.add(whiteMoreAttackers.get(wNext));
                whites.sort(VirtualPieceOnSquare::compareTo);
                wNext++;
            } else { // blacks turn
                debugPrint(DEBUGMSG_FUTURE_CLASHES, "Black adds "+blackMoreAttackers.get(bNext));
                blacks.add(blackMoreAttackers.get(bNext));
                blacks.sort(VirtualPieceOnSquare::compareTo);
                bNext++;
            }
            // main calculation
            // ToDo: Check if algo still works after calcClashResultExcludingOne was changed to also pull from the oothers-lists
            res[nr]=calcClashResultExcludingOne(
                    turn, currentVPceOnSquare,
                    whites,blacks,
                    null,
                    whiteMoreAttackers, blackMoreAttackers, null);
            debugPrint(DEBUGMSG_FUTURE_CLASHES, " => "+res[nr]);
            nr++;
            // switch sides (or not)
            if ( isWhite(turn) && bNext<blackMoreAttackers.size()
                || !isWhite(turn) && wNext<whiteMoreAttackers.size() ) {
                turn = !turn;   // if opponent still has pieces left, we switch sides...
            } else {
                // if not then result stays the same and same side can also bring in more pieces
                res[nr]=res[nr-1];
                nr++;
            }
        }
        return res;
    }


    public int getAttacksValueforColor(boolean color) {
        return coverageOfColorPerHops.get(1).get(colorIndex(color)).size()
                + ( (int)((coverageOfColorPerHops.get(2).get(colorIndex(color)).size()))/2 );
    }

    public int countDirectAttacksWithColor(boolean color) {
        return coverageOfColorPerHops.get(1).get(colorIndex(color)).size();
    }

    private int vPceCoverOrAttackDistance(VirtualPieceOnSquare vPce) {
        if (vPce==null)
            return INFINITE_DISTANCE;
        ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();
        int dist = rmd.dist();
        if (    // there must not be a NoGo on the way to get here  -  except for pawns, which currently signal a NoGo if they cannot "beat" to an empty square, but still cover it...
                (rmd.hasNoGo()
                        && (colorlessPieceType(vPce.getPieceType())!=PAWN || rmd.getNoGo()!=getMyPos()) )  //TODo!: is a bug, if another nogo on the way was overritten - as only the last nogo is stored at he moment.
            || rmd.isInfinite()
            || dist>MAX_INTERESTING_NROF_HOPS ) {
            return INFINITE_DISTANCE;
        }
        if (// some more exception for pawns
            colorlessPieceType(vPce.getPieceType())==PAWN ) {
            if (// a pawn at dist==1 can beat, but not "run into" a piece
                //Todo: other dists are also not possible if the last step must be straight - this is hard to tell here
                    fileOf(myPos)==fileOf(vPce.myPiece().getPos()) && dist==1
            ) {
                return INFINITE_DISTANCE;
            }
            //in the same way do only count with extra distance, if it requires the opponent to do us a favour by
            // moving a piece to beat in the way (except the final piece that we want to evaluate the attacks/covers on)
            return dist + rmd.countHelpNeededFromColorExceptOnPos(opponentColor(vPce.color()),myPos);
        }

        // if distane is unconditional, then there is nothing more to think about:
        if (rmd.isUnconditional())
            return dist;

        // correct?: seems strange...
        //// other than that: same color coverage is always counted
        //if (myPieceID!=NO_PIECE_ID && vPce.color()==myPiece().color())
        //    return dist;
        //// from here on it is true: assert(colorlessPieceType(vPce.getPieceType())!=colorlessPieceType(myPieceType())
        //return dist;


        // a dist==1 + condition means a pinned piece, but actually no direct attack
        // so we cannot count that piece as covering or attacking directly,
        if (dist==1 && rmd.nrOfConditions()>0)
            return 2;
        else
            return dist;  // else simply take the dist... same color conditions are counted as 1 dist anyway and opponent ones... who knows what will happen here

        // this case has become very simple :-)
        // it used to be that: add opponents if they are in beating distance already or of different piece type (as
        //      the same piece type can usually not come closer without being beaten itself
        // but: now this should already be covered by the NoGo mechanism.
        // was:  if ( colorlessPieceType(vPce.getPieceType())==colorlessPieceType(myPieceType())
        //             && rmd.dist()==1 )
        // TODO: check+testcases if this working (even for the formerly complex cases: "but do not add queen or king on hv-dirs with dist>2 if I have a rook
        //        //       and also not a queen or king on diagonal dirs with dit <2 if I have a bishop"
        // TODO - to be implemented not here, but in the nogo-code!: check if piece here is king-pinned or costly to move away then opponent could perhaps come closer anyway

        //??         && ( ! (colorlessPieceTypeNr(vPce.getPieceType())==QUEEN   // similarly a queen cannot attack a rook or a bishop  if it is the attack direction with the same distance backwards
        //                   && myChessBoard.getBoardSquares()[vPce.myPos].getvPiece(myPieceID).rawMinDistance.dist()==vPce.getMinDistanceFromPiece().getShortestDistanceEvenUnderCondition() ) ) ) )

    }

    private int myPieceType() {
        if (isSquareEmpty()
)
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
                (isSquareEmpty() ? " is empty" : " with " + myPiece()) +
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
                .max().orElse(0);
        return clashResultsLastUpdate >= maxLatestUpdate;
    }

    public int clashEval() {
        //still needed - or always up to date after a move?
        getClashes();  // assures clashes are calculated if outdated
        int eval=0;
        for (int i=1; i<MAX_INTERESTING_NROF_HOPS; i++) {
            eval += clashResultPerHops[i]>>(i-1);
        }
        return eval;
    }

    public int clashEval(int level) {
        //still needed - or always up to date after a move?
        getClashes();  // assures clashes are calculated if outdated
        return clashResultPerHops[level];
    }

    /**
     *
     * @param color (as boolean, see ChessBasics) Color of side for which it is checked to come to this square
     * @return boolean to tell if a piece exists, that has a dist==1 (for now only this dist counts) and has a relEval
     * that generally (without sacrifice etc.) allows to come here
     */
    public boolean isColorLikelyToComeHere(boolean color) {
        int ci = colorIndex(color);
        // difficulty: pawns may be able to come here straightly - then theay are not contained in the "coverage" list used above...
        // so check pawns first.
        final int colPawnPieceType = (isWhite(color) ? PAWN : PAWN_BLACK);
        for( VirtualPieceOnSquare vPce : vPieces )
            if (vPce!=null
                && vPce.getPieceType()==colPawnPieceType
                && fileOf(vPce.getPiecePos())==fileOf(getMyPos())
                && vPce.getMinDistanceFromPiece().dist()==1
                && evalIsOkForColByMin( vPce.getRelEval(), color, EVAL_TENTH ) )
                return true;
        // then all others (already in the coverage list)
        List<VirtualPieceOnSquare> directCandidates = new ArrayList<>(coverageOfColorPerHops.get(1).get(ci));
        for( VirtualPieceOnSquare vPce : directCandidates )
            if ( evalIsOkForColByMin( vPce.getRelEval(), color, EVAL_TENTH ) )
                return true;
        return false;
    }

}
