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
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters.ResourceTypes;
import games.everdell.EverdellParameters.Locations;
import games.everdell.components.EverdellLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        state.playerTurn = 0;


        state.PlayerResources = new HashMap<>();
        state.cardCount = new Counter[state.getNPlayers()];
        state.workers = new Counter[state.getNPlayers()];
        state.pointTokens = new Counter[state.getNPlayers()];
        state.currentSeason = new EverdellParameters.Seasons[state.getNPlayers()];

        state.playerHands = new ArrayList<>();
        state.playerVillage = new ArrayList<>(state.getNPlayers());

        state.resourceLocations = new HashMap<>();


        //Assigning functions to each location
        for(var location : Locations.values()){
            if(location == Locations.ONE_BERRY){
                state.resourceLocations.put(location,new EverdellLocation(location, k-> state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment(), true));
            }
            if(location == Locations.ONE_BERRY_ONE_CARD){
                state.resourceLocations.put(location,new EverdellLocation(location, k-> {
                    state.PlayerResources.get(ResourceTypes.BERRY)[state.playerTurn].increment();
                    if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                        state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    }
                    state.cardCount[state.playerTurn].increment();
                    return k;
                }, false));
            }
            if(location == Locations.ONE_PEBBLE){
                state.resourceLocations.put(location,new EverdellLocation(location, k-> state.PlayerResources.get(ResourceTypes.PEBBLE)[state.playerTurn].increment(), false));
            }
            if(location == Locations.TWO_CARD_ONE_POINT){
                state.resourceLocations.put(location,new EverdellLocation(location, k-> {
                    state.pointTokens[state.playerTurn].increment();
                    if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()-1){
                        state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                        state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    } else if (state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                        state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    }
                    state.cardCount[state.playerTurn].increment(2);
                    return k;
                }, true));
            }
            if(location == Locations.TWO_RESIN){
                state.resourceLocations.put(location,new EverdellLocation(location, k-> state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment(2), false));
            }
            if(location == Locations.TWO_WOOD_ONE_CARD){
                state.resourceLocations.put(location,new EverdellLocation(location, k-> {
                    state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(2);
                    if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                        state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    }
                    state.cardCount[state.playerTurn].increment();
                    return k;
                }, true));
            }
            if(location == Locations.THREE_WOOD){
                state.resourceLocations.put(location,new EverdellLocation(location, k-> state.PlayerResources.get(ResourceTypes.TWIG)[state.playerTurn].increment(3), false));
            }
            if(location == Locations.ONE_RESIN_ONE_CARD){
                state.resourceLocations.put(location,new EverdellLocation(location, k-> {
                    state.PlayerResources.get(ResourceTypes.RESIN)[state.playerTurn].increment();
                    if(state.playerHands.get(state.playerTurn).getSize() < state.playerHands.get(state.playerTurn).getCapacity()){
                        state.playerHands.get(state.playerTurn).add(state.cardDeck.draw());
                    }
                    state.cardCount[state.playerTurn].increment();
                    return k;
                }, true));
            }
        }



        //Set up the deck to be drawn from
        state.cardDeck = new Deck<>("Card Deck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        for(Map.Entry<EverdellCard.CardType, Integer> entry : parameters.everdellCardCount.entrySet()){
            for(int i = 0; i < entry.getValue(); i++){
                EverdellCard card = new EverdellCard(entry.getKey());
                state.cardDeck.add(card,0);
            }
        }
        System.out.println(state.cardDeck.getSize());
        System.out.println(state.cardDeck);
        state.cardDeck.shuffle(state.getRnd());

        //Add Cards to the meadow deck
        state.meadowDeck = new Deck<>("Meadow Deck", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
        for (int i = 0; i < 8; i++) {
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
            state.currentSeason[i] = EverdellParameters.Seasons.WINTER;

            state.cardCount[i].increment(5+i);
            state.workers[i].increment(2);


            state.playerVillage.add(new Deck<>("Player Village",i, CoreConstants.VisibilityMode.VISIBLE_TO_ALL));
            state.playerHands.add(new Deck<>("Player Hand", i, CoreConstants.VisibilityMode.VISIBLE_TO_OWNER));
            for (int j = 0; j < state.cardCount[i].getValue(); j++) {
                //Set Hand Capacity to 8
                state.playerHands.get(i).setCapacity(8);

                //Set Village Capacity to 15
                state.playerVillage.get(i).setCapacity(15);


                EverdellCard card = state.cardDeck.draw();
                state.playerHands.get(i).add(card);
            }
        }
        System.out.println(state.playerHands);
        System.out.println(state.playerVillage);

    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        List<AbstractAction> actions = new ArrayList<>();
        // TODO: create action classes for the current player in the given game state and add them to the list. Below just an example that does nothing, remove.
        actions.add(new PlaceWorker());
        actions.add(new PlayCard());
        actions.add(new MoveSeason());
        return actions;
    }
}
