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

import static de.ensel.tideeval.ChessBasics.*;
import static de.ensel.tideeval.ChessBoard.*;
import static de.ensel.tideeval.EvaluatedMove.addEvaluatedMoveToSortedListOfCol;
import static java.lang.Math.*;

public class ChessPiece {
    static long debug_propagationCounter = 0;
    static long debug_updateMobilityCounter = 0;

    private final ChessBoard board;
    private final int myPceType;
    private final int myPceID;
    private int myPos;
    private long latestUpdate;   // virtual "time"stamp (=consecutive number) for last/ongoing update.

    /** The Pieces mobility (=nr of squares it can safely go) on the first max three hops.
     *  Must  always be updated right after correction of relEvals.
     *  Index corresponds to the dist.  (mobilityFor3Hops[3] is nr of squares reachable with three hops)
     *  BUT: dist==1 is split into [0]->dist==1 and no condition, [1]->dist==1 but conditional
     */
    private final int[] mobilityFor3Hops;

    private HashMap<Move,int[]> movesAndChances;   // stores real moves (i.d. d==1) and the chances they have on certain future-levels (thus the Array of relEvals)
    private HashMap<Move,int[]> movesAwayChances;  // stores real moves (i.d. d==1) and the chances they have on certain future-levels concerning moving the Piece away from its origin
    private HashMap<Move,int[]> forkingChances;
    private int bestRelEvalAt;  // bestRelEval found at dist==1 by moving to this position. ==NOWHERE if no move available

    static final int KEEP_MAX_BEST_MOVES = 2;
    List<EvaluatedMove> bestMoves;

    HashMap<Move, int[]> legalMovesAndChances;


    ChessPiece(ChessBoard myChessBoard, int pceTypeNr, int pceID, int pcePos) {
        this.board = myChessBoard;
        myPceType = pceTypeNr;
        myPceID = pceID;
        myPos = pcePos;
        latestUpdate = 0;
        mobilityFor3Hops = new int[min(4,MAX_INTERESTING_NROF_HOPS)+1];
        resetPieceBasics();
    }

    private void resetPieceBasics() {
        Arrays.fill(mobilityFor3Hops, 0);
        bestRelEvalAt = POS_UNSET;
        clearMovesAndAllChances();
        resetBestMoves();
        legalMovesAndChances = null;
    }

    void resetBestMoves() {
        bestMoves = new ArrayList<>(KEEP_MAX_BEST_MOVES);
    }

    public int getValue() {
        // Todo calc real/better value of piece
        return pieceBaseValue(myPceType);
    }

    public HashMap<Move, int[]> getMovesAndChances() {
        if (movesAndChances==null || movesAndChances.size()==0)
            return null;

        return movesAndChances;
    }

    public HashMap<Move, int[]> getLegalMovesAndChances() {
        if (legalMovesAndChances!=null)
            return legalMovesAndChances;
        legalMovesAndChances = new HashMap<>(movesAndChances.size());
        for (Map.Entry<Move, int[]> e : movesAndChances.entrySet() ) {
            if (isBasicallyALegalMoveForMeTo(e.getKey().to())
                    && board.moveIsNotBlockedByKingPin(this, e.getKey().to())
                    && ( !board.isCheck(color())
                    || board.nrOfChecks(color())==1 && board.posIsBlockingCheck(color(),e.getKey().to())
                    || isKing(myPceType))
            ) {
                //System.out.println("legal move of " + toString() + ": "+e.getKey());
                legalMovesAndChances.put(e.getKey(), e.getValue());
            }
        }
        return legalMovesAndChances;
    }

        /**
     * getSimpleMobilities()
     * @return int[] for mobility regarding hopdistance i (not considering whether there is chess at the moment)
     * - i.e. result[0] reflects how many moves the piece can make fro where it stands.
     * - result[1] is how many squares can be reached in 2 hops
     *   (so one move of the above + one more OR because another figure moves out of the way)
     */
    int[] getSimpleMobilities() {
        // TODO: discriminate between a) own figure in the way (which i can control) or uncovered opponent (which I can take)
        // and b) opponent piece blocking the way (but which also "pins" it there to keep it up)
        int[] mobilityCountForHops = new int[MAX_INTERESTING_NROF_HOPS];
        for( Square sq : board.getBoardSquares() ) {
            int distance = sq.getDistanceToPieceId(myPceID);
            if (distance!=0 && distance<=MAX_INTERESTING_NROF_HOPS)
                mobilityCountForHops[distance-1]++;
        }
        return mobilityCountForHops;
    }


