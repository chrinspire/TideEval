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

package de.ensel.UCI4ChessEngine;

import de.ensel.chessgui.ChessEngine;
import de.ensel.tideeval.ChessBoardController;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

import static de.ensel.tideeval.ChessBasics.FENPOS_STARTPOS;

public class UCI4ChessEngine {
    ChessEngine engine = null;
    BufferedOutputStream uciLog = null;
    boolean uciMode = true;

    public UCI4ChessEngine() {
        initNewBoard();
    }

    void initNewBoard() {
        playOrNewBoard(FENPOS_STARTPOS);
    }

    void playOrNewBoard(String fen) {
        if (engine==null)
            engine = new ChessBoardController();
        engine.setBoard(fen);
    }

    static final String RE_ONEORMORE_BLANKS = "((\\s)+)";
    static final String RE_BLANKS_ORNOTHING = "((\\s)*)";
    static final String RE_MOVE = "([a-h][1-8][a-h][1-8]([QqNnBbRr]?)((\\s)*))";
    static final String RE_FENBOARDPART = "("+RE_BLANKS_ORNOTHING+"(([pnbrqkKQRBNP/]|[1-8])+))";
    static final String RE_FENATTRIBPART = "("+RE_BLANKS_ORNOTHING+"(w|b)"
            +RE_ONEORMORE_BLANKS+"(([KQkqABCDEFGHabcdefgh]+)|\\-)"
            +RE_ONEORMORE_BLANKS+"(([a-h][1-8])|\\-)"
            +RE_ONEORMORE_BLANKS+"([0-9]+)"
            +RE_ONEORMORE_BLANKS+"([0-9]+)"
            +RE_BLANKS_ORNOTHING+")";

