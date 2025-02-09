package games.everdell.components;

import core.components.Card;
import games.catan.components.CatanCard;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.CardType;
import games.everdell.EverdellParameters.CardDetails;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;
import org.apache.spark.internal.config.R;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class EverdellCard extends Card {


    private final String name;
    private final CardDetails cardEnumValue;
    private final CardType cardType;
    private final boolean isConstruction;
    private int points;
    private final HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost;

    private final Function<EverdellGameState, Boolean> applyCardEffect;
    public Consumer<EverdellGameState> removeCardEffect;

    private boolean isCardPayedFor;
    private boolean isUnique;
    private boolean paidForByResources;

    public int roundCardWasBought = -1;  // -1 is not bought
    //public final String cardDescription;

    public EverdellCard(String name, CardDetails cardEnumValue, CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect) {
        super(name);
        this.name = name;
        this.cardEnumValue = cardEnumValue;
        this.cardType = cardType;
        this.isConstruction = isConstruction;
        this.points = points;
        this.resourceCost = resourceCost;
        this.applyCardEffect = applyCardEffect;
        this.removeCardEffect = removeCardEffect;
        this.isUnique = isUnique;
        isCardPayedFor = false;
        paidForByResources = false;
    }
    private EverdellCard(String name, CardDetails cardEnumValue, CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, int id) {
        super(name, id);
        this.name = name;
        this.cardEnumValue = cardEnumValue;
        this.cardType = cardType;
        this.isConstruction = isConstruction;
        this.points = points;
        this.resourceCost = resourceCost;
        this.applyCardEffect = applyCardEffect;
        this.removeCardEffect = removeCardEffect;
        this.isUnique = isUnique;
        isCardPayedFor = false;
    }

    @Override
    public EverdellCard copy() {
        EverdellCard card = new EverdellCard(name,cardEnumValue,cardType,isConstruction, isUnique, points,resourceCost,applyCardEffect,removeCardEffect);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }


    // Getters for all fields
    public String getName() { return name; }
    public CardDetails getCardEnumValue() { return cardEnumValue; }
    public CardType getCardType() { return cardType; }
    public boolean isConstruction() { return isConstruction; }
    public int getPoints() { return points; }
    public HashMap<EverdellParameters.ResourceTypes, Integer> getResourceCost() { return resourceCost; }
    protected void applyCardEffect(EverdellGameState state) {
        applyCardEffect.apply(state);
    }
    public void removeCardEffect(EverdellGameState state) {
        removeCardEffect.accept(state);
    }
    public boolean isCardPayedFor() { return isCardPayedFor; }
    public boolean isUnique() { return isUnique; }


    //Setter
    public void setCardPoints(int points){
        this.points = points;
    }
    public void payForCard() { isCardPayedFor = true; }

}

