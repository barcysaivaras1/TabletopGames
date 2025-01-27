package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class CritterCard extends EverdellCard{

    //RED DESTINATION VARIABLE
    private EverdellParameters.RedDestinationLocation redDestinationLocation;

    public CritterCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect);
    }

    //RED DESTINATION CONSTRUCTOR
    public CritterCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect);
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
}
