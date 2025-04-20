package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.*;
import games.everdell.EverdellParameters.CardDetails;
import games.everdell.EverdellParameters.RedDestinationLocation;
import games.everdell.EverdellParameters.BasicEvent;
import games.everdell.EverdellParameters.HavenLocation;
import games.everdell.EverdellParameters.JourneyLocations;

import java.util.*;
import java.util.stream.Collectors;

public class SelectLocation extends AbstractAction implements IExtendedSequence {

    int playerId;
    int locationId;

    ArrayList<Integer> everdellLocationIDs;

    boolean executed;

    boolean loopAction;

    boolean value;

    /* ****************** Each Locations Action Call Path ****************** */
    //Basic Locations
    // All Locations -> PlaceWorker


    //Forest Locations
    /* THREE_BEERY -> PlaceWorker
    *  TWO_RESIN_ONE_TWIG -> PlaceWorker
    *  THREE_CARDS_ONE_PEBBLE -> PlaceWorker
    *  ONE_TWIG_ONE_RESIN_ONE_BERRY ->
    *  TWO_ANY -> ResourceSelect -> PlaceWorker
    *  TWO_CARDS_ONE_ANY -> ResourceSelect -> PlaceWorker
    *  DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED -> SelectAListOfCards -> PlaceWorker
    *  DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED -> SelectAListOfCards -> ResourceSelect -> PlaceWorker
    *  DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT -> SelectAListOfCards (2 Meadow Cards) -> SelectAListOfCards (Selecting 1 specific card) -> PlaceWorker -> (Go through steps to place the card)
    *  COPY_BASIC_LOCATION_DRAW_CARD -> SelectLocation(Basic Location) -> PlaceWorker
    *  */

    //Basic Events
    /* All Basic Events -> PlaceWorker */

    //Haven
    /* Haven -> SelectAListOfCards -> ResourceSelect -> PlaceWorker */

    //Journey
    /* All Journey Locations -> SelectAListOfCards -> PlaceWorker */

    //Red Destination
    /*QUEEN_DESTINATION -> SelectAListOfCards -> PlaceWorker -> SelectCard -> ...(Card Specific Actions)... -> PlayCard
    * CHAPEL_DESTINATION -> PlaceWorker
    * POST_OFFICE_DESTINATION -> SelectAListOfCard -> SelectAListOfCards -> SelectPlayer -> PlaceWorker
    * MONASTERY_DESTINATION -> ResourceSelect -> SelectPlayer -> PlaceWorker
    * CEMETERY_DESTINATION -> Select Discard Deck or Select Normal Deck -> SelectAListOfCards ->  PlaceWorker
    * UNIVERSITY_DESTINATION -> SelectAListOfCards -> ResourceSelect -> PlaceWorker
    * STOREHOUSE_DESTINATION -> PlaceWorker
    * LOOKOUT_DESTINATION -> SelectLocation -> ...Location Specific Actions... -> PlaceWorker
    * INN_DESTINATION -> SelectAListOfCards -> PlaceAWorker -> SelectCard(With selected card) -> ...Card Specific Actions -> PlayCard*/


    public SelectLocation(int playerId, int locationId, ArrayList<Integer> everdellLocationIDs) {
        this.playerId = playerId;
        this.locationId = locationId;
        this.everdellLocationIDs = everdellLocationIDs;
        this.loopAction = true;
        this.value = false;
    }
    public SelectLocation(int playerId, int locationId, ArrayList<Integer> everdellLocationIDs, boolean loopAction, boolean value) {
        this.playerId = playerId;
        this.locationId = locationId;
        this.everdellLocationIDs = everdellLocationIDs;
        this.loopAction = loopAction;
        this.value = value;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        System.out.println("SelectLocation: execute");
        EverdellGameState state = (EverdellGameState) gs;
        playerId = gs.getCurrentPlayer();
        if(loopAction){
            state.setActionInProgress(this);
        }
        return true;
    }



    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        System.out.println("SelectLocation: _computeAvailableActions");

        List<AbstractAction> actions = new ArrayList<>();

        EverdellGameState egs = (EverdellGameState) state;

