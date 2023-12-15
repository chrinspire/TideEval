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

    static final int KEEP_MAX_BEST_MOVES = 4;
    List<EvaluatedMove> bestMoves;
    List<EvaluatedMove> restMoves;

    HashMap<Move, int[]> legalMovesAndChances;
    HashMap<Move, int[]> soonLegalMovesAndChances;


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

    public HashMap<Move, int[]> getMovesAndChances() {
        if (movesAndChances==null || movesAndChances.isEmpty())
            return null;

        return movesAndChances;
    }

    public HashMap<Move, int[]> getLegalMovesAndChances() {
        if (legalMovesAndChances == null)
            extractLegalAndSoonMovesAndChances();
        return legalMovesAndChances;
    }

    public HashMap<Move, int[]> getSoonLegalMovesAndChances() {
        if (legalMovesAndChances == null)
            extractLegalAndSoonMovesAndChances();
        return soonLegalMovesAndChances;
    }

    /** extracts the moves now on board from all movesAndChances.
     * Additionally, extracts the moves that would become legal if one condition is fulfilled.
     *
     * @returns legalMovesAndChances, sideEffect: sets legalMovesAndChances and soonLegalMovesAndChance
     */
    private void extractLegalAndSoonMovesAndChances() {
        legalMovesAndChances = new HashMap<>(movesAndChances.size());
        soonLegalMovesAndChances = new HashMap<>(movesAndChances.size());
        for (Map.Entry<Move, int[]> e : movesAndChances.entrySet() ) {
            if ( DEBUGMSG_MOVEEVAL_INTEGRITY
                    && isBasicallyALegalMoveForMeTo(e.getKey().to()) != e.getKey().isBasicallyLegal() )
                board.internalErrorPrintln("Inconsistent legality information for move " + e.getKey()
                        + "(" + Arrays.toString(e.getValue()) + ") isLegalNow()="+ e.getKey().isBasicallyLegal() + ".");
            if (isALegalMoveForMe(e.getKey())
            ) {
                //System.out.println("legal move of " + toString() + ": "+e.getKey());
                legalMovesAndChances.put(e.getKey(), e.getValue());
            }
            else {
                /*System.out.println("Detected not yet, but soon legal move of " + toString() + ": "+e.getKey()
                                    + ":(" + Arrays.toString(e.getValue()) + ")" ); */
                soonLegalMovesAndChances.put(e.getKey(), e.getValue());
            }
        }
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
    public void prepareMoves() {
        boolean prevMoveability = canMoveAwayReasonably();
        bestRelEvalAt = NOWHERE;
        int bestRelEvalSoFar = isWhite() ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE;
        Arrays.fill(mobilityFor3Hops, 0);  // TODO:remove line + member
        if (DEBUGMSG_MOVEEVAL) {
            debugPrintln(DEBUGMSG_MOVEEVAL, "");
            debugPrintln(DEBUGMSG_MOVEEVAL, "Adding relevals for piece " + this + " on square " + squareName(myPos) + ".");
        }
        for (int p=0; p<board.getBoardSquares().length; p++) {
            if (DEBUGMSG_MOVEEVAL && abs(board.getBoardSquare(p).getvPiece(myPceID).getRelEvalOrZero())>3)
                debugPrintln(DEBUGMSG_MOVEEVAL,"checking square "+ squareName(p)+": " + board.getBoardSquare(p).getvPiece(myPceID) + " ("+board.getBoardSquare(p).getvPiece(myPceID).getRelEvalOrZero()+").");
            VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
            final int relEval = vPce.getRelEvalOrZero();
            if (isBasicallyALegalMoveForMeTo(p)) {
                if (isWhite() ? relEval > bestRelEvalSoFar
                        : relEval < bestRelEvalSoFar) {
                    bestRelEvalSoFar = relEval;
                    bestRelEvalAt = p;
                }
                if (abs(relEval)>=3) {
                    if (!vPce.getMinDistanceFromPiece().hasNoGo()) {
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrintln(DEBUGMSG_MOVEEVAL, "Adding releval of " + relEval + "@" + 0
                                + " as unconditional result/benefit for " + vPce + " on square " + squareName(myPos) + ".");
                    } else {  // although it must have NoGo, it is still a valid move...
                        if (DEBUGMSG_MOVEEVAL)
                            debugPrintln(DEBUGMSG_MOVEEVAL, "Adding releval of " + relEval + "@" + 0
                                + " as result/benefit despite nogo for " + vPce + " on square " + squareName(myPos) + ".");
                    }
                }
                vPce.addChance(relEval, 0 );
            }
        }
        if (prevMoveability != canMoveAwayReasonably()) {
            // initiate updates/propagations for/from all vPces on this square.
            board.getBoardSquare(myPos).propagateLocalChange();
        }
    }

    public void rewardMovingOutOfTrouble() {
        final int relEval = board.getBoardSquare(myPos).getvPiece(myPceID).getRelEvalOrZero();
        // check if piece here itself is in trouble
        if ( !evalIsOkForColByMin(relEval, color(), -EVAL_HALFAPAWN) ) { // 47u22-47u66 added , - EVAL...
            this.addMoveAwayChance2AllMovesUnlessToBetween(
                    -relEval >> 3, 0,
                    ANY, ANY, false);  // staying fee
        }
    }

    /*
    public void preparePredecessorsAndMobility() {
        for (int d = 1 ; d <= board.MAX_INTERESTING_NROF_HOPS ; d++) {
            for (int p = 0; p < board.getBoardSquares().length; p++) {
                VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
                if (vPce.getRawMinDistanceFromPiece().dist() == d)
                    vPce.rememberAllPredecessors();
            }
        }
        for (int d = board.MAX_INTERESTING_NROF_HOPS; d>0; d--) {
            for (int p = 0; p < board.getBoardSquares().length; p++) {
                VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
                if (vPce!=null && vPce.getRawMinDistanceFromPiece().dist() == d) {
                    if (d == board.MAX_INTERESTING_NROF_HOPS) {
                        vPce.addMobility( 1<<(board.MAX_INTERESTING_NROF_HOPS-d) );
                        vPce.addMobilityMap(1 << p);
                    }
                    if (d>1) {
                        for (VirtualPieceOnSquare predVPce : vPce.getShortestReasonableUnconditionedPredecessors()) {
                            predVPce.addMobility((1 << (board.MAX_INTERESTING_NROF_HOPS - d)) + (vPce.getMobility()));
                            predVPce.addMobilityMap(vPce.getMobilityMap());
                        }
                    }
                    else if ( d==1 && !vPce.getMinDistanceFromPiece().hasNoGo() ) {
                        //System.out.println("Mobility on d=" + d + " for " + this + " on " + squareName(p) + ": " + vPce.getMobility() + " / " + bitMapToString(vPce.getMobilityMap()) + ".");

                        int benefit =  (vPce.getMobility()>>3);
                        if  ( benefit > (EVAL_TENTH<<1) )
                            benefit -= (benefit-(EVAL_TENTH<<1))>>1;
                        if (isKing(vPce.getPieceType()))
                            benefit >>= 2;
                        if (colorlessPieceType(getPieceType())==QUEEN)
                            benefit >>= 1;  // reduce for queens
                        if (!isWhite())
                            benefit = -benefit;

                        if (DEBUGMSG_MOVEEVAL && abs(benefit)>1)
                            debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit for mobility of " + vPce + " is " + benefit + "@0.");
                        vPce.addChance(benefit, 0 );
                        // award blocking others not possilble here, because not all mobilities are calculated ywt - just in progress here...
*/                        /*for (VirtualPieceOnSquare otherVPce : board.getBoardSquare(p).getVPieces() ) {
                            if ( otherVPce!=null && otherVPce!=vPce
                                    && (colorlessPieceType(vPce.getPieceType()) <= colorlessPieceType(otherVPce.getPieceType())
                                        || vPce.color()==otherVPce.color() )
                            ) {
                                benefit = isWhite() ? (-otherVPce.getMobility()>>3)
                                                     : (otherVPce.getMobility()>>3);
                                if (isKing(vPce.getPieceType()) || isQueen(vPce.getPieceType()))
                                    benefit >>= 1;
                                if (vPce.color()==otherVPce.color())
                                    benefit >>= 1;  // do not hold up ourselves so much
                                int nr = otherVPce.getStdFutureLevel()-1;
                                if (abs(benefit)>1) {
                                    debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit/fee for blocking mobility of " + otherVPce + " is " + benefit + "@" + nr + ".");
                                    vPce.addRawChance(benefit, nr);
                                }
                            }
                        }*/
/*                    }
                }
            }
        }
    }

*/

    void preparePredecessorsAndMobility() {
        for (int d = 1 ; d <= board.MAX_INTERESTING_NROF_HOPS ; d++) {
            for (int p = 0; p < board.getBoardSquares().length; p++) {
                VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
                if (vPce.getRawMinDistanceFromPiece().dist() == d)
                    vPce.rememberAllPredecessors();
            }
        }
        // initialize at the "end" of mobility
        // break it down, closer and closer to piece
        int mobBase = 0;
        for (int d = board.MAX_INTERESTING_NROF_HOPS; d>0; d--) {
            for (int p = 0; p < board.getBoardSquares().length; p++) {
                VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
                if (vPce!=null
                        && vPce.getRawMinDistanceFromPiece().dist() == d
                ) {
                    if (d == board.MAX_INTERESTING_NROF_HOPS) {
                        vPce.addMobility( 1);   // same as 1<<(board.MAX_INTERESTING_NROF_HOPS-d) );
                        vPce.addMobilityMap(1 << p);
                    }
                    int m = vPce.getMobility();
                    if (d>0) {
                        /*if (board.hasPieceOfColorAt(vPce.color(), p))
                            m -= m>>2;
                        if (vPce.getMinDistanceFromPiece().hasNoGo())
                            m -= m>>2;  // cannot be reached safely, so do not count so much. */
                        for (VirtualPieceOnSquare predVPce : vPce.getShortestReasonableUnconditionedPredecessors()) {
                            predVPce.addMobility((1 << (board.MAX_INTERESTING_NROF_HOPS - d))
                                    + /*(predVPce.isKillable() ? (m>>1) : m) */ m );
                            predVPce.addMobilityMap(vPce.getMobilityMap());
                        }
                    }
                    if (d==1 && m>mobBase)
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
                    case KNIGHT -> benefit -= benefit >> 2;  // reduce for knights
                    //case ROOK   -> benefit += benefit>>4;
                    //slightly worse: case BISHOP -> benefit -= benefit>>4;
                }

                if ( benefit > (EVAL_TENTH<<1) )
                    benefit -= (benefit-(EVAL_TENTH<<1))>>1;
                else if ( benefit < -(EVAL_TENTH<<1) )
                    benefit -= (benefit+(EVAL_TENTH<<1))>>1;

                if (!isWhite())
                    benefit = -benefit;

                if (DEBUGMSG_MOVEEVAL && abs(benefit)>1)
                    debugPrintln(DEBUGMSG_MOVEEVAL, " Benefit for mobility of " + vPce + " is " + benefit + "@0.");
                vPce.addChance(benefit,  0); // vPce.getRawMinDistanceFromPiece().isUnconditional() ? 0 : 1);

            }
        }
    }

    public int getBestMoveRelEval() {
        if (bestMoves!=null && bestMoves.size()>0 && bestMoves.get(0)!=null)
            return bestMoves.get(0).getEval()[0];
        if (bestRelEvalAt==NOWHERE)
            return isWhite() ? WHITE_IS_CHECKMATE : BLACK_IS_CHECKMATE;
        if (bestRelEvalAt==POS_UNSET)
            return NOT_EVALUATED;
        return board.getBoardSquares()[bestRelEvalAt].getvPiece(myPceID).getRelEvalOrZero();
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
        // size of 8 is exacly sufficient for all 1hop pieces,
        // but might be too small for slidigPieces on a largely empty board
        clearMovesAndRemoteChancesOnly();
        movesAwayChances = new HashMap<>(8);
        resetLegalMovesAndChances();
    }

    private void resetLegalMovesAndChances() {
        legalMovesAndChances = null;
    }

    void resetChancesOfAllVPces() {
        for (Square sq : board.getBoardSquares()) {
            final VirtualPieceOnSquare vPce = sq.getvPiece(myPceID);
            vPce.resetJustChances();
        }
    }


    private void clearMovesAndRemoteChancesOnly() {
        movesAndChances = new HashMap<>(8);
        forkingChances = new HashMap<>(8);
    }

    void addVPceMovesAndChances() {
        resetLegalMovesAndChances();
        HashMap<Integer,EvaluatedMove> collectedChances = new HashMap<>();
        for (Square sq : board.getBoardSquares()) {
            VirtualPieceOnSquare vPce = sq.getvPiece(myPceID);
            HashMap<Integer,EvaluatedMove> chances = vPce.getChances();
            for (Map.Entry<Integer,EvaluatedMove> e : chances.entrySet() ) {
                if ( sq.getMyPos() == getPos() ) {
                    addMoveAwayChance(e.getValue() );
                    continue;
                }
                //else
                EvaluatedMove em = collectedChances.get(e.getKey());
                if ( em == null ) {
                    // not found -> this is a new move+target combination
                    collectedChances.put(e.getKey(), e.getValue());
                }
                else {
                    // we already had an evaluation for the same move with the same target!
                    if (isWhite())
                        em.incEvaltoMax(e.getValue().getEval());
                    else
                        em.incEvaltoMin(e.getValue().getEval());
                }
            }
            /* List<HashMap<Move, Integer>> chances = vPce.getChances();
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
            } */
        }
        for (EvaluatedMove em : collectedChances.values()) {
            // perform the old collection routine on the collected (and for the same targets max-ed) moves
            // this ensures the largely same behaviour than before
            // TODO: refactor, not to use this old futureLevel-by-fl insertation of single values...
            for (int i = 0; i <= MAX_INTERESTING_NROF_HOPS; i++) {
                addMoveWithChance(new Move(em), i, em.getEval()[i] );
            }
        }
    }

    void addMoveWithChance(Move move, int futureLevel, int relEval) {

        if (DEBUGMSG_MOVEEVAL_INTEGRITY ) {
            if (isBasicallyALegalMoveForMeTo(move.to()) != move.isBasicallyLegal()) {
                board.internalErrorPrintln("Inconsistent legality information at addMoveWithChance for move " + move
                        + " isLegalNow()=" + move.isBasicallyLegal() + ".");
            }
            if (board.getBoardSquare(move.to()).getvPiece(getPieceID()).getRawMinDistanceFromPiece().dist() != 1) {
                board.internalErrorPrintln("Inconsistent distance addMoveWithChance for move " + move
                        + " at " + board.getBoardSquare(move.to()).getvPiece(getPieceID()) + ".");
            }
        }

        int[] evalsPerLevel = movesAndChances.get(move);
        if (evalsPerLevel==null) {
            evalsPerLevel = new int[MAX_INTERESTING_NROF_HOPS+1];
            evalsPerLevel[futureLevel] = relEval;
            movesAndChances.put(move, evalsPerLevel);
        }
        else {
            // while collecting evaluations of moves, it is awarded to have multiple boni on one move
            // (not identical, but similar to forks)
            // remark: forking a king is only accounted a little checking benefit here, because checking alone would
            // not be much benefit, so we need to check the extra flag -> but not here
            int[] forkingChancePerLevel = forkingChances.get(move);
            int forkingbenefit = isWhite() ? min(relEval, evalsPerLevel[futureLevel])
                                           : max(relEval, evalsPerLevel[futureLevel]);
            if ( evalIsOkForColByMin(forkingbenefit, color(), -(positivePieceBaseValue(PAWN)-EVAL_TENTH) ) ) {
                if (forkingChancePerLevel == null) {
                    forkingChancePerLevel = new int[MAX_INTERESTING_NROF_HOPS + 1];
                    forkingChances.put(move,forkingChancePerLevel);
                    // TODO!!! : PUSH
                }
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
                forkingbenefit >>= 1;  // lets be moderate in the test of the fork benefit feature...
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL, "Detected fork-like chances of " + forkingbenefit + "@" + (futureLevel - 1) + " for " + this.toString()
                        + " after move " + move + ".");
                if (futureLevel > 0)
                    addMoveWithChance(move, futureLevel - 1, forkingbenefit); // add forking benefit one move earlier -> the real fork moment
            }
            evalsPerLevel[futureLevel] += relEval;
        }
    }

    /*private void addMoveAwayChance(Move move, int futureLevel, int relEval) {
        int[] evalsPerLevel = movesAwayChances.get(move);
        if (evalsPerLevel==null) {
            evalsPerLevel = new int[MAX_INTERESTING_NROF_HOPS+1];
            evalsPerLevel[futureLevel] = relEval;
            movesAwayChances.put(move, evalsPerLevel);
        } else {
            evalsPerLevel[futureLevel] += relEval;
        }
    }*/

    private void addMoveAwayChance(EvaluatedMove move) {
        int[] evalsPerLevel = movesAwayChances.get(move);
        if (evalsPerLevel==null) {
            evalsPerLevel = move.getEval().clone();
            movesAwayChances.put(move, evalsPerLevel);
        } else {
            for (int i=0; i<evalsPerLevel.length; i++)
                evalsPerLevel[i] += move.getEval()[i];
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
        StringBuilder res = new StringBuilder(""+movesAndChances.size()+" moves: " );
        for ( Map.Entry<Move,int[]> m : movesAndChances.entrySet() ) {
            res.append(" " + m.getKey()+"=");
            if (m.getValue().length>0
                    && m.getKey().isBasicallyLegal()  //abs(m.getValue()[0])<checkmateEval(BLACK)+ pieceBaseValue(QUEEN) ) {
            ) {
                for (int eval : m.getValue())
                    res.append((eval == 0 ? "" : eval) + "/");
            }
            else {
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
        return board.getBoardSquare(myPos).clashEval(); // .getvPiece(myPceID).getRelEval();
    }

    /**
     * Collect moves with dist==1 (legal and moves with conditions) and their chances from all vPces.
     * So careful, this also adds illegal moves "onto" own pieces (needed to keep+calc their clash contributions)
     * and through opponents pieces  (needed if opponents move away enables a move)
     */
    public void collectMoves() {
        clearMovesAndAllChances();
        // TODO: do not search for the moves... should be collected during distance calculating wherever dist matches...
        for (Square sq : board.getBoardSquares()) {
            sq.getvPiece(myPceID).resetBasics();
            if (sq.getvPiece(myPceID).getRawMinDistanceFromPiece().dist()==1) {  // TODO!: test with filter for only unconditional here
                Move m = new Move(myPos,sq.getMyPos());
                if ( isBasicallyALegalMoveForMeTo(sq.getMyPos()) )
                    m.setBasicallyLegal();
                addMoveWithChance(m, 0, 0);
            }
        }
    }

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

    public int getTargetOfContribSlidingOverPos(int blockingPos) {
        if ( !isSlidingPieceType(myPceType) )
            return 0;
        HashMap<Move,int[]> simpleMovesAndChances = movesAndChances;
        for (Map.Entry<Move, int[]> m : simpleMovesAndChances.entrySet()) {
            if (!m.getKey().isBasicallyLegal()) // abs(m.getValue()[0]) >= checkmateEval(BLACK) + pieceBaseValue(QUEEN))
                continue;
            if ( m.getKey().direction() == calcDirFromTo(myPos, blockingPos))
                return m.getKey().to();
        }
        return NOWHERE;
    }

    public void mapLostChances() {
        // calculate into each first move, that it actually looses (or prolongs) the chances of the other first moves
        HashMap<Move,int[]> simpleMovesAndChances = movesAndChances;
        clearMovesAndRemoteChancesOnly();
        for (Map.Entry<Move, int[]> m : simpleMovesAndChances.entrySet())  {
            if ( m.getKey().isBasicallyLegal() ) { // abs(m.getValue()[0]) < checkmateEval(BLACK)+ pieceBaseValue(QUEEN) ) {
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL,"Map lost chances for: "+ m.getKey() + "=" + Arrays.toString(m.getValue()) +".");
                int[] omaxbenefits = new int[m.getValue().length];
                // calc non-negative maximum benefit of the other (hindered/prolonged) moves
                int maxLostClashContribs=0;
                // deal with double square pawn moves that could be taken en passant:
                // maximise their benefit to the one of hte single pawn move.
                boolean doubleSquarePawnMoveLimiting = isPawn(getPieceType())
                        && distanceBetween(m.getKey().from(),m.getKey().to())==2
                        && board.getBoardSquare(m.getKey().from()+(isWhite()?UP:DOWN)).isAttackedByPawnOfColor(opponentColor(color()));
                int[] maxDPMbenefit = null;
                int[] pawnDoubleHopBenefits = null;

                ChessPiece moveTargetPce = board.getBoardSquare(m.getKey().to()).myPiece();
                for (Map.Entry<Move, int[]> om : simpleMovesAndChances.entrySet()) {
                    if (m != om
                            && ( (isSlidingPieceType(getPieceType())
                                    && !dirsAreOnSameAxis(m.getKey().direction(), om.getKey().direction()))
                                 || (!isSlidingPieceType(getPieceType()) ) )   // Todo! is wrong for queen with magic right triangle
                    ) {
                        // note: do not check for legal moves here, moving onto a covering piece would always be illegal... so no if ( isBasicallyALegalMoveForMeTo(om.getKey().to() ) )

                        // special pawn thing...
                        if ( isPawn(getPieceType())
                                && fileOf(m.getKey().to()) == fileOf(om.getKey().to())
                                && abs(rankOf(om.getKey().from()) - rankOf(om.getKey().to())) == 2 )  // 2 square move
                            pawnDoubleHopBenefits = om.getValue().clone();

                        // special case: queen with magic right triangle  (and same for King at dist==1)
                        if ( ( ( isQueen(getPieceType() )
                                    && !board.posIsBlockingCheck(color(),m.getKey().to() ) )
                               || ( isKing(getPieceType())
                                    && distanceBetween(m.getKey().to(), om.getKey().to())==1 )
                             )
                                 && ( ( fileOf(m.getKey().to()) == fileOf(om.getKey().to())
                                        && fileOf(m.getKey().from()) != fileOf(m.getKey().to()) )
                                     || ( rankOf(m.getKey().to()) == rankOf(om.getKey().to())
                                        && rankOf(m.getKey().from()) != rankOf(m.getKey().to()) )
                                    )
                                 && board.allSquaresEmptyFromto(m.getKey().to(),om.getKey().to() )
                        ) {
                            if (DEBUGMSG_MOVEEVAL)
                                debugPrintln(DEBUGMSG_MOVEEVAL, "("+this+" is happy about magical right triangle to " + squareName(om.getKey().to()) + " and " + squareName(m.getKey().to()) + ".");
                        }
                        else {
                            int omLostClashContribs = board.getBoardSquare(om.getKey().to())
                                    .getvPiece(myPceID).getClashContribOrZero();
                            if (DEBUGMSG_MOVEEVAL && abs(omLostClashContribs) >= 0)
                                debugPrintln(DEBUGMSG_MOVEEVAL, ".. checking other move " + om.getKey()
                                        + " 's + lostClashContrib=" + omLostClashContribs + ".");
                            if (moveTargetPce != null) {
                                int targetPceSameClashContrib = board.getBoardSquare(om.getKey().to())
                                        .getvPiece(moveTargetPce.getPieceID()).getClashContribOrZero();
                                if (abs(targetPceSameClashContrib) > EVAL_DELTAS_I_CARE_ABOUT) {
                                    if (DEBUGMSG_MOVEEVAL && abs(targetPceSameClashContrib) > 0)
                                        debugPrintln(DEBUGMSG_MOVEEVAL, "  (" + moveTargetPce + " has contrib of "
                                                + targetPceSameClashContrib + " on same square, which I make impossible by taking it.  This positively counteracts loosing my contribution.)");
                                    omLostClashContribs = (omLostClashContribs + targetPceSameClashContrib) >> 4;  // could also be set to 0, see comparison v.29z10-13
                                }
                            }
                        /* not needed any more, see special triangle above: if (isQueen(myPceType))
                            omLostClashContribs -= omLostClashContribs >> 2;  // *0,75  // as there is a chance to cover it, see todo above, which would really solve this... */
                            if (isWhite() ? omLostClashContribs > maxLostClashContribs
                                          : omLostClashContribs < maxLostClashContribs) {
                                maxLostClashContribs = omLostClashContribs;
                            }
                        }
                        // TODO: to take the move axis as indicator for moving away (or not) works for T and B, but not always for Q due to the "magic recangle", which needs to  be taken into account here (does the target position still cover the piece we have contribution to?)
                        /*if (omClashContrib!=0)
                            System.out.println(om.getKey()+"-clashContrib="+omClashContrib);*/
                        //wrong, because movingAwayFees are important still : was: not this: as we only what to calculate real benefits, not moves that are anyway negative for me
                        /*if (isWhite() && omClashContrib > omaxbenefits[0]
                                || !isWhite() && omClashContrib < omaxbenefits[0] )
                                omaxbenefits[0] = omClashContrib; */
                        //TODO: check if isBasicallyALegalMoveForMeTo should not be better here than strange value comparison
                        if ( om.getKey().isBasicallyLegal()) {  // abs(om.getValue()[0]) < checkmateEval(BLACK)+ pieceBaseValue(QUEEN) ) {
                            // if this is a doable move, we also consider its further chances (otherwise only the clash contribution)
                            for (int i = 0; i < m.getValue().length; i++) {
                                // non-precise assumption: benefits of other moves can only come one (back) hop later, so we find their maximimum and subtract that
                                // todo: think about missed move opportunities more thoroughly :-) e.g. king moving N still has same distance to NW and NE square than before...
                                int val = om.getValue()[i];
                                if (isWhite() &&  val > omaxbenefits[i]
                                        || !isWhite() && val < omaxbenefits[i] )
                                    omaxbenefits[i] = val;
                            }
                        }

                        if (doubleSquarePawnMoveLimiting && fileOf(m.getKey().to())==fileOf(om.getKey().to()) )
                            maxDPMbenefit = Arrays.copyOf(om.getValue(), om.getValue().length);
                    }
                }
                boolean mySquareIsSafeToComeBack = !isPawn(myPceType ) && evalIsOkForColByMin(staysEval(), color()); // TODO: or is sliding piece and chance is from opposit direction
                int[] newmbenefit = new int[m.getValue().length];
                int[] maCs = movesAwayChances.get(new EvaluatedMove(m.getKey(),getPos()));
                /*// did neither improve nor make it worse (also with >>1)
                if ( !evalIsOkForColByMin(staysEval(), color()) )
                    maxLostClashContribs >>= 2; // ideas: if we cannot stay, because we will be killed, then our clash contributions are not worth much any more. */
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL,"... - other moves' maxLostClashContribs="+ (maxLostClashContribs)+"*0.88 max=" + Arrays.toString(omaxbenefits) + "/4 "
                                                         + "+ move away chances="+Arrays.toString(maCs)+".");
                for (int i = 0; i < m.getValue().length; i++) {
                    // unprecise assuption: benefits of other moves can only come one (back) hop later, so we find their maximimum and subtract that
                    // todo: think about missed move opportunities more thoroughly :-) e.g. king moving N still has same distance to NW and NE square than before...
                    int maC = maCs!=null ? maCs[i] : 0;
                    newmbenefit[i] = m.getValue()[i]     // original chance
                            - ( i==0 ? (maxLostClashContribs-(maxLostClashContribs>>3))
                                     : (omaxbenefits[i]>>2) )   // minus what I loose not choosing the other moves // maybe not, punishes forks...
                            + ( (i>1 && mySquareIsSafeToComeBack) ? omaxbenefits[i-2]:0)   // plus adding that what the other moves can do, I can now still do, but one move later
                            + maC
                            + ( (i>0 && pawnDoubleHopBenefits!=null) ? pawnDoubleHopBenefits[i-1] : 0); // first simple pawn move keeps the options of the 2 square pawn move
                    // plus the chance of this move, because the piece moves away
                    if (maxDPMbenefit!=null)
                        newmbenefit[i] = isWhite() ? min(newmbenefit[i], maxDPMbenefit[i] )
                                                   : max(newmbenefit[i], maxDPMbenefit[i] );
                }
                movesAndChances.put(m.getKey(), newmbenefit);
                if (DEBUGMSG_MOVEEVAL)
                    debugPrintln(DEBUGMSG_MOVEEVAL,"...=results in: "+ m.getKey() + "="+ Arrays.toString(movesAndChances.get(m.getKey())) + ".");
            }
        }
    }


    public void addChecking2AllMovesUnlessToBetween(final int fromPosExcl, final  int toPosExcl) {
        for ( Map.Entry<Move,int[]> e : movesAndChances.entrySet() ) {
            int to = e.getKey().to();
            if ( to != fromPosExcl
                    && (fromPosExcl<0 || !isBetweenFromAndTo(to, fromPosExcl, toPosExcl ) ) ) {
                debugPrint(DEBUGMSG_MOVEEVAL,"->[indirectCheckHelp:" + fenCharFromPceType(myPceType) + e.getKey() + "]: ");
                VirtualPieceOnSquare toVPce = board.getBoardSquare(to).getvPiece(myPceID);

                if (isBasicallyALegalMoveForMeTo(to))   // 47u3 -> also process non-legal d==1 moves
                    toVPce.setCheckGiving();
                else if (DEBUGMSG_MOVEEVAL)
                    debugPrint(DEBUGMSG_MOVEEVAL, "[" + e.getKey() + " is no legal move]");
            }
        }
    }

    public void addMoveAwayChance2AllMovesUnlessToBetween(final int benefit, final int futureNr,
                                                          final int fromPos, final  int toPosIncl,
                                                          final boolean chanceAddedForFromPos) {
        for ( Map.Entry<Move,int[]> e : movesAndChances.entrySet() ) {
            int to = e.getKey().to();
            if ( (chanceAddedForFromPos || to!=fromPos)
                    && (fromPos<0 || !(isBetweenFromAndTo(to, fromPos, toPosIncl ) || to==toPosIncl))) {
                debugPrint(DEBUGMSG_MOVEEVAL,"->[indirectHelp:" + fenCharFromPceType(myPceType) + e.getKey() + "]: ");
                VirtualPieceOnSquare baseVPce = board.getBoardSquare(myPos).getvPiece(myPceID);

                if (isBasicallyALegalMoveForMeTo(to))    // 47u3 -> also process non-legal d==1 moves
                    baseVPce.addMoveAwayChance(benefit, futureNr,e.getKey());
                else if (DEBUGMSG_MOVEEVAL)
                    debugPrint(DEBUGMSG_MOVEEVAL, "[" + e.getKey() + " is no legal move]");
            }
        }
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
                    -1, -1, false );
        }
    }

    /**
     *
     * @return nr of legal moves
     */
    public int selectBestMove() {
        int nrOfLegalMoves = 0;
        resetBestMoves();
        if (getLegalMovesAndChances()==null)
            return 0;

        // collect moves and their chances from all vPces
        if (DEBUGMSG_MOVEEVAL) {
            debugPrintln(DEBUGMSG_MOVESELECTION, "");
            debugPrintln(DEBUGMSG_MOVESELECTION, "-- Checking " + this + " with stayEval=" + this.staysEval() + ": " + getLegalMovesAndChances().keySet().toString());
        }
        for (Map.Entry<Move, int[]> e : getLegalMovesAndChances().entrySet()) {
            EvaluatedMove eMove =  new EvaluatedMove(e.getKey(), e.getValue());
            if ( isPawn(myPceType) && isPromotionRankForColor(eMove.to(), color())) {
                // System.out.println("promotion!");
                eMove.setPromotesTo(isWhite() ? QUEEN : QUEEN_BLACK);
            }
            nrOfLegalMoves++;

            // flag checkGiving if so
            // TODO: do this already in collectMoves - but needs movesAndChances-Hashmap to be converted into a List or Set of newer&nicer EvaluatedMoves
            if (board.getBoardSquare(eMove.to()).getvPiece(myPceID).isCheckGiving())
                eMove.setIsCheckGiving();

            ChessPiece beatenPiece = board.getPieceAt(e.getKey().to());
            /*int corrective = // here not  -staysEval()
                    - (beatenPiece != null && beatenPiece.canMove()
                            ? (( beatenPiece.getBestMoveRelEval()  // the best move except its contribution that is already calculated in the stayEval...
                            -board.getBoardSquares()[myPos].getvPiece(beatenPiece.myPceID).getClashContrib() )>>2)   // /4, as it might not be the best opponents move and thus never be done) - the best ar calculated separately later in the overall move selection
                            : 0);  // eliminated effect of beaten Piece
            */

            if (color()==board.getTurnCol()) {
                // check board repetition by this move
                int leadsToRepetitions = board.moveLeadsToRepetitionNr(eMove.from(), eMove.to());
                if (leadsToRepetitions >= 3) {
                    int deltaToDraw = -board.boardEvaluation(1);
                    if (DEBUGMSG_MOVESELECTION)
                        debugPrintln(DEBUGMSG_MOVESELECTION, "  3x repetition after move " + e.getKey()
                            + " setting eval " + Arrays.toString(e.getValue()) + " to " + deltaToDraw + ".");
                    eMove.initEval(deltaToDraw);
                } else if (leadsToRepetitions == 2) {
                    int deltaToDraw = -board.boardEvaluation(1);
                    if (DEBUGMSG_MOVESELECTION)
                       debugPrintln(DEBUGMSG_MOVESELECTION, "  drawish repetition ahead after move " + e.getKey()
                            + " changing eval " + Arrays.toString(e.getValue()) + " half way towards " + deltaToDraw + ".");
                    for (int i = 0; i < eMove.getEval().length; i++)
                        eMove.getEval()[i] = (deltaToDraw + eMove.getEval()[i]) >> 1;
                }
            }
            if (DEBUGMSG_MOVESELECTION)
                debugPrintln(DEBUGMSG_MOVESELECTION,"  chk move " + e.getKey() + " " + Arrays.toString(e.getValue())
                    + (beatenPiece != null && beatenPiece.canMove()
                    ? " -" + beatenPiece.getBestMoveRelEval()
                    + "+" + board.getBoardSquares()[myPos].getvPiece(beatenPiece.myPceID).getClashContribOrZero()
                    : "."));
            addEvaluatedMoveToSortedListOfCol(eMove,bestMoves,color(), KEEP_MAX_BEST_MOVES, restMoves);
        }

        // a bit of a hack here, to add a never evaluated castling move... just assuming the evaluation 75 + the rook move
        if ( isKing(getPieceType())
                && board.isKingsideCastlingPossible(color())   //  only if allowed and
                && ( isWhite() ? getBestMoveRelEval()<EVAL_HALFAPAWN  // no other great king move is there
                               : getBestMoveRelEval()>(-EVAL_HALFAPAWN) )
        ) {
            EvaluatedMove castlingMove = new EvaluatedMove( getPos(), getPos()+2 );
            castlingMove.initEval(isWhite()  // 0.75
                    ?  (EVAL_HALFAPAWN + (EVAL_HALFAPAWN>>1))
                    : -(EVAL_HALFAPAWN + (EVAL_HALFAPAWN>>1))  );
            // find rook move
            int rookPos = board.findRook(getPos()+1, isWhite() ? coordinateString2Pos("h1") : coordinateString2Pos("h8"));
            if (rookPos != NOWHERE) {
                ChessPiece rook = board.getPieceAt(rookPos);
                HashMap<Move, int[]> rookMovesAndChances = rook.getLegalMovesAndChances();
                if (rookMovesAndChances!=null) {
                    for (Map.Entry<Move, int[]> e : movesAndChances.entrySet()) {
                        if (e.getKey().to() == (isWhite() ? CASTLING_KINGSIDE_ROOKTARGET[CIWHITE]
                                : CASTLING_KINGSIDE_ROOKTARGET[CIBLACK])) {
                            castlingMove.addEval(e.getValue());  // add the eval of the single rook move, assuming this is still somewhat relevant
                            break;
                        }
                    }
                    HashMap<Move, int[]> kingMovesAndChances = getLegalMovesAndChances();
                    if (kingMovesAndChances != null) {
                        for (Map.Entry<Move, int[]> e : movesAndChances.entrySet()) {
                            if (e.getKey().to() == getPos()+1) {
                                castlingMove.addEval(e.getValue());  // add the eval of the single king move one to the right, assuming this is still somewhat relevant
                                break;
                            }
                        }
                    } else
                        board.internalErrorPrintln("Castling problem: No King move?.");
                    if (DEBUGMSG_MOVESELECTION)
                        debugPrintln(DEBUGMSG_MOVESELECTION, "  Hurray, castling is possible! " + castlingMove + ".");
                    castlingMove.setBasicallyLegal();
                    addEvaluatedMoveToSortedListOfCol(castlingMove, bestMoves, color(), KEEP_MAX_BEST_MOVES, restMoves);
                }
                else
                    board.internalErrorPrintln("Castling problem: No Rook move?.");
            }
            else
                board.internalErrorPrintln("Castling problem: No Rook?");

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

    public int getNrBestMoves() {
        return bestMoves.size();
    }

    public void resetRelEvalsAndChances() {
        for (int p=0; p<board.getBoardSquares().length; p++) {
            VirtualPieceOnSquare vPce = board.getBoardSquare(p).getvPiece(myPceID);
            vPce.setRelEval(NOT_EVALUATED);
            vPce.resetKillable();
            vPce.resetBasics();
            vPce.resetPredecessors();
        }
    }


    public void reduceToSingleContribution() {
        if ( getMovesAndChances()==null)
            return;
        int highestContrib = 0;
        int highestContribPos = NOWHERE;
        for (Map.Entry<Move, int[]> e : getMovesAndChances().entrySet() ) {
            int c = board.getBoardSquare(e.getKey().to()).getvPiece(getPieceID()).getClashContribOrZero();
            if ( c != 0) {
                /*if (highestContribPos==NOWHERE)
                    debugPrintln(DEBUGMSG_MOVEEVAL, "1st contribution of " + c + " on square " + squareName(e.getKey().to()) + " by " + this + ".");
                else
                    debugPrintln(DEBUGMSG_MOVEEVAL, " + further contribution " + c + " on square " + squareName(e.getKey().to()) + " by " + this + ".");
                */
                if ( isWhite() ? c > highestContrib : c < highestContrib ) {
                    handleOverworkedContribution(highestContribPos, highestContrib, e.getKey().to());
                    highestContribPos = e.getKey().to();
                    highestContrib = c;
                }
                // TODO: do not "randomly" choose one of the highest, but equal contributions
                else
                    handleOverworkedContribution(e.getKey().to(), c, highestContribPos);
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
