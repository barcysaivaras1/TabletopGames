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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class EverdellLocation extends Component {

    int numberOfSpaces;

    boolean canTheSamePlayerBeOnLocationMultipleTimes;

    private final AbstractLocations location;

    private Consumer<EverdellGameState> locationEffect;

    public ArrayList<Integer> playersOnLocation;


    //STANDARD CONSTRUCTOR
    public EverdellLocation(AbstractLocations location, int numberOfSpaces, boolean canTheSamePlayerBeOnLocationMultipleTimes, Consumer<EverdellGameState> locationEffect){
        super(CoreConstants.ComponentType.LOCATION);
        this.location = location;
        this.locationEffect = locationEffect;
        this.numberOfSpaces = numberOfSpaces;
        this.playersOnLocation = new ArrayList<>();
        this.canTheSamePlayerBeOnLocationMultipleTimes = canTheSamePlayerBeOnLocationMultipleTimes;
    }

    //COPY CONSTRUCTOR
    public EverdellLocation(AbstractLocations location, int numberOfSpaces, boolean canTheSamePlayerBeOnLocationMultipleTimes, Consumer<EverdellGameState> locationEffect, ArrayList<Integer> playersOnLocation, int compID){
        super(CoreConstants.ComponentType.LOCATION, compID);
        this.location = location;
        this.locationEffect = locationEffect;
        this.numberOfSpaces = numberOfSpaces;
        this.canTheSamePlayerBeOnLocationMultipleTimes = canTheSamePlayerBeOnLocationMultipleTimes;
        this.playersOnLocation = new ArrayList<>(playersOnLocation);
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


        if(location instanceof EverdellParameters.RedDestinationLocation){
            System.out.println("IS RED DESTINATION LOCATION");
            if(location != EverdellParameters.RedDestinationLocation.INN_DESTINATION && location != EverdellParameters.RedDestinationLocation.POST_OFFICE_DESTINATION){
                System.out.println("IS NOT INN OR POST OFFICE");
                //We need to check if the player owns this location, as this is not a public location
                    if(ownerId == state.getCurrentPlayer()){
                        System.out.println("IS LOCATION FREE : "+((isThereSpace && !playersOnLocation.contains(state.getCurrentPlayer())) || (canTheSamePlayerBeOnLocationMultipleTimes  && isThereSpace)));
                        System.out.println("Players on the Location : "+ location + " : " + playersOnLocation);
                        return (isThereSpace && !playersOnLocation.contains(state.getCurrentPlayer())) || (canTheSamePlayerBeOnLocationMultipleTimes  && isThereSpace);
                    }
                return false;
            }
        }

        System.out.println("Is the location free for the player : "+((isThereSpace && !playersOnLocation.contains(state.getCurrentPlayer())) || (canTheSamePlayerBeOnLocationMultipleTimes  && isThereSpace)));
        System.out.println("Players on the Location : "+ location + " : " + playersOnLocation);
        System.out.println("Component Id : " + componentID);
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

    //Red Destination Helper Function
    public static Integer findCardLinkedToLocation(EverdellGameState state, EverdellLocation locationToLookFor){
        if(!(locationToLookFor.getAbstractLocation() instanceof EverdellParameters.RedDestinationLocation)){
            throw new RuntimeException("Location provided is not a Red Destination Location || Invalid Call || Location Given -> "+locationToLookFor.getAbstractLocation());
        }
        for(var playerDeck : state.playerVillage) {
            for (var card : playerDeck) {
                if (card instanceof ConstructionCard cc) {
                    if (cc.getLocation(state) != null) {
                        if (cc.getLocation(state).getComponentID() == locationToLookFor.getComponentID()) {
                            return cc.getComponentID();
                        }
                    }
                } else if (card instanceof CritterCard cc) {
                    if(cc.getLocation(state) != null) {
                        if (cc.getLocation(state) == locationToLookFor) {
                            if (cc.getLocation(state).getComponentID() == locationToLookFor.getComponentID()) {
                                return cc.getComponentID();
                            }
                        }
                    }
                }
            }
        }
        throw new RuntimeException("There is no card placed that matches this location || Invalid Call || Location Given -> "+locationToLookFor.getAbstractLocation());
    }

    public void setNumberOfSpaces(int numberOfSpaces){
        this.numberOfSpaces = numberOfSpaces;
    }

    public EverdellLocation copy(){
        EverdellLocation copy = new EverdellLocation(location, numberOfSpaces, canTheSamePlayerBeOnLocationMultipleTimes, locationEffect, playersOnLocation, componentID);
        copyComponentTo(copy);
        return copy;
    }
}
