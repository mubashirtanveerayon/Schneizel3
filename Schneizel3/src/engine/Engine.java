package engine;

import evaluation.classic.ClassicalEvaluation;
import evaluation.classic.EvaluationValues;
import server.utils.jnn.Matrix;
import server.board.Board;
import server.exception.IllegalMoveException;
import server.move.Move;
import server.utils.Constants;
import server.utils.Utils;
import java.util.*;

import server.move.MoveGenerator;

import javax.swing.text.Highlighter;

public class Engine extends Thread{

    Board currentPosition;
    public Stack<Board> stack;
    HashMap<Long,Integer> occurredPositions;
    private boolean searching;
    public boolean searchCancelled;

    private Move engineMove;
    public long searchTime = 0;

    int depthReached = 0;

    public boolean isThreeFoldRepetition;



    public boolean bookUsable = false, usenn = false;


//    Random random = new Random();
    public Engine(){
        init(Constants.STARTING_FEN);
    }

    public Engine(String fen){
        init(fen);

    }

    @Override
    public void run(){
        searching = true;


        searchTime = System.currentTimeMillis();
        depthReached = 0;
        searchCancelled = false;

        engineMove = null;
        if(bookUsable) {
            Move bookMove = OpeningBookLoader.readOpeningBook(currentPosition);
            if(bookMove == null){
                bookUsable = false;
//                iterativeDeepening();
//                iterativeDeepening2();
                iterativeDeepening3();
            }else{
                engineMove = bookMove;
            }
        }else{
//            iterativeDeepening();
//            iterativeDeepening2();
            iterativeDeepening3();
        }
        searchTime = System.currentTimeMillis() - searchTime;
        searching = false;
    }





    public float eval(){
//        return EvaluationNetwork.evaluatePosition(currentPosition);
        float endGameWeight = ClassicalEvaluation.endGameWeight(currentPosition);

        return ClassicalEvaluation.evaluatePosition2(currentPosition,endGameWeight);

//        if(!usenn || currentPosition.nPieces[Constants.WHITE] + currentPosition.nPieces[Constants.BLACK] > 10){
//            return ClassicalEvaluation.evaluatePosition2(currentPosition,endGameWeight);
//        }
//        return ClassicalEvaluation.evaluatePosition2(currentPosition,endGameWeight) + EvaluationNetwork.evaluatePosition(currentPosition) * (endGameWeight);
    }


