package games.everdell.components;

import core.components.Card;
import games.catan.components.CatanCard;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.CardType;
import org.apache.spark.internal.config.R;

import java.util.HashMap;
import java.util.function.Function;

public class EverdellCard extends Card {


    public CardDetails cardDetails;

    public int roundCardWasBought = -1;  // -1 is not bought
    //public final String cardDescription;

    public EverdellCard(CardDetails cardDetails) {
        super(cardDetails.name());
        this.cardDetails = cardDetails;
    }
    private EverdellCard(CardDetails cardDetails, int id) {
        super(cardDetails.name(), id);
        this.cardDetails = cardDetails;
    }

    @Override
    public EverdellCard copy() {
        EverdellCard card = new EverdellCard(cardDetails, componentID);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }


    public enum CardDetails {
        FARM, RESIN_REFINERY, GENERAL_STORE, WANDERER, WIFE, HUSBAND;

        public CardType cardType;
        public Function<EverdellGameState, CardDetails> applyCardEffect;
        public HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost;
        public int points;

        static{
            //Get 1 Berry
            FARM.resourceCost = new HashMap<>();
            FARM.cardType = CardType.GREEN_PRODUCTION;
            FARM.resourceCost.put(EverdellParameters.ResourceTypes.TWIG, 2);
            FARM.resourceCost.put(EverdellParameters.ResourceTypes.RESIN, 1);
            FARM.points = 1;
            FARM.applyCardEffect = (state) -> {
                state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.playerTurn].increment();
                return FARM;
            };

            //Get 1 Resin
            RESIN_REFINERY.resourceCost = new HashMap<>();
            RESIN_REFINERY.cardType = CardType.GREEN_PRODUCTION;
            RESIN_REFINERY.resourceCost.put(EverdellParameters.ResourceTypes.RESIN, 1);
            RESIN_REFINERY.resourceCost.put(EverdellParameters.ResourceTypes.PEBBLE, 1);
            RESIN_REFINERY.points = 1;
            RESIN_REFINERY.applyCardEffect = (state) -> {
                state.PlayerResources.get(EverdellParameters.ResourceTypes.RESIN)[state.playerTurn].increment();
                return RESIN_REFINERY;
            };

            //Get 1 Berry, Get an extra berry if the player has a farm. Extra berry can only be triggered once per production event
            GENERAL_STORE.resourceCost = new HashMap<>();
            GENERAL_STORE.cardType = CardType.GREEN_PRODUCTION;
            GENERAL_STORE.resourceCost.put(EverdellParameters.ResourceTypes.RESIN, 1);
            GENERAL_STORE.resourceCost.put(EverdellParameters.ResourceTypes.PEBBLE, 1);
            GENERAL_STORE.points = 1;
            GENERAL_STORE.applyCardEffect = (state) -> {
                state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.playerTurn].increment();

                for(var everdellCard : state.playerVillage.get(state.playerTurn).getComponents()){
                    if(everdellCard.cardDetails == FARM){
                        state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.playerTurn].increment();
                        break;
                    }
                }

                return GENERAL_STORE;
            };

            WANDERER.resourceCost = new HashMap<>();
            WANDERER.cardType = CardType.TAN_TRAVELER;
            WANDERER.resourceCost.put(EverdellParameters.ResourceTypes.BERRY, 2);
            WANDERER.points = 1;
            WANDERER.applyCardEffect = (state) -> {
                for(int i = 0 ; i<3; i++){
                    if(state.playerHands.get(state.playerTurn).getSize() == state.playerHands.get(state.playerTurn).getCapacity()){
                        break;
                    }
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    state.cardCount[state.playerTurn].increment();
                }
                return WANDERER;
            };

            //Can share a space with Husband card. Will be worth 3 points if paired with a husband instead of 2
            WIFE.resourceCost = new HashMap<>();
            WIFE.cardType = CardType.PURPLE_PROSPERITY;
            WIFE.resourceCost.put(EverdellParameters.ResourceTypes.BERRY, 2);
            WIFE.points = 2;
            WIFE.applyCardEffect = (state) -> {
                for(var everdellCard : state.playerVillage.get(state.playerTurn).getComponents()){
                    if(everdellCard.cardDetails == HUSBAND){
                        WIFE.points = 3;
                        break;
                    }
                }

                return WIFE;
            };
        }
    }
}

