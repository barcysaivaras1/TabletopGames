package games.everdell.gui;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.CoreConstants;
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

import games.everdell.components.PeddlerCard;
import gui.AbstractGUIManager;
import gui.GamePanel;

import core.components.Counter;
import gui.IScreenHighlight;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;
import org.apache.log4j.Layout;
import org.w3c.dom.css.RGBColor;
import players.human.ActionController;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.xml.stream.Location;
import java.awt.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        createVillageCardPanel(gameState, villageCardPanel,0);
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
                    if(gameState.Locations.get(location).playersOnLocation.contains(gameState.playerTurn)) {
                        return;
                    }
                    if(location == ForestLocations.TWO_ANY){
                        int numOfResource =2;
                        resourceSelectionPanel(gameState,panel,playerInfoPanel,numOfResource, "Select "+numOfResource+" Resources", new ArrayList<ResourceTypes>(){{
                            add(ResourceTypes.BERRY);
                            add(ResourceTypes.PEBBLE);
                            add(ResourceTypes.RESIN);
                            add(ResourceTypes.TWIG);
                        }}, game -> {
                            new PlaceWorker(location).execute(game);
                            createPlayerResourceInfoPanel(game,playerInfoPanel);
                            panel.removeAll();
                            createPlayerCardPanel(game,panel);
                            return true;
                        });
                    }
                    else{
                        int numOfResource =1;
                        resourceSelectionPanel(gameState,panel,playerInfoPanel,numOfResource,"Select "+numOfResource+" Resources", new ArrayList<ResourceTypes>(){{
                            add(ResourceTypes.BERRY);
                            add(ResourceTypes.PEBBLE);
                            add(ResourceTypes.RESIN);
                            add(ResourceTypes.TWIG);
                        }}, game -> {
                            new PlaceWorker(location).execute(game);
                            createPlayerResourceInfoPanel(game,playerInfoPanel);
                            panel.removeAll();
                            createPlayerCardPanel(game,panel);
                            return true;
                        });
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
                            ForestLocations.cardChoices.add(card);
                        });
                        panel.add(discardLabel, BorderLayout.NORTH);

                        JButton doneButton = new JButton("Discard Selected Cards");
                        doneButton.addActionListener(k2 -> {
                            resourceSelectionPanel(gameState,panel,playerInfoPanel,ForestLocations.cardChoices.size(),
                                    "Select "+ForestLocations.cardChoices.size()+" Resources",  new ArrayList<ResourceTypes>(){{
                                add(ResourceTypes.BERRY);
                                add(ResourceTypes.PEBBLE);
                                add(ResourceTypes.RESIN);
                                add(ResourceTypes.TWIG);
                            }}, game -> {
                                new PlaceWorker(location).execute(game);
                                createPlayerResourceInfoPanel(game,playerInfoPanel);
                                panel.removeAll();
                                createPlayerCardPanel(game,panel);
                                return true;
                            });
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

    private void resourceSelectionPanel(EverdellGameState gameState, JPanel panel, JPanel playerInfoPanel, int numberOfResources, String labelText, ArrayList<ResourceTypes> allowedResources, Function<EverdellGameState,Boolean> buttonAction){
        panel.removeAll();
        panel.setLayout(new BorderLayout());


        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            panel.removeAll();
            gameState.resourceSelection = new HashMap<ResourceTypes,Counter>();
            gameState.resourceSelection.put(ResourceTypes.BERRY, new Counter());
            gameState.resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
            gameState.resourceSelection.put(ResourceTypes.RESIN, new Counter());
            gameState.resourceSelection.put(ResourceTypes.TWIG, new Counter());
            createPlayerCardPanel(gameState,panel);
        });

        JPanel traversalPanel = new JPanel();
        traversalPanel.setLayout(new GridLayout(1,2));

        JPanel resourcePanel = new JPanel();


        JLabel resourceLabel = new JLabel(labelText);
        resourceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resourceLabel.setBackground(Color.WHITE);
        resourceLabel.setOpaque(true);

        Consumer<EverdellGameState> redraw = game -> {
            panel.removeAll();
            resourceSelectionPanel(game,panel,playerInfoPanel,numberOfResources, labelText,allowedResources,buttonAction);
        };

        JPanel berryPanel = new JPanel();
        drawResourceButtons(gameState,berryPanel,ResourceTypes.BERRY,new Color(160, 66, 239),redraw,numberOfResources);

        JPanel pebblePanel = new JPanel();
        drawResourceButtons(gameState,pebblePanel,ResourceTypes.PEBBLE,new Color(162, 162, 151),redraw,numberOfResources);

        JPanel resinPanel = new JPanel();
        drawResourceButtons(gameState,resinPanel,ResourceTypes.RESIN,new Color(250, 168, 79),redraw,numberOfResources);

        JPanel twigPanel = new JPanel();
        drawResourceButtons(gameState,twigPanel,ResourceTypes.TWIG,new Color(145, 92, 52),redraw,numberOfResources);

        JButton doneButton = new JButton("Done");

        //Could make it more accessible by making this function work differently.
        //Could pass a boolean value to say whether selection is strict or not
        //Aka if the player can select less or if it has to be the exact amount
        doneButton.addActionListener(k -> {
            if(gameState.resourceSelection.values().stream().mapToInt(Counter::getValue).sum() <= numberOfResources) {
                buttonAction.apply(gameState);

                gameState.resourceSelection = new HashMap<ResourceTypes, Counter>();
                gameState.resourceSelection.put(ResourceTypes.BERRY, new Counter());
                gameState.resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
                gameState.resourceSelection.put(ResourceTypes.RESIN, new Counter());
                gameState.resourceSelection.put(ResourceTypes.TWIG, new Counter());
            }
        });


        if(allowedResources.contains(ResourceTypes.BERRY)){
            resourcePanel.add(berryPanel);
        }
        if(allowedResources.contains(ResourceTypes.PEBBLE)){
            resourcePanel.add(pebblePanel);
        }
        if(allowedResources.contains(ResourceTypes.RESIN)){
            resourcePanel.add(resinPanel);
        }
        if(allowedResources.contains(ResourceTypes.TWIG)){
            resourcePanel.add(twigPanel);
        }

        resourcePanel.setLayout(new GridLayout(2,2));

        panel.add(resourceLabel,BorderLayout.NORTH);
        panel.add(resourcePanel,BorderLayout.CENTER);

        traversalPanel.add(back);
        traversalPanel.add(doneButton);
        panel.add(traversalPanel,BorderLayout.SOUTH);
    }

    private void drawResourceButtons(EverdellGameState state ,JPanel panelToDrawOn, ResourceTypes resourceType, Color resourceColour, Consumer<EverdellGameState> redrawButton, int amountOfResources){

        JButton resourceButtonPlus = new JButton(resourceType+" +");
        resourceButtonPlus.addActionListener(k -> {
            if(state.resourceSelection.get(resourceType).getValue() == amountOfResources){
                return;
            }
            state.resourceSelection.get(resourceType).increment();
            redrawButton.accept(state);
        });
        JButton resourceButtonMinus = new JButton(resourceType+" -");
        resourceButtonMinus.addActionListener(k -> {
            if(state.resourceSelection.get(resourceType).getValue() == 0){
                return;
            }
            state.resourceSelection.get(resourceType).decrement();
            redrawButton.accept(state);
        });

        JLabel resourceLabel = new JLabel("" + state.resourceSelection.get(resourceType).getValue());

        panelToDrawOn.setBackground(resourceColour);
        panelToDrawOn.add(resourceButtonMinus);
        panelToDrawOn.add(resourceLabel);
        panelToDrawOn.add(resourceButtonPlus);

    }

    private void createVillageCardPanel(EverdellGameState gameState,JPanel panel, int currentVillage){
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(45, 105, 17));

        panel.add(new JLabel("Player "+(currentVillage+1)+" Village"),BorderLayout.NORTH);

        JPanel villagePanel = new JPanel();
        villagePanel.setLayout(new GridLayout(3,5));

        Deck<EverdellCard> deck = gameState.playerVillage.get(currentVillage);
