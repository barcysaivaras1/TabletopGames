package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.*;
import games.everdell.EverdellParameters.CardDetails;
import org.apache.spark.sql.sources.In;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SelectCard extends AbstractAction implements IExtendedSequence {

    int playerId;
    int cardId;
    int occupationId;

    boolean executed;

    ArrayList<Integer> cardsToSelectFromIds;

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
     * Husband(If played with Wife and Farm) -> ResourceSelect -> PlayCard
     * Husband(If Condition is not met) -> PlayCard
     * Postal Pigeon -> SelectAListOfCards -> SelectAListOfCards -> PlayCard -> *** Card Specific Actions ***
     * */


    public SelectCard(int playerId, int cardId, ArrayList<Integer> cardsToSelectFromIds) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.cardsToSelectFromIds = cardsToSelectFromIds;
        this.occupationId = -1;
    }
    private SelectCard(int playerId, int cardId, int occupationId, ArrayList<Integer> cardsToSelectFromIds, boolean payWithResources, boolean payWithDiscount, boolean payWithOccupation) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.occupationId = occupationId;
        this.cardsToSelectFromIds = cardsToSelectFromIds;
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
        else if(payWithOccupation && occupationId == -1){
            state.setActionInProgress(this);
        }

        return true;
    }



    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        System.out.println("SelectCard: _computeAvailableActions");
        List<AbstractAction> actions = new ArrayList<>();
        EverdellGameState egs = (EverdellGameState) state;

        if(cardId == -1) { // Select Card
            for (Integer cardId : cardsToSelectFromIds) {
                EverdellCard card = (EverdellCard) egs.getComponentById(cardId);
                if (canCardBePlayed(card, egs)) {
                    actions.add(new SelectCard(playerId, card.getComponentID(), cardsToSelectFromIds));
                }

            }
        }
        else if (payWithOccupation) {
            //Select a card to occupy
            System.out.println("Selecting Occupation Card");
            for(EverdellCard card : egs.playerVillage.get(state.getCurrentPlayer())){
                if(card instanceof ConstructionCard cc){
                    EverdellCard cardTryingToOccupy = (EverdellCard) egs.getComponentById(cardId);
                    if(cc.canCardOccupyThis(egs, cardTryingToOccupy)){
                        actions.add(new SelectCard(playerId, cardId, card.getComponentID(), cardsToSelectFromIds, false, false, true));
                    }
                }
            }

        }
        else{ // Select Payment Method

            System.out.println("Selecting Payment Method");

            //Paying with resources
            if(thisCardCanBePaidByResources(cardId, egs)){
                actions.add(new SelectCard(playerId, cardId, occupationId, cardsToSelectFromIds, true, false, false));
            }

            //Paying with discount

            //Paying with occupation
            if(thisCardCanOccupy(cardId, egs)){
                actions.add(new SelectCard(playerId, cardId, occupationId, cardsToSelectFromIds, false, false, true));
            }

        }


        return actions;
    }

    private boolean thisCardCanOccupy(Integer cardId, EverdellGameState state){
        EverdellCard card = (EverdellCard) state.getComponentById(cardId);
        if(card instanceof ConstructionCard){
            return false;
        }

        for(EverdellCard cardToOccupy : state.playerVillage.get(playerId)){
            if(cardToOccupy == card){
                continue;
            }
            if(cardToOccupy instanceof ConstructionCard cc){
                if(cc.canCardOccupyThis(state, card) && !cc.isOccupied()){
                    return true;
                }
            }

        }
        return false;

    }

    private boolean thisCardCanBePaidByResources(Integer cardId, EverdellGameState state){
        EverdellCard card = (EverdellCard) state.getComponentById(cardId);
        PlayCard pc = new PlayCard(playerId, card.getComponentID(), new ArrayList<>(), new HashMap<>());
        return pc.checkIfPlayerCanBuyCard(state, playerId);
    }

    private boolean canCardBePlayed(EverdellCard card, EverdellGameState state){
        //Eventually add addtional check for occupation + discount
        PlayCard pc = new PlayCard(playerId, card.getComponentID(), new ArrayList<>(), new HashMap<>());

        //Fool is a special case, as it can be placed anywhere
        if(card.getCardEnumValue() == CardDetails.FOOL){
            return ((FoolCard) card).canFoolBePlaced(state, playerId) && pc.checkIfPlayerCanBuyCard(state, playerId);
        }
        //If the Village Has Space, AND (Can be bought by an occupation OR by resources) AND the player can place this unique card
        return pc.checkIfVillageHasSpace(state, playerId) && (thisCardCanOccupy(card.getComponentID(), state) || thisCardCanBePaidByResources(card.getComponentID(), state)) && pc.checkIfPlayerCanPlaceThisUniqueCard(state, playerId);
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        System.out.println("SelectCard: _afterAction");
        SelectCard selectCard = (SelectCard) action;
        EverdellCard c = (EverdellCard) state.getComponentById(selectCard.cardId);
        System.out.println("Card Selected For Play : " + c.getCardEnumValue());
        //If payment has not been selected, Select the payment method
        if((!selectCard.payWithDiscount && !selectCard.payWithOccupation && !selectCard.payWithResources)){
            //new SelectCard(playerId, selectCard.cardId).execute(state);
        }
        else if (selectCard.payWithOccupation && selectCard.occupationId == -1){
        }
        else{ // Payment has been selected, Choose the next step
            EverdellGameState egs = (EverdellGameState) state;
            EverdellCard card = (EverdellCard) egs.getComponentById(selectCard.cardId);
            //If card requires no additional actions, play the card
            if(selectCard.payWithResources || selectCard.payWithOccupation) {
                if(selectCard.payWithOccupation){
                    //Need to pay for the card
                    ConstructionCard occupation = (ConstructionCard) egs.getComponentById(selectCard.occupationId);
                    occupation.occupyConstruction((CritterCard) card);
                }
//                if(card.getCardEnumValue() == CardDetails.CHIP_SWEEP){
//                    if(chipSweepCardId == -1) {
//                        ArrayList<Integer> cardsToSelectFrom = new ArrayList<>();
//                        for (var cardForSelection : egs.playerVillage.get(playerId).stream().filter(isNotChipSweep -> isNotChipSweep.getCardEnumValue() != CardDetails.CHIP_SWEEP).filter(isGreen -> isGreen.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).collect(Collectors.toCollection(ArrayList::new))) {
//                            cardsToSelectFrom.add(cardForSelection.getComponentID());
//                        }
//                        new SelectCard(playerId, -1, -1, card.getComponentID(), cardsToSelectFrom, true, false, false).execute(state);
//                        executed = true;
//                        return;
//                    }
//                    else{
//                        CopyCard chipSweepCard = (CopyCard) egs.getComponentById(selectCard.chipSweepCardId);
//                        chipSweepCard.setCardToCopy((EverdellCard) egs.getComponentById(selectCard.cardId));
//                        card = chipSweepCard;
//                    }
//                }
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
                    ArrayList<EverdellParameters.ResourceTypes> resources = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values()));
                    new ResourceSelect(playerId, card.getComponentID(), -1, resources, 2, true, false, false).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.BARD){
                    EverdellCard finalCard = card;
                    ArrayList<EverdellCard> cardsToPickFrom = egs.playerHands.get(playerId).getComponents().stream().filter(bardCard -> bardCard != finalCard).collect(Collectors.toCollection(ArrayList::new));
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
                else if(card.getCardEnumValue() == CardDetails.POSTAL_PIGEON){
                    ArrayList<EverdellCard> cardsToPickFrom = new ArrayList<>();
                    for(int i = 0; i < 2; i++){
                        EverdellCard ppCard = egs.cardDeck.draw();
                        if(ppCard.getPoints() <=3){
                            cardsToPickFrom.add(ppCard);
                        }
                        else{
                            egs.discardDeck.add(ppCard);
                        }
                    }
                    //If the revealed cards are not valid the effect is not triggered
                    if(cardsToPickFrom.isEmpty()){
                        new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                        executed = true;
                        return;
                    }
                    new SelectAListOfCards(playerId, -1, card.getComponentID(), cardsToPickFrom, 1, true).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.HUSBAND){
                    //Need to check if conditions are met
                    HusbandCard hc = (HusbandCard) card;
                    if(hc.isThereAFarm(egs) && hc.findWife(egs)) {
                        new ResourceSelect(playerId, card.getComponentID(), -1, new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values())), 1, true, false, true).execute(state);
                    }
                    else{
                        new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                    }
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
        SelectCard sc = new SelectCard(playerId, cardId, occupationId, cardsToSelectFromIds, payWithResources, payWithDiscount, payWithOccupation);
        sc.executed = executed;

        return sc;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectCard that = (SelectCard) o;
        return playerId == that.playerId && cardId == that.cardId && occupationId == that.occupationId && executed == that.executed && payWithResources == that.payWithResources && payWithDiscount == that.payWithDiscount && payWithOccupation == that.payWithOccupation && Objects.equals(cardsToSelectFromIds, that.cardsToSelectFromIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, cardId, occupationId, executed, cardsToSelectFromIds, payWithResources, payWithDiscount, payWithOccupation);
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
