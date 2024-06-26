package server3.move;

import server3.board.Board;
import server3.preload.PrecomputedMasks;
import server3.utils.Constants;
import server3.utils.Utils;

import java.util.ArrayList;

public class MoveGenerator {


    public static server3.move.Move fromString(Board board, String moveStr){
        for(server3.move.Move move:generateMoves(board)){
            if(move.toString().equals(moveStr)){
                return move;
            }
        }
        return null;
    }


    public static void addMovesToList(long moveMask, int startSquare, int pieceIndex,  Board board, ArrayList<server3.move.Move> list){
        while(moveMask != 0){
            long targetSquareMask = Utils.lastOne(moveMask);
            int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
            list.add(new server3.move.Move(startSquare,targetSquare,pieceIndex,board.indicesBoard[targetSquare], Constants.INVALID_INDEX));
            moveMask ^= targetSquareMask;
        }
    }



    public static void generateWhitePawnCaptures(Board board, ArrayList<server3.move.Move> list){
        if(board.isDoubleCheck){
            return;
        }

        long pawns = board.bitboard[board.side][Constants.PAWN_INDEX];

        long captureLeft = ((pawns >>> 7) & board.occupancyBoard[board.opponent]) & ~PrecomputedMasks.FILE_MASKS[0];
        long captureRight = (( pawns >>> 9) & board.occupancyBoard[board.opponent]) & ~PrecomputedMasks.FILE_MASKS[7];
        long promotionRank = PrecomputedMasks.RANK_MASKS[0] ;

        long captureRightNoPromotions = captureRight & board.checkRayMask & ~promotionRank;

        while(captureRightNoPromotions != 0){
            long targetSquareMask = Utils.lastOne(captureRightNoPromotions);
            captureRightNoPromotions ^= targetSquareMask;

            long startSquareMask = targetSquareMask << 9;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.INVALID_INDEX));
            }

        }

        long captureLeftMovesNoPromotions = captureLeft & board.checkRayMask & ~promotionRank;
        while(captureLeftMovesNoPromotions != 0){
            long targetSquareMask = Utils.lastOne(captureLeftMovesNoPromotions);
            captureLeftMovesNoPromotions ^= targetSquareMask;

            long startSquareMask = targetSquareMask << 7;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.INVALID_INDEX));
            }


        }


        long captureRightPromotionMoves = captureRight & board.checkRayMask & promotionRank;
        while(captureRightPromotionMoves!=0){
            long targetSquareMask = Utils.lastOne(captureRightPromotionMoves);
            captureRightPromotionMoves ^= targetSquareMask;

            long startSquareMask = targetSquareMask << 9;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.QUEEN_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.KNIGHT_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.ROOK_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.BISHOP_INDEX));
            }
        }


        long captureLeftPromotionsMoves = captureLeft & board.checkRayMask & promotionRank;
        while(captureLeftPromotionsMoves != 0){
            long targetSquareMask = Utils.lastOne(captureLeftPromotionsMoves);
            captureLeftPromotionsMoves ^= targetSquareMask;

            long startSquareMask = targetSquareMask << 7;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.QUEEN_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.KNIGHT_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.ROOK_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.BISHOP_INDEX));
            }


        }



        if(board.isEPAvailable){
            int epSquare = Long.numberOfTrailingZeros(board.epMask);
            int epRank = epSquare/8;
            long captureRankMask = PrecomputedMasks.RANK_MASKS[epRank+1];
            long epCandidatePawns = PrecomputedMasks.KING_MOVES[epSquare] & captureRankMask & pawns;

            boolean kingOnCaptureRank = (board.bitboard[board.side][Constants.KING_INDEX] & captureRankMask) != 0;

            while(epCandidatePawns != 0){
                long epPawn = Utils.lastOne(epCandidatePawns);
                epCandidatePawns ^= epPawn;
                int startSquare = Long.numberOfTrailingZeros(epPawn);



                boolean epPossible;
                if(kingOnCaptureRank){
                    if(board.kingSquare > startSquare){
                        epPossible = (Utils.firstOne(Utils.lowerBits(startSquare) & captureRankMask & board.occupied) & board.straightLineSlidingAttackers) == 0;
                    }else{
                        epPossible = (Utils.lastOne(Utils.higherBits(startSquare) & captureRankMask & board.occupied) & board.straightLineSlidingAttackers) == 0;
                    }
                }else{
                    epPossible = true;
                }

                if(epPossible){

                    long targetSquareMask = PrecomputedMasks.PAWN_ATTACKS[board.side][startSquare] & board.checkRayMask & board.epMask & board.pinRays[startSquare];
                    if (targetSquareMask!=0) {
                        int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                        list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.INVALID_INDEX));
                    }
                }

            }

        }


    }


    public static void generateBlackPawnCaptures(Board board, ArrayList<server3.move.Move> list){
        if(board.isDoubleCheck){
            return;
        }

        long pawns = board.bitboard[board.side][Constants.PAWN_INDEX];

        long captureLeft = ((pawns << 9) & board.occupancyBoard[board.opponent]) & ~PrecomputedMasks.FILE_MASKS[0];
        long captureRight = (( pawns << 7) & board.occupancyBoard[board.opponent]) & ~PrecomputedMasks.FILE_MASKS[7];
        long promotionRank = PrecomputedMasks.RANK_MASKS[7] ;

        long captureRightNoPromotions = captureRight & board.checkRayMask & ~promotionRank;

        while(captureRightNoPromotions != 0){
            long targetSquareMask = Utils.lastOne(captureRightNoPromotions);
            captureRightNoPromotions ^= targetSquareMask;

            long startSquareMask = targetSquareMask >>> 7;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.INVALID_INDEX));
            }

        }

        long captureLeftMovesNoPromotions = captureLeft & board.checkRayMask & ~promotionRank;
        while(captureLeftMovesNoPromotions != 0){
            long targetSquareMask = Utils.lastOne(captureLeftMovesNoPromotions);
            captureLeftMovesNoPromotions ^= targetSquareMask;

            long startSquareMask = targetSquareMask >>> 9;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.INVALID_INDEX));
            }


        }


        long captureRightPromotionMoves = captureRight & board.checkRayMask & promotionRank;
        while(captureRightPromotionMoves!=0){
            long targetSquareMask = Utils.lastOne(captureRightPromotionMoves);
            captureRightPromotionMoves ^= targetSquareMask;

            long startSquareMask = targetSquareMask >>> 7;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.QUEEN_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.KNIGHT_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.ROOK_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.BISHOP_INDEX));
            }
        }


        long captureLeftPromotionsMoves = captureLeft & board.checkRayMask & promotionRank;
        while(captureLeftPromotionsMoves != 0){
            long targetSquareMask = Utils.lastOne(captureLeftPromotionsMoves);
            captureLeftPromotionsMoves ^= targetSquareMask;

            long startSquareMask = targetSquareMask >>> 9;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.QUEEN_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.KNIGHT_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.ROOK_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX,board.indicesBoard[targetSquare], Constants.BISHOP_INDEX));
            }


        }



        if(board.isEPAvailable){
            int epSquare = Long.numberOfTrailingZeros(board.epMask);
            int epRank = epSquare/8;
            long captureRankMask = PrecomputedMasks.RANK_MASKS[epRank-1];
            long epCandidatePawns = PrecomputedMasks.KING_MOVES[epSquare] & captureRankMask & pawns;

            boolean kingOnCaptureRank = (board.bitboard[board.side][Constants.KING_INDEX] & captureRankMask) != 0;

            while(epCandidatePawns != 0){
                long epPawn = Utils.lastOne(epCandidatePawns);
                epCandidatePawns ^= epPawn;
                int startSquare = Long.numberOfTrailingZeros(epPawn);



                boolean epPossible;
                if(kingOnCaptureRank){
                    if(board.kingSquare > startSquare){
                        epPossible = (Utils.firstOne(Utils.lowerBits(startSquare) & captureRankMask & board.occupied) & board.straightLineSlidingAttackers) == 0;
                    }else{
                        epPossible = (Utils.lastOne(Utils.higherBits(startSquare) & captureRankMask & board.occupied) & board.straightLineSlidingAttackers) == 0;
                    }
                }else{
                    epPossible = true;
                }

                if(epPossible){

                    long targetSquareMask = PrecomputedMasks.PAWN_ATTACKS[board.side][startSquare] & board.checkRayMask & board.epMask & board.pinRays[startSquare];
                    if (targetSquareMask!=0) {
                        int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                        list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.INVALID_INDEX));
                    }
                }

            }

        }
    }


    public static void generateWhitePawnMoves(Board board, ArrayList<server3.move.Move> list){
        if(board.isDoubleCheck){
            return;
        }
        generateWhitePawnCaptures(board, list);
        long pawns = board.bitboard[board.side][Constants.PAWN_INDEX];
        long promotionRank = PrecomputedMasks.RANK_MASKS[0];

        long singlePush = pawns >>> 8 & board.emptySquaresMask;

        long doublePushCandidates = pawns & PrecomputedMasks.RANK_MASKS[6] & (singlePush << 8);

        long doublePushMask = doublePushCandidates >>> 16 & board.emptySquaresMask & board.checkRayMask;

        long singlePushNoPromotions = singlePush & ~promotionRank & board.checkRayMask;
        while(singlePushNoPromotions != 0){
            long targetSquareMask = Utils.lastOne(singlePushNoPromotions);
            singlePushNoPromotions ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask << 8);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.INVALID_INDEX));
            }
        }

        long singlePushPromotions = singlePush & promotionRank & board.checkRayMask;

        while(singlePushPromotions != 0){
            long targetSquareMask = Utils.lastOne(singlePushPromotions);
            singlePushPromotions ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask << 8);
            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.QUEEN_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.KNIGHT_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.ROOK_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.BISHOP_INDEX));
            }
        }

        while(doublePushMask != 0){
            long targetSquareMask = Utils.lastOne(doublePushMask);
            doublePushMask ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask << 16);
            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.INVALID_INDEX));
            }
        }

    }

    public static void generateBlackPawnMoves(Board board, ArrayList<server3.move.Move> list){
        if(board.isDoubleCheck){
            return;
        }
        generateBlackPawnCaptures(board, list);
        long pawns = board.bitboard[board.side][Constants.PAWN_INDEX];
        long promotionRank = PrecomputedMasks.RANK_MASKS[7];

        long singlePush = pawns << 8 & board.emptySquaresMask;

        long doublePushCandidates = pawns & PrecomputedMasks.RANK_MASKS[1] & (singlePush >> 8);

        long doublePushMask = doublePushCandidates << 16 & board.emptySquaresMask & board.checkRayMask;

        long singlePushNoPromotions = singlePush & ~promotionRank & board.checkRayMask;
        while(singlePushNoPromotions != 0){
            long targetSquareMask = Utils.lastOne(singlePushNoPromotions);
            singlePushNoPromotions ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask >> 8);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.INVALID_INDEX));
            }
        }

        long singlePushPromotions = singlePush & promotionRank & board.checkRayMask;

        while(singlePushPromotions != 0){
            long targetSquareMask = Utils.lastOne(singlePushPromotions);
            singlePushPromotions ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask >> 8);
            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.QUEEN_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.KNIGHT_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.ROOK_INDEX));
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.BISHOP_INDEX));
            }
        }

        while(doublePushMask != 0){
            long targetSquareMask = Utils.lastOne(doublePushMask);
            doublePushMask ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask >> 16);
            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new server3.move.Move(startSquare,targetSquare, Constants.PAWN_INDEX, Constants.INVALID_INDEX, Constants.INVALID_INDEX));
            }
        }

    }

    public static void generateQueenCaptures(int square,Board board, ArrayList<server3.move.Move> list){
        if((PrecomputedMasks.SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if (board.isCheck){
                return;
            }
            int targetSquare = Long.numberOfTrailingZeros(board.pinRays[square] & board.occupancyBoard[board.opponent]);
            list.add(new server3.move.Move(square,targetSquare, Constants.QUEEN_INDEX,board.indicesBoard[targetSquare], Constants.INVALID_INDEX));
        }else if(board.isDoubleCheck){

        }else {
            long queenCaptures = board.queenRays(square) & board.occupancyBoard[board.opponent]  & board.checkRayMask;
            addMovesToList(queenCaptures, square, Constants.QUEEN_INDEX, board, list);
        }
    }

    public static void generateQueenMoves(int square,Board board,ArrayList<server3.move.Move> list){
        if((PrecomputedMasks.SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            addMovesToList(board.pinRays[square] & ~board.occupancyBoard[board.side],square, Constants.QUEEN_INDEX,  board,list);
        }else if(board.isDoubleCheck){

        }else{
            long queenRays = board.queenRays(square) & ~board.occupancyBoard[board.side]  & board.checkRayMask;
            addMovesToList(queenRays,square, Constants.QUEEN_INDEX,board,list);
        }
    }


    public static void generateBishopCaptures(int square, Board board, ArrayList<server3.move.Move> list){
        if((PrecomputedMasks.SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            long bishopCapture = board.bishopRays(square) & board.occupancyBoard[board.opponent] & board.pinRays[square];
            if(bishopCapture != 0){
                int targetSquare = Long.numberOfTrailingZeros(bishopCapture);
                list.add(new server3.move.Move(square,targetSquare, Constants.BISHOP_INDEX,board.indicesBoard[targetSquare], Constants.INVALID_INDEX));
            }
        }else if(board.isDoubleCheck){

        }else{
            long bishopCaptures = board.bishopRays(square) & board.occupancyBoard[board.opponent] & board.checkRayMask;
            addMovesToList(bishopCaptures,square, Constants.BISHOP_INDEX,board,list);
        }
    }

    public static void generateBishopMoves(int square, Board board, ArrayList<server3.move.Move> list){
        if((PrecomputedMasks.SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            addMovesToList(board.bishopRays(square) & board.pinRays[square] & ~board.occupancyBoard[board.side],square, Constants.BISHOP_INDEX,  board,list);
        }else if(board.isDoubleCheck){

        }else{
            long bishopRays = board.bishopRays(square) & ~board.occupancyBoard[board.side]  & board.checkRayMask;
            addMovesToList(bishopRays,square, Constants.BISHOP_INDEX,board,list);
        }
    }

    public static void generateRookCaptures(int square, Board board, ArrayList<server3.move.Move> list){
        if((PrecomputedMasks.SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            long rookCapture = board.rookRays(square) & board.occupancyBoard[board.opponent] & board.pinRays[square];
            if(rookCapture != 0){
                int targetSquare = Long.numberOfTrailingZeros(rookCapture);
                list.add(new server3.move.Move(square,targetSquare, Constants.ROOK_INDEX,board.indicesBoard[targetSquare], Constants.INVALID_INDEX));
            }
        }else if(board.isDoubleCheck){

        }else{
            long rookCaptures = board.rookRays(square) & board.occupancyBoard[board.opponent] & board.checkRayMask;
            addMovesToList(rookCaptures,square, Constants.ROOK_INDEX,board,list);
        }
    }

    public static void generateRookMoves(int square, Board board, ArrayList<server3.move.Move> list){
        if((PrecomputedMasks.SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            addMovesToList(board.rookRays(square) & board.pinRays[square] & ~board.occupancyBoard[board.side],square, Constants.ROOK_INDEX, board,list);
        }else if(board.isDoubleCheck){

        }else{
            long rookRays = board.rookRays(square) & ~board.occupancyBoard[board.side]  & board.checkRayMask;
            addMovesToList(rookRays,square, Constants.ROOK_INDEX,board,list);
        }
    }

    public static void generateKnightCaptures(int square, Board board, ArrayList<server3.move.Move> list){
        if((PrecomputedMasks.SQUARE_MASKS[square] & board.pinnedPiecesMask) !=0 || board.isDoubleCheck){
            return;
        }

        addMovesToList(PrecomputedMasks.KNIGHT_MOVES[square] & board.occupancyBoard[board.opponent] & board.checkRayMask,square, Constants.KNIGHT_INDEX,board,list);
    }

    public static void generateKnightMoves(int square, Board board, ArrayList<server3.move.Move> list){
        if((PrecomputedMasks.SQUARE_MASKS[square] & board.pinnedPiecesMask) !=0 || board.isDoubleCheck){
            return;
        }

        addMovesToList(PrecomputedMasks.KNIGHT_MOVES[square] & board.checkRayMask & ~board.occupancyBoard[board.side],square, Constants.KNIGHT_INDEX,board,list);
    }

    public static void generateKingCaptures(Board board, ArrayList<server3.move.Move> list){
        addMovesToList(PrecomputedMasks.KING_MOVES[board.kingSquare] & board.safeSquares & board.occupancyBoard[board.opponent], board.kingSquare, Constants.KING_INDEX,board,list );
    }

    public static void generateKingMoves( Board board, ArrayList<server3.move.Move> list){
        addMovesToList(PrecomputedMasks.KING_MOVES[board.kingSquare] & ~board.occupancyBoard[board.side] & board.safeSquares,board.kingSquare, Constants.KING_INDEX,board,list);


        if(board.isCheck || !board.isCastlingPossible){
            return;
        }


        if ((board.castle & Constants.CASTLE_BIT_MASK[board.side][Constants.QUEEN_SIDE_CASTLE_INDEX]) != 0){
            if((PrecomputedMasks.KING_CASTLING_PATH[board.side][Constants.QUEEN_SIDE_CASTLE_INDEX] & (~board.safeSquares | board.occupied)) == 0 && (PrecomputedMasks.SQUARE_MASKS[board.kingSquare-3] & board.occupied) == 0){
                list.add(new server3.move.Move(board.kingSquare,board.kingSquare-2, Constants.KING_INDEX, Constants.INVALID_INDEX, Constants.INVALID_INDEX));
            }
        }

        if((board.castle & Constants.CASTLE_BIT_MASK[board.side][Constants.KING_SIDE_CASTLE_INDEX]) != 0){
            if((PrecomputedMasks.KING_CASTLING_PATH[board.side][Constants.KING_SIDE_CASTLE_INDEX] & (~board.safeSquares | board.occupied))==0){
                list.add(new server3.move.Move(board.kingSquare,board.kingSquare + 2, Constants.KING_INDEX, Constants.INVALID_INDEX, Constants.INVALID_INDEX));
            }
        }
    }


    public static ArrayList<server3.move.Move> generateMoves(Board board){
        ArrayList<server3.move.Move> moves = new ArrayList<>();
        if(board.whiteToMove){
            generateWhitePawnMoves(board, moves);
        }else{
            generateBlackPawnMoves(board,moves);
        }

        generateKingMoves(board,moves);

        long rookBB = board.bitboard[board.side][Constants.ROOK_INDEX];
        while(rookBB != 0){
            long rook = Utils.lastOne(rookBB);
            rookBB ^= rook;
            generateRookMoves(Long.numberOfTrailingZeros(rook),board,moves);
        }

        long bishopBB = board.bitboard[board.side][Constants.BISHOP_INDEX];
        while(bishopBB != 0){
            long bishop = Utils.lastOne(bishopBB);
            bishopBB ^= bishop;
            generateBishopMoves(Long.numberOfTrailingZeros(bishop),board,moves);
        }

        long knightBB = board.bitboard[board.side][Constants.KNIGHT_INDEX];
        while(knightBB != 0){
            long knight = Utils.lastOne(knightBB);
            knightBB ^= knight;
            generateKnightMoves(Long.numberOfTrailingZeros(knight), board, moves);
        }

        long queenBB = board.bitboard[board.side][Constants.QUEEN_INDEX];
        while(queenBB != 0){
            long queen = Utils.lastOne(queenBB);
            queenBB ^= queen;
            generateQueenMoves(Long.numberOfTrailingZeros(queen), board, moves);
        }

        return moves;
    }



    public static ArrayList<server3.move.Move> generateCaptureMoves(Board board){
        ArrayList<Move> moves = new ArrayList<>();
        if(board.whiteToMove){
            generateWhitePawnCaptures(board,moves);
        }else{
            generateBlackPawnCaptures(board, moves);
        }

        generateKingCaptures(board,moves);

        long rookBB = board.bitboard[board.side][Constants.ROOK_INDEX];
        while(rookBB != 0){
            long rook = Utils.lastOne(rookBB);
            rookBB ^= rook;
            generateRookCaptures(Long.numberOfTrailingZeros(rook),board,moves);
        }

        long bishopBB = board.bitboard[board.side][Constants.BISHOP_INDEX];
        while(bishopBB != 0){
            long bishop = Utils.lastOne(bishopBB);
            bishopBB ^= bishop;
            generateBishopCaptures(Long.numberOfTrailingZeros(bishop),board,moves);
        }

        long knightBB = board.bitboard[board.side][Constants.KNIGHT_INDEX];
        while(knightBB != 0){
            long knight = Utils.lastOne(knightBB);
            knightBB ^= knight;
            generateKnightCaptures(Long.numberOfTrailingZeros(knight), board, moves);
        }

        long queenBB = board.bitboard[board.side][Constants.QUEEN_INDEX];
        while(queenBB != 0){
            long queen = Utils.lastOne(queenBB);
            queenBB ^= queen;
            generateQueenCaptures(Long.numberOfTrailingZeros(queen), board, moves);
        }

        return moves;
    }




}
