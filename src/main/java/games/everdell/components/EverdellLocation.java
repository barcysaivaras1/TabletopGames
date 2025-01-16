package games.everdell.components;

import core.AbstractGameState;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.Locations;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.EverdellParameters.ForestLocations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EverdellLocation {

    boolean shared;
    int numberOfSpaces;
    //boolean occupied;

    private Locations location;


    public List<Integer> playersOnLocation;

    public EverdellLocation(Locations location, int numberOfSpaces){
        this.location = location;
        //this.shared = shared;
        this.numberOfSpaces = numberOfSpaces;
        this.playersOnLocation = new ArrayList<>();
    }

//    public void placeWorkerOnLocation(AbstractGameState gs){
//        EverdellGameState state = (EverdellGameState) gs;
//
//        //If the location is not shared and is empty run the code
//        //If the location is shared and the player has not placed a worker on it, run the code
//        if(isLocationFreeForPlayer(state)) {
//            playersOnLocation.add(((EverdellGameState) gs).playerTurn);
//            locationEffect.apply(location);
//        }
//    }

    public boolean isLocationFreeForPlayer(AbstractGameState gs){
        EverdellGameState state = (EverdellGameState) gs;
        return (numberOfSpaces > playersOnLocation.size() && !playersOnLocation.contains(state.playerTurn));
    }

    public Locations getLocation(){
        return location;
    }



}
