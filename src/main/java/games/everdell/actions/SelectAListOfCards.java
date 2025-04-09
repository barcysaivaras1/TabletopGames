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
import games.everdell.components.*;

import java.util.*;
import java.util.stream.Collectors;

public class SelectAListOfCards extends AbstractAction implements IExtendedSequence {

    final int playerId;
    final int locationId;
    final int cardId;

    final boolean isMovingSeason;

    ArrayList<EverdellCard> cardsToSelectFrom;

    ArrayList<EverdellCard> selectedCards;

    final int maxAmount;

    final boolean isStrict;

    boolean loopAction;

    boolean executed;

    //Location OR Card Action
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
        this.loopAction = true;
        this.isMovingSeason = false;
    }

    //MoveSeason Action
    public SelectAListOfCards(int playerId, boolean isMovingSeason, ArrayList<EverdellCard> cardsToSelectFrom, int maxAmount, boolean isStrict) {
        this.playerId = playerId;
        this.locationId = -1;
        this.cardId = -1;
        this.isMovingSeason = isMovingSeason;
        this.maxAmount = maxAmount;
        this.isStrict = isStrict;
        ArrayList<EverdellCard> ctsf = new ArrayList<>();
        for(EverdellCard card : cardsToSelectFrom){
            ctsf.add(card.copy());
        }
        this.cardsToSelectFrom = ctsf;
        this.loopAction = true;
    }

    private SelectAListOfCards(int playerId, int locationId, int cardId, boolean isMovingSeason, ArrayList<EverdellCard> cardsToSelectFrom, int maxAmount, boolean isStrict, ArrayList<EverdellCard> selectedCards, boolean loopAction) {
        this.playerId = playerId;
        this.locationId = locationId;
        this.cardId = cardId;
        this.isMovingSeason = isMovingSeason;
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

        if(loopAction){
            gs.setActionInProgress(this);
        }
        return true;
    }



    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        System.out.println("SelectAListOfCards: _computeAvailableActions");
        List<AbstractAction> actions = new ArrayList<>();

        generateCardCombinations(new ArrayList<>(), 0, actions);
        if(actions.isEmpty()){
            actions.add(new SelectAListOfCards(playerId, locationId, cardId, isMovingSeason, cardsToSelectFrom, maxAmount, isStrict, new ArrayList<>(), false));
        }
        return actions;
    }

    private void generateCardCombinations(List<EverdellCard> currentCombination, int start, List<AbstractAction> actions) {
        if (currentCombination.size() == maxAmount || (!isStrict && currentCombination.size() <= maxAmount)) {
            actions.add(new SelectAListOfCards(playerId, locationId, cardId, isMovingSeason, cardsToSelectFrom, maxAmount, isStrict, new ArrayList<>(currentCombination), false));
            if (isStrict && currentCombination.size() == maxAmount) return;
        }
        for (int i = start; i < cardsToSelectFrom.size(); i++) {
            currentCombination.add(cardsToSelectFrom.get(i));
            generateCardCombinations(currentCombination, i + 1, actions);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }

    public ArrayList<Integer> cardsToIDs(ArrayList<EverdellCard> cards){
        //Ids of all the cards selected
        ArrayList<Integer> cardIds = new ArrayList<>();
        for(EverdellCard card : cards){
            cardIds.add(card.getComponentID());
        }
        return cardIds;
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

        ArrayList<Integer> cardIDs = new ArrayList<>();
        for(EverdellCard card : sa.selectedCards){
            cardIDs.add(card.getComponentID());
        }


        if(locationId != -1) {
            EverdellLocation location = (EverdellLocation) egs.getComponentById(locationId);
            //Forest Locations
            if (location.getAbstractLocation() == ForestLocations.DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED) {
                ForestLocations.cardChoices = sa.selectedCards;
                new PlaceWorker(state.getCurrentPlayer(), locationId, cardsToIDs(cardsToSelectFrom), new HashMap<>()).execute(egs);
            } else if (location.getAbstractLocation() == ForestLocations.DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED) {
                ForestLocations.cardChoices = sa.selectedCards;
                new ResourceSelect(playerId, -1, locationId, new ArrayList<>(List.of(ResourceTypes.values())), sa.selectedCards.size(), true, false).execute(egs);
            }
            else if (location.getAbstractLocation() == ForestLocations.DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT) {
                //Check if any of the 2 cards can be played
                ForestLocations.cardChoices = sa.selectedCards;
                boolean canPlay = false;
                for(EverdellCard card : sa.selectedCards){
                    //Check if its unique and can be played
                    if(!card.checkIfPlayerCanPlaceThisUniqueCard(egs, playerId)){
                        continue;
                    }
                    //Check for space
                    if(egs.villageMaxSize[playerId].getValue() <= egs.playerVillage.get(playerId).getSize()){
                        continue;
                    }
                    //Check if the card can be played with the discount
                    System.out.println("Can the card : "+card.getCardEnumValue()+" be played with discount? "+card.checkIfPlayerCanBuyCardWithDiscount(egs, 1));
                    if(!card.checkIfPlayerCanBuyCardWithDiscount(egs, 1)){
                        continue;
                    }
                    canPlay = true;
                }
                if(canPlay) {
                    new SelectCard(playerId, -1, locationId, cardsToIDs(sa.selectedCards)).execute(egs);
                }
                else{ //No card we can play
                    new PlaceWorker(playerId, locationId, new ArrayList<>(), new HashMap<>()).execute(egs);
                }
            }

            //Haven
            if(location.getAbstractLocation() instanceof HavenLocation){
                //Fill Card Selection
                for(EverdellCard card : sa.selectedCards){
                    egs.cardSelection.add(card.copy());
                }
                new ResourceSelect(playerId, -1, locationId, new ArrayList<>(List.of(ResourceTypes.values())), sa.selectedCards.size()/2, true, false).execute(egs);
            }

            //Journey
            if(location.getAbstractLocation() instanceof JourneyLocations){
                new PlaceWorker(state.getCurrentPlayer(), locationId, cardsToIDs(cardsToSelectFrom), new HashMap<>()).execute(egs);
            }

            //Red Destination
            if(location.getAbstractLocation() instanceof RedDestinationLocation){
                if(location.getAbstractLocation() == RedDestinationLocation.QUEEN_DESTINATION){
                    new PlaceWorker(state.getCurrentPlayer(), locationId, cardsToIDs(cardsToSelectFrom), new HashMap<>()).execute(egs);
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.POST_OFFICE_DESTINATION){
                    if(egs.cardSelection.isEmpty()){
                        //Cards To Give Away
                        egs.cardSelection.addAll(sa.selectedCards);
                        System.out.println("Cards to give away POSTOFFICE SELECTALISTOFCARDS: "+egs.cardSelection);
                        ArrayList<EverdellCard> cardsToSelectFrom = egs.playerHands.get(playerId).getComponents().stream().filter(cardToCheck -> !sa.selectedCards.contains(cardToCheck)).collect(Collectors.toCollection(ArrayList::new));
                        new SelectAListOfCards(playerId, locationId, -1, cardsToSelectFrom, cardsToSelectFrom.size(), false).execute(egs);
                    }
                    else{
                        new SelectPlayer(playerId, -1, locationId).execute(egs);
                    }
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.CEMETERY_DESTINATION){
                    egs.cardSelection.addAll(cardsToSelectFrom);
                    egs.cardSelection.add(0, sa.selectedCards.get(0));
                    new PlaceWorker(playerId, locationId, cardsToIDs(egs.cardSelection), new HashMap<>()).execute(egs);
                }
                else if(location.getAbstractLocation() == RedDestinationLocation.UNIVERSITY_DESTINATION){
                    egs.cardSelection.addAll(cardsToSelectFrom);
                    new ResourceSelect(playerId, -1, locationId, new ArrayList<>(List.of(ResourceTypes.values())), 1, true, false).execute(egs);
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
                new SelectAListOfCards(playerId, -1, cardId, isMovingSeason, new ArrayList<>(egs.meadowDeck.getComponents()), 1, true, null, true).execute(egs);
            }
            else {
                new PlayCard(playerId, cardId, cardsToIDs(cardsToSelectFrom), new HashMap<>()).execute(egs);
            }
        }
        if(isMovingSeason){
            new MoveSeason(cardIDs, state.getCurrentPlayer()).execute(egs);
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
        SelectAListOfCards retValue = new SelectAListOfCards(playerId, locationId, cardId, isMovingSeason, ctsf, maxAmount, isStrict, sc, loopAction);

        retValue.executed = executed;
        return retValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectAListOfCards that = (SelectAListOfCards) o;
        return playerId == that.playerId && locationId == that.locationId && cardId == that.cardId && isMovingSeason == that.isMovingSeason && maxAmount == that.maxAmount && isStrict == that.isStrict && loopAction == that.loopAction && executed == that.executed && Objects.equals(cardsToSelectFrom, that.cardsToSelectFrom) && Objects.equals(selectedCards, that.selectedCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, locationId, cardId, isMovingSeason, cardsToSelectFrom, selectedCards, maxAmount, isStrict, loopAction, executed);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return toString();
    }

    @Override
    public String toString(){
        if(isMovingSeason){
            return "Selecting Cards for Moving Season";
        }
        return "Selecting A List of Cards";
    }

}
