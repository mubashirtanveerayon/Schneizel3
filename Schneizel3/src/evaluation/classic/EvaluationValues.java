package evaluation.classic;

public class EvaluationValues {



    public static final float PAWN_VALUE = 100f;//1.7f;

    public static final float KNIGHT_VALUE = 330f;//3.2f;

    public static final float BISHOP_VALUE = 350f;//3.5f;

    public static final float ROOK_VALUE = 550f;

    public static final float QUEEN_VALUE = 960f;//9f;

    public static final float KING_VALUE = 0f;

    public static final float[] PIECE_VALUES = {KING_VALUE, PAWN_VALUE, KNIGHT_VALUE, BISHOP_VALUE, ROOK_VALUE, QUEEN_VALUE};


    public static final float PROMOTION_SCORE = 3f;
    public static final float EN_PASSANT_CAPTURE_SCORE = 2;
    public static final float CASTLING_SCORE = 2f;

    public static final float DOUBLED_PAWN_SCORE = 1f;

    public static final float PAWN_CHAIN_SCORE = 3f;

    public static final float BLOCKER_PAWNS_SCORE = 2f;
    public static final float PAWN_SHIELD_SCORE = 0.25f;


    public static final float[][] PAWN_RANK_BONUS = new float[][]{
            {0,0.9f,0.5f,0.3f,.1f,.05f,0,0},
            {0,0,.05f,.10f,.30f,.50f,.90f,0}

    };

    public static final float[] WHITE_PAWN = new float[]

            {
                    0,  0,  0,  0,  0,  0,  0,  0,
                    5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f,
                    1.0f, 1.0f, 2.0f, 3.0f, 3.0f, 2.0f, 1.0f, 1.0f,
                    .5f,  .5f, 1.0f, 2.5f, 2.5f, 1.0f,  .5f,  .5f,
                    0,  0,  0, 2.3f, 2.3f,  0,  0,  0,
                    .5f, -.5f,-1.0f, 0, 0, -1.0f, -.5f,  .5f,
                    .5f, 1.0f, 1.0f,-2.4f,-2.4f, 1.0f, 1.0f,  .5f,
                    0,  0,  0,  0,  0,  0,  0,  0
            };

    public static final float[] WHITE_KNIGHT = new float[]
            {
                    -5.0f,-4.0f,-3.0f,-3.0f,-3.0f,-3.0f,-4.0f,-5.0f,
                    -4.0f,-2.0f,  0,  0,  0,  0,-2.0f,-4.0f,
                    -3.0f,  0, .6f, 1.5f, 1.5f, .6f,  0,-3.0f,
                    -3.0f,  .5f, 1.5f, 2.0f, 2.0f, 1.5f,  .5f,-3.0f,
                    -3.0f,  0, 1.5f, 2.0f, 2.0f, 1.5f,  0,-3.0f,
                    -3.0f,  .5f, .6f, 1.5f, 1.5f, .6f,  .5f,-3.0f,
                    -4.0f,-2.0f,  0,  .5f,  .5f,  0,-2.0f,-4.0f,
                    -5.0f,-3.5f,-3.0f,-3.0f,-3.0f,-3.0f,-3.5f,-5.0f
            };

    public static final float[] WHITE_BISHOP = new float[]

            {
                    -2.0f,-1.0f,-1.0f,-1.0f,-1.0f,-1.0f,-1.0f,-2.0f,
                    -1.0f,  0,  0,  0,  0,  0,  0,-1.0f,
                    -1.0f,  0,  .5f, 1.0f, 1.0f,  .5f,  0,-1.0f,
                    -1.0f,  .5f,  .5f, 1.0f, 1.0f,  .5f,  .5f,-1.0f,
                    -1.0f,  0, 1.0f, 1.0f, 1.0f, 1.0f,  0,-1.0f,
                    -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,-1.0f,
                    -1.0f,  .5f,  0,  0,  0,  0,  .5f,-1.0f,
                    -2.0f,-1.0f,-1.0f,-1.0f,-1.0f,-1.0f,-1.0f,-2.0f

            };

    public static final float[] WHITE_ROOK = new float[]


            {
                    0,  0,  0,  0,  0,  0,  0,  0,
                    .5f, .8f, 1.0f, 1.0f, 1.0f, 1.0f, .8f,  .5f,
                    -.5f,  0,  0,  0,  0,  0,  0, -.5f,
                    -.5f, 0,  0,  0,  0,  0,  0, -.5f,
                    -.5f,  0,  0,  0,  0,  0,  0, -.5f,
                    -.5f,  0,  0,  0,  0,  0,  0, -.5f,
                    -.5f,  0,  0,  0,  0,  0,  0, -.5f,
                     0,  0,  0,  .5f,  0,  .5f,  0,  0
            };


    public static final float[] WHITE_KING_MID_GAME_TABLE = {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            20, 20,  0,  0,  0,  0, 20, 20,
            20, 30, 10,  0,  0, 10, 30, 20
    };


