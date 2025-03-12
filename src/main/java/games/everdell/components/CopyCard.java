package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class CopyCard extends CritterCard{
    //This class is specifically meant for the Miner Mole and Chip Sweep
    //They perform the same tasks but the context in which the card is selected is different

    private EverdellCard cardToCopy;


    public CopyCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect);
    }

    private CopyCard(String name, int compID, EverdellCard cardToCopy) {
        super(name, compID);
        this.cardToCopy = cardToCopy;
    }


    public void applyCardEffect(EverdellGameState state) {

        //Chip Sweep takes in a production card and copies its effect.
        //The player can select which production card to copy

        if (cardToCopy == null) {
            return;
        }

        if (cardToCopy.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION) {
            if (cardToCopy instanceof ConstructionCard constructionCard) {
                constructionCard.applyCardEffect(state);
            } else {
                CritterCard critterCard = (CritterCard) cardToCopy;
                critterCard.applyCardEffect(state);
            }
        }

        cardToCopy = null;
    }

    public void setCardToCopy(EverdellCard card){
        cardToCopy = card;
    }
    public EverdellCard getCardToCopy(){
        return cardToCopy;
    }


    @Override
    public CopyCard copy() {
        CopyCard card;
        card = new CopyCard(getName(), componentID, cardToCopy.copy());
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}
