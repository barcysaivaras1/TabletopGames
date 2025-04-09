package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class CemeteryCard extends ConstructionCard{

    public CemeteryCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(rdl, name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
    }

    //Copy Constructor
    private CemeteryCard(String name, int compID) {
        super(name, compID);

    }




    public void applyCardEffect(EverdellGameState state) {
        super.applyCardEffect(state, setLocationEffect(state));
        state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c.getCardEnumValue() == EverdellParameters.CardDetails.UNDERTAKER ).forEach(c -> {
            unlockSecondLocation(state);
        });
    }

    public Consumer<EverdellGameState> setLocationEffect(EverdellGameState state){
        //Initialise the Location Effect

        //Cemetery, the player must choose to reveal 4 cards from the discard or draw pile.
        //Once the cards are revealed the player must then select ONE card to play for free

        //If for example the discard pile has less than 4 cards and they select the discard pile.
        //They will reveal however many cards there are. If discard has 3 cards, then they will reveal 3 cards.

        //cardSelection[0] will represent the card that they want to choose to play
        //Anything after that will be discarded

        return k -> {
            if(!state.cardSelection.isEmpty()) {
                state.cardSelection.get(0).payForCard();
                //state.temporaryDeck.add(state.cardSelection.get(0));
                ArrayList<EverdellCard> cardsToDiscard = new ArrayList<>(state.cardSelection);
                cardsToDiscard.remove(state.cardSelection.get(0));
                discardCards(state, cardsToDiscard);
            }
        };
    }

    public void discardCards(EverdellGameState state, ArrayList<EverdellCard> cardsToDiscard){
        System.out.println("Card Selection in Cemetery is : " + state.cardSelection);
        System.out.println("Card to Place is : " + state.cardSelection.get(0));
        for (int i = 1; i < cardsToDiscard.size(); i++){
            cardsToDiscard.get(i).discardCard(state);
            cardsToDiscard.remove(0);
        }
    }

    public void unlockSecondLocation(EverdellGameState state){
        super.getLocation(state).setNumberOfSpaces(2);
    }

    public void lockSecondLocation(EverdellGameState state){
        super.getLocation(state).setNumberOfSpaces(1);
    }

    @Override
    public void removeCardEffect(EverdellGameState state){
        state.everdellLocations.remove(getLocation(state));
    }

    @Override
    public CemeteryCard copy() {
        CemeteryCard card;
        card = new CemeteryCard(getName(), componentID);

        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

}
