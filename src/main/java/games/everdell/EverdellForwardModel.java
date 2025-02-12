package games.everdell;

import core.AbstractGameState;
import core.CoreConstants;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Counter;
import core.components.Deck;
import games.everdell.actions.EverdellAction;
import games.everdell.actions.MoveSeason;
import games.everdell.actions.PlaceWorker;
import games.everdell.actions.PlayCard;
import games.everdell.components.ConstructionCard;
import games.everdell.components.CritterCard;
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters.ResourceTypes;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.components.EverdellLocation;
import games.tictactoe.TicTacToeGameState;

import java.util.*;

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

        state.playerHands = new ArrayList<>();
        state.playerVillage = new ArrayList<>(state.getNPlayers());

        state.Locations = new HashMap<>();
        state.cardSelection = new ArrayList<>();

        state.discardDeck = new Deck<>("Discard Deck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);

        //Creating an EverdellLocation object for each basic location
        for(var location : BasicLocations.values()){
            if(location == BasicLocations.ONE_BERRY){
                state.Locations.put(location,new EverdellLocation(location,sharedLocationSpace, false, BasicLocations.ONE_BERRY.getLocationEffect(state)));
            }
            if(location == BasicLocations.ONE_BERRY_ONE_CARD){
                state.Locations.put(location,new EverdellLocation(location,exclusiveLocationSpace, false, BasicLocations.ONE_BERRY_ONE_CARD.getLocationEffect(state)));
            }
            if(location == BasicLocations.ONE_PEBBLE){
                state.Locations.put(location,new EverdellLocation(location, exclusiveLocationSpace, false, BasicLocations.ONE_PEBBLE.getLocationEffect(state)));
            }
            if(location == BasicLocations.TWO_CARD_ONE_POINT){
                state.Locations.put(location,new EverdellLocation(location, sharedLocationSpace, false, BasicLocations.TWO_CARD_ONE_POINT.getLocationEffect(state)));
            }
            if(location == BasicLocations.TWO_RESIN){
                state.Locations.put(location,new EverdellLocation(location, exclusiveLocationSpace, false, BasicLocations.TWO_RESIN.getLocationEffect(state)));
            }
            if(location == BasicLocations.TWO_WOOD_ONE_CARD){
                state.Locations.put(location,new EverdellLocation(location,sharedLocationSpace, false, BasicLocations.TWO_WOOD_ONE_CARD.getLocationEffect(state)));
            }
            if(location == BasicLocations.THREE_WOOD){
                state.Locations.put(location,new EverdellLocation(location, exclusiveLocationSpace, false, BasicLocations.THREE_WOOD.getLocationEffect(state)));
            }
            if(location == BasicLocations.ONE_RESIN_ONE_CARD){
                state.Locations.put(location,new EverdellLocation(location, sharedLocationSpace, false, BasicLocations.ONE_RESIN_ONE_CARD.getLocationEffect(state)));
            }
        }

        //Randomly Select forest locations
        int numberOfLocations = 3;
        if(state.getNPlayers() >= 3){
            numberOfLocations = 4;
        }
        Set<EverdellParameters.ForestLocations> selectedLocations = new HashSet<>();
        Random random = new Random();
        //Ensure the Locations are unique
        while (selectedLocations.size() < numberOfLocations) {
            EverdellParameters.ForestLocations location = EverdellParameters.ForestLocations.values()[random.nextInt(EverdellParameters.ForestLocations.values().length)];
            selectedLocations.add(location);
        }
        //Insert the selected locations into the game state
        for (EverdellParameters.ForestLocations location : selectedLocations) {
            state.Locations.put(location, new EverdellLocation(location, forestLocationSpace, false, location.getLocationEffect(state)));
        }

        //Set up values for Forest Locations
        state.resourceSelection = new HashMap<ResourceTypes, Counter>();
        state.resourceSelection.put(ResourceTypes.TWIG, new Counter());
        state.resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
        state.resourceSelection.put(ResourceTypes.BERRY, new Counter());
        state.resourceSelection.put(ResourceTypes.RESIN, new Counter());

        EverdellParameters.ForestLocations.cardChoices = new ArrayList<>();

        //Setup Haven
        for (EverdellParameters.HavenLocation event : EverdellParameters.HavenLocation.values()) {
            state.Locations.put(event, new EverdellLocation(event, 999, true, event.getLocationEffect(state)));
        }


        //Set up Basic Events
        for (EverdellParameters.BasicEvent event : EverdellParameters.BasicEvent.values()) {
            state.Locations.put(event, new EverdellLocation(event, exclusiveLocationSpace, false, event.getLocationEffect(state)));
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

            state.cardCount[i] = new Counter(8,"Player "+(i+1)+" Card Count");
            state.workers[i] = new Counter("Player "+(i+1)+" Workers");
            state.pointTokens[i] = new Counter("Player "+(i+1)+" Point Tokens");
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
        System.out.println(state.playerHands);
        System.out.println(state.playerVillage);


        //Add green production cards to player 2
        while(state.playerVillage.get(1).getComponents().size() < 5){
            EverdellCard card = state.cardDeck.draw();
            if(card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION){
                state.playerVillage.get(1).add(card);
            }
        }

    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        List<AbstractAction> actions = new ArrayList<>();
        EverdellGameState egs = (EverdellGameState) gameState;
        // TODO: create action classes for the current player in the given game state and add them to the list. Below just an example that does nothing, remove.
        EverdellParameters params = (EverdellParameters) gameState.getGameParameters();

        for(EverdellLocation location : egs.Locations.values()){
            if(location.getLocation() instanceof BasicLocations){
                if(location.isLocationFreeForPlayer(gameState)){
                    actions.add(new PlaceWorker(location.getLocation(), egs.cardSelection, egs.resourceSelection));
                }
            }
        }

        return actions;
    }


    @Override
    protected void _afterAction(AbstractGameState currentState, AbstractAction action) {

        endPlayerTurn(currentState);
    }
}
