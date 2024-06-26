package server.utils;

import java.io.BufferedReader;
import java.io.FileReader;

import server.board.Board;
import server.move.Move;
import server.move.MoveGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static server.utils.Constants.*;

public class PGNUtils {

    public static final int ALL_GAMES = -1;

    public static String generateSANMoveText(ArrayList<Move> movesMade){
        Board board = new Board();
        String pgn = "";
        int plyCount = 1;
        for(Move move:movesMade){
            if(board.whiteToMove){
                pgn += plyCount+". ";
                plyCount+=1;
            }
            pgn += PGNUtils.cvt(move,board)+" ";
            board.makeMove(move);
        }
        return pgn.trim();
    }

    public static String cvt(Move move, Board board){//move to san
        if(move.isCastling){
            if(move.isKingSideCastling){
                return KING_SIDE_CASTLING;
            }else{
                return QUEEN_SIDE_CASTLING;
            }
        }

        int locFile = move.startSquare % 8;
        int locRank = move.startSquare / 8;

        ArrayList<Move> moves = MoveGenerator.generateMoves(board);

        ArrayList<Move> similarMoves = new ArrayList<>();
        int side = board.whiteToMove ? WHITE:BLACK;
        String pieceToMove = Utils.getPieceRepresentation(side,move.pieceIndex);
        for(Move m:moves){
            if(!move.equals(m) && (move.startSquare !=m.startSquare) && (move.targetSquare == m.targetSquare)){
                if(move.pieceIndex == m.pieceIndex){
                    similarMoves.add(m);
                }
            }
        }

        String san;
        if(move.pieceIndex == PAWN_INDEX){
            san = "";
            if(!similarMoves.isEmpty()|| move.isEP || move.isCapture ){
                san = Character.toString(FILES.charAt(locFile));
            }
        }else{
            san = pieceToMove.toUpperCase();
            boolean fileSame = false,rankSame = false;
            for(Move similar:similarMoves){
                fileSame = fileSame || locFile == similar.startSquare%8;
                rankSame = rankSame || locRank == similar.startSquare/8;
            }

            if(rankSame && fileSame){
                san += Character.toString(FILES.charAt(locFile)) + Character.toString(RANKS.charAt(locRank));;
            }else if(rankSame){
                san += Character.toString(FILES.charAt(locFile));
            }else if(fileSame){
                san += Character.toString(RANKS.charAt(locRank));
            }else if(!similarMoves.isEmpty()){
                san += Character.toString(FILES.charAt(locFile));
            }

        }
        if(move.isCapture ||move.isEP){
            san += "x";
        }

        int destFile = move.targetSquare % 8;
        int destRank = move.targetSquare / 8;

        san += Character.toString(FILES.charAt(destFile)) + Character.toString(RANKS.charAt(destRank));


        if(move.isPromotion){
            san += "="+ Utils.getPieceRepresentation(WHITE,move.promotionPieceIndex);
        }

        Board tBoard = new Board(board);

        tBoard.makeMove(move);

        if(tBoard.isCheck){
            if(MoveGenerator.generateMoves(tBoard).isEmpty()){
                san += "#";
            }else{
                san += "+";
            }
        }

        return san;
    }

    public static ArrayList<HashMap<String,String>> parsePGNFile(String path,int numberOfGames,boolean movesOnly){
        return parsePGNText(getContent(path),numberOfGames,movesOnly);
    }

    public static ArrayList<HashMap<String,String>> parsePGNText(ArrayList<String> lines,int numberOfGames,boolean movesOnly){
        ArrayList<HashMap<String,String>> games = new ArrayList<>();
        boolean parsingGame = false;
        HashMap<String,String> gameInfo = new HashMap<>();
        Pattern coordPattern = Pattern.compile("[a-h][1-8]");
        String moveText = "";
        int n = 0;
        for(String line:lines){
            String[] parts = line.split(" ");
            if(line.startsWith("[Event ")){
                parsingGame = true;
                gameInfo=new HashMap<>();
                moveText = "";
            }else if(!line.contains("\"") && (parts[parts.length-1].contains("1-") || parts[parts.length-1].contains("-1") || parts[parts.length-1].contains("*"))) {
                parsingGame = false;
                moveText += line;
                gameInfo.put("Moves",moveText.trim());
                games.add(gameInfo);
                n += 1;
                if(n == numberOfGames){
                    break;
                }
            }
            if (parsingGame){
                if(line.startsWith("[") && (!movesOnly || line.startsWith("[Result"))){
                    String infoLine = line.replace("[", "").replace("]","");
                    String[] infoSegments = infoLine.split("\"");
                    if(infoSegments.length<2){
                        continue;
                    }
                    String key = infoSegments[0].trim();
                    String value = infoSegments[1].trim();
                    gameInfo.put(key,value);
                }else if(Character.isDigit(line.charAt(0)) || parts[0].contains(KING_SIDE_CASTLING) || coordPattern.matcher(parts[0]).find()){
                    moveText += line+" ";
                }
            }
        }
        return games;
    }


