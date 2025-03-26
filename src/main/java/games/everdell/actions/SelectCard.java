package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Counter;
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
    int secondaryCardId;

    boolean executed;

    ArrayList<Integer> cardsToSelectFromIds;

    private HashMap<EverdellParameters.ResourceTypes, Integer> resourcesSelected;

    boolean payWithResources;
    boolean payWithDiscount;

    int discountMethodID;
    boolean payWithOccupation;


    //Everdell Card Paths

    //Basic Cards
    /* Basic Card -> PlayCard
    /* Here is a list of cards that are considered basic :
     * Castle, Chapel, Evertree, Fairgrounds, Farm, General Store, Mine, Palace, Resin Refinery, School, Theatre, Twig Barge, King, Historian, Architect, Wife, Shopkeeper, Wanderer, Barge Toad, Crane, InnKeeper
       This also includes all Red Destination cards*/

    //Special Cards (These are cards that require extra steps !)
    /* Wood_Carver -> ResourceSelect -> PlayCard
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
     * Ruins -> SelectAListOfCards -> PlayCard
     * Shepherd -> SelectAPlayer -> PlayCard
     * Storehouse -> ResourceSelection -> PlayCard
     * Judge -> PlayCard (When conditions are met this card effect will be triggered)
     * Courthouse -> PlayCard (When conditions are met this card effect will be triggered)
     * Chip_Sweep -> SelectCard -> ...Card Specific Actions... -> PlayCard
     * Miner_Mole -> SelectCard -> ...Card Specific Actions... -> PlayCard */



    public SelectCard(int playerId, int cardId, ArrayList<Integer> cardsToSelectFromIds) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.cardsToSelectFromIds = cardsToSelectFromIds;
        this.occupationId = -1;
        this.resourcesSelected = null;
        this.discountMethodID = -1;
        this.secondaryCardId = -1;
    }
    private SelectCard(int playerId, int cardId, int occupationId, ArrayList<Integer> cardsToSelectFromIds, boolean payWithResources, boolean payWithDiscount, boolean payWithOccupation, HashMap<EverdellParameters.ResourceTypes, Integer> resourcesSelected) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.occupationId = occupationId;
        this.cardsToSelectFromIds = cardsToSelectFromIds;
        this.payWithResources = payWithResources;
        this.payWithDiscount = payWithDiscount;
        this.payWithOccupation = payWithOccupation;
        this.resourcesSelected = resourcesSelected;
    }
    private SelectCard(int playerId, int cardId, int occupationId, ArrayList<Integer> cardsToSelectFromIds, boolean payWithResources, boolean payWithDiscount, boolean payWithOccupation, HashMap<EverdellParameters.ResourceTypes, Integer> resourcesSelected, int discountMethodID, int secondaryCardId) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.occupationId = occupationId;
        this.cardsToSelectFromIds = cardsToSelectFromIds;
        this.payWithResources = payWithResources;
        this.payWithDiscount = payWithDiscount;
        this.payWithOccupation = payWithOccupation;
        this.resourcesSelected = resourcesSelected;
        this.discountMethodID = discountMethodID;
        this.secondaryCardId = secondaryCardId;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        System.out.println("SelectCard: execute");
        playerId = gs.getCurrentPlayer();
        EverdellGameState state = (EverdellGameState) gs;
        if(cardId == -1 || (!payWithResources && !payWithDiscount && !payWithOccupation)){
            state.setActionInProgress(this);
        }
        else if(payWithOccupation && occupationId == -1){
            state.setActionInProgress(this);
        }
        else if(payWithDiscount && resourcesSelected == null){
            state.setActionInProgress(this);
        }
        else if(payWithDiscount && discountMethodID != -1){
            EverdellCard card = (EverdellCard) state.getComponentById(discountMethodID);
            if(card instanceof DungeonCard dc){
                if(secondaryCardId == -1){
                    System.out.println("SETTING ACTION IN PROGRESS BECAUSE OF SECONDARYCARDID");
                    state.setActionInProgress(this);
                }
            }
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
                        actions.add(new SelectCard(playerId, cardId, card.getComponentID(), cardsToSelectFromIds, false, false, true, null));
                    }
                }
            }

        }
        else if (payWithDiscount){
            EverdellCard discountCard = (EverdellCard) egs.getComponentById(discountMethodID);
            if(resourcesSelected == null) {
                //Select exact discount you want to apply
                System.out.println("Selecting Discount");
                HashMap<EverdellParameters.ResourceTypes, Integer> amountOwned = new HashMap<>();
                for (var resource : egs.PlayerResources.keySet()) {
                    amountOwned.put(resource, egs.PlayerResources.get(resource)[playerId].getValue());
                }
                computeAllValidDiscounts(actions, cardId, 3, ((EverdellCard) egs.getComponentById(cardId)).getResourceCost(), amountOwned);
            }
            else if(discountCard instanceof DungeonCard dc){
                System.out.println("Selecting Critter Card for Dungeon");
                //Create an action for every critter card selection in the dungeon
                System.out.println("Current Player : " + playerId);
                if(dc.cell1ID == -1){
                    System.out.println("CHECK1");
                    for(EverdellCard card : egs.playerVillage.get(getCurrentPlayer(state))){
                        System.out.println("Checking Card for Dungeon : " + card.getCardEnumValue());
                        if(card instanceof CritterCard && card.getCardEnumValue() != CardDetails.RANGER){
                            System.out.println("CHECKLOOP");
                            actions.add(new SelectCard(playerId, cardId, -1, new ArrayList<>(), false, true, false, resourcesSelected, discountMethodID, card.getComponentID()));
                        }
                    }
                }
                System.out.println("CHECK2");
            }
        }
        else{ // Select Payment Method

            System.out.println("Selecting Payment Method");

            //Paying with resources
            if(thisCardCanBePaidByResources(cardId, egs)){
                actions.add(new SelectCard(playerId, cardId, occupationId, cardsToSelectFromIds, true, false, false, null));
            }

            //Paying with discount
            if(thisCardCanBePaidByDiscount(cardId, egs)){
                generateDiscountOptions(actions, cardId, egs);
            }

            //Paying with occupation
            if(thisCardCanOccupy(cardId, egs)){
                actions.add(new SelectCard(playerId, cardId, occupationId, cardsToSelectFromIds, false, false, true, null));
            }
        }
        System.out.println("CHECK3");
        return actions;
    }

    public void computeAllValidDiscounts(List<AbstractAction> actions, int cardID, int discount, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, HashMap<EverdellParameters.ResourceTypes, Integer> amountOwned) {
        //Generate all possible selections of resources
        ArrayList<ArrayList<Integer>> allPossibleSelections = new ArrayList<>();
        int totalCost = resourceCost.values().stream().mapToInt(Integer::intValue).sum();
        generateDiscountSelections(Math.min(totalCost, discount), resourceCost.size(), new ArrayList<>(), allPossibleSelections, resourceCost);

        ArrayList<ArrayList<Integer>> validDiscounts = new ArrayList<>();

        for(ArrayList<Integer> selection : allPossibleSelections){
            int counter = 0;
            boolean valid = true;
            HashMap<EverdellParameters.ResourceTypes, Integer> possibleDiscount = new HashMap<>();
            for(var resource : resourceCost.keySet()){
                possibleDiscount.put(resource, selection.get(counter));
                if(Math.max(resourceCost.get(resource) - selection.get(counter), 0) > amountOwned.get(resource)){
                    valid = false;
                }
                counter++;
            }
            if(valid){
                actions.add(new SelectCard(0, cardID, -1, new ArrayList<>(), false, true, false, possibleDiscount, discountMethodID, secondaryCardId));
                validDiscounts.add(selection);
            }
        }

        System.out.println("ALL DISCOUNTS");
        System.out.println(allPossibleSelections);

        System.out.println("**********************");

        System.out.println("Valid DISCOUNTS");
        System.out.println(validDiscounts);
    }


    private static void generateDiscountSelections(int discount, int size, ArrayList<Integer> current, ArrayList<ArrayList<Integer>> allPossibleSelections, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost) {
        if (current.size() == size) {
            int sum = current.stream().mapToInt(Integer::intValue).sum();
            if (sum == discount) {
                allPossibleSelections.add(new ArrayList<>(current));
            }
            return;
        }

        int position = current.size();
        int maxValue = (int) resourceCost.values().toArray()[position];

        for (int i = 0; i <= Math.min(discount, maxValue); i++) {
            current.add(i);
            generateDiscountSelections(discount, size, current, allPossibleSelections, resourceCost);
            current.remove(current.size() - 1);
        }
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

    //Will player be able to pay for the card with the resources they have if ANY kind of discount is applied
    private boolean thisCardCanBePaidByDiscount(Integer cardId, EverdellGameState state){
        EverdellCard card = (EverdellCard) state.getComponentById(cardId);
        boolean craneDiscount = false;
        boolean innKeeperDiscount = false;
        boolean dungeonDiscount = false;

        for(var discountCard : state.playerVillage.get(playerId)){
            if(discountCard.getCardEnumValue() == CardDetails.CRANE){
                craneDiscount = true;
            }
            if(discountCard.getCardEnumValue() == CardDetails.INNKEEPER){
                innKeeperDiscount = true;
            }
            if(discountCard.getCardEnumValue() == CardDetails.DUNGEON){
                DungeonCard dc = (DungeonCard) discountCard;
                if(dc.isThereACellFree()) {
                    dungeonDiscount = true;
                }
            }
        }

        if(card instanceof ConstructionCard cc){
            //Crane Discount
            if(craneDiscount){
                if(standardDiscountCheck(state, cc)){
                    return true;
                }
            }
            else if(dungeonDiscount){
                if (dungeonDiscountCheck(state, cc)){
                    return true;
                }
            }
        }
        else if(card instanceof CritterCard cc){
            //Innkeeper Discount
            if(innKeeperDiscount){
                return standardDiscountCheck(state, cc);
            }
            else if(dungeonDiscount){
                if (dungeonDiscountCheck(state, cc)) {
                    return true;
                }
            }
        }

        return false;
    }

    //Will player be able to pay for the card with the resources they have
    private boolean standardDiscountCheck(EverdellGameState state, EverdellCard cc){
        int totalCost = cc.getResourceCost().values().stream().mapToInt(Integer::intValue).sum();
        int totalResources = 3;
        for(var resource : cc.getResourceCost().keySet()){
            if(cc.getResourceCost().get(resource) == 0){continue;}
            totalResources += state.PlayerResources.get(resource)[playerId].getValue();
        }
        return totalResources >= totalCost;
    }

    //Will player be able to pay for the card with the resources they have
    private boolean dungeonDiscountCheck(EverdellGameState state, EverdellCard cc){
        //Is there a critter card that can be placed in the dungeon
        boolean critterCardInVillage = false;
        for(var card : state.playerVillage.get(playerId)){
            if (card instanceof CritterCard) {
                critterCardInVillage = true;
                break;
            }
        }

        return standardDiscountCheck(state, cc) && critterCardInVillage;
    }



    //Creates actions to determine which type of discount the player wants to apply
    private void generateDiscountOptions(List<AbstractAction> actions, int cardId, EverdellGameState state){
        EverdellCard card = (EverdellCard) state.getComponentById(cardId);
        boolean craneDiscount = false;
        EverdellCard craneCard = null;
        boolean innKeeperDiscount = false;
        EverdellCard innKeeperCard = null;
        boolean dungeonDiscount = false;
        EverdellCard dungeonCard = null;

        for(var discountCard : state.playerVillage.get(playerId)){
            if(discountCard.getCardEnumValue() == CardDetails.CRANE){
                craneDiscount = true;
                craneCard = discountCard;
            }
            if(discountCard.getCardEnumValue() == CardDetails.INNKEEPER){
                innKeeperDiscount = true;
                innKeeperCard = discountCard;
            }
            if(discountCard.getCardEnumValue() == CardDetails.DUNGEON){
                DungeonCard dc = (DungeonCard) discountCard;
                if(dc.isThereACellFree()) {
                    dungeonDiscount = true;
                    dungeonCard = discountCard;
                }
            }
        }
        if(dungeonDiscount){
            if(dungeonDiscountCheck(state, card)){
                actions.add(new SelectCard(playerId, cardId, -1, new ArrayList<>(), false, true, false, null, dungeonCard.getComponentID(), secondaryCardId));
            }
        }

        if(card instanceof ConstructionCard cc){
            //Crane Discount
            if(craneDiscount){
                if(standardDiscountCheck(state, cc)){
                    actions.add(new SelectCard(playerId, cardId, -1, new ArrayList<>(), false, true, false, null, craneCard.getComponentID(), secondaryCardId));
                }
            }
        }
        else if(card instanceof CritterCard cc){
            //Innkeeper Discount
            if(innKeeperDiscount){
                if(standardDiscountCheck(state, cc)){
                    actions.add(new SelectCard(playerId, cardId, -1, new ArrayList<>(), false, true, false, null, innKeeperCard.getComponentID(), secondaryCardId));
                }
            }
        }
    }

    private boolean thisCardCanBePaidByResources(Integer cardId, EverdellGameState state){
        EverdellCard card = (EverdellCard) state.getComponentById(cardId);
        return card.checkIfPlayerCanBuyCard(state, playerId);
    }

    private boolean canCardBePlayed(EverdellCard card, EverdellGameState state){
        //Eventually add addtional check for occupation + discount
        PlayCard pc = new PlayCard(playerId, card.getComponentID(), new ArrayList<>(), new HashMap<>());

        //Fool is a special case, as it can be placed anywhere
        if(card.getCardEnumValue() == CardDetails.FOOL){
            return ((FoolCard) card).canFoolBePlaced(state, playerId) && card.checkIfPlayerCanBuyCard(state, playerId);
        }
        //If the Village Has Space, AND (Can be bought by an occupation OR by resources OR by discount) AND the player can place this unique card
        return pc.checkIfVillageHasSpace(state, playerId) && (thisCardCanOccupy(card.getComponentID(), state) || thisCardCanBePaidByResources(card.getComponentID(), state) || thisCardCanBePaidByDiscount(card.getComponentID(), state)) && card.checkIfPlayerCanPlaceThisUniqueCard(state, playerId);
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        System.out.println("SelectCard: _afterAction");
        System.out.println("After Action Current Player : " + playerId);
        System.out.println("After Action Current Player : " + getCurrentPlayer(state));
        SelectCard selectCard = (SelectCard) action;
        EverdellCard c = (EverdellCard) state.getComponentById(selectCard.cardId);
        EverdellGameState egs = (EverdellGameState) state;
        System.out.println("Card Selected For Play : " + c.getCardEnumValue());

        boolean discountApplied = false;

        //If discount option was chosen, we must apply the discount
        if(selectCard.payWithDiscount && selectCard.resourcesSelected != null){
            EverdellCard cardApplyingDiscount = (EverdellCard) state.getComponentById(selectCard.discountMethodID);
            HashMap<EverdellParameters.ResourceTypes, Counter> resourcesSelection = new HashMap<>();

            for(var resource : selectCard.resourcesSelected.keySet()){
                resourcesSelection.put(resource, new Counter());
                resourcesSelection.get(resource).setValue(selectCard.resourcesSelected.get(resource));
            }

            //Applying discount
            //Removing Crane from the village
            //Resetting values
            if(cardApplyingDiscount.getCardEnumValue() == CardDetails.CRANE ){
                ConstructionCard cc = (ConstructionCard) cardApplyingDiscount;
                egs.resourceSelection = resourcesSelection;
                cc.applyCardEffect(egs);
                egs.playerVillage.get(playerId).remove(cc);
                egs.discardDeck.add(cc);
                egs.resourceSelection.keySet().forEach(resource -> egs.resourceSelection.get(resource).setValue(0));
                discountApplied = true;
            }
            else if (cardApplyingDiscount.getCardEnumValue() == CardDetails.INNKEEPER){
                CritterCard cc = (CritterCard) cardApplyingDiscount;
                egs.resourceSelection = resourcesSelection;
                cc.applyCardEffect(egs);
                egs.playerVillage.get(playerId).remove(cc);
                egs.discardDeck.add(cc);
                egs.resourceSelection.keySet().forEach(resource -> egs.resourceSelection.get(resource).setValue(0));
                discountApplied = true;
            }
            //Dungeon Discount, we must place the critter in the dungeon and trigger the discount
            else if (cardApplyingDiscount.getCardEnumValue() == CardDetails.DUNGEON && selectCard.secondaryCardId != -1){
                System.out.println("PAYING WITH DISCOUNT");
                DungeonCard dc = (DungeonCard) cardApplyingDiscount;
                egs.resourceSelection = resourcesSelection;
                dc.placeCritterInCell((CritterCard) egs.getComponentById(selectCard.secondaryCardId));
                dc.applyCardEffect(egs);
                egs.resourceSelection.keySet().forEach(resource -> egs.resourceSelection.get(resource).setValue(0));
                discountApplied = true;
            }
        }


        //If payment has not been selected, Select the payment method
        if((!selectCard.payWithDiscount && !selectCard.payWithOccupation && !selectCard.payWithResources)){
            //new SelectCard(playerId, selectCard.cardId).execute(state);
        }
        else if (selectCard.payWithOccupation && selectCard.occupationId == -1){
        }
        else if (selectCard.payWithDiscount && !discountApplied){
        }
        else{ // Payment has been selected, Choose the next step
            System.out.println("Card is going to be placed!");
            EverdellCard card = (EverdellCard) egs.getComponentById(selectCard.cardId);
            //If card requires no additional actions, play the card
            if(selectCard.payWithResources || selectCard.payWithOccupation || selectCard.payWithDiscount) {
                if(selectCard.payWithOccupation){
                    //Need to pay for the card
                    ConstructionCard occupation = (ConstructionCard) egs.getComponentById(selectCard.occupationId);
                    occupation.occupyConstruction((CritterCard) card);
                }
                //Check if the card would require additional steps
                if(card.getCardEnumValue() == CardDetails.WOOD_CARVER){
                    ArrayList<EverdellParameters.ResourceTypes> resources = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.TWIG));
                    new ResourceSelect(playerId, card.getComponentID(), -1, resources, 3, false, true).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.DOCTOR){
                    ArrayList<EverdellParameters.ResourceTypes> resources = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.BERRY));
                    new ResourceSelect(playerId, card.getComponentID(), -1, resources, 3, false, true).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.PEDDLER){
                    ArrayList<EverdellParameters.ResourceTypes> resources = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values()));
                    new ResourceSelect(playerId, card.getComponentID(), -1, resources, 2, false, true).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.BARD){
                    EverdellCard finalCard = card;
                    ArrayList<EverdellCard> cardsToPickFrom = egs.playerHands.get(playerId).getComponents().stream().filter(bardCard -> bardCard != finalCard).collect(Collectors.toCollection(ArrayList::new));
                    new SelectAListOfCards(playerId, -1, card.getComponentID(), cardsToPickFrom, cardsToPickFrom.size(), false).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.STORE_HOUSE){
                    new ResourceSelect(playerId, card.getComponentID(), -1, new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values())), 1, true, false).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.CHIP_SWEEP){
                    ArrayList<Integer> cardIds = egs.playerVillage.get(playerId).stream().filter(greenCard -> greenCard.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).filter(greenCard -> greenCard.getCardEnumValue() != CardDetails.CHIP_SWEEP).map(EverdellCard::getComponentID).collect(Collectors.toCollection(ArrayList::new));
                    if(cardIds.isEmpty()){
                        new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                    }
                    else if (egs.copyMode){ //In the scenario where a minermole is copying a chipsweep, we do not want to enter an infinite loop of them copying eachother
                        EverdellCard copyCard = (EverdellCard) egs.getComponentById(egs.copyID);
                        if( copyCard.getCardEnumValue() == CardDetails.MINER_MOLE) {
                            cardIds = egs.playerVillage.get(playerId).stream().filter(greenCard -> greenCard.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).filter(greenCard -> greenCard.getCardEnumValue() != CardDetails.CHIP_SWEEP).filter(greenCard -> greenCard.getCardEnumValue() != CardDetails.MINER_MOLE).map(EverdellCard::getComponentID).collect(Collectors.toCollection(ArrayList::new));
                            if(cardIds.isEmpty()){
                                new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                            }
                            else{
                                new SelectCard(playerId, -1, -1, cardIds, true, false, false, null).execute(state);
                            }
                        }
                    }
                    else {
                        egs.copyMode = true;
                        egs.copyID = card.getComponentID();

                        new SelectCard(playerId, -1, -1, cardIds, true, false, false, null).execute(state);
                    }
                }
                else if(card.getCardEnumValue() == CardDetails.MINER_MOLE){
                    ArrayList<Integer> cardIds = new ArrayList<>();
                    for(int i=0; i<egs.getNPlayers(); i++){
                        if( i == egs.getCurrentPlayer()) continue;
                        cardIds.addAll(egs.playerVillage.get(i).stream().filter(greenCard -> greenCard.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).filter(greenCard -> greenCard.getCardEnumValue() != CardDetails.MINER_MOLE).map(EverdellCard::getComponentID).collect(Collectors.toCollection(ArrayList::new)));
                    }
                    if(cardIds.isEmpty()){
                        new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                    }
                    else if (egs.copyMode){ //In the scenario where a chipsweep is copying a miner mole, we do not want to enter an infinite loop of them copying eachother
                        EverdellCard copyCard = (EverdellCard) egs.getComponentById(egs.copyID);
                        if( copyCard.getCardEnumValue() == CardDetails.CHIP_SWEEP) {
                            for(int i=0; i<egs.getNPlayers(); i++){
                                if( i == egs.getCurrentPlayer()) continue;
                                cardIds = new ArrayList<>();
                                cardIds.addAll(egs.playerVillage.get(i).stream().filter(greenCard -> greenCard.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).filter(greenCard -> greenCard.getCardEnumValue() != CardDetails.MINER_MOLE).filter(greenCard -> greenCard.getCardEnumValue() != CardDetails.CHIP_SWEEP).map(EverdellCard::getComponentID).collect(Collectors.toCollection(ArrayList::new)));
                            }
                            if(cardIds.isEmpty()){
                                new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                            }
                            else{
                                new SelectCard(playerId, -1, -1, cardIds, true, false, false, null).execute(state);
                            }
                        }
                    }
                    else {
                        egs.copyMode = true;
                        egs.copyID = card.getComponentID();
                        new SelectCard(playerId, -1, -1, cardIds, true, false, false, null).execute(state);
                    }
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
                    new ResourceSelect(playerId, card.getComponentID(), -1, resources, 2, false, true).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.UNDERTAKER){
                    ArrayList<EverdellCard> cardsToPickFrom = new ArrayList<>(egs.meadowDeck.getComponents());
                    new SelectAListOfCards(playerId, -1, card.getComponentID(), cardsToPickFrom, 3, true).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.RUINS){
                    ArrayList<EverdellCard> cardsToPickFrom = egs.playerVillage.get(playerId).stream().filter(EverdellCard::isConstruction).collect(Collectors.toCollection(ArrayList::new));
                    new SelectAListOfCards(playerId, -1, card.getComponentID(), cardsToPickFrom, 1, true).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.SHEPHERD){
                    new SelectPlayer(playerId, card.getComponentID(), -1).execute(state);
                }
                else if(card.getCardEnumValue() == CardDetails.POSTAL_PIGEON){
                    ArrayList<EverdellCard> cardsToPickFrom = new ArrayList<>();
                    for(int i = 0; i < 2; i++){
                        EverdellCard ppCard = egs.cardDeck.draw();
                        if(ppCard.getPoints() <=3){
                            cardsToPickFrom.add(ppCard);
                        }
                        else{
                            ppCard.discardCard(egs);
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
                        new ResourceSelect(playerId, card.getComponentID(), -1, new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values())), 1, true, true).execute(state);
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
        SelectCard sc = new SelectCard(playerId, cardId, occupationId, cardsToSelectFromIds, payWithResources, payWithDiscount, payWithOccupation, resourcesSelected);
        sc.executed = executed;
        sc.discountMethodID = discountMethodID;
        sc.secondaryCardId = secondaryCardId;

        return sc;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectCard that = (SelectCard) o;
        return playerId == that.playerId && cardId == that.cardId && occupationId == that.occupationId && secondaryCardId == that.secondaryCardId && executed == that.executed && payWithResources == that.payWithResources && payWithDiscount == that.payWithDiscount && discountMethodID == that.discountMethodID && payWithOccupation == that.payWithOccupation && Objects.equals(cardsToSelectFromIds, that.cardsToSelectFromIds) && Objects.equals(resourcesSelected, that.resourcesSelected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, cardId, occupationId, secondaryCardId, executed, cardsToSelectFromIds, resourcesSelected, payWithResources, payWithDiscount, discountMethodID, payWithOccupation);
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
