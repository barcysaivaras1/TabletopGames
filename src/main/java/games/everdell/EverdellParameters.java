package games.everdell;

import core.AbstractGameState;
import core.AbstractParameters;
import evaluation.optimisation.TunableParameters;
import games.catan.components.CatanCard;
import games.everdell.components.EverdellCard;

import java.awt.*;
import java.util.HashMap;

/**
 * <p>This class should hold a series of variables representing game parameters (e.g. number of cards dealt to players,
 * maximum number of rounds in the game etc.). These parameters should be used everywhere in the code instead of
 * local variables or hard-coded numbers, by accessing these parameters from the game state via {@link AbstractGameState#getGameParameters()}.</p>
 *
 * <p>It should then implement appropriate {@link #_copy()}, {@link #_equals(Object)} and {@link #hashCode()} functions.</p>
 *
 * <p>The class can optionally extend from {@link TunableParameters} instead, which allows to use
 * automatic game parameter optimisation tools in the framework.</p>
 */
public class EverdellParameters extends AbstractParameters {


    enum Resources {
        WOOD, RESIN, PEBBLE, BERRY, TWIG
    }

    enum Seasons {
        SPRING, SUMMER, FALL, WINTER
    }
    enum number_of_cards_at_start{
        Player1(5), Player2(6), Player3(7), Player4(8);

        private final int value;
        number_of_cards_at_start(int value){
            this.value = value;
        }
        public int getNumOfCards(){
            return value;
        }
    }

    public HashMap<EverdellCard.CardType, Color> cardColour = new HashMap<EverdellCard.CardType, Color>() {{
        put(EverdellCard.CardType.BLUE_GOVERNANCE, Color.BLUE);
        put(EverdellCard.CardType.GREEN_PRODUCTION, Color.GREEN);
        put(EverdellCard.CardType.PURPLE_PROSPERITY, Color.MAGENTA);
        put(EverdellCard.CardType.RED_DESTINATION, Color.RED);
        put(EverdellCard.CardType.TAN_TRAVELER, Color.ORANGE);
    }};


    HashMap<EverdellCard.CardType, Integer> villageCardCount = new HashMap<EverdellCard.CardType, Integer>() {{
        put(EverdellCard.CardType.BLUE_GOVERNANCE, 10);
        put(EverdellCard.CardType.GREEN_PRODUCTION, 10);
        put(EverdellCard.CardType.PURPLE_PROSPERITY, 10);
        put(EverdellCard.CardType.RED_DESTINATION, 10);
        put(EverdellCard.CardType.TAN_TRAVELER, 10);
    }};

    @Override
    protected AbstractParameters _copy() {
        // TODO: deep copy of all variables.
        return this;
    }

    @Override
    protected boolean _equals(Object o) {
        // TODO: compare all variables.
        return o instanceof EverdellParameters;
    }

    @Override
    public int hashCode() {
        // TODO: include the hashcode of all variables.
        return super.hashCode();
    }

}
