package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Component;
import core.components.Counter;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.*;
import games.everdell.EverdellParameters.CardDetails;
import utilities.Hash;

import java.util.*;

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
public class PlayCard extends AbstractAction implements IExtendedSequence{

    /**
     * Executes this action, applying its effect to the given game state. Can access any component IDs stored
     * through the {@link AbstractGameState#getComponentById(int)} method.
     * @param gs - game state which should be modified by this action.
     * @return - true if successfully executed, false otherwise.
     */

    //private EverdellCard currentCard;

    private boolean executed;

    private int currentCardID;

    private ArrayList<Integer> cardSelectionID;
    private HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues;

    private int playerId;




    public PlayCard(int playerId, int cardID, ArrayList<Integer> cardSelectionID, HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues){
        this.cardSelectionID = new ArrayList<>(cardSelectionID);
        this.resourceSelectionValues = resourceSelectionValues;
        currentCardID = cardID;
        this.playerId = playerId;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        // TODO: Some functionality applied which changes the given game state.
        EverdellGameState state = (EverdellGameState) gs;

        System.out.println("Executing Play Card Action");
        resetValues(state);

        EverdellCard currentCard = (EverdellCard) state.getComponentById(currentCardID);
        state.currentCard = currentCard;
        ArrayList<EverdellCard> cardSelection = new ArrayList<>();
        System.out.println("Card Selection ID in playcard: " + cardSelectionID);
        for(var cardID : cardSelectionID){
            cardSelection.add((EverdellCard) state.getComponentById(cardID));
        }
        HashMap<EverdellParameters.ResourceTypes, Counter> resourceSelection = new HashMap<>();
        for(var resource : resourceSelectionValues.keySet()){
            resourceSelection.put(resource, new Counter());
            resourceSelection.get(resource).setValue(resourceSelectionValues.get(resource));
        }
        for(var resource : resourceSelection.keySet()){
            state.resourceSelection.get(resource).setValue(resourceSelection.get(resource).getValue());
        }

        //Fool Card has a special case where the player must select a player to give the card to
        if(currentCard instanceof FoolCard){
            return foolSpecialTreatment(state);
        }

        //AI COPYMODE PLAY
        if(state.copyMode){
            Component comp = state.getComponentById(state.copyID);
            if(comp instanceof CopyCard cc){
                if(cc.getCardEnumValue() == CardDetails.CHIP_SWEEP){
                    cc.setCardToCopy(currentCard);
                }
                if(cc.getCardEnumValue() == CardDetails.MINER_MOLE){
                    cc.setCardToCopy(currentCard);
                }
                currentCard = cc;
                currentCardID = cc.getComponentID();
                state.copyID = -1;
                state.copyMode = false;
            }
        }

        //AI Green Production
        if(state.greenProductionMode) {
            System.out.println("Green Production Mode in PLAYCARD");
            EverdellCard greenCard = state.greenProductionCards.get(0);
            triggerCardEffect(state, greenCard);
            state.greenProductionCards.remove(0);

            if(state.greenProductionCards.isEmpty()){
                state.greenProductionMode = false;
                return true;
            }

            ArrayList<Integer> greenIds = new ArrayList<>();
            for(var card : state.greenProductionCards){
                greenIds.add(card.getComponentID());
            }

            new SelectCard(playerId, -1, greenIds).execute(state);
            return true;
        }

        //Only working for the first player, 0 values need to be updated to be playerTurn
        if(checkIfVillageHasSpace(state, state.getCurrentPlayer())){
            state.currentCard = currentCard;
            state.cardSelection = new ArrayList<>(cardSelection);


            //Check if the card is Unique and if the player has this card in their village
            //Cannot have duplicate unique cards
            if(!currentCard.checkIfPlayerCanPlaceThisUniqueCard(state, state.getCurrentPlayer())){
                System.out.println("You already have this Unique card in your village");
                return false;
            }


            System.out.println("Current Card is : " + currentCard.getCardEnumValue());
            System.out.println("Is current card paid for : " + currentCard.isCardPayedFor());
            //Check if the player can buy the card
            if(!currentCard.checkIfPlayerCanBuyCard(state, state.getCurrentPlayer())){
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


            //Apply Card Effect
            triggerCardEffect(state, currentCard);


            resetValues(state);


            //AI PLAY
            if(currentCard.getCardEnumValue() == CardDetails.POSTAL_PIGEON){
                if(!cardSelectionID.isEmpty()) {
                    new SelectCard(playerId, cardSelectionID.get(0), new ArrayList<>()).execute(state);
                }
            }
            else if(state.rangerCardMode){
                RangerCard rc = (RangerCard) currentCard;
                ArrayList<Integer> locationsToSelectFrom = new ArrayList<>();
                for (var location : state.everdellLocations) {
                    if(location.getComponentID() == rc.getLocationFrom().getComponentID()){
                        continue;
                    }
                    System.out.println("Adding Location to Select TO : " + location.getAbstractLocation());
                    locationsToSelectFrom.add(location.getComponentID());
                }
                state.rangerCardMode = false;
                new SelectLocation(playerId, -1, locationsToSelectFrom).execute(state);
            }


            checkForCardsThatNeedToActivateAfterPlayingACard(state);
            return true;
        }
        System.out.println("Cannot place card, village is full");
        return false;
    }

    public void resetValues(EverdellGameState state){
        //Reset the resource selection
        state.resourceSelection = new HashMap<>();
        for(var resource : EverdellParameters.ResourceTypes.values()){
            state.resourceSelection.put(resource, new Counter());
            state.resourceSelection.get(resource).setValue(0);
        }
        //Reset Card Selection
//        for (var card : state.cardSelection){
//            state.temporaryDeck.add(card);
//        }
        state.cardSelection.clear();
        state.currentCard = null;
    }

    public boolean checkIfVillageHasSpace(EverdellGameState state, int playerId){
        return state.playerVillage.get(playerId).getSize() < state.villageMaxSize[playerId].getValue();
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

        if(state.currentCard instanceof ConstructionCard cc){
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


        if(!state.playerHands.get(state.getCurrentPlayer()).remove(currentCard)){
            state.meadowDeck.remove(currentCard);
            if(state.meadowDeck.getSize() < state.meadowDeck.getCapacity()) {
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
        EverdellCard currentCard = (EverdellCard) state.getComponentById(currentCardID);


        EverdellCard judge = null;
        EverdellCard courthouse = null;

        for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){

            if(card.getCardEnumValue() == CardDetails.SHOP_KEEPER && currentCard.getCardEnumValue() != CardDetails.SHOP_KEEPER && !currentCard.isConstruction()){
                //Trigger Shop keeper effect
                triggerCardEffect(state, card);
            }
            if(card.getCardEnumValue() == CardDetails.HISTORIAN && currentCard.getCardEnumValue() != CardDetails.HISTORIAN){
                //Trigger Historian effect
                triggerCardEffect(state, card);
            }
            if(card.getCardEnumValue() == CardDetails.JUDGE && currentCard.getCardEnumValue() != CardDetails.JUDGE){
                System.out.println("JUDGE CARD");
                judge = card;
            }
            if(card.getCardEnumValue() == CardDetails.COURTHOUSE && currentCard.getCardEnumValue() != CardDetails.COURTHOUSE && currentCard.isConstruction()){
                System.out.println("COURTHOUSE CARD");
                courthouse = card;
            }
        }

        //Need to allow the AI to select which to trigger first
        if (judge != null){
            System.out.println("JUDGE CARD TRIGGERED");
            new ResourceSelect(state.getCurrentPlayer(), judge.getComponentID(), -1, new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values())), 1, false, true).execute(state);

        }
        if(courthouse != null){
            System.out.println("COURTHOUSE CARD TRIGGERED");
            new ResourceSelect(state.getCurrentPlayer(), courthouse.getComponentID(), -1, new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values())), 1, true, false).execute(state);
        }
        return false;
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
        if(!currentCard.checkIfPlayerCanBuyCard(state, state.getCurrentPlayer())){
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
    public PlayCard copy() {
        // TODO: copy non-final variables appropriately
        ArrayList<Integer> csID = new ArrayList<>(cardSelectionID);
        HashMap<EverdellParameters.ResourceTypes, Integer> rsID = new HashMap<>();
        for(var resource : this.resourceSelectionValues.keySet()){
            rsID.put(resource, this.resourceSelectionValues.get(resource));
        }
        PlayCard retValue = new PlayCard(playerId, currentCardID, csID, rsID);
        retValue.executed = executed;
        return retValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlayCard playCard = (PlayCard) o;
        return executed == playCard.executed && currentCardID == playCard.currentCardID && playerId == playCard.playerId && Objects.equals(cardSelectionID, playCard.cardSelectionID) && Objects.equals(resourceSelectionValues, playCard.resourceSelectionValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executed, currentCardID, cardSelectionID, resourceSelectionValues, playerId);
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
