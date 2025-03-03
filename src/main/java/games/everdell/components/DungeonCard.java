package games.everdell.components;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class DungeonCard extends ConstructionCard{

    private boolean secondCellUnlocked = false;
    public CritterCard cell1;
    public CritterCard cell2;
    private boolean canDiscount = false;

    public DungeonCard(String name, EverdellParameters.CardDetails cardEnumValue, EverdellParameters.CardType cardType, boolean isConstruction, boolean isUnique, int points, HashMap<EverdellParameters.ResourceTypes, Integer> resourceCost, Function<EverdellGameState, Boolean> applyCardEffect, Consumer<EverdellGameState> removeCardEffect, ArrayList<EverdellParameters.CardDetails> cardsThatCanOccupy) {
        super(name, cardEnumValue, cardType, isConstruction, isUnique, points, resourceCost, applyCardEffect, removeCardEffect, cardsThatCanOccupy);
    }
    private DungeonCard(String name, int compID, boolean secondCellUnlocked, CritterCard cell1, CritterCard cell2, boolean canDiscount) {
        super(name, compID);
        this.secondCellUnlocked = secondCellUnlocked;
        this.cell1 = cell1;
        this.cell2 = cell2;
        this.canDiscount = canDiscount;
    }



    public void applyCardEffect(EverdellGameState state) {
        //The player chooses a critter card to place in the dungeon
        //When a card is in the dungeon the player can play a card for a discount of 3 resources

        //Find the Ranger Card
        secondCellUnlocked = state.playerVillage.get(state.getCurrentPlayer()).stream().anyMatch(c -> c instanceof RangerCard);

        if(canDiscount){
            //Remove the cards from the village
            state.playerVillage.get(state.getCurrentPlayer()).remove(cell1);
            if(cell2 != null){
                state.playerVillage.get(state.getCurrentPlayer()).remove(cell2);
            }
            int discountCounter = 0;
            // **NOTE** This card will currently add the resources even if the card costs less than 3 resources
            // Currently assumed that the player will always select the correct amount of resources
            for (var resource : state.resourceSelection.keySet()) {
                for (int i = 0; i < state.resourceSelection.get(resource).getValue(); i++) {
                    if (discountCounter == 3) {
                        break;
                    }
                    discountCounter++;
                    state.PlayerResources.get(resource)[state.getCurrentPlayer()].increment();
                }
                if (discountCounter == 3) {
                    break;
                }
            }

            //Remove Resources based on card cost
            for (var resource : state.currentCard.getResourceCost().keySet()) {
                state.PlayerResources.get(resource)[state.getCurrentPlayer()].decrement(state.currentCard.getResourceCost().get(resource));
            }
            state.currentCard.payForCard();

        }
        canDiscount = false;

    }


    //A Critter must be placed in a cell for the effect to be triggered
    public void placeCritterInCell(CritterCard critterCard){
        if(cell1 == null){
            cell1 = critterCard;
            canDiscount = true;
        }
        else{
            if(secondCellUnlocked){
                if(cell2 == null){
                    cell2 = critterCard;
                    canDiscount = true;
                }
            }
        }
    }

    public boolean isThereACellFree(){
        if(cell1 == null){
            return true;
        }
        else if(cell2 == null & secondCellUnlocked){
            return true;
        }
        return false;
    }

    public void unlockSecondCell(){
        secondCellUnlocked = true;
    }

    public void lockSecondCell(){
        secondCellUnlocked = false;
    }

    @Override
    public DungeonCard copy() {
        DungeonCard card;
        card = new DungeonCard(getName(), componentID, secondCellUnlocked, cell1.copy(), cell2.copy(), canDiscount);
        super.copyTo(card);
        card.roundCardWasBought = -1;  // Assigned in game state copy of the deck
        return card;
    }
}
