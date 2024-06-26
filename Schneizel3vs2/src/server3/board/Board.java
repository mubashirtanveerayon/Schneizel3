package server3.board;

import server3.move.Move;
import server3.preload.PrecomputedMasks;
import server3.utils.Constants;
import server3.utils.Utils;

import java.util.Arrays;

public class Board {


    public int opponent, side, kingFile,kingRank;

    public byte castle;
    public long hash, occupied, emptySquaresMask, safeSquares, checkersMask, pinnedPiecesMask, checkRayMask, epMask, straightLineSlidingAttackers, diagonalSlidingAttackers;


    public boolean isSingleCheck, isDoubleCheck, isCheck, isCastlingPossible, whiteToMove, isEPAvailable;

    public long[] occupancyBoard;

    public long[] pinRays;

    public int kingSquare;

    public int[] indicesBoard,nPieces;

    private long[] possibleAttackRays;

    public long[][] bitboard;
    public int halfMoveClock, fullMoveClock;

    public Board(){
        init(Constants.STARTING_FEN);
    }


    public Board(String fen){
        init(fen);
    }


    public Board(Board board){
        side = board.side;
        opponent = board.opponent;
        kingFile = board.kingFile;
        kingRank = board.kingRank;
        kingSquare = board.kingSquare;
        halfMoveClock = board.halfMoveClock;
        fullMoveClock = board.fullMoveClock;
        castle = board.castle;
        occupied = board.occupied;
        emptySquaresMask = board.emptySquaresMask;
        safeSquares = board.safeSquares;
        checkersMask = board.checkersMask;
        pinnedPiecesMask = board.pinnedPiecesMask;
        checkRayMask = board.checkRayMask;
        epMask = board.epMask;
        straightLineSlidingAttackers = board.straightLineSlidingAttackers;
        diagonalSlidingAttackers = board.diagonalSlidingAttackers;
        isSingleCheck = board.isSingleCheck;
        isDoubleCheck = board.isDoubleCheck;
        isCheck = board.isCheck;
        isCastlingPossible = board.isCastlingPossible;
        whiteToMove = board.whiteToMove;
        isEPAvailable = board.isEPAvailable;

        indicesBoard = new int[64];
        System.arraycopy(board.indicesBoard,0,indicesBoard,0,64);
        occupancyBoard = new long[2];
        System.arraycopy(board.occupancyBoard,0,occupancyBoard,0,2);
        pinRays = new long[64];
        System.arraycopy(board.pinRays,0,pinRays,0,64);
        possibleAttackRays = new long[2];
        bitboard = new long[2][6];
        for(int i=0;i<2;i++){
            System.arraycopy(board.bitboard[i],0,bitboard[i],0,6);
        }
        hash = board.hash;
        nPieces = new int[2];
        System.arraycopy(board.nPieces,0,nPieces,0,2);
    }


    private void init(String fen){
        bitboard = Utils.setupBitboard(fen);
        String[] fenParts = fen.split(" ");

        side = fenParts[1].equalsIgnoreCase("w") ? Constants.WHITE : Constants.BLACK;
        opponent = side ^ 1;

        castle = 0;
        if (fenParts[2].contains("K")){
            castle |= Constants.CASTLE_BIT_MASK[Constants.WHITE][Constants.KING_SIDE_CASTLE_INDEX];
        }
        if (fenParts[2].contains("Q")){
            castle |= Constants.CASTLE_BIT_MASK[Constants.WHITE][Constants.QUEEN_SIDE_CASTLE_INDEX];
        }
        if (fenParts[2].contains("k")){
            castle |= Constants.CASTLE_BIT_MASK[Constants.BLACK][Constants.KING_SIDE_CASTLE_INDEX];
        }
        if (fenParts[2].contains("q")){
            castle |= Constants.CASTLE_BIT_MASK[Constants.BLACK][Constants.QUEEN_SIDE_CASTLE_INDEX];
        }

        if (fenParts[3].equals("-")){
            epMask = 0;
        }else{
            epMask = PrecomputedMasks.SQUARE_MASKS[Utils.squareIndex(fenParts[3])];
        }

        halfMoveClock = Integer.parseInt(fenParts[4]);
        fullMoveClock = Integer.parseInt(fenParts[5]);

        pinRays = new long[64];
        indicesBoard = new int[64];
        occupancyBoard = new long[2];

        Arrays.fill(indicesBoard, Constants.INVALID_INDEX);

        for (int side=0;side<2;side++) {
            for (int pieceIndex = 0; pieceIndex < 6; pieceIndex++) {
                long pieceBitboard = bitboard[side][pieceIndex];
                occupancyBoard[side] |= pieceBitboard;
                while (pieceBitboard != 0) {
                    long piece = pieceBitboard & -pieceBitboard;
                    indicesBoard[Long.numberOfTrailingZeros(piece)] = pieceIndex;
                    pieceBitboard ^= piece;
                }
            }
        }

        possibleAttackRays = new long[2];

        nPieces = new int[2];

        update();






    }


