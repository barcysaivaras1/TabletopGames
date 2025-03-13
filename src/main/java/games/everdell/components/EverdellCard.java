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

public abstract class EverdellCard extends Card {


    private  String name;
    private  CardDetails cardEnumValue;
    private  CardType cardType;
    private  boolean isConstruction;
    private int points;
    private  HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost;

    private  Function<EverdellGameState, Boolean> applyCardEffect;
    public Consumer<EverdellGameState> removeCardEffect;

    private boolean isCardPayedFor;
    private boolean isUnique;

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
        this.isCardPayedFor = false;
    }

    protected EverdellCard(String name, CardDetails cardEnumValue, CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, int id) {
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
        this.isCardPayedFor = false;
    }
    public EverdellCard(String name, int compID) {
        super(name,compID);
    }


    public void copyTo(EverdellCard card){
        card.name = this.name;
        card.cardType = this.cardType;
        card.isConstruction = this.isConstruction;
        card.cardEnumValue = this.cardEnumValue;
        card.points = this.points;
        card.resourceCost = this.resourceCost;
        card.applyCardEffect = this.applyCardEffect;
        card.removeCardEffect = this.removeCardEffect;
        card.isUnique = this.isUnique;
        card.isCardPayedFor = this.isCardPayedFor;
    }
    @Override
    public EverdellCard copy() {
        return this;
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
    protected Function<EverdellGameState, Boolean> getApplyCardEffect() { return applyCardEffect; }
    public void removeCardEffect(EverdellGameState state) {
        removeCardEffect.accept(state);
    }
    public boolean isCardPayedFor() { return isCardPayedFor; }
    public boolean isUnique() { return isUnique; }

    //Helper Functions
    public Boolean checkIfPlayerCanPlaceThisUniqueCard(EverdellGameState state, int playerId){
        //Check if the player has this Unique card in their village
        if(this.isUnique()){
            for(EverdellCard card : state.playerVillage.get(playerId).getComponents()){
                if(card.getCardEnumValue() == getCardEnumValue()){
                    return false;
                }
            }
        }
        return true;
    }

    public Boolean checkIfPlayerCanBuyCard(EverdellGameState state, int playerId){
        //Check if the player has enough resources to buy the card

        //The card can be paid with occupation.
        if(isCardPayedFor()){
            return true;
        }
        for(var resource : getResourceCost().keySet()){
            if(state.PlayerResources.get(resource)[playerId].getValue() < getResourceCost().get(resource)){
                return false;
            }
        }

        return true;
    }


    //Setter
    public void setCardPoints(int points){
        this.points = points;
    }
    public void payForCard() { this.isCardPayedFor = true; }

}

