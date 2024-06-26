import debug.Stockfish;
import engine.Engine;
import server.board.Board;
import server.move.Move;
import server.move.MoveGenerator;
import server.utils.Constants;
import server.utils.Utils;

import java.util.HashMap;
import java.util.Scanner;

public class Debugger {

    //r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1
    Engine engine ;
    Stockfish fish ;
    String fen;
    Scanner sc ;

    public static void main(String[] args) {

        Debugger driver = new Debugger();
        driver.run();

    }


    //set 3rk2r/p1p2ppp/8/Qp6/1K1nP3/3P4/PP1q3P/R6R w k - 6 20
    public Debugger(){
        engine = new Engine();
        fish = new Stockfish();
        fish.startEngine();
        fen = Constants.STARTING_FEN;
        sc = new Scanner (System.in);
    }

    public void run(){
        while(true){
            String input = sc.nextLine();
            String[] inputParts = input.split(" ");
            switch(inputParts[0].toLowerCase()){
                case "d":
                    parseVisualCommand(inputParts);
                    break;
                case "fen":
                    parseFenCommand(inputParts);
                    break;
                case "fish":
                    parseFishCommand(inputParts);
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
                case "quit":
                    break;

            }
        }
    }

    private void parsePerftCommand(String[] inputParts) {
        if(Character.isDigit(inputParts[1].charAt(0))){
            System.out.println(engine.perft(Integer.parseInt(inputParts[1])));
            System.out.printf("Time Taken: "+engine.searchTime+" ms");
        }else{
            for(int i=1;i <= Integer.parseInt(inputParts[2]);i ++){
                String[] engineOutput = engine.perft(i).split("\n");
                System.out.println("perft "+i+" "+engineOutput[engineOutput.length-1].split(": ")[1]+" ("+engine.searchTime+" ms)");
            }
        }
    }

    private void parsePushCommand(String[] inputParts) {
        Board board = engine.getCurrentBoard();
        Move move = MoveGenerator.fromString(board,inputParts[1]);
        if(move == null){
            return;
        }
        System.out.println(move.info());
        engine.push(move);
        fen = engine.fen();
        System.out.println(fen);
    }

    private void parseSetCommand(String[] inputParts) {
        fen = "";
        for(int i=1;i < inputParts.length;i++){
            fen += inputParts[i]+" ";
        }
        engine = new Engine(fen);

    }

    private void parseFishCommand(String[] inputParts) {
        fish.sendCommand("position fen "+fen);
        int depth = Integer.parseInt(inputParts[2]);
        fish.sendCommand("go perft "+depth);
        String[] fishLines = null;
        String fishTotalNodes = null;
        try {
            fishLines = fish.getOutput(depth * 500).split("\n");
            fishTotalNodes = fishLines[fishLines.length-1].split(": ")[1];
        }catch(Exception e){
            e.printStackTrace();
        }
        if(fishLines == null)return;
        String[] engineLines = engine.perft(depth).split("\n");
        String engineTotalNodes = engineLines[engineLines.length-1].split(": ")[1];

        HashMap<String, String> fishDict = new HashMap<>();
        HashMap<String, String> engineDict = new HashMap<>();

        for(int i=0;i<fishLines.length-1;i++){
            String[] parts = fishLines[i].split(": ");
            fishDict.put(parts[0],parts[1]);
        }

        for(int i=0;i<engineLines.length-1;i++){
            String[] parts = engineLines[i].split(": ");
            engineDict.put(parts[0],parts[1]);
        }


        String output = "+------------+-------------------------+-------------------------+\n" +
                        "|    Move    |        Stockfish        |         Engine          |\n" +
                        "+------------+-------------------------+-------------------------+\n";

        int misMatchCount = 0;
        for(String move: fishDict.keySet()){
            output += "|    "+move;
            for(int i=0;i<12-4-move.length();i++){
                output+=" ";
            }
            output += "|";
            for(int i=0;i<25-fishDict.get(move).length();i++){
                output += " ";
            }
            output += fishDict.get(move);
            output += "|";
            String engineMoveNumber = null;
            try{
                engineMoveNumber = engineDict.get(move);
                engineDict.remove(move);
            }catch(Exception e){

            }
            if(engineMoveNumber == null){
                output += "        None             | *";
                misMatchCount ++;
            }else{
                for(int i=0;i<25-engineMoveNumber.length();i++){
                    output += " ";
                }
                output += engineMoveNumber+"|";
                if(!engineMoveNumber.equals(fishDict.get(move))){
                    output += " *";
                    misMatchCount ++;
                }
            }
            output += "\n+------------+-------------------------+-------------------------+\n";

        }

        if(!engineDict.isEmpty()){
            for(String move:engineDict.keySet()){
                output += "|    "+move;
                for(int i=0;i<12-4-move.length();i++){
                    output += " ";
                }
                output += "|          None           |";
                for(int i=0;i<25-engineDict.get(move).length();i++){
                    output += " ";
                }
                output += engineDict.get(move)+"| *";
                output += "\n+------------+-------------------------+-------------------------+\n";
            }
            misMatchCount += engineDict.size();
        }
        output += "|    Nodes   |";
        for(int i=0;i<25-fishTotalNodes.length();i++){
            output += " ";
        }
        output += fishTotalNodes+"|";
        for(int i=0;i<25-engineTotalNodes.length();i++){
            output += " ";
        }
        output += engineTotalNodes+"|";
        output += "\n+------------+-------------------------+-------------------------+\n";
        output += "Mismatch count: "+misMatchCount;
        System.out.println(output);
    }

    private void parseFenCommand(String[] inputParts) {
        System.out.println(fen);
    }

    private void parseVisualCommand(String[] inputParts) {
        if(inputParts.length == 1){
            System.out.println(Utils.getBoardVisual(engine.getCurrentBoard().bitboard));
        }else if(inputParts[1].charAt(0) != '-' && !Character.isDigit(inputParts[1].charAt(0))){
            long[][] bitboard = engine.getCurrentBoard().bitboard;
            char piece = inputParts[1].charAt(0);
            System.out.println(Utils.getBitboardVisual(bitboard[Character.isUpperCase(piece) ? Constants.WHITE:Constants.BLACK][Utils.getPieceIndex(piece)]));
        }else{
            System.out.println(Utils.getBitboardVisual(Long.parseLong(inputParts[1])));
        }

    }


}
