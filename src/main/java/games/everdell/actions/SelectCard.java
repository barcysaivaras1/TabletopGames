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
    int locationId;
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
     * Miner_Mole -> SelectCard -> ...Card Specific Actions... -> PlayCard
     * Ranger -> SelectLocation (From) -> PlayCard -> SelectLocation (To) -> ...Location Specific Actions... -> PlaceWorker?*/



    //Standard Constructor
    public SelectCard(int playerId, int cardId, ArrayList<Integer> cardsToSelectFromIds) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.cardsToSelectFromIds = cardsToSelectFromIds;
        this.occupationId = -1;
        this.locationId = -1;
        this.resourcesSelected = null;
        this.discountMethodID = -1;
        this.secondaryCardId = -1;
    }

    //Constructor for locations that discount cards
    public SelectCard(int playerId, int cardId, int locationId, ArrayList<Integer> cardsToSelectFromIds) {
        this.playerId = playerId;
        this.locationId = locationId;
        this.cardId = cardId;
        this.cardsToSelectFromIds = cardsToSelectFromIds;
        this.occupationId = -1;
        this.resourcesSelected = null;
        this.discountMethodID = -1;
        this.secondaryCardId = -1;
    }


    private SelectCard(int playerId, int cardId, int locationId, int occupationId, ArrayList<Integer> cardsToSelectFromIds, boolean payWithResources, boolean payWithDiscount, boolean payWithOccupation, HashMap<EverdellParameters.ResourceTypes, Integer> resourcesSelected) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.locationId = locationId;
        this.occupationId = occupationId;
        this.cardsToSelectFromIds = cardsToSelectFromIds;
        this.payWithResources = payWithResources;
        this.payWithDiscount = payWithDiscount;
        this.payWithOccupation = payWithOccupation;
        this.resourcesSelected = resourcesSelected;
    }
    private SelectCard(int playerId, int cardId, int locationId, int occupationId, ArrayList<Integer> cardsToSelectFromIds, boolean payWithResources, boolean payWithDiscount, boolean payWithOccupation, HashMap<EverdellParameters.ResourceTypes, Integer> resourcesSelected, int discountMethodID, int secondaryCardId) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.locationId = locationId;
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

        System.out.println("Before Execute Player ID : " + playerId);
        playerId = gs.getCurrentPlayer();
        System.out.println("After Execute Player ID : " + playerId);
        EverdellGameState state = (EverdellGameState) gs;
        if(cardId == -1 || (!payWithResources && !payWithDiscount && !payWithOccupation)){
            System.out.println("SelectCard: execute - No Card Selected");
            gs.setActionInProgress(this);
        }
        else if(payWithOccupation && occupationId == -1){
            System.out.println("SelectCard: execute - No Occupation Selected");
            gs.setActionInProgress(this);
        }
        else if(payWithDiscount && resourcesSelected == null){
            System.out.println("SelectCard: execute - No Resources Selected");
            gs.setActionInProgress(this);
        }
        else if(payWithDiscount && discountMethodID != -1){
            System.out.println("SelectCard: execute - No Discount Method Selected");
            EverdellCard card = (EverdellCard) state.getComponentById(discountMethodID);
            if(card instanceof DungeonCard dc){
                if(secondaryCardId == -1){
                    gs.setActionInProgress(this);
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
            for (int cardId : cardsToSelectFromIds) {
                System.out.println("SelectCard CardID: " + cardId);
                EverdellCard card = (EverdellCard) egs.getComponentById(cardId);
                System.out.println("SelectCard Card Name : " + card.getCardEnumValue());
                //Test **********
                if(card == null){
                    System.out.println("Player ID is : " + playerId);
                    egs.printAllComponents();
                }
                //******
                if (canCardBePlayed(cardId, egs)) {
                    actions.add(new SelectCard(playerId, cardId, locationId, cardsToSelectFromIds));
                }
            }
        }
        else if (payWithOccupation) {
            //Select a card to occupy
            System.out.println("Selecting Occupation Card");
            for(EverdellCard card : egs.playerVillage.get(playerId)){
                if(card instanceof ConstructionCard cc){
                    EverdellCard cardTryingToOccupy = (EverdellCard) egs.getComponentById(cardId);
                    if(cc.canCardOccupyThis(egs, cardTryingToOccupy)){
                        actions.add(new SelectCard(playerId, cardId, locationId, card.getComponentID(), cardsToSelectFromIds, false, false, true, null));
                    }
                }
            }

        }
        else if (payWithDiscount){
            EverdellCard discountCard = (EverdellCard) egs.getComponentById(discountMethodID);
            if(resourcesSelected == null) {
                //Select exact discount you want to apply
                int discountAmount = 3;
                System.out.println("Selecting Discount");
                HashMap<EverdellParameters.ResourceTypes, Integer> amountOwned = new HashMap<>();
                for (var resource : egs.PlayerResources.keySet()) {
                    amountOwned.put(resource, egs.PlayerResources.get(resource)[playerId].getValue());
                }

                if(locationId!=-1){
                    EverdellLocation location = (EverdellLocation) egs.getComponentById(locationId);
                    if(location.getAbstractLocation() == EverdellParameters.ForestLocations.DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT){
                        discountAmount = 1;
                    }
                }

                computeAllValidDiscounts(actions, cardId, discountAmount, ((EverdellCard) egs.getComponentById(cardId)).getResourceCost(), amountOwned);
            }
            //Dungeon Card requires additional selection
            else if(discountCard instanceof DungeonCard dc){
                System.out.println("Selecting Critter Card for Dungeon");
                //Create an action for every critter card selection in the dungeon
                if(dc.isThereACellFree()){
                    System.out.println("There is a cell free");
                    for(EverdellCard card : egs.playerVillage.get(playerId)){
                        System.out.println("card in village for dungeon : " + card.getCardEnumValue());
                        if(card instanceof CritterCard && card.getCardEnumValue() != CardDetails.RANGER){
                            actions.add(new SelectCard(playerId, cardId, locationId, -1, new ArrayList<>(), false, true, false, resourcesSelected, discountMethodID, card.getComponentID()));
                        }
                    }
                }
            }
        }
        else{ // Select Payment Method

            System.out.println("Selecting Payment Method");

            if(locationId != -1){
                System.out.println("Location Id is not -1");
                actions.add(new SelectCard(playerId, cardId, locationId, -1, cardsToSelectFromIds, false, true, false, null, -1, secondaryCardId));
                return actions;
            }

            //If green production mode is enabled, the card is paid for already so we can ignore this step.
            if(egs.greenProductionMode){
                actions.add(new SelectCard(playerId, cardId, locationId, occupationId, cardsToSelectFromIds, true, false, false, null));
                return actions;
            }

            //Paying with resources
            if(thisCardCanBePaidByResources(cardId, egs)){
                System.out.println("Card can be paid with resources");
                actions.add(new SelectCard(playerId, cardId, locationId, occupationId, cardsToSelectFromIds, true, false, false, null));
                EverdellCard card = (EverdellCard) egs.getComponentById(cardId);

                //If the card is paid for, we default to paying with resources
                if(card.isCardPayedFor()){
                    System.out.println("Card is paid for in SELECTCARD");
                    return actions;
                }
            }

            //Paying with discount
            if(thisCardCanBePaidByDiscount(cardId, egs)){
                System.out.println("Card can be paid with discount");
                generateDiscountOptions(actions, cardId, egs);
            }

            //Paying with occupation
            if(thisCardCanOccupy(cardId, egs)){
                System.out.println("Card can be occupied");
                actions.add(new SelectCard(playerId, cardId, locationId, occupationId, cardsToSelectFromIds, false, false, true, null));
            }
        }
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
                System.out.println("Resource : " + resource + " Cost with discount : " + (resourceCost.get(resource) - selection.get(counter)));
                System.out.println("Amount Owned : " + amountOwned.get(resource));
                if(Math.max(resourceCost.get(resource) - selection.get(counter), 0) > amountOwned.get(resource)){
                    valid = false;
                }
                counter++;
            }
            if(valid){
                actions.add(new SelectCard(0, cardID, locationId, -1, new ArrayList<>(), false, true, false, possibleDiscount, discountMethodID, secondaryCardId));
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
                if(card.checkIfPlayerCanBuyCardWithDiscount(state, 3)){
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
                return card.checkIfPlayerCanBuyCardWithDiscount(state, 3);
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
    private boolean dungeonDiscountCheck(EverdellGameState state, EverdellCard cc){
        //Is there a critter card that can be placed in the dungeon
        boolean critterCardInVillage = false;
        for(var card : state.playerVillage.get(playerId)){
            if (card instanceof CritterCard && card.getCardEnumValue() != CardDetails.RANGER) {
                critterCardInVillage = true;
                break;
            }
        }

        return cc.checkIfPlayerCanBuyCardWithDiscount(state, 3) && critterCardInVillage;
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
                actions.add(new SelectCard(playerId, cardId, locationId, -1, new ArrayList<>(), false, true, false, null, dungeonCard.getComponentID(), secondaryCardId));
            }
        }

        if(card instanceof ConstructionCard cc){
            //Crane Discount
            if(craneDiscount){
                if(cc.checkIfPlayerCanBuyCardWithDiscount(state, 3)){
                    actions.add(new SelectCard(playerId, cardId, locationId, -1, new ArrayList<>(), false, true, false, null, craneCard.getComponentID(), secondaryCardId));
                }
            }
        }
        else if(card instanceof CritterCard cc){
            //Innkeeper Discount
            if(innKeeperDiscount){
                if(cc.checkIfPlayerCanBuyCardWithDiscount(state, 3)){
                    actions.add(new SelectCard(playerId, cardId, locationId, -1, new ArrayList<>(), false, true, false, null, innKeeperCard.getComponentID(), secondaryCardId));
                }
            }
        }
    }

    private boolean thisCardCanBePaidByResources(int cardId, EverdellGameState state){
        EverdellCard card = (EverdellCard) state.getComponentById(cardId);
        System.out.println("Is Card : " + card.getCardEnumValue() + " Payed For : " + card.isCardPayedFor());
        return card.checkIfPlayerCanBuyCard(state, playerId);
    }

    private boolean canCardBePlayed(int cardID, EverdellGameState state){
        EverdellCard card = (EverdellCard) state.getComponentById(cardID);
        PlayCard pc = new PlayCard(playerId, cardID, new ArrayList<>(), new HashMap<>());

        //Checking if we are using a location to apply a discount
        if(locationId != -1){
            EverdellLocation location = (EverdellLocation) state.getComponentById(locationId);
            if(location.getAbstractLocation() == EverdellParameters.RedDestinationLocation.INN_DESTINATION){
                return pc.checkIfVillageHasSpace(state, playerId) && card.checkIfPlayerCanBuyCardWithDiscount(state, 3) && card.checkIfPlayerCanPlaceThisUniqueCard(state, playerId);
            }
            else if(location.getAbstractLocation() == EverdellParameters.ForestLocations.DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT){
                System.out.println("Within locationId -1");
                System.out.println("Card LOOKING AT : "+ card.getCardEnumValue());
                System.out.println("Can Player buy with discount : "+ card.checkIfPlayerCanBuyCardWithDiscount(state, 1));
                return pc.checkIfVillageHasSpace(state, playerId) && card.checkIfPlayerCanBuyCardWithDiscount(state, 1) && card.checkIfPlayerCanPlaceThisUniqueCard(state, playerId);
            }
        }

        //Green Production card is not played. If the card is being copied, it is not played. We are only activating the effect
        if (state.greenProductionMode || state.copyMode){
            System.out.println("GREEN PROD OR COPY MODE IN SELECTCARD");
            return true;
        }

        //Fool is a special case, as it can be placed anywhere
        if(card.getCardEnumValue() == CardDetails.FOOL){
            return ((FoolCard) card).canFoolBePlaced(state, playerId) && card.checkIfPlayerCanBuyCard(state, playerId);
        }


        System.out.println("Standard payment check in SelectCard");
        System.out.println("Pay by Occupation : " + thisCardCanOccupy(cardID, state));
        System.out.println("Pay by Resources : " + thisCardCanBePaidByResources(cardID, state));
        System.out.println("Pay by Discount : " + thisCardCanBePaidByDiscount(cardID, state));
        //If the Village Has Space, AND (Can be bought by an occupation OR by resources OR by discount) AND the player can place this unique card
        return pc.checkIfVillageHasSpace(state, playerId) && (thisCardCanOccupy(cardID, state) || thisCardCanBePaidByResources(cardID, state) || thisCardCanBePaidByDiscount(cardID, state)) && card.checkIfPlayerCanPlaceThisUniqueCard(state, playerId);
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
        EverdellGameState egs = (EverdellGameState) state;
        System.out.println("Card Selected For Play : " + c.getCardEnumValue());
        System.out.println("Card Selected FOr Play  ID : " + c.getComponentID());

        boolean discountApplied = false;

        //If discount option was chosen, we must apply the discount
        if(selectCard.payWithDiscount && selectCard.resourcesSelected != null){
            //Discount Location Scenario
            egs.currentCard = c;
            if(locationId != -1){
                System.out.println("Discount Location Selected");
                EverdellLocation location = (EverdellLocation) egs.getComponentById(locationId);
                if(location.getAbstractLocation() == EverdellParameters.RedDestinationLocation.INN_DESTINATION) {
                    //If the location is the inn, we must place the card in the inn
                    System.out.println("Placing Card in Inn");
                    int locationCardID = EverdellLocation.findCardLinkedToLocation(egs, location);
                    InnCard innCard = (InnCard) egs.getComponentById(locationCardID);
                    innCard.setPlayers(playerId);
                }
                new PlaceWorker(playerId, selectCard.locationId, new ArrayList<>(List.of(c.getComponentID())), selectCard.resourcesSelected).execute(state);
                executed = true;
                return;
            }

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
                CritterCard ic = (CritterCard) cardApplyingDiscount;
                egs.resourceSelection = resourcesSelection;
                ic.applyCardEffect(egs);
                egs.playerVillage.get(playerId).remove(ic);
                egs.discardDeck.add(ic);
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
                    ArrayList<EverdellCard> cardsToPickFrom = egs.playerHands.get(playerId).getComponents().stream().filter(bardCard -> bardCard != card).collect(Collectors.toCollection(ArrayList::new));
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
                                new SelectCard(playerId, -1, locationId, -1, cardIds, true, false, false, null).execute(state);
                            }
                        }
                    }
                    else {
                        egs.copyMode = true;
                        egs.copyID = card.getComponentID();

                        new SelectCard(playerId, -1, locationId, -1, cardIds, true, false, false, null).execute(state);
                    }
                }
                else if(card.getCardEnumValue() == CardDetails.MINER_MOLE){
                    ArrayList<Integer> cardIds = new ArrayList<>();
                    for(int i=0; i<egs.getNPlayers(); i++){
                        if( i == playerId) continue;
                        cardIds.addAll(egs.playerVillage.get(i).stream().filter(greenCard -> greenCard.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).filter(greenCard -> greenCard.getCardEnumValue() != CardDetails.MINER_MOLE).map(EverdellCard::getComponentID).collect(Collectors.toCollection(ArrayList::new)));
                    }
                    if(cardIds.isEmpty()){
                        new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                    }
                    else if (egs.copyMode){ //In the scenario where a chipsweep is copying a miner mole, we do not want to enter an infinite loop of them copying eachother
                        EverdellCard copyCard = (EverdellCard) egs.getComponentById(egs.copyID);
                        if( copyCard.getCardEnumValue() == CardDetails.CHIP_SWEEP) {
                            for(int i=0; i<egs.getNPlayers(); i++){
                                if( i == playerId) continue;
                                cardIds = new ArrayList<>(egs.playerVillage.get(i).stream().filter(greenCard -> greenCard.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).filter(greenCard -> greenCard.getCardEnumValue() != CardDetails.MINER_MOLE).filter(greenCard -> greenCard.getCardEnumValue() != CardDetails.CHIP_SWEEP).map(EverdellCard::getComponentID).collect(Collectors.toCollection(ArrayList::new)));
                            }
                            if(cardIds.isEmpty()){
                                new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                            }
                            else{
                                new SelectCard(playerId, -1, locationId, -1, cardIds, true, false, false, null).execute(state);
                            }
                        }
                    }
                    else {
                        egs.copyMode = true;
                        egs.copyID = card.getComponentID();
                        new SelectCard(playerId, -1, locationId, -1, cardIds, true, false, false, null).execute(state);
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
                else if(card.getCardEnumValue() == CardDetails.RANGER){
                    //Create a list of locations that the player is ON
                    ArrayList<Integer> locationsToSelect = new ArrayList<>();
                    for(EverdellLocation location : egs.everdellLocations){
                        if(location.isPlayerOnLocation(egs.getCurrentPlayer())){
                            System.out.println("Location that is being selected for FROM : " + location.getAbstractLocation()+ " with ID : " + location.getComponentID());
                            locationsToSelect.add(location.getComponentID());
                        }
                    }
                    if (locationsToSelect.isEmpty()){
                        new PlayCard(playerId, selectCard.cardId, new ArrayList<>(), new HashMap<>()).execute(state);
                    }
                    else {
                        egs.rangerCardMode = true;
                        //Need to select a location to move FROM
                        System.out.println("Locations to select from : " + locationsToSelect);
                        new SelectLocation(playerId, -1, locationsToSelect).execute(egs);
                    }

                }
                else if(card.getCardEnumValue() == CardDetails.POSTAL_PIGEON){
                    ArrayList<EverdellCard> cardsToPickFrom = new ArrayList<>();
                    for(int i = 0; i < 2; i++){
                        EverdellCard ppCard = egs.cardDeck.draw();
                        if(ppCard.getPoints() <=3){
                            cardsToPickFrom.add(ppCard);
                            egs.temporaryDeck.add(ppCard);
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
                        new ResourceSelect(playerId, card.getComponentID(), -1, new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values())), 1, true, false).execute(state);
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
        SelectCard sc = new SelectCard(playerId, cardId, locationId, occupationId, cardsToSelectFromIds, payWithResources, payWithDiscount, payWithOccupation, resourcesSelected);
        sc.executed = executed;
        sc.discountMethodID = discountMethodID;
        sc.secondaryCardId = secondaryCardId;

        return sc;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectCard that = (SelectCard) o;
        return playerId == that.playerId && cardId == that.cardId && locationId == that.locationId && occupationId == that.occupationId && secondaryCardId == that.secondaryCardId && executed == that.executed && payWithResources == that.payWithResources && payWithDiscount == that.payWithDiscount && discountMethodID == that.discountMethodID && payWithOccupation == that.payWithOccupation && Objects.equals(cardsToSelectFromIds, that.cardsToSelectFromIds) && Objects.equals(resourcesSelected, that.resourcesSelected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, cardId, locationId, occupationId, secondaryCardId, executed, cardsToSelectFromIds, resourcesSelected, payWithResources, payWithDiscount, discountMethodID, payWithOccupation);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        if(cardId == -1){
            return toString();
        }
        EverdellCard card = (EverdellCard) gameState.getComponentById(cardId);
        return "Selecting a Card "+ card.getCardEnumValue();
    }

    @Override
    public String toString(){
        return "Selecting A Card";
    }

}
