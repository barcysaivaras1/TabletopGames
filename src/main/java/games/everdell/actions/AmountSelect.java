package games.everdell.actions;

import core.AbstractGameState;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.terraformingmars.rules.requirements.ResourceRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AmountSelect extends PlayCard{
    public int amount;
    public EverdellParameters.ResourceTypes resourceType;
    public HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues;

    public int cardID;
    ArrayList<Integer> cardSelectionID;

    public AmountSelect(int cardID, ArrayList<Integer> cardSelectionID, HashMap<EverdellParameters.ResourceTypes, Integer> resourceSelectionValues, int amount, EverdellParameters.ResourceTypes resourceType){
        super(cardID, cardSelectionID, resourceSelectionValues);
        this.amount = amount;
        this.resourceType = resourceType;
        this.cardSelectionID = new ArrayList<>(cardSelectionID);
        this.cardID = cardID;
        this.resourceSelectionValues = resourceSelectionValues;
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
        return new AmountSelect(cardID, csID, rsID, amount, resourceType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AmountSelect that = (AmountSelect) o;
        return amount == that.amount && resourceType == that.resourceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amount, resourceType);
    }

    @Override
    public String toString() {
        return "Amount Selected : " +amount + " of " + resourceType;
    }
}
