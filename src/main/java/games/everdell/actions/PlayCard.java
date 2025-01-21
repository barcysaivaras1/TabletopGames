package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Component;
import games.everdell.EverdellGameState;
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters.CardDetails;

import java.util.Random;

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
public class PlayCard extends AbstractAction {

    /**
     * Executes this action, applying its effect to the given game state. Can access any component IDs stored
     * through the {@link AbstractGameState#getComponentById(int)} method.
     * @param gs - game state which should be modified by this action.
     * @return - true if successfully executed, false otherwise.
     */

    private EverdellCard currentCard;

    public PlayCard(EverdellCard card){
        currentCard = card;
    }


    @Override
    public boolean execute(AbstractGameState gs) {
        // TODO: Some functionality applied which changes the given game state.
        EverdellGameState state = (EverdellGameState) gs;

        state.currentCard = this.currentCard;

        //Only working for the first player, 0 values need to be updated to be playerTurn
        if(state.cardCount[state.playerTurn].getValue() > 0 && state.playerVillage.get(state.playerTurn).getSize() < 15){
            state.cardSelection.clear();


            //Check if the card is Unique and if the player has this card in their village
            //Cannot have duplicate unique cards
            if(!checkIfPlayerCanPlaceThisUniqueCard(state)){
                System.out.println("You already have this Unique card in your village");
                return false;
            }


            //Check if the player can buy the card
            if(!checkIfPlayerCanBuyCard(state) && !state.currentCard.isCardPayedFor()){
                System.out.println("You don't have enough resources to buy this card");
                return false;
            }

            //Make the player pay for the resources, it hasn't been paid for yet (via occupation)
            if(!state.currentCard.isCardPayedFor()){
                makePlayerPayForCard(state);
                state.currentCard.payForCard();
            }

            //Add Card to village
            state.playerVillage.get(state.playerTurn).add(state.currentCard);

            //Remove card from hand
            //If we fail to remove that card object from the hand, it means that the card was in the meadow
            //We remove the card from the meadow and add a new card to the meadow
            if(!state.playerHands.get(state.playerTurn).remove(state.currentCard)){
                state.meadowDeck.remove(state.currentCard);
                state.meadowDeck.add(state.cardDeck.draw());
            }
            //We played the card from our hand
            else{
                //Decrement Card counter
                state.cardCount[state.playerTurn].decrement();
            }



            //Apply Card Effect
            state.currentCard.applyCardEffect(state);
            checkForCardsThatNeedToActivateAfterPlayingACard(state);
            System.out.println("You have placed a card");
            return true;
        }
        System.out.println("You have no cards left");
        return false;
    }

    private Boolean checkForCardsThatNeedToActivateAfterPlayingACard(EverdellGameState state){
        //Check if the card we played has any cards that need to be activated after playing a card
        //This is for cards that do NOT need GUI elements to be function
        //There is a separate function for cards that need GUI elements to function in the GUIManager

        for(EverdellCard card : state.playerVillage.get(state.playerTurn).getComponents()){
            if(card.getCardEnumValue() == CardDetails.SHOP_KEEPER){
                //Trigger Shop keeper effect
                card.applyCardEffect(state);
                return true;
            }
            if(card.getCardEnumValue() == CardDetails.CASTLE){
                //Trigger Castle effect
                card.applyCardEffect(state);
                return true;
            }
            if(card.getCardEnumValue() == CardDetails.PALACE){
                //Trigger Palace effect
                card.applyCardEffect(state);
                return true;
            }
            if(card.getCardEnumValue() == CardDetails.THEATRE){
                //Trigger Theatre effect
                card.applyCardEffect(state);
                return true;
            }
            if(card.getCardEnumValue() == CardDetails.SCHOOL){
                //Trigger School effect
                card.applyCardEffect(state);
                return true;
            }
        }
        return false;
    }

    private Boolean checkIfPlayerCanPlaceThisUniqueCard(EverdellGameState state){
        //Check if the player has this Unique card in their village
        if(state.currentCard.isUnique()){
            for(EverdellCard card : state.playerVillage.get(state.playerTurn).getComponents()){
                if(card.getCardEnumValue() == state.currentCard.getCardEnumValue()){
                    return false;
                }
            }
        }
        return true;
    }

    private Boolean checkIfPlayerCanBuyCard(EverdellGameState state){
        //Check if the player has enough resources to buy the card
        for(var resource : state.currentCard.getResourceCost().keySet()){
            if(state.PlayerResources.get(resource)[state.playerTurn].getValue() < state.currentCard.getResourceCost().get(resource)){
                return false;
            }
        }

        return true;
    }

    private void makePlayerPayForCard(EverdellGameState state){
        //Make the player pay for the resources
        for(var resource : state.currentCard.getResourceCost().keySet()){
            state.PlayerResources.get(resource)[state.playerTurn].decrement(state.currentCard.getResourceCost().get(resource));
        }
    }

    /**
     * @return Make sure to return an exact <b>deep</b> copy of the object, including all of its variables.
     * Make sure the return type is this class (e.g. GTAction) and NOT the super class AbstractAction.
     * <p>If all variables in this class are final or effectively final (which they should be),
     * then you can just return <code>`this`</code>.</p>
     */
    @Override
    public PlayCard copy() {
        // TODO: copy non-final variables appropriately
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        // TODO: compare all other variables in the class
        return obj instanceof EverdellAction;
    }

    @Override
    public int hashCode() {
        // TODO: return the hash of all other variables in the class
        return 0;
    }

    @Override
    public String toString() {
        // TODO: Replace with appropriate string, including any action parameters
        return "My action name";
    }

    /**
     * @param gameState - game state provided for context.
     * @return A more descriptive alternative to the toString action, after access to the game state to e.g.
     * retrieve components for which only the ID is stored on the action object, and include the name of those components.
     * Optional.
     */
    @Override
    public String getString(AbstractGameState gameState) {
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
