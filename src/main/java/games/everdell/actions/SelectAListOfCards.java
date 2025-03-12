package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.HavenLocation;
import games.everdell.EverdellParameters.JourneyLocations;
import games.everdell.EverdellParameters.ForestLocations;
import games.everdell.EverdellParameters.RedDestinationLocation;
import games.everdell.EverdellParameters.ResourceTypes;
import games.everdell.components.EverdellCard;
import games.everdell.components.EverdellLocation;
import games.everdell.components.TeacherCard;

import java.util.*;

public class SelectAListOfCards extends AbstractAction implements IExtendedSequence {

    final int playerId;
    final int locationId;
    final int cardId;

    ArrayList<EverdellCard> cardsToSelectFrom;

    ArrayList<EverdellCard> selectedCards;

    final int maxAmount;

    final boolean isStrict;

    boolean loopAction;

    boolean executed;

    public SelectAListOfCards(int playerId, int locationId, int cardId, ArrayList<EverdellCard> cardsToSelectFrom, int maxAmount, boolean isStrict) {
        this.playerId = playerId;
        this.locationId = locationId;
        this.cardId = cardId;
        this.maxAmount = maxAmount;
        this.isStrict = isStrict;
        ArrayList<EverdellCard> ctsf = new ArrayList<>();
        for(EverdellCard card : cardsToSelectFrom){
            ctsf.add(card.copy());
        }
        this.cardsToSelectFrom = ctsf;
        this.loopAction = false;
    }
    private SelectAListOfCards(int playerId, int locationId, int cardId, ArrayList<EverdellCard> cardsToSelectFrom, int maxAmount, boolean isStrict, ArrayList<EverdellCard> selectedCards, boolean loopAction) {
        this.playerId = playerId;
        this.locationId = locationId;
        this.cardId = cardId;
        this.maxAmount = maxAmount;
        this.isStrict = isStrict;
        this.loopAction = loopAction;
        ArrayList<EverdellCard> ctsf = new ArrayList<>();
        for(EverdellCard card : cardsToSelectFrom){
            ctsf.add(card.copy());
        }
        this.cardsToSelectFrom = ctsf;

        if(selectedCards != null) {
            ArrayList<EverdellCard> sc = new ArrayList<>();
            for (EverdellCard card : selectedCards) {
                sc.add(card.copy());
            }
            this.selectedCards = sc;
        }

    }

    @Override
    public boolean execute(AbstractGameState gs) {
        System.out.println("SelectAListOfCards: execute");

        if(selectedCards == null){
            gs.setActionInProgress(this);
        }
        return true;
    }



    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        System.out.println("SelectAListOfCards: _computeAvailableActions");
        List<AbstractAction> actions = new ArrayList<>();

        generateCardCombinations(new ArrayList<>(), 0, actions);

