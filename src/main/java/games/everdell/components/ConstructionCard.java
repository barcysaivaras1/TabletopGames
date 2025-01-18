package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.CardDetails;

import java.util.ArrayList;

public class ConstructionCard extends EverdellCard{
    private Boolean isOccupied;
    ArrayList<CardDetails> cardsThatCanOccupy;
    public ConstructionCard(EverdellCard everdellCard, ArrayList<CardDetails> cardsThatCanOccupy) {
        super(everdellCard.getName(), everdellCard.getCardEnumValue(), everdellCard.getCardType(), everdellCard.isConstruction(), everdellCard.isUnique(), everdellCard.getPoints(), everdellCard.getResourceCost(), everdellCard.getApplyCardEffect());
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