    static String name = "TideEval 0.47t3";
    public static void main(String[] args) throws Exception {
        UCI4ChessEngine uci4ce = new UCI4ChessEngine();
        uci4ce.initNewBoard();
        uci4ce.initUCI();

        System.out.println("Welcome to " + name + " by Christian Ensel");  // + uci4ce.engine.getBoard());

        try {
            Scanner scanner = new Scanner(System.in);
            String input = "";
            while (true) {
                uci4ce.nonUCIprint("C:\\> :-)  ");
                input = scanner.nextLine();
                uci4ce.writelnComLog("> " + input);
                if ( input.matches("uci("+RE_ONEORMORE_BLANKS+".*)?") ) {
                    uci4ce.initUCI();
                    uci4ce.answerUCI("id name " + name );  // + uci4ce.engine.getBoard());
                    uci4ce.answerUCI("id author Christian Ensel");
                    //uci4ce.answerUCI("option name minDepth type spin default "+wBoard.getSuggestedFurtherDepth()+" min 0 max 4");
                    //uci4ce.answerUCI("option name extraDepth type spin default "+(wBoard.getMaxFurtherDepth()-wBoard.getSuggestedFurtherDepth())+" min 0 max 8");
                    uci4ce.answerUCI("option name UCI_Chess960 type check default false");
                    //uci4ce.answerUCI("info string Hello, I'm " + name);
                    uci4ce.answerUCI("uciok");
                    continue;
                }

                else if (input.matches("isready")) {
                    uci4ce.answerUCI("readyok");
                }
                else if (input.matches("(ucinewgame)|(position startpos)|(new)")) {
                    uci4ce.initNewBoard();
                }
                else if (input.matches("position startpos moves" + RE_ONEORMORE_BLANKS + "(" + RE_MOVE + "+)")) {
                    if (input.length() > 23) {
                        input = input.substring(24);
                    }
                    input = input.trim();
                    uci4ce.writelnComLog("=new Board: + " + input);
                    uci4ce.playOrNewBoard(FENPOS_STARTPOS + " " + input);
                }
                else if (input.matches("setoption name.*")) {
                    input = input.substring(14).trim();
                    String[] params = input.split(RE_ONEORMORE_BLANKS,2);
                    uci4ce.writelnComLog("=set option " + params[0] + " to " + params[1]);
                    uci4ce.engine.setParam(params[0], params[1]);
                }
                else if (input.matches("position moves" + RE_ONEORMORE_BLANKS + "(" + RE_MOVE + "+)")) {
                    input = input.substring(15);
                    input = input.trim();
                    uci4ce.writelnComLog("=fresh board + moves " + input);
                    uci4ce.playOrNewBoard(FENPOS_STARTPOS + " " + input);
                }
                else if (input.matches("go((\\s)+.*)?")) {
                    uci4ce.writelnComLog("=go " + input);
                    String move = uci4ce.engine.getMove();
                    if (move!=null) {
                        //uci4ce.answerUCI("info pv " + move);
                        uci4ce.doUCIEngineMove(move);
                    }
                    else {
                        uci4ce.writelnComLog("No more moves found on board: " + uci4ce.engine.getBoard() + " Trying to initialize." );
                        uci4ce = new UCI4ChessEngine();
                        uci4ce.playOrNewBoard(uci4ce.engine.getBoard());
                        uci4ce.initUCI();
                        move = uci4ce.engine.getMove();
                        if (move!=null) {
                            //uci4ce.answerUCI("info pv " + move);
                            uci4ce.doUCIEngineMove(move);
                        }
                        else {
                            uci4ce.writelnComLog("No more moves found on board: " + uci4ce.engine.getBoard() + " -> Giving up.");
                            //uci4ce.answerUCI("exit");
                        }
                    }
                }

                // position fen 8/5p1p/2p2K1k/2P3RB/6P1/8/8/8 w - - 0 1
                // position fen 5r2/2p2rb1/1pNp4/p2Pp1pk/2P1K3/PP3PP1/5R2/5R2 w - - 1 51
                else if (input.matches("position fen" + RE_FENBOARDPART + "(" + RE_FENATTRIBPART + "?)")) {
                    input = input.substring(12);
                    input = input.trim();
                    uci4ce.writelnComLog("=fen board " + input);
                    uci4ce.playOrNewBoard(input);
                }
                else if (input.matches("position fen" + RE_FENBOARDPART + "(" + RE_FENATTRIBPART + "?)" + RE_ONEORMORE_BLANKS + "((moves" + RE_ONEORMORE_BLANKS + "(" + RE_MOVE + "+))?)")) {
                    input = input.substring(12);
                    input = input.trim();
                    uci4ce.writelnComLog("=fen board + moves " + input);
                    uci4ce.playOrNewBoard(input);
                }
                else if (input.matches("move" + RE_ONEORMORE_BLANKS + RE_MOVE)) {
                    input = input.substring(5).trim();
                    uci4ce.doMoves(input);
                }
                // else if (!uci4ce.uciMode && input.matches("^$"))
                else if (input.matches("(" + RE_BLANKS_ORNOTHING + RE_MOVE + ")+")) {
                    input = input.trim();
                    uci4ce.doMoves(input);
                }
                else if (input.matches("(info|square) [a-h][1-8]")) {
                    System.out.println("Square Statistics: " + uci4ce.engine.getSquareInfo(input.split(RE_ONEORMORE_BLANKS, 2)[1], ""));
                }
                else if (input.matches("(info|square) [a-h][1-8] [a-h][1-8]")) {
                    String[] params = input.split(RE_ONEORMORE_BLANKS,3);
                    System.out.println("Square Statistics: " + uci4ce.engine.getSquareInfo(params[1], params[2]));
                }
                else if (input.matches("board|show|fen")) {
                    System.out.println("Current board: " + uci4ce.engine.getBoard());
                }
                else if (input.matches("stats")) {
                    System.out.println("Board Statistics: " + uci4ce.engine.getBoardInfo());
                }
                else if (input.matches("info")) {
                    System.out.println(
                            "    TideEval - Wired New Chess Algorithm\n" +
                                    "    with UCI4CHessEngine - a simple UCI interpreter\n" +
                                    "    Copyright (C) 2023 Christian Ensel\n" +
                                    "    This program comes with ABSOLUTELY NO WARRANTY.\n" +       //; for details type `show w'.\n" +
                                    "    This is free software, and you are welcome to redistribute it\n" +
                                    "    under certain conditions, see GPLv3, file COPYING. \n");   // type `show c' for details.");
                }
                else if (input.matches(RE_BLANKS_ORNOTHING+"(exit|quit|ende)"+RE_BLANKS_ORNOTHING)) {
                    break;
                }
                else {
                    uci4ce.nonUCIprint("Hmm, " + input + "?");
                    uci4ce.nonUCIprint("to move:        move A1A4*");
                    uci4ce.nonUCIprint("to print details:  stats|square E3, info a2a4, board|show|stats");
                    uci4ce.nonUCIprint("to initalize:   new");
                    uci4ce.nonUCIprint("or:             exit");
                }
            }
        } catch (Exception e) {
            System.err.println("Sorry, Exception happened:");
            System.err.println(e.getMessage());
            uci4ce.writelnComLog("Error: " + e.getMessage());
            uci4ce.writelnComLog("Error: " + e.getStackTrace().toString());
            e.printStackTrace();
        }
        System.out.println("Thanks, goodbye!");
    }

    private void doUCIEngineMove(String move) {
        //int eval = engine.getBoardEvaluation();
        //answerUCI("info score cp " + eval);
        //String suggestedmove = engine.getMove();
        answerUCI("bestmove " + move);
        engine.doMove(move);
    }

    private void doMoves(String moveOrMoves) {
        String[] moves = moveOrMoves.split(RE_ONEORMORE_BLANKS);
        for (String move : moves) {
            engine.doMove(move);
            nonUCIprint("moving " + move);
        }
    }

    private void nonUCIprint(String msg) {
        if (!uciMode)
            System.out.print(msg);
    }

    void writelnComLog(String s) {
        if (uciLog==null || s==null)
            return;
        try {
            uciLog.write(s.getBytes());
            uciLog.write("\n".getBytes());
            uciLog.flush();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    void initUCI()  {
        try {
            uciLog = new BufferedOutputStream(new FileOutputStream("./tideeval_debug.out",true));
        } catch (Exception e){
            System.err.println("**** Fehler: Kann ComLog nicht schreibend öffnen.");
            uciLog = null;
        }
        uciMode = true;
        writelnComLog("Log started at: " + (new Date()) );
    }

    void answerUCI(String s)  {
        System.out.println(s);
        if (uciLog==null)
            return;
        writelnComLog("<- " + s);
    }

}