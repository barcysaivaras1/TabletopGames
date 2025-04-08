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
    private EverdellParameters.RedDestinationLocation redDestinationAbstractLocation;
    private int redDestinationLocationID;


    //STANDARD CONSTRUCTOR
    public ConstructionCard(String name, CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<CardDetails> cardsThatCanOccupy) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        this.cardsThatCanOccupy = cardsThatCanOccupy;
        isOccupied = false;
    }

    //RED DESTINATION CONSTRUCTOR
    public ConstructionCard(EverdellParameters.RedDestinationLocation rdl, String name, CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<CardDetails> cardsThatCanOccupy) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        this.cardsThatCanOccupy = cardsThatCanOccupy;
        isOccupied = false;

        //RED DESTINATION VARIABLE
        this.redDestinationAbstractLocation = rdl;
        redDestinationLocationID = -1;
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
    public ConstructionCard(EverdellParameters.RedDestinationLocation rdl, Integer rdlID, String name, boolean isOccupied, ArrayList<CardDetails> cardsThatCanOccupy, int compID) {
        super(name, compID);
        this.cardsThatCanOccupy = cardsThatCanOccupy;
        this.isOccupied = isOccupied;

        //RED DESTINATION VARIABLE
        this.redDestinationAbstractLocation = rdl;
        redDestinationLocationID = rdlID;
    }


    @Override
    public void applyCardEffect(EverdellGameState state) {
        System.out.println("CONSTRUCTION CARD");
        if(redDestinationAbstractLocation != null){
            System.out.println("RED DESTINATION CARD");
            EverdellLocation location = new EverdellLocation(redDestinationAbstractLocation,1, false, redDestinationAbstractLocation.getLocationEffect(state));
            location.setOwnerId(state.getCurrentPlayer());
            state.everdellLocations.add(location);
            redDestinationLocationID = location.getComponentID();
        }
        else {
            super.applyCardEffect(state);
        }
    }

    //To be called by Card with Red Destination Locations
    public void applyCardEffect(EverdellGameState state, Consumer<EverdellGameState> specialLocationEffect) {
        EverdellLocation location = new EverdellLocation(redDestinationAbstractLocation,1, true, specialLocationEffect);
        location.setOwnerId(state.getCurrentPlayer());
        state.everdellLocations.add(location);
        redDestinationLocationID = location.getComponentID();
    }



    public boolean occupyConstruction(CritterCard card){
        if(isOccupied){
            return false;
        }
        for(CardDetails cardEnumValue : cardsThatCanOccupy){
            if(cardEnumValue == card.getCardEnumValue()){
                isOccupied = true;
                card.payForCard();
                return true;
            }
        }
        return false;
    }
    public boolean canCardOccupyThis(EverdellGameState state,EverdellCard card){
        for(CardDetails cardEnumValue : cardsThatCanOccupy){
            if(cardEnumValue == card.getCardEnumValue() && !isOccupied){
                return true;
            }
        }
        return false;
    }

    public EverdellLocation getLocation(EverdellGameState state){
        if(redDestinationAbstractLocation != null){
            return (EverdellLocation) state.getComponentById(redDestinationLocationID);
        }
        return null;
    }
    public int getRedDestinationLocationID(){
        return redDestinationLocationID;
    }

    public boolean isOccupied(){
        return isOccupied;
    }
    public ArrayList<CardDetails> getCardsThatCanOccupy(){
        return cardsThatCanOccupy;
    }


    public void copyTo(ConstructionCard card){
        ArrayList<CardDetails> cardsThatCanOccupy = new ArrayList<>(this.cardsThatCanOccupy);
        if(redDestinationAbstractLocation != null){
            card.redDestinationAbstractLocation = this.redDestinationAbstractLocation;
        }
        card.cardsThatCanOccupy = cardsThatCanOccupy;
        super.copyTo(card);
    }

    @Override
    public ConstructionCard copy() {
        ArrayList<CardDetails> cardsThatCanOccupy = new ArrayList<>(this.cardsThatCanOccupy);
        ConstructionCard card;
        if(redDestinationAbstractLocation != null){
            card = new ConstructionCard(redDestinationAbstractLocation, redDestinationLocationID, getName(), isOccupied, cardsThatCanOccupy, componentID);
        }else{
            card = new ConstructionCard(cardsThatCanOccupy, getName(), isOccupied, componentID);
        }
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
