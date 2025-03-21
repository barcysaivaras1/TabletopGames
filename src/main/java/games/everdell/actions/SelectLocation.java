package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.CemeteryCard;
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters.CardDetails;
import games.everdell.EverdellParameters.RedDestinationLocation;
import games.everdell.EverdellParameters.BasicEvent;
import games.everdell.EverdellParameters.HavenLocation;
import games.everdell.EverdellParameters.JourneyLocations;
import games.everdell.components.EverdellLocation;

import java.util.*;
import java.util.stream.Collectors;

public class SelectLocation extends AbstractAction implements IExtendedSequence {

    int playerId;
    int locationId;

    boolean executed;

    boolean loopAction;

    boolean value;

    //Each Locations Path

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
    * CEMETERY_DESTINATION -> SelectAListOfCards -> PlaceWorker -> SelectCard -> ...(Card Specific Actions)... -> PlayCard
    * CHAPEL_DESTINATION -> PlaceWorker
    * POST_OFFICE_DESTINATION -> SelectAListOfCard -> SelectAListOfCards -> SelectPlayer -> PlaceWorker
    * MONASTERY_DESTINATION -> ResourceSelect -> SelectPlayer -> PlaceWorker
    * CEMETERY_DESTINATION -> Select Discard Deck or Select Normal Deck -> SelectAListOfCards ->  PlaceWorker
    * UNIVERSITY_DESTINATION -> SelectAListOfCards -> ResourceSelect -> PlaceWorker
    * STOREHOUSE_DESTINATION -> PlaceWorker
    * LOOKOUT_DESTINATION -> SelectLocation -> ...Location Specific Actions ... -> PlaceWorker*/


    public SelectLocation(int playerId, int locationId) {
        this.playerId = playerId;
        this.locationId = locationId;
        this.loopAction = true;
        this.value = false;
    }
    public SelectLocation(int playerId, int locationId, boolean loopAction, boolean value) {
        this.playerId = playerId;
        this.locationId = locationId;
        this.loopAction = loopAction;
        this.value = value;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        System.out.println("SelectLocation: execute");
        EverdellGameState state = (EverdellGameState) gs;
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

            //actions.addAll(getBasicLocationActions(egs));
            //actions.addAll(getForestLocationActions(egs));
            //actions.addAll(getBasicEventActions(egs));
            //actions.addAll(getSpecialEventActions(egs));
            actions.addAll(getRedDestinationActions(egs));
            //actions.addAll(getHavenActions(egs));
            //actions.addAll(getJourneyActions(egs));
        }
        else{
            EverdellLocation location = (EverdellLocation) egs.getComponentById(locationId);
            //We need to select a basic location for this location to be able to work
            if(location.getAbstractLocation() == EverdellParameters.ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD){
                actions.addAll(getBasicLocationActions(egs));
            }
            else if(location.getAbstractLocation() == RedDestinationLocation.LOOKOUT_DESTINATION){
                //actions.addAll(getBasicLocationActions(egs));
                actions.addAll(getForestLocationActions(egs));
            }
            else if(location.getAbstractLocation() == RedDestinationLocation.CEMETERY_DESTINATION){
                actions.add(new SelectLocation(playerId, locationId, false, true));
                actions.add(new SelectLocation(playerId, locationId, false, false));
            }
        }

