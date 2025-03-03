package games.everdell.components;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class ShepherdCard extends CritterCard{

    private int selectedPlayer;
    private Counter beforePR;
    private Counter afterPR;

    public ShepherdCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);

        beforePR = new Counter();
        afterPR = new Counter();
    }

    private ShepherdCard(String name, int compID, int selectedPlayer, Counter beforePR, Counter afterPR) {
        super(name, compID);
        this.selectedPlayer = selectedPlayer;
        this.beforePR = beforePR;
        this.afterPR = afterPR;
    }


    public void applyCardEffect(EverdellGameState state) {
        //We need to know the difference between their berries before playing the card and RIGHT after playing the card
        //This is so we know how much they need to pay the other player
        afterPR.increment(state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.getCurrentPlayer()].getValue());

        //The player will just select a player to gain 3 berries as they pay for the card by paying a player
        //If paid via occupation, selected player will gain nothing
        state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[selectedPlayer].increment(beforePR.getValue()-afterPR.getValue());

        //The player will gain 3 berries due to the card effect
        state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.getCurrentPlayer()].increment(3);

        //Find the chapel card in the players village
        for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){
            if(card.getCardEnumValue() == EverdellParameters.CardDetails.CHAPEL){
                //The player will gain 1 point for each point token on the chapel card
                state.pointTokens[state.getCurrentPlayer()].increment(card.getPoints()-2);

            }
        }

    }

    //Before placing the card, the player must select a player to give berries to
    public void setSelectedPlayer(int selectedPlayer){
        this.selectedPlayer = selectedPlayer;
    }

    //Berries before
    public void setBeforePR(int berryCount){
        beforePR.increment(berryCount);
    }

    @Override
    public ShepherdCard copy() {
        ShepherdCard card;
        card = new ShepherdCard(getName(), componentID, selectedPlayer, beforePR.copy(), afterPR.copy());
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