    public static ArrayList<String> getContent(String filePath){
        ArrayList<String> content = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            for(String line;(line = reader.readLine()) != null;){
                if(!line.isEmpty()){
                    content.add(line.trim());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        //System.out.println(content);
        return content;
    }
    public static Move parse(String san, Board board,ArrayList<Move> allMoves){//san to move

        if (san.equalsIgnoreCase(QUEEN_SIDE_CASTLING)){

            if(board.whiteToMove){
                return new Move(WHITE_KING_INITIAL_POSITION,WHITE_KING_INITIAL_POSITION-2,KING_INDEX,INVALID_INDEX,INVALID_INDEX);
            }else{
                return new Move(BLACK_KING_INITIAL_POSITION,BLACK_KING_INITIAL_POSITION-2,KING_INDEX,INVALID_INDEX,INVALID_INDEX);
            }
        }else if (san.equalsIgnoreCase(KING_SIDE_CASTLING)){
            if(board.whiteToMove){
                return new Move(WHITE_KING_INITIAL_POSITION,WHITE_KING_INITIAL_POSITION+2,KING_INDEX,INVALID_INDEX,INVALID_INDEX);
            }else{
                return new Move(BLACK_KING_INITIAL_POSITION,BLACK_KING_INITIAL_POSITION+2,KING_INDEX,INVALID_INDEX,INVALID_INDEX);
            }
        }else{
            Pattern coordPattern = Pattern.compile("[a-h][1-8]");

            Matcher matcher = coordPattern.matcher(san);
            String from = "",to="";
            boolean invalid = true;
            while(matcher.find()){
                if(to.isEmpty()){
                    to = matcher.group();
                }else{
                    from = to;
                    to = matcher.group();
                }
                invalid = false;
            }
            if(invalid){
                return null;
            }

            for(Move move:allMoves){
                if(move.isCastling){
                    continue;
                }

                int index = move.targetSquare;
                String dstCoord = Utils.squareCoord(index);
                if(dstCoord.equals(to)){

                    index = move.startSquare;

                    if(Character.isUpperCase(san.charAt(0))){
                        if(move.pieceIndex == Utils.getPieceIndex(san.charAt(0))) {
                            if (from.isEmpty()) {
                                String firstPart = san.split(to)[0];
                                if (firstPart.length()>1 && firstPart.charAt(1) != 'x'){
                                    if(Utils.squareCoord(index).contains(Character.toString(firstPart.charAt(1)))){
                                        return move;
                                    }
                                }else{
                                    return move;
                                }
                            } else {
                                if (index == FILES.indexOf(from.charAt(0)) + RANKS.indexOf(from.charAt(1)) * 8) {
                                    return move;
                                }
                            }
                        }
                    }else{
                        if(move.pieceIndex == Constants.PAWN_INDEX){
                            if(san.contains("=")){
                                if(!move.isEP){
                                    if(move.promotionPieceIndex == Utils.getPieceIndex(san.charAt(san.indexOf("=")+1))){
                                        return move;
                                    }
                                }
                            }else if(move.startSquare%8 == FILES.indexOf(san.charAt(0)) ){
                                return move;
                            }
                        }
                    }

                }

            }
        }
        return null;
    }

    public static ArrayList<Move> getMoves(String moveText){
        Board board = new Board();
        ArrayList<Move> movesMade = new ArrayList<>();
        ArrayList<Move> allMoves = MoveGenerator.generateMoves(board);
        boolean shouldGenerateMoves = false;
        for(String line:moveText.split("\n")){
            if(line.isEmpty()){
                continue;
            }
            for(String segment:line.split(" ")){
                if(segment.isEmpty() || Character.isDigit(segment.charAt(0)) || segment.charAt(0) == '{' || segment.contains("clk") || segment.contains("eval") || segment.endsWith("}")){
                    continue;
                }
                if(shouldGenerateMoves) {
                    allMoves = MoveGenerator.generateMoves(board);
                }
                Move move = parse(segment, board, allMoves);
                if (move != null) {
                    movesMade.add(move);
                    board.makeMove(move);
                    shouldGenerateMoves = true;
                }else{
                    shouldGenerateMoves = false;
                }

            }
        }
        return movesMade;
    }

}
