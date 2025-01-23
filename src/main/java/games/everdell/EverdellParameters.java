package games.everdell;

import com.beust.ah.A;
import core.AbstractGameState;
import core.AbstractParameters;
import core.actions.AbstractAction;
import core.components.Counter;
import evaluation.optimisation.TunableParameters;
import games.catan.components.CatanCard;
import games.everdell.components.*;
import games.loveletter.LoveLetterGameState;
import games.loveletter.actions.HandmaidAction;
import games.loveletter.actions.PlayCard;
import org.checkerframework.checker.units.qual.C;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
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
        void applyLocationEffect(EverdellGameState state);
        String name();
    }

    public enum BasicLocations implements AbstractLocations{
        THREE_WOOD, TWO_WOOD_ONE_CARD, ONE_BERRY, ONE_BERRY_ONE_CARD, ONE_PEBBLE, TWO_CARD_ONE_POINT, TWO_RESIN, ONE_RESIN_ONE_CARD, ONE_BERRY_RED_DESTINATION;

        public Function<EverdellGameState, BasicLocations> applyLocationEffect;

        @Override
        public void applyLocationEffect(EverdellGameState state) {
            applyLocationEffect.apply(state);
        }

        static{
            THREE_WOOD.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(3);
                return THREE_WOOD;
            };
            TWO_WOOD_ONE_CARD.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(2);
                if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    state.cardCount[state.playerTurn].increment();
                }
                return TWO_WOOD_ONE_CARD;
            };
            ONE_BERRY.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment(1);
                return ONE_BERRY;
            };
            ONE_BERRY_ONE_CARD.applyLocationEffect = (state) -> {
                    state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment();
                    if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                        state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                        state.cardCount[state.playerTurn].increment();
                    }
                return ONE_BERRY_ONE_CARD;
            };
            ONE_PEBBLE.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.PEBBLE)[state.playerTurn].increment(1);
                return ONE_PEBBLE;
            };
            TWO_CARD_ONE_POINT.applyLocationEffect = (state) -> {
                state.pointTokens[state.playerTurn].increment();
                if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()-1){
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                } else if (state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                }
                state.cardCount[state.playerTurn].increment(2);
                return TWO_CARD_ONE_POINT;
            };
            TWO_RESIN.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment(2);
                return TWO_RESIN;
            };
            ONE_RESIN_ONE_CARD.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment();
                if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                }
                state.cardCount[state.playerTurn].increment();
                return ONE_RESIN_ONE_CARD;
            };
        }
    }

    public enum ForestLocations implements AbstractLocations{
        THREE_BERRY,TWO_BERRY_ONE_CARD,TWO_RESIN_ONE_TWIG,THREE_CARDS_ONE_PEBBLE,ONE_TWIG_ONE_RESIN_ONE_BERRY, TWO_ANY, TWO_CARDS_ONE_ANY,
        DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED, DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED,
        /*DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT*/ COPY_BASIC_LOCATION_DRAW_CARD;

        public Function<EverdellGameState, ForestLocations> applyLocationEffect;


        //Maybe a bad design decision. Maybe good?
        public static ArrayList<EverdellCard> cardChoices;
        public static BasicLocations basicLocationChoice;

        @Override
        public void applyLocationEffect(EverdellGameState state) {
            applyLocationEffect.apply(state);
        }

        static{
            THREE_BERRY.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment(3);
                return THREE_BERRY;
            };
            TWO_BERRY_ONE_CARD.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment(2);
                if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    state.cardCount[state.playerTurn].increment();
                }
                return TWO_BERRY_ONE_CARD;
            };
            TWO_RESIN_ONE_TWIG.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment(2);
                state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(1);

                return TWO_RESIN_ONE_TWIG;
            };
            THREE_CARDS_ONE_PEBBLE.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.PEBBLE)[state.playerTurn].increment(1);

                for(int i = 0 ; i<3; i++){
                    if(state.playerHands.get(state.playerTurn).getSize() == state.playerHands.get(state.playerTurn).getCapacity()){
                        break;
                    }
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    state.cardCount[state.playerTurn].increment();
                }

                return THREE_CARDS_ONE_PEBBLE;
            };
            ONE_TWIG_ONE_RESIN_ONE_BERRY.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment(1);
                state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(1);
                state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment(1);

                return ONE_TWIG_ONE_RESIN_ONE_BERRY;
            };

            TWO_ANY.applyLocationEffect = (state) ->{
                for(var resources :  state.resourceSelection.keySet()){
                    state.PlayerResources.get(resources)[state.playerTurn].increment(state.resourceSelection.get(resources).getValue());
                }


                return TWO_ANY;
            };
            TWO_CARDS_ONE_ANY.applyLocationEffect = (state) ->{

                //Add the selected resources
                for(var resources :  state.resourceSelection.keySet()){
                    state.PlayerResources.get(resources)[state.playerTurn].increment(state.resourceSelection.get(resources).getValue());
                }

                // Draw two cards
                for(int i = 0 ; i<2; i++){
                    if(state.playerHands.get(state.playerTurn).getSize() == state.playerHands.get(state.playerTurn).getCapacity()){
                        break;
                    }
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    state.cardCount[state.playerTurn].increment();
                }

                return TWO_CARDS_ONE_ANY;
            };

            DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED.applyLocationEffect = (state) ->{
                //Discard a card
                for(var card : cardChoices){
                    try{
                        state.playerHands.get(state.playerTurn).remove((EverdellCard) card);
                        state.cardCount[state.playerTurn].decrement();
                    } catch (Exception e){
                        System.out.println("Error in Forest Locations, Choices did not contain cards");
                    }
                }


                //Draw two cards
                for(int i = 0 ; i<(cardChoices.size()*2); i++){
                    if(state.playerHands.get(state.playerTurn).getSize() == state.playerHands.get(state.playerTurn).getCapacity()){
                        break;
                    }
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    state.cardCount[state.playerTurn].increment();
                }
                return DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED;
            };

            DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED.applyLocationEffect = (state) ->{
                //Discard a card
                for(var card : cardChoices){
                    try{
                        state.playerHands.get(state.playerTurn).remove((EverdellCard) card);
                        state.cardCount[state.playerTurn].decrement();
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

                        state.PlayerResources.get(resource)[state.playerTurn].increment(1);
                    }
                }
                return DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED;
            };

            COPY_BASIC_LOCATION_DRAW_CARD.applyLocationEffect = (state) ->{
                //Copy a basic location
                basicLocationChoice.applyLocationEffect.apply(state);

                //Draw a card
                if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    state.cardCount[state.playerTurn].increment();
                }
                return COPY_BASIC_LOCATION_DRAW_CARD;
            };

            //THIS NEEDS EXTRA WORK, WHEN I HAVE ADDDED COSTS TO CARDS