        return actions;
    }

    private void generateCardCombinations(List<EverdellCard> currentCombination, int start, List<AbstractAction> actions) {
        if (currentCombination.size() == maxAmount || (!isStrict && currentCombination.size() <= maxAmount)) {
            actions.add(new SelectAListOfCards(playerId, locationId, cardId, cardsToSelectFrom, maxAmount, isStrict, new ArrayList<>(currentCombination), false));
            if (isStrict && currentCombination.size() == maxAmount) return;
        }
        for (int i = start; i < cardsToSelectFrom.size(); i++) {
            currentCombination.add(cardsToSelectFrom.get(i));
            generateCardCombinations(currentCombination, i + 1, actions);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        System.out.println("SelectAListOfCards: After Action");
        EverdellGameState egs = (EverdellGameState) state;

        SelectAListOfCards sa = (SelectAListOfCards) action;

        //Ids of all the cards selected
        ArrayList<Integer> cardIds = new ArrayList<>();
        for(EverdellCard c : sa.selectedCards){
            cardIds.add(c.getComponentID());
        }

        if(locationId != -1) {
            EverdellLocation location = (EverdellLocation) egs.getComponentById(locationId);
            //Forest Locations
            if (location.getAbstractLocation() == ForestLocations.DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED) {
                ForestLocations.cardChoices = sa.selectedCards;
                new PlaceWorker(state.getCurrentPlayer(), locationId, cardIds, new HashMap<>()).execute(egs);
            } else if (location.getAbstractLocation() == ForestLocations.DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED) {
                ForestLocations.cardChoices = sa.selectedCards;
                new ResourceSelect(playerId, -1, locationId, new ArrayList<>(List.of(ResourceTypes.values())), sa.selectedCards.size(), false, true, true).execute(egs);
            }

            //Haven
            if(location.getAbstractLocation() instanceof HavenLocation){
                //Fill Card Selection
                for(EverdellCard card : sa.selectedCards){
                    egs.cardSelection.add(card.copy());
                }
                new ResourceSelect(playerId, -1, locationId, new ArrayList<>(List.of(EverdellParameters.ResourceTypes.values())), sa.selectedCards.size()/2, false, true, true).execute(egs);
            }

            //Journey
            if(location.getAbstractLocation() instanceof JourneyLocations){
                new PlaceWorker(state.getCurrentPlayer(), locationId, cardIds, new HashMap<>()).execute(egs);
            }

            //Red Destination
            if(location.getAbstractLocation() instanceof RedDestinationLocation){
                if(location.getAbstractLocation() == RedDestinationLocation.QUEEN_DESTINATION){
                    new PlaceWorker(state.getCurrentPlayer(), locationId, cardIds, new HashMap<>()).execute(egs);
                }
            }
        }
        if(cardId != -1){
            EverdellCard card = (EverdellCard) egs.getComponentById(cardId);
            if(card.getCardEnumValue() == EverdellParameters.CardDetails.TEACHER){
                //Must Add to Card Selection the card to keep and after the card to give away
                egs.cardSelection.add(sa.selectedCards.get(0));
                if(sa.cardsToSelectFrom.get(0) != sa.selectedCards.get(0)){
                    egs.cardSelection.add(sa.cardsToSelectFrom.get(0));
                }
                else{
                    egs.cardSelection.add(sa.cardsToSelectFrom.get(1));
                }
                new SelectPlayer(playerId, cardId, -1).execute(egs);
            }
            else if(card.getCardEnumValue() == EverdellParameters.CardDetails.UNDERTAKER && !loopAction){
                //Remove Cards from meadow
                for (EverdellCard c : sa.selectedCards) {
                    egs.meadowDeck.remove(c);
                }
                //Replenish the meadow
                while (egs.meadowDeck.getSize() != 8) {
                    egs.meadowDeck.add(egs.cardDeck.draw());
                }
                new SelectAListOfCards(playerId, -1, cardId, new ArrayList<>(egs.meadowDeck.getComponents()), 1, true, null, true).execute(egs);
            }
            else {
                new PlayCard(playerId, cardId, cardIds, new HashMap<>()).execute(egs);
            }
        }


        executed = true;
    }

    @Override
    public boolean executionComplete(AbstractGameState state) {
        return executed;
    }

    @Override
    public SelectAListOfCards copy() {
        ArrayList<EverdellCard> ctsf = new ArrayList<>();
        for(EverdellCard card : cardsToSelectFrom){
            ctsf.add(card.copy());
        }
        ArrayList<EverdellCard> sc = new ArrayList<>();
        if(selectedCards != null){
            for(EverdellCard card : selectedCards){
                sc.add(card.copy());
            }
        }
        SelectAListOfCards retValue = new SelectAListOfCards(playerId, locationId, cardId, ctsf, maxAmount, isStrict, sc, loopAction);
        retValue.executed = executed;
        return retValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectAListOfCards that = (SelectAListOfCards) o;
        return playerId == that.playerId && locationId == that.locationId && cardId == that.cardId && maxAmount == that.maxAmount && isStrict == that.isStrict && loopAction == that.loopAction && executed == that.executed && Objects.equals(cardsToSelectFrom, that.cardsToSelectFrom) && Objects.equals(selectedCards, that.selectedCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, locationId, cardId, cardsToSelectFrom, selectedCards, maxAmount, isStrict, loopAction, executed);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return "Selecting A list of cards";
    }

    @Override
    public String toString(){
        return "Selecting A List of Cards";
    }

}
