package evaluation.classic;

import server.board.Board;
import server.move.Move;
import server.utils.Utils;
import static server.utils.Constants.*;
import static evaluation.re.ReEvaluationValues.PIECE_SQUARE_TABLE;
import static evaluation.re.ReEvaluationValues.PIECE_VALUES;

public class StaticEvaluation {

    public static int positionStaticEvaluation(Board position){
        int eval = 0;
        for(int pieceIndex=0;pieceIndex<6;pieceIndex++){
            long bitboard = position.bitboard[WHITE][pieceIndex];
            int count = 0;
            while(bitboard != 0){
                long piece = Utils.lastOne(bitboard);
                bitboard ^= piece;
                eval += PIECE_SQUARE_TABLE[WHITE][pieceIndex][Long.numberOfTrailingZeros(piece)];
                count++;
            }
            eval += count * PIECE_VALUES[pieceIndex];

            bitboard = position.bitboard[BLACK][pieceIndex];
            count=0;
            while(bitboard!=0) {
                long piece = Utils.lastOne(bitboard);
                bitboard ^= piece;
                eval -= PIECE_SQUARE_TABLE[BLACK][pieceIndex][Long.numberOfTrailingZeros(piece)];
                count++;
            }
            eval -= count * PIECE_VALUES[pieceIndex];
        }
        return eval;
    }

    public static int moveStaticEvaluation(Move move){
        int eval = 0;

        if(move.isCapture)eval += PIECE_VALUES[move.capturePieceIndex];
        if(move.isPromotion){
            eval += PIECE_VALUES[move.promotionPieceIndex];
            eval -= PIECE_VALUES[move.pieceIndex];
        }else if(move.isEP)eval += PIECE_VALUES[PAWN_INDEX];
        else if(move.isCastling && !move.isKingSideCastling){
            if(move.sideToMove == WHITE)eval += PIECE_SQUARE_TABLE[move.sideToMove][ROOK_INDEX][59];
            else eval += PIECE_SQUARE_TABLE[move.sideToMove][ROOK_INDEX][4];
        }

        eval += PIECE_SQUARE_TABLE[move.sideToMove][move.pieceIndex][move.targetSquare];
        eval -= PIECE_SQUARE_TABLE[move.sideToMove][move.pieceIndex][move.startSquare];

        return eval * (move.sideToMove == BLACK ? -1 : 1);
    }
}
