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
    private boolean isOccupied;
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

    //Copy constructors
    public ConstructionCard(String name, int compID) {
        super(name, compID);
    }
    public ConstructionCard(ArrayList<CardDetails> cardsThatCanOccupy, String name, boolean isOccupied, int compID) {
        super(name, compID);
        this.cardsThatCanOccupy = cardsThatCanOccupy;
        this.isOccupied = isOccupied;
    }
    public ConstructionCard(EverdellParameters.RedDestinationLocation rdl, String name, boolean isOccupied, ArrayList<CardDetails> cardsThatCanOccupy, int compID) {
        super(name, compID);
        this.cardsThatCanOccupy = cardsThatCanOccupy;
        this.isOccupied = isOccupied;

        //RED DESTINATION VARIABLE
        this.redDestinationLocation = rdl;
    }


    @Override
    public void applyCardEffect(EverdellGameState state) {
        System.out.println("CONSTRUCTION CARD");
        if(redDestinationLocation != null){
            System.out.println("RED DESTINATION CARD");
            EverdellLocation location = new EverdellLocation(redDestinationLocation,1, false, redDestinationLocation.getLocationEffect(state));
            state.Locations.put(redDestinationLocation, location);
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


    public void copyTo(ConstructionCard card){
        ArrayList<CardDetails> cardsThatCanOccupy = new ArrayList<>(this.cardsThatCanOccupy);
        if(redDestinationLocation != null){
            card.redDestinationLocation = this.redDestinationLocation;
        }
        card.cardsThatCanOccupy = cardsThatCanOccupy;
        super.copyTo(card);
    }

    @Override
    public ConstructionCard copy() {
        ArrayList<CardDetails> cardsThatCanOccupy = new ArrayList<>(this.cardsThatCanOccupy);
        ConstructionCard card;
        if(redDestinationLocation != null){
            card = new ConstructionCard(redDestinationLocation, getName(), isOccupied, cardsThatCanOccupy, componentID);
        }else{
            card = new ConstructionCard(cardsThatCanOccupy, getName(), isOccupied, componentID);
        }
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