    /** Mobility counters need to be updated with every update wave, i.e. right after recalc of relEvals
     *
     */
    public void ORIGupdateMobilityInklMoves() {
        // little Optimization:
        // the many calls to here lead to about 15-18 sec longer for the overall ~90 sec for the boardEvaluation_Test()
        // for the std. 400 games on my current VM -> so almost 20%... with the following optimization it is reduced to
        // about +6 sec. Much better. Result is not exactly the same, it has influence on 0.001% of the evaluated boards
        if (board.currentDistanceCalcLimit() > mobilityFor3Hops.length) {
            // for optimization, we assume that if the current Distanc-Calc limit is already above the number we are
            // interested in, then nothing will change for the counts of the lower mobilities
            return;
        }
        boolean doUpdateMoveLists = (board.currentDistanceCalcLimit() == mobilityFor3Hops.length);

        // the following does not improve anything at the moment, as one of the vPves of the ChessPies has always changed
        // ... would need a more intelligent dirty-call from the vPcesa
        // if (bestRelEvalAt!=POS_UNSET)
        //    return;  // not dirty or new, so we trust the value still

        debug_updateMobilityCounter++;

        boolean prevMoveability = canMoveAwayReasonably();

        // clear mobility counter
        Arrays.fill(mobilityFor3Hops, 0);
        bestRelEvalAt = NOWHERE;

        // and re-count - should be replaced by always-up-to-date mechanism
        // TODO: Clean up and remove (almost) unused mobilityFor3Hops[]
        int bestRelEvalSoFar = isWhite() ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE;
        for( Square sq : board.getBoardSquares() ) {
            VirtualPieceOnSquare vPce = sq.getvPiece(myPceID);
            ConditionalDistance cd = vPce.getMinDistanceFromPiece();
            int distance = cd.dist();
            if (distance>0
                    && distance<mobilityFor3Hops.length
                    && distance<= board.currentDistanceCalcLimit() ) {
                if (!cd.hasNoGo()) {
                    int targetPceID = sq.getPieceID();
                    if (distance == 1
                            && cd.isUnconditional()
                            && (targetPceID == NO_PIECE_ID
                            || board.getPiece(targetPceID).color() != color())  // has no piece of my own color (test needed, because this has no condition although that piece actually has to go away first)
                    ) {
                        mobilityFor3Hops[0]++;
                        final int relEval = vPce.getRelEval();
                        if (isWhite() ? relEval > bestRelEvalSoFar
                                : relEval < bestRelEvalSoFar) {
                            bestRelEvalSoFar = relEval;
                            bestRelEvalAt = sq.getMyPos();
                        }
                        if (doUpdateMoveLists) {
                            if (abs(relEval)>3)
                                debugPrintln(DEBUGMSG_MOVEEVAL," adding releval of " + relEval + "@"+0+" as unconditional result/benefit for "+vPce+" on square "+ squareName(myPos)+".");
                            vPce.addChance(relEval, 0);
                            //addVPceMovesAndChances(vPce);
                        }
                        // vPce.addChance(relEval, 1);
                    } else
                        mobilityFor3Hops[distance]++;
                }
                else if (distance==1
                        //&& cd.isUnconditional()
                        && doUpdateMoveLists
                ) {  // although it must have NoGo, it is still a valid move...
                    if (abs(vPce.getRelEval())>3)
                        debugPrintln(DEBUGMSG_MOVEEVAL," adding releval of " + vPce.getRelEval() + "@"+0+" as conditional result/benefit for "+vPce+" on square "+ squareName(myPos)+".");
                    vPce.addChance(vPce.getRelEval(), 0);
                }
            }
        }
        if (prevMoveability != canMoveAwayReasonably()) {
            // initiate updates/propagations for/from all vPces on this square.
            board.getBoardSquares()[myPos].propagateLocalChange();
        }
    }

