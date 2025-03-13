package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters.CardDetails;
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

    boolean isFirstAction;

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
    /*Queen -> SelectAListOfCards -> PlaceWorker -> SelectCard.AfterAction -> ...(Card Specific Actions)... -> PlayCard
    * CHAPEL -> PlaceWorker*/


    public SelectLocation(int playerId, int locationId, boolean isFirstAction) {
        this.playerId = playerId;
        this.locationId = locationId;
        this.isFirstAction = isFirstAction;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        System.out.println("SelectLocation: execute");
        EverdellGameState state = (EverdellGameState) gs;
        if(isFirstAction){
            state.setActionInProgress(this);
        }
        else{
            EverdellLocation location = (EverdellLocation) state.getComponentById(locationId);

            //We need to select a basic location for this location to be able to work
            if(location.getAbstractLocation() == EverdellParameters.ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD){
                System.out.println("SelectLocation: execute: Need to select a basic location first");
                state.setActionInProgress(this);
            }
        }
        return true;
    }



    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        System.out.println("SelectLocation: _computeAvailableActions");
        List<AbstractAction> actions = new ArrayList<>();

        EverdellGameState egs = (EverdellGameState) state;

        if(isFirstAction){

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
        }

        return actions;
    }

    private List<AbstractAction> getBasicLocationActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();
        //Basic Locations
        for(EverdellLocation location : state.everdellLocations){
            if(location.getAbstractLocation() instanceof EverdellParameters.BasicLocations){
                if(locationIsFree(state, location)){
                    actions.add(new SelectLocation(playerId, location.getComponentID(), false));
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
                    actions.add(new SelectLocation(playerId, location.getComponentID(), false));
                }
            }
        }


        return actions;
    }

    private List<AbstractAction> getBasicEventActions(EverdellGameState state){
        List<AbstractAction> actions = new ArrayList<>();

        //Basic Locations
        for(EverdellLocation location : state.everdellLocations){
            if(location.getAbstractLocation() instanceof EverdellParameters.BasicEvent){
                if(locationIsFree(state, location)){
                    if(BasicEvent.defaultCheckIfConditionMet(state, (BasicEvent)location.getAbstractLocation())){
                        actions.add(new SelectLocation(playerId, location.getComponentID(), false));
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
                    actions.add(new SelectLocation(playerId, location.getComponentID(), false));
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
                    actions.add(new SelectLocation(playerId, location.getComponentID(), false));
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
                    if(EverdellParameters.JourneyLocations.defaultCheckIfConditionMet(state, jl)) {
                        actions.add(new SelectLocation(playerId, location.getComponentID(), false));
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
        if(isFirstAction){
            //Basic Location
            if(location.getAbstractLocation() instanceof EverdellParameters.BasicLocations){
                new PlaceWorker(state.getCurrentPlayer(), selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
            }

            //Forest Location
            if(location.getAbstractLocation() instanceof EverdellParameters.ForestLocations) {

                if (location.getAbstractLocation() == EverdellParameters.ForestLocations.TWO_ANY) {
                    ArrayList<EverdellParameters.ResourceTypes> resourcesToSelect = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values()));
                    new ResourceSelect(playerId, -1, selectLocation.locationId, resourcesToSelect, 2, false, true, true).execute(egs);

                } else if (location.getAbstractLocation() == EverdellParameters.ForestLocations.TWO_CARDS_ONE_ANY) {
                    ArrayList<EverdellParameters.ResourceTypes> resourcesToSelect = new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values()));
                    new ResourceSelect(playerId, -1, selectLocation.locationId, resourcesToSelect, 1, false, true, true).execute(egs);

                } else if (location.getAbstractLocation() == EverdellParameters.ForestLocations.DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED) {
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, cardsToSelectFrom.size(), false).execute(egs);

                } else if (location.getAbstractLocation() == EverdellParameters.ForestLocations.DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED) {
                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 3, false).execute(egs);
                }
                else if (location.getAbstractLocation() == EverdellParameters.ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD) {
                    //We would be looping back for another selection
                }
                else {
                    new PlaceWorker(state.getCurrentPlayer(), selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                }
            }
            //Basic Event
            if(location.getAbstractLocation() instanceof EverdellParameters.BasicEvent){
                new PlaceWorker(state.getCurrentPlayer(), selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
            }

            //Special Event


            //Haven
            if(location.getAbstractLocation() instanceof EverdellParameters.HavenLocation){
                ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
                new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, cardsToSelectFrom.size(), false).execute(egs);
            }

            //Journey
            if(location.getAbstractLocation() instanceof EverdellParameters.JourneyLocations jl){
                ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>(egs.playerHands.get(playerId).getComponents());
                new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, jl.cardsNeededToDiscard(), true).execute(egs);
            }

            //Red Destination
            if(location.getAbstractLocation() instanceof EverdellParameters.RedDestinationLocation){
                if(location.getAbstractLocation() == EverdellParameters.RedDestinationLocation.QUEEN_DESTINATION){

                    ArrayList<EverdellCard> cardsToSelectFrom = new ArrayList<>();
                    cardsToSelectFrom.addAll(egs.playerHands.get(egs.getCurrentPlayer()).getComponents().stream().filter(card -> card.getPoints() <= 3).toList());
                    cardsToSelectFrom.addAll(egs.meadowDeck.getComponents().stream().filter(card -> card.getPoints() <= 3).toList());
                    new SelectAListOfCards(playerId, selectLocation.locationId, -1, cardsToSelectFrom, 1, true).execute(egs);
                }
                else{
                    new PlaceWorker(state.getCurrentPlayer(), selectLocation.locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                }
            }
        }
        else{ //Not the first action
            //locationId is the orginal location that was selected
            //SelectLocation is the location that was selected after the first action
            EverdellLocation originalLocation = (EverdellLocation) egs.getComponentById(locationId);
            EverdellLocation selectedLocation = (EverdellLocation) egs.getComponentById(selectLocation.locationId);

            if(originalLocation.getAbstractLocation() == EverdellParameters.ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD){
                EverdellParameters.ForestLocations.basicLocationChoice = (EverdellParameters.BasicLocations) selectedLocation.getAbstractLocation();
                new PlaceWorker(state.getCurrentPlayer(), locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
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
        SelectLocation retValue = new SelectLocation(playerId, locationId, isFirstAction);
        retValue.executed = executed;
        return retValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectLocation that = (SelectLocation) o;
        return playerId == that.playerId && locationId == that.locationId && executed == that.executed && isFirstAction == that.isFirstAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, locationId, executed, isFirstAction);
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
