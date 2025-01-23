package games.everdell.gui;

import core.components.Deck;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.ConstructionCard;
import games.everdell.components.EverdellCard;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class TopPanel extends JPanel {

    EverdellGUIManager everdellGUIManager;
    EverdellGameState state;

    Boolean makeCardsButtons;

    private Consumer<EverdellCard> buttonAction;
    private int numberOfCardSelections;

    public TopPanel(EverdellGUIManager EverdellGUIManager, EverdellGameState state){
        super();
        this.everdellGUIManager = EverdellGUIManager;
        this.state = state;
        makeCardsButtons = false;
    }

    private void draw(){
        this.removeAll();
        this.setLayout(new BorderLayout());
        this.setBackground(Color.gray);
        JLabel meadowLabel = new JLabel("Meadow Cards");
        meadowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(meadowLabel, BorderLayout.NORTH);

        JPanel meadowPanel = new JPanel();
        meadowPanel.setLayout(new GridLayout(4,2));
        meadowPanel.setBackground(Color.gray);


        if(makeCardsButtons){
            this.add(everdellGUIManager.drawCardButtons(state,new ArrayList<>(state.meadowDeck.getComponents()), meadowPanel, buttonAction, numberOfCardSelections));
        }
        else {
            this.add(everdellGUIManager.drawCards(state,state.meadowDeck, meadowPanel));
        }
    }

    public void drawMeadowPanel(){
        makeCardsButtons = false;
        draw();
    }

    public void drawMeadowPanelButtons(int numberOfCardSelections, Consumer<EverdellCard> buttonAction){
        this.buttonAction = buttonAction;
        this.numberOfCardSelections = numberOfCardSelections;

        makeCardsButtons = true;
        draw();
    }





}
