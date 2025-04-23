package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class PostOfficeCard extends ConstructionCard{

    int playerOwner;
    private int selectedPlayer;


    public PostOfficeCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(rdl, name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
        this.playerOwner = -1;
        this.selectedPlayer = -1;
    }

    public PostOfficeCard(String name, int compID, int playerOwner, int selectedPlayer) {
        super(name, compID);
        this.playerOwner = playerOwner;
        this.selectedPlayer = selectedPlayer;
    }


    public void applyCardEffect(EverdellGameState state) {
        this.playerOwner = state.getCurrentPlayer();
        super.applyCardEffect(state, setLocationEffect(state));
    }

    public Consumer<EverdellGameState> setLocationEffect(EverdellGameState k){
        //playerOwner is the player who owns the card
        //occupyingPlayer is the player who chose to place a worker on the card
        //selectedPlayer is the player that was chosen by the occupying player,to trigger the effect on

        //Initialise the Location Effect

        //Give an opponent 2 cards from your hand,
        //You can discard as many cards as you want after
        //Draw cards from the deck to the limit.
        //If a player uses your post office, they must pay 1 point token to you

        //Card Selection index [0] and index [1] represent the two cards to give away

        return state -> {
            //If the occupying player is not the owner, the owner gains 1 token
            int occupyingPlayer = state.getCurrentPlayer();

//            PostOfficeCard card = (PostOfficeCard) state.getComponentById(getComponentID());
//            selectedPlayer = card.getSelectedPlayer();

            if(occupyingPlayer != playerOwner){
                state.pointTokens[playerOwner].increment();
            }

            if(state.cardSelection.isEmpty()) {
                System.out.println("No cards selected");
                return;
            }
            System.out.println("Select Player is : " + getSelectedPlayer());
            System.out.println("Occupying Player is : " + occupyingPlayer);

            System.out.println("IN POSTOFFICE CARD, CARD SELECTION IS : " + state.cardSelection);
            System.out.println("IN POSTOFFICE CARD, PLAYER HANDS ARE : " + state.playerHands.get(occupyingPlayer));
            //Move the cards from the occupying player to the selected player
            state.playerHands.get(occupyingPlayer).remove(state.cardSelection.get(0));
            state.playerHands.get(occupyingPlayer).remove(state.cardSelection.get(1));
            System.out.println("IN POSTOFFICE CARD, PLAYER HANDS AFTER REMOVAL ARE : " + state.playerHands.get(occupyingPlayer));
            state.cardCount[occupyingPlayer].decrement(2);

            for (int i = 0; i < 2; i++) {
                if (state.playerHands.get(getSelectedPlayer()).getSize() == state.playerHands.get(getSelectedPlayer()).getCapacity()) {
                    break;
                }
                System.out.println("Adding card to player " + getSelectedPlayer());
                System.out.println("Card: " + state.cardSelection.get(i));
                state.playerHands.get(getSelectedPlayer()).add(state.cardSelection.get(i));
                state.cardCount[getSelectedPlayer()].increment();
            }

            //Remove the cards that were given away from the card selection
            state.cardSelection.remove(0);
            state.cardSelection.remove(0);

            System.out.println("Card Selection after removing cards: " + state.cardSelection);

            //Discard any selected cards
            for(var c : state.cardSelection){
                if(state.discardDeck.contains(c)) continue;
                EverdellParameters.CardDetails.discardEverdellCard(state, c);
            }

            //Draw to the Limit
            while(state.playerHands.get(occupyingPlayer).getSize() < state.playerHands.get(occupyingPlayer).getCapacity()){
                state.playerHands.get(occupyingPlayer).add(state.cardDeck.draw());
            }

            state.cardCount[occupyingPlayer].setValue(8);
        };

    }


    //Players NEED to be set before the location EFFECT is called
    public void setPlayers(int selectedPlayer){
        this.selectedPlayer = selectedPlayer;
    }

    public int getSelectedPlayer(){
        return selectedPlayer;
    }

    @Override
    public void removeCardEffect(EverdellGameState state){
        state.everdellLocations.remove(getLocation(state));
    }

    @Override
    public PostOfficeCard copy() {
        PostOfficeCard card = new PostOfficeCard(getName(), componentID, playerOwner, selectedPlayer);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
