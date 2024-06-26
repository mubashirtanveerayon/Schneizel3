package server3.utils.jnn;
import java.util.ArrayList;


public class NeuralNetwork{

    public ArrayList<Matrix> weights,biases;
    public boolean initialized;
    public int numInputs,numOutputs,numLayers=0;
    float learningRate = 0.01f;


    public ArrayList<Activation> activationFunctions;
    public NeuralNetwork(ArrayList<Matrix>w, ArrayList<Matrix>b,ArrayList<Activation> f){
        if(w.size() != b.size() && w.size() != f.size()){
            System.out.println("invalid model");
            System.out.println("weights: "+w.size());
            System.out.println("biases: "+b.size());
            System.out.println("activation functions: "+f.size());
            return;
        }
        weights = new ArrayList<>(w);
        biases = new ArrayList<>(b);
        numInputs = w.get(0).cols;
        numOutputs = w.get(w.size()-1).rows;
        numLayers = w.size()-1;
        activationFunctions = new ArrayList<>(f);
    }

    public NeuralNetwork(int numI,int numO,Activation output){
        numInputs = numI;
        numOutputs = numO;
        weights = new ArrayList<>();
        biases = new ArrayList<>();
        activationFunctions = new ArrayList<>();
        activationFunctions.add(output);
    }

    public void initialize(){
        initialized = numLayers>0;
        if(initialized){
          System.out.println("Initialized neural network with "+numLayers+" hidden layers, "+numInputs+" inputs and "+numOutputs+" outputs!");
            System.out.println(summary());
        }
    }


    public void addLayer(int numNodes, Activation activationFunction){
        if(initialized)return;
        if(numLayers==0){
            weights.add(new Matrix(numNodes,numInputs).randomize());
            weights.add(new Matrix(numOutputs,numNodes).randomize());
            biases.add(new Matrix(numNodes,1).randomize());
            biases.add(new Matrix(numOutputs,1).randomize());
        }else{
            weights.remove(weights.size()-1);
            biases.remove(biases.size()-1);
            weights.add(new Matrix(numNodes,weights.get(weights.size()-1).rows).randomize());
            biases.add(new Matrix(numNodes,1).randomize());
            biases.add(new Matrix(numOutputs,1).randomize());
            weights.add(new Matrix(numOutputs,numNodes).randomize());
        }
        activationFunctions.add(numLayers,activationFunction);
        numLayers++;
    }



    private Matrix applyActivation(Matrix in,Activation function,boolean requireDerivative){
        switch(function){
            case SIGMOID:
                return requireDerivative? in.dSigmoidFromPreviousSigmoid():in.sigmoid();
            case RELU:
                return requireDerivative? in.dReLuFromPreviousReLu():in.reLu();
            case TANH:
                return requireDerivative? in.dTanhFromPreviousTanh():in.tanh();
            default:
                return null;
        }
    }

    public String summary(){
        if(!initialized)return "Not initialized";
        String sum = "";
        sum += "Model has "+numInputs+" input parameters, "+numLayers+" hidden layers and "+numOutputs+" output nodes.\n";
        for(int i=0;i<numLayers;i++){
            sum += "Hidden layer "+(i+1)+" uses "+activationFunctions.get(i).toString()+" activation function. Has "+weights.get(i).rows+" nodes.\n";
        }
        sum += "Output layer uses "+activationFunctions.get(numLayers).toString()+" activation function. Has "+weights.get(numLayers).rows+" nodes.\n";
        return sum;
    }

    public Matrix feedForward(Matrix input){
        if(!initialized)return null;
        Matrix result = input;
        for(int i=0;i<weights.size();i++){
            result = Matrix.mult(weights.get(i),result).add(biases.get(i));
            result = applyActivation(result,activationFunctions.get(i),false);
        }
        return result;
    }

    public void train(final Matrix input, final Matrix target){
        //feedforward
        Matrix result = input.copy();
        ArrayList<Matrix> layerOutputs = new ArrayList<>();
        layerOutputs.add(input.copy());
        for(int i=0;i<weights.size();i++){
            result = Matrix.mult(weights.get(i),result).add(biases.get(i));
            result = applyActivation(result,activationFunctions.get(i),false);
            layerOutputs.add(result.copy());
        }


        //backpropagation
        Matrix outputError = Matrix.add(target, Matrix.mult(result,-1));
        Matrix layerError = outputError;




        for(int i=layerOutputs.size()-1;i>=1;i--){
            Matrix dLayerOutput = applyActivation(layerOutputs.get(i),activationFunctions.get(i-1),true);
            Matrix gradient = layerError.copy().mult(learningRate).multElementWise(dLayerOutput);

            Matrix wDT = Matrix.mult(gradient, Matrix.transpose(layerOutputs.get(i-1)));
            Matrix w = weights.get(i-1);

            layerError = Matrix.mult(Matrix.transpose(w),layerError);
            w.add(wDT);
            Matrix b = biases.get(i-1);
            b.add(gradient);
        }

    }




}