    public void update(){
        Arrays.fill(pinRays, Constants.BOARD_MASK);
        occupied = occupancyBoard[side] | occupancyBoard[opponent];
        emptySquaresMask = ~occupied;
        checkRayMask = Constants.BOARD_MASK;
        checkersMask = 0;
        pinnedPiecesMask = 0;
        kingSquare = Long.numberOfTrailingZeros(bitboard[side][Constants.KING_INDEX]);
        kingFile = kingSquare % 8;
        kingRank = kingSquare/8;

        straightLineSlidingAttackers = bitboard[opponent][Constants.QUEEN_INDEX] | bitboard[opponent][Constants.ROOK_INDEX] ;
        diagonalSlidingAttackers = bitboard[opponent][Constants.BISHOP_INDEX] | bitboard[opponent][Constants.QUEEN_INDEX];



        safeSquares = ~generateDangerMask();
        detectChecks();
        detectPins();

        isSingleCheck = isSingleCheck();
        isDoubleCheck = isDoubleCheck();
        isCheck = isCheck();
        isCastlingPossible = isCastlingPossible();
        whiteToMove = whiteToMove();
        isEPAvailable = isEPAvailable();
        hash = hash();

        nPieces[side] = Long.bitCount(occupancyBoard[side]);
        nPieces[opponent] = Long.bitCount(occupancyBoard[opponent]);


        //System.out.println(Utils.getBitboardVisual(checkRayMask));
    }



