package games.everdell.gui;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.components.EverdellCard;
import gui.AbstractGUIManager;
import gui.GamePanel;
import gui.IScreenHighlight;
import players.human.ActionController;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * <p>This class allows the visualisation of the game. The game components (accessible through {@link Game#getGameState()}
 * should be added into {@link javax.swing.JComponent} subclasses (e.g. {@link javax.swing.JLabel},
 * {@link javax.swing.JPanel}, {@link javax.swing.JScrollPane}; or custom subclasses such as those in {@link gui} package).
 * These JComponents should then be added to the <code>`parent`</code> object received in the class constructor.</p>
 *
 * <p>An appropriate layout should be set for the parent GamePanel as well, e.g. {@link javax.swing.BoxLayout} or
 * {@link java.awt.BorderLayout} or {@link java.awt.GridBagLayout}.</p>
 *
 * <p>Check the super class for methods that can be overwritten for a more custom look, or
 * {@link games.terraformingmars.gui.TMGUI} for an advanced game visualisation example.</p>
 *
 * <p>A simple implementation example can be found in {@link games.tictactoe.gui.TicTacToeGUIManager}.</p>
 */
public class EverdellGUIManager extends AbstractGUIManager {

    public EverdellGUIManager(GamePanel parent, Game game, ActionController ac, Set<Integer> human) {
        super(parent, game, ac, human);
        if (game == null) {
            return;
        }

        EverdellGameState gameState = (EverdellGameState) game.getGameState();
        EverdellParameters param = (EverdellParameters) gameState.getGameParameters();

        // TODO: set up GUI components and add to `parent`

        //Main Panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Season: "+gameState.currentSeason), BorderLayout.NORTH);
        panel.setBackground(Color.LIGHT_GRAY);

        //Meadow Cards Panel
        panel.add(meadowCardsPanel(gameState,param), BorderLayout.CENTER);

        //Player Cards Panel
        panel.add(playerCardsPanel(gameState,param), BorderLayout.SOUTH);

        //Info Panel
        JPanel infoPanel = createGameStateInfoPanel("Everdell", gameState, width, defaultInfoPanelHeight);

        //Player Resource Count Panel
        JPanel playerInfoPanel = createPlayerResourceInfoPanel(gameState.getNPlayers(),gameState);

        //Player Possible Actions Panel
        JPanel actionPanel = playerActionsPanel();

        //Add all panels to parent
        parent.setLayout(new BorderLayout());
        parent.add(infoPanel, BorderLayout.NORTH);
        parent.add(playerInfoPanel, BorderLayout.WEST);
        parent.add(panel, BorderLayout.CENTER);
        parent.add(actionPanel, BorderLayout.SOUTH);
        parent.setPreferredSize(new Dimension(400,400));
        parent.revalidate();
        parent.setVisible(true);
        parent.repaint();
    }

    //Displays all the resources that each player has
    private JPanel createPlayerResourceInfoPanel(int numPlayers,EverdellGameState gameState) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(numPlayers, 1));

        Color[] colors = {Color.RED, Color.ORANGE, Color.CYAN, Color.GREEN};

        for (int i = 0; i < numPlayers; i++) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new GridLayout(8, 1));
            playerPanel.setBackground(colors[i]);
            playerPanel.add(new JLabel("Player " + (i+1)));
            playerPanel.add(new JLabel(gameState.berries[i] + " Berries"));
            playerPanel.add(new JLabel(gameState.pebbles[i] + " Pebbles"));
            playerPanel.add(new JLabel(gameState.resin[i] + " Resin"));
            playerPanel.add(new JLabel(gameState.twigs[i] + " Twigs"));
            playerPanel.add(new JLabel(gameState.cards[i] + " Cards"));
            playerPanel.add(new JLabel(gameState.workers[i] + " Workers"));
            playerPanel.add(new JLabel(gameState.pointTokens[i] + " Point Tokens"));
            panel.add(playerPanel);
        }
        return panel;
    }

    private JPanel meadowCardsPanel(EverdellGameState gameState, EverdellParameters params){
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4,2,40 ,40));
        panel.setBackground(Color.gray);
        mainPanel.add(new JLabel("Meadow Cards", SwingConstants.CENTER), BorderLayout.NORTH);
        for(EverdellCard card : gameState.meadowDeck){
            JPanel cardPanel = new JPanel();
            cardPanel.setBackground(params.cardColour.get(card.cardType));
            cardPanel.add(new JLabel(card.cardType.name()));
            cardPanel.setPreferredSize(new Dimension(50, 100));
            panel.add(cardPanel);
        }
        mainPanel.add(panel, BorderLayout.CENTER);
        return mainPanel;
    }

    //Displays the cards that the player has in their hand
    private JPanel playerCardsPanel(EverdellGameState gameState, EverdellParameters params){
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.add(new JLabel("Player Cards"));
        for(EverdellCard card : gameState.playerHands.get(0)){
            JPanel cardPanel = new JPanel();
            cardPanel.setBackground(params.cardColour.get(card.cardType));
            cardPanel.add(new JLabel(card.cardType.name()));
            panel.add(cardPanel);
        }
        return panel;
    }

    //Displays the possible actions that the player can take
    private JPanel playerActionsPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel workerActionPanel = new JPanel();
        workerActionPanel.setBackground(Color.YELLOW);
        JPanel cardActionPanel = new JPanel();
        cardActionPanel.setBackground(Color.BLUE);
        JPanel seasonActionPanel = new JPanel();
        seasonActionPanel.setBackground(Color.GREEN);

//        cardActionPanel.setPreferredSize(new Dimension(200, 50));
        workerActionPanel.add(new JButton("Place Worker"));
        cardActionPanel.add(new JButton("Play Card"));
        seasonActionPanel.add(new JButton("End Season"));

        // Place workerActionPanel on the far left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(workerActionPanel, gbc);

        // Place cardActionPanel in the center
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(cardActionPanel, gbc);

        // Place seasonActionPanel on the far right
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(seasonActionPanel, gbc);

        return panel;
    }

    /**
     * Defines how many action button objects will be created and cached for usage if needed. Less is better, but
     * should not be smaller than the number of actions available to players in any game state.
     *
     * @return maximum size of the action space (maximum actions available to a player for any decision point in the game)
     */
    @Override
    public int getMaxActionSpace() {
        // TODO
        return 10;
    }

    /**
     * Updates all GUI elements given current game state and player that is currently acting.
     *
     * @param player    - current player acting.
     * @param gameState - current game state to be used in updating visuals.
     */
    @Override
    protected void _update(AbstractPlayer player, AbstractGameState gameState) {
        // TODO
    }
}
