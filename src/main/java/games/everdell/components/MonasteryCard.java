package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class MonasteryCard extends ConstructionCard{
    int selectedPlayer;


    public MonasteryCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(rdl, name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
        selectedPlayer = -1;
    }

    private MonasteryCard(String name, int compID, int selectedPlayer) {
        super(name, compID);
        this.selectedPlayer = selectedPlayer;
    }


    public void applyCardEffect(EverdellGameState state) {
        super.applyCardEffect(state, setLocationEffect(state));
        state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof MonkCard).forEach(c -> {
            unlockSecondLocation(state);
        });
    }

    public Consumer<EverdellGameState> setLocationEffect(EverdellGameState state){
        //Initialise the Location Effect

        //ResourceSelection will tell which resource they want to give up

        return k -> {

            int counter = 0;

            System.out.println("Player selected to donate to is : " + selectedPlayer);

            //Transfer 2 resources from the current player to the selected player
            for(var resource : state.resourceSelection.keySet()){
                for(int i = 0; i < state.resourceSelection.get(resource).getValue(); i++){
                    if (counter == 2){
                        break;
                    }
                    state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement();
                    state.PlayerResources.get(resource)[selectedPlayer].increment();
                    counter++;
                }
            }

            //Give the current player 4 points
            if(counter == 2){
                state.pointTokens[state.getCurrentPlayer()].increment(4);
            }
            else{
                System.out.println("Location : MonasteryCard");
                System.out.println("Problem : Not enough resources selected");
            }


        };
    }

    public void unlockSecondLocation(EverdellGameState state){
        System.out.println("Unlocking Second Location");
        super.getLocation(state).setNumberOfSpaces(2);
        System.out.println(super.getLocation(state).getNumberOfSpaces());
    }

    public void lockSecondLocation(EverdellGameState state){
        super.getLocation(state).setNumberOfSpaces(1);
    }

    //Players NEED to be set before the location EFFECT is called
    public void setPlayers(int selectedPlayer){
        this.selectedPlayer = selectedPlayer;

    }

    @Override
    public void removeCardEffect(EverdellGameState state){
        state.everdellLocations.remove(getLocation(state));
    }

    @Override
    public MonasteryCard copy() {
        MonasteryCard card;
        card = new MonasteryCard(getName(), componentID, selectedPlayer);

        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
