package games.everdell;

import core.AbstractGameState;
import core.AbstractParameters;
import core.components.Counter;
import evaluation.optimisation.TunableParameters;
import games.everdell.components.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>This class should hold a series of variables representing game parameters (e.g. number of cards dealt to players,
 * maximum number of rounds in the game etc.). These parameters should be used everywhere in the code instead of
 * local variables or hard-coded numbers, by accessing these parameters from the game state via {@link AbstractGameState#getGameParameters()}.</p>
 *
 * <p>It should then implement appropriate {@link #_copy()}, {@link #_equals(Object)} and {@link #hashCode()} functions.</p>
 *
 * <p>The class can optionally extend from {@link TunableParameters} instead, which allows to use
 * automatic game parameter optimisation tools in the framework.</p>
 */
public class EverdellParameters extends AbstractParameters {


    public enum ResourceTypes {
        RESIN, PEBBLE, BERRY, TWIG
    }

    public enum Seasons {
        WINTER, SPRING, SUMMER, AUTUMN

    }

    public interface AbstractLocations {
        Consumer<EverdellGameState> getLocationEffect(EverdellGameState state);
        String name();
    }

    public enum BasicLocations implements AbstractLocations{
        THREE_WOOD, TWO_WOOD_ONE_CARD, ONE_BERRY, ONE_BERRY_ONE_CARD, ONE_PEBBLE, TWO_CARD_ONE_POINT, TWO_RESIN, ONE_RESIN_ONE_CARD, ONE_BERRY_RED_DESTINATION;

        public Consumer<EverdellGameState> applyLocationEffect;

        @Override
        public Consumer<EverdellGameState> getLocationEffect(EverdellGameState state) {
            return applyLocationEffect;
        }

        static{
            THREE_WOOD.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].increment(3);
            };
            TWO_WOOD_ONE_CARD.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].increment(2);
                if(state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
            };
            ONE_BERRY.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment(1);
            };
            ONE_BERRY_ONE_CARD.applyLocationEffect = (state) -> {
                    state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment();
                    if(state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                        state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                        state.cardCount[state.getCurrentPlayer()].increment();
                    }
            };
            ONE_PEBBLE.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.PEBBLE)[state.getCurrentPlayer()].increment(1);
            };
            TWO_CARD_ONE_POINT.applyLocationEffect = (state) -> {
                state.pointTokens[state.getCurrentPlayer()].increment();
                if(state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(state.getCurrentPlayer()).getCapacity()-1){
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                } else if (state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                }
                state.cardCount[state.getCurrentPlayer()].increment(2);
            };
            TWO_RESIN.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].increment(2);
            };
            ONE_RESIN_ONE_CARD.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].increment();
                if(state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                }
                state.cardCount[state.getCurrentPlayer()].increment();
            };
        }
    }

    public enum ForestLocations implements AbstractLocations{
        THREE_BERRY,TWO_BERRY_ONE_CARD,TWO_RESIN_ONE_TWIG,THREE_CARDS_ONE_PEBBLE,ONE_TWIG_ONE_RESIN_ONE_BERRY, TWO_ANY, TWO_CARDS_ONE_ANY,
        DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED, DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED,
        DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT, COPY_BASIC_LOCATION_DRAW_CARD;

        public Consumer<EverdellGameState> applyLocationEffect;


        //Maybe a bad design decision. Maybe good?
        public static ArrayList<EverdellCard> cardChoices;
        public static BasicLocations basicLocationChoice;

        @Override
        public Consumer<EverdellGameState> getLocationEffect(EverdellGameState state) {
            return applyLocationEffect;
        }

        static{
            THREE_BERRY.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment(3);
            };
            TWO_BERRY_ONE_CARD.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment(2);
                if(state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
            };
            TWO_RESIN_ONE_TWIG.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].increment(2);
                state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].increment(1);
            };
            THREE_CARDS_ONE_PEBBLE.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.PEBBLE)[state.getCurrentPlayer()].increment(1);

                for(int i = 0 ; i<3; i++){
                    if(state.playerHands.get(state.getCurrentPlayer()).getSize() == state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                        break;
                    }
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
            };
            ONE_TWIG_ONE_RESIN_ONE_BERRY.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].increment(1);
                state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].increment(1);
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment(1);
            };

            TWO_ANY.applyLocationEffect = (state) ->{
                for(var resources :  state.resourceSelection.keySet()){
                    state.PlayerResources.get(resources)[state.getCurrentPlayer()].increment(state.resourceSelection.get(resources).getValue());
                }
            };
            TWO_CARDS_ONE_ANY.applyLocationEffect = (state) ->{

                //Add the selected resources
                for(var resources :  state.resourceSelection.keySet()){
                    state.PlayerResources.get(resources)[state.getCurrentPlayer()].increment(state.resourceSelection.get(resources).getValue());
                }

                // Draw two cards
                for(int i = 0 ; i<2; i++){
                    if(state.playerHands.get(state.getCurrentPlayer()).getSize() == state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                        break;
                    }
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
            };

            DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED.applyLocationEffect = (state) ->{
                //Discard a card
                for(var card : cardChoices){
                    try{
                        state.playerHands.get(state.getCurrentPlayer()).remove((EverdellCard) card);
                        state.discardDeck.add(card);
                        state.cardCount[state.getCurrentPlayer()].decrement();
                    } catch (Exception e){
                        System.out.println("Error in Forest Locations, Choices did not contain cards");
                    }
                }


                //Draw two cards
                for(int i = 0 ; i<(cardChoices.size()*2); i++){
                    if(state.playerHands.get(state.getCurrentPlayer()).getSize() == state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                        break;
                    }
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
            };

            DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED.applyLocationEffect = (state) ->{
                //Discard a card
                for(var card : cardChoices){
                    try{
                        state.playerHands.get(state.getCurrentPlayer()).remove((EverdellCard) card);
                        state.discardDeck.add(card);
                        state.cardCount[state.getCurrentPlayer()].decrement();
                    } catch (Exception e){
                        System.out.println("Error in Forest Locations, Choices did not contain cards");
                    }
                }

                //Gain resources
                int resourceCounter = 0;
                for(var resource :  state.resourceSelection.keySet()){
                    for(int i = 0 ; i<state.resourceSelection.get(resource).getValue(); i++){
                        if(resourceCounter == cardChoices.size()){
                            break;
                        }
                        resourceCounter++;

                        state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment(1);
                    }
                }
            };

            COPY_BASIC_LOCATION_DRAW_CARD.applyLocationEffect = (state) ->{
                //Copy a basic location
                basicLocationChoice.getLocationEffect(state).accept(state);

                //Draw a card
                if(state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
            };

            //THIS NEEDS EXTRA WORK, WHEN I HAVE ADDDED COSTS TO CARDS
            DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT.applyLocationEffect = (state) ->{
                //Draw two cards
                //Card Choices will hold the card that the player wants to take in their hand
                //Card Selection will hold the card that the player wants to play at a discount

                //Attempt to place the card in the players hand

                if(state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(state.getCurrentPlayer()).getCapacity()){
                    System.out.println("Adding card to hand : "+cardChoices.get(0).getName());
                    state.playerHands.get(state.getCurrentPlayer()).add(cardChoices.get(0));
                    state.cardCount[state.getCurrentPlayer()].increment();
                }

                //Apply the discount to the card
                System.out.println("Applying discount to card : "+state.cardSelection.get(0).getName());
                for(var resource : state.cardSelection.get(0).getResourceCost().keySet()){
                    int discount = state.resourceSelection.get(resource).getValue();
                    System.out.println("Resource Type is : "+resource);
                    System.out.println("RESOURCE COUNT FROM STATE : "+state.resourceSelection.get(resource).getValue());
                    int initialCost = state.cardSelection.get(0).getResourceCost().get(resource);

                    System.out.println("Initial Cost : "+initialCost);
                    System.out.println("Discount : "+discount);

                    int finalCost = Math.max(initialCost - discount, 0);

                    System.out.println("Final Cost : "+finalCost);

                    state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement(finalCost);
                }
                state.cardSelection.get(0).payForCard();
            };
        }
    }

    public enum BasicEvent implements AbstractLocations{
        GREEN_PRODUCTION_EVENT, RED_DESTINATION_EVENT, BLUE_GOVERNANCE_EVENT, TAN_TRAVELER_EVENT;

        public Consumer<EverdellGameState> applyLocationEffect;

        @Override
        public Consumer<EverdellGameState> getLocationEffect(EverdellGameState state) {
            return k -> state.pointTokens[state.getCurrentPlayer()].increment(3);
        }


    }

    public enum RedDestinationLocation implements AbstractLocations{
        LOOKOUT_DESTINATION, QUEEN_DESTINATION, INN_DESTINATION, POST_OFFICE_DESTINATION,
        MONASTERY_DESTINATION, CEMETERY_DESTINATION;


        public static AbstractLocations copyLocationChoice;
        public Consumer<EverdellGameState> applyLocationEffect;

        @Override
        public Consumer<EverdellGameState> getLocationEffect(EverdellGameState state) {
            return applyLocationEffect;
        }

        static{
            LOOKOUT_DESTINATION.applyLocationEffect = (state) -> {
                if(copyLocationChoice != null){
                    copyLocationChoice.getLocationEffect(state).accept(state);
                }
            };
            QUEEN_DESTINATION.applyLocationEffect = (state) -> {
                state.cardSelection.get(0).payForCard();
            };

            INN_DESTINATION.applyLocationEffect = (state) -> {
                //From gameState Resource Selection will tell us how much of a discount will be applied.
                //The card selection will hold the card that the player selected to play at a discount



                for(var resource : state.cardSelection.get(0).getResourceCost().keySet()){
                    int discount = state.resourceSelection.get(resource).getValue();
                    int initialCost = state.cardSelection.get(0).getResourceCost().get(resource);

                    int finalCost = Math.max(initialCost - discount, 0);

                    state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement(finalCost);
                }
                state.cardSelection.get(0).payForCard();
            };

        }

    }

    public enum CardType {
        TAN_TRAVELER,
        GREEN_PRODUCTION,
        RED_DESTINATION,
        BLUE_GOVERNANCE,
        PURPLE_PROSPERITY;
    }

    public enum CardDetails {
        FARM, RESIN_REFINERY, GENERAL_STORE, WANDERER, WIFE, HUSBAND, FAIRGROUNDS, MINE, TWIG_BARGE, SHOP_KEEPER, BARGE_TOAD,
        CASTLE, KING, PALACE, BARD, THEATRE, SCHOOL, RUINS, WOOD_CARVER, DOCTOR, ARCHITECT, PEDDLER, CHIP_SWEEP, LOOKOUT,
        QUEEN, INN, POST_OFFICE, MONK, FOOL, TEACHER, MONASTERY, HISTORIAN, CEMETERY, UNDERTAKER, POSTAL_PIGEON, JUDGE,
        COURTHOUSE, CRANE, INNKEEPER;

        public Function<EverdellGameState, EverdellCard> createEverdellCard;

        static {
            FARM.createEverdellCard = (gamestate) -> new ConstructionCard("Farm", FARM, CardType.GREEN_PRODUCTION, true, false, 1,
                    new HashMap<>() {{
                        put(ResourceTypes.TWIG, 0); //2
                        put(ResourceTypes.RESIN, 0); //1
                    }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment();
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(WIFE)));


            RESIN_REFINERY.createEverdellCard = (gameState) -> new ConstructionCard("Resin Refinery", RESIN_REFINERY, CardType.GREEN_PRODUCTION, true, false, 1, new HashMap<>() {{
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].increment();
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(CHIP_SWEEP)));

            GENERAL_STORE.createEverdellCard = (gameState) -> new ConstructionCard("General Store", GENERAL_STORE, CardType.GREEN_PRODUCTION, true, false, 1, new HashMap<>() {{
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment();

                for (var everdellCard : state.playerVillage.get(state.getCurrentPlayer()).getComponents()) {
                    if (everdellCard.getCardEnumValue() == FARM) {
                        state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment();
                        break;
                    }
                }

                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(SHOP_KEEPER)));

            //Wanderer also has a special feature that it does not take up a village slot
            WANDERER.createEverdellCard = (gameState) -> new CritterCard("Wanderer", WANDERER, CardType.TAN_TRAVELER, false, false, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                for (int i = 0; i < 3; i++) {
                    if (state.playerHands.get(state.getCurrentPlayer()).getSize() == state.playerHands.get(state.getCurrentPlayer()).getCapacity()) {
                        break;
                    }
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
                state.villageMaxSize[state.getCurrentPlayer()].increment();
                return true;
            }, (everdellGameState -> {
                everdellGameState.villageMaxSize[everdellGameState.getCurrentPlayer()].decrement();}));

            WIFE.createEverdellCard = (gameState) -> new WifeCard("Wife", WIFE, CardType.PURPLE_PROSPERITY, true, false, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 0); //2
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}));

            HUSBAND.createEverdellCard = (gameState) -> new HusbandCard("Husband", HUSBAND, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}));

            FAIRGROUNDS.createEverdellCard = (gameState) -> new ConstructionCard("Fairgrounds", FAIRGROUNDS, CardType.GREEN_PRODUCTION, true, true, 3, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 2);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                for (int i = 0; i < 2; i++) {
                    if (state.playerHands.get(state.getCurrentPlayer()).getSize() == state.playerHands.get(state.getCurrentPlayer()).getCapacity()) {
                        break;
                    }
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(WIFE)));//WIFE is incorrect. but this is for testing purposes

            MINE.createEverdellCard = (gameState) -> new ConstructionCard("Mine", MINE, CardType.GREEN_PRODUCTION, true, false, 2, new HashMap<>() {{
                put(ResourceTypes.PEBBLE, 1);
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 1);
            }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.PEBBLE)[state.getCurrentPlayer()].increment(1);
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(WIFE)));//WIFE is incorrect. but this is for testing purposes

            TWIG_BARGE.createEverdellCard = (gameState) -> new ConstructionCard("Twig Barge", TWIG_BARGE, CardType.GREEN_PRODUCTION, true, false, 1, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].increment(2);
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(BARGE_TOAD)));

            BARGE_TOAD.createEverdellCard = (gameState) -> new CritterCard("Barge Toad", BARGE_TOAD, CardType.GREEN_PRODUCTION, false, false, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == FARM) {
                        state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].increment(2);
                    }
                }
                return true;
            }, (everdellGameState -> {}));


            SHOP_KEEPER.createEverdellCard = (gameState) -> new CritterCard("Shop Keeper", SHOP_KEEPER, CardType.BLUE_GOVERNANCE, false, true, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                if (state.cardSelection.get(0).getCardEnumValue() == SHOP_KEEPER) {
                    return false;
                }
                //Check if it is a critter
                if (!state.cardSelection.get(0).isConstruction()) {
                    state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment(1);
                }
                return true;
            }, (everdellGameState -> {}));

            CASTLE.createEverdellCard = (gameState) -> new ConstructionCard("Castle", CASTLE, CardType.PURPLE_PROSPERITY, true, true, 4, new HashMap<>() {{
                put(ResourceTypes.TWIG, 2);
                put(ResourceTypes.RESIN, 3);
                put(ResourceTypes.PEBBLE, 2);
            }}, (state) -> {

                int counter = 0;
                //Find all non-unique constructions
                //Add a point for each non-unique construction
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.isConstruction() && !(card.isUnique())) {
                        counter++;
                    }
                }
                //Add the points to the castle card
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == CASTLE) {
                        card.setCardPoints(counter + 4);
                        break;
                    }
                }

                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(KING)));

            //NOT FULLY IMPLEMENTED BECAUSE IT REQUIRES SPECIAL EVENTS ASWELLL
            KING.createEverdellCard = (gameState) -> new CritterCard("King", KING, CardType.PURPLE_PROSPERITY, false, true, 4, new HashMap<>() {{
                put(ResourceTypes.BERRY, 6);
            }}, (state) -> {
                int counter = 0;

                for (var loc : state.Locations.values()) {
                    if (loc.getLocation() instanceof BasicEvent && loc.playersOnLocation.contains(state.getCurrentPlayer())) {
                        counter++;
                    }
                }
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == KING) {
                        card.setCardPoints(counter + 4);
                        break;
                    }
                }

                return true;
            }, (everdellGameState -> {}));


            PALACE.createEverdellCard = (gameState) -> new ConstructionCard("Palace", PALACE, CardType.PURPLE_PROSPERITY, true, true, 4, new HashMap<>() {{
                put(ResourceTypes.TWIG, 2);
                put(ResourceTypes.RESIN, 3);
                put(ResourceTypes.PEBBLE, 3);
            }}, (state) -> {
                int counter = 0;
                //Find all unique constructions
                //Add a point for each unique construction
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.isConstruction() && card.isUnique()) {
                        counter++;
                    }
                }
                //Add the points to the castle card
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == PALACE) {
                        card.setCardPoints(counter + 4);
                        break;
                    }
                }

                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(WIFE)));//INCORRECT


            THEATRE.createEverdellCard = (gameState) -> new ConstructionCard("Theatre", THEATRE, CardType.PURPLE_PROSPERITY, true, true, 3, new HashMap<>() {{
                put(ResourceTypes.TWIG, 3);
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                int counter = 0;
                //Find all unique critters
                //Add a point for each unique critter
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (!card.isConstruction() && card.isUnique()) {
                        counter++;
                    }
                }
                //Add the points to the Theatre card
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == THEATRE) {
                        card.setCardPoints(counter + 3);
                        break;
                    }
                }

                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(BARD)));

            SCHOOL.createEverdellCard = (gameState) -> new ConstructionCard("School", SCHOOL, CardType.PURPLE_PROSPERITY, true, true, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 2);
                put(ResourceTypes.RESIN, 2);
            }}, (state) -> {
                int counter = 0;
                //Find all common critters
                //Add a point for common critters
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (!card.isConstruction() && !card.isUnique()) {
                        counter++;
                    }
                }
                //Add the points to the School card
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == SCHOOL) {
                        card.setCardPoints(counter + 2);
                        break;
                    }
                }

                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(BARD)));

            BARD.createEverdellCard = (gameState) -> new CritterCard("Bard", BARD, CardType.TAN_TRAVELER, false, true, 0, new HashMap<>() {{
                put(ResourceTypes.BERRY, 3);
            }}, (state) -> {
                int counter = 0;

                //Discard up to 5 cards, Give 1 point for each card discarded
                for (var card : state.cardSelection) {
                    try {
                        state.playerHands.get(state.getCurrentPlayer()).remove((EverdellCard) card);
                        state.discardDeck.add(card);
                        state.cardCount[state.getCurrentPlayer()].decrement();
                        counter++;
                    } catch (Exception e) {
                        System.out.println("Error in Bard, Choices did not contain cards");
                    }
                }
                state.pointTokens[state.getCurrentPlayer()].increment(counter);
                return true;
            }, (everdellGameState -> {}));

            //There can definitely be a problem where if this card is placed when their village is at max capacity.
            //This is because the Ruin card will not be placed in the village, but the player will still be able to select a card to remove
            //Ruins can also currently select itself which probably should not be allowed
            //Look into this when you have revised the card system.
            RUINS.createEverdellCard = (gameState) -> new ConstructionCard("Ruins", RUINS, CardType.TAN_TRAVELER, true, false, 0, new HashMap<>() {{
            }}, (state) -> {

                if (!state.cardSelection.isEmpty()) {
                    //Remove the selected card from the village
                    System.out.println("Removing card from village");
                    System.out.println(state.cardSelection.get(0));

                    for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                        if (card == state.cardSelection.get(0)) {
                            state.playerVillage.get(state.getCurrentPlayer()).remove(card);
                            state.discardDeck.add(card);
                            break;
                        }
                    }

                    state.cardCount[state.getCurrentPlayer()].decrement();

                    //Refund the Resources
                    for (var resource : state.cardSelection.get(0).getResourceCost().keySet()) {
                        state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment(state.cardSelection.get(0).getResourceCost().get(resource));
                    }

                    //Draw 2 Cards
                    for (int i = 0; i < 2; i++) {
                        if (state.playerHands.get(state.getCurrentPlayer()).getSize() == state.playerHands.get(state.getCurrentPlayer()).getCapacity()) {
                            break;
                        }
                        state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                        state.cardCount[state.getCurrentPlayer()].increment();
                    }

                }

                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(BARD)));//THIS IS INCORRECT, BUT FOR TESTING PURPOSES. IT TAKES IN THE PEDDLER


            WOOD_CARVER.createEverdellCard = (gameState) -> new CritterCard("Wood Carver", WOOD_CARVER, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 0);
            }}, (state) -> {
                if (!state.resourceSelection.isEmpty()) {
                    //Increment Points based on how much wood was given
                    //It can take a max of 3 wood
                    int amount = Math.min(state.resourceSelection.get(ResourceTypes.TWIG).getValue(), 3);
                    amount = Math.min(amount, state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].getValue());
                    state.pointTokens[state.getCurrentPlayer()].increment(amount);
                    state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].decrement(amount);

                    //Reset it to 0
                    state.resourceSelection = new HashMap<ResourceTypes, Counter>();
                    state.resourceSelection.put(ResourceTypes.BERRY, new Counter());
                    state.resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
                    state.resourceSelection.put(ResourceTypes.RESIN, new Counter());
                    state.resourceSelection.put(ResourceTypes.TWIG, new Counter());
                }

                return true;
            }, (everdellGameState -> {}));

            DOCTOR.createEverdellCard = (gameState) -> new CritterCard("Doctor", DOCTOR, CardType.GREEN_PRODUCTION, false, true, 4, new HashMap<>() {{
                put(ResourceTypes.BERRY, 4);
            }}, (state) -> {
                if (!state.resourceSelection.isEmpty()) {
                    //Increment Points based on how many berries were given
                    //It can take a max of 3 berries
                    int amount = Math.min(state.resourceSelection.get(ResourceTypes.BERRY).getValue(), 3);
                    amount = Math.min(amount, state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].getValue());
                    state.pointTokens[state.getCurrentPlayer()].increment(amount);
                    state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].decrement(amount);

                    //Reset it to 0
                    state.resourceSelection = new HashMap<ResourceTypes, Counter>();
                    state.resourceSelection.put(ResourceTypes.BERRY, new Counter());
                    state.resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
                    state.resourceSelection.put(ResourceTypes.RESIN, new Counter());
                    state.resourceSelection.put(ResourceTypes.TWIG, new Counter());
                }

                return true;
            }, (everdellGameState -> {}));

            ARCHITECT.createEverdellCard = (gameState) -> new CritterCard("Architect", ARCHITECT, CardType.PURPLE_PROSPERITY, false, true, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 4);
            }}, (state) -> {
                //Architect gives points based on how many pebbles and Resin they have up to a maximum of 6 points

                int amount = 0;

                amount += state.PlayerResources.get(ResourceTypes.PEBBLE)[state.getCurrentPlayer()].getValue();
                amount += state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].getValue();

                amount = Math.min(amount, 6);

                //Add the points to the Architect card
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == ARCHITECT) {
                        card.setCardPoints(amount + 2);
                        break;
                    }
                }

                return true;
            }, (everdellGameState -> {}));

            PEDDLER.createEverdellCard = (gameState) -> new PeddlerCard("Peddler", PEDDLER, CardType.GREEN_PRODUCTION, false, false, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                        //Peddler, you can pay up to 2 of any resource and you can get 1 any for each resource paid
                        //Its effect exists within PeddlerCard
                        return false;
                    }, (everdellGameState -> {}));

            CHIP_SWEEP.createEverdellCard = (gameState) -> new CritterCard("Chip Sweep", CHIP_SWEEP, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 0);
            }}, (state) -> {
                //Chip Sweep takes in a production card and copies its effect.
                //The player can select which production card to copy

                if(state.cardSelection.isEmpty()){
                    return false;
                }

                EverdellCard card = state.cardSelection.get(0);

                System.out.println("Chip Sweep is copying: " + card);

                if (card.getCardType() == CardType.GREEN_PRODUCTION) {
                    if (card instanceof ConstructionCard constructionCard) {
                        System.out.println("Copying Construction Card");
                        constructionCard.applyCardEffect(state);
                    } else {
                        System.out.println("Copying Critter Card");
                        CritterCard critterCard = (CritterCard) card;
                        critterCard.applyCardEffect(state);
                    }
                    return true;
                }


                return false;
            }, (everdellGameState -> {}));

            LOOKOUT.createEverdellCard = (gameState) -> new ConstructionCard(RedDestinationLocation.LOOKOUT_DESTINATION, "Lookout", LOOKOUT, CardType.RED_DESTINATION, true, true, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(WANDERER)));

            QUEEN.createEverdellCard = (gameState) -> new CritterCard(RedDestinationLocation.QUEEN_DESTINATION, "Queen", QUEEN, CardType.RED_DESTINATION, true, true, 4, new HashMap<>() {{
                put(ResourceTypes.BERRY, 5);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}));

            INN.createEverdellCard = (gameState) -> new ConstructionCard(RedDestinationLocation.INN_DESTINATION, "Inn", INN, CardType.RED_DESTINATION, true, false, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 2);
                put(ResourceTypes.RESIN, 1);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of())); //THIS NEEDS TO OCCUPY INNKEEPER

           POST_OFFICE.createEverdellCard = (gameState) -> new PostOfficeCard(RedDestinationLocation.POST_OFFICE_DESTINATION, "Post Office", POST_OFFICE, CardType.RED_DESTINATION, true, false, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 2);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(POSTAL_PIGEON)));

            MONK.createEverdellCard = (gameState) -> new MonkCard("Monk", MONK, CardType.GREEN_PRODUCTION, false, true, 0, new HashMap<>() {{
                put(ResourceTypes.BERRY, 1);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}));

            FOOL.createEverdellCard = (gameState) -> new FoolCard("Fool", FOOL, CardType.TAN_TRAVELER, false, true, -2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 3);
            }}, (state) -> {
                return true;
            },(everdellGameState -> {}));

            TEACHER.createEverdellCard = (gameState) -> new TeacherCard("Teacher", TEACHER, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}));

            MONASTERY.createEverdellCard = (gameState) -> new MonasteryCard(RedDestinationLocation.MONASTERY_DESTINATION, "Monastery", MONASTERY, CardType.RED_DESTINATION, true, true, 1, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(MONK))); //THIS NEEDS TO OCCUPY MONK

            HISTORIAN.createEverdellCard = (gameState) -> new CritterCard("Historian", HISTORIAN, CardType.BLUE_GOVERNANCE, false, true, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 0);
            }}, (state) -> {
                if(state.currentCard.getCardEnumValue() == HISTORIAN ){
                    return true;
                }
                //Makes you draw 1 card whenever a construction or critter card is played
                if(state.playerVillage.get(state.getCurrentPlayer()).getSize() < state.playerVillage.get(state.getCurrentPlayer()).getCapacity()){
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
                return true;
            }, (everdellGameState -> {}));

            CEMETERY.createEverdellCard = (gameState) -> new CemeteryCard(RedDestinationLocation.CEMETERY_DESTINATION,"Cemetery", CEMETERY, CardType.RED_DESTINATION, true, true, 0, new HashMap<>() {{
                put(ResourceTypes.PEBBLE, 0);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {}), new ArrayList<>(List.of(UNDERTAKER))); //THIS NEEDS TO OCCUPY Undertaker

            UNDERTAKER.createEverdellCard = (gameState) -> new CritterCard("Undertaker", UNDERTAKER, CardType.TAN_TRAVELER, false, true, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 0);
            }}, (state) -> {
                //Undertaker allows the player to remove 3 cards from the meadow,
                //Replenish those cards, and then allow the player to draw 1 card from the meadow

                //CardSelection[0] will hold the card the player selects from the meadow

                //Unlock the second Location of the cemetary card (if it exists within the village)
                state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof CemeteryCard).forEach(c -> {
                    CemeteryCard cc = (CemeteryCard) c;
                    cc.unlockSecondLocation();
                });

                if(state.playerHands.get(state.getCurrentPlayer()).getSize() < 8){
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardSelection.get(0));
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
                return true;
            }, (everdellGameState -> {}));

            POSTAL_PIGEON.createEverdellCard = (gameState) -> new CritterCard("Postal Pigeon", POSTAL_PIGEON, CardType.TAN_TRAVELER, false, false, 0, new HashMap<>() {{
                put(ResourceTypes.BERRY, 0);
            }}, (state) -> {
                //The postal pigeon allows the player to reveal 2 cards, the player may select 1 of these cards to play for free
                // up to 3 points

                //CardSelection[0] will hold the card that the player chooses to play for free
                //CardSelection[1] will hold the card that the player chose to discard
                //If CardSelection[0] has a point token value higher than 3, it will not be paid for

                if (state.cardSelection.get(0).getPoints() <= 3) {
                    state.cardSelection.get(0).payForCard();
                    state.discardDeck.add(state.cardSelection.get(1));
                }
                return true;
            }, (everdellGameState -> {}));

            JUDGE.createEverdellCard = (gameState) -> new JudgeCard("Judge", JUDGE, CardType.BLUE_GOVERNANCE, false, true, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 0);
            }}, (state) -> {
                //The Judge allows the player to select a card from the meadow and play it for free
                return true;
        }, (everdellGameState -> {}));

        COURTHOUSE.createEverdellCard = (gameState) -> new ConstructionCard("Courthouse", COURTHOUSE, CardType.BLUE_GOVERNANCE, true, true, 2, new HashMap<>() {{
            put(ResourceTypes.TWIG, 0);
            put(ResourceTypes.RESIN, 0);
            put(ResourceTypes.PEBBLE, 0);
        }}, (state) -> {
            //The Courthouse allows the player to select a card from the meadow and play it for free
            return true;
        }, (everdellGameState -> {}),
                new ArrayList<>(List.of(JUDGE)));

        CRANE.createEverdellCard = (gameState) -> new ConstructionCard("Crane", CRANE, CardType.BLUE_GOVERNANCE, true, true, 1, new HashMap<>() {{
            put(ResourceTypes.PEBBLE,0);
        }}, (state) -> {
            //This gives a discount of 3 resources to a card
            //ResourceSelection will tell us which 3 resources they had selected
            //CurrentCard will represent which card the player is trying to place

            int discountCounter = 0;

            //Give resources as a way of applying the discount

            // **NOTE** This card will currently add the resources even if the card costs less than 3 resources
            // Currently assumed that the player will always select the correct amount of resources
            for(var resource : state.resourceSelection.keySet()){
                for(int i=0; i<state.resourceSelection.get(resource).getValue(); i++){
                    if(discountCounter == 3){
                        break;
                    }
                    discountCounter++;
                    state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment();
                }
                if(discountCounter == 3){
                    break;
                }
            }

            //Remove Resources based on card cost
            for(var resource : state.currentCard.getResourceCost().keySet()){
                state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement(state.currentCard.getResourceCost().get(resource));
            }
            state.currentCard.payForCard();

            return true;
        }, (everdellGameState -> {}), new ArrayList<>(List.of(ARCHITECT)));


        INNKEEPER.createEverdellCard = (gameState) -> new CritterCard("Innkeeper", INNKEEPER, CardType.BLUE_GOVERNANCE, false, true, 1, new HashMap<>() {{
            put(ResourceTypes.BERRY, 0);
        }}, (state) -> {
            //Innkeeper allows the player to discount a card up to 3 Berries
            //ResourceSelection will tell us which 3 resources they had selected
            //CurrentCard will represent which card the player is trying to place

            int discountCounter = 0;

            //Give resources as a way of applying the discount
            for(int i=0; i<state.resourceSelection.get(ResourceTypes.BERRY).getValue(); i++){
                //If we have already added an amount of resources that the card costs, we stop
                if(discountCounter == state.currentCard.getResourceCost().get(ResourceTypes.BERRY)){
                    break;
                }

                if(discountCounter == 3){
                    break;
                }
                discountCounter++;
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment();
            }
            state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].decrement(state.currentCard.getResourceCost().get(ResourceTypes.BERRY));

            state.currentCard.payForCard();

            return true;
        }, (everdellGameState -> {}));

        }
    }
    

    public HashMap<CardType, Color> cardColour = new HashMap<CardType, Color>() {{
        put(CardType.BLUE_GOVERNANCE, new Color(45, 114, 173));
        put(CardType.GREEN_PRODUCTION, new Color(48, 126, 38));
        put(CardType.PURPLE_PROSPERITY, new Color(152, 53, 190));
        put(CardType.RED_DESTINATION, new Color(201, 23, 23));
        put(CardType.TAN_TRAVELER, new Color(162, 131, 93));
    }};

    public static ArrayList<Color> playerColour = new ArrayList<>() {{
        add(Color.RED);
        add(Color.ORANGE);
        add(Color.CYAN);
        add(Color.GREEN);
    }};


    HashMap<CardDetails, Integer> everdellCardCount = new HashMap<CardDetails, Integer>() {{
        put(CardDetails.FARM, 15);
        put(CardDetails.RESIN_REFINERY, 0);
        put(CardDetails.GENERAL_STORE, 0);
        put(CardDetails.WANDERER, 0);
        put(CardDetails.WIFE, 15);
        put(CardDetails.HUSBAND, 0);
        put(CardDetails.FAIRGROUNDS, 0);
        put(CardDetails.MINE, 0);
        put(CardDetails.TWIG_BARGE, 0);
        put(CardDetails.SHOP_KEEPER, 0);
        put(CardDetails.BARGE_TOAD, 0);
        put(CardDetails.CASTLE, 0);
        put(CardDetails.KING, 0);
        put(CardDetails.PALACE, 0);
        put(CardDetails.THEATRE, 0);
        put(CardDetails.SCHOOL, 0);
        put(CardDetails.BARD, 0);
        put(CardDetails.RUINS, 0);
        put(CardDetails.WOOD_CARVER, 0);
        put(CardDetails.DOCTOR, 0);
        put(CardDetails.PEDDLER, 0);
        put(CardDetails.LOOKOUT, 0);
        put(CardDetails.QUEEN, 0);
        put(CardDetails.INN, 0);
        put(CardDetails.POST_OFFICE, 0);
        put(CardDetails.MONK, 0);
        put(CardDetails.FOOL, 0);
        put(CardDetails.TEACHER, 0);
        put(CardDetails.MONASTERY, 0);
        put(CardDetails.HISTORIAN, 0);
        put(CardDetails.CEMETERY, 0);
        put(CardDetails.UNDERTAKER, 0);
        put(CardDetails.POSTAL_PIGEON, 0);
        put(CardDetails.JUDGE, 0);
        put(CardDetails.CHIP_SWEEP, 0);
        put(CardDetails.CRANE, 1);
        put(CardDetails.INNKEEPER, 10);
    }};

    @Override
    protected AbstractParameters _copy() {
        // TODO: deep copy of all variables.

        return this;
    }

    @Override
    protected boolean _equals(Object o) {
        // TODO: compare all variables.
        return o instanceof EverdellParameters;
    }

    @Override
    public int hashCode() {
        // TODO: include the hashcode of all variables.
        return super.hashCode();
    }

}
