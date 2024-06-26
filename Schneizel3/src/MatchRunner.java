import engine.Engine;
import server.utils.jnn.Matrix;
import server.move.Move;
import server.utils.Constants;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class MatchRunner {

    public static void main(String[] args) {
        Engine engine = new Engine();

        System.out.print("Enter number of matches to run: ");

        int matchCount = new Scanner(System.in).nextInt();

        System.out.println("Running "+matchCount+" matches.");

        Random random = new Random();

        while(matchCount != 0){
            int moveTime = random.nextInt(500,1000);
            System.out.println("Move time: "+moveTime+" ms");
            ArrayList<Move> legalMoves = engine.getLegalMoves();
            Move randomMove = legalMoves.get(random.nextInt(legalMoves.size()));
            System.out.println(randomMove.info());
            engine.push(randomMove);
            Matrix result;
            while((result=engine.gameResult()) == Constants.GAME_RESULTS[Constants.CONTINUE_INDEX]){
                engine.beginSearch();
                try{
                    Thread.sleep(moveTime);
                }catch(Exception e){
                    e.printStackTrace();
                }
                Move bestMove = engine.getBestMove();
                System.out.println(bestMove.info());
                engine.push(bestMove);
            }

            System.out.println("Result: "+result.toString());
            //engine.trainNetwork(5);
            matchCount--;
        }
        engine.saveNetwork();

    }

}
