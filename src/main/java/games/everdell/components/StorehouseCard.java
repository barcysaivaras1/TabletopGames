package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import org.apache.spark.sql.sources.In;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class StorehouseCard extends ConstructionCard{

    HashMap<EverdellParameters.ResourceTypes, Integer> resourceStorage;

    public StorehouseCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(rdl, name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
        resourceStorage = new HashMap<>();

        for (EverdellParameters.ResourceTypes rt : EverdellParameters.ResourceTypes.values()) {
            resourceStorage.put(rt, 0);
        }
    }

    private StorehouseCard(String name, int compID, HashMap<EverdellParameters.ResourceTypes, Integer> resourceStorage) {
        super(name, compID);
        this.resourceStorage = resourceStorage;
    }


    public void applyCardEffect(EverdellGameState state) {
        //When playing the card and each green production, the player has a choice
        //Store 3 Twigs from the general supply on this card
        //Store 2 Resin from the general supply on this card
        //Store 1 Pebble from the general supply on this card
        //Store 2 Berry from the general supply on this card

        //Resource Selection from the state will represent which resource the player wants to store currently

        for(EverdellParameters.ResourceTypes rt : state.resourceSelection.keySet()){
            if(state.resourceSelection.get(rt).getValue() > 0){
                int valueToAdd;
                if(rt == EverdellParameters.ResourceTypes.TWIG){
                    valueToAdd = 3;
                }
                else if (rt == EverdellParameters.ResourceTypes.RESIN){
                    valueToAdd = 2;
                }
                else if (rt == EverdellParameters.ResourceTypes.PEBBLE){
                    valueToAdd = 1;
                }
                else{ // Berry
                    valueToAdd = 2;
                }
                resourceStorage.put(rt, resourceStorage.get(rt)+valueToAdd);
                break;
            }
        }


        //Set the location of the card if it is not already set
        if(super.getRedDestinationLocationID() == -1) {
            super.applyCardEffect(state, setLocationEffect(state));
        }
    }

    public Consumer<EverdellGameState> setLocationEffect(EverdellGameState state){
        //When placing a worker, the player claims all the resources stored on this card

        return k -> {
            for(EverdellParameters.ResourceTypes rt : resourceStorage.keySet()){
                state.PlayerResources.get(rt)[state.getCurrentPlayer()].increment(resourceStorage.get(rt));
                resourceStorage.put(rt, 0);
            }
        };
    }

    @Override
    public void removeCardEffect(EverdellGameState state){
        state.everdellLocations.remove(getLocation(state));
    }

    @Override
    public StorehouseCard copy() {
        HashMap<EverdellParameters.ResourceTypes, Integer> resourceStorageCopy = new HashMap<>();
        for (EverdellParameters.ResourceTypes rt : EverdellParameters.ResourceTypes.values()) {
            resourceStorageCopy.put(rt, resourceStorage.get(rt));
        }

        StorehouseCard card = new StorehouseCard(getName(), componentID, resourceStorageCopy);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
