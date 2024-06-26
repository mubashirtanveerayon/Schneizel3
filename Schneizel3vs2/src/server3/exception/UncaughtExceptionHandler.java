package server3.exception;

import evaluation.network.EvaluationNetwork;
public class UncaughtExceptionHandler  implements Thread.UncaughtExceptionHandler{

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if(EvaluationNetwork.isTraining) {
            EvaluationNetwork.save();
        }
        e.printStackTrace();
        System.exit(0);
    }


}
