package evaluation.classic;

import server.board.Board;
import static server.utils.Utils.*;

import static server.preload.PrecomputedMasks.*;
import static server.utils.Constants.*;
import static evaluation.classic.EvaluationValues.*;

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
        int nPieces = (position.nPieces[position.side]+position.nPieces[position.opponent]);

        int whitePiecesInBlacksTerritory = Long.bitCount(position.occupancyBoard[WHITE] & TERRITORY[BLACK]);
        int blackPiecesInWhitesTerritory = Long.bitCount(position.occupancyBoard[BLACK] & TERRITORY[WHITE]);


        weight += (whitePiecesInBlacksTerritory + blackPiecesInWhitesTerritory) * nPieces * 0.01f;

//        int whitePawnsInBlacksTerritory = Long.bitCount(position.bitboard[WHITE][PAWN_INDEX] * TERRITORY[BLACK]);
//        int blackPawnsInWhitesTerritory = Long.bitCount(position.bitboard[BLACK][PAWN_INDEX] * TERRITORY[WHITE]);
//

//
//        weight += (whitePawnsInBlacksTerritory+blackPawnsInWhitesTerritory) * nPieces * 0.033f;
//
        weight += Math.abs(2-nQueens) * 2f;
        weight += Math.abs(8-nMinors) * 0.8f;
        weight += (32-nPieces) * 0.048f;
