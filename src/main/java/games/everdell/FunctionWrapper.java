package games.everdell;

import com.beust.ah.A;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class FunctionWrapper {


    private static ArrayList<Callable<Boolean>> functionsToRun;

    private static HashMap<Callable<Boolean>, String> functionDescription;

    //This class is used to run a list of functions in sequence.
    //These functions might activate a GUI
    //If the function returns true, this means it will display GUI and we have to wait
    //If the function returns false, this means it will not display GUI and we can move to the next function



    private static void executeFunction(Callable<Boolean> function) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(function);
        printAllFunctions();
        functionsToRun.remove(function);
        functionDescription.remove(function);
        try {
            if(!future.get()){
                activateNextFunction();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }


    public static void setupFunctionWrapper(){
        functionsToRun = new ArrayList<>();
        functionDescription = new HashMap<>();
    }
    public static void addAFunction(Callable<Boolean> function, String name) {
        functionsToRun.add(function);
        functionDescription.put(function, name);
    }

    public static void addAFunction(Callable<Boolean> function, String name, int index) {
        functionsToRun.add(index, function);
        functionDescription.put(function, name);
    }

    public static void activateNextFunction() {
        if (!functionsToRun.isEmpty()) {
            executeFunction(functionsToRun.get(0));
        }
    }

    public static void activateNextFunction(Callable<Boolean> function, String name) {
            addAFunction(function, name, 0);
            executeFunction(functionsToRun.get(0));
    }

    public static void setDescriptor(Callable<Boolean> function, String name){
        functionDescription.put(function,name);
    }

    public static String getDescriptor(Callable<Boolean> function){
        return functionDescription.get(function);
    }

    public static void printAllFunctions(){
        System.out.println();
        for(var function : functionsToRun){
            System.out.print(functionDescription.get(function)+" -> ");
        }
        System.out.println();
    }
}
