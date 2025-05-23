package games.everdell;

import core.AbstractGameState;
import core.AbstractParameters;
import core.components.Counter;
import evaluation.optimisation.TunableParameters;
import games.everdell.components.*;

import java.awt.*;
import java.util.*;
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

    public interface AbstractLocations{
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
                int playerID = state.getCurrentPlayer();
                state.PlayerResources.get(ResourceTypes.TWIG)[playerID].increment(2);
                if(state.playerHands.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()){
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
            };
            ONE_BERRY.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment(1);
            };
            ONE_BERRY_ONE_CARD.applyLocationEffect = (state) -> {
                int playerID = state.getCurrentPlayer();
                state.PlayerResources.get(ResourceTypes.BERRY)[playerID].increment();
                if(state.playerHands.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()){
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
            };
            ONE_PEBBLE.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.PEBBLE)[state.getCurrentPlayer()].increment(1);
            };
            TWO_CARD_ONE_POINT.applyLocationEffect = (state) -> {
                state.pointTokens[state.getCurrentPlayer()].increment();
                int playerID = state.getCurrentPlayer();
                if(state.playerHands.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()-1){
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                } else if (state.playerHands.get(state.getCurrentPlayer()).getSize() < state.playerHands.get(playerID).getCapacity()){
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                }
                state.cardCount[playerID].increment(2);
            };
            TWO_RESIN.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].increment(2);
            };
            ONE_RESIN_ONE_CARD.applyLocationEffect = (state) -> {
                int playerID = state.getCurrentPlayer();
                state.PlayerResources.get(ResourceTypes.RESIN)[playerID].increment();
                if(state.playerHands.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()){
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                }
                state.cardCount[playerID].increment();
            };
        }
    }

    public enum JourneyLocations implements AbstractLocations{
        JOURNEY_2, JOURNEY_3, JOURNEY_4, JOURNEY_5;


        public Consumer<EverdellGameState> applyLocationEffect;

        //Every Journey Location, Number = Amount of Card Discards AND Points earned
        //Journey_2 = 2 Card Discard and 2 Points
        //All Journey Locations are EXCLUSIVE, EXCEPT for Journey_2 which can hold any number of workers
        //Journey location are ONLY AVAILABLE during Autumn

        //Card Selection will hold the cards that the player wants to discard

        @Override
        public Consumer<EverdellGameState> getLocationEffect(EverdellGameState k) {
            return state -> {
                if(state.currentSeason[state.getCurrentPlayer()] != Seasons.AUTUMN){
                    return;
                }

                //Discard Cards
                int numOfPoints = 0;
                for (EverdellCard card : state.cardSelection) {
                    CardDetails.discardEverdellCard(state, card);
                    numOfPoints++;
                }

                //Gain Points
                state.pointTokens[state.getCurrentPlayer()].increment(numOfPoints);
            };
        }
        public static Boolean defaultCheckIfConditionMet(EverdellGameState state, JourneyLocations journey){
            //Check They meet the card Conditions
            if(state.currentSeason[state.getCurrentPlayer()] != Seasons.AUTUMN){
                return false;
            }


            return switch (journey) {
                case JOURNEY_2 -> {
                    if (state.playerHands.get(state.getCurrentPlayer()).getSize() >= 2) {
                        yield true;
                    }
                    yield false;
                }
                case JOURNEY_3 -> {
                    if (state.playerHands.get(state.getCurrentPlayer()).getSize() >= 3) {
                        yield true;
                    }
                    yield false;
                }
                case JOURNEY_4 -> {
                    if (state.playerHands.get(state.getCurrentPlayer()).getSize() >= 4) {
                        yield true;
                    }
                    yield false;
                }
                case JOURNEY_5 -> {
                    if (state.playerHands.get(state.getCurrentPlayer()).getSize() >= 5) {
                        yield true;
                    }
                    yield false;
                }
            };


        }

        public int cardsNeededToDiscard(){
            return switch (this) {
                case JOURNEY_2 -> 2;
                case JOURNEY_3 -> 3;
                case JOURNEY_4 -> 4;
                case JOURNEY_5 -> 5;
            };
        }

    }

    public enum HavenLocation implements AbstractLocations{
        HAVEN;


        //The haven location has unlimited spots for workers. It can even take workers of the same colour
        //When a worker is placed here, the player may discard as many cards as they like
        //For every 2 cards they discard, they may select 1 of any resource

        //Card Selection will represent which cards they would like to discard
        //Resource selection will represent which resources they have selected

        public Consumer<EverdellGameState> applyLocationEffect;
        @Override
        public Consumer<EverdellGameState> getLocationEffect(EverdellGameState state) {
            return applyLocationEffect;
        }
        static {
            HAVEN.applyLocationEffect = (state) -> {
                //Remove cards from player hand
                for(EverdellCard card : state.cardSelection){
//                    state.discardDeck.add(card);
//                    state.playerHands.get(state.getCurrentPlayer()).remove(card);
//                    state.cardCount[state.getCurrentPlayer()].decrement();
                    CardDetails.discardEverdellCard(state, card);
                }

                int numbOfResources = state.cardSelection.size()/2;
                int counter = 0;

                //Give the player the resources they selected
                for(var resource : state.resourceSelection.keySet()){
                    for(int i =0; i< state.resourceSelection.get(resource).getValue(); i++){
                        state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment();
                        counter++;

                        if(counter == numbOfResources){
                            break;
                        }
                    }
                    if(counter == numbOfResources){
                        break;
                    }
                }
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
                int playerID = state.getCurrentPlayer();
                state.PlayerResources.get(ResourceTypes.BERRY)[playerID].increment(2);
                if(state.playerHands.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()){
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
            };
            TWO_RESIN_ONE_TWIG.applyLocationEffect = (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].increment(2);
                state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].increment(1);
            };
            THREE_CARDS_ONE_PEBBLE.applyLocationEffect = (state) -> {
                int playerID = state.getCurrentPlayer();
                state.PlayerResources.get(ResourceTypes.PEBBLE)[playerID].increment(1);

                for(int i = 0 ; i<3; i++){
                    if(state.playerHands.get(playerID).getSize() == state.playerHands.get(playerID).getCapacity()){
                        break;
                    }
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
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
                int playerID = state.getCurrentPlayer();

                //Add the selected resources
                for(var resources :  state.resourceSelection.keySet()){
                    state.PlayerResources.get(resources)[playerID].increment(state.resourceSelection.get(resources).getValue());
                }

                // Draw two cards
                for(int i = 0 ; i<2; i++){
                    if(state.playerHands.get(playerID).getSize() == state.playerHands.get(playerID).getCapacity()){
                        break;
                    }
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
            };

            DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED.applyLocationEffect = (state) ->{
                int playerID = state.getCurrentPlayer();
                //Discard a card
                for(var card : cardChoices){
                    try{
//                        state.discardDeck.add(card);
//                        state.playerHands.get(playerID).remove((EverdellCard) card);
//                        state.cardCount[playerID].decrement();
                        CardDetails.discardEverdellCard(state, card);
                    } catch (Exception e){
                        System.out.println("Error in Forest Locations, Choices did not contain cards");
                    }
                }


                //Draw two cards
                for(int i = 0 ; i<(cardChoices.size()*2); i++){
                    if(state.playerHands.get(playerID).getSize() == state.playerHands.get(playerID).getCapacity()){
                        break;
                    }
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
            };

            DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED.applyLocationEffect = (state) ->{
                //Discard a card
                System.out.println("IN PARAMETERS DISCARDING CARDS");
                System.out.println("CARDS TO DISCARD == "+cardChoices);
                System.out.println("RESOURCES TO GAIN : "+state.resourceSelection);
                for(var card : cardChoices){
                    try{
//                        state.discardDeck.add(card);
//                        state.playerHands.get(state.getCurrentPlayer()).remove((EverdellCard) card);
//                        state.cardCount[state.getCurrentPlayer()].decrement();
                        CardDetails.discardEverdellCard(state, card);
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
                int playerID = state.getCurrentPlayer();
                //Copy a basic location
                if(basicLocationChoice != null) {
                    System.out.println("Basic Copying  : "+basicLocationChoice);
                    basicLocationChoice.getLocationEffect(state).accept(state);
                }
                System.out.println("IN COPY BASIC LOCATION DRAW CARD");
                //Draw a card
                if(state.playerHands.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()){
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }

                basicLocationChoice = null;
            };


            DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT.applyLocationEffect = (state) ->{
                //Draw two cards
                //Card Choices will hold the card that the player wants to take in their hand
                //Card Selection will hold the card that the player wants to play at a discount
                //Resource Selection will define which resource we want to discount
                //If both card selections cannot be played via discount, then we draw both cards

                //Attempt to place the card in the players hand
                int playerID = state.getCurrentPlayer();

                if(!state.cardSelection.isEmpty()){
                    if(state.cardSelection.get(0).getComponentID() == EverdellParameters.ForestLocations.cardChoices.get(0).getComponentID()){
                        EverdellParameters.ForestLocations.cardChoices.remove(0);
                    }
                }

                if(state.playerHands.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()){
                    System.out.println("Adding card to hand : "+cardChoices.get(0).getName());
                    state.playerHands.get(playerID).add(cardChoices.get(0));
                    state.meadowDeck.remove(cardChoices.get(0));
                    state.meadowDeck.add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
                //If there are no cards in cardSelection,  it means that we could not play the card, we will attempt to draw instead
                if(state.cardSelection.isEmpty()) {
                    System.out.println("DRAW TWO MEADOW, NO PLAYABLE CARDS, DRAWING BOTH");
                    if (state.playerHands.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()) {
                        System.out.println("Adding card to hand : " + cardChoices.get(1).getName());
                        state.playerHands.get(playerID).add(cardChoices.get(1));
                        state.meadowDeck.remove(cardChoices.get(1));
                        state.meadowDeck.add(state.cardDeck.draw());
                        state.cardCount[playerID].increment();
                    }
                    return;
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

                    state.PlayerResources.get(resource)[playerID].decrement(finalCost);
                }
                state.cardSelection.get(0).payForCard();
                //state.temporaryDeck.add(state.cardSelection.get(0));
                cardChoices.clear();
            };
        }
    }

    public enum BasicEvent implements AbstractLocations{
        GREEN_PRODUCTION_EVENT, RED_DESTINATION_EVENT, BLUE_GOVERNANCE_EVENT, TAN_TRAVELER_EVENT;

        public Consumer<EverdellGameState> applyLocationEffect;

        @Override
        public Consumer<EverdellGameState> getLocationEffect(EverdellGameState k) {
            return state -> state.pointTokens[state.getCurrentPlayer()].increment(3);
        }

        public static Boolean defaultCheckIfConditionMet(EverdellGameState state, BasicEvent event){
            //Check They meet the card Conditions
            int target;
            int counter;
            switch (event){
                case GREEN_PRODUCTION_EVENT:
                    target = 4;
                    counter = 0;
                    for(var card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){
                        if (card.getCardType() == CardType.GREEN_PRODUCTION){
                            counter++;
                        }
                    }
                    if(target <= counter){

                        return true;
                    }
                    return false;
                case BLUE_GOVERNANCE_EVENT:
                    target = 3;
                    counter = 0;
                    for(var card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){
                        if (card.getCardType() == CardType.BLUE_GOVERNANCE){
                            counter++;
                        }
                    }
                    if(target <= counter){
                        return true;
                    }
                    return false;
                case RED_DESTINATION_EVENT:
                    target = 3;
                    counter = 0;
                    for(var card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){
                        if (card.getCardType() == CardType.RED_DESTINATION){
                            counter++;
                        }
                    }
                    if(target <= counter){
                        return true;
                    }
                    return false;
                case TAN_TRAVELER_EVENT:
                    target = 3;
                    counter = 0;
                    for(var card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){
                        if (card.getCardType() == CardType.TAN_TRAVELER){
                            counter++;
                        }
                    }
                    if(target <= counter){
                        return true;
                    }
                    return false;
            }


            return false;

        }



    }

    public enum SpecialEvent implements AbstractLocations{
        THE_EVERDELL_GAMES, CROAK_WARE_CURE, TAX_RELIEF, FLYING_DOCTOR_SERVICE, CAPTURE_OF_THE_ACORN_THIEVES, GRADUATION_OF_SCHOLARS,
        AN_EVENING_OF_FIREWORKS, PERFORMER_IN_RESIDENCE, ANCIENT_SCROLLS_DISCOVERED, A_BRILLIANT_MARKETING_PLAN, UNDER_NEW_MANAGEMENT,
        PRISTINE_CHAPEL_CEILING, PATH_OF_THE_PILGRIMS, MINISTERING_TO_MISCREANTS, REMEMBERING_THE_FALLEN, A_WELL_RUN_CITY;

        //Applicable to All Events
        public Consumer<EverdellGameState> applyLocationEffect;
        public Function<EverdellGameState, Boolean> checkIfConditionMet;

        //Specific to some Events
        public EverdellLocation selectedLocation;
        public HashMap<Integer, HashMap<ResourceTypes, Counter>> playersToGiveResources;


        public static Boolean defaultCheckIfConditionMet(EverdellGameState state, SpecialEvent event){
            //Check They meet the card Conditions
            boolean hasCards = true;
            for(CardDetails card : event.eventConditions){
                boolean hasCard = false;
                for(EverdellCard everdellCard : state.playerVillage.get(state.getCurrentPlayer())){
                    if(everdellCard.getCardEnumValue() == card){
                        hasCard = true;
                        break;
                    }
                }
                if(!hasCard){
                    hasCards = false;
                    break;
                }
            }
            //Needs to Return hasCards, currently true for testing
            return hasCards;
        }

        public ArrayList<CardDetails> eventConditions;

        @Override
        public Consumer<EverdellGameState> getLocationEffect(EverdellGameState state) {
            return applyLocationEffect;
        }

        static {
            THE_EVERDELL_GAMES.eventConditions = new ArrayList<>();
            THE_EVERDELL_GAMES.checkIfConditionMet = (state) -> {
                //They must have 2 of each type of card in their city
                //This will give them 9 points upon claiming
                int TAN_TRAVELLER_Counter = 0;
                int GREEN_PRODUCTION_Counter = 0;
                int RED_DESTINATION_Counter = 0;
                int BLUE_GOVERNANCE_Counter = 0;
                int PURPLE_PROSPERITY_Counter = 0;


                for (EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())) {
                    CardType type = card.getCardType();
                    if (type == CardType.TAN_TRAVELER) {
                        TAN_TRAVELLER_Counter++;
                    } else if (type == CardType.GREEN_PRODUCTION) {
                        GREEN_PRODUCTION_Counter++;
                    } else if (type == CardType.RED_DESTINATION) {
                        RED_DESTINATION_Counter++;
                    } else if (type == CardType.BLUE_GOVERNANCE) {
                        BLUE_GOVERNANCE_Counter++;
                    } else if (type == CardType.PURPLE_PROSPERITY) {
                        PURPLE_PROSPERITY_Counter++;
                    }
                }
                if (TAN_TRAVELLER_Counter >= 2 && GREEN_PRODUCTION_Counter >= 2 && RED_DESTINATION_Counter >= 2 && BLUE_GOVERNANCE_Counter >= 2 && PURPLE_PROSPERITY_Counter >= 2) {
                    return true;
                }
                //SHOULD BE FALSE, TRUE FOR TESTING
                return false;

            };
            THE_EVERDELL_GAMES.applyLocationEffect = (state) -> {
                //This will give them 9 points upon claiming
                if (THE_EVERDELL_GAMES.checkIfConditionMet.apply(state)) {
                    state.pointTokens[state.getCurrentPlayer()].increment(9);
                }

            };
            CROAK_WARE_CURE.eventConditions = new ArrayList<>(List.of(CardDetails.UNDERTAKER, CardDetails.BARGE_TOAD));
            CROAK_WARE_CURE.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, CROAK_WARE_CURE);
            };
            CROAK_WARE_CURE.applyLocationEffect = (state) -> {
                //If the player has the cards in the eventConditions, they may activate this card
                //Upon activation, the player loses 2 berries and discards 2 cards from the village
                //When Discard the cards from the village, we must call the onRemove function
                //After the effect, they gain 6 points
                //Card Selection will hold the cards that the player wants to discard

                //Check They meet the card Conditions
                if (!CROAK_WARE_CURE.checkIfConditionMet.apply(state)) {
                    return;
                }

                //Decrement Berries
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].decrement(2);

                //Discard Cards
                for (EverdellCard card : state.cardSelection) {
//                    state.discardDeck.add(card);
//                    state.playerVillage.get(state.getCurrentPlayer()).remove(card);
//                    card.removeCardEffect(state);
                    CardDetails.discardEverdellCard(state, card);
                }


                //Gain Points
                state.pointTokens[state.getCurrentPlayer()].increment(6);
            };


            TAX_RELIEF.eventConditions = new ArrayList<>(List.of(CardDetails.JUDGE, CardDetails.QUEEN));
            TAX_RELIEF.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, TAX_RELIEF);
            };
            TAX_RELIEF.applyLocationEffect = (state) -> {
                //This will trigger the green Production Event.
                //This occurs fully outside of this lambda function
            };

            FLYING_DOCTOR_SERVICE.eventConditions = new ArrayList<>(List.of(CardDetails.DOCTOR, CardDetails.POSTAL_PIGEON));
            FLYING_DOCTOR_SERVICE.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, FLYING_DOCTOR_SERVICE);
            };
            FLYING_DOCTOR_SERVICE.applyLocationEffect = (state) -> {
                //Gain 3 points for each Husband/Wife PAIR in the city

                int points = 0;

                //Find Every wife card, check if the wife has a husband
                //If a wife has a husband, then this means it is a pair
                for (EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == CardDetails.WIFE) {
                        WifeCard wc = (WifeCard) card;
                        if(wc.getHusband() != -1){
                            points += 3;
                        }
                    }
                }
                state.pointTokens[state.getCurrentPlayer()].increment(points);
            };


            CAPTURE_OF_THE_ACORN_THIEVES.eventConditions = new ArrayList<>(List.of(CardDetails.RANGER, CardDetails.COURTHOUSE));
            CAPTURE_OF_THE_ACORN_THIEVES.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, CAPTURE_OF_THE_ACORN_THIEVES);
            };
            CAPTURE_OF_THE_ACORN_THIEVES.applyLocationEffect = (state) -> {
                //Place up to 2 critters on this Event from the CITY, aka removing the cards from the city
                //For each critter placed on this event, gain 3 points
                //Card Selection will hold the cards that the player wants to place on this event

                int points = 0;
                for (EverdellCard card : state.cardSelection) {
                    state.playerVillage.get(state.getCurrentPlayer()).remove(card);
                    points += 3;
                }
                state.pointTokens[state.getCurrentPlayer()].increment(points);
            };

            GRADUATION_OF_SCHOLARS.eventConditions = new ArrayList<>(List.of(CardDetails.TEACHER, CardDetails.UNIVERSITY));
            GRADUATION_OF_SCHOLARS.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, GRADUATION_OF_SCHOLARS);
            };
            GRADUATION_OF_SCHOLARS.applyLocationEffect = (state) -> {
                //Place up to 3 critters on this Event from your HAND
                //For each critter placed on this event, gain 2 points
                //Card Selection will hold the cards that the player wants to place on this event

                int points = 0;
                for (EverdellCard card : state.cardSelection) {
                    state.playerHands.get(state.getCurrentPlayer()).remove(card);
                    points += 2;
                }
                state.pointTokens[state.getCurrentPlayer()].increment(points);
            };

            AN_EVENING_OF_FIREWORKS.eventConditions = new ArrayList<>(List.of(CardDetails.LOOKOUT, CardDetails.MINER_MOLE));
            AN_EVENING_OF_FIREWORKS.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, AN_EVENING_OF_FIREWORKS);
            };
            AN_EVENING_OF_FIREWORKS.applyLocationEffect = (state) -> {
                //Place up to 3 wood on this event
                //Gain 2 points on this event for each wood
                //Resource Selection will hold the wood they choose to give up

                int twigsGiven = Math.min(state.resourceSelection.get(ResourceTypes.TWIG).getValue(), 3);

                state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].decrement(twigsGiven);
                state.pointTokens[state.getCurrentPlayer()].increment(twigsGiven*2);
            };

            PERFORMER_IN_RESIDENCE.eventConditions = new ArrayList<>(List.of(CardDetails.INN, CardDetails.BARD));
            PERFORMER_IN_RESIDENCE.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, PERFORMER_IN_RESIDENCE);
            };
            PERFORMER_IN_RESIDENCE.applyLocationEffect = (state) -> {
                //Place up to 3 Berries on this event
                //Gain 2 points on this event for each berry
                //Resource Selection will hold the berry they choose to give up

                int berriesGiven = Math.min(state.resourceSelection.get(ResourceTypes.BERRY).getValue(), 3);

                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].decrement(berriesGiven);
                state.pointTokens[state.getCurrentPlayer()].increment(berriesGiven*2);
            };

            ANCIENT_SCROLLS_DISCOVERED.eventConditions = new ArrayList<>(List.of(CardDetails.HISTORIAN, CardDetails.RUINS));
            ANCIENT_SCROLLS_DISCOVERED.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, ANCIENT_SCROLLS_DISCOVERED);
            };
            ANCIENT_SCROLLS_DISCOVERED.applyLocationEffect = (state) -> {
                int playerID = state.getCurrentPlayer();
                //5 Cards are Revealed From the deck to the player
                //The player may select however many to draw into their hand and however many to place under the event
                //Each card that is placed under the event, will give 1 point

                //Card Selection represents the cards they want to draw

                //Draw the cards
                int points = 5;
                for (EverdellCard card : state.cardSelection) {
                    if(state.playerHands.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()){
                        state.playerHands.get(playerID).add(card);
                        state.cardCount[playerID].increment();
                        points--;
                    }
                }
                //Add Points
                state.pointTokens[state.getCurrentPlayer()].increment(Math.max(points,0));
            };
            UNDER_NEW_MANAGEMENT.eventConditions = new ArrayList<>(List.of(CardDetails.PEDDLER, CardDetails.GENERAL_STORE));
            UNDER_NEW_MANAGEMENT.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, UNDER_NEW_MANAGEMENT);
            };
            UNDER_NEW_MANAGEMENT.applyLocationEffect = (state) -> {
                //The player can place up to 3 of any resource on this event
                // Twigs and Berries are worth 1 point each,
                // Resin and Pebbles are worth 2 points each

                //Resource Selection will represent the resources the player wants to give up
                int points = 0;
                int totalResources = 0;
                for(var resource : state.resourceSelection.keySet()){
                    for(int i = 0; i< state.resourceSelection.get(resource).getValue(); i++){
                        if(totalResources == 3){
                            break;
                        }
                        if(resource == ResourceTypes.TWIG || resource == ResourceTypes.BERRY){
                            points += 1;
                        } else {
                            points += 2;
                        }
                        totalResources++;
                        state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement();
                    }
                    if(totalResources == 3){
                        break;
                    }
                }
                state.pointTokens[state.getCurrentPlayer()].increment(points);
            };

            PRISTINE_CHAPEL_CEILING.eventConditions = new ArrayList<>(List.of(CardDetails.CHAPEL, CardDetails.WOOD_CARVER));
            PRISTINE_CHAPEL_CEILING.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, PRISTINE_CHAPEL_CEILING);
            };
            PRISTINE_CHAPEL_CEILING.applyLocationEffect = (state) -> {
                //Draw 1 card and Gain 1 of any resource for each point on the chapel
                //Resource Selection will dictate which resource they want to gain
                int playerID = state.getCurrentPlayer();

                int numberOfPointsPlaced = 0;

                int pointsChapelCardsStartWith = 2;
                //Find Chapel Card
                for(EverdellCard card : state.playerVillage.get(playerID)){
                    if(card.getCardEnumValue() == CardDetails.CHAPEL){
                        numberOfPointsPlaced = card.getPoints()-pointsChapelCardsStartWith;
                        break;
                    }
                }

                //Gain Resources
                int counter = numberOfPointsPlaced;
                for(var resource : state.resourceSelection.keySet()){
                    for(int i = 0; i<state.resourceSelection.get(resource).getValue(); i++){
                        if (counter == 0){
                            break;
                        }
                        state.PlayerResources.get(resource)[playerID].increment();
                        counter--;
                    }
                    if(counter == 0){
                        break;
                    }
                }
                //Draw Cards
                for (int i = 0; i < numberOfPointsPlaced; i++) {
                    if (state.playerHands.get(playerID).getSize() == state.playerHands.get(playerID).getCapacity()) {
                        break;
                    }
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
            };
            PATH_OF_THE_PILGRIMS.eventConditions = new ArrayList<>(List.of(CardDetails.WANDERER, CardDetails.MONASTERY));
            PATH_OF_THE_PILGRIMS.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, PATH_OF_THE_PILGRIMS);
            };
            PATH_OF_THE_PILGRIMS.applyLocationEffect = (state) -> {
                //Gain 3 points for each worker on the Monastery
                //Find MonasteryCard
                for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
                    if(card.getCardEnumValue() == CardDetails.MONASTERY){
                        MonasteryCard mc = (MonasteryCard) card;
                        EverdellLocation location = mc.getLocation(state);
                        state.pointTokens[state.getCurrentPlayer()].increment( 3*location.getPlayersOnLocation().size());
                    }
                }

            };
            REMEMBERING_THE_FALLEN.eventConditions = new ArrayList<>(List.of(CardDetails.CEMETERY, CardDetails.SHEPHERD));
            REMEMBERING_THE_FALLEN.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, REMEMBERING_THE_FALLEN);
            };
            REMEMBERING_THE_FALLEN.applyLocationEffect = (state) -> {
                //Gain 3 points for each worker on the Cemetery
                //Find Cemetery Card
                for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
                    if(card.getCardEnumValue() == CardDetails.CEMETERY){
                        CemeteryCard cc = (CemeteryCard) card;
                        state.pointTokens[state.getCurrentPlayer()].increment( 3*cc.getLocation(state).getPlayersOnLocation().size());
                    }
                }

            };
            MINISTERING_TO_MISCREANTS.eventConditions = new ArrayList<>(List.of(CardDetails.MONK, CardDetails.DUNGEON));
            MINISTERING_TO_MISCREANTS.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, PATH_OF_THE_PILGRIMS);
            };
            MINISTERING_TO_MISCREANTS.applyLocationEffect = (state) -> {
                //Gain 3 points for each Critter on the Dungeon
                //Find Dungeon Card
                for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
                    if(card.getCardEnumValue() == CardDetails.DUNGEON){
                        DungeonCard dc = (DungeonCard) card;
                        int crittersJailed = 0;
                        if(dc.cell1ID != -1){
                            crittersJailed++;
                        }
                        if(dc.cell2ID != -1){
                            crittersJailed++;
                        }
                        state.pointTokens[state.getCurrentPlayer()].increment( 3*crittersJailed);
                    }
                }
            };
            A_WELL_RUN_CITY.eventConditions = new ArrayList<>(List.of(CardDetails.CHIP_SWEEP, CardDetails.CLOCK_TOWER));
            A_WELL_RUN_CITY.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, A_WELL_RUN_CITY);
            };
            A_WELL_RUN_CITY.applyLocationEffect = (state) -> {
                //Bring Back A Deployed Worker
                A_WELL_RUN_CITY.selectedLocation.removePlayerFromLocation(state.getCurrentPlayer());
                state.workers[state.getCurrentPlayer()].increment();

                //Give player 4 points
                state.pointTokens[state.getCurrentPlayer()].increment(4);
            };

            A_BRILLIANT_MARKETING_PLAN.eventConditions = new ArrayList<>(List.of(CardDetails.POST_OFFICE, CardDetails.SHOP_KEEPER));
            A_BRILLIANT_MARKETING_PLAN.checkIfConditionMet = (state) -> {
                //Check They meet the card Conditions
                return defaultCheckIfConditionMet(state, A_BRILLIANT_MARKETING_PLAN);
            };
            A_BRILLIANT_MARKETING_PLAN.applyLocationEffect = (state) -> {
                //The player can give up to 3 resources to opponents.
                //For each resource given, the player gains 2 points

                //playersToGiveResources will hold which resources and how many are given to what players
                //This will have to be filled in at the GUI/AI Level
                int maxResourcesToGiveOut = 3;
                int counter = 0;
                //Give out Resources
                for(int player : A_BRILLIANT_MARKETING_PLAN.playersToGiveResources.keySet()){
                    HashMap<ResourceTypes, Counter> resourcesToGive = A_BRILLIANT_MARKETING_PLAN.playersToGiveResources.get(player);
                    for(var resource : resourcesToGive.keySet()){
                        for(int i = 0; i<resourcesToGive.get(resource).getValue(); i++){
                            if (counter == maxResourcesToGiveOut){
                                break;
                            }
                            state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement();
                            state.PlayerResources.get(resource)[player].increment();
                            state.pointTokens[state.getCurrentPlayer()].increment(2);
                            counter++;
                        }
                        if (counter == maxResourcesToGiveOut){
                            break;
                        }
                    }
                    if (counter == maxResourcesToGiveOut){
                        break;
                    }
                }

            };

        }
    }

    public enum RedDestinationLocation implements AbstractLocations{
        LOOKOUT_DESTINATION, QUEEN_DESTINATION, INN_DESTINATION, POST_OFFICE_DESTINATION,
        MONASTERY_DESTINATION, CEMETERY_DESTINATION, UNIVERSITY_DESTINATION, CHAPEL_DESTINATION, STORE_HOUSE_DESTINATION;

        //Cards that do not have a Location Effect here, have it created in their own card class

        public static AbstractLocations copyLocationChoice;
        public Consumer<EverdellGameState> applyLocationEffect;

        @Override
        public Consumer<EverdellGameState> getLocationEffect(EverdellGameState state) {
            return applyLocationEffect;
        }

        static{
            LOOKOUT_DESTINATION.applyLocationEffect = (state) -> {
                if(copyLocationChoice != null){
                    System.out.println("LOOKOUT DESTINATION ACTION BEING PERFORMED");
                    copyLocationChoice.getLocationEffect(state).accept(state);
                }
            };
            QUEEN_DESTINATION.applyLocationEffect = (state) -> {
                state.cardSelection.get(0).payForCard();
            };

            UNIVERSITY_DESTINATION.applyLocationEffect = (state) -> {
                //Allows the player to select a card to discard from the city
                //The cost of the card will be refunded and the university will generate 1 of any resource and 1 point

                //CardSelection[0] will represent the card that they want to discard
                //ResourceSelection will hold the resource that they want to gain

                if(!state.cardSelection.isEmpty()) {
                    //Discard the card
//                    state.discardDeck.add(state.cardSelection.get(0));
//                    state.playerVillage.get(state.getCurrentPlayer()).remove(state.cardSelection.get(0));
//                    state.cardSelection.get(0).removeCardEffect(state);
                    CardDetails.discardEverdellCard(state, state.cardSelection.get(0));

                    //Refund the cost of the card
                    for (var resource : state.cardSelection.get(0).getResourceCost().keySet()) {
                        state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment(state.cardSelection.get(0).getResourceCost().get(resource));
                    }

                    //Gain the resource
                    for (var resource : state.resourceSelection.keySet()) {
                        if (state.resourceSelection.get(resource).getValue() > 0) {
                            state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment();
                            break;
                        }
                    }

                    //Gain a point
                    state.pointTokens[state.getCurrentPlayer()].increment();
                }
            };
            CHAPEL_DESTINATION.applyLocationEffect = (state) -> {
                int playerID = state.getCurrentPlayer();
                //Places 1 point on the Chapel card
                //Makes the player draw 2 cards for every point that is on the chapel

                //Place a point on the Chapel card
                EverdellCard chapelCard = null;
                for(var card : state.playerVillage.get(playerID)){
                    if(card.getCardEnumValue() == CardDetails.CHAPEL){
                        chapelCard = card;
                        chapelCard.setCardPoints(chapelCard.getPoints()+1);
                        break;
                    }
                }

                //Draw 2 Cards for every point PLACED on the chapel card
                for (int i = 0; i < (chapelCard.getPoints()-2)*2; i++) {
                    if (state.playerHands.get(playerID).getSize() == state.playerHands.get(playerID).getCapacity()) {
                        break;
                    }
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
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
        COURTHOUSE, CRANE, INNKEEPER, UNIVERSITY, CHAPEL, SHEPHERD, CLOCK_TOWER, RANGER, DUNGEON, MINER_MOLE, EVER_TREE, STORE_HOUSE;

        public Function<EverdellGameState, EverdellCard> createEverdellCard;

        public static void discardEverdellCard(EverdellGameState state, EverdellCard card){
            //This will be called when the card is discarded
            //The card will be removed from the player's hand, meadow or village
            //It will make a new card that is of the same type and add it to the discard pile
            //This is so that any values are reset
            EverdellCard newCard = card.getCardEnumValue().createEverdellCard.apply(state);
            state.discardDeck.add(newCard);

            if(state.playerHands.get(state.getCurrentPlayer()).contains(card)){
                state.playerHands.get(state.getCurrentPlayer()).remove(card);
                state.cardCount[state.getCurrentPlayer()].decrement();
            }
            else if(state.meadowDeck.contains(card)){
                state.playerHands.get(state.getCurrentPlayer()).remove(card);
                state.cardCount[state.getCurrentPlayer()].decrement();
            }
            else if(state.playerVillage.get(state.getCurrentPlayer()).contains(card)) {
                System.out.println("Card is being removed from the village");
                card.removeCardEffect(state);
                state.playerVillage.get(state.getCurrentPlayer()).remove(card);
            }

            if(state.temporaryDeck.contains(card)) {
                state.temporaryDeck.remove(card);
            }
        }

        static {
            FARM.createEverdellCard = (gamestate) -> new ConstructionCard("Farm", FARM, CardType.GREEN_PRODUCTION, true, false, 1,
                    new HashMap<>() {{
                        put(ResourceTypes.TWIG, 2); //2
                        put(ResourceTypes.RESIN, 1); //1
                    }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment();
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(WIFE, HUSBAND)));


            RESIN_REFINERY.createEverdellCard = (gameState) -> new ConstructionCard("Resin Refinery", RESIN_REFINERY, CardType.GREEN_PRODUCTION, true, false, 1, new HashMap<>() {{
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.RESIN)[state.getCurrentPlayer()].increment();
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(CHIP_SWEEP)));

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
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(SHOP_KEEPER)));

            //Wanderer also has a special feature that it does not take up a village slot
            WANDERER.createEverdellCard = (gameState) -> new CritterCard("Wanderer", WANDERER, CardType.TAN_TRAVELER, false, false, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                int playerID = state.getCurrentPlayer();
                for (int i = 0; i < 3; i++) {
                    if (state.playerHands.get(playerID).getSize() == state.playerHands.get(playerID).getCapacity()) {
                        break;
                    }
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
                state.villageMaxSize[playerID].increment();
                return true;
            }, (everdellGameState -> {
                everdellGameState.villageMaxSize[everdellGameState.getCurrentPlayer()].decrement();
            }));

            WIFE.createEverdellCard = (gameState) -> new WifeCard("Wife", WIFE, CardType.PURPLE_PROSPERITY, false, false, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
            }));

            HUSBAND.createEverdellCard = (gameState) -> new HusbandCard("Husband", HUSBAND, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 3);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
            }));

            FAIRGROUNDS.createEverdellCard = (gameState) -> new ConstructionCard("Fairgrounds", FAIRGROUNDS, CardType.GREEN_PRODUCTION, true, true, 3, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 2);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                int playerID = state.getCurrentPlayer();
                for (int i = 0; i < 2; i++) {
                    if (state.playerHands.get(playerID).getSize() == state.playerHands.get(playerID).getCapacity()) {
                        break;
                    }
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(FOOL)));

            MINE.createEverdellCard = (gameState) -> new ConstructionCard("Mine", MINE, CardType.GREEN_PRODUCTION, true, false, 2, new HashMap<>() {{
                put(ResourceTypes.PEBBLE, 1);
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 1);
            }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.PEBBLE)[state.getCurrentPlayer()].increment(1);
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(MINER_MOLE)));

            TWIG_BARGE.createEverdellCard = (gameState) -> new ConstructionCard("Twig Barge", TWIG_BARGE, CardType.GREEN_PRODUCTION, true, false, 1, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].increment(2);
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(BARGE_TOAD)));

            BARGE_TOAD.createEverdellCard = (gameState) -> new CritterCard("Barge Toad", BARGE_TOAD, CardType.GREEN_PRODUCTION, false, false, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == FARM) {
                        state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].increment(2);
                    }
                }
                return true;
            }, (everdellGameState -> {
            }));


            SHOP_KEEPER.createEverdellCard = (gameState) -> new CritterCard("Shop Keeper", SHOP_KEEPER, CardType.BLUE_GOVERNANCE, false, true, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                //Check if it is a critter
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment(1);
                return true;
            }, (everdellGameState -> {
            }));

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
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(KING)));

            //NOT FULLY IMPLEMENTED BECAUSE IT REQUIRES SPECIAL EVENTS ASWELL
            KING.createEverdellCard = (gameState) -> new CritterCard("King", KING, CardType.PURPLE_PROSPERITY, false, true, 4, new HashMap<>() {{
                put(ResourceTypes.BERRY, 6);
            }}, (state) -> {
                int counter = 0;

                for (var loc : state.everdellLocations) {
                    if (loc.getAbstractLocation() instanceof BasicEvent && loc.isPlayerOnLocation(state.getCurrentPlayer())) {
                        counter++;
                    }
                }
                for (var loc : state.everdellLocations) {
                    if (loc.getAbstractLocation() instanceof SpecialEvent && loc.isPlayerOnLocation(state.getCurrentPlayer())) {
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
            }, (everdellGameState -> {
            }));


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
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(QUEEN)));


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
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(BARD)));

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
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(TEACHER)));

            BARD.createEverdellCard = (gameState) -> new CritterCard("Bard", BARD, CardType.TAN_TRAVELER, false, true, 0, new HashMap<>() {{
                put(ResourceTypes.BERRY, 3);
            }}, (state) -> {
                int counter = 0;

                System.out.println("Bard : Card Selection  : "+state.cardSelection);
                //Discard up to 5 cards, Give 1 point for each card discarded
                for (var card : state.cardSelection) {
                    try {
//                        state.discardDeck.add(card);
//                        state.playerHands.get(state.getCurrentPlayer()).remove((EverdellCard) card);
//                        state.cardCount[state.getCurrentPlayer()].decrement();
                        discardEverdellCard(state, card);
                        counter++;
                    } catch (Exception e) {
                        System.out.println("Error in Bard, Choices did not contain cards");
                    }
                }
                state.pointTokens[state.getCurrentPlayer()].increment(counter);
                return true;
            }, (everdellGameState -> {
            }));

            //There can definitely be a problem where if this card is placed when their village is at max capacity.
            //This is because the Ruin card will not be placed in the village, but the player will still be able to select a card to remove
            //Ruins can also currently select itself which probably should not be allowed
            //Look into this when you have revised the card system.
            RUINS.createEverdellCard = (gameState) -> new ConstructionCard("Ruins", RUINS, CardType.TAN_TRAVELER, true, false, 0, new HashMap<>() {{
            }}, (state) -> {
                //A card must be selected to be removed from the village
                //This uses CardSelection from the gameState to determine which card to remove
                int playerID = state.getCurrentPlayer();

                if (!state.cardSelection.isEmpty()) {
                    //Remove the selected card from the village
                    System.out.println("Removing card from village");
                    System.out.println(state.cardSelection.get(0));

//                    state.discardDeck.add(state.cardSelection.get(0));
//                    state.cardSelection.get(0).removeCardEffect(state);
//                    state.playerVillage.get(state.getCurrentPlayer()).remove(state.cardSelection.get(0));
                    discardEverdellCard(state, state.cardSelection.get(0));


                    //Refund the Resources
                    for (var resource : state.cardSelection.get(0).getResourceCost().keySet()) {
                        state.PlayerResources.get(resource)[playerID].increment(state.cardSelection.get(0).getResourceCost().get(resource));
                    }
                }
                //Draw 2 Cards
                for (int i = 0; i < 2; i++) {
                    if (state.playerHands.get(playerID).getSize() == state.playerHands.get(playerID).getCapacity()) {
                        break;
                    }
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }

                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(PEDDLER)));


            WOOD_CARVER.createEverdellCard = (gameState) -> new CritterCard("Wood Carver", WOOD_CARVER, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                if (!state.resourceSelection.isEmpty()) {
                    //Increment Points based on how much wood was given
                    //It can take a max of 3 wood
                    System.out.println("Wood Carver : Twig Count : "+state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].getValue());
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
                System.out.println("Wood Carver : Twig Count : "+state.PlayerResources.get(ResourceTypes.TWIG)[state.getCurrentPlayer()].getValue());

                return true;
            }, (everdellGameState -> {
            }));

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
            }, (everdellGameState -> {
            }));

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
            }, (everdellGameState -> {
            }));

            PEDDLER.createEverdellCard = (gameState) -> new PeddlerCard("Peddler", PEDDLER, CardType.GREEN_PRODUCTION, false, false, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                //Peddler, you can pay up to 2 of any resource and you can get 1 any for each resource paid
                //Its effect exists within PeddlerCard
                return false;
            }, (everdellGameState -> {
            }));

            CHIP_SWEEP.createEverdellCard = (gameState) -> new CopyCard("Chip Sweep", CHIP_SWEEP, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 3);
            }}, (state) -> {
                //Chip Sweep takes in a production card and copies its effect.
                //The player can select which production card to copy

                return true;
            }, (everdellGameState -> {
            }));

            LOOKOUT.createEverdellCard = (gameState) -> new ConstructionCard(RedDestinationLocation.LOOKOUT_DESTINATION, "Lookout", LOOKOUT, CardType.RED_DESTINATION, true, true, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
                //Remove Effect

                //Find Lookout Card in players village
                for (var card : everdellGameState.playerVillage.get(everdellGameState.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == LOOKOUT) {
                        ConstructionCard cc = (ConstructionCard) card;
                        everdellGameState.everdellLocations.remove(cc.getLocation(everdellGameState));
                    }
                }
            }), new ArrayList<>(List.of(WANDERER)));

            QUEEN.createEverdellCard = (gameState) -> new CritterCard(RedDestinationLocation.QUEEN_DESTINATION, "Queen", QUEEN, CardType.RED_DESTINATION, false, true, 4, new HashMap<>() {{
                put(ResourceTypes.BERRY, 5);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
                //Remove Effect

                //Find Queen Card in players village
                for (var card : everdellGameState.playerVillage.get(everdellGameState.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == QUEEN) {
                        CritterCard cc = (CritterCard) card;
                        everdellGameState.everdellLocations.remove(cc.getLocation(everdellGameState));
                    }
                }
            }));

            INN.createEverdellCard = (gameState) -> new InnCard(RedDestinationLocation.INN_DESTINATION, "Inn", INN, CardType.RED_DESTINATION, true, false, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 2);
                put(ResourceTypes.RESIN, 1);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(INNKEEPER)));

            POST_OFFICE.createEverdellCard = (gameState) -> new PostOfficeCard(RedDestinationLocation.POST_OFFICE_DESTINATION, "Post_Office", POST_OFFICE, CardType.RED_DESTINATION, true, false, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 2);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(POSTAL_PIGEON)));

            MONK.createEverdellCard = (gameState) -> new MonkCard("Monk", MONK, CardType.GREEN_PRODUCTION, false, true, 0, new HashMap<>() {{
                put(ResourceTypes.BERRY, 1);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
            }));

            FOOL.createEverdellCard = (gameState) -> new FoolCard("Fool", FOOL, CardType.TAN_TRAVELER, false, true, -2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 3);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
            }));

            TEACHER.createEverdellCard = (gameState) -> new TeacherCard("Teacher", TEACHER, CardType.GREEN_PRODUCTION, false, false, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
            }));

            MONASTERY.createEverdellCard = (gameState) -> new MonasteryCard(RedDestinationLocation.MONASTERY_DESTINATION, "Monastery", MONASTERY, CardType.RED_DESTINATION, true, true, 1, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(MONK)));

            HISTORIAN.createEverdellCard = (gameState) -> new CritterCard("Historian", HISTORIAN, CardType.BLUE_GOVERNANCE, false, true, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                int playerID = state.getCurrentPlayer();
                if (state.currentCard.getCardEnumValue() == HISTORIAN) {
                    return true;
                }
                //Makes you draw 1 card whenever a construction or critter card is played
                if (state.playerVillage.get(playerID).getSize() < state.playerHands.get(playerID).getCapacity()) {
                    state.playerHands.get(playerID).add(state.cardDeck.draw());
                    state.cardCount[playerID].increment();
                }
                return true;
            }, (everdellGameState -> {
            }));

            CEMETERY.createEverdellCard = (gameState) -> new CemeteryCard(RedDestinationLocation.CEMETERY_DESTINATION, "Cemetery", CEMETERY, CardType.RED_DESTINATION, true, true, 0, new HashMap<>() {{
                put(ResourceTypes.PEBBLE, 2);
            }}, (state) -> {
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(UNDERTAKER)));

            UNDERTAKER.createEverdellCard = (gameState) -> new CritterCard("Undertaker", UNDERTAKER, CardType.TAN_TRAVELER, false, true, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                //Undertaker allows the player to remove 3 cards from the meadow,
                //Replenish those cards, and then allow the player to draw 1 card from the meadow

                //CardSelection[0] will hold the card the player selects from the meadow

                //Unlock the second Location of the cemetary card (if it exists within the village)
                state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof CemeteryCard).forEach(c -> {
                    CemeteryCard cc = (CemeteryCard) c;
                    cc.unlockSecondLocation(state);
                });

                if (state.playerHands.get(state.getCurrentPlayer()).getSize() < 8) {
                    state.playerHands.get(state.getCurrentPlayer()).add(state.cardSelection.get(0));
                    state.meadowDeck.remove(state.cardSelection.get(0));
                    state.meadowDeck.add(state.cardDeck.draw());
                    state.cardCount[state.getCurrentPlayer()].increment();
                }
                return true;
            }, (everdellGameState -> {
                for(var card : everdellGameState.playerVillage.get(everdellGameState.getCurrentPlayer())){
                    if(card instanceof CemeteryCard cc){
                        cc.lockSecondLocation(everdellGameState);
                    }
                }
            }));

            POSTAL_PIGEON.createEverdellCard = (gameState) -> new CritterCard("Postal Pigeon", POSTAL_PIGEON, CardType.TAN_TRAVELER, false, false, 0, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                //The postal pigeon allows the player to reveal 2 cards, the player may select 1 of these cards to play for free
                // up to 3 points

                //CardSelection[0] will hold the card that the player chooses to play for free
                //CardSelection[1] will hold the card that the player chose to discard
                //If CardSelection[0] has a point token value higher than 3, it will not be paid for
                if(state.cardSelection.isEmpty()){
                    return true;
                }

                if (state.cardSelection.get(0).getPoints() <= 3) {
                    state.cardSelection.get(0).payForCard();
                    if(state.cardSelection.size() == 2) {
                        discardEverdellCard(state, state.cardSelection.get(1));
                    }
                }
                return true;
            }, (everdellGameState -> {
            }));

            JUDGE.createEverdellCard = (gameState) -> new JudgeCard("Judge", JUDGE, CardType.BLUE_GOVERNANCE, false, true, 2, new HashMap<>() {{
                put(ResourceTypes.BERRY, 3);
            }}, (state) -> {
                //The Judge allows the player to select a card from the meadow and play it for free
                return true;
            }, (everdellGameState -> {
            }));

            CRANE.createEverdellCard = (gameState) -> new ConstructionCard("Crane", CRANE, CardType.BLUE_GOVERNANCE, true, true, 1, new HashMap<>() {{
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                //This gives a discount of 3 resources to a card
                //ResourceSelection will tell us which 3 resources they had selected
                //CurrentCard will represent which card the player is trying to place

                int discountCounter = 0;

                //Give resources as a way of applying the discount

                // **NOTE** This card will currently add the resources even if the card costs less than 3 resources
                // Currently assumed that the player will always select the correct amount of resources
                for (var resource : state.resourceSelection.keySet()) {
                    for (int i = 0; i < state.resourceSelection.get(resource).getValue(); i++) {
                        if (discountCounter == 3) {
                            break;
                        }
                        discountCounter++;
                        state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment();
                    }
                    if (discountCounter == 3) {
                        break;
                    }
                }

                //Remove Resources based on card cost
                for (var resource : state.currentCard.getResourceCost().keySet()) {
                    state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement(state.currentCard.getResourceCost().get(resource));
                }
                state.currentCard.payForCard();

                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(ARCHITECT)));


            INNKEEPER.createEverdellCard = (gameState) -> new CritterCard("Innkeeper", INNKEEPER, CardType.BLUE_GOVERNANCE, false, true, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 1);
            }}, (state) -> {
                //Innkeeper allows the player to discount a card up to 3 Berries
                //ResourceSelection will tell us which 3 resources they had selected
                //CurrentCard will represent which card the player is trying to place

                int discountCounter = 0;

                //Give resources as a way of applying the discount
                for (int i = 0; i < state.resourceSelection.get(ResourceTypes.BERRY).getValue(); i++) {
                    //If we have already added an amount of resources that the card costs, we stop
                    if (discountCounter == state.currentCard.getResourceCost().get(ResourceTypes.BERRY)) {
                        break;
                    }

                    if (discountCounter == 3) {
                        break;
                    }
                    discountCounter++;
                    state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].increment();
                }
                state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].decrement(state.currentCard.getResourceCost().get(ResourceTypes.BERRY));

                state.currentCard.payForCard();

                return true;
            }, (everdellGameState -> {
            }));


            UNIVERSITY.createEverdellCard = (gameState) -> new ConstructionCard(RedDestinationLocation.UNIVERSITY_DESTINATION, "University", UNIVERSITY, CardType.RED_DESTINATION, true, true, 3, new HashMap<>() {{
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 2);
            }}, (state) -> {
                //Check University Location for behaviour
                return true;
            }, (everdellGameState -> {
                //Remove Effect

                //Find University Card in players village
                for (var card : everdellGameState.playerVillage.get(everdellGameState.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == UNIVERSITY) {
                        ConstructionCard cc = (ConstructionCard) card;
                        everdellGameState.everdellLocations.remove(cc.getLocation(everdellGameState));
                    }
                }
            }), new ArrayList<>(List.of(DOCTOR)));


            CHAPEL.createEverdellCard = (gameState) -> new ConstructionCard(RedDestinationLocation.CHAPEL_DESTINATION, "Chapel", CHAPEL, CardType.RED_DESTINATION, true, true, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 2);
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                //Check Chapel Location for behaviour
                return true;
            }, (everdellGameState -> {
                //Remove Effect
                System.out.println("Chapel Remove card Effect");
                //Find Chapel Card in players village
                for (var card : everdellGameState.playerVillage.get(everdellGameState.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == CHAPEL) {
                        ConstructionCard cc = (ConstructionCard) card;
                        everdellGameState.everdellLocations.remove(cc.getLocation(everdellGameState));
                    }
                }
            }), new ArrayList<>(List.of(SHEPHERD)));

            SHEPHERD.createEverdellCard = (gameState) -> new ShepherdCard("Shepherd", SHEPHERD, CardType.TAN_TRAVELER, false, true, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 3);
            }}, (state) -> {
                //Check Shepherd Card for behaviour
                return true;
            }, (everdellGameState -> {
            }));

            CLOCK_TOWER.createEverdellCard = (gameState) -> new ClockTowerCard("Clock Tower", CLOCK_TOWER, CardType.BLUE_GOVERNANCE, true, true, 3, new HashMap<>() {{
                put(ResourceTypes.TWIG, 3);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                //Check ClockTowerCard for behaviour
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(HISTORIAN)));

            COURTHOUSE.createEverdellCard = (gameState) -> new ConstructionCard("Courthouse", COURTHOUSE, CardType.BLUE_GOVERNANCE, true, true, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 2);
            }}, (state) -> {
                //The Courthouse allows the player to gain a Twig, a resin or a pebble after placing a construcion

                //ResourceSelection will hold the resource that the player has selected

                int counter = 0;

                for (var resource : state.resourceSelection.keySet()) {
                    for (int i = 0; i < state.resourceSelection.get(resource).getValue(); i++) {
                        if (counter == 1) {
                            break;
                        }
                        state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment();
                        counter++;
                    }
                    if (counter == 1) {
                        break;
                    }
                }
                state.resourceSelection.keySet().forEach(resource -> state.resourceSelection.get(resource).setValue(0));

                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(JUDGE)));


            RANGER.createEverdellCard = (gameState) -> new RangerCard("Ranger", RANGER, CardType.TAN_TRAVELER, false, true, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 2);
            }}, (state) -> {
                //Ranger allows the player to draw 1 card from the deck

                return true;

            }, (everdellGameState -> {
            }));

            DUNGEON.createEverdellCard = (gameState) -> new DungeonCard("Dungeon", DUNGEON, CardType.BLUE_GOVERNANCE, true, true, 0, new HashMap<>() {{
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 2);
            }}, (state) -> {
                //Dungeon allows the player to draw 2 cards from the deck
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(RANGER)));//Ranger

            MINER_MOLE.createEverdellCard = (gameState) -> new CopyCard("Miner Mole", MINER_MOLE, CardType.GREEN_PRODUCTION, false, false, 1, new HashMap<>() {{
                put(ResourceTypes.BERRY, 3);
            }}, (state) -> {

                //Miner Mole takes in a production card and copies its effect.
                //The player can select which production card to copy
                return true;
            }, (everdellGameState -> {
            }));

            EVER_TREE.createEverdellCard = (gameState) -> new ConstructionCard("Ever Tree", EVER_TREE, CardType.PURPLE_PROSPERITY, true, true, 5, new HashMap<>() {{
                put(ResourceTypes.TWIG, 3);
                put(ResourceTypes.RESIN, 3);
                put(ResourceTypes.PEBBLE, 3);
            }}, (state) -> {
                //The Ever Tree gives the player 1 point for every purple prosperity card in the village (Includes itself)
                int defaultPoints = 5;

                for(var card : state.playerVillage.get(state.getCurrentPlayer())){
                    if(card.getCardType() == CardType.PURPLE_PROSPERITY){
                        defaultPoints++;
                    }
                }
                for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (card.getCardEnumValue() == EVER_TREE) {
                        card.setCardPoints(defaultPoints);
                        break;
                    }
                }
                return true;
            }, (everdellGameState -> {
                //The Ever tree can house any critter in the game
            }), new ArrayList<>(List.of(Arrays.stream(CardDetails.values()).filter(c -> c != CardDetails.EVER_TREE && !c.createEverdellCard.apply(null).isConstruction()).toArray(CardDetails[]::new))));;

            STORE_HOUSE.createEverdellCard = (gameState) -> new StorehouseCard(RedDestinationLocation.STORE_HOUSE_DESTINATION, "Storehouse", STORE_HOUSE, CardType.GREEN_PRODUCTION, true, false, 2, new HashMap<>() {{
                put(ResourceTypes.TWIG, 1);
                put(ResourceTypes.RESIN, 1);
                put(ResourceTypes.PEBBLE, 1);
            }}, (state) -> {
                //Check STORE HOUSE card for behaviour
                return true;
            }, (everdellGameState -> {
            }), new ArrayList<>(List.of(WOOD_CARVER)));

        }
    }

    public HashMap<CardType, Color> cardColour = new HashMap<CardType, Color>() {{
        put(CardType.BLUE_GOVERNANCE, new Color(44, 115, 166));
        put(CardType.GREEN_PRODUCTION, new Color(40, 112, 32));
        put(CardType.PURPLE_PROSPERITY, new Color(145, 57, 186));
        put(CardType.RED_DESTINATION, new Color(172, 21, 21));
        put(CardType.TAN_TRAVELER, new Color(156, 125, 89));
    }};

    public static ArrayList<Color> playerColour = new ArrayList<>() {{
        add(new Color(220, 220, 220));
        add(new Color(220, 220, 220));
        add(new Color(220, 220, 220));
        add(new Color(220, 220, 220));
    }};


    HashMap<CardDetails, Integer> everdellCardCount = new HashMap<CardDetails, Integer>() {{
        //put(CardDetails.FARM, 64);
//        put(CardDetails.WIFE, 16);
//        put(CardDetails.HUSBAND, 16);

        put(CardDetails.ARCHITECT, 2);
        put(CardDetails.BARD, 2);
        put(CardDetails.BARGE_TOAD, 3);
        put(CardDetails.CASTLE, 2);
        put(CardDetails.CEMETERY, 2);
        put(CardDetails.CHAPEL, 2);
        put(CardDetails.CHIP_SWEEP, 3);
        put(CardDetails.CLOCK_TOWER, 3);
        put(CardDetails.COURTHOUSE, 2);
        put(CardDetails.CRANE, 3);
        put(CardDetails.DOCTOR, 2);
        put(CardDetails.DUNGEON, 2);
        put(CardDetails.EVER_TREE, 2);
        put(CardDetails.FAIRGROUNDS, 3);
        put(CardDetails.FARM, 8);
        put(CardDetails.FOOL, 2);
        put(CardDetails.GENERAL_STORE, 3);
        put(CardDetails.HISTORIAN, 3);
        put(CardDetails.HUSBAND, 4);
        put(CardDetails.INN, 3);
        put(CardDetails.INNKEEPER, 3);
        put(CardDetails.JUDGE, 2);
        put(CardDetails.KING, 2);
        put(CardDetails.LOOKOUT, 2);
        put(CardDetails.MINE, 3);
        put(CardDetails.MINER_MOLE, 3);
        put(CardDetails.MONASTERY, 2);
        put(CardDetails.MONK, 2);
        put(CardDetails.PALACE, 2);
        put(CardDetails.PEDDLER, 3);
        put(CardDetails.POST_OFFICE, 3);
        put(CardDetails.POSTAL_PIGEON, 3);
        put(CardDetails.QUEEN, 2);
        put(CardDetails.RANGER, 2);
        put(CardDetails.RESIN_REFINERY, 3);
        put(CardDetails.RUINS, 3);
        put(CardDetails.SCHOOL, 2);
        put(CardDetails.SHEPHERD, 2);
        put(CardDetails.SHOP_KEEPER, 3);
        put(CardDetails.STORE_HOUSE, 3);
        put(CardDetails.TEACHER, 3);
        put(CardDetails.THEATRE, 2);
        put(CardDetails.TWIG_BARGE, 3);
        put(CardDetails.UNDERTAKER, 2);
        put(CardDetails.UNIVERSITY, 2);
        put(CardDetails.WANDERER, 3);
        put(CardDetails.WIFE, 4);
        put(CardDetails.WOOD_CARVER, 3);
    }};

    @Override
    protected AbstractParameters _copy() {
        // TODO: deep copy of all variables.
        EverdellParameters copy = new EverdellParameters();
        copy.cardColour = new HashMap<>(this.cardColour);
        copy.everdellCardCount = new HashMap<>(this.everdellCardCount);

        return copy;
    }


    @Override
    public boolean _equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EverdellParameters that = (EverdellParameters) o;
        return Objects.equals(cardColour, that.cardColour) && Objects.equals(everdellCardCount, that.everdellCardCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardColour, everdellCardCount);
    }
}
