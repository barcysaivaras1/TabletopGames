package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class WifeCard extends CritterCard{

    private Integer husbandId;
    private boolean increasedMaxSize;

    public WifeCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        husbandId = null;
        increasedMaxSize = false;
    }
    
    private WifeCard(String name, int compID, Integer husbandId, boolean increasedMaxSize) {
        super(name, compID);
        this.husbandId = husbandId;
        this.increasedMaxSize = increasedMaxSize;
    }


    public void applyCardEffect(EverdellGameState state) {

        if(husbandId == null) {
            for (EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())) {
                if (card.getCardEnumValue() == EverdellParameters.CardDetails.HUSBAND) {
                    System.out.println("HUSBAND FOUND");
                    if(((HusbandCard) card).getWife() == null) {
                        System.out.println("HUSBAND IS FREE");
                        ((HusbandCard) card).setWife(this);
                        setHusband((HusbandCard) card);
                    }
                }
                if(husbandId != null){
                    break;
                }
            }
        }

        //If the wife has a husband, she is worth 3 points
        if(husbandId != null){
            if(!increasedMaxSize){
                state.villageMaxSize[state.getCurrentPlayer()].increment();
                increasedMaxSize = true;
                HusbandCard husband = (HusbandCard) state.getComponentById(husbandId);
                husband.setIncreasedMaxSize();
            }
        }
        //Otherwise nothing happens
    }

    public void removeCardEffect(EverdellGameState state){
        if(husbandId != null){
            EverdellCard husband = (EverdellCard) state.getComponentById(husbandId);
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

    public Integer getHusband(){
        return husbandId;
    }

    public void setHusband(HusbandCard husband){
        if (husband != null) {
            super.setCardPoints(3);
        }
        this.husbandId = husband.getComponentID();
    }

    @Override
    public WifeCard copy() {
        WifeCard card;
        if(husbandId != null){
            card = new WifeCard(getName(), componentID, husbandId, increasedMaxSize);
        }
        else {
            card = new WifeCard(getName(), componentID, null, increasedMaxSize);
        }
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}
