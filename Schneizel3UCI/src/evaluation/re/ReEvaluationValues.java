package evaluation.re;

public class ReEvaluationValues {

    public static final int[] PIECE_VALUES = {0,100,300,350,500,900};

    public static final int[] EMPTY_TABLE = new int[]
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0
            };

    public static final int[] WHITE_PAWN = new int[]

        {
                90,  90,  90,  90,  90,  90,  90,  90,
                30,  30,  30,  40,  40,  30,  30,  30,
                20,  20,  20,  30,  30,  30,  20,  20,
                10,  10,  10,  20,  20,  10,  10,  10,
                5,   5,  10,  20,  20,   5,   5,   5,
                0,   0,   0,   5,   5,   0,   0,   0,
                0,   0,   0, -10, -10,   0,   0,   0,
                0,   0,   0,   0,   0,   0,   0,   0
        };

    public static final int[] WHITE_BISHOP = new int[]
            {
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0,   0,   0,   0,   0,   0,
                    0,   0,   0,  10,  10,   0,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,  10,   0,   0,   0,   0,  10,   0,
                    0,  30,   0,   0,   0,   0,  30,   0,
                    0,   0, -10,   0,   0, -10,   0,   0
            };

    public static final int[] WHITE_ROOK = new int[]
            {
                    50,  50,  50,  50,  50,  50,  50,  50,
                    50,  50,  50,  50,  50,  50,  50,  50,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,  10,  20,  20,  10,   0,   0,
                    0,   0,   0,  20,  20,   0,   0,   0
            };

    public static final int[] WHITE_KNIGHT = new int[]
            {
                    -5,   0,   0,   0,   0,   0,   0,  -5,
                    -5,   0,   0,  10,  10,   0,   0,  -5,
                    -5,   5,  20,  20,  20,  20,   5,  -5,
                    -5,  10,  20,  30,  30,  20,  10,  -5,
                    -5,  10,  20,  30,  30,  20,  10,  -5,
                    -5,   5,  20,  10,  10,  20,   5,  -5,
                    -5,   0,   0,   0,   0,   0,   0,  -5,
                    -5, -10,   0,   0,   0,   0, -10,  -5
            };


    public static final int[] BLACK_KNIGHT;
    public static final int[] BLACK_BISHOP;
    public static final int[] BLACK_PAWN;
    public static final int[] BLACK_ROOK;


    public static final int[][][] PIECE_SQUARE_TABLE;

    static{
        BLACK_KNIGHT = new int[64];
        BLACK_BISHOP = new int[64];
        BLACK_PAWN = new int[64];
        BLACK_ROOK = new int[64];

        for(int i=63;i>=0;i--){
            BLACK_KNIGHT[63-i] = WHITE_KNIGHT[i];
            BLACK_BISHOP[63-i] = WHITE_BISHOP[i];
            BLACK_ROOK[63-i] = WHITE_ROOK[i];
            BLACK_PAWN[63-i] = WHITE_PAWN[i];
        }

        PIECE_SQUARE_TABLE = new int[][][]
                {
                        {EMPTY_TABLE,WHITE_PAWN, WHITE_KNIGHT,WHITE_BISHOP,WHITE_ROOK,EMPTY_TABLE},
                        {EMPTY_TABLE,BLACK_PAWN, BLACK_KNIGHT,BLACK_BISHOP,BLACK_ROOK,EMPTY_TABLE}
        };
    }


}
