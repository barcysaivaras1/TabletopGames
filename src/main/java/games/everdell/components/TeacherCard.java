package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class TeacherCard extends CritterCard{


    private int selectedPlayer;

    public TeacherCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
    }


    public void applyCardEffect(EverdellGameState state) {
        //The player draw 2 cards from the deck, and then gives 1 card to another player

        //Card Selection holds the 2 cards, Card [0[ is the card the player will keep
        //Card [1] is the card the player will give to another player

        if(state.cardSelection.isEmpty()){
            return;
        }

        //Store the card in the players hand
        if(state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(state.getCurrentPlayer()).getCapacity()) {
            state.playerHands.get(state.getCurrentPlayer()).add(state.cardSelection.get(0));
            state.cardCount[state.getCurrentPlayer()].increment();
        }

        //Give 1 card to another player
        if(state.playerHands.get(selectedPlayer).getSize() < state.playerHands.get(selectedPlayer).getCapacity()){
            state.playerHands.get(selectedPlayer).add(state.cardSelection.get(1));
            state.cardCount[selectedPlayer].increment();
        }


    }

    //Before placing the card, the player must select a player to give berries to
    public void setSelectedPlayer(int selectedPlayer){
        this.selectedPlayer = selectedPlayer;
    }

}
