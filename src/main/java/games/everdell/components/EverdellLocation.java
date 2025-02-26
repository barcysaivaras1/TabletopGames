package games.everdell.components;

import core.AbstractGameState;
import core.CoreConstants;
import core.components.Component;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.AbstractLocations;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.EverdellParameters.ForestLocations;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class EverdellLocation extends Component {

    boolean shared;
    int numberOfSpaces;
    //boolean occupied;
    boolean canTheSamePlayerBeOnLocationMultipleTimes;

    private final AbstractLocations location;

    private Consumer<EverdellGameState> locationEffect;


    public List<Integer> playersOnLocation;

    public EverdellLocation(AbstractLocations location, int numberOfSpaces, boolean canTheSamePlayerBeOnLocationMultipleTimes, Consumer<EverdellGameState> locationEffect){
        super(CoreConstants.ComponentType.LOCATION);
        this.location = location;
        //this.shared = shared;
        this.locationEffect = locationEffect;
        this.numberOfSpaces = numberOfSpaces;
        this.playersOnLocation = new ArrayList<>();
        this.canTheSamePlayerBeOnLocationMultipleTimes = canTheSamePlayerBeOnLocationMultipleTimes;
    }
    public EverdellLocation(AbstractLocations location, int numberOfSpaces, boolean canTheSamePlayerBeOnLocationMultipleTimes, Consumer<EverdellGameState> locationEffect, int compID){
        super(CoreConstants.ComponentType.LOCATION, compID);
        this.location = location;
        //this.shared = shared;
        this.locationEffect = locationEffect;
        this.numberOfSpaces = numberOfSpaces;
        this.playersOnLocation = new ArrayList<>();
        this.canTheSamePlayerBeOnLocationMultipleTimes = canTheSamePlayerBeOnLocationMultipleTimes;
    }

    public void applyLocationEffect(EverdellGameState state){
        locationEffect.accept(state);
    }
    public void setLocationEffect(Consumer<EverdellGameState> locationEffect){
        this.locationEffect = locationEffect;
    }

    public boolean isLocationFreeForPlayer(AbstractGameState gs){
        EverdellGameState state = (EverdellGameState) gs;
        boolean isThereSpace = numberOfSpaces > playersOnLocation.size();
        System.out.println("isThereSpace at "+location+": " + isThereSpace);
        return (isThereSpace && !playersOnLocation.contains(state.getCurrentPlayer())) || (canTheSamePlayerBeOnLocationMultipleTimes  && isThereSpace);
    }

    public boolean isPlayerOnLocation(EverdellGameState state){
        return playersOnLocation.contains(state.getCurrentPlayer());
    }

    public AbstractLocations getAbstractLocation(){
        return location;
    }
    public int getNumberOfSpaces(){
        return numberOfSpaces;
    }
    public boolean canTheSamePlayerBeOnLocationMultipleTimes(){
        return canTheSamePlayerBeOnLocationMultipleTimes;
    }

    public void setNumberOfSpaces(int numberOfSpaces){
        this.numberOfSpaces = numberOfSpaces;
    }


    public EverdellLocation copy(){
        EverdellLocation copy = new EverdellLocation(location, numberOfSpaces, canTheSamePlayerBeOnLocationMultipleTimes, locationEffect, componentID);
        copy.playersOnLocation = new ArrayList<>();
        copy.playersOnLocation.addAll(playersOnLocation);
        return copy;
    }

}
