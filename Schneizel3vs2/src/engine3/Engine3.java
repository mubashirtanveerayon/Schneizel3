package engine3;

import evaluation.classic.ClassicalEvaluation;
import evaluation.network.EvaluationNetwork;
import server3.utils.jnn.Matrix;
import server3.board.Board;
import server3.exception.IllegalMoveException;
import server3.move.Move;
import server3.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import server3.move.MoveGenerator;

public class Engine3 extends Thread{

    Board currentPosition;
    public Stack<Board> stack;
    HashMap<Long,Integer> occurredPositions;
    private boolean searching;
    public boolean searchCancelled;

    private Move engineMove;
    public long searchTime = 0;

    int depthReached = 0;

    HashMap<Long,Float> transpositionTable;


    public boolean bookUsable = true;


    Random random = new Random();
    public Engine3(){
        init(Constants.STARTING_FEN);
    }

    public Engine3(String fen){
        init(fen);

    }

    @Override
    public void run(){
        searching = true;


        searchTime = System.currentTimeMillis();
        depthReached = 0;
        searchCancelled = false;

        engineMove = null;
        transpositionTable.clear();
        if(bookUsable) {
            Move bookMove = OpeningBookLoader.readOpeningBook(currentPosition);
            if(bookMove == null){
                bookUsable = false;
                iterativeDeepening();
            }else{
                engineMove = bookMove;
            }
        }else{
            iterativeDeepening();
        }
        searchTime = System.currentTimeMillis() - searchTime;
        searching = false;
    }





    public float eval(){
        return ClassicalEvaluation.evaluatePosition(currentPosition);
    }

    private void iterativeDeepening(){
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);
        float[] scores = new float[moves.size()];
        boolean foundMate = false;
        for(int depth=1;!foundMate && !searchCancelled;depth++){

            System.out.println("info depth "+depth);

            for(int i=0;i<moves.size();i++){

                Move move = moves.get(i);
                push(move);
                float score = -negamax(depth, -Constants.HIGHEST_VALUE,Constants.HIGHEST_VALUE);
                takeBack();
                if(searchCancelled){
                    break;
                }
                System.out.println("info currmove "+move.toString()+" eval " + score);



                scores[i] = score;
//                int moveNumber = i;
                for(int j=i-1;j>=0;j--){
                    if(scores[j] < score){
                        scores[i] = scores[j];
                        scores[j] = score;
                        moves.remove(move);
                        moves.add(j,move);
//                        moveNumber = j;
                    }
                }

//                System.out.println("info depth "+depth+" currmove "+move+" currmovenumber "+moveNumber);

                if(score == Constants.HIGHEST_VALUE){
//                    System.out.println("Found mate in "+depth);
                    foundMate = true;
                    break;
                }

            }
            depthReached = depth;
        }

