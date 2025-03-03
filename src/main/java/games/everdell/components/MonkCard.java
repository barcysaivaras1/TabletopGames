package games.everdell.components;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.MonasteryCard;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class MonkCard extends CritterCard{

    private int selectedPlayer;

    public MonkCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
    }

    private MonkCard(String name, int compID, int selectedPlayer) {
        super(name, compID);
        this.selectedPlayer = selectedPlayer;
    }


    public void applyCardEffect(EverdellGameState state) {
        state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof MonasteryCard).forEach(c -> {
            MonasteryCard mc = (MonasteryCard) c;
            mc.unlockSecondLocation();
        });

        int berriesToGive = state.resourceSelection.get(EverdellParameters.ResourceTypes.BERRY).getValue();
        //ResourceSelection will tell us how many berries the player chooses to give up
        //They will receive 2 points for each berry they give to another player
        if(state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.getCurrentPlayer()].getValue() < 2){
            berriesToGive = state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.getCurrentPlayer()].getValue();
        }


        state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.getCurrentPlayer()].decrement(berriesToGive);
        state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[selectedPlayer].increment(berriesToGive);

        state.pointTokens[state.getCurrentPlayer()].increment(berriesToGive*2);
    }

    //Before placing the card, the player must select a player to give berries to
    public void setSelectedPlayer(int selectedPlayer){
        this.selectedPlayer = selectedPlayer;
    }


    public void removeCardEffect(EverdellGameState state) {
        MonasteryCard mc = (MonasteryCard) state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof MonasteryCard).findFirst().get();
        mc.lockSecondLocation();
    }

    @Override
    public MonkCard copy() {
        MonkCard card;
        card = new MonkCard(getName(), componentID, selectedPlayer);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }


}