    private void detectPins() {
        long kingLowerBits = Utils.lowerBits(kingSquare);
        long kingHigherBits = Utils.higherBits(kingSquare);

        long kingRankBB = occupied & PrecomputedMasks.RANK_MASKS[kingRank];
        long kingRankRightBB = kingLowerBits & kingRankBB;

        long possibleAlly = Utils.firstOne(kingRankRightBB);
        if ((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = Utils.firstOne(kingRankRightBB ^ possibleAlly);
            if((possibleAttacker & straightLineSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = horizontalRay(kingSquare, true);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingRankLeftBB = kingHigherBits & kingRankBB;
        possibleAlly = Utils.lastOne(kingRankLeftBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = Utils.lastOne(kingRankLeftBB ^ possibleAlly);
            if((possibleAttacker & straightLineSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = horizontalRay(kingSquare, false);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingFileBB = occupied & PrecomputedMasks.FILE_MASKS[kingFile];

        long kingFileDownBB = kingFileBB & kingLowerBits;
        possibleAlly = Utils.firstOne(kingFileDownBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = Utils.firstOne(kingFileDownBB ^ possibleAlly);
            if((possibleAttacker & straightLineSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = verticalRay(kingSquare, false);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingFileUpBB = kingFileBB & kingHigherBits;
        possibleAlly = Utils.lastOne(kingFileUpBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = Utils.lastOne(kingFileUpBB ^ possibleAlly);
            if ((possibleAttacker & straightLineSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = verticalRay(kingSquare, true);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long diagonalBB = PrecomputedMasks.DIAGONAL_MASKS[kingFile+kingRank] & occupied;

        long kingDiagonalUpBB = kingHigherBits & diagonalBB;
        possibleAlly = Utils.lastOne(kingDiagonalUpBB);
        if((possibleAlly & occupancyBoard [side])!= 0){
            long possibleAttacker = Utils.lastOne(kingDiagonalUpBB ^ possibleAlly);
            if((possibleAttacker & diagonalSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = diagonalRay(kingSquare, true,true);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingDiagonalDownBB = kingLowerBits & diagonalBB;

        possibleAlly = Utils.firstOne(kingDiagonalDownBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = Utils.firstOne(kingDiagonalDownBB ^ possibleAlly);

            if((possibleAttacker & diagonalSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = diagonalRay(kingSquare, false,false);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long antiDiagonalBB = PrecomputedMasks.ANTI_DIAGONAL_MASKS[kingRank+7-kingFile] & occupied;

        long kingAntiDiagonalUpBB = kingHigherBits & antiDiagonalBB;

        possibleAlly = Utils.lastOne(kingAntiDiagonalUpBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = Utils.lastOne(kingAntiDiagonalUpBB ^ possibleAlly);
            if((possibleAttacker & diagonalSlidingAttackers ) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = diagonalRay(kingSquare, true,false);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingAntiDiagonalDownBB = kingLowerBits & antiDiagonalBB;
        possibleAlly = Utils.firstOne(kingAntiDiagonalDownBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = Utils.firstOne(kingAntiDiagonalDownBB ^ possibleAlly);
            if((possibleAttacker & diagonalSlidingAttackers ) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = diagonalRay(kingSquare, false,true);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

    }

    private void detectChecks() {
        long rookRay = rookRays(kingSquare);
        if((rookRay & straightLineSlidingAttackers) != 0){

            possibleAttackRays[0] = rookRay & PrecomputedMasks.RANK_MASKS[kingRank];
            possibleAttackRays[1] = rookRay & PrecomputedMasks.FILE_MASKS[kingFile];

            for (long ray : possibleAttackRays){
                long possibleAttacker = Utils.firstOne(ray) | Utils.lastOne(ray);
                while(possibleAttacker != 0){
                    long attacker = possibleAttacker & -possibleAttacker;
                    if((attacker & straightLineSlidingAttackers) != 0){
                        checkersMask |= attacker;
                        break;
                    }
                    possibleAttacker ^= attacker;
                }

            }

        }

        long bishopRay = bishopRays(kingSquare);
//        System.out.println("h");
//        System.out.println(Utils.getBitboardVisual(bishopRay));
        if((bishopRay & diagonalSlidingAttackers) != 0){
            possibleAttackRays[0] = bishopRay & PrecomputedMasks.DIAGONAL_MASKS[kingFile+kingRank];
            possibleAttackRays[1] = bishopRay & PrecomputedMasks.ANTI_DIAGONAL_MASKS[kingRank+7-kingFile];

            for (long ray : possibleAttackRays){
                long possibleAttacker = Utils.firstOne(ray) | Utils.lastOne(ray);
                while(possibleAttacker != 0){
                    long attacker = possibleAttacker & -possibleAttacker;
                    if((attacker & diagonalSlidingAttackers) != 0){
                        checkersMask |= attacker;
                        break;
                    }
                    possibleAttacker ^= attacker;
                }

            }
        }

        long possibleOpponentPawnCheckers = bitboard[opponent][Constants.PAWN_INDEX] & PrecomputedMasks.KING_MOVES[kingSquare] & ~PrecomputedMasks.FILE_MASKS[kingFile] & ~PrecomputedMasks.RANK_MASKS[kingRank];

        while(possibleOpponentPawnCheckers != 0){
            long possiblePawn = Utils.lastOne(possibleOpponentPawnCheckers);
            if((PrecomputedMasks.PAWN_ATTACKS[opponent][Long.numberOfTrailingZeros(possiblePawn)] & bitboard[side][Constants.KING_INDEX]) != 0){
                checkersMask |= possiblePawn;
                break;
            }
            possibleOpponentPawnCheckers ^= possiblePawn;
        }


        checkersMask |= PrecomputedMasks.KNIGHT_MOVES[kingSquare] & bitboard[opponent][Constants.KNIGHT_INDEX];

        if (isSingleCheck()){
            int checkerSquare = Long.numberOfTrailingZeros(checkersMask);
            int checkerFile = checkerSquare%8, checkerRank = checkerSquare/8;

            if (kingFile == checkerFile){
                  checkRayMask = verticalRay(kingSquare, checkerRank > kingRank);
            }else if(kingRank == checkerRank){
                checkRayMask = horizontalRay(kingSquare, checkerFile < kingFile);
            }else if(Utils.onBishopRay(kingSquare, checkerSquare)){
                checkRayMask = diagonalRay(kingSquare, checkerRank > kingRank, checkerFile < kingFile);
                if (isEPAvailable()){
                    if((checkersMask & bitboard[opponent][Constants.PAWN_INDEX]) != 0) {
                        checkRayMask |= epMask;
                    }else{
                        epMask = 0;
                    }
                }
            }else{
                checkRayMask = checkersMask;
            }
        }

    }



    private long generateDangerMask() {
        long dangerMask = 0;

        occupied ^= bitboard[side][Constants.KING_INDEX];

        for (int pieceIndex=0 ; pieceIndex<6;pieceIndex ++){
            long enemyPieceBitboard = bitboard[opponent][pieceIndex];


            if(pieceIndex == Constants.PAWN_INDEX){
                dangerMask |= PrecomputedMasks.maskPawnAttacks(opponent,enemyPieceBitboard);
                continue;
            }else if(pieceIndex == Constants.KNIGHT_INDEX){
                dangerMask |= PrecomputedMasks.maskKnightMoves(enemyPieceBitboard);
                continue;
            }else if(pieceIndex == Constants.KING_INDEX){
                dangerMask |= PrecomputedMasks.maskKingMoves(enemyPieceBitboard);
                continue;
            }





            while(enemyPieceBitboard != 0){
                long enemyPiece = Utils.lastOne(enemyPieceBitboard);
                enemyPieceBitboard ^= enemyPiece;

                int enemyPieceSquare = Long.numberOfTrailingZeros(enemyPiece);

                switch(pieceIndex){
//                    case PAWN_INDEX:
//                        dangerMask |= PAWN_ATTACKS[opponent][enemyPieceSquare];
//                        break;
//                    case KNIGHT_INDEX:
//                        dangerMask |= KNIGHT_MOVES[enemyPieceSquare];
//                        break;
//                    case KING_INDEX:
//                        dangerMask |= KING_MOVES[enemyPieceSquare];
//                        break;
                    case Constants.BISHOP_INDEX:
                        dangerMask |= bishopRays(enemyPieceSquare);
                        break;
                    case Constants.ROOK_INDEX:
                        dangerMask |= rookRays(enemyPieceSquare);
                        break;
                    case Constants.QUEEN_INDEX:
                        dangerMask |= queenRays(enemyPieceSquare);
                        break;
                }
            }

        }

        occupied ^= bitboard[side][Constants.KING_INDEX];

        return dangerMask;
    }

    public long horizontalRay(int square, boolean right){
        long slider = PrecomputedMasks.SQUARE_MASKS[square];
        long rankMask = PrecomputedMasks.RANK_MASKS[square/8];
        if (right){
            return (occupied ^ Long.reverse(Long.reverse(occupied) - 2 * Long.reverse(slider))) & rankMask;
        }else{
            return (occupied ^ (occupied - 2 * slider)) & rankMask;
        }
    }

    public long verticalRay(int square, boolean top){
        long slider = PrecomputedMasks.SQUARE_MASKS[square];
        long fileMask = PrecomputedMasks.FILE_MASKS[square%8];

        long ray = ((occupied & fileMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & fileMask) - 2  * Long.reverse(slider));
        ray &= fileMask;

        if (top){
            return ray & Utils.higherBits(square);
        }else{
            return ray & Utils.lowerBits(square);
        }
    }

    public long diagonalRay(int square, boolean top, boolean right){
        long slider = PrecomputedMasks.SQUARE_MASKS[square];
        int file = square%8;
        int rank = square/8;

        if (top){
            if(right){
                long diagonalMask = PrecomputedMasks.DIAGONAL_MASKS[file+rank];
                long diagonalRay = (((occupied & diagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & diagonalMask) - (2 * Long.reverse(slider)))) & diagonalMask;
                return diagonalRay & Utils.higherBits(square);
            }else{
                long antiDiagonalMask = PrecomputedMasks.ANTI_DIAGONAL_MASKS[rank+7-file];
                long antiDiagonalRay = (((occupied & antiDiagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & antiDiagonalMask) - (2 * Long.reverse(slider)))) & antiDiagonalMask;
                return antiDiagonalRay & Utils.higherBits(square);
            }
        }else{
            if(right){
                long antiDiagonalMask = PrecomputedMasks.ANTI_DIAGONAL_MASKS[rank+7-file];
                long antiDiagonalRay = (((occupied & antiDiagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & antiDiagonalMask) - (2 * Long.reverse(slider)))) & antiDiagonalMask;
                return antiDiagonalRay & Utils.lowerBits(square);
            }else{
                long diagonalMask = PrecomputedMasks.DIAGONAL_MASKS[file+rank];
                long diagonalRay = (((occupied & diagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & diagonalMask) - (2 * Long.reverse(slider)))) & diagonalMask;
                return diagonalRay & Utils.lowerBits(square);
            }
        }
    }

    public long rookRays(int square){
        long slider = PrecomputedMasks.SQUARE_MASKS[square];
        int file = square%8, rank = square / 8;

        return (((occupied - 2 * slider ^ Long.reverse(Long.reverse(occupied) - 2 * Long.reverse(slider)))) & PrecomputedMasks.RANK_MASKS[rank]) | ((((occupied & PrecomputedMasks.FILE_MASKS[file]) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & PrecomputedMasks.FILE_MASKS[file]) - (2 * Long.reverse(slider)))) & PrecomputedMasks.FILE_MASKS[file]);
    }

    public long bishopRays(int square){
        long slider = PrecomputedMasks.SQUARE_MASKS[square];
        int file = square%8, rank = square/8;

        long diagonalMask = PrecomputedMasks.DIAGONAL_MASKS[file+rank];
        long antiDiagonalMask = PrecomputedMasks.ANTI_DIAGONAL_MASKS[rank+7-file];

        long diagonalRay = (((occupied & diagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & diagonalMask) - (2 * Long.reverse(slider)))) & diagonalMask;
        long antiDiagonalRay = (((occupied & antiDiagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & antiDiagonalMask) - (2 * Long.reverse(slider)))) & antiDiagonalMask;

        return antiDiagonalRay | diagonalRay;
    }



    public void makeMove(Move move){
        long moveMask = PrecomputedMasks.SQUARE_MASKS[move.startSquare] | PrecomputedMasks.SQUARE_MASKS[move.targetSquare];


        bitboard[side][move.pieceIndex] ^= moveMask;

        occupancyBoard[side] ^= moveMask;
        indicesBoard[move.startSquare] = Constants.INVALID_INDEX;
        indicesBoard[move.targetSquare] = move.pieceIndex;

        if(move.isCapture){
            halfMoveClock = 0;
            nPieces[opponent]--;
            bitboard[opponent][move.capturePieceIndex] ^= PrecomputedMasks.SQUARE_MASKS[move.targetSquare];
            occupancyBoard[opponent] ^= PrecomputedMasks.SQUARE_MASKS[move.targetSquare];
            if (move.capturePieceIndex == Constants.ROOK_INDEX){
                int targetFile = move.targetSquare%8;

                if (targetFile == 0 && (castle & Constants.CASTLE_BIT_MASK[opponent][Constants.QUEEN_SIDE_CASTLE_INDEX]) != 0){
                    int opponentCastlingRank = whiteToMove() ? 0 : 7;
                    if(move.targetSquare/8 == opponentCastlingRank){
                        castle ^= Constants.CASTLE_BIT_MASK[opponent][Constants.QUEEN_SIDE_CASTLE_INDEX];
                    }
                }else if(targetFile == 7 && (castle & Constants.CASTLE_BIT_MASK[opponent][Constants.KING_SIDE_CASTLE_INDEX]) != 0){
                    int opponentCastlingRank = whiteToMove() ? 0 : 7;
                    if(move.targetSquare/8 == opponentCastlingRank){
                        castle ^= Constants.CASTLE_BIT_MASK[opponent][Constants.KING_SIDE_CASTLE_INDEX];
                    }
                }
            }
        }else{
            halfMoveClock ++;
        }

        if (move.isPawnMove){
            halfMoveClock = 0;
            if (move.isPromotion){
                bitboard[side][Constants.PAWN_INDEX] ^= PrecomputedMasks.SQUARE_MASKS[move.targetSquare];
                bitboard[side][move.promotionPieceIndex] ^= PrecomputedMasks.SQUARE_MASKS[move.targetSquare];
                indicesBoard[move.targetSquare] = move.promotionPieceIndex;
                epMask = 0;
            }else if(move.isEP){
                long captureSquareMask = whiteToMove() ? epMask << 8:epMask >>> 8;
                bitboard[opponent][Constants.PAWN_INDEX] ^= captureSquareMask;
                occupancyBoard[opponent] ^= captureSquareMask;
                indicesBoard[Long.numberOfTrailingZeros(captureSquareMask)] = Constants.INVALID_INDEX;
                epMask = 0;
                nPieces[opponent]--;
            }else{
                if(Math.abs(move.targetSquare-move.startSquare) == 16 && (PrecomputedMasks.KING_MOVES[move.targetSquare] & PrecomputedMasks.RANK_MASKS[move.targetSquare/8] & bitboard[opponent][Constants.PAWN_INDEX] ) != 0){
                    epMask = whiteToMove() ? PrecomputedMasks.SQUARE_MASKS[move.startSquare-8]  : PrecomputedMasks.SQUARE_MASKS[move.startSquare+8];
                }else{
                    epMask = 0;
                }
            }
        }else if(move.isCastling){
            epMask = 0;
            castle &= ~Constants.CASTLING_SIDE[side];
            int rookSquare, rookNewSquare;
            if(move.isKingSideCastling){
                rookSquare = move.startSquare + 3;
                rookNewSquare = rookSquare - 2;
            }else{
                rookSquare = move.startSquare - 4;
                rookNewSquare = rookSquare + 3;
            }
            long rookMask = PrecomputedMasks.SQUARE_MASKS[rookSquare] | PrecomputedMasks.SQUARE_MASKS[rookNewSquare];
            bitboard[side][Constants.ROOK_INDEX] ^= rookMask;
            occupancyBoard[side] ^= rookMask;
            indicesBoard[rookSquare] = Constants.INVALID_INDEX;
            indicesBoard[rookNewSquare] = Constants.ROOK_INDEX;
        }else{
            epMask = 0;

            if (move.pieceIndex == Constants.ROOK_INDEX){
                int startFile = move.startSquare%8;
                if(startFile == 0 && (castle& Constants.CASTLE_BIT_MASK[side][Constants.QUEEN_SIDE_CASTLE_INDEX]) != 0){
                    int startRank = move.startSquare/8;
                    int castlingRank = whiteToMove() ? 7 : 0;
                    if (startRank == castlingRank){
                        castle ^= Constants.CASTLE_BIT_MASK[side][Constants.QUEEN_SIDE_CASTLE_INDEX];
                    }
                }else if(startFile == 7 && (castle& Constants.CASTLE_BIT_MASK[side][Constants.KING_SIDE_CASTLE_INDEX]) != 0){
                    int startRank = move.startSquare/8;
                    int castlingRank = whiteToMove() ? 7 : 0;
                    if (startRank == castlingRank){
                        castle ^= Constants.CASTLE_BIT_MASK[side][Constants.KING_SIDE_CASTLE_INDEX];
                    }
                }
            }else if(move.pieceIndex == Constants.KING_INDEX && isCastlingPossible()){
                castle &= ~Constants.CASTLING_SIDE[side];
            }
        }

        opponent = side;
        side ^= 1;
        if (whiteToMove()){
            fullMoveClock++;
        }

        // System.out.println(Utils.getBoardVisual(bitboard));

        update();

    }


    public long queenRays(int square){
        return bishopRays(square) | rookRays(square);
    }



    public String fen(){
        String fen = "";
        String binaryString = Long.toBinaryString(Long.reverse(occupied));
        int square = 64 - binaryString.length();
        int emptySquares = square;

        for(char bit:binaryString.toCharArray()){
            if(bit == '1'){
                if(emptySquares != 0){
                    fen += emptySquares;
                }
                int pieceIndex = indicesBoard[square];
                fen += Utils.getPieceRepresentation((PrecomputedMasks.SQUARE_MASKS[square] & occupancyBoard[Constants.WHITE]) != 0? Constants.WHITE: Constants.BLACK, pieceIndex);
                emptySquares = 0;
            }else{
                emptySquares ++;
            }
            square++;

            if(square%8 == 0){
                if(emptySquares>0){
                    fen += emptySquares;
                }
                if(square < 63) {
                    fen += "/";
                }
                emptySquares = 0;
            }
        }

        fen += whiteToMove ? " w ":" b ";
        if(castle == 0){
            fen += "- ";
        }else{
            if((castle & Constants.CASTLE_BIT_MASK[Constants.WHITE][Constants.KING_SIDE_CASTLE_INDEX]) != 0){
                fen += "K";
            }
            if((castle & Constants.CASTLE_BIT_MASK[Constants.WHITE][Constants.QUEEN_SIDE_CASTLE_INDEX]) != 0){
                fen += "Q";
            }
            if((castle & Constants.CASTLE_BIT_MASK[Constants.BLACK][Constants.KING_SIDE_CASTLE_INDEX]) != 0){
                fen += "k";
            }
            if((castle & Constants.CASTLE_BIT_MASK[Constants.BLACK][Constants.QUEEN_SIDE_CASTLE_INDEX]) != 0){
                fen += "q";
            }
            fen += " ";
        }

        if(isEPAvailable){
            fen += Utils.squareCoord(Long.numberOfTrailingZeros(epMask));
        }else{
            fen += "-";
        }

        fen += " "+halfMoveClock + " "+fullMoveClock;

        return fen;
    }

    private long hash(){
        long hash = 0;
        for(long[] pieceBitboard:bitboard){
            for(long piece:pieceBitboard){
                hash ^= piece;
            }
        }
        if(whiteToMove){
            hash ^= PrecomputedMasks.RANDOM_ZOBRIST_VALUE;
        }
        hash ^= castle;
        hash ^= epMask;

        return hash;
    }



    private boolean whiteToMove(){
        return side == Constants.WHITE;
    }

    private boolean isCheck(){
        return checkersMask != 0;
    }

    private boolean isDoubleCheck(){
        return Long.bitCount(checkersMask) > 1;
    }

    private boolean isSingleCheck() {
        return Long.bitCount(checkersMask) == 1;
    }

    private boolean isEPAvailable(){
        return epMask != 0;
    }

    private boolean isCastlingPossible(){
        return (castle & Constants.CASTLING_SIDE[side]) != 0;
    }

}
