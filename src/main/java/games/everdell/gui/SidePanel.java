package games.everdell.gui;

import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;

import javax.swing.*;
import java.awt.*;

public class SidePanel extends JPanel {

    EverdellGUIManager everdellGUIManager;
    EverdellGameState state;


    SidePanel(EverdellGUIManager EverdellGUIManager, EverdellGameState state){
        super();
        this.everdellGUIManager = EverdellGUIManager;
        this.state = state;
    }

    //Displays all the resources that each player has
    private void draw(){
        this.removeAll();
        this.setLayout(new GridLayout(state.getNPlayers(), 1));


        for (int i = 0; i < state.getNPlayers(); i++) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new GridLayout(9, 1));
            playerPanel.setBackground(EverdellParameters.playerColour.get(i));
            playerPanel.add(new JLabel("Player " + (i+1)));
            playerPanel.add(new JLabel("Season : "+state.currentSeason[i]));
            playerPanel.add(new JLabel(state.PlayerResources.get(EverdellParameters.ResourceTypes.BERRY)[i].getValue() + " Berries"));
            playerPanel.add(new JLabel(state.PlayerResources.get(EverdellParameters.ResourceTypes.PEBBLE)[i].getValue() + " Pebbles"));
            playerPanel.add(new JLabel(state.PlayerResources.get(EverdellParameters.ResourceTypes.RESIN)[i].getValue() + " Resin"));
            playerPanel.add(new JLabel(state.PlayerResources.get(EverdellParameters.ResourceTypes.TWIG)[i].getValue() + " Twigs"));
            playerPanel.add(new JLabel(state.cardCount[i].getValue() + " Cards"));
            playerPanel.add(new JLabel(state.workers[i].getValue() + " Workers"));
            playerPanel.add(new JLabel(state.pointTokens[i].getValue() + " Point Tokens"));
            playerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.add(playerPanel);
        }

    }

    public void drawPlayerInfoPanel(){
        draw();
    }

}
