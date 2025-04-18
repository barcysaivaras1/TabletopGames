package games.everdell.components;


import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters.CardDetails;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.CardType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class PostOfficeCard extends ConstructionCard{

    int playerOwner;
    int selectedPlayer;
    int occupyingPlayer;


    public PostOfficeCard(EverdellParameters.RedDestinationLocation rdl, String name, CardDetails cardEnumValue, CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<CardDetails> cardsThatCanOccupy) {
        super(rdl, name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
    }

    private PostOfficeCard(String name, int compID, int playerOwner, int selectedPlayer, int occupyingPlayer) {
        super(name, compID);
        this.playerOwner = playerOwner;
        this.selectedPlayer = selectedPlayer;
        this.occupyingPlayer = occupyingPlayer;
    }


    public void applyCardEffect(EverdellGameState state) {
        //This means they are placing the card, we can assign the playerOwner
        playerOwner = state.getCurrentPlayer();

//        EverdellLocation location = new EverdellLocation(rdl,1, false, setLocationEffect(state));
//        location.setOwnerId(state.getCurrentPlayer());
//        state.everdellLocations.add(location);
        super.applyCardEffect(state, setLocationEffect(state));
//        EverdellLocation location = super.getLocation(state);
//        locationId = location.getComponentID();
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
            if(occupyingPlayer != playerOwner){
                state.pointTokens[playerOwner].increment();
            }

            if(state.cardSelection.isEmpty()){
                System.out.println("No cards selected");
                return;
            }

            System.out.println("IN POSTOFFICE CARD, CARD SELECTION IS : " + state.cardSelection);
            //Move the cards from the occupying player to the selected player
            state.playerHands.get(occupyingPlayer).remove(state.cardSelection.get(0));
            state.playerHands.get(occupyingPlayer).remove(state.cardSelection.get(1));
            state.cardCount[occupyingPlayer].decrement(2);

            for (int i = 0; i < 2; i++) {
                if (state.playerHands.get(selectedPlayer).getSize() == state.playerHands.get(state.getCurrentPlayer()).getCapacity()) {
                    break;
                }
                System.out.println("Adding card to player " + selectedPlayer);
                System.out.println("Card: " + state.cardSelection.get(i).getName());
                state.playerHands.get(selectedPlayer).add(state.cardSelection.get(i));
                state.cardCount[selectedPlayer].increment();
            }



            //Discard any selected cards
            if (state.cardSelection.size() > 2) {
                for(var c : state.cardSelection.subList(2, state.cardSelection.size())){
                    state.playerHands.get(occupyingPlayer).remove(c);
                    state.cardCount[occupyingPlayer].decrement();
                }
            }
            //Draw to the Limit
            while(state.playerHands.get(occupyingPlayer).getSize() < state.playerHands.get(occupyingPlayer).getCapacity()){
                state.playerHands.get(occupyingPlayer).add(state.cardDeck.draw());
            }

            state.cardCount[occupyingPlayer].setValue(8);
        };
    }

    //Players NEED to be set before the location EFFECT is called
    public void setPlayers(int selectedPlayer, int occupyingPlayer){
        this.selectedPlayer = selectedPlayer;
        this.occupyingPlayer = occupyingPlayer;

    }

    @Override
    public void removeCardEffect(EverdellGameState state){
        System.out.println("Removing Post Office Card Effect");
        System.out.println("Post Office Card Location : " + getLocation(state));
        System.out.println("Post Office Card Location ID : " + getLocation(state).getComponentID());
        state.everdellLocations.remove(getLocation(state));
    }

    @Override
    public PostOfficeCard copy() {
        PostOfficeCard card;
        card = new PostOfficeCard(getName(), componentID, playerOwner, selectedPlayer, occupyingPlayer);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }



}
