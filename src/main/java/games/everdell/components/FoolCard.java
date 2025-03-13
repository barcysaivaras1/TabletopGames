package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.actions.PlayCard;
import games.everdell.actions.SelectAListOfCards;

import java.util.ArrayList;
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
        System.out.println("Fool Card Effect");
        System.out.println("Player " + state.getCurrentPlayer() + " is placing the Fool Card in Player " + selectedPlayer + "'s village");
        state.pointTokens[selectedPlayer].decrement(2);
        state.playerVillage.get(selectedPlayer).add(this);
    }

    public boolean canFoolBePlaced(EverdellGameState state, int player){
        //We need to check if the fool card can be placed in the game
        boolean canBePlaced = false;
        for(int i=0 ; i<state.getNPlayers(); i++){
            if(i == player){continue;}

            if(canFoolBePlacedInThisPlayersVillage(state, i)){
                canBePlaced = true;
            }
        }
        return canBePlaced;
    }

    public boolean canFoolBePlacedInThisPlayersVillage(EverdellGameState state, int playerToPlaceIn){
        boolean canPlace = true;

        //If village is full, we cannot place the card
        PlayCard pc = new PlayCard(playerToPlaceIn, this.componentID, new ArrayList<>(), new HashMap<>());
        if(!pc.checkIfPlayerCanPlaceThisUniqueCard(state, playerToPlaceIn)){
            canPlace = false;
        }
        //If the player has a fool card, we cannot place the card
        if(!pc.checkIfPlayerCanPlaceThisUniqueCard(state, playerToPlaceIn)){
            canPlace = false;
        }
        return canPlace;
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
