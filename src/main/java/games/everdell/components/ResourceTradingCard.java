package games.everdell.components;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.ResourceTypes;

import java.util.HashMap;
import java.util.function.Function;

/*This is an attempt at trying to generalise cards that trade resources to gain something else
The main problem i am encountering is the fact that, i need to create a function that calls the cards effect
however to get certains cards effects i need these values that track the resources and i don't see a way where i can
target these variables within a lambda function as they can't refer to the object that they are part of*/

public class ResourceTradingCard extends CritterCard {
    
    private HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToLose;
    private HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToGain;
    
    public ResourceTradingCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction,
            boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, HashMap<EverdellParameters.ResourceTypes, Counter> rtl, HashMap<EverdellParameters.ResourceTypes, Counter> rtg, Function<EverdellGameState,
            Boolean> applyCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect);
        resourcesToLose = rtl;
        resourcesToGain = rtg;

        resourcesToGain.put(ResourceTypes.BERRY, new Counter());
        resourcesToGain.put(ResourceTypes.TWIG, new Counter());
        resourcesToGain.put(ResourceTypes.RESIN, new Counter());
        resourcesToGain.put(ResourceTypes.PEBBLE, new Counter());

        resourcesToLose.put(ResourceTypes.BERRY, new Counter());
        resourcesToLose.put(ResourceTypes.TWIG, new Counter());
        resourcesToLose.put(ResourceTypes.RESIN, new Counter());
        resourcesToLose.put(ResourceTypes.PEBBLE, new Counter());
    }

    public void applyCardEffect(EverdellGameState state){
        super.applyCardEffect(state);
    }

    public void addResourcesToLose(HashMap<ResourceTypes, Counter> rtl){
        for(ResourceTypes rt : rtl.keySet()){
            resourcesToLose.get(rt).increment(rtl.get(rt).getValue());
        }
    }
    public void addResourcesToGain(HashMap<ResourceTypes, Counter> rtg){
        for(ResourceTypes rt : rtg.keySet()){
            resourcesToGain.get(rt).increment(rtg.get(rt).getValue());
        }
    }

    
}
