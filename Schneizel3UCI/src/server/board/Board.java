package server.board;

import evaluation.classic.StaticEvaluation;
import evaluation.re.ReEvaluation;
import server.move.Move;

import java.util.Arrays;

import static server.preload.PrecomputedMasks.*;
import static server.utils.Constants.*;
import static server.utils.Utils.*;

public class Board {


    public int currentEvaluation;
    public int opponent, side, kingFile,kingRank;

    public byte castle;
    public long hash, occupied, emptySquaresMask, safeSquares, checkersMask, pinnedPiecesMask, checkRayMask, epMask, notEP, straightLineSlidingAttackers, diagonalSlidingAttackers;


    public boolean isSingleCheck, isDoubleCheck, isCheck, isCastlingPossible, whiteToMove, isEPAvailable;

    public long[] occupancyBoard;

    public long[] pinRays;

    public int kingSquare;

    public int[] indicesBoard,nPieces;

    private long[] possibleAttackRays;

    public long[][] bitboard;
    public int halfMoveClock, fullMoveClock;

//    int lastMoveEvalImpact;

    public Board(){
        init(STARTING_FEN);
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
        notEP = board.notEP;
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
        currentEvaluation = board.currentEvaluation;
    }


    private void init(String fen){
        bitboard = setupBitboard(fen);
        String[] fenParts = fen.split(" ");

        side = fenParts[1].equalsIgnoreCase("w") ? WHITE : BLACK;
        opponent = side ^ 1;

        castle = 0;
        if (fenParts[2].contains("K")){
            castle |= CASTLE_BIT_MASK[WHITE][KING_SIDE_CASTLE_INDEX];
        }
        if (fenParts[2].contains("Q")){
            castle |= CASTLE_BIT_MASK[WHITE][QUEEN_SIDE_CASTLE_INDEX];
        }
        if (fenParts[2].contains("k")){
            castle |= CASTLE_BIT_MASK[BLACK][KING_SIDE_CASTLE_INDEX];
        }
        if (fenParts[2].contains("q")){
            castle |= CASTLE_BIT_MASK[BLACK][QUEEN_SIDE_CASTLE_INDEX];
        }

        if (fenParts[3].equals("-")){
            epMask = 0;
        }else{
            epMask = SQUARE_MASKS[squareIndex(fenParts[3])];
        }

        halfMoveClock = Integer.parseInt(fenParts[4]);
        fullMoveClock = Integer.parseInt(fenParts[5]);

        pinRays = new long[64];
        indicesBoard = new int[64];
        occupancyBoard = new long[2];

        Arrays.fill(indicesBoard,INVALID_INDEX);

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


//        lastMoveEvalImpact = 0;
//        currentEvaluation = ReEvaluation.reEvaluatePosition(this);// + ReEvaluation.reEvaluateKingSafety(this);
        currentEvaluation = StaticEvaluation.positionStaticEvaluation(this);
        update();


//        currentEvaluation = ReEvaluation.reEvaluatePosition(this);




    }


    public void update(){
        Arrays.fill(pinRays,BOARD_MASK);
        occupied = occupancyBoard[side] | occupancyBoard[opponent];
        emptySquaresMask = ~occupied;
        checkRayMask = BOARD_MASK;
        checkersMask = 0;
        pinnedPiecesMask = 0;
        kingSquare = Long.numberOfTrailingZeros(bitboard[side][KING_INDEX]);
        kingFile = kingSquare % 8;
        kingRank = kingSquare/8;

        straightLineSlidingAttackers = bitboard[opponent][QUEEN_INDEX] | bitboard[opponent][ROOK_INDEX] ;
        diagonalSlidingAttackers = bitboard[opponent][BISHOP_INDEX] | bitboard[opponent][QUEEN_INDEX];



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

        notEP = ~epMask;


        //System.out.println(Utils.getBitboardVisual(checkRayMask));
    }



