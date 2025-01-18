package games.everdell.gui;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.components.Deck;
import games.catan.CatanParameters;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.actions.MoveSeason;
import games.everdell.actions.PlaceWorker;
import games.everdell.actions.PlayCard;
import games.everdell.components.ConstructionCard;
import games.everdell.components.CritterCard;
import games.everdell.components.EverdellCard;
import games.everdell.EverdellParameters.ResourceTypes;
import games.everdell.EverdellParameters.ForestLocations;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.EverdellParameters.BasicEvent;

import gui.AbstractGUIManager;
import gui.GamePanel;

import core.components.Counter;
import gui.IScreenHighlight;
import org.apache.log4j.Layout;
import org.w3c.dom.css.RGBColor;
import players.human.ActionController;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.xml.stream.Location;
import java.awt.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

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
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3,1));
        mainPanel.setBackground(Color.LIGHT_GRAY);

        //Meadow Cards Panel
        JPanel meadowCardsPanel = new JPanel();
        createMeadowCardsPanel(gameState, meadowCardsPanel);
        mainPanel.add(meadowCardsPanel);

        //Player Village Cards Panel
        JPanel villageCardPanel = new JPanel();
        createVillageCardPanel(gameState,param,villageCardPanel);
        mainPanel.add(villageCardPanel);

        //Player Cards Panel
        JPanel playerCardPanel = new JPanel();
        createPlayerCardPanel(gameState,playerCardPanel);
        mainPanel.add(playerCardPanel);



        //Info Panel
        JPanel infoPanel = createGameStateInfoPanel("Everdell", gameState, 400, defaultInfoPanelHeight);

        //Player Resource Count Panel
        JPanel playerInfoPanel = new JPanel();
        createPlayerResourceInfoPanel(gameState,playerInfoPanel);

        //Worker Placement Options Panel
        JPanel workerPlacementPanel = new JPanel();
        createWorkerPlacementPanel(gameState,workerPlacementPanel,playerInfoPanel);



        //Add all panels to parent
        parent.setLayout(new BorderLayout());

        //Player Possible Actions Panel
        JPanel actionPanel = playerActionsPanel(gameState,playerInfoPanel,villageCardPanel,playerCardPanel,meadowCardsPanel);

        parent.add(infoPanel, BorderLayout.NORTH);
        parent.add(playerInfoPanel, BorderLayout.WEST);
        parent.add(mainPanel, BorderLayout.CENTER);
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


        for (int i = 0; i < gameState.getNPlayers(); i++) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new GridLayout(9, 1));
            playerPanel.setBackground(EverdellParameters.playerColour.get(i));
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


    //Displays the possible locations that the player can place their worker
    private void createWorkerPlacementPanel(EverdellGameState gameState, JPanel panel, JPanel playerInfoPanel){
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(147, 136, 40));
        panel.add(new JLabel("Worker Placement"), BorderLayout.NORTH);
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            panel.removeAll();
            createPlayerCardPanel(gameState,panel);
        });
        panel.add(back,BorderLayout.SOUTH);

        JPanel workerOptionsPanel = new JPanel();

        JButton basicLocationsButton = new JButton("Basic Locations");
        basicLocationsButton.addActionListener(k -> {
            createBasicLocationsPlacementPanel(gameState,panel,playerInfoPanel, location -> {
                  new PlaceWorker(location).execute(gameState);
                  createPlayerResourceInfoPanel(gameState,playerInfoPanel);
                  panel.removeAll();
                  createPlayerCardPanel(gameState,panel);
            } );
        });
        workerOptionsPanel.add(basicLocationsButton,BorderLayout.WEST);
        JButton forestLocationsButton = new JButton("Forest Locations");
        forestLocationsButton.addActionListener(k -> {
            createForestLocationsPlacementPanel(gameState,panel,playerInfoPanel);
        });
        workerOptionsPanel.add(forestLocationsButton,BorderLayout.EAST);

        JButton basicEventsButton = new JButton("Basic Events");
        basicEventsButton.addActionListener(k -> {
            createBasicEventsPlacementPanel(gameState,panel,playerInfoPanel);
        });
        workerOptionsPanel.add(basicEventsButton);

        panel.add(workerOptionsPanel,BorderLayout.CENTER);
    }

    //Displays the possible locations that the player can place their worker
    private void createBasicLocationsPlacementPanel(EverdellGameState gameState, JPanel panel, JPanel playerInfoPanel, Consumer<EverdellParameters.AbstractLocations> buttionAction){
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(147, 136, 40));
        panel.add(new JLabel("Worker Placement"), BorderLayout.NORTH);
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            panel.removeAll();
            createWorkerPlacementPanel(gameState,panel,playerInfoPanel);
        });
        panel.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,gameState.Locations.size()));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : gameState.Locations.keySet()){
            if(!(location instanceof BasicLocations)){
                continue;
            }
            JButton button = new JButton(location.name());
            if(!gameState.Locations.get(location).isLocationFreeForPlayer(gameState)) {
                button.setBackground(Color.LIGHT_GRAY);
            }

            button.addActionListener(k -> {
                buttionAction.accept(location);
//                gameState.currentLocation = location;
//                new PlaceWorker().execute(gameState);
//                createPlayerResourceInfoPanel(gameState,playerInfoPanel);
//                panel.removeAll();
//                createPlayerCardPanel(gameState,panel);
            });
            locationPanel.add(button);
        }
        panel.add(locationPanel,BorderLayout.CENTER);
    }

    //Displays the possible locations that the player can place their worker
    private void createBasicEventsPlacementPanel(EverdellGameState gameState, JPanel panel, JPanel playerInfoPanel){
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(147, 136, 40));
        panel.add(new JLabel("Worker Placement"), BorderLayout.NORTH);
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            panel.removeAll();
            createWorkerPlacementPanel(gameState,panel,playerInfoPanel);
        });
        panel.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,gameState.Locations.size()));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : gameState.Locations.keySet()){
            if(!(location instanceof BasicEvent)){
                continue;
            }
            JButton button = new JButton(location.name());
            if(!gameState.Locations.get(location).isLocationFreeForPlayer(gameState)) {
                button.setBackground(EverdellParameters.playerColour.get(gameState.Locations.get(location).playersOnLocation.get(0)));
            }

            button.addActionListener(k -> {
                new PlaceWorker(location).execute(gameState);
                createPlayerResourceInfoPanel(gameState,playerInfoPanel);
                panel.removeAll();
                createPlayerCardPanel(gameState,panel);
            });
            locationPanel.add(button);
        }
        panel.add(locationPanel,BorderLayout.CENTER);
    }

    //Displays the possible locations that the player can place their worker
    private void createForestLocationsPlacementPanel(EverdellGameState gameState, JPanel panel, JPanel playerInfoPanel){
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(147, 136, 40));
        panel.add(new JLabel("Worker Placement"), BorderLayout.NORTH);
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            panel.removeAll();
            createWorkerPlacementPanel(gameState,panel,playerInfoPanel);
        });
        panel.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,gameState.Locations.size()));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : gameState.Locations.keySet()){
            if(!(location instanceof ForestLocations)){
                continue;
            }


            JButton button = new JButton(location.name());
            if(!gameState.Locations.get(location).isLocationFreeForPlayer(gameState)) {
                button.setBackground(Color.LIGHT_GRAY);
            }

            if(location == ForestLocations.TWO_ANY || location == ForestLocations.TWO_CARDS_ONE_ANY){
                button.addActionListener(k -> {
                    if(gameState.Locations.get(location).playersOnLocation.contains(gameState.playerTurn)){
                        return;
                    }

                    if(location == ForestLocations.TWO_ANY){
                        resourceSelectionPanel(gameState,panel,playerInfoPanel,2, location);
                    }
                    else{
                        resourceSelectionPanel(gameState,panel,playerInfoPanel,1, location);
                    }
                });
            }
            else if (location == ForestLocations.DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED || location == ForestLocations.DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED){
                button.addActionListener(k -> {
                    if(gameState.Locations.get(location).playersOnLocation.contains(gameState.playerTurn)){
                        return;
                    }
                    ForestLocations.cardChoices = new ArrayList<>();


                    if(location == ForestLocations.DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED){
                        turnPlayerCardsIntoButtons(gameState,panel,playerInfoPanel,panel,panel,  gameState.playerHands.get(gameState.playerTurn).getSize(), card -> {
                            gameState.currentCard = card;
                            ForestLocations.cardChoices.add(card);
                        });
                        JButton doneButton = new JButton("Discard Selected Cards");
                        doneButton.addActionListener(k2 -> {
                            System.out.println(ForestLocations.cardChoices);
                            new PlaceWorker(location).execute(gameState);
                            createPlayerResourceInfoPanel(gameState,playerInfoPanel);
                            panel.removeAll();
                            createPlayerCardPanel(gameState,panel);
                        });
                        panel.add(doneButton, BorderLayout.SOUTH);
                    }
                    else{
                        JLabel discardLabel = new JLabel("Discard up to 3 cards");
                        turnPlayerCardsIntoButtons(gameState,panel,playerInfoPanel,panel,panel, 3, card -> {
                            gameState.currentCard = card;
                            ForestLocations.cardChoices.add(card);
                        });
                        panel.add(discardLabel, BorderLayout.NORTH);

                        JButton doneButton = new JButton("Discard Selected Cards");
                        doneButton.addActionListener(k2 -> {
                            resourceSelectionPanel(gameState,panel,playerInfoPanel,ForestLocations.cardChoices.size(), location);
                        });
                        panel.add(doneButton, BorderLayout.SOUTH);
                    }
                });
            }
            else if (location == ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD){
                button.addActionListener(k -> {
                    if(gameState.Locations.get(location).playersOnLocation.contains(gameState.playerTurn)){
                        return;
                    }
                    createBasicLocationsPlacementPanel(gameState,panel,playerInfoPanel, basicLocation -> {
                        ForestLocations.basicLocationChoice = (BasicLocations) basicLocation;
                        new PlaceWorker(location).execute(gameState);

                        createPlayerResourceInfoPanel(gameState, playerInfoPanel);
                        panel.removeAll();
                        createPlayerCardPanel(gameState, panel);
                    });
                });
            }
            else {
                button.addActionListener(k -> {
                    new PlaceWorker(location).execute(gameState);
                    createPlayerResourceInfoPanel(gameState, playerInfoPanel);
                    panel.removeAll();
                    createPlayerCardPanel(gameState, panel);
                });
            }
            locationPanel.add(button);

        }
        panel.add(locationPanel,BorderLayout.CENTER);
    }

    private void resourceSelectionPanel(EverdellGameState gameState, JPanel panel, JPanel playerInfoPanel, int numberOfResources, EverdellParameters.AbstractLocations location){
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(147, 136, 40));
        panel.add(new JLabel("Worker Placement"), BorderLayout.NORTH);
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            panel.removeAll();
            createWorkerPlacementPanel(gameState,panel,playerInfoPanel);
        });
        panel.add(back,BorderLayout.SOUTH);

        JPanel resourcePanel = new JPanel();


        gameState.resourceChoices = new ArrayList<>();

        JButton berryButton = new JButton("Berry");
        berryButton.addActionListener(k -> {
            gameState.resourceChoices.add(ResourceTypes.BERRY);
            if(gameState.resourceChoices.size() == numberOfResources){
                new PlaceWorker(location).execute(gameState);
                createPlayerResourceInfoPanel(gameState,playerInfoPanel);
                panel.removeAll();
                createPlayerCardPanel(gameState,panel);
            }
        });
        resourcePanel.add(berryButton);
        JButton pebbleButton = new JButton("Pebble");
        pebbleButton.addActionListener(k -> {
            gameState.resourceChoices.add(ResourceTypes.PEBBLE);
            if(gameState.resourceChoices.size() == numberOfResources){
                new PlaceWorker(location).execute(gameState);
                createPlayerResourceInfoPanel(gameState,playerInfoPanel);
                panel.removeAll();
                createPlayerCardPanel(gameState,panel);
            }
        });
        resourcePanel.add(pebbleButton);
        JButton resinButton = new JButton("Resin");
        resinButton.addActionListener(k -> {
            gameState.resourceChoices.add(ResourceTypes.RESIN);
            if(gameState.resourceChoices.size() == numberOfResources){
                new PlaceWorker(location).execute(gameState);
                createPlayerResourceInfoPanel(gameState,playerInfoPanel);
                panel.removeAll();
                createPlayerCardPanel(gameState,panel);
            }
        });
        resourcePanel.add(resinButton);
        JButton twigButton = new JButton("Twig");
        twigButton.addActionListener(k -> {
            gameState.resourceChoices.add(ResourceTypes.TWIG);
            if(gameState.resourceChoices.size() == numberOfResources){
                new PlaceWorker(location).execute(gameState);
                createPlayerResourceInfoPanel(gameState,playerInfoPanel);
                panel.removeAll();
                createPlayerCardPanel(gameState,panel);
            }
        });
        resourcePanel.add(twigButton);

        panel.add(resourcePanel,BorderLayout.CENTER);


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
//            for(EverdellCard card : deck){
//                JPanel cardPanel = new JPanel();
//                cardPanel.setBackground(params.cardColour.get(card.getCardType()));
//                cardPanel.add(new JLabel(card.getName()));
//                cardPanel.setPreferredSize(new Dimension(50, 100));
//                villagePanel.add(cardPanel);
//            }
            titleWrapper.add(drawCards(gameState,deck,villagePanel ),BorderLayout.CENTER);
            panel.add(titleWrapper);
        }
    }

    private void createMeadowCardsPanel(EverdellGameState gameState, JPanel panel){

        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.gray);
        JLabel meadowLabel = new JLabel("Meadow Cards");
        meadowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(meadowLabel, BorderLayout.NORTH);

        JPanel meadowPanel = new JPanel();
        meadowPanel.setLayout(new GridLayout(4,2));
        meadowPanel.setBackground(Color.gray);

        panel.add(drawCards(gameState,gameState.meadowDeck, meadowPanel));

//        for(EverdellCard card : gameState.meadowDeck){
//            JPanel cardPanel = new JPanel();
//            cardPanel.setBackground(params.cardColour.get(card.getCardType()));
//            cardPanel.add(new JLabel(card.getName()));
//            cardPanel.setPreferredSize(new Dimension(50, 100));
//
//            JPanel resourcePanel = new JPanel();
//            resourcePanel.setBackground(params.cardColour.get(card.getCardType()));
//            resourcePanel.setLayout(new GridLayout(3,1));
//            for(ResourceTypes resource : card.getResourceCost().keySet()){
//                resourcePanel.add(new JLabel(resource.name() + " : " + card.getResourceCost().get(resource)));
//            }
//            cardPanel.add(resourcePanel);
//
//            panel.add(cardPanel);
//        }
    }
    private void createPaymentChoicePanel(EverdellGameState gameState, JPanel panel, JPanel playerInfoPanel, JPanel villagePanel, JPanel playerCardPanel, JPanel meadowCardsPanel,int numberOfOccupation, ConstructionCard constructionCard){
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(147, 136, 40));
        panel.add(new JLabel("Worker Placement"), BorderLayout.NORTH);
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            panel.removeAll();
            createPlayerCardPanel(gameState,panel);
        });
        panel.add(back,BorderLayout.SOUTH);

        JPanel paymentPanel = new JPanel();


        gameState.resourceChoices = new ArrayList<>();

        JButton resourcesButton = new JButton("Pay With Resources");
        resourcesButton.addActionListener(k -> {
            //Place the card
            placeACard(gameState,playerInfoPanel,villagePanel,playerCardPanel,meadowCardsPanel);
        });
        paymentPanel.add(resourcesButton);

        JButton occupationButton = new JButton("Pay With Occupation");
        occupationButton.addActionListener(k -> {
            if(numberOfOccupation == 1) {

                constructionCard.occupyConstruction((CritterCard) gameState.currentCard);

                //Place the card
                placeACard(gameState,playerInfoPanel,villagePanel,playerCardPanel,meadowCardsPanel);
            }

        });
        paymentPanel.add(occupationButton);

        panel.add(paymentPanel,BorderLayout.CENTER);


    }

    //Displays the cards that the player has in their hand
    private void createPlayerCardPanel(EverdellGameState gameState, JPanel panel ){
        EverdellParameters params = (EverdellParameters) gameState.getGameParameters();

        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel playerCardLabel = new JLabel("Player Cards");
        playerCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(playerCardLabel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel();
        cardsPanel.setBackground(Color.BLACK);
        panel.add(drawCards(gameState,gameState.playerHands.get(0), cardsPanel), BorderLayout.CENTER);
    }

    private void turnPlayerCardsIntoButtons(EverdellGameState gameState, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel,JPanel meadowPanel,int numberOfSelections, Consumer<EverdellCard> buttonAction){
        //Convert Player Cards into Buttons
        EverdellParameters params = (EverdellParameters) gameState.getGameParameters();

        playerCardPanel.removeAll();
        playerCardPanel.setLayout(new BorderLayout());
        playerCardPanel.setBackground(Color.WHITE);

        JLabel playerCardLabel = new JLabel("Player Cards");
        playerCardLabel.setHorizontalAlignment(SwingConstants.CENTER);

        playerCardPanel.add(playerCardLabel, BorderLayout.NORTH);

        JPanel handPanel = new JPanel();
        handPanel.setBackground(Color.BLACK);


        for(EverdellCard card : gameState.playerHands.get(0)){
            JPanel cardPanel = new JPanel();

            JButton cardButton = new JButton();
            cardButton.setBackground(params.cardColour.get(card.getCardType()));
            cardButton.setBorder(new LineBorder(Color.green, 2));

            cardButton.addActionListener(k -> {
                //If the card is already selected, unselect it
                if(cardButton.getBackground() == Color.GRAY){
                    cardButton.setBackground(params.cardColour.get(card.getCardType()));
                    ForestLocations.cardChoices.remove(card);
                }
                //Limit the number of selections
                else if(numberOfSelections == ForestLocations.cardChoices.size()){
                    return;
                }
                //Select the card
                else {
                    buttonAction.accept(card);
                    cardButton.setBackground(Color.GRAY);
                }
            });
            JPanel resourcePanel = new JPanel();
            resourcePanel.setBackground(params.cardColour.get(card.getCardType()));
            resourcePanel.setLayout(new GridLayout(3,1));
            for(ResourceTypes resource : card.getResourceCost().keySet()){
                resourcePanel.add(new JLabel(resource.name() + " : " + card.getResourceCost().get(resource)));
            }
            JLabel cardLabel = new JLabel(card.getName());

            cardButton.setLayout(new FlowLayout());
            cardButton.add(cardLabel);
            cardButton.add(resourcePanel);

            handPanel.add(cardButton);
        }
        playerCardPanel.add(handPanel,BorderLayout.CENTER);
    }


    private void turnMeadowCardsIntoButtons(EverdellGameState gameState, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel,JPanel meadowPanel, Consumer<EverdellCard> buttonAction){
        //Convert Player Cards into Buttons
        EverdellParameters params = (EverdellParameters) gameState.getGameParameters();

        meadowPanel.removeAll();
        JPanel meadowCardsPanel = new JPanel();
        meadowCardsPanel.setLayout(new GridLayout(4,2));
        //Convert Meadow Cards into Buttons
        for(EverdellCard card : gameState.meadowDeck){
            JButton cardButton = new JButton();
            cardButton.setBackground(params.cardColour.get(card.getCardType()));
            cardButton.setBorder(new LineBorder(Color.green, 2));
            cardButton.setPreferredSize(new Dimension(50, 100));
            cardButton.setHorizontalAlignment(SwingConstants.CENTER);

            cardButton.addActionListener(k -> {
                if(cardButton.getBackground() == Color.GRAY){
                    cardButton.setBackground(params.cardColour.get(card.getCardType()));
                    gameState.cardSelection.remove(card);
                }
                else {
                    cardButton.setBackground(Color.GRAY);
                    buttonAction.accept(card);
                }
            });

            JPanel resourcePanel = new JPanel();
            resourcePanel.setBackground(params.cardColour.get(card.getCardType()));
            resourcePanel.setLayout(new GridLayout(3,1));
            for(ResourceTypes resource : card.getResourceCost().keySet()){
                resourcePanel.add(new JLabel(resource.name() + " : " + card.getResourceCost().get(resource)));
            }
            JLabel cardLabel = new JLabel(card.getName());

            cardButton.setLayout(new FlowLayout());
            cardButton.add(cardLabel);
            cardButton.add(resourcePanel);

            meadowCardsPanel.add(cardButton);
        }
        JLabel meadowLabel = new JLabel("Meadow Cards");
        meadowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        meadowPanel.add(meadowLabel, BorderLayout.NORTH);

        meadowPanel.add(meadowCardsPanel, BorderLayout.CENTER);
    }

//    private JPanel drawCardButtons(EverdellGameState state, Deck<EverdellCard> cards, JPanel panelToDrawOn, Consumer<EverdellCard> buttonAction){
//        EverdellParameters params = (EverdellParameters) state.getGameParameters();
//
//        for(EverdellCard card : cards){
//            JButton cardButton = new JButton();
//            cardButton.setBackground(params.cardColour.get(card.getCardType()));
//            cardButton.setBorder(new LineBorder(Color.green, 2));
//            cardButton.setPreferredSize(new Dimension(50, 100));
//            cardButton.setHorizontalAlignment(SwingConstants.CENTER);
//
//            cardButton.addActionListener(k -> {
//                if(cardButton.getBackground() == Color.GRAY){
//                    cardButton.setBackground(params.cardColour.get(card.getCardType()));
//                    state.cardSelection.remove(card);
//                }
//                else {
//                    cardButton.setBackground(Color.GRAY);
//                    buttonAction.accept(card);
//                }
//            });
//
//            JPanel resourcePanel = new JPanel();
//            resourcePanel.setBackground(params.cardColour.get(card.getCardType()));
//            resourcePanel.setLayout(new GridLayout(3,1));
//            for(ResourceTypes resource : card.getResourceCost().keySet()){
//                resourcePanel.add(new JLabel(resource.name() + " : " + card.getResourceCost().get(resource)));
//            }
//            JLabel cardLabel = new JLabel(card.getName());
//
//            cardButton.setLayout(new FlowLayout());
//            cardButton.add(cardLabel);
//            cardButton.add(resourcePanel);
//
//            panelToDrawOn.add(cardButton);
//        }
//        return panelToDrawOn;
//    }

    private JPanel drawCards(EverdellGameState state, Deck<EverdellCard> cards,JPanel panelToDrawOn){
        EverdellParameters params = (EverdellParameters) state.getGameParameters();
        //panel.setLayout(new GridLayout(1,cards.getSize()));
        for(EverdellCard card : cards){
            JPanel cardPanel = new JPanel();
            cardPanel.setBackground(params.cardColour.get(card.getCardType()));
            cardPanel.add(new JLabel(card.getName()));
            JPanel resourcePanel = new JPanel();
            resourcePanel.setBackground(params.cardColour.get(card.getCardType()));
            resourcePanel.setLayout(new GridLayout(3,1));
            for(ResourceTypes resource : card.getResourceCost().keySet()){
                resourcePanel.add(new JLabel(resource.name() + " : " + card.getResourceCost().get(resource)));
            }
            cardPanel.add(resourcePanel);
            panelToDrawOn.add(cardPanel);
        }
        return panelToDrawOn;
    }

    private void placeACard(EverdellGameState gameState, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel, JPanel meadowCardsPanel){
        checkForAdditionalStepsForCard(gameState,playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel,gameState.currentCard);

        //Place the card
        new PlayCard().execute(gameState);

        //Redraw the Panels
        createPlayerResourceInfoPanel(gameState,playerInfoPanel);
        createVillageCardPanel(gameState,(EverdellParameters) gameState.getGameParameters(),villagePanel);
        createPlayerCardPanel(gameState,playerCardPanel);
        createMeadowCardsPanel(gameState,meadowCardsPanel);
    }

    //Displays the possible actions that the player can take
    private JPanel playerActionsPanel(EverdellGameState gameState, JPanel playerInfoPanel, JPanel villagePanel, JPanel playerCardPanel, JPanel meadowCardsPanel){
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
                createPlayerResourceInfoPanel(gameState,playerInfoPanel);
                playerCardPanel.removeAll();
                createWorkerPlacementPanel(gameState,playerCardPanel,playerInfoPanel);
        });
        workerActionPanel.add(placeWorkerButton);

        JButton playCardButton = new JButton("Play Card");
        playCardButton.addActionListener(k -> {
            //Make player cards available for selection via buttons
            turnPlayerCardsIntoButtons(gameState,playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel, gameState.playerHands.get(gameState.playerTurn).getSize(), card -> {
                //Define which card needs to be placed
                gameState.currentCard = card;

                //Can the card occupy a Construction Card
                for(EverdellCard c : gameState.playerVillage.get(gameState.playerTurn)) {
                    if (c instanceof ConstructionCard) {
                        System.out.println("Construction Card");
                        System.out.println(((ConstructionCard) c).getCardsThatCanOccupy());
                        System.out.println(card.getCardEnumValue());
                        if(((ConstructionCard) c).canCardOccupyThis(gameState)){
                            createPaymentChoicePanel(gameState, playerCardPanel, playerInfoPanel, villagePanel, playerCardPanel, meadowCardsPanel, ((ConstructionCard) c).getCardsThatCanOccupy().size(),(ConstructionCard) c);
                            return;
                        }
                    }
                }
                //If not we make them pay with resources
                //Place the card
                placeACard(gameState, playerCardPanel, playerInfoPanel, villagePanel, meadowCardsPanel);
            });

            //Make meadow cards available for selection via buttons
            turnMeadowCardsIntoButtons(gameState, playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel, card ->{
                //Define which card needs to be placed
                gameState.currentCard = card;

                //Can the card occupy a Construction Card
                for(EverdellCard c : gameState.playerVillage.get(gameState.playerTurn)) {
                    if (c instanceof ConstructionCard) {
                        System.out.println("Construction Card");
                        System.out.println(((ConstructionCard) c).getCardsThatCanOccupy());
                        System.out.println(card.getCardEnumValue());
                        if(((ConstructionCard) c).canCardOccupyThis(gameState)){
                            createPaymentChoicePanel(gameState, playerCardPanel, playerInfoPanel, villagePanel, playerCardPanel, meadowCardsPanel, ((ConstructionCard) c).getCardsThatCanOccupy().size(),(ConstructionCard) c);
                            return;
                        }
                    }
                }

                //Place the card
                placeACard(gameState, playerCardPanel, playerInfoPanel, villagePanel, meadowCardsPanel);
            });
        });
        cardActionPanel.add(playCardButton);

        JButton moveSeasonButton = new JButton("Move Season");
        moveSeasonButton.addActionListener(k -> {
            new MoveSeason().execute(gameState);

            //If it is summer, the player must draw 2 cards from the meadow
            if(gameState.currentSeason[gameState.playerTurn] == EverdellParameters.Seasons.SUMMER){
                summerEventGUI(gameState, playerCardPanel, playerInfoPanel, villagePanel, meadowCardsPanel);
            }
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

    private Boolean checkForAdditionalStepsForCard(EverdellGameState state, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel, JPanel meadowPanel, EverdellCard card){
        //Husband requires the player to
        if(state.currentCard.getCardEnumValue() == EverdellParameters.CardDetails.HUSBAND){

        }
        return false;
    }

    private void summerEventGUI(EverdellGameState state,JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel,JPanel meadowPanel){
        state.cardSelection = new ArrayList<EverdellCard>();

        //When it is summer, the player will be given the choice to grab 2 cards from the meadow into their hand(Depending on how many cards they have)

        if(state.playerHands.get(state.playerTurn).getSize() >=  state.playerHands.get(state.playerTurn).getCapacity()){
            //Players hand is full, we do nothing
            return;
        }
        else if(state.playerHands.get(state.playerTurn).getSize() ==  state.playerHands.get(state.playerTurn).getCapacity()-1){
            //Allow the player to pick 1 card
            turnMeadowCardsIntoButtons(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel, card -> {
                state.cardSelection.add(card);
                new MoveSeason().summerEvent(state);
                //Redraw the Panels
                createMeadowCardsPanel(state,meadowPanel);
                createPlayerCardPanel(state,playerCardPanel);
                createPlayerResourceInfoPanel(state,playerInfoPanel);
            });
        }
        else{
            //Allow the player to pick 2 cards
            turnMeadowCardsIntoButtons(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel, card -> {
                state.cardSelection.add(card);
                if(state.cardSelection.size() == 2){
                    new MoveSeason().summerEvent(state);
                    //Redraw the Panels
                    createMeadowCardsPanel(state,meadowPanel);
                    createPlayerCardPanel(state,playerCardPanel);
                    createPlayerResourceInfoPanel(state,playerInfoPanel);
                }
            });
        }
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
