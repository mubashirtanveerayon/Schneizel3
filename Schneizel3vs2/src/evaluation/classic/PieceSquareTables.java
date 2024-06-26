package evaluation.classic;

public class PieceSquareTables {






    public static final float[] WHITE_PAWN = new float[]

            {
                    0,  0,  0,  0,  0,  0,  0,  0,
                    50, 50, 50, 50, 50, 50, 50, 50,
                    10, 10, 20, 30, 30, 20, 10, 10,
                    5,  5, 10, 25, 25, 10,  5,  5,
                    0,  0,  0, 20, 20,  0,  0,  0,
                    5, -5,-10,  0,  0,-10, -5,  5,
                    5, 10, 10,-25,-25, 10, 10,  5,
                    0,  0,  0,  0,  0,  0,  0,  0
            };

    public static final float[] WHITE_KNIGHT = new float[]
            {
                    -50,-40,-30,-30,-30,-30,-40,-50,
                    -40,-20,  0,  0,  0,  0,-20,-40,
                    -30,  0, 10, 15, 15, 10,  0,-30,
                    -30,  5, 15, 20, 20, 15,  5,-30,
                    -30,  0, 15, 20, 20, 15,  0,-30,
                    -30,  5, 10, 15, 15, 10,  5,-30,
                    -40,-20,  0,  5,  5,  0,-20,-40,
                    -50,-40,-30,-30,-30,-30,-40,-50
            };

    public static final float[] WHITE_BISHOP = new float[]

            {
                    -20,-10,-10,-10,-10,-10,-10,-20,
                    -10,  0,  0,  0,  0,  0,  0,-10,
                    -10,  0,  5, 10, 10,  5,  0,-10,
                    -10,  5,  5, 10, 10,  5,  5,-10,
                    -10,  0, 10, 10, 10, 10,  0,-10,
                    -10, 10, 10, 10, 10, 10, 10,-10,
                    -10,  5,  0,  0,  0,  0,  5,-10,
                    -20,-10,-10,-10,-10,-10,-10,-20

            };

    public static final float[] WHITE_ROOK = new float[]


            {
                    0,  0,  0,  0,  0,  0,  0,  0,
                    5, 10, 10, 10, 10, 10, 10,  5,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                     0,  0,  0,  5,  5,  0,  0,  0
            };



    public static final float[] BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK;

    static{
        BLACK_PAWN = new float[64];
        BLACK_KNIGHT = new float[64];
        BLACK_BISHOP = new float[64];
        BLACK_ROOK = new float[64];

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
        }


    }

    public static final float[] EMPTY = new float[64];

    public static final float[][] WHITE_PST = {EMPTY, WHITE_PAWN, WHITE_KNIGHT, WHITE_BISHOP, WHITE_BISHOP, WHITE_ROOK, EMPTY};
    public static final float[][] BLACK_PST = {EMPTY, BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_BISHOP, BLACK_ROOK, EMPTY};

    public static final float[][][] PST = {WHITE_PST, BLACK_PST};






}
