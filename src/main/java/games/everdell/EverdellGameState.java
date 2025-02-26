package games.everdell;

import core.AbstractGameState;
import core.AbstractParameters;
import core.components.Component;
import core.components.Counter;
import core.components.Deck;
import core.components.FrenchCard;
import games.GameType;
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.ResourceTypes;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.EverdellParameters.AbstractLocations;
import games.everdell.EverdellParameters.ForestLocations;
import games.everdell.components.EverdellLocation;
import org.apache.spark.internal.config.R;

import java.util.*;
import java.util.function.Function;

import static java.util.Objects.*;

/**
 * <p>The game state encapsulates all game information. It is a data-only class, with game functionality present
 * in the Forward Model or actions modifying the state of the game.</p>
 * <p>Most variables held here should be {@link Component} subclasses as much as possible.</p>
 * <p>No initialisation or game logic should be included here (not in the constructor either). This is all handled externally.</p>
 * <p>Computation may be included in functions here for ease of access, but only if this is querying the game state information.
 * Functions on the game state should never <b>change</b> the state of the game.</p>
 */
public class EverdellGameState extends AbstractGameState {
    /**
     * @param gameParameters - game parameters.
     * @param nPlayers       - number of players in the game
     */
    public Deck<EverdellCard> cardDeck;
    public Deck<EverdellCard> discardDeck;

    public  Deck<EverdellCard> meadowDeck;
    public List<Deck<EverdellCard>> playerHands;
    public List<Deck<EverdellCard>> playerVillage;
    public EverdellParameters.Seasons[] currentSeason;

    public HashMap<AbstractLocations,EverdellLocation> Locations;

    public HashMap<ResourceTypes,Counter[]> PlayerResources;

    public Counter[] cardCount;
    public Counter[] workers;
    public Counter[] pointTokens;

    public Counter[] villageMaxSize;

    //These values are used to indicate what the player has selected in their turn
    //I'm not sure what is better. To use it like this or try to send the actions values instead?
    //I think this keeps things more organised

    public EverdellCard currentCard;
    public HashMap<ResourceTypes, Counter> resourceSelection;
    public ArrayList<EverdellCard> cardSelection;






    public EverdellGameState(AbstractParameters gameParameters, int nPlayers) {
        super(gameParameters, nPlayers);
    }



    /**
     * @return the enum value corresponding to this game, declared in {@link GameType}.
     */
    @Override
    protected GameType _getGameType() {
        // TODO: replace with game-specific enum value declared in GameType
        return GameType.Everdell;
    }


    public void printAllComponents(){
        System.out.println("Printing all components");
        for(var component : _getAllComponents()){
            System.out.println(component+" : "+component.getComponentID()+" : "+component.getComponentID());
        }
        System.out.println("End of components");
    }

    /**
     * Returns all Components used in the game and referred to by componentId from actions or rules.
     * This method is called after initialising the game state, so all components will be initialised already.
     *
     * @return - List of Components in the game.
     */
    @Override
    protected List<Component> _getAllComponents() {
        // TODO: add all components to the list
        List<Component> components = new ArrayList<>();

        components.add(cardDeck.copy());
        components.add(meadowDeck.copy());
        for(var hand : playerHands){
            components.add(hand.copy());
            for(var card : hand){
                components.add(card.copy());
            }
        }
        for(var village : playerVillage){
            components.add(village.copy());
        }
        for(var resource : PlayerResources.keySet()){
            for(int i = 0; i< this.getNPlayers(); i++){
                components.add(PlayerResources.get(resource)[i].copy());
            }
        }
        for(int i = 0; i< cardCount.length; i++){
            components.add(cardCount[i].copy());
            components.add(workers[i].copy());
            components.add(pointTokens[i].copy());
            components.add(villageMaxSize[i].copy());
        }

        for(var resource : resourceSelection.keySet()){
            components.add(resourceSelection.get(resource).copy());
        }

        //Locations
        for(EverdellLocation location : Locations.values()){
            components.add(location.copy());
        }

        if(currentCard != null){
            components.add(currentCard.copy());
        }

        return components;
    }

