package games.everdell.gui;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import games.catan.CatanParameters;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.actions.MoveSeason;
import games.everdell.actions.PlaceWorker;
import games.everdell.actions.PlayCard;
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters.ResourceTypes;
import gui.AbstractGUIManager;
import gui.GamePanel;

import core.components.Counter;
import gui.IScreenHighlight;
import org.w3c.dom.css.RGBColor;
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
        panel.setLayout(new GridLayout(3,1));
        panel.setBackground(Color.LIGHT_GRAY);

        //Meadow Cards Panel
        panel.add(meadowCardsPanel(gameState,param));

        //Player Village Cards Panel
        JPanel villageCardPanel = new JPanel();
        createVillageCardPanel(gameState,param,villageCardPanel);
        panel.add(villageCardPanel);

        //Player Cards Panel
        JPanel playerCardPanel = new JPanel();
        createPlayerCardPanel(gameState,playerCardPanel);
        panel.add(playerCardPanel);

        //Info Panel
        JPanel infoPanel = createGameStateInfoPanel("Everdell", gameState, 400, defaultInfoPanelHeight);

        //Player Resource Count Panel
        JPanel playerInfoPanel = new JPanel();
        createPlayerResourceInfoPanel(gameState,playerInfoPanel);


        //Player Possible Actions Panel
        JPanel actionPanel = playerActionsPanel(gameState,playerInfoPanel,villageCardPanel,playerCardPanel);

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
    private void createPlayerResourceInfoPanel(EverdellGameState gameState, JPanel panel) {
        panel.removeAll();
        panel.setLayout(new GridLayout(gameState.getNPlayers(), 1));

        Color[] colors = {Color.RED, Color.ORANGE, Color.CYAN, Color.GREEN};

        for (int i = 0; i < gameState.getNPlayers(); i++) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new GridLayout(9, 1));
            playerPanel.setBackground(colors[i]);
            playerPanel.add(new JLabel("Player " + (i+1)));
            playerPanel.add(new JLabel("Season : "+gameState.currentSeason[i]));
            playerPanel.add(new JLabel(gameState.PlayerResources.get(ResourceTypes.BERRY)[i].getValue() + " Berries"));
            playerPanel.add(new JLabel(gameState.PlayerResources.get(ResourceTypes.PEBBLE)[i].getValue() + " Pebbles"));
            playerPanel.add(new JLabel(gameState.PlayerResources.get(ResourceTypes.RESIN)[i].getValue() + " Resin"));
            playerPanel.add(new JLabel(gameState.PlayerResources.get(ResourceTypes.TWIG)[i].getValue() + " Twigs"));
            playerPanel.add(new JLabel(gameState.cardCount[i].getValue() + " Cards"));
            playerPanel.add(new JLabel(gameState.workers[i].getValue() + " Workers"));
            playerPanel.add(new JLabel(gameState.pointTokens[i].getValue() + " Point Tokens"));
            panel.add(playerPanel);
        }
    }

    private void createVillageCardPanel(EverdellGameState gameState, EverdellParameters params, JPanel panel){
        panel.removeAll();
        panel.setLayout(new GridLayout(gameState.getNPlayers(),1));
        panel.setBackground(new Color(45, 105, 17));

        int counter = 0;
        for(var deck : gameState.playerVillage){
            counter+=1;

            JPanel titleWrapper = new JPanel();
            titleWrapper.setLayout(new BorderLayout());
            titleWrapper.add(new JLabel("Player "+counter+" Village"),BorderLayout.NORTH);

            JPanel villagePanel = new JPanel();
            villagePanel.setLayout(new GridLayout(3,5));
            for(EverdellCard card : deck){
                JPanel cardPanel = new JPanel();
                cardPanel.setBackground(params.cardColour.get(card.cardType));
                cardPanel.add(new JLabel(card.cardType.name()));
                cardPanel.setPreferredSize(new Dimension(50, 100));
                villagePanel.add(cardPanel);
            }
            titleWrapper.add(villagePanel,BorderLayout.CENTER);
            panel.add(titleWrapper);
        }
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
    private void createPlayerCardPanel(EverdellGameState gameState, JPanel panel ){
        EverdellParameters params = (EverdellParameters) gameState.getGameParameters();

        panel.removeAll();

        panel.setBackground(Color.BLACK);
        panel.add(new JLabel("Player Cards"));
        for(EverdellCard card : gameState.playerHands.get(0)){
            JPanel cardPanel = new JPanel();
            cardPanel.setBackground(params.cardColour.get(card.cardType));
            cardPanel.add(new JLabel(card.cardType.name()));
            panel.add(cardPanel);
        }
    }

    //Displays the possible actions that the player can take
    private JPanel playerActionsPanel(EverdellGameState gameState, JPanel playerInfoPanel, JPanel villagePanel, JPanel playerCardPanel){
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
        JButton placeWorkerButton = new JButton("Place Worker");
        placeWorkerButton.addActionListener(k -> {
            new PlaceWorker().execute(gameState);
            createPlayerResourceInfoPanel(gameState,playerInfoPanel);
        });
        workerActionPanel.add(placeWorkerButton);

        JButton playCardButton = new JButton("Play Card");
        playCardButton.addActionListener(k -> {
            new PlayCard().execute(gameState);
            createPlayerResourceInfoPanel(gameState,playerInfoPanel);
            createVillageCardPanel(gameState,(EverdellParameters) gameState.getGameParameters(),villagePanel);
            createPlayerCardPanel(gameState,playerCardPanel);
        });
        cardActionPanel.add(playCardButton);

        JButton moveSeasonButton = new JButton("Move Season");
        moveSeasonButton.addActionListener(k -> {
            new MoveSeason().execute(gameState);
            createPlayerResourceInfoPanel(gameState,playerInfoPanel);
        });
        seasonActionPanel.add(moveSeasonButton);

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
