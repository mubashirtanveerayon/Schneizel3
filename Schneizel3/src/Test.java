import evaluation.network.EvaluationNetwork;
import server.board.Board;
import server.move.Move;
import server.move.MoveGenerator;
import server.preload.PrecomputedMasks;
import server.utils.Constants;
import server.utils.PGNUtils;
import server.utils.Utils;
import server.utils.jnn.Matrix;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Test {

    public static final String DATABASE_PATH = "data";

    public static void main(String[] args) {

        EvaluationNetwork.initialize();

        int correctCount = 0, totalCount = 0;

        for(File file:new File(DATABASE_PATH).listFiles()){
            String fileName = file.getName();
            if(fileName.endsWith(".pgn")){


                ArrayList<HashMap<String,String>> games = PGNUtils.parsePGNFile(file.getPath(),PGNUtils.ALL_GAMES,true);

                for (HashMap<String,String> game:games){
                    String moves = (game.get("Moves"));
                    String result = (game.get("Result"));

                    if(result == null || result.isEmpty() || result.equals("*")){
                        continue;
                    }

                    ArrayList<Move> movesMade = PGNUtils.getMoves(moves);

                    if(movesMade.isEmpty()){
                        continue;
                    }

                    Board board = new Board();
                    int numberOfPositions = movesMade.size();
                    int limit = Math.min(numberOfPositions, 10);


                    for(int i=numberOfPositions; i>limit; i--){
                        Move move = movesMade.remove(0);
                        board.makeMove(move);
                    }

                    for(Move move:movesMade){

                        float eval = EvaluationNetwork.evaluatePosition(board);


                        if(eval>0){
                            if((result.equals("1-0") && board.side == Constants.WHITE) || (result.equals("0-1") && board.side == Constants.BLACK)){
                                correctCount ++;
                            }
                        }else{
                            if((result.equals("1-0") && board.side == Constants.BLACK) || (result.equals("0-1") && board.side == Constants.WHITE)){
                                correctCount ++;
                            }
                        }
                        totalCount++;

                        board.makeMove(move);

                    }

                    System.out.println("Correct guess: "+correctCount);


                }




            }
        }
        float accuracy = (float)correctCount/totalCount * 100;
        System.out.println("Accuracy: "+accuracy+"%");


    }
}
