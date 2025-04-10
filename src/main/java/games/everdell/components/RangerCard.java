package games.everdell.components;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.actions.PlaceWorker;
import org.apache.spark.sql.sources.In;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class RangerCard extends CritterCard{

    private EverdellLocation locationFrom;
    private EverdellLocation locationTo;

    public RangerCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
    }
    private RangerCard(String name, int compID, EverdellLocation locationFrom, EverdellLocation locationTo) {
        super(name, compID);
        this.locationFrom = locationFrom;
        this.locationTo = locationTo;
    }


    public void applyCardEffect(EverdellGameState state) {
        //Ranger allows the player to move a worker from one location to another
        //It also unlocks the second cell in the Dungeon Card

        //Placing a worker on a location would work outside of here, because the location might require GUI for a human player
        //For AI, extra steps would have to be taken to ensure the effects are triggered properly
        System.out.println("Applying Ranger Card Effect");
        System.out.println("Ranger Location From: " + locationFrom);
        System.out.println("Ranger Location To: " + locationTo);

        boolean isThereDungeon = state.playerVillage.get(state.getCurrentPlayer()).stream().anyMatch(c -> c instanceof DungeonCard);
        if(isThereDungeon){
            DungeonCard dc = (DungeonCard) state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof DungeonCard).findFirst().get();
            dc.unlockSecondCell();
        }


        if(state.rangerCardMode){//This is specifically for AI play as this makes development easier
            if (locationFrom == null){
                System.out.println("Location From is null");
                return;
            }

            System.out.println("Ranger Card Mode");
            locationFrom.removePlayerFromLocation(state.getCurrentPlayer());
            state.workers[state.getCurrentPlayer()].increment();
            return;
        }

        if(locationFrom == null || locationTo == null || !(locationTo.isLocationFreeForPlayer(state))){
            return;
        }

        locationFrom.removePlayerFromLocation(state.getCurrentPlayer());
        locationTo.applyLocationEffect(state);
        locationTo.addPlayerToLocation(state.getCurrentPlayer());
        state.workers[state.getCurrentPlayer()].decrement();

    }

    //Before placing the card, the player must select which locations to move between
    public void setLocationFrom(EverdellLocation locationFrom){
        System.out.println("Setting Location From: " + locationFrom.getAbstractLocation());
        this.locationFrom = locationFrom;
    }
    public void setLocationTo(EverdellLocation locationTo){
        System.out.println("Setting Location To: " + locationTo.getAbstractLocation());
        this.locationTo = locationTo;
    }

    public EverdellLocation getLocationFrom(){
        return locationFrom;
    }

    @Override
    public void removeCardEffect(EverdellGameState state) {
        for(var card : state.playerVillage.get(state.getCurrentPlayer())){
            if(card instanceof DungeonCard dc){
                dc.lockSecondCell();
            }
        }
    }

    @Override
    public RangerCard copy() {
        RangerCard card;
        if (locationFrom == null && locationTo == null) {
            card = new RangerCard(getName(), componentID, null, null);
        }
        else if (locationTo == null) {
            card = new RangerCard(getName(), componentID, locationFrom.copy(), null);
        }
        else {
            card = new RangerCard(getName(), componentID, locationFrom.copy(), locationTo.copy());
        }
        //Calls CritterCard copy
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
