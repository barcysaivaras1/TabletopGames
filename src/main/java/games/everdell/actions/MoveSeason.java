package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Component;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.Seasons;
import games.everdell.components.ConstructionCard;
import games.everdell.components.CritterCard;
import games.everdell.components.EverdellCard;
import games.everdell.components.EverdellLocation;
import org.apache.spark.sql.sources.In;

import java.util.ArrayList;
import java.util.Objects;

/**
 * <p>Actions are unit things players can do in the game (e.g. play a card, move a pawn, roll dice, attack etc.).</p>
 * <p>Actions in the game can (and should, if applicable) extend one of the other existing actions, in package {@link core.actions}.
 * Or, a game may simply reuse one of the existing core actions.</p>
 * <p>Actions may have parameters, so as not to duplicate actions for the same type of functionality,
 * e.g. playing card of different types (see {@link games.sushigo.actions.ChooseCard} action from SushiGo as an example).
 * Include these parameters in the class constructor.</p>
 * <p>They need to extend at a minimum the {@link AbstractAction} super class and implement the {@link AbstractAction#execute(AbstractGameState)} method.
 * This is where the main functionality of the action should be inserted, which modifies the given game state appropriately (e.g. if the action is to play a card,
 * then the card will be moved from the player's hand to the discard pile, and the card's effect will be applied).</p>
 * <p>They also need to include {@link Object#equals(Object)} and {@link Object#hashCode()} methods.</p>
 * <p>They <b>MUST NOT</b> keep references to game components. Instead, store the {@link Component#getComponentID()}
 * in variables for any components that must be referenced in the action. Then, in the execute() function,
 * use the {@link AbstractGameState#getComponentById(int)} function to retrieve the actual reference to the component,
 * given your componentID.</p>
 */
public class MoveSeason extends AbstractAction {

    /**
     * Executes this action, applying its effect to the given game state. Can access any component IDs stored
     * through the {@link AbstractGameState#getComponentById(int)} method.
     * @param gs - game state which should be modified by this action.
     * @return - true if successfully executed, false otherwise.
     */

    private ArrayList<Integer> cardSelectionID;


    //String printout value
    private String seasonName;

    public MoveSeason(ArrayList<Integer> cardSelection){
        this.cardSelectionID = cardSelection;
    }

    //This will need to have unique effects implemented each season


    //Spring, Production event and +1 max workers
    //Summer, Draw 2 from the meadow and +1 max workers
    //Autumn, Production event and +2 max workers
    @Override
    public boolean execute(AbstractGameState gs) {
        // TODO: Some functionality applied which changes the given game state.

        EverdellGameState state = (EverdellGameState) gs;

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

    public void summerEvent(EverdellGameState state){
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


    /**
     * @return Make sure to return an exact <b>deep</b> copy of the object, including all of its variables.
     * Make sure the return type is this class (e.g. GTAction) and NOT the super class AbstractAction.
     * <p>If all variables in this class are final or effectively final (which they should be),
     * then you can just return <code>`this`</code>.</p>
     */
    @Override
    public MoveSeason copy() {
        // TODO: copy non-final variables appropriately
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MoveSeason that = (MoveSeason) o;
        return Objects.equals(cardSelectionID, that.cardSelectionID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cardSelectionID);
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
