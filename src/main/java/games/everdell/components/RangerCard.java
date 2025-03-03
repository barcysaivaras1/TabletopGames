package games.everdell.components;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.actions.PlaceWorker;

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

        DungeonCard dc = (DungeonCard) state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof DungeonCard).findFirst().get();
        if(dc != null){
            dc.unlockSecondCell();
        }

        if(locationFrom == null || locationTo == null || !(locationTo.isLocationFreeForPlayer(state))){
            return;
        }

        locationFrom.playersOnLocation.remove(state.getCurrentPlayer());
        locationTo.applyLocationEffect(state);
        locationTo.playersOnLocation.add(state.getCurrentPlayer());
        state.workers[state.getCurrentPlayer()].decrement();

    }

    //Before placing the card, the player must select which locations to move between
    public void setLocationFrom(EverdellLocation locationFrom){
        this.locationFrom = locationFrom;
    }
    public void setLocationTo(EverdellLocation locationTo){
        this.locationTo = locationTo;
    }


    public void removeCardEffect(EverdellGameState state) {
        DungeonCard dc = (DungeonCard) state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof DungeonCard).findFirst().get();
        dc.lockSecondCell();
    }

    @Override
    public RangerCard copy() {
        RangerCard card;
        card = new RangerCard(getName(), componentID, locationFrom.copy(), locationTo.copy());
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
