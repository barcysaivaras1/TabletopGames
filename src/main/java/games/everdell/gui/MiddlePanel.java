package games.everdell.gui;

import core.components.Deck;
import games.everdell.EverdellGameState;
import games.everdell.components.EverdellCard;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class MiddlePanel extends JPanel {

    EverdellGUIManager everdellGUIManager;
    EverdellGameState state;

    Boolean makeCardsButtons;
    int villageToDisplay;

    private ArrayList<EverdellCard> cardsToDisplay;
    private Consumer<EverdellCard> buttonAction;
    private int numberOfCardSelections;

    MiddlePanel(EverdellGUIManager EverdellGUIManager, EverdellGameState state){
        super();
        this.everdellGUIManager = EverdellGUIManager;
        this.state = state;

        makeCardsButtons = false;
        villageToDisplay = 0;
    }

    private void draw(){
        this.removeAll();
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(45, 105, 17));

        this.add(new JLabel("Player "+(villageToDisplay+1)+" Village"),BorderLayout.NORTH);

        JPanel villagePanel = new JPanel();
        villagePanel.setLayout(new GridLayout(3,5));

        Deck<EverdellCard> deck = state.playerVillage.get(villageToDisplay);

        if(makeCardsButtons){
            this.add(everdellGUIManager.drawCardButtons(state, cardsToDisplay, villagePanel, buttonAction, numberOfCardSelections));
        }
        else {
            this.add(everdellGUIManager.drawCards(state,deck, villagePanel));
        }

        drawVillageNavigationButtons();
    }

    private void drawVillageNavigationButtons(){
        JPanel villageNavigationPanel = new JPanel();
        villageNavigationPanel.setLayout(new GridLayout(1,4));

        for(int i=0 ; i< state.getNPlayers(); i++){
            JButton villageButton = new JButton("Village "+(i+1));
            int num = i;
            villageButton.addActionListener(k -> {
                villageToDisplay = num;
                draw();
            });
            villageNavigationPanel.add(villageButton);
        }
        this.add(villageNavigationPanel,BorderLayout.SOUTH);
    }

    public void drawVillagePanel(){
        makeCardsButtons = false;
        draw();
    }

    public void drawVillagePanelButtons(ArrayList<EverdellCard> cardsToDisplay,int numberOfCardSelections, Consumer<EverdellCard> buttonAction){
        this.cardsToDisplay = cardsToDisplay;
        this.buttonAction = buttonAction;
        this.numberOfCardSelections = numberOfCardSelections;

        makeCardsButtons = true;
        draw();
    }

}
