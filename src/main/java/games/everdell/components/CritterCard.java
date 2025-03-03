package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class CritterCard extends EverdellCard{

    //RED DESTINATION VARIABLE
    private EverdellParameters.RedDestinationLocation redDestinationLocation;

    public CritterCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
    }

    //RED DESTINATION CONSTRUCTOR
    public CritterCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        redDestinationLocation = rdl;
    }

    //Copy Constructors
    public CritterCard(String name, int compID) {
        super(name, compID);
    }
    public CritterCard(EverdellParameters.RedDestinationLocation rdl, String name,  int compID) {
        super(name, compID);
        redDestinationLocation = rdl;
    }

    @Override
    public void applyCardEffect(EverdellGameState state){
        if(redDestinationLocation != null){
            state.Locations.put(redDestinationLocation, new EverdellLocation(redDestinationLocation,1, false, redDestinationLocation.getLocationEffect(state)));
        }
        else {
            super.applyCardEffect(state);
        }
    }

    public void copyTo(CritterCard card){
        if(redDestinationLocation != null){card.redDestinationLocation = this.redDestinationLocation;}
        super.copyTo(card);
    }

    @Override
    public CritterCard copy() {
        CritterCard card;
        if(redDestinationLocation != null){card = new CritterCard(redDestinationLocation, getName(),componentID);}
        else {card = new CritterCard(getName(), componentID);}

        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}
