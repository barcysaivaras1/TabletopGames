package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class WifeCard extends CritterCard{

    private HusbandCard husband;
    private boolean increasedMaxSize;

    public WifeCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        husband = null;
        increasedMaxSize = false;
    }
    
    private WifeCard(String name, int compID, HusbandCard husband, boolean increasedMaxSize) {
        super(name, compID);
        this.husband = husband;
        this.increasedMaxSize = increasedMaxSize;
    }


    public void applyCardEffect(EverdellGameState state) {

        if(husband == null) {
            for (EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())) {
                if (card instanceof HusbandCard) {
                    if (((HusbandCard) card).getWife() == null) {
                        ((HusbandCard) card).setWife(this);
                        setHusband((HusbandCard) card);
                    }
                }
                if(husband != null){
                    break;
                }
            }
        }

        //If the wife has a husband, she is worth 3 points
        if(husband != null){
            if(!increasedMaxSize){
                state.villageMaxSize[state.getCurrentPlayer()].increment();
                increasedMaxSize = true;
                husband.setIncreasedMaxSize();
            }
            super.setCardPoints(3);
        }
        //Otherwise nothing happens
    }

    public void removeCardEffect(EverdellGameState state){
        if(husband != null){
            ((HusbandCard) husband).setWife(null);
        }
        if(increasedMaxSize){
            state.villageMaxSize[state.getCurrentPlayer()].decrement();
        }
        super.setCardPoints(2);
    }

    public void setIncreasedMaxSize(){
        increasedMaxSize = true;
    }

    public EverdellCard getHusband(){
        return husband;
    }

    public void setHusband(HusbandCard husband){
        this.husband = husband;
    }

    @Override
    public WifeCard copy() {
        WifeCard card;
        card = new WifeCard(getName(), componentID, husband.copy(), increasedMaxSize);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}
