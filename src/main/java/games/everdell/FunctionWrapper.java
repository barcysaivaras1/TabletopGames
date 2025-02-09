package games.everdell;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class FunctionWrapper {


    private static int currentFunctionIndex = 0;
    private static ArrayList<Callable<Boolean>> functionsToRun;

    //This class is used to run a list of functions in sequence.
    //These functions might activate a GUI
    //If the function returns true, this means it will display GUI and we have to wait
    //If the function returns false, this means it will not display GUI and we can move to the next function

    private static void executeFunction(Callable<Boolean> function) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(function);
        try {
            if(!future.get()){
                activateNextFunction();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public static void setupListOfFunctions(ArrayList<Callable<Boolean>> functions) {
        currentFunctionIndex = 0;
        functionsToRun = functions;
    }

    public static void addAFunction(Callable<Boolean> function) {
        functionsToRun.add(function);
    }

    public static void activateNextFunction() {
        if (currentFunctionIndex >= functionsToRun.size()) {
            currentFunctionIndex = 0;
            functionsToRun.clear();
        }else {
            System.out.println("Activating function " + currentFunctionIndex);
            currentFunctionIndex++;
            executeFunction(functionsToRun.get(currentFunctionIndex - 1));
        }
    }
}