    /**
     * <p>Create a deep copy of the game state containing only those components the given player can observe.</p>
     * <p>If the playerID is NOT -1 and If any components are not visible to the given player (e.g. cards in the hands
     * of other players or a face-down deck), then these components should instead be randomized (in the previous examples,
     * the cards in other players' hands would be combined with the face-down deck, shuffled together, and then new cards drawn
     * for the other players). This process is also called 'redeterminisation'.</p>
     * <p>There are some utilities to assist with this in utilities.DeterminisationUtilities. One firm is guideline is
     * that the standard random number generator from getRnd() should not be used in this method. A separate Random is provided
     * for this purpose - redeterminisationRnd.
     *  This is to avoid this RNG stream being distorted by the number of player actions taken (where those actions are not themselves inherently random)</p>
     * <p>If the playerID passed is -1, then full observability is assumed and the state should be faithfully deep-copied.</p>
     *
     * <p>Make sure the return type matches the class type, and is not AbstractGameState.</p>
     *
     *
     * @param playerId - player observing this game state.
     */
    @Override
    protected EverdellGameState _copy(int playerId) {
        EverdellGameState copy = new EverdellGameState(gameParameters, getNPlayers());
        // TODO: deep copy all variables to the new game state.

        copy.cardDeck = cardDeck.copy();
        copy.meadowDeck = meadowDeck.copy();

        copy.playerHands = new ArrayList<>();
        for(var hand : playerHands){
            copy.playerHands.add(hand.copy());
        }
        copy.playerVillage = new ArrayList<>();
        for(var village : playerVillage){
            copy.playerVillage.add(village.copy());
        }


        copy.workers = new Counter[workers.length];
        copy.pointTokens = new Counter[pointTokens.length];
        copy.cardCount = new Counter[cardCount.length];
        copy.villageMaxSize = new Counter[villageMaxSize.length];
        for(int i = 0; i< cardCount.length; i++){
            copy.workers[i] = workers[i].copy();
            copy.pointTokens[i] = pointTokens[i].copy();
            copy.cardCount[i] = cardCount[i].copy();
            copy.villageMaxSize[i] = villageMaxSize[i].copy();
        }

        copy.resourceSelection = new HashMap<>();
        for(var resource : resourceSelection.keySet()){
            copy.resourceSelection.put(resource, resourceSelection.get(resource).copy());
        }

        copy.cardSelection = new ArrayList<>();
        for(var card : cardSelection){
            copy.cardSelection.add(card.copy());
        }


        copy.currentSeason = new EverdellParameters.Seasons[currentSeason.length];
        for(int i=0;i<currentSeason.length;i++){
            copy.currentSeason[i] = currentSeason[i];
        }

        copy.Locations = new HashMap<>();
        for(var location : Locations.keySet()){
            copy.Locations.put(location, Locations.get(location).copy());
        }

        copy.PlayerResources = new HashMap<>();
        for(var resource : PlayerResources.keySet()){
            copy.PlayerResources.put(resource, new Counter[PlayerResources.get(resource).length]);
            for(int i = 0; i< this.getNPlayers(); i++){
                copy.PlayerResources.get(resource)[i] = PlayerResources.get(resource)[i].copy();
            }
        }

        if(currentCard != null){
            copy.currentCard = currentCard.copy();
        }
        return copy;
    }

    /**
     * @param playerId - player observing the state.
     * @return a score for the given player approximating how well they are doing (e.g. how close they are to winning
     * the game); a value between 0 and 1 is preferred, where 0 means the game was lost, and 1 means the game was won.
     */
    @Override
    protected double _getHeuristicScore(int playerId) {
        if (isNotTerminal()) {
            // TODO calculate an approximate value
            return 0;
        } else {
            // The game finished, we can instead return the actual result of the game for the given player.
            return getPlayerResults()[playerId].value;
        }
    }

    /**
     * @param playerId - player observing the state.
     * @return the true score for the player, according to the game rules. May be 0 if there is no score in the game.
     */
    @Override
    public double getGameScore(int playerId) {
        // TODO: What is this player's score (if any)?
        return 0;
    }




    @Override
    public boolean _equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EverdellGameState that = (EverdellGameState) o;
        return Objects.equals(cardDeck, that.cardDeck) && Objects.equals(meadowDeck, that.meadowDeck) && Objects.equals(playerHands, that.playerHands) && Objects.equals(playerVillage, that.playerVillage) && deepEquals(currentSeason, that.currentSeason) && Objects.equals(Locations, that.Locations) && Objects.equals(PlayerResources, that.PlayerResources) && deepEquals(cardCount, that.cardCount) && deepEquals(workers, that.workers) && deepEquals(pointTokens, that.pointTokens) && Objects.equals(currentCard, that.currentCard) && Objects.equals(resourceSelection, that.resourceSelection) && Objects.equals(cardSelection, that.cardSelection);
    }

    @Override
    public int hashCode() {
        return hash(cardDeck, meadowDeck, playerHands, playerVillage, Arrays.hashCode(currentSeason), Locations, PlayerResources, Arrays.hashCode(cardCount), Arrays.hashCode(workers), Arrays.hashCode(pointTokens), currentCard, resourceSelection, cardSelection);
    }


    // TODO: Consider the methods below for possible implementation
    // TODO: These all have default implementations in AbstractGameState, so are not required to be implemented here.
    // TODO: If the game has 'teams' that win/lose together, then implement the next two nethods.
    /**
     * Returns the number of teams in the game. The default is to have one team per player.
     * If the game does not have 'teams' that win/lose together, then ignore these two methods.
     */
   // public int getNTeams();
    /**
     * Returns the team number the specified player is on.
     */
    //public int getTeam(int player);

    // TODO: If your game has multiple special tiebreak options, then implement the next two methods.
    // TODO: The default is to tie-break on the game score (if this is the case, ignore these)
    // public double getTiebreak(int playerId, int tier);
    // public int getTiebreakLevels();


    // TODO: If your game does not have a score of any type, and is an 'insta-win' type game which ends
    // TODO: as soon as a player achieves a winning condition, and has some bespoke method for determining 1st, 2nd, 3rd etc.
    // TODO: Then you *may* want to implement:.
    //public int getOrdinalPosition(int playerId);

    // TODO: Review the methods below...these are all supported by the default implementation in AbstractGameState
    // TODO: So you do not (and generally should not) implement your own versions - take advantage of the framework!
    // public Random getRnd() returns a Random number generator for the game. This will be derived from the seed
    // in game parameters, and will be updated correctly on a reset

    // Ths following provide access to the id of the current player; the first player in the Round (if that is relevant to a game)
    // and the current Turn and Round numbers.
    // public int getCurrentPlayer()
    // public int getFirstPlayer()
    // public int getRoundCounter()
    // public int getTurnCounter()
    // also make sure you check out the standard endPlayerTurn() and endRound() methods in StandardForwardModel

    // This method can be used to log a game event (e.g. for something game-specific that you want to include in the metrics)
    // public void logEvent(IGameEvent...)
}