    public static final float[] WHITE_KING_END_GAME_TABLE = {
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50
    };
//    public static final float[] WHITE_PAWN = new float[]
//
//            {
//                    0,  0,  0,  0,  0,  0,  0,  0,
//                    50, 50, 50, 50, 50, 50, 50, 50,
//                    10, 10, 20, 30, 30, 20, 10, 10,
//                    5,  5, 10, 25, 25, 10,  5,  5,
//                    0,  0,  0, 23, 23,  0,  0,  0,
//                    5, -5,-10, 0, 0, -10, -5,  5,
//                    5, 10, 10,-24,-24, 10, 10,  5,
//                    0,  0,  0,  0,  0,  0,  0,  0
//            };
//
//    public static final float[] WHITE_KNIGHT = new float[]
//            {
//                    -50,-40,-30,-30,-30,-30,-40,-50,
//                    -40,-20,  0,  0,  0,  0,-20,-40,
//                    -30,  0, 6, 15, 15, 6,  0,-30,
//                    -30,  5, 15, 20, 20, 15,  5,-30,
//                    -30,  0, 15, 20, 20, 15,  0,-30,
//                    -30,  5, 6, 15, 15, 6,  5,-30,
//                    -40,-20,  0,  5,  5,  0,-20,-40,
//                    -50,-35,-30,-30,-30,-30,-35,-50
//            };
//
//    public static final float[] WHITE_BISHOP = new float[]
//
//            {
//                    -20,-10,-10,-10,-10,-10,-10,-20,
//                    -10,  0,  0,  0,  0,  0,  0,-10,
//                    -10,  0,  5, 10, 10,  5,  0,-10,
//                    -10,  5,  5, 10, 10,  5,  5,-10,
//                    -10,  0, 10, 10, 10, 10,  0,-10,
//                    -10, 10, 10, 10, 10, 10, 10,-10,
//                    -10,  5,  0,  0,  0,  0,  5,-10,
//                    -20,-10,-10,-10,-10,-10,-10,-20
//
//            };
//
//    public static final float[] WHITE_ROOK = new float[]
//
//
//            {
//                    0,  0,  0,  0,  0,  0,  0,  0,
//                    5, 8, 10, 10, 10, 10, 8,  5,
//                    -5,  0,  0,  0,  0,  0,  0, -5,
//                    -5,  0,  0,  0,  0,  0,  0, -5,
//                    -5,  0,  0,  0,  0,  0,  0, -5,
//                    -5,  0,  0,  0,  0,  0,  0, -5,
//                    -5,  0,  0,  0,  0,  0,  0, -5,
//                     0,  0,  0,  5,  0,  5,  0,  0
//            };
//
//
//    public static final float[] WHITE_KING_MID_GAME_TABLE = {
//            -30,-40,-40,-50,-50,-40,-40,-30,
//            -30,-40,-40,-50,-50,-40,-40,-30,
//            -30,-40,-40,-50,-50,-40,-40,-30,
//            -30,-40,-40,-50,-50,-40,-40,-30,
//            -20,-30,-30,-40,-40,-30,-30,-20,
//            -10,-20,-20,-20,-20,-20,-20,-10,
//            20, 20,  0,  0,  0,  0, 20, 20,
//            20, 30, 10,  0,  0, 10, 30, 20
//    };
//
//
//    public static final float[] WHITE_KING_END_GAME_TABLE = {
//            -50,-40,-30,-20,-20,-30,-40,-50,
//            -30,-20,-10,  0,  0,-10,-20,-30,
//            -30,-10, 20, 30, 30, 20,-10,-30,
//            -30,-10, 30, 40, 40, 30,-10,-30,
//            -30,-10, 30, 40, 40, 30,-10,-30,
//            -30,-10, 20, 30, 30, 20,-10,-30,
//            -30,-30,  0,  0,  0,  0,-30,-30,
//            -50,-30,-30,-30,-30,-30,-30,-50
//    };

    public static final float[] BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_KING_MID_GAME_TABLE, BLACK_KING_END_GAME_TABLE;

    static{
        BLACK_PAWN = new float[64];
        BLACK_KNIGHT = new float[64];
        BLACK_BISHOP = new float[64];
        BLACK_ROOK = new float[64];
        BLACK_KING_MID_GAME_TABLE = new float[64];
        BLACK_KING_END_GAME_TABLE = new float[64];
//        for(int i=0;i<64;i++){
//            BLACK_PAWN[i] = WHITE_PAWN[i^56];
//            BLACK_KNIGHT[i] = WHITE_KNIGHT[i^56];
//            BLACK_BISHOP[i] = WHITE_BISHOP[i^56];
//            BLACK_ROOK[i] = WHITE_ROOK[i^56];
//        }



        for(int i=63;i>=0;i--){
            BLACK_PAWN[63-i] = WHITE_PAWN[i];
            BLACK_KNIGHT[63-i] = WHITE_KNIGHT[i];
            BLACK_BISHOP[63-i] = WHITE_BISHOP[i];
            BLACK_ROOK[63-i] = WHITE_ROOK[i];
            BLACK_KING_MID_GAME_TABLE[63-i] = WHITE_KING_MID_GAME_TABLE[i];
            BLACK_KING_END_GAME_TABLE[63-i] = WHITE_KING_END_GAME_TABLE[i];

        }




    }

    public static final float[] EMPTY = new float[64];

    public static final float[][] WHITE_PST = {EMPTY, WHITE_PAWN, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, EMPTY};
    public static final float[][] BLACK_PST = {EMPTY, BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, EMPTY};

    public static final float[][][] PST = {WHITE_PST, BLACK_PST};

    public static final float[][] KING_MID_GAME_PST = {WHITE_KING_MID_GAME_TABLE, BLACK_KING_MID_GAME_TABLE};
    public static final float[][] KING_END_GAME_PST = {WHITE_KING_END_GAME_TABLE, BLACK_KING_END_GAME_TABLE};






}
