package games.everdell;

import core.AbstractGameState;
import core.CoreConstants;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Counter;
import core.components.Deck;
import games.everdell.actions.*;
import games.everdell.components.ConstructionCard;
import games.everdell.components.CritterCard;
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters.ResourceTypes;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.EverdellParameters.ForestLocations;
import games.everdell.components.EverdellLocation;

import java.util.*;

import static core.CoreConstants.GameResult.GAME_END;

/**
 * <p>The forward model contains all the game rules and logic. It is mainly responsible for declaring rules for:</p>
 * <ol>
 *     <li>Game setup</li>
 *     <li>Actions available to players in a given game state</li>
 *     <li>Game events or rules applied after a player's action</li>
 *     <li>Game end</li>
 * </ol>
 */
public class EverdellForwardModel extends StandardForwardModel {

    /**
     * Initializes all variables in the given game state. Performs initial game setup according to game rules, e.g.:
     * <ul>
     *     <li>Sets up decks of cards and shuffles them</li>
     *     <li>Gives player cards</li>
     *     <li>Places tokens on boards</li>
     *     <li>...</li>
     * </ul>
     *
     * @param firstState - the state to be modified to the initial game state.
     */
    @Override
    protected void _setup(AbstractGameState firstState) {
        // TODO: perform initialization of variables and game setup
        EverdellGameState state = (EverdellGameState) firstState;
        EverdellParameters parameters = (EverdellParameters) state.getGameParameters();

        int sharedLocationSpace = 4;
        int exclusiveLocationSpace = 1;
        int forestLocationSpace = 2;


        state.PlayerResources = new HashMap<>();
        state.cardCount = new Counter[state.getNPlayers()];
        state.workers = new Counter[state.getNPlayers()];
        state.pointTokens = new Counter[state.getNPlayers()];
        state.villageMaxSize = new Counter[state.getNPlayers()];
        state.currentSeason = new EverdellParameters.Seasons[state.getNPlayers()];
        state.copyMode = false;
        state.copyID = -1;

        state.playerHands = new ArrayList<>();
        state.playerVillage = new ArrayList<>(state.getNPlayers());

        state.everdellLocations = new ArrayList<>();
        state.cardSelection = new ArrayList<>();

        state.discardDeck = new Deck<>("Discard Deck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        state.temporaryDeck = new Deck<>("Temporary Deck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);

        //Creating an EverdellLocation object for each basic location
        for(var location : BasicLocations.values()){
            if(location == BasicLocations.ONE_BERRY){
                state.everdellLocations.add(new EverdellLocation(location,sharedLocationSpace, false, BasicLocations.ONE_BERRY.getLocationEffect(state)));
            }
            if(location == BasicLocations.ONE_BERRY_ONE_CARD){
                state.everdellLocations.add(new EverdellLocation(location,exclusiveLocationSpace, false, BasicLocations.ONE_BERRY_ONE_CARD.getLocationEffect(state)));
            }
            if(location == BasicLocations.ONE_PEBBLE){
                state.everdellLocations.add(new EverdellLocation(location, exclusiveLocationSpace, false, BasicLocations.ONE_PEBBLE.getLocationEffect(state)));
            }
            if(location == BasicLocations.TWO_CARD_ONE_POINT){
                state.everdellLocations.add(new EverdellLocation(location, sharedLocationSpace, false, BasicLocations.TWO_CARD_ONE_POINT.getLocationEffect(state)));
            }
            if(location == BasicLocations.TWO_RESIN){
                state.everdellLocations.add(new EverdellLocation(location, exclusiveLocationSpace, false, BasicLocations.TWO_RESIN.getLocationEffect(state)));
            }
            if(location == BasicLocations.TWO_WOOD_ONE_CARD){
                state.everdellLocations.add(new EverdellLocation(location,sharedLocationSpace, false, BasicLocations.TWO_WOOD_ONE_CARD.getLocationEffect(state)));
            }
            if(location == BasicLocations.THREE_WOOD){
                state.everdellLocations.add(new EverdellLocation(location, exclusiveLocationSpace, false, BasicLocations.THREE_WOOD.getLocationEffect(state)));
            }
            if(location == BasicLocations.ONE_RESIN_ONE_CARD){
                state.everdellLocations.add(new EverdellLocation(location, sharedLocationSpace, false, BasicLocations.ONE_RESIN_ONE_CARD.getLocationEffect(state)));
            }
        }

        //Randomly Select forest locations
        int numberOfLocations = 3;
        if(state.getNPlayers() >= 3){
            numberOfLocations = 4;
        }
        Set<ForestLocations> selectedLocations = new HashSet<>();
        Random random = new Random();
        //Ensure the Locations are unique
        while (selectedLocations.size() < numberOfLocations) {
            ForestLocations location = ForestLocations.values()[random.nextInt(ForestLocations.values().length)];
            selectedLocations.add(location);
        }
        //Insert the selected locations into the game state



        //Tests
        selectedLocations.add(ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD);

        for (ForestLocations location : selectedLocations) {
            state.everdellLocations.add(new EverdellLocation(location, forestLocationSpace, false, location.getLocationEffect(state)));
        }

        //Set up values for Forest Locations
        state.resourceSelection = new HashMap<ResourceTypes, Counter>();
        state.resourceSelection.put(ResourceTypes.TWIG, new Counter());
        state.resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
        state.resourceSelection.put(ResourceTypes.BERRY, new Counter());
        state.resourceSelection.put(ResourceTypes.RESIN, new Counter());

        ForestLocations.cardChoices = new ArrayList<>();


        //Setup Haven
        for (EverdellParameters.HavenLocation event : EverdellParameters.HavenLocation.values()) {
            state.everdellLocations.add(new EverdellLocation(event, 999, true, event.getLocationEffect(state)));
        }

        //Setup Journey Locations
        for (EverdellParameters.JourneyLocations event : EverdellParameters.JourneyLocations.values()) {
            if(event == EverdellParameters.JourneyLocations.JOURNEY_2){
                state.everdellLocations.add(new EverdellLocation(event, 999, true, event.getLocationEffect(state)));
            }
            else {
                state.everdellLocations.add(new EverdellLocation(event, exclusiveLocationSpace, false, event.getLocationEffect(state)));
            }
        }

        //Setup Special Events

        int numOfSpecialEvents = 12;
        Set<EverdellParameters.SpecialEvent> selectedSpecialEvents = new HashSet<>();
        //Ensure the Locations are unique
        while (selectedSpecialEvents.size() < numOfSpecialEvents) {
            EverdellParameters.SpecialEvent location = EverdellParameters.SpecialEvent.values()[random.nextInt(EverdellParameters.SpecialEvent.values().length)];
            selectedSpecialEvents.add(location);
        }
        //Insert the selected events into the game state
        for (EverdellParameters.SpecialEvent location : selectedSpecialEvents) {
            state.everdellLocations.add(new EverdellLocation(location, 1, false, location.getLocationEffect(state)));
        }


        //Set up Basic Events
        for (EverdellParameters.BasicEvent event : EverdellParameters.BasicEvent.values()) {
            state.everdellLocations.add(new EverdellLocation(event, exclusiveLocationSpace, false, event.getLocationEffect(state)));
        }


        //Set up the deck to be drawn from
        state.cardDeck = new Deck<>("Card Deck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        for(Map.Entry<EverdellParameters.CardDetails, Integer> entry : parameters.everdellCardCount.entrySet()){
            for(int i = 0; i < entry.getValue(); i++){
                EverdellCard card = entry.getKey().createEverdellCard.apply(state);
                if(card instanceof ConstructionCard constructionCard){
                    state.cardDeck.add(constructionCard,0);
                }
                else{
                    state.cardDeck.add(card,0);
                }
            }
        }
        System.out.println(state.cardDeck.getSize());
        System.out.println(state.cardDeck);
        state.cardDeck.shuffle(state.getRnd());

        //Add Cards to the meadow deck
        state.meadowDeck = new Deck<>("Meadow Deck", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
        state.meadowDeck.setCapacity(8);
        for (int i = 0; i < state.meadowDeck.getCapacity(); i++) {
            EverdellCard card = state.cardDeck.draw();
            state.meadowDeck.add(card);
        }

        //Add Cards to the player hands
        // and set up player resources

        state.PlayerResources.put(ResourceTypes.TWIG, new Counter[state.getNPlayers()]);
        state.PlayerResources.put(ResourceTypes.PEBBLE, new Counter[state.getNPlayers()]);
        state.PlayerResources.put(ResourceTypes.BERRY, new Counter[state.getNPlayers()]);
        state.PlayerResources.put(ResourceTypes.RESIN, new Counter[state.getNPlayers()]);

        for (int i = 0; i < state.getNPlayers(); i++) {

            state.PlayerResources.get(ResourceTypes.TWIG)[i] = new Counter();
            state.PlayerResources.get(ResourceTypes.PEBBLE)[i] = new Counter();
            state.PlayerResources.get(ResourceTypes.BERRY)[i] = new Counter();
            state.PlayerResources.get(ResourceTypes.RESIN)[i] = new Counter();

            //TEST
            state.PlayerResources.get(ResourceTypes.TWIG)[i].increment(10);
            state.PlayerResources.get(ResourceTypes.BERRY)[i].increment(10);
            //******

            state.cardCount[i] = new Counter(8,"Player "+(i+1)+" Card Count");
            state.workers[i] = new Counter("Player "+(i+1)+" Workers");
            state.pointTokens[i] = new Counter("Player "+(i+1)+" Point Tokens");
            state.pointTokens[i].setMinimum(-999);
            state.villageMaxSize[i] = new Counter("Player "+(i+1)+" Village Max Size");
            state.currentSeason[i] = EverdellParameters.Seasons.WINTER;

            state.villageMaxSize[i].increment(15);
            state.cardCount[i].increment(5+i);
            state.workers[i].increment(2);


            state.playerVillage.add(new Deck<>("Player Village",i, CoreConstants.VisibilityMode.VISIBLE_TO_ALL));
            state.playerHands.add(new Deck<>("Player Hand", i, CoreConstants.VisibilityMode.VISIBLE_TO_OWNER));
            for (int j = 0; j < state.cardCount[i].getValue(); j++) {
                //Set Hand Capacity to 8
                state.playerHands.get(i).setCapacity(8);

                //Set Village Capacity to 15
               // state.playerVillage.get(i).setCapacity(15;


                EverdellCard card = state.cardDeck.draw();
                state.playerHands.get(i).add(card);
            }
        }

        populateWithTest(state);

    }


    private void populateWithTest(EverdellGameState state){
        //Add green production cards to player 2
//        while(state.playerVillage.get(1).getComponents().size() < 5){
//            EverdellCard card = state.cardDeck.draw();
//            if(card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION){
//                state.playerVillage.get(1).add(card);
//            }
//        }
        //Add the inn card to player 2
//        while(state.playerVillage.get(1).getComponents().size() < 6){
//            EverdellCard card = state.cardDeck.draw();
//            state.temporaryDeck.add(card);
//            if(card.getCardEnumValue() == EverdellParameters.CardDetails.CHAPEL){
//                card.payForCard();
//                System.out.println("Adding Chapel to Player 2");
//                System.out.println("Card: "+card);
//                endPlayerTurn(state);
//                new PlayCard(1, card.getComponentID(), new ArrayList<>(), new HashMap<>()).execute(state);
//                break;
//            }
//        }

    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        System.out.println("Forward Model : Computing Available Actions, Current Player: "+gameState.getCurrentPlayer());
        for(int i=0; i<gameState.getNPlayers(); i++){
            System.out.println("Player : "+i+" with Results : "+gameState.getPlayerResults()[i]);
        }
        List<AbstractAction> actions = new ArrayList<>();
        EverdellGameState egs = (EverdellGameState) gameState;
        // TODO: create action classes for the current player in the given game state and add them to the list. Below just an example that does nothing, remove.
        EverdellParameters params = (EverdellParameters) gameState.getGameParameters();

        //Location Decisions
        if(!new SelectLocation(gameState.getCurrentPlayer(), -1)._computeAvailableActions(egs).isEmpty()) {
            actions.add(new SelectLocation(gameState.getCurrentPlayer(), -1));
        }

        //Card Decisions
        ArrayList<Integer> cardsToSelectFrom = new ArrayList<>();
//        for (EverdellCard card : egs.meadowDeck.getComponents()) {
//            cardsToSelectFrom.add(card);
//        }
        for (EverdellCard card : egs.playerHands.get(gameState.getCurrentPlayer()).getComponents()) {
            cardsToSelectFrom.add(card.getComponentID());
        }
        System.out.println("*************CHECKING IF ACTION CAN BE PERFORMED*************");
        if(!new SelectCard(gameState.getCurrentPlayer(), -1, cardsToSelectFrom)._computeAvailableActions(egs).isEmpty()) {
            System.out.println("*************PERFORMING ACTION*************");
            actions.add(new SelectCard(gameState.getCurrentPlayer(), -1, cardsToSelectFrom));
        }

        //Season Decisions
//        if(egs.workers[egs.getCurrentPlayer()].getValue() == 0) {
//            actions.add(new MoveSeason(new ArrayList<>()));
//        }
        if(actions.isEmpty()){
            actions.add(new EndGame());

        }

        return actions;
    }

    @Override
    protected void _afterAction(AbstractGameState currentState, AbstractAction action) {
        if (currentState.isActionInProgress()) return;

        EverdellGameState egs = (EverdellGameState) currentState;


        updatePurpleProsperityCards(egs);

        System.out.println("Forward Model : After Action");
        if(checkEndForPlayer((EverdellGameState) currentState, action)){
            System.out.println("Game Over for Player "+currentState.getCurrentPlayer());
            System.out.println("Player "+currentState.getCurrentPlayer()+" with card : "+egs.playerHands.get(currentState.getCurrentPlayer()));
            currentState.setPlayerResult(GAME_END, currentState.getCurrentPlayer());
            System.out.println("Player "+currentState.getCurrentPlayer()+" has ENDED WITH RESULT : "+currentState.getPlayerResults()[currentState.getCurrentPlayer()]);
        }
        if(checkEnd((EverdellGameState) currentState)){
            System.out.println("Game End");
            endGame(currentState);
            return;
        }

        egs.temporaryDeck.clear();

        endPlayerTurn(currentState);
    }
    private boolean checkEndForPlayer(EverdellGameState state, AbstractAction action){
        if(action instanceof EndGame){
            return true;
        }
        return false;
    }


    private boolean checkEnd(EverdellGameState state){
        for (var result : state.getPlayerResults()) {
            if (result != GAME_END) {
                return false;
            }
        }
        return true;
    }

    private void updatePurpleProsperityCards(EverdellGameState state){
        //Go through each player and check if they have purple prosperity cards and call their card functions
        for(int i=0; i<state.getNPlayers(); i++){
            for (var card : state.playerVillage.get(i)){
                if(card.getCardType() == EverdellParameters.CardType.PURPLE_PROSPERITY){
                    if(card instanceof ConstructionCard cc){
                        cc.applyCardEffect(state);
                    }
                    else{
                        CritterCard cc = (CritterCard) card;
                        cc.applyCardEffect(state);
                    }
                }
            }
        }

    }

}
