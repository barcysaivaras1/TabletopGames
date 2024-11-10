package games.everdell.components;

import core.components.Card;
import games.catan.components.CatanCard;
import games.everdell.EverdellParameters;

public class EverdellCard extends Card {

    public final CardType cardType;
    public int roundCardWasBought = -1;  // -1 is not bought

    public EverdellCard(CardType cardType) {
        super(cardType.name());
        this.cardType = cardType;
    }
    private EverdellCard(CardType cardType, int id) {
        super(cardType.name(), id);
        this.cardType = cardType;
    }

    @Override
    public EverdellCard copy() {
        EverdellCard card = new EverdellCard(cardType, componentID);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }

    public enum CardType {
        TAN_TRAVELER,
        GREEN_PRODUCTION,
        RED_DESTINATION,
        BLUE_GOVERNANCE,
        PURPLE_PROSPERITY;
    }
}