//            DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT.applyLocationEffect = (state) ->{
//                //Draw two cards
//                //Card Choices will hold the cards that the player drew
//                for(var card : cardChoices){
//                    try{
//                        state.playerHands.get(state.playerTurn).add(card);
//                        state.cardCount[state.playerTurn].increment();
//                    } catch (Exception e){
//                        System.out.println("Error in Forest Locations, Choices did not contain cards");
//                    }
//                }
//
//                //Card Selection will hold the card that the player selected to play at a discount
//                //Play one card at a discount
//                for(var card : cardChoices){
//                    try{
//                        state.playerHands.get(state.playerTurn).remove((EverdellCard) card);
//                        state.cardCount[state.playerTurn].increment();
//                    } catch (Exception e){
//                        System.out.println("Error in Forest Locations, Choices did not contain cards");
//                    }
//                }
//                return DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT;
//            };
        }
    }

    public enum BasicEvent implements AbstractLocations{
        GREEN_PRODUCTION_EVENT, RED_DESTINATION_EVENT, BLUE_GOVERNANCE_EVENT, TAN_TRAVELER_EVENT;

        public Function<EverdellGameState, BasicEvent> applyLocationEffect;

        @Override
        public void applyLocationEffect(EverdellGameState state) {
            applyLocationEffect.apply(state);
        }


    }

    public enum RedDestinationLocation implements AbstractLocations{
        LOOKOUT_DESTINATION, QUEEN_DESTINATION, INN_DESTINATION;


        public static AbstractLocations copyLocationChoice;
        public Function<EverdellGameState, RedDestinationLocation> applyLocationEffect;

        @Override
        public void applyLocationEffect(EverdellGameState state) {
            applyLocationEffect.apply(state);
        }

        static{
            LOOKOUT_DESTINATION.applyLocationEffect = (state) -> {
                if(copyLocationChoice != null){
                    copyLocationChoice.applyLocationEffect(state);
                }

                return LOOKOUT_DESTINATION;
            };
            QUEEN_DESTINATION.applyLocationEffect = (state) -> {
                state.cardSelection.get(0).payForCard();

                return QUEEN_DESTINATION;
            };

            INN_DESTINATION.applyLocationEffect = (state) -> {
                //From gameState Resource Selection will tell us how much of a discount will be applied.
                //The card selection will hold the card that the player selected to play at a discount

                for(var playerResource : state.PlayerResources.keySet()){
                    int discount = state.resourceSelection.get(playerResource).getValue();
                    int initialCost = state.cardSelection.get(0).getResourceCost().get(playerResource);

                    int finalCost = discount-initialCost;

                    state.PlayerResources.get(playerResource)[state.playerTurn].decrement(finalCost);
                }
                state.cardSelection.get(0).payForCard();
                return INN_DESTINATION;
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
        CASTLE, KING, PALACE, BARD, THEATRE, SCHOOL, RUINS, WOOD_CARVER, DOCTOR, ARCHITECT, PEDDLER, CHIP_SWEEP, LOOKOUT, QUEEN, INN;

        public Function<EverdellGameState, EverdellCard> createEverdellCard;

        static{
            FARM.createEverdellCard = (gamestate) -> new ConstructionCard("Farm", FARM, CardType.GREEN_PRODUCTION, true, false,1,
            new HashMap<>()
            {{
                put(ResourceTypes.TWIG, 2);
                put(ResourceTypes.RESIN, 1);
            }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment();
                return true;
            }
            ,  new ArrayList<>(List.of(WIFE)));


            RESIN_REFINERY.createEverdellCard = (gameState) -> new ConstructionCard("Resin Refinery", RESIN_REFINERY, CardType.GREEN_PRODUCTION, true, false,1, new HashMap<>()
            {{
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment();
                return true;
            }, new ArrayList<>(List.of(CHIP_SWEEP)));

            GENERAL_STORE.createEverdellCard = (gameState) -> new ConstructionCard("General Store", GENERAL_STORE, CardType.GREEN_PRODUCTION, true, false,1, new HashMap<>()
                {{
                    put(ResourceTypes.RESIN, 1);
                    put(ResourceTypes.PEBBLE, 1);
                }}, (state) -> {
                    state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment();

                    for(var everdellCard : state.playerVillage.get(state.playerTurn).getComponents()){
                        if(everdellCard.getCardEnumValue() == FARM){
                            state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment();
                            break;
                        }
                    }

                    return true;
            }, new ArrayList<>(List.of(SHOP_KEEPER)));

            //Wanderer also has a special feature that it does not take up a village slot NEEDS TO BE IMPLEMENTED
            WANDERER.createEverdellCard = (gameState) -> new CritterCard("Wanderer", WANDERER, CardType.TAN_TRAVELER, false, false,1, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 2);
                }}, (state) -> {
                    for(int i = 0 ; i<3; i++){
                        if(state.playerHands.get(state.playerTurn).getSize() == state.playerHands.get(state.playerTurn).getCapacity()){
                            break;
                        }
                        state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                        state.cardCount[state.playerTurn].increment();
                    }
                    return true;
            });
            WIFE.createEverdellCard = (gameState) -> new CritterCard("Wife", WIFE, CardType.PURPLE_PROSPERITY, true, false,1, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 2);
                }}, (state) -> {
                    for(var everdellCard : state.playerVillage.get(state.playerTurn).getComponents()){
                        if(everdellCard.getCardEnumValue() == HUSBAND){

                        }
                    }

                    return true;
            });

            HUSBAND.createEverdellCard = (gameState) -> new CritterCard("Husband", HUSBAND, CardType.GREEN_PRODUCTION, false, false,2, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 2);
                }}, (state) -> {
                    boolean isThereWife = false;
                    boolean isThereFarm = false;

                    for(var card : state.playerVillage.get(state.playerTurn)){
                        if(card.getCardEnumValue() == WIFE){
                            isThereWife = true;
                        }
                        if(card.getCardEnumValue() == FARM){
                            isThereFarm = true;
                        }
                    }
                    if(isThereFarm && isThereWife){
                        int counter = 0;
                        for(var resource : state.resourceSelection.keySet()){
                            for(int i = 0 ; i<state.resourceSelection.get(resource).getValue(); i++){
                                if(counter == 2){
                                    break;
                                }
                                counter++;
                                state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment(1);
                            }
                        }
                    }

                    return true;
            });

            FAIRGROUNDS.createEverdellCard = (gameState) -> new ConstructionCard("Fairgrounds", FAIRGROUNDS, CardType.GREEN_PRODUCTION, true, true, 3, new HashMap<>()
                {{
                    put(ResourceTypes.TWIG, 1);
                    put(ResourceTypes.RESIN, 2);
                    put(ResourceTypes.PEBBLE, 1);
                }}, (state) -> {
                for(int i = 0 ; i<2; i++){
                    if(state.playerHands.get(state.playerTurn).getSize() == state.playerHands.get(state.playerTurn).getCapacity()){
                        break;
                    }
                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    state.cardCount[state.playerTurn].increment();
                }
                return true;
            }, new ArrayList<>(List.of(WIFE)));//WIFE is incorrect. but this is for testing purposes

            MINE.createEverdellCard = (gameState) -> new ConstructionCard("Mine", MINE, CardType.GREEN_PRODUCTION, true, false, 2, new HashMap<>()
                {{
                    put(ResourceTypes.PEBBLE, 1);
                    put(ResourceTypes.TWIG, 1);
                    put(ResourceTypes.RESIN, 1);
                }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.PEBBLE)[state.playerTurn].increment(1);
                return true;
            }, new ArrayList<>(List.of(WIFE)));//WIFE is incorrect. but this is for testing purposes

            TWIG_BARGE.createEverdellCard = (gameState) -> new ConstructionCard("Twig Barge", TWIG_BARGE, CardType.GREEN_PRODUCTION, true, false, 1, new HashMap<>()
                {{
                    put(ResourceTypes.TWIG, 1);
                    put(ResourceTypes.PEBBLE, 1);
                }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(2);
                return true;
            }, new ArrayList<>(List.of(BARGE_TOAD)));

            BARGE_TOAD.createEverdellCard = (gameState) -> new CritterCard("Barge Toad", BARGE_TOAD, CardType.GREEN_PRODUCTION, false, false, 1, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 2);
                }}, (state) -> {
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(card.getCardEnumValue() == FARM){
                        state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(2);
                    }
                }
                return true;
            });


            SHOP_KEEPER.createEverdellCard = (gameState) -> new CritterCard("Shop Keeper", SHOP_KEEPER, CardType.BLUE_GOVERNANCE, false, true, 1, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 2);
                }}, (state) -> {
                if(state.currentCard.getCardEnumValue() == SHOP_KEEPER){
                    return false;
                }
                //Check if it is a critter
                if (!state.currentCard.isConstruction()) {
                    state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment(1);
                }
                return true;
            });

            CASTLE.createEverdellCard = (gameState) -> new ConstructionCard("Castle", CASTLE, CardType.PURPLE_PROSPERITY, true, true, 4, new HashMap<>()
                {{
                    put(ResourceTypes.TWIG, 2);
                    put(ResourceTypes.RESIN, 3);
                    put(ResourceTypes.PEBBLE, 2);
                }}, (state) -> {

                int counter = 0;
                //Find all non-unique constructions
                //Add a point for each non-unique construction
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(card.isConstruction() && !(card.isUnique())){
                        counter++;
                    }
                }
                //Add the points to the castle card
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(card.getCardEnumValue() == CASTLE){
                        card.setCardPoints(counter + 4);
                        break;
                    }
                }

                return true;
            }, new ArrayList<>(List.of(KING)));

            //NOT FULLY IMPLEMENTED BECAUSE IT REQUIRES SPECIAL EVENTS ASWELLL
            KING.createEverdellCard = (gameState) -> new CritterCard("King", KING, CardType.PURPLE_PROSPERITY, false, true, 4, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 6);
                }}, (state) -> {
                int counter = 0;

                for(var loc : state.Locations.values()){
                    if(loc.getLocation() instanceof BasicEvent && loc.playersOnLocation.contains(state.playerTurn)){
                        counter++;
                    }
                }
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(card.getCardEnumValue() == KING){
                        card.setCardPoints(counter + 4);
                        break;
                    }
                }

                return true;
            });


            PALACE.createEverdellCard = (gameState) -> new ConstructionCard("Palace", PALACE, CardType.PURPLE_PROSPERITY, true, true, 4, new HashMap<>()
                {{
                    put(ResourceTypes.TWIG, 2);
                    put(ResourceTypes.RESIN, 3);
                    put(ResourceTypes.PEBBLE, 3);
                }}, (state) -> {
                int counter = 0;
                //Find all unique constructions
                //Add a point for each unique construction
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(card.isConstruction() && card.isUnique()){
                        counter++;
                    }
                }
                //Add the points to the castle card
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(card.getCardEnumValue() == PALACE){
                        card.setCardPoints(counter + 4);
                        break;
                    }
                }

                return true;
            }, new ArrayList<>(List.of(WIFE)));//INCORRECT


            THEATRE.createEverdellCard = (gameState) -> new ConstructionCard("Theatre", THEATRE, CardType.PURPLE_PROSPERITY, true, true, 3, new HashMap<>()
                {{
                    put(ResourceTypes.TWIG, 3);
                    put(ResourceTypes.RESIN, 1);
                    put(ResourceTypes.PEBBLE, 1);
                }}, (state) -> {
                int counter = 0;
                //Find all unique critters
                //Add a point for each unique critter
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(!card.isConstruction() && card.isUnique()){
                        counter++;
                    }
                }
                //Add the points to the Theatre card
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(card.getCardEnumValue() == THEATRE){
                        card.setCardPoints(counter + 3);
                        break;
                    }
                }

                return true;
            }, new ArrayList<>(List.of(BARD)));

            SCHOOL.createEverdellCard = (gameState) -> new ConstructionCard("School", SCHOOL, CardType.PURPLE_PROSPERITY, true, true, 2, new HashMap<>()
                {{
                    put(ResourceTypes.TWIG, 2);
                    put(ResourceTypes.RESIN, 2);
                }}, (state) -> {
                int counter = 0;
                //Find all common critters
                //Add a point for common critters
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(!card.isConstruction() && !card.isUnique()){
                        counter++;
                    }
                }
                //Add the points to the School card
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(card.getCardEnumValue() == SCHOOL){
                        card.setCardPoints(counter + 2);
                        break;
                    }
                }

                return true;
            }, new ArrayList<>(List.of(BARD)));

            BARD.createEverdellCard = (gameState) -> new CritterCard("Bard", BARD, CardType.TAN_TRAVELER, false, true, 0, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 3);
                }}, (state) -> {
                int counter = 0;

                //Discard up to 5 cards, Give 1 point for each card discarded
                for(var card : state.cardSelection){
                    try{
                        state.playerHands.get(state.playerTurn).remove((EverdellCard) card);
                        state.cardCount[state.playerTurn].decrement();
                        counter++;
                    } catch (Exception e){
                        System.out.println("Error in Bard, Choices did not contain cards");
                    }
                }
                state.pointTokens[state.playerTurn].increment(counter);
                return true;
            });

            //There can definitely be a problem where if this card is placed when their village is at max capacity.
            //This is because the Ruin card will not be placed in the village, but the player will still be able to select a card to remove
            //Ruins can also currently select itself which probably should not be allowed
            //Look into this when you have revised the card system.
            RUINS.createEverdellCard = (gameState) -> new ConstructionCard("Ruins", RUINS, CardType.TAN_TRAVELER, true, false, 0, new HashMap<>()
                {{
                }}, (state) -> {


                if(!state.cardSelection.isEmpty()){
                    if(state.cardSelection.get(0) == state.currentCard || !state.cardSelection.get(0).isConstruction()){
                        return false;
                    }
                    //Remove the selected card from the village
                    System.out.println("Removing card from village");
                    System.out.println(state.cardSelection.get(0));

                    for(var card : state.playerVillage.get(state.getCurrentPlayer())){
                        if(card == state.cardSelection.get(0)){
                            state.playerVillage.get(state.playerTurn).remove(card);
                            break;
                        }
                    }

                    state.cardCount[state.playerTurn].decrement();

                    //Refund the Resources
                    for(var resource : state.cardSelection.get(0).getResourceCost().keySet()){
                        state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment(state.cardSelection.get(0).getResourceCost().get(resource));
                    }

                    //Draw 2 Cards
                    for(int i = 0 ; i<2; i++){
                        if(state.playerHands.get(state.playerTurn).getSize() == state.playerHands.get(state.playerTurn).getCapacity()){
                            break;
                        }
                        state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                        state.cardCount[state.playerTurn].increment();
                    }

                }

                return true;
            }, new ArrayList<>(List.of(BARD)));//THIS IS INCORRECT, BUT FOR TESTING PURPOSES. IT TAKES IN THE PEDDLER


            WOOD_CARVER.createEverdellCard = (gameState) -> new CritterCard("Wood Carver", WOOD_CARVER, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 2);
                }}, (state) -> {
                if(!state.resourceSelection.isEmpty()){
                    //Increment Points based on how much wood was given
                    //It can take a max of 3 wood
                    int amount = Math.min(state.resourceSelection.get(ResourceTypes.TWIG).getValue(), 3);
                    amount = Math.min(amount, state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].getValue());
                    state.pointTokens[state.playerTurn].increment(amount);
                    state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].decrement(amount);

                    //Reset it to 0
                    state.resourceSelection = new HashMap<ResourceTypes, Counter>();
                    state.resourceSelection.put(ResourceTypes.BERRY, new Counter());
                    state.resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
                    state.resourceSelection.put(ResourceTypes.RESIN, new Counter());
                    state.resourceSelection.put(ResourceTypes.TWIG, new Counter());
                }

                return true;
            });

            DOCTOR.createEverdellCard = (gameState) -> new CritterCard("Doctor", DOCTOR, CardType.GREEN_PRODUCTION, false, true, 4, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 4);
                }}, (state) -> {
                if(!state.resourceSelection.isEmpty()){
                    //Increment Points based on how many berries were given
                    //It can take a max of 3 berries
                    int amount = Math.min(state.resourceSelection.get(ResourceTypes.BERRY).getValue(), 3);
                    amount = Math.min(amount, state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].getValue());
                    state.pointTokens[state.playerTurn].increment(amount);
                    state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].decrement(amount);

                    //Reset it to 0
                    state.resourceSelection = new HashMap<ResourceTypes, Counter>();
                    state.resourceSelection.put(ResourceTypes.BERRY, new Counter());
                    state.resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
                    state.resourceSelection.put(ResourceTypes.RESIN, new Counter());
                    state.resourceSelection.put(ResourceTypes.TWIG, new Counter());
                }

                return true;
            });

            ARCHITECT.createEverdellCard = (gameState) -> new CritterCard("Architect", ARCHITECT, CardType.PURPLE_PROSPERITY, false, true, 2, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 4);
                }}, (state) -> {
                //Architect gives points based on how many pebbles and Resin they have up to a maximum of 6 points

                int amount = 0;

                amount += state.PlayerResources.get(ResourceTypes.PEBBLE)[state.getCurrentPlayer()].getValue();
                amount += state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].getValue();

                amount = Math.min(amount, 6);

                //Add the points to the Architect card
                for(var card : state.playerVillage.get(state.playerTurn)){
                    if(card.getCardEnumValue() == ARCHITECT){
                        card.setCardPoints(amount + 2);
                        break;
                    }
                }

                return true;
            });

            PEDDLER.createEverdellCard = (gameState) -> new PeddlerCard("Peddler", PEDDLER, CardType.GREEN_PRODUCTION, false, false, 1, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 2);
                }}, new HashMap<>(), new HashMap<>(),
                    (state) -> {
                //Peddler, you can pay up to 2 of any resource and you can get 1 any for each resource paid
                //Its effect exists within PeddlerCard
                return false;
            });

            CHIP_SWEEP.createEverdellCard = (gameState) -> new CritterCard("Chip Sweep", CHIP_SWEEP, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 3);
                }}, (state) -> {
                //Chip Sweep takes in a production card and copies its effect.
                //The player can select which production card to copy
                for(var card : state.cardSelection){
                    if(card.getCardType() == CardType.GREEN_PRODUCTION){
                        if(card instanceof ConstructionCard constructionCard){
                            constructionCard.applyCardEffect(state);
                        }
                        else{
                            CritterCard critterCard = (CritterCard) card;
                            critterCard.applyCardEffect(state);
                        }
                        return true;
                    }
                }


                return false;
            });

            LOOKOUT.createEverdellCard = (gameState) -> new ConstructionCard(RedDestinationLocation.LOOKOUT_DESTINATION,"Lookout", LOOKOUT, CardType.RED_DESTINATION, true, true, 2, new HashMap<>()
                {{
                    put(ResourceTypes.TWIG, 1);
                    put(ResourceTypes.RESIN, 1);
                    put(ResourceTypes.PEBBLE, 1);
                }}, (state) -> {
                return true;
            }, new ArrayList<>(List.of(WANDERER)));

            QUEEN.createEverdellCard = (gameState) -> new CritterCard(RedDestinationLocation.QUEEN_DESTINATION,"Queen", QUEEN, CardType.RED_DESTINATION, true, true, 4, new HashMap<>()
                {{
                    put(ResourceTypes.BERRY, 5);
                }}, (state) -> {
                return true;
            });

            INN.createEverdellCard = (gameState) -> new ConstructionCard(RedDestinationLocation.INN_DESTINATION,"Inn", INN, CardType.RED_DESTINATION, true, false, 2, new HashMap<>()
                {{
                    put(ResourceTypes.TWIG, 2);
                    put(ResourceTypes.RESIN, 1);
                }}, (state) -> {
                return true;
            }, new ArrayList<>(List.of())); //THIS NEEDS TO OCCUPY INNKEEPER


        }
    }



