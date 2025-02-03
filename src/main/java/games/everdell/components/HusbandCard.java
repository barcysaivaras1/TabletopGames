package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class HusbandCard extends CritterCard{

    private WifeCard wife;
    private boolean increasedMaxSize;

    public HusbandCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        wife = null;
        increasedMaxSize = false;
    }


    public void applyCardEffect(EverdellGameState state) {
        for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
            if(card instanceof WifeCard){
                if(((WifeCard) card).getHusband() == null){
                    ((WifeCard) card).setHusband(this);
                }
                wife = (WifeCard) card;
            }
        }

        //If the husband has a wife, and a farm is present, the husband will allow the player to choose 1 resource
        //Resource selection will say which resource the player chose


        if(wife != null) {
            if (!increasedMaxSize) {
                state.villageMaxSize[state.getCurrentPlayer()].increment();
                increasedMaxSize = true;
                wife.setIncreasedMaxSize();
            }

            for (var resource : state.PlayerResources.keySet()) {
                if (state.PlayerResources.get(resource)[state.getCurrentPlayer()].getValue() > 0) {
                    state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment();
                    break;
                }
            }
        }
    }

    public void removeCardEffect(EverdellGameState state){
        if(wife != null){
            ((WifeCard) wife).setHusband(null);
        }

        if(increasedMaxSize){
            state.villageMaxSize[state.getCurrentPlayer()].decrement();
        }
    }

    public EverdellCard getWife(){
        return wife;
    }

    public void setWife(WifeCard wife){
        this.wife = wife;
    }

    public void setIncreasedMaxSize(){
        this.increasedMaxSize = true;
    }

}
