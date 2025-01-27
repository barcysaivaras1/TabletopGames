package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class MonasteryCard extends ConstructionCard{
    int selectedPlayer;

    public EverdellLocation location;

    EverdellParameters.RedDestinationLocation rdl;

    public MonasteryCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(rdl, name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, cardsThatCanOccupy);
        this.rdl = rdl;
    }


    public void applyCardEffect(EverdellGameState state) {
        //This means they are placing the card, we can assign the playerOwner
        state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof MonkCard).forEach(c -> {
            unlockSecondLocation();
        });

        this.location = new EverdellLocation(rdl,1, true, setLocationEffect(state));
        state.Locations.put(rdl, location);
        System.out.println(location);
    }

    public Consumer<EverdellGameState> setLocationEffect(EverdellGameState state){



        //Initialise the Location Effect

        //ResourceSelection will tell which resource they want to give up

        return k -> {

            int counter = 0;

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

    public void unlockSecondLocation(){
        location.setNumberOfSpaces(2);
    }

    public void lockSecondLocation(){
        location.setNumberOfSpaces(1);
    }

    //Players NEED to be set before the location EFFECT is called
    public void setPlayers(int selectedPlayer){
        this.selectedPlayer = selectedPlayer;

    }

}
