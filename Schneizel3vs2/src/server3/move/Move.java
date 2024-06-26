package server3.move;

import server3.utils.Utils;
import server3.utils.Constants;

public class Move {

    public final int startSquare, targetSquare, pieceIndex, capturePieceIndex, promotionPieceIndex;


    public final boolean isKingSideCastling, isPawnMove, isEP, isCapture, isCastling, isPromotion;



    public Move(int startSquare, int targetSquare, int pieceIndex, int capturePieceIndex, int promotionPieceIndex){
        this.startSquare = startSquare;
        this.targetSquare = targetSquare;
        this.pieceIndex = pieceIndex;
        this.capturePieceIndex = capturePieceIndex;
        this.promotionPieceIndex = promotionPieceIndex;

        isPawnMove = pieceIndex == Constants.PAWN_INDEX;
        isPromotion = promotionPieceIndex != Constants.INVALID_INDEX;
        isEP = isPawnMove && Utils.onBishopRay(startSquare,targetSquare) && capturePieceIndex == Constants.INVALID_INDEX;
        isCastling = pieceIndex == Constants.KING_INDEX && Math.abs(startSquare-targetSquare) == 2;
        isKingSideCastling = isCastling && startSquare - targetSquare == -2;
        isCapture = capturePieceIndex != Constants.INVALID_INDEX;
    }


    public String toString(){
        if(isPromotion){
            return Utils.squareCoord(startSquare) + Utils.squareCoord(targetSquare) + Utils.getPieceRepresentation(Constants.BLACK,promotionPieceIndex);
        }else{
            return Utils.squareCoord(startSquare) + Utils.squareCoord(targetSquare);
        }
    }


    public String info(){
        String info = "";
        info += "Algebraic: "+toString()+"\n";
        info += "Piece to move: "+Utils.getPieceRepresentation(Constants.BLACK,pieceIndex)+"\n";
        if(isCapture) {
            info += "Piece to capture: " + Utils.getPieceRepresentation(Constants.BLACK, capturePieceIndex) + "\n";
        }else{
            info += "Piece to capture: " + "\n";
        }
        if(isPromotion) {
            info += "Promotion piece: " + Utils.getPieceRepresentation(Constants.BLACK, promotionPieceIndex) + "\n";
        }else{
            info += "Promotion piece: " + "\n";
        }
        info += "Castle: "+isCastling+"\n";
        info += "King side castle: "+isKingSideCastling+"\n";
        info += "En-passant: "+isEP;

        return info;

    }

}
