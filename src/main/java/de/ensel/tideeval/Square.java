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
        clearCoveragePerHopsLists();
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
        debugPrintln(DEBUGMSG_CLASH_CALCULATION, "");
        debugPrint(DEBUGMSG_CLASH_CALCULATION, "Evaluating " + this + ": ");

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
                moves.add(new Move(assassin.getPiecePos(), getMyPos()));
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
                resultIfTaken[exchangeCnt] = -assassin.myPiece().getValue();
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
                    boolean shouldBeBeneficalForWhite = ((i % 2 == 1) == (firstTurnCI == colorIndex(WHITE)));
                    if ((myBeatResult > -EVAL_DELTAS_I_CARE_ABOUT && shouldBeBeneficalForWhite   // neither positive result evaluation and it is whites turn
                            || myBeatResult < EVAL_DELTAS_I_CARE_ABOUT && !shouldBeBeneficalForWhite)   // nor good (i.e. neg. value) result for black
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
                        }
                        else {
                            int vPceFoundAt = clashCandidates.get(colorIndex(vPce.color())).indexOf(vPce);
                            if (vPceFoundAt > -1) {
                                int vPceClashIndex = vPceFoundAt * 2 + (colorIndex(vPce.color()) == firstTurnCI ? 0 : 1);  // convert from place in clashCandidates to final clash order
                                if ( vPceClashIndex <= endOfClash-1 && myPieceID!=NO_PIECE_ID   // if vPce is part of the clash (even the last) remember its contribution to the clash.
                                        ||  vPceClashIndex > endOfClash-1 && vPceClashIndex < exchangeCnt
                                        && myPieceID!=NO_PIECE_ID && vPce.color()==myPiece().color() // vPce was not part of the active clash fight, but part of the remaining defence, so it could still contribute in covering
                                ) {
                                    // TODO: check: usage of this old method might be incorrect in some cases concerning Pieves from the 2ndRow (see above)
                                    int clashResultWithoutVPce = calcClashResultExcludingOne(isWhiteColorIndex(firstTurnCI),
                                            board.getBoardSquares()[myPos].getvPiece(myPieceID),
                                            clashCandidates.get(colorIndex(WHITE)),
                                            clashCandidates.get(colorIndex(BLACK)),
                                            vPce,
                                            new ArrayList<VirtualPieceOnSquare>(0),
                                            new ArrayList<VirtualPieceOnSquare>(0),
                                            null);
                                    vPce.setClashContrib(clashEvalResult-clashResultWithoutVPce);
                                }
                                if (vPceClashIndex == 0) {
                                    // this vPce was anyway the first mover in the normal clash -> take clash result
                                    if (endOfClash==0)  // this means already the first clash-move would not have been done
                                        vPce.setRelEval(resultIfTaken[1]);   // so lets document=set the bad result here-
                                    else
                                        vPce.setRelEval(resultFromHereOn);
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
                                        vPce.setRelEval(-vPce.myPiece().baseValue()); // vPce would be killed in the clash, so it can go here, but not go pn from here -> so a bad value like checkmate will result in a NoGo Flag, but not really say the truth -> a clash calc (considering if this Pce would go either first or wait until the end of the clash) is needed!
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
                                    // Todo: is incorrect, if curren vpce origins from the "2nd row", while its enabling piece was the last one.
                                    int nextOpponentAt = vPceFoundAt + (colorIndex(vPce.color()) == firstTurnCI ? 0 : 1);
                                    if (nextOpponentAt >= clashCandidates.get(colorIndex(!vPce.color())).size()) {
                                        // no more opponents left, so yes we can go there - but only after the clash & if it actually took place
                                        old_updateRelEval(vPce);  //see todo above...
                                        //vPce.setRelEval(0);
                                    }
                                    else if (vPce.myPiece().isWhite() && vPce.myPiece().getValue() - EVAL_DELTAS_I_CARE_ABOUT >= -clashCandidates.get(colorIndex(!vPce.color())).get(nextOpponentAt).myPiece().getValue()
                                            || !vPce.myPiece().isWhite() && vPce.myPiece().getValue() + EVAL_DELTAS_I_CARE_ABOUT <= -clashCandidates.get(colorIndex(!vPce.color())).get(nextOpponentAt).myPiece().getValue()
                                    )   // i am more valuable than the next opponent
                                        vPce.setRelEval(-vPce.myPiece().baseValue());  // vPce would be killed in the clash, so it can go here, but not go pn from here -> so a bad value like checkmate will result in a NoGo Flag
                                    else {
                                        // no more opponents left, so yes we can go there - but only after the clash
                                        old_updateRelEval(vPce);  //see todo above...
                                        //vPce.setRelEval(0);
                                    }
                                }
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
        List<VirtualPieceOnSquare> whites = new ArrayList<>(coverageOfColorPerHops.get(0).get(colorIndex(WHITE)));
        List<VirtualPieceOnSquare> blacks = new ArrayList<>(coverageOfColorPerHops.get(0).get(colorIndex(BLACK)));
        whites.addAll(coverageOfColorPerHops.get(1).get(colorIndex(WHITE)));
        blacks.addAll(coverageOfColorPerHops.get(1).get(colorIndex(BLACK)));
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
                fuzzedWithKingInList=true;
            }
            else
                currentResult = -currentVPceOnSquare.myPiece().getValue();
        }

        List<VirtualPieceOnSquare> whiteOthers = new ArrayList<>(); //coverageOfColorPerHops.get(2).get(colorIndex(WHITE)));
        List<VirtualPieceOnSquare> blackOthers = new ArrayList<>(); //coverageOfColorPerHops.get(2).get(colorIndex(BLACK)));
        // TODO-refactor: this code piece is duplicated
        for(int h = 2; h< min(4, MAX_INTERESTING_NROF_HOPS); h++) {
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
                && isPawn(evalVPce.getPieceType())
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
                        && evalIsOkForColByMin( vPce.getRelEval(), turn)
                ) {
                    firstMover = vPce;
                    break;
                }
            if (firstMover==null) {  // --> no straight pawn victim found,
                // we could take a Piece from the other attackers, but as this is a pawn guarding the square, that piece would always have a NoGo to get here and never do it.
                turn = opponentColor(turn);
                currentResult = -evalVPce.myPiece().baseValue(); // checkmateEval(turn);  // a "random" very high bad value, so the piece will get a NoGo later in the algorithm
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

    // todo: add similar check for normal forks  (compare vs. todays improvides method of 2 benefits in the same distance of a legal move)
    void evalCheckingForks() {
        for (VirtualPieceOnSquare vPce : vPieces)
            if (vPce != null && !isKing(vPce.getPieceType()) ) {
                ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();
                int inOrderNr = rmd.dist()
                        + rmd.countHelpNeededFromColorExceptOnPos(opponentColor(vPce.color()), myPos);
                if (inOrderNr <= 0)
                    inOrderNr = 0;
                if (inOrderNr >= MAX_INTERESTING_NROF_HOPS           // out of interest
                    || !vPce.isCheckGiving()                         // no check, no check giving fork...
                    || vPce.getMinDistanceFromPiece().hasNoGo() )    // cannot go there really
                    continue;
                debugPrintln(DEBUGMSG_MOVEEVAL," Evaluating checking fork @"+ inOrderNr + " on square "+ squareName(myPos)+" for " + vPce + ": ");
                // find best neighbour benefit besides king-check
                //VirtualPieceOnSquare bestNeighbour = null;
                int max=0;
                //TODO!!: this only works for non-sliding pieces ... for sliding (or then for all) pieces it needs
                // a different approach of handing all addChances also to the predecessor squares, esp. if they
                // are checking -> then can even handle normal forks!
                for (VirtualPieceOnSquare neighbour : vPce.getNeighbours())
                    if ( neighbour!=null && !isKing(neighbour.getPieceType()) ) {
                        int nBestChance = neighbour.getBestChanceOnLevel(inOrderNr);
                        if ( (isWhite(vPce.color()) ? nBestChance > max
                                                    : nBestChance < max )
                        ) {
                            max = nBestChance;
                            //bestNeighbour = neighbour;
                        }
                        //debugPrintln(DEBUGMSG_MOVEEVAL," Found checking fork benefit " + nBestChance +"@"+ inOrderNr + " on square "+ squareName(myPos)+" for " + vPce + ".");
                    }
                if (!evalIsOkForColByMin(max,vPce.color()))
                    continue;
                // and add it on level closer
                if ( inOrderNr > 0)
                    inOrderNr--;
                if (!evalIsOkForColByMin(max,vPce.color()))
                    continue;
                if (abs(max)>3)
                    debugPrintln(DEBUGMSG_MOVEEVAL," Detected max checking fork benefit of " + max+ "@"+ inOrderNr + " on square "+ squareName(myPos)+" for " + vPce + ".");
                vPce.addChance(max, inOrderNr );
            }
    }

    void calcFutureClashEval() {
        //must be already called: getClashes();  // makes sure clash-lists are updated
        if (isSquareEmpty()) {
            futureClashResults = null;
        }
        else if (!isKing(myPieceType())) {  // attacks on kings are treated differently further down
        /*for ( VirtualPieceOnSquare vPce: vPieces )
            if (vPce!=null) {
                //vPce.resetChances();   // TODO - make every calculation here change-dependent, not reset and recalc all...
                if (vPce.distIsNormal()
                        && evalIsOkForColByMin(vPce.getRelEval(), vPce.color(), -EVAL_DELTAS_I_CARE_ABOUT)
                        && vPce.getRelEval() != NOT_EVALUATED) {
                    // also add relEval-Chances (without the beating calculation of below // Todo: Which option is better? this here or the calc below...? (see todo below)
                    /*vPce.addChance(vPce.getRelEval() + (vPce.myPiece().isWhite() ? -23 : +22),
                            vPce.getMinDistanceFromPiece().dist()
                                    + vPce.getMinDistanceFromPiece().countHelpNeededFromColorExceptOnPos(opponentColor(vPce.color()), myPos));
                     */
         /*       }
            }*/
            // start simulation with my own piece on the square and the opponent of that piece starting to decide whether to
            // bring in additional attackers

            boolean initialTurn = opponentColor(colorOfPieceType(myPieceType()));
            boolean turn = initialTurn;
            final VirtualPieceOnSquare currentVPceOnSquare = getvPiece(myPieceID);
            List<VirtualPieceOnSquare> whites = new ArrayList<>(coverageOfColorPerHops.get(0).get(colorIndex(WHITE)));
            List<VirtualPieceOnSquare> blacks = new ArrayList<>(coverageOfColorPerHops.get(0).get(colorIndex(BLACK)));
            whites.addAll(coverageOfColorPerHops.get(1).get(colorIndex(WHITE)));
            blacks.addAll(coverageOfColorPerHops.get(1).get(colorIndex(BLACK)));
            List<VirtualPieceOnSquare> whiteMoreAttackers = coverageOfColorPerHops.get(2).get(colorIndex(WHITE));
            List<VirtualPieceOnSquare> blackMoreAttackers = coverageOfColorPerHops.get(2).get(colorIndex(BLACK));
            futureClashResults = new int[Math.max(MAX_INTERESTING_NROF_HOPS + 1,
                    (Math.max(coverageOfColorPerHops.get(2).get(colorIndex(WHITE)).size(),
                            coverageOfColorPerHops.get(2).get(colorIndex(BLACK)).size())
                            * 2 + 1))];
            int nr = 0;
            int bNext = 0;
            int wNext = 0;
            debugPrintln(DEBUGMSG_FUTURE_CLASHES, "future clashes on " + this);
            //Todo: Add moreAttackers from d==3ff (but with move of opponent in between, if he can till add a d==3 Piee etc....) - is only addad as chances for now, see below
            if (isWhite(turn) && bNext < blackMoreAttackers.size()
                    || !isWhite(turn) && wNext < whiteMoreAttackers.size()) {
                turn = !turn;   // if opponent still has pieces left, but we don't, then switch sides...
            }
            while ( /*isWhite(turn) ? */  wNext < whiteMoreAttackers.size() ||
                    /*:*/ bNext < blackMoreAttackers.size()) {
                debugPrintln(DEBUGMSG_FUTURE_CLASHES, "");
                VirtualPieceOnSquare additionalAttacker;
                // bring additional pieces in
                if (isWhite(turn)) {
                    debugPrint(DEBUGMSG_FUTURE_CLASHES, "White adds " + whiteMoreAttackers.get(wNext));
                    additionalAttacker = whiteMoreAttackers.get(wNext);
                    whites.add(additionalAttacker);
                    whites.sort(VirtualPieceOnSquare::compareTo);
                    wNext++;
                } else { // blacks turn
                    debugPrint(DEBUGMSG_FUTURE_CLASHES, "Black adds " + blackMoreAttackers.get(bNext));
                    additionalAttacker = blackMoreAttackers.get(bNext);
                    blacks.add(additionalAttacker);
                    blacks.sort(VirtualPieceOnSquare::compareTo);
                    bNext++;
                }
                // main calculation
                // ToDo: Check if algo still works after calcClashResultExcludingOne was changed to also pull from the others-lists
                futureClashResults[nr] = calcClashResultExcludingOne(
                        initialTurn, currentVPceOnSquare,
                        whites, blacks,
                        null,
                        whiteMoreAttackers, blackMoreAttackers, null);
                // add new chances
                int benefit = 0;
                if (evalIsOkForColByMin(futureClashResults[nr] - clashEval(),
                        additionalAttacker.color(), -EVAL_DELTAS_I_CARE_ABOUT)) {
                    benefit = futureClashResults[nr] - clashEval();
                    if (abs(benefit)>3)
                         debugPrintln(DEBUGMSG_MOVEEVAL," Benefit " + benefit + " for close future chances on square "+ squareName(myPos)+" with " + additionalAttacker + ": " + futureClashResults[nr] + "-" + clashEval());
                } else {
                    // no direct positive result on the clash but let's check the following:
                    if (countDirectAttacksWithColor(additionalAttacker.color())==0
                            || countDirectAttacksWithColor(additionalAttacker.color()) < countDirectAttacksWithColor(opponentColor(additionalAttacker.color()))) {
                        if (additionalAttacker.color() != currentVPceOnSquare.color()) {
                            // still a little attacking chance improvement if a piece comes closer to an enemy, right?
                            benefit = EVAL_TENTH + myPiece().getValue() >> 3;
                        } else if (additionalAttacker.color() == currentVPceOnSquare.color()) {
                            // still a little attacking chance improvement if a piece comes closer to cover one own piece once more, right?
                            benefit = EVAL_TENTH + myPiece().getValue() >> 3;
                        }
                    }
                }
                if (isKing(additionalAttacker.getPieceType()))
                    benefit >>= 1;  // /2 for kings
                benefit += getKingAreaBenefit(additionalAttacker);
                ConditionalDistance rmd = additionalAttacker.getRawMinDistanceFromPiece();
                int inOrderNr = rmd.dist() - 1
                        - (currentVPceOnSquare.color() == additionalAttacker.color() ? 1 : 0)  // covering happens 1 step faster than beating
                        + (rmd.isUnconditional() ? 0 : 1)
                        //+ (nr >> 1)
                        + rmd.countHelpNeededFromColorExceptOnPos(opponentColor(additionalAttacker.color()), myPos);
                if (abs(benefit)>3)
                    debugPrintln(DEBUGMSG_MOVEEVAL," Benefit of " + benefit + "@"+inOrderNr+" for close future chances on square "+ squareName(myPos)+" with " + additionalAttacker + ".");
                additionalAttacker.addChance(benefit,inOrderNr);

                debugPrint(DEBUGMSG_FUTURE_CLASHES, " => " + futureClashResults[nr]);
                nr++;
                // switch sides (or not)
                if (isWhite(turn) && bNext < blackMoreAttackers.size()
                        || !isWhite(turn) && wNext < whiteMoreAttackers.size()) {
                    turn = !turn;   // if opponent still has pieces left, we switch sides...
                } else {
                    // if not then result stays the same and same side can also bring in more pieces
                    futureClashResults[nr] = futureClashResults[nr - 1];
                    nr++;
                }
            }

            // add additional future chances
            int hopDistance = 3;  // 0-2 has already been considered in calculation above
            boolean attackerColor = opponentColor(currentVPceOnSquare.color());
            while (hopDistance < coverageOfColorPerHops.size()) {
                final List<VirtualPieceOnSquare> moreAttackers = isWhite(attackerColor)
                        ? coverageOfColorPerHops.get(hopDistance).get(colorIndex(WHITE))
                        : coverageOfColorPerHops.get(hopDistance).get(colorIndex(BLACK));
                for (VirtualPieceOnSquare additionalAttacker : moreAttackers) {
                    // still a little attacking chance improvement if a piece comes closer to an enemy, right?
                    int benefit = -myPiece().getValue() >> hopDistance;
                    benefit += getKingAreaBenefit(additionalAttacker);
                    ConditionalDistance rmd = additionalAttacker.getRawMinDistanceFromPiece();
                    int inOrderNr = rmd.dist() - 1
                            - (currentVPceOnSquare.color() == additionalAttacker.color() ? 1 : 0)  // covering happens 1 step faster than beating
                            + (rmd.isUnconditional() ? 0 : 1)
                            + rmd.countHelpNeededFromColorExceptOnPos(opponentColor(additionalAttacker.color()), myPos);
                    if (abs(benefit)>3)
                        debugPrintln(DEBUGMSG_MOVEEVAL," Benefit of " + benefit + "@"+inOrderNr+" for later future chances on square "+ squareName(myPos)+" with " + additionalAttacker + ".");
                    additionalAttacker.addChance(benefit, inOrderNr );
                    if (abs(benefit)>3)
                        debugPrintln(DEBUGMSG_MOVEEVAL, ".");
                }
                hopDistance++;
            }
        }

        //// extra benefits here for Pieces that could ge here (soon)

        // benefit for controlling extra squares
        int[] ableToTakeControlBonus = {0, 0};  // indicating nobody can take control
        for (int ci=0; ci<=1; ci++) {
            int oci = opponentColorIndex(ci);
            if (countDirectAttacksWithColor(colorFromColorIndex(opponentColorIndex(ci))) == 0) {
                ableToTakeControlBonus[ci] = EVAL_TENTH>>1; // because we do not yet cover this square at all
            }
            else if ( countDirectAttacksWithColor(colorFromColorIndex(ci))
                      < countDirectAttacksWithColor(colorFromColorIndex(oci))
                    && board.distanceToKing(getMyPos(), colorFromColorIndex(ci))==1) {
                // defend square next to my king
                ableToTakeControlBonus[ci] = pieceBaseValue(PAWN)<<1;
            }
            else if ( ( (clashEval()<=-EVAL_DELTAS_I_CARE_ABOUT && isWhiteColorIndex(ci) )
                            || (clashEval()>=EVAL_DELTAS_I_CARE_ABOUT && !isWhiteColorIndex(ci) ) ) ) {
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
        ableToTakeControlBonus[colorIndex(BLACK)] = -ableToTakeControlBonus[colorIndex(BLACK)];

        for (VirtualPieceOnSquare vPce : vPieces )
            if (vPce!=null) {
                ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();
                int inOrderNr = rmd.dist()
                        + rmd.countHelpNeededFromColorExceptOnPos(opponentColor(vPce.color()), myPos);
                if (inOrderNr<=0)
                    inOrderNr=0;  // TODO: Debug, if this can happen
                if (inOrderNr>MAX_INTERESTING_NROF_HOPS+1)
                    continue;;

                // benefit for coming closer to control extra squares
                if ( ( !isKing(vPce.getPieceType())
                            || board.getPieceCounterForColor(vPce.color())<7)
                        && abs(ableToTakeControlBonus[colorIndex(vPce.color())])>0
                        && rmd.dist()>1) {
                    int conquerSquBenefit = ( ( (ableToTakeControlBonus[colorIndex(vPce.color())]
                                                * vPce.myPiece().reverseBaseEval())>>8 )
                                              + ableToTakeControlBonus[colorIndex(vPce.color())] );
                    if (rmd.dist()<=3)  // more benefit for dist = 2 or 3 - hope it first brings more "friends" towards the square
                        conquerSquBenefit -= conquerSquBenefit>>2; // * 0,75
                    if (vPce.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                        conquerSquBenefit >>= 3;
                    if ( isKing(vPce.getPieceType()) )
                        conquerSquBenefit >>= 1;
                    int nr = inOrderNr>0 ? inOrderNr-1 : 0;
                    if (abs(conquerSquBenefit)>3)
                        debugPrintln(DEBUGMSG_MOVEEVAL," " + conquerSquBenefit + "@"+nr+" Benefit for conquering square "+ squareName(myPos)+" with " + vPce + ".");
                    vPce.addChance( conquerSquBenefit, nr );
                }

                // moving king towards pawns in endgames
                if ( ( !isSquareEmpty()
                        && isKing(vPce.getPieceType())
                        && board.getPieceCounterForColor(vPce.color())<8)  // Todo: take wxistance of queen as end game indicator
                        && isPawn(myPiece().getPieceType())
                ) {
                    int protectPawnBenefit;
                    if (vPce.color() ==  myPiece().color())    // own pawn
                        protectPawnBenefit = EVAL_TENTH;  // 10
                    else
                        protectPawnBenefit = EVAL_TENTH<<1;  // 20
                    if (vPce.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                        protectPawnBenefit >>= 3;
                    int nr = inOrderNr - 1; // ( (myPiece()!=null && vPce.color()==myPiece().color()) ? 1 : 0);  // covering is one faster then attacking+beating
                    if (nr<0)
                        nr=0;
                    if (abs(protectPawnBenefit)>3)
                        debugPrintln(DEBUGMSG_MOVEEVAL," " + protectPawnBenefit + "@"+nr+" Benefit for king approaching pawn on square "+ squareName(myPos)+" with " + vPce + ".");
                    vPce.addChance( protectPawnBenefit, nr );
                }


                // pawns try to get to promoting rank
                if (isPawn(vPce.getPieceType())
                        && (isFirstRank(getMyPos()) || isLastRank(getMyPos())) ) {
                    int promoBenefit = rmd.isUnconditional()
                            ? pieceBaseValue(QUEEN)- pieceBaseValue(PAWN)
                            : (pieceBaseValue(PAWN)>>1) - EVAL_TENTH;
                    if (vPce.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                        promoBenefit >>= 3;
                    if (abs(promoBenefit)>3)
                        debugPrintln(DEBUGMSG_MOVEEVAL," +/- " + promoBenefit + "@"+inOrderNr+ " Benefit for pawn "+vPce+" for moving towards promotion on "+ squareName(myPos)+".");
                    if ( isLastRank(getMyPos()) && isWhite(vPce.color()) )
                        vPce.addChance(promoBenefit, inOrderNr);
                    if ( isFirstRank(getMyPos()) && !isWhite(vPce.color()) )
                        vPce.addChance(-promoBenefit, inOrderNr);
                }

                // King Area Attacks/Defence
                int kingAreaBenefit = getKingAreaBenefit(vPce);
                if (abs(kingAreaBenefit)>EVAL_TENTH) {
                    int nr = inOrderNr>0 ? inOrderNr-1 : 0;
                    debugPrintln(DEBUGMSG_MOVEEVAL," Sum of benefits on king on "+ squareName(myPos)+" is: " + kingAreaBenefit + "@"+nr +".");
                    vPce.addChance(kingAreaBenefit, nr );
                }

                // moves/evals activated indirectly by moving away
                if (rmd.dist()>=1 && !rmd.isUnconditional() && vPce.hasRelEval()) {
                    int benefit = vPce.getRelEval();
                    for (Integer fromCond : rmd.getFromConds()) {
                        if (fromCond != -1 && benefit!=NOT_EVALUATED
                                && evalIsOkForColByMin(benefit, vPce.color(), -EVAL_TENTH) ) {
                            int nr = inOrderNr - 1 - ( (myPiece()!=null && vPce.color()==myPiece().color()) ? 1 : 0);  // covering is one faster then attacking+beating
                            if (nr<0)
                                nr=0;  // TODO!: Debug, why this can happen */
                            if (vPce.getMinDistanceFromPiece().hasNoGo())
                                benefit >>= 3;
                            if (isKing(vPce.getPieceType()))
                                benefit >>= 1;
                            if (abs(benefit)>3)
                                debugPrintln(DEBUGMSG_MOVEEVAL," " + benefit + "@"+nr+ " Benefit helping pieces freeing way of "+vPce+" to "+ squareName(myPos)+".");
                            // TODO: Take into account that moving away piece could incluence the benefit, as the getRelEval could rely on the 2Bmoved piece to take part in the clash
                            vPce.addChances2PieceThatNeedsToMove(
                                    benefit,
                                    nr,  // -2 because dist 1 is already a direct threat and -1 because one help is already fulfilled by moving away
                                    fromCond );
                        }
                    }
                }

                // avoid directly moving pieces on squares where a king-pin is likely
                if (rmd.dist()==1 && rmd.isUnconditional()
                        && positivePieceBaseValue(vPce.getPieceType() ) >= positivePieceBaseValue(PAWN)+EVAL_TENTH )  {
                    for ( VirtualPieceOnSquare pinner : vPieces )
                        if (pinner!=null && pinner.color()!=vPce.color()
                                && isSlidingPieceType(pinner.getPieceType())
                                && pinner.getRawMinDistanceFromPiece().dist()==2  //TODO!: make it generic for all futer levels
                                && !pinner.getRawMinDistanceFromPiece().hasNoGo()
                        ) {
                            ConditionalDistance pinnerRmd = pinner.getRawMinDistanceFromPiece();
                            inOrderNr = pinnerRmd.dist() - 2
                                    + (pinnerRmd.isUnconditional() ? 0 : 1)
                                    + pinnerRmd.countHelpNeededFromColorExceptOnPos(vPce.color(), myPos);
                            int kingPos = board.getKingPos(vPce.color());
                            if (kingPos<0)
                                continue;  // can happen in test cases
                            VirtualPieceOnSquare pinnerAtKingPos = board.getBoardSquares()[kingPos].getvPiece(pinner.getPieceID());
                            ConditionalDistance pinner2kingDist = pinnerAtKingPos.getRawMinDistanceFromPiece();
                            if ( pinner2kingDist.dist() != 2 || !pinner2kingDist.isUnconditional() )
                                continue;  // not able to give check in 1 move
                            for ( Move checkMove : pinnerAtKingPos.getFirstUncondMovesToHere() ) {
                                if (isBetweenFromAndTo(myPos, checkMove.to(), kingPos)) {
                                    int danger = -(vPce.myPiece().getValue() + (pinner.myPiece().getValue() >> 1)) >> 1;
                                    if (abs(danger)>3)
                                        debugPrintln(DEBUGMSG_MOVEEVAL," Avoiding kin-pin " +danger+"@"+inOrderNr
                                                        + " for "+vPce+" to "+ squareName(myPos)+".");
                                    vPce.addChance(danger, inOrderNr);  // warn vPce not to go there
                                    if (pinnerRmd.dist()>2) {
                                        pinner.getRawMinDistanceFromPiece().lastMoveOrigin()
                                                .addChance(danger >> 1, inOrderNr); // award possible pinner to come closer
                                        if (abs(danger)>3)
                                            debugPrintln(DEBUGMSG_MOVEEVAL," Benefit for coming closer to possible kin-pin "
                                                    +(danger>>1)+ "@"+inOrderNr+ " for "+(pinner.getRawMinDistanceFromPiece().lastMoveOrigin())
                                                    +" via "+ squareName(myPos)+".");
                                    }
                                }
                            }
                        }
                }

                // avoid moving to square where another one gets pinned to me
                // and maybe engourage the other one to move away a little?
                if ( rmd.dist()<3  // consider only for low range, doable moves
                        && !vPce.getMinDistanceFromPiece().hasNoGo() ) {
                    int nr = inOrderNr>0 ? inOrderNr-1 : 0;
                    VirtualPieceOnSquare pinnedVPce = null;
                    VirtualPieceOnSquare pinnerVPce = null;
                    // find possible pinned piece, if I move there
                    for (VirtualPieceOnSquare vp : getVPieces())
                        if ( vp!=null && isSlidingPieceType(vp.getPieceType())
                                && vp.color()!= vPce.color()
                                && vp.getRawMinDistanceFromPiece().dist()==1
                                && !vp.getRawMinDistanceFromPiece().hasNoGo()
                                && !vp.getRawMinDistanceFromPiece().isUnconditional()
                        ) {
                            // if opponent vp is attacking here almost directly but with a condition
                            int pinnedPos = vp.getRawMinDistanceFromPiece().getFromCond(0);
                            if (pinnedPos<0)
                                continue;  // was not a from-condition
                            ChessPiece pinnedPiece = board.getPieceAt(pinnedPos);
                            if (pinnedPiece==null)
                                continue; // should not happen, but to be sure
                            VirtualPieceOnSquare alsoPinnedVPce = board.getBoardSquares()[pinnedPos].getvPiece(pinnedPiece.getPieceID());
                            if (pinnedVPce==null
                                    || abs(alsoPinnedVPce.myPiece().getValue()) > abs(pinnedVPce.myPiece().getValue()) ) {
                                pinnedVPce = alsoPinnedVPce;  // find the most worthy pinned piece
                                pinnerVPce = vp;
                            }
                        }
                    if (pinnedVPce!=null) {
                        int benefit = (min(abs(vPce.myPiece().getValue()), abs(pinnedVPce.myPiece().getValue()))>>1);
                        if (rmd.dist() == 2)
                            benefit >>= 2;
                        if (!isWhite(vPce.color()))
                            benefit = -benefit;
                        if (abs(benefit) > 3)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " " + benefit + "@" + nr
                                    + " anti-benefit for going to pin-dangerous square " + squareName(myPos)
                                    + " with " + vPce
                                    + " possibly pinned by " + pinnerVPce.myPiece() + " on " + squareName(pinnerVPce.myPiece().getPos())
                                    + " pinning "+ pinnedVPce +".");
                        vPce.addChance(-benefit, nr);
                        pinnedVPce.addChance(-benefit, nr);
                        pinnerVPce.addChance(benefit, nr+1);
                    }
                }
            }

        if (!isSquareEmpty()) {
            // benefit to give king "air"
            int kingNeedsAirBenefit = getKingNeedsAirBenefit();
            if (abs(kingNeedsAirBenefit) > 0) {
                int nr = 0;
                debugPrintln(DEBUGMSG_MOVEEVAL, " Benefits of giving king air on " + squareName(myPos) + " is: " + kingNeedsAirBenefit + "@" + nr + ".");
                myPiece().addMoveAwayChance2AllMovesUnlessToBetween(
                        kingNeedsAirBenefit,
                        nr,
                        -1, -1, false );
            }
        }
    }

    private int getKingNeedsAirBenefit() {
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
        int checkablePieces = board.getBoardSquares()[kingPos].countFutureAttacksWithColor(opponentColor(kcol), 2);
        if ( board.nrOfLegalMovesForPieceOnPos(kingPos) == 0
                && countDirectAttacksWithColor(opponentColor(kcol)) == 0
                && (checkablePieces>0
                    || board.getBoardSquares()[kingPos].countFutureAttacksWithColor(opponentColor(kcol), 3)>0 )
        ) { // king can be checked soon
            if (checkablePieces>0)
                return isWhite(kcol) ? (positivePieceBaseValue(QUEEN))
                                     : -(positivePieceBaseValue(QUEEN)) ;
            else
                return isWhite(kcol) ? (positivePieceBaseValue(PAWN)-EVAL_TENTH)
                                     : -(positivePieceBaseValue(PAWN)-EVAL_TENTH) ;
        }
        return 0;
    }

    public void calcCheckBlockingOptions() {
        if (myPiece()==null)
            return; // should not happen
        boolean col = myPiece().color();
        int nrofkingmoves = board.nrOfLegalMovesForPieceOnPos(myPos);
        int blockingbenefit = nrofkingmoves==0 ? (pieceBaseValue(PAWN))
                : ( nrofkingmoves<=2 ? (pieceBaseValue(PAWN>>1))
                : (pieceBaseValue(PAWN)>>2 ) );
        //blockingbenefit = 0;
        if (!myPiece().isWhite())
            blockingbenefit = -blockingbenefit;
        debugPrintln(DEBUGMSG_MOVEEVAL,"Checking checks for king on " + squareName(myPos)+".");
        for (VirtualPieceOnSquare checkerVPceAtKing : vPieces) {
            if (checkerVPceAtKing!=null && checkerVPceAtKing.color()==opponentColor(col)
                    && !isKing(checkerVPceAtKing.getPieceType())
            ) {
                ConditionalDistance checkerRmdToKing = checkerVPceAtKing.getRawMinDistanceFromPiece();
                checkerVPceAtKing.clearCheckGiving();
                if  ( !checkerRmdToKing.hasNoGo()
                        && (checkerRmdToKing.dist()==2 && checkerRmdToKing.isUnconditional()  //TODO!: make it generic for all future levels )
                        || checkerRmdToKing.dist()==1 && !checkerRmdToKing.isUnconditional())
                ) {
                    for ( VirtualPieceOnSquare checkerAtCheckingPos : checkerVPceAtKing.getPredecessorNeighbours() ) {
                        ConditionalDistance checkerMinDistToCheckingPos = checkerAtCheckingPos.getMinDistanceFromPiece();
                        if (checkerMinDistToCheckingPos.dist()==1 && checkerMinDistToCheckingPos.isUnconditional())  {
                            int checkFromPos = checkerAtCheckingPos.myPos;
                            debugPrint(DEBUGMSG_MOVEEVAL, checkerAtCheckingPos + " is able to give check on " + squareName(checkFromPos) + " and ");
                            checkerAtCheckingPos.setCheckGiving();
                            int inOrderNr = checkerMinDistToCheckingPos.dist()
                                    + (checkerMinDistToCheckingPos.isUnconditional() ? 0 : 1)
                                    + checkerMinDistToCheckingPos.countHelpNeededFromColorExceptOnPos(col, myPos);
                            // count how many previously legal moves are blocked by the check
                            int countNowCoveredMoves = 0;
                            int countFreedMoves = 0;
                            for (VirtualPieceOnSquare neighbour : getvPiece(myPieceID).getNeighbours()) {
                                VirtualPieceOnSquare checkerVPceAroundKing = board.getBoardSquares()[neighbour.myPos].getvPiece(checkerVPceAtKing.getPieceID());
                                boolean wasLegalKingMove = myPiece().isBasicallyALegalMoveForMeTo(checkerVPceAroundKing.myPos);
                                ConditionalDistance checkerRmdAroundKing = checkerVPceAroundKing.getRawMinDistanceFromPiece();
                                if (  /*// TODO: does not work? - why?: makes mateIn1-Tests drop from ~2800 passes to ~2200
                                    (checkerRmdAroundKing.dist()==2 && checkerRmdAroundKing.isUnconditional()  //TODO!: make it generic for all future levels )
                                    || checkerRmdAroundKing.dist()==1 && !checkerRmdAroundKing.isUnconditional())
                                   && */
                                        checkerVPceAroundKing.getPredecessorNeighbours().contains(checkerAtCheckingPos)
                                        && board.getBoardSquares()[checkerVPceAroundKing.myPos]
                                          .countDirectAttacksWithColor(checkerVPceAroundKing.color()) == 0  // count only newly covered places
                                ) {
                                    countNowCoveredMoves++;
                                }
                                debugPrintln(DEBUGMSG_MOVEEVAL, " .. checking freeing: " + squareName(checkerVPceAroundKing.myPos) + " checkerRmdAroundKin=" + checkerRmdAroundKing
                                        + " !onSameAxis:" + (!dirsAreOnSameAxis(calcDirFromTo(checkerVPceAtKing.myPiece().getPos(), checkFromPos), calcDirFromTo(checkFromPos, checkerVPceAroundKing.myPos)))
                                        + " !legalKingMove:" + !wasLegalKingMove
                                        + " current attacks: " + board.getBoardSquares()[checkerVPceAroundKing.myPos].countDirectAttacksWithColor(checkerVPceAroundKing.color()) + "<=1.");

                                if (checkerRmdAroundKing.dist() == 1 && checkerRmdAroundKing.isUnconditional()  //TODO!: make it generic for all future levels )
                                        && !dirsAreOnSameAxis(calcDirFromTo(checkerVPceAtKing.myPiece().getPos(), checkFromPos),
                                        calcDirFromTo(checkFromPos, checkerVPceAroundKing.myPos))
                                        && !wasLegalKingMove
                                        && !board.hasPieceOfColorAt(col, checkerVPceAroundKing.myPos)
                                        && board.getBoardSquares()[checkerVPceAroundKing.myPos]
                                            .countDirectAttacksWithColor(checkerVPceAroundKing.color()) <= 1  // checker must be the last to cover target square of king

                                ) {
                                    ///if (wasLegalKingMove)
                                    ///    System.err.println("Problem in calcCheckBlocking on board "+ board.toString() + " at " + checkerVPceAroundKing + ".");
                                    countFreedMoves++;
                                }
                            }
                            debugPrintln(DEBUGMSG_MOVEEVAL, "is able to cover " + countNowCoveredMoves
                                    + (countFreedMoves > 0 ? " but frees " + countFreedMoves : "")
                                    + " of " + nrofkingmoves + " king moves.");
                            // find and give bonus to possible check blocking moves
                            int benefit;
                            if (nrofkingmoves + countFreedMoves - countNowCoveredMoves <= 0) {
                                // no more moves for the king!
                                benefit = checkmateEval(col) >> 2;   // was checkmate, if nobody blocks! (but must not really be, as blocks are possible, this is not finally analysed here...)
                            } else if (countFreedMoves > countNowCoveredMoves) {
                                benefit = 0;
                            } else {
                                benefit = (int) ((float) blockingbenefit * ((float) (countNowCoveredMoves - countFreedMoves) / (float) nrofkingmoves));  // proportion of remaining squares
                            }
                            if (!evalIsOkForColByMin(benefit, checkerVPceAtKing.color(), -(EVAL_TENTH >> 1)))
                                continue;  // move could loose more covered squares than it covers additionally.
                            // benefit to those who can block it
                            int countBlockers = 0;
                            for (int pos : calcPositionsFromTo(checkFromPos, myPos)) {
                                for (VirtualPieceOnSquare blocker : board.getBoardSquares()[pos].vPieces) {
                                    if (blocker != null && blocker.color() == col && !isKing(blocker.getPieceType())
                                            && (blocker.getRawMinDistanceFromPiece().dist() == 1   //TODO!: make it generic for all future levels )
                                            && blocker.getRawMinDistanceFromPiece().isUnconditional())
                                            && (!blocker.getRawMinDistanceFromPiece().hasNoGo())
                                    ) {
                                        int finalBenefit = (blocker.myPiece().baseValue() <= checkerVPceAtKing.myPiece().baseValue())
                                                ? benefit : (benefit >> 2);
                                        if (abs(finalBenefit) > 3)
                                            debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit " + finalBenefit + "@" + inOrderNr + " for Check blocking by " + blocker + " to " + squareName(myPos) + ".");
                                        blocker.addChance(finalBenefit, inOrderNr);
                                        countBlockers++;
                                    }
                                }
                            }
                            // but also benefit to the one who gives the check!
                            if (nrofkingmoves + countFreedMoves - countNowCoveredMoves <= 0
                                    && countBlockers == 0) {
                                // no more moves for the king and no blocking possible
                                benefit = checkmateEval(col);   // should be mate
                            }
                            if (abs(benefit) > 3)
                                debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit " + benefit + "@" + inOrderNr + " for checking possibility by " + checkerVPceAtKing + " to " + squareName(myPos) + ".");
                            checkerAtCheckingPos.addChance(benefit, 0);
                        }
                    }
                }
            }
        }
    }

    private int getKingAreaBenefit(VirtualPieceOnSquare attacker) {
        if (isKing(attacker.getPieceType()))
            return 0;
        boolean acol = attacker.color();
        boolean attackerIsWhite = isWhite(acol);
        boolean kcol = opponentColor(acol);
        int benefit0 = 0;
        int benefit1 = 0;
        // Benefit for checking or attacking the opponents king -- be aware: normal relEval on king is often negative, as it is assumed that a king always needs to move away (out of check) when threatened.
        // this it not fully compensated/covered in checkingücheckblocking method
        if (attackerIsWhite ? myPieceType()==KING_BLACK
                : myPieceType()==KING ) { // increase attack on King
            benefit0 =  (positivePieceBaseValue(PAWN)>>1)   // 50 reduced by opponents necessary aid
                        /(1+attacker.getRawMinDistanceFromPiece().countHelpNeededFromColorExceptOnPos(kcol,myPos));
            int nrofkingmoves = board.nrOfLegalMovesForPieceOnPos(board.getKingPos(kcol));
            if (nrofkingmoves==0)
                benefit0 <<= 2;  // *4 if king has no moves  (cannot say checkmate, as we here cannot tell, if other counter moves exist.)
            else if (nrofkingmoves==1)
                benefit0 <<= 1;  // *2
            else if (nrofkingmoves>=3)
                benefit0 >>= 2;  // /2
            if (attacker.getRawMinDistanceFromPiece().hasNoGo())
                benefit0 >>= 3;
            if ( !attackerIsWhite )
                benefit0 = -benefit0;
            debugPrintln(DEBUGMSG_MOVEEVAL, "  " + benefit0
                    + " benefit for move towards "+ squareName(myPos)+" for " + attacker +" for king attack.");
        }
        // + benefit "1 around the king"
        if (board.distanceToKing(myPos, kcol)==1
                && !attacker.getMinDistanceFromPiece().hasNoGo()
        ) {
            benefit1 =  (positivePieceBaseValue(PAWN)>>1)  // 50
                        /(1+attacker.getRawMinDistanceFromPiece().countHelpNeededFromColorExceptOnPos(kcol,myPos));
            if ( countDirectAttacksWithColor(acol) ==0 )
                benefit1 += benefit1>>1;           // *1.5 because we do not yet cover this square at all
            if ( countDirectAttacksWithColor(acol) <= countDirectAttacksWithColor(kcol))
                benefit1 += benefit1>>2;  // *1.25 because up to now we only cover this square lest often than the enemy
            int alreadyAttacking = board.getNrOfKingAreaAttacks(kcol);
            int attackerDist = attacker.getRawMinDistanceFromPiece().dist();
            if ( isSlidingPieceType(attacker.getPieceType())
                    && attackerDist > 1
                    && alreadyAttacking < 3 )
                benefit1 += (benefit1)/(1+attackerDist);
            if (attackerDist<=1)  //  not no much for moving directly next to the king
                benefit1 >>= 2;
            if ( !attackerIsWhite )
                benefit1 = -benefit1;
            debugPrintln(DEBUGMSG_MOVEEVAL,"  " + benefit1 + " benefit for move towards "+ squareName(myPos)+" for " + attacker +" for near king attack/coverage.");
        }
        if (attacker.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
            return (benefit0+benefit1)>>1;
        return benefit0+benefit1;
    }

    public int getAttacksValueforColor(final boolean color) {
        return countDirectAttacksWithColor(color)
                + ( (int)((coverageOfColorPerHops.get(2).get(colorIndex(color)).size()))/2 );
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
            return 1;
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

    private int myPieceType() {
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
        boolean noPieceChangedDistance = true;
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
                    && fileOf(vPce.getPiecePos())==fileOf(getMyPos())
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


    public List<ChessPiece> DirectAttacksWithout2ndRowWithColor(boolean color) {
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
                if (ad.dist() < 2 && ad.nrOfConditions() == 1 && ad.getFromCond(0) == fromPos)
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

}
