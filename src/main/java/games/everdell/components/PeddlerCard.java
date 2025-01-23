package games.everdell.components;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Function;

public class PeddlerCard extends CritterCard {

    private HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToLose;
    private HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToGain;

    public PeddlerCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction,
                       boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, HashMap<EverdellParameters.ResourceTypes, Counter> rtl, HashMap<EverdellParameters.ResourceTypes, Counter> rtg, Function<EverdellGameState,
            Boolean> applyCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect);
        resourcesToLose = rtl;
        resourcesToGain = rtg;

        resourcesToGain.put(EverdellParameters.ResourceTypes.BERRY, new Counter());
        resourcesToGain.put(EverdellParameters.ResourceTypes.TWIG, new Counter());
        resourcesToGain.put(EverdellParameters.ResourceTypes.RESIN, new Counter());
        resourcesToGain.put(EverdellParameters.ResourceTypes.PEBBLE, new Counter());

        resourcesToLose.put(EverdellParameters.ResourceTypes.BERRY, new Counter());
        resourcesToLose.put(EverdellParameters.ResourceTypes.TWIG, new Counter());
        resourcesToLose.put(EverdellParameters.ResourceTypes.RESIN, new Counter());
        resourcesToLose.put(EverdellParameters.ResourceTypes.PEBBLE, new Counter());
    }

    public void applyCardEffect(EverdellGameState state) {

        int counter = 0;
        //Need to make the max limit change depending on whether they give 1 or 2 resources, NOT DONE


        //First make them pay the resources


        for (EverdellParameters.ResourceTypes rt : resourcesToLose.keySet()) {
            for(int i = 0; i < resourcesToLose.get(rt).getValue(); i++){
                if(counter == 2){
                    break;
                }
                state.PlayerResources.get(rt)[state.getCurrentPlayer()].decrement();
                counter++;
            }
        }

        counter = 0;
        //Then give them the resources
        for (EverdellParameters.ResourceTypes rt : resourcesToGain.keySet()) {
            for(int i = 0; i < resourcesToGain.get(rt).getValue(); i++){
                if(counter == 2){
                    break;
                }
                counter++;
                state.PlayerResources.get(rt)[state.getCurrentPlayer()].increment();
            }
        }

    }

    public void addResourcesToLose(HashMap<EverdellParameters.ResourceTypes, Counter> rtl) {
        for (EverdellParameters.ResourceTypes rt : rtl.keySet()) {
            resourcesToLose.get(rt).increment(rtl.get(rt).getValue());
        }
    }

    public void addResourcesToGain(HashMap<EverdellParameters.ResourceTypes, Counter> rtg) {
        for (EverdellParameters.ResourceTypes rt : rtg.keySet()) {
            resourcesToGain.get(rt).increment(rtg.get(rt).getValue());
        }
    }
}