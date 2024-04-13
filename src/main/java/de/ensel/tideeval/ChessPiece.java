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
import java.util.stream.Stream;

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

    private EvaluatedMovesCollection movesAwayChances;  // stores real moves (i.e. d==1) and the chances they have on certain future-levels concerning moving the Piece away from its origin
    private int bestRelEvalAt;  // bestRelEval found at dist==1 by moving to this position. ==NOWHERE if no move available

    static final int KEEP_MAX_BEST_MOVES = 4;
    List<EvaluatedMove> bestMoves;
    List<EvaluatedMove> restMoves;

    private EvaluatedMovesCollection legalMovesAndChances;
    private EvaluatedMovesCollection soonLegalMovesAndChances;

    ////
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
        resetLegalMovesAndChances();
    }

    void resetBestMoves() {
        bestMoves = new ArrayList<>(KEEP_MAX_BEST_MOVES);
        restMoves = new ArrayList<>(KEEP_MAX_BEST_MOVES);
    }

    public int getValue() {
        // Todo calc real/better value of piece

        // adjusted basic piece value
        if (isLightPieceType(getPieceType())
                && board.getLightPieceCounterForPieceType(getPieceType()) == 1
        ) {
            // exception for single (left over) light pieces
            return singleLightPieceBaseValue(getPieceType());
        }
        else if (isPawn(getPieceType()) ) {
            // exception for pawns is implemented in my vPce here.
            return board.getBoardSquare(getPos()).getvPiece(getPieceID()).getValue();
        }

        int val = pieceBaseValue(getPieceType());

        // further adjustment concerning higher value of fewer remaining pieces
        /* todo: revise, it unexpectedly makes test results slighly worse (sign error? but does not look like)
        int boardPceValSum = board.boardEvaluation(1);
        if ( !evalIsOkForColByMin( boardPceValSum, color(), EVAL_HALFAPAWN*5 ) ) {
            if (isQueen(getPieceType()) )
                val += ((isWhite() ? EVAL_TENTH : -EVAL_TENTH)>>5) - (boardPceValSum>>4);
            else if (isRook(getPieceType()) )
                val += (isWhite() ? 3 : -3);
            // even worse
            //    val += -boardPceValSum>>4;
            //else if (isLightPieceType(getPieceType()))
            //    val += (isWhite() ? EVAL_TENTH : -EVAL_TENTH)>>1;
        }
        */
        return val;
    }

    public EvaluatedMovesCollection getLegalMovesAndChances() {
        //if (legalMovesAndChances == null)
        //    extractLegalAndSoonMovesAndChances();
        return legalMovesAndChances;
    }

    public EvaluatedMovesCollection getSoonLegalMovesAndChances() {
        //if (legalMovesAndChances == null)
        //    extractLegalAndSoonMovesAndChances();
        return soonLegalMovesAndChances;
    }

    private boolean isALegalMoveForMe(Move m) {
        return isBasicallyALegalMoveForMeTo(m.to())   // TODO: do we need to check this again, or may .isBasicallyLegal be used here?
                && board.moveIsNotBlockedByKingPin(this, m.to())
                && (!board.isCheck(color())
                    || board.nrOfChecks(color()) == 1 && board.posIsBlockingCheck(color(), m.to())
                    || isKing(myPceType));
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



    /**
     * collects possible legal moves and covering places.
     * to be called once around round 3
     */
    public void prepareMoves(final boolean addChances) {
        boolean prevMoveability = canMoveAwayReasonably();
        bestRelEvalAt = NOWHERE;
        int bestRelEvalSoFar = isWhite() ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE;
        Arrays.fill(mobilityFor3Hops, 0);  // TODO:remove line + member
        if (DEBUGMSG_MOVEEVAL) {
            debugPrintln(DEBUGMSG_MOVEEVAL, "");
            debugPrintln(DEBUGMSG_MOVEEVAL, "Adding relevals for piece " + this + ".");
        }
        for (int p=0; p<board.getBoardSquares().length; p++) {
            if (DEBUGMSG_MOVEEVAL && abs(board.getBoardSquare(p).getvPiece(myPceID).getRelEvalOrZero())>DEBUGMSG_MOVEEVALTHRESHOLD)
                debugPrintln(DEBUGMSG_MOVEEVAL,"checking square "+ squareName(p)+": " + board.getBoardSquare(p).getvPiece(myPceID) + " ("+board.getBoardSquare(p).getvPiece(myPceID).getRelEvalOrZero()+").");
            VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
            final int relEval = vPce.getRelEvalOrZero();
            if (isBasicallyALegalMoveForMeTo(p)) {
                if (isWhite() ? relEval > bestRelEvalSoFar
                        : relEval < bestRelEvalSoFar) {
                    bestRelEvalSoFar = relEval;
                    bestRelEvalAt = p;
                }
                if (abs(relEval)>DEBUGMSG_MOVEEVALTHRESHOLD) {
                    if (!vPce.getMinDistanceFromPiece().hasNoGo()) {
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrintln(DEBUGMSG_MOVEEVAL, "Adding releval of " + relEval + "@" + 0
                                + " as unconditional result/benefit for " + vPce + ".");
                    } else {  // although it must have NoGo, it is still a valid move...
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrintln(DEBUGMSG_MOVEEVAL, "Adding releval of " + relEval + "@" + 0
                                + " as result/benefit despite nogo for " + vPce + ".");
                    }
                }
                if (addChances) {
                    vPce.addChance(relEval, 0);
                }
            }
            /* no, this is done in calcfutureClashEval
            else if (addChances && evalIsOkForColByMin(relEval, vPce.color(), -EVAL_DELTAS_I_CARE_ABOUT)) {
                // not a legal move, but a chance for the future
                int bonus = relEval;
                final int kpos = board.getKingPos(vPce.myOpponentsColor());
                if ( p == kpos ) {
                    // do not overrate attackers to the King -> real check benefits are evaluated in separate methods.
                    bonus >>= 3;
                }
                vPce.addChance(relEval, vPce.getStdFutureLevel() );
            } */

        }
        if (prevMoveability != canMoveAwayReasonably()) {
            // initiate updates/propagations for/from all vPces on this square.
            board.getBoardSquare(myPos).propagateLocalChange();
        }
    }

    public void rewardMovingOutOfTrouble() {
        final int relEval = board.getBoardSquare(myPos).getvPiece(myPceID).getRelEvalOrZero();
        // check if piece here itself is in trouble
        if ( !evalIsOkForColByMin(relEval, color(), EVAL_DELTAS_I_CARE_ABOUT) ) { // 47u22-47u66 added , - EVAL...
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL, "Reward " + this + " for moving out of trouble of " + relEval + "@" + 0 + ".");
            this.addMoveAwayChance2AllMovesUnlessToBetween(
                    -((relEval >> 2)-(relEval >> 4)), 0,  // (relEval >> 2) + ((ChessBoard.engineP1()*(relEval >> 2))/100  ) ), 0,
                    ANYWHERE, ANYWHERE, false, getPos());  // staying fee
        }
    }


    void preparePredecessors() {
        for (int d = 1; d <= board.MAX_INTERESTING_NROF_HOPS; d++) {
            for (int p = 0; p < board.getBoardSquares().length; p++) {
                VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
                if (vPce.getRawMinDistanceFromPiece().dist() == d)
                    vPce.rememberAllPredecessors();
            }
        }
    }

    /*void evaluateMobility() {
        for (int p = 0; p < board.getBoardSquares().length; p++) {
            VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
            if (vPce == null || !vPce.getRawMinDistanceFromPiece().distIsNormal())
                continue;
            vPce.getNeighbours()

        }
    }*/

    void evaluateMobility() {
        // initialize at the "end" of mobility
        // break it down, closer and closer to piece
        int mobBase = 0;
        for (int d = board.MAX_INTERESTING_NROF_HOPS; d>0; d--) {
            for (int p = 0; p < board.getBoardSquares().length; p++) {
                VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
                if (vPce != null
                        && vPce.getRawMinDistanceFromPiece().dist() == d
// TEST in 48h54 and 54b - to be continued :-)
//                ) {
//                    boolean canReasonablyBeHere = evalIsOkForColByMin(vPce.getRelEvalOrZero(), vPce.color());
//                    if ( (d == board.MAX_INTERESTING_NROF_HOPS && canReasonablyBeHere) ) {
//                        vPce.addMobility( 1);   // same as 1<<(board.MAX_INTERESTING_NROF_HOPS-d) );
//                        vPce.addMobilityMap(1 << p);
//                    }
//                    int m = vPce.getMobility();
//                    if ( m == 0 && d < board.MAX_INTERESTING_NROF_HOPS && canReasonablyBeHere ) {
//                        vPce.addMobility( 1<<(board.MAX_INTERESTING_NROF_HOPS-(d+1)) );
//                        vPce.addMobilityMap(1 << p);
//                        m = vPce.getMobility();
//                    }
//                    if ( d > 0 && canReasonablyBeHere ) {
                ) {
                    if (d == board.MAX_INTERESTING_NROF_HOPS) {
                        vPce.addMobility( 1);   // same as 1<<(board.MAX_INTERESTING_NROF_HOPS-d) );
                        vPce.addMobilityMap(1 << p);
                    }
                    int m = vPce.getMobility();
                    //is always >0 :-) if (d>0) {
//
                    /*if (board.hasPieceOfColorAt(vPce.color(), p))
                        m -= m>>2;
                    if (vPce.getMinDistanceFromPiece().hasNoGo())
                        m -= m>>2;  // cannot be reached safely, so do not count so much. */
                    for (VirtualPieceOnSquare predVPce : vPce.getShortestReasonableUnconditionedPredecessors()) {
                        predVPce.addMobility((1 << (board.MAX_INTERESTING_NROF_HOPS - d))
                                + /*(predVPce.isKillable() ? (m>>1) : m) */ m );
                        predVPce.addMobilityMap(vPce.getMobilityMap());
                    }
                    //}
                    if (d == 1 && m > mobBase)
                        mobBase = m;
                }
            }
        }
        // set matching chances
        //int mobBase = board.getBoardSquare(myPos).getvPiece(myPceID).getMobility();
        //mobBase >>= 1; // calculated max is not used for now, it makes score worse... probably, because baseline is different for every piece and thus, this takes away the differences
        mobBase = 0; // EVAL_TENTH-(EVAL_TENTH>>2);  // 8
        for (int p = 0; p < board.getBoardSquares().length; p++) {
            VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
            if (vPce!=null
                    && vPce.getRawMinDistanceFromPiece().dist() == 1
                    && !vPce.getMinDistanceFromPiece().hasNoGo()
            ) {
                //System.out.println("Mobility on d=" + d + " for " + this + " on " + squareName(p) + ": " + vPce.getMobility() + " / " + bitMapToString(vPce.getMobilityMap()) + ".");
                int benefit =  (vPce.getMobility()-mobBase)>>2;

                switch (colorlessPieceType(getPieceType())) {
                    case KING -> benefit >>= 2;
                    case QUEEN -> benefit >>= 1;  // reduce for queens
                    case KNIGHT -> benefit >>= 1;  // reduce for knights
                    //case ROOK   -> benefit += benefit>>4;
                    //slightly worse: case BISHOP -> benefit -= benefit>>4;
                }

                if ( benefit > (EVAL_TENTH<<1) )
                    benefit -= (benefit-(EVAL_TENTH<<1))>>1;
                else if ( benefit < -(EVAL_TENTH<<1) )
                    benefit -= (benefit+(EVAL_TENTH<<1))>>1;

                if (!isWhite())
                    benefit = -benefit;

                if (DEBUGMSG_MOVEEVAL && abs(benefit)>DEBUGMSG_MOVEEVALTHRESHOLD)
                    debugPrintln(DEBUGMSG_MOVEEVAL, "Benefit for mobility of " + vPce + " is " + benefit + "@0.");
                vPce.addChance(benefit,  0); // vPce.getRawMinDistanceFromPiece().isUnconditional() ? 0 : 1);

            }
        }
    }

    public int getBestMoveRelEval() {
        if (bestMoves!=null && bestMoves.size()>0 && bestMoves.get(0)!=null)
            return bestMoves.get(0).getEvalAt(0);
        if (bestRelEvalAt==NOWHERE)
            return isWhite() ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE;
        if (bestRelEvalAt==POS_UNSET)
            return NOT_EVALUATED;
        return board.getBoardSquare(bestRelEvalAt).getvPiece(myPceID).getRelEvalOrZero();
    }

    public int getBestMoveTarget() {
        if (bestMoves!=null && bestMoves.size()>0 && bestMoves.get(0)!=null)
            return bestMoves.get(0).to();
        if (bestRelEvalAt==NOWHERE)
            return NOWHERE;
        if (bestRelEvalAt==POS_UNSET)
            return NOT_EVALUATED;
        return bestRelEvalAt;
    }

    /*public int getBestMoveRelEvalExceptTo(int notToPos) {
        if (bestMoves!=null && bestMoves.size()>0 && bestMoves.get(0)!=null) {
            int i=0;
            while (i<bestMoves.size()) {
                if ( bestMoves.get(i).to() != notToPos );
                    return bestMoves.get(i).getEval()[0];
                i++;
            }
        }
        if (bestRelEvalAt==NOWHERE)
            return isWhite() ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE;
        if (bestRelEvalAt==POS_UNSET)
            return NOT_EVALUATED;
        return //TODO: here the same as above, but needs loop over all moves...
                    board.getBoardSquares()[bestRelEvalAt].getvPiece(myPceID).getRelEvalOrZero();
    }
    */

    boolean canMoveAwayReasonably() {
        //if ( getLegalMovesAndChances().size()>0 && test auf reasonable... )
        int eval = getBestMoveRelEval();
        if (eval==NOT_EVALUATED)
            return false;  // we do not know, so maybe yes... - TODO!!: but currently ther is a bug (see doMove_String_Test1fen) that only occurs if this is set to true...
        return (evalIsOkForColByMin(eval, color()));
        // TODO: include the moveAwayChances/Fees - however, for now they are not calculated yet, when this method is used...
    }

    boolean canMoveAwayPositively() {
        //if ( getLegalMovesAndChances().size()>0 && test auf reasonable... )
        int eval = getBestMoveRelEval();
        if (eval==NOT_EVALUATED)
            return false;  // we do not know, so maybe yes... - TODO!: but currently ther is a bug (see doMove_String_Test1fen) that only occurs if this is set to true...
        return evalIsOkForColByMin(eval, color(), -EVAL_HALFAPAWN);
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
        movesAwayChances = new EvaluatedMovesCollection(color());
        resetLegalMovesAndChances();
    }

    private void resetLegalMovesAndChances() {
        legalMovesAndChances = new EvaluatedMovesCollection(color());
        soonLegalMovesAndChances = new EvaluatedMovesCollection(color());
    }

    void resetChancesOfAllVPces() {
        for (Square sq : board.getBoardSquares()) {
            final VirtualPieceOnSquare vPce = sq.getvPiece(myPceID);
            vPce.resetJustChances();
        }
    }

    void aggregateVPcesChancesAndCollectMoves() {
        resetLegalMovesAndChances();
        // propagate chances back from far away, closer and closer to piece
        if (DEBUGMSG_MOVEEVAL_AGGREGATION)
            debugPrintln(DEBUGMSG_MOVEEVAL_AGGREGATION, "Aggregating evals for " + this + ":");
//        final boolean iAmUpInPieces = evalIsOkForColByMin( board.boardEvaluation(1), color(), -(positivePieceBaseValue(KNIGHT)-EVAL_HALFAPAWN) ) ;
        for (int d = board.MAX_INTERESTING_NROF_HOPS; d>0; d--) {
            debugPrint(DEBUGMSG_MOVEEVAL_AGGREGATION, "d=" + d + ": ");
            for (Square sq : board.getBoardSquares()) {
                VirtualPieceOnSquare vPce = sq.getvPiece(myPceID);
                if ( vPce==null // TODO: eliminate check, should not be necessary, i.e. null not possible
                     || vPce.getRawMinDistanceFromPiece().hasNoGo()
                     || vPce.getRawMinDistanceFromPiece().dist() != d )
                    continue;

                if (d < board.MAX_INTERESTING_NROF_HOPS) {
                    // now that all future chances are known, aggregate them and add the now known forking chance
                    vPce.consolidateChances();
                }

                if (DEBUGMSG_MOVEEVAL_AGGREGATION && getPieceID() == DEBUGFOCUS_VP)
                    debugPrintln(DEBUGMSG_MOVEEVAL_AGGREGATION, "passing agg.eval=" + vPce.getChances() + " for " + vPce+ "");
                // pass chances down to vPce, one step closer to the piece
                for (VirtualPieceOnSquare predVPce : vPce.getShortestReasonablePredecessors()) {  // vPce.getPredecessors()) {
                    if (DEBUGMSG_MOVEEVAL_AGGREGATION && getPieceID() == DEBUGFOCUS_VP)
                        debugPrintln(DEBUGMSG_MOVEEVAL_AGGREGATION, "   to " + predVPce);
                    if (vPce.getMinDistanceFromPiece().hasNoGo())
                        continue;

                    int flDelta;
                    if (predVPce.getRawMinDistanceFromPiece().dist() == 0)  // needed as stdFutureLevel is 0 for dist==0 not -1.
                        flDelta = 0;
                    else
                        flDelta = predVPce.getStdFutureLevel() - vPce.getStdFutureLevel() + 1;
                    if (flDelta == 0) {
                        predVPce.aggregateInFutureChances( vPce.getChances() );
                        if (DEBUGMSG_MOVEEVAL_AGGREGATION && getPieceID() == DEBUGFOCUS_VP)
                            debugPrintln(DEBUGMSG_MOVEEVAL_AGGREGATION, ".");
                    }
                    else if (flDelta > 0) {
                        //EvalPerTargetAggregation chances = new EvalPerTargetAggregation(vPce.getChances() );
                        //chances.timeWarp(flDelta);
                        if (DEBUGMSG_MOVEEVAL_AGGREGATION && getPieceID() == DEBUGFOCUS_VP)
                            debugPrintln(DEBUGMSG_MOVEEVAL_AGGREGATION, " - a better NOT, it would need time warp " +flDelta +" and rerun of the pasing down from there."); // +" = " + chances + ". ");
                        //predVPce.aggregateInFutureChances( chances );
                    }
                    else  {
                        if (DEBUGMSG_MOVEEVAL_AGGREGATION && getPieceID() == DEBUGFOCUS_VP)
                            debugPrintln(DEBUGMSG_MOVEEVAL_AGGREGATION, " ... äh, it's closer than expected by " + (flDelta) + ", but still passing on");
                        predVPce.aggregateInFutureChances( vPce.getChances() );
                    }
                }
                /*if (DEBUGMSG_MOVEEVAL_AGGREGATION)
                    debugPrintln(DEBUGMSG_MOVEEVAL_AGGREGATION, ".");*/
                // final round, d==1 are real moves to remember
                if (vPce.rawMinDistanceIs1orSoon1()) {
                    rawAddMoveAwayChance( new EvaluatedMove( getPos(), vPce.getMyPos(), vPce.getMoveAwayChance() ) );
                    EvaluatedMove newEM = new EvaluatedMove(getPos(), vPce.getMyPos(), vPce.getChance());
                    /* was a little worse (see 48h74)
                    // test: assume it is a little negative to take a piece without big benefit - unless we are already at least a knight up
                    if ( !iAmUpInPieces
                            && !board.hasPieceOfColorAt(opponentColor(color()), vPce.getMyPos())
                            && evalIsOkForColByMin(newEM.getEvalAt(0),opponentColor(color())) ) {
                        newEM.subtractEvalAt(evalForColor(EVAL_TENTH<<1, color()), 0);
                    }
                    */
                    /* was not helpful, see (48h63d)
                    // assume that it is negative to let the opponent have "the last take" in a clash - unless at the border of the board - to admit, just an assumption... and actually not in the spirit of the rest of the code to not introduce typical strategic evaluations not based on the distance/clash values...
                    ChessPiece lrt = sq.lastReasonableTaker();
                    if ( lrt != null && lrt.color() != vPce.color() ) {
                        int bonus = 0;
                        final int toRank = rankOf(vPce.getMyPos());
                        if ( (toRank > 1 && toRank < NR_RANKS-2 )
                                || toRank == rankOf(board.getKingPos(WHITE))
                                || toRank == rankOf(board.getKingPos(BLACK))
                        ) {
                            bonus -= EVAL_HALFAPAWN>>1;  // 25
                        }
                        final int toFile = fileOf(vPce.getMyPos());
                        if ( (toFile <= 1 || toFile >= NR_RANKS-2 )
                                && toFile != fileOf(board.getKingPos(WHITE))
                                && toFile != fileOf(board.getKingPos(BLACK))
                        ) {
                            bonus += EVAL_TENTH<<1; // 20
                        }
                        if (isBlack(vPce.color()))
                            bonus = -bonus;
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrint(DEBUGMSG_MOVEEVAL_AGGREGATION, "  Bonus of " + bonus + " for having lrT: ");
                        newEM.addEvalAt(bonus, 0);
                    } */
                    /* was not helpful, see (48h63a+b)
                    // assume that it is positive to have "the last take" in a clash - unless at the border of the board - to admit, just an assumption... and actually not in the spirit of the rest of the code to not introduce typical strategic evaluations not based on the distance/clash values...
                    ChessPiece lrt = sq.lastReasonableTaker();
                    if ( lrt != null && lrt.color() == vPce.color() ) {
                        int bonus = 0;
                        final int toRank = rankOf(vPce.getMyPos());
                        if ( (toRank > 1 && toRank < NR_RANKS-2 )
                                || toRank == rankOf(board.getKingPos(WHITE))
                                || toRank == rankOf(board.getKingPos(BLACK))
                        ) {
                            bonus += EVAL_HALFAPAWN>>1;  // 25
                        }
                        final int toFile = fileOf(vPce.getMyPos());
                        if ( (toFile <= 1 || toFile >= NR_RANKS-2 )
                                && toFile != fileOf(board.getKingPos(WHITE))
                                && toFile != fileOf(board.getKingPos(BLACK))
                        ) {
                            bonus -= EVAL_TENTH<<1; // 20
                        }
                        if (isBlack(vPce.color()))
                            bonus = -bonus;
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrint(DEBUGMSG_MOVEEVAL_AGGREGATION, "  Bonus of " + bonus + " for having lrT: ");
                        newEM.addEvalAt(bonus, 0);
                    } */
                    if (DEBUGMSG_MOVEEVAL_AGGREGATION && getPieceID() == DEBUGFOCUS_VP)
                        debugPrintln(DEBUGMSG_MOVEEVAL_AGGREGATION, " --> adding move " + newEM + ". ");
                    rawAddLegalOrSoonLegalMove(newEM);
                }
            }
            debugPrintln(DEBUGMSG_MOVEEVAL_AGGREGATION && getPieceID() == DEBUGFOCUS_VP, " end(" + d + "). ");
        }
    }


    void addUnevaluatedMove(Move m) {
        rawAddLegalOrSoonLegalMove(new EvaluatedMove(m));
    }

    void changeMoveWithChance(Move m, int futureLevel, int relEval) {
        rawAddLegalOrSoonLegalMove(
                new EvaluatedMove(m,
                                  new Evaluation(relEval, futureLevel, m.to()) ));
    }

    private void rawAddLegalOrSoonLegalMove(EvaluatedMove em) {
        if (isALegalMoveForMe(em)) {
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL, "$$_ Legal move of " + toString() + ": "+em);
            em.setBasicallyLegal();
            legalMovesAndChances.add(em);
        }
        else {
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL, "$$ Detected not yet, but soon legal move of " + toString() + ": "+ em + ")" );
            soonLegalMovesAndChances.add(em);
        }
    }

    private void rawAddMoveAwayChance(EvaluatedMove em) {
        if (isALegalMoveForMe(em)) {
            //System.out.println("legal move of " + toString() + ": "+e.getKey());
            em.setBasicallyLegal();
            movesAwayChances.add(em);
        }
        else {
            if (DEBUGMSG_MOVEEVAL_INTEGRITY )
                debugPrintln(DEBUGMSG_MOVEEVAL_INTEGRITY, "Cannot handle non legal move-away-move " + toString() + ": "+em );
            // soonLegalMovesAwayChances.add(em);
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
    public boolean continueDistanceCalc() {
        return continueDistanceCalc(board.currentDistanceCalcLimit());
    }

    public boolean continueDistanceCalc(int depthlimit) {
        int n = 0;
        startNextUpdate();
        while (queCallNext(depthlimit)) {
            if (DEBUGMSG_DISTANCE_PROPAGATION)
                debugPrint(DEBUGMSG_DISTANCE_PROPAGATION, " Que:" + n);
            n++;
        }
        if (DEBUGMSG_DISTANCE_PROPAGATION && n>0)
            debugPrintln(DEBUGMSG_DISTANCE_PROPAGATION, " QueDone: " + n);
        endUpdate();
        return n>0;
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
                    && toVPce.isUnavoidableOnShortestPath(frompos,MAX_INTERESTING_NROF_HOPS)
            ) {
                startingVPce = fromVPce;
                finalizingVPce = toVPce;
            } else {
                startingVPce = toVPce;
                finalizingVPce = fromVPce;
            }
            // the main update
            startingVPce.resetDistances();
            startingVPce.resetRelEvalsAndChances();
            // already done in resetDistances: startingVPce.setLatestChangeToNow(); // a piece coming or going is always a change and e.g. triggers later clashCacl
            startingVPce.recalcRawMinDistanceFromNeighboursAndPropagate();
            // the queued recalcs need to be calced first, othererwise the 2nd step would work on old data
            continueDistanceCalc(MAX_INTERESTING_NROF_HOPS); // TODO-Check: effect on time? + does it break the nice call order of continueDistanceCalcUpTo()?
//TODO-BUG!! Check:solved? see SquareTest.knightNogoDist_ExBugTest() and Test above.
// Reason is here, that Moving Piece can make NoGos and thus prolong other pieces' distances.
// However, here it is assumed that only shortened distances are propagated.

            // then check if that automatically reached the other square
            // but: we do not check, we always call it, because it could be decreasing, while the above call was increasing (and can not switch that)
            // if (finalizingVPce.getLatestChange() != getLatestUpdate()) {
            // sorry, updates also have to be triggered here
            endUpdate();
            startNextUpdate();
            finalizingVPce.resetDistances();
            finalizingVPce.resetRelEvalsAndChances();
            // already done in resetDistances: finalizingVPce.setLatestChangeToNow(); // a piece coming or going is always a change and e.g. triggers later clashCacl
            finalizingVPce.recalcRawMinDistanceFromNeighboursAndPropagate();
            //}
            if ( isPawn(getPieceType())
                   && (    (  isWhite() && rankOf(getPos()) == 1 && (rankOf(frompos)==2 || rankOf(topos)==2) )
                        || ( !isWhite() && rankOf(getPos()) == NR_RANKS-2 && (rankOf(frompos)==NR_RANKS-3 || rankOf(topos)==NR_RANKS-3) ) )
            ) {
                // we are a start in the starting grid and the square in front of me changed
                // extra update is needed for the 2 square move
                endUpdate();
                startNextUpdate();
                board.getBoardSquare(  getPos() + (isWhite() ? 2*UP : 2*DOWN ) )
                        .getvPiece(getPieceID())
                        .recalcRawMinDistanceFromNeighboursAndPropagate();
            }
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
        return pieceColorAndName(myPceType) + " on " + squareName(getPos());
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
        StringBuilder res = new StringBuilder(""+ (legalMovesAndChances.size())+" legal moves: " );
        if (legalMovesAndChances.size() == 0)
            res.append("-. ");
        else
            for ( EvaluatedMove m : legalMovesAndChances ) {
                res.append( m+" ");
            }
        res.append(" + "+ (soonLegalMovesAndChances.size())+" legal moves: " );
        if (soonLegalMovesAndChances.size() == 0)
            res.append("-. ");
        else
            for ( EvaluatedMove m : soonLegalMovesAndChances ) {
                res.append( m+" ");
            }
        res.append(" therein for moving away: ");
        for ( EvaluatedMove m : movesAwayChances ) {
            res.append(m+" ");
        }
        return new String(res);
    }

    public int staysEval() {
        return board.getBoardSquare(myPos).clashEval(); // .getvPiece(myPceID).getRelEval();
    }

    /**
     * Collect still unevaluated moves with dist==1 (legal and moves with conditions) and their chances from all vPces.
     * So careful, this also adds illegal moves "onto" own pieces (needed to keep+calc their clash contributions)
     * and through opponents pieces  (needed if opponents move away enables a move).
     * Used during dist calculation e.g. to know if a piece can move away.
     */
    public void collectUnevaluatedMoves() {
        clearMovesAndAllChances();
        // TODO: do not search for the moves... should be collected during distance calculating wherever dist matches...
        for (Square sq : board.getBoardSquares()) {
            sq.getvPiece(myPceID).resetBasics();
            if (sq.getvPiece(myPceID).getRawMinDistanceFromPiece().dist()==1) {  // TODO!: test with filter for only unconditional here
                Move m = new Move(myPos,sq.getMyPos());
                if ( isBasicallyALegalMoveForMeTo(sq.getMyPos()) )
                    m.setBasicallyLegal();
                addUnevaluatedMove(m);
            }
        }
    }

    boolean isBasicallyALegalMoveForMeTo(int topos) {
        Square sq = board.getBoardSquare(topos);
        ConditionalDistance rmd = sq.getvPiece(myPceID).getRawMinDistanceFromPiece();
        return rmd.dist() == 1
                && rmd.isUnconditional()
                && !(board.hasPieceOfColorAt(color(), topos))   // not: square blocked by own piece
                && !(isKing(myPceType)                         // and not:  king tries to move to a checked square
                         && (sq.countDirectAttacksWithout2ndRowWithColor(opponentColor(color())) >= 1
                             || //sq.countDirectAttacksWithout2ndRowWithColor(opponentColor(color()))==0
                                //&&
                                sq.attackByColorAfterFromCondFulfilled(opponentColor(color()),myPos)
                             || sq.extraCoverageOfKingPinnedPiece(opponentColor(color()))
                            )
                    )
                && !(isPawn(myPceType)
                     && fileOf(topos) != fileOf(myPos)
                     && !board.hasPieceOfColorAt(opponentColor(color()), topos)
                    );
    }

    public int getTargetOfContribSlidingOverPos(int blockingPos) {
        if ( !isSlidingPieceType(myPceType) )
            return 0;
        for (EvaluatedMove em : legalMovesAndChances ) {
            if (!em.isBasicallyLegal()) // abs(m.getValue()[0]) >= checkmateEval(BLACK) + pieceBaseValue(QUEEN))
                continue;
            if ( em.direction() == calcDirFromTo(myPos, blockingPos))
                return em.to();
        }
        return NOWHERE;
    }

    public void mapLostChances() {
        // calculate into each first move, that it actually looses (or prolongs) the chances of the other first moves
        EvaluatedMovesCollection newLegalMovesAndChances = new EvaluatedMovesCollection(color());
        for (EvaluatedMove em : legalMovesAndChances )  {
            if ( !em.isBasicallyLegal() )  // abs(m.getValue()[0]) < checkmateEval(BLACK)+ pieceBaseValue(QUEEN) ) {
                continue;
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL,"Map lost chances for: "+ em +".");
            Evaluation omMaxBenefit = new Evaluation(ANYWHERE);
            Evaluation sameAxisMaxBenefit = new Evaluation(ANYWHERE);
            // calc non-negative maximum benefit of the other (hindered/prolonged) moves
            int maxLostClashContribs=0;
            // deal with double square pawn moves that could be taken en passant:
            // maximise their benefit to the one of hte single pawn move.
            boolean doubleSquarePawnMoveLimiting = isPawn(getPieceType())
                    && distanceBetween(em.from(),em.to())==2
                    && board.getBoardSquare(em.from()+(isWhite()?UP:DOWN)).isAttackedByPawnOfColor(opponentColor(color()));
            Evaluation maxDPMbenefit = null;
            //Evaluation pawnDoubleHopBenefits = null;

            ChessPiece moveTargetPce = board.getBoardSquare(em.to()).myPiece();

            // we need all moves now and do not check for legal moves here, moving onto a covering piece would always be illegal...
            for (EvaluatedMove om : (Iterable<EvaluatedMove>) (getAllMovesStream()
                    .filter(om -> om!=em)
                    //TEST: be aware, usind this line disables parts in the loop further down
                    .filter( om -> !isSlidingPieceType(getPieceType())             // for sliding pieces exclude other moves in the same direction as move, because its benefits do not get lost
                                            || !dirsAreOnSameAxis(em.direction(), om.direction()) ) // wrong for queen with magic right triangle, but this is solved below
            )::iterator ) {
                // special case: queen with magic right triangle  (and same for King at dist==1) does not loose contribution
                if ( ( ( isQueen(getPieceType() )
                            && !board.posIsBlockingCheck(color(),em.to() ) )
                       || ( isKing(getPieceType())
                            && distanceBetween(em.to(), om.to())==1 )
                     )
                         && formRightTriangle(em.from(), em.to(), om.to())
                         && board.allSquaresEmptyFromTo(em.to(),om.to())
                ) {
                    if (DEBUGMSG_MOVEEVAL)
                        debugPrintln(DEBUGMSG_MOVEEVAL, "("+this+" is happy about magical right triangle to " + squareName(om.to()) + " after move to " + squareName(em.to()) + ".");
                }
                else if ( !isSlidingPieceType(getPieceType())             // for sliding pieces exclude other moves in the same direction as move, because its benefits do not get lost
                          || !dirsAreOnSameAxis(em.direction(), om.direction()) // wrong for queen with magic right triangle, but this is already solved above
                ) {
                    int omLostClashContribs = board.getBoardSquare(om.to())
                            .getvPiece(myPceID).getClashContribOrZero();
                    if (DEBUGMSG_MOVEEVAL && abs(omLostClashContribs) >= 0)
                        debugPrintln(DEBUGMSG_MOVEEVAL, ".. checking other move " + om
                                + " 's + lostClashContrib=" + omLostClashContribs + ".");
                    if (moveTargetPce != null) {
                        int targetPceSameClashContrib = board.getBoardSquare(om.to())
                                .getvPiece(moveTargetPce.getPieceID()).getClashContribOrZero();
                        if (abs(targetPceSameClashContrib) > EVAL_TENTH) {
                            if (DEBUGMSG_MOVEEVAL)
                                debugPrintln(DEBUGMSG_MOVEEVAL, "  (" + moveTargetPce + " has contrib of "
                                        + targetPceSameClashContrib + " on same square, which I make impossible as a counteract of loosing my contribution.)");
                            omLostClashContribs = (omLostClashContribs + targetPceSameClashContrib) >> 4;  // could also be set to 0, see comparison v.29z10-13
                        }
                    }
                    if (isBetterThenFor(omLostClashContribs, maxLostClashContribs, color()))
                        maxLostClashContribs = omLostClashContribs;
                }
                if ( om.isBasicallyLegal()) {  // abs(om.getValue()[0]) < checkmateEval(BLACK)+ pieceBaseValue(QUEEN) ) {
                    // if om is a doable move, we also consider its further chances (otherwise only the clash contribution)
                    // non-precise assumption: benefits of other moves can only come one (back) hop later, so we find their maximimum and subtract that
                    if ( isSlidingPieceType(getPieceType())             // for sliding pieces along the same axis we only prolongen the omaxbenefit by 1 not 2 plys, so we remeber it separately
                            && dirsAreOnSameAxis(em.direction(), om.direction()) )
                        ; //sameAxisMaxBenefit.maxEvalPerFutureLevelFor(om.eval(), color());
                    else
                        omMaxBenefit.maxEvalPerFutureLevelFor(om.eval(), color());
                }
                if (doubleSquarePawnMoveLimiting && fileOf(em.to())==fileOf(om.to()) )  // om is the matching one square move of the pawn
                    maxDPMbenefit = om.eval();
            }

            boolean mySquareIsSafeToComeBack = !isPawn(myPceType ) && evalIsOkForColByMin(staysEval(), color()); // TODO: or is sliding piece and chance is from opposit direction
            /*// did neither improve nor make it worse (also with >>1)
            if ( !evalIsOkForColByMin(staysEval(), color()) )
                maxLostClashContribs >>= 2; // ideas: if we cannot stay, because we will be killed, then our clash contributions are not worth much any more. */
            Evaluation futureReturnBenefits = null;
            if (mySquareIsSafeToComeBack) {
                // adding what the other moves could do, because I can still do that two moves later (after coming back, if that is possible)
                futureReturnBenefits = new Evaluation(omMaxBenefit)  //TODO:does not need a copy any more, is a copy anyway.
                        .timeWarp(+2)
                        .onlyBeneficialFor(color());  // piece would not come back for a negative benefit...
            }
            sameAxisMaxBenefit.timeWarp(+1)
                    .onlyBeneficialFor(color());  // piece would not come back for a negative benefit...
            if (futureReturnBenefits == null)
                futureReturnBenefits = sameAxisMaxBenefit;
            else
                futureReturnBenefits.maxEvalPerFutureLevelFor(sameAxisMaxBenefit, color());
            omMaxBenefit.setEval(0,0) // 0 out the direct move
                    .devideBy(3);
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL,"... - other moves' maxLostClashContribs="+ (maxLostClashContribs) + "*0.94 "
                        +" omax0/3=" + omMaxBenefit
                        + "+ move away chances="+movesAwayChances
                        + "+ future return benefits="+futureReturnBenefits+".");

            Evaluation newEmBenefit =
                new Evaluation(em.eval())                                       // new eval starts with the original one
                        .decEvaltoMinFor(maxDPMbenefit, color())         // decrease en passant takeable double pawn move to the eval given by the single pawn move
                        .addEval(movesAwayChances.getEvMove(em.to()).eval()) // add matching move away chance
                        .addEval(futureReturnBenefits)
                        .subtractEval(omMaxBenefit)                     // minus what I loose not choosing the other moves
                        .addEval(-(maxLostClashContribs-(maxLostClashContribs>>4)), 0 );// minus the best contribution that the move directly loses
//                        .addEval(-(int)(((double)maxLostClashContribs*((double)ChessBoard.engineP1()))/100.0), 0 );// minus the best contribution that the move directly loses
            /* works, but does not make sense,,, the evals of the double move should already be contained in the single move,,,
            if (pawnDoubleHopBenefits != null
                    && onSameFile(em.to(), em.from())
                    && distanceBetween(em.to(), em.from()) == 1  ) {
                // a simple pawn move, where a double pawn move is also still possible
                // it keeps the options of the 2 square pawn move
                pawnDoubleHopBenefits.timeWarp(+1);
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL,"... + remaining double pawn move chances "+ (pawnDoubleHopBenefits) );
                newEmBenefit.addEval(pawnDoubleHopBenefits);
            }
            */

            EvaluatedMove newEM = new EvaluatedMove(em, newEmBenefit);
            newLegalMovesAndChances.add(newEM);
            if (DEBUGMSG_MOVEEVAL)
                debugPrintln(DEBUGMSG_MOVEEVAL,"...=results in: "+ newEM + ".");
        }
        legalMovesAndChances = newLegalMovesAndChances;
    }

    @NotNull
    private Stream<EvaluatedMove> getAllMovesStream() {
        Stream<EvaluatedMove> allOtherMoves =
                Stream.concat(legalMovesAndChances.stream(),soonLegalMovesAndChances.stream());
        return allOtherMoves;
    }


    public void addChecking2AllMovesUnlessToBetween(final int fromPosExcl, final int toPosExcl, final VirtualPieceOnSquare checker) {
        getAllMovesStream()
            .filter(em -> em.to() != fromPosExcl)
            .filter( em -> (fromPosExcl<0 || !isBetweenFromAndTo(em.to(), fromPosExcl, toPosExcl ) ) )
            .filter( em -> isBasicallyALegalMoveForMeTo(em.to()) )
            .forEach(em -> {
                debugPrint(DEBUGMSG_MOVEEVAL," [" + fenCharFromPceType(myPceType) + em + "] ");
                board.getBoardSquare(em.to()).getvPiece(myPceID)
                    .setAbzugCheckGivingBy(checker);
            } );
    }

    public void addMoveAwayChance2AllMovesUnlessToBetween(final int benefit, final int futureNr,
                                                          final int fromPos, final  int toPosIncl,
                                                          final boolean chanceAddedForFromPos,
                                                          final int target
    ) {
        getAllMovesStream()
            .filter(em -> (chanceAddedForFromPos || em.to() != fromPos))
            .filter( em -> (fromPos < 0 || !( isBetweenFromAndTo(em.to(), fromPos, toPosIncl )
                                                       || em.to()==toPosIncl) ) )
            .filter( em -> isBasicallyALegalMoveForMeTo(em.to()) )  // todo: finish 47u3-experiment -> also process non-legal d==1 moves
            .forEach(em -> {
                if (DEBUGMSG_MOVEEVAL)
                    debugPrint(DEBUGMSG_MOVEEVAL,"  [indirectHelp:" + fenCharFromPceType(myPceType) + em + "] ");
                board.getBoardSquare(em.to()).getvPiece(myPceID)
                        .addMoveAwayChance(benefit, futureNr, target);
            } );
        // todo: could add "unless moving away opponents piece is covering e.g. the target square".
    }

    /* still unused, needs rework and testing */
    void giveLuftForKingInFutureBenefit() {
        int benefit = 0;
        boolean kcol;
        if (board.distanceToKing(myPos, WHITE)==1
                && color()==WHITE )
            kcol = WHITE;
        else if (board.distanceToKing(myPos, BLACK)==1
                && color()==BLACK )
            kcol = BLACK;
        else
            return;
        int kingPos = board.getKingPos(kcol);
        // counting checkable pieces is imprecise: checker might need to give up square control for checking and might have Nogo
        // but here we count those who cannot give check now, but only in the future (d==3 or d==4), as d==2 is covered more thouroughly in the king checking methods
        int checkingSoonPieces = board.getBoardSquare(kingPos).countFutureAttacksWithColor(opponentColor(kcol), 3);
        int checkingLaterPieces = board.getBoardSquare(kingPos).countFutureAttacksWithColor(opponentColor(kcol), 4);
        if ( board.nrOfLegalMovesForPieceOnPos(kingPos) <= 2
                && board.getBoardSquare(myPos).countDirectAttacksWithColor(opponentColor(kcol)) == 0
                && (checkingSoonPieces>0 || checkingLaterPieces>0 )
        ) { // king can be checked soon
            if ( checkingSoonPieces == 0 )
                benefit = EVAL_TENTH;
            else
                benefit = (EVAL_HALFAPAWN>>1) + (checkingSoonPieces<<1);  // 25+# = not so big benefit, as we cannot be sure here if it is mate... Todo: more thorough test
            benefit += checkingLaterPieces;
            if (isBlack(kcol))
                benefit = -benefit;
            if (DEBUGMSG_MOVEEVAL && abs(benefit)>4)
                debugPrintln(DEBUGMSG_MOVEEVAL, " Benefits of giving air to king at " + squareName(myPos) + " is: " + benefit + "@" + (checkingSoonPieces>0 ? 0 : 1) + ".");
            addMoveAwayChance2AllMovesUnlessToBetween(
                    benefit,
                    checkingSoonPieces>0 ? 0 : 1,
                    ANYWHERE, ANYWHERE, false, getPos() );
        }
    }

    /**
     *
     * @return nr of legal moves
     */
    public int selectBestMove() {
        resetBestMoves();
        if (getLegalMovesAndChances()==null)
            return 0;

        // collect moves and their chances from all vPces
        if (DEBUGMSG_MOVEEVAL) {
            debugPrintln(DEBUGMSG_MOVESELECTION, "");
            debugPrintln(DEBUGMSG_MOVESELECTION, "-- Checking " + this + " with stayEval=" + this.staysEval() + ": " + getLegalMovesAndChances());
        }
        int keepMaxBestMoves = KEEP_MAX_BEST_MOVES;
        /*TEST if (isQueen(getPieceType()))
            keepMaxBestMoves <<= 1;
        else if (isSlidingPieceType(getPieceType()))
            keepMaxBestMoves += keepMaxBestMoves>>1;*/
        for (EvaluatedMove em : getLegalMovesAndChances()) {
            if ( isPawn(myPceType) && isPromotionRankForColor(em.to(), color())) {
                // System.out.println("promotion!");
                em.setPromotesTo(isWhite() ? QUEEN : QUEEN_BLACK);
            }

            // flag checkGiving if so  // TODO: do this already in collectMoves
            if (board.getBoardSquare(em.to()).getvPiece(myPceID).isCheckGiving())
                em.setIsCheckGiving();

            if (color()==board.getTurnCol()) {
                // check board repetition by this move
                int leadsToRepetitions = board.moveLeadsToRepetitionNr(em.from(), em.to());
                if (leadsToRepetitions >= 3) {
                    int deltaToDraw = -board.boardEvaluation(1);
                    if (DEBUGMSG_MOVESELECTION)
                        debugPrintln(DEBUGMSG_MOVESELECTION, "  3x repetition after move " + em + " -> setting eval to " + deltaToDraw + ".");
                    em.initEval(deltaToDraw);
                } else if (leadsToRepetitions == 2) {
                    int deltaToDraw = -board.boardEvaluation(1);
                    if (DEBUGMSG_MOVESELECTION)
                       debugPrintln(DEBUGMSG_MOVESELECTION, "  drawish repetition ahead after move " + em + " -> changing eval half way towards " + deltaToDraw + ".");
                    em.changeEvalHalfWayTowards(deltaToDraw);
                }
            }
            if (DEBUGMSG_MOVESELECTION) {
                ChessPiece beatenPiece = board.getPieceAt(em.to());
                debugPrintln(DEBUGMSG_MOVESELECTION, "  chk move " + em
                        + (beatenPiece != null && beatenPiece.canMove()
                        ? " -" + beatenPiece.getBestMoveRelEval()
                        + "+" + board.getBoardSquare(getPos()).getvPiece(beatenPiece.myPceID).getClashContribOrZero()
                        : "."));
            }
            addEvaluatedMoveToSortedListOfCol(em,bestMoves,color(), keepMaxBestMoves, restMoves);
        }

        // a bit of a hack here, to add a never evaluated castling move... just assuming the evaluation 75 + the rook move + the king move
        if ( isKing(getPieceType())
                && board.isKingsideCastlingPossible(color())   //  only if allowed and
                && ( isWhite() ? getBestMoveRelEval()<EVAL_HALFAPAWN  // no other great king move is there
                               : getBestMoveRelEval()>(-EVAL_HALFAPAWN) )
        ) {
            EvaluatedMove castlingMove = new EvaluatedMove( getPos(), getPos()+2 );
            castlingMove.initEval(isWhite()  // 0.75
                    ?  (EVAL_HALFAPAWN + (EVAL_HALFAPAWN>>1))
                    : -(EVAL_HALFAPAWN + (EVAL_HALFAPAWN>>1))  );
            // find rook move - even rook needs to be found (due to chess960 support)
            int rookPos = board.findRook(getPos()+1, isWhite() ? coordinateString2Pos("h1")
                                                               : coordinateString2Pos("h8"));
            if (rookPos != NOWHERE
                    // and check three is no other rook in that range... then (chess960 scenario) we are not sure with which
                    // rook to castle - in normal chess, it disalows casteling for now.
                    // TODO for chess960: after the correct one moves away first and the wrong remains, this will
                    //  incorrectly seem "castleable". needs to be corrected by interpreting the X-FEN-castling-"flag"=rookpos correctly
                    && board.findRook(rookPos+1, isWhite() ? coordinateString2Pos("h1")
                                                           : coordinateString2Pos("h8")) == NOWHERE ) {
                ChessPiece rook = board.getPieceAt(rookPos);
                EvaluatedMove rookMove = rook.getLegalMovesAndChances().getEvMove(isWhite() ? CASTLING_KINGSIDE_ROOKTARGET[CIWHITE]
                                                                                                        : CASTLING_KINGSIDE_ROOKTARGET[CIBLACK] );
                if (rookMove!=null) {
                    castlingMove.addEval(rookMove.eval());  // add the eval of the single rook move, assuming this is still somewhat relevant
                    EvaluatedMove kingMove = getLegalMovesAndChances().getEvMove(getPos()+1 );
                    if (kingMove != null) {
                        castlingMove.addEval(kingMove.eval());  // add the eval of the single king move one to the right, assuming this is still somewhat relevant
                    } else
                        board.internalErrorPrintln("Castling problem: No King move?.");
                    if (DEBUGMSG_MOVESELECTION)
                        debugPrintln(DEBUGMSG_MOVESELECTION, "  Hurray, castling is possible! " + castlingMove + ".");
                    castlingMove.setBasicallyLegal();
                    addEvaluatedMoveToSortedListOfCol(castlingMove, bestMoves, color(), keepMaxBestMoves, restMoves);
                }
                else
                    board.internalErrorPrintln("Castling problem: No Rook move?.");
            }
            //else  - was an error, but not any more, it can ocur due to chess960 support, that the wrong rook moved in between in the meantime and prevents finding the right rook and prevents casteling for now
            //    board.internalErrorPrintln("Castling problem: No Rook?");
        }

        return bestMoves.size() + restMoves.size();
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

    /** returns the best move along each of the 4 possible axis, if it has at least a reasonable evaluation.
     *
     * @return Piece's best EvaluatedMove for each of the axis [E-W, NE-SW, N-S, SE-NW] (or [...,null,...])
     */
    public EvaluatedMove[] getBestReasonableEvaluatedMoveOnAxis() {
        EvaluatedMove[] bestMovesOnAxis = new EvaluatedMove[4];
        if (colorlessPieceType(myPceType)==KNIGHT)  // Todo: how to trap a Knight...  return all it's moves here?
            return bestMovesOnAxis;
        getLegalMovesAndChances().stream()
            .filter(em -> evalIsOkForColByMin(em.getEvalAt(0),color()) )
            .forEach(em -> {
                int dir = calcDirFromTo(getPos(), em.to());
                if ( dir != NONE ) {
                    int axisIndex = convertDir2AxisIndex(dir);
                    if (bestMovesOnAxis[axisIndex] == null || em.isBetterForColorThan(color(), bestMovesOnAxis[axisIndex]))
                        bestMovesOnAxis[axisIndex] = em;

                } else {
                    // TODO!!!: why does this happen?
                    //  e.g. in doMove_String_Test3():
                    //      Error: weiße Dame on h5 has move without direction!?: h5d2=[0, 0, 0, 0, 0, 0, 0]$51
                    // and doMove_isPinnedByKing_Test():
                    //      Error: schwarzer Turm on e2 has move without direction!?: e2b8=[0, 0, 0, 0, 0, 0, 0]$1
                    //      Error: schwarzer Turm on e2 has move without direction!?: e2b6=[0, 0, 0, 0, 0, 0, 0]$17
                    //      Error: schwarzer Turm on e2 has move without direction!?: e2b7=[0, 0, 0, 0, 0, 0, 0]$9
                    if (DEBUGMSG_MOVEEVAL_INTEGRITY)
                        debugPrintln(DEBUGMSG_MOVEEVAL_INTEGRITY, "Error: "+ this +" has move without direction!?: " + em );
                }
            } );
        return bestMovesOnAxis;
    }

/*    public int[] getBestMoveEval(int nr) {
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
*/

    public List<EvaluatedMove> getBestEvaluatedMoves() {
        if (bestMoves==null)
            return new ArrayList<>(0);
        return bestMoves;
    }

    public List<EvaluatedMove> getEvaluatedRestMoves() {
        if (restMoves==null)
            return new ArrayList<>(0);
        return restMoves;
    }

    public void resetRelEvalsAndChances() {
        for (int p=0; p<board.getBoardSquares().length; p++) {
            VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
            vPce.setRelEval(NOT_EVALUATED);
            vPce.resetRelEvalsAndChances();
        }
    }


    public void reduceToSingleContribution() {
        if (legalMovesAndChances == null || legalMovesAndChances.size() == 0)
            return;
        int highestContrib = 0;
        int highestContribPos = NOWHERE;
        for ( EvaluatedMove em : legalMovesAndChances.getAllEvMoves()) {
            int c = board.getBoardSquare(em.to()).getvPiece(getPieceID()).getClashContribOrZero();
            if ( c != 0) {
                /*if (highestContribPos==NOWHERE)
                    debugPrintln(DEBUGMSG_MOVEEVAL, "1st contribution of " + c + " on square " + squareName(em.getKey().to()) + " by " + this + ".");
                else
                    debugPrintln(DEBUGMSG_MOVEEVAL, " + further contribution " + c + " on square " + squareName(em.getKey().to()) + " by " + this + ".");
                */
                if ( isBetterThenFor(c, highestContrib, color()) ) {
                    handleOverworkedContribution(highestContribPos, highestContrib, em.to());
                    highestContribPos = em.to();
                    highestContrib = c;
                }
                // TODO: do not "randomly" choose one of the highest, but equal contributions
                else
                    handleOverworkedContribution(em.to(), c, highestContribPos);
            }
        }
    }

    private void handleOverworkedContribution(int targetPos, int price, int remainingContrPos ) {
        if (targetPos == NOWHERE)
            return;
        if (DEBUGMSG_MOVEEVAL)
            debugPrintln(DEBUGMSG_MOVEEVAL, "Eliminating contribution of " + price + " on square " + squareName(targetPos) + " for " + this + ".");
        board.getBoardSquare(targetPos).getvPiece(getPieceID()).setClashContrib(0);
        for (VirtualPieceOnSquare vPce : board.getBoardSquare(targetPos).getVPieces()) {
            if (vPce==null || vPce.color() == this.color() || vPce.coverOrAttackDistance()!=1 )
                continue;
            // here we have an opponent in attacking distance. it has advantage of hte fact that this piece can only defend at a high price
            // unless moving this piece would loose the attack on the other piece... (Todo: make this more precise by calculating the clash without vPce and this piece and subtracting this here instead of doing nothing
            if ( remainingContrPos==NOWHERE || board.getBoardSquare(remainingContrPos).getvPiece(vPce.getPieceID()).coverOrAttackDistance() != 1 ) {
                int bonus = - (price - (price >> 4));
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " -> bonus of " + bonus + " for " + vPce + ".");
                vPce.addChance(bonus, 0);
            }
        }
    }
}
