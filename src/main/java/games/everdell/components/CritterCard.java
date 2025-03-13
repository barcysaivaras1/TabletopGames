package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class CritterCard extends EverdellCard{

    //RED DESTINATION VARIABLE
    private EverdellParameters.RedDestinationLocation redDestinationAbstractLocation;
    private Integer redDestinationLocationID;


    public CritterCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
    }

    //RED DESTINATION CONSTRUCTOR
    public CritterCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        redDestinationAbstractLocation = rdl;
        redDestinationLocationID = -1;
    }

    //Copy Constructors
    public CritterCard(String name, int compID) {
        super(name, compID);
    }
    public CritterCard(EverdellParameters.RedDestinationLocation rdl, Integer rdlID, String name,  int compID) {
        super(name, compID);
        redDestinationAbstractLocation = rdl;
        redDestinationLocationID = rdlID;
    }

    public EverdellLocation getLocation(EverdellGameState state){
        if(redDestinationAbstractLocation != null){
            return (EverdellLocation) state.getComponentById(redDestinationLocationID);
        }
        return null;
    }

    @Override
    public void applyCardEffect(EverdellGameState state){
        if(redDestinationAbstractLocation != null){
            EverdellLocation location = new EverdellLocation(redDestinationAbstractLocation,1, false, redDestinationAbstractLocation.getLocationEffect(state));
            location.setOwnerId(state.getCurrentPlayer());
            state.everdellLocations.add(location);
            redDestinationLocationID = location.getComponentID();
        }
        else {
            super.applyCardEffect(state);
        }
    }

    public void copyTo(CritterCard card){
        if(redDestinationAbstractLocation != null){card.redDestinationAbstractLocation = this.redDestinationAbstractLocation;}
        super.copyTo(card);
    }

    @Override
    public CritterCard copy() {
        CritterCard card;
        if(redDestinationAbstractLocation != null){card = new CritterCard(redDestinationAbstractLocation, redDestinationLocationID, getName(),componentID);}
        else {card = new CritterCard(getName(), componentID);}

        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}