        engineMove = moves.get(0);


    }

    private void orderMoves(ArrayList<Move> moves){
        float[] scores = new float[moves.size()];
        for(int i=0;i<moves.size();i++){
            Move move = moves.get(i);
            float score = 0;
            if(move.isCapture ){
                score += -Constants.PIECE_VALUES[move.pieceIndex] + Constants.PIECE_VALUES[move.capturePieceIndex];
            }
            if(move.isPawnMove) {
                if (move.isPromotion) {
                    score += Constants.PROMOTION_SCORE * Constants.PIECE_VALUES[move.promotionPieceIndex];
                }
                if (move.isEP) {
                    score += Constants.EN_PASSANT_CAPTURE_SCORE;
                }
            }else if(move.isCastling){
                score += Constants.CASTLING_SCORE;
            }
            scores[i] = score;
            for(int j=i-1;j>=0;j--){
                if(scores[j] < score){
                    scores[i] = scores[j];
                    scores[j] = score;
                    moves.remove(move);
                    moves.add(j,move);
                }
            }
        }
    }


    private float negamax(int depth,float alpha, float beta){
        if(searchCancelled){
            return alpha;
        }
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);

        if(moves.isEmpty()){
            if(currentPosition.isCheck){
                return -Constants.HIGHEST_VALUE;
            }else{
                return 0;
            }
        }else if(isFiftyMoveDraw() || isThreeFoldRepetition() || isInsufficientMaterial()){
            return 0;
        }else if(depth == 0){
            return quiescenceSearch(alpha,beta);
        }

        orderMoves(moves);

        for(Move move:moves){
            push(move);
            float score = -negamax(depth-1,-beta,-alpha);
            takeBack();
            if(score >= beta){
                return beta;
            }
            alpha = Math.max(score,alpha);
        }
        return alpha;
    }

    private float quiescenceSearch(float alpha, float beta){
        if(searchCancelled){
            return alpha;
        }

//        float eval = eval();
        float eval;

        if(transpositionTable.containsKey(currentPosition.hash)){
            eval = transpositionTable.get(currentPosition.hash);
        }else{
            eval = eval();
            transpositionTable.put(currentPosition.hash,eval);
        }


//        float eval = currentPosition.eval();
        if(eval >= beta){
            return beta;
        }

        alpha = Math.max(alpha, eval);

        ArrayList<Move> captureMoves = MoveGenerator.generateCaptureMoves(currentPosition);
        orderMoves(captureMoves);


        for(Move move: captureMoves){
            push(move);
            eval = -quiescenceSearch(-beta,-alpha);
            takeBack();
            if(eval >= beta){
                return beta;
            }
            alpha = Math.max(alpha,eval);
        }
        return alpha;


    }

    public Board getCurrentBoard(){
        return new Board(currentPosition);
    }

    private void init(String fen){
        currentPosition = new Board(fen);
        stack = new Stack<>();
        occurredPositions = new HashMap<>();
        transpositionTable = new HashMap<>();
        occurredPositions.put(currentPosition.hash, 1);
//        EvaluationNetwork.initialize();
        bookUsable = OpeningBookLoader.loadOpeningBook();
    }




    public void push(Move move){
        stack.push(new Board(currentPosition));
        currentPosition.makeMove(move);
        if(occurredPositions.containsKey(currentPosition.hash)){
            occurredPositions.put(currentPosition.hash,occurredPositions.get(currentPosition.hash)+1);
        }else{
            occurredPositions.put(currentPosition.hash, 1);
        }
    }

    public void takeBack(){
        long recentPositionHash = currentPosition.hash;
        currentPosition = stack.pop();
        occurredPositions.put(recentPositionHash,occurredPositions.get(recentPositionHash) - 1);
        if(occurredPositions.get(recentPositionHash) <= 0){
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

    public boolean isThreeFoldRepetition(){
        for(int repeatCount:occurredPositions.values()){
            if(repeatCount>=3){
                return true;
            }
        }
        return false;
    }

    public boolean isFiftyMoveDraw(){
        return currentPosition.halfMoveClock >= 100;
    }

    public boolean isInsufficientMaterial(){
        return Long.bitCount(currentPosition.occupied) == 2;
    }

    public Matrix gameResult(){
        if(isFiftyMoveDraw() || isThreeFoldRepetition() || isInsufficientMaterial() ){
            return Constants.GAME_RESULTS[Constants.DRAW_INDEX];
        }
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);
        if(moves.isEmpty()){
            if(currentPosition.isCheck){
                if(currentPosition.whiteToMove){
                    return Constants.GAME_RESULTS[Constants.BLACK];
                }else{
                    return Constants.GAME_RESULTS[Constants.WHITE];
                }
            }else{
                return Constants.GAME_RESULTS[Constants.DRAW_INDEX];
            }
        }
        return Constants.GAME_RESULTS[Constants.CONTINUE_INDEX];
    }

    public String fen(){
        return currentPosition.fen();
    }

    public boolean isSearching(){
        return searching;
    }

    public Move getBestMove(){
        if(isSearching()) {
            stopSearch();
        }
        return engineMove;
    }

    public void stopSearch(){
        searchCancelled = true;

        while(isSearching()){
            System.out.print("");
        }

//        System.out.println("stopSearch()");
    }

    public ArrayList<Move> getLegalMoves(){
        return MoveGenerator.generateMoves(currentPosition);
    }

    public void search(int depth, boolean printbestmoveWhenFinished){
        new Thread(this).start();
        searching = true;
        while(depthReached<depth && isSearching()){
            System.out.print("");
        }

        stopSearch();

        if(printbestmoveWhenFinished && engineMove != null){
            System.out.println("bestmove "+engineMove.toString());
        }
        //System.out.println("search()");
    }


    public void beginSearch() {
        Thread searchThread = new Thread(this);
        searchThread.start();
    }

    public void beginSearch(int moveTime, boolean printbestmoveWheneFinished) {
        Thread searchThread = new Thread(this);
        searchThread.start();
        Thread stopper = new Thread(){
            @Override
            public void run(){
                try{
                    Thread.sleep(moveTime);
                }catch(Exception e){
                    e.printStackTrace();
                }
                if(searchThread.isAlive()){
                    stopSearch();
                }
                if(printbestmoveWheneFinished && engineMove != null){
                    System.out.println("bestmove "+engineMove.toString());
                }
            }
        };
        stopper.start();
    }

    public void saveNetwork() {
        EvaluationNetwork.save();
    }


    public void trainNetwork(int numberOfFinalPositions){
        Matrix gameResult = gameResult();
        if(gameResult == null){
            return;
        }

        int numberOfPositions = stack.size();

        while(!stack.isEmpty()){
            if(stack.size()>=numberOfPositions-numberOfFinalPositions) {
                EvaluationNetwork.generateInputMatrix(currentPosition);
                EvaluationNetwork.trainNetwork(EvaluationNetwork.inputMatrix, gameResult);
            }
            takeBack();
        }

        System.out.println("Network is trained!");

    }

    public void pushUCI(String moveStr) {
        Move move = MoveGenerator.fromString(currentPosition,moveStr);
        if(move == null){
            throw new IllegalMoveException(fen(),moveStr);
        }
        push(move);
    }

    public boolean whiteToMove(){
        return currentPosition.whiteToMove;
    }
}