    private void detectPins() {
        long kingLowerBits = lowerBits(kingSquare);
        long kingHigherBits = higherBits(kingSquare);

        long kingRankBB = occupied & RANK_MASKS[kingRank];
        long kingRankRightBB = kingLowerBits & kingRankBB;

        long possibleAlly = firstOne(kingRankRightBB);
        if ((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = firstOne(kingRankRightBB ^ possibleAlly);
            if((possibleAttacker & straightLineSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = horizontalRay(kingSquare, true);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingRankLeftBB = kingHigherBits & kingRankBB;
        possibleAlly = lastOne(kingRankLeftBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = lastOne(kingRankLeftBB ^ possibleAlly);
            if((possibleAttacker & straightLineSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = horizontalRay(kingSquare, false);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingFileBB = occupied & FILE_MASKS[kingFile];

        long kingFileDownBB = kingFileBB & kingLowerBits;
        possibleAlly = firstOne(kingFileDownBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = firstOne(kingFileDownBB ^ possibleAlly);
            if((possibleAttacker & straightLineSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = verticalRay(kingSquare, false);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingFileUpBB = kingFileBB & kingHigherBits;
        possibleAlly = lastOne(kingFileUpBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = lastOne(kingFileUpBB ^ possibleAlly);
            if ((possibleAttacker & straightLineSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = verticalRay(kingSquare, true);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long diagonalBB = DIAGONAL_MASKS[kingFile+kingRank] & occupied;

        long kingDiagonalUpBB = kingHigherBits & diagonalBB;
        possibleAlly = lastOne(kingDiagonalUpBB);
        if((possibleAlly & occupancyBoard [side])!= 0){
            long possibleAttacker = lastOne(kingDiagonalUpBB ^ possibleAlly);
            if((possibleAttacker & diagonalSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = diagonalRay(kingSquare, true,true);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingDiagonalDownBB = kingLowerBits & diagonalBB;

        possibleAlly = firstOne(kingDiagonalDownBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = firstOne(kingDiagonalDownBB ^ possibleAlly);

            if((possibleAttacker & diagonalSlidingAttackers) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = diagonalRay(kingSquare, false,false);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long antiDiagonalBB = ANTI_DIAGONAL_MASKS[kingRank+7-kingFile] & occupied;

        long kingAntiDiagonalUpBB = kingHigherBits & antiDiagonalBB;

        possibleAlly = lastOne(kingAntiDiagonalUpBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = lastOne(kingAntiDiagonalUpBB ^ possibleAlly);
            if((possibleAttacker & diagonalSlidingAttackers ) != 0){
                occupied ^= possibleAlly;
                pinRays[Long.numberOfTrailingZeros(possibleAlly)] = diagonalRay(kingSquare, true,false);
                occupied ^= possibleAlly;
                pinnedPiecesMask |= possibleAlly;
            }
        }

        long kingAntiDiagonalDownBB = kingLowerBits & antiDiagonalBB;
        possibleAlly = firstOne(kingAntiDiagonalDownBB);
        if((possibleAlly & occupancyBoard[side]) != 0){
            long possibleAttacker = firstOne(kingAntiDiagonalDownBB ^ possibleAlly);
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

            possibleAttackRays[0] = rookRay & RANK_MASKS[kingRank];
            possibleAttackRays[1] = rookRay & FILE_MASKS[kingFile];

            for (long ray : possibleAttackRays){
                long possibleAttacker = firstOne(ray) | lastOne(ray);
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
            possibleAttackRays[0] = bishopRay & DIAGONAL_MASKS[kingFile+kingRank];
            possibleAttackRays[1] = bishopRay & ANTI_DIAGONAL_MASKS[kingRank+7-kingFile];

            for (long ray : possibleAttackRays){
                long possibleAttacker = firstOne(ray) | lastOne(ray);
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

        long possibleOpponentPawnCheckers = bitboard[opponent][PAWN_INDEX] & KING_MOVES[kingSquare] & ~FILE_MASKS[kingFile] & ~RANK_MASKS[kingRank];

        while(possibleOpponentPawnCheckers != 0){
            long possiblePawn = lastOne(possibleOpponentPawnCheckers);
            if((PAWN_ATTACKS[opponent][Long.numberOfTrailingZeros(possiblePawn)] & bitboard[side][KING_INDEX]) != 0){
                checkersMask |= possiblePawn;
                break;
            }
            possibleOpponentPawnCheckers ^= possiblePawn;
        }


        checkersMask |= KNIGHT_MOVES[kingSquare] & bitboard[opponent][KNIGHT_INDEX];

        if (isSingleCheck()){
            int checkerSquare = Long.numberOfTrailingZeros(checkersMask);
            int checkerFile = checkerSquare%8, checkerRank = checkerSquare/8;

            if (kingFile == checkerFile){
                  checkRayMask = verticalRay(kingSquare, checkerRank > kingRank);
            }else if(kingRank == checkerRank){
                checkRayMask = horizontalRay(kingSquare, checkerFile < kingFile);
            }else if(onBishopRay(kingSquare, checkerSquare)){
                checkRayMask = diagonalRay(kingSquare, checkerRank > kingRank, checkerFile < kingFile);
//                if (isEPAvailable()){
//                    if((checkersMask & bitboard[opponent][PAWN_INDEX]) != 0) {
//                        checkRayMask |= epMask;
//                    }else{
//                        epMask = 0;
//                    }
//                }
            }else{
                checkRayMask = checkersMask;
            }
        }

    }



    private long generateDangerMask() {
        long dangerMask = 0;

        occupied ^= bitboard[side][KING_INDEX];

        for (int pieceIndex=0 ; pieceIndex<6;pieceIndex ++){
            long enemyPieceBitboard = bitboard[opponent][pieceIndex];


            if(pieceIndex == PAWN_INDEX){
                dangerMask |= maskPawnAttacks(opponent,enemyPieceBitboard);
                continue;
            }else if(pieceIndex == KNIGHT_INDEX){
                dangerMask |= maskKnightMoves(enemyPieceBitboard);
                continue;
            }else if(pieceIndex == KING_INDEX){
                dangerMask |= maskKingMoves(enemyPieceBitboard);
                continue;
            }





            while(enemyPieceBitboard != 0){
                long enemyPiece = lastOne(enemyPieceBitboard);
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
                    case BISHOP_INDEX:
                        dangerMask |= bishopRays(enemyPieceSquare);
                        break;
                    case ROOK_INDEX:
                        dangerMask |= rookRays(enemyPieceSquare);
                        break;
                    case QUEEN_INDEX:
                        dangerMask |= queenRays(enemyPieceSquare);
                        break;
                }
            }

        }

        occupied ^= bitboard[side][KING_INDEX];

        return dangerMask;
    }

    public long horizontalRay(int square, boolean right){
        long slider = SQUARE_MASKS[square];
        long rankMask = RANK_MASKS[square/8];
        if (right){
            return (occupied ^ Long.reverse(Long.reverse(occupied) - 2 * Long.reverse(slider))) & rankMask;
        }else{
            return (occupied ^ (occupied - 2 * slider)) & rankMask;
        }
    }

    public long verticalRay(int square, boolean top){
        long slider = SQUARE_MASKS[square];
        long fileMask = FILE_MASKS[square%8];

        long ray = ((occupied & fileMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & fileMask) - 2  * Long.reverse(slider));
        ray &= fileMask;

        if (top){
            return ray & higherBits(square);
        }else{
            return ray & lowerBits(square);
        }
    }

    public long diagonalRay(int square, boolean top, boolean right){
        long slider = SQUARE_MASKS[square];
        int file = square%8;
        int rank = square/8;

        if (top){
            if(right){
                long diagonalMask = DIAGONAL_MASKS[file+rank];
                long diagonalRay = (((occupied & diagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & diagonalMask) - (2 * Long.reverse(slider)))) & diagonalMask;
                return diagonalRay & higherBits(square);
            }else{
                long antiDiagonalMask = ANTI_DIAGONAL_MASKS[rank+7-file];
                long antiDiagonalRay = (((occupied & antiDiagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & antiDiagonalMask) - (2 * Long.reverse(slider)))) & antiDiagonalMask;
                return antiDiagonalRay & higherBits(square);
            }
        }else{
            if(right){
                long antiDiagonalMask = ANTI_DIAGONAL_MASKS[rank+7-file];
                long antiDiagonalRay = (((occupied & antiDiagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & antiDiagonalMask) - (2 * Long.reverse(slider)))) & antiDiagonalMask;
                return antiDiagonalRay & lowerBits(square);
            }else{
                long diagonalMask = DIAGONAL_MASKS[file+rank];
                long diagonalRay = (((occupied & diagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & diagonalMask) - (2 * Long.reverse(slider)))) & diagonalMask;
                return diagonalRay & lowerBits(square);
            }
        }
    }

    public long rookRays(int square){
        long slider = SQUARE_MASKS[square];
        int file = square%8, rank = square / 8;

        return (((occupied - 2 * slider ^ Long.reverse(Long.reverse(occupied) - 2 * Long.reverse(slider)))) & RANK_MASKS[rank]) | ((((occupied & FILE_MASKS[file]) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & FILE_MASKS[file]) - (2 * Long.reverse(slider)))) & FILE_MASKS[file]);
    }

    public long bishopRays(int square){
        long slider = SQUARE_MASKS[square];
        int file = square%8, rank = square/8;

        long diagonalMask = DIAGONAL_MASKS[file+rank];
        long antiDiagonalMask = ANTI_DIAGONAL_MASKS[rank+7-file];

        long diagonalRay = (((occupied & diagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & diagonalMask) - (2 * Long.reverse(slider)))) & diagonalMask;
        long antiDiagonalRay = (((occupied & antiDiagonalMask) - (2 * slider)) ^ Long.reverse(Long.reverse(occupied & antiDiagonalMask) - (2 * Long.reverse(slider)))) & antiDiagonalMask;

        return antiDiagonalRay | diagonalRay;
    }

    public void makeMove(Move move){
        long moveMask = SQUARE_MASKS[move.startSquare] | SQUARE_MASKS[move.targetSquare];


        bitboard[side][move.pieceIndex] ^= moveMask;

        occupancyBoard[side] ^= moveMask;
        indicesBoard[move.startSquare] = INVALID_INDEX;
        indicesBoard[move.targetSquare] = move.pieceIndex;

        if(move.isCapture){
            halfMoveClock = 0;
            nPieces[opponent]--;
            bitboard[opponent][move.capturePieceIndex] ^= SQUARE_MASKS[move.targetSquare];
            occupancyBoard[opponent] ^= SQUARE_MASKS[move.targetSquare];
            if (move.capturePieceIndex == ROOK_INDEX){
                int targetFile = move.targetSquare%8;

                if (targetFile == 0 && (castle & CASTLE_BIT_MASK[opponent][QUEEN_SIDE_CASTLE_INDEX]) != 0){
                    int opponentCastlingRank = whiteToMove() ? 0 : 7;
                    if(move.targetSquare/8 == opponentCastlingRank){
                        castle ^= CASTLE_BIT_MASK[opponent][QUEEN_SIDE_CASTLE_INDEX];
                    }
                }else if(targetFile == 7 && (castle & CASTLE_BIT_MASK[opponent][KING_SIDE_CASTLE_INDEX]) != 0){
                    int opponentCastlingRank = whiteToMove() ? 0 : 7;
                    if(move.targetSquare/8 == opponentCastlingRank){
                        castle ^= CASTLE_BIT_MASK[opponent][KING_SIDE_CASTLE_INDEX];
                    }
                }
            }
        }else{
            halfMoveClock ++;
        }

        if (move.isPawnMove){
            halfMoveClock = 0;
            if (move.isPromotion){
                bitboard[side][PAWN_INDEX] ^= SQUARE_MASKS[move.targetSquare];
                bitboard[side][move.promotionPieceIndex] ^= SQUARE_MASKS[move.targetSquare];
                indicesBoard[move.targetSquare] = move.promotionPieceIndex;
                epMask = 0;
            }else if(move.isEP){
                long captureSquareMask = whiteToMove() ? epMask << 8:epMask >>> 8;
                bitboard[opponent][PAWN_INDEX] ^= captureSquareMask;
                occupancyBoard[opponent] ^= captureSquareMask;
                indicesBoard[Long.numberOfTrailingZeros(captureSquareMask)] = INVALID_INDEX;
                epMask = 0;
                nPieces[opponent]--;
            }else{
                if(Math.abs(move.targetSquare-move.startSquare) == 16 && (KING_MOVES[move.targetSquare] & RANK_MASKS[move.targetSquare/8] & bitboard[opponent][PAWN_INDEX] ) != 0){
                    epMask = whiteToMove() ? SQUARE_MASKS[move.startSquare-8]  : SQUARE_MASKS[move.startSquare+8];
                }else{
                    epMask = 0;
                }
            }
        }else if(move.isCastling){
            epMask = 0;
            castle &= (byte) ~CASTLING_SIDE[side];
            int rookSquare, rookNewSquare;
            if(move.isKingSideCastling){
                rookSquare = move.startSquare + 3;
                rookNewSquare = rookSquare - 2;
            }else{
                rookSquare = move.startSquare - 4;
                rookNewSquare = rookSquare + 3;
            }
            long rookMask = SQUARE_MASKS[rookSquare] | SQUARE_MASKS[rookNewSquare];
            bitboard[side][ROOK_INDEX] ^= rookMask;
            occupancyBoard[side] ^= rookMask;
            indicesBoard[rookSquare] = INVALID_INDEX;
            indicesBoard[rookNewSquare] = ROOK_INDEX;
        }else{
            epMask = 0;

            if (move.pieceIndex == ROOK_INDEX){
                int startFile = move.startSquare%8;
                if(startFile == 0 && (castle&CASTLE_BIT_MASK[side][QUEEN_SIDE_CASTLE_INDEX]) != 0){
                    int startRank = move.startSquare/8;
                    int castlingRank = whiteToMove() ? 7 : 0;
                    if (startRank == castlingRank){
                        castle ^= CASTLE_BIT_MASK[side][QUEEN_SIDE_CASTLE_INDEX];
                    }
                }else if(startFile == 7 && (castle&CASTLE_BIT_MASK[side][KING_SIDE_CASTLE_INDEX]) != 0){
                    int startRank = move.startSquare/8;
                    int castlingRank = whiteToMove() ? 7 : 0;
                    if (startRank == castlingRank){
                        castle ^= CASTLE_BIT_MASK[side][KING_SIDE_CASTLE_INDEX];
                    }
                }
            }else if(move.pieceIndex == KING_INDEX && isCastlingPossible()){
                castle &= (byte) ~CASTLING_SIDE[side];
            }
        }

        opponent = side;
        side ^= 1;
        if (whiteToMove()){
            fullMoveClock++;
        }

        // System.out.println(Utils.getBoardVisual(bitboard));
        currentEvaluation += move.evalImpact;
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
        for (int i = 0; i < square / 8; i++) {
            fen += "8/";
            emptySquares -= 8;
        }


        for(char bit:binaryString.toCharArray()){
            if(bit == '1'){
                if(emptySquares != 0){
                    fen += emptySquares;
                }
                int pieceIndex = indicesBoard[square];
                fen += getPieceRepresentation((SQUARE_MASKS[square] & occupancyBoard[WHITE]) != 0? WHITE:BLACK, pieceIndex);
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
            if((castle & CASTLE_BIT_MASK[WHITE][KING_SIDE_CASTLE_INDEX]) != 0){
                fen += "K";
            }
            if((castle & CASTLE_BIT_MASK[WHITE][QUEEN_SIDE_CASTLE_INDEX]) != 0){
                fen += "Q";
            }
            if((castle & CASTLE_BIT_MASK[BLACK][KING_SIDE_CASTLE_INDEX]) != 0){
                fen += "k";
            }
            if((castle & CASTLE_BIT_MASK[BLACK][QUEEN_SIDE_CASTLE_INDEX]) != 0){
                fen += "q";
            }
            fen += " ";
        }

        if(isEPAvailable){
            fen += squareCoord(Long.numberOfTrailingZeros(epMask));
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
            hash ^= RANDOM_ZOBRIST_VALUE;
        }
        hash ^= castle;
        hash ^= epMask;

        return hash;
    }



    private boolean whiteToMove(){
        return side == WHITE;
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
        return (castle & CASTLING_SIDE[side]) != 0;
    }

}