        return actions;
    }

    private List<AbstractAction> getBasicLocationActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();
        //Basic Locations
        for(EverdellLocation location : state.everdellLocations){
            if(location.getAbstractLocation() instanceof EverdellParameters.BasicLocations){
                if(locationIsFree(state, location)){
                    actions.add(new SelectLocation(playerId, location.getComponentID(),false, false));
                }
            }
        }
        return actions;
    }
    private List<AbstractAction> getForestLocationActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Forest Locations
        for(EverdellLocation location : state.everdellLocations){
            if(location.getAbstractLocation() instanceof EverdellParameters.ForestLocations){
                if(locationIsFree(state, location)){
                    actions.add(new SelectLocation(playerId, location.getComponentID(),false, false));
                }
            }
        }


        return actions;
    }

    private List<AbstractAction> getBasicEventActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Basic Locations
        for(EverdellLocation location : state.everdellLocations){
            if(location.getAbstractLocation() instanceof BasicEvent){
                if(locationIsFree(state, location)){
                    if(BasicEvent.defaultCheckIfConditionMet(state, (BasicEvent)location.getAbstractLocation())){
                        actions.add(new SelectLocation(playerId, location.getComponentID() ,false, false));
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
        for(EverdellLocation location : state.everdellLocations){
            if(location.getAbstractLocation() instanceof EverdellParameters.RedDestinationLocation){
                if(locationIsFree(state, location)){
                    actions.add(new SelectLocation(playerId, location.getComponentID(), false, false));
                }
            }
        }
        System.out.println("Red Destination Actions: " + actions);

        return actions;
    }

    private List<AbstractAction> getHavenActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Haven Locations
        for(EverdellLocation location : state.everdellLocations){
            if(location.getAbstractLocation() instanceof HavenLocation){
                if(locationIsFree(state, location)){
                    actions.add(new SelectLocation(playerId, location.getComponentID()));
                }
            }
        }

        return actions;
    }

    private List<AbstractAction> getJourneyActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Journey Locations
        for(EverdellLocation location : state.everdellLocations){
            if(location.getAbstractLocation() instanceof JourneyLocations jl){
                if(locationIsFree(state, location)){
                    if(JourneyLocations.defaultCheckIfConditionMet(state, jl)) {
                        actions.add(new SelectLocation(playerId, location.getComponentID()));
                    }
                }
            }
        }

        return actions;
    }

    private boolean locationIsFree(EverdellGameState state, EverdellLocation location){
        return location.isLocationFreeForPlayer(state) && state.workers[state.getCurrentPlayer()].getValue() > 0;
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

        //First Action
        if(loopAction){
            //Basic Location
            if(location.getAbstractLocation() instanceof EverdellParameters.BasicLocations){

                //COPY_BASIC_LOCATION_DRAW_CARD
                if(locationId != -1){
                    EverdellLocation basicL = (EverdellLocation) egs.getComponentById(locationId);
                    if(basicL.getAbstractLocation() == EverdellParameters.ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD ){
                        EverdellParameters.ForestLocations.basicLocationChoice = (EverdellParameters.BasicLocations) location.getAbstractLocation();
                        new PlaceWorker(state.getCurrentPlayer(), locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                    }
                }
                else { //STANDARD OUTCOME
                    new PlaceWorker(state.getCurrentPlayer(), selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
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
                        new SelectLocation(playerId, selectLocation.locationId).execute(state);
                    }
                }
                else {
                    new PlaceWorker(state.getCurrentPlayer(), selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                }
            }
            //Basic Event
            if(location.getAbstractLocation() instanceof BasicEvent){
                new PlaceWorker(state.getCurrentPlayer(), selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
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
                    cardsToSelectFrom.addAll(egs.playerHands.get(egs.getCurrentPlayer()).getComponents().stream().filter(card -> card.getPoints() <= 3).toList());
                    cardsToSelectFrom.addAll(egs.meadowDeck.getComponents().stream().filter(card -> card.getPoints() <= 3).toList());
                    cardsToSelectFrom = cardsToSelectFrom.stream().filter(card -> card.checkIfPlayerCanPlaceThisUniqueCard(egs, playerId)).collect(Collectors.toCollection(ArrayList::new));
                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 1, true).execute(egs);
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.CEMETERY_DESTINATION){
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>();
                    if(locationId == -1){
                        new SelectLocation(playerId, selectLocation.locationId).execute(state);
                    }
                    else {
                        //Select Discard Deck or Select Normal Deck
                        if (selectLocation.value && !egs.discardDeck.getComponents().isEmpty()) {
                            for (int i = 0; i < 4; i++) {
                                if(egs.discardDeck.getComponents().isEmpty()){
                                    break;
                                }
                                cardsToSelectFrom.add(egs.discardDeck.draw());
                            }
                        } else {
                            for (int i = 0; i < 4; i++) {
                                cardsToSelectFrom.add(egs.cardDeck.draw());
                            }
                        }
                        if(cardsToSelectFrom.isEmpty()){
                            new PlaceWorker(playerId, selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                        }
                        else {
                            //All invalid cards we place in the card selection for discarding
                            egs.cardSelection.addAll(cardsToSelectFrom.stream().filter(card -> !card.checkIfPlayerCanPlaceThisUniqueCard(egs, playerId)).collect(Collectors.toCollection(ArrayList::new)));
                            cardsToSelectFrom.removeIf(card -> !card.checkIfPlayerCanPlaceThisUniqueCard(egs, playerId));
                            new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 1, true).execute(egs);
                        }
                    }
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.UNIVERSITY_DESTINATION){
                    ArrayList<EverdellCard> cardsToSelectFrom = egs.playerVillage.get(egs.getCurrentPlayer()).getComponents().stream().filter(card -> card.getCardEnumValue() != CardDetails.UNIVERSITY).collect(Collectors.toCollection(ArrayList::new));
                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 1, true).execute(egs);
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.POST_OFFICE_DESTINATION){
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(egs.getCurrentPlayer()).getComponents());
                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 2, true).execute(egs);
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.MONASTERY_DESTINATION){
                    new ResourceSelect(playerId, -1, selectLocation.locationId, new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values())), 2, false, true).execute(egs);
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.LOOKOUT_DESTINATION){
                    //We would be looping back for another selection
                    if(locationId == -1){
                        egs.copyMode = true;
                        egs.copyID = selectLocation.locationId;
                        new SelectLocation(playerId, selectLocation.locationId).execute(state);
                    }
                }
                else{ //CHAPEL_DESTINATION or STOREHOUSE_DESTINATION
                    new PlaceWorker(state.getCurrentPlayer(), selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
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
        SelectLocation retValue = new SelectLocation(playerId, locationId, loopAction, value);
        retValue.executed = executed;
        return retValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectLocation that = (SelectLocation) o;
        return playerId == that.playerId && locationId == that.locationId && executed == that.executed && loopAction == that.loopAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, locationId, executed, loopAction);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return "Selecting a Location";
    }

    @Override
    public String toString(){
        return "Selecting A Location";
    }

}
