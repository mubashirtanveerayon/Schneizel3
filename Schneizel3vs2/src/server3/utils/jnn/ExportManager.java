package server3.utils.jnn;
import java.io.*;
import java.util.*;

public class ExportManager {

    public static void saveText(String text,String filePath){
        try(BufferedWriter br=new BufferedWriter(new FileWriter(filePath))){
            br.write(text);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean exportModel(NeuralNetwork nn,String path){

            if(!nn.initialized)return false;


            File directory = new File(path);
            if(directory.exists() && directory.isDirectory()){

            }else{
                if(!directory.mkdir()){
                    System.out.println("Could not create directory");
                    return false;
                }
            }

            for(int i=0;i<=nn.numLayers;i++){
                String text = nn.activationFunctions.get(i).toString()+"\n";
                text += nn.weights.get(i).toString();
                saveText(text, directory.getPath()+File.separator+"layer"+i+".weight");
                text = nn.biases.get(i).toString();
                saveText(text, directory.getPath()+File.separator+"layer"+i+".bias");
            }
            return true;

    }


    public static NeuralNetwork createModel(String path){
        File dir = new File(path);
        if(!dir.exists() || !dir.isDirectory()){
            System.out.println("No such directory");
            return null;
        }
        ArrayList<Matrix> weights = new ArrayList<>();
        ArrayList<Matrix> biases = new ArrayList<>();
        ArrayList<Activation> functions = new ArrayList<Activation>();


        File[] files = dir.listFiles();
        HashMap<Integer,File> weightsMap = new HashMap<>();
        HashMap<Integer,File> biasesMap = new HashMap<>();
        for(File file:files){
            String fileName = file.getName();
            if(fileName.contains("layer") ){
                if (fileName.endsWith(".weight")) {
                    weightsMap.put(Integer.parseInt(fileName.split(".weight")[0].split("layer")[1]),file);
                } else if (fileName.endsWith(".bias")) {
                    biasesMap.put(Integer.parseInt(fileName.split(".bias")[0].split("layer")[1]),file);
                }
            }
        }


        int numLayers = weightsMap.size();
        if(numLayers!= biasesMap.size()){
            System.out.println("Incompatible size of layers");
            return null;
        }

        List<Integer> weightKeys = new ArrayList( weightsMap.keySet());
        Collections.sort(weightKeys);
        List<Integer> biasKeys = new ArrayList(biasesMap.keySet());
        Collections.sort(biasKeys);
        for(int i=0;i<numLayers;i++){
            File weightFile = weightsMap.get(weightKeys.get(i));
            String line;
            ArrayList<String> matrixData = new ArrayList<>();
            try(BufferedReader reader = new BufferedReader(new FileReader(weightFile))){

                int lineNumber = 1;
                while((line = reader.readLine())!=null){
                    if(lineNumber == 1){

                        line = line.trim().toUpperCase();
                        for(Activation func:Activation.values()){
                            if(line.equals(func.toString())){
                                functions.add(func);
                                break;
                            }
                        }
                        if(functions.size() == i){
                            System.out.println("Invalid model file");
                            return null;
                        }

                        lineNumber++;
                    }else{
                        matrixData.add(line);
                        if(lineNumber == 3){
                            weights.add(new Matrix(matrixData));
                            break;
                        }
                        lineNumber++;
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
            matrixData.clear();
            File biasFile = biasesMap.get(biasKeys.get(i));
            try(BufferedReader reader = new BufferedReader(new FileReader(biasFile))){
                while((line = reader.readLine()) != null){
                    matrixData.add(line);
                }
                biases.add(new Matrix(matrixData));
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }


        return new NeuralNetwork(weights,biases,functions);
    }

}
