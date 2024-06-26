package evaluation.classic;

import server3.board.Board;
import server3.utils.Utils;
import static server3.utils.Constants.*;
import static server3.preload.PrecomputedMasks.*;
public class ClassicalEvaluation {


    public static float endGameWeight(Board position){
        float weight = 1;

        int nQueens = Long.bitCount(position.bitboard[position.side][QUEEN_INDEX] | position.bitboard[position.opponent][QUEEN_INDEX]);
        int nMinors = Long.bitCount(position.bitboard[position.side][BISHOP_INDEX] | position.bitboard[position.opponent][BISHOP_INDEX] | position.bitboard[position.side][KNIGHT_INDEX] | position.bitboard[position.opponent][KNIGHT_INDEX]);

//        int piecesOnCenterSquares = Long.bitCount(position.occupied & CENTER_SQUARES_MASK);
//        int piecesOnMidRanks = Long.bitCount(position.occupied & CENTER_RANKS) - piecesOnCenterSquares*0;
//
//        int pawnsOnCenterSquares = Long.bitCount((position.bitboard[position.side][PAWN_INDEX] | position.bitboard[position.opponent][PAWN_INDEX]) & CENTER_SQUARES_MASK);//mid-game

//        int whiteRooksInBlacksTerritory = Long.bitCount((RANK_MASKS[0] | RANK_MASKS[1] | RANK_MASKS[2]) & position.bitboard[WHITE][ROOK_INDEX]);
//        int blackRooksInWhitesTerritory = Long.bitCount((RANK_MASKS[5] | RANK_MASKS[6] | RANK_MASKS[7]) & position.bitboard[BLACK][ROOK_INDEX]);

//        int distanceBetweenKings = Math.abs(position.kingSquare - Long.numberOfTrailingZeros(position.bitboard[position.opponent][KING_INDEX]));


        int whitePiecesInBlacksTerritory = Long.bitCount(position.occupancyBoard[WHITE] & TERRITORY[BLACK]);
        int blackPiecesInWhitesTerritory = Long.bitCount(position.occupancyBoard[BLACK] & TERRITORY[WHITE]);


        weight += (whitePiecesInBlacksTerritory + blackPiecesInWhitesTerritory) * 0.58f;

        weight += Math.abs(2-nQueens) * 3;
        weight += Math.abs(8-nMinors) * 0.6f;
        weight += (32-position.nPieces[position.side]-position.nPieces[position.opponent]) * 0.8f;
//        weight += piecesOnCenterSquares * 0.3f;
//        weight += piecesOnMidRanks * 0.25f;
//        weight -= pawnsOnCenterSquares * 0.25f;
//        weight += (whiteRooksInBlacksTerritory + blackRooksInWhitesTerritory) * 3;
//        weight -= distanceBetweenKings * 0.01f;
        if(weight < 0) {
            System.out.println("eg_score: " + weight + " fc: " + position.fullMoveClock);
        }
        return weight;
    }


