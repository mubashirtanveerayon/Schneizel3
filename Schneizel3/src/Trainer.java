import server.utils.jnn.Matrix;
import evaluation.network.EvaluationNetwork;
import server.board.Board;
import server.move.Move;
import server.utils.Constants;
import server.utils.PGNUtils;

import java.io.File;
import java.util.*;

public class Trainer {

    public static final String DATABASE_PATH = "data";

    public static final int NUMBER_OF_FINAL_POSITIONS_TO_TRAIN_ON = 7;

    public static void main(String[] args) {
        EvaluationNetwork.isTraining = true;

        File dbPath = new File(DATABASE_PATH);

        if(!dbPath.isDirectory()){
            return;
        }

        EvaluationNetwork.initialize();
        Matrix resultMatrix;
        int totalNumberOfGames = 1;
        for(File pgnFile:dbPath.listFiles()){
            if(!pgnFile.getName().endsWith(".pgn")){
                continue;
            }
            ArrayList<HashMap<String, String>> games = PGNUtils.parsePGNFile(pgnFile.getPath(),PGNUtils.ALL_GAMES,true);

            for(HashMap<String, String> game:games){
                String result = game.get("Result");
                if(result == null || result.equals("*")){
                    continue;
                }

                ArrayList<Move> movesMade = PGNUtils.getMoves(game.get("Moves"));
                if (movesMade.size() < 20){
                    continue;
                }


                if(result.equals("1-0")){
                    resultMatrix = Constants.GAME_RESULTS[Constants.WHITE];
                }else if(result.equals("0-1")){
                    resultMatrix = Constants.GAME_RESULTS[Constants.BLACK];
                }else{
                    resultMatrix = Constants.GAME_RESULTS[Constants.DRAW_INDEX];
                }



                Board board = new Board();
                int numberOfPositions = movesMade.size();
                int limit = Math.min(numberOfPositions, NUMBER_OF_FINAL_POSITIONS_TO_TRAIN_ON);


                for(int i=numberOfPositions; i>limit; i--){
                    Move move = movesMade.remove(0);
                    board.makeMove(move);
                }


                for(Move move:movesMade){
                    EvaluationNetwork.generateInputMatrix(board);
                    EvaluationNetwork.trainNetwork(EvaluationNetwork.inputMatrix,resultMatrix);
                    board.makeMove(move);
                }



                System.out.println("Training completed on game number: "+totalNumberOfGames);
                totalNumberOfGames++;

                if(totalNumberOfGames%10000 == 0){
                    EvaluationNetwork.save();
                    System.out.println("Network saved.");
                }

            }



        }

        EvaluationNetwork.save();
        System.out.println("Network saved.");



    }






}