        if(locationId == -1){
            if(egs.clockTowerMode){
                actions.addAll(getBasicLocationActions(egs));
                actions.addAll(getForestLocationActions(egs));
            }
            else {
                actions.addAll(getBasicLocationActions(egs));
                actions.addAll(getForestLocationActions(egs));
                actions.addAll(getBasicEventActions(egs));
                //actions.addAll(getSpecialEventActions(egs));
                actions.addAll(getRedDestinationActions(egs));
                actions.addAll(getHavenActions(egs));
                actions.addAll(getJourneyActions(egs));
            }
        }
        else{
            EverdellLocation location = (EverdellLocation) egs.getComponentById(locationId);
            //We need to select a basic location for this location to be able to work
            if(location.getAbstractLocation() == EverdellParameters.ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD){
                //Add every basic location to the list of actions
                for(int locationID : everdellLocationIDs){
                    EverdellLocation basicLocation = (EverdellLocation) egs.getComponentById(locationID);
                    if(basicLocation.getAbstractLocation() instanceof EverdellParameters.BasicLocations){
                        actions.add(new SelectLocation(playerId, locationID, everdellLocationIDs,false, false));
                    }
                }
                //actions.addAll(getBasicLocationActions(egs));
            }
            else if(location.getAbstractLocation() == RedDestinationLocation.LOOKOUT_DESTINATION){
                actions.addAll(getBasicLocationActions(egs));
                actions.addAll(getForestLocationActions(egs));
            }
            else if(location.getAbstractLocation() == RedDestinationLocation.CEMETERY_DESTINATION){
                actions.add(new SelectLocation(playerId, locationId, everdellLocationIDs, false, true));
                actions.add(new SelectLocation(playerId, locationId, everdellLocationIDs, false, false));
            }
        }

