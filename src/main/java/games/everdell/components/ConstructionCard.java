package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.CardDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class ConstructionCard extends EverdellCard{
    private Boolean isOccupied;
    ArrayList<CardDetails> cardsThatCanOccupy;
    public ConstructionCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Function<EverdellGameState, Boolean> checkIfEffectApplies, ArrayList<CardDetails> cardsThatCanOccupy) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, checkIfEffectApplies);
        this.cardsThatCanOccupy = cardsThatCanOccupy;
        isOccupied = false;
    }

    public boolean occupyConstruction(CritterCard card){
        if(isOccupied){
            return false;
        }
        for(EverdellParameters.CardDetails cardEnumValue : cardsThatCanOccupy){
            if(cardEnumValue == card.getCardEnumValue()){
                isOccupied = true;
                card.payForCard();
                return true;
            }
        }
        return false;
    }
    public boolean canCardOccupyThis(EverdellGameState state){

        for(EverdellParameters.CardDetails cardEnumValue : cardsThatCanOccupy){
            if(cardEnumValue == state.currentCard.getCardEnumValue() && !isOccupied){
                return true;
            }
        }
        return false;
    }

    public boolean isOccupied(){
        return isOccupied;
    }
    public ArrayList<CardDetails> getCardsThatCanOccupy(){
        return cardsThatCanOccupy;
    }


}
