package games.everdell.components;

public class CritterCard extends EverdellCard{
    public CritterCard(EverdellCard everdellCard) {
        super(everdellCard.getName(), everdellCard.getCardEnumValue(), everdellCard.getCardType(), everdellCard.isConstruction(), everdellCard.isUnique(), everdellCard.getPoints(), everdellCard.getResourceCost(), everdellCard.getApplyCardEffect());
    }
}
