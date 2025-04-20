package games.everdell.components;

import core.AbstractGameState;
import core.CoreConstants;
import core.components.Component;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.AbstractLocations;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.EverdellParameters.ForestLocations;
import org.apache.spark.sql.sources.In;

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

    private ArrayList<Integer> playersOnLocation;


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
        this.playersOnLocation = playersOnLocation;
    }

    public void applyLocationEffect(EverdellGameState state){
        System.out.println("STATE Card Selection in PlaceWorker 3: " + state.cardSelection);
        locationEffect.accept(state);
    }
    public void setLocationEffect(Consumer<EverdellGameState> locationEffect){
        this.locationEffect = locationEffect;
    }

    public boolean isLocationFreeForPlayer(AbstractGameState gs){
        EverdellGameState state = (EverdellGameState) gs;
        boolean isThereSpace = numberOfSpaces > playersOnLocation.size();


        if(location instanceof EverdellParameters.RedDestinationLocation){
            if(location != EverdellParameters.RedDestinationLocation.INN_DESTINATION && location != EverdellParameters.RedDestinationLocation.POST_OFFICE_DESTINATION){
                //We need to check if the player owns this location, as this is not a public location
                    if(ownerId == state.getCurrentPlayer()){
                        return (isThereSpace && !playersOnLocation.contains(state.getCurrentPlayer())) || (canTheSamePlayerBeOnLocationMultipleTimes  && isThereSpace);
                    }
                return false;
            }
        }
        return (isThereSpace && !playersOnLocation.contains(state.getCurrentPlayer())) || (canTheSamePlayerBeOnLocationMultipleTimes  && isThereSpace);
    }

    public boolean isPlayerOnLocation(int playerId){
        return playersOnLocation.contains(playerId);
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
    public static int findCardLinkedToLocation(EverdellGameState state, EverdellLocation locationToLookFor){
        if(!(locationToLookFor.getAbstractLocation() instanceof EverdellParameters.RedDestinationLocation)){
            throw new RuntimeException("Location provided is not a Red Destination Location || Invalid Call || Location Given -> "+locationToLookFor.getAbstractLocation());
        }
        for(var playerDeck : state.playerVillage) {
            for (var card : playerDeck) {
                System.out.println("Card: "+card.getCardEnumValue());
                System.out.println("Card ID: "+card.getComponentID());

                if (card instanceof ConstructionCard cc) {
                    System.out.println("Is Construction Card");

                    if (cc.getLocation(state) != null) {
                        System.out.println("Location: "+cc.getLocation(state).getAbstractLocation());
                        System.out.println("Location ID: "+cc.getLocation(state).getComponentID());
                        System.out.println("Location to look for ID: "+locationToLookFor.getComponentID());
                        if (cc.getLocation(state).getComponentID() == locationToLookFor.getComponentID()) {
                            System.out.println("Found Location ID: "+cc.getLocation(state).getComponentID());
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
        //Print out all the locations and their IDS
        System.out.println("Locations in the game:");
        for(var locations : state.everdellLocations){
            System.out.println("Location: "+locations.getAbstractLocation());
            System.out.println("Location ID: "+locations.getComponentID());
        }

        throw new RuntimeException("There is no card placed that matches this location || Invalid Call || Location Given -> "+locationToLookFor.getAbstractLocation());
    }

    public void removePlayerFromLocation(int playerId){
        //If the location is a basic event, we need to remove the player from the location
        //And make it no longer available, as it has been claimed
        //(This will most likely apply to special events aswell)
        if(getAbstractLocation() instanceof EverdellParameters.BasicEvent){
            setNumberOfSpaces(0);
        }
        playersOnLocation.removeIf(player -> player == playerId);
    }

    public void addPlayerToLocation(int playerId){
        playersOnLocation.add(playerId);
    }

    public ArrayList<Integer> getPlayersOnLocation(){
        return playersOnLocation;
    }

    public void setNumberOfSpaces(int numberOfSpaces){
        this.numberOfSpaces = numberOfSpaces;
    }

    public EverdellLocation copy(){
        //Create a copy of players on location
        ArrayList<Integer> playersOnLocation = new ArrayList<>(this.playersOnLocation);

        EverdellLocation copy = new EverdellLocation(location, numberOfSpaces, canTheSamePlayerBeOnLocationMultipleTimes, locationEffect, playersOnLocation, componentID);
        copyComponentTo(copy);
        return copy;
    }
}