//    (new EverdellCard("Farm", CardType.GREEN_PRODUCTION, true, 1, new HashMap<>() {{
//        put(ResourceTypes.TWIG, 2);
//        put(ResourceTypes.RESIN, 1);
//    }}, (state) -> {
//        state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment();
//        return true;
//    }));



//
//    public enum x {
//        FARM, RESIN_REFINERY, GENERAL_STORE, WANDERER, WIFE, HUSBAND;
//
//        public CardType cardType;
//        public Function<EverdellGameState, CardDetails> applyCardEffect;
//        public HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost;
//        public int points;
//
//        static{
//            //Get 1 Berry
//            FARM.resourceCost = new HashMap<>();
//            FARM.cardType = CardType.GREEN_PRODUCTION;
//            FARM.resourceCost.put(EverdellParameters.ResourceTypes.TWIG, 2);
//            FARM.resourceCost.put(EverdellParameters.ResourceTypes.RESIN, 1);
//            FARM.points = 1;
//            FARM.applyCardEffect = (state) -> {
//                state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.playerTurn].increment();
//                return FARM;
//            };
//
//
//
//            //Get 1 Resin
//            RESIN_REFINERY.resourceCost = new HashMap<>();
//            RESIN_REFINERY.cardType = CardType.GREEN_PRODUCTION;
//            RESIN_REFINERY.resourceCost.put(EverdellParameters.ResourceTypes.RESIN, 1);
//            RESIN_REFINERY.resourceCost.put(EverdellParameters.ResourceTypes.PEBBLE, 1);
//            RESIN_REFINERY.points = 1;
//            RESIN_REFINERY.applyCardEffect = (state) -> {
//                state.PlayerResources.get(EverdellParameters.ResourceTypes.RESIN)[state.playerTurn].increment();
//                return RESIN_REFINERY;
//            };
//
//            //Get 1 Berry, Get an extra berry if the player has a farm. Extra berry can only be triggered once per production event
//            GENERAL_STORE.resourceCost = new HashMap<>();
//            GENERAL_STORE.cardType = CardType.GREEN_PRODUCTION;
//            GENERAL_STORE.resourceCost.put(EverdellParameters.ResourceTypes.RESIN, 1);
//            GENERAL_STORE.resourceCost.put(EverdellParameters.ResourceTypes.PEBBLE, 1);
//            GENERAL_STORE.points = 1;
//            GENERAL_STORE.applyCardEffect = (state) -> {
//                state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.playerTurn].increment();
//
//                for(var everdellCard : state.playerVillage.get(state.playerTurn).getComponents()){
//                    if(everdellCard.cardDetails == FARM){
//                        state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.playerTurn].increment();
//                        break;
//                    }
//                }
//
//                return GENERAL_STORE;
//            };
//
//            WANDERER.resourceCost = new HashMap<>();
//            WANDERER.cardType = CardType.TAN_TRAVELER;
//            WANDERER.resourceCost.put(EverdellParameters.ResourceTypes.BERRY, 2);
//            WANDERER.points = 1;
//            WANDERER.applyCardEffect = (state) -> {
//                for(int i = 0 ; i<3; i++){
//                    if(state.playerHands.get(state.playerTurn).getSize() == state.playerHands.get(state.playerTurn).getCapacity()){
//                        break;
//                    }
//                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
//                    state.cardCount[state.playerTurn].increment();
//                }
//                return WANDERER;
//            };
//
//            //Can share a space with Husband card. Will be worth 3 points if paired with a husband instead of 2
//            WIFE.resourceCost = new HashMap<>();
//            WIFE.cardType = CardType.PURPLE_PROSPERITY;
//            WIFE.resourceCost.put(EverdellParameters.ResourceTypes.BERRY, 2);
//            WIFE.points = 2;
//            WIFE.applyCardEffect = (state) -> {
//                for(var everdellCard : state.playerVillage.get(state.playerTurn).getComponents()){
//                    if(everdellCard.cardDetails == HUSBAND){
//                        WIFE.points = 3;
//                        break;
//                    }
//                }
//
//                return WIFE;
//            };
//        }
//    }

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
        put(CardDetails.FARM, 10);
        put(CardDetails.RESIN_REFINERY, 1);
        put(CardDetails.GENERAL_STORE, 1);
        put(CardDetails.WANDERER, 1);
        put(CardDetails.WIFE, 4);
        put(CardDetails.HUSBAND, 1);
        put(CardDetails.FAIRGROUNDS, 1);
        put(CardDetails.MINE, 1);
        put(CardDetails.TWIG_BARGE, 1);
        put(CardDetails.SHOP_KEEPER, 1);
        put(CardDetails.BARGE_TOAD, 1);
        put(CardDetails.CASTLE, 1);
        put(CardDetails.KING, 1);
        put(CardDetails.PALACE, 1);
        put(CardDetails.THEATRE, 1);
        put(CardDetails.SCHOOL, 1);
        put(CardDetails.BARD, 5);
        put(CardDetails.RUINS, 1);
        put(CardDetails.WOOD_CARVER, 3);
        put(CardDetails.DOCTOR, 3);
        put(CardDetails.PEDDLER, 3);
        put(CardDetails.LOOKOUT, 3);
        put(CardDetails.QUEEN, 10);
        //put(CardDetails.INN, 10);
        //put(CardDetails.CHIP_SWEEP, 10);
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
