import engine.Engine;
import engine3.Engine3;
import server3.board.Board;
import server3.move.Move;
import server3.move.MoveGenerator;
import server3.utils.PGNUtils;
import server3.utils.jnn.Matrix;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class MatchRunner {

    public static void main(String[] args) {
        System.out.print("Enter number of games: ");

        int matchCount = new Scanner(System.in).nextInt();
        int done = 0;
        Random random = new Random();

        while(done <= matchCount){
            Engine3 e3 = new Engine3();
            Engine e2 = new Engine();
            int thinkTime = (int)(Math.random() * 1000);

            boolean v3IsBlack = done%2 == 0;
            String moveStr;
            ArrayList<String> movesMade = new ArrayList<>();
            String white,black;
            if(v3IsBlack){
                moveStr = e2.getLegalMoves().get(random.nextInt(20)).toString();
                e2.makeMove(e2.cvtToMove(moveStr));
                white = "v2";
                black = "v3";
//                e3.pushUCI(moveStr);
            }else{
                moveStr = e3.getLegalMoves().get(random.nextInt(20)).toString();
                e3.pushUCI(moveStr);
                white = "v3";
                black = "v2";
//                e2.makeMove(e2.cvtToMove(moveStr));
            }
            movesMade.add(moveStr);
            String result=null;
            while(!(e3.isFiftyMoveDraw() || e3.isInsufficientMaterial() || e3.isThreeFoldRepetition())){

                if(v3IsBlack){
                    e3.pushUCI(moveStr);
                    Matrix gameResult = e3.gameResult();
                    if(gameResult != null){
                        if(gameResult.array[0][0] != 0 || gameResult.array[1][0] != 0){
                            result = gameResult.array[0][0] == 1 ? "1-0":"0-1";
                        }else{
                            result = "1/2-1/2";
                        }
                        break;
                    }
                    e3.beginSearch(thinkTime,false);
                    try{
                        Thread.sleep(thinkTime);

                    }catch(Exception e){
                        e.printStackTrace();
                        break;
                    }
                    moveStr = e3.getBestMove().toString();
                    e3.push(e3.getBestMove());
                }else{
                    e2.makeMove(e2.cvtToMove(moveStr));
                    if(e2.getLegalMoves().isEmpty()){
                        if(e2.cb.checkers.isEmpty()){
                            result = "1/2-1/2";
                        }else{
                            result = e2.cb.whiteToMove ? "0-1":"1-0";
                        }
                        break;
                    }
                    e2.beginSearch(thinkTime);
                    try{
                        Thread.sleep(thinkTime);
                        moveStr = e2.getEngineMove().toString();
                        e2.makeMove(e2.getEngineMove());
                    }catch(Exception e){
                        e.printStackTrace();
                        break;
                    }

                }
                v3IsBlack = !v3IsBlack;
                movesMade.add(moveStr);
            }

            if(result == null){
                result = "1/2-1/2";
            }

            ArrayList<Move> allMoves = new ArrayList<>();
            Board board = new Board();
            for(String m:movesMade){
                Move move = MoveGenerator.fromString(board,m);
                board.makeMove(move);
                allMoves.add(move);
            }


            String moveText = PGNUtils.generateSANMoveText(allMoves);
            String pgn = "[Event \"v3 vs v2\"]\n";
            pgn += "[White "+white+"\"]\n";
            pgn += "[Black "+black+"\"]\n";
            pgn += "[Result "+result+"\"]\n";
            pgn += moveText;

            System.out.println(pgn);


            done++;

        }

    }



}
