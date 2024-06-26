import engine.Engine;
import server.board.Board;
import server.move.Move;
import server.preload.PrecomputedMasks;
import server.utils.Constants;
import server.utils.PGNUtils;
import server.utils.Utils;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        Board board = new Board("8/8/8/3Q4/5k2/8/2K5/8 b - - 3 2");
        //
        System.out.println(Utils.getBoardVisual(board.bitboard));
        System.out.println(board.fen());


//        float v = Utils.squareIndex("e4") + Utils.squareIndex("e5") + Utils.squareIndex("d4") + Utils.squareIndex("d5");
//        System.out.println(v/4.0f);




//        int square = Utils.squareIndex("e1");
//        int rank = square / 8;
//        System.out.println(Utils.getBitboardVisual(Constants.BOARD_MASK << 8 * (rank+1)));



//        int squareIndex = Utils.squareIndex("e2");
//        long pawn = PrecomputedMasks.SQUARE_MASKS[squareIndex];
//
//        int fileIndex = squareIndex%8;
//        int rankIndex = squareIndex/8;
//
//        long rankMask = PrecomputedMasks.RANK_MASKS[rankIndex];
//        long fileMask = PrecomputedMasks.FILE_MASKS[fileIndex];
//        long leftFileMask = PrecomputedMasks.FILE_MASKS[Math.max(0,fileIndex-1)];
//        long rightFileMask = PrecomputedMasks.FILE_MASKS[Math.min(7,fileIndex+1)];
//
//
//        long advanceBits = Utils.lowerBits(squareIndex) & ~rankMask;
//
//        long l = (fileMask | leftFileMask | rightFileMask) & advanceBits;
//
//        System.out.println(Utils.getBitboardVisual(pawn));
//        System.out.println(Utils.getBitboardVisual(l));








//        String moveText = "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Nxe4 6. d4 b5 7. Bb3 d5 8. dxe5\n" +
//                "Be6 9. Nbd2 Nc5 10. c3 Be7 11. Bc2 d4 12. Nb3 d3 13. Bb1 Nxb3 14. axb3 Bf5 15.\n" +
//                "Be3 O-O 16. Bd4 Qd5 17. Re1 d2 18. Re2 Bxb1 19. Rxb1 Nxd4 20. Nxd4 Bg5 21. g3\n" +
//                "c5 22. Nf5 Qe6 23. Nd6 Rad8 24. f4 Be7 25. Ra1 f6 26. f5 Qd7 27. Rxa6 fxe5 28.\n" +
//                "Rxd2 Bg5 29. Rd5 Rxf5 30. Nxf5 Qxd5 31. Qxd5+ Rxd5 32. Rd6 Rd2 33. Rxd2 Bxd2\n" +
//                "34. Kf2 b4 35. c4 Bg5 36. Kf3 Kf7 37. Nd6+ Ke6 38. Ne4 Be7 39. g4 Kd7 40. Nf2\n" +
//                "Ke6 41. Nd3 Bf8 42. Nf2 Kf6 43. Ne4+ Kg6 44. Nf2 Kg5 45. Kg3 Kg6 46. Kf3 Kg5\n" +
//                "47. Kg3 Be7 48. h3 Kf6 49. h4 g6 50. Ne4+ Ke6 51. Ng5+ Bxg5 52. hxg5 e4 53. Kf4\n" +
//                "e3 54. Kf3 Kd6 55. Ke2 Ke6 56. Kd3 Kd6 57. Ke2 Ke6 58. Kf3 Kd6 59. Kxe3 Ke5 60.\n" +
//                "Kf3 Kd4 61. Kf4 Kd3 62. Ke5 Kc2 63. Kd5 Kxb2 64. Kxc5 Kxb3 65. Kd4 Kc2 66. c5\n" +
//                "b3 67. c6 b2 68. c7 b1=Q 69. c8=Q+ Kd1 70. Qd7 Qb2+ 71. Kc4+ Qd2 72. Qxh7 Qxg5\n" +
//                "73. Qh1+ Kc2 74. Qa1 Qxg4+ 75. Kd5 g5 76. Qa2+ Kc1 77. Qc4+ Qxc4+ 78. Kxc4 g4\n" +
//                "79. Kd4 g3 80. Ke3 g2 81. Kf2 g1=B+ 1/2-1/2\n";
//        ArrayList<Move> movesMade = PGNUtils.getMoves(moveText);
//        System.out.println(movesMade.size());
//        System.out.println(movesMade);


//        System.out.println(Utils.getBitboardVisual(PrecomputedMasks.TERRITORY[Constants.WHITE]));



