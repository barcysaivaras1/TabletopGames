package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Counter;
import core.components.Component;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.BasicEvent;
import games.everdell.components.CritterCard;
import games.everdell.components.EverdellCard;
import games.everdell.components.EverdellLocation;
import games.everdell.gui.EverdellGUIManager;
import org.apache.spark.sql.sources.In;
import scala.Int;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
public class PlaceWorker extends AbstractAction implements IExtendedSequence{

    /**
     * Executes this action, applying its effect to the given game state. Can access any component IDs stored
     * through the {@link AbstractGameState#getComponentById(int)} method.
     * @param gs - game state which should be modified by this action.
     * @return - true if successfully executed, false otherwise.
     */
    //private EverdellParameters.AbstractLocations locationToPlaceIn;
    private int playerId;
    private int locationComponentID;
    private String locationName;
    private ArrayList<Integer> cardSelectionID;
    private HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues;

    public PlaceWorker(int playerId, int location, ArrayList<Integer> cardSelectionID, HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelection) {
        this.playerId = playerId;
        locationComponentID = location;
        this.cardSelectionID = cardSelectionID;
        this.resourceSelectionValues = resourceSelection;
    }



    @Override
    public boolean execute(AbstractGameState gs) {
        // TODO: Some functionality applied which changes the given game state.
        EverdellGameState state = (EverdellGameState) gs;
        EverdellLocation locationToPlaceIn = ((EverdellLocation) state.getComponentById(locationComponentID));


        //Check if this location is free
        if(state.workers[state.getCurrentPlayer()].getValue() > 0 && locationToPlaceIn.isLocationFreeForPlayer(gs)){
            System.out.println("Placing Worker in : " + locationToPlaceIn);


            state.cardSelection = new ArrayList<>();
            for(var cardId : cardSelectionID){
                state.cardSelection.add((EverdellCard) state.getComponentById(cardId));
            }


            HashMap<EverdellParameters.ResourceTypes, Counter> resourceSelection = state.resourceSelection;
            for(var resource : resourceSelectionValues.keySet()){
                resourceSelection.get(resource).setValue(resourceSelectionValues.get(resource));
            }

            //Check if we meet the requirements for the basic event
            if(locationToPlaceIn.getAbstractLocation() instanceof EverdellParameters.BasicEvent be){
                if(!BasicEvent.defaultCheckIfConditionMet(state, be)){
                    return false;
                }
                for(var card : state.playerVillage.get(state.getCurrentPlayer())){
                    //If there is a King Card Present we must apply the effect after a basic Event claim
                    if(card.getCardEnumValue() == EverdellParameters.CardDetails.KING){
                        CritterCard kingCard = (CritterCard) card;
                        kingCard.applyCardEffect(state);
                    }
                }
            }

            state.workers[state.getCurrentPlayer()].decrement();
            //EverdellLocation everdellLocation = state.Locations.get(locationToPlaceIn);
            locationToPlaceIn.applyLocationEffect(state);
            locationToPlaceIn.playersOnLocation.add(((EverdellGameState) gs).getCurrentPlayer());


            //Reset the resource selection
            state.resourceSelection.keySet().forEach(resource -> state.resourceSelection.get(resource).setValue(0));
            //Reset Card Selection
            state.cardSelection = new ArrayList<>();

            //AI PLAY
            if(locationToPlaceIn.getAbstractLocation() == EverdellParameters.RedDestinationLocation.QUEEN_DESTINATION){
                new SelectCard(playerId, cardSelectionID.get(0), new ArrayList<>()).execute(state);
            }

            return true;
        }

        return false;
    }

    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        return List.of();
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        return;
    }

    @Override
    public boolean executionComplete(AbstractGameState state) {
        return true;
    }

    /**
     * @return Make sure to return an exact <b>deep</b> copy of the object, including all of its variables.
     * Make sure the return type is this class (e.g. GTAction) and NOT the super class AbstractAction.
     * <p>If all variables in this class are final or effectively final (which they should be),
     * then you can just return <code>`this`</code>.</p>
     */
    @Override
    public PlaceWorker copy() {
        // TODO: copy non-final variables appropriately
        ArrayList<Integer> cardSelection = new ArrayList<>(this.cardSelectionID);
        HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues = new HashMap<>(this.resourceSelectionValues);
        return new PlaceWorker(playerId, locationComponentID, cardSelection, resourceSelectionValues);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlaceWorker that = (PlaceWorker) o;
        return playerId == that.playerId && locationComponentID == that.locationComponentID && Objects.equals(locationName, that.locationName) && Objects.equals(cardSelectionID, that.cardSelectionID) && Objects.equals(resourceSelectionValues, that.resourceSelectionValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, locationComponentID, locationName, cardSelectionID, resourceSelectionValues);
    }

    @Override
    public String toString() {
        // TODO: Replace with appropriate string, including any action parameters
        return "Place Worker in Location : " + locationName;
    }

    /**
     * @param gameState - game state provided for context.
     * @return A more descriptive alternative to the toString action, after access to the game state to e.g.
     * retrieve components for which only the ID is stored on the action object, and include the name of those components.
     * Optional.
     */
    @Override
    public String getString(AbstractGameState gameState) {

        locationName = ""+((EverdellLocation) gameState.getComponentById(locationComponentID)).getAbstractLocation();
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
