package server.move;

import static server.utils.Constants.*;
import static server.utils.Utils.*;

import server.board.Board;
import static server.preload.PrecomputedMasks.*;

import java.util.ArrayList;

public class MoveGenerator {


    public static Move fromString(Board board,String moveStr){
        for(Move move:generateMoves(board)){
            if(move.toString().equals(moveStr)){
                return move;
            }
        }
        return null;
    }


    public static void addMovesToList(long moveMask, int startSquare, int pieceIndex,  Board board, ArrayList<Move> list){
        while(moveMask != 0){
            long targetSquareMask = lastOne(moveMask);
            int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
            list.add(new Move(board.side,startSquare,targetSquare,pieceIndex,board.indicesBoard[targetSquare],INVALID_INDEX));
            moveMask ^= targetSquareMask;
        }
    }



    public static void generateWhitePawnCaptures(Board board, ArrayList<Move> list){
        if(board.isDoubleCheck){
            return;
        }

        long pawns = board.bitboard[board.side][PAWN_INDEX];

        long captureLeft = ((pawns >>> 7) & board.occupancyBoard[board.opponent]) & ~FILE_MASKS[0];
        long captureRight = (( pawns >>> 9) & board.occupancyBoard[board.opponent]) & ~FILE_MASKS[7];
        long promotionRank = RANK_MASKS[0] ;

        long captureRightNoPromotions = captureRight & board.checkRayMask & ~promotionRank;

        while(captureRightNoPromotions != 0){
            long targetSquareMask = lastOne(captureRightNoPromotions);
            captureRightNoPromotions ^= targetSquareMask;

            long startSquareMask = targetSquareMask << 9;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],INVALID_INDEX));
            }

        }

        long captureLeftMovesNoPromotions = captureLeft & board.checkRayMask & ~promotionRank;
        while(captureLeftMovesNoPromotions != 0){
            long targetSquareMask = lastOne(captureLeftMovesNoPromotions);
            captureLeftMovesNoPromotions ^= targetSquareMask;

            long startSquareMask = targetSquareMask << 7;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],INVALID_INDEX));
            }


        }


        long captureRightPromotionMoves = captureRight & board.checkRayMask & promotionRank;
        while(captureRightPromotionMoves!=0){
            long targetSquareMask = lastOne(captureRightPromotionMoves);
            captureRightPromotionMoves ^= targetSquareMask;

            long startSquareMask = targetSquareMask << 9;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],QUEEN_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],KNIGHT_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],ROOK_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],BISHOP_INDEX));
            }
        }


        long captureLeftPromotionsMoves = captureLeft & board.checkRayMask & promotionRank;
        while(captureLeftPromotionsMoves != 0){
            long targetSquareMask = lastOne(captureLeftPromotionsMoves);
            captureLeftPromotionsMoves ^= targetSquareMask;

            long startSquareMask = targetSquareMask << 7;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],QUEEN_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],KNIGHT_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],ROOK_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],BISHOP_INDEX));
            }


        }



        if(board.isEPAvailable){
            int epSquare = Long.numberOfTrailingZeros(board.epMask);
            int epRank = epSquare/8;
            long captureRankMask = RANK_MASKS[epRank+1];
            long epCandidatePawns = KING_MOVES[epSquare] & captureRankMask & pawns;

            boolean kingOnCaptureRank = (board.bitboard[board.side][KING_INDEX] & captureRankMask) != 0;

            while(epCandidatePawns != 0){
                long epPawn = lastOne(epCandidatePawns);
                epCandidatePawns ^= epPawn;
                int startSquare = Long.numberOfTrailingZeros(epPawn);

//3rk2r/p1p3pp/8/QpK1Ppq1/3n4/3P4/PP5P/R6R w k f6 0 22
                //6n1/2p4R/p7/1p4k1/2P1pP2/r7/6K1/4R3 b - f3 0 46

                boolean epPossible;
                if(kingOnCaptureRank){


                    long rankNeighbours = (KING_MOVES[startSquare] | SQUARE_MASKS[startSquare]) & board.occupied & captureRankMask;
                    board.occupied &= ~rankNeighbours;
                    epPossible = (board.horizontalRay(board.kingSquare, board.kingSquare > startSquare) & board.straightLineSlidingAttackers) == 0;
                    board.occupied |= rankNeighbours;

//                    if(((board.horizontalRay(startSquare,board.kingSquare < startSquare) & board.occupied) ^ SQUARE_MASKS[board.kingSquare]) == 0){
//                        if(board.kingSquare > startSquare){
//                            epPossible = (firstOne((lowerBits(startSquare) & captureRankMask & board.occupied) ^ board.epMask) & board.straightLineSlidingAttackers) == 0;
//                        }else{
//                            epPossible = (firstOne((higherBits(startSquare) & captureRankMask & board.occupied) ^ board.epMask) & board.straightLineSlidingAttackers) == 0;
//                        }
//                    }else{
//                        epPossible = true;
//                    }
//
//
//                    if(board.kingSquare > startSquare){
//
//
//
//
//                        epPossible = (firstOne(lowerBits(startSquare) & captureRankMask & board.occupied) & board.straightLineSlidingAttackers) == 0;
//                    }else{
//                        epPossible = (lastOne(higherBits(startSquare) & captureRankMask & board.occupied) & board.straightLineSlidingAttackers) == 0;
//                    }
                }else{
                    //epPossible = true;


                    if(board.isCheck){
                        epPossible = (board.checkersMask & board.bitboard[board.opponent][PAWN_INDEX]) != 0;
                    }else{
                        epPossible = true;
                    }
                }

                if(epPossible){

                    long targetSquareMask = PAWN_ATTACKS[board.side][startSquare] & board.epMask & board.pinRays[startSquare];
                    if (targetSquareMask!=0) {
                        int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                        list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,INVALID_INDEX));
                    }
                }

            }

        }


    }


    public static void generateBlackPawnCaptures(Board board, ArrayList<Move> list){
        if(board.isDoubleCheck){
            return;
        }

        long pawns = board.bitboard[board.side][PAWN_INDEX];

        long captureLeft = ((pawns << 9) & board.occupancyBoard[board.opponent]) & ~FILE_MASKS[0];
        long captureRight = (( pawns << 7) & board.occupancyBoard[board.opponent]) & ~FILE_MASKS[7];
        long promotionRank = RANK_MASKS[7] ;

        long captureRightNoPromotions = captureRight & board.checkRayMask & ~promotionRank;

        while(captureRightNoPromotions != 0){
            long targetSquareMask = lastOne(captureRightNoPromotions);
            captureRightNoPromotions ^= targetSquareMask;

            long startSquareMask = targetSquareMask >>> 7;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],INVALID_INDEX));
            }

        }

        long captureLeftMovesNoPromotions = captureLeft & board.checkRayMask & ~promotionRank;
        while(captureLeftMovesNoPromotions != 0){
            long targetSquareMask = lastOne(captureLeftMovesNoPromotions);
            captureLeftMovesNoPromotions ^= targetSquareMask;

            long startSquareMask = targetSquareMask >>> 9;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],INVALID_INDEX));
            }


        }


        long captureRightPromotionMoves = captureRight & board.checkRayMask & promotionRank;
        while(captureRightPromotionMoves!=0){
            long targetSquareMask = lastOne(captureRightPromotionMoves);
            captureRightPromotionMoves ^= targetSquareMask;

            long startSquareMask = targetSquareMask >>> 7;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],QUEEN_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],KNIGHT_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],ROOK_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],BISHOP_INDEX));
            }
        }


        long captureLeftPromotionsMoves = captureLeft & board.checkRayMask & promotionRank;
        while(captureLeftPromotionsMoves != 0){
            long targetSquareMask = lastOne(captureLeftPromotionsMoves);
            captureLeftPromotionsMoves ^= targetSquareMask;

            long startSquareMask = targetSquareMask >>> 9;
            int startSquare = Long.numberOfTrailingZeros(startSquareMask);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],QUEEN_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],KNIGHT_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],ROOK_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,board.indicesBoard[targetSquare],BISHOP_INDEX));
            }


        }



        if(board.isEPAvailable){
            int epSquare = Long.numberOfTrailingZeros(board.epMask);
            int epRank = epSquare/8;
            long captureRankMask = RANK_MASKS[epRank-1];
            long epCandidatePawns = KING_MOVES[epSquare] & captureRankMask & pawns;

            boolean kingOnCaptureRank = (board.bitboard[board.side][KING_INDEX] & captureRankMask) != 0;

            while(epCandidatePawns != 0){
                long epPawn = lastOne(epCandidatePawns);
                epCandidatePawns ^= epPawn;
                int startSquare = Long.numberOfTrailingZeros(epPawn);



                boolean epPossible;
                if(kingOnCaptureRank){


                    long rankNeighbours = (KING_MOVES[startSquare] | SQUARE_MASKS[startSquare]) & board.occupied & captureRankMask;
                    board.occupied &= ~rankNeighbours;
                    epPossible = (board.horizontalRay(board.kingSquare, board.kingSquare > startSquare) & board.straightLineSlidingAttackers) == 0;
                    board.occupied |= rankNeighbours;

//                    if(((board.horizontalRay(startSquare,board.kingSquare < startSquare) & board.occupied) ^ SQUARE_MASKS[board.kingSquare]) == 0){
//                        if(board.kingSquare > startSquare){
//                            epPossible = (firstOne((lowerBits(startSquare) & captureRankMask & board.occupied) ^ board.epMask) & board.straightLineSlidingAttackers) == 0;
//                        }else{
//                            epPossible = (firstOne((higherBits(startSquare) & captureRankMask & board.occupied) ^ board.epMask) & board.straightLineSlidingAttackers) == 0;
//                        }
//                    }else{
//                        epPossible = true;
//                    }
//
//
//                    if(board.kingSquare > startSquare){
//
//
//
//
//                        epPossible = (firstOne(lowerBits(startSquare) & captureRankMask & board.occupied) & board.straightLineSlidingAttackers) == 0;
//                    }else{
//                        epPossible = (lastOne(higherBits(startSquare) & captureRankMask & board.occupied) & board.straightLineSlidingAttackers) == 0;
//                    }
                }else{
                    if(board.isCheck){
                        epPossible = (board.checkersMask & board.bitboard[board.opponent][PAWN_INDEX]) != 0;
                    }else{
                        epPossible = true;
                    }
                }

                if(epPossible){

                    long targetSquareMask = PAWN_ATTACKS[board.side][startSquare] & board.epMask & board.pinRays[startSquare];
                    if (targetSquareMask!=0) {
                        int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                        list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,INVALID_INDEX));
                    }
                }

            }

        }
    }


    public static void generateWhitePawnMoves(Board board, ArrayList<Move> list){
        if(board.isDoubleCheck){
            return;
        }
        generateWhitePawnCaptures(board, list);
        long pawns = board.bitboard[board.side][PAWN_INDEX];
        long promotionRank = RANK_MASKS[0];

        long singlePush = pawns >>> 8 & board.emptySquaresMask;

        long doublePushCandidates = pawns & RANK_MASKS[6] & (singlePush << 8);

        long doublePushMask = doublePushCandidates >>> 16 & board.emptySquaresMask & board.checkRayMask;

        long singlePushNoPromotions = singlePush & ~promotionRank & board.checkRayMask;
        while(singlePushNoPromotions != 0){
            long targetSquareMask = lastOne(singlePushNoPromotions);
            singlePushNoPromotions ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask << 8);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,INVALID_INDEX));
            }
        }

        long singlePushPromotions = singlePush & promotionRank & board.checkRayMask;

        while(singlePushPromotions != 0){
            long targetSquareMask = lastOne(singlePushPromotions);
            singlePushPromotions ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask << 8);
            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,QUEEN_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,KNIGHT_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,ROOK_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,BISHOP_INDEX));
            }
        }

        while(doublePushMask != 0){
            long targetSquareMask = lastOne(doublePushMask);
            doublePushMask ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask << 16);
            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,INVALID_INDEX));
            }
        }

    }

    public static void generateBlackPawnMoves(Board board, ArrayList<Move> list){
        if(board.isDoubleCheck){
            return;
        }
        generateBlackPawnCaptures(board, list);
        long pawns = board.bitboard[board.side][PAWN_INDEX];
        long promotionRank = RANK_MASKS[7];

        long singlePush = pawns << 8 & board.emptySquaresMask;

        long doublePushCandidates = pawns & RANK_MASKS[1] & (singlePush >> 8);

        long doublePushMask = doublePushCandidates << 16 & board.emptySquaresMask & board.checkRayMask;

        long singlePushNoPromotions = singlePush & ~promotionRank & board.checkRayMask;
        while(singlePushNoPromotions != 0){
            long targetSquareMask = lastOne(singlePushNoPromotions);
            singlePushNoPromotions ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask >> 8);

            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,INVALID_INDEX));
            }
        }

        long singlePushPromotions = singlePush & promotionRank & board.checkRayMask;

        while(singlePushPromotions != 0){
            long targetSquareMask = lastOne(singlePushPromotions);
            singlePushPromotions ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask >> 8);
            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,QUEEN_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,KNIGHT_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,ROOK_INDEX));
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,BISHOP_INDEX));
            }
        }

        while(doublePushMask != 0){
            long targetSquareMask = lastOne(doublePushMask);
            doublePushMask ^= targetSquareMask;
            int startSquare = Long.numberOfTrailingZeros(targetSquareMask >> 16);
            if((targetSquareMask & board.pinRays[startSquare]) != 0){
                int targetSquare = Long.numberOfTrailingZeros(targetSquareMask);
                list.add(new Move(board.side,startSquare,targetSquare,PAWN_INDEX,INVALID_INDEX,INVALID_INDEX));
            }
        }

    }

    public static void generateQueenCaptures(int square,Board board, ArrayList<Move> list){
        if((SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if (board.isCheck){
                return;
            }
            int targetSquare = Long.numberOfTrailingZeros(board.pinRays[square] & board.occupancyBoard[board.opponent]);
            list.add(new Move(board.side,square,targetSquare,QUEEN_INDEX,board.indicesBoard[targetSquare],INVALID_INDEX));
        }else if(board.isDoubleCheck){

        }else {
            long queenCaptures = board.queenRays(square) & board.occupancyBoard[board.opponent]  & board.checkRayMask;
            addMovesToList(queenCaptures, square, QUEEN_INDEX, board, list);
        }
    }

    public static void generateQueenMoves(int square,Board board,ArrayList<Move> list){
        if((SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            addMovesToList(board.pinRays[square] & ~board.occupancyBoard[board.side],square,QUEEN_INDEX,  board,list);
        }else if(board.isDoubleCheck){

        }else{
            long queenRays = board.queenRays(square) & ~board.occupancyBoard[board.side]  & board.checkRayMask;
            addMovesToList(queenRays,square,QUEEN_INDEX,board,list);
        }
    }


    public static void generateBishopCaptures(int square, Board board, ArrayList<Move> list){
        if((SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            long bishopCapture = board.bishopRays(square) & board.occupancyBoard[board.opponent] & board.pinRays[square];
            if(bishopCapture != 0){
                int targetSquare = Long.numberOfTrailingZeros(bishopCapture);
                list.add(new Move(board.side,square,targetSquare,BISHOP_INDEX,board.indicesBoard[targetSquare], INVALID_INDEX));
            }
        }else if(board.isDoubleCheck){

        }else{
            long bishopCaptures = board.bishopRays(square) & board.occupancyBoard[board.opponent] & board.checkRayMask;
            addMovesToList(bishopCaptures,square,BISHOP_INDEX,board,list);
        }
    }

    public static void generateBishopMoves(int square, Board board, ArrayList<Move> list){
        if((SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            addMovesToList(board.bishopRays(square) & board.pinRays[square] & ~board.occupancyBoard[board.side],square,BISHOP_INDEX,  board,list);
        }else if(board.isDoubleCheck){

        }else{
            long bishopRays = board.bishopRays(square) & ~board.occupancyBoard[board.side]  & board.checkRayMask;
            addMovesToList(bishopRays,square,BISHOP_INDEX,board,list);
        }
    }

    public static void generateRookCaptures(int square, Board board, ArrayList<Move> list){
        if((SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            long rookCapture = board.rookRays(square) & board.occupancyBoard[board.opponent] & board.pinRays[square];
            if(rookCapture != 0){
                int targetSquare = Long.numberOfTrailingZeros(rookCapture);
                list.add(new Move(board.side,square,targetSquare,ROOK_INDEX,board.indicesBoard[targetSquare], INVALID_INDEX));
            }
        }else if(board.isDoubleCheck){

        }else{
            long rookCaptures = board.rookRays(square) & board.occupancyBoard[board.opponent] & board.checkRayMask;
            addMovesToList(rookCaptures,square,ROOK_INDEX,board,list);
        }
    }

    public static void generateRookMoves(int square, Board board, ArrayList<Move> list){
        if((SQUARE_MASKS[square] & board.pinnedPiecesMask) != 0){
            if(board.isCheck){
                return;
            }
            addMovesToList(board.rookRays(square) & board.pinRays[square] & ~board.occupancyBoard[board.side],square,ROOK_INDEX, board,list);
        }else if(board.isDoubleCheck){

        }else{
            long rookRays = board.rookRays(square) & ~board.occupancyBoard[board.side]  & board.checkRayMask;
            addMovesToList(rookRays,square,ROOK_INDEX,board,list);
        }
    }

    public static void generateKnightCaptures(int square, Board board, ArrayList<Move> list){
        if((SQUARE_MASKS[square] & board.pinnedPiecesMask) !=0 || board.isDoubleCheck){
            return;
        }

        addMovesToList(KNIGHT_MOVES[square] & board.occupancyBoard[board.opponent] & board.checkRayMask,square,KNIGHT_INDEX,board,list);
    }

    public static void generateKnightMoves(int square, Board board, ArrayList<Move> list){
        if((SQUARE_MASKS[square] & board.pinnedPiecesMask) !=0 || board.isDoubleCheck){
            return;
        }

        addMovesToList(KNIGHT_MOVES[square] & board.checkRayMask & ~board.occupancyBoard[board.side],square, KNIGHT_INDEX,board,list);
    }

    public static void generateKingCaptures(Board board, ArrayList<Move> list){
        addMovesToList(KING_MOVES[board.kingSquare] & board.safeSquares & board.occupancyBoard[board.opponent], board.kingSquare, KING_INDEX,board,list );
    }

    public static void generateKingMoves( Board board, ArrayList<Move> list){
        addMovesToList(KING_MOVES[board.kingSquare] & ~board.occupancyBoard[board.side] & board.safeSquares,board.kingSquare,KING_INDEX,board,list);


        if(board.isCheck || !board.isCastlingPossible){
            return;
        }


        if ((board.castle & CASTLE_BIT_MASK[board.side][QUEEN_SIDE_CASTLE_INDEX]) != 0){
            if((KING_CASTLING_PATH[board.side][QUEEN_SIDE_CASTLE_INDEX] & (~board.safeSquares | board.occupied)) == 0 && (SQUARE_MASKS[board.kingSquare-3] & board.occupied) == 0){
                list.add(new Move(board.side,board.kingSquare,board.kingSquare-2,KING_INDEX,INVALID_INDEX,INVALID_INDEX));
            }
        }

        if((board.castle & CASTLE_BIT_MASK[board.side][KING_SIDE_CASTLE_INDEX]) != 0){
            if((KING_CASTLING_PATH[board.side][KING_SIDE_CASTLE_INDEX] & (~board.safeSquares | board.occupied))==0){
                list.add(new Move(board.side,board.kingSquare,board.kingSquare + 2,KING_INDEX,INVALID_INDEX,INVALID_INDEX));
            }
        }
    }


    public static ArrayList<Move> generateMoves(Board board){
        ArrayList<Move> moves = new ArrayList<>();
        if(board.whiteToMove){
            generateWhitePawnMoves(board, moves);
        }else{
            generateBlackPawnMoves(board,moves);
        }

        generateKingMoves(board,moves);

        long rookBB = board.bitboard[board.side][ROOK_INDEX];
        while(rookBB != 0){
            long rook = lastOne(rookBB);
            rookBB ^= rook;
            generateRookMoves(Long.numberOfTrailingZeros(rook),board,moves);
        }

        long bishopBB = board.bitboard[board.side][BISHOP_INDEX];
        while(bishopBB != 0){
            long bishop = lastOne(bishopBB);
            bishopBB ^= bishop;
            generateBishopMoves(Long.numberOfTrailingZeros(bishop),board,moves);
        }

        long knightBB = board.bitboard[board.side][KNIGHT_INDEX];
        while(knightBB != 0){
            long knight = lastOne(knightBB);
            knightBB ^= knight;
            generateKnightMoves(Long.numberOfTrailingZeros(knight), board, moves);
        }

        long queenBB = board.bitboard[board.side][QUEEN_INDEX];
        while(queenBB != 0){
            long queen = lastOne(queenBB);
            queenBB ^= queen;
            generateQueenMoves(Long.numberOfTrailingZeros(queen), board, moves);
        }

        return moves;
    }



    public static ArrayList<Move> generateCaptureMoves(Board board){
        ArrayList<Move> moves = new ArrayList<>();
        if(board.whiteToMove){
            generateWhitePawnCaptures(board,moves);
        }else{
            generateBlackPawnCaptures(board, moves);
        }

        generateKingCaptures(board,moves);

        long rookBB = board.bitboard[board.side][ROOK_INDEX];
        while(rookBB != 0){
            long rook = lastOne(rookBB);
            rookBB ^= rook;
            generateRookCaptures(Long.numberOfTrailingZeros(rook),board,moves);
        }

        long bishopBB = board.bitboard[board.side][BISHOP_INDEX];
        while(bishopBB != 0){
            long bishop = lastOne(bishopBB);
            bishopBB ^= bishop;
            generateBishopCaptures(Long.numberOfTrailingZeros(bishop),board,moves);
        }

        long knightBB = board.bitboard[board.side][KNIGHT_INDEX];
        while(knightBB != 0){
            long knight = lastOne(knightBB);
            knightBB ^= knight;
            generateKnightCaptures(Long.numberOfTrailingZeros(knight), board, moves);
        }

        long queenBB = board.bitboard[board.side][QUEEN_INDEX];
        while(queenBB != 0){
            long queen = lastOne(queenBB);
            queenBB ^= queen;
            generateQueenCaptures(Long.numberOfTrailingZeros(queen), board, moves);
        }

        return moves;
    }




}
