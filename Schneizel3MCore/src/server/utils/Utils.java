package server.utils;


import static server.utils.Constants.*;

import server.exception.UncaughtExceptionHandler;
import server.preload.PrecomputedMasks;

public class Utils {


    public static long lastOne(long bitboard){
        return bitboard & -bitboard;
    }

    public static long firstOne(long bitboard) {
        if(bitboard == 0)return 0;
        return (1L << 63) >>> Long.numberOfLeadingZeros(bitboard);
    }

    public static long higherBits(int square){
        return ~1L << square;
    }

    public static long lowerBits(int square){
        return (1L << square) - 1;
    }

    public static long[][] setupBitboard(String fen){
        long[][] bitboard = new long[2][6];

        String[] fenParts = fen.split(" ");

        int square = 0;

        for (String rank:fenParts[0].split("/")){
            for(char c:rank.toCharArray()){
                if(Character.isDigit(c)){
                    square += Integer.parseInt(Character.toString(c));
                }else{
                    if (Character.isLowerCase(c)){
                        bitboard[BLACK][getPieceIndex(c)] |= (1L << square);
                    }else{
                        bitboard[WHITE][getPieceIndex(c)] |= (1L << square);
                    }
                    square ++;
                }
            }
        }

        return bitboard;

    }

    public static String getArrayVisual(int[] array){
        String visual = "    h   g   f   e   d   c   b   a\n  +---+---+---+---+---+---+---+---+\n";
        for (int rank = 7; rank >= 0; rank --) {
            visual += (8 - rank) + " | ";
            for (int file = 7; file >= 0; file--) {
                visual += array[file+rank*8] + " | ";
            }
            visual += (8 - rank) + "\n  +---+---+---+---+---+---+---+---+\n";
        }

        visual += "    h   g   f   e   d   c   b   a\n";
        return visual;
    }


    public static String getBoardVisual(long[][] bitboard){

        String visual = "    h   g   f   e   d   c   b   a\n  +---+---+---+---+---+---+---+---+\n";

        for (int rank = 7;rank>=0; rank --){
            visual += (8-rank) + " | ";
            for (int file = 7;file >= 0; file--){
                String piece = " ";
                for (int i=0;i<2;i++){
                    boolean pieceDetected = false;

                    for (int index=0;index<6;index++){
                        if((bitboard[i][index] & PrecomputedMasks.SQUARE_MASKS[file+rank * 8]) != 0){
                            piece = getPieceRepresentation(i, index);
                            pieceDetected = true;
                            break;
                        }
                    }
                    if (pieceDetected){
                        break;
                    }
                }
                visual += piece + " | ";
            }
            visual += (8 - rank) + "\n  +---+---+---+---+---+---+---+---+\n";
        }

        visual += "    h   g   f   e   d   c   b   a\n";

        return visual;

    }


    public static String getBitboardVisual(long bitboard){
        String visual = "    h   g   f   e   d   c   b   a\n  +---+---+---+---+---+---+---+---+\n";
        for (int rank = 7; rank >= 0; rank --){
            visual += (8 - rank) + " | ";
            for (int file = 7; file >= 0; file --){
                if((bitboard & PrecomputedMasks.SQUARE_MASKS[(file+rank*8)]) != 0){
                    visual += "1 | ";
                }else{
                    visual += "0 | ";
                }
            }
            visual += (8 - rank) + "\n  +---+---+---+---+---+---+---+---+\n";
        }
        visual += "    h   g   f   e   d   c   b   a\n";

        visual += "Bitboard: " + Long.toBinaryString(bitboard) + "\n";
        visual += "Value: " + bitboard;
        return visual;
    }

    public static int squareIndex(String coord){
        return FILES.indexOf(coord.charAt(0)) + RANKS.indexOf(coord.charAt(1)) * 8;
    }

    public static String squareCoord(int squareIndex){
        return Character.toString(FILES.charAt(squareIndex%8)) + RANKS.charAt(squareIndex/8);
    }


    public static int getPieceIndex(char pieceRepresentation){
        switch(Character.toLowerCase(pieceRepresentation)){
            case 'k':
                return KING_INDEX;
            case 'p':
                return PAWN_INDEX;
            case 'n':
                return KNIGHT_INDEX;
            case 'b':
                return BISHOP_INDEX;
            case 'r':
                return ROOK_INDEX;
            case 'q':
                return QUEEN_INDEX;
            default:
                return INVALID_INDEX;
        }
    }

    public static String getPieceRepresentation(int sideIndex, int index){
        return PIECES[sideIndex][index];
    }

    public static boolean onBishopRay(int sq1, int sq2){
        return Math.abs(sq1%8-sq2%8) == Math.abs(sq1/8-sq2/8);
    }



    static{
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

}
