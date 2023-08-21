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

import java.util.*;
import java.util.stream.Collectors;

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBasics.calcDirFromTo;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.ConditionalDistance.INFINITE_DISTANCE;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static java.lang.Math.max;

public class Square {
    private static final int MAX_LOOKAHEAD_FOR2NDROW_CANDIDATES = 4;
    final ChessBoard board;
    private final int myPos; // mainly for debugging and output
    private int myPieceID;  // the ID of the ChessPiece sitting directly on this square - if any, otherwise NO_PIECE_ID
    private final List<VirtualPieceOnSquare> vPieces;  // TODO: change to plain old []

    private int clashEvalResult = 0;
    private int[] futureClashResults = null;
    private long clashResultsLastUpdate = -1;

    // new implementation of clash calculation is without GlubschFishes CBM code - able to deal wit varying piece values, but non-caching
    /**
     * coverageOfColorPerHops.get(HOPCOUNT).get(COLOR) -> List of vPces
     * HOPCOUNT==0 is actuall hopcount 1 with direct clash and
     * HOPCOUNT==1 is hopcount 1 in 2nd row. all following n are n...
     */
    List<List<List<VirtualPieceOnSquare>>> coverageOfColorPerHops;
    boolean[] blocksCheckFor = new boolean[2];  // tells if a piece here can block a check here (for king with colorindex) by taking a checker of moving in the way


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
        this.board = myChessBoard;
        this.myPos = myPos;
        myPieceID = NO_PIECE_ID;
        vPieces = new ArrayList<>(MAX_PIECES);
        coverageOfColorPerHops = new ArrayList<>(MAX_INTERESTING_NROF_HOPS+1);
        //clashResultsNowAndFuture = new int[MAX_INTERESTING_NROF_HOPS];
        for (int h=0; h<=MAX_INTERESTING_NROF_HOPS; h++) {
            coverageOfColorPerHops.add(new ArrayList<>(2));
            coverageOfColorPerHops.get(h).add(new ArrayList<>()); // for white
            coverageOfColorPerHops.get(h).add(new ArrayList<>()); // for black
        }
    }

    void prepareNewPiece(int newPceID) {
        vPieces.add(newPceID, VirtualPieceOnSquare.generateNew(board, newPceID, getMyPos() ));
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
            board.removePiece(myPieceID);
        } else {
            // TODO: does not work for rook, when castling - why not?
            //  assert (vPieces.get(pid).realMinDistanceFromPiece() == 1);
        }
        myPieceID = pid;
    }

    public void removePiece(int pceID) {
        vPieces.set(pceID,null);
        futureClashResults = null;
        //TODO-optimize: do this only, if piece was relevant for clashes
        clearCoveragePerHopsLists();
        clashResultsLastUpdate = 0; // outdated
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
                vPce.quePropagateDistanceChangeToAllNeighbours();
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
                    ChessPiece blockingPiece = board.getPieceAt(blockingPiecePos);
                    if (blockingPiece!=null && blockingPiece.color()==color) // Problem ==null on 9th game move Qxd5 -> here square e1 vPce id=3, blockingPcePos==36==e4 is empty
                        result.add(blockingPiecePos);   // the pos is stored in the fromCondition from where the piece needs to disappear from, so that vPce covers the wanted square.
                }
            }
        }
        return result;
    }

    String getCoverageInfoByColorForLevel(final boolean color, final int level) {
        StringBuilder s = new StringBuilder(20);
        s.append(level).append(":");
        if (level==1 && coverageOfColorPerHops.get(0).get(colorIndex(color)).size()>0 ) {
            // at (0) there are also level==1vPieces, so print them also
            for (VirtualPieceOnSquare vPce : coverageOfColorPerHops.get(0).get(colorIndex(color)))
                s.append(fenCharFromPceType(vPce.getPieceType()));
            if (coverageOfColorPerHops.get(1).get(colorIndex(color)).size()>0)
                s.append("+");
        }
        for (VirtualPieceOnSquare vPce:coverageOfColorPerHops.get(level).get(colorIndex(color)))
            s.append(fenCharFromPceType(vPce.getPieceType()));
        return s.toString();
    }


    public int getClosestChanceReachout(boolean color) {
        int closest = MAX_INTERESTING_NROF_HOPS+1;
        for (VirtualPieceOnSquare vPce:vPieces) {
            if (vPce!=null && vPce.color()==color) {
                int r = vPce.getClosestChanceReachout();
                if (r < closest) {
                    closest = r;
                }
            }
        }
        return closest;
    }

    public String getClosestChanceMove(boolean color) {
        int closest = MAX_INTERESTING_NROF_HOPS+1;
        String moves = "no moves with chances";
        for (VirtualPieceOnSquare vPce:vPieces)
            if (vPce!=null && vPce.color()==color) {
                int r = vPce.getClosestChanceReachout();
                if (r < closest) {
                    closest = r;
                    Set<Move> m = vPce.getFirstUncondMovesToHere();
                    if (m != null)
                        moves = vPce.myPiece().symbol() + m.toString();
                }
            }
        return moves;
    }


    /**
     * calculates the clash result if a piece vPceOnSquare is on a square directly (d==1) covered
     * by whites and blacks. it excludes the one excludeVPce. this is usefule to calc as if that pce had
     * moved here, to check if that is possible.
     * Does not change/manipulate the lists and contents of whites + backs.
     * But be careful: it destroys/consumes whiteOthers and blackOthers!
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

        // see if whites and blacks need to be filled up from the 2nd row, as conditions are fulfilled now:
        if (whiteOthers!=null)
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
        if (blackOthers!=null)
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

        // start simulation with my own piece on the square and the opponent to decide whether to take it or not
        int resultIfTaken = -vPceOnSquare.getValue();
        VirtualPieceOnSquare assassin;
        if (isWhite(turn)) {
            if (whites.size() == 0)
                return 0;
            assassin = whites.get(0); //.poll();
            if (assassin==excludeVPce) {
                if (whites.size() == 1)
                    return 0;
                assassin = whites.get(1); //.poll();
                whites = whites.subList(2, whites.size() );
            } else
                whites = whites.subList(1, whites.size() );
        } else {
            if (blacks.size() == 0)
                return 0;
            assassin = blacks.get(0); //.poll();
            if (assassin==excludeVPce) {
                if (blacks.size() == 1)
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
        moves.add(new Move( assassin.getMyPiecePos(),
                vPceOnSquare.myPos));  // ToDo: Make+use getter for myPos
        //// filling up whites and blacks from 2nd row, was originally implemented here, but in cases where the 1st row is empty from the beginning, this was not working.

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
     * Evaluates the clash result on each square (as is) and the "relEval" of all vPieces - telling what
     * would happen if it came there (with a "survival"-idea, i.e. e.g. after the clash there is resolved and it became safe)
     */
    void updateClashResultAndRelEvals() {
        if (areClashResultsUpToDate())
            return;  // nothing new to calculate
        clashResultsLastUpdate = board.nextUpdateClockTick();

        // update/set coverageOfColorPerHops
        clearCoveragePerHopsLists();
        // run over all vPieces on this square and correctly build the pre-ordered vPce-Lists
        // (that are later used to calculate the clashes)
        // at the same time find clash candidates, that will be sorted into the two above during clash evaluation
        List<List<VirtualPieceOnSquare>> clashCandidates = new ArrayList<>(2);
        //clash2ndRow those "in second row" (Queen behind a rook, bishop behind a pawn, etc.)
        List<List<VirtualPieceOnSquare>> clash2ndRow = new ArrayList<>(2);
        for (int ci = 0; ci <= 1; ci++) {
            clashCandidates.add(new ArrayList<>());
            clash2ndRow.add(new ArrayList<>());
        }
        if (DEBUGMSG_CLASH_CALCULATION) {
            debugPrintln(DEBUGMSG_CLASH_CALCULATION, "");
            debugPrint(DEBUGMSG_CLASH_CALCULATION, "Evaluating " + this + ": ");
        }

        for (VirtualPieceOnSquare vPce : vPieces)
            if (vPce!=null) {
                int d = vPceCoverOrAttackDistance(vPce);
                // fill clashCandidates initially with those clearly directly covering/attacking the square + sort it according to Piece value
                if (d == 1) {
                    debugPrint(DEBUGMSG_CLASH_CALCULATION, " +adding direct clash candidate:");
                    putVPceIntoCoverageList(vPce, 0);
                    clashCandidates.get(colorIndex(vPce.color()))
                            .add(vPce);
                }
                // fill 2nd row clash candidates
                else if (d <= MAX_LOOKAHEAD_FOR2NDROW_CANDIDATES    // we only look max 4 hops ahead. enough for a queen behind a rook and another rook - we neglect e.g. having 2 queens and 2 rooks in a row... - now 4 as 3 is not enough if bishop behind pawn, where pawn cannot move easily (and is 1+3+1==4...)
                        && !vPce.getRawMinDistanceFromPiece().isUnconditional()
                        && vPce instanceof VirtualSlidingPieceOnSquare
                        && ((VirtualSlidingPieceOnSquare) vPce).fulfilledConditionsCouldMakeDistIs1()) {
                    //  || p instanceof VirtualPawnPieceOnSquare && p.getRawMinDistanceFromPiece().dist()==1 )
                    if (DEBUGMSG_CLASH_CALCULATION)
                        debugPrint(DEBUGMSG_CLASH_CALCULATION, " +adding " + vPce
                            + " to 2nd row clash candidates with d=" + d + " ");
                    clash2ndRow.get(colorIndex(vPce.color()))
                            .add(vPce);
                } else if (d < MAX_INTERESTING_NROF_HOPS && d > 0)               // sort all others into their bucket according to d...
                    putVPceIntoCoverageList(vPce, d);
            }
        for (int ci = 0; ci <= 1; ci++) {
            clashCandidates.get(ci).sort(VirtualPieceOnSquare::compareTo);
        }

        // simulate the clash!
        // instead of recursion (prev. implementation) now we loop down to integrate the 2nd row candidates at the
        // right spot (and remember the natural end of the clash, to later loop back up from there to collect the
        // results (the intermediate results stored for usage further down).
        // For the integration of piece from the "2nd row", it already matters whose turn (firstturn) it is.
        final int myPieceCIorNeg = isSquareEmpty() ? -1
                : colorIndex(colorOfPieceType(myPieceType()));
        for (int firstTurnCI = 0; firstTurnCI<=1; firstTurnCI++) {
            if (firstTurnCI==myPieceCIorNeg)
                continue;   // skip 2nd run: if there is a piece on the square, only calc opponent is moving/beating here first.
            //TODO do not skip=continue here for same color as piece on square, but calc if was useful, if an own piece would come closer
            int turnCI = firstTurnCI;  // we alternate, which color makes the 1st move ... and the 3rd, 5th,...
            int exchangeCnt = 0;
            int[] resultIfTaken = new int[clashCandidates.get(0).size() + clashCandidates.get(1).size()
                    + clash2ndRow.get(0).size() + clash2ndRow.get(1).size() + 1];
            resultIfTaken[0] = (isSquareEmpty() || (colorlessPieceType(myPiece().getPieceType())==KING)
                    ? 0   // treat king like empty square - it will never be beaten directly, but move away before
                    : -myPiece().getValue());
            VirtualPieceOnSquare assassin = null;
            List<Move> moves = new ArrayList<>();
            final boolean noOppDefenders = clashCandidates.get(turnCI^1).size() == 0;  // defender meaning opposite color defenders compared to the first assassin (whos turn is assumend at this evaluation round)
            List<List<VirtualPieceOnSquare>> clashCandidatesWorklist = new ArrayList<>(2);
            for (int ci = 0; ci <= 1; ci++)
                clashCandidatesWorklist.add(clashCandidates.get(ci).subList(0, clashCandidates.get(ci).size()));

            while (clashCandidatesWorklist.get(turnCI).size()>0) {
                // take the first vPiece (of whose turn it is) and virtually make the beating move.
                assassin = clashCandidatesWorklist.get(turnCI).get(0);  // emulate pull()
                clashCandidatesWorklist.set(turnCI,
                        clashCandidatesWorklist.get(turnCI).subList(1, clashCandidatesWorklist.get(turnCI).size()));
                moves.add(new Move(assassin.getMyPiecePos(), getMyPos()));
                // pull more indirectly covering pieces into the clash from the "2nd row"
                for (int ci = 0; ci <= 1; ci++) {
                    for (Iterator<VirtualPieceOnSquare> iterator = clash2ndRow.get(ci).iterator(); iterator.hasNext(); ) {
                        VirtualPieceOnSquare row2vPce = iterator.next();
                        ConditionalDistance row2vPceMinDist = row2vPce.getMinDistanceFromPiece();
                        if (row2vPceMinDist.movesFulfillConditions(moves) > 0
                                && row2vPceMinDist.distWhenAllConditionsFulfilled(ci == 0 ? WHITE : BLACK) == 1) {
                            debugPrint(DEBUGMSG_CLASH_CALCULATION, " +adding 2nd row clash candidate:");
                            clashCandidatesWorklist.get(ci).add(row2vPce);
                            putVPceIntoCoverageList(row2vPce, 1);
                            iterator.remove();
                            clashCandidatesWorklist.get(ci).sort(VirtualPieceOnSquare::compareTo); //TODO-Bug? prpbably wrong, must be sorted, but only behind the piece that moved first to enable this piece from the second row
                            break;  // if one is found, there cannot be another behind the one that moved that also directly covers now.
                        }
                    }
                }
                // prepare the next round
                turnCI ^= 1;
                exchangeCnt++;
                resultIfTaken[exchangeCnt] = -assassin.getValue();
            }

            // if 2nd row candidates are still left after the 2nd turn (both colors have started), sort them in as normal late pieces
            if (firstTurnCI==1 || (firstTurnCI==0 && myPieceCIorNeg==1) )
                for (int ci = 0; ci <= 1; ci++) {
                    for ( VirtualPieceOnSquare row2vPce: clash2ndRow.get(ci) ){
                        int d = vPceCoverOrAttackDistance(row2vPce);
                        if (d<=MAX_INTERESTING_NROF_HOPS)
                            putVPceIntoCoverageList(row2vPce, d );
                    }
                }
            for(int h=0; h<MAX_INTERESTING_NROF_HOPS; h++) {
                coverageOfColorPerHops.get(h).get(0).sort(VirtualPieceOnSquare::compareTo); // for white
                coverageOfColorPerHops.get(h).get(1).sort(VirtualPieceOnSquare::compareTo); // for black
            }
            // if nothing happened - i.e. no direct piece of firstTurnCI is there
            if (exchangeCnt==0) { // nothing happened - original piece stays untouched,
                for (VirtualPieceOnSquare vPce : vPieces) if (vPce!=null) {
                    vPce.setClashContrib(0);
                    if (vPce.getPieceID() == myPieceID) {
                        vPce.setRelEval(0);  // I have a good stand here, no threats, so no eval changes (just +/-1 to signal "ownership") :-)
                    }
                    else if (colorIndex(vPce.color()) == firstTurnCI) {
                        if (vPce.getRawMinDistanceFromPiece().hasNoGo()
                                || vPce.getMinDistanceFromPiece().dist() > MAX_INTERESTING_NROF_HOPS) {
                            vPce.setRelEval(isWhite(vPce.color()) ? -EVAL_DELTAS_I_CARE_ABOUT : EVAL_DELTAS_I_CARE_ABOUT);  // I cannot really come here -> so a just enough bad value will result in a NoGo Flag
                            //alternative: vPce.setRelEval(NOT_EVALUATED);
                        }
                        else  { // opponent comes here in the future to beat this piece
                            int sqPceTakeEval = resultIfTaken[0];   //(!isSquareEmpty() ? myPiece().getValue() : 0);
                            if (noOppDefenders)  // ... and it is undefended
                                if (isSquareEmpty()) {
                                    if (clashCandidates.get(colorIndex(vPce.color())).size() == 0)
                                        vPce.setRelEval(0);  // it can go there but it would not be defended there
                                    else
                                        vPce.setRelEval(vPce.myPiece().isWhite() ? 2 : -2);  // it would be defended there
                                } else
                                    vPce.setRelEval(sqPceTakeEval);
                            else
                                vPce.setRelEval(sqPceTakeEval - vPce.myPiece().getValue());  // it is defended, so I'd loose myself
                        }
                    } //else i.e. same color Piece
                }
                clashEvalResult = Integer.compare( clashCandidates.get(0).size(), clashCandidates.get(1).size() );
                // TODO? clean up / correct coverage piece lists
            }
            else {
                // run backwards to see how far beating was useful or if that side had actually stopped beating
                int resultFromHereOn = 0;
                int endOfClash = exchangeCnt;
                int myBeatResult = 0;
                resultIfTaken[exchangeCnt] = resultFromHereOn;  // the very last piece can always safely go there.
                for (int i = exchangeCnt; i > 0; i--) {
                    myBeatResult = resultFromHereOn + resultIfTaken[i - 1];
                    boolean shouldBeBeneficialForWhite = ((i % 2 == 1) == (firstTurnCI == CIWHITE));
                    if ((myBeatResult > -EVAL_DELTAS_I_CARE_ABOUT && shouldBeBeneficialForWhite   // neither positive result evaluation and it is whites turn
                            || myBeatResult < EVAL_DELTAS_I_CARE_ABOUT && !shouldBeBeneficialForWhite)   // nor good (i.e. neg. value) result for black
                    ) {
                        resultFromHereOn = myBeatResult;
                    } else {
                        resultFromHereOn = 0;      // it was not worth beating from here on, sw we calc upwards that wi did not take:
                        endOfClash = i - 1;  // and we remember that position - points "one to high", so is 2 (==third index) if two pieces took something in the clash
                    }
                    resultIfTaken[i - 1] = resultFromHereOn;
                }

                if (myPieceCIorNeg != -1)
                    clashEvalResult = resultFromHereOn;

                // derive relEvals for all Pieces from that
                for (VirtualPieceOnSquare vPce : vPieces)      // && colorIndex(vPce.color())==firstTurnCI
                    if (vPce != null
                            && vPce.getRawMinDistanceFromPiece().dist()<=MAX_INTERESTING_NROF_HOPS
                            && (myPieceCIorNeg != -1 || colorIndex(vPce.color()) == firstTurnCI)) {
                        vPce.setClashContrib(0);
                        if (vPce.getPieceID() == myPieceID) {
                            vPce.setRelEval(resultFromHereOn);  // If I stay, this will come out.
                            continue;
                        }
                        int vPceFoundAt = clashCandidates.get(colorIndex(vPce.color())).indexOf(vPce);
                        if (vPceFoundAt > -1) {
                            int vPceClashIndex = vPceFoundAt * 2 + (colorIndex(vPce.color()) == firstTurnCI ? 0 : 1);  // convert from place in clashCandidates to final clash order
                            int clashContrib = 0;
                            if ( vPceClashIndex <= endOfClash-1 && myPieceID!=NO_PIECE_ID   // if vPce is part of the clash (even the last) remember its contribution to the clash.
                                    ||  (vPceClashIndex > endOfClash-1 && vPceClashIndex < exchangeCnt
                                        && myPieceID!=NO_PIECE_ID && vPce.color()==myPiece().color() ) // vPce was not part of the active clash fight, but part of the remaining defence, so it could still contribute in covering
                            ) {
                                // TODO: check: usage of this old method might be incorrect in some cases concerning Pieves from the 2ndRow (see above)
                                int clashResultWithoutVPce = calcClashResultExcludingOne(ChessBasics.isWhite(firstTurnCI),
                                        board.getBoardSquares()[myPos].getvPiece(myPieceID),
                                        clashCandidates.get(CIWHITE),
                                        clashCandidates.get(CIBLACK),
                                        vPce,
                                        new ArrayList<VirtualPieceOnSquare>(0),
                                        new ArrayList<VirtualPieceOnSquare>(0),
                                        null);  //Todo: Check if a first move needs to be added, as it could already fulful conditions!
                                clashContrib = clashEvalResult-clashResultWithoutVPce;
                            }
                            if (vPceClashIndex == 0) {
                                // this vPce was anyway the first mover in the normal clash -> take clash result
                                if (endOfClash==0)  // this means already the first clash-move would not have been done
                                    vPce.setRelEval(resultIfTaken[1]  // so lets document=set the bad result here
                                            - ( (myPiece() == null ) // NOT, was attepted in v0.29z2, but not improving:  || isKing(myPiece().getPieceType()) ) // || isKing(myPiece().getPieceType()) ) //was .29z2 hmm, unclear maybe negative. Idea (seeming correct, but maybe it stopped vPce to move away?): unless it is a king which is calculated as moving away before...
                                                ? 0 : myPiece().getValue()) );  // minus the piece standing there,
                                else {
                                    vPce.setRelEval(resultFromHereOn);
                                    // reduce/anihilate clashContribution as moving there is anyway reflected in the relEval and thus later in the direct move (and also as lost contribution in the pieces' other moves)
                                    clashContrib >>= 2;  // may be could even be =?.
                                }
                                // Todo: may be necessary to distinguish empty square with first mover from occupied square?
                            }
                            else if (vPceClashIndex < endOfClash-1) {
                                // this vPce is part of the normal clash
                                //Todo: How to set relEval properly:
                                // e.g. distinguish, if vPce can move here first and how this would influence the value
                                // this would be: old_updateRelEval(vPce);
                                // or/and if square is not empty: do assume the clash goes on as normal and take the clash result
                                // (+somehow mark vPce as being part of the clash + dying if not the very last in the clash)
                                //Todo!! this relates to Todo!! one else clause below.
                                if ( evalIsOkForColByMin(resultIfTaken[vPceClashIndex], vPce.color()))
                                    vPce.setRelEval(-vPce.getValue()); // vPce would be killed in the clash, so it can go here, but not go pn from here -> so a bad value like checkmate will result in a NoGo Flag, but not really say the truth -> a clash calc (considering if this Pce would go either first or wait until the end of the clash) is needed!
                                else
                                    //original code (see Todo-comment): vPce.setRelEval(resultIfTaken[vPceClashIndex]); // if the continuation of the clash is negative anyway, this is taken as the relEval
                                    old_updateRelEval(vPce);  // alternative - however, it generates much more Nogos, although Piece could come here after hte clash - we need a "clash-fought-out" condition...

                            }
                            else if (vPceClashIndex == endOfClash-1) {  // was last in clash
                                //TODO!! - 2 different conflicting cases - a result from after the clash (resultIfTaken[vPceClashIndex + 1])
                                //  indicates that the Piece can go+be here (after the clash) and thus has no NoGo and can continue
                                //  However: This positive value also indicates (for the move selection) that it could go
                                //  here immediately - which is not true, so a Piece needs to be able to distinguish these 2 cases.
                                // original code: vPce.setRelEval(resultIfTaken[vPceClashIndex + 1]); // or: checkmateEval(vPce.color()));  // or: resultFromHereOn); // or?: willDie-Flag + checkmateEval(vPce.color()));  // vPce would be killed in the clash, so it can go here, but not go pn from here -> so a bad value like checkmate will result in a NoGo Flag
                                old_updateRelEval(vPce);  // alternative - however, it generates much more Nogos, although Piece could come here after hte clash - we need a "clash-fought-out" condition...
                            }
                            else {
                                // check if right at the end of the clash, this vPce could have taken instead
                                // todo: it is treated to simple here, if clash was only fought out half way.
                                // Todo: is incorrect, if current vpce origins from the "2nd row", while its enabling piece was the last one.
                                // TODO!: is also incorrect if vPce is the one activating a 2nd-row-piece of the opponent
                                int nextOpponentAt = vPceFoundAt + (colorIndex(vPce.color()) == firstTurnCI ? 0 : 1);
                                if (nextOpponentAt >= clashCandidates.get(colorIndex(!vPce.color())).size()) {
                                    // no more opponents left, so yes we can go there - but only after the clash & if it actually took place
                                    old_updateRelEval(vPce);  //see todo above...
                                    //vPce.setRelEval(0);
                                }
                                else if (vPce.myPiece().isWhite() && vPce.getValue() - EVAL_DELTAS_I_CARE_ABOUT >= -clashCandidates.get(colorIndex(!vPce.color())).get(nextOpponentAt).myPiece().getValue()
                                        || !vPce.myPiece().isWhite() && vPce.getValue() + EVAL_DELTAS_I_CARE_ABOUT <= -clashCandidates.get(colorIndex(!vPce.color())).get(nextOpponentAt).myPiece().getValue()
                                )   // i am more valuable than the next opponent
                                    vPce.setRelEval(-vPce.getValue());  // vPce would be killed in the clash, so it can go here, but not go pn from here -> so a bad value like checkmate will result in a NoGo Flag
                                else {
                                    // no more opponents left, so yes we can go there - but only after the clash
                                    old_updateRelEval(vPce);  //see todo above...
                                    //vPce.setRelEval(0);
                                }
                            }
                            if (DEBUGMSG_MOVEEVAL && abs(clashContrib)>4
                                    && board.currentDistanceCalcLimit()==MAX_INTERESTING_NROF_HOPS)  // actually we do not know at what level it is called the final time, overriding the prev. calculations (which are not well sorted out yet)
                                debugPrintln(DEBUGMSG_MOVEEVAL, "Adding ClashContrib of " + clashContrib + " to " + vPce + ".");
                            vPce.addClashContrib(clashContrib);
                        } else {
                            // vPce is not in the clash candidates
                            // TODO!! implementation of this case still needed - simulate, if this vPce would come here?
                            // for now set to 0 if no opponents or use old evaluation for the simulation
                            if (endOfClash == exchangeCnt - 1 // clash was beaten until the very end
                                    || (clashCandidates.get(colorIndex(vPce.myOpponentsColor())).size() == 0
                                    && clashCandidates.get(colorIndex(vPce.color())).size() > 0) ) {  // ... opponent has no defenders, but vPce has own defenders
                                //|| clashCandidates.get(colorIndex(vPce.color())).size() > clashCandidates.get(colorIndex(!vPce.color())).size())   // ... opponent has no more defenders, so the assassin would be undefended after beating
                                old_updateRelEval(vPce);  //see todo above...
                                //vPce.setRelEval(0);  // no more opponents left, so yes we can co there
                            }
                            else
                                /*vPce.setRelEval(*/ old_updateRelEval(vPce); // );
                            // TODO! - complete implementation here, the old_method is only simplified.
                            //  truth is: a virtual clash calculation just like in this method is needed
                        }
                    }
            }
        }
    }


    private void clearCoveragePerHopsLists() {
        clashResultsLastUpdate = -1;
        for(int h=0; h<MAX_INTERESTING_NROF_HOPS; h++) {
            coverageOfColorPerHops.get(h).get(0).clear(); // for white
            coverageOfColorPerHops.get(h).get(1).clear(); // for black
        }
    }

    private void putVPceIntoCoverageList(VirtualPieceOnSquare vPce, int d) {
        // add this piece to the list of attackers/defenders
        debugPrint(DEBUGMSG_CLASH_CALCULATION, " +adding " + vPce + " at d=" + d + " ");
        coverageOfColorPerHops
                .get(d)
                .get(colorIndex(vPce.color()))
                .add(vPce);
    }


    /**
     * Evaluate the "relEval" of one of my vPieces - telling what would happen if that Piece came here (first of all)
     * @param evalVPce one virtual Piece to calculate
     */
    private void old_updateRelEval(VirtualPieceOnSquare evalVPce) {
        //already called: getClashes();  // makes sure clash-lists are updated
        // for debug reasons:  do nothing, just assume every clash result is 0:
        //evalVPce.setRelEval(0);
        //return;

        boolean turn = opponentColor( colorOfPieceType(evalVPce.getPieceType()) );
        int currentResult = 0;
        List<VirtualPieceOnSquare> whites = new ArrayList<>(coverageOfColorPerHops.get(0).get(CIWHITE));
        List<VirtualPieceOnSquare> blacks = new ArrayList<>(coverageOfColorPerHops.get(0).get(CIBLACK));
        whites.addAll(coverageOfColorPerHops.get(1).get(CIWHITE));
        blacks.addAll(coverageOfColorPerHops.get(1).get(CIBLACK));
        boolean fuzzedWithKingInList = false;
        VirtualPieceOnSquare currentVPceOnSquare = null;
        if (myPieceID!=NO_PIECE_ID) {
            currentVPceOnSquare = getvPiece(myPieceID);
            ConditionalDistance rmd = evalVPce.getRawMinDistanceFromPiece();
            int dist = rmd.dist();
            if (colorOfPieceType(currentVPceOnSquare.getPieceType())==colorOfPieceType(evalVPce.getPieceType())
                || (isPawn(evalVPce.getPieceType())
                    && fileOf(myPos)==fileOf(evalVPce.myPos) && (dist==1 || (dist-rmd.nrOfConditions())==1) )
            ) {
                // cannot move on a square already occupied by one of my own pieces
                // + also cannot move a pawn straight into a piece.
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
                fuzzedWithKingInList = true;
            }
            else
                currentResult = -currentVPceOnSquare.getValue();
        }

        List<VirtualPieceOnSquare> whiteOthers = new ArrayList<>(); //coverageOfColorPerHops.get(2).get(CIWHITE));
        List<VirtualPieceOnSquare> blackOthers = new ArrayList<>(); //coverageOfColorPerHops.get(2).get(CIBLACK));
        // TODO-refactor: this code piece is duplicated
        for(int h = 2; h< min(4, MAX_INTERESTING_NROF_HOPS); h++) {
            whiteOthers.addAll(coverageOfColorPerHops
                    .get(h).get(CIWHITE)
                    .stream()
                    .filter(VirtualPieceOnSquare::isConditional )
                    .collect(Collectors.toList() )
            );
            blackOthers.addAll((Collection<? extends VirtualPieceOnSquare>) coverageOfColorPerHops
                    .get(h).get(CIBLACK)
                    .stream()
                    .filter(VirtualPieceOnSquare::isConditional )
                    .collect(Collectors.toList() )
            );
        }

        List<Move> moves = new ArrayList<>();
        moves.add(new Move( evalVPce.getMyPiecePos(), myPos));

        if ( isSquareEmpty()
                && isPawn(evalVPce.getPieceType())
                && fileOf(evalVPce.getMyPiecePos())!=fileOf(getMyPos())  // only for beating scenarios -
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
                        && fileOf(vPce.getMyPiecePos())==fileOf(getMyPos())
                        && vPce.getMinDistanceFromPiece().dist()<3  // hard coded, hmm, a pawn can come here in 2 straight moves (not 100% precise...)
                        // also accounted here, although it goes there to calculate, not to die ;-):
                        && evalIsOkForColByMin( vPce.getRelEval(), turn)
                ) {
                    firstMover = vPce;
                    break;
                }
            if (firstMover==null) {  // --> no straight pawn victim found,
                // we could take a Piece from the other attackers, but as this is a pawn guarding the square, that piece would always have a NoGo to get here and never do it.
                turn = opponentColor(turn);
                currentResult = -evalVPce.getValue(); // checkmateEval(turn);  // a "random" very high bad value, so the piece will get a NoGo later in the algorithm
            } else
                currentResult += calcClashResultExcludingOne(turn,firstMover,  // the opponents pawn is now on the square
                        whites, blacks, null,   // the vPce is not excluded, it is now part of the clash (it had to be moved to ahead of the list, but as it is a pawn it is there (among pawns) anyway.
                        whiteOthers, blackOthers, moves);
        } else
            currentResult += calcClashResultExcludingOne(turn,evalVPce,   // the vPce itself goes first
                    whites, blacks, evalVPce,    // and is thus excluded from the rest of the clash
                    whiteOthers, blackOthers,
                    moves);
        evalVPce.setRelEval(currentResult);

        if (fuzzedWithKingInList) {
            if (currentVPceOnSquare.getPieceType()==KING)
                whites.remove(currentVPceOnSquare);
            else
                blacks.remove(currentVPceOnSquare);
        }
    }


    int warningLevel() {
        if (abs(clashEval())>3)
            return 1000;
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
        if (futureClashResults ==null) {
            //System.err.println("futureClashEval() called, but never calculated.");
            return new int[0];
        }
        return futureClashResults;
    }

    // todo: add similar check for normal forks  (compare vs. todays improvised method of 2 benefits in the same distance of a legal move)
    void evalCheckingForks() {
        for (VirtualPieceOnSquare vPce : getVPieces()) {
            if (vPce == null
                    || isKing(vPce.getPieceType())
                    || vPce.getMinDistanceFromPiece().hasNoGo()   // todo!: reward covering pieces to continue to to so!
            )
                continue;
            int inFutureLevel = vPce.getAttackingFutureLevelPlusOne()-1;
            if (inFutureLevel < 0)
                inFutureLevel = 0;
            if (inFutureLevel > MAX_INTERESTING_NROF_HOPS           // out of interest
                    || !vPce.isCheckGiving())                         // no check, no check giving fork...
                continue;
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL, " Evaluating checking fork @" + inFutureLevel + " on square " + squareName(myPos) + " for " + vPce + ": ");
            // find best neighbour benefit besides king-check
            //VirtualPieceOnSquare bestNeighbour = null;
            int max = 0;
            //TODO!!: this only works for non-sliding pieces ... for sliding (or then for all) pieces it needs
            // a different approach of handing all addChances also to the predecessor squares, esp. if they
            // are checking -> then algo can even handle normal forks!
            for (VirtualPieceOnSquare neighbour : vPce.getNeighbours())
                if (neighbour != null && neighbour.myPos != board.getKingPos(opponentColor(neighbour.color()))) {
                    int nBestChance = neighbour.getRelEvalOrZero(); //getBestChanceOnLevel(inFutureLevel);
                    if ((isWhite(vPce.color()) ? nBestChance > max
                            : nBestChance < max)
                    ) {
                        max = nBestChance;
                        //bestNeighbour = neighbour;
                    }
                    //debugPrintln(DEBUGMSG_MOVEEVAL," Found checking fork benefit " + nBestChance +"@"+ inFutureLevel + " on square "+ squareName(myPos)+" for " + vPce + ".");
                    // additionally warn/fee other pieces from going here
                    // solves the bug "5r2/6k1/1p1N2P1/p3n3/2P4p/1P2P3/P5RK/8 w - - 5 45, NOT g2g5"//
                    // BUT makes test games slightly worse - even with just warning = +/-EVAL_TENTH
                    for ( VirtualPieceOnSquare opponentAtForkingDanger : board.getBoardSquare(neighbour.myPos).getVPieces() ) {
                        if (opponentAtForkingDanger==null
                                || opponentAtForkingDanger.color() == vPce.color()               // not forking myself :-)
                                || isKing(opponentAtForkingDanger.getPieceType())
                                || opponentAtForkingDanger.getRawMinDistanceFromPiece().dist() > 1    // too far away, no need to warn
                                || opponentAtForkingDanger.getRawMinDistanceFromPiece().dist() == 0   // already there - todo: motivate to move away?
                                || ( opponentAtForkingDanger.getRawMinDistanceFromPiece().dist()      // it could go there, but would not fall into trap, but even cover the forking square
                                     - getvPiece(opponentAtForkingDanger.getPieceID()).getRawMinDistanceFromPiece().dist() == -1 )
                       /* makes worse:         || ( getvPiece(opponentAtForkingDanger.getPieceID()).getRawMinDistanceFromPiece().dist() == 1  // similar, but piece to be forked already covers the square and will even after moving to forking square
                                        && getvPiece(opponentAtForkingDanger.getPieceID()).getRawMinDistanceFromPiece().isUnconditional()
                                        && dirsAreOnSameAxis(calcDirFromTo(opponentAtForkingDanger.getMyPiecePos(),opponentAtForkingDanger.myPos),
                                                             calcDirFromTo(opponentAtForkingDanger.getMyPiecePos(), getMyPos()) ) )
                       */ )
                            continue;
                        int warnFutureLevel = opponentAtForkingDanger.getAttackingFutureLevelPlusOne() - 1;
                        int warning = - (opponentAtForkingDanger.getValue() + (neighbour.getValue()>>3)); // estimation, forking piece might die or not... TODO: should be calculated more precisely as a real clashResult
                        if ( !evalIsOkForColByMin(warning, opponentAtForkingDanger.color(), -1)) {
                            warning >>= 4; // (isWhite(opponentAtForkingDanger.color()) ? -EVAL_TENTH : EVAL_TENTH); //warning>>2;
                            if (DEBUGMSG_MOVEEVAL && abs(warning) > 4)
                                debugPrintln(DEBUGMSG_MOVEEVAL, " Warning of " + warning + "@" + warnFutureLevel + " about checking fork of on square " + squareName(myPos) + " for " + opponentAtForkingDanger + ".");
                            opponentAtForkingDanger.addRawChance(warning, warnFutureLevel); //, target: neighbour.myPos
                        }
                    }
                }
            if (!evalIsOkForColByMin(max, vPce.color()))
                continue;
            max -= max >> 4;  // reduce fork by 6%
            if (DEBUGMSG_MOVEEVAL && abs(max) > 4)
                debugPrintln(DEBUGMSG_MOVEEVAL, " Detected max checking fork benefit of " + max + "@" + inFutureLevel + " on square " + squareName(myPos) + " for " + vPce + ".");
            vPce.addChance(max, inFutureLevel);
        }
    }

    void calcFutureClashEval() {
        // note: clash-lists must already be updated
        // TODO - make every calculation here change-dependent, not reset and recalc all...
        if (isSquareEmpty()  // bonus for taking control of emty squares is treated elsewhere
                || isKing(myPieceType())   // king is also treated differently
        ) {
            futureClashResults = null;
            return;
        }

        // start simulation with my own piece on the square and the opponent of that piece starting to decide whether to
        // bring in additional attackers

        boolean initialTurn = opponentColor(myPiece().color());
        boolean turn = initialTurn;

        final VirtualPieceOnSquare currentVPceOnSquare = getvPiece(myPieceID);
        List<VirtualPieceOnSquare> whites = new ArrayList<>(coverageOfColorPerHops.get(0).get(CIWHITE));
        List<VirtualPieceOnSquare> blacks = new ArrayList<>(coverageOfColorPerHops.get(0).get(CIBLACK));
        whites.addAll(coverageOfColorPerHops.get(1).get(CIWHITE));
        blacks.addAll(coverageOfColorPerHops.get(1).get(CIBLACK));
        List<VirtualPieceOnSquare> whiteMoreAttackers = coverageOfColorPerHops.get(2).get(CIWHITE);
        List<VirtualPieceOnSquare> blackMoreAttackers = coverageOfColorPerHops.get(2).get(CIBLACK);
        futureClashResults = new int[Math.max(MAX_INTERESTING_NROF_HOPS + 2,
                (Math.max(coverageOfColorPerHops.get(2).get(CIWHITE).size(),
                        coverageOfColorPerHops.get(2).get(CIBLACK).size())
                        * 2 + 1))];
        int nr = 0;
        int bNext = 0;
        int wNext = 0;
        if (DEBUGMSG_FUTURE_CLASHES)
            debugPrintln(DEBUGMSG_FUTURE_CLASHES, "future clashes on " + this);
        //Todo: Add moreAttackers from d==3ff (but with move of opponent in between, if he can till add a d==3 Piee etc....) - is only addad as chances for now, see below
        if (isWhite(turn) && bNext < blackMoreAttackers.size()
                || isBlack(turn) && wNext < whiteMoreAttackers.size()) {
            turn = !turn;   // if opponent still has pieces left, but we don't, then switch sides...
        }
        List<VirtualPieceOnSquare>[] preparer = new ArrayList[]{new ArrayList<>(), new ArrayList<>()};
        VirtualPieceOnSquare additionalAttacker = null;
        VirtualPieceOnSquare prevAddAttacker = null;
        int prevFutureLevel = 0;
        int multipleAdditions = 0;  // nr of multiple additional attackers of the same color (after the opponent had run out of additional pieces)
        loop: while ( /*isWhite(turn) ? */  wNext < whiteMoreAttackers.size() ||
                /*:*/ bNext < blackMoreAttackers.size()) {
            if (DEBUGMSG_FUTURE_CLASHES)
                debugPrintln(DEBUGMSG_FUTURE_CLASHES, "");
            // bring additional pieces in
            if (isWhite(turn)) {
                debugPrint(DEBUGMSG_FUTURE_CLASHES, "White adds " + whiteMoreAttackers.get(wNext));
                additionalAttacker = whiteMoreAttackers.get(wNext);
                while (wNext < whiteMoreAttackers.size()
                        && additionalAttacker.getRawMinDistanceFromPiece().dist()<2) {
                    // skip this attacker, it must be a non2nd-row attacker with D==1 and a condition, so it cannot be benefited to come closer really...)
                    wNext++;
                    if ( wNext >= whiteMoreAttackers.size() ) {
                        // it was the last one, we should not even have entered this loop for white
                        turn = !turn;
                        continue loop;
                    }
                    additionalAttacker = whiteMoreAttackers.get(wNext);
                }
                whites.add(additionalAttacker);
                whites.sort(VirtualPieceOnSquare::compareTo);
                wNext++;
            } else { // blacks turn
                debugPrint(DEBUGMSG_FUTURE_CLASHES, "Black adds " + blackMoreAttackers.get(bNext));
                additionalAttacker = blackMoreAttackers.get(bNext);
                while (bNext < blackMoreAttackers.size()
                        && additionalAttacker.getRawMinDistanceFromPiece().dist()<2) {
                    // skip this attacker, it must be a non2nd-row attacker with D==1 and a condition, so it cannot be benefited to come closer really...)
                    bNext++;
                    if ( bNext >= blackMoreAttackers.size() ) {
                        // it was the last one, we should not even have entered this loop for white
                        turn = !turn;
                        continue loop;
                    }
                    additionalAttacker = blackMoreAttackers.get(bNext);
                }
                blacks.add(additionalAttacker);
                blacks.sort(VirtualPieceOnSquare::compareTo);
                bNext++;
            }
            // main calculation
            futureClashResults[nr] = calcClashResultExcludingOne(
                    initialTurn, currentVPceOnSquare,
                    whites, blacks,
                    null,
                    null, null, null);
                   // whiteMoreAttackers, blackMoreAttackers, null);

            // add new chances
            int benefit = 0;
            ConditionalDistance rmd = additionalAttacker.getRawMinDistanceFromPiece();
            /*int futureLevel = additionalAttacker.getStdFutureLevel() -2
                    - ((currentVPceOnSquare.color() == additionalAttacker.color() ) //&& additionalAttacker.color()==board.getTurnCol()
                    ? 1 : 0)  // covering happens 1 step faster than beating if, it is my turn  / Todo: do we want dependency on who's turn it is here?
                    + (rmd.isUnconditional() ? 0 : 1)  // TODO:Shouldn't this be nrOfConditions()?
                    ; */
            int futureLevel = additionalAttacker.getAttackingFutureLevelPlusOne() -1
                    - (currentVPceOnSquare.color() == additionalAttacker.color()
                            && additionalAttacker.color()==board.getTurnCol()  // results are much worse without this line!
                        ? 1 : 0)  // covering happens 1 step faster than beating if, it is my turn  / Todo: do we want dependency on who's turn it is here?
                    //+ (rmd.isUnconditional() ? 0 : 1)  // TODO:Shouldn't this be nrOfConditions()?
                    + (multipleAdditions>1? multipleAdditions-1 : 0) // this benefit can only be achieved after the previous additional attackers have also come into play
                    ; //+ ( isWhite(additionalAttacker.color()) ? wNext-1 : bNext-1 ); // if several attackers need to be brought in, obey the order
            int clashContribution = futureClashResults[nr] - clashEval();
            /*if (nr>1 && (isWhite(additionalAttacker.color()) && futureClashResults[nr] > futureClashResults[nr-2]
                         || (isBlack(additionalAttacker.color()) && futureClashResults[nr] < futureClashResults[nr-2]) )
            ) {
                debugPrintln(DEBUGMSG_MOVEEVAL, "(reducing benefit as it was already " + futureClashResults[nr-2] + "before.");
                clashContribution += futureClashResults[nr - 2]-futureClashResults[nr]; // account only the additional benefit compared to the last step of the same color (Todo: takes wrong res[-2] if there was no opponent color piece left
            }*/
            if (nr>1
                    && abs(futureClashResults[nr] - futureClashResults[nr-1])<(EVAL_TENTH>>1)
                    && evalIsOkForColByMin( futureClashResults[nr-1] - clashEval(), prevAddAttacker.color(), -EVAL_DELTAS_I_CARE_ABOUT)
            ) {
                // nothing changed despite this new attacker(=defender here), but the previous attacker had a nice benefit..., so this new attacker is powerless...
                // so even more benefit to previous attacker
                int moreBenefit = isWhite(prevAddAttacker.color()) ? (EVAL_TENTH<<1) : (-EVAL_TENTH<<1);
                if (!myPiece().canMoveAwayPositively())
                    moreBenefit += moreBenefit>>1;
                if (!myPiece().canMove())
                    moreBenefit += moreBenefit>>1;
                if (!prevAddAttacker.getRawMinDistanceFromPiece().isUnconditional())
                    moreBenefit >>= 1;
                debugPrintln(DEBUGMSG_MOVEEVAL, "(Alert for " + colorName(additionalAttacker.color())
                        + ": cannot save " + squareName(myPos) + " after additional attack of "+ prevAddAttacker+", so more benefit "+moreBenefit+"@"+prevFutureLevel+" for the latter.) ");
                prevAddAttacker.addChance(moreBenefit,prevFutureLevel );
            }

            if ( isKing(additionalAttacker.getPieceType() ) ) {
                benefit = calcKingAttacksBenefit(additionalAttacker);
            }
            else if (evalIsOkForColByMin( clashContribution,
                    additionalAttacker.color(), -EVAL_DELTAS_I_CARE_ABOUT)) {
                benefit = clashContribution;
                if ( abs(benefit)>EVAL_DELTAS_I_CARE_ABOUT
                        && myPiece().color() != additionalAttacker.color()
                        && myPiece().canMoveAwayPositively()
                        && myPiece().getBestMoveTarget() != additionalAttacker.getMyPiecePos() // if the best moves is to where the attacker comes from, then we actually do not know if it has another good move, lets assume not and attack anyway...

                ) {
                    debugPrintln(DEBUGMSG_MOVEEVAL, "(hmm, reducing benefit for trying to additionally attack piece " + myPiece() + " at " + squareName(myPos)
                            + " with benefit " + benefit + " by " + additionalAttacker + " although, it has a good move (" + myPiece().getBestMoveRelEval()
                            +  ") ");
                    benefit -= (benefit >>3);  //2) + (benefit >> 3);  // *0,87
                    // -> makes not much difference, becomes even slightly worse the more one subtracts here... but not really anymore after the skipping of conditioned abave was introduced
                } else
                    debugPrint(DEBUGMSG_MOVEEVAL, " (bonus for additionally attacking piece " + myPiece() + " at " + squareName(myPos)
                            + " with benefit " + benefit + " by " + additionalAttacker + ") ");

                if (myPiece().color() == additionalAttacker.color()) {
                    if (!additionalAttacker.canCoverFromSavePlace()) {
                        benefit >>= 2;
                        debugPrint(DEBUGMSG_MOVEEVAL,"(reducing covering benefit, as there is no save square for covering:) ");
                    }
                }
                if (DEBUGMSG_MOVEEVAL && abs(benefit)>4)
                     debugPrintln(DEBUGMSG_MOVEEVAL," Benefit " + benefit + " for close future chances on square "
                             + squareName(myPos)+" with " + additionalAttacker + ": " + futureClashResults[nr] + "-" + clashEval());

                /* seems logical, but does not improve, but worsens the eval in all SF11+14+selfv26 test games
                if (multipleAdditions>1 && preparer[colorIndex(turn)].size()==0) {
                    // here the additional attacker is a multiple one at the end after the opponent ran out of defenders
                    // but also, the preparer list is empty meaning there was already a preparer + one after that awarded the preparer
                    // so let's keep it down a little her with giving the same bonus again...
                    benefit -= benefit>>2;  // *0.75
                } */
                for ( VirtualPieceOnSquare preparerVPce : preparer[colorIndex(turn)] ) {
                    int preparerBenefit = benefit;
                    if (preparerVPce.getRawMinDistanceFromPiece().hasNoGo())
                        preparerBenefit >>= 3;
                    benefit >>= 1; // we are starting with the first/cheaper ones, so this brings less and less benefit to the later ones...
                    if (DEBUGMSG_MOVEEVAL && abs(preparerBenefit) > 4)
                        debugPrintln(DEBUGMSG_MOVEEVAL, ", but actually give benefit " + benefit + "@" + futureLevel + " for other piece that should go first towards  "
                                + squareName(myPos) + ": " + preparerVPce + ".");
                    preparerVPce.addChance(preparerBenefit, futureLevel);
                }
                preparer[colorIndex(turn)].clear();

            }
            else {
                // no direct positive result on the clash but let's check the following:
                if ( isKing(additionalAttacker.getPieceType() ) ) {
                    benefit = calcKingAttacksBenefit(additionalAttacker);
                }
                else if (countDirectAttacksWithColor(additionalAttacker.color())==0
                        || countDirectAttacksWithColor(additionalAttacker.color()) <= countDirectAttacksWithColor(opponentColor(additionalAttacker.color()))) {
                    if (additionalAttacker.color() != currentVPceOnSquare.color()) {
                        // still a little attacking chance improvement if a piece comes closer to an enemy, right?
                        benefit = ( ((myPiece().isWhite() ? -EVAL_TENTH : EVAL_TENTH)<<1)
                                   - myPiece().getValue()) >> 4;
                        if ( myPiece().canMoveAwayPositively()
                                && myPiece().getBestMoveTarget() != additionalAttacker.getMyPiecePos() // if the best moves is to where the attacker comes from, then we actually do not know if it has another good move, lets assume not and attack anyway...

                        ) {
                            debugPrintln(DEBUGMSG_MOVEEVAL, "(hmmm, reducing benefit for trying to additionally come closer to in sometime attack piece " + myPiece() + " at " + squareName(myPos)
                                    + " with benefit " + benefit + " by " + additionalAttacker + " although, it has a good move (" + myPiece().getBestMoveRelEval()
                                    +  ") ");
                            benefit -= benefit >> 2;  // *0,75
                        }
                    }
                    else { // i.e. if (additionalAttacker.color() == currentVPceOnSquare.color()) {
                        // still a little defending chance improvement if a piece comes closer to cover one own piece once more, right?
                       if ( abs(clashEval()) > EVAL_HALFAPAWN
                            && abs(futureClashResults[nr]) < EVAL_HALFAPAWN
                       )
                            benefit = -(clashEval() - (clashEval()>>2));
                       else
                            benefit = (((additionalAttacker.myPiece().isWhite() ? EVAL_TENTH : -EVAL_TENTH) << 2)
                                + additionalAttacker.myPiece().reverseBaseEval()) >> 5;
                    }

                    if (myPiece().color() == additionalAttacker.color()) {
                        if (!additionalAttacker.canCoverFromSavePlace()) {
                            benefit >>= 2;
                            debugPrint(DEBUGMSG_MOVEEVAL,"(reducing additional covering benefit, as there is no save place for covering:) ");
                        }
                    }
                }
                preparer[colorIndex(turn)].add(additionalAttacker); // keep it for later, it could be a preparer for a later chance
            }
            /*if (isKing(additionalAttacker.getPieceType()))
                benefit >>= 1;  // /2 for kings */
            // anyway calculated further down: benefit += getKingAreaBenefit(additionalAttacker)>>1;
            //TODO: +countHelpNeededFromColorExceptOnPos is incorrect if some firstMovesToHere hava more conditions than others.

            if (additionalAttacker.getRawMinDistanceFromPiece().hasNoGo())
                benefit >>= 3;
            if ( DEBUGMSG_MOVEEVAL && abs(benefit)>4)
                debugPrintln(DEBUGMSG_MOVEEVAL," Final benefit of " + benefit + "@"+futureLevel+" for close future chances on square "+ squareName(myPos)+" with " + additionalAttacker + ".");

            if (benefit!=0)
                additionalAttacker.addChance(benefit,futureLevel);

            prevAddAttacker = additionalAttacker;
            prevFutureLevel = futureLevel;

            debugPrint(DEBUGMSG_FUTURE_CLASHES, " => " + futureClashResults[nr]);
            nr++;
            // switch sides (or not)
            if (isWhite(turn) && bNext < blackMoreAttackers.size()
                    || !isWhite(turn) && wNext < whiteMoreAttackers.size()) {
                turn = !turn;   // if opponent still has pieces left, we switch sides...
            } else {
                // if not then result stays the same and same side can even bring in more pieces
                futureClashResults[nr] = futureClashResults[nr - 1];
                nr++;
                multipleAdditions++;
            }
        }
        //debugPrintln(true, " END " + squareName(myPos)+ ".");

        // add additional future chances
        int hopDistance = 3;  // 0-2 has already been considered in calculation above
        boolean attackerColor = opponentColor(currentVPceOnSquare.color());
        while (hopDistance < coverageOfColorPerHops.size()) {
            final List<VirtualPieceOnSquare> moreAttackers = isWhite(attackerColor)
                    ? coverageOfColorPerHops.get(hopDistance).get(CIWHITE)
                    : coverageOfColorPerHops.get(hopDistance).get(CIBLACK);
            for (VirtualPieceOnSquare additionalFutureAttacker : moreAttackers) {
                // still a little attacking chance improvement if a piece comes closer to an enemy, right?
                int futureLevel = additionalFutureAttacker.getAttackingFutureLevelPlusOne() - 1
                        - (currentVPceOnSquare.color() == additionalFutureAttacker.color() && additionalFutureAttacker.color() == board.getTurnCol()
                        ? 1 : 0)  // covering happens 1 step faster than beating if, it is my turn  / Todo: do we want dependency on who's turn it is here?
                        ; //+ (rmd.isUnconditional() ? 0 : 1);
                int benefit;
                if ( isKing(additionalFutureAttacker.getPieceType() ) ) {
                    benefit = calcKingAttacksBenefit(additionalFutureAttacker)>>(futureLevel>1?futureLevel-1:1);
                }
                else {
                    benefit //= -myPiece().getValue() >> hopDistance;
                            = (((additionalFutureAttacker.myPiece().isWhite() ? EVAL_TENTH : -EVAL_TENTH) << 3)
                            + additionalFutureAttacker.myPiece().reverseBaseEval()) >> 5;
                    if (countDirectAttacksWithColor(additionalFutureAttacker.color()) > 0
                            && countDirectAttacksWithColor(additionalFutureAttacker.color())
                            > countDirectAttacksWithColor(opponentColor(additionalFutureAttacker.color())))
                        benefit >>= 1;
                }
                //benefit += getKingAreaBenefit(additionalFutureAttacker)>>1;
                if (additionalFutureAttacker.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                    benefit >>= 3;
                if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit of " + benefit + "@" + futureLevel + " for later future chances on square " + squareName(myPos) + " with " + additionalFutureAttacker + ".");
                if (benefit!=0)
                    additionalFutureAttacker.addChance(benefit, futureLevel);
                if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, ".");
            }
            hopDistance++;
        }
    }

    private int calcKingAttacksBenefit(VirtualPieceOnSquare additionalAttacker) {
        int benefit;
        //if (DEBUGMSG_MOVEEVAL) debugPrintln(DEBUGMSG_MOVEEVAL, "Analysing " + additionalAttacker.myPiece() + " to " + squareName(myPos) + ".");
        // special treatment of kings
        boolean acol = additionalAttacker.color();
        if ( ( countDirectAttacksWithColor(acol)
                == countDirectAttacksWithColor(opponentColor(acol)) )
                || ( board.getTurnCol() == acol
                && countDirectAttacksWithColor(acol) == countDirectAttacksWithColor(opponentColor(acol))-1 )
                && !additionalAttacker.getRawMinDistanceFromPiece().hasNoGo()
                && !isKing(myPiece().getPieceType())
                && !isQueen(myPiece().getPieceType())
        ) {
            benefit = myPiece().getValue() >> 4;
            if ( acol != myPiece().color() )
                benefit = -benefit;  // it is an attack not a defense
            if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                debugPrintln(DEBUGMSG_MOVEEVAL, " King is helping out on " + squareName(myPos) + ".");
        }
        else
            benefit = 0;
        return benefit;
    }

    void calcExtraBenefits() {
        //// extra benefits here for Pieces that could ge here (soon)

        // calc benefit for controlling extra squares
        int[] ableToTakeControlBonus = {0, 0};  // indicating nobody can take control
        for (int ci=0; ci<=1; ci++) {
            int oci = opponentColorIndex(ci);
            if (countDirectAttacksWithColor(colorFromColorIndex(oci)) == 0) {
                ableToTakeControlBonus[ci] = EVAL_TENTH>>1; // because we do not yet cover this square at all
            }
            else if ( countDirectAttacksWithColor(colorFromColorIndex(ci))
                      < countDirectAttacksWithColor(colorFromColorIndex(oci))
                    && board.distanceToKing(getMyPos(), colorFromColorIndex(ci))==1) {
                // defend square next to my king
                ableToTakeControlBonus[ci] = pieceBaseValue(PAWN)<<1;
            }
            else if ( ( (clashEval()<=-EVAL_DELTAS_I_CARE_ABOUT && ChessBasics.isWhite(ci) )
                            || (clashEval()>=EVAL_DELTAS_I_CARE_ABOUT && !ChessBasics.isWhite(ci) ) ) ) {
                // todo: better would be to calculate per soecific vPce if clash really improves
                // strengthen necessary defence
                ableToTakeControlBonus[ci] = EVAL_TENTH; // because we then cover it more often - which does not say too much however...
            }
            else if ( countDirectAttacksWithColor(colorFromColorIndex(ci))
                       == countDirectAttacksWithColor(colorFromColorIndex(oci))
                    && abs(clashEval())<EVAL_DELTAS_I_CARE_ABOUT ) {
                // strengthen not yet necessary defence
                ableToTakeControlBonus[ci] = EVAL_TENTH>>1; // because we then cover it more often - which does not say too much however...
            }
        }
        ableToTakeControlBonus[CIBLACK] = -ableToTakeControlBonus[CIBLACK];

        for (VirtualPieceOnSquare vPce : vPieces ) {
            if (vPce == null
                    || !vPce.getRawMinDistanceFromPiece().distIsNormal()
                    || vPce.getRawMinDistanceFromPiece().dist()>=MAX_INTERESTING_NROF_HOPS )
                continue;
            int inFutureLevel = vPce.getAttackingFutureLevelPlusOne();
            ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();

            // benefit for coming closer to control extra squares
            if ((!isKing(vPce.getPieceType())
                    || board.getPieceCounterForColor(vPce.color()) < 7)
                    && abs(ableToTakeControlBonus[colorIndex(vPce.color())]) > 0
                    && rmd.dist() > 1) {
                int conquerSquBenefit = (((ableToTakeControlBonus[colorIndex(vPce.color())]
                        * vPce.myPiece().reverseBaseEval()) >> 8)
                        + ableToTakeControlBonus[colorIndex(vPce.color())]);
                if (rmd.dist() <= 3)  // more benefit for dist = 2 or 3 - hope it first brings more "friends" towards the square
                    conquerSquBenefit -= conquerSquBenefit >> 2; // * 0,75
                if (vPce.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                    conquerSquBenefit >>= 3;
                if (isKing(vPce.getPieceType()))
                    conquerSquBenefit >>= 1;
                int fl = inFutureLevel > 0 ? inFutureLevel - 1 : 0;
                if (DEBUGMSG_MOVEEVAL && abs(conquerSquBenefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " " + conquerSquBenefit + "@" + fl + " Benefit for conquering square " + squareName(myPos) + " with " + vPce + ".");
                vPce.addChance(conquerSquBenefit, fl);
            }

            // moving king towards pawns in endgames
            if ((!isSquareEmpty()
                    && isKing(vPce.getPieceType())
                    && board.getPieceCounterForColor(vPce.color()) < 8)  // Todo: take existence of queen as end game indicator
                    && isPawn(myPiece().getPieceType())
            ) {
                int protectPawnBenefit;
                if (vPce.color() == myPiece().color())    // own pawn
                    protectPawnBenefit = EVAL_TENTH;  // 10
                else
                    protectPawnBenefit = EVAL_TENTH << 1;  // 20
                if (vPce.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                    protectPawnBenefit >>= 3;
                int nr = inFutureLevel - 1; // ( (myPiece()!=null && vPce.color()==myPiece().color()) ? 1 : 0);  // covering is one faster then attacking+beating
                if (nr < 0)
                    nr = 0;
                if (isBlack(vPce.color()))
                    protectPawnBenefit = -protectPawnBenefit;
                if (DEBUGMSG_MOVEEVAL && abs(protectPawnBenefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " " + protectPawnBenefit + "@" + nr + " Benefit for king approaching pawn on square " + squareName(myPos) + " with " + vPce + ".");
                vPce.addChance(protectPawnBenefit, nr);
            }

            // pawns try to get to promoting rank
            if (isPawn(vPce.getPieceType()) )
                calcPawnsExtraBenefits(vPce, inFutureLevel);

            // check king Attacks/Defence
            addKingCheckReleatedBenefits(vPce, inFutureLevel);

            // King Area Attacks/Defence
            int kingAreaBenefit = getKingAreaBenefit(vPce);
            if (abs(kingAreaBenefit) > (EVAL_TENTH >> 1)) {
                int nr = inFutureLevel - 1;
                if (nr < 0)
                    nr = 0;
                if (DEBUGMSG_MOVEEVAL && abs(kingAreaBenefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " Sum of benefits around king on " + squareName(myPos) + " is: " + kingAreaBenefit + "@" + nr + ".");
                vPce.addChance(kingAreaBenefit, nr);
            }

            // moves/evals activated indirectly by moving away
            // TODO: check if this should be replaced by a new general distributing moving away chances
            if (!isKing(myPieceType())
                    && rmd.dist() >= 1
                    && !rmd.isUnconditional()
                    && vPce.hasRelEval()) {
                int benefit = vPce.getRelEval();
                benefit -= benefit>>3;  // *0.87
                for (Integer fromCond : rmd.getFromConds()) {
                    if (fromCond != -1 && benefit != NOT_EVALUATED
                            && evalIsOkForColByMin(benefit, vPce.color(), -EVAL_TENTH)
                            && colorlessPieceType(board.getPieceTypeAt(fromCond)) != colorlessPieceType(vPce.getPieceType())   // not my same type, because I'd anyway X-ray through
                    ) {
                        int nr = inFutureLevel - 1 - ((myPiece() != null && vPce.color() == myPiece().color()) ? 1 : 0);  // covering is one faster then attacking+beating
                        if (nr < 0)
                            nr = 0;  // TODO!: Debug, why this can happen
                        if (vPce.getMinDistanceFromPiece().hasNoGo())
                            benefit >>= 3;
                        if (isKing(vPce.getPieceType()))
                            benefit >>= 1;
                        if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " " + benefit + "@" + nr + " Benefit helping pieces freeing way of " + vPce + " to " + squareName(myPos) + ".");
                        // TODO: Take into account that moving away piece could influence the benefit, as the getRelEval could rely on the 2Bmoved piece to take part in the clash
                        vPce.addChances2PieceThatNeedsToMove(
                                benefit,
                                nr,  // -2 because dist 1 is already a direct threat and -1 because one help is already fulfilled by moving away
                                fromCond);
                    }
                }
            }


            // avoid directly moving pieces on squares where a king-pin is likely
            /*
            8 ░░░   ░░░ k3░░░   ░░░      k: king,  3: pinnerAtKingPos
            7    ░░░   ░░░   ░░░   ░░░
            6 ░░░   ░░░   ░░░   ░░░
            5    ░░░   ░░░   ░░░   ░░░   V: v.myPiece
            4 ░░░ V ░░░ v2░░░   ░░░      v: myPos = this vPce, possible pin-victim if its Piece V moves here, dist==1, unCond.
            3    ░░░   ░░░   ░░░   ░░░   2: pinner vPce here: opposite color than k, dist==2, no NoGo
            2 ░░░   ░░░  1░░░ P ░░░      P: pinner.myPiece - opposite color than k+v
            1    ░░░   ░░░   ░░░   ░░░   1: checkMove.to()
            A  B  C  D  E  F  G  H    */
            if (rmd.dist() == 1 && rmd.isUnconditional()
                    && !isKing(vPce.getPieceType())
                // && positivePieceBaseValue(vPce.getPieceType() ) >= positivePieceBaseValue(PAWN)+EVAL_TENTH
            ) {
                for (VirtualPieceOnSquare pinner : vPieces)
                    if (pinner != null && pinner.color() != vPce.color()
                            && isSlidingPieceType(pinner.getPieceType())
                            && positivePieceBaseValue(pinner.getPieceType()) != positivePieceBaseValue(vPce.getPieceType())
                            && pinner.getRawMinDistanceFromPiece().dist() == 2  //TODO!: make it generic for all futer levels
                            && !pinner.getRawMinDistanceFromPiece().hasNoGo()
                    ) {
                        ConditionalDistance pinnerRmd = pinner.getRawMinDistanceFromPiece();
                        inFutureLevel = pinner.getAttackingFutureLevelPlusOne() - 2
                                + (pinnerRmd.isUnconditional() ? 0 : 1);
                        if (inFutureLevel < 0)
                            inFutureLevel = 0;
                        int kingPos = board.getKingPos(vPce.color());
                        if (kingPos < 0)
                            continue;  // can happen in test cases
                        VirtualPieceOnSquare pinnerAtKingPos = board.getBoardSquares()[kingPos].getvPiece(pinner.getPieceID());
                        ConditionalDistance pinner2kingRmd = pinnerAtKingPos.getRawMinDistanceFromPiece();
                        if (pinner2kingRmd.dist() != 2 || !pinner2kingRmd.isUnconditional())
                            continue;  // not able to give check in 1 move
                        for (Move checkMove : pinnerAtKingPos.getFirstMovesWithReasonableShortestWayToHere()) {
                            if (isBetweenFromAndTo(myPos, checkMove.to(), kingPos)) {
                                //TODO!: if pinner on checkMove.to() will be uncovered, but vPce covers it with its move, then there is no danger
                                int danger = (abs(vPce.getValue()) - abs((pinner.getValue() >> 1))) >> 1;
                                if (danger < EVAL_TENTH)
                                    danger = EVAL_TENTH;  // happens if pinner is much more valuable than pinned pce
                                if (isBlack(vPce.color()))
                                    danger = -danger;
                                if (DEBUGMSG_MOVEEVAL && abs(danger) > 4)
                                    debugPrintln(DEBUGMSG_MOVEEVAL, " Avoiding king-pin " + (-danger) + "@" + inFutureLevel
                                            + " for " + vPce + " to " + squareName(myPos) + " by " + pinner + ".");
                                vPce.addChance(-danger, inFutureLevel );  // warn vPce not to go there
                                if (pinnerRmd.dist() > 2 && abs(danger) > 2) {
                                    if (DEBUGMSG_MOVEEVAL && abs(danger) > 4)
                                        debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit for coming closer to possible kin-pin "
                                                + (-(danger >> 1)) + "@" + inFutureLevel + " for " + (pinner.getRawMinDistanceFromPiece().oneLastMoveOrigin())
                                                + " via " + squareName(myPos) + ".");
                                    board.getBoardSquare(checkMove.to())
                                            .getvPiece(pinner.getPieceID())
                                            .addChance(-(danger >> 1), inFutureLevel, getMyPos() ); // award possible pinner to come closer
                                }
                            }
                        }
                    }
            }

            // avoid moving to square where another one gets pinned to me
            // and maybe encourage the other one to move away a little?
            if (rmd.dist() < 3  // consider only for low range, doable moves
                    && !vPce.getMinDistanceFromPiece().hasNoGo()
                    && (board.getPieceAt(myPos) == null || board.getPieceAt(myPos).color() != vPce.color())  // place is free to go there
            ) {
                int fl = inFutureLevel > 0 ? inFutureLevel - 1 : 0;
                VirtualPieceOnSquare pinnedVPce = null;
                VirtualPieceOnSquare pinnerVPce = null;
                // find possible pinned piece, if I move there
                for (VirtualPieceOnSquare vp : getVPieces())
                    if (vp != null && isSlidingPieceType(vp.getPieceType())
                            && vp.color() != vPce.color()
                            && vp.getRawMinDistanceFromPiece().dist() == 1
                            && !vp.getRawMinDistanceFromPiece().hasNoGo()
                            && !vp.getRawMinDistanceFromPiece().isUnconditional()
                    ) {
                        // if opponent vp is attacking here almost directly but with a condition
                        List<Integer> fromConds = vp.getRawMinDistanceFromPiece().getFromConds();
                        if (fromConds.size() == 1)
                            continue; // no from Condition
                        int pinnedPos = fromConds.get(0);
                        ChessPiece pinnedPiece = board.getPieceAt(pinnedPos);
                        if (pinnedPiece == null)
                            continue; // should not happen, but to be sure
                        VirtualPieceOnSquare alsoPinnedVPce = board.getBoardSquares()[pinnedPos].getvPiece(pinnedPiece.getPieceID());
                        if (pinnedVPce == null
                                || abs(alsoPinnedVPce.getValue()) > abs(pinnedVPce.getValue())) {
                            pinnedVPce = alsoPinnedVPce;  // find the most worthy pinned piece
                            pinnerVPce = vp;
                        }
                    }
                if (pinnedVPce != null) {
                    int benefit = (min(abs(vPce.getValue()), abs(pinnedVPce.getValue())) >> 1);
                    if (rmd.dist() == 2)
                        benefit >>= 2;
                    if (!isWhite(vPce.color()))
                        benefit = -benefit;
                    if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " " + benefit + "@" + fl
                                + " anti-benefit for going to pin-dangerous square " + squareName(myPos)
                                + " with " + vPce
                                + " possibly pinned by " + pinnerVPce.myPiece() + " on " + squareName(pinnerVPce.myPiece().getPos())
                                + " pinning " + pinnedVPce + ".");
                    vPce.addChance(-benefit, fl);
                    pinnedVPce.myPiece().addMoveAwayChance2AllMovesUnlessToBetween(-benefit>>4, fl,
                            pinnedVPce.myPos, getMyPos(), false ); // was: .addChance(-benefit, fl, ); but did this do anything?
                    pinnerVPce.addChance(benefit, fl + 1 );
                }
            }

        }

        if (!isSquareEmpty()) {
            // benefit to give king "air"
            int kingNeedsAirBenefit = getKingNeedsAirBenefit();
            if (abs(kingNeedsAirBenefit) > 0) {
                int nr = 0;
                if (DEBUGMSG_MOVEEVAL && abs(kingNeedsAirBenefit)>4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " Benefits of giving air to king at " + squareName(myPos) + " is: " + kingNeedsAirBenefit + "@" + nr + ".");
                myPiece().addMoveAwayChance2AllMovesUnlessToBetween(
                        kingNeedsAirBenefit,
                        nr,
                        -1, -1, false );
            }
        }
    }

    private void calcPawnsExtraBenefits(final VirtualPieceOnSquare vPce, final int inFutureLevel) {
        ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();

        // promotion benefits + counter measures
        if ( ((isFirstRank(getMyPos()) && isBlack(vPce.color()))
            || (isLastRank(getMyPos()) && isWhite(vPce.color())))) {
            int promoBenefit = rmd.needsHelpFrom(vPce.myOpponentsColor())
                    ? (pieceBaseValue(PAWN)) - EVAL_TENTH
                    : pieceBaseValue(QUEEN) - pieceBaseValue(PAWN);
            if (vPce.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                promoBenefit = (pieceBaseValue(PAWN) + EVAL_TENTH) >> 2;
            if (isBlack(vPce.color()))
                promoBenefit = -promoBenefit;
            if (DEBUGMSG_MOVEEVAL && abs(promoBenefit) > 4)
                debugPrintln(DEBUGMSG_MOVEEVAL, " " + promoBenefit + "@" + inFutureLevel + " Benefit for pawn " + vPce + " for moving towards promotion on " + squareName(myPos) + ".");
            vPce.addChance(promoBenefit, inFutureLevel, isFirstRank(getMyPos()) ? FIRST_RANK_CBM : LAST_RANK_CBM);

            // run to protect promotion square, "if just in reach"
            if (abs(promoBenefit) > EVAL_TENTH) {
                int pawnDist = vPce.getRawMinDistanceFromPiece().dist();
                if (vPce.color() == board.getTurnCol())
                    pawnDist--;
                VirtualPieceOnSquare closestDefender = null;
                for (VirtualPieceOnSquare defender : vPieces)
                    if (defender != null && defender.color() != vPce.color()) {
                        int defenderDist = defender.getRawMinDistanceFromPiece().dist() - 1;
                        if (defenderDist <= pawnDist) {
                            if (closestDefender == null || defenderDist < (closestDefender.getRawMinDistanceFromPiece().dist() - 1))
                                closestDefender = defender;  // remember closest defender that is close enough to defend.
                        }
                    }
                if (closestDefender != null) { // we have a defender
                    int defendBenefit = -(promoBenefit >> 1);  // /2
                    int defenderDist = closestDefender.getRawMinDistanceFromPiece().dist() - 1;
                    int inFutureLevelDefend = (pawnDist - defenderDist > 0) ? (pawnDist - defenderDist) : 0;
                    if (DEBUGMSG_MOVEEVAL && abs(defendBenefit) > 4)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " +/- " + defendBenefit + "@" + inFutureLevelDefend + " Benefit for keeping pawn " + vPce + " from moving towards promotion on " + squareName(myPos) + ".");
                    closestDefender.addChance(defendBenefit, inFutureLevelDefend, isFirstRank(getMyPos()) ? FIRST_RANK_CBM : LAST_RANK_CBM);
                    if (defenderDist == 0) // already covering -> do not move away!
                        closestDefender.addClashContrib(defendBenefit);
                }
                // give the same benefit to those who can just take the pawn
                if ((pawnDist == 3 && closestDefender == null)
                        || (pawnDist < 3)
                ) {
                    Square pawnSq = board.getBoardSquare(vPce.myPiece().getPos());
                    pawnSq.addImmediateTakeBenefitFor(vPce,
                            -promoBenefit / (1 + promotionDistanceForColor(pawnSq.getMyPos(), vPce.color())));
                }
                //}
            }
        }

        if ( rmd.dist() == 1 ) {
            boolean isBeating =  abs(vPce.myPos - vPce.getMyPiecePos()) == 1;
            // motivating pawns to move forward, esp in endgames
            final int nrOfPiece = board.getPieceCounter();
            if (nrOfPiece < 21 && !rmd.hasNoGo()) {
                int forwardBenefit = (24 - nrOfPiece) >> 2;
                if (isBlack(vPce.color()))
                    forwardBenefit = -forwardBenefit;
                if (DEBUGMSG_MOVEEVAL && abs(forwardBenefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " " + forwardBenefit + "@0 benefit for " + (isBeating ? "beating with" : "advancing") + " pawn to " + squareName(myPos) + ".");
                vPce.addChance(forwardBenefit, 0);
            }

            // avoid doubling pawns (when beating)
            if ( isBeating && board.getPawnCounterForColorInFileOfPos(vPce.color(), vPce.myPos ) > 0 )  {
                int doublePawnFee = EVAL_TENTH - (EVAL_TENTH >> 2);
                if (isWhite(vPce.color()))
                    doublePawnFee = -doublePawnFee;
                if (DEBUGMSG_MOVEEVAL && abs(doublePawnFee) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " " + doublePawnFee + "@0 fee for doubeling pawn at " + squareName(myPos) + ".");
                vPce.addChance( doublePawnFee, 0);
            }
        }
    }

    void calcContributionBlocking() {
        for (VirtualPieceOnSquare vPce : vPieces ) {
            if (vPce == null)
                continue;
            int inFutureLevel = vPce.getAttackingFutureLevelPlusOne();
            if (inFutureLevel > MAX_INTERESTING_NROF_HOPS)
                continue;
            ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();

            // avoid moving to square where I block my own clash contribution
            // and maybe encourage the other one to move away a little?
            if (rmd.dist() == 1  // consider only for low range, doable moves
                    && !rmd.hasNoGo()
                    && board.isSquareEmpty(getMyPos())  // square was empty so far
            ) {
                int nr = inFutureLevel > 0 ? inFutureLevel - 1 : 0;
                for (VirtualPieceOnSquare contributor : getVPieces()) {
                    if (contributor == null
                            || !isSlidingPieceType(contributor.getPieceType())
                            || contributor.getRawMinDistanceFromPiece().dist() != 1
                            || !contributor.getRawMinDistanceFromPiece().isUnconditional()
                    )
                        continue;
                    int contribToPos = contributor.myPiece().getTargetOfContribSlidingOverPos(getMyPos());
                    if (contribToPos==NOWHERE)
                        continue;
                    int contrib = board.getBoardSquare(contribToPos).getvPiece(contributor.getPieceID()).getClashContribOrZero();
                    VirtualPieceOnSquare myVPceAtContribTarget = board.getBoardSquare(contribToPos).getvPiece(vPce.getPieceID());

                    if (evalIsOkForColByMin(contrib, contributor.color(), -EVAL_DELTAS_I_CARE_ABOUT)
                        && myVPceAtContribTarget.getRawMinDistanceFromPiece().dist()!=2  // if =02, then we are not really blocking, just putting it into 2nd row -
                                                                                         // TODO:check, as evaluation got a even a little worse by this
                    ) {
                        // here it is too late to add to the vPces
                        // vPce.addChance(-contrib, 0);
                        // vPce.addChance(contrib, 1); // its not gone, but postponed...
                        //we need to add it to the Pieces moves instead
                        if (vPce.color() == contributor.color()) {
                            // blocking my own piece
                            vPce.myPiece().addMoveWithChance(new Move(vPce.getMyPiecePos(),getMyPos()),
                                    0, -contrib>>1);
                            vPce.myPiece().addMoveWithChance(new Move(vPce.getMyPiecePos(),getMyPos()),
                                    1, contrib>>2);
                        } /*else {
                            // blocking opponent piece
                            vPce.myPiece().addMoveWithChance(new Move(vPce.getMyPiecePos(),getMyPos()),
                                    0, -contrib>>2);
                            vPce.myPiece().addMoveWithChance(new Move(vPce.getMyPiecePos(),getMyPos()),
                                    1, contrib>>3);
                        } */
                    }
                }
            }
        }
    }

    private void addImmediateTakeBenefitFor(VirtualPieceOnSquare takenVPce, final int benefit) {
        int takenPieceID = takenVPce.getPieceID();
        for (VirtualPieceOnSquare vPce : vPieces) {
            if (vPce!=null
                    && vPce.getPieceID() != takenPieceID
                    && takenVPce.color() != vPce.color()
            ) {
                ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();
                if (rmd.dist() != 1 || !rmd.isUnconditional() )
                    continue;
                // iterate over all opponents that can directly beat here
                int takeBenefit = (benefit + vPce.getRelEvalOrZero())>>1;  // /2
                if (evalIsOkForColByMin(takeBenefit, vPce.color())) {
                    if (DEBUGMSG_MOVEEVAL && abs(takeBenefit)>4)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " Benefits of taking pawn with promotion chances at " + squareName(myPos) + " is: " + takeBenefit + "@0.");
                    vPce.addChance(takeBenefit, 0, getMyPos() );
                }
            }
        }
    }

    private int getKingNeedsAirBenefit() {
        int benefit = 0;
        if (isSquareEmpty()) {
            return 0;
        }
        boolean kcol;
        if (board.distanceToKing(myPos, WHITE)==1
                && myPiece().color()==WHITE )
            kcol = WHITE;
        else if (board.distanceToKing(myPos, BLACK)==1
                && myPiece().color()==BLACK )
            kcol = BLACK;
        else
            return 0;
        int kingPos = board.getKingPos(kcol);
        // Todo: counting checkable pieces is imprecise: checker might need to give up square control for checking and might have Nogo
        int checkablePieces = board.getBoardSquares()[kingPos].countFutureAttacksWithColor(opponentColor(kcol), 2);
        if ( board.nrOfLegalMovesForPieceOnPos(kingPos) == 0
                && countDirectAttacksWithColor(opponentColor(kcol)) == 0
                && (checkablePieces>0
                    || board.getBoardSquares()[kingPos].countFutureAttacksWithColor(opponentColor(kcol), 3)>0 )
        ) { // king can be checked soon
            if (checkablePieces>0)
                benefit = EVAL_HALFAPAWN;  // 50 not so big benefit, as we cannot be sure here if it is mate... Todo: more thorough test
            else
                benefit = EVAL_TENTH;
            if (isBlack(kcol))
                benefit = -benefit;
        }
        return benefit;
    }


    // called on square with king
    public void calcCheckBlockingOptions() {
        if (myPiece()==null)
            return; // should not happen
        boolean kcol = myPiece().color();
        int nrofkingmoves = board.nrOfLegalMovesForPieceOnPos(myPos);
        int blockingbenefit = nrofkingmoves==0 ? (pieceBaseValue(PAWN))
                : ( nrofkingmoves<=2 ? (pieceBaseValue(PAWN)-(pieceBaseValue(PAWN)>>2))
                : (pieceBaseValue(PAWN)>>1 ) );
        //blockingbenefit = 0;
        if (!myPiece().isWhite())
            blockingbenefit = -blockingbenefit;
        if (DEBUGMSG_MOVEEVAL)
            debugPrintln(DEBUGMSG_MOVEEVAL,"Checking checks for king on " + squareName(myPos)+".");
        for (VirtualPieceOnSquare checkerVPceAtKing : vPieces) {
            if (checkerVPceAtKing!=null && checkerVPceAtKing.color()==opponentColor(kcol)
                    && !isKing(checkerVPceAtKing.getPieceType())
            ) {
                ConditionalDistance checkerRmdToKing = checkerVPceAtKing.getRawMinDistanceFromPiece();
                checkerVPceAtKing.clearCheckGiving();
                if  ( !checkerRmdToKing.hasNoGo()
                        && (checkerRmdToKing.dist()==2 && checkerRmdToKing.isUnconditional()  //TODO!: make it generic for all future levels )
                            // No, because the condition must be fulfilled by opponent here: || checkerRmdToKing.dist()==1 && !checkerRmdToKing.isUnconditional()
                            // not yet, Todo: must be taken into account in code below first: || (checkerRmdToKing.dist()==2 && checkerRmdToKing.nrOfConditions()==1) // implies that the condition can be fulfilled by myself, so it is also a 1-move check
                            )
                ) {
                    for ( VirtualPieceOnSquare checkerAtCheckingPos : checkerVPceAtKing.getShortestReasonableUnconditionedPredecessors() ) {   // getPredecessorNeighbours() )
                        ConditionalDistance checkerMinDistToCheckingPos = checkerAtCheckingPos.getMinDistanceFromPiece();
                        if (checkerMinDistToCheckingPos.dist()==1
                                && checkerMinDistToCheckingPos.isUnconditional()
                        )  {
                            int checkFromPos = checkerAtCheckingPos.myPos;
                            // Todo!: There is a bug here: e.g. after 1. d4 d5  2. Dd3 there is a dist==2 to king check (via b5), but it looks like via e3 is the same (has no condition to go to e3), but it does not know about the later condition from e3 to e8...
                            //  "vPce(27=weiße Dame) on [e3] 1 ok away from origin {d3} is able to give check on e3 and [...]
                            //   is able to cover 1 of 1 king moves.
                            //   Benefit 24999@1 for Check blocking by vPce(11=schwarzer Bauer) on [e5] 1 ok away from origin {e7} to e8.
                            //   ->e7e5(24999@1)"
                            if (DEBUGMSG_MOVEEVAL)
                                debugPrintln(DEBUGMSG_MOVEEVAL, checkerAtCheckingPos + " is able to give check on " + squareName(checkFromPos) + " and ");
                            checkerAtCheckingPos.setCheckGiving();
                            int futureLevel = 0;  /* getStdFutureLevel(checkerAtCheckingPos)
                                                    + (checkerMinDistToCheckingPos.isUnconditional() ? 0 : 1); */
                            // count how many previously legal moves are blocked by the check
                            int countNowCoveredMoves = 0;
                            int countFreedMoves = 0;
                            for (VirtualPieceOnSquare kingsNeighbour : getvPiece(myPieceID).getNeighbours()) {
                                if ( isPawn(checkerAtCheckingPos.getPieceType())
                                    && fileOf(checkerAtCheckingPos.myPos) == fileOf(kingsNeighbour.myPos) )
                                    continue; // a pawn does not attack much in the straight direction
                                VirtualPieceOnSquare checkerVPceAroundKing = board.getBoardSquare(kingsNeighbour.myPos).getvPiece(checkerVPceAtKing.getPieceID());
                                boolean wasLegalKingMove = myPiece().isBasicallyALegalMoveForMeTo(checkerVPceAroundKing.myPos);
                                ConditionalDistance checkerRmdAroundKing = checkerVPceAroundKing.getRawMinDistanceFromPiece();
                                debugPrint(DEBUGMSG_MOVEEVAL, " .. check covering " + squareName(checkerVPceAroundKing.myPos) + ": ");
                                if (  /*// TODO: does not work? - why?: makes mateIn1-Tests drop from ~2800 passes to ~2200
                                    (checkerRmdAroundKing.dist()==2 && checkerRmdAroundKing.isUnconditional()  //TODO!: make it generic for all future levels )
                                    || checkerRmdAroundKing.dist()==1 && !checkerRmdAroundKing.isUnconditional())
                                   && */
                                        checkerVPceAroundKing.getShortestReasonableUnconditionedPredecessors().contains(checkerAtCheckingPos)  // TODO!!!: remove this line, thisis always true within this for loop!
                                        && wasLegalKingMove
                                        && board.getBoardSquares()[checkerVPceAroundKing.myPos]
                                          .countDirectAttacksWithColor(checkerVPceAroundKing.color()) == 0  // count only newly covered places
                                ) {
                                    countNowCoveredMoves++;
                                    if (DEBUGMSG_MOVEEVAL)
                                        debugPrintln(DEBUGMSG_MOVEEVAL, " +1 = " + countNowCoveredMoves + ". ");
                                }
                                else
                                    debugPrintln(DEBUGMSG_MOVEEVAL, " no. ");

                                /*debugPrint(DEBUGMSG_MOVEEVAL, " .. check freeing: " + squareName(checkerVPceAroundKing.myPos)
                                        + " checkerRmdAroundKin=" + checkerRmdAroundKing
                                        + " !onSameAxis:" + (!dirsAreOnSameAxis(calcDirFromTo(checkerVPceAtKing.myPiece().getPos(), checkFromPos),
                                                                                calcDirFromTo(checkFromPos, checkerVPceAroundKing.myPos)))
                                        + " !legalKingMove:" + !wasLegalKingMove
                                        + " current attacks: " + board.getBoardSquares()[checkerVPceAroundKing.myPos].countDirectAttacksWithColor(checkerVPceAroundKing.color()) + "<=1: ");
                                */
                                if (checkerRmdAroundKing.dist() == 1 && checkerRmdAroundKing.isUnconditional()  //TODO!: make it generic for all future levels )
                                        && !dirsAreOnSameAxis(calcDirFromTo(checkerVPceAtKing.myPiece().getPos(), checkFromPos),
                                                              calcDirFromTo(checkFromPos, checkerVPceAroundKing.myPos))
                                        && !wasLegalKingMove
                                        && !board.hasPieceOfColorAt(kcol, checkerVPceAroundKing.myPos)
                                        && board.getBoardSquare(checkerVPceAroundKing.myPos)
                                            .countDirectAttacksWithColor(checkerVPceAroundKing.color()) <= 1  // checker must be the last to cover target square of king

                                ) {
                                    ///if (wasLegalKingMove)
                                    ///    System.err.println("Problem in calcCheckBlocking on board "+ board.toString() + " at " + checkerVPceAroundKing + ".");
                                    countFreedMoves++;
                                    //debugPrintln(DEBUGMSG_MOVEEVAL, " +1 = " + countFreedMoves + ". ");
                                }
                                //else
                                    //debugPrintln(DEBUGMSG_MOVEEVAL, "no.");

                            }
                            if (DEBUGMSG_MOVEEVAL)
                                debugPrintln(DEBUGMSG_MOVEEVAL, "is able to cover " + countNowCoveredMoves
                                    + (countFreedMoves > 0 ? " but frees " + countFreedMoves : "")
                                    + " of " + nrofkingmoves + " king moves.");
                            // find and give bonus to possible check blocking moves
                            int benefit;
                            if (nrofkingmoves + countFreedMoves - countNowCoveredMoves <= 0
                                    && !checkerMinDistToCheckingPos.hasNoGo()
                            ) { // no more moves for the king!
                                benefit = isWhite(kcol) ? -colorlessPieceType(PAWN) : colorlessPieceType(PAWN);
                                benefit <<= 1;
                                // was checkmateEval(kcol) >> 2;   // it was checkmate, if nobody blocks! (but must not really be, as blocks are possible, this is not finally analysed here...)
                            }
                            else if (countFreedMoves > countNowCoveredMoves) {
                                benefit = 0;
                            }
                            else {
                                benefit = (int) ((float) blockingbenefit * ((float) (countNowCoveredMoves - countFreedMoves) / (float) nrofkingmoves));  // proportion of remaining squares
                            }
                            if (checkerMinDistToCheckingPos.hasNoGo() )
                                benefit >>= 3;
                            if (!evalIsOkForColByMin(benefit, checkerVPceAtKing.color(), -(EVAL_TENTH >> 1)))
                                continue;  // move could loose more covered squares than it covers additionally.
                            // benefit to those who can block it
                            int countBlockers = checkerVPceAtKing.addBenefitToBlockers(checkFromPos, futureLevel, benefit);
                            // benefit to those who can cover the target square
                            //TODO!!! Bug? shouldn't this loop run over checkFromPos instead of getMyPos()?
                            //  and: isn't covering the checkfrompos already included in the above addBenefitToBlockers() call?
                            for ( VirtualPieceOnSquare coverer : board.getBoardSquare(getMyPos()).getVPieces() ) {
                                if (coverer != null && coverer.color() == kcol && !isKing(coverer.getPieceType())
                                        && coverer.getRawMinDistanceFromPiece().dist() == 2   //TODO!: make it generic for all future levels )
                                        && coverer.getRawMinDistanceFromPiece().isUnconditional()
                                        && !coverer.getRawMinDistanceFromPiece().hasNoGo()
                                ) {
                                    int attackdelta = countDirectAttacksWithColor(coverer.color()) - countDirectAttacksWithColor(coverer.myOpponentsColor());
                                    int finalBenefit = attackdelta >= 0 ? (benefit >> 1)                        // already covered more often than attacked
                                                                        : ( attackdelta < -1 ? benefit          // will not be enough
                                                                                             : (benefit << 1) );// just enough, lets cover it!
                                    if (DEBUGMSG_MOVEEVAL && abs(finalBenefit) > 4)
                                        debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit " + finalBenefit + "@" + futureLevel
                                                + " for Check hindering by " + coverer + " covering " + squareName(getMyPos()) + ".");
                                    coverer.addChance(finalBenefit, futureLevel, checkFromPos );
                                    countBlockers++;
                                }
                            }
                            if ( !checkerMinDistToCheckingPos.hasNoGo()
                                    && nrofkingmoves + countFreedMoves - countNowCoveredMoves <= 0
                                    && countBlockers == 0) {
                                // no more moves for the king and no blocking possible
                                benefit = checkmateEval(kcol);   // should be mate
                            }
                            if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                                debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit " + benefit + "@" + futureLevel
                                        + " for checking possibility by " + checkerVPceAtKing + " to " + squareName(myPos) + ".");
                            checkerAtCheckingPos.addChance(benefit, 0 );
                        }
                    }
                }
            }
        }
    }

    private void addKingCheckReleatedBenefits(VirtualPieceOnSquare attacker, final int inFutureLevel) {
        int nr = inFutureLevel - 2; // -2 because already threatening the square where check is, is benefit.
        boolean acol = attacker.color();
        boolean attackerIsWhite = isWhite(acol);
        // danger for king to move to its neighbour squares
        int dist2k = board.distanceToKing(myPos, acol);
        ConditionalDistance attackerRmd = attacker.getRawMinDistanceFromPiece();

        // first the king himself likes to know, if checking becomes more likely if he goes to a neighbouring square.
        if (isKing(attacker.getPieceType())
                && dist2k > 0 && dist2k < 3
                && attackerRmd.isUnconditional()
        ) {
            int currentKingDangerLevel = board.getBoardSquare(board.getKingPos(acol)).getFutureDangerValueForColor(opponentColor(acol));
            int dangerLevelHere = getFutureDangerValueForColor(opponentColor(acol));
            int benefit = (currentKingDangerLevel - dangerLevelHere) * (EVAL_TENTH - (EVAL_TENTH >> 2));  // +/-8,16,24,32
            if (benefit > 0 || dist2k > 1)
                benefit >>= 1;  // bonus is awarded less then fees will cost
            if (benefit < 0 && dist2k > 1)
                benefit = -1;  // almost no fee towards bad places two squares away
            if (!attackerIsWhite)
                benefit = -benefit;
            if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                debugPrintln(DEBUGMSG_MOVEEVAL, " Adding " + benefit + "@" + nr
                        + " benefit/fee for king move from level " + currentKingDangerLevel + " towards level " + dangerLevelHere + " more/less dangerous square " + squareName(myPos) + " for " + attacker + ".");
            if (nr < 0)
                nr = 0;
            attacker.addChance(benefit, nr);
        }

        // award pinning opponent piece to its king
        boolean kcol = opponentColor(acol);
        int benefit = 0;
        int benefit1 = 0;
        // Benefit for checking or attacking the opponents king -- be aware: normal relEval on king is often negative, as it is assumed that a king always needs to move away (out of check) when threatened.
        // this it not fully compensated/covered in checking/checkblocking method
        if ((attackerIsWhite ? myPieceType() == KING_BLACK : myPieceType() == KING) // increase attack on opponent King, which resides on this square
                && (attackerRmd.dist() == 1 || attackerRmd.dist() == 2) // not already pinning
                && attackerRmd.hasExactlyOneFromToAnywhereCondition()  // needs one to move away - this is the pinned piece!
        ) {
            Square pinnedSquare = board.getBoardSquare(attackerRmd.getFromCond(0));
            if (pinnedSquare.myPiece() != null // Todo: this should not be possible, but due to a bug it sometimes still is
                    && pinnedSquare.myPiece().color() != attacker.color()
            ) {    // it's a king-pin!
                // TODO!: this awards king-pins, but does not help to avoid them unless a move hinders/blocks the attacker move), but this could even be suicide. Actually king or pinnd piece should walk away
                debugPrint(DEBUGMSG_MOVEEVAL, "  king-pin possibility detected: ");
                VirtualPieceOnSquare attackerAtFromCond = pinnedSquare.getvPiece(attacker.getPieceID());
                if (!evalIsOkForColByMin(attackerAtFromCond.getRelEval(), attacker.color())
                        || attackerAtFromCond.getRawMinDistanceFromPiece().hasNoGo())
                    return; //continue;
                benefit1 = -attackerAtFromCond.getRelEval(); //abs(pinnedSquare.myPiece().getValue()) - abs(attacker.myPiece().getValue());
                //min ( attackerAtFromCond.getRelEval(),
                //    attackerAtFromCond.getRawMinDistanceFromPiece().lastMoveOrigin().getRelEval() );
                benefit1 -= benefit1 >> 3;
                if (attackerRmd.hasNoGo())
                    benefit1 >>= 3;
                if (!attackerIsWhite)
                    benefit1 = -benefit1;
                int pinFutureLevel = nr - 1;
                if (pinFutureLevel < 0)
                    pinFutureLevel = 0;
                if (abs(benefit1) > 2) {
                    if (DEBUGMSG_MOVEEVAL)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " Adding " + benefit1 + "@" + pinFutureLevel
                                + " benefit for pinning chance with move towards " + squareName(myPos) + " for " + attacker + ".");
                    attacker.addChance(benefit1, pinFutureLevel, pinnedSquare.getMyPos());

                    if (attackerRmd.dist() == 2) {  // in the case of ==1 it is too late already...
                        // motivate king to move away:
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " + " + (-benefit1 >> 1) + "@" + pinFutureLevel + " motivation for king to move away from pin.");
                        board.getPieceAt(myPos).addMoveAwayChance2AllMovesUnlessToBetween(
                                -benefit1 >> 1, pinFutureLevel,
                                pinnedSquare.getMyPos(), myPos + calcDirFromTo(pinnedSquare.getMyPos(), myPos),  // to on behing/through the king - hope this works at the boarder of the board...
                                false);
                        // motivate this piece here to move away, too
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " + " + (-benefit1 >> 1) + "@" + pinFutureLevel + " motivation for piece " + pinnedSquare.myPiece() + " on " + squareName(pinnedSquare.getMyPos()) + " to move away from pin.");
                        pinnedSquare.myPiece().addMoveAwayChance2AllMovesUnlessToBetween(
                                -benefit1 >> 1, pinFutureLevel,
                                pinnedSquare.getMyPos(), myPos + calcDirFromTo(pinnedSquare.getMyPos(), myPos),  // to one square behind/through the king - hope this works at the boarder of the board...
                                false);
                    }
                }
            }

            // simple checking benefit
            benefit = (positivePieceBaseValue(PAWN) >> 1)   // 50 reduced by opponents necessary aid
                    / (1 + attackerRmd.countHelpNeededFromColorExceptOnPos(kcol, myPos));
            int nrofkingmoves = board.nrOfLegalMovesForPieceOnPos(board.getKingPos(kcol));
            if (nrofkingmoves == 0)
                benefit <<= 2;  // *4 if king has no moves  (cannot say checkmate, as we here cannot tell, if other counter moves exist.)
            else if (nrofkingmoves == 1)
                benefit <<= 1;  // *2
            else if (nrofkingmoves >= 3)
                benefit >>= 3;  // /2
            if (attackerRmd.hasNoGo())
                benefit >>= 3;
            if (!attackerIsWhite)
                benefit = -benefit;
            if (nr < 0)
                nr = 0;
            if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                debugPrintln(DEBUGMSG_MOVEEVAL, " Adding " + benefit + "@" + nr
                        + " benefit for move towards " + squareName(myPos) + " for " + attacker + " for king attack.");
            attacker.addChance(benefit, nr, board.getKingPos(kcol));
        }

        // hanging pieces behind king
        if (!isSquareEmpty()
                && myPiece().color() != attacker.color()
                && !isKing(myPieceType())
                && (attackerRmd.dist() > 0 && attackerRmd.dist() <= 3) // not already pinning
                && attackerRmd.hasExactlyOneFromToAnywhereCondition()  // needs king to move away - this opens the way to the piecce here
                && attackerRmd.getFromCond(0) == board.getKingPos(opponentColor(attacker.color()))
                && evalIsOkForColByMin(attacker.getRelEvalOrZero(), myPiece().color(), -EVAL_TENTH)
        ){
            // the only fromCond indicates that the piece here can directly or in 1 mre move) give check and after king
            // moes away take or threaten here.
            // beware: could also be ok, as maybe the check can easily be blocked by a third of my pieces

            // lets start easy:
            int nowAchieveableDist2OppK = board.distanceToKing(myPos, opponentColor(acol));
            if (myPiece().color()==board.getTurnCol())
                nowAchieveableDist2OppK--;
            benefit = attacker.getRelEvalOrZero()>>2;
            if ( attackerRmd.dist()>=2 ) {
                int attackBenefit = -benefit;
                if (nowAchieveableDist2OppK<=2)
                    attackBenefit >>= 2;
                // motivate the attacker  // todo: should better motivate towards the checking place!
                attacker.addChance(attackBenefit>>1, attackerRmd.dist()-2);
                board.getBoardSquare(board.getKingPos(myPiece().color())).getvPiece(attacker.getPieceID())
                        .addChance(attackBenefit>>1, attackerRmd.dist()-2);
            }
            if ( nowAchieveableDist2OppK==1 && attackerRmd.dist()==1 ) {
                // already checking, but king can maybe cover the hanging piece
                getvPiece(board.getPieceIdAt(board.getKingPos(myPiece().color())))
                        .addChance( benefit, 0 );
            }
            else if ( nowAchieveableDist2OppK>1
                    && attackerRmd.dist()>1
                    && nowAchieveableDist2OppK-1<=attackerRmd.dist()) {
                // not already checking, but king in time to cover the hanging piece
                getvPiece(board.getPieceIdAt(board.getKingPos(myPiece().color())))
                        .addChance( benefit, 0 );
                // or move piece away  // todo: away is very unspecific here, could lead to places with same problem, should be selective
                myPiece().addMoveAwayChance2AllMovesUnlessToBetween(-benefit>>2, 0, NOWHERE, NOWHERE, false );
            }
            else if ( nowAchieveableDist2OppK>attackerRmd.dist()+1 ) {
                // king cannot cover in time, but king or piece can move away
                // todo: away is very unspecific here, could lead to places with same problem, should be selective
                board.getPieceAt(board.getKingPos(myPiece().color()))
                        .addMoveAwayChance2AllMovesUnlessToBetween(-benefit>>2, 0, NOWHERE, NOWHERE, false );
                myPiece().addMoveAwayChance2AllMovesUnlessToBetween(-benefit>>2, 0, NOWHERE, NOWHERE, false );
            }
        }
    }

    private int getKingAreaBenefit(VirtualPieceOnSquare attacker) {
        boolean acol = attacker.color();
        boolean attackerIsWhite = isWhite(acol);
        ConditionalDistance attackerRmd = attacker.getRawMinDistanceFromPiece();
        boolean kcol = opponentColor(acol);
        int benefit1 = 0;
        // + benefit "1 around the king"
        if (board.distanceToKing(myPos, kcol)==1) {
            benefit1 =   ((EVAL_TENTH<<2) - (EVAL_TENTH>>2) )  // 38
                        /(1+ attackerRmd.countHelpNeededFromColorExceptOnPos(kcol,myPos));
/*
            if ( countDirectAttacksWithColor(acol) == 0 ) {
                if ( countDirectAttacksWithColor(kcol ) >1 )
                    benefit1 += benefit1 >> 1;           // *1.25 because we do not yet cover this square at all, but it is well defended
                else
                    benefit1 += benefit1 >> 1;           // *1.5 because we do not yet cover this square at all
            }
            if ( countDirectAttacksWithColor(acol) == countDirectAttacksWithColor(kcol))
                benefit1 += benefit1>>2; // + benefit1>>3;  // *1.37 because up to now we only cover this square lest often than the enemy
            else if ( countDirectAttacksWithColor(acol) < countDirectAttacksWithColor(kcol))
                benefit1 += benefit1>>2;  // *1.25 because up to now we only cover this square lest often than the enemy
            int alreadyAttacking = board.getNrOfKingAreaAttacks(kcol);
*/
            if ( countDirectAttacksWithColor(acol) ==0 )
                benefit1 += benefit1>>1;           // *1.5 because we do not yet cover this square at all
            if ( countDirectAttacksWithColor(acol) <= countDirectAttacksWithColor(kcol))
                benefit1 += benefit1>>2;  // *1.25 because up to now we only cover this square lest often than the enemy
            int alreadyAttacking = board.getNrOfKingAreaAttacks(kcol);
            int attackerDist = attackerRmd.dist();
            if ( isSlidingPieceType(attacker.getPieceType())
                    && attackerDist > 1
                    && alreadyAttacking < 4 )
                benefit1 += (benefit1)/(attackerDist);
            if (attackerDist<=1)  //  not no much for moving directly next to the king
                benefit1 >>= 1;
            if (attackerRmd.hasNoGo())
                benefit1 >>= 3;
            if ( !attackerIsWhite )
                benefit1 = -benefit1;
            if (DEBUGMSG_MOVEEVAL && abs(benefit1)>4)
                debugPrintln(DEBUGMSG_MOVEEVAL,"  " + benefit1 + " benefit for move towards "+ squareName(myPos)
                    +" for " + attacker +" for near king attack/coverage.");
        }
        return benefit1;
    }

