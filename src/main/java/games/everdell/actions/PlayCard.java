package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Component;
import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.*;
import games.everdell.EverdellParameters.CardDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
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

    //private EverdellCard currentCard;

    private int currentCardID;

    private ArrayList<Integer> cardSelectionID;
    private HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues;




    public PlayCard(int cardID, ArrayList<Integer> cardSelectionID, HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues){
//        currentCard = card;
        this.cardSelectionID = new ArrayList<>(cardSelectionID);
        this.resourceSelectionValues = resourceSelectionValues;
        currentCardID = cardID;
    }


    @Override
    public boolean execute(AbstractGameState gs) {
        // TODO: Some functionality applied which changes the given game state.
        EverdellGameState state = (EverdellGameState) gs;

        System.out.println("Play Card Resources : "+resourceSelectionValues);


        EverdellCard currentCard = (EverdellCard) state.getComponentById(currentCardID);
        ArrayList<EverdellCard> cardSelection = new ArrayList<>();
        for(var cardID : cardSelectionID){
            cardSelection.add((EverdellCard) state.getComponentById(cardID));
        }
        HashMap<EverdellParameters.ResourceTypes, Counter> resourceSelection = state.resourceSelection;
        for(var resource : resourceSelectionValues.keySet()){
            resourceSelection.get(resource).setValue(resourceSelectionValues.get(resource));
        }
        System.out.println("Card ID : "+currentCardID);

        if(currentCard instanceof FoolCard){
            //Fool Card has a special case where the player must select a player to give the card to
            return foolSpecialTreatment(state);
        }

        //Only working for the first player, 0 values need to be updated to be playerTurn
        if(state.playerVillage.get(state.getCurrentPlayer()).getSize() < state.villageMaxSize[state.getCurrentPlayer()].getValue()){
            state.currentCard = currentCard;
            state.cardSelection = cardSelection;
//            if(state.playerVillage.get(state.getCurrentPlayer()).contains(currentCard)){
//                System.out.println("You already have this card in your village");
//                return false;
//            }

            //Check if the card is Unique and if the player has this card in their village
            //Cannot have duplicate unique cards
            if(!checkIfPlayerCanPlaceThisUniqueCard(state)){
                System.out.println("You already have this Unique card in your village");
                return false;
            }



            //Check if the player can buy the card
            if(!checkIfPlayerCanBuyCard(state)){
                System.out.println("You don't have enough resources to buy this card");
                return false;
            }

            //Make the player pay for the resources, it hasn't been paid for yet (via occupation)
            if(!currentCard.isCardPayedFor()){
                makePlayerPayForCard(state);
                currentCard.payForCard();
            }

            //Add Card to village

            state.playerVillage.get(state.getCurrentPlayer()).add(currentCard);

            //Remove Card
            removeCard(state);


            System.out.println("TRIGGER");
            //Apply Card Effect
            triggerCardEffect(state, currentCard);


            checkForCardsThatNeedToActivateAfterPlayingACard(state);

            state.cardSelection.clear();
            for(var resource : state.resourceSelection.keySet()){
                state.resourceSelection.put(resource, new Counter());
            }

            //System.out.println("This is the max village size "+state.villageMaxSize[state.getCurrentPlayer()].getValue());;

            return true;
        }
        System.out.println("Cannot place card, village is full");
        return false;
    }

    public void triggerCardEffect(EverdellGameState state, EverdellCard currentCard){
        //If the card is already in the village, we will assume we want to trigger the card effect
        ArrayList<EverdellCard> cardSelection = new ArrayList<>();
        for(var cardID : cardSelectionID){
            cardSelection.add((EverdellCard) state.getComponentById(cardID));
        }
        HashMap<EverdellParameters.ResourceTypes, Counter> resourceSelection = state.resourceSelection;
        for(var resource : resourceSelectionValues.keySet()){
            resourceSelection.get(resource).setValue(resourceSelectionValues.get(resource));
        }


        state.currentCard = (EverdellCard) state.getComponentById(currentCard.getComponentID());
        state.cardSelection = cardSelection;
        state.resourceSelection = resourceSelection;

        System.out.println("Triggering Card Effect");
        System.out.println("Card Selected : "+currentCard.getName());
        System.out.println("Card is Construction : "+currentCard.isConstruction());

        if(state.currentCard instanceof ConstructionCard cc){
            System.out.println("Construction Card");
            cc.applyCardEffect(state);
        }
        else{
            CritterCard cc = (CritterCard) state.currentCard;
            cc.applyCardEffect(state);
        }
    }


    public ArrayList<EverdellCard> canPayWithOccupation(EverdellGameState state, EverdellCard cardToCheck){

        //Can the card occupy a Construction Card
        ArrayList<EverdellCard> cardsThatCanOccupy = new ArrayList<>();

        for(EverdellCard c : state.playerVillage.get(state.getCurrentPlayer())) {
            //Only Construction cards can occupy
            if (c instanceof ConstructionCard) {
                //Can the card occupy this construction
                if(((ConstructionCard) c).canCardOccupyThis(state, cardToCheck)){
                    System.out.println("A card can occupy this");
                    cardsThatCanOccupy.add(c);
                }
            }
        }

        return cardsThatCanOccupy;
    }
    private void removeCard(EverdellGameState state){
        //Remove card from hand
        //If we fail to remove that card object from the hand, it means that the card was in the meadow
        //We remove the card from the meadow and add a new card to the meadow

        EverdellCard currentCard = (EverdellCard) state.getComponentById(currentCardID);

        System.out.println("Removing card");
        System.out.println("Card Selected : "+currentCard.getName());


        if(!state.playerHands.get(state.getCurrentPlayer()).remove(currentCard)){
            state.meadowDeck.remove(currentCard);
            if(state.meadowDeck.getSize() < state.meadowDeck.getCapacity()) {
                System.out.println("Card was in the meadow");
                state.meadowDeck.add(state.cardDeck.draw());
            }
        }
        //We played the card from our hand
        else{
            //Decrement Card counter
            state.cardCount[state.getCurrentPlayer()].decrement();
        }
    }

    private Boolean checkForCardsThatNeedToActivateAfterPlayingACard(EverdellGameState state){
        //Check if the card we played has any cards that need to be activated after playing a card
        //This is for cards that do NOT need GUI elements to be function
        //There is a separate function for cards that need GUI elements to function in the GUIManager

        for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){

            if(card.getCardEnumValue() == CardDetails.SHOP_KEEPER){
                //Trigger Shop keeper effect
                triggerCardEffect(state, card);
                return true;
            }
            if(card.getCardEnumValue() == CardDetails.HISTORIAN){
                System.out.println("Historian");
                //Trigger Historian effect
                triggerCardEffect(state, card);
                return true;
            }
            if(card.getCardEnumValue() == CardDetails.CASTLE){
                //Trigger Castle effect
                triggerCardEffect(state, card);
                return true;
            }
            if(card.getCardEnumValue() == CardDetails.PALACE){
                //Trigger Palace effect
                triggerCardEffect(state, card);
                return true;
            }
            if(card.getCardEnumValue() == CardDetails.THEATRE){
                //Trigger Theatre effect
                triggerCardEffect(state, card);
                return true;
            }
            if(card.getCardEnumValue() == CardDetails.SCHOOL){
                //Trigger School effect
                triggerCardEffect(state, card);
                return true;
            }
        }
        return false;
    }

    private Boolean checkIfPlayerCanPlaceThisUniqueCard(EverdellGameState state){
        //Check if the player has this Unique card in their village
        EverdellCard currentCard = (EverdellCard) state.getComponentById(currentCardID);
        if(currentCard.isUnique()){
            for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){
                if(card.getCardEnumValue() == currentCard.getCardEnumValue()){
                    return false;
                }
            }
        }
        return true;
    }

    public Boolean checkIfPlayerCanBuyCard(EverdellGameState state){
        //Check if the player has enough resources to buy the card
        System.out.println("Current Card ID : "+currentCardID);
        System.out.println("Current Card : "+state.getComponentById(currentCardID));
        EverdellCard currentCard = (EverdellCard) state.getComponentById(currentCardID);

        //The card can be paid with occupation.
        if(currentCard.isCardPayedFor()){
            return true;
        }
        for(var resource : currentCard.getResourceCost().keySet()){
            if(state.PlayerResources.get(resource)[state.getCurrentPlayer()].getValue() < currentCard.getResourceCost().get(resource)){
                return false;
            }
        }

        return true;
    }

    private void makePlayerPayForCard(EverdellGameState state){
        //Make the player pay for the resources
        EverdellCard currentCard = (EverdellCard) state.getComponentById(currentCardID);
        for(var resource : currentCard.getResourceCost().keySet()){
            state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement(currentCard.getResourceCost().get(resource));
        }
    }

    private Boolean foolSpecialTreatment(EverdellGameState state){
        //Fool Card has a special case where the player must select a player to give the card to
        //Check if the card is Unique and if the player has this card in their village
        //Cannot have duplicate unique cards
        EverdellCard currentCard = (EverdellCard) state.getComponentById(currentCardID);
        FoolCard foolCard = (FoolCard) currentCard;

        if(state.playerVillage.get(foolCard.getSelectedPlayer()).stream().anyMatch(card -> card.getCardEnumValue() == CardDetails.FOOL)){
            System.out.println("Fool is already in this village");
            return false;
        }

        //Check if the player can buy the card
        if(!checkIfPlayerCanBuyCard(state)){
            System.out.println("You don't have enough resources to buy this card");
            return false;
        }

        //Make the player pay for the resources, it hasn't been paid for yet (via occupation)
        if(!currentCard.isCardPayedFor()){
            makePlayerPayForCard(state);
            currentCard.payForCard();
        }
        //Remove Card
        removeCard(state);
        //Apply Card Effect
        triggerCardEffect(state, currentCard);

        System.out.println("You have placed a card");
        return true;
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
        ArrayList<Integer> csID = new ArrayList<>(cardSelectionID);
        HashMap<EverdellParameters.ResourceTypes, Integer> rsID = new HashMap<>(resourceSelectionValues);
        return new PlayCard(currentCardID, csID, rsID);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlayCard playCard = (PlayCard) o;
        return currentCardID == playCard.currentCardID && Objects.equals(cardSelectionID, playCard.cardSelectionID) && Objects.equals(resourceSelectionValues, playCard.resourceSelectionValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCardID, cardSelectionID, resourceSelectionValues);
    }

    @Override
    public String toString() {
        // TODO: Replace with appropriate string, including any action parameters
        return "Placing Card : " + currentCardID;
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
