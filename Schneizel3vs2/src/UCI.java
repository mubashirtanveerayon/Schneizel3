import engine3.Engine3;
import evaluation.classic.ClassicalEvaluation;
import server3.board.Board;
import server3.move.Move;
import server3.utils.Constants;

import java.util.Scanner;
import server3.move.MoveGenerator;
import server3.utils.Utils;

public class UCI {

    //r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1
    Engine3 engine3;
    String fen;
    Scanner sc ;

    boolean showBench = false;


    int numberOfMovesMade = 0;
    public static void main(String[] args) {
        UCI driver = new UCI();
        driver.run();

    }


    public UCI(){
        engine3 = new Engine3();
        fen = Constants.STARTING_FEN;
        sc = new Scanner (System.in);
    }

    public void run(){
        while(true){
            String input = sc.nextLine();
            String[] inputParts = input.split(" ");
            switch(inputParts[0].toLowerCase()){
                case "bench":
                    showBench = !showBench;
                    break;
                case "uci":
                    parseUCICommand();
                    break;
                case "isready":
                    parseIsReadyCommand();
                    break;
                case "position":
                    parsePositionCommand(inputParts);
                    break;
                case "d":
                    parseVisualCommand(inputParts);
                    break;
                case "fen":
                    parseFenCommand(inputParts);
                    break;
                case "set":
                    parseSetCommand(inputParts);
                    break;
                case "push":
                    parsePushCommand(inputParts);
                    break;
                case "perft":
                    parsePerftCommand(inputParts);
                    break;
                case "eval":
                    parseEvalCommand(inputParts);
                    break;
                case "go":
                    parseGoCommand(inputParts);
                    break;
                case "stop":
                    parseStopCommand(inputParts);
                    break;
                case "ucinewgame":
                    parseUCINewGameCommand(inputParts);
                    break;
                case "use":
                    parseUseCommand(inputParts);
                    break;
                case "quit":
                    engine3.saveNetwork();
                    return;

            }
        }
    }

    private void parseUseCommand(String[] inputParts) {
        switch(inputParts[1].toLowerCase()){
            case "book":
                engine3.bookUsable = !engine3.bookUsable;
                break;
            case "nn":
                //engine.usenn = !engine.usenn;
                break;
        }
    }

    private void parseUCICommand() {
        System.out.println("uciok");
    }

    private void parseIsReadyCommand() {
        System.out.println("readyok");
    }

    private void parseUCINewGameCommand(String[] inputParts) {
        engine3 = new Engine3();
        fen = Constants.STARTING_FEN;
        numberOfMovesMade = 0;
    }

    private void parsePositionCommand(String[] inputParts) {
        switch(inputParts[1].toLowerCase()){
            case "fen":

                fen = "";
                for(int i=2;i<inputParts.length;i++){
                    fen += inputParts[i]+" ";
                }
                fen = fen.trim();
                engine3 = new Engine3(fen);
                numberOfMovesMade = 0;
                break;

            case "startpos":
                if (inputParts.length>2 && inputParts[2].equalsIgnoreCase("moves")) {
                    for(int i=3+numberOfMovesMade;i<inputParts.length;i++){
                        engine3.pushUCI(inputParts[i]);
                        numberOfMovesMade ++;
                    }
                    fen = engine3.fen();
                }else{
                    fen = Constants.STARTING_FEN;
                    engine3 = new Engine3(fen);
                    numberOfMovesMade = 0;
                }
                break;
        }
    }

    private void parseStopCommand(String[] inputParts) {
        Move bestMove = engine3.getBestMove();
        if(bestMove!=null) {
            System.out.println("bestmove " + bestMove.toString());
        }
        if(showBench){
            System.out.println("Total time (ms) : "+ engine3.searchTime);
        }
    }

    private void parseGoCommand(String[] inputParts) {
        switch (inputParts[1].toLowerCase()){
            case "perft":
                System.out.println(engine3.perft(Integer.parseInt(inputParts[2])));
                break;
            case "infinite":
                engine3.beginSearch();
                break;
            case "movetime":
                engine3.beginSearch(Integer.parseInt(inputParts[2]),true);
                break;
            case "depth":
                engine3.search(Integer.parseInt(inputParts[2]),true);
                break;
            case "wtime":
                int remainingTime = engine3.whiteToMove() ? Integer.parseInt(inputParts[2]):Integer.parseInt(inputParts[4]);
                int increment = engine3.whiteToMove() ? Integer.parseInt(inputParts[6]):Integer.parseInt(inputParts[8]);
                double thinkTime = remainingTime / 30.0f;
                if(thinkTime > increment * 2){
                    thinkTime += increment * 0.8;
                }

                engine3.beginSearch((int)Math.ceil(thinkTime),true);

                break;
        }


    }

    private void parseEvalCommand(String[] inputParts) {
        Board board = engine3.getCurrentBoard();
        float classicalEval = ClassicalEvaluation.evaluatePosition(board);
//        float networkEval = EvaluationNetwork.evaluatePosition(board);
//        float networkConfidence = board.calculateEndGameWeight()/26.235f*100;
        System.out.println("Classical evaluation : "+classicalEval);
//        System.out.println("Network evaluation   : "+networkEval);
//        System.out.println("Network confidence   : "+networkConfidence+" %");
        System.out.println("Final evaluation     : "+ engine3.eval());
    }

    private void parsePerftCommand(String[] inputParts) {
        if(Character.isDigit(inputParts[1].charAt(0))){
            System.out.println(engine3.perft(Integer.parseInt(inputParts[1])));
            if(showBench){
                System.out.println("Total time (ms) : "+ engine3.searchTime);
            }
        }else{
            for(int i=1;i <= Integer.parseInt(inputParts[2]);i ++){
                String[] engineOutput = engine3.perft(i).split("\n");
                System.out.println("perft "+i+" "+engineOutput[engineOutput.length-1].split(": ")[1]+" ("+ engine3.searchTime+" ms)");
            }
        }
    }

    private void parsePushCommand(String[] inputParts) {
        Board board = engine3.getCurrentBoard();
        Move move = MoveGenerator.fromString(board,inputParts[1]);
        if(move == null){
            return;
        }
        System.out.println(move.info());
        engine3.push(move);
        fen = engine3.fen();
        System.out.println("FEN: "+fen);
    }

    private void parseSetCommand(String[] inputParts) {
        fen = "";
        for(int i=1;i < inputParts.length;i++){
            fen += inputParts[i]+" ";
        }
        engine3 = new Engine3(fen);

    }

    private void parseFenCommand(String[] inputParts) {
        System.out.println("FEN: "+fen);
    }

    private void parseVisualCommand(String[] inputParts) {
        if(inputParts.length == 1){
            System.out.println(Utils.getBoardVisual(engine3.getCurrentBoard().bitboard));
        }else if(inputParts[1].charAt(0) != '-' && !Character.isDigit(inputParts[1].charAt(0))){
            long[][] bitboard = engine3.getCurrentBoard().bitboard;
            char piece = inputParts[1].charAt(0);
            System.out.println(Utils.getBitboardVisual(bitboard[Character.isUpperCase(piece) ? Constants.WHITE:Constants.BLACK][Utils.getPieceIndex(piece)]));
        }else{
            System.out.println(Utils.getBitboardVisual(Long.parseLong(inputParts[1])));
        }

    }


}