//        weight += piecesOnCenterSquares * 0.3f;
//        weight += piecesOnMidRanks * 0.25f;
//        weight -= pawnsOnCenterSquares * 0.25f;
//        weight += (whiteRooksInBlacksTerritory + blackRooksInWhitesTerritory) * 3;
//        weight -= distanceBetweenKings * 0.01f;
//        if(weight < 0) {
//            System.out.println("eg_score: " + weight + " fc: " + position.fullMoveClock);
//        }
//        System.out.println("eg_score: " + weight + " fc: " + position.fullMoveClock);
        return weight;
    }



    public static float evaluatePosition(Board board, float endGameWeight){
        float eval = 0;

        eval += evaluatePawns(board.side,board.bitboard[board.side][PAWN_INDEX],board.bitboard[board.opponent][PAWN_INDEX],endGameWeight);
        eval -= evaluatePawns(board.opponent,board.bitboard[board.opponent][PAWN_INDEX],board.bitboard[board.side][PAWN_INDEX],endGameWeight);

//        for(int fileIndex = 0;fileIndex<8;fileIndex++){
//            eval -= (Long.bitCount(board.bitboard[board.side][PAWN_INDEX] & FILE_MASKS[fileIndex])-1) * DOUBLED_PAWN_SCORE;
//            eval += (Long.bitCount(board.bitboard[board.opponent][PAWN_INDEX] & FILE_MASKS[fileIndex])-1) * DOUBLED_PAWN_SCORE;
//        }

        for(int pieceIndex = KNIGHT_INDEX;pieceIndex < 6; pieceIndex ++){
//            if(pieceIndex == PAWN_INDEX){
//                continue;
//            }

            float pieceValue = PIECE_VALUES[pieceIndex];

            long allyPieceBitboard = board.bitboard[board.side][pieceIndex];
            while(allyPieceBitboard!=0){
                long allyPiece = lastOne(allyPieceBitboard);
                allyPieceBitboard ^= allyPiece;
                int squareIndex = Long.numberOfTrailingZeros(allyPiece);
                eval += pieceValue;
                eval += PST[board.opponent][pieceIndex][squareIndex] / endGameWeight;
//                if(pieceIndex != ROOK_INDEX) {
//                    eval += PST[board.side][pieceIndex][squareIndex] / endGameWeight;
//                }

//                if(pieceIndex == PAWN_INDEX){
//                    long pawnsOnAdjacentFiles = (PAWN_ATTACKS[board.side][squareIndex] | PAWN_ATTACKS[board.opponent][squareIndex]) & board.bitboard[board.side][pieceIndex];
//                    eval += Long.bitCount(pawnsOnAdjacentFiles) * PAWN_CHAIN_SCORE;
//
//                    int fileIndex = squareIndex%8;
//
//                    long advanceBits = ADVANCE_BITS[board.side][squareIndex];
//
//                    long fileMask = FILE_MASKS[fileIndex];
//                    long leftFileMask = FILE_MASKS[Math.max(0,fileIndex-1)];
//                    long rightFileMask = FILE_MASKS[Math.min(7,fileIndex+1)];
//
//                    long blockerPawnsMask = (fileMask | leftFileMask | rightFileMask) & advanceBits & board.bitboard[board.opponent][pieceIndex];
//
//                    eval -= Long.bitCount(blockerPawnsMask) * BLOCKER_PAWNS_SCORE * endGameWeight;
//
//
//
//                }
//                else if(pieceIndex == KING_INDEX){
//                    int nPawnShield = Long.bitCount(ADVANCE_BITS[board.side][squareIndex] & KING_MOVES[squareIndex] & board.bitboard[board.side][PAWN_INDEX]);
//
//                    eval += nPawnShield * PAWN_SHIELD_SCORE;
//                }


            }

            long enemyPieceBitboard = board.bitboard[board.opponent][pieceIndex];
            while(enemyPieceBitboard!=0){
                long enemyPiece = lastOne(enemyPieceBitboard);
                enemyPieceBitboard ^= enemyPiece;
                int squareIndex = Long.numberOfTrailingZeros(enemyPiece);
                eval -= pieceValue;
                eval -= PST[board.opponent][pieceIndex][squareIndex] / endGameWeight;
//                if(pieceIndex != ROOK_INDEX) {
//                    eval -= PST[board.opponent][pieceIndex][squareIndex] / endGameWeight;
//                }

//                if(pieceIndex == PAWN_INDEX){
//                    long supportPawns = (PAWN_ATTACKS[board.side][squareIndex] | PAWN_ATTACKS[board.opponent][squareIndex]) & board.bitboard[board.opponent][pieceIndex];
//                    eval -= Long.bitCount(supportPawns) * PAWN_CHAIN_SCORE;
//
//                    int fileIndex = squareIndex%8;
//
//                    long advanceBits = ADVANCE_BITS[board.opponent][squareIndex];
//
//                    long fileMask = FILE_MASKS[fileIndex];
//                    long leftFileMask = FILE_MASKS[Math.max(0,fileIndex-1)];
//                    long rightFileMask = FILE_MASKS[Math.min(7,fileIndex+1)];
//
//                    long blockerPawnsMask = (fileMask | leftFileMask | rightFileMask) & advanceBits & board.bitboard[board.side][pieceIndex];
//
//                    eval += Long.bitCount(blockerPawnsMask) * BLOCKER_PAWNS_SCORE * endGameWeight;
//
//                    long pawnsOnSameFile = (board.bitboard[board.opponent][pieceIndex] & fileMask) ^ SQUARE_MASKS[squareIndex];
//                    eval += Long.bitCount(pawnsOnSameFile) * DOUBLED_PAWN_SCORE;
//
//                }
//                else if(pieceIndex == KING_INDEX){
//                    int nPawnShield = Long.bitCount(ADVANCE_BITS[board.opponent][squareIndex] & KING_MOVES[squareIndex] & board.bitboard[board.opponent][PAWN_INDEX]);
//
//                    eval -= nPawnShield * PAWN_SHIELD_SCORE;

//                    int opponentKingSquare = squareIndex;
//                    int opponentKingFile = opponentKingSquare%8;
//                    int opponentKingRank = opponentKingSquare/8;
//
//                    eval += (Math.abs(3-opponentKingFile) + Math.abs(3-opponentKingRank)) * endGameWeight * 0.5f;

//                    eval += (float) (Math.abs(31.5-squareIndex) + Math.abs(board.kingSquare-squareIndex)) * endGameWeight * 0.01f;


//                }

            }


        }

        int opponentKingSquare = Long.numberOfTrailingZeros(board.bitboard[board.opponent][KING_INDEX]);

        float kingPositionalScoreMultiplier = endGameWeight * 0.01f;
        eval += (float) (Math.abs(31.5-opponentKingSquare) * kingPositionalScoreMultiplier);
        eval -= (float) (Math.abs(31.5-board.kingSquare) * kingPositionalScoreMultiplier);
        eval -= Math.abs(board.kingSquare-opponentKingSquare) * kingPositionalScoreMultiplier;

//        long attackedSquares = ~board.safeSquares;
//        int numberOfAttackedRooks = Long.bitCount(board.bitboard[board.side][ROOK_INDEX] & attackedSquares);
//        int numberOfAttackedQueens = Long.bitCount(board.bitboard[board.side][QUEEN_INDEX] & attackedSquares);
//        int numberOfAttackedMinors = Long.bitCount((board.bitboard[board.side][BISHOP_INDEX] | board.bitboard[board.side][KNIGHT_INDEX]) & attackedSquares);
//        eval -= numberOfAttackedMinors * 3;
//        eval -= numberOfAttackedRooks * ROOK_INDEX;
//        eval -= numberOfAttackedQueens * QUEEN_INDEX;

//        eval += (float) (Math.abs(31.5-opponentKingSquare) - Math.abs(board.kingSquare-opponentKingSquare)) * endGameWeight * 0.05f;

//        eval += KING_MID_GAME_PST[board.side][board.kingSquare] / endGameWeight;
//        eval -= KING_MID_GAME_PST[board.opponent][opponentKingSquare] / endGameWeight;
//
//        eval += KING_END_GAME_PST[board.side][board.kingSquare] * endGameWeight;
//        eval -= KING_END_GAME_PST[board.opponent][opponentKingSquare] * endGameWeight;


        return eval;
    }
    
    
    public static float evaluatePawns(int side, long allyPawns, long enemyPawns, float endGameWeight){
        float eval = 0;
        
        long pawns = allyPawns;
        while(pawns != 0){
            long pawn = lastOne(pawns);
            pawns ^= pawn;
            int squareIndex = Long.numberOfTrailingZeros(pawn);
            eval += PAWN_VALUE;
            eval += PST[side][PAWN_INDEX][squareIndex]/endGameWeight;

            int fileIndex = squareIndex%8;
            int rankIndex = squareIndex/8;
            eval += PAWN_RANK_BONUS[side][rankIndex];
            long leftFileMask = FILE_MASKS[Math.max(0,fileIndex-1)];
            long rightFileMask = FILE_MASKS[Math.min(7,fileIndex+1)];
            long blockerPawnsMask = (FILE_MASKS[fileIndex] | leftFileMask | rightFileMask) & ADVANCE_BITS[side][squareIndex] & enemyPawns;
            eval -= Long.bitCount(blockerPawnsMask) * BLOCKER_PAWNS_SCORE * endGameWeight;
            long pawnsOnSameFile = (allyPawns & FILE_MASKS[fileIndex]) ^ SQUARE_MASKS[squareIndex];
            eval -= Long.bitCount(pawnsOnSameFile) * DOUBLED_PAWN_SCORE;
        }
        
        
        return eval;
    }



    public static float evaluatePosition2(Board position, float endGameWeight){
        float eval = 0;

        eval += evaluatePawns(position.side,position.bitboard[position.side][PAWN_INDEX],position.bitboard[position.opponent][PAWN_INDEX],endGameWeight);
        eval -= evaluatePawns(position.opponent,position.bitboard[position.opponent][PAWN_INDEX],position.bitboard[position.side][PAWN_INDEX],endGameWeight);


        for(int pieceIndex=KNIGHT_INDEX;pieceIndex<6;pieceIndex++){
            eval += Long.bitCount(position.bitboard[position.side][pieceIndex]) * PIECE_VALUES[pieceIndex];
            eval -= Long.bitCount(position.bitboard[position.opponent][pieceIndex]) * PIECE_VALUES[pieceIndex];
        }

        int opponentKingSquare = Long.numberOfTrailingZeros(position.bitboard[position.opponent][KING_INDEX]);

        float kingPositionalScoreMultiplier = endGameWeight * 0.01f;
        eval += (float) (Math.abs(31.5-opponentKingSquare) * kingPositionalScoreMultiplier);
        eval -= (float) (Math.abs(31.5-position.kingSquare) * kingPositionalScoreMultiplier);
        eval -= Math.abs(position.kingSquare-opponentKingSquare) * kingPositionalScoreMultiplier;

        return eval;


    }


}



















