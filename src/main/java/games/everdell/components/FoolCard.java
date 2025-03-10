package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class FoolCard extends CritterCard{



    private int selectedPlayer;

    public FoolCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
    }

    private FoolCard(String name, int compID, int selectedPlayer) {
        super(name, compID);
        this.selectedPlayer = selectedPlayer;
    }

    public void applyCardEffect(EverdellGameState state) {
        //The player must place this card in another players village
        state.playerVillage.get(selectedPlayer).add(this);
    }

    //Before placing the card, the player must select a player to give berries to
    public void setSelectedPlayer(int selectedPlayer){
        this.selectedPlayer = selectedPlayer;
    }

    public int getSelectedPlayer(){
        return selectedPlayer;
    }

    @Override
    public FoolCard copy() {
        FoolCard card;
        card = new FoolCard(getName(), componentID, selectedPlayer);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}
