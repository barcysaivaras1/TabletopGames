package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Component;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.Seasons;
import games.everdell.components.ConstructionCard;
import games.everdell.components.CritterCard;
import games.everdell.components.EverdellCard;
import games.everdell.components.EverdellLocation;
import org.apache.spark.sql.sources.In;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MoveSeason extends AbstractAction implements IExtendedSequence{

    private ArrayList<Integer> cardSelectionID;

    private final int playerID;

    private boolean executed;

    private boolean clocktowerEvent = false;


    //String printout value
    private String seasonName;

    public MoveSeason(ArrayList<Integer> cardSelection, int playerID) {
        this.cardSelectionID = cardSelection;
        this.playerID = playerID;
    }

    public MoveSeason(ArrayList<Integer> cardSelection, int playerID, boolean clocktowerEvent) {
        this.cardSelectionID = cardSelection;
        this.playerID = playerID;
        this.clocktowerEvent = clocktowerEvent;
    }

    //This will need to have unique effects implemented each season


    //Spring, Production event and +1 max workers
    //Summer, Draw 2 from the meadow and +1 max workers
    //Autumn, Production event and +2 max workers
    @Override
    public boolean execute(AbstractGameState gs) {
        // TODO: Some functionality applied which changes the given game state.
        System.out.println("MoveSeason: execute");

        EverdellGameState state = (EverdellGameState) gs;

        //Check if the player has clocktower card
        for(var card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){
            if(card.getCardEnumValue() == EverdellParameters.CardDetails.CLOCK_TOWER){
                clocktowerEvent = true;
                break;
            }
        }


        ArrayList<EverdellCard> cardSelection = new ArrayList<>();
        for (var id : cardSelectionID){
            cardSelection.add((EverdellCard) state.getComponentById(id));
        }

        if (state.workers[state.getCurrentPlayer()].getValue() == 0 && state.currentSeason[state.getCurrentPlayer()] != Seasons.AUTUMN) {
            state.cardSelection = cardSelection;
            Seasons currentSeason = state.currentSeason[state.getCurrentPlayer()];
            Seasons newSeason = Seasons.values()[(currentSeason.ordinal() + 1) % Seasons.values().length];
            state.currentSeason[state.getCurrentPlayer()] = newSeason;

            //Increment workers
            //Autumn has a special case of incrementing by 2
            switch (newSeason) {
                case SPRING:
                    //Production event
                    state.workers[state.getCurrentPlayer()].increment();
                    productionEvent(state);
                    break;
                case SUMMER:
                    //Draw 2 from the meadow
                    state.workers[state.getCurrentPlayer()].increment();
                    summerEvent(state);
                    break;
                case AUTUMN:
                    //Production event
                    state.workers[state.getCurrentPlayer()].increment(2);
                    productionEvent(state);
                    break;
            }

                //Bring back all workers
                for (EverdellLocation location : state.everdellLocations) {
                    //Monastery and Cemetery Card has a special case where the worker is not returned
                    if(location.getAbstractLocation() == EverdellParameters.RedDestinationLocation.MONASTERY_DESTINATION || location.getAbstractLocation() == EverdellParameters.RedDestinationLocation.CEMETERY_DESTINATION){
                        continue;
                    }

                    //If no players are on the location, skip
                    if (location.playersOnLocation.isEmpty()) continue;

                    if(location.getAbstractLocation() instanceof EverdellParameters.BasicEvent){
                        state.workers[state.getCurrentPlayer()].increment();
                        continue;
                    }

                    System.out.println("Players on Location : "+ location.playersOnLocation);
                    //If player is on the location, remove them and increment their workers
                    if(location.playersOnLocation.contains(state.getCurrentPlayer())){
                        location.playersOnLocation.remove((Integer)state.getCurrentPlayer());
                        state.workers[state.getCurrentPlayer()].increment();
                    }
                }

                return true;
            }
            return false;
    }

    public void productionEvent(EverdellGameState state){
        //AI Green Production
        if(!cardSelectionID.isEmpty()){
            System.out.println("AI Green Production");
            state.setActionInProgress(this);
            state.greenProductionMode = true;
            //Generate a list of green production cards that need to be activated
            for(var id : cardSelectionID){
                state.greenProductionCards.add((EverdellCard) state.getComponentById(id));
            }

            return;
        }

        //GUI Green Production
        System.out.println("Production Event");
        //Iterate through all players
        for(int i = 0; i<state.getNPlayers(); i++){
            //Check if player has a production building
            for(var card : state.playerVillage.get(i).getComponents()){
                if(card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION){
                    System.out.println("Green Production, In MoveSeason, Activating Card : "+card.getName());
                    //Apply production effect
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

    private void summerEvent(EverdellGameState state){
        //Take cards from meadow and place in player hand
        //It is assumed that the player has space in their hand

        ArrayList<EverdellCard> cardSelection = new ArrayList<>();
        for (var id : cardSelectionID){
            cardSelection.add((EverdellCard) state.getComponentById(id));
        }
        for(var c : cardSelection){
            state.playerHands.get(state.getCurrentPlayer()).add(c);
            state.cardCount[state.getCurrentPlayer()].increment();
            state.meadowDeck.remove(c);
            state.meadowDeck.add(state.cardDeck.draw());
        }
    }


    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        //What is there to select?
        /* -If it's summer the player must select cards to gain from the meadow, can be done using selectAListOfCards
        *  -Need to handle green production events here. If its green production we handle this here. If its summer
        * we handle it kinda here. We need to handle cards that activate just before moving seasons, i.e Clocktower(might be the only one :)))
        *  - Could worry about selecting order of activation for green production. Could also just ignore it as it might be quite annoying to implement
        * */
        //But what actions am i selecting from here? The actions are already defined by which season we are in.
        //When we move seasons we either 1, draw cards from the meadow or 2, activate green production cards
        //WE can do green production here, but we need to handle the meadow drawing in the selectAListOfCards action
        //The only actions that we can compute would be to decide which order would we activate the green production cards
        //We will probably have to store a list of green production cards in the game state and then select the order of activation
        //We can also use a boolean to check if we should activate green production. SelectCard could then behave differently depending
        //on the boolean value, false would be standard behaviours and true would be green production behaviours.
        ArrayList<AbstractAction> actions = new ArrayList<>();
        actions.add(new SelectCard(playerID, -1, cardSelectionID));
        return actions;
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerID;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        executed = true;
        return;

    }

    @Override
    public boolean executionComplete(AbstractGameState state) {
        return executed;
    }

    /**
     * @return Make sure to return an exact <b>deep</b> copy of the object, including all of its variables.
     * Make sure the return type is this class (e.g. GTAction) and NOT the super class AbstractAction.
     * <p>If all variables in this class are final or effectively final (which they should be),
     * then you can just return <code>`this`</code>.</p>
     */
    @Override
    public MoveSeason copy() {
        // TODO: copy non-final variables appropriately
        ArrayList<Integer> cardSelectionID = new ArrayList<>(this.cardSelectionID);
        MoveSeason ms = new MoveSeason(cardSelectionID, playerID);
        ms.executed = executed;
        return ms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoveSeason that = (MoveSeason) o;
        return executed == that.executed && Objects.equals(cardSelectionID, that.cardSelectionID) && Objects.equals(seasonName, that.seasonName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardSelectionID, executed, seasonName);
    }

    @Override
    public String toString() {
        // TODO: Replace with appropriate string, including any action parameters
        return "Moving to Season : "+seasonName;
    }

    /**
     * @param gameState - game state provided for context.
     * @return A more descriptive alternative to the toString action, after access to the game state to e.g.
     * retrieve components for which only the ID is stored on the action object, and include the name of those components.
     * Optional.
     */
    @Override
    public String getString(AbstractGameState gameState) {
        seasonName = ((EverdellGameState) gameState).currentSeason[((EverdellGameState) gameState).getCurrentPlayer()].toString();
        return toString();
    }


    /**
     * This next one is optional.
     *
     *  May optionally be implemented if Actions are not fully visible
     *  The only impact this has is in the GUI, to avoid this giving too much information to the human player.
     *
     *  An example is in Resistance or Sushi Go, in which all cards are technically revealed simultaneously,
     *  but the game engine asks for the moves sequentially. In this case, the action should be able to
     *  output something like "Player N plays card", without saying what the card is.
     * @param gameState - game state to be used to generate the string.
     * @param playerId - player to whom the action should be represented.
     * @return
     */
    // @Override
    // public String getString(AbstractGameState gameState, int playerId);
}
