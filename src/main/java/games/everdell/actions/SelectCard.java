package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters.CardDetails;
import games.everdell.components.FoolCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SelectCard extends AbstractAction implements IExtendedSequence {

    int playerId;
    int cardId;

    boolean executed;

    boolean payWithResources;
    boolean payWithDiscount;
    boolean payWithOccupation;

    //Everdell Card Paths

    //Basic Cards
    /* Basic Card -> PlayCard
    /* Here is a list of cards that are considered basic :
    *  Castle, Chapel, Evertree, Fairgrounds, Farm, General Store, Mine, Palace, Resin Refinery, School, Theatre, Twig Barge, King, Historian, Architect, Wife, Shopkeeper, Wanderer, Barge Toad
       This also includes all Red Destination cards*/

    //Special Cards (These are cards that require extra steps !)
    /* WOOD_CARVER -> ResourceSelect -> PlayCard
     * Doctor -> ResourceSelect -> PlayCard
     * Peddler -> ResourceSelect -> ResourceSelect -> PlayCard
     * Bard -> SelectAListOfCards -> PlayCard
     * Teacher -> SelectAListOfCards -> SelectAPlayer -> PlayCard
     * Fool -> SelectAPlayer -> PlayCard
     * Monk -> ResourceSelect -> SelectAPlayer -> PlayCard
     * Undertaker -> SelectAListOfCards -> SelectAListOfCards -> PlayACard
     * */


    public SelectCard(int playerId, int cardId) {
        this.playerId = playerId;
        this.cardId = cardId;
    }
    private SelectCard(int playerId, int cardId, boolean payWithResources, boolean payWithDiscount, boolean payWithOccupation) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.payWithResources = payWithResources;
        this.payWithDiscount = payWithDiscount;
        this.payWithOccupation = payWithOccupation;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        System.out.println("SelectCard: execute");
        EverdellGameState state = (EverdellGameState) gs;
        if(cardId == -1 || (!payWithResources && !payWithDiscount && !payWithOccupation)){
            state.setActionInProgress(this);
        }
        return true;
    }



    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        System.out.println("SelectCard: _computeAvailableActions");
        List<AbstractAction> actions = new ArrayList<>();

        EverdellGameState egs = (EverdellGameState) state;


        //This is currently only iterating over the player's hand, but it should be iterating over the meadow aswell
        if(cardId == -1) { // Select Card
            System.out.println("Selecting Card");
            for (EverdellCard card : egs.playerHands.get(playerId)) {
                if (canCardBePlayed(card, egs)) {
                    actions.add(new SelectCard(playerId, card.getComponentID()));
                }

            }
        }
        else{ // Select Payment Method

            System.out.println("Selecting Payment Method");
            //Paying with resources
            actions.add(new SelectCard(playerId, cardId, true, false, false));

            //Paying with discount

            //Paying with occupation
        }


        return actions;
    }

    private boolean canCardBePlayed(EverdellCard card, EverdellGameState state){
        //Eventually add addtional check for occupation + discount
        PlayCard pc = new PlayCard(playerId, card.getComponentID(), new ArrayList<>(), new HashMap<>());

        //Fool is a special case, as it can be placed anywhere
        if(card.getCardEnumValue() == CardDetails.FOOL){
            return ((FoolCard) card).canFoolBePlaced(state, playerId) && pc.checkIfPlayerCanBuyCard(state, playerId);
        }
        return pc.checkIfVillageHasSpace(state, playerId) && pc.checkIfPlayerCanBuyCard(state, playerId) && pc.checkIfPlayerCanPlaceThisUniqueCard(state, playerId);
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        System.out.println("SelectCard: _afterAction");
        SelectCard selectCard = (SelectCard) action;

        //If payment has not been selected, Select the payment method
        if(!selectCard.payWithDiscount && !selectCard.payWithOccupation && !selectCard.payWithResources){
            //new SelectCard(playerId, selectCard.cardId).execute(state);
        }
        else{ // Payment has been selected, Choose the next step

            EverdellGameState egs = (EverdellGameState) state;
            EverdellCard card = (EverdellCard) egs.getComponentById(selectCard.cardId);
            //If card requires no additional actions, play the card
            if(selectCard.payWithResources) {
                System.out.println("PLAYING A CARD");
                //Check if the card would require additional steps
                if(card.getCardEnumValue() == CardDetails.WOOD_CARVER){
                    ArrayList<EverdellParameters.ResourceTypes> resources = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.TWIG));
                    new ResourceSelect(playerId, card.getComponentID(), -1, resources, 3, true, false, false).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.DOCTOR){
                    ArrayList<EverdellParameters.ResourceTypes> resources = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.BERRY));
                    new ResourceSelect(playerId, card.getComponentID(), -1, resources, 3, true, false, false).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.PEDDLER){
                    System.out.println("PEDDLER CARD");
                    ArrayList<EverdellParameters.ResourceTypes> resources = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values()));
                    new ResourceSelect(playerId, card.getComponentID(), -1, resources, 2, true, false, false).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.BARD){
                    ArrayList<EverdellCard> cardsToPickFrom = egs.playerHands.get(playerId).getComponents().stream().filter(bardCard -> bardCard != card).collect(Collectors.toCollection(ArrayList::new));
                    new SelectAListOfCards(playerId, -1, card.getComponentID(), cardsToPickFrom, cardsToPickFrom.size(), false).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.TEACHER){
                    ArrayList<EverdellCard> cardsToPickFrom = new ArrayList<>();
                    //Draw 2 Cards
                    cardsToPickFrom.add(egs.cardDeck.draw());
                    cardsToPickFrom.add(egs.cardDeck.draw());
                    new SelectAListOfCards(playerId, -1, card.getComponentID(), cardsToPickFrom, 1, true).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.FOOL){
                    new SelectPlayer(playerId, card.getComponentID(), -1).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.MONK){
                    ArrayList<EverdellParameters.ResourceTypes> resources = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.BERRY));
                    new ResourceSelect(playerId, card.getComponentID(), -1, resources, 2, true, false, false).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.UNDERTAKER){
                    ArrayList<EverdellCard> cardsToPickFrom = new ArrayList<>(egs.meadowDeck.getComponents());
                    new SelectAListOfCards(playerId, -1, card.getComponentID(), cardsToPickFrom, 3, true).execute(state);
                }
                else {
                    new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                }
            }
        }
        executed = true;
    }

    @Override
    public boolean executionComplete(AbstractGameState state) {
        return executed;
    }

    @Override
    public SelectCard copy() {
        SelectCard retValue = new SelectCard(playerId, cardId);
        retValue.executed = executed;
        retValue.payWithResources = payWithResources;
        retValue.payWithDiscount = payWithDiscount;
        retValue.payWithOccupation = payWithOccupation;
        return retValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectCard that = (SelectCard) o;
        return playerId == that.playerId && cardId == that.cardId && executed == that.executed && payWithResources == that.payWithResources && payWithDiscount == that.payWithDiscount && payWithOccupation == that.payWithOccupation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, cardId, executed, payWithResources, payWithDiscount, payWithOccupation);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return "Selecting a Card";
    }

    @Override
    public String toString(){
        return "Selecting A Card";
    }

}