    private void iterativeDeepening2(){
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);
        Utils.orderMovesBasedOnImpact(moves,true);
        int movesCount = moves.size();
        int[] scores = new int[movesCount];
        boolean foundMate = false;
        for(int depth = 1;!foundMate && !searchCancelled;depth++){
            for(int i=0;i<movesCount;i++){
                Move move = moves.get(i);
                push(move);
                int eval = -minimax(depth,-Constants.HIGHEST_VALUE,Constants.HIGHEST_VALUE);
                takeBack();
                if(searchCancelled)break;
                System.out.println("info string move "+move.toString()+" eval " + eval);

                scores[i] = eval;
                if(eval == Constants.HIGHEST_VALUE){
                    foundMate = true;
                    break;
                }
            }
            Utils.orderMovesBasedOnEvaluation(moves,scores,true);
            if(!searchCancelled) {
                depthReached = depth;
                System.out.println("info depth " + depth+" score cp "+ (int)scores[0]);
            }

        }
        engineMove = moves.get(0);

    }



    private void iterativeDeepening3(){
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);
        Utils.orderMovesBasedOnImpact(moves,currentPosition.whiteToMove);
        int movesCount = moves.size();
        int[] scores = new int[movesCount];
        boolean foundMate = false;


        for(int depth = 1;!foundMate && !searchCancelled;depth++){
            for(int i=0;i<movesCount;i++){
                Move move = moves.get(i);
                push(move);
                int eval = minimaxAB(depth,-Constants.HIGHEST_VALUE,Constants.HIGHEST_VALUE);
                takeBack();
                if(searchCancelled)break;
                System.out.println("info string move "+move.toString()+" eval " + eval);

                scores[i] = eval;
                if((currentPosition.whiteToMove && eval == Constants.HIGHEST_VALUE) || (currentPosition.side == Constants.BLACK && eval == -Constants.HIGHEST_VALUE)){
                    foundMate = true;
                    break;
                }
            }

            if(!searchCancelled) {
                Utils.orderMovesBasedOnEvaluation(moves,scores,currentPosition.whiteToMove);
                depthReached = depth;
                System.out.println("info depth " + depth+" score cp "+ (int)scores[0]);
            }

        }
        engineMove = moves.get(0);


    }

    private void iterativeDeepening(){
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);
        float[] scores = new float[moves.size()];
        boolean foundMate = false;
        for(int depth=1;!foundMate && !searchCancelled;depth++){

            for(int i=0;i<moves.size();i++){

                Move move = moves.get(i);
                push(move);
//                if(move.startSquare == Utils.squareIndex("g1")){
//                    System.out.print("");
//                }
                float score = -negamax(depth, -Constants.HIGHEST_VALUE,Constants.HIGHEST_VALUE);
                takeBack();
                if(searchCancelled){
                    break;
                }
                System.out.println("info string move "+move.toString()+" eval " + score);



                scores[i] = score;
//                int moveNumber = i;
                for(int j=i-1;j>=0;j--){
                    if(scores[j] < score){
                        scores[j+1] = scores[j];
                        scores[j] = score;
                        //moves.remove(move);
                        moves.remove(j+1);
                        moves.add(j,move);
//                        moveNumber = j;
                    }else{
                        break;
                    }
                }

//                System.out.println("info depth "+depth+" currmove "+move+" currmovenumber "+moveNumber);

                if(score == Constants.HIGHEST_VALUE){
//                    System.out.println("Found mate in "+depth);
                    foundMate = true;
                    break;
                }

            }

            if(!searchCancelled) {
                depthReached = depth;
                System.out.println("info depth " + depth+" score cp "+ (int)scores[0]);
            }
        }

        engineMove = moves.get(0);


    }

    private void orderMoves(ArrayList<Move> moves){
        float[] scores = new float[moves.size()];
        for(int i=0;i<moves.size();i++){
            Move move = moves.get(i);
            float score = EvaluationValues.PST[currentPosition.side][move.pieceIndex][move.targetSquare];
            if(move.isCapture ){
                score += -EvaluationValues.PIECE_VALUES[move.pieceIndex] + EvaluationValues.PIECE_VALUES[move.capturePieceIndex];
            }
            if(move.isPawnMove) {
                if (move.isPromotion) {
                    score += EvaluationValues.PROMOTION_SCORE * EvaluationValues.PIECE_VALUES[move.promotionPieceIndex];
                }
                if (move.isEP) {
                    score += EvaluationValues.EN_PASSANT_CAPTURE_SCORE;
                }
            }else if(move.isCastling){
                score += EvaluationValues.CASTLING_SCORE;
            }
            scores[i] = score;
            for(int j=i-1;j>=0;j--){
                if(scores[j] < score){
                    scores[j+1] = scores[j];
                    scores[j] = score;
                    //moves.remove(move);
                    moves.remove(j+1);
                    moves.add(j,move);
                }else{
                    break;
                }
            }
        }
    }




    private int searchAllCaptures(int alpha,int beta){
        if(searchCancelled)return alpha;



        int eval = currentPosition.currentEvaluation;

        if(eval>=beta)return beta;

        alpha = Math.max(alpha,eval);

        ArrayList<Move> captureMoves = MoveGenerator.generateCaptureMoves(currentPosition);
        Utils.orderMovesBasedOnImpact(captureMoves,true);

        for(Move move:captureMoves){
            push(move);
            eval = -searchAllCaptures(-beta,-alpha);
            takeBack();
            if(eval>=beta)return beta;
            alpha = Math.max(alpha,eval);
        }
        return alpha;
    }

    private int minimax(int depth,int alpha,int beta){
        if(searchCancelled)return alpha;
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);
        if(moves.isEmpty()) {
            if (currentPosition.isCheck) {
                return -Constants.HIGHEST_VALUE;
            } else {
                return 0;
            }
        }else if(isFiftyMoveDraw() || isThreeFoldRepetition || isInsufficientMaterial()){
            return 0;
        }else if(depth == 0){
            return searchAllCaptures(alpha,beta);
        }


        for(Move move:moves){
            push(move);
            int score = -minimax(depth-1,-beta,-alpha);
            takeBack();
            if(score >= beta){
                return beta;
            }
            alpha = Math.max(score,alpha);
        }
        return alpha;
    }

    private float negamax(int depth,float alpha, float beta){
        if(searchCancelled){
            return 0;
        }
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);

        if(moves.isEmpty()){
            if(currentPosition.isCheck){
                return -Constants.HIGHEST_VALUE;
            }else{
                return 0;
            }
        }else if(isFiftyMoveDraw() || isThreeFoldRepetition || isInsufficientMaterial()){
            return 0;
        }else if(depth == 0){
            return quiescenceSearch(alpha,beta);
//            return eval();
        }