    /**
     * collects possible legal moves and covering places.
     * to be called once around round 3
     */
    public void prepareMobilityInklMoves() {
        boolean prevMoveability = canMoveAwayReasonably();
        bestRelEvalAt = NOWHERE;
        int bestRelEvalSoFar = isWhite() ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE;
        Arrays.fill(mobilityFor3Hops, 0);  // TODO:remove line + member
        debugPrintln(DEBUGMSG_MOVEEVAL,"Adding relevals for piece "+this+" on square "+ squareName(myPos)+".");
        for (int p=0; p<board.getBoardSquares().length; p++) {
            debugPrintln(DEBUGMSG_MOVEEVAL,"checking square "+ squareName(p)+": " + board.getBoardSquares()[p].getvPiece(myPceID) + " ("+board.getBoardSquares()[p].getvPiece(myPceID).getRelEval()+").");
            if (isBasicallyALegalMoveForMeTo(p)) {
                VirtualPieceOnSquare targetVPce = board.getBoardSquares()[p].getvPiece(myPceID);
                final int relEval = targetVPce.getRelEval();
                if (isWhite() ? relEval > bestRelEvalSoFar
                        : relEval < bestRelEvalSoFar) {
                    bestRelEvalSoFar = relEval;
                    bestRelEvalAt = p;
                }
                //if (abs(relEval)>3) {
                if (!targetVPce.getMinDistanceFromPiece().hasNoGo())
                    debugPrintln(DEBUGMSG_MOVEEVAL, "Adding releval of " + relEval + "@" + 0
                            + " as unconditional result/benefit for " + targetVPce + " on square " + squareName(myPos) + ".");
                else   // although it must have NoGo, it is still a valid move...
                    debugPrintln(DEBUGMSG_MOVEEVAL, "Adding releval of " + relEval + "@" + 0
                            + " as result/benefit despite nogo for " + targetVPce + " on square " + squareName(myPos) + ".");
                //}
                targetVPce.addChance(relEval, 0);

            }
        }
        if (prevMoveability != canMoveAwayReasonably()) {
            // initiate updates/propagations for/from all vPces on this square.
            board.getBoardSquares()[myPos].propagateLocalChange();
        }
    }

    public int getBestMoveRelEval() {
        if (bestMoves!=null && bestMoves.size()>0 && bestMoves.get(0)!=null)
            return bestMoves.get(0).getEval()[0];
        if (bestRelEvalAt==NOWHERE)
            return isWhite() ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE;
        if (bestRelEvalAt==POS_UNSET)
            return NOT_EVALUATED;
        return board.getBoardSquares()[bestRelEvalAt].getvPiece(myPceID).getRelEval();
    }


    boolean canMoveAwayReasonably() {
        //if ( getLegalMovesAndChances().size()>0 && test auf reasonable... )
        int eval = getBestMoveRelEval();
        if (eval==NOT_EVALUATED)
            return false;
        return (evalIsOkForColByMin(eval, color()));
    }

    boolean canMove() {
        if (bestMoves!=null && bestMoves.size()>0 && bestMoves.get(0)!=null)
            return bestMoves.get(0).isMove();
        return bestRelEvalAt!=NOWHERE && bestRelEvalAt!=POS_UNSET;
    }


    /**
     * getMobilities()
     * @return int for mobility regarding hopdistance 1-3 (not considering whether there is chess at the moment)
     */
    int getMobilities() {
        int mobSum = mobilityFor3Hops[0];
        for (int i=1; i<mobilityFor3Hops.length; i++)  // MAX_INTERESTING_NROF_HOPS
            mobSum += mobilityFor3Hops[i]>>(i);   // rightshift, so hops==1&conditional counts half, hops==2 counts quater, hops==3 counts only eightth...
        return mobSum;
    }
    /* was:
        {
            // TODO: (see above)
            int[] mobilityCountForHops = new int[MAX_INTERESTING_NROF_HOPS];
            for( Square sq : myChessBoard.getBoardSquares() ) {
                int distance = sq.getDistanceToPieceId(myPceID);
                int relEval = sq.getvPiece(myPceID).getRelEval();
                if (relEval!=NOT_EVALUATED) {
                    if (!isWhite())
                        relEval = -relEval;
                    if (distance!=0 && distance<=MAX_INTERESTING_NROF_HOPS
                            && relEval>=-EVAL_TENTH)
                        mobilityCountForHops[distance-1]++;
                }
            }
            // sum first three levels up into one value, but weight later hops lesser
            int mobSum = mobilityCountForHops[0];
            for (int i=1; i<=2; i++)  // MAX_INTERESTING_NROF_HOPS
                mobSum += mobilityCountForHops[i]>>(i+1);   // rightshift, so hops==2 counts quater, hops==3 counts only eightth...
            return mobSum;
        }
    */

    final int[] getRawMobilities() {
        return mobilityFor3Hops;
    }

    private void clearMovesAndAllChances() {
        // size of 8 is exacly sufficient for all 1hop pieces,
        // but might be too small for slidigPieces on a largely empty board
        clearMovesAndRemoteChancesOnly();
        movesAwayChances = new HashMap<>(8);
        legalMovesAndChances = null;
    }

