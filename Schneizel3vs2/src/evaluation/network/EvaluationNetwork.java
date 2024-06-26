package evaluation.network;

import server3.board.Board;
import server3.preload.PrecomputedMasks;
import server3.utils.Constants;
import server3.utils.jnn.Activation;
import server3.utils.jnn.ExportManager;
import server3.utils.jnn.Matrix;
import server3.utils.jnn.NeuralNetwork;

import java.io.File;

public class EvaluationNetwork {
    
    public static final String NETWORK_NAME = "10 sig 100 sig 50 sig";
    public static boolean isTraining = false;


    private static NeuralNetwork evaluationNetwork = null;

    public static Matrix inputMatrix = new Matrix(1028,1);

    private static void createNetwork(){
        evaluationNetwork = new NeuralNetwork(1028,2, Activation.SIGMOID);
        evaluationNetwork.addLayer(10,Activation.SIGMOID);
        evaluationNetwork.addLayer(100,Activation.SIGMOID);
        evaluationNetwork.addLayer(50,Activation.SIGMOID);
        evaluationNetwork.initialize();
    }


    public static void initialize(){
        if(evaluationNetwork != null){
            return;
        }
        File modelPath = new File(NETWORK_NAME);
        if(modelPath.exists() && modelPath.isDirectory()){
            evaluationNetwork = ExportManager.createModel(NETWORK_NAME);
            evaluationNetwork.initialize();
        }else{
            createNetwork();
        }
    }

    public static float evaluatePosition(Board board){
        Matrix inputMatrix = new Matrix(1028,1);
        generateInputMatrix(board,inputMatrix);
        Matrix result = evaluationNetwork.feedForward(inputMatrix);
        return board.whiteToMove ? result.array[Constants.WHITE][0] - result.array[Constants.BLACK][0] : result.array[Constants.BLACK][0] - result.array[Constants.WHITE][0];
    }


    public static void save(){
        if(evaluationNetwork == null){
            return;
        }
        ExportManager.exportModel(evaluationNetwork,NETWORK_NAME);
    }


    public static void trainNetwork(Matrix input, Matrix result){
        evaluationNetwork.train(input,result);
    }

    public static void generateInputMatrix(Board board){
        EvaluationNetwork.inputMatrix.resetValues();
        generateInputMatrix(board, inputMatrix);
    }

    public static void generateInputMatrix(Board board,Matrix matrix){
        String occupancyBits = Long.toBinaryString(board.occupied);
        int beginningEmptySquares = 64-occupancyBits.length();
        int squareIndex = beginningEmptySquares;
        for(int arrayIndex=beginningEmptySquares * 8;arrayIndex<64*8;arrayIndex+=8){

            if(occupancyBits.charAt(squareIndex-beginningEmptySquares) == '1'){
                //occupancy
                matrix.array[64*8+64+4+squareIndex][0] = 1;
                int piece = board.indicesBoard[squareIndex];

                boolean isWhitePiece = (board.occupancyBoard[Constants.WHITE] & PrecomputedMasks.SQUARE_MASKS[squareIndex]) != 0;

                int sideIndex = isWhitePiece ? Constants.WHITE : Constants.BLACK;
                matrix.array[arrayIndex+sideIndex][0] = 1;

                matrix.array[arrayIndex+2+piece][0] = 1;
                if(isWhitePiece){
                    //occupancy
                    matrix.array[64*8+64+4+64+64+64+squareIndex][0] = 1;
                }else{
                    //occupancy
                    matrix.array[64*8+64+4+64+64+64+64+squareIndex][0] = 1;
                }
                if(piece == Constants.KING_INDEX){
                    if(isWhitePiece){
                        //wk
                        matrix.array[64*8+64+4+64+squareIndex][0] = 1;
                    }else{
                        //bk
                        matrix.array[64*8+64+4+64+64+squareIndex][0] = 1;
                    }
                }else if(piece == Constants.PAWN_INDEX){
                    if(isWhitePiece){
                        //wp
                        matrix.array[64*8+64+4+64+64+64+squareIndex][0] = 1;
                    }else{
                        //bp
                        matrix.array[64*8+64+4+64+64+64+64+squareIndex][0] = 1;
                    }
                }
            }
            squareIndex++;

        }

        //en-passant
        if(board.isEPAvailable){
            matrix.array[Long.numberOfLeadingZeros(board.epMask) + 64*8][0] = 1;
        }

        if(board.castle==0){
            return;
        }


        //castle
        String castleBits = Byte.toString(board.castle);
        int leadingZeros = 4 - castleBits.length();
        for(int i=leadingZeros;i < 4;i++){
            matrix.array[64*8+64+i][0] = Integer.parseInt(Character.toString(castleBits.charAt(i-leadingZeros)));
        }



    }

}
