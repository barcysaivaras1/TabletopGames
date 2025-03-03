package games.everdell.components;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class PeddlerCard extends CritterCard {

    private HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToLose;
    private HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToGain;

    public PeddlerCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        resourcesToLose = new HashMap<>();
        resourcesToGain = new HashMap<>();

        for (EverdellParameters.ResourceTypes rt : EverdellParameters.ResourceTypes.values()) {
            resourcesToGain.put(rt, new Counter());
        }
        for (EverdellParameters.ResourceTypes rt : EverdellParameters.ResourceTypes.values()) {
            resourcesToLose.put(rt, new Counter());
        }
    }

    private PeddlerCard(String name, int compID, HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToLose, HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToGain) {
        super(name, compID);
        this.resourcesToLose = resourcesToLose;
        this.resourcesToGain = resourcesToGain;
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
        resetValues();

    }

    private void resetValues(){
        for (EverdellParameters.ResourceTypes rt : resourcesToLose.keySet()) {
            resourcesToLose.get(rt).setValue(0);
        }
        for (EverdellParameters.ResourceTypes rt : resourcesToGain.keySet()) {
            resourcesToGain.get(rt).setValue(0);
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

    @Override
    public PeddlerCard copy() {
        PeddlerCard card;

        HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToLose = new HashMap<>();
        HashMap<EverdellParameters.ResourceTypes, Counter> resourcesToGain = new HashMap<>();
        for (EverdellParameters.ResourceTypes rt : EverdellParameters.ResourceTypes.values()) {
            resourcesToGain.put(rt, new Counter());
        }
        for (EverdellParameters.ResourceTypes rt : EverdellParameters.ResourceTypes.values()) {
            resourcesToLose.put(rt, new Counter());
        }
        card = new PeddlerCard(getName(), componentID, resourcesToLose, resourcesToGain);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}