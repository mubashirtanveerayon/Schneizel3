package server.move;

import evaluation.classic.StaticEvaluation;
import evaluation.re.ReEvaluation;
import server.utils.Constants;
import server.utils.Utils;

import static server.utils.Constants.*;

public class Move {

    public final int startSquare, targetSquare, pieceIndex, capturePieceIndex, promotionPieceIndex;

    public final int evalImpact;

    public final boolean isKingSideCastling, isPawnMove, isEP, isCapture, isCastling, isPromotion;

    public final int sideToMove;

    public Move(int sideToMove,int startSquare, int targetSquare, int pieceIndex, int capturePieceIndex, int promotionPieceIndex){
        this.sideToMove = sideToMove;
        this.startSquare = startSquare;
        this.targetSquare = targetSquare;
        this.pieceIndex = pieceIndex;
        this.capturePieceIndex = capturePieceIndex;
        this.promotionPieceIndex = promotionPieceIndex;
        isCapture = capturePieceIndex != INVALID_INDEX;
        isPawnMove = pieceIndex == PAWN_INDEX;
        isPromotion = promotionPieceIndex != INVALID_INDEX;
        isEP = isPawnMove && Utils.onBishopRay(startSquare,targetSquare) && !isCapture;
        isCastling = pieceIndex == KING_INDEX && Math.abs(startSquare-targetSquare) == 2;
        isKingSideCastling = isCastling && startSquare - targetSquare == -2;

        evalImpact = StaticEvaluation.moveStaticEvaluation(this);
    }


    public String toString(){
        if(isPromotion){
            return Utils.squareCoord(startSquare) + Utils.squareCoord(targetSquare) + Utils.getPieceRepresentation(BLACK,promotionPieceIndex);
        }else{
            return Utils.squareCoord(startSquare) + Utils.squareCoord(targetSquare);
        }
    }


    public String info(){
        String info = "Side To Move: "+(sideToMove == WHITE ? "White":"Black")+"\n";
        info += "Algebraic: "+toString()+"\n";
        info += "Piece to move: "+Utils.getPieceRepresentation(BLACK,pieceIndex)+"\n";
        if(isCapture) {
            info += "Piece to capture: " + Utils.getPieceRepresentation(BLACK, capturePieceIndex) + "\n";
        }else{
            info += "Piece to capture: " + "\n";
        }
        if(isPromotion) {
            info += "Promotion piece: " + Utils.getPieceRepresentation(BLACK, promotionPieceIndex) + "\n";
        }else{
            info += "Promotion piece: " + "\n";
        }
        info += "Castle: "+isCastling+"\n";
        info += "King side castle: "+isKingSideCastling+"\n";
        info += "En-passant: "+isEP+"\n";
        info += "Eval impact: "+Integer.toString(evalImpact);
        return info;

    }


}
