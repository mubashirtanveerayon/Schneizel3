package server.utils;

import server.utils.jnn.Matrix;

public class Constants {

    public static final String FILES = "abcdefgh";

    public static final String RANKS = "87654321";

    public static final int INVALID_INDEX = -1, KING_INDEX = 0, PAWN_INDEX = 1, KNIGHT_INDEX = 2, BISHOP_INDEX = 3, ROOK_INDEX = 4, QUEEN_INDEX = 5;

    public static final int WHITE = 0, BLACK = 1;

    public static final int KING_SIDE_CASTLE_INDEX = 0, QUEEN_SIDE_CASTLE_INDEX = 1;

    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";


    public static final int WHITE_KING_INITIAL_POSITION = 60;


    public static final int BLACK_KING_INITIAL_POSITION = 4;


    public static final int DRAW_INDEX = 2, CONTINUE_INDEX = 3;

    public static final float HIGHEST_VALUE = 0xFFFFFFF;


    public static final Matrix[] GAME_RESULTS;



    static{
        Matrix whiteWon = new Matrix(2,1);
        whiteWon.array[WHITE][0] = 1;
        Matrix blackWon = new Matrix(2,1);
        blackWon.array[BLACK][0] = 1;
        Matrix draw = new Matrix(2,1);
        GAME_RESULTS = new Matrix[]{whiteWon,blackWon,draw,null};
    }


    public static final byte[][] CASTLE_BIT_MASK = {
            {0b1000, 0b100},
            {0b10, 0b1}
    };


    public static final byte[] CASTLING_SIDE = {0b1100,0b11};

    public static String KING_SIDE_CASTLING = "O-O";

    public static String QUEEN_SIDE_CASTLING = "O-O-O";

    public static final String[][] PIECES = {{"K","P","N","B","R","Q"},{"k","p","n","b","r","q"}};

    public static long BOARD_MASK = 0xFFFFFFFF;



}
