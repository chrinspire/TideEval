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

import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    private List<Move> clashMoves = null;
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
    private final boolean[] extraCoverageOfKingPinnedPiece  = new boolean[2];  // extra coverage of this square by a king-pinned piece - this does not count for clashes, but still prevents the king to take back or go there...


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
        if (vPce==null || !vPce.getRawMinDistanceFromPiece().isUnconditional() )
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
     * by whites and blacks. it excludes the one excludeVPce. this is useful to calc as if that pce had
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
        return calcClashResultExcludingOne(turn, vPceOnSquare, whites, blacks,
                                           excludeVPce, vPceOnSquare, whiteOthers, blackOthers, moves);
    }

    private static int calcClashResultExcludingOne( final boolean turn,
                                                    final VirtualPieceOnSquare vPceOnSquare,
                                                    List<VirtualPieceOnSquare> whites,
                                                    List<VirtualPieceOnSquare> blacks,
                                                    final VirtualPieceOnSquare excludeVPce1,
                                                    final VirtualPieceOnSquare excludeVPce2,
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
            if (assassin==excludeVPce1 || assassin==excludeVPce2) {
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
            if (assassin==excludeVPce1 || assassin==excludeVPce2) {
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
                vPceOnSquare.getMyPos()));  // ToDo: Make+use getter for myPos
        //// filling up whites and blacks from 2nd row, was originally implemented here, but in cases where the 1st row is empty from the beginning, this was not working.

        resultIfTaken += calcClashResultExcludingOne(
                !turn,assassin,
                whites, blacks,
                excludeVPce1,
                excludeVPce2,
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
        clashMoves = null;
        extraCoverageOfKingPinnedPiece[CIWHITE] = false;
        extraCoverageOfKingPinnedPiece[CIBLACK] = false;
        for (VirtualPieceOnSquare vPce : vPieces)
            if (vPce != null ) {
                int d = vPce.coverOrAttackDistanceNogofree();
                // TODO: deal with pinned pieces if mover unpins... board.moveIsNotBlockedByKingPin(vPce.myPiece()
                //fill clashCandidates initially with those clearly directly covering/attacking the square + sort it according to Piece value
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
                    //todo: looks like a check is needed, if 2nd row pce is king pinned
                    if (DEBUGMSG_CLASH_CALCULATION)
                        debugPrint(DEBUGMSG_CLASH_CALCULATION, " +adding " + vPce
                            + " to 2nd row clash candidates with d=" + d + " ");
                    clash2ndRow.get(colorIndex(vPce.color()))
                            .add(vPce);
                } else if (d < MAX_INTERESTING_NROF_HOPS && d > 0) {              // sort all others into their bucket according to d...
                    if ( board.currentDistanceCalcLimit()>=2  // king pins are not known before about round 2
                            && !board.moveIsNotBlockedByKingPin(vPce.myPiece(), getMyPos())
                            && vPce.getRawMinDistanceFromPiece().dist() == 1
                            && ( ( !isPawn(vPce.getPieceType())
                                   && vPce.getRawMinDistanceFromPiece().isUnconditional() )
                                 || (isPawn(vPce.getPieceType())
                                     && vPce.getMinDistanceFromPiece().nrOfConditions() == 1
                                     && vPce.getMinDistanceFromPiece().getToCond(0) >= 0 ) )
                    ) {
                        extraCoverageOfKingPinnedPiece[colorIndex(vPce.color())] = true;
                    }
                    putVPceIntoCoverageList(vPce, d);
                }
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
                    : -getvPiece(getPieceID()).getValue());
            /* bias did not bring advantages - test series was varying strongly with small value changes
            // bias clashes if mover is behind on material
            if ( colorFromColorIndex(turnCI) == board.getTurnCol()
                    && evalIsOkForColByMin(board.boardEvaluation(1),
                    opponentColor(colorFromColorIndex(turnCI)), -(EVAL_HALFAPAWN*3)) ) {
                int bias = ChessBoard.engineP1(); // EVAL_HALFAPAWN>>1 - (EVAL_TENTH<<1);  // 34
                if (isWhite(colorFromColorIndex(turnCI)))
                    bias = -bias;
                resultIfTaken[0] += bias;
            }
            // pos. bias if ahead in material
            if ( colorFromColorIndex(turnCI) == board.getTurnCol()
                    && evalIsOkForColByMin(board.boardEvaluation(1),
                          colorFromColorIndex(turnCI), -(EVAL_HALFAPAWN*3)) ) {
                int bias = EVAL_HALFAPAWN - (EVAL_TENTH<<1);  // 34
                if (isBlack(colorFromColorIndex(turnCI)))
                    bias = -bias;
                resultIfTaken[0] += bias;
            }*/
            VirtualPieceOnSquare assassin = null;
            List<Move> moves = new ArrayList<>();
            final boolean noOppDefenders = clashCandidates.get(turnCI^1).size() == 0;  // defender meaning opposite color defenders compared to the first assassin (whos turn is assumend at this evaluation round)
            List<List<VirtualPieceOnSquare>> clashCandidatesWorklist = new ArrayList<>(2);
            for (int ci = 0; ci <= 1; ci++)
                clashCandidatesWorklist.add(clashCandidates.get(ci).subList(0, clashCandidates.get(ci).size()));
            VirtualPieceOnSquare firstAssassin = null;
            if (clashCandidatesWorklist.get(turnCI).size()>0)
                firstAssassin = clashCandidatesWorklist.get(turnCI).get(0);

            while (clashCandidatesWorklist.get(turnCI).size()>0) {
                // take the first vPiece (of whose turn it is) and virtually make the beating move.
                assassin = clashCandidatesWorklist.get(turnCI).get(0);
                if ( isPawn(assassin.getPieceType())
                        && exchangeCnt==0
                        && isSquareEmpty()
                        && !onSameFile(getMyPos(), assassin.getMyPiecePos())
                ) {
                    // a pawn cannot come here by beating as the first piece, if the square is empty (or own piece)
                    if ( clashCandidatesWorklist.get(turnCI).size() > 1 ) {
                        int swappos = 1;
                        assassin = clashCandidatesWorklist.get(turnCI).get(1);
                        if ( isPawn(assassin.getPieceType())
                                && !onSameFile(getMyPos(), assassin.getMyPiecePos())
                        ) {  //n is another pawn that cannot beat...
                            if ( clashCandidatesWorklist.get(turnCI).size() > 2 )
                                swappos = 2;
                            else // nothing here to move to the square!
                                break;
                        }
                        assassin = clashCandidatesWorklist.get(turnCI).get(swappos);
                        clashCandidatesWorklist.get(turnCI).set(swappos, clashCandidatesWorklist.get(turnCI).get(0) );  // [non-pawn]=[pawn at 0]
                    }
                    else
                        break;
                }
                // King can only take if there are no more enemies left defending this square + remaining piece
                if ( isKing(assassin.getPieceType())
                     && ( clashCandidatesWorklist.get(turnCI^1).size()>0
                          || extraCoverageOfKingPinnedPiece[ colorIndex(assassin.myOpponentsColor())] ) )
                    break;

                clashCandidatesWorklist.set(turnCI,   // emulate pull()  (together with the get above)
                        clashCandidatesWorklist.get(turnCI).subList(1, clashCandidatesWorklist.get(turnCI).size()));
                moves.add(new Move(assassin.getMyPiecePos(), getMyPos()));
                // pull more indirectly covering pieces into the clash from the "2nd row"
                for (int ci = 0; ci <= 1; ci++) {
                    for (Iterator<VirtualPieceOnSquare> iterator = clash2ndRow.get(ci).iterator(); iterator.hasNext(); ) {
                        VirtualPieceOnSquare row2vPce = iterator.next();
                        ConditionalDistance row2vPceMinDist = row2vPce.getMinDistanceFromPiece();
                        if (row2vPceMinDist.movesFulfillConditions(moves) > 0
                                && row2vPceMinDist.distWhenAllConditionsFulfilled(colorFromColorIndex(ci)) == 1
                        ) {
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
                final int assassinCurrentVal = board.getBoardSquare(assassin.getMyPiecePos()).getvPiece(assassin.getPieceID()).getValue();
                resultIfTaken[exchangeCnt] = -assassinCurrentVal;
            }

            // if 2nd row candidates are still left after the 2nd turn (both colors have started), sort them in as normal late pieces
            if (firstTurnCI==1 || (firstTurnCI==0 && myPieceCIorNeg==1) )
                for (int ci = 0; ci <= 1; ci++) {
                    for ( VirtualPieceOnSquare row2vPce: clash2ndRow.get(ci) ){
                        int d = row2vPce.coverOrAttackDistanceNogofree();
                        if (d<=MAX_INTERESTING_NROF_HOPS) {
                            if (d==2 /* all already fulfilled here:
                                        && !vPce.getRawMinDistanceFromPiece().isUnconditional()
                                        && vPce instanceof VirtualSlidingPieceOnSquare
                                        && ((VirtualSlidingPieceOnSquare) vPce).fulfilledConditionsCouldMakeDistIs1() */
                                &&  row2vPce.getRawMinDistanceFromPiece().piecesMovesMayFulfillAllFromConds(
                                    coverageOfColorPerHops.get(0).get(CIWHITE),
                                    coverageOfColorPerHops.get(0).get(CIBLACK) )
                            ) {
                                putVPceIntoCoverageList(row2vPce, 1);  // remains in List for 2nd-row
                            }
                            else
                                putVPceIntoCoverageList(row2vPce, d);  // sort in as non-2nd-row
                        }
                    }
                    //TODO!: bug here: pces from 2nd row list, which are actually in "3rd row" are hre incorrectly sorted into later lists, although they are 2nd row still
                }
            for(int h=0; h<MAX_INTERESTING_NROF_HOPS; h++) {
                coverageOfColorPerHops.get(h).get(CIWHITE).sort(VirtualPieceOnSquare::compareTo); // for white
                coverageOfColorPerHops.get(h).get(CIBLACK).sort(VirtualPieceOnSquare::compareTo); // for black
            }
            // if nothing happened - i.e. no direct piece of firstTurnCI is there
            if (exchangeCnt==0) { // nothing happened - original piece stays untouched,
                for (VirtualPieceOnSquare vPce : vPieces) {
                    if ( vPce == null )
                        continue;
                    vPce.setClashContrib(0);
                    vPce.resetKillable();
                    if (vPce.getPieceID() == myPieceID) {
                        vPce.setRelEval(0);  // I have a good stand here, no threats, so no eval changes (just +/-1 to signal "ownership") :-)
                    }
                    else if (colorIndex(vPce.color()) == firstTurnCI) {
                        if ( vPce.getRawMinDistanceFromPiece().hasNoGo()
                                || (vPce.getMinDistanceFromPiece().dist() > MAX_INTERESTING_NROF_HOPS )
                        ) {
                            // I cannot really come here -> OLD: so a just enough bad value will result in a NoGo Flag - now almost 0 no NoGo needed
                            vPce.setRelEval(isWhite(vPce.color()) ? -1 : 1 ); // -EVAL_DELTAS_I_CARE_ABOUT : EVAL_DELTAS_I_CARE_ABOUT);  // ?problem with -1 : 1 );
                            //alternative: vPce.setRelEval(NOT_EVALUATED);
                        }
                        else  { // opponent comes here in the future to beat this piece
                            int sqPceTakeEval = resultIfTaken[0];   //(!isSquareEmpty() ? myPiece().getValue() : 0);
                            if ( noOppDefenders ) {  // ... and it is undefended
                                if ( isSquareEmpty() ) {
                                    if ( clashCandidates.get(colorIndex(vPce.color())).size() == 0 )
                                        vPce.setRelEval(0);  // it can go there but it would not be defended there
                                    else
                                        vPce.setRelEval(vPce.myPiece().isWhite() ? 2 : -2);  // it would be defended there
                                } else
                                    vPce.setRelEval(sqPceTakeEval);
                            }
                            else if ( isPawn(vPce.getPieceType())
                                        && isSquareEmpty()
                                        && fileOf(vPce.getMyPiecePos()) == fileOf(getMyPos())
                                        /* following idea was good, but test not necessary, because relEval needs to be recalculated anyway, whether a 2nd row piece covers it or not.
                                           (ideas was: straight pawn move could also trigger a 2nd row piece also the pawn was not part of the clash (because it was not attacking straight))
                                            && (clash2ndRow.get(CIWHITE).stream().filter(vp -> fileOf(vp.getMyPiecePos())==fileOf(getMyPos()) ).count() > 0
                                           || clash2ndRow.get(CIBLACK).stream().filter(vp -> fileOf(vp.getMyPiecePos())==fileOf(getMyPos()) ).count() > 0 )
                                         */
                            ) {
                                // it needs actually to be recalculated because a straight moving pawn was not part of the clash calculation but could be taken or be save here
                                old_updateRelEval(vPce);
                            }
                            else {
                                vPce.setRelEval(sqPceTakeEval - board.getBoardSquare(vPce.getMyPiecePos()).getvPiece(vPce.getPieceID()).getValue());  // it is defended, so I'd loose myself (with the value from where i stand now
                                vPce.setKillable();
                            }
                        }
                    } //else i.e. same color Piece
                }
                clashEvalResult = Integer.compare( clashCandidates.get(0).size(), clashCandidates.get(1).size() );
                clashMoves = new ArrayList<>(0);
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
                        resultFromHereOn = 0;  // it was not worth beating from here on, so we calc upwards that we did not take:
                        endOfClash = i - 1;    // and we remember that position - points "one to high", so it's 2 (==third index) if two pieces took something in the clash
                    }
                    resultIfTaken[i - 1] = resultFromHereOn;
                }

                if (myPieceCIorNeg != -1) {
                    clashEvalResult = resultFromHereOn;
                    clashMoves = moves.subList(0, endOfClash);
                }
                // derive relEvals for all Pieces from that
                for (VirtualPieceOnSquare vPce : vPieces)      // && colorIndex(vPce.color())==firstTurnCI
                    if (vPce != null
                            && vPce.getRawMinDistanceFromPiece().dist()<=MAX_INTERESTING_NROF_HOPS
                            && (myPieceCIorNeg != -1 || colorIndex(vPce.color()) == firstTurnCI)) {
                        vPce.setClashContrib(0);
                        if (vPce.getPieceID() == myPieceID) {
                            vPce.setRelEval(resultFromHereOn);  // If I stay, this will come out.
                            vPce.setKillable();
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
                                        board.getBoardSquare(getMyPos()).getvPiece(myPieceID),
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
                                if (endOfClash==0) {
                                    // however, this first clash-move would already not have been done
                                    vPce.setRelEval(resultIfTaken[1]  // so lets document=set the bad result here
                                            - ((myPiece() == null) // NOT, was attempted in v0.29z2, but not improving:  || isKing(myPiece().getPieceType()) ) // || isKing(myPiece().getPieceType()) ) //was .29z2 hmm, unclear maybe negative. Idea (seeming correct, but maybe it stopped vPce to move away?): unless it is a king which is calculated as moving away before...
                                            ? 0 : getvPiece(getPieceID()).getValue()));  // minus the piece standing there,
                                    // and as we know it would have taken, there must be a rease, whis is: it would die here:
                                    vPce.setKillable();
                                }
                                else {
                                    vPce.setRelEval(resultFromHereOn);
                                    // reduce/annihilate clashContribution as moving there is anyway reflected in the relEval and thus later in the direct move (and also as lost contribution in the pieces' other moves)
                                    clashContrib >>= 2;  // may be could even be =?.
                                    if (endOfClash>1) {
                                        // there are opponents left
                                        vPce.setKillable();
                                        vPce.setPriceToKill(resultIfTaken[1]);
                                    }
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
                                if ( evalIsOkForColByMin(resultIfTaken[vPceClashIndex], vPce.color())) {
                                    // vPce would be killed in the clash, so it can go here, but not go pn from here -
                                    // > so a bad value like -minus its value will result in a NoGo Flag, but not really say the
                                    // truth -> a clash calc (considering if this Pce would go either first or wait until
                                    // the end of the clash) is needed!
                                    vPce.setRelEval(-vPce.getValue());  // TODO: try usig old_updateelEval() here, it should also be negative and also express what happens if it goes here first
                                    vPce.setKillable();
                                    vPce.setPriceToKill(clashEvalResult);
                                }
                                else {
                                    //original code (see Todo-comment):
                                    //   if the continuation of the clash is negative anyway, this is taken as the relEval
                                    //   vPce.setRelEval(resultIfTaken[vPceClashIndex]);
                                    old_updateRelEval(vPce);  // alternative - however, it generates much more Nogos, although Piece could come here after hte clash - we need a "clash-fought-out" condition...
                                    // Idea: here I could save the piece from the kill by paying a price - (but have no field to store a life saving price, yet)
                                    vPce.setKillable();  // for now let's continue to assume reasonably carried out clashes, which would kill the vPce;
                                }
                            }
                            else if (vPceClashIndex == endOfClash-1) {  // was last in clash
                                //TODO!! - 2 different conflicting cases - a result from after the clash (resultIfTaken[vPceClashIndex + 1])
                                //  indicates that the Piece can go+be here (after the clash) and thus has no NoGo and can continue
                                //    -> original code: vPce.setRelEval(resultIfTaken[vPceClashIndex + 1]); // or: checkmateEval(vPce.color()));  // or: resultFromHereOn); // or?: willDie-Flag + checkmateEval(vPce.color()));  // vPce would be killed in the clash, so it can go here, but not go pn from here -> so a bad value like checkmate will result in a NoGo Flag
                                //  However: Such a positive value would also indicate (for the move selection) that it could go
                                //  here immediately - which is not true, so a Piece needs to be able to distinguish these 2 cases.
                                //let the old_ decide, if it is killable
                                old_updateRelEval(vPce);  // alternative - however, it generates much more Nogos, although Piece could come here after hte clash - we need a "clash-fought-out" condition...
                                final int lastTakerValueDelta = vPce.getValue() - board.getBoardSquare(vPce.getMyPiecePos()).getvPiece(vPce.getPieceID()).getValue();
                                if (firstAssassin != null)
                                    firstAssassin.addRelEval( lastTakerValueDelta );  // add benefit (or fee) for position change
                            }
                            else {
                                // check if right at the end of the clash, this vPce could have taken instead
                                // todo: it is treated to simple here, if clash was only fought out half way.
                                // Todo: is incorrect, if current vpce origins from the "2nd row", while its enabling piece
                                //  was the last one.
                                // TODO!: is also incorrect if vPce is the one activating a 2nd-row-piece of the opponent
                                int nextOpponentAt = vPceFoundAt + (colorIndex(vPce.color()) == firstTurnCI ? 0 : 1);
                                if (nextOpponentAt >= clashCandidates.get(colorIndex(!vPce.color())).size()) {
                                    // no more opponents left, so yes we can go there - but only after the clash & if
                                    // it actually took place
                                    // let the old_ decide, if it is killable
                                    old_updateRelEval(vPce);  //see todo above...
                                    /* not here, only for the one typical end of clash - otherwise the benefits sum up here - best would be maximum...
                                    final int lastTakerValueDelta = vPce.getValue() - board.getBoardSquare(vPce.getMyPiecePos()).getvPiece(vPce.getPieceID()).getValue();
                                    if (firstAssassin!=null)
                                        firstAssassin.addRelEval( lastTakerValueDelta );  // add benefit (or fee) for position change
                                     */
                                }
                                else if (vPce.myPiece().isWhite() && vPce.getValue() - EVAL_DELTAS_I_CARE_ABOUT >= -clashCandidates.get(colorIndex(!vPce.color())).get(nextOpponentAt).myPiece().getValue()
                                        || !vPce.myPiece().isWhite() && vPce.getValue() + EVAL_DELTAS_I_CARE_ABOUT <= -clashCandidates.get(colorIndex(!vPce.color())).get(nextOpponentAt).myPiece().getValue()
                                ) {
                                    // i am more valuable than the next opponent
                                    vPce.setRelEval(-vPce.getValue());  // vPce would be killed in the clash, so it can go here, but not go pn from here -> so a bad value like checkmate will result in a NoGo Flag
                                    vPce.setKillable();
                                }
                                else {
                                    // i am less valuable than the next opponent, but the clash was not continue here, so it'd be bad to come here
                                    old_updateRelEval(vPce);  //see todo above...
                                    //vPce.setRelEval(0);
                                }
                            }
                            if (DEBUGMSG_MOVEEVAL && abs(clashContrib)>DEBUGMSG_MOVEEVALTHRESHOLD
                                    && board.currentDistanceCalcLimit()==MAX_INTERESTING_NROF_HOPS)  // actually we do not know at what level it is called the final time, overriding the prev. calculations (which are not well sorted out yet)
                                debugPrintln(DEBUGMSG_MOVEEVAL, "Adding ClashContrib of " + clashContrib + " to " + vPce + ".");
                            vPce.addClashContrib(clashContrib);
                        } else {
                            // vPce is not in the clash candidates
                            // TODO!! implementation of this case still needed - simulate, if this vPce would come here?
                            // for now set to 0 if no opponents or use old evaluation for the simulation
                            if (endOfClash == exchangeCnt // clash was beaten until the very end
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
        //whites.addAll(coverageOfColorPerHops.get(1).get(CIWHITE));
        //blacks.addAll(coverageOfColorPerHops.get(1).get(CIBLACK));
        List<VirtualPieceOnSquare> whiteOthers = new ArrayList<>(coverageOfColorPerHops.get(1).get(CIWHITE)); //coverageOfColorPerHops.get(2).get(CIWHITE));
        List<VirtualPieceOnSquare> blackOthers = new ArrayList<>(coverageOfColorPerHops.get(1).get(CIBLACK)); //coverageOfColorPerHops.get(2).get(CIBLACK));
        boolean fuzzedWithKingInList = false;
        VirtualPieceOnSquare currentVPceOnSquare = null;
        if (myPieceID!=NO_PIECE_ID) {
            currentVPceOnSquare = getvPiece(myPieceID);
            ConditionalDistance rmd = evalVPce.getRawMinDistanceFromPiece();
            int dist = rmd.dist();
            if (colorOfPieceType(currentVPceOnSquare.getPieceType())==colorOfPieceType(evalVPce.getPieceType())
                || (isPawn(evalVPce.getPieceType())
                    && fileOf(getMyPos())==fileOf(evalVPce.getMyPos()) && (dist==1 || (dist-rmd.nrOfConditions())==1) )
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

        /*
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
         */

        List<Move> moves = new ArrayList<>();
        moves.add(new Move( evalVPce.getMyPiecePos(), getMyPos()));

        if ( isSquareEmpty()
                && isPawn(evalVPce.getPieceType())
                && fileOf(evalVPce.getMyPiecePos())!=fileOf(getMyPos())  // only for beating by pawn scenarios -
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
                        whiteOthers, blackOthers,
                        moves);
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

    // todo: add similar check for normal forks  (compare vs. todays method)
    void evalCheckingForks() {
        for (VirtualPieceOnSquare vPce : getVPieces()) {
            if (vPce == null
                    || isKing(vPce.getPieceType())
                    // no || vPce.getRawMinDistanceFromPiece().hasNoGo() - we calculate anyway and give contribution to those covering this square
            )
                continue;

            // todo: reward pieces causing nogo to continue to to so!
            //  e.g.: "r1bqk2r/p1pp1ppp/4p3/8/1n1P4/NP6/PB4PP/R3KQNR w KQkq - 0 13" NOT Na1c3

            int inFutureLevel = vPce.getAttackingFutureLevelPlusOne()-1;
            if (inFutureLevel < 0)
                inFutureLevel = 0;
            if (inFutureLevel >= MAX_INTERESTING_NROF_HOPS           // out of interest
                    || !vPce.isCheckGiving())                         // no check, no check giving fork...
                continue;
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL, " Evaluating checking fork @" + inFutureLevel + " on square " + squareName(getMyPos()) + " for " + vPce + ": ");
            int kingId = board.getKingId(vPce.myOpponentsColor());
            // find best neighbour benefit besides king-check
            //VirtualPieceOnSquare bestNeighbour = null;
            int maxFork = 0;
            VirtualPieceOnSquare forkerAtBestNeighbourVPce = null;
            boolean forkIsDoable = !vPce.getMinDistanceFromPiece().hasNoGo()
                                    && !vPce.isKillableReasonably()
                                    && vPce.getMinDistanceFromPiece().isUnconditional();

            // additional bonus, not for the fork, but for taking with Abzug at the same time
            final ConditionalDistance kingHereRmd = getvPiece(kingId).getRawMinDistanceFromPiece();
            if (vPce.hasAbzugChecker()
                    && myPiece() != null  // beating something
                    && myPiece().color() != vPce.color()
                    && !(kingHereRmd.dist() == 1   // the king cannot beat back himself
                         && countDirectAttacksWithColor(vPce.color()) <= 1
                         && !extraCoverageOfKingPinnedPiece(vPce.color()) )
            ) {
                /*if ( kingHereRmd.dist() == 1        // TODO:REMOVE - was just here to find example cases
                        && countDirectAttacksWithColor(vPce.color()) <= 1
                        && extraCoverageOfKingPinnedPiece(vPce.color()) ) {
                    board.internalErrorPrintln("INFO: extraCoverageOfKingPinnedPiece hinders king from taking back forker " + vPce+".");
                }*/

                int abzugCaptureBonus = -myPiece().getValue();
                if (evalIsOkForColByMin(abzugCaptureBonus, vPce.color(),-EVAL_DELTAS_I_CARE_ABOUT)) {
                    // in the best case we fully get the value of the taken piece here and can move away safely afterwards - however,
                    // this is of course incorrect, if blocking the check is itself a blocking move or if there is no safe way back...
                    abzugCaptureBonus -= (vPce.getRelEvalOrZero()-(vPce.getRelEvalOrZero()>>2));
                    abzugCaptureBonus -= abzugCaptureBonus>>2; // *0.87
                    if (!vPce.isRealChecker()) //not a double check
                        abzugCaptureBonus >>= 1; // *0.5
                    else
                        debugPrintln(DEBUGMSG_MOVEEVAL,
//System.err.print(
                            "Double check: ");
                    if (DEBUGMSG_MOVEEVAL && abs(abzugCaptureBonus)>DEBUGMSG_MOVEEVALTHRESHOLD)
                        debugPrintln(DEBUGMSG_MOVEEVAL,
//System.err.println(
                            "Abzug-Check and Take Bonus " +abzugCaptureBonus+"@0 (compensated by -"+vPce.getRelEvalOrZero()+") for " + vPce + " with checker "+vPce.getAbzugChecker()
                                +" on board "+ board.getBoardFEN() +". ");
                    vPce.addRawChance(abzugCaptureBonus,0,getMyPos());
                }
            }

            //TODO!: this only fully works for non-sliding pieces ... for sliding (or then for all) pieces it needs
            // a different approach of handing all addChances also to the predecessor squares, esp. if they
            // are checking -> then algo can even handle normal forks!

            // vPce gives check here (directly or indirectly), what else does it threaten from here?:
            for (VirtualPieceOnSquare atNeighbour : vPce.getNeighbours()) {
                if (atNeighbour == null
                        || atNeighbour.getMyPos() == board.getKingPos(vPce.myOpponentsColor())
                        || atNeighbour.getMyPos() == vPce.getMyPiecePos()  // cannot fork backwards to where I came from...
                ) {
                    continue;
                }
                Square neigbourSq = board.getBoardSquare(atNeighbour.getMyPos());
                int chanceAtN = atNeighbour.getRelEvalOrZero(); //getBestChanceOnLevel(inFutureLevel);
                int forkedPceId = neigbourSq.getPieceID();
                if (forkedPceId != NO_PIECE_ID) {
                    //ConditionalDistance forkedPieceRmd2forkingSq = getvPiece(forkedPceId).getRawMinDistanceFromPiece();
                    //if (forkedPieceRmd2forkingSq.dist() == 1 && forkedPieceRmd2forkingSq.isUnconditional()) {
                    if ( getvPiece(forkedPceId).coverOrAttackDistance() == 1) {
                        //TODO: is there a better way to deal with forked piece itself covers the forking square, this could make the fork impossible (or not)
                        chanceAtN = minFor(chanceAtN,
                                           -(vPce.getValue()+getvPiece(forkedPceId).getValue()), vPce.color());
                    }
                }
                ConditionalDistance kingRmd = neigbourSq.getvPiece(kingId).getRawMinDistanceFromPiece();
                if (kingRmd.dist() == 2
                        && !kingRmd.hasNoGo()
                        && kingRmd.isUnconditional()
                        && neigbourSq.countDirectAttacksWithout2ndRowWithColor(vPce.color()) == 0
                        && neigbourSq.countDirectAttacksWithout2ndRowWithColor(vPce.myOpponentsColor()) == 0
                ) {
                    // king can reach the forked piece and this is not attacked or covered by any other piece,
                    // so the king can cover while moving out of check and take back
                    final int forkingPceVal = vPce.myPiece().getValue();
                    chanceAtN -= forkingPceVal - (forkingPceVal>>3);  // *0,87   //H19: - not +!
                }
                if ( !evalIsOkForColByMin(chanceAtN, vPce.color(), -EVAL_DELTAS_I_CARE_ABOUT)
                     && board.hasPieceOfColorAt(vPce.myOpponentsColor(),atNeighbour.getMyPos()))
                    continue; // there is an opponent, but even beating him is not benefical, so we do not need to continue with benefits or warnings
                if (isBetterThenFor(chanceAtN, maxFork, vPce.color())) {
                    maxFork = chanceAtN;
                    forkerAtBestNeighbourVPce = atNeighbour;
                }
                // additionally warn/fee other pieces from going here
                // solves the bug "5r2/6k1/1p1N2P1/p3n3/2P4p/1P2P3/P5RK/8 w - - 5 45, NOT g2g5"//
                // BUT makes test games slightly worse - even with just warning = +/-EVAL_TENTH
                //if ( !board.hasPieceOfColorAt(vPce.myOpponentsColor(), atNeighbour.getMyPos()) ) { // opponent cannot go there, if he is already there...
                if (DEBUGMSG_MOVEEVAL && abs(chanceAtN)>DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL," Found checking fork chance " + chanceAtN +"@"+ inFutureLevel
                            + " for " + vPce + " at " + atNeighbour + ".");
                for (VirtualPieceOnSquare opponentAtForkingDanger :neigbourSq.getVPieces()) {
                    if (opponentAtForkingDanger == null
                            || opponentAtForkingDanger.color() == vPce.color()               // not forking myself :-)
                            || isKing(opponentAtForkingDanger.getPieceType())
                            || opponentAtForkingDanger.getRawMinDistanceFromPiece().dist() > 1    // too far away, no need to warn
                            || (opponentAtForkingDanger.getRawMinDistanceFromPiece().dist()      // it could go there, but would not fall into trap, but even cover the forking square
                            - getvPiece(opponentAtForkingDanger.getPieceID()).getRawMinDistanceFromPiece().dist() == -1)
               /* makes it worse:         || ( getvPiece(opponentAtForkingDanger.getPieceID()).getRawMinDistanceFromPiece().dist() == 1  // similar, but piece to be forked already covers the square and will even after moving to forking square
                                && getvPiece(opponentAtForkingDanger.getPieceID()).getRawMinDistanceFromPiece().isUnconditional()
                                && dirsAreOnSameAxis(calcDirFromTo(opponentAtForkingDanger.getMyPiecePos(),opponentAtForkingDanger.getMyPos()),
                                                     calcDirFromTo(opponentAtForkingDanger.getMyPiecePos(), getMyPos()) ) ) */
                            || isBetweenFromAndTo(opponentAtForkingDanger.getMyPos(),
                            getMyPos(),
                            board.getKingPos(opponentAtForkingDanger.color())) // unless piece here would block the check - line necessary? - might not even be possible
                            || ( abs(vPce.getValue()) >= abs(opponentAtForkingDanger.getValue()-EVAL_TENTH )  // cannot fork sam piece type (TODO!: extend to queen cannot for b or t from their attacking dir)
                                  && colorlessPieceType(atNeighbour.getPieceType()) == colorlessPieceType(opponentAtForkingDanger.getPieceType()) )
                    )
                        continue;
                    if (opponentAtForkingDanger.getRawMinDistanceFromPiece().dist() == 0) {
                        // already there, must be warned to move away)
                        int runAwayBenefit = chanceAtN >> (2 + inFutureLevel);
                        if (DEBUGMSG_MOVEEVAL && abs(runAwayBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit of " + runAwayBenefit + "@" + inFutureLevel
                                    + " for moving out of forking danger with " + opponentAtForkingDanger + ".");
                        opponentAtForkingDanger.addMoveAwayChance(runAwayBenefit, inFutureLevel, getMyPos());
                        //opponentAtForkingDanger.myPiece().addMoveAwayChance2AllMovesUnlessToBetween(runAwayBenefit, inFutureLevel, NOWHERE, NOWHERE, true, getMyPos());
                    }
                    else {
                        //int warnFutureLevel = opponentAtForkingDanger.getAttackingFutureLevelPlusOne() - 1;
                        int warning = -(opponentAtForkingDanger.getValue() + (atNeighbour.getValue() >> 3)); // estimation, forking piece might die or not... TODO: should be calculated more precisely as a real clashResult
                        if (!forkIsDoable)
                            warning >>= 2;
                        if (!evalIsOkForColByMin(warning, opponentAtForkingDanger.color(), -1)) {
                            warning >>= 1; // (isWhite(opponentAtForkingDanger.color()) ? -EVAL_TENTH : EVAL_TENTH); //warning>>2;
                            if (DEBUGMSG_MOVEEVAL && abs(warning) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                debugPrintln(DEBUGMSG_MOVEEVAL, " Warning of " + warning + "@" + inFutureLevel
                                        + " not to come here due to potential checking fork by " + vPce
                                        + " for " + opponentAtForkingDanger + ".");
                            opponentAtForkingDanger.addRawChance(warning, inFutureLevel, atNeighbour.getMyPos()); //, target: atNeighbour.getMyPos()
                        }
                    }
                }
            //}
            }
            int forkBenefit = maxFork - (maxFork >> 4);  // reduce fork by 6%
            VirtualPieceOnSquare realChecker;
            if (!vPce.hasAbzugChecker()) {
                realChecker = vPce;

                if (evalIsOkForColByMin(forkBenefit, vPce.color(), -EVAL_DELTAS_I_CARE_ABOUT)) {
                    int protectionBenefit = -(forkBenefit >> 2);
                    // add Chance to forking move
                    if (forkIsDoable) {
                        if (DEBUGMSG_MOVEEVAL && abs(forkBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " Detected doable checking fork with max benefit of "
                                    + forkBenefit + "@" + inFutureLevel + " for " + vPce + ".");
                        vPce.addChance(forkBenefit, inFutureLevel);
                    } else {
                        if (DEBUGMSG_MOVEEVAL && abs((forkBenefit >> 3)) > DEBUGMSG_MOVEEVALTHRESHOLD)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " Detected not (yet) doable checking fork. Giving warning/benefit of "
                                    + (forkBenefit >> 3) + "@" + inFutureLevel + " on square " + squareName(getMyPos()) + ".");
                        vPce.addChance((forkBenefit >> 2), inFutureLevel);
                    }
                    // warn others to cover forking square
                    for (VirtualPieceOnSquare opponentAtForkingSquare : getVPieces()) {
                        if (opponentAtForkingSquare == null
                                || opponentAtForkingSquare.color() == vPce.color()               // not hindering myself from forking
                        )
                            continue;
                        if (opponentAtForkingSquare.getRawMinDistanceFromPiece().dist() == 0
                                && !isPawn(opponentAtForkingSquare.getPieceID())) {
                            // already there, but it can protect additionally by moving away :-)
                            if (DEBUGMSG_MOVEEVAL && abs(protectionBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit protecting by moving away with benefit of " + protectionBenefit + "@" + inFutureLevel + " for " + opponentAtForkingSquare + ".");
                            opponentAtForkingSquare.addMoveAwayChance(protectionBenefit, inFutureLevel, getMyPos());
                        }
                        else if (opponentAtForkingSquare.coverOrAttackDistance() == 1 // getRawMinDistanceFromPiece().dist() == 1
                                && inFutureLevel == 0  // we do not have future contributions implemented yet, so only for immediate threats
                        ) {
                            // already protecting
                            if (DEBUGMSG_MOVEEVAL && abs(protectionBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                debugPrintln(DEBUGMSG_MOVEEVAL, " Detected contribution of " + protectionBenefit + " for " + opponentAtForkingSquare + " for protecting against fork.");
                            if (countDirectAttacksWithColor(opponentAtForkingSquare.color()) == 1) {
                                // the last one to protect the square!
/*System.err.println("#### interesting case of giving contrib of " + forkerAtBestNeighbourVPce.getRelEvalOrZero() + "/"
                                        + (-(forkBenefit - (forkBenefit>>2))) + " for " + opponentAtForkingSquare
                                        + " for being the last to protect against check fork of "
                                        +  forkBenefit + "@" + inFutureLevel
                                        + " by " + forkerAtBestNeighbourVPce
                                        + " on board " + board.getBoardFEN() + " ."  );*/
                                opponentAtForkingSquare.addClashContrib(protectionBenefit);
                            } else
                                opponentAtForkingSquare.addClashContrib(protectionBenefit>>1);
                            if (opponentAtForkingSquare.getRawMinDistanceFromPiece().hasExactlyOneFromToAnywhereCondition()) {
                                int fromCond = opponentAtForkingSquare.getRawMinDistanceFromPiece().getFromCond(0);
                                if (fromCond >= 0) {
                                    ChessPiece inbetweener = board.getPieceAt(fromCond);
                                    if (inbetweener != null) {
                                        if (DEBUGMSG_MOVEEVAL && abs(protectionBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                            debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit/Malus of " + protectionBenefit + "@" + inFutureLevel
                                                    + " for check-fork enabling by moving " + inbetweener + " out of the way.");
                                        inbetweener.addMoveAwayChance2AllMovesUnlessToBetween(
                                                protectionBenefit, inFutureLevel,
                                                getMyPos(),  // todo: frompos should not be included here
                                                opponentAtForkingSquare.getMyPiecePos(), false,
                                                opponentAtForkingSquare.getMyPos());
                                    }
                                }
                            }
                        }
                        else if (opponentAtForkingSquare.getRawMinDistanceFromPiece().dist() == 2) {   // >2 would be too far away, no need to warn
                            int warnFutureLevel = max(inFutureLevel, opponentAtForkingSquare.getAttackingFutureLevelPlusOne() - 1);
                            if (DEBUGMSG_MOVEEVAL && abs(protectionBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                debugPrintln(DEBUGMSG_MOVEEVAL, " Motivation of " + protectionBenefit + "@" + warnFutureLevel
                                        + " for " + opponentAtForkingSquare + " to protect potential checking fork on square " + squareName(getMyPos()) + ".");
                            opponentAtForkingSquare.addRawChance(protectionBenefit, warnFutureLevel, getMyPos()); //, target: neighbour.getMyPos()
                        }
                    }
                }
            } else {
                // was indirect check = Abzugschach
                realChecker = vPce.getAbzugChecker();
                if (forkIsDoable) {
                    if (DEBUGMSG_MOVEEVAL && abs(forkBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " Detected doable Abzugschach with additional threat with max benefit of "
                                + forkBenefit + "@" + inFutureLevel + " for " + vPce + ".");
                    vPce.addRawChance(forkBenefit, inFutureLevel, getMyPos());  // addRaw, as counter measures do not apply
                } else {
                    // not doable, but as there is check it could be possibly anyway...
                    forkBenefit >>= 1;
                    if (DEBUGMSG_MOVEEVAL && abs(forkBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " Detected seemingly not doable threat, but as it is Abzugschach: benefit of " + forkBenefit + "@" + inFutureLevel + " for " + vPce + ".");
                    vPce.addRawChance((forkBenefit >> 3), inFutureLevel, getMyPos());
                }
            }
            if (inFutureLevel==0 && (forkIsDoable || vPce.hasAbzugChecker()) ) {
                // if it gets tough also consider to move king out of the way
                int moveKingAwayBenefit = -(forkBenefit>>2);  // * (ChessBoard.engineP1()+10))/40;   // -(forkBenefit >> 2);
                if (!vPce.hasAbzugChecker())
                    moveKingAwayBenefit >>= 2;
                ChessPiece king = board.getPiece(kingId);
                if (DEBUGMSG_MOVEEVAL && abs(forkBenefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " Warning of "+moveKingAwayBenefit+"@0 to " +king
                                    +" to move away from " + vPce + ".");
                king.addMoveAwayChance2AllMovesUnlessToBetween(
                        moveKingAwayBenefit, inFutureLevel,
                        king.getPos(), realChecker.getMyPiecePos(), true,
                        king.getPos() );
            }
        }
    }

    void calcFutureClashEval() {
        // note: clash-lists must already be updated
        // TODO - make every calculation here change-dependent, not reset and recalc all...
        if (isSquareEmpty()  // bonus for taking control of empty squares is treated elsewhere
               // removed with 47v1 + v3ff, now slightly better, but partly slighly worse...:  || isKing(myPieceType())   // king is also treated differently
               // 47v2 tries intermediate, but is worse than v1: || ( isKing(myPieceType()) && myPiece().color() == board.getTurnCol() )
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
        // Todo!!: check, if here the get(1) should not better get into whiteOthers in the clash-call below!!
        List<VirtualPieceOnSquare> whiteOthers = new ArrayList<>(coverageOfColorPerHops.get(1).get(CIWHITE)); //coverageOfColorPerHops.get(2).get(CIWHITE));
        List<VirtualPieceOnSquare> blackOthers = new ArrayList<>(coverageOfColorPerHops.get(1).get(CIBLACK)); //coverageOfColorPerHops.get(2).get(CIBLACK));
        //whites.addAll(coverageOfColorPerHops.get(1).get(CIWHITE));
        //blacks.addAll(coverageOfColorPerHops.get(1).get(CIBLACK));
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
                        && additionalAttacker.getRawMinDistanceFromPiece().dist()<2
                        && !( additionalAttacker.getRawMinDistanceFromPiece().dist()==1
                                && additionalAttacker.getRawMinDistanceFromPiece().hasExactlyOneFromToAnywhereCondition() )
                ) {
                    // was: skip this attacker, it must be a non2nd-row attacker with D==1 and a condition, so it cannot be benefited to come closer really...)
                    // but: if d==1 and fromCond with an opponent, then this is also like a d==2 (by taking)
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
                    whiteOthers, blackOthers, null);
                   // whiteMoreAttackers, blackMoreAttackers, null);

            // add new chances
            int benefit = 0;
            //ConditionalDistance rmd = additionalAttacker.getRawMinDistanceFromPiece();
            /*int futureLevel = additionalAttacker.getStdFutureLevel() -2
                    - ((currentVPceOnSquare.color() == additionalAttacker.color() ) //&& additionalAttacker.color()==board.getTurnCol()
                    ? 1 : 0)  // covering happens 1 step faster than beating if, it is my turn  / Todo: do we want dependency on who's turn it is here?
                    + (rmd.isUnconditional() ? 0 : 1)  // TODO:Shouldn't this be nrOfConditions()?
                    ; */
            int futureLevel = additionalAttacker.getAttackingFutureLevelPlusOne() -1
                    - (currentVPceOnSquare.color() == additionalAttacker.color()
                           && additionalAttacker.color()==board.getTurnCol()  // results are much worse without this line!
                        ? 1 : 0)  // covering happens 1 step faster than beating if, it is my turn
                    //+ (rmd.isUnconditional() ? 0 : 1)  // TODO:Shouldn't this be nrOfConditions()?
                    + (multipleAdditions>1? multipleAdditions-1 : 0) // this benefit can only be achieved after the previous additional attackers have also come into play
                    ; //+ ( isWhite(additionalAttacker.color()) ? wNext-1 : bNext-1 ); // if several attackers need to be brought in, obey the order
            /*if (nr>1 && (isWhite(additionalAttacker.color()) && futureClashResults[nr] > futureClashResults[nr-2]
                         || (isBlack(additionalAttacker.color()) && futureClashResults[nr] < futureClashResults[nr-2]) )
            ) {
                debugPrintln(DEBUGMSG_MOVEEVAL, "(reducing benefit as it was already " + futureClashResults[nr-2] + "before.");
                clashContribution += futureClashResults[nr - 2]-futureClashResults[nr]; // account only the additional benefit compared to the last step of the same color (Todo: takes wrong res[-2] if there was no opponent color piece left
            }*/

            int clashContribution = futureClashResults[nr] - clashEval();
            int relEval = adjustBenefitToCircumstances(additionalAttacker, additionalAttacker.getRelEvalOrZero()) >> 1;  // /2 is best according to testrow in 0.48h44i
            /*if ( isKing(myPieceType()) ) {
                // do not overrate attackers to the King -> real check benefits are evaluated in separate methods.
                clashContribution >>= 2;
                relEval >>= 2;
            }*/

            if ( isKing(additionalAttacker.getPieceType() ) ) {
                benefit = calcKingAttacksBenefit(additionalAttacker);
            }
            else if (evalIsOkForColByMin( clashContribution,
                    additionalAttacker.color(), -EVAL_DELTAS_I_CARE_ABOUT)) {

                benefit = adjustBenefitToCircumstances(additionalAttacker, clashContribution);

                if (DEBUGMSG_MOVEEVAL && abs(benefit)>4)
                     debugPrintln(DEBUGMSG_MOVEEVAL," Benefit " + benefit + " for close future chances on square "
                             + squareName(getMyPos())+" with " + additionalAttacker + ": " + futureClashResults[nr] + "-" + clashEval());
                /* seems logical, but does not improve, but worsens the eval in all SF11+14+selfv26 test games
                if (multipleAdditions>1 && preparer[colorIndex(turn)].size()==0) {
                    // here the additional attacker is a multiple one at the end after the opponent ran out of defenders
                    // but also, the preparer list is empty meaning there was already a preparer + one after that awarded the preparer
                    // so let's keep it down a little her with giving the same bonus again...
                    benefit -= benefit>>2;  // *0.75
                } */
                for ( VirtualPieceOnSquare preparerVPce : preparer[colorIndex(turn)] ) {
                    int preparerBenefit = benefit; // 47u22-47u66tried: >>2;
                    if (preparerVPce.getRawMinDistanceFromPiece().hasNoGo())
                        preparerBenefit >>= 3;  // 47u22-47u66tried: >>= 2;
                    else  // 47u22-47u66
                        benefit -= (benefit >> 2 + benefit >> 3);  // >>= 1; // we are starting with the first/cheaper ones, so this brings less and less benefit to the later ones...
                    if (DEBUGMSG_MOVEEVAL && abs(preparerBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                        debugPrintln(DEBUGMSG_MOVEEVAL, ", but additionally give benefit " + preparerBenefit + "@" + futureLevel + " for other piece that should go first towards  "
                                + squareName(getMyPos()) + ": " + preparerVPce + ".");
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
                         || countDirectAttacksWithColor(additionalAttacker.color())
                            <= countDirectAttacksWithColor(opponentColor(additionalAttacker.color()))) {
                    if (additionalAttacker.color() != currentVPceOnSquare.color()) {
                        //benefit = additionalAttacker.getRelEvalOrZero() >> 2;
                        //if (!evalIsOkForColByMin(benefit, additionalAttacker.color(),-EVAL_TENTH)) {
                            // should always be entered - should be a rare case that the relEval is really positive here for the additionaAttacker, but still coming closer is a little benefitial...
                            benefit = (((additionalAttacker.myPiece().isWhite() ? EVAL_TENTH : -EVAL_TENTH) << 2)
                                        + additionalAttacker.myPiece().reverseBaseEval()) >> 5;
                            //benefit -= benefit>>2;
                        //}
                    }
                    /*if (additionalAttacker.color() != currentVPceOnSquare.color()) {
                        // still a little attacking chance improvement if a piece comes closer to an enemy, right?
                        benefit = additionalAttacker.getRelEvalOrZero() >> 2;
                        if ( myPiece().canMoveAwayPositively()
                                && myPiece().getBestMoveTarget() != additionalAttacker.getMyPiecePos() // if the best moves is to where the attacker comes from, then we actually do not know if it has another good move, lets assume not and attack anyway...
                        ) {
                            debugPrintln(DEBUGMSG_MOVEEVAL, "(hmmm, reducing benefit for trying to additionally come closer to in sometime attack piece " + myPiece() + " at " + squareName(getMyPos())
                                    + " with benefit " + benefit + " by " + additionalAttacker + " although, it has a good move (" + myPiece().getBestMoveRelEval()
                                    +  ") ");
                            benefit -= benefit >> 2;  // *0,75
                        }
                    }  */
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
                    /*if (myPiece().color() == additionalAttacker.color()) {
                        if (!additionalAttacker.canCoverFromSavePlace()) {
                            benefit >>= 2;
                            debugPrint(DEBUGMSG_MOVEEVAL,"(reducing additional covering benefit, as there is no save place for covering:) ");
                        }
                    } */
                    benefit = adjustBenefitToCircumstances(additionalAttacker, benefit);
                }
                preparer[colorIndex(turn)].add(additionalAttacker); // keep it for later, it could be a preparer for a later chance
            }

            if ( prevAddAttacker!=null && prevAddAttacker.color()!= myPiece().color()  // 47u22-47u66: prev was an attack not a defence
                    && nr>1
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
                debugPrintln(DEBUGMSG_MOVEEVAL, "(Alert for " + colorName(myPiece().color())
                        + ": cannot save " + squareName(getMyPos()) + " after additional attack of "
                        + prevAddAttacker+", so more benefit "+moreBenefit+"@"+prevFutureLevel+" for the latter.) ");
                prevAddAttacker.addChance(moreBenefit,prevFutureLevel );
                //the attacker looses most of its benefit (it was already "used")
                benefit = additionalAttacker.getRelEvalOrZero() >> 2;
                if (!evalIsOkForColByMin(benefit, additionalAttacker.color(),-EVAL_TENTH))
                    benefit = 0;  // we do not want negative benefits here.
            }

            /*if (isKing(additionalAttacker.getPieceType()))
                benefit >>= 1;  // /2 for kings */
            // anyway calculated further down: benefit += getKingAreaBenefit(additionalAttacker)>>1;
            //TODO: +countHelpNeededFromColorExceptOnPos is incorrect if some firstMovesToHere hava more conditions than others.

            if (additionalAttacker.getRawMinDistanceFromPiece().hasNoGo())
                benefit >>= 3;
            //TODO: switch on to reduce benefit for high futureLevel  (spe. question here:  does it even make sense, is fl ever >1 here?)
            // if (futureLevel>1)
            //    benefit /= futureLevel;  // in Future the benefit is not taking the piece, but scariying it away
            int finalFL = futureLevel;  // 0.48h44o
            if ( additionalAttacker.color() == myPiece().color()         // it is a defence
                && abs(clashEval()) <= (EVAL_DELTAS_I_CARE_ABOUT<<1) )   // but it is not urgent
                finalFL++;
            if ( DEBUGMSG_MOVEEVAL && abs(benefit)>4)
                debugPrintln(DEBUGMSG_MOVEEVAL," Final benefit: max of " + benefit + "@"+finalFL
                        + " and relEval " + relEval +"@"+finalFL
                        +" for close" + (finalFL>futureLevel?", but not urgent":" ") + " future chances on square "+ squareName(getMyPos())+" with " + additionalAttacker + ".");

            additionalAttacker.addBetterChance(benefit, finalFL, relEval, additionalAttacker.getAttackingFutureLevelPlusOne()-1);

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
        //debugPrintln(true, " END " + squareName(getMyPos())+ ".");

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
                //if (futureLevel>2)  // reduce benefit for high futureLevel
                //    benefit /= (futureLevel-1);  // in Future the benefit is not taking the piece, but scaring it away
                int relEval = adjustBenefitToCircumstances(additionalFutureAttacker, additionalFutureAttacker.getRelEvalOrZero()) >> 1;  // /2 is best according to testrow in 0.48h44i
                /*if ( isKing(myPieceType()) ) // do not overrate attackers to the King -> real check benefits are evaluated in separate methods.
                    relEval >>= 3;*/
                if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit of max of " + benefit + "@" + futureLevel
                            + " and relEval " + relEval +"@"+futureLevel
                            + " for later future chances on square " + squareName(getMyPos()) + " with " + additionalFutureAttacker + ".");

                benefit = additionalFutureAttacker.addBetterChance(benefit, futureLevel, relEval, additionalFutureAttacker.getAttackingFutureLevelPlusOne()-1);

                if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, ".");
            }
            hopDistance++;
        }
    }

    /** expects already correctly signed benefit and adjusts it accourding to questions like:
     *  - is the square safe, from where i want to cover something?
     *  - doas an attacked piece want to move away anyway?
     * @param attacker
     * @param benefit
     * @return corrected benefit
     */
    private int adjustBenefitToCircumstances(final VirtualPieceOnSquare attacker, int benefit) {
        // tried long time ago, but then it was worse than without... todo: check now.
        if ( myPiece().color() != attacker.color()
                && attacker.coverOrAttackDistance() >= 2
                && abs(attacker.getValue()) >= abs(myPiece().getValue()-EVAL_TENTH )
                && attacker.attackTowardsPosMayFallVictimToSelfDefence()
        ) {
            int takeBack = -attacker.getValue() - myPiece().getValue(); // at least loosing attacked piece and attacker
            benefit = minFor(benefit, takeBack, myPiece().color());
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL, "(changing benefit for trying to additionally attack piece " + myPiece() + " at " + squareName(getMyPos())
                    + " with benefit " + benefit + " by " + attacker + " because it can not approach safely without being beaten itself.)");
            //benefit -= benefit>>4;
        }
        /*else {
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL, "(good, attacked piece " + myPiece()
                        + " cannot reasonably strike " + attacker +" on it's approach.)");
        }*/

        if ( abs(benefit)>EVAL_DELTAS_I_CARE_ABOUT
                && myPiece().color() != attacker.color()
                && myPiece().canMoveAwayPositively()
                && myPiece().getBestMoveTarget() != attacker.getMyPiecePos() // if the best moves is to where the attacker comes from, then we actually do not know if it has another good move, lets assume not and attack anyway...
        ) {
            debugPrint(DEBUGMSG_MOVEEVAL, "(hmm, reducing benefit for trying to additionally attack piece " + myPiece()
                    + " with benefit " + benefit + " by " + attacker + " although, it has a good move (" + myPiece().getBestMoveRelEval()
                    +  ") ");
            //benefit >>= 1;  // 0.48h44j: trying again to reduce more :-) as it now only counts if the relEval is smaller...
            benefit = (benefit*10)/27;  // after test series with 0.48h44l --> 44m
            // up to 48h44i: benefit -= (benefit >>3);  //2) + (benefit >> 3);  // *0,87
            // made not much difference, becomes even slightly worse the more one subtracts here... but not really anymore after the skipping of conditioned abave was introduced
        } else
            debugPrint(DEBUGMSG_MOVEEVAL, " (bonus for additionally attacking piece " + myPiece() //+ " at " + squareName(getMyPos())
                    + " with benefit " + benefit + " by " + attacker + ") ");

        if (myPiece().color() == attacker.color()) {
            if (!attacker.canCoverFromSavePlace()) {
                benefit >>= 2;
                debugPrint(DEBUGMSG_MOVEEVAL,"(reducing covering benefit, as there is no save square for covering:) ");
            }
        }
        return benefit;
    }

    private int calcKingAttacksBenefit(VirtualPieceOnSquare additionalAttacker) {
        int benefit;
        //if (DEBUGMSG_MOVEEVAL) debugPrintln(DEBUGMSG_MOVEEVAL, "Analysing " + additionalAttacker.myPiece() + " to " + squareName(getMyPos()) + ".");
        // special treatment of kings
        boolean acol = additionalAttacker.color();
        if ( ( countDirectAttacksWithColor(acol)
                == countDirectAttacksWithColor(opponentColor(acol))
                || ( board.getTurnCol() == acol
                    && countDirectAttacksWithColor(acol) == countDirectAttacksWithColor(opponentColor(acol))-1 ) )
             && !additionalAttacker.getMinDistanceFromPiece().hasNoGo()   //48h20: RawMin instead if Min: little worse against vs14
             && !isKing(myPiece().getPieceType())
             && !isQueen(myPiece().getPieceType())
        ) {
            benefit =  ((myPiece().isWhite() ? EVAL_HALFAPAWN : -EVAL_HALFAPAWN)>>3)
                    + (myPiece().getValue() >> 5);
            /*if (isQueen(myPiece().getPieceType()))
                benefit >>= 1;*/
            if ( acol != myPiece().color() ) {
                //benefit -= benefit >> 3; //*0.87
                benefit = -benefit;  // it is an attack not a defense
            }
            if (DEBUGMSG_MOVEEVAL && abs(benefit) > 4)
                debugPrintln(DEBUGMSG_MOVEEVAL, " King is helping out on " + squareName(getMyPos()) + ".");
        }
        else
            benefit = 0;
        return benefit;
    }

    void calcExtraBenefits() {
        //// extra benefits here for Pieces that could ge here (soon)

        //// calc benefit for controlling extra squares
        int[] ableToTakeControlBonus = {0, 0};  // indicating nobody can take control
        for (int ci=0; ci<=1; ci++) {
            final int oci = opponentColorIndex(ci);
            final int myAttackCount = countDirectAttacksWithColor(colorFromColorIndex(ci));
            final int oppAttackCount = countDirectAttacksWithColor(colorFromColorIndex(oci));
            final int attackCountDelta = myAttackCount - oppAttackCount;
            if ( attackCountDelta <= 0
                    && board.distanceToKing(getMyPos(), colorFromColorIndex(ci)) == 1 ) {
                // defend square next to my king
                ableToTakeControlBonus[ci] =
                    switch(attackCountDelta) {
//                        case 0 -> EVAL_HALFAPAWN; // 50  >>1; // 25
//                        case -1 -> EVAL_HALFAPAWN + (EVAL_HALFAPAWN>>1); // 75
//                        default -> EVAL_HALFAPAWN + (EVAL_HALFAPAWN>>1); // 75 if <=-2
                        case 0 -> EVAL_HALFAPAWN>>1; // 25
                        case -1 -> EVAL_HALFAPAWN + (EVAL_HALFAPAWN>>2); // 62
                        default -> EVAL_HALFAPAWN; // 50 if <=-2
                    };
                if (!evalIsOkForColByMin(clashEval(), colorFromColorIndex(ci)))
                    ableToTakeControlBonus[ci] <<= 1;  // danger is already there
            }
            else if ( attackCountDelta <= 0
                    && board.distanceToKing(getMyPos(), colorFromColorIndex(oci)) == 1 ) {
                // attack squares arround opponents king
                ableToTakeControlBonus[ci] =
                    switch(attackCountDelta) {
                        case -1 -> EVAL_TENTH<<1; // 20
                        case 0 -> EVAL_TENTH + (EVAL_TENTH<<1); // 30
                        default -> (EVAL_TENTH >> 1); // 5
                    };
            }
            else if (oppAttackCount == 0) {
                // opp does not yet cover this square at all
                ableToTakeControlBonus[ci] =
                    switch(myAttackCount) {
                        case 0 -> (EVAL_TENTH >> 1) + 1; // 6
                        case 1 -> (EVAL_TENTH >> 1) - 2; // 3
                        default -> 0;
                    };
            }
            else if ( !evalIsOkForColByMin(clashEval(), colorFromColorIndex(ci)) ) {
                // already in trouble here
                // todo: better would be to calculate per specific vPce if clash really improves
                // strengthen necessary defence
                ableToTakeControlBonus[ci] = EVAL_TENTH; // because we then cover it more often - which does not say too much however...
            }
            else if ( attackCountDelta == 0
                    && abs(clashEval()) <= EVAL_DELTAS_I_CARE_ABOUT ) {
                // strengthen not yet necessary defence
                ableToTakeControlBonus[ci] = EVAL_TENTH>>1; // because we then cover it more often - which does not say too much however...
            }
            //reducing (from 10>>1, 2P, 10, 10>>1) here did make testgames a little worse: ableToTakeControlBonus[ci] -= (ableToTakeControlBonus[ci]+5)>>3;
        }
        ableToTakeControlBonus[CIBLACK] = -ableToTakeControlBonus[CIBLACK];

        for (VirtualPieceOnSquare vPce : vPieces ) {
            if (vPce == null
                    //|| !vPce.getRawMinDistanceFromPiece().distIsNormal() // not any more, drops all vPce with d==0, but these are needed for pin detection
                    || vPce.getRawMinDistanceFromPiece().dist()>=MAX_INTERESTING_NROF_HOPS )
                continue;
            final int inFutureLevel = vPce.getAttackingFutureLevelPlusOne() - 1;
            ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();

            //// benefit for coming closer to control extra squares
            if ( (!isKing(vPce.getPieceType())
                    || board.getPieceCounterForColor(vPce.color()) < 7)
                    && ableToTakeControlBonus[colorIndex(vPce.color())] != 0
                    && rmd.dist() > 1) {
                final int controlFutureLevel = inFutureLevel; // NOT: > 1 ? inFutureLevel-1 : 0; one could think, -1 is correct (as covering from 1 away is sufficient), but results are worse - this matches the thought, that covering the square is not a benefit as such, but the benefit (that the opponent cannot go there) comes later
                int controlSqBenefit = (((ableToTakeControlBonus[colorIndex(vPce.color())]   // factors here bring about *4 for pawns, but *1 for queens
                        * vPce.myPiece().reverseBaseEval()) >> 8)
                        + ableToTakeControlBonus[colorIndex(vPce.color())]);
                if (rmd.dist() <= 3)  // less benefit for dist = 2 or 3 - hope it first brings more "friends" towards the square
                    controlSqBenefit -= controlSqBenefit >> 2; // * 0,75
                // gets worse with
                // if (vPce.getRawMinDistanceFromPiece().hasNoGo())
                //    controlSqBenefit -= controlSqBenefit>>2; // v0.47t-lowtide8
                // combined with >>= 2 instead of >>3 for getMinDist.nogo
                if (vPce.getRawMinDistanceFromPiece().hasNoGo())
                    controlSqBenefit >>= 3;
                if (isKing(vPce.getPieceType()))
                    controlSqBenefit >>= 1;
                if (DEBUGMSG_MOVEEVAL && abs(controlSqBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " " + controlSqBenefit + "@" + controlFutureLevel + " Benefit for conquering square " + squareName(getMyPos()) + " with " + vPce + ".");
                vPce.addChance(controlSqBenefit, controlFutureLevel);
            }

            //// contributions of being the last to control a square where the opponent can go to
            // TODO!: this is unclear - does it do at all what is described here?
            if ( rmd.dist() == 1
                    && vPce.getPieceID() != myPieceType()
                    && ( ( rmd.isUnconditional()
                        && !( isPawn(vPce.getPieceType())  // not a straight moving pawn
                              && fileOf(vPce.getMyPos()) == fileOf(vPce.getMyPiecePos()) ) )
                      || ( !rmd.isUnconditional()
                        && isPawn(vPce.getPieceType())  // a taking pawn
                        && fileOf(vPce.getMyPos()) != fileOf(vPce.getMyPiecePos()) ) )
            ) {
                for (VirtualPieceOnSquare oppVPce : vPieces ) {
                    if (oppVPce == null
                            || oppVPce.color() == vPce.color()
                            || (!isSquareEmpty() && myPiece().color() == oppVPce.color( )
                            || oppVPce.getRawMinDistanceFromPiece().dist() != 1) )
                        continue;
                    ConditionalDistance oppRmd = oppVPce.getRawMinDistanceFromPiece();
                    if ( !oppRmd.isUnconditional() ) {
                            // opp has a condition, this is only relevant if vPve itself can free this condition
                        if ( !( oppRmd.nrOfConditions()==1 && oppRmd.getFromCond(0) == vPce.getMyPiecePos()) )
                            continue;
                    }
                    int c = 0;
                    int enablingFromCond = board.getBoardSquare(getMyPos()).getEnablingFromConditionForVPiece(oppVPce);
                    if (enablingFromCond == vPce.getMyPiecePos()) {
                        // vPce would free this square if moving away
                        if (oppVPce.isCheckGiving()) {
                            c = (EVAL_HALFAPAWN);  // 50
                            if ( board.getPieceAt(board.getKingPos(vPce.color())).getLegalMovesAndChances().size() <= 2 )
                                c += c >> 1;
                        }
                        else if (isSquareEmpty())
                            c = EVAL_TENTH - (EVAL_TENTH>>2); //(EVAL_HALFAPAWN+EVAL_TENTH)>>2;  // 15
                    }
                    else if (enablingFromCond!=NOWHERE) {
                        // another of my pieces is the last controller of this square, let' also keep an eye on here
                        if (oppVPce.isCheckGiving())
                            c = 0; // (EVAL_TENTH);  // 10
                        else
                            c = 0; // EVAL_TENTH>>2;  // 2
                    }
                    else if (oppVPce.isCheckGiving())
                        c = 0; // (EVAL_HALFAPAWN-EVAL_TENTH)>>1;  // 20
                    if (isBlack(vPce.color()))
                        c = -c;
                    if (DEBUGMSG_MOVEEVAL && abs(c) > DEBUGMSG_MOVEEVALTHRESHOLD)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " " + c + " Contribution for square " + squareName(getMyPos())
                                + " for " + vPce + " against " + oppVPce + ".");
                    vPce.addClashContrib(c);
                    //TODO!: addClashContrib brings the problem, that it will also count as a fee against the move that would take the
                    // piece that causes the problem - although this also solves/eliminates the problem, too.
                    // e.g. Qs contrib to cover c2 against fork of ne2c2: "1rbq1rk1/1pp2pbp/p2p1np1/8/4P3/2NNnP2/PP2Q1PP/R3KB1R w KQ - 0 13, e2e3"
                }
            }

            //// moving king towards pawns in endgames
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
                if (isBlack(vPce.color()))
                    protectPawnBenefit = -protectPawnBenefit;
                if (DEBUGMSG_MOVEEVAL && abs(protectPawnBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " " + protectPawnBenefit + "@" + inFutureLevel + " Benefit for king approaching pawn on square " + squareName(getMyPos()) + " with " + vPce + ".");
                vPce.addChance(protectPawnBenefit, inFutureLevel);
            }

            if (rmd.dist()>0) {
                //// pawns try to get to promoting rank
                if (isPawn(vPce.getPieceType()))
                    calcPawnsExtraBenefits(vPce, inFutureLevel);  // looks correcter, but is worse fl-1

                //// checking king related
                addKingCheckReleatedBenefits(vPce, inFutureLevel);

                //// King Area Attacks/Defence
                int kingAttackFutureLevel = inFutureLevel + 1;
                int kingAreaBenefit = getKingAreaBenefit(vPce, WHITE); //opponentColor(vPce.color()));  //
                if ((isBlack(vPce.color()) && board.getNrOfKingAreaAttacks(WHITE) == 0)
                        || (isWhite(vPce.color()) && board.getNrOfKingAreaAttacks(BLACK) == 0)) {
                    //the very first attack, so let's be honest, that this will only happen later
                    kingAttackFutureLevel++;
                }
                if (abs(kingAreaBenefit) > (EVAL_TENTH >> 1)) {

                    int nr = kingAttackFutureLevel - 1;
                    if (nr < 0)
                        nr = 0;
                    if (DEBUGMSG_MOVEEVAL && abs(kingAreaBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " Sum of benefits around king on " + squareName(getMyPos()) + " is: " + kingAreaBenefit + "@" + nr + ".");
                    vPce.addChance(kingAreaBenefit, nr);
                }
                kingAreaBenefit = getKingAreaBenefit(vPce, BLACK);
                if (abs(kingAreaBenefit) > (EVAL_TENTH >> 1)) {
                    int nr = kingAttackFutureLevel - 1;
                    if (nr < 0)
                        nr = 0;
                    if (DEBUGMSG_MOVEEVAL && abs(kingAreaBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " Sum of benefits around king on " + squareName(getMyPos()) + " is: " + kingAreaBenefit + "@" + nr + ".");
                    vPce.addChance(kingAreaBenefit, nr);
                }
            }

            addMoveAwayChances(vPce);

            //// avoid directly moving pieces on squares where a king-pin is likely
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
                        int pinFutureLevel = pinner.getAttackingFutureLevelPlusOne() - 2
                                + (pinnerRmd.isUnconditional() ? 0 : 1);
                        if (pinFutureLevel < 0)
                            pinFutureLevel = 0;
                        int kingPos = board.getKingPos(vPce.color());
                        if (kingPos < 0)
                            continue;  // can happen in test cases
                        VirtualPieceOnSquare pinnerAtKingPos = board.getBoardSquare(kingPos).getvPiece(pinner.getPieceID());
                        ConditionalDistance pinner2kingRmd = pinnerAtKingPos.getRawMinDistanceFromPiece();
                        if (pinner2kingRmd.dist() != 2 || !pinner2kingRmd.isUnconditional())
                            continue;  // not able to give check in 1 move
                        for (Move checkMove : pinnerAtKingPos.getFirstMovesWithReasonableShortestWayToHere()) {
                            if (isBetweenFromAndTo(getMyPos(), checkMove.to(), kingPos)) {
                                //TODO!: if pinner on checkMove.to() will be uncovered, but vPce covers it with its move, then there is no danger
                                int danger = (abs(vPce.getValue()) - abs((pinner.getValue() >> 1))) >> 1;
                                if (danger < EVAL_TENTH)
                                    danger = EVAL_TENTH;  // happens if pinner is much more valuable than pinned pce
                                if (isBlack(vPce.color()))
                                    danger = -danger;
                                if (DEBUGMSG_MOVEEVAL && abs(danger) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                    debugPrintln(DEBUGMSG_MOVEEVAL, " Avoiding king-pin " + (-danger) + "@" + pinFutureLevel
                                            + " for " + vPce + " to " + squareName(getMyPos()) + " by " + pinner + ".");
                                vPce.addChance(-danger, pinFutureLevel );  // warn vPce not to go there
                                if (pinnerRmd.dist() > 2 && abs(danger) > 2) {
                                    if (DEBUGMSG_MOVEEVAL && abs(danger) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                        debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit for coming closer to possible kin-pin "
                                                + (-(danger >> 1)) + "@" + pinFutureLevel + " for " + (pinner.getRawMinDistanceFromPiece().oneLastMoveOrigin())
                                                + " via " + squareName(getMyPos()) + ".");
                                    board.getBoardSquare(checkMove.to())
                                            .getvPiece(pinner.getPieceID())
                                            .addChance(-(danger >> 1), pinFutureLevel, getMyPos() ); // award possible pinner to come closer
                                }
                            }
                        }
                    }
            }

            //// avoid moving to square where another one gets pinned to me
            // and maybe encourage the other one to move away a little?
            if ( rmd.dist() == 0
                || (inFutureLevel < 3  // consider only for low range, doable moves
                    && !vPce.getMinDistanceFromPiece().hasNoGo()
                    && !board.hasPieceOfColorAt(vPce.color(), getMyPos())) // place is free to go there
            ) {
                // find possible pinned piece, if I move there
                pinnerAndPinned pnp = getPinnerAndPinned(vPce.color());
                if (pnp.pinnedVPce != null) {
                    int myDanger = abs(vPce.myPiece().getValue());
                    // todo: this is just an estimation, if mypos was still safe for vPce (and pinner), better was a clashcalc with both extra pieces
                    if (countDirectAttacksWithColor(vPce.color()) >= 1 ) { // already covered by myself
                        if ( myDanger < abs(pnp.pinnerVPce.myPiece().getValue()))
                            myDanger = 0;
                        else
                            myDanger = myDanger - abs(pnp.pinnerVPce.myPiece().getValue());
                    }
                    if (isWhite(vPce.color()))
                        myDanger = -myDanger;
                    VirtualPieceOnSquare pinnerAtPinned = board.getBoardSquare(pnp.pinnedVPce.getMyPos()).getvPiece(pnp.pinnerVPce.getPieceID());
                    int pinnerTakesPinnedDanger = pinnerAtPinned.getRelEvalOrZero();
                    if (!evalIsOkForColByMin(pinnerTakesPinnedDanger, pnp.pinnerVPce.color(), 0))
                        pinnerTakesPinnedDanger = 0;
                    int futurePinnedDanger = board.getBoardSquare(pnp.pinnedVPce.getMyPos()).lowestReasonableExtraThreatFrom(pnp.pinnerVPce.color());
                    int pinnedDanger = max( abs(pinnerTakesPinnedDanger), abs(futurePinnedDanger) );
                    int pinDanger = (min(myDanger, pinnedDanger) >> (1+inFutureLevel));
                    //pinDanger -= pinDanger>>3; // 0.87
                    if (DEBUGMSG_MOVEEVAL && abs(pinDanger) > DEBUGMSG_MOVEEVALTHRESHOLD)
                        debugPrintln(DEBUGMSG_MOVEEVAL, "Warning of " + pinDanger + "@" + inFutureLevel
                                + " pinDanger (min("+myDanger+",max("+pinnerTakesPinnedDanger+","+futurePinnedDanger+"))) for " + vPce
                                + ( rmd.dist()==0 ? " already" : " possibly") + " pinned by " + pnp.pinnerVPce.myPiece()
                                + " pinning " + pnp.pinnedVPce + ".");
                    if ( rmd.dist() == 0 ) {
                        // already pinned motivate to move away - but only little - although this (with more benefit) solves certail concrete situations, it seems to influence games negatively overall
                        if ( abs(futurePinnedDanger) > abs(myDanger) )  // EVAL_DELTAS_I_CARE_ABOUT )  // and there is the danger, that it gets attacked, then only moving away can help
                            myPiece().addMoveAwayChance2AllMovesUnlessToBetween(-(pinDanger>>4), inFutureLevel,
                                pnp.pinnedVPce.getMyPos(), getMyPos(), false, getMyPos());
                        pnp.pinnedVPce.myPiece().addMoveAwayChance2AllMovesUnlessToBetween(-pinDanger >> 5, inFutureLevel,
                                pnp.pinnedVPce.getMyPos(), getMyPos(), false, getMyPos());
                    }
                    else {
                        vPce.addChance(pinDanger, inFutureLevel);
                        pnp.pinnedVPce.myPiece().addMoveAwayChance2AllMovesUnlessToBetween(-pinDanger >> 4, inFutureLevel,
                                pnp.pinnedVPce.getMyPos(), getMyPos(), false, getMyPos());
                    }
                    pnp.pinnerVPce.addChance(pinDanger >> 3, inFutureLevel + 1);
                }
            }

        }

        // original code restored from .46u21 ->
        if (!isSquareEmpty()) {
            // benefit to give king some "Luft"
            int kingNeedsAirBenefit = getKingNeedsAirBenefit();
            if (abs(kingNeedsAirBenefit) > 0) {
                int nr = 0;
                if (DEBUGMSG_MOVEEVAL && abs(kingNeedsAirBenefit)>4)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " Benefits of giving air to king at " + squareName(getMyPos()) + " is: " + kingNeedsAirBenefit + "@" + nr + ".");
                myPiece().addMoveAwayChance2AllMovesUnlessToBetween(
                        kingNeedsAirBenefit, nr,
                        -1, -1, false,
                        board.getKingPos(myPiece().color()) );
            }
        }
        // <-up2here
    }



    void addMoveAwayChances(VirtualPieceOnSquare vPce) {
        //// moves/evals to here activated indirectly by moving away
        // TODO: check if this should be replaced by a new general distributing moving away chances
        final int inFutureLevel = vPce.getAttackingFutureLevelPlusOne() - 1;
        ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();

        if ( rmd.dist() >= 1
                && !rmd.isUnconditional()
                && vPce.hasRelEval()) {
            int benefit = vPce.getRelEval();
            //benefit -= benefit>>4;  // *0.93 - until 48h55 it was: >>3 = *0.87 but with fix of  addChances2PieceThatNeedsToMove() it gives out less benefit -> 48h55b with full benefit was slightly best
            //if (inFutureLevel>2)  // reduce benefit for high futureLevel
            //    benefit /= (inFutureLevel-1);  // in Future the benefit is not taking the piece, but scaring it away
            //or? benefit >>= (inFutureLevel-1);
            for (Integer fromCond : rmd.getFromConds()) {
                if (fromCond >= 0 && board.getPieceIdAt(fromCond)<0) {
                    if (DEBUGMSG_MOVEEVAL)
                        board.internalErrorPrintln("Error in from-condition of " + vPce + ": points to empty square " + squareName(fromCond)+" :-(.");
                    continue;
                }
                if (fromCond >= 0
                        && //colorlessPieceType(board.getPieceTypeAt(fromCond))
                        //!= colorlessPieceType(vPce.getPieceType())   // not my same type, because I'd anyway X-ray through
                        // should do hte same, but more generic
                        !(getvPiece(board.getPieceIdAt(fromCond)).coverOrAttackDistance() == 1)
                ) {
                    if (benefit != NOT_EVALUATED
                            && evalIsOkForColByMin(benefit, vPce.color(), -EVAL_TENTH)
                    ) {
                        int nr = inFutureLevel - ((myPiece() != null && vPce.color() == myPiece().color()) ? 1 : 0);  // covering is one faster then attacking+beating
                        if (nr < 0)
                            nr = 0;
                        if (vPce.getMinDistanceFromPiece().hasNoGo())
                            benefit >>= 3;
                        if (isKing(vPce.getPieceType()))
                            benefit >>= 1;
                        if (!rmd.hasExactlyOneFromToAnywhereCondition())
                            benefit >>= 2 + rmd.nrOfConditions();   // if several pieces are in the way, then moving one away ias actually still safe... so let's strongly reduce the benefit
                        if (!isKing(myPieceType())) {
                            if (DEBUGMSG_MOVEEVAL && abs(benefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                debugPrintln(DEBUGMSG_MOVEEVAL, " " + benefit + "@" + nr
                                        + " Benefit helping pieces freeing way of " + vPce + " to " + squareName(getMyPos()) + ".");
                            // TODO: Take into account that moving away piece could influence the benefit, as the getRelEval could rely on the 2Bmoved piece to take part in the clash
                            vPce.addChances2PieceThatNeedsToMove(
                                    benefit,
                                    nr,  // -2 because dist 1 is already a direct threat and -1 because one help is already fulfilled by moving away
                                    fromCond);
                        }
                    }
                    if ( isKing(myPieceType())
                            && isSlidingPieceType(vPce.getPieceType())
                            && vPce.color() != myPiece().color()
                            && rmd.dist() == 2
                            && isBetweenFromAndTo( fromCond, vPce.getMyPiecePos(), getMyPos())
                            && vPce.color() == board.getPieceAt(fromCond).color()
                            && rmd.hasExactlyOneFromToAnywhereCondition()
                    ) {
                        if ( (DEBUGMSG_MOVEEVAL) )
                            debugPrintln(DEBUGMSG_MOVEEVAL, " Bonus for Abzugschach for " + vPce + " by " + board.getPieceAt(fromCond)
                                    + " on " + board.getBoardFEN() + ".");
                        vPce.addChances2PieceThatNeedsToMove(
                                isWhite(vPce.color()) ? EVAL_TENTH : -EVAL_TENTH ,
                                0,
                                fromCond);
                    }
                }
            }
        }

    }

    private int lowestReasonableExtraThreatFrom(boolean col) {
        if (coverageOfColorPerHops.get(2).get(colorIndex(col)).size()<=0)
            return 0;
        VirtualPieceOnSquare vPce = coverageOfColorPerHops.get(2).get(colorIndex(col)).get(0);
        return vPce.getRelEvalOrZero();
    }

    @NotNull
    private pinnerAndPinned getPinnerAndPinned(boolean pinnedColor) {
        VirtualPieceOnSquare pinnedVPce = null;
        VirtualPieceOnSquare pinnerVPce = null;
        for (VirtualPieceOnSquare vp : getVPieces()) {
            if (vp != null && isSlidingPieceType(vp.getPieceType())
                    && vp.color() != pinnedColor
            ) {
                ConditionalDistance vpRmd = vp.getRawMinDistanceFromPiece();
                if ( vpRmd.dist() == 1
                        && !vpRmd.hasNoGo()
                        && !vpRmd.isUnconditional()
                        && vpRmd.hasExactlyOneFromToAnywhereCondition() ) {
                    // if opponent vp is attacking here almost directly but with a condition
                    int pinnedPos = vpRmd.getFromCond(0);
                    ChessPiece pinnedPiece = board.getPieceAt(pinnedPos);
                    if (pinnedPiece == null)
                        continue; // should not happen, but to be sure
                    VirtualPieceOnSquare alsoPinnedVPce = board.getBoardSquare(pinnedPos).getvPiece(pinnedPiece.getPieceID());
                    if (pinnedVPce == null
                            || abs(alsoPinnedVPce.getValue()) > abs(pinnedVPce.getValue())) {
                        pinnedVPce = alsoPinnedVPce;  // find the most worthy pinned piece
                        pinnerVPce = vp;
                    }
                }
            }
        }
        pinnerAndPinned pnp = new pinnerAndPinned(pinnedVPce, pinnerVPce);
        return pnp;
    }

    private static class pinnerAndPinned {
        public final VirtualPieceOnSquare pinnedVPce;
        public final VirtualPieceOnSquare pinnerVPce;

        public pinnerAndPinned(VirtualPieceOnSquare pinnedVPce, VirtualPieceOnSquare pinnerVPce) {
            this.pinnedVPce = pinnedVPce;
            this.pinnerVPce = pinnerVPce;
        }
    }

    private void calcPawnsExtraBenefits(final VirtualPieceOnSquare vPce, final int inFutureLevel) {
        ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();

        // promotion benefits + countermeasures
        if ( ((isFirstRank(getMyPos()) && isBlack(vPce.color()))
            || (isLastRank(getMyPos()) && isWhite(vPce.color())))) {
            int promoBenefit = rmd.needsHelpFrom(vPce.myOpponentsColor())
                    ? (pieceBaseValue(PAWN)) - EVAL_TENTH
                    : pieceBaseValue(QUEEN) - pieceBaseValue(PAWN);
            if (vPce.minDistanceSuggestionTo1HopNeighbour().hasNoGo())
                promoBenefit = (pieceBaseValue(PAWN) + EVAL_TENTH) >> 2;
            if (isBlack(vPce.color()))
                promoBenefit = -promoBenefit;
            int pawnDist = vPce.getRawMinDistanceFromPiece().dist();
            int countDefenders = 0;

            boolean promotionDirectlyAhead = inFutureLevel < 4
                        && !vPce.getMinDistanceFromPiece().hasNoGo()
                        && fileOf(vPce.getMyPos()) == fileOf(vPce.getMyPiecePos())
                        && !rmd.needsHelpFrom(vPce.myOpponentsColor());
            // run to protect promotion square, "if just in reach"
            VirtualPieceOnSquare closestDefender = null;
            //if (promotionDirectlyAhead) {
                if (vPce.color() == board.getTurnCol())
                    pawnDist--;
                for (VirtualPieceOnSquare defender : vPieces) {
                    if (defender != null
                            && defender.color() != vPce.color()
                    ) {
                        int defenderDist = defender.coverOrAttackDistance() - 1;
                        if ( (defenderDist <= pawnDist)
                        ) {
                            countDefenders++;
                            //VirtualPieceOnSquare defenderAtPawn = board.getBoardSquare(vPce.getMyPiecePos()).getvPiece(defender.getPieceID());
                            if ((closestDefender == null
                                    || defenderDist < (closestDefender.getRawMinDistanceFromPiece().dist() - 1))
                                //&& defenderAtPawn.coverOrAttackDistance()!=1
                            ) {
                                closestDefender = defender;  // remember the closest defender that is close enough to defend.
                            }
                        }
                    }
                }
                if (countDefenders > 0) {
                    // we have a defender diminish benefit. Pawn will make pressure, but most certainly cannot promote
                    promoBenefit >>= countDefenders;
                }
                else {
                    // no defender, make it very urgent, resp. leave benefit as high as already calculated
                    if (!promotionDirectlyAhead) {
                        promoBenefit >>= 1 + rmd.countHelpNeededFromColorExceptOnPos(vPce.myOpponentsColor(), ANYWHERE);
                        promoBenefit /= 1 + abs(fileOf(vPce.getMyPos()) - fileOf(vPce.getMyPiecePos()));
                    }
                }
            //}

            //motivate the pawn
            int directBenefitPart=0;
            if (promotionDirectlyAhead) {
                directBenefitPart = (promoBenefit >> 4) + promoBenefit / (3 + inFutureLevel);
            }
            int endBenefitPart = promoBenefit - directBenefitPart;
            if (DEBUGMSG_MOVEEVAL && abs(endBenefitPart) > DEBUGMSG_MOVEEVALTHRESHOLD)
                debugPrint(DEBUGMSG_MOVEEVAL, " " + endBenefitPart + "@" + inFutureLevel + " Benefit1 for pawn " + vPce + " for moving towards promotion on " + squareName(getMyPos()) + ".");
            vPce.addChance(endBenefitPart, inFutureLevel, isFirstRank(getMyPos()) ? FIRST_RANK_CBM : LAST_RANK_CBM);
            if (abs(directBenefitPart) > 0) {
                if (DEBUGMSG_MOVEEVAL && abs(directBenefitPart) > DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " + " + directBenefitPart + "@0 Benefit2 for pawn " + vPce + " for moving towards promotion on " + squareName(getMyPos()) + ".");
                vPce.addChance(directBenefitPart, 0, isFirstRank(getMyPos()) ? FIRST_RANK_CBM : LAST_RANK_CBM);
            }

            // give the same benefit to those who can just take the pawn
            int countReasonableTakers = 0;
            if (promotionDirectlyAhead
                    && ( (pawnDist < 3) || (pawnDist == 3 && countDefenders == 0) )
            ) {
                Square pawnSq = board.getBoardSquare(vPce.myPiece().getPos());
                int justTakeBenefit = -promoBenefit / (1 + pawnDist);
                countReasonableTakers = pawnSq.addImmediateTakeBenefitForExcept(justTakeBenefit, vPce,
                        ( countDefenders<3 ) ? NO_PIECE_ID : closestDefender.getPieceID() );
            }

            if (closestDefender != null) { // we have a defender
                int defendBenefit = -(promoBenefit) / (countReasonableTakers+1);  // /2 and reduce more the more opponents can simply take the pawn
                int defenderDist = closestDefender.getRawMinDistanceFromPiece().dist() - 1;
                int inFutureLevelDefend = (pawnDist - defenderDist > 0) ? (pawnDist - defenderDist ) : 0;
                if (DEBUGMSG_MOVEEVAL && abs(defendBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " +/- " + defendBenefit + "@" + inFutureLevelDefend
                            + " Benefit for keeping pawn " + vPce + " from moving towards promotion on " + squareName(getMyPos()) + ".");
                closestDefender.addChance(defendBenefit, inFutureLevelDefend);
                if (defenderDist == 0) // already covering -> do not move away!
                    closestDefender.addClashContrib(defendBenefit);
            }

        }

        if ( rmd.dist() == 1 ) {
            boolean isBeating = abs(fileOf(vPce.getMyPos()) - fileOf(vPce.getMyPiecePos())) == 1;
            // motivating pawns to move forward, esp in endgames
            final int nrOfPiece = board.getPieceCounter();
            if (nrOfPiece < 21 && !rmd.hasNoGo()) {
                int forwardBenefit = (24 - nrOfPiece) >> 2;
                if (isBlack(vPce.color()))
                    forwardBenefit = -forwardBenefit;
                if (DEBUGMSG_MOVEEVAL && abs(forwardBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " " + forwardBenefit + "@0 benefit for " + (isBeating ? "beating with" : "advancing") + " pawn to " + squareName(getMyPos()) + ".");
                vPce.addChance(forwardBenefit, 0);
            }

            // avoid doubling pawns (when beating)
            if ( isBeating && board.getPawnCounterForColorInFileOfPos(vPce.color(), vPce.getMyPos() ) > 0 )  {
                int doublePawnFee = EVAL_TENTH - (EVAL_TENTH >> 2);
                if (isWhite(vPce.color()))
                    doublePawnFee = -doublePawnFee;
                if (DEBUGMSG_MOVEEVAL && abs(doublePawnFee) > DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " " + doublePawnFee + "@0 fee for doubling pawn at " + squareName(getMyPos()) + ".");
                vPce.addRawChance( doublePawnFee, 0, getMyPos());
            }
            // motivate to become a passed pawn (when beating) if possible
            if ( isBeating
                    && board.getPawnCounterForColorInFileOfPos(opponentColor(vPce.color()), vPce.getMyPos() ) == 0
                    && (isFirstFile(vPce.getMyPos())
                        || board.getPawnCounterForColorInFileOfPos(opponentColor(vPce.color()), vPce.getMyPos()+LEFT ) == 0 )
                    && (isLastFile(vPce.getMyPos())
                        || board.getPawnCounterForColorInFileOfPos(opponentColor(vPce.color()), vPce.getMyPos()+RIGHT ) == 0 )
            )  {
                // todo: needs to check if the there are opponent's pawns at the side, but still I am a passed pawn, because they are behind me...
                int passedPawnBenefit = EVAL_TENTH >> 1;
                if (isBlack(vPce.color()))
                    passedPawnBenefit = -passedPawnBenefit;
                if (DEBUGMSG_MOVEEVAL && abs(passedPawnBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " " + passedPawnBenefit + "@0 benefit to become a passed pawn at " + squareName(getMyPos()) + ".");
                vPce.addChance( passedPawnBenefit, 0);
            }
        }
    }

    /*  Removed - does the same as calcContribBlocking!?
    // was called in calcBestMove()
    // removing did not change the avaluation, but saved up to 8% time ;-)
    //TODO: Check if something interesting here needs to be moved/added in oter method
    void calcContributionBlocking() {
        if ( !board.isSquareEmpty(getMyPos()) )  // square is not empty
            return;
        for (VirtualPieceOnSquare vPce : getVPieces() ) {
            if (vPce == null)
                continue;
            int inFutureLevel = vPce.getAttackingFutureLevelPlusOne();
            if (inFutureLevel > MAX_INTERESTING_NROF_HOPS)
                continue;
            ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();

            // avoid moving to square where I block my own clash contribution
            // and maybe encourage the other one to move away a little?
            if (rmd.dist() != 1  // consider only for low range, doable moves
                || rmd.hasNoGo()
            )
                continue;
            int nr = inFutureLevel > 0 ? inFutureLevel - 1 : 0;  // TODO: nr is unused??
            for (VirtualPieceOnSquare contributor : getVPieces()) {
                if (contributor == null
                        || !isSlidingPieceType(contributor.getPieceType())
                        || contributor.getRawMinDistanceFromPiece().dist() != 1
                        || !contributor.getRawMinDistanceFromPiece().isUnconditional()
                        || contributor.getPieceID() == vPce.getPieceID()
                )
                    continue;
                int contribToPos = contributor.myPiece().getTargetOfContribSlidingOverPos(getMyPos());
                if ( contribToPos == NOWHERE                        // no contrib
                        || contribToPos == vPce.getMyPiecePos())    // contrib to the piece that moves away anyway
                    continue;
                int contrib = board.getBoardSquare(contribToPos).getvPiece(contributor.getPieceID()).getClashContribOrZero();
                VirtualPieceOnSquare myVPceAtContribTarget = board.getBoardSquare(contribToPos).getvPiece(vPce.getPieceID());

                if (evalIsOkForColByMin(contrib, contributor.color(), -EVAL_DELTAS_I_CARE_ABOUT)
                    && !myVPceAtContribTarget.getPredecessors().contains(vPce)  // if it is part o the predecessors then we are not really blocking, just putting it into 2nd row -
                                                                                     // TODO:check, as evaluation got a even a little worse by this
                ) {
                    // here it is too late to add to the vPces
                    // vPce.addChance(-contrib, 0);
                    // vPce.addChance(contrib, 1); // its not gone, but postponed...
                    // we need to add it to the Pieces moves instead
                    //TODO: see if sq.calcContributionBlocking() can be performed before Pieces.addVPceMovesAndChances()
                    //to enable the cleaner approach (described above)
                    Move m = new Move(vPce.getMyPiecePos(), getMyPos());
                    if ( vPce.myPiece().isBasicallyALegalMoveForMeTo(getMyPos()) ) {
                        m.setBasicallyLegal();
                    }
                    else {
                        // no error, we could have e.g. created a king moving into check, so no:    board.internalErrorPrintln("Illegal move "+m+" to block " + contributor.myPiece() + "'s contribution of " + contrib + " at " + squareName(contribToPos) + " by " + vPce + ".");
                        return;
                    }
                    if ( vPce.color() == contributor.color() ) {
                        // blocking my own piece
                        if (DEBUGMSG_MOVEEVAL && abs(contrib) > 4)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " Ohoh, blocking " + contributor.myPiece() + "'s contribution of "+ contrib + " at " + squareName(contribToPos) + " by " + vPce + ".");
                        vPce.myPiece().changeMoveWithChance(m,  0, -contrib>>1);
                        //vPce.myPiece().addMoveWithChance(new Move(vPce.getMyPiecePos(),getMyPos()),1, contrib>>2);
                    }
                    else if ( !isKing(vPce.getPieceType()) ) {  // king cannot block an opponents sliding piece...
                        // blocking opponent piece - does not work immediately, as it is opponents turn next
                        vPce.myPiece().changeMoveWithChance(m, 1, -contrib>>2);
                    }
                }
            }
        }
    }  */


    /**
     * add benefit to those who can immediate take takenVPce here on this square
     *
     * @param benefit     benefit amount (@fl==0)
     * @param takenVPce
     * @param exceptPceId
     * @return nr of pieces that can take reasonable
     */
    private int addImmediateTakeBenefitForExcept(final int benefit, final VirtualPieceOnSquare takenVPce, final int exceptPceId) {
        int takenPieceID = takenVPce.getPieceID();
        int countReasonableTakers = 0;
        for (VirtualPieceOnSquare vPce : vPieces) {
            if (vPce!=null
                    && vPce.getPieceID() != takenPieceID
                    && vPce.getPieceID() != exceptPceId
                    && takenVPce.color() != vPce.color()
            ) {
                ConditionalDistance rmd = vPce.getRawMinDistanceFromPiece();
                if ( rmd.dist() != 1 || !rmd.isUnconditional() )
                    continue;
                // iterate over all opponents that can directly beat here
                int takeBenefit = (benefit - vPce.getRelEvalOrZero());  //? is it correct: vPce.getRelEvalOrZero()), isn't it already calculated for this move anyway? // take out relEval, it is anyway already in eval of the move
                if (evalIsOkForColByMin(takeBenefit, vPce.color())) {
                    if (DEBUGMSG_MOVEEVAL && abs(takeBenefit)>DEBUGMSG_MOVEEVALTHRESHOLD)
                        debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit for " + vPce + " for taking " + takenVPce.myPiece() + " at " + squareName(getMyPos()) + " is: " + takeBenefit + "@0.");
                    vPce.addChance(takeBenefit, 0, getMyPos() );
                    countReasonableTakers++;
                }
            }
        }
        return countReasonableTakers;
    }

    // original method from .46u21 restored:
    private int getKingNeedsAirBenefit() {
        int benefit = 0;
        if (isSquareEmpty()) {
            return 0;
        }
        boolean kcol;
        if (board.distanceToKing(getMyPos(), WHITE)==1
                && myPiece().color()==WHITE )
            kcol = WHITE;
        else if (board.distanceToKing(getMyPos(), BLACK)==1
                && myPiece().color()==BLACK )
            kcol = BLACK;
        else
            return 0;
        int kingPos = board.getKingPos(kcol);
        // Todo: counting checkable pieces is imprecise: checker might need to give up square control for checking and might have Nogo
        int checkablePieces = board.getBoardSquare(kingPos).countFutureAttacksWithColor(opponentColor(kcol), 2);
        if ( board.nrOfLegalMovesForPieceOnPos(kingPos) == 0
                && countDirectAttacksWithColor(opponentColor(kcol)) == 0
                && ( !extraCoverageOfKingPinnedPiece(opponentColor(kcol)) )
                && (checkablePieces>0
                    || board.getBoardSquare(kingPos).countFutureAttacksWithColor(opponentColor(kcol), 3)>0 )
        ) { // king can be checked soon
            if (checkablePieces>0)
                benefit = EVAL_HALFAPAWN;  // 50 not so big benefit, as we cannot be sure here if it is mate... Todo: more thorough test
            else
                benefit = EVAL_TENTH;
            if (isBlack(kcol))
                benefit = -benefit;
        }
        /*else if ( board.nrOfLegalMovesForPieceOnPos(kingPos) == 0    // TODO:REMOVE - was just here to find example cases
                && countDirectAttacksWithColor(opponentColor(kcol)) == 0
                && ( extraCoverageOfKingPinnedPiece(opponentColor(kcol)) )
                && (checkablePieces>0
                || board.getBoardSquare(kingPos).countFutureAttacksWithColor(opponentColor(kcol), 3)>0 ) ) {
            board.internalErrorPrintln("INFO: do not give Air4King bonus on " + this + " due to extraCoverageOfKingPinnedPiece.");
        }*/

        return benefit;
    }


    // called on square with king
    public void setCheckings() {
        boolean kcol = myPiece().color();
        if (DEBUGMSG_MOVEEVAL)
            debugPrint(DEBUGMSG_MOVEEVAL,"Checking/setting checks for king on " + squareName(getMyPos())+": ");
        for (VirtualPieceOnSquare checkerAtKing : vPieces) {
            if ( checkerAtKing == null
                    || checkerAtKing.color()==kcol
                    || isKing(checkerAtKing.getPieceType() ) )
                continue;
            ConditionalDistance checkerRmdToKing = checkerAtKing.getRawMinDistanceFromPiece();  // 47u22-47u66 line moved before clearCheckGiving
            checkerAtKing.clearCheckGiving();  // why here and only for those at the king?

           /* boolean checkByTakingOpp = checkerRmdToKing.dist() == 1
                    && checkerRmdToKing.hasExactlyOneFromToAnywhereCondition()
                    && checkerRmdToKing.getFromCond(0) >= 0
                    && board.hasPieceOfColorAt(checkerAtKing.myOpponentsColor(), checkerRmdToKing.getFromCond(0) );

            // set direct checks
            if  (  ( checkerRmdToKing.dist() == 2       // totally direct :-)
                      && checkerRmdToKing.isUnconditional() )
                    || checkByTakingOpp                 // also checks by taking a piece that is in the way to the king is a direct check...
            ) { */
                Set<VirtualPieceOnSquare> preds = checkerAtKing.getDirectAttackVPcs();
                // for all squares from where checker can give check
                for (VirtualPieceOnSquare checkerAtCheckingPos : preds) {   // getPredecessorNeighbours() )
                    if (checkerAtCheckingPos == null)
                        continue;
                    if ( !isSlidingPieceType(checkerAtCheckingPos.getPieceType())
                            || ( isSlidingPieceType(checkerAtCheckingPos.getPieceType())
                                 && ((VirtualSlidingPieceOnSquare)checkerAtCheckingPos).canDirectlyGoTo(getMyPos()) )
                    ) {
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrint(DEBUGMSG_MOVEEVAL, " " + squareName(checkerAtCheckingPos.getMyPos()));
                        checkerAtCheckingPos.setCheckGiving();
                    }
                }
            //}

            // set Abzugschach
            int fromCond = ANYWHERE;
            if ( isSlidingPieceType(checkerAtKing.getPieceType())
                    && checkerRmdToKing.dist() == 2                                 // 1 to move away + 1 to the king == 2
                    && checkerRmdToKing.hasExactlyOneFromToAnywhereCondition()
            ) {
                fromCond = checkerRmdToKing.getFromCond(0);  // will be/stay ANYWHERE if it is not a fromCond
                if ( isBetweenFromAndTo(fromCond, checkerAtKing.getMyPiecePos(), getMyPos()) ) {  // probably unnecessary, but to be sure it's a straight line
                    if ((DEBUGMSG_MOVEEVAL))
                        debugPrint(DEBUGMSG_MOVEEVAL, " + Abzugschach possible for " + checkerAtKing + " by " + board.getPieceAt(fromCond)
                                +". "); //+ " on " + board.getBoardFEN() + ".");
                    checkerAtKing.addCheckFlag2PieceThatNeedsToMove(fromCond);
                }
            }
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL,".");
        }
    }

    public void calcCheckBlockingOptions() {
        if (myPiece()==null)
            return; // should not happen
        boolean kcol = myPiece().color();
        int nrofkingmoves = board.nrOfLegalMovesForPieceOnPos(getMyPos());
        int rawBlockingBenefit = nrofkingmoves==0 ? ( pieceBaseValue(PAWN) )
                                               : ( nrofkingmoves<=2 ? (pieceBaseValue(PAWN)-(pieceBaseValue(PAWN)>>2))
                                                                    : (pieceBaseValue(PAWN)>>1) );
        //blockingbenefit = 0;
        if (isBlack(kcol))
            rawBlockingBenefit = -rawBlockingBenefit;
        if (DEBUGMSG_MOVEEVAL)
            debugPrintln(DEBUGMSG_MOVEEVAL,"Bonus for checking and blocking checks against king on " + squareName(getMyPos())+".");

        for (VirtualPieceOnSquare checkerVPceAtKing : vPieces) {
            if ( checkerVPceAtKing == null
                 || checkerVPceAtKing.color()==kcol  // 47u22-47u66 2 lines moved before clearCheckGiving
                 || isKing(checkerVPceAtKing.getPieceType() )
            )
                continue;
            ConditionalDistance checkerRmdToKing = checkerVPceAtKing.getRawMinDistanceFromPiece();  // 47u22-47u66 line moved before clearCheckGiving

    /* in loop now:
            int fromCond = ANYWHERE;
            if ( checkerRmdToKing.dist() == 2
                                    && checkerRmdToKing.nrOfConditions() == 1 )
                fromCond = checkerRmdToKing.getFromCond(0);  // will be/stay ANYWHERE if it is not a fromCond
*//*            if  ( !(checkerRmdToKing.dist() == 2 && checkerRmdToKing.isUnconditional() )  //TODO!: make it generic for all future levels )
                        // or a (one) condition must be fulfilled by opponent here:
                        && !(fromCond >= 0)
                        // existing fromCondition hinders direct check, the option is not considered here yet,
                        // Todo: must be taken into account in code below first: && ! (checkerRmdToKing.dist()==2 && checkerRmdToKing.nrOfConditions()==1) // implies that the condition can be fulfilled by myself, so it is also a 1-move check
            )
                continue; */
            //TODO!: do the same for Abzugschach, where the fromCond-Piece is not also giving check at the same time.
            //Set<VirtualPieceOnSquare> preds = checkerVPceAtKing.getShortestReasonableUnconditionedPredecessors();
            Set<VirtualPieceOnSquare> preds = checkerVPceAtKing.getDirectAttackVPcs();
  /*          if ( preds.size() == 0 && fromCond >= 0 ) {
                // getShortestReasonableUnconditionedPredecessors is empty when piece is in the way on the last segment
                // todo!: do not use getShortestReasonableUnconditionedPredecessors() it leaves out the one with a from-condition on the last move (=the checking)
            } */

            // for all squares from where checkerVPceAtKing can give check
            for ( VirtualPieceOnSquare checkerAtCheckingPos : preds) {   // getPredecessorNeighbours() )
                if (checkerAtCheckingPos == null || !checkerAtCheckingPos.isCheckGiving() )
                    continue;
                ConditionalDistance checkerMinDistToCheckingPos = checkerAtCheckingPos.getMinDistanceFromPiece();
                int fromCond = ANYWHERE;
                if ( checkerMinDistToCheckingPos.dist() == 1
                        && checkerMinDistToCheckingPos.hasExactlyOneFromToAnywhereCondition() )
                    fromCond = checkerMinDistToCheckingPos.getFromCond(0);  // will be/stay ANYWHERE if it is not a fromCond
                /*not needed any more. is coverd by checkingFlag and wrong for chess by beating
                if ( checkerMinDistToCheckingPos.dist() == 1
                        && ( checkerMinDistToCheckingPos.isUnconditional()
                             || fromCond >= 0 ) )  { */
                int checkFromPos = checkerAtCheckingPos.getMyPos();
                // Todo!: There is a bug here: e.g. after 1. d4 d5  2. Dd3 there is a dist==2 to king check (via b5), but it looks like via e3 is the same (has no condition to go to e3), but it does not know about the later condition from e3 to e8...
                //  "vPce(27=weiße Dame) on [e3] 1 ok away from origin {d3} is able to give check on e3 and [...]
                //   is able to cover 1 of 1 king moves.
                //   Benefit 24999@1 for Check blocking by vPce(11=schwarzer Bauer) on [e5] 1 ok away from origin {e7} to e8.
                //   ->e7e5(24999@1)"
                if (DEBUGMSG_MOVEEVAL) {
                    debugPrintln(DEBUGMSG_MOVEEVAL, "");
                    debugPrintln(DEBUGMSG_MOVEEVAL, checkerAtCheckingPos + " is able to give check on " + squareName(checkFromPos)
                            + ((fromCond >= 0) ? " if " + board.getPieceAt(fromCond) + " moves away" : "")
                            + " and ");
                }
                int futureLevel = checkerAtCheckingPos.getStdFutureLevel()
                                       ; // + (checkerMinDistToCheckingPos.isUnconditional() ? 0 : 1);
                int countNowCoveredMoves = 0;
                int countFreedMoves = 0;
                int checkingDir = calcDirFromTo(checkerAtCheckingPos.getMyPos(), getMyPos());
                Set<ChessPiece> luftGiver = new HashSet<>();
                // count how many previously legal moves of the king are blocked by the check
                int oneNeighbourPos = NOWHERE;
                for (VirtualPieceOnSquare kingsNeighbour : getvPiece(myPieceID).getNeighbours()) {
                    if ( isPawn(checkerAtCheckingPos.getPieceType())
                            && fileOf(checkerAtCheckingPos.getMyPos()) == fileOf(kingsNeighbour.getMyPos()) )
                        continue; // a pawn does not attack much in the straight direction
                    VirtualPieceOnSquare checkerAroundKing = board.getBoardSquare(kingsNeighbour.getMyPos()).getvPiece(checkerVPceAtKing.getPieceID());
                    boolean wasLegalKingMove = myPiece().isBasicallyALegalMoveForMeTo(checkerAroundKing.getMyPos());
                    ConditionalDistance checkerRmdAroundKing = checkerAroundKing.getRawMinDistanceFromPiece();
                    debugPrint(DEBUGMSG_MOVEEVAL, " .. check covering " + squareName(checkerAroundKing.getMyPos()) + ": ");
                    // see what the move does:

                    boolean nowCovered = false;
                    if ( checkerAroundKing.getDirectAttackVPcs().contains(checkerAtCheckingPos)
                            || (checkerAtCheckingPos instanceof VirtualSlidingPieceOnSquare
                                && ((VirtualSlidingPieceOnSquare)checkerAtCheckingPos).canDirectlyGoTo(kingsNeighbour.getMyPos()) )
                    ) {
                        // the checker attacks also this square
                        nowCovered = true;
                    }
                    else if ( checkerAtCheckingPos.hasAbzugChecker()) {  // could be ||, but is separated for different debug ouputs...
                        // or at additional AbzugSchach: the realChecker does cover it...
                        VirtualPieceOnSquare realCheckerAroundKing = board.getBoardSquare(kingsNeighbour.getMyPos())
                                .getvPiece(checkerAtCheckingPos.getAbzugChecker().getPieceID());
                        ConditionalDistance realCheckerAroundKingRmd = realCheckerAroundKing.getRawMinDistanceFromPiece();
                        if ( realCheckerAroundKingRmd.dist() == 2
                                && realCheckerAroundKingRmd.hasExactlyOneFromToAnywhereCondition()
                                && realCheckerAroundKingRmd.getFromCond(0) == checkerAtCheckingPos.getMyPiecePos()
                        ) {
                            nowCovered = true;
                        }
                    } else if ( checkerRmdAroundKing.dist() == 1
                            && checkerRmdAroundKing.hasExactlyOneFromToAnywhereCondition()
                            && checkerRmdAroundKing.getFromCond(0) == checkFromPos  // the only fromCond was Beaten
                    ) {
                        // another similar but different case (e.g. 3R1r1k/2p3pp/8/4qp2/8/2N5/PPP2PPP/6K1  w - - 1 25)
                        // square is covered after the taking move
                        nowCovered = true;
                    }
                    boolean nowAdditionallyCovered;
                    if ( /* TODO: does not work? - why?: makes mateIn1-Tests drop from ~2800 passes to ~2200
                                            && (checkerRmdAroundKing.dist()==2 && checkerRmdAroundKing.isUnconditional()  //TODO!: make it generic for all future levels )
                                            || checkerRmdAroundKing.dist()==1 && !checkerRmdAroundKing.isUnconditional()) */
                        nowCovered
                            && wasLegalKingMove
                            && board.getBoardSquare(checkerAroundKing.getMyPos())
                                        .countDirectAttacksWithout2ndRowWithColor(checkerAroundKing.color()) == 0  // count only newly covered places
                            && !board.getBoardSquare(checkerAroundKing.getMyPos())
                                        .extraCoverageOfKingPinnedPiece(checkerAroundKing.color())
                    ) {
                        nowAdditionallyCovered = true;
                    }
                    else
                        nowAdditionallyCovered = false;

                    /*debugPrint(DEBUGMSG_MOVEEVAL, " .. check freeing: " + squareName(checkerAroundKing.getMyPos())
                            + " checkerRmdAroundKin=" + checkerRmdAroundKing
                            + " !onSameAxis:" + (!dirsAreOnSameAxis(calcDirFromTo(checkerVPceAtKing.myPiece().getPos(), checkFromPos),
                                                                    calcDirFromTo(checkFromPos, checkerAroundKing.getMyPos())))
                            + " !legalKingMove:" + !wasLegalKingMove
                            + " current attacks: " + board.getBoardSquares()[checkerAroundKing.getMyPos()].countDirectAttacksWithColor(checkerAroundKing.color()) + "<=1: ");
                    */
                    boolean nowFreed = (checkerRmdAroundKing.dist() == 1 && checkerRmdAroundKing.isUnconditional()  //TODO!: make it generic for all future levels )
                            && !dirsAreOnSameAxis(calcDirFromTo(checkerVPceAtKing.getMyPiecePos(), checkFromPos),
                                                  calcDirFromTo(checkFromPos, checkerAroundKing.getMyPos()))
                            && !wasLegalKingMove
                            && !board.hasPieceOfColorAt(kcol, checkerAroundKing.getMyPos())
                            && board.getBoardSquare(checkerAroundKing.getMyPos())
                                        .countDirectAttacksWithColor(checkerAroundKing.color()) <= 1  // checker must be the last to cover target square of king
                            && !board.getBoardSquare(checkerAroundKing.getMyPos())
                                        .extraCoverageOfKingPinnedPiece(checkerAroundKing.color()) );

                    if (nowAdditionallyCovered) {
                        countNowCoveredMoves++;
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " c+1=" + countNowCoveredMoves + ". ");
                    }
                    //else
                    //    debugPrintln(DEBUGMSG_MOVEEVAL, " no. ");

                    if (nowFreed && !nowCovered) {
                        countFreedMoves++;
                        debugPrint(DEBUGMSG_MOVEEVAL, " f-1=-" + countFreedMoves + ". ");
                    }
                    //else
                    //debugPrintln(DEBUGMSG_MOVEEVAL, "no.");

                    if ( !nowAdditionallyCovered
                            && board.hasPieceOfColorAt(kcol, checkerAroundKing.getMyPos()) ) {  // yes, there is a friend blocking the king
                        // this square remains unattacked by the checker, can the piece here  give Luft to the king?
                        if ( board.getBoardSquare(checkerAroundKing.getMyPos()).walkable4king(kcol)   // it is not attacked by opponent
                             || (nowFreed
                                 && board.getBoardSquare(checkerAroundKing.getMyPos())           // or checker was the last, but leaves...
                                                .countDirectAttacksWithColor(checkerAroundKing.color()) == 1
                                 && !board.getBoardSquare(checkerAroundKing.getMyPos())           // or checker was the last, but leaves...
                                                .extraCoverageOfKingPinnedPiece(opponentColor(kcol)) )
                        ) {
                            luftGiver.add(board.getPieceAt(checkerAroundKing.getMyPos()));
                        }
                    }

                    if (!nowCovered && (wasLegalKingMove || nowFreed))
                        oneNeighbourPos = kingsNeighbour.getMyPos();

                } // end loop around kings neighbours
                if ( plusDirIsStillLegal(getMyPos(), checkingDir)
                        && dirsAreOnSameAxis(checkingDir,
                                 calcDirFromTo(checkerAtCheckingPos.getMyPos(), getMyPos()+checkingDir))
                        && board.getBoardSquare(getMyPos() + checkingDir).walkable4king(kcol)
                        && !board.hasPieceOfColorAt(kcol, getMyPos()+checkingDir)
                ) {
                    // this attack points to the king, so also count the square behind the kind as covered
                    countNowCoveredMoves++;
                    if (DEBUGMSG_MOVEEVAL)
                        debugPrint(DEBUGMSG_MOVEEVAL, " +1 at opposite side = " + countNowCoveredMoves + ". ");
                }

                // find and give bonus to possible check blocking moves
                int defendBenefit = 0;  // benefit for defender(!)
                final int nrOfKingMovesAfterCheck = nrofkingmoves + countFreedMoves - countNowCoveredMoves;
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " It is able to cover " + countNowCoveredMoves
                            + (countFreedMoves > 0 ? " but frees " + countFreedMoves : "")
                            + " of " + nrofkingmoves + " king moves -> " + nrOfKingMovesAfterCheck + " are left.");

                int blockFutureLevel = futureLevel;
                if ( fromCond >= 0 )
                    blockFutureLevel++;

                // count possible blockers
                int countBlockers = checkerVPceAtKing.addBenefitToBlockers(
                        checkFromPos, blockFutureLevel, 0);
                //note, wo do not count blockers of the way from checker to checkingpos, as they would be to late, but we still reward/motivate them further down
                if (DEBUGMSG_MOVEEVAL)
                    debugPrint(DEBUGMSG_MOVEEVAL, " nr of checking path blockers: " +countBlockers + ", ");

                if (nrOfKingMovesAfterCheck <= 0
                        && !checkerMinDistToCheckingPos.hasNoGo()
                ) { // no more moves for the king!
                    if (countBlockers == 0
                            && ( ( futureLevel == 0
                                 && checkerMinDistToCheckingPos.isUnconditional()
                                 && fromCond < 0  )
                               || ( futureLevel == 1
                                    && fromCond >= 0 ) )
                    )
                        defendBenefit = checkmateEval(BLACK);  // it is checkmate, nobody can block
                    else if (futureLevel==1)
                        defendBenefit = EVAL_HALFAPAWN<<1;        // but must not really be, as blocks are possible
                    else
                        defendBenefit = EVAL_HALFAPAWN;        // but must not really be, as blocks are possible
                     if (isBlack(kcol)) // in TEST 48h44s: moved below (as in older, and assumed as incorrect versions <48h44s)
                        defendBenefit = -defendBenefit;
                }
                else if (countFreedMoves > countNowCoveredMoves) {
                    defendBenefit = 0;
                }
                else if (nrofkingmoves > 0) {
                    defendBenefit = (rawBlockingBenefit * (countNowCoveredMoves - countFreedMoves)) / nrofkingmoves;  // proportion of remaining squares
                }
                //if (isBlack(kcol))    // test in 48h44s
                //    defendBenefit = -defendBenefit;

                // defendBenefit to those who can block it
                if ( fromCond >= 0 ) {
                    defendBenefit >>= 2;
                }
                int coverOrBlockBenefit = defendBenefit;

                if (checkerMinDistToCheckingPos.hasNoGo() )
                    coverOrBlockBenefit >>= 3;
                else if (checkerAtCheckingPos.isKillableReasonably() )
                    coverOrBlockBenefit >>= 2;

                /*cannot happen here: if (!evalIsOkForColByMin(defendBenefit, checkerVPceAtKing.color(), -(EVAL_TENTH >> 1)))
                    continue;  // move could lose more covered squares than it covers additionally. */
                checkerVPceAtKing.addBenefitToBlockers(
                        checkFromPos, blockFutureLevel, coverOrBlockBenefit);
                // motivate blockers of first move
                if (checkerAtCheckingPos.coverOrAttackDistance() == 1) {
                    if (DEBUGMSG_MOVEEVAL)
                        debugPrint(DEBUGMSG_MOVEEVAL, "  + Motivating to block the move of checker: ");
                    checkerAtCheckingPos.addBenefitToBlockers(
                            checkerAtCheckingPos.getMyPiecePos(), blockFutureLevel, (coverOrBlockBenefit>>1) ) ;
                }
                // defendBenefit to those who can cover the target square, if necessary
                /* now included in addBenefitToBlockers()!
                if ( !checkerAtCheckingPos.isKillableReasonably() && futureLevel <= 2) {
                    for (VirtualPieceOnSquare coverer : board.getBoardSquare(checkFromPos).getVPieces()) {
                        if (coverer != null && coverer.color() == kcol && !isKing(coverer.getPieceType())
                        ) {
                            //ConditionalDistance covererRmd = coverer.getRawMinDistanceFromPiece();
                            int covererAttackDist = coverer.coverOrAttackDistance(true);
                            if ( covererAttackDist > 1   //TODO!: make it generic for all future levels )
                                    && covererAttackDist <= futureLevel+2   //TODO!: make it generic for all future levels )
                            ) {
                                int attackdelta = countDirectAttacksWithColor(coverer.color())
                                                - countDirectAttacksWithColor(coverer.myOpponentsColor());
                                int finalBenefit = attackdelta >= 0 ? (coverOrBlockBenefit >> 1)                        // already covered more often than attacked
                                        : (attackdelta < -1 ? coverOrBlockBenefit          // will not be enough
                                        : (coverOrBlockBenefit << 1));// just enough, lets cover it!
                                finalBenefit >>= futureLevel;
                                if (DEBUGMSG_MOVEEVAL && abs(finalBenefit) > 3)
                                    debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit " + finalBenefit + "@" + blockFutureLevel
                                            + " for check hindering by " + coverer + " covering " + squareName(getMyPos()) + ".");
                                coverer.addChance(finalBenefit, blockFutureLevel, checkFromPos);
                                if (covererAttackDist == 2 && coverer.color() == board.getTurnCol() )
                                    countBlockers++;   // only counts if it is not already too late, because it is not the coverers turn
                            }
                        }
                    }
                }
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " resp. incl. those covering the checking square: " +countBlockers + " + "+luftGiver.size()+" Luft givers." );

                if ( !checkerMinDistToCheckingPos.hasNoGo()
                        && nrOfKingMovesAfterCheck <= 0
                        && ( countBlockers == 0  || fromCond >= 0 )  // TODO!!: but then it is not checkmate yet, still, if at fromCond is my own piece, then I my not move it away now, this (only) this case should still get the checkmate eval
                ) {
                    // no more moves for the king and no blocking possible
                    defendBenefit = -checkmateEval(kcol);   // should be mate
                    // mate can still be avoided (giving Luft or moving the king where it can escape)
                }
                */

                Square checkFromSquare = board.getBoardSquare(checkFromPos);
                if ( checkerMinDistToCheckingPos.dist() == 1
                        && checkerMinDistToCheckingPos.isUnconditional()
                        && checkerMinDistToCheckingPos.hasNoGo() ) {
                    //TODO: this part is partly "inoperable", as a nogo-path is rarely even considered in the rmd of the possible checker - usually only the non-nogo-paths are considered, even if they are longer or conditioned. So this code for now is never/rarely executed
                    // give contribution to those covering the checking square
                    int checkingSquareDefendContrib = defendBenefit;  // 48h44s2-Test was: = -defendBenefit;  while s3 was = defendBenefit - and was much worse... although it is correct (while -benefit definitely has the wrong sign...)
                    /*if ( nrOfKingMovesAfterCheck <= 0 && countBlockers == 0
                         && countDirectAttacksWithColor(kcol) == 1 ) {  // TODO!!:why==1 not ==0 and !extraKingPinned...
                        checkingSquareDefendContrib = -checkmateEval(kcol);   // would be mate
                    }*/
                    for ( VirtualPieceOnSquare defender : checkFromSquare.directAttackVPcesWithout2ndRowWithColor(kcol) ) {
                        if ( defender == null || isKing(defender.getPieceType()) )
                            continue;
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrint(DEBUGMSG_MOVEEVAL, " Giving a checkingSquareDefendContrib of "
                                    +checkingSquareDefendContrib + " for " + defender + ", ");
                        defender.addClashContrib(checkingSquareDefendContrib);
                    }
                    defendBenefit >>= 3;
                    //if (checkerMinDistToCheckingPos.dist()>1)
                    continue; //was(further up, same condition):
                }

                // Now add move away chances to who can indirectly cover the square by moving out of the way.
                // TODO: Add this to benefitBlockers in general?
                for ( VirtualPieceOnSquare coverer : checkFromSquare.getVPieces() ) {
                    if (coverer != null && coverer.color() == kcol && !isKing(coverer.getPieceType())
                    ) {
                        ConditionalDistance covererRmd = coverer.getRawMinDistanceFromPiece();
                        if (covererRmd.dist() == 2   //TODO!: make it generic for all future levels )
                                && !covererRmd.hasNoGo()
                                && covererRmd.hasExactlyOneFromToAnywhereCondition()
                        ) {
                            // indirect blocking by moving out of the way might be possible
                            int attackdelta = countDirectAttacksWithColor(coverer.color()) - countDirectAttacksWithColor(coverer.myOpponentsColor());
                            int coverIndirectlyBenefit = attackdelta >= 0 ? (coverOrBlockBenefit >> 1)                        // already covered more often than attacked
                                                                : (attackdelta < -1 ? coverOrBlockBenefit          // will not be enough
                                                                                    : (coverOrBlockBenefit << 1));// just enough, lets cover it!
                            int covererFromCond = covererRmd.getFromCond(0);
                            if (covererFromCond>=0) {
                                ChessPiece inbetweener = board.getPieceAt(covererFromCond);
                                if (inbetweener!=null) {
                                    if (DEBUGMSG_MOVEEVAL && abs(coverIndirectlyBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                        debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit " + coverIndirectlyBenefit + "@" + blockFutureLevel
                                                + " for check hindering by " + coverer + " covering " + squareName(getMyPos()) + "by moving " + inbetweener + " out of the way.");
                                    inbetweener.addMoveAwayChance2AllMovesUnlessToBetween(
                                            coverIndirectlyBenefit, blockFutureLevel,
                                            checkerVPceAtKing.getMyPos(), checkerAtCheckingPos.getMyPiecePos(), false,
                                            checkFromPos);
                                    countBlockers++; // still counted, but like luftGivers does not reduce, the urgency of seeing a direct mate threat here
                                }
                            }
                        }
                    }
                }

                //fee self-blocking the last available escape square of the king , 48h44p
                if (nrOfKingMovesAfterCheck == 1 && oneNeighbourPos >= 0) {
                    int selfBlockingFee;
                    if (countBlockers == 0
                            && luftGiver.size() == 0
                    ) {
                        selfBlockingFee = checkmateEval(kcol);
                        /* in cases other than the pure creating of checkmate a fee does not improve the test games:
                        } else {
                        selfBlockingFee = EVAL_TENTH;
                        if (countBlockers >= 1)
                            selfBlockingFee >>= 1;
                        if (luftGiver.size() >= 1)
                            selfBlockingFee >>= 1;
                        if (isWhite(kcol))
                            selfBlockingFee = -selfBlockingFee; } */

                        for (VirtualPieceOnSquare selfBlocker : board.getBoardSquare(oneNeighbourPos).getVPieces()) {
                            if (selfBlocker == null || selfBlocker.color() != kcol
                                    || selfBlocker.getMinDistanceFromPiece().dist() != 1
                                    || isKing(selfBlocker.getPieceType()))
                                continue;
                            // for every own piece that can directly come here
                            if (DEBUGMSG_MOVEEVAL && abs(selfBlockingFee) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                debugPrintln(DEBUGMSG_MOVEEVAL, "Warning of " + selfBlockingFee + "@" + 0
                                        + " for " + selfBlocker + " for blocking last square of king after check by " + checkerAtCheckingPos + ".");
                            selfBlocker.addRawChance(selfBlockingFee, 0, oneNeighbourPos);
                        }
                    }
                }

                if (checkerAtCheckingPos.color() != board.getTurnCol() || countBlockers != 0) {
                    // giving Luft is only making sense if king is not already being mated first (because it's opponents turn and there are no kingmoves left)
                    countBlockers += (luftGiver.size()+1)>>1;  // luftGiver.size() in 47u22-47u66
                    //tested e.g. in .47u64, but no improvement, may be even a little worse - is sign wrong? doesn't look like
                    //but lets try to reactivat the following, since the above !=turnCol condition was added:
                    if (nrOfKingMovesAfterCheck <= 0
                            // && fromCond < 0
                    ) {
                        int luftBenefit = fromCond < 0 ? (defendBenefit >> 2) : (defendBenefit >> 3);
                        if (countBlockers != 0)
                            luftBenefit /= (countBlockers+1);
                        for (ChessPiece l : luftGiver) {
                            if (DEBUGMSG_MOVEEVAL && abs(luftBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                                debugPrintln(DEBUGMSG_MOVEEVAL, "Benefits of giving Luft to king to escape to " + squareName(l.getPos())
                                        + " is: " + luftBenefit + "@" + blockFutureLevel + ".");
                            l.addMoveAwayChance2AllMovesUnlessToBetween(
                                    luftBenefit,
                                    blockFutureLevel,
                                    -1, -1, false, getMyPos());
                        }
                    }
                }

                //blocking those who cover the mating square - but only if that makes me able to have the upper hand on that square
                int coverageDeltaAfterBlocking = checkFromSquare.countDirectAttacksWithColor(checkerVPceAtKing.color()) - 1   // after blocking one (-1) we cover same times
                        - checkFromSquare.countDirectAttacksWithColor(kcol);
                if ( coverageDeltaAfterBlocking <= 0 && coverageDeltaAfterBlocking >= -3) {
                    int blockCoDef = defendBenefit + ((coverOrBlockBenefit>>5) + (isWhite(kcol)? EVAL_TENTH:-EVAL_TENTH));
                    // note: defendBenefit (=-checking benefit) equalizes the checking benefit of the opponent, because
                    // later move selection will not recognize that this move is blocking the checking move (as they seem
                    // unrelated) then +benefit derived from coverOrBlock, but much less (/32 is still 3125 against a
                    // mate), +10 to prevent that no benefit remains :-)
                    if ( coverageDeltaAfterBlocking < -1 )
                        blockCoDef >>= 3;
                    else if ( coverageDeltaAfterBlocking == -1 )
                        blockCoDef >>= 3;
                    for ( VirtualPieceOnSquare checkerHelper : checkFromSquare.directAttackVPcesWithout2ndRowWithColor(checkerVPceAtKing.color()) ) {
                        if (checkerHelper.getPieceID() == checkerAtCheckingPos.getPieceID())
                            continue;
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrint(DEBUGMSG_MOVEEVAL, " Trying to cover opponents co-defender of checking position "
                                        + checkerHelper + ". ");
                        checkerHelper.addBenefitToBlockers(
                                    checkerHelper.getMyPiecePos(), blockFutureLevel, blockCoDef );
                    }
                }

                // benefit for giving check
                int checkingBenefit = -defendBenefit;
                if (DEBUGMSG_MOVEEVAL && abs(checkingBenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL, "-> Benefit " + checkingBenefit + "@" + futureLevel
                            + " for checking possibility by " + checkerAtCheckingPos + " to " + squareName(getMyPos()) + ".");
                if ( fromCond >= 0) {
                    //checkerAtCheckingPos.addChance( defendBenefit, futureLevel+2 );  // esp. for the counter moves, which are only valid one move later
                    checkerAtCheckingPos.addRawChance( checkingBenefit, futureLevel, checkerAtCheckingPos.getMyPos());  // but still the move as such is mate immeditaley if blocking pice moves away
                }
                else
                    checkerAtCheckingPos.addChance( checkingBenefit, futureLevel );

                // avoid moving out of the way
                if ( fromCond>=0 && board.getPieceAt(fromCond) != null ) {
                    if (DEBUGMSG_MOVEEVAL && abs(checkingBenefit) > 3)
                        debugPrintln(DEBUGMSG_MOVEEVAL, "Fee of " + (checkingBenefit) + "@" + futureLevel
                                + " against moving away of " + (board.getPieceAt(fromCond))
                                + " for enabling checking by " + checkerVPceAtKing + " to " + squareName(getMyPos()) + ".");
                    int ootwFl = futureLevel-1; // moving out of the way brings enemy one step closer
                    if (ootwFl < 0)
                        ootwFl = 0;
                    //todo: unless covering checking square after moving away...
                    board.getPieceAt(fromCond).addMoveAwayChance2AllMovesUnlessToBetween(
                            checkingBenefit, ootwFl,
                            checkFromPos, checkerVPceAtKing.getMyPiecePos(), false,
                            board.getKingPos(myPiece().color()) );
                }
            }
            // benefit to those who can attack the potential checker  48h44l
            if ( checkerRmdToKing.dist() == 2 && checkerRmdToKing.isUnconditional()) {
                int checkerPos = checkerVPceAtKing.getMyPiecePos();
                for (VirtualPieceOnSquare counterAttacker : board.getBoardSquare(checkerPos).getVPieces()) {
                    if (counterAttacker != null
                            && counterAttacker.color() == kcol
                            && !isKing(counterAttacker.getPieceType())
                    ) {
                        int cAAttackDist = counterAttacker.coverOrAttackDistance(true);
                        if ( cAAttackDist != 2 )
                            continue;
                        int cABenefit = EVAL_TENTH;
                        if ( counterAttacker.getMinDistanceFromPiece().hasNoGo() )
                            cABenefit >>= 2;
                        if ( isBlack(kcol) )
                            cABenefit = -cABenefit;
                        if (DEBUGMSG_MOVEEVAL && abs(cABenefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " Motivation to chase away possible check giver "
                                    + cABenefit + "@" + 0 + " by " + counterAttacker + ".");
                        counterAttacker.addChance( cABenefit, 0, checkerPos);
                    }
                }
            }
        }
    }

    private void addKingCheckReleatedBenefits(VirtualPieceOnSquare attacker, final int inFutureLevel) {
        final int nr = inFutureLevel == 0 ? 0 : inFutureLevel - 1; // -1 because already threatening the square where check is, is benefit.
        boolean acol = attacker.color();
        boolean kcol = opponentColor(acol);
        boolean attackerIsWhite = isWhite(acol);
        // danger for king to move to its neighbour squares
        int dist2k = board.distanceToKing(getMyPos(), acol);
        ConditionalDistance attackerRmd = attacker.getRawMinDistanceFromPiece();

        // first the king himself likes to know, if checking becomes more likely if he goes to a neighbouring square.
        if (isKing(attacker.getPieceType())
                && ( dist2k == 1
                     || dist2k == 2 && !board.hasPieceOfColorAt(acol, getMyPos()) )
                && attackerRmd.isUnconditional()
        ) {
            int currentKingDangerLevel = board.getBoardSquare(board.getKingPos(acol)).getFutureDangerValueThroughColor(kcol);
            int dangerLevelHere = getFutureDangerValueThroughColor(kcol);
            int benefit = (currentKingDangerLevel - dangerLevelHere) * (EVAL_TENTH - (EVAL_TENTH >> 2));  // +/-8,16,24,32
            // does not improve, makes worse:
            //  if (dist2k == 1 && currentKingDangerLevel >= 2 && dangerLevelHere < 2)
            //    benefit += benefit>>1; //this square cannot be attacked immediately, but my current position can, so motivate to go away
            if (benefit > 0 || dist2k > 1)
                benefit >>= 1;  // bonus is awarded less then fees will cost
            if (benefit < 0 && dist2k > 1)
                benefit = -1;  // almost no fee towards bad places two squares away
            if (!attackerIsWhite)
                benefit = -benefit;
            if (DEBUGMSG_MOVEEVAL && abs(benefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                debugPrintln(DEBUGMSG_MOVEEVAL, " Adding " + benefit + "@" + nr
                        + " benefit/fee for king move from level " + currentKingDangerLevel + " towards level " + dangerLevelHere + " more/less dangerous square " + squareName(getMyPos()) + " for " + attacker + ".");
            attacker.addChance(benefit, nr);
        }

        // award pinning opponent piece to its king
        int benefit = 0;
        int benefit1 = 0;
        // Benefit for checking or attacking the opponents king -- be aware: normal relEval on king is often negative, as it is assumed that a king always needs to move away (out of check) when threatened.
        // this it not fully compensated/covered in checking/checkblocking method
        if ((attackerIsWhite ? myPieceType() == KING_BLACK : myPieceType() == KING) // increase attack on opponent King, which resides on this square
                && attackerRmd.hasExactlyOneFromToAnywhereCondition()  // needs one to move away - this is the pinned piece!
        ) {
            Square pinnedSquare = board.getBoardSquare(attackerRmd.getFromCond(0));
            if (pinnedSquare.myPiece() != null // Todo: this should not be possible, but due to a bug it sometimes still is
                    && pinnedSquare.myPiece().color() != attacker.color()
                    // not necessary as relEval would also be close to 0 then: && pinnedSquare.myPieceType() != attacker.getPieceType()  // cannot pin same piece type  // todo, test if it can strike back, e.g. q cannot pin l on diagonal line
            ) {    // it's a king-pin!
                // TODO!: this awards king-pins, but does not help to avoid them unless a move hinders/blocks the attacker move), but this could even be suicide. Actually king or pinnd piece should walk away
                VirtualPieceOnSquare attackerAtFromCond = pinnedSquare.getvPiece(attacker.getPieceID());
                debugPrint(DEBUGMSG_MOVEEVAL, "  Pin or possibility to pin " + pinnedSquare + " to king detected: "
                    /*+ Arrays.toString(attackerAtFromCond.getRawMinDistanceFromPiece().getLastMoveOrigins().stream()
                        .filter(vPce -> calcDirFromTo(vPce.getMyPos(),pinnedSquare.getMyPos())
                                == calcDirFromTo(pinnedSquare.getMyPos(), getMyPos()) ).toArray())
                    + " // " + Arrays.toString( attackerAtFromCond.getRawMinDistanceFromPiece().getLastMoveOrigins().stream()
                        .filter(vPce -> calcDirFromTo(vPce.getMyPos(),pinnedSquare.getMyPos())
                                == calcDirFromTo(pinnedSquare.getMyPos(), getMyPos()) )
                        .map( vPce -> Integer.valueOf(board.getBoardSquare(vPce.getMyPos())
                                .getvPiece(pinnedSquare.getPieceID())
                                .coverOrAttackDistance()) ).toArray() ) + " " */);
                if (!evalIsOkForColByMin(attackerAtFromCond.getRelEval(), attacker.color())
                        || attackerAtFromCond.getRawMinDistanceFromPiece().hasNoGo())
                    return; //continue;
                benefit1 = attackerAtFromCond.getRelEval(); //abs(pinnedSquare.myPiece().getValue()) - abs(attacker.myPiece().getValue());
                if (attacker.attackViaPosTowardsHereMayFallVictimToSelfDefence(attackerRmd.getFromCond(0))
                ) {
                    int takeBack = -attacker.getValue() - pinnedSquare.myPiece().getValue(); // loosing pinned piece and attacker
                    if (DEBUGMSG_MOVEEVAL)
                        debugPrint(DEBUGMSG_MOVEEVAL, " so: taking back == " + takeBack + " vs. " + benefit1 + " = ");
                    if (pinnedSquare.myPiece().isWhite())
                        benefit1 = max(benefit1, takeBack);
                    else
                        benefit1 = min(benefit1, takeBack);
                    if (DEBUGMSG_MOVEEVAL)
                        debugPrintln(DEBUGMSG_MOVEEVAL, "" + benefit1 + ". ");
                }
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
                    if ( (attackerRmd.dist() == 1 || attackerRmd.dist() == 2) ) { // not already pinning
                        if (DEBUGMSG_MOVEEVAL && abs(benefit1) > DEBUGMSG_MOVEEVALTHRESHOLD)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " Adding " + benefit1 + "@" + pinFutureLevel
                                    + " benefit for pinning chance with move towards " + squareName(getMyPos()) + " for " + attacker + ".");
                        attacker.addChance(benefit1, pinFutureLevel, pinnedSquare.getMyPos());
                    }
                    // let's do this in any case - no more: if (attackerRmd.dist() == 2) {  // in the case of ==1 it is too late already...
                        // motivate king to move away:
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " + " + (-benefit1 >> 1) + "@" + pinFutureLevel + " motivation for king to move away from pin.");
                        board.getPieceAt(getMyPos()).addMoveAwayChance2AllMovesUnlessToBetween(
                                -benefit1 >> 1, pinFutureLevel,
                                pinnedSquare.getMyPos(),
                                getMyPos() + calcDirFromTo(pinnedSquare.getMyPos(), getMyPos()),
                                false,  // to on behing/through the king - hope this works at the boarder of the board...
                                getMyPos());
                        // motivate this piece here to move away, too
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " + " + (-benefit1 >> 1) + "@" + pinFutureLevel + " motivation for piece " + pinnedSquare.myPiece() + " on " + squareName(pinnedSquare.getMyPos()) + " to move away from pin.");
                        pinnedSquare.myPiece().addMoveAwayChance2AllMovesUnlessToBetween(
                                -benefit1 >> 1, pinFutureLevel,
                                pinnedSquare.getMyPos(),
                                getMyPos() + calcDirFromTo(pinnedSquare.getMyPos(), pinnedSquare.getMyPos()),  // to one square behind/through the king - hope this works at the boarder of the board...
                                false,
                                getMyPos());
                    //}
                }
            }

            // simple checking benefit
            benefit = (positivePieceBaseValue(PAWN) >> 1)   // 50 reduced by opponents necessary aid
                    / (1 + attackerRmd.countHelpNeededFromColorExceptOnPos(kcol, getMyPos()));
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
            if (DEBUGMSG_MOVEEVAL && abs(benefit) > DEBUGMSG_MOVEEVALTHRESHOLD)
                debugPrintln(DEBUGMSG_MOVEEVAL, " Adding " + benefit + "@" + nr
                        + " benefit for move towards " + squareName(getMyPos()) + " for " + attacker + " for king attack.");
            attacker.addChance(benefit, nr, board.getKingPos(kcol));
        }

        //---- hanging pieces behind king

        if (!isSquareEmpty()
                && myPiece().color() != attacker.color()
                && !isKing(myPieceType())
                && isSlidingPieceType(attacker.getPieceType())
                && !attackerRmd.hasNoGo()
                && (attackerRmd.dist() > 0 && attackerRmd.dist() <= 3) // not already pinning
                && attackerRmd.hasExactlyOneFromToAnywhereCondition()  // needs king to move away - this opens the way to the piecce here
                && attackerRmd.getFromCond(0) == board.getKingPos(opponentColor(attacker.color()))
                && evalIsOkForColByMin(attacker.getRelEvalOrZero(), attacker.color(), -EVAL_TENTH)
        ){
            // the only fromCond indicates that the piece here can directly (or in 1 more move) give check and after king
            // moves away take or threaten here.
            // beware: could also be ok, as maybe the check can easily be blocked by a third of my pieces -> see block check

            // let's start easy:

            final int kingPos = board.getKingPos(myPiece().color());
            if (kingPos<0)
                return;  // no king, may only happen in some test cases...
            VirtualPieceOnSquare attackerAtKingPos = board.getBoardSquare(kingPos).getvPiece(attacker.getPieceID());
            if ( attackerAtKingPos.getRawMinDistanceFromPiece().dist() != attackerRmd.dist() )
                return;  // king is not in the last path, but somewhere earlier, so it is not what we are looking for...

            int nowAchieveableDist2OppK = board.distanceToKing(getMyPos(), opponentColor(acol));
            if (myPiece().color()==board.getTurnCol())
                nowAchieveableDist2OppK--;
            int defendBenefit = -attacker.getRelEvalOrZero();
            final int fl = attackerRmd.dist() - 2;
            int attackingPos = attacker.getClosestLastMoveOriginInDir(calcDirFromTo(kingPos, getMyPos()));
            if (attackingPos < 0) {
                /*board.internalErrorPrintln("Error in treating hanging pieces behind king, attackingPos==" + attackingPos
                        + " for " + attacker + " towards king at " + squareName(kingPos)
                        + " / " + attackerAtKingPos + " and " + squareName(getMyPos()) + "."); */
                return;
            }
            VirtualPieceOnSquare attackerAtAttackingPos = board.getBoardSquare(attackingPos).getvPiece(attacker.getPieceID());
            int countBlockers = attackerAtKingPos.addBenefitToBlockers(attackingPos, fl, 0);
            if ( fl == 0 ) {
                // no more time. let's try to block
                defendBenefit -= defendBenefit >> 4;  // *0.94
                if (countBlockers == 0) {
                    //board.internalErrorPrintln("NO ERROR :-) Found non-blockable attack through king.");
                    if (DEBUGMSG_MOVEEVAL)
                        debugPrint(DEBUGMSG_MOVEEVAL, "Non blockable ");
                } /* else if (countBlockers > 0) {
                    //defendBenefit -= defendBenefit >> 3;  // *0.83
                    defendBenefit /= 2 + countBlockers;
                } */
                if (DEBUGMSG_MOVEEVAL && abs(defendBenefit) > 3)
                    debugPrint(DEBUGMSG_MOVEEVAL, " Through-king attack from " + squareName(attackingPos) + " by " + attackerAtKingPos);
                attackerAtKingPos.addBenefitToBlockers(attackingPos, fl, defendBenefit);
            }
            else {
                defendBenefit >>= fl;
                if (fl >= 2)  // getting trapped is still quite far away, traps are probably not long lived
                    defendBenefit = (defendBenefit >> 2) + (defendBenefit >> (fl - 1));
                debugPrint(DEBUGMSG_MOVEEVAL, " Future attack");
            }
            if (DEBUGMSG_MOVEEVAL) {
                if (abs(defendBenefit) > 3)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " (" + (-defendBenefit) + "@" + fl + ") through king possible on " + squareName(getMyPos())
                            + " by " + attacker + ": ");
                else
                    debugPrintln(DEBUGMSG_MOVEEVAL, ".");
            }
            if ( attackerRmd.dist()>=2 ) {
                int attackBenefit = -defendBenefit;
                if (countBlockers == 1)
                    attackBenefit -= attackBenefit>>2;  // *0.74
                else if (countBlockers > 1)
                    attackBenefit /= countBlockers;
                if (nowAchieveableDist2OppK <= 2 && abs(attacker.getValue()) > abs(myPiece().getValue()) )
                        attackBenefit >>= 2;   // todo: assumes king can cover enough, but this must not be true...
                // motivate the attacker  // todo: should better motivate towards the checking place!
                if (DEBUGMSG_MOVEEVAL && abs(attackBenefit)>DEBUGMSG_MOVEEVALTHRESHOLD )
                    debugPrintln(DEBUGMSG_MOVEEVAL," * Through-king-attack benefit of " + (attackBenefit)
                            + "@" + (attackerRmd.dist()-2) + " for " + attackerAtAttackingPos + ".");
                attackerAtAttackingPos.addChance(attackBenefit, attackerRmd.dist()-2);
                /*attacker.addChance(attackBenefit>>1, attackerRmd.dist()-2);
                attackerAtKingPos.addChance(attackBenefit>>1, attackerRmd.dist()-2); */
            }
            if ( nowAchieveableDist2OppK==1 && attackerRmd.dist()==1 ) {
                // already checking, but king can maybe cover the hanging piece
                if (DEBUGMSG_MOVEEVAL && abs(defendBenefit)>DEBUGMSG_MOVEEVALTHRESHOLD )
                    debugPrintln(DEBUGMSG_MOVEEVAL," * Try to at least to cover hanging piece defendBenefit of " + (defendBenefit>>1) + "@" + (0) + " for " +
                            getvPiece(board.getPieceIdAt(kingPos))  + ".");
                getvPiece(board.getPieceIdAt(kingPos)).addChance( defendBenefit>>1, 0 );
            }
            else if ( nowAchieveableDist2OppK>1
                    && attackerRmd.dist()>1
                    && nowAchieveableDist2OppK-1<=attackerRmd.dist()) {
                // not already checking, but king in time to cover the hanging piece
                if (DEBUGMSG_MOVEEVAL && abs(defendBenefit)>DEBUGMSG_MOVEEVALTHRESHOLD )
                    debugPrintln(DEBUGMSG_MOVEEVAL," * Covering defendBenefit of " + (defendBenefit>>1) + "@" + (0) + " for " +
                            getvPiece(board.getPieceIdAt(kingPos))  + ".");
                // or move piece away  // todo: away is very unspecific here, could lead to places with same problem, should be selective
                getvPiece(board.getPieceIdAt(kingPos)).addChance( defendBenefit>>1, 0 );
                if (DEBUGMSG_MOVEEVAL && abs(defendBenefit)>3 )
                    debugPrint(DEBUGMSG_MOVEEVAL," * Move away defendBenefit of " + (defendBenefit>>1) + "@" + (0) + " for " +  myPiece()  + ".");
                myPiece().addMoveAwayChance2AllMovesUnlessToBetween(
                        defendBenefit>>1, 0,
                        NOWHERE, NOWHERE, false,
                        getMyPos());
            }
            else { // if ( nowAchieveableDist2OppK > attackerRmd.dist()+1 ) {
                // king cannot cover in time, but king or piece can move away
                // todo: away is very unspecific here, could lead to places with same problem, should be selective
                if (DEBUGMSG_MOVEEVAL && abs(defendBenefit)>3 )
                    debugPrint(DEBUGMSG_MOVEEVAL," * Move away defendBenefit of " + (defendBenefit>>1) + "@" + (0) + " each for " + board.getPieceAt(kingPos) + " AND " + myPiece()  + ".");
                if ( distanceBetween(attackingPos, kingPos)==2
                        && board.getBoardSquare(attackingPos).walkable4king(kcol)
                ) { // king may go to denf the square himmself
                    board.getPieceAt(kingPos).addMoveAwayChance2AllMovesUnlessToBetween(
                            defendBenefit>>1, 0,
                            NOWHERE, NOWHERE, false,
                            getMyPos() );
                } else {
                    board.getPieceAt(kingPos).addMoveAwayChance2AllMovesUnlessToBetween(
                            defendBenefit >> 1, 0,
                            attackingPos, kingPos, false,
                            getMyPos() );
                    myPiece().addMoveAwayChance2AllMovesUnlessToBetween(
                            defendBenefit >> 1, 0,
                            NOWHERE, NOWHERE, false,
                            getMyPos() );
                }
            }
        }
    }

    private int getKingAreaBenefit(VirtualPieceOnSquare attacker, boolean kingCol) {
        //TODO: refactor/check that this method has a very similar intention than ableToTakeControlBonus around king in calcExtraBenefits
        boolean acol = attacker.color();
        ConditionalDistance attackerRmd = attacker.getRawMinDistanceFromPiece();
        boolean oppCol = opponentColor(acol);

        if (DEBUGMSG_MOVEEVAL && board.getKingPos(kingCol) == getMyPos() ) {
            debugPrintln(DEBUGMSG_MOVEEVAL, " FYI: King safety on " + squareName(getMyPos())
                    + " is " + board.getNrOfKingAreaDefends(kingCol) + " - " + board.getNrOfKingAreaAttacks(kingCol)
                    + " = " + board.getKingSafetyEstimation(kingCol) + ".");
        }
        if (board.distanceToKing(getMyPos(), kingCol)!=1         // we are not close to king
                || isKing(attacker.getPieceType()) )  // and do not attack king with other king or itself...
            return 0;

        // give contribution to keep attacking/defending around the king
        // did test series from 0-54, but almost no difference, even may be slightly worse with more
        // try for attacking only:
        if (attacker.coverOrAttackDistance() == 1 && acol != kingCol) {
            int contrib = EVAL_TENTH + (EVAL_TENTH>>1);  // 15
            if (isBlack(attacker.color()))
                contrib = -contrib;
            attacker.addClashContrib(contrib);
        }
///
        int benefit =  ( (EVAL_TENTH<<2) - (EVAL_TENTH>>2) )  // 38
                    /(1+ attackerRmd.countHelpNeededFromColorExceptOnPos(kingCol,getMyPos()));
/*
        if ( countDirectAttacksWithColor(acol) == 0 ) {
            if ( countDirectAttacksWithColor(kingCol ) >1 )
                benefit += benefit >> 1;           // *1.25 because we do not yet cover this square at all, but it is well defended
            else
                benefit += benefit >> 1;           // *1.5 because we do not yet cover this square at all
        }
        if ( countDirectAttacksWithColor(acol) == countDirectAttacksWithColor(kingCol))
            benefit += benefit>>2; // + benefit>>3;  // *1.37 because up to now we only cover this square lest often than the enemy
        else if ( countDirectAttacksWithColor(acol) < countDirectAttacksWithColor(kingCol))
            benefit += benefit>>2;  // *1.25 because up to now we only cover this square lest often than the enemy
        int alreadyAttacking = board.getNrOfKingAreaAttacks(kingCol);
*/
        if ( countDirectAttacksWithColor(acol) == 0 )
            benefit += benefit >> 1;           // *1.5 because we do not yet cover this square at all
        if ( acol == kingCol ) {
            if (countDirectAttacksWithColor(acol) > countDirectAttacksWithColor(oppCol) + (acol==board.getTurnCol() ? 0 : 1)) {
                if (countDirectAttacksWithColor(acol) > countDirectAttacksWithColor(oppCol) + (acol==board.getTurnCol() ? 0 : 1) + 2)
                    benefit >>= 1;  // *0.5 because we are covering this square often enough by far
                else
                    benefit -= benefit >> 2;  // *0.75 because we are covering this square often enough
            }
            int kingSafety = board.getKingSafetyEstimation(kingCol);
            if ( kingSafety > 3 && board.getNrOfKingAreaAttacks(acol) <= 3 )
                benefit = (benefit>>1) + (benefit >> (kingSafety-2));  // - for anyway good own king safety
            int oppKingSafety = board.getKingSafetyEstimation(oppCol);
            if ( oppKingSafety < -4 &&  oppKingSafety < kingSafety+2 )
                benefit = (benefit>>1) + (benefit >> (-oppKingSafety-3));  // - for bad opponents king safety -> so we take more risk
        }
        else {
            if ( countDirectAttacksWithColor(acol) <= countDirectAttacksWithColor(oppCol) ) {
                if (countDirectAttacksWithColor(acol) < countDirectAttacksWithColor(oppCol) - 2)
                    benefit += benefit >> 2;  // *1.25 because even after covering, we will cover this square lest often than the opponent
                else
                    benefit += benefit >> 2;  // *1.5 because up to now we only cover this square lest often than the opponent, but can keep up
            }
        }
        // if ( countDirectAttacksWithColor(acol) <= countDirectAttacksWithColor(oppCol))
        //    benefit += benefit>>3;  // *1.12 because up to now we only cover this square lest often than the enemy
        int alreadyAttacking = board.getNrOfKingAreaAttacks(kingCol);
        int attackerDist = attackerRmd.dist();
        if ( isSlidingPieceType(attacker.getPieceType())
                && attackerDist > 1
                && alreadyAttacking < 4  // motivate to start attacking
                && acol != kingCol )
            benefit += benefit/attackerDist;
        if (attackerDist<=1)  //  not no much for moving directly next to the king
            benefit >>= 1;
        if (attackerRmd.hasNoGo())
            benefit >>= 3;
        /*if (!attacker.getMinDistanceFromPiece().hasNoGo())
            benefit += benefit >> 2;*/
        if ( isBlack(acol) )
            benefit = -benefit;
        if (DEBUGMSG_MOVEEVAL && abs(benefit)>4)
            debugPrintln(DEBUGMSG_MOVEEVAL,"  " + benefit + " benefit for move towards "+ squareName(getMyPos())
                +" for " + attacker +" for near king " + (acol!=kingCol ? "attack":"coverage") + ".");
        return benefit;
    }

    public int getAttacksValueForColor(final boolean color) {
        return countDirectAttacksWithColor(color)
                + ( (int)((coverageOfColorPerHops.get(2).get(colorIndex(color)).size()))/2 );
    }

    public int getFutureDangerValueThroughColor(final boolean color) {
        int res = 0;  // coverageOfColorPerHops.get(1).get(colorIndex(color)).size();  // is 2nd row relevant here?
        res += coverageOfColorPerHops.get(2).get(colorIndex(color)).size() << 1;   // the 1-move away attackers *2
        res += (coverageOfColorPerHops.get(3).get(colorIndex(color)).size()+1)>>1; // the 2-move aways /2
        return res;
    }

    public boolean extraCoverageOfKingPinnedPiece(final boolean covererColor) {
        return extraCoverageOfKingPinnedPiece[colorIndex(covererColor)];
    }

    public boolean walkable4king(final boolean kingColor) {
        return !extraCoverageOfKingPinnedPiece(opponentColor(kingColor))
               && countDirectAttacksWithout2ndRowWithColor(opponentColor(kingColor)) == 0;
    }

    public int countDirectAttacksWithColor(final boolean color) {
        return coverageOfColorPerHops.get(0).get(colorIndex(color)).size()
                + coverageOfColorPerHops.get(1).get(colorIndex(color)).size();
    }

    public int countFutureAttacksWithColor(final boolean color, final int atDist) {
        return coverageOfColorPerHops.get(atDist).get(colorIndex(color)).size();
    }

    public int countDirectAttacksWithout2ndRowWithColor(final boolean color) {
        return coverageOfColorPerHops.get(0).get(colorIndex(color)).size();
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
                "" + squareName(getMyPos()) +
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
       /* did not improve, see 0.47u113 and u114
       int corr = 0;
        if (clashMoves!=null && clashMoves.size()>0) {
            corr = EVAL_TENTH*3; //EVAL_DELTAS_I_CARE_ABOUT - (EVAL_DELTAS_I_CARE_ABOUT >> 2);
            if ( abs(clashEvalResult) > (positivePieceBaseValue(PAWN)<<1) )
                corr <<= 1;
            if (isBlack((lastTakersColor())))
                    corr = -corr;
        }
        return clashEvalResult+corr;
        */
        return clashEvalResult;
    }

    private boolean lastTakersColor() {
        return board.getPieceAt(clashMoves.get(clashMoves.size() - 1).from()).color();
    }

    public boolean isPceTypeOfFirstClashMove(int pceType) {
        if (clashMoves==null || clashMoves.size()==0)
            return false;
        return pceType == board.getPieceTypeAt(clashMoves.get(0).from() );
    }

    public boolean clashWinsTempo() {
        if (clashMoves==null || clashMoves.size()==0)
            return false;
        return board.getPieceAt(clashMoves.get(0).from()).color() != lastTakersColor();
    }

    public boolean isPartOfClash(int pceId) {
        if (clashMoves==null || clashMoves.size()==0)
            return false;
        return clashMoves.stream().anyMatch(m -> board.getPieceIdAt(m.from()) == pceId );
    }

    public int reasonableClashLength() {
        if (clashMoves==null)
            return 0;
        else
            return clashMoves.size();
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

    public List<VirtualPieceOnSquare> directAttackVPcesWithout2ndRowWithColor(boolean color) {
        return coverageOfColorPerHops.get(0).get(colorIndex(color));
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
                    && fileOf(vPce.getMyPiecePos())!=fileOf(vPce.getMyPos())
            ) {
                return true;
            }
        return false;
    }

    public void evalContribBlocking() {
        if (isSquareEmpty())
            return;
        for ( VirtualPieceOnSquare vPce : vPieces ) {
            if (vPce == null || vPce.getRawMinDistanceFromPiece().dist() != 1
                || !vPce.getRawMinDistanceFromPiece().isUnconditional()
                || !isSlidingPieceType(vPce.getPieceType()) )
                continue;
            int blockingFee = -vPce.getClashContribOrZero();
            if ( evalIsOkForColByMin( -blockingFee, vPce.color(), -EVAL_HALFAPAWN) ) {
                // vPce has a Contribution here, nobody should block this way...
                debugPrintln(DEBUGMSG_MOVEEVAL,"scan blocking of contribution of " + blockingFee
                        + " of " + vPce + ".");
                blockingFee -= blockingFee >> 3;  // * 0.87
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
            boolean blockerIsFriend =  blocker.color() == board.getPiece(contributorId).color();
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
            // did not improve, but very slightly negative 48h53b
            // if (!blockerIsFriend)
            //    inFutureLevel++; // if I block my opponents contribution, this is good, but it will be his turn so fl+1
            if (inFutureLevel>MAX_INTERESTING_NROF_HOPS)
                continue;
            if (inFutureLevel<0)
                inFutureLevel=0;
            ConditionalDistance rmd = blocker.getRawMinDistanceFromPiece();
            if (DEBUGMSG_MOVEEVAL && abs(blockingFee)>DEBUGMSG_MOVEEVALTHRESHOLD)
                debugPrintln(DEBUGMSG_MOVEEVAL," " + blockingFee + "@"+inFutureLevel+" Fee for blocking a contribution on square "+ squareName(getMyPos())+" with " + blocker + ".");
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
                        && attacker.getRawMinDistanceFromPiece().getToCond(0 )==getMyPos()) )
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
                        forkingDanger >>= 3;
                    if ( !evalIsOkForColByMin(forkingDanger, vPce.color()) ) {
                        if (DEBUGMSG_MOVEEVAL && abs(forkingDanger)>DEBUGMSG_MOVEEVALTHRESHOLD)
                            debugPrintln(DEBUGMSG_MOVEEVAL," " + forkingDanger + "@0 danger moving " + vPce
                                    + " into possible fork on square "+ squareName(getMyPos())+ " by " + attackerAtLMO + ".");
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
            benefit -= benefit>>2;  // -> 8
            if ( rmd.dist() == 0 ) {
                // motivate to move away
                ChessPiece piece2Bmoved = board.getPieceAt(getMyPos());
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL," " + benefit + "@1 motivation for " + vPce + " to clear king castling area.");
                piece2Bmoved.addMoveAwayChance2AllMovesUnlessToBetween(
                        benefit, 1,
                        NOWHERE, NOWHERE, false,
                        board.getKingPos(myPiece().color()) );
            }
            else if ( rmd.dist() == 1 && rmd.isUnconditional() ) {
                // motivate to not move here
                benefit >>= 1; // -> 7
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL," " + (-benefit) + "@1 warning to " + vPce + " to keep king castling area clear.");
                vPce.addRawChance(-benefit, 1, board.getKingPos(col));
            }
        }
    }

    public int getEnablingFromConditionForVPiece(VirtualPieceOnSquare vPce) {
        boolean color = vPce.color();
        if ( evalIsOkForColByMin(vPce.getRelEvalOrZero(), vPce.color()) )
            return NOWHERE; // no condition needed, I can go there anyway...
        // Todo!: better Conds needed to express moving away from an axis. Then this should return
        //  the vPce, to enable the the aller to build sich a more pecise condition.
        int fromCond = NOWHERE;
        List<VirtualPieceOnSquare> whites = coverageOfColorPerHops.get(0).get(CIWHITE);
        List<VirtualPieceOnSquare> blacks = coverageOfColorPerHops.get(0).get(CIBLACK);
        List<VirtualPieceOnSquare> whiteOthers = null;
        List<VirtualPieceOnSquare> blackOthers = null;
        List<Move> moves = new ArrayList<>();
        moves.add(new Move( vPce.getMyPiecePos(), getMyPos()));
        for (VirtualPieceOnSquare vPceToMoveAway : vPieces) {
            if ( vPceToMoveAway == null  // contained in color comparison: || vPceToMoveAway == vPce
                    ||  vPceToMoveAway.color() == color   // Todo: check if also adding same color could be regarded as an enabing condition, but then a to condition of same color
                    ||  vPceToMoveAway.getRawMinDistanceFromPiece().dist() != 1
                    || ( ! vPceToMoveAway.getRawMinDistanceFromPiece().isUnconditional()
                          && !( isPawn( vPceToMoveAway.getPieceType()) &&  vPceToMoveAway.getRawMinDistanceFromPiece().hasExactlyOneFromAnywhereToHereCondition() ) )
            )
                continue;
            if (fromCond==NOWHERE) {
                // init only once at the first time, but nnot before the for loop, as it might never be necessary
                whiteOthers = new ArrayList<>(coverageOfColorPerHops.get(1).get(CIWHITE));
                blackOthers = new ArrayList<>(coverageOfColorPerHops.get(1).get(CIBLACK));
            }
            int resWithout = calcClashResultExcludingOne(opponentColor(color), vPce, whites, blacks,
                    vPceToMoveAway, whiteOthers, blackOthers, moves );
            // Todo: there can actually be several alternative conditions that fulfill this, but currently
            //  ConditionalDistance can handle only AND condiions not OR. So we pick only one here...
            if ( evalIsOkForColByMin(resWithout, color))
                return vPceToMoveAway.getMyPiecePos();
        }
        return fromCond;
    }

    boolean takingByPieceWinsTempo(ChessPiece p) {
        if (isKing(p.getPieceType()))
            return false;
        return (isPceTypeOfFirstClashMove(p.getPieceType())
                    && clashWinsTempo())  // p starts the regular clash (as calculated) and opponent needs to take back (moves last)
               || reasonableClashLength() == 0  // or reasonably there is no clash, but p seems to take anyway, so also here opponent needs to take back
               || (!isPceTypeOfFirstClashMove(p.getPieceType())   // clash was started with a more expensive piece than expected, let's see if it is "expensive enough" so that opponent would take back reasonably
                   && !evalIsOkForColByMin(getvPiece(p.getPieceID()).getRelEvalOrZero(),
                                                        p.color()) )
               || (!isPceTypeOfFirstClashMove(p.getPieceType())
                  && reasonableClashLength() > 0   // there is a longer clash, but p is a more expensive piece than expected
                  && abs(myPiece().getValue()) - EVAL_HALFAPAWN > abs( getvPiece(p.getPieceID()).getRelEvalOrZero() )
                  );  // todo: this last part is not precise,it might not be reasonable to take back, this is not played out here, but estimated via a comparison of the piece value and the relEval when taking...
    }
}
