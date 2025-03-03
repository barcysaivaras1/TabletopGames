package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClockTowerCard extends ConstructionCard{

    public EverdellParameters.AbstractLocations locationSelected;

    public ClockTowerCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
    }

    private ClockTowerCard(String name, int compID, EverdellParameters.AbstractLocations locationSelected) {
        super(name, compID);
        this.locationSelected = locationSelected;
    }


    public void applyCardEffect(EverdellGameState state) {
        if(locationSelected == null){
            return;
        }

        if(super.getPoints() == 0){
            return;
        }

        super.setCardPoints(super.getPoints()-1);
        state.Locations.get(locationSelected).applyLocationEffect(state);

        locationSelected = null;
    }


    public void selectLocation(EverdellParameters.AbstractLocations location) {
        locationSelected = location;
    }

    @Override
    public ClockTowerCard copy() {
        ClockTowerCard card;
        card = new ClockTowerCard(getName(), componentID, locationSelected);

        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
