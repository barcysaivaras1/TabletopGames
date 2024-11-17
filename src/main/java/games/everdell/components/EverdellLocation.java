package games.everdell.components;

import core.AbstractGameState;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters.Locations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EverdellLocation {

    boolean shared;
    //boolean occupied;

    Locations location;
    public Function locationEffect;

    public List<Integer> playersOnLocation;

    public EverdellLocation(Locations location, Function locationEffect, boolean shared){
        this.location = location;
        this.locationEffect = locationEffect;
        this.shared = shared;
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
        return (!shared && playersOnLocation.isEmpty()) || (shared && !playersOnLocation.contains(state.playerTurn));
    }



}