//        Board board = new Board("rnbqkbnr/ppppp1pp/5p2/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2");
//        System.out.println(Utils.getBitboardVisual(board.checkersMask));
//        System.out.println(Utils.getBitboardVisual(board.checkRayMask));
//        board.makeMove(MoveGenerator.fromString(board,"d1h5"));
//        System.out.println(Utils.getBitboardVisual(board.diagonalRay(board.kingSquare,true,false)));
//        System.out.println(Utils.getBitboardVisual(PrecomputedMasks.ANTI_DIAGONAL_MASKS[board.kingRank+7-board.kingFile]));
//        System.out.println(Utils.getBitboardVisual(board.checkRayMask));
//        System.out.println(Utils.getBitboardVisual(board.checkersMask));


//        Engine engine = new Engine();
//        engine.perft(5);
//2r1k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/1R2K2R w Kk - 2 2
//2r1k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/1R2K2R w Kk - 2 2
        //"2r1k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/1R2K2R w Kk - 2 2"
        //Board board = new Board("rnbqkbnr/1ppppppp/p7/8/8/7P/PPPPPPPR/RNBQKBN1 b - - 1 2");
//        EvaluationNetwork.initialize();
//        System.out.println(EvaluationNetwork.evaluatePosition(board));

        //System.out.println(Constants.HIGHEST_VALUE);

//        board.makeMove(MoveGenerator.fromString(board,"e1g1"));
//        System.out.println(Utils.getBoardVisual(board.bitboard));
//        System.out.println(Long.toBinaryString(Long.reverse(board.occupied)));
//        System.out.println(Long.parseLong(Long.toBinaryString(Long.reverse(board.occupied))+"00"));
//        System.out.println(Utils.getBitboardVisual(Long.parseLong(Long.toBinaryString(Long.reverse(board.occupied))+"00")));
//        System.out.println(board.fen());

//"8/2k5/8/8/8/8/4p3/2K2Q2 b - - 0 1"
        //Engine engine = new Engine();
//        int i=-100;
//        while(i<2) {
//            engine.push(engine.getLegalMoves().get(0));
//            i++;
//        }
//        while(!engine.stack.isEmpty()) {
//            engine.takeBack();
//            System.out.println(engine.fen());
//        }
//        engine.trainNetwork();
//        engine.saveNetwork();
//        System.out.println(engine.fen().equals(Constants.STARTING_FEN));



//        engine.beginSearch();
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(engine.getBestMove().toString());
//

        //System.out.println(Utils.getBitboardVisual(PrecomputedMasks.KNIGHT_MOVES[Utils.squareIndex("g1")]));

//        Engine engine = new Engine();
//        engine.push(MoveGenerator.fromString(engine.getCurrentBoard(),"a2a4"));
//        engine.push(MoveGenerator.fromString(engine.getCurrentBoard(),"e7e5"));
//        engine.push(MoveGenerator.fromString(engine.getCurrentBoard(),"b1c3"));
//        for(long hash:engine.occurredPositions.keySet()){
//            System.out.println(hash+": "+engine.occurredPositions.get(hash));
//        }



//        Engine engine = new Engine();
//
//        System.out.println(engine.perft(1));

//        Board board1 = new Board(Constants.STARTING_FEN);
//
//        Board board2 = new Board(board1);
//
//        System.out.println(Utils.getBoardVisual(board2.bitboard));

//        System.out.println(board.fen());
//        System.out.println(Utils.getBitboardVisual(board.bitboard[board.side][Constants.ROOK_INDEX]));
//        System.out.println(Utils.getBoardVisual(board.bitboard));


//        System.out.println("public static final int WHITE_KING_INITIAL_POSITION = "+Utils.squareIndex("e1"));
//        System.out.println("public static final int BLACK_KING_INITIAL_POSITION = "+Utils.squareIndex("e8"));

        //long bitboard = PrecomputedMasks.KING_MOVES[5];

//        System.out.println(Utils.getBitboardVisual(bitboard));
//        System.out.println(Utils.getBitboardVisual(Utils.popLSB((Long)(bitboard))));
//        System.out.println(Utils.getBitboardVisual(bitboard));

//        for (int i=0;i<8;i++){
//            System.out.println(Utils.getBitboardVisual(PrecomputedMasks.FILE_MASKS[i]));
//            System.out.println(Utils.getBitboardVisual(PrecomputedMasks.RANK_MASKS[i]));
//            System.out.println("##########################################");
//        }

//        for(int i=0;i<64;i++){
//            System.out.println(Utils.getBitboardVisual(PrecomputedMasks.SQUARE_MASKS[i]));
//        }

//        System.out.println(Utils.getBitboardVisual(PrecomputedMasks.SQUARE_MASKS[62]));


    }

}
