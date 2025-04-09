package games.everdell.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Component;
import core.interfaces.IExtendedSequence;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.Seasons;
import games.everdell.components.*;
import org.apache.spark.sql.sources.In;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BeforeMoveSeasonAction extends AbstractAction implements IExtendedSequence {

    private int playerID;

    private boolean executed;

    private boolean clocktowerEvent;

    private boolean loopAction;

    public BeforeMoveSeasonAction(int playerID) {
        this.playerID = playerID;
        this.loopAction = true;
    }

    private BeforeMoveSeasonAction(int playerID, boolean clocktowerEvent, boolean loopAction) {
        this.playerID = playerID;
        this.clocktowerEvent = clocktowerEvent;
        this.loopAction = loopAction;
    }


    @Override
    public boolean execute(AbstractGameState gs) {
        // TODO: Some functionality applied which changes the given game state.
        System.out.println("BeforeMoveSeasonAction: execute");
        playerID = gs.getCurrentPlayer();

        EverdellGameState state = (EverdellGameState) gs;

        if(loopAction){
            state.setActionInProgress(this);
        }

        return true;
    }


    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState abstractGameState) {
        System.out.println("BeforeMoveSeasonAction: ComputeAvailableActions");
        EverdellGameState state = (EverdellGameState) abstractGameState;
        ArrayList<AbstractAction> actions = new ArrayList<>();
        boolean hasClocktower = false;
        //Check if the player has clocktower card
        for (var card : state.playerVillage.get(state.getCurrentPlayer()).getComponents()) {
            if (card instanceof ClockTowerCard ctc) {
                if(ctc.canPerformAction()) {
                    hasClocktower = true;
                }
                break;
            }
        }

        if(hasClocktower){
            actions.add(new BeforeMoveSeasonAction(playerID, true, false));
        }


        actions.add(new BeforeMoveSeasonAction(playerID, false, false));
        return actions;
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerID;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        System.out.println("BeforeMoveSeasonAction: AfterAction");
        EverdellGameState egs = (EverdellGameState) state;
        BeforeMoveSeasonAction bmsa = (BeforeMoveSeasonAction) action;

        if(bmsa.clocktowerEvent){
            //Go through the clock tower process
            System.out.println("Clock Tower Event");
            egs.clockTowerMode = true;

            ArrayList<Integer> locationsToSelectFrom = new ArrayList<>();
            for (var location : egs.everdellLocations) {
                locationsToSelectFrom.add(location.getComponentID());
            }

            new SelectLocation(playerID, -1, locationsToSelectFrom).execute(egs);
        }
        else{
            //Go through standard season process
            EverdellParameters.Seasons nextSeason = EverdellParameters.Seasons.values()[(egs.currentSeason[egs.getCurrentPlayer()].ordinal() + 1) % EverdellParameters.Seasons.values().length];
            System.out.println("Next Season: "+nextSeason);

            //Green Production, Select the order in which to play the cards
            if(nextSeason == EverdellParameters.Seasons.AUTUMN || nextSeason == EverdellParameters.Seasons.SPRING){
                ArrayList<Integer> greenCardsToSelectFrom = egs.playerVillage.get(egs.getCurrentPlayer()).getComponents().stream().filter(card -> card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).mapToInt(Component::getComponentID).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                new MoveSeason(greenCardsToSelectFrom, egs.getCurrentPlayer()).execute(egs);
            }

            //Summer Event, Select cards to take from the meadow
            else if (nextSeason == EverdellParameters.Seasons.SUMMER) {
                int cardsToDraw = Math.min(2, egs.playerHands.get(egs.getCurrentPlayer()).getCapacity() - egs.playerHands.get(egs.getCurrentPlayer()).getSize());
                ArrayList<EverdellCard> seasonCardsToSelectFrom = new ArrayList<>(egs.meadowDeck.getComponents());

                System.out.println("Cards to draw : "+cardsToDraw);
                //Perhaps can be reworked at some point but this works
                if(cardsToDraw > 0) {
                    new SelectAListOfCards(egs.getCurrentPlayer(), true, seasonCardsToSelectFrom, cardsToDraw, false).execute(egs);
                }
                else{
                    new MoveSeason(new ArrayList<>(), egs.getCurrentPlayer()).execute(egs);
                }
            }
        }




        executed = true;
        return;

    }

    @Override
    public boolean executionComplete(AbstractGameState state) {
        return executed;
    }

    /**
     * @return Make sure to return an exact <b>deep</b> copy of the object, including all of its variables.
     * Make sure the return type is this class (e.g. GTAction) and NOT the super class AbstractAction.
     * <p>If all variables in this class are final or effectively final (which they should be),
     * then you can just return <code>`this`</code>.</p>
     */
    @Override
    public BeforeMoveSeasonAction copy() {
        // TODO: copy non-final variables appropriately
        BeforeMoveSeasonAction ms = new BeforeMoveSeasonAction(playerID, clocktowerEvent, loopAction);
        ms.executed = executed;
        return ms;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BeforeMoveSeasonAction that = (BeforeMoveSeasonAction) o;
        return playerID == that.playerID && executed == that.executed && clocktowerEvent == that.clocktowerEvent && loopAction == that.loopAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerID, executed, clocktowerEvent, loopAction);
    }

    @Override
    public String toString() {
        // TODO: Replace with appropriate string, including any action parameters
        return "Checking for actions before moving to next season";
    }

    /**
     * @param gameState - game state provided for context.
     * @return A more descriptive alternative to the toString action, after access to the game state to e.g.
     * retrieve components for which only the ID is stored on the action object, and include the name of those components.
     * Optional.
     */
    @Override
    public String getString(AbstractGameState gameState) {
        return toString();
    }

}
