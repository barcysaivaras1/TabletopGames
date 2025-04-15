package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.*;
import games.everdell.EverdellParameters.CardDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SelectPlayer extends AbstractAction implements IExtendedSequence {

    int playerId;
    int cardId;
    int locationId;

    int playerSelectedId;

    boolean executed;

    //This is to be used to select a player to perform an action on


    public SelectPlayer(int playerId, int cardId, int locationId) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.locationId = locationId;
        this.playerSelectedId = -1;
    }

    private SelectPlayer(int playerId, int cardId, int locationId, int playerSelectedId) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.locationId = locationId;
        this.playerSelectedId = playerSelectedId;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        System.out.println("SelectPlayer: execute");
        EverdellGameState state = (EverdellGameState) gs;
        if(playerSelectedId == -1){
            state.setActionInProgress(this);
        }
        return true;
    }



    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        System.out.println("SelectPlayer: _computeAvailableActions");
        List<AbstractAction> actions = new ArrayList<>();

        EverdellGameState egs = (EverdellGameState) state;

        if(cardId !=-1){
            EverdellCard card = (EverdellCard) egs.getComponentById(cardId);
            if(card.getCardEnumValue() == CardDetails.FOOL){
                FoolCard fc = (FoolCard) card;
                for(int i = 0; i < egs.getNPlayers(); i++){
                    if(fc.canFoolBePlacedInThisPlayersVillage(egs, i)){
                        actions.add(new SelectPlayer(playerId, cardId, locationId, i));
                    }
                }
            }
            else{
                for (int i = 0; i < egs.getNPlayers(); i++) {
                    if (i != playerId) {
                        actions.add(new SelectPlayer(playerId, cardId, locationId, i));
                    }
                }
            }
        }
        else {
            for (int i = 0; i < egs.getNPlayers(); i++) {
                if (i != playerId) {
                    actions.add(new SelectPlayer(playerId, cardId, locationId, i));
                }
            }
        }

        return actions;
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        System.out.println("SelectPlayer: _afterAction");

        EverdellGameState egs = (EverdellGameState) state;

        SelectPlayer sp = (SelectPlayer) action;

        //Ids of all the cards selected
        ArrayList<Integer> cardIds = new ArrayList<>();
        for(EverdellCard c : egs.cardSelection){
            cardIds.add(c.getComponentID());
        }

        //Counter to Values conversion
        HashMap<EverdellParameters.ResourceTypes, Integer> resourcesForAction = new HashMap<>();
        for(var resource : egs.resourceSelection.keySet()){
            resourcesForAction.put(resource, egs.resourceSelection.get(resource).getValue());
        }

        if(cardId != -1){
            EverdellCard card = (EverdellCard) egs.getComponentById(cardId);
            if(card.getCardEnumValue() == CardDetails.TEACHER){
                TeacherCard tc = (TeacherCard) card;
                tc.setSelectedPlayer(sp.playerSelectedId);
                new PlayCard(playerId, cardId, cardIds, new HashMap<>()).execute(state);
            }
            else if(card.getCardEnumValue() == CardDetails.FOOL){
                FoolCard fc = (FoolCard) card;
                fc.setSelectedPlayer(sp.playerSelectedId);
                new PlayCard(playerId, cardId, cardIds, new HashMap<>()).execute(state);
            }
            else if(card.getCardEnumValue() == CardDetails.MONK){
                MonkCard mc = (MonkCard) card;
                mc.setSelectedPlayer(sp.playerSelectedId);
                new PlayCard(playerId, cardId, cardIds, resourcesForAction).execute(state);
            }
            else if(card.getCardEnumValue() == CardDetails.SHEPHERD){
                ShepherdCard sc = (ShepherdCard) card;
                sc.setBeforePR(egs.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[playerId].getValue());
                sc.setSelectedPlayer(sp.playerSelectedId);
                new PlayCard(playerId, cardId, cardIds, resourcesForAction).execute(state);
            }
        }

        if(locationId != -1){
            EverdellLocation location = (EverdellLocation) egs.getComponentById(locationId);
            if(location.getAbstractLocation() == EverdellParameters.RedDestinationLocation.POST_OFFICE_DESTINATION){
                int locationCardID = EverdellLocation.findCardLinkedToLocation(egs, location);
                PostOfficeCard locationCard = (PostOfficeCard) egs.getComponentById(locationCardID);
                locationCard.setPlayers(sp.playerSelectedId, playerId);
                new PlaceWorker(playerId, locationId, cardIds, new HashMap<>()).execute(state);
            }
            else if(location.getAbstractLocation() == EverdellParameters.RedDestinationLocation.MONASTERY_DESTINATION){
                int locationCardID = EverdellLocation.findCardLinkedToLocation(egs, location);
                MonasteryCard locationCard = (MonasteryCard) egs.getComponentById(locationCardID);
                System.out.println("Selected Player: " + sp.playerSelectedId);
                locationCard.setPlayers(sp.playerSelectedId);
                new PlaceWorker(playerId, locationId, cardIds, resourcesForAction).execute(state);
            }
        }

        executed = true;
    }

    @Override
    public boolean executionComplete(AbstractGameState state) {
        return executed;
    }

    @Override
    public SelectPlayer copy() {
        SelectPlayer retValue;
        retValue = new SelectPlayer(playerId, cardId, locationId, playerSelectedId);
        retValue.executed = executed;
        return retValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectPlayer that = (SelectPlayer) o;
        return playerId == that.playerId && cardId == that.cardId && locationId == that.locationId && playerSelectedId == that.playerSelectedId && executed == that.executed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, cardId, locationId, playerSelectedId, executed);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return "Selecting A Player";
    }

    @Override
    public String toString(){
        return "Selecting A Player";
    }

}