//            for(EverdellCard card : deck){
//                JPanel cardPanel = new JPanel();
//                cardPanel.setBackground(params.cardColour.get(card.getCardType()));
//                cardPanel.add(new JLabel(card.getName()));
//                cardPanel.setPreferredSize(new Dimension(50, 100));
//                villagePanel.add(cardPanel);
//            }
        panel.add(drawCards(gameState,deck,villagePanel),BorderLayout.CENTER);

        JButton village1 = new JButton("Village 1");
        village1.addActionListener(k -> {
            createVillageCardPanel(gameState,panel,0);
        });
        JButton village2 = new JButton("Village 2");
        village2.addActionListener(k -> {
            createVillageCardPanel(gameState,panel,1);
        });
        JButton village3 = new JButton("Village 3");
        village3.addActionListener(k -> {
            createVillageCardPanel(gameState,panel,2);
        });
        JButton village4 = new JButton("Village 4");
        village4.addActionListener(k -> {
            createVillageCardPanel(gameState,panel,3);
        });

        JPanel villageSelectionPanel = new JPanel();
        villageSelectionPanel.setLayout(new GridLayout(1,4));
        villageSelectionPanel.add(village1);
        villageSelectionPanel.add(village2);
        if(gameState.getNPlayers() > 2) {
            villageSelectionPanel.add(village3);
        }
        if (gameState.getNPlayers() > 3) {
            villageSelectionPanel.add(village4);
        }
        panel.add(villageSelectionPanel,BorderLayout.SOUTH);
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
    }
    private void createPaymentChoicePanel(EverdellGameState gameState, JPanel panel, JPanel playerInfoPanel, JPanel villagePanel, JPanel playerCardPanel, JPanel meadowCardsPanel,int numberOfOccupation, ConstructionCard constructionCard, EverdellCard cardToPlace){
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

        JButton resourcesButton = new JButton("Pay With Resources");
        resourcesButton.addActionListener(k -> {
            //Place the card
            placeACard(gameState,playerCardPanel,playerInfoPanel, villagePanel,meadowCardsPanel,cardToPlace);
        });
        paymentPanel.add(resourcesButton);

        JButton occupationButton = new JButton("Pay With Occupation");
        occupationButton.addActionListener(k -> {
            if(numberOfOccupation == 1) {

                constructionCard.occupyConstruction((CritterCard) cardToPlace);

                //Place the card
                placeACard(gameState,playerCardPanel,playerInfoPanel, villagePanel,meadowCardsPanel,cardToPlace);
            }

        });
        paymentPanel.add(occupationButton);

        panel.add(paymentPanel,BorderLayout.CENTER);


    }

    //Displays the cards that the player has in their hand
    private void createPlayerCardPanel(EverdellGameState gameState, JPanel panel ){
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel playerCardLabel = new JLabel("Player Cards");
        playerCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(playerCardLabel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel();
        cardsPanel.setBackground(Color.BLACK);
        cardsPanel.setLayout(new GridLayout(2,4));
        panel.add(drawCards(gameState,gameState.playerHands.get(0), cardsPanel), BorderLayout.CENTER);
    }

    private void turnPlayerCardsIntoButtons(EverdellGameState gameState, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel,JPanel meadowPanel,int numberOfSelections, Consumer<EverdellCard> buttonAction){
        playerCardPanel.removeAll();
        playerCardPanel.setLayout(new BorderLayout());
        playerCardPanel.setBackground(Color.WHITE);

        JLabel playerCardLabel = new JLabel("Player Cards");
        playerCardLabel.setHorizontalAlignment(SwingConstants.CENTER);

        playerCardPanel.add(playerCardLabel, BorderLayout.NORTH);

        //Convert Player Cards into Buttons
        JPanel handPanel = new JPanel();
        handPanel.setBackground(Color.BLACK);
        handPanel.setLayout(new GridLayout(2,4));

        drawCardButtons(gameState,new ArrayList<>(gameState.playerHands.get(0).getComponents()),handPanel,buttonAction,numberOfSelections);

        playerCardPanel.add(handPanel,BorderLayout.CENTER);
    }


    private void turnMeadowCardsIntoButtons(EverdellGameState gameState, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel,JPanel meadowPanel, int numberOfSelections, Consumer<EverdellCard> buttonAction){
        //Convert Meadow Cards into Buttons
        JPanel meadowCardsPanel = new JPanel();
        meadowCardsPanel.setLayout(new GridLayout(4,2));

        // To remove the component from the center
        BorderLayout layout = (BorderLayout) meadowPanel.getLayout();
        Component centerComponent = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent != null) {
            meadowPanel.remove(centerComponent);
        }



        drawCardButtons(gameState,new ArrayList<>(gameState.meadowDeck.getComponents()),meadowCardsPanel,buttonAction,numberOfSelections);

        meadowPanel.add(meadowCardsPanel, BorderLayout.CENTER);
    }
    private void turnVillageCardsIntoButtons(EverdellGameState gameState, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel,JPanel meadowPanel, ArrayList<EverdellCard> cardsToTurnIntoButtons, Consumer<EverdellCard> buttonAction, int numberOfSelections){
        //Convert Village Cards into Buttons
        JPanel villageCardsPanel = new JPanel();
        villageCardsPanel.setLayout(new GridLayout(3,5));

        // To remove the component from the center
        BorderLayout layout = (BorderLayout) villagePanel.getLayout();
        Component centerComponent = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent != null) {
            villagePanel.remove(centerComponent);
        }

        drawCardButtons(gameState,cardsToTurnIntoButtons,villageCardsPanel,buttonAction,numberOfSelections);
        villagePanel.add(villageCardsPanel, BorderLayout.CENTER);
    }



    private JPanel drawCardButtons(EverdellGameState state, ArrayList<EverdellCard> cards, JPanel panelToDrawOn, Consumer<EverdellCard> buttonAction, int numberOfSelections){
        EverdellParameters params = (EverdellParameters) state.getGameParameters();

        for(EverdellCard card : cards){

            JButton cardButton = new JButton();
            cardButton.setBackground(params.cardColour.get(card.getCardType()));
            cardButton.setBorder(new LineBorder(Color.green, 2));

            cardButton.addActionListener(k -> {
                //If the card is already selected, unselect it
                if(cardButton.getBackground() == Color.GRAY){
                    cardButton.setBackground(params.cardColour.get(card.getCardType()));
                    ForestLocations.cardChoices.remove(card);
                    state.cardSelection.remove(card);
                }
                //Limit the number of selections
                else if(numberOfSelections == ForestLocations.cardChoices.size() || numberOfSelections == state.cardSelection.size()){
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

            JPanel additionalInfoPanel = new JPanel();
            additionalInfoPanel.setBackground(params.cardColour.get(card.getCardType()));
            additionalInfoPanel.setLayout(new GridLayout(3,1));
            if(card instanceof ConstructionCard){
                additionalInfoPanel.add(new JLabel("Construction"));
                String cardsThatCanOccupy = "";
                if(((ConstructionCard) card).isOccupied()){
                    cardsThatCanOccupy = "Occupied";
                }
                else{
                    for(EverdellParameters.CardDetails c : ((ConstructionCard) card).getCardsThatCanOccupy()){
                        cardsThatCanOccupy += c.name() + ", ";
                    }
                }
                JLabel canOccupyLabel = new JLabel(cardsThatCanOccupy);
                additionalInfoPanel.add(canOccupyLabel);
            }
            else{
                additionalInfoPanel.add(new JLabel("Critter"));
            }
            JPanel cardNamePanel = new JPanel();
            cardNamePanel.setLayout(new BorderLayout());
            cardNamePanel.setBackground(params.cardColour.get(card.getCardType()));

            JLabel cardLabel = new JLabel(card.getName());
            cardLabel.setOpaque(true);
            cardLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cardLabel.setBackground(new Color(201, 153, 35));

            cardNamePanel.add(cardLabel, BorderLayout.NORTH);

            JLabel isUnique = new JLabel();
            isUnique.setOpaque(true);
            isUnique.setHorizontalAlignment(SwingConstants.CENTER);
            if(card.isUnique()){
                isUnique.setText("Unique");
                isUnique.setBackground(new Color(201, 153, 35));
            }
            else{
                isUnique.setText("Common");
                isUnique.setBackground(Color.white);
            }
            cardNamePanel.add(isUnique, BorderLayout.SOUTH);

            additionalInfoPanel.add(new JLabel("Points : " + card.getPoints()));

            cardButton.setLayout(new FlowLayout());
            cardButton.add(additionalInfoPanel);
            cardButton.add(cardNamePanel);
            cardButton.add(resourcePanel);

            panelToDrawOn.add(cardButton);
        }


        return panelToDrawOn;
    }

    private JPanel drawCards(EverdellGameState state, Deck<EverdellCard> cards,JPanel panelToDrawOn){
        EverdellParameters params = (EverdellParameters) state.getGameParameters();
        //panel.setLayout(new GridLayout(1,cards.getSize()));
        for(EverdellCard card : cards){
            JPanel cardPanel = new JPanel();
            cardPanel.setBackground(params.cardColour.get(card.getCardType()));
            JPanel resourcePanel = new JPanel();
            resourcePanel.setBackground(params.cardColour.get(card.getCardType()));
            resourcePanel.setLayout(new GridLayout(3,1));
            for(ResourceTypes resource : card.getResourceCost().keySet()){
                resourcePanel.add(new JLabel(resource.name() + " : " + card.getResourceCost().get(resource)));
            }
            JPanel additionalInfoPanel = new JPanel();
            additionalInfoPanel.setBackground(params.cardColour.get(card.getCardType()));
            additionalInfoPanel.setLayout(new GridLayout(3,1));
            if(card instanceof ConstructionCard){
                additionalInfoPanel.add(new JLabel("Construction"));
                String cardsThatCanOccupy = "";
                if(((ConstructionCard) card).isOccupied()){
                    cardsThatCanOccupy = "Occupied";
                }
                else{
                    for(EverdellParameters.CardDetails c : ((ConstructionCard) card).getCardsThatCanOccupy()){
                        cardsThatCanOccupy += c.name() + ", ";
                    }
                }
                JLabel canOccupyLabel = new JLabel(cardsThatCanOccupy);
                additionalInfoPanel.add(canOccupyLabel);
            }
            else{
                additionalInfoPanel.add(new JLabel("Critter"));
            }
            JPanel cardNamePanel = new JPanel();
            cardNamePanel.setLayout(new BorderLayout());
            cardNamePanel.setBackground(params.cardColour.get(card.getCardType()));

            JLabel cardLabel = new JLabel(card.getName());
            cardLabel.setOpaque(true);
            cardLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cardLabel.setBackground(new Color(201, 153, 35));

            cardNamePanel.add(cardLabel, BorderLayout.NORTH);

            JLabel isUnique = new JLabel();
            isUnique.setOpaque(true);
            isUnique.setHorizontalAlignment(SwingConstants.CENTER);
            if(card.isUnique()){
                isUnique.setText("Unique");
                isUnique.setBackground(new Color(201, 153, 35));
            }
            else{
                isUnique.setText("Common");
                isUnique.setBackground(Color.white);
            }
            cardNamePanel.add(isUnique, BorderLayout.SOUTH);


            additionalInfoPanel.add(new JLabel("Points : " + card.getPoints()));
            cardPanel.add(additionalInfoPanel);
            cardPanel.add(cardNamePanel);
            cardPanel.add(resourcePanel);
            panelToDrawOn.add(cardPanel);
        }
        return panelToDrawOn;
    }


    private void placeACard(EverdellGameState gameState, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel, JPanel meadowCardsPanel, EverdellCard card){
        checkForAdditionalStepsForCard(gameState,playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel,new ArrayList<>(Collections.singletonList(card)));

        //Place the card
        new PlayCard(card).execute(gameState);

        redrawPanels(gameState,playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel);

        if(card.isCardPayedFor()){
            checkForAdditionalStepsForCard(gameState,playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel,new ArrayList<>(Collections.singletonList(card)));
        }
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
                redrawPanels(gameState,playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel);
                createPlayerResourceInfoPanel(gameState,playerInfoPanel);
                playerCardPanel.removeAll();
                createWorkerPlacementPanel(gameState,playerCardPanel,playerInfoPanel);
        });
        workerActionPanel.add(placeWorkerButton);

        JButton playCardButton = new JButton("Play Card");
        playCardButton.addActionListener(k -> {
            //Make player cards available for selection via buttons
            turnPlayerCardsIntoButtons(gameState,playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel, gameState.playerHands.get(gameState.playerTurn).getSize(), card -> {

                //Can the card occupy a Construction Card
                for(EverdellCard c : gameState.playerVillage.get(gameState.playerTurn)) {
                    if (c instanceof ConstructionCard) {
                        System.out.println("Construction Card");
                        System.out.println(((ConstructionCard) c).getCardsThatCanOccupy());
                        System.out.println(card.getCardEnumValue());
                        if(((ConstructionCard) c).canCardOccupyThis(gameState)){
                            createPaymentChoicePanel(gameState, playerCardPanel, playerInfoPanel, villagePanel, playerCardPanel, meadowCardsPanel, ((ConstructionCard) c).getCardsThatCanOccupy().size(),(ConstructionCard) c, card);
                            return;
                        }
                    }
                }
                //If not we make them pay with resources
                //Place the card
                placeACard(gameState, playerCardPanel, playerInfoPanel, villagePanel, meadowCardsPanel,card);
            });

            //Make meadow cards available for selection via buttons
            turnMeadowCardsIntoButtons(gameState, playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel, 1, card ->{

                //Can the card occupy a Construction Card
                for(EverdellCard c : gameState.playerVillage.get(gameState.playerTurn)) {
                    if (c instanceof ConstructionCard) {
                        System.out.println("Construction Card");
                        System.out.println(((ConstructionCard) c).getCardsThatCanOccupy());
                        System.out.println(card.getCardEnumValue());
                        if(((ConstructionCard) c).canCardOccupyThis(gameState)){
                            createPaymentChoicePanel(gameState, playerCardPanel, playerInfoPanel, villagePanel, playerCardPanel, meadowCardsPanel, ((ConstructionCard) c).getCardsThatCanOccupy().size(),(ConstructionCard) c, card);
                            return;
                        }
                    }
                }

                //Place the card
                placeACard(gameState, playerCardPanel, playerInfoPanel, villagePanel, meadowCardsPanel, card);
            });
        });
        cardActionPanel.add(playCardButton);

        JButton moveSeasonButton = new JButton("Move Season");
        moveSeasonButton.addActionListener(k -> {
            redrawPanels(gameState,playerCardPanel,playerInfoPanel,villagePanel,meadowCardsPanel);

            new MoveSeason().execute(gameState);

            EverdellParameters.Seasons currentSeason = gameState.currentSeason[gameState.getCurrentPlayer()];

            //If it is summer, the player must draw 2 cards from the meadow
            if(currentSeason == EverdellParameters.Seasons.SUMMER){
                summerEventGUI(gameState, playerCardPanel, playerInfoPanel, villagePanel, meadowCardsPanel);
            }
            //If it is Spring or Autumn, we must trigger the green production event and see if any additional actions
            // need to be taken
            if(currentSeason == EverdellParameters.Seasons.AUTUMN || currentSeason == EverdellParameters.Seasons.SPRING){
                greenProductionEventGUI(gameState, playerCardPanel, playerInfoPanel, villagePanel, meadowCardsPanel);
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

    //This is in charge of providing CARD specific GUI so that the player can make decisions
    private Boolean checkForAdditionalStepsForCard(EverdellGameState state, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel, JPanel meadowPanel, ArrayList<EverdellCard> cardsToActivate){

        JButton doneButton;
        int numOfResource;

        System.out.println("Checking for additional steps for card");

        if(cardsToActivate.isEmpty()){
            redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
            return false;
        }

        EverdellCard c = cardsToActivate.get(0);
        EverdellParameters.CardDetails cardClass = c.getCardEnumValue();

        switch (cardClass) {
            case BARD:
                //The player can discard up to 5 cards
                turnPlayerCardsIntoButtons(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel,  5, card -> {
                    state.cardSelection.add(card);
                });
                doneButton = new JButton("Discard Selected Cards");
                doneButton.addActionListener(k2 -> {
                    c.applyCardEffect(state);
                    createPlayerResourceInfoPanel(state,playerInfoPanel);
                    playerCardPanel.removeAll();
                    createPlayerCardPanel(state,playerCardPanel);
                    redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
                });
                playerCardPanel.add(doneButton, BorderLayout.SOUTH);
                break;

            case RUINS:
                //Get every card that is a construction in the village so that we can send it for selection
                ArrayList<EverdellCard> constructionCards = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(card -> card instanceof ConstructionCard).filter(card -> card.getCardEnumValue() != EverdellParameters.CardDetails.RUINS).collect(Collectors.toCollection(ArrayList::new));

                turnVillageCardsIntoButtons(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel, constructionCards, card -> {
                    state.cardSelection.add(card);
                }, 1);
                doneButton = new JButton("Discard Selected Card, Refund Resources and Draw 2 Cards");
                doneButton.addActionListener(k2 -> {
                    c.applyCardEffect(state);
                    redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
                });

                playerCardPanel.add(doneButton, BorderLayout.SOUTH);

                break;

            case WOOD_CARVER:
                numOfResource = 3;
                resourceSelectionPanel(state,playerCardPanel,playerInfoPanel,numOfResource, "Trade Up to 3 Twigs for 1 Point Each",new ArrayList<ResourceTypes>(){{
                    add(ResourceTypes.TWIG);
                }}, game -> {
                    c.applyCardEffect(game);
                    redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
                    cardsToActivate.remove(c);
                    checkForAdditionalStepsForCard(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel, cardsToActivate);
                    return true;
                });

                break;

            case DOCTOR:
                numOfResource = 3;
                resourceSelectionPanel(state,playerCardPanel,playerInfoPanel,numOfResource, "Trade Up to 3 Berries for 1 Point Each",new ArrayList<ResourceTypes>(){{
                    add(ResourceTypes.BERRY);
                }}, game -> {
                    c.applyCardEffect(state);
                    redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
                    cardsToActivate.remove(c);
                    checkForAdditionalStepsForCard(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel, cardsToActivate);
                    return true;
                });
                break;

            case PEDDLER:

                PeddlerCard pc = (PeddlerCard) c;
                numOfResource = 2;
                resourceSelectionPanel(state,playerCardPanel,playerInfoPanel,numOfResource, "Give Up to 2 of any resource, get 1 of any resource for each one given",new ArrayList<ResourceTypes>(){{
                    add(ResourceTypes.BERRY);
                    add(ResourceTypes.PEBBLE);
                    add(ResourceTypes.RESIN);
                    add(ResourceTypes.TWIG);
                }}, game -> {
                    //Select the resources to give up
                    pc.addResourcesToLose(game.resourceSelection);

                    //Reset it to 0
                    state.resourceSelection = new HashMap<ResourceTypes, Counter>();
                    state.resourceSelection.put(ResourceTypes.BERRY, new Counter());
                    state.resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
                    state.resourceSelection.put(ResourceTypes.RESIN, new Counter());
                    state.resourceSelection.put(ResourceTypes.TWIG, new Counter());
                    redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);

                    resourceSelectionPanel(state,playerCardPanel,playerInfoPanel,numOfResource, "Select "+numOfResource+" Resources to Gain",new ArrayList<ResourceTypes>(){{
                        add(ResourceTypes.BERRY);
                        add(ResourceTypes.PEBBLE);
                        add(ResourceTypes.RESIN);
                        add(ResourceTypes.TWIG);
                    }}, game2 -> {
                        //Select the resources to gain
                        pc.addResourcesToGain(game2.resourceSelection);
                        pc.applyCardEffect(game2);
                        redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
                        cardsToActivate.remove(c);
                        checkForAdditionalStepsForCard(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel, cardsToActivate);
                        return true;
                    });
                    return true;
                });
                break;

            case CHIP_SWEEP:
                //The player must select a production card from their village, which its effect will be activated.
                //Get every card that is a green production card in the village so that we can send it for selection
                ArrayList<EverdellCard> greenProductionCards = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(card -> card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION ).filter(card -> card.getCardEnumValue() != EverdellParameters.CardDetails.CHIP_SWEEP).collect(Collectors.toCollection(ArrayList::new));

                turnVillageCardsIntoButtons(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel, greenProductionCards, card -> {
                    state.cardSelection.add(card);
                }, 1);
                doneButton = new JButton("Done");
                doneButton.addActionListener(k2 -> {
                    if(checkForAdditionalStepsForCard(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel,state.cardSelection)){
                        redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
                        //If the card has additional steps
                        cardsToActivate.remove(c);
                        checkForAdditionalStepsForCard(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel, cardsToActivate);
                        return;
                    }
                    else{
                        c.applyCardEffect(state);
                        redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
                        cardsToActivate.remove(c);
                        checkForAdditionalStepsForCard(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel, cardsToActivate);
                    }
                });

                playerCardPanel.add(doneButton, BorderLayout.SOUTH);

                break;

        }
        return false;
    }


    private void redrawPanels(EverdellGameState state, JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel, JPanel meadowPanel){
        playerCardPanel.removeAll();
        playerInfoPanel.removeAll();
        villagePanel.removeAll();
        meadowPanel.removeAll();


        createPlayerResourceInfoPanel(state,playerInfoPanel);
        createVillageCardPanel(state,villagePanel, 0);
        createPlayerCardPanel(state,playerCardPanel);
        createMeadowCardsPanel(state,meadowPanel);
    }


    private void greenProductionEventGUI(EverdellGameState state,JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel,JPanel meadowPanel){
        //Find all cards that are greenProduction and put it into a list
        System.out.println("Green Production Event GUI");
        ArrayList<EverdellCard> greenProductionCards = new ArrayList<>();
        for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
            if(card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION){
                greenProductionCards.add(card);
            }
        }

        checkForAdditionalStepsForCard(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel,greenProductionCards);
    }


    //THIS NEEDS TO BE UPDATED TO USE THE NEW SYSTEM
    //SUMMER GUI IS CURRENTLY NOT WORKING
    private void summerEventGUI(EverdellGameState state,JPanel playerCardPanel, JPanel playerInfoPanel, JPanel villagePanel,JPanel meadowPanel){
        state.cardSelection = new ArrayList<EverdellCard>();
        int cardsToDraw = Math.min(2, state.playerHands.get(state.getCurrentPlayer()).getCapacity() - state.playerHands.get(state.getCurrentPlayer()).getSize());

        //When it is summer, the player will be given the choice to grab 2 cards from the meadow into their hand(Depending on how many cards they have)

        if(cardsToDraw <=0){
            //Players hand is full, we do nothing
            return;
        }

        JLabel summerLabel = new JLabel("It is Summer, You may pick up to "+cardsToDraw+" cards from the meadow");
        summerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playerCardPanel.add(summerLabel, BorderLayout.NORTH);
        turnMeadowCardsIntoButtons(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel,cardsToDraw, card -> {
                state.cardSelection.add(card);
            });

        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new GridLayout(1,2));

        JButton passButton = new JButton("Pass");
        passButton.addActionListener(k -> {
            redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
        });

        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(k -> {
            new MoveSeason().summerEvent(state);
            redrawPanels(state,playerCardPanel,playerInfoPanel,villagePanel,meadowPanel);
        });
        navigationPanel.add(passButton);
        navigationPanel.add(doneButton);

        playerCardPanel.add(navigationPanel, BorderLayout.SOUTH);
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
