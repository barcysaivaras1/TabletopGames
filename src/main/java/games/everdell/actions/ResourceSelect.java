package games.everdell.actions;

import com.ibm.icu.text.ArabicShaping;
import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.EverdellCard;
import games.everdell.components.EverdellLocation;
import games.everdell.components.PeddlerCard;
import org.apache.spark.sql.sources.In;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ResourceSelect extends AbstractAction implements IExtendedSequence {

    private boolean executed;

    boolean isForCard;
    boolean isForLocation;
    boolean isStrict;
    boolean loopAction;

    public HashMap<EverdellParameters.ResourceTypes, Integer> resourcesSelected;
    private final ArrayList<EverdellParameters.ResourceTypes> resourcesToSelectFor;

    private final int maxAmount;

    private final int playerId;
    private final int cardId;
    private final int locationId;

    public ResourceSelect(int playerId, int cardId, int locationId, ArrayList<EverdellParameters.ResourceTypes> resourcesToSelectFor, int maxAmount, boolean isForCard, boolean isForLocation, boolean isStrict){
        this.playerId = playerId;
        this.resourcesToSelectFor = resourcesToSelectFor;
        this.maxAmount = maxAmount;
        this.isForCard = isForCard;
        this.isForLocation = isForLocation;
        this.cardId = cardId;
        this.locationId = locationId;
        this.isStrict = isStrict;
        loopAction = false;
    }
    private ResourceSelect(int playerId, int cardId, int locationId, HashMap<EverdellParameters.ResourceTypes, Integer> resourcesSelected,  ArrayList<EverdellParameters.ResourceTypes> resourcesToSelectFor, int maxAmount, boolean isForCard, boolean isForLocation, boolean isStrict, boolean loopAction){
        this.playerId = playerId;
        this.resourcesToSelectFor = resourcesToSelectFor;
        this.resourcesSelected = resourcesSelected;
        this.maxAmount = maxAmount;
        this.isForCard = isForCard;
        this.isForLocation = isForLocation;
        this.cardId = cardId;
        this.locationId = locationId;
        this.isStrict = isStrict;
        this.loopAction = loopAction;
    }


    @Override
    public boolean execute(AbstractGameState gs) {
        System.out.println("Resource SELECT EXECUTED");

        if(resourcesSelected == null) {
            gs.setActionInProgress(this);
        }

        return true;
    }
    //amountActions.addAll(new AmountSelect(new ArrayList<>(Collections.nCopies(resourcesToSelectFor.size(),0)),maxAmount)._computeAvailableActions(state));


    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        System.out.println("Computing Resource Select Actions");
        List<AbstractAction> amountActions = new ArrayList<>();
        EverdellGameState egs = (EverdellGameState) state;

        HashMap<EverdellParameters.ResourceTypes, Integer> amountOwned = new HashMap<>();
        for(EverdellParameters.ResourceTypes resource : resourcesToSelectFor){
            amountOwned.put(resource, egs.PlayerResources.get(resource)[playerId].getValue());
        }

        if(isForCard){
            EverdellCard card = (EverdellCard) egs.getComponentById(cardId);
            if(card.getCardEnumValue() == EverdellParameters.CardDetails.PEDDLER && !loopAction){
                generateCostBasedResourceCombinations(new HashMap<>(), maxAmount, amountActions, amountOwned);
            }
            else if(card.getCardEnumValue() == EverdellParameters.CardDetails.MONK){
                generateCostBasedResourceCombinations(new HashMap<>(), maxAmount, amountActions, amountOwned);
            }
            else{
                generateResourceCombinations(new HashMap<>(), maxAmount, amountActions);
            }
        }
        else {
            generateResourceCombinations(new HashMap<>(), maxAmount, amountActions);
        }
        return amountActions;
    }

    private void generateResourceCombinations(HashMap<EverdellParameters.ResourceTypes, Integer> currentCombination, int remaining, List<AbstractAction> actions) {
        if (remaining < 0) return;
        if (remaining == 0 || !isStrict) {
            actions.add(new ResourceSelect(playerId, cardId, locationId, new HashMap<>(currentCombination), new ArrayList<>(resourcesToSelectFor), maxAmount, isForCard, isForLocation, isStrict, false));
            if (remaining == 0) return;
        }
        for (EverdellParameters.ResourceTypes resource : resourcesToSelectFor) {
            currentCombination.put(resource, currentCombination.getOrDefault(resource, 0) + 1);
            generateResourceCombinations(currentCombination, remaining - 1, actions);
            currentCombination.put(resource, currentCombination.get(resource) - 1);
        }
    }

    private void generateCostBasedResourceCombinations(HashMap<EverdellParameters.ResourceTypes, Integer> currentCombination, int remaining, List<AbstractAction> actions, HashMap<EverdellParameters.ResourceTypes, Integer> amountOwned) {
        if (remaining < 0) return;
        if (remaining == 0 || (!isStrict && remaining <= maxAmount)) {
            actions.add(new ResourceSelect(playerId, cardId, locationId, new HashMap<>(currentCombination), new ArrayList<>(resourcesToSelectFor), currentCombination.values().stream().mapToInt(Integer::intValue).sum(), isForCard, isForLocation, true, false));
            if (remaining == 0) return;
        }
        for (EverdellParameters.ResourceTypes resource : resourcesToSelectFor) {
            int maxResourceAmount = Math.min(maxAmount, amountOwned.getOrDefault(resource, 0));
            if (currentCombination.getOrDefault(resource, 0) < maxResourceAmount) {
                currentCombination.put(resource, currentCombination.getOrDefault(resource, 0) + 1);
                generateCostBasedResourceCombinations(currentCombination, remaining - 1, actions, amountOwned);
                currentCombination.put(resource, currentCombination.get(resource) - 1);
            }
        }
    }



    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        System.out.println("After Action Resource Select");
        EverdellGameState egs = (EverdellGameState) state;
        ResourceSelect resourceSelect = (ResourceSelect) action;
        System.out.println("Resources Selected: " + resourceSelect.resourcesSelected);

        //Ids of all the cards selected
        ArrayList<Integer> cardIds = new ArrayList<>();
        for(EverdellCard c : egs.cardSelection){
            cardIds.add(c.getComponentID());
        }

        if(isForCard){
            EverdellCard card = (EverdellCard) egs.getComponentById(cardId);
            //Peddler Special Need
            if(card.getCardEnumValue() == EverdellParameters.CardDetails.PEDDLER){
                PeddlerCard pc = (PeddlerCard) card;
                pc.resourcesToLoseSelected = true;
                if(!loopAction) {
                    pc.addResourcesToLose(resourceSelect.resourcesSelected);
                    new ResourceSelect(playerId, cardId, -1, null, new ArrayList<>(resourcesToSelectFor), pc.getResourcesToLose().values().stream().mapToInt(Integer::intValue).sum(), true, false, true, true).execute(state);
                }
                else{
                    pc.addResourcesToGain(resourceSelect.resourcesSelected);
                    new PlayCard(playerId, cardId, new ArrayList<>(), resourceSelect.resourcesSelected).execute(state);
                }
            }
            else if(card.getCardEnumValue() == EverdellParameters.CardDetails.MONK){
                egs.resourceSelection.get(EverdellParameters.ResourceTypes.BERRY).increment(resourceSelect.resourcesSelected.getOrDefault(EverdellParameters.ResourceTypes.BERRY, 0));
                new SelectPlayer(playerId, cardId, -1).execute(state);
            }
            else if(card.getCardEnumValue() == EverdellParameters.CardDetails.UNDERTAKER){
                egs.resourceSelection.get(EverdellParameters.ResourceTypes.BERRY).increment(resourceSelect.resourcesSelected.getOrDefault(EverdellParameters.ResourceTypes.BERRY, 0));
                new SelectPlayer(playerId, cardId, -1).execute(state);
            }
            else {
                new PlayCard(playerId, cardId, new ArrayList<>(), resourceSelect.resourcesSelected).execute(state);
            }
        }
        else if(isForLocation){
            EverdellLocation location = (EverdellLocation) egs.getComponentById(locationId);
            new PlaceWorker(locationId, cardIds, resourceSelect.resourcesSelected).execute(state);
        }
        executed = true;
    }


    @Override
    public boolean executionComplete(AbstractGameState state) {
        return executed;
    }

    @Override
    public ResourceSelect copy() {

        HashMap<EverdellParameters.ResourceTypes, Integer> resources = null;
        if(this.resourcesSelected != null) {
            resources = new HashMap<>(this.resourcesSelected);
        }

        ResourceSelect retValue = new ResourceSelect(playerId, cardId, locationId, resources,new ArrayList<>(resourcesToSelectFor), maxAmount, isForCard, isForLocation, isStrict, loopAction);
        retValue.executed = executed;
        return retValue;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ResourceSelect that = (ResourceSelect) o;
        return executed == that.executed && isForCard == that.isForCard && isForLocation == that.isForLocation && isStrict == that.isStrict && loopAction == that.loopAction && maxAmount == that.maxAmount && playerId == that.playerId && cardId == that.cardId && locationId == that.locationId && Objects.equals(resourcesSelected, that.resourcesSelected) && Objects.equals(resourcesToSelectFor, that.resourcesToSelectFor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executed, isForCard, isForLocation, isStrict, loopAction, resourcesSelected, resourcesToSelectFor, maxAmount, playerId, cardId, locationId);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return "Resource Select";
    }

}
