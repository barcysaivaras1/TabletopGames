package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class InnCard extends ConstructionCard{

    int playerOwner;
    int occupyingPlayer;

    public InnCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(rdl, name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
    }

    private InnCard(String name, int compID, int playerOwner, int occupyingPlayer) {
        super(name, compID);
        this.playerOwner = playerOwner;
        this.occupyingPlayer = occupyingPlayer;
    }


    public void applyCardEffect(EverdellGameState state) {
        //This means they are placing the card, we can assign the playerOwner
        playerOwner = state.getCurrentPlayer();
        super.applyCardEffect(state, setLocationEffect(state));
    }

    public Consumer<EverdellGameState> setLocationEffect(EverdellGameState state){
        //playerOwner is the player who owns the card
        //occupyingPlayer is the player who chose to place a worker on the card

        //Initialise the Location Effect

        //The player may play any card from the meadow for a discount of 3 resources

        //If a player uses your inn, they must pay 1 point token to you

        //Card Selection index [0] will represent the card to play

        return k -> {
            //If the occupying player is not the owner, they must pay 1 token to the owner
            if(occupyingPlayer != playerOwner){
                state.pointTokens[playerOwner].increment();
            }


            System.out.println("IN THE INNCARD WE ARE TRYING TO PLAY THE CARD, IS CARD SELECTIO EMPTY ? : " + state.cardSelection.isEmpty());
            //From gameState Resource Selection will tell us how much of a discount will be applied.
            //The card selection will hold the card that the player selected to play at a discount
            if(!state.cardSelection.isEmpty()) {
                System.out.println("TRYING TO PLAY THE CARD : "+ state.cardSelection.get(0).getCardEnumValue()+ " with ID : " + state.cardSelection.get(0).getComponentID());
                for (var resource : state.cardSelection.get(0).getResourceCost().keySet()) {
                    int discount = state.resourceSelection.get(resource).getValue();
                    int initialCost = state.cardSelection.get(0).getResourceCost().get(resource);

                    int finalCost = Math.max(initialCost - discount, 0);

                    state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement(finalCost);
                }
                state.cardSelection.get(0).payForCard();
            }
        };
    }

    //Players NEED to be set before the location EFFECT is called
    public void setPlayers(int occupyingPlayer){
        this.occupyingPlayer = occupyingPlayer;
    }

    @Override
    public void removeCardEffect(EverdellGameState state){
        state.everdellLocations.remove(getLocation(state));
    }

    @Override
    public InnCard copy() {
        InnCard card = new InnCard(getName(), componentID, playerOwner, occupyingPlayer);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}
