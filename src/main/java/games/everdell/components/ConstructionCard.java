package games.everdell.components;

import games.dominion.cards.CardType;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.CardDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConstructionCard extends EverdellCard{
    private Boolean isOccupied;
    ArrayList<CardDetails> cardsThatCanOccupy;

    //RED DESTINATION VARIABLE
    private EverdellParameters.RedDestinationLocation redDestinationLocation;


    //STANDARD CONSTRUCTOR
    public ConstructionCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<CardDetails> cardsThatCanOccupy) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        this.cardsThatCanOccupy = cardsThatCanOccupy;
        isOccupied = false;
    }

    //RED DESTINATION CONSTRUCTOR
    public ConstructionCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<CardDetails> cardsThatCanOccupy) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        this.cardsThatCanOccupy = cardsThatCanOccupy;
        isOccupied = false;

        //RED DESTINATION VARIABLE
        this.redDestinationLocation = rdl;
    }


    @Override
    public void applyCardEffect(EverdellGameState state) {
        System.out.println("CONSTRUCTION CARD");
        if(redDestinationLocation != null){
            System.out.println("RED DESTINATION CARD");
            state.Locations.put(redDestinationLocation, new EverdellLocation(redDestinationLocation,1, false, redDestinationLocation.getLocationEffect(state)));
        }
        else {
            super.applyCardEffect(state);
        }
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
    public boolean canCardOccupyThis(EverdellGameState state,EverdellCard card){

        for(EverdellParameters.CardDetails cardEnumValue : cardsThatCanOccupy){
            if(cardEnumValue == card.getCardEnumValue() && !isOccupied){
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
