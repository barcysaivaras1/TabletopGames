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


    public enum ResourceTypes {
        RESIN, PEBBLE, BERRY, TWIG
    }

    public enum Seasons {
        SPRING, SUMMER, FALL, WINTER
    }

    public enum Locations{
        THREE_WOOD,TWO_WOOD_ONE_CARD,TWO_RESIN,ONE_RESIN_ONE_CARD,
        TWO_CARD_ONE_POINT,ONE_PEBBLE,ONE_BERRY,ONE_BERRY_ONE_CARD
    }


    public HashMap<EverdellCard.CardType, Color> cardColour = new HashMap<EverdellCard.CardType, Color>() {{
        put(EverdellCard.CardType.BLUE_GOVERNANCE, new Color(45, 114, 173));
        put(EverdellCard.CardType.GREEN_PRODUCTION, new Color(48, 126, 38));
        put(EverdellCard.CardType.PURPLE_PROSPERITY, new Color(152, 53, 190));
        put(EverdellCard.CardType.RED_DESTINATION, new Color(201, 23, 23));
        put(EverdellCard.CardType.TAN_TRAVELER, new Color(162, 131, 93));
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
