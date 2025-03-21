package games.everdell.components;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import org.apache.spark.sql.sources.In;
import scala.Int;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class JudgeCard extends CritterCard{

    private HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToLose;
    private HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToGain;

    public JudgeCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        resourcesToLose = new HashMap<>();
        resourcesToGain = new HashMap<>();

        for (EverdellParameters.ResourceTypes rt : EverdellParameters.ResourceTypes.values()) {
            resourcesToGain.put(rt, 0);
        }
        for (EverdellParameters.ResourceTypes rt : EverdellParameters.ResourceTypes.values()) {
            resourcesToLose.put(rt, 0);
        }
    }

    private JudgeCard(String name, int compID, HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToLose, HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToGain) {
        super(name, compID);
        this.resourcesToLose = resourcesToLose;
        this.resourcesToGain = resourcesToGain;
    }



    public void applyCardEffect(EverdellGameState state) {
        System.out.println("Judge Card Effect Applied");
        System.out.println("Resources to Lose: " + resourcesToLose);
        System.out.println("Resources to Gain: " + resourcesToGain);

        int counter = 0;
        //Need to make the max limit change depending on whether they give 1 or 2 resources, NOT DONE


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
                if(counter == 1){
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

    @Override
    public JudgeCard copy() {
        JudgeCard card;
        HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToLose = new HashMap<>(this.resourcesToLose);
        HashMap<EverdellParameters.ResourceTypes, Integer> resourcesToGain = new HashMap<>(this.resourcesToGain);
        card = new JudgeCard(getName(), componentID, resourcesToLose, resourcesToGain);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
