package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.terraformingmars.rules.requirements.ResourceRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AmountSelect extends PlayCard{
    private int amount;
    private int maxAmount;
    private EverdellParameters.ResourceTypes resourceType;
    private HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues;

    private int cardID;
    ArrayList<Integer> cardSelectionID;

    public AmountSelect(int cardID, ArrayList<Integer> cardSelectionID, HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues, int maxAmount, EverdellParameters.ResourceTypes resourceType){
        super(cardID, cardSelectionID, resourceSelectionValues);
        this.maxAmount = maxAmount;
        this.resourceType = resourceType;
        this.cardSelectionID = new ArrayList<>(cardSelectionID);
        this.cardID = cardID;
        this.resourceSelectionValues = resourceSelectionValues;
    }
    public AmountSelect(int cardID, ArrayList<Integer> cardSelectionID, HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues, int amount, int maxAmount, EverdellParameters.ResourceTypes resourceType){
        super(cardID, cardSelectionID, resourceSelectionValues);
        this.amount = amount;
        this.maxAmount = maxAmount;
        this.resourceType = resourceType;
        this.cardSelectionID = new ArrayList<>(cardSelectionID);
        this.cardID = cardID;
        this.resourceSelectionValues = resourceSelectionValues;
    }

    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        // TODO populate this list with available actions
        List<AbstractAction> actions = new ArrayList<>();

        EverdellGameState egs = (EverdellGameState) state;
        for(int i = 0; i <= Math.min(maxAmount, egs.PlayerResources.get(EverdellParameters.ResourceTypes.TWIG)[egs.getCurrentPlayer()].getValue()); i++){
            amount = i;
            actions.add(this.copy());
        }

        return actions;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        EverdellGameState state = (EverdellGameState) gs;
        resourceSelectionValues.put(resourceType, amount);
        System.out.println("Amount Selected : " + resourceSelectionValues);
        return super.execute(state);
    }


    @Override
    public PlayCard copy() {
        // TODO: copy non-final variables appropriately
        ArrayList<Integer> csID = new ArrayList<>(cardSelectionID);
        HashMap<EverdellParameters.ResourceTypes, Integer> rsID = new HashMap<>(resourceSelectionValues);
        return new AmountSelect(cardID, csID, rsID, amount, maxAmount, resourceType);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AmountSelect that = (AmountSelect) o;
        return amount == that.amount && maxAmount == that.maxAmount && cardID == that.cardID && resourceType == that.resourceType && Objects.equals(resourceSelectionValues, that.resourceSelectionValues) && Objects.equals(cardSelectionID, that.cardSelectionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amount, maxAmount, resourceType, resourceSelectionValues, cardID, cardSelectionID);
    }

    @Override
    public String toString() {
        return "Amount Selected : " +amount + " of " + resourceType;
    }
}
