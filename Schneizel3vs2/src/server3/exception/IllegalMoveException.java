package server3.exception;

public class IllegalMoveException  extends RuntimeException{

    public IllegalMoveException(String fen,String moveStr){
        super("Illegal move for position (FEN): "+fen+"\nMove: "+moveStr);
    }

}
