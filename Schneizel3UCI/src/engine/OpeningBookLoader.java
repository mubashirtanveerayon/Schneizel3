package engine;

import server.board.Board;
import server.move.Move;
import server.move.MoveGenerator;
import server.utils.Constants;
import server.utils.PGNUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class OpeningBookLoader {


    public static final String OPENINGS_BOOK = "openings";

    public static ArrayList<HashMap<String,String>> openingBook;

    private static Random random = new Random();

    public static Move readOpeningBook(Board currentPosition){
        if(openingBook == null){
            return null;
        }

        ArrayList<Move> availableBestMoves = new ArrayList<>();
        ArrayList<Move> availableGoodMoves = new ArrayList<>();

        for(HashMap<String, String> game:OpeningBookLoader.openingBook){
            String gameResult = game.get("Result");
            if(gameResult == null){
                continue;
            }
            boolean isBest = (currentPosition.side == Constants.WHITE && gameResult.equals("1-0")) || (currentPosition.side == Constants.BLACK && gameResult.equals("0-1"));
            boolean isGood = gameResult.equals("1/2-1/2");
            boolean shouldSearch = isBest || isGood;
            if(!shouldSearch){
                continue;
            }

            Board board = new Board();
            String moveText = game.get("Moves");
            ArrayList<Move> allMoves = MoveGenerator.generateMoves(board);
            boolean shouldGenerateMoves = false;
            for(String line:moveText.split("\n")){
                if(line.isEmpty()){
                    continue;
                }
                boolean done = false;
                for(String segment:line.split(" ")) {
                    if (segment.isEmpty() || Character.isDigit(segment.charAt(0)) || segment.charAt(0) == '{' || segment.contains("clk") || segment.contains("eval") || segment.endsWith("}")) {
                        continue;
                    }
                    if (shouldGenerateMoves) {
                        allMoves = MoveGenerator.generateMoves(board);
                    }
                    Move move = PGNUtils.parse(segment, board, allMoves);
                    if(move == null){
                        shouldGenerateMoves = false;
                    }else{

                        if(board.hash == currentPosition.hash){
                            if(isBest){
                                availableBestMoves.add(move);
                            }else{
                                availableGoodMoves.add(move);
                            }
                            done = true;
                            break;
                        }else{
                            board.makeMove(move);
                            shouldGenerateMoves = true;
                        }


                    }

                }
                if(done){
                    break;
                }

            }




        }

        System.out.println("Available best moves: "+availableBestMoves.size());
        System.out.println("Available good moves: "+availableGoodMoves.size());

        if(!availableBestMoves.isEmpty()){
            return availableBestMoves.get(random.nextInt(availableBestMoves.size()));
        }else if(!availableGoodMoves.isEmpty()){
            return availableGoodMoves.get(random.nextInt(availableGoodMoves.size()));
        }

        return null;


    }

    public static boolean loadOpeningBook() {
        if(openingBook != null){
            return true;
        }
        File directory = new File(OPENINGS_BOOK);

        if(!directory.isDirectory()){
            System.out.println("\""+OPENINGS_BOOK+"\", no such directory!");
            return false;
        }

        ArrayList<String> pgnFiles = new ArrayList<>();


        for(File file : directory.listFiles()){
            if(file.getName().endsWith(".pgn")){
                pgnFiles.add(file.getPath());
            }
        }

        String randomPgnFile = pgnFiles.get(random.nextInt(pgnFiles.size()));
        System.out.println("Loading opening book: "+randomPgnFile);

        openingBook = PGNUtils.parsePGNFile(randomPgnFile,PGNUtils.ALL_GAMES,true);

        System.out.println("Opening book loaded!");
        return true;


    }




}