    private void clearMovesAndRemoteChancesOnly() {
        movesAndChances = new HashMap<>(8);
        forkingChances = new HashMap<>(8);
    }

    void addVPceMovesAndChances() {
        for (Square sq : board.getBoardSquares()) {
            VirtualPieceOnSquare vPce = sq.getvPiece(myPceID);
            List<HashMap<Move, Integer>> chances = vPce.getChances();
            for (int i = 0; i < chances.size(); i++) {
                HashMap<Move, Integer> chance = chances.get(i);
                for (Map.Entry<Move, Integer> entry : chance.entrySet()) {
                    if (!(entry.getKey() instanceof MoveCondition)) {
                        if ( sq.getMyPos() == getPos() )
                            addMoveAwayChance(entry.getKey(), i, entry.getValue());
                        else
                            addMoveWithChance(entry.getKey(), i, entry.getValue());
                    }
                }
            }
        }
    }

    private void addMoveWithChance(Move move, int futureLevel, int relEval) {
        int[] evalsPerLevel = movesAndChances.get(move);
        if (evalsPerLevel==null) {
            evalsPerLevel = new int[MAX_INTERESTING_NROF_HOPS+1];
            evalsPerLevel[futureLevel] = relEval;
            movesAndChances.put(move, evalsPerLevel);
        }
        else {
            // todo!:  forking a king is only accounted "100" checking benefit, beause checking alone would not be much benefit -> needs an extra flag, to be treated as -> advantage is "other fork part benefit".
            int[] forkingChancePerLevel = forkingChances.get(move);
            int forkingbenefit = isWhite() ? min(relEval, evalsPerLevel[futureLevel])
                                           : max(relEval, evalsPerLevel[futureLevel]);
            if ( evalIsOkForColByMin(forkingbenefit, color(), -(positivePieceBaseValue(PAWN)-EVAL_TENTH) ) ) {
                if (forkingChancePerLevel == null)
                    forkingChancePerLevel = new int[MAX_INTERESTING_NROF_HOPS+1];
                if (forkingChancePerLevel[futureLevel] == 0) {
                    forkingChancePerLevel[futureLevel] = forkingbenefit;
                }
                else {
                    if (abs(forkingbenefit) > abs(forkingChancePerLevel[futureLevel])) {  // even better forking
                        int tmp = forkingChancePerLevel[futureLevel];
                        forkingChancePerLevel[futureLevel] = forkingbenefit;
                        forkingbenefit -= tmp; // the remaining extra improvement
                    } else {
                        forkingbenefit = 0;
                    }
                }
                //forkingbenefit >>= 1;  // for now lets be moderate in the test of the fork benefit feature...
                debugPrintln(DEBUGMSG_MOVEEVAL, "Detected forking possibility of " + forkingbenefit + "@" + (futureLevel - 1) + " for " + this.toString()
                        + " after move " + move + ".");
                if (futureLevel > 0)
                    addMoveAwayChance(move, futureLevel - 1, forkingbenefit); // add forking benefit one move earlier -> the real fork moment
            }
            evalsPerLevel[futureLevel] += relEval;
        }
    }

    private void addMoveAwayChance(Move move, int futureLevel, int relEval) {
        int[] evalsPerLevel = movesAwayChances.get(move);
        if (evalsPerLevel==null) {
            evalsPerLevel = new int[MAX_INTERESTING_NROF_HOPS+1];
            evalsPerLevel[futureLevel] = relEval;
            movesAwayChances.put(move, evalsPerLevel);
        } else {
            evalsPerLevel[futureLevel] += relEval;
        }
    }


    /**
     * die() piece is EOL - clean up
     */
    public void die() {
        // little to clean up here...
        myPos = -1;
    }


    /** ordered que  - to implement a breadth search for propagation **/

    private static final int QUE_MAX_DEPTH = MAX_INTERESTING_NROF_HOPS+3;
    private final List<List<Runnable>> searchPropagationQues = new ArrayList<>();
    {
        // prepare List of HashSets
        for (int i=0; i<QUE_MAX_DEPTH+1; i++) {
            searchPropagationQues.add(new ArrayList<>());
        }
    }

    void quePropagation(final int queIndex, final Runnable function) {
        searchPropagationQues.get(Math.min(queIndex, QUE_MAX_DEPTH)).add(function);
    }