/* think about this later - might be better in the current move fashion to calc this per every benefit added
    void evalMovingOutOfTheWayEffects() {
        assert (myPieceID!=NO_PIECE_ID);  // must only be called on squares with pieces on it
        VirtualPieceOnSquare myVPce = getvPiece(myPieceID);
        for (VirtualPieceOnSquare vPce : vPieces )
            if (vPce!=null && vPce!=myVPce) {
                int inFutureLevel = getStdFutureLevel(vPce);
                if (inFutureLevel > MAX_INTERESTING_NROF_HOPS)  // here not +1 as we will also add one level with own move away anyway
                    continue;
                ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();
            }
    }
*/

    public int getAttacksValueForColor(final boolean color) {
        return countDirectAttacksWithColor(color)
                + ( (int)((coverageOfColorPerHops.get(2).get(colorIndex(color)).size()))/2 );
    }

    public int getFutureDangerValueForColor(final boolean color) {
        int res = coverageOfColorPerHops.get(1).get(colorIndex(color)).size();  // is 2nd row relevant here?
        res += coverageOfColorPerHops.get(2).get(colorIndex(color)).size() << 1;   // the 1-move away attackers *2
        res += (coverageOfColorPerHops.get(3).get(colorIndex(color)).size()+1)>>1; // the 2-move aways /2
        return res;
    }

    public int countDirectAttacksWithColor(final boolean color) {
        return coverageOfColorPerHops.get(0).get(colorIndex(color)).size()
                + coverageOfColorPerHops.get(1).get(colorIndex(color)).size();
    }

    public int countFutureAttacksWithColor(final boolean color, final int futureLevel) {
        return coverageOfColorPerHops.get(futureLevel).get(colorIndex(color)).size();
    }

    public int countDirectAttacksWithout2ndRowWithColor(final boolean color) {
        return coverageOfColorPerHops.get(0).get(colorIndex(color)).size();
    }

    private int vPceCoverOrAttackDistance(final VirtualPieceOnSquare vPce) {
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
        //TODO: Check why this worsenes the mateIn1-Test-Puzzles (from 223 to 291)
        //still we leave it here, it should improve other cases where being pinned needs to be obeyed
        if ( dist!=0 && !board.moveIsNotBlockedByKingPin(vPce.myPiece(), getMyPos()) ) {
            //debugPrintln(DEBUGMSG_MOVEEVAL,"King pin matters on square " + squareName(getMyPos()));
            if ( board.nrOfLegalMovesForPieceOnPos(board.getKingPos(vPce.color())) > 0 )  // todo: be more precise and check move axis
                dist++;  // if piece is pinned to king, increase dist by one, so first king can move away.
            else
                dist += 2; // even worse
        }
        final int colorlessPieceType = colorlessPieceType(vPce.getPieceType());
        if ( colorlessPieceType==PAWN ) {
            // some more exception for pawns
            if (// a pawn at dist==1 can beat, but not "run into" a piece
                //Todo: other dists are also not possible if the last step must be straight - this is hard to tell here
                    fileOf(myPos)==fileOf(vPce.myPiece().getPos()) && (dist==1 || (dist-rmd.nrOfConditions())==1)
            ) {
                return INFINITE_DISTANCE;
            }
            //in the same way do only count with extra distance, if it requires the opponent to do us a favour by
            // moving a piece to beat in the way (except the final piece that we want to evaluate the attacks/covers on)
            return dist + rmd.countHelpNeededFromColorExceptOnPos(opponentColor(vPce.color()),myPos);
        }
        else if ( dist==0 && colorlessPieceType==KING ) {
            // king is treated as if it'd defend itself, because after approach of the enemy, it has to move away and thus defends this square
            // TODO: Handle "King has no" moves here?
            return 2; //v0.29z4 tried 1 instead of 2, but even a little worse
        }

        // if distance is unconditional, then there is nothing more to think about:
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

    int myPieceType() {
        if (isSquareEmpty())
            return EMPTY;
        return board.getPiece(myPieceID).getPieceType();
    }

    public ChessPiece myPiece() {
        if (myPieceID==NO_PIECE_ID)
            return null;
        return board.getPiece(myPieceID);
    }

    @Override
    public String toString() {
        return "Square{" +
                "" + squareName(myPos) +
                (isSquareEmpty() ? " is empty" : " with " + myPiece()) +
                '}';
    }


    private boolean areClashResultsUpToDate() {
        // check if recalc is necessary
        long prevClashResultsUpdate = clashResultsLastUpdate;
        //boolean noPieceChangedDistance = true;
        for (VirtualPieceOnSquare vPce : vPieces) {
            // check if vPieces changed (distance) since last clash calculation
            if (vPce!=null && vPce.getLatestChange() > prevClashResultsUpdate)
                return false;
        }
        return true;
    }

    public int clashEval() {
        return clashEvalResult;
    }

    public int futureClashEval(int level) {
        //still needed - or always up to date after a move?
        //getClashes();  // assures clashes are calculated if outdated
        return (futureClashResults ==null || level>= futureClashResults.length)
                ? 0
                : futureClashResults[level];
    }

    /**
     *
     * @param color (as boolean, see ChessBasics) Color of side for which it is checked to come to this square
     * @return boolean to tell if a piece exists, that has a dist==1 (for now only this dist counts) and has a relEval
     * that generally (without sacrifice etc.) allows to come here
     */
    public boolean isColorLikelyToComeHere(boolean color) {
        int ci = colorIndex(color);
        // difficulty: pawns may be able to come here straightly - then they are not contained in the "coverage" list used above...
        // so check pawns first.
        final int colPawnPieceType = (isWhite(color) ? PAWN : PAWN_BLACK);
        for( VirtualPieceOnSquare vPce : vPieces )
            if (vPce!=null
                    && vPce.getPieceType()==colPawnPieceType
                    && fileOf(vPce.getMyPiecePos())==fileOf(getMyPos())
                    && vPce.getMinDistanceFromPiece().dist()==1
                    && evalIsOkForColByMin( vPce.getRelEvalOrZero(), color ) )
                return true;
        // then all others (already in the coverage list)
        for( VirtualPieceOnSquare vPce : coverageOfColorPerHops.get(0).get(ci) )
            if ( evalIsOkForColByMin( vPce.getRelEvalOrZero(), color ) )
                return true;
        for( VirtualPieceOnSquare vPce : coverageOfColorPerHops.get(1).get(ci) )
            if ( evalIsOkForColByMin( vPce.getRelEvalOrZero(), color ) )
                return true;
        return false;
    }


    public List<ChessPiece> directAttacksWithout2ndRowWithColor(boolean color) {
        List<ChessPiece> attackers = new ArrayList<>();
        // no 2nd row: for (int i=0; i<=1; i++) {
        for (VirtualPieceOnSquare vPce : coverageOfColorPerHops.get(0).get(colorIndex(color)))
            attackers.add(vPce.myPiece());
        //}
        return attackers;
    }

    public void resetBlocksChecks() {
        blocksCheckFor[0] = false;
        blocksCheckFor[1] = false;
    }

    public void setBlocksCheckFor(boolean color) {
        blocksCheckFor[colorIndex(color)] = true;
    }

    public boolean blocksCheckFor(boolean color) {
        return blocksCheckFor[colorIndex(color)];
    }

    public boolean attackByColorAfterFromCondFulfilled(boolean col, int fromPos) {
        for ( VirtualPieceOnSquare vPce : vPieces ) {
            if (vPce!=null && vPce.color()==col) {
                ConditionalDistance ad = vPce.getRawMinDistanceFromPiece();
                // TODO?: cond.dist we are looking for should be 1, but doe to curren dist.algo it is 2
                if (ad.dist() < 2
                        && ad.hasExactlyOneFromToAnywhereCondition()
                        && ad.getFromCond(0) == fromPos)
                    return true;
            }
        }
        return false;
    }

    VirtualPieceOnSquare getvPiece(int pid) {
        return vPieces.get(pid);
    }
    /*public void setvPiece(VirtualPieceOnSquare vPiece) {
        this.vPiece = vPiece;
    }*/

    public List<VirtualPieceOnSquare> getVPieces() {
        return vPieces;
    }

    public long getLatestClashResultUpdate() {
        return clashResultsLastUpdate;
    }

    public boolean isAttackedByPawnOfColor(boolean col) {
        for(VirtualPieceOnSquare vPce : getVPieces())
            if (vPce!=null
                    && vPce.color()==col
                    && isPawn(vPce.getPieceType())
                    && vPce.getRawMinDistanceFromPiece().dist()==1
                    && fileOf(vPce.getMyPiecePos())!=fileOf(vPce.myPos)
            ) {
                return true;
            }
        return false;
    }

    public void evalContribBlocking() {
        if (isSquareEmpty())
            return;
        for ( VirtualPieceOnSquare vPce : vPieces ) {
            if (vPce == null || vPce.getRawMinDistanceFromPiece().dist()!=1
                || !vPce.getRawMinDistanceFromPiece().isUnconditional()
                || !isSlidingPieceType(vPce.getPieceType()) )
                continue;
            int blockingFee = -vPce.getClashContribOrZero();
            if ( evalIsOkForColByMin( -blockingFee, vPce.color(), -EVAL_HALFAPAWN) ) {
                // vPce has a Contribution here, nobody should block this way...
                // TODO: Check, it does not really benefit -> reduced to >>4
                debugPrintln(DEBUGMSG_MOVEEVAL,"scan blocking of contribution of " + blockingFee
                        + " of " + vPce + ".");
                blockingFee >>= 1;
                for (int pos : calcPositionsFromTo(getMyPos(), vPce.myPiece().getPos()) ) {
                    if ( pos==getMyPos() )
                        continue;
                    // forall positions in between here and the piece
                    board.getBoardSquare(pos).setEvalsForBlockingHereExceptPceIdOrAttackingToo( blockingFee, getMyPos(), vPce.getPieceID() );
                }
            }
        }
    }

    private void setEvalsForBlockingHereExceptPceIdOrAttackingToo(final int blockingFee, final int contribPos, final int contributorId) {
        for ( VirtualPieceOnSquare blocker : vPieces ) {
            if ( blocker == null
                    || blocker.getRawMinDistanceFromPiece().dist() != 1   // if it is not I direct move we cannot set a warning - we would need a benefit degradation possibility in the downward "low tide" calculation
                    || isKing(blocker.getPieceType()) )
                continue;
            boolean blockerIsFriend =  blocker.color() == colorOfPieceType(contributorId);
            if ( !blockerIsFriend && blocker.getMinDistanceFromPiece().hasNoGo() )
                continue;
            final int blockerId = blocker.getPieceID();
            if ( blockerId==contributorId || blockerId==board.getPieceIdAt(contribPos) )
                continue;
            // check if this Pce would come closer to defend
            ConditionalDistance rmdToTarget = board.getBoardSquare(contribPos).getvPiece(blockerId).getRawMinDistanceFromPiece();
            if ( blockerIsFriend && rmdToTarget.dist() == blocker.getMinDistanceFromPiece().dist()+1 )
                continue; // no need to fee, the "blocker" also covers the contribution (and thus puts the contributor only in 2nd row)
            int inFutureLevel = blocker.getStdFutureLevel();
            if (inFutureLevel>MAX_INTERESTING_NROF_HOPS)
                continue;
            if (inFutureLevel<0)
                inFutureLevel=0;
            ConditionalDistance rmd = blocker.getRawMinDistanceFromPiece();
            if (DEBUGMSG_MOVEEVAL && abs(blockingFee)>4)
                debugPrintln(DEBUGMSG_MOVEEVAL," " + blockingFee + "@"+inFutureLevel+" DEACTIVATED: Fee for blocking a contribution on square "+ squareName(myPos)+" with " + blocker + ".");
            blocker.addChance( blockingFee, inFutureLevel );
        }
    }


    public void avoidRunningIntoForks() {
        for ( VirtualPieceOnSquare vPce : getVPieces() ) {
            if (vPce == null
                    || vPce.getRawMinDistanceFromPiece().dist()!=1
                    || !vPce.getRawMinDistanceFromPiece().isUnconditional()
                    || !evalIsOkForColByMin(vPce.getRelEvalOrZero() , vPce.color() ) )
                continue;
            // run over all vPces that can go here directly
            // check if an attacker (that is of less value than vPce) can threaten this square in one move.
            for ( VirtualPieceOnSquare attacker : getVPieces() ) {
                if (attacker == null
                        || attacker.color() == vPce.color()
                        // not needed, included in the value compare below  || attacker.getPieceType() == vPce.getPieceType()  // cannot fork with same piecetype it can usually just take back
                        || attacker.getRawMinDistanceFromPiece().dist() != 2
                        || attacker.getRawMinDistanceFromPiece().hasNoGo()
                        || !( attacker.getRawMinDistanceFromPiece().isUnconditional()
                        || (isPawn(attacker.getPieceType())   // unconditional or a toCond that enables a pawn to come here - which vpce would exactly do...
                        && attacker.getRawMinDistanceFromPiece().nrOfConditions()==1
                        && attacker.getRawMinDistanceFromPiece().getToCond(0 )==myPos) )
                        || abs(attacker.getValue())-EVAL_TENTH >= abs(vPce.getValue()) )
                    continue;
                // we have an attacker that can attack this square in 1 move
                // loop over all positions from where the opponent can attack/cover this square
                for (VirtualPieceOnSquare attackerAtLMO : attacker.getShortestReasonableUnconditionedPredecessors()) {
                    if (attackerAtLMO == null
                            || attackerAtLMO.getRawMinDistanceFromPiece().hasNoGo()
                            || attackerAtLMO.getRawMinDistanceFromPiece().dist()!=1
                            || !attackerAtLMO.getRawMinDistanceFromPiece().isUnconditional() )
                        continue;
                    // if this is already a dangerous move, in sum this is a fork...
                    int forkingDanger = attackerAtLMO.additionalChanceWouldGenerateForkingDanger(
                            getMyPos(),
                            vPce.getRelEvalOrZero() - (vPce.getValue()-(vPce.getValue()>>3)) );
                    if (attackerAtLMO.getMinDistanceFromPiece().hasNoGo())
                        forkingDanger >>= 1;
                    if ( !evalIsOkForColByMin(forkingDanger, vPce.color()) ) {
                        if (DEBUGMSG_MOVEEVAL && abs(forkingDanger)>4)
                            debugPrintln(DEBUGMSG_MOVEEVAL," " + forkingDanger + "@0 danger moving " + vPce + " into possible fork on square "+ squareName(myPos)+ " by " + attackerAtLMO + ".");
                        vPce.addChance(forkingDanger, 0 );
                    }
                }
            }
        }
    }


    public void motivateToEnableCastling(boolean col) {
        for (VirtualPieceOnSquare vPce : vPieces) {
            if (vPce == null
                    || vPce.color() != col
                    || isKing(vPce.getPieceType() )
                    || isRook(vPce.getPieceType() ) )
                continue;
            ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();
            int benefit = isWhite(col) ? EVAL_TENTH : -EVAL_TENTH;
            if ( rmd.dist() == 0 ) {
                // motivate to move away
                ChessPiece piece2Bmoved = board.getPieceAt(getMyPos());
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL," " + benefit + "@0 motivation for " + vPce + " to clear king castling area.");
                piece2Bmoved.addMoveAwayChance2AllMovesUnlessToBetween(
                        benefit,
                        0,
                        NOWHERE, NOWHERE, false);
            }
            else if ( rmd.dist() == 1 && rmd.isUnconditional() ) {
                // motivate to not move here
                benefit >>= 1;
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL," " + (-benefit) + "@1 warning to " + vPce + " to keep king castling area clear.");
                vPce.addRawChance(-benefit, 1);
            }
        }
    }
}
