package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class HusbandCard extends CritterCard{

    private Integer wifeId;
    private boolean increasedMaxSize;

    public HusbandCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        wifeId = null;
        increasedMaxSize = false;
    }

    private HusbandCard(String name, int compID, Integer wifeId, boolean increasedMaxSize) {
        super(name, compID);
        this.wifeId = wifeId;
        this.increasedMaxSize = increasedMaxSize;
    }


    public void applyCardEffect(EverdellGameState state) {
        //If the husband has a wife, and a farm is present, the husband will allow the player to choose 1 resource
        //Resource selection will say which resource the player chose

        if(findWife(state)) {
            System.out.println("Found a Wife!");
            if (!increasedMaxSize) {
                WifeCard wife = (WifeCard) state.getComponentById(wifeId);
                state.villageMaxSize[state.getCurrentPlayer()].increment();
                increasedMaxSize = true;
                wife.setIncreasedMaxSize();
            }

            if (isThereAFarm(state)) {
                for (var resource : state.resourceSelection.keySet()) {
                    if (state.resourceSelection.get(resource).getValue() > 0) {
                        state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment();
                        break;
                    }
                }
            }
        }
    }

    public void removeCardEffect(EverdellGameState state){
        if(wifeId != null){
            WifeCard wife = (WifeCard) state.getComponentById(wifeId);
            wife.setHusband(null);
        }

        if(increasedMaxSize){
            state.villageMaxSize[state.getCurrentPlayer()].decrement();
        }
    }

    public boolean isThereAFarm(EverdellGameState state){
        for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
            if(card.getCardEnumValue() == EverdellParameters.CardDetails.FARM){
                return true;
            }
        }
        return false;
    }

    public boolean findWife(EverdellGameState state){
        if(wifeId == null) {
            for (EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())) {
                if (card instanceof WifeCard) {
                    if (((WifeCard) card).getHusband() == null) {
                        ((WifeCard) card).setHusband(this);
                        setWife((WifeCard) card);
                    }
                }
                if(wifeId != null) {
                    break;
                }
            }
        }

        return wifeId != null;
    }

    public Integer getWife(){
        return wifeId;
    }

    public void setWife(WifeCard wife){
        this.wifeId = wife.getComponentID();
    }

    public void setIncreasedMaxSize(){
        this.increasedMaxSize = true;
    }

    @Override
    public HusbandCard copy() {
        HusbandCard card;
        if(wifeId == null){
            card = new HusbandCard(getName(), componentID, null, increasedMaxSize);
        } else {
            card = new HusbandCard(getName(), componentID, wifeId, increasedMaxSize);
        }
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
