package games.everdell;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class FunctionWrapper {


    private static ArrayList<Callable<Boolean>> functionsToRun;

    //This class is used to run a list of functions in sequence.
    //These functions might activate a GUI
    //If the function returns true, this means it will display GUI and we have to wait
    //If the function returns false, this means it will not display GUI and we can move to the next function

    private static void executeFunction(Callable<Boolean> function) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(function);
        System.out.println("Function executed");
        functionsToRun.remove(function);
        try {
            if(!future.get()){
                activateNextFunction();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }


    public static void setupListOfFunctions(ArrayList<Callable<Boolean>> functions) {
        functionsToRun = functions;
    }

    public static void addAFunction(Callable<Boolean> function) {
        functionsToRun.add(function);
    }

    public static void addAFunction(Callable<Boolean> function, int index) {
        functionsToRun.add(index, function);
    }

    public static void activateNextFunction() {
        if (!functionsToRun.isEmpty()) {
            executeFunction(functionsToRun.get(0));
        }
    }

    public static void activateNextFunction(Callable<Boolean> function) {
            System.out.println("Activating function " + function);
            addAFunction(function,0);
            executeFunction(function);
    }
}
