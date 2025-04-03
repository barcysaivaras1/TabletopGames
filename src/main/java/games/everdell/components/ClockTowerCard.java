package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClockTowerCard extends ConstructionCard{

    private int locationSelectedId;

    public ClockTowerCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
        locationSelectedId = -1;
    }

    private ClockTowerCard(String name, int compID, int locationSelectedId) {
        super(name, compID);
        this.locationSelectedId = locationSelectedId;
    }


    public void applyCardEffect(EverdellGameState state) {
        if(locationSelectedId == -1){
            return;
        }

        if(super.getPoints() == 0){
            return;
        }

        super.setCardPoints(super.getPoints()-1);

        EverdellLocation location = (EverdellLocation) state.getComponentById(locationSelectedId);
        location.applyLocationEffect(state);
        locationSelectedId = -1;
    }

    public boolean canPerformAction(){
        return getPoints() > 0;
    }

    public void selectLocation(int locationId) {
        locationSelectedId = locationId;
    }

    @Override
    public ClockTowerCard copy() {
        ClockTowerCard card;
        card = new ClockTowerCard(getName(), componentID, locationSelectedId);

        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
