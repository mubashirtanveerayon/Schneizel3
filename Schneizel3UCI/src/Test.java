import server.board.Board;
import server.move.Move;
import server.move.MoveGenerator;
import server.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Test {

    public static void sort(int[] array){
        if(array.length<2)return;
        int midIdx = array.length/2;
        int[] leftArray = new int[midIdx];
        int[] rightArray = new int[array.length-midIdx];

        for(int i=0;i<midIdx;i++)leftArray[i] = array[i];
        for(int i=midIdx;i<array.length;i++)rightArray[i-midIdx] = array[i];

        sort(rightArray);
        sort(leftArray);


        int leftIdx=0,rightIdx=0,i=0;
        while(leftIdx < leftArray.length && rightIdx < rightArray.length){
            if(leftArray[leftIdx] >= rightArray[rightIdx]){
                array[i] = leftArray[leftIdx];
                leftIdx++;
            }else {
                array[i] = rightArray[rightIdx];
                rightIdx++;
            }
            i++;
        }

        while(leftIdx<leftArray.length)array[i++] = leftArray[leftIdx++];
        while(rightIdx<rightArray.length)array[i++] = rightArray[rightIdx++];
    }


    public static void sort1(ArrayList<String> strings, int[] values){
        if(values.length < 2)return;

        ArrayList<String> leftArray = new ArrayList<>();
        ArrayList<String> rightArray = new ArrayList<>();
        int midIndex = values.length /2 ;
        int[] leftValues = new int[midIndex];
        int[] rightValues = new int[values.length-midIndex];
        for(int i=0;i<midIndex;i++){
            leftArray.add(strings.get(i));
            leftValues[i] = values[i];
        }
        for(int i=midIndex;i<values.length;i++){
            rightArray.add(strings.get(i));
            rightValues[i-midIndex] = values[i];
        }

        sort1(leftArray,leftValues);
        sort1(rightArray,rightValues);
        strings.clear();
        int leftIdx=0,rightIdx=0,i=0;
        while(leftIdx < leftValues.length && rightIdx < rightValues.length){
            if(leftValues[leftIdx] >= rightValues[rightIdx]){
                strings.add(i,leftArray.get(leftIdx));
                leftIdx++;
            }else{
                strings.add(i,rightArray.get(rightIdx));
                rightIdx++;
            }
            i++;
        }

        while(leftIdx < leftValues.length){
            strings.add(i,leftArray.get(leftIdx));
            leftIdx++;
            i++;
        }

        while(rightIdx < rightValues.length){
            strings.add(i,rightArray.get(rightIdx));
            rightIdx++;
            i++;
        }


    }


    public static void sort2(int[] array, boolean descending){
        if (array.length<2)return;
        int midIndex = array.length/2;
        int[] leftArray=new int[midIndex];
        int[] rightArray=new int[array.length-midIndex];
        for (int i=0;i<midIndex;i++)leftArray[i] = array[i];
        for(int i=midIndex;i<array.length;i++)rightArray[i-midIndex]=array[i];

        sort2(leftArray,descending);
        sort2(rightArray,descending);
        int leftIdx=0,rightIdx=0,i=0;
        while(leftIdx < leftArray.length && rightIdx < rightArray.length){
            if((descending && leftArray[leftIdx] >= rightArray[rightIdx]) || (!descending && leftArray[leftIdx] <= rightArray[rightIdx])){
                array[i] = leftArray[leftIdx];
                leftIdx++;
            }else {
                array[i] = rightArray[rightIdx];
                rightIdx++;
            }
            i++;
        }

        while(leftIdx<leftArray.length)array[i++] = leftArray[leftIdx++];
        while(rightIdx<rightArray.length)array[i++] = rightArray[rightIdx++];
    }

    public static void main(String[] args) {
//        Board board = new Board("2k3r1/6R1/8/8/8/5n2/8/2KR4 b - - 0 26");
//        System.out.println(MoveGenerator.generateCaptureMoves(board));


//        int[] array = new int[]{23,10,1,-2,22,90,0,1,12};
//        sort(array);
//        for(int i:array) System.out.print(i+",");

        Random rand = new Random();
//
//        int length = 10;
//        int[] array = new int[length];
//        ArrayList<String> strings=new ArrayList<>();
//        for(int i=0;i<length;i++){
//            array[i] = rand.nextInt(100);
//            strings.add(Integer.toString(array[i]));
//        }
//
//        System.out.println("Before");
//        System.out.println(Arrays.toString(array));
//        sort2(array,false);
////        System.out.println(strings);
////        sort1(strings,array);
//        System.out.println("After");
//        System.out.println(Arrays.toString(array));

        Board board=new Board();
        ArrayList<Move>moves = MoveGenerator.generateMoves(board);
        int[] array = new int[moves.size()];
        for (int i=0;i<array.length;i++)array[i] = rand.nextInt(100);
        System.out.println("before:");
        for (Integer n:array) System.out.print(n+",");
        System.out.println();
        for (Move move:moves) System.out.print(move.toString()+",");
        System.out.println("\nafter");


        Utils.orderMovesBasedOnEvaluation(moves,array,false);
        for (Integer n:array) System.out.print(n+",");
        System.out.println();
        for (Move move:moves) System.out.print(move.toString()+",");



    }
}
