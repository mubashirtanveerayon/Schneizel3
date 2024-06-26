package server.preload;

import java.security.SecureRandom;
import static server.utils.Constants.*;

public class PrecomputedMasks {

    public static final long[] RANK_MASKS, FILE_MASKS, DIAGONAL_MASKS, ANTI_DIAGONAL_MASKS, SQUARE_MASKS, KNIGHT_MOVES, KING_MOVES, TERRITORY;


    public static final long[][] PAWN_ATTACKS, KING_CASTLING_PATH, ADVANCE_BITS;
    public static final long RANDOM_ZOBRIST_VALUE, CENTER_SQUARES_MASK, CENTER_RANKS;



    static {
        SQUARE_MASKS = new long[64];
        RANK_MASKS = new long[8];
        FILE_MASKS = new long[8];
        DIAGONAL_MASKS = new long[15];
        ANTI_DIAGONAL_MASKS = new long[15];

        KNIGHT_MOVES = new long[64];
        KING_MOVES = new long[64];
        PAWN_ATTACKS = new long[2][64];

        KING_CASTLING_PATH = new long[2][2];

        RANDOM_ZOBRIST_VALUE = new SecureRandom().nextLong();



        TERRITORY = new long[2];


        for(int i = 0;i < 64; i++){
            SQUARE_MASKS[i] = 1L << i;
        }

        for (int i = 0; i < 8; i ++){
            long fileBitboard = 0;
            long rankBitboard = 0;
            for (int rank = 0; rank < 8; rank ++){
                for(int file = 0; file < 8; file ++){
                    if (file == i){
                        fileBitboard |= SQUARE_MASKS[file+rank*8];
                    }
                    if(rank == i){
                        rankBitboard |= SQUARE_MASKS[file+rank*8];
                    }
                }
            }
            RANK_MASKS[i] = rankBitboard;
            FILE_MASKS[i] = fileBitboard;
            if(i <= 3){
                TERRITORY[BLACK] |= rankBitboard;
            }else{
                TERRITORY[WHITE] |= rankBitboard;
            }
        }

        CENTER_SQUARES_MASK =  (FILE_MASKS[3] | FILE_MASKS[4] | FILE_MASKS[5]) & (RANK_MASKS[3] | RANK_MASKS[4]);



        for(int i=0; i<15; i++){
            long dBitboard = 0;
            long antiDBitboard = 0;
            for (int rank = 0; rank < 8; rank ++){
                for(int file = 0; file < 8; file ++){
                    if (file+rank == i){
                        dBitboard |= SQUARE_MASKS[file+rank*8];
                    }
                    if(rank+7-file == i){
                        antiDBitboard |= SQUARE_MASKS[file+rank*8];
                    }
                }
            }
            DIAGONAL_MASKS[i] = dBitboard;

            ANTI_DIAGONAL_MASKS[i] = antiDBitboard;
        }


        KING_CASTLING_PATH[WHITE][KING_SIDE_CASTLE_INDEX] = SQUARE_MASKS[WHITE_KING_INITIAL_POSITION + 1] | SQUARE_MASKS[WHITE_KING_INITIAL_POSITION + 2];
        KING_CASTLING_PATH[BLACK][KING_SIDE_CASTLE_INDEX] = SQUARE_MASKS[BLACK_KING_INITIAL_POSITION + 1] | SQUARE_MASKS[BLACK_KING_INITIAL_POSITION + 2];
        KING_CASTLING_PATH[WHITE][QUEEN_SIDE_CASTLE_INDEX] = SQUARE_MASKS[WHITE_KING_INITIAL_POSITION - 1] | SQUARE_MASKS[WHITE_KING_INITIAL_POSITION - 2];
        KING_CASTLING_PATH[BLACK][QUEEN_SIDE_CASTLE_INDEX] = SQUARE_MASKS[BLACK_KING_INITIAL_POSITION - 1] | SQUARE_MASKS[BLACK_KING_INITIAL_POSITION - 2];


        long[] whiteAdvanceBits = new long[64];
        long[] blackAdvanceBits = new long[64];

        for (int i=0; i<64; i++){

            int rankIndex = i/8;

            if(i != 0) {
                whiteAdvanceBits[i] = BOARD_MASK >>> 8 * (7 - rankIndex + 1);
            }else{
                whiteAdvanceBits[i] = 0;
            }

            if(i != 7){
                blackAdvanceBits[i] = BOARD_MASK << 8 * (rankIndex + 1);
            }else{
                blackAdvanceBits[i] = 0;
            }


            PAWN_ATTACKS[WHITE][i] = maskPawnAttacks(WHITE,SQUARE_MASKS[i]);
            PAWN_ATTACKS[BLACK][i] = maskPawnAttacks(BLACK,SQUARE_MASKS[i]);
            KNIGHT_MOVES[i] = maskKnightMoves(SQUARE_MASKS[i]);
            KING_MOVES[i] = maskKingMoves(SQUARE_MASKS[i]);


//            PAWN_ATTACKS[WHITE][i] = maskPawnAttacks(WHITE,i);
//            PAWN_ATTACKS[BLACK][i] = maskPawnAttacks(BLACK,i);
//            KNIGHT_MOVES[i] = maskKnightMoves(i);
//            KING_MOVES[i] = maskKingMoves(i);

        }

        ADVANCE_BITS = new long[][]{whiteAdvanceBits, blackAdvanceBits};

        CENTER_RANKS = (RANK_MASKS[2] | RANK_MASKS[3] | RANK_MASKS[4] | RANK_MASKS[5]);



    }

    public static long maskPawnAttacks(int side, long pawn){
        long attacks = 0;
        if (side == WHITE){
            attacks |= (pawn >>> 7) & ~FILE_MASKS[0];
            attacks |= (pawn >>> 9) & ~FILE_MASKS[7];
        }else{
            attacks |= (pawn << 7) & ~FILE_MASKS[7];
            attacks |= (pawn << 9) & ~FILE_MASKS[0];
        }
        return attacks;
    }

    public static long maskKnightMoves(long knight){
        long moves = 0;
        moves |= knight >>> 6 & ~FILE_MASKS[0] & ~FILE_MASKS[1];
        moves |= knight >>> 10 & ~FILE_MASKS[6] & ~FILE_MASKS[7];
        moves |= knight >>> 15 & ~FILE_MASKS[0];
        moves |= knight >>> 17 & ~FILE_MASKS[7];

        moves |= knight << 6 & ~FILE_MASKS[6] & ~FILE_MASKS[7];
        moves |= knight << 10 & ~FILE_MASKS[0] & ~FILE_MASKS[1];
        moves |= knight << 15 & ~FILE_MASKS[7];
        moves |= knight << 17 & ~FILE_MASKS[0];

        return moves;
    }

    public static long maskKingMoves(long king){
        long moves = 0;

        moves |= king >>> 1 & ~FILE_MASKS[7];
        moves |= king << 1 & ~FILE_MASKS[0];
        moves |= king >>> 7 & ~FILE_MASKS[0];
        moves |= king >>> 8;
        moves |= king >>> 9 & ~FILE_MASKS[7];
        moves |= king << 7 & ~FILE_MASKS[7];
        moves |= king << 8;
        moves |= king << 9 & ~FILE_MASKS[0];

        return moves;
    }

}