    public static float evaluatePosition(Board board){
        float eval = 0;
        float endGameWeight = endGameWeight(board);

        for(int pieceIndex = 0;pieceIndex < 6; pieceIndex ++){

            float pieceValue = PIECE_VALUES[pieceIndex];

            long allyPieceBitboard = board.bitboard[board.side][pieceIndex];
            while(allyPieceBitboard!=0){
                long allyPiece = Utils.lastOne(allyPieceBitboard);
                allyPieceBitboard ^= allyPiece;
                int squareIndex = Long.numberOfTrailingZeros(allyPiece);
                eval += pieceValue;
                eval += PieceSquareTables.PST[board.side][pieceIndex][squareIndex] * pieceValue / endGameWeight;

                if(pieceIndex == PAWN_INDEX){
                    long pawnsOnAdjacentFiles = (PAWN_ATTACKS[board.side][squareIndex] | PAWN_ATTACKS[board.opponent][squareIndex]) & board.bitboard[board.side][pieceIndex];
                    eval += Long.bitCount(pawnsOnAdjacentFiles) * PAWN_CHAIN_SCORE;

                    int fileIndex = squareIndex%8;

                    long advanceBits = ADVANCE_BITS[board.side][squareIndex];

                    long fileMask = FILE_MASKS[fileIndex];
                    long leftFileMask = FILE_MASKS[Math.max(0,fileIndex-1)];
                    long rightFileMask = FILE_MASKS[Math.min(7,fileIndex+1)];

                    long blockerPawnsMask = (fileMask | leftFileMask | rightFileMask) & advanceBits & board.bitboard[board.opponent][pieceIndex];

                    eval -= Long.bitCount(blockerPawnsMask) * BLOCKER_PAWNS_SCORE * endGameWeight;

                    long pawnsOnSameFile = (board.bitboard[board.side][pieceIndex] & fileMask) ^ SQUARE_MASKS[squareIndex];
                    eval -= Long.bitCount(pawnsOnSameFile) * DOUBLED_PAWN_SCORE;

                }else if(pieceIndex == KING_INDEX){
                    int nPawnShield = Long.bitCount(ADVANCE_BITS[board.side][squareIndex] & KING_MOVES[squareIndex] & board.bitboard[board.side][PAWN_INDEX]);

                    eval += nPawnShield * PAWN_SHIELD_SCORE;
                }


            }

            long enemyPieceBitboard = board.bitboard[board.opponent][pieceIndex];
            while(enemyPieceBitboard!=0){
                long enemyPiece = Utils.lastOne(enemyPieceBitboard);
                enemyPieceBitboard ^= enemyPiece;
                int squareIndex = Long.numberOfTrailingZeros(enemyPiece);
                eval -= pieceValue;
                eval -= PieceSquareTables.PST[board.opponent][pieceIndex][squareIndex] * pieceValue / endGameWeight;

                if(pieceIndex == PAWN_INDEX){
                    long supportPawns = (PAWN_ATTACKS[board.side][squareIndex] | PAWN_ATTACKS[board.opponent][squareIndex]) & board.bitboard[board.opponent][pieceIndex];
                    eval -= Long.bitCount(supportPawns) * PAWN_CHAIN_SCORE;

                    int fileIndex = squareIndex%8;

                    long advanceBits = ADVANCE_BITS[board.opponent][squareIndex];

                    long fileMask = FILE_MASKS[fileIndex];
                    long leftFileMask = FILE_MASKS[Math.max(0,fileIndex-1)];
                    long rightFileMask = FILE_MASKS[Math.min(7,fileIndex+1)];

                    long blockerPawnsMask = (fileMask | leftFileMask | rightFileMask) & advanceBits & board.bitboard[board.side][pieceIndex];

                    eval += Long.bitCount(blockerPawnsMask) * BLOCKER_PAWNS_SCORE * endGameWeight;

                    long pawnsOnSameFile = (board.bitboard[board.opponent][pieceIndex] & fileMask) ^ SQUARE_MASKS[squareIndex];
                    eval += Long.bitCount(pawnsOnSameFile) * DOUBLED_PAWN_SCORE;

                }else if(pieceIndex == KING_INDEX){
                    int nPawnShield = Long.bitCount(ADVANCE_BITS[board.opponent][squareIndex] & KING_MOVES[squareIndex] & board.bitboard[board.opponent][PAWN_INDEX]);

                    eval -= nPawnShield * PAWN_SHIELD_SCORE;

                    int opponentKingSquare = squareIndex;
                    int opponentKingFile = opponentKingSquare%8;
                    int opponentKingRank = opponentKingSquare/8;

                    eval += (Math.abs(3-opponentKingFile) + Math.abs(3-opponentKingRank)) * endGameWeight;

                }

            }


        }

        return eval;
    }




}



















