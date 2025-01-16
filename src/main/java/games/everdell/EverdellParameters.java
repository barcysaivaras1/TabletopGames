package games.everdell;

import com.beust.ah.A;
import core.AbstractGameState;
import core.AbstractParameters;
import core.actions.AbstractAction;
import evaluation.optimisation.TunableParameters;
import games.catan.components.CatanCard;
import games.everdell.components.EverdellCard;
import games.everdell.components.EverdellCard.CardDetails;
import games.everdell.components.EverdellLocation;
import games.loveletter.LoveLetterGameState;
import games.loveletter.actions.HandmaidAction;
import games.loveletter.actions.PlayCard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
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

    public interface Locations {
        void applyLocationEffect(EverdellGameState state);
        String name();
    }

    public enum BasicLocations implements Locations{
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

    public enum ForestLocations implements Locations{
        THREE_BERRY,TWO_BERRY_ONE_CARD,TWO_RESIN_ONE_TWIG,THREE_CARDS_ONE_PEBBLE,ONE_TWIG_ONE_RESIN_ONE_BERRY, TWO_ANY, TWO_CARDS_ONE_ANY,
        DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED, DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED,
        DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT, COPY_BASIC_LOCATION_DRAW_CARD;

        public Function<EverdellGameState, ForestLocations> applyLocationEffect;


        //Maybe a bad design decision. Maybe good?
        public static ArrayList<ResourceTypes> resourceChoices;
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

                for(var resources :  resourceChoices){
                    if(resources == ResourceTypes.RESIN){
                        state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment(1);
                    } else if (resources == ResourceTypes.PEBBLE) {
                        state.PlayerResources.get(ResourceTypes.PEBBLE)[state.playerTurn].increment(1);
                    } else if (resources == ResourceTypes.BERRY) {
                        state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment(1);
                    } else if (resources == ResourceTypes.TWIG) {
                        state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(1);
                    }
                    else{
                        System.out.println("Error in Forest Locations, Choices did not match any resource type");
                    }
                }
                return TWO_ANY;
            };
            TWO_CARDS_ONE_ANY.applyLocationEffect = (state) ->{

                for(var resources :  resourceChoices){
                    if(resources == ResourceTypes.RESIN){
                        state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment(1);
                    } else if (resources == ResourceTypes.PEBBLE) {
                        state.PlayerResources.get(ResourceTypes.PEBBLE)[state.playerTurn].increment(1);
                    } else if (resources == ResourceTypes.BERRY) {
                        state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment(1);
                    } else if (resources == ResourceTypes.TWIG) {
                        state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(1);
                    }
                    else{
                        System.out.println("Error in Forest Locations, Choices did not match any resource type");
                    }
                }
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
                for(var resources :  resourceChoices){
                    if(cardChoices.size() == resourceCounter){
                        break;
                    }
                    resourceCounter++;

                    if(resources == ResourceTypes.RESIN){
                        state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment(1);
                    } else if (resources == ResourceTypes.PEBBLE) {
                        state.PlayerResources.get(ResourceTypes.PEBBLE)[state.playerTurn].increment(1);
                    } else if (resources == ResourceTypes.BERRY) {
                        state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment(1);
                    } else if (resources == ResourceTypes.TWIG) {
                        state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(1);
                    }
                    else{
                        System.out.println("Error in Forest Locations, Choices did not match any resource type");
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
            DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT.applyLocationEffect = (state) ->{
                //Draw two cards
                for(var card : cardChoices){
                    try{
                        state.playerHands.get(state.playerTurn).add(card);
                        state.cardCount[state.playerTurn].increment();
                    } catch (Exception e){
                        System.out.println("Error in Forest Locations, Choices did not contain cards");
                    }
                }

                //Play one card at a discount
                for(var card : cardChoices){
                    try{
                        state.playerHands.get(state.playerTurn).remove((EverdellCard) card);
                        state.cardCount[state.playerTurn].increment();
                    } catch (Exception e){
                        System.out.println("Error in Forest Locations, Choices did not contain cards");
                    }
                }
                return DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT;
            };
        }
    }

    public enum BasicEvent implements Locations{
        GREEN_PRODUCTION_EVENT, RED_DESTINATION_EVENT, BLUE_GOVERNANCE_EVENT, TAN_TRAVELER_EVENT;

        public Function<EverdellGameState, BasicEvent> applyLocationEffect;

        @Override
        public void applyLocationEffect(EverdellGameState state) {
            state.pointTokens[state.playerTurn].increment(3);
        }

    }

    public enum CardType {
        TAN_TRAVELER,
        GREEN_PRODUCTION,
        RED_DESTINATION,
        BLUE_GOVERNANCE,
        PURPLE_PROSPERITY;

//        //THIS IS TEMPORARY AS WE HAVE NO CARDS YET
//        public Function<EverdellGameState, CardType> applyCardEffect;
//        public HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost;
//
//        static{
//            //Draw a Card
//            TAN_TRAVELER.resourceCost = new HashMap<>();
//            TAN_TRAVELER.resourceCost.put(EverdellParameters.ResourceTypes.BERRY, 1);
//            TAN_TRAVELER.applyCardEffect = (state) -> {
//                if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
//                    System.out.println("Drawing a card");
//                    state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
//                    System.out.println(state.playerHands.get(state.playerTurn).toString() + " Player hand after using Tan Traveler");
//                    state.cardCount[state.playerTurn].increment();
//                }
//                return TAN_TRAVELER;
//            };
//            //Get one wood
//            GREEN_PRODUCTION.resourceCost = new HashMap<>();
//            GREEN_PRODUCTION.resourceCost.put(EverdellParameters.ResourceTypes.TWIG, 2);
//            GREEN_PRODUCTION.resourceCost.put(EverdellParameters.ResourceTypes.RESIN, 1);
//            GREEN_PRODUCTION.applyCardEffect = (state) -> {
//                state.PlayerResources.get(EverdellParameters.ResourceTypes.TWIG)[state.playerTurn].increment();
//                return GREEN_PRODUCTION;
//            };
//            //Creates a new Location
//            RED_DESTINATION.resourceCost = new HashMap<>();
//            RED_DESTINATION.resourceCost.put(EverdellParameters.ResourceTypes.PEBBLE, 1);
//            RED_DESTINATION.applyCardEffect = (state) -> {
//                EverdellLocation location = new EverdellLocation(EverdellParameters.BasicLocations.ONE_BERRY_RED_DESTINATION, 1);
//                EverdellParameters.BasicLocations.ONE_BERRY_RED_DESTINATION.applyLocationEffect = (gs) -> {
//                    state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[state.playerTurn].increment();
//                    return EverdellParameters.BasicLocations.ONE_BERRY_RED_DESTINATION;
//                };
//                state.Locations.put(EverdellParameters.BasicLocations.ONE_BERRY_RED_DESTINATION, location);
//                //state.resourceLocations.put(location,new EverdellLocation(location, false));
//                System.out.println("Created a new location");
//                System.out.println(state.Locations.toString());
//                return RED_DESTINATION;
//            };
//            //Get one pebble
//            BLUE_GOVERNANCE.resourceCost = new HashMap<>();
//            BLUE_GOVERNANCE.resourceCost.put(EverdellParameters.ResourceTypes.RESIN, 1);
//            BLUE_GOVERNANCE.resourceCost.put(EverdellParameters.ResourceTypes.PEBBLE, 2);
//            BLUE_GOVERNANCE.applyCardEffect = (state) -> {
//                state.PlayerResources.get(EverdellParameters.ResourceTypes.PEBBLE)[state.playerTurn].increment();
//                return BLUE_GOVERNANCE;
//            };
//            //Get one point
//            PURPLE_PROSPERITY.resourceCost = new HashMap<>();
//            PURPLE_PROSPERITY.resourceCost.put(EverdellParameters.ResourceTypes.BERRY, 4);
//            PURPLE_PROSPERITY.applyCardEffect = (state) -> {
//                state.pointTokens[state.playerTurn].increment();
//                return PURPLE_PROSPERITY;
//            };
//        }
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
        put(CardDetails.FARM, 10);
        put(CardDetails.RESIN_REFINERY, 10);
        put(CardDetails.GENERAL_STORE, 10);
        put(CardDetails.WANDERER, 10);
        put(CardDetails.WIFE, 10);
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
