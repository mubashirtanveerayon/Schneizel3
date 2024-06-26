package evaluation.re;

import server.board.Board;

import static evaluation.re.ReEvaluationValues.PIECE_SQUARE_TABLE;
import static evaluation.re.ReEvaluationValues.PIECE_VALUES;
import server.utils.Constants;

import server.move.Move;
import server.utils.*;

public class ReEvaluation {




    public static int reEvaluateMove(Move move){
        int eval = 0;

        if(move.isCapture)eval += PIECE_VALUES[move.capturePieceIndex];
        if(move.isPromotion){
            eval += PIECE_VALUES[move.promotionPieceIndex];
            eval -= PIECE_VALUES[move.pieceIndex];
        }else if(move.isEP)eval += PIECE_VALUES[Constants.PAWN_INDEX];
        else if(move.isCastling && !move.isKingSideCastling){
            if(move.sideToMove == Constants.WHITE)eval += PIECE_SQUARE_TABLE[move.sideToMove][Constants.ROOK_INDEX][59];
            else eval += PIECE_SQUARE_TABLE[move.sideToMove][Constants.ROOK_INDEX][63-59];
        }

        eval += PIECE_SQUARE_TABLE[move.sideToMove][move.pieceIndex][move.targetSquare];
        eval -= PIECE_SQUARE_TABLE[move.sideToMove][move.pieceIndex][move.startSquare];

        return eval;
    }
    // material and PST
    public static int reEvaluatePosition(Board position){
        int eval = 0;

        for(int pieceIndex = 0;pieceIndex<6;pieceIndex++){
            long bitboard = position.bitboard[position.side][pieceIndex];
            int count = 0;
            while(bitboard != 0){
                long piece = Utils.lastOne(bitboard);
                bitboard ^= piece;
                eval += PIECE_SQUARE_TABLE[position.side][pieceIndex][Long.numberOfTrailingZeros(piece)];
                count++;
            }
            eval += count * PIECE_VALUES[pieceIndex];

            bitboard = position.bitboard[position.opponent][pieceIndex];
            count = 0;
            while(bitboard != 0){
                long piece = Utils.lastOne(bitboard);
                bitboard ^= piece;
                eval -= PIECE_SQUARE_TABLE[position.opponent][pieceIndex][Long.numberOfTrailingZeros(piece)];
                count++;
            }
            eval -= count * PIECE_VALUES[pieceIndex];


        }

        // eval += reEvaluateKingSafety(position);

        return eval;
    }

    public static int reEvaluateKingSafety(Board position){
        int eval = 0;
        int opponentKingSquare = Long.numberOfTrailingZeros(position.bitboard[position.opponent][Constants.KING_INDEX]);
        int kingPositionalScoreMultiplier = 1;// 32-Long.bitCount(position.occupied) + position.fullMoveClock;
        eval += Math.abs(32 - opponentKingSquare) * kingPositionalScoreMultiplier;
        eval -= Math.abs(32 - position.kingSquare) * kingPositionalScoreMultiplier;
        return eval;
    }

}