//        orderMoves(moves);

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
            return 0;
        }

        float eval = eval();



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



    private int minimaxCaptures(int alpha,int beta){
        if(searchCancelled)return currentPosition.whiteToMove?alpha:beta;
        ArrayList<Move> moves = MoveGenerator.generateCaptureMoves(currentPosition);
        if (moves.isEmpty()){
            if (currentPosition.isCheck)return currentPosition.whiteToMove?-Constants.HIGHEST_VALUE:Constants.HIGHEST_VALUE;
            else return currentPosition.currentEvaluation;
        }else if(isFiftyMoveDraw() || isThreeFoldRepetition || isInsufficientMaterial()) return 0;
        if(currentPosition.whiteToMove){
            Utils.orderMovesBasedOnImpact(moves,true);
            int maxEval = -Constants.HIGHEST_VALUE;
            for (Move move : moves){
                push(move);
                int eval = minimaxCaptures(alpha,beta);
                takeBack();
                maxEval = Math.max(eval,maxEval);
                alpha = Math.max(alpha,eval);
                if (beta<=alpha)break;
            }
            return maxEval;
        }else{
            Utils.orderMovesBasedOnImpact(moves,false);
            int minEval = Constants.HIGHEST_VALUE;
            for (Move move : moves){
                push(move);
                int eval = minimaxCaptures(alpha,beta);
                takeBack();
                minEval = Math.min(eval,minEval);
                beta = Math.min(beta,eval);
                if (beta<=alpha)break;
            }
            return minEval;
        }
    }
    private int minimaxAB(int depth,int alpha,int beta){
        if(searchCancelled)return currentPosition.whiteToMove?alpha:beta;
        ArrayList<Move> moves = MoveGenerator.generateMoves(currentPosition);
        if(moves.isEmpty()) {
            if (currentPosition.isCheck) {
                return currentPosition.side == Constants.BLACK ? Constants.HIGHEST_VALUE: -Constants.HIGHEST_VALUE;
            } else {
                return 0;
            }
        }else if(isFiftyMoveDraw() || isThreeFoldRepetition || isInsufficientMaterial()){
            return 0;
        }else if(depth == 0){
            return minimaxCaptures(alpha,beta);
        }

        if(currentPosition.whiteToMove){
            Utils.orderMovesBasedOnImpact(moves,true);
            int maxEval = -Constants.HIGHEST_VALUE;
            for (Move move : moves){
                push(move);
                int eval = minimaxAB(depth-1,alpha,beta);
                takeBack();
                maxEval = Math.max(eval,maxEval);
                alpha = Math.max(alpha,eval);
                if (beta<=alpha)break;
            }
            return maxEval;
        }else{
            Utils.orderMovesBasedOnImpact(moves,false);
            int minEval = Constants.HIGHEST_VALUE;
            for (Move move : moves){
                push(move);
                int eval = minimaxAB(depth-1,alpha,beta);
                takeBack();
                minEval = Math.min(eval,minEval);
                beta = Math.min(beta,eval);
                if (beta<=alpha)break;
            }
            return minEval;
        }


    }

    public Board getCurrentBoard(){
        return new Board(currentPosition);
    }

    private void init(String fen){
        currentPosition = new Board(fen);
        stack = new Stack<>();
        occurredPositions = new HashMap<>();
        occurredPositions.put(currentPosition.hash, 1);
//        EvaluationNetwork.initialize();
//        bookUsable = OpeningBookLoader.loadOpeningBook();
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

    public Matrix gameResult(){
        if(isFiftyMoveDraw() || isThreeFoldRepetition || isInsufficientMaterial() ){
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

        System.out.println("info string Total time: "+searchTime+" ms");
    }

    public ArrayList<Move> getLegalMoves(){
        return MoveGenerator.generateMoves(currentPosition);
    }

    public void search(int depth, boolean printbestmoveWhenFinished){
        new Thread(this).start();
        searching = true;
        depthReached = 0;
        while(depthReached<depth && isSearching()){
            System.out.print("");
        }

        stopSearch();

        if(printbestmoveWhenFinished && engineMove != null){
            System.out.println("bestmove "+engineMove.toString());
        }
        //System.out.println("search()");
    }
//position startpos moves b1c3 d7d5 e2e4 d5d4 c3e2 b8c6 g1f3 e7e5 d2d3 g8f6 c2c3 c8g4 c3d4 g4f3 g2f3 f8b4 c1d2 b4d2 e1d2 c6d4 f3f4 d4e2 f4e5 e2d4 e5f6 d8f6 d1a4 b7b5 a4b4 f6f2 f1e2 f2e2 d2c3 a8d8 b4a5 e2c2 c3b4 c2d2

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
