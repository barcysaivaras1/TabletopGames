package games.everdell.components;

import core.components.Card;
import games.catan.components.CatanCard;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.function.Function;

public class EverdellCard extends Card {

    public final CardType cardType;
    public int roundCardWasBought = -1;  // -1 is not bought
    //public final String cardDescription;

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

        //THIS IS TEMPORARY AS WE HAVE NO CARDS YET
        public Function<EverdellGameState, CardType> applyCardEffect;

        static{
            //Draw a Card
            TAN_TRAVELER.applyCardEffect = (state) -> {
                if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                    System.out.println("Drawing a card");
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    System.out.println(state.playerHands.get(state.playerTurn).toString() + " Player hand after using Tan Traveler");
                    state.cardCount[state.playerTurn].increment();
                }
                return TAN_TRAVELER;
            };
            //Get one wood
            GREEN_PRODUCTION.applyCardEffect = (state) -> {
                state.PlayerResources.get(EverdellParameters.ResourceTypes.TWIG)[state.playerTurn].increment();
                return GREEN_PRODUCTION;
            };
            //Creates a new Location
            RED_DESTINATION.applyCardEffect = (state) -> {
                EverdellLocation location = new EverdellLocation(EverdellParameters.Locations.ONE_BERRY_RED_DESTINATION, false);
                EverdellParameters.Locations.ONE_BERRY_RED_DESTINATION.applyLocationEffect = (gs) -> {
                    state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.playerTurn].increment();
                    return EverdellParameters.Locations.ONE_BERRY_RED_DESTINATION;
                };
                state.resourceLocations.put(EverdellParameters.Locations.ONE_BERRY_RED_DESTINATION, location);
                //state.resourceLocations.put(location,new EverdellLocation(location, false));
                System.out.println("Created a new location");
                System.out.println(state.resourceLocations.toString());
                return RED_DESTINATION;
            };
            //Get one pebble
            BLUE_GOVERNANCE.applyCardEffect = (state) -> {
                state.PlayerResources.get(EverdellParameters.ResourceTypes.PEBBLE)[state.playerTurn].increment();
                return BLUE_GOVERNANCE;
            };
            //Get one point
            PURPLE_PROSPERITY.applyCardEffect = (state) -> {
                state.pointTokens[state.playerTurn].increment();
                return PURPLE_PROSPERITY;
            };
        }
    }
}

