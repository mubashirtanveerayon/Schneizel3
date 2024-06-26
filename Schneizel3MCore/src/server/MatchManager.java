package server;
import server.board.Board;
import server.exception.IllegalMoveException;
import server.move.Move;
import server.move.MoveGenerator;
import server.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class MatchManager {

    public Board currentPosition;

    public Stack<Board> stack;
    HashMap<Long,Integer> occurredPositions;

    public boolean isThreeFoldRepetition;

    public long searchTime;

    public MatchManager(MatchManager other){
        
    }



    public MatchManager(String fen){
        init(fen);
    }


    public MatchManager(){
        init(Constants.STARTING_FEN);
    }
    private void init(String fen){
        currentPosition = new Board(fen);
        stack = new Stack<>();
        occurredPositions = new HashMap<>();
        occurredPositions.put(currentPosition.hash, 1);
    }

    public void push(Move move){
        stack.push(new Board(currentPosition));
        currentPosition.makeMove(move);
        if(occurredPositions.containsKey(currentPosition.hash)){
            int occurrence = occurredPositions.get(currentPosition.hash);
            if(occurrence >= 2){
                isThreeFoldRepetition = true;
            }
            occurredPositions.put(currentPosition.hash,occurrence+1);
        }else{
            occurredPositions.put(currentPosition.hash, 1);
        }
    }

    public void pushUCI(String moveStr) throws IllegalMoveException {
        Move move = MoveGenerator.fromString(currentPosition,moveStr);
        if(move == null){
            throw new IllegalMoveException(fen(),moveStr);
        }
        push(move);
    }

    public String fen(){
        return currentPosition.fen();
    }

    public void takeBack(){
        long recentPositionHash = currentPosition.hash;
        currentPosition = stack.pop();
        int occurrence = occurredPositions.get(recentPositionHash);
        occurrence -= 1;
        if(occurrence >= 2){
            isThreeFoldRepetition = false;
        }
        occurredPositions.put(recentPositionHash,occurrence);
        if(occurrence <= 0){
            occurredPositions.remove(recentPositionHash);
        }
    }

    public String perft(int depth){
        int totalNodes = 0;
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);
        searchTime = System.currentTimeMillis();
        String output = "";
        for(Move move:moves){
            push(move);
            //System.out.println(move.info());
            int numberOfPositions = positionCounter(depth - 1);
            takeBack();

            totalNodes += numberOfPositions;
            output += move.toString() + ": "+numberOfPositions+"\n";
        }
        searchTime = System.currentTimeMillis() - searchTime;
        output+="Nodes searched: "+totalNodes;
        return output;
    }


    private int positionCounter(int depth){
        if(depth == 0){
            return 1;
        }
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);
        int numberOfPositions = 0;
        for(Move move:moves){
            push(move);
            numberOfPositions += positionCounter(depth - 1);
            takeBack();
        }
        return numberOfPositions;
    }


    public boolean isFiftyMoveDraw(){
        return currentPosition.halfMoveClock >= 100;
    }

    public boolean isInsufficientMaterial(){
        return Long.bitCount(currentPosition.occupied) == 2;
    }

}