    /**
     * Execute one stored function call from the que with lowest available index
     * it respects a calc depth limit and stops with false
     * if it has reached the limit
     * @param depth hop depth limit
     * @return returns if one propagation was executed or not.
     */
    private boolean queCallNext(final int depth) {
        List<Runnable> searchPropagationQue;
        for (int i = 0, quesSize = Math.min(depth, searchPropagationQues.size());
             i <= quesSize; i++) {
            searchPropagationQue = searchPropagationQues.get(i);
            if (searchPropagationQue != null && searchPropagationQue.size() > 0 ) {
                //System.out.print(" (L"+i+")");
                debug_propagationCounter++;
                searchPropagationQue.get(0).run();
                searchPropagationQue.remove(0);
                return true;  // end loop, we only work on one at a time.
            }
        }
        return false;
    }

    /**
     * executes all open distance calculations and propagations up to the
     * boards currentDistanceCalcLimit()
     */
    public void continueDistanceCalc() {
        continueDistanceCalc(board.currentDistanceCalcLimit());
    }

    public void continueDistanceCalc(int depthlimit) {
        int n = 0;
        startNextUpdate();
        while (queCallNext(depthlimit))
            debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, " Que:" + (n++));
        if (n>0)
            debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, " QueDone: " + n);
        endUpdate();
    }

    /** Orchestrate update of distances for this Piece in all its vPieces after a move by another piece
     * @param frompos from this position
     * @param topos to this one.
     */
    public void updateDueToPceMove(final int frompos, final int topos) {
        startNextUpdate();
        Square[] squares = board.getBoardSquares();
        VirtualPieceOnSquare fromVPce = squares[frompos].getvPiece(myPceID);
        VirtualPieceOnSquare toVPce   = squares[topos].getvPiece(myPceID);

        if (squares[topos].getPieceID()==myPceID) {
            // I myself was the moved piece, so update is handled the old style:
            toVPce.myOwnPieceHasMovedHereFrom(frompos);
        }
        else {
            // for all other pieces, there are two changes on the board:
            ConditionalDistance d1 = fromVPce.getMinDistanceFromPiece();
            ConditionalDistance d2 = toVPce.getMinDistanceFromPiece();

            // depending on if the piece moves towards me or further away, we have to adapt the update order
            VirtualPieceOnSquare startingVPce;
            VirtualPieceOnSquare finalizingVPce;
            if (d1.cdIsSmallerThan(d2)
                    || !(d2.cdIsSmallerThan(d1))
                    && toVPce.isUnavoidableOnShortestPath(frompos,MAX_INTERESTING_NROF_HOPS)) {
                startingVPce = fromVPce;
                finalizingVPce = toVPce;
            } else {
                startingVPce = toVPce;
                finalizingVPce = fromVPce;
            }
            // the main update
            startingVPce.resetDistances();
            startingVPce.recalcRawMinDistanceFromNeighboursAndPropagate();
            // the queued recalcs need to be calced first, othererwise the 2nd step would work on old data
            continueDistanceCalc(MAX_INTERESTING_NROF_HOPS); // TODO-Check: effect on time? + does it break the nice call order of continueDistanceCalcUpTo()?
//TODO-BUG!! Check:solved? see SquareTest.knightNogoDist_ExBugTest() and Test above.
// Reason is here, that Moving Piece can make NoGos and thus prolongen other pieces distances.
// However, here it is assumend that only shortended distances are propagated.

            // then check if that automatically reached the other square
            // but: we do not check, we always call it, because it could be decreasing, while the above call was increasing (and can not switch that)
            // if (finalizingVPce.getLatestChange() != getLatestUpdate()) {
            // sorry, updates also have to be triggered here
            endUpdate();
            startNextUpdate();
            finalizingVPce.resetDistances();
            finalizingVPce.recalcRawMinDistanceFromNeighboursAndPropagate();
            //}
        }
        endUpdate();
    }



    public boolean pawnCanTheoreticallyReach(final int pos) {
        //TODO: should be moved to a subclass e.g. PawnChessPiece
        assert(colorlessPieceType(myPceType)==PAWN);
        int deltaFiles = abs( fileOf(myPos) - fileOf(pos));
        int deltaRanks;
        if (this.isWhite())
            deltaRanks = rankOf(pos)-rankOf(myPos);
        else
            deltaRanks = rankOf(myPos)-rankOf(pos);
        return (deltaFiles<=deltaRanks);
    }


    public long getLatestUpdate() {
        return latestUpdate;
    }

    public long startNextUpdate() {
        latestUpdate = board.nextUpdateClockTick();
        return latestUpdate;
    }

    public void endUpdate() {
    }

    public int getPieceType() {
        return myPceType;
    }

    public int getPieceID() {
        return myPceID;
    }

    @Override
    public String toString() {
        return pieceColorAndName(myPceType);
    }

    public boolean color() {
        return colorOfPieceType(myPceType);
    }

    int baseValue() {
        return pieceBaseValue(myPceType);
    }

    int reverseBaseEval() {
        return reversePieceBaseValue(myPceType);
    }


    public int getPos() {
        return myPos;
    }

    public void setPos(int pos) {
        myPos = pos;
        resetPieceBasics();
    }

    public boolean isWhite() {
        return ChessBasics.isPieceTypeWhite(myPceType);
    }

    public char symbol() {
        return fenCharFromPceType(getPieceType());
    }

    public String getMovesAndChancesDescription() {
        StringBuilder res = new StringBuilder(""+movesAndChances.size()+" moves: " );
        for ( Map.Entry<Move,int[]> m : movesAndChances.entrySet() ) {
            res.append(" " + m.getKey()+"=");
            if (m.getValue().length>0 && abs(m.getValue()[0])<checkmateEval(BLACK)+ pieceBaseValue(QUEEN) ) {
                for (int eval : m.getValue())
                    res.append((eval == 0 ? "" : eval) + "/");
            } else {
                res.append("no");
            }
        }
        res.append(" therein for moving away: ");
        for ( Map.Entry<Move,int[]> m : movesAwayChances.entrySet() ) {
            res.append(" " + m.getKey()+"=");
            for ( int eval : m.getValue() )
                res.append( (eval==0?"":eval)+"/");
        }
        return new String(res);
    }

    public int staysEval() {
        return board.getBoardSquares()[myPos].clashEval(); // .getvPiece(myPceID).getRelEval();
    }

    public void collectMoves() {
        clearMovesAndAllChances();
        // collect moves and their chances from all vPces
        for (Square sq : board.getBoardSquares()) {
            sq.getvPiece(myPceID).resetChances();
            //careful, this also adds illegal moves "onto" own pieces (needed to keep+calc their clash contributions)
            if (sq.getvPiece(myPceID).getRawMinDistanceFromPiece().dist()==1) {  // TODO!: test with filter for only unconditional here
                addMoveWithChance(new Move(myPos,sq.getMyPos()), 0, 0);
                if (isBasicallyALegalMoveForMeTo(sq.getMyPos())) {

                }
            }
        }
    }

    /*
    boolean isBasicallyALegalMoveForMeTo(Square sq) {
        ConditionalDistance rmd = sq.getvPiece(myPceID).getRawMinDistanceFromPiece();
        return rmd.dist() == 1
                && rmd.isUnconditional()
                && !(board.hasPieceOfColorAt(color(), sq.getMyPos())   // not: square blocked by own piece
                     || (isKing(myPceType)                         // and not:  king tries to move to a checked square
                         && sq.countDirectAttacksWithColor(opponentColor(color())) > 0)
                    )
                && !(isPawn(myPceType)
                     && fileOf(sq.getMyPos()) != fileOf(myPos)
                     && !board.hasPieceOfColorAt(opponentColor(color()), sq.getMyPos())
                    );
    }
     */

    boolean isBasicallyALegalMoveForMeTo(int topos) {
        Square sq = board.getBoardSquares()[topos];
        ConditionalDistance rmd = sq.getvPiece(myPceID).getRawMinDistanceFromPiece();
        return rmd.dist() == 1
                && rmd.isUnconditional()
                && !(board.hasPieceOfColorAt(color(), topos))   // not: square blocked by own piece
                && !(isKing(myPceType)                         // and not:  king tries to move to a checked square
                         && (sq.countDirectAttacksWithout2ndRowWithColor(opponentColor(color())) >= 1
                             || sq.countDirectAttacksWithout2ndRowWithColor(opponentColor(color()))==0
                                && sq.attackByColorAfterFromCondFulfilled(opponentColor(color()),myPos)
                            )
                    )
                && !(isPawn(myPceType)
                     && fileOf(topos) != fileOf(myPos)
                     && !board.hasPieceOfColorAt(opponentColor(color()), topos)
                    );
    }

    public void mapLostChances() {
        // calculate into each first move, that it actually looses (or prolongs) the chances of the other first moves
        HashMap<Move,int[]> simpleMovesAndChances = movesAndChances;
        clearMovesAndRemoteChancesOnly();
        for (Map.Entry<Move, int[]> m : simpleMovesAndChances.entrySet())  {
            debugPrintln(DEBUGMSG_MOVEEVAL,"Map lost chances for: "+ m.getKey() + "=" + Arrays.toString(m.getValue()) +".");
            if ( abs(m.getValue()[0]) < checkmateEval(BLACK)+ pieceBaseValue(QUEEN) ) {
                int[] omaxbenefits = new int[m.getValue().length];
                // calc non-negative maximum benefit of the other (hindered/prolonged) moves
                for (Map.Entry<Move, int[]> om : simpleMovesAndChances.entrySet()) {
                    if (m != om
                            && ( (isSlidingPieceType(myPceType)
                            && !dirsAreOnSameAxis(m.getKey().direction(), om.getKey().direction()))
                            || (!isSlidingPieceType(myPceType) ) )
                    ) {
                        int omClashContrib = board.getBoardSquares()[om.getKey().to()].getvPiece(myPceID).getClashContrib();
                        /*if (omClashContrib!=0)
                            System.out.println(om.getKey()+"-clashContrib="+omClashContrib);*/
                        /*not this: as we only what to calculate real benefits, not moves that are anyway negative for me
                            if (isWhite() && omClashContrib > omaxbenefits[0]
                                || !isWhite() && omClashContrib < omaxbenefits[0] )
                                omaxbenefits[0] = omClashContrib;*/
                        if ( abs(om.getValue()[0]) < checkmateEval(BLACK)+ pieceBaseValue(QUEEN) ) {
                            // if this is a doable move, we also consider its further chances (otherwise only the clash contribution)
                            for (int i = 0; i < m.getValue().length; i++) {
                                // non-precise assumption: benefits of other moves can only come one (back) hop later, so we find their maximimum and subtract that
                                // todo: think about missed move opportunities this more thoroughly :-) e.g. king moving N still has same distance to NW and NE square than before...
                                int val = om.getValue()[i];
                                if (isWhite() &&  val > omaxbenefits[i]
                                        || !isWhite() && val < omaxbenefits[i] )
                                    omaxbenefits[i] = val;
                            }
                        }
                    }
                }
                debugPrintln(DEBUGMSG_MOVEEVAL,"... other moves' max = " + Arrays.toString(omaxbenefits) + ".");
                boolean mySquareIsSafeToComeBack = !isPawn(myPceType ) && evalIsOkForColByMin(staysEval(), color()); // TODO: or is sliding piece and chance is from opposit direction
                int[] newmbenefit = new int[m.getValue().length];
                int[] maCs = movesAwayChances.get(m.getKey());
                for (int i = 0; i < m.getValue().length; i++) {
                    // unprecise assuption: benefits of other moves can only come one (back) hop later, so we find their maximimum and subtract that
                    // todo: think about missed move opportunities more thoroughly :-) e.g. king moving N still has same distance to NW and NE square than before...
                    int maC = maCs!=null ? maCs[i] : 0;
                    newmbenefit[i] = m.getValue()[i]     // original chance
                            - ( i>0 ? (omaxbenefits[i]>>2) :0 )   // minus what I loose not choosing the other moves // maybe not, punishes forks...
                            + (i>1 && mySquareIsSafeToComeBack? omaxbenefits[i-2]:0)   // plus adding that what the other moves can do, I can now still do, but one move later
                            + maC;              // plus the chance of this move, because the piece moves away
                }
                movesAndChances.put(m.getKey(), newmbenefit);
                debugPrintln(DEBUGMSG_MOVEEVAL,"... results in: "+ m.getKey() + "="+ Arrays.toString(newmbenefit) + ".");
            }
        }
    }

    public void addMoveAwayChance2AllMovesUnlessToBetween(final int benefit, final int inOrderNr,
                                                          final int fromPos,final  int toPosExcl,
                                                          final boolean chanceAddedForFromPos) {
        for ( Map.Entry<Move,int[]> e : movesAndChances.entrySet() ) {
            int to = e.getKey().to();
            if ( (chanceAddedForFromPos || to!=fromPos)
                    && !isBetweenFromAndTo(to, fromPos, toPosExcl )) {
                debugPrint(DEBUGMSG_MOVEEVAL,"->[indirectHelp:" + fenCharFromPceType(myPceType) + e.getKey() + "]: ");
                VirtualPieceOnSquare baseVPce = board.getBoardSquares()[myPos].getvPiece(myPceID);

                if (isBasicallyALegalMoveForMeTo(to))
                    baseVPce.addMoveAwayChance(benefit, inOrderNr,e.getKey());
                else
                    debugPrintln(DEBUGMSG_MOVEEVAL, "- (no legal move)");
            }
        }
    }

    /**
     *
     * @return nr of legal moves
     */
    public int selectBestMove() {
        int nrOfLegalMoves = 0;
        resetBestMoves();
        getLegalMovesAndChances();
        if (getLegalMovesAndChances()==null)
            return 0;

        // collect moves and their chances from all vPces
        debugPrintln(DEBUGMSG_MOVESELECTION, "");
        debugPrintln(DEBUGMSG_MOVESELECTION, "-- Checking " + this + " with stayEval=" + this.staysEval() + ": " + getLegalMovesAndChances().keySet().toString() );
        int threshold = (color() ? pieceBaseValue(PAWN)
                : pieceBaseValue(PAWN_BLACK));
        for (Map.Entry<Move, int[]> e : getLegalMovesAndChances().entrySet()) {
            EvaluatedMove eMove =  new EvaluatedMove(e.getKey(), e.getValue());
            if ( isPawn(myPceType)
                    && (isWhite() ? isLastRank(eMove.to())
                                  : isFirstRank(eMove.to()))
            ) {
                // System.out.println("promotion!");
                eMove.setPromotesTo(isWhite() ? QUEEN : QUEEN_BLACK);
            }
            nrOfLegalMoves++;
            ChessPiece beatenPiece = board.getPieceAt(e.getKey().to());
            int corrective = // here not  -staysEval()
                    - (beatenPiece != null && beatenPiece.canMove()
                            ? (( beatenPiece.getBestMoveRelEval()  // the best move except its contribution that is already calculated in the stayEval...
                            -board.getBoardSquares()[myPos].getvPiece(beatenPiece.myPceID).getClashContrib() )>>2)   // /4, as it might not be the best opponents move and thus never be done) - the best ar calculated separately later in the overall move selection
                            : 0);  // eliminated effect of beaten Piece
            debugPrintln(DEBUGMSG_MOVESELECTION,"  chk move " + e.getKey() + " " + Arrays.toString(e.getValue())
                    + (beatenPiece != null && beatenPiece.canMove()
                    ? " -" + beatenPiece.getBestMoveRelEval()
                    + "+" + board.getBoardSquares()[myPos].getvPiece(beatenPiece.myPceID).getClashContrib()
                    : "."));
            addEvaluatedMoveToSortedListOfCol(eMove,bestMoves,color(), KEEP_MAX_BEST_MOVES);
        }
        return nrOfLegalMoves;
    }



    /**
     * to be called only for directly (=1 move) reachable positions.
     * For 1hop pieces, this is just the position itself.
     * For sliding pieces, additionally all the squares in between
     * @param pos - target position (excluded)
     * @return list of squares able to block my way to pos, from this piece's myPos (included) to pos excluded
     */
    public int[] allPosOnWayTo(int pos) {
        int[] ret;
        if (isSlidingPieceType(myPceType)) {
            int dir = calcDirFromTo(myPos,pos);
            assert (dir!=NONE);
            ret = new int[distanceBetween(myPos,pos)];
            for (int i=0,p=myPos; p!=pos && i<ret.length; p+=dir, i++)
                ret[i]=p;
        }
        else {
            ret = new int[1];
            ret[0] = myPos;
        }
        return ret;
    }

    public EvaluatedMove[] getBestReasonableEvaluatedMoveOnAxis() {
        EvaluatedMove[] bestMovesOnAxis = new EvaluatedMove[4];
        if (colorlessPieceType(myPceType)==KNIGHT)  // Todo: how to trap a Knight...
            return bestMovesOnAxis;
        for ( Map.Entry<Move, int[]> m : getLegalMovesAndChances().entrySet() ) {
            int eval = m.getValue()[0];
            if (evalIsOkForColByMin(eval,color())) {
                int axisIndex = convertDir2AxisIndex( calcDirFromTo(getPos(), m.getKey().to()) );
                EvaluatedMove em = new EvaluatedMove(m.getKey(), m.getValue());
                if ( bestMovesOnAxis[axisIndex]==null || em.isBetterForColorThan(color(), bestMovesOnAxis[axisIndex]) )
                    bestMovesOnAxis[axisIndex] = em;
            }
        }
        return bestMovesOnAxis;
    }

    public int[] getBestMoveEval(int nr) {
        if (nr>bestMoves.size())
            return null;
        return bestMoves.get(nr).getEval();
    }

    public Move getBestMoveSoFar(int nr) {
        if (nr>bestMoves.size())
            return null;
        return bestMoves.get(nr);
    }

    public EvaluatedMove getBestEvaluatedMove(int nr) {
        if (nr>bestMoves.size())
            return null;
        return bestMoves.get(nr);
    }

    public int getNrBestMoves() {
        return bestMoves.size();
    }


}