        return actions;
    }

    private List<AbstractAction> getBasicLocationActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Basic Locations
        for(int locationID : everdellLocationIDs){
            EverdellLocation location = (EverdellLocation) state.getComponentById(locationID);
            if(location.getAbstractLocation() instanceof EverdellParameters.BasicLocations){
                if(state.clockTowerMode || state.copyMode){ // If we are copying, we can select any location
                    actions.add(new SelectLocation(playerId, location.getComponentID(), everdellLocationIDs,false, false));
                }
                else if(locationIsFree(state, location)){
                    actions.add(new SelectLocation(playerId, location.getComponentID(), everdellLocationIDs,false, false));
                }
            }
        }
        return actions;
    }
    private List<AbstractAction> getForestLocationActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Forest Locations
        for(int locationID : everdellLocationIDs){
            EverdellLocation location = (EverdellLocation) state.getComponentById(locationID);

            //DEBUGGING
//            if(location.getAbstractLocation() == EverdellParameters.ForestLocations.DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT){
//                continue;
//            }
            //*******


            if(location.getAbstractLocation() instanceof EverdellParameters.ForestLocations){
                if(state.clockTowerMode || state.copyMode){ //If we are copying, we can select any location

                    //Choosing to skip this scenario, too much effort to implement such a specific case
                    if(state.clockTowerMode && location.getAbstractLocation() == EverdellParameters.ForestLocations.DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT){
                        continue;
                    }
                    actions.add(new SelectLocation(playerId, location.getComponentID(), everdellLocationIDs,false, false));
                }
                else if(locationIsFree(state, location)){
                    actions.add(new SelectLocation(playerId, location.getComponentID(), everdellLocationIDs,false, false));
                }
            }
        }


        return actions;
    }

    private List<AbstractAction> getBasicEventActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Basic Locations
        for(int locationID : everdellLocationIDs){
            EverdellLocation location = (EverdellLocation) state.getComponentById(locationID);
            if(location.getAbstractLocation() instanceof BasicEvent){
                if(locationIsFree(state, location)){
                    if(BasicEvent.defaultCheckIfConditionMet(state, (BasicEvent)location.getAbstractLocation()) || state.rangerCardMode){
                        actions.add(new SelectLocation(playerId, location.getComponentID(), everdellLocationIDs ,false, false));
                    }
                }
            }
        }

        return actions;
    }

    private List<AbstractAction> getSpecialEventActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        return actions;
    }

    private List<AbstractAction> getRedDestinationActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Red Destination Locations
        for(int locationID : everdellLocationIDs){
            EverdellLocation location = (EverdellLocation) state.getComponentById(locationID);
            if(location.getAbstractLocation() instanceof EverdellParameters.RedDestinationLocation){
                if(locationIsFree(state, location)){
                    System.out.println("Red Destination Location Get ID : " + location.getComponentID());
                    System.out.println("Red Destination Location static ID : " + locationID);
                    actions.add(new SelectLocation(playerId, locationID, everdellLocationIDs, false, false));
                }
            }
        }
        System.out.println("Red Destination Actions: " + actions);

        return actions;
    }

    private List<AbstractAction> getHavenActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Haven Locations
        for(int locationID : everdellLocationIDs){
            EverdellLocation location = (EverdellLocation) state.getComponentById(locationID);
            if(location.getAbstractLocation() instanceof HavenLocation){
                if(locationIsFree(state, location)){
                    actions.add(new SelectLocation(playerId, location.getComponentID(), everdellLocationIDs, false, false));
                }
            }
        }

        return actions;
    }

    private List<AbstractAction> getJourneyActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        System.out.println("Journey Locations");
        System.out.println("IS range card mode activated !? : " + state.rangerCardMode);
        //Journey Locations
        for(int locationID : everdellLocationIDs){
            EverdellLocation location = (EverdellLocation) state.getComponentById(locationID);
            if(location.getAbstractLocation() instanceof JourneyLocations jl){
                System.out.println("Is location free ? : " + locationIsFree(state, location));
                if(locationIsFree(state, location)){
                    if(JourneyLocations.defaultCheckIfConditionMet(state, jl) || state.rangerCardMode) {
                        actions.add(new SelectLocation(playerId, location.getComponentID(), everdellLocationIDs, false, false));
                    }
                }
            }
        }

        return actions;
    }

    private boolean locationIsFree(EverdellGameState state, EverdellLocation location){
        if(state.rangerCardMode){
            return true;
        }

        return location.isLocationFreeForPlayer(state) && state.workers[playerId].getValue() > 0;
    }


    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        System.out.println("SelectLocation: _afterAction");
        EverdellGameState egs = (EverdellGameState) state;

        SelectLocation selectLocation = (SelectLocation) action;
        EverdellLocation location = (EverdellLocation) egs.getComponentById(selectLocation.locationId);
        System.out.println("Location Selected: " + location.getAbstractLocation());


        //This is a special case for the ranger card, as it is not a location
        if(egs.rangerCardMode){
            //Find card
            int cardID = -1;
            ArrayList<EverdellCard> cardsToLookThrough = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
            cardsToLookThrough.addAll(egs.meadowDeck.getComponents());
            cardsToLookThrough.addAll(egs.temporaryDeck.getComponents());
            System.out.println("In Ranger Card Mode. PlayerID is : " + playerId);
            for(EverdellCard card : cardsToLookThrough){
                System.out.println("Looking for ranger card, card found : "+ card.getCardEnumValue());
                if(card.getCardEnumValue() == CardDetails.RANGER){
                    cardID = card.getComponentID();
                    break;
                }
            }
            RangerCard rc = (RangerCard) egs.getComponentById(cardID);
            rc.setLocationFrom(location);
            new PlayCard(playerId, cardID, new ArrayList<>(), new HashMap<>()).execute(egs);
            executed = true;
            return;
        }

        //First Action
        if(loopAction){
            System.out.println("Location ID : " + selectLocation.locationId);
            //Basic Location
            if(location.getAbstractLocation() instanceof EverdellParameters.BasicLocations){

                //COPY_BASIC_LOCATION_DRAW_CARD
                if(locationId != -1){
                    EverdellLocation basicL = (EverdellLocation) egs.getComponentById(locationId);
                    if(basicL.getAbstractLocation() == EverdellParameters.ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD ){
                        EverdellParameters.ForestLocations.basicLocationChoice = (EverdellParameters.BasicLocations) location.getAbstractLocation();
                        new PlaceWorker(playerId, locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                    }
                }
                else { //STANDARD OUTCOME
                    new PlaceWorker(playerId, selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                }
            }

            //Forest Location
            if(location.getAbstractLocation() instanceof EverdellParameters.ForestLocations) {

                if (location.getAbstractLocation() == EverdellParameters.ForestLocations.TWO_ANY) {
                    ArrayList<EverdellParameters.ResourceTypes> resourcesToSelect = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values()));
                    new ResourceSelect(playerId, -1, selectLocation.locationId, resourcesToSelect, 2, true, false).execute(egs);

                } else if (location.getAbstractLocation() == EverdellParameters.ForestLocations.TWO_CARDS_ONE_ANY) {
                    ArrayList<EverdellParameters.ResourceTypes> resourcesToSelect = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values()));
                    new ResourceSelect(playerId, -1, selectLocation.locationId, resourcesToSelect, 1, true, false).execute(egs);

                } else if (location.getAbstractLocation() == EverdellParameters.ForestLocations.DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED) {
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, cardsToSelectFrom.size(), false).execute(egs);

                } else if (location.getAbstractLocation() == EverdellParameters.ForestLocations.DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED) {
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 3, false).execute(egs);
                }
                else if (location.getAbstractLocation() == EverdellParameters.ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD) {
                    //We would be looping back for another selection
                    //This resolves in the basic location ifstatement
                    if(locationId == -1 || egs.copyMode){
                        new SelectLocation(playerId, selectLocation.locationId, everdellLocationIDs).execute(state);
                    }
                }
                else if(location.getAbstractLocation() == EverdellParameters.ForestLocations.DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT) {
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.meadowDeck.getComponents());
                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 2, true).execute(egs);
                }
                else {
                    //Standard Action
                    new PlaceWorker(playerId, selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                }
            }
            //Basic Event
            if(location.getAbstractLocation() instanceof BasicEvent){
                new PlaceWorker(playerId, selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
            }

            //Special Event


            //Haven
            if(location.getAbstractLocation() instanceof HavenLocation){
                ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
                new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, cardsToSelectFrom.size(), false).execute(egs);
            }

            //Journey
            if(location.getAbstractLocation() instanceof JourneyLocations jl){
                ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
                new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, jl.cardsNeededToDiscard(), true).execute(egs);
            }

            //Red Destination
            if(location.getAbstractLocation() instanceof RedDestinationLocation){
                if(location.getAbstractLocation() == RedDestinationLocation.QUEEN_DESTINATION){
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>();
                    for(var card : egs.playerHands.get(playerId)){
                        if(card.getPoints() <= 3){
                            cardsToSelectFrom.add(card);
                        }
                    }
                    for(var card : egs.meadowDeck){
                        if(card.getPoints() <= 3){
                            cardsToSelectFrom.add(card);
                        }
                    }
//                    cardsToSelectFrom.addAll(egs.playerHands.get(playerId).getComponents().stream().filter(card -> card.getPoints() <= 3).toList());
//                    cardsToSelectFrom.addAll(egs.meadowDeck.getComponents().stream().filter(card -> card.getPoints() <= 3).toList());

                    cardsToSelectFrom.removeIf(card -> !card.checkIfPlayerCanPlaceThisUniqueCard(egs, playerId));

                    //Look for fool to check for edge case
                    for(EverdellCard card : cardsToSelectFrom){
                        if(card.getCardEnumValue() == CardDetails.FOOL){
                            FoolCard fc = (FoolCard) card;
                            if(!fc.canFoolBePlaced(egs, playerId)){
                                cardsToSelectFrom.remove(card);
                            }
                            break;
                        }
                    }

                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 1, true).execute(egs);
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.CEMETERY_DESTINATION){
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>();
                    if(locationId == -1){
                        new SelectLocation(playerId, selectLocation.locationId, everdellLocationIDs).execute(state);
                    }
                    else {
                        //Select Discard Deck or Select Normal Deck
                        if (selectLocation.value && !egs.discardDeck.getComponents().isEmpty()) {
                            for (int i = 0; i < 4; i++) {
                                if(egs.discardDeck.getComponents().isEmpty()){
                                    break;
                                }
                                EverdellCard drawnCard = egs.discardDeck.draw();
                                cardsToSelectFrom.add(drawnCard);
                                egs.temporaryDeck.add(drawnCard);

                            }
                        } else {
                            for (int i = 0; i < 4; i++) {
                                EverdellCard drawnCard = egs.cardDeck.draw();
                                cardsToSelectFrom.add(drawnCard);
                                egs.temporaryDeck.add(drawnCard);
                            }
                        }
                        for(var card: cardsToSelectFrom){
                            if(!card.checkIfPlayerCanPlaceThisUniqueCard(egs,playerId)){
                                egs.discardDeck.add(card);
                                cardsToSelectFrom.remove(card);
                            }
                        }
                        //cardsToSelectFrom.removeIf(card -> !card.checkIfPlayerCanPlaceThisUniqueCard(egs, playerId));


                        //Look for fool to check for edge case
                        for(EverdellCard card : cardsToSelectFrom){
                            if(card instanceof FoolCard fc){
                                if(!fc.canFoolBePlaced(egs, playerId)){
                                    cardsToSelectFrom.remove(card);
                                    egs.discardDeck.add(card);
                                }
                            }
                        }

                        if(cardsToSelectFrom.isEmpty()){
                            new PlaceWorker(playerId, selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                        }
                        else {
                            //All invalid cards we place in the card selection for discarding
                            //egs.cardSelection.addAll(cardsToSelectFrom.stream().filter(card -> !card.checkIfPlayerCanPlaceThisUniqueCard(egs, playerId)).collect(Collectors.toCollection(ArrayList::new)));
                            new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 1, true).execute(egs);
                        }
                    }
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.UNIVERSITY_DESTINATION){
                    //ArrayList<EverdellCard> cardsToSelectFrom = egs.playerVillage.get(playerId).getComponents().stream().filter(card -> card.getCardEnumValue() != CardDetails.UNIVERSITY).collect(Collectors.toCollection(ArrayList::new));
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerVillage.get(playerId).getComponents());
                    cardsToSelectFrom.removeIf(card -> card.getCardEnumValue() == CardDetails.UNIVERSITY);
                    System.out.println("UNIVERSITY CARD TO SELECT FROM :"+ cardsToSelectFrom);
                    if(cardsToSelectFrom.isEmpty()){
                        //if university is the only card in the village, we can just place the worker
                        new PlaceWorker(playerId, selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                    }
                    else {
                        new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 1, true).execute(egs);
                    }
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.INN_DESTINATION){
                    ArrayList<Integer> cardsToSelectFrom = new ArrayList<>();
                    for(EverdellCard card : egs.meadowDeck.getComponents()){

                        //Check if its unique and can be played
                        if(!card.checkIfPlayerCanPlaceThisUniqueCard(egs, playerId)){
                            continue;
                        }
                        //Check for space
                        if(egs.villageMaxSize[playerId].getValue() <= egs.playerVillage.get(playerId).getSize()){
                            continue;
                        }
                        //Check if the card can be played with the discount
                        if(!card.checkIfPlayerCanBuyCardWithDiscount(egs, 3)){
                            continue;
                        }

                        //If the card can be played, we can select it
                        cardsToSelectFrom.add(card.getComponentID());
                    }
                    //If there were no valid cards, we can just place the worker, no action occurs
                    if(cardsToSelectFrom.isEmpty()){
                        int locationCardID = EverdellLocation.findCardLinkedToLocation(egs, location);
                        InnCard innCard = (InnCard) egs.getComponentById(locationCardID);
                        innCard.setPlayers(playerId);
                        new PlaceWorker(playerId, selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                    }
                    else {
                        egs.cardSelection.clear();
                        new SelectCard(playerId, -1, selectLocation.locationId, cardsToSelectFrom).execute(egs);
                    }
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.POST_OFFICE_DESTINATION){
                    if(egs.playerHands.get(playerId).getComponents().size() < 2){
                        //They need at least 2 cards to trigger the effect
                        new PlaceWorker(playerId, selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                    }
                    else {
                        ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
                        new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 2, true).execute(egs);
                    }
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.MONASTERY_DESTINATION){
                    new ResourceSelect(playerId, -1, selectLocation.locationId, new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values())), 2, false, true).execute(egs);
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.LOOKOUT_DESTINATION){
                    //We would be looping back for another selection
                    if(locationId == -1){
                        egs.copyMode = true;
                        egs.copyID = selectLocation.locationId;
                        new SelectLocation(playerId, selectLocation.locationId, everdellLocationIDs).execute(state);
                    }
                }
                else{ //CHAPEL_DESTINATION or STOREHOUSE_DESTINATION
                    new PlaceWorker(playerId, selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
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
    public SelectLocation copy() {
        ArrayList<Integer> everdellLocationIDs = new ArrayList<>(this.everdellLocationIDs);
        SelectLocation retValue = new SelectLocation(playerId, locationId, everdellLocationIDs, loopAction, value);
        retValue.executed = executed;
        return retValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectLocation that = (SelectLocation) o;
        return playerId == that.playerId && locationId == that.locationId && executed == that.executed && loopAction == that.loopAction && value == that.value && Objects.equals(everdellLocationIDs, that.everdellLocationIDs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, locationId, everdellLocationIDs, executed, loopAction, value);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        if(locationId == -1){
            return toString();
        }
        EverdellLocation location = (EverdellLocation) gameState.getComponentById(locationId);
        return toString()+ ", " + location.getAbstractLocation();
    }

    @Override
    public String toString(){
        return "Selecting A Location";
    }

}
