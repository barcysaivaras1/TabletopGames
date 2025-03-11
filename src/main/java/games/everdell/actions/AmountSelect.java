package games.everdell.actions;

import com.sun.research.ws.wadl.ResourceType;
import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellParameters.ResourceTypes;
import org.apache.spark.sql.sources.In;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AmountSelect extends AbstractAction{
    public ArrayList<Integer> amount;
    private final int maxAmount;


    AmountSelect(ArrayList<Integer> amount, int maxAmount){
        this.amount = amount;
        this.maxAmount = maxAmount;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        System.out.println("Amount Selected : " + amount);
        return true;
    }

    public List<ArrayList<Integer>> generateAmounts() {
        List<ArrayList<Integer>> result = new ArrayList<>();
        generateAmountsHelper(maxAmount, amount.size(), new ArrayList<>(amount), 0, result);
        return result;
    }

    private void generateAmountsHelper(int maxAmount, int n, ArrayList<Integer> currentArray, int index, List<ArrayList<Integer>> result) {
        if (index == n) {
            result.add(new ArrayList<>(currentArray));
            return;
        }

        for (int i = 0; i <= maxAmount; i++) {
            currentArray.set(index, i);
            generateAmountsHelper(maxAmount, n, currentArray, index + 1, result);
        }
    }

    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        List<AbstractAction> amountActions = new ArrayList<>();
        List<ArrayList<Integer>> possibleAmounts = generateAmounts();
        for (ArrayList<Integer> i : possibleAmounts) {
            amountActions.add(new AmountSelect(i, maxAmount));
        }
        return amountActions;
    }


    @Override
    public AmountSelect copy() {
        ArrayList<Integer> amount = new ArrayList<>(this.amount);
        return new AmountSelect(amount, maxAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AmountSelect that = (AmountSelect) o;
        return amount == that.amount && maxAmount == that.maxAmount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, maxAmount);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return toString();
    }

    @Override
    public String toString() {
        return "Amount Selected : " +amount;
    }
}
