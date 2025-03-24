package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class CopyCard extends CritterCard{
    //This class is specifically meant for the Miner Mole and Chip Sweep
    //They perform the same tasks but the context in which the card is selected is different

    private int cardToCopyID;


    public CopyCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
        cardToCopyID = -1;
    }

    private CopyCard(String name, int compID, int cardToCopy) {
        super(name, compID);
        this.cardToCopyID = cardToCopy;
    }


    public void applyCardEffect(EverdellGameState state) {

        //Chip Sweep takes in a production card and copies its effect.
        //The player can select which production card to copy

        if (cardToCopyID == -1) {
            return;
        }
        EverdellCard cardToCopy = (EverdellCard) state.getComponentById(cardToCopyID);

        if (cardToCopy.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION) {
            if (cardToCopy instanceof ConstructionCard constructionCard) {
                constructionCard.applyCardEffect(state);
            } else {
                CritterCard critterCard = (CritterCard) cardToCopy;
                critterCard.applyCardEffect(state);
            }
        }
        cardToCopyID = -1;
    }

    public void setCardToCopy(EverdellCard card){
        cardToCopyID = card.getComponentID();
    }
    public EverdellCard getCardToCopy(EverdellGameState state){
        EverdellCard cardToCopy = (EverdellCard) state.getComponentById(cardToCopyID);
        return cardToCopy;
    }


    @Override
    public CopyCard copy() {
        CopyCard card;
        card = new CopyCard(getName(), componentID, cardToCopyID);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}
