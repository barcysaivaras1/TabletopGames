package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class CemeteryCard extends ConstructionCard{
    public EverdellLocation location;
    EverdellParameters.RedDestinationLocation rdl;

    public CemeteryCard(EverdellParameters.RedDestinationLocation rdl, String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(rdl, name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
        this.rdl = rdl;
    }


    public void applyCardEffect(EverdellGameState state) {
        this.location = new EverdellLocation(rdl,1, true, setLocationEffect(state));
        state.Locations.put(rdl, location);
        state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c.getCardEnumValue() == EverdellParameters.CardDetails.UNDERTAKER ).forEach(c -> {
            unlockSecondLocation();
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
            state.cardSelection.get(0).payForCard();

            for (int i = 1; i < state.cardSelection.size(); i++){
                state.discardDeck.add(state.cardSelection.get(i));
            }

            state.cardSelection.clear();
        };
    }

    public void unlockSecondLocation(){
        location.setNumberOfSpaces(2);
    }

    public void lockSecondLocation(){
        location.setNumberOfSpaces(1);
    }

}
