package games.everdell.components;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import org.apache.spark.sql.sources.In;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class PeddlerCard extends CritterCard {

    public boolean resourcesToLoseSelected;

    private HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToLose;
    private HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToGain;

    public PeddlerCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        resourcesToLose = new HashMap<>();
        resourcesToGain = new HashMap<>();
        resourcesToLoseSelected = false;
    }

    private PeddlerCard(String name, int compID, HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToLose, HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToGain) {
        super(name, compID);
        this.resourcesToLose = resourcesToLose;
        this.resourcesToGain = resourcesToGain;
    }

    public void applyCardEffect(EverdellGameState state) {

        int counter = 0;
        //Need to make the max limit change depending on whether they give 1 or 2 resources

        //First make them pay the resources
        for (EverdellParameters.ResourceTypes rt : resourcesToLose.keySet()) {
            for(int i = 0; i < resourcesToLose.get(rt); i++){
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
            for(int i = 0; i < resourcesToGain.get(rt); i++){
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
        resourcesToLose.replaceAll((r, v) -> 0);
        resourcesToGain.replaceAll((r, v) -> 0);
    }

    public void addResourcesToLose(HashMap<EverdellParameters.ResourceTypes, Integer> rtl) {
        for (EverdellParameters.ResourceTypes rt : rtl.keySet()) {
            resourcesToLose.put(rt, rtl.get(rt));
        }
    }

    public void addResourcesToGain(HashMap<EverdellParameters.ResourceTypes, Integer> rtg) {
        for (EverdellParameters.ResourceTypes rt : rtg.keySet()) {
            resourcesToGain.put(rt, rtg.get(rt));
        }
    }

    public HashMap<EverdellParameters.ResourceTypes, Integer> getResourcesToLose() {
        return resourcesToLose;
    }

    public boolean hasSelectedResourcesToLose() {
        return resourcesToLoseSelected;
    }


    @Override
    public PeddlerCard copy() {
        PeddlerCard card;
        HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToLose = new HashMap<>(this.resourcesToLose);
        HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToGain = new HashMap<>(this.resourcesToGain);
        card = new PeddlerCard(getName(), componentID, resourcesToLose, resourcesToGain);
        card.resourcesToLoseSelected = resourcesToLoseSelected;
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}