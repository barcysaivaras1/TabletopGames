package games.everdell.gui;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.components.Deck;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.FunctionWrapper;
import games.everdell.actions.EndTurn;
import games.everdell.actions.MoveSeason;
import games.everdell.actions.PlaceWorker;
import games.everdell.actions.PlayCard;
import games.everdell.components.*;
import games.everdell.EverdellParameters.ResourceTypes;
import games.everdell.EverdellParameters.ForestLocations;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.EverdellParameters.BasicEvent;

import games.poker.actions.Call;
import gui.AbstractGUIManager;
import gui.GamePanel;

import core.components.Counter;
import gui.IScreenHighlight;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;
import org.apache.log4j.Layout;
import org.apache.spark.sql.sources.In;
import org.w3c.dom.css.RGBColor;
import players.human.ActionController;
import players.human.HumanGUIPlayer;
import scala.collection.immutable.Stream;
import shapeless.ops.function;
import utilities.Hash;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.xml.stream.Location;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>This class allows the visualisation of the game. The game components (accessible through {@link Game#getGameState()}
 * should be added into {@link JComponent} subclasses (e.g. {@link JLabel},
 * {@link JPanel}, {@link JScrollPane}; or custom subclasses such as those in {@link gui} package).
 * These JComponents should then be added to the <code>`parent`</code> object received in the class constructor.</p>
 *
 * <p>An appropriate layout should be set for the parent GamePanel as well, e.g. {@link BoxLayout} or
 * {@link BorderLayout} or {@link GridBagLayout}.</p>
 *
 * <p>Check the super class for methods that can be overwritten for a more custom look, or
 * {@link games.terraformingmars.gui.TMGUI} for an advanced game visualisation example.</p>
 *
 * <p>A simple implementation example can be found in {@link games.tictactoe.gui.TicTacToeGUIManager}.</p>
 */
public class EverdellGUIManager extends AbstractGUIManager {


    private TopPanel meadowCardsPanel;
    private MiddlePanel villagePanel;
    private BottomPanel playerCardPanel;
    private SidePanel playerInfoPanel;
    private JPanel playerActionsPanel;

    public ArrayList<EverdellCard> cardSelection;
    public HashMap<ResourceTypes, Integer> resourceSelection;

    ActionController aC;

    public EverdellGUIManager(GamePanel parent, Game game, ActionController ac, Set<Integer> human) {
        super(parent, game, ac, human);
        if (game == null) {
            return;
        }

        aC = ac;

        EverdellGameState state = (EverdellGameState) game.getGameState();
        FunctionWrapper.setupFunctionWrapper();

        cardSelection = new ArrayList<>();
        resourceSelection = new HashMap<>();
        resourceSelection.put(ResourceTypes.TWIG, 0);
        resourceSelection.put(ResourceTypes.PEBBLE, 0);
        resourceSelection.put(ResourceTypes.BERRY, 0);
        resourceSelection.put(ResourceTypes.RESIN, 0);

        // TODO: set up GUI components and add to `parent`

        //Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3,1));
        mainPanel.setBackground(Color.LIGHT_GRAY);

        //Meadow Cards Panel
        meadowCardsPanel = new TopPanel(this,state);
        meadowCardsPanel.drawMeadowPanel();
        mainPanel.add(meadowCardsPanel);

        //Player Village Cards Panel
        villagePanel = new MiddlePanel(this,state);
        villagePanel.drawVillagePanel();
        mainPanel.add(villagePanel);

        //Player Cards Panel
        playerCardPanel = new BottomPanel(this, state);
        playerCardPanel.drawPlayerCards();
        mainPanel.add(playerCardPanel);



        //Info Panel
        JPanel infoPanel = createGameStateInfoPanel("Everdell", state, 400, defaultInfoPanelHeight);

        //Player Resource Count Panel
        playerInfoPanel = new SidePanel(this,state);
        playerInfoPanel.drawPlayerInfoPanel();

        //Add all panels to parent
        parent.setLayout(new BorderLayout());

        //Player Possible Actions Panel
        playerActionsPanel = new JPanel();
        drawPlayerActionsPanel(state);

        parent.add(infoPanel, BorderLayout.NORTH);
        parent.add(playerInfoPanel, BorderLayout.WEST);
        parent.add(mainPanel, BorderLayout.CENTER);
        parent.add(playerActionsPanel, BorderLayout.SOUTH);
        parent.setPreferredSize(new Dimension(400,400));
        parent.revalidate();
        parent.setVisible(true);
        parent.repaint();
    }


    public void drawResourceButtons(EverdellGameState state ,JPanel panelToDrawOn, ResourceTypes resourceType, Color resourceColour, Consumer<EverdellGameState> redrawButton, int amountOfResources){

        JButton resourceButtonPlus = new JButton(resourceType+" +");
        resourceButtonPlus.addActionListener(k -> {
            if(resourceSelection.get(resourceType) == amountOfResources){
                return;
            }
            resourceSelection.put(resourceType, resourceSelection.get(resourceType) + 1);
            redrawButton.accept(state);
        });
        JButton resourceButtonMinus = new JButton(resourceType+" -");
        resourceButtonMinus.addActionListener(k -> {
            if(resourceSelection.get(resourceType) == 0){
                return;
            }
            resourceSelection.put(resourceType, resourceSelection.get(resourceType) - 1);
            redrawButton.accept(state);
        });

        JLabel resourceLabel = new JLabel("" + resourceSelection.get(resourceType));

        panelToDrawOn.setBackground(resourceColour);
        panelToDrawOn.add(resourceButtonMinus);
        panelToDrawOn.add(resourceLabel);
        panelToDrawOn.add(resourceButtonPlus);

    }

    private void createPaymentChoicePanel(EverdellGameState state, EverdellCard cardToPlace){
        redrawPanels();
        playerCardPanel.removeAll();
        playerCardPanel.setLayout(new BorderLayout());
        playerCardPanel.setBackground(new Color(147, 136, 40));
        playerCardPanel.add(new JLabel("Worker Placement"), BorderLayout.NORTH);
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            playerCardPanel.removeAll();
            this.playerCardPanel.drawPlayerCards();
        });
        playerCardPanel.add(back,BorderLayout.SOUTH);

        JPanel paymentPanel = new JPanel();

        //======================================
        //Payment Method Checks
        //======================================

        //Will always have the option to pay with resources
        JButton resourcesButton = new JButton("Pay With Resources");
        resourcesButton.addActionListener(k -> {
            //Place the card
            placeACard(state, cardToPlace);
        });
        paymentPanel.add(resourcesButton);


        //Check if they can pay via occupation
        ArrayList<EverdellCard> cardsThatCanOccupy = playCardActionWithComponentToIDConversion(state, cardToPlace, cardSelection, resourceSelection).canPayWithOccupation(state, cardToPlace);
        if(!cardsThatCanOccupy.isEmpty()){
            JButton occupationButton = new JButton("Pay With Occupation");
            occupationButton.addActionListener(k -> {
                //If there is 1 card it can occupy, there is no ambiguity
                if(cardsThatCanOccupy.size() == 1) {
                    ConstructionCard cc = (ConstructionCard) cardsThatCanOccupy.get(0);
                    cc.occupyConstruction((CritterCard) cardToPlace);

                    //Place the card
                    placeACard(state, cardToPlace);
                }
                //If there is more than 1 card it can occupy, then there is ambiguity on which card they would like to occupy
                else{
                    villagePanel.drawVillagePanelButtons(cardsThatCanOccupy, 1, card -> {
                        ConstructionCard cc = (ConstructionCard) card;
                        cc.occupyConstruction((CritterCard) cardToPlace);

                        //Place the card
                        placeACard(state, cardToPlace);
                    });
                }
            });
            paymentPanel.add(occupationButton);
        }

        //Check if there are special cards that can apply a discount {Crane, Innkeeper}

        for(var sCard : state.playerVillage.get(state.getCurrentPlayer())){
            if(sCard.getCardEnumValue() == EverdellParameters.CardDetails.CRANE & cardToPlace instanceof ConstructionCard){
                //Give player the choice to pay via Crane
                JButton craneButton = new JButton("Discount via Crane");

                craneButton.addActionListener(k -> {
                    //They must select resources they want to discount

                    ArrayList<ResourceTypes> r = new ArrayList<>();
                    r.add(ResourceTypes.RESIN);
                    r.add(ResourceTypes.TWIG);
                    r.add(ResourceTypes.PEBBLE);

                    playerCardPanel.drawResourceSelection(3, "Select 3 Resources to discount", r, (s) -> {
                        //Crane Card is sCard in this instance
                        //Trigger Crane Card Effect
                        playCardActionWithComponentToIDConversion(state, cardToPlace, cardSelection, resourceSelection).triggerCardEffect(state, sCard);

                        //Card needs to be removed from the village
                        state.playerVillage.get(state.getCurrentPlayer()).remove(sCard);

                        //Place the now paid for card
                        placeACard(state,cardToPlace);
                        return true;
                    });
                });

                paymentPanel.add(craneButton);
            }
            else if (sCard.getCardEnumValue() == EverdellParameters.CardDetails.INNKEEPER & cardToPlace instanceof CritterCard){
                //Give player the choice to pay via Innkeeper
                JButton innkeeperButton = new JButton("Discount via Innkeeper");


                innkeeperButton.addActionListener(k -> {
                    //They must select resources they want to discount

                    ArrayList<ResourceTypes> r = new ArrayList<>();
                    r.add(ResourceTypes.BERRY);

                    playerCardPanel.drawResourceSelection(3, "Select 3 Resources to discount", r, (s) -> {
                        //Innkeeper Card is sCard in this instance
                        //Trigger Innkeeper Card Effect
                        playCardActionWithComponentToIDConversion(state, cardToPlace, cardSelection, resourceSelection).triggerCardEffect(s, sCard);

                        //Card needs to be removed from the village
                        state.playerVillage.get(state.getCurrentPlayer()).remove(sCard);

                        //Place the now paid for card
                        placeACard(state, cardToPlace);
                        return true;
                    });
                });
                paymentPanel.add(innkeeperButton);
            }
            //Needs to have dungeon card placed, There needs to be a critter in the village, there must be 1 or more cell available
            else if(sCard.getCardEnumValue() == EverdellParameters.CardDetails.DUNGEON & state.playerVillage.get(state.getCurrentPlayer()).stream().anyMatch(c -> c instanceof CritterCard)) {
                DungeonCard dc = (DungeonCard) sCard;

                if (dc.isThereACellFree()){
                        //Give player the choice to pay via Innkeeper
                        JButton dungeonButton = new JButton("Discount via Dungeom");


                        dungeonButton.addActionListener(k -> {
                        //They must select resources they want to discount

                        ArrayList<ResourceTypes> r = new ArrayList<>();
                        r.add(ResourceTypes.BERRY);

                        ArrayList cardsToChooseFrom = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(c -> c instanceof CritterCard).collect(Collectors.toCollection(ArrayList::new));
                        villagePanel.drawVillagePanelButtons(cardsToChooseFrom, 1, card -> {
                            dc.placeCritterInCell((CritterCard) card);
                            //This happens after a critter card is placed in a cell
                            playerCardPanel.drawResourceSelection(3, "Select 3 Resources to discount", r, (s) -> {
                                //Innkeeper Card is sCard in this instance
                                //Trigger Innkeeper Card Effect
                                playCardActionWithComponentToIDConversion(state, cardToPlace, cardSelection, resourceSelection).triggerCardEffect(s, sCard);

                                //Place the now paid for card
                                placeACard(state, cardToPlace);
                                return true;
                            });
                        });
                    });
                    paymentPanel.add(dungeonButton);
                }
            }
        }
        playerCardPanel.add(paymentPanel,BorderLayout.CENTER);
    }

    public JPanel drawCardButtons(EverdellGameState state, ArrayList<EverdellCard> cards, JPanel panelToDrawOn, Consumer<EverdellCard> buttonAction, int numberOfSelections){
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
                    cardSelection.remove(card);
                }
                //Limit the number of selections
                else if(numberOfSelections == ForestLocations.cardChoices.size() || numberOfSelections == cardSelection.size()){
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
                    if(card.getCardEnumValue() == EverdellParameters.CardDetails.EVER_TREE){
                        cardsThatCanOccupy = "All Critters";
                    }
                    else {
                        for (EverdellParameters.CardDetails c : ((ConstructionCard) card).getCardsThatCanOccupy()) {
                            cardsThatCanOccupy += c.name() + ", ";
                        }
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

    public JPanel drawCards(EverdellGameState state, Deck<EverdellCard> cards,JPanel panelToDrawOn){
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
                    if(card.getCardEnumValue() == EverdellParameters.CardDetails.EVER_TREE){
                        cardsThatCanOccupy = "All Critters";
                    }
                    else {
                        for (EverdellParameters.CardDetails c : ((ConstructionCard) card).getCardsThatCanOccupy()) {
                            cardsThatCanOccupy += c.name() + ", ";
                        }
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

    public void placeACard(EverdellGameState state, EverdellCard card){

        System.out.println("Placing a card");
        //We check if the player can afford the card
        if(playCardActionWithComponentToIDConversion(state, card, cardSelection, resourceSelection).checkIfPlayerCanBuyCard(state, state.getCurrentPlayer())){
            FunctionWrapper.addAFunction(() -> checkForAdditionalStepsForCard(state, new ArrayList<>(List.of(card)), false),"Checking For Additional Steps...");
            FunctionWrapper.addAFunction(() -> {
                        playCardActionWithComponentToIDConversion(state, card, cardSelection, resourceSelection).execute(state);
                        redrawPanels();
                        return false;
                    }
            ,"Playing the card...");
            FunctionWrapper.addAFunction(() -> {
                        afterPlayingCard(state,card);
                        return true;
                    }
                    ,"Checking for After Card steps...");
            FunctionWrapper.activateNextFunction();
        }
        else{
            redrawPanels();
        }
    }

    //Displays the possible actions that the player can take
    private void drawPlayerActionsPanel(EverdellGameState state){
        playerActionsPanel.removeAll();
        playerActionsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel workerActionPanel = new JPanel();
        workerActionPanel.setBackground(Color.YELLOW);
        JPanel cardActionPanel = new JPanel();
        cardActionPanel.setBackground(Color.BLUE);
        JPanel seasonActionPanel = new JPanel();
        seasonActionPanel.setBackground(Color.GREEN);
        JPanel endTurnPanel = new JPanel();
        endTurnPanel.setBackground(new Color(165, 163, 163, 255));

        JButton placeWorkerButton = new JButton("Place Worker");
        placeWorkerButton.addActionListener(k -> {
            redrawPanels();
            this.playerCardPanel.drawWorkerPlacement();
        });
        workerActionPanel.add(placeWorkerButton);

        JButton playCardButton = new JButton("Play Card");
        playCardButton.addActionListener(k -> {
            //Make player cards available for selection via buttons
            this.playerCardPanel.drawPlayerCardsButtons(1, "Select a card to play", card -> {
                createPaymentChoicePanel(state, card);
            });

            //Make meadow cards available for selection via buttons
            this.meadowCardsPanel.drawMeadowPanelButtons( 1, card ->{
                createPaymentChoicePanel(state, card);
            });
        });

        cardActionPanel.add(playCardButton);

        JButton moveSeasonButton = new JButton("Move Season");
        moveSeasonButton.addActionListener(k -> {
            redrawPanels();

            FunctionWrapper.addAFunction(() -> cardsActivatedBySeasonChange(state),"Checking if a card was activated by season change...");
            FunctionWrapper.addAFunction(() ->
                    {
                        changeSeason(state);
                        return true;
                    }
                    ,"Changing season...");


            FunctionWrapper.activateNextFunction();
        });
        seasonActionPanel.add(moveSeasonButton);


        JButton endTurnButton = new JButton("End Turn");
        endTurnButton.addActionListener(k -> {
            redrawPanels();
            System.out.println("Ending Turn : "+state.getCurrentPlayer());
            ac.addAction(new EndTurn());
        });
        endTurnPanel.add(endTurnButton);


        // Place workerActionPanel on the far left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        playerActionsPanel.add(workerActionPanel, gbc);

        // Place cardActionPanel in the center
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        playerActionsPanel.add(cardActionPanel, gbc);

        // Place seasonActionPanel on the far right
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        playerActionsPanel.add(seasonActionPanel, gbc);

        // Place endTurnPanel on the far right
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        playerActionsPanel.add(endTurnPanel, gbc);

    }

    private void changeSeason(EverdellGameState state){
        System.out.println("Changing Season");
        ArrayList<Integer> csID = new ArrayList<>();
        for(var c : cardSelection){
            csID.add(c.getComponentID());
        }
        new MoveSeason(csID).execute(state);

        EverdellParameters.Seasons currentSeason = state.currentSeason[state.getCurrentPlayer()];

        //If it is summer, the player must draw 2 cards from the meadow
        if(currentSeason == EverdellParameters.Seasons.SUMMER){
            FunctionWrapper.addAFunction(() -> {
                summerEventGUI(state);
                return true;
            },"Displaying Summer GUI...");
        }
        //If it is Spring or Autumn, we must trigger the green production event and see if any additional actions
        // need to be taken
        if(currentSeason == EverdellParameters.Seasons.AUTUMN || currentSeason == EverdellParameters.Seasons.SPRING){
            FunctionWrapper.addAFunction(() -> {
                greenProductionEventGUI(state);
                return true;
            }, "Displaying Green Production GUI");
        }
        FunctionWrapper.activateNextFunction();
    }

    private boolean cardsActivatedBySeasonChange(EverdellGameState state){
        //Iterate Over the village and seek out the cards that are activate by the season change (Not for Green Production)

        for(var card : state.playerVillage.get(state.getCurrentPlayer())){


            switch (card.getCardEnumValue()){
                case CLOCK_TOWER:
                    ClockTowerCard ct = (ClockTowerCard) card;

                    //Iterate Over each Location and create a button for each one
                    playerCardPanel.removeAll();
                    playerCardPanel.activateCopyMode(copyLocationID -> {
                        EverdellLocation copyLocation = (EverdellLocation) state.getComponentById(copyLocationID);
                        ct.selectLocation(copyLocationID);
                        playCardActionWithComponentToIDConversion(state, card, cardSelection, resourceSelection).triggerCardEffect(state, ct);
                        playerCardPanel.deactivateCopyMode();
                        redrawPanels();
                        System.out.println("Should be called before changing season");
                        System.out.println("Player Village : "+state.playerVillage.get(state.getCurrentPlayer()));

                        //This is the only location that places a card
                        //Therefore we need to place the card first and then move to the next function
                        if(copyLocation.getAbstractLocation() != ForestLocations.DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT){
                            FunctionWrapper.activateNextFunction();
                        }
                    });

                    playerCardPanel.drawWorkerPlacement();
                    return true;
            }

        }
        return false;

    }

    //This is in charge of providing CARD specific GUI so that the player can make decisions
    private Boolean checkForAdditionalStepsForCard(EverdellGameState state, ArrayList<EverdellCard> cardsToCheck, Boolean isGreenProductionEvent){

        HashMap<Integer, Callable<Boolean>> cardsToActivate = new HashMap<>();


        Consumer<EverdellGameState> listCardsAction = (state2) -> {
            if (cardsToActivate.isEmpty()){
                redrawPanels();
                System.out.println("No cards to activate");
                FunctionWrapper.activateNextFunction();
                return;
            }
            if(cardsToCheck.size() == 1){

                FunctionWrapper.activateNextFunction(cardsToActivate.get(cardsToCheck.get(0).getComponentID()), FunctionWrapper.getDescriptor(cardsToActivate.get(cardsToCheck.get(0).getComponentID())));

                return;
            }
            ArrayList<EverdellCard> cardsToSend = new ArrayList<>();
            for(Integer i : cardsToActivate.keySet()){
                cardsToSend.add((EverdellCard) state2.getComponentById(i));
            }
            playerCardPanel.drawPlayerCardsButtons(1, cardsToSend, "Cards Have been Triggered by Green Production, Select which one you want to trigger first", card -> {
                try {
                    FunctionWrapper.activateNextFunction(cardsToActivate.get(card),FunctionWrapper.getDescriptor(cardsToActivate.get(cardsToCheck.get(0))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        };

        ArrayList<EverdellCard> selectedCards = new ArrayList<>();

        for(var cardToCheck : cardsToCheck) {
            EverdellParameters.CardDetails cardClass = cardToCheck.getCardEnumValue();
            Integer c = cardToCheck.getComponentID();


            System.out.println("Checking for additional steps for card");

            ArrayList<ResourceTypes> resourceSelect =  new ArrayList<ResourceTypes>(){{
                add(ResourceTypes.BERRY);
                add(ResourceTypes.PEBBLE);
                add(ResourceTypes.RESIN);
                add(ResourceTypes.TWIG);}};

            resourceSelection = new HashMap<ResourceTypes, Integer>();
            resourceSelection.put(ResourceTypes.TWIG, 0);
            resourceSelection.put(ResourceTypes.PEBBLE, 0);
            resourceSelection.put(ResourceTypes.BERRY, 0);
            resourceSelection.put(ResourceTypes.RESIN, 0);


            switch (cardClass) {
                case BARD:
                    Callable bardAction = () -> {
                        redrawPanels();
                        //The player can discard up to 5 cards
                        EverdellCard cardToActOn = (EverdellCard) state.getComponentById(c);
                        ArrayList<EverdellCard> cardsToDisplayBard = state.playerHands.get(state.getCurrentPlayer()).getComponents().stream().filter(card -> card != cardToActOn).collect(Collectors.toCollection(ArrayList::new));


                        this.playerCardPanel.drawPlayerCardsButtons(5, cardsToDisplayBard, "Select up to 5 cards to discard, Gain 1 point per card", selectedCards::add);

                        JButton doneButtonBard = new JButton("Discard Selected Cards");
                        doneButtonBard.addActionListener(k2 -> {
                            cardSelection = selectedCards;
                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);
                        });
                        playerCardPanel.add(doneButtonBard, BorderLayout.SOUTH);
                        return true;
                    };
                    FunctionWrapper.setDescriptor(bardAction, "Bard Action...");
                    cardsToActivate.put(c, bardAction);
                    break;

                case PEDDLER:
                    Callable peddlerAction = () -> {
                        redrawPanels();

                        PeddlerCard cardToActOn = (PeddlerCard) state.getComponentById(c);
                        int numOfResourcePeddler = 2;
                        this.playerCardPanel.drawResourceSelection(numOfResourcePeddler, "Give Up to 2 of any resource, get 1 of any resource for each one given", resourceSelect, game -> {
                            //Select the resources to give up
                            cardToActOn.addResourcesToLose(resourceSelection);
                            //Reset it to 0
                            resetValues();
                            redrawPanels();

                            this.playerCardPanel.drawResourceSelection(numOfResourcePeddler, "Select " + numOfResourcePeddler + " Resources to Gain", resourceSelect, game2 -> {
                                //Select the resources to gain
                                cardToActOn.addResourcesToGain(resourceSelection);
                                if (isGreenProductionEvent) {
                                    playCardActionWithComponentToIDConversion(game, cardToActOn, cardSelection, resourceSelection).triggerCardEffect(state, cardToActOn);
                                }
                                cardsToActivate.remove(c);
                                listCardsAction.accept(game2);
                                return true;
                            });
                            return true;
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(peddlerAction, "Peddler Action...");
                    cardsToActivate.put(c, peddlerAction);
                    break;

                case RUINS:
                    Callable ruinsAction = () -> {
                        redrawPanels();
                        //Get every card that is a construction in the village so that we can send it for selection
                        ArrayList<EverdellCard> constructionCards = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(card -> card instanceof ConstructionCard).filter(card -> card.getCardEnumValue() != EverdellParameters.CardDetails.RUINS).collect(Collectors.toCollection(ArrayList::new));

                        this.villagePanel.drawVillagePanelButtons(constructionCards, 1, card -> {
                            cardSelection.add(card);
                        });

                        JButton doneButton2 = new JButton("Discard Selected Card, Refund Resources and Draw 2 Cards");
                        doneButton2.addActionListener(k2 -> {
                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);
                        });
                        playerCardPanel.add(doneButton2, BorderLayout.SOUTH);
                        return true;
                    };
                    FunctionWrapper.setDescriptor(ruinsAction, "Ruins Action...");
                    cardsToActivate.put(c, ruinsAction);
                    break;

                case HUSBAND:
                    Callable husbandAction = () -> {
                        redrawPanels();
                        //If there is a wife on the board and a farm, the Husband card can be activated

                        HusbandCard hc = (HusbandCard) state.getComponentById(c);

                        //Check for wife
                        if(hc.getWife() == null) {
                            for (var card : state.playerVillage.get(state.getCurrentPlayer())) {
                                if (card.getCardEnumValue() == EverdellParameters.CardDetails.WIFE) {
                                    if (((WifeCard) card).getHusband() == null) {
                                        System.out.println("Setting wife");
                                        ((WifeCard) card).setHusband(hc);
                                        hc.setWife((WifeCard) card);
                                    }
                                }
                                if(hc.getWife() != null){
                                    break;
                                }
                            }
                        }

                        //Check for farm
                        if(hc.isThereAFarm(state) && hc.getWife() != null){
                            //Allow worker to select 1 resource to gain
                            playerCardPanel.drawResourceSelection(1, "Select 1 Resource to Gain", new ArrayList<>(List.of(ResourceTypes.values())), state1 -> {
                                if(isGreenProductionEvent){
                                    playCardActionWithComponentToIDConversion(state, hc, cardSelection, resourceSelection).triggerCardEffect(state, hc);
                                }
                                cardsToActivate.remove(c);
                                listCardsAction.accept(state);
                                return true;
                            });
                        }
                        else{
                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);
                        }

                        return true;
                    };
                    FunctionWrapper.setDescriptor(husbandAction, "Husband Action...");
                    cardsToActivate.put(c, husbandAction);
                    break;

                case WOOD_CARVER:
                    Callable woodCarverAction = () -> {
                        redrawPanels();
                        int numOfResourceWoodCarver = 3;

                        this.playerCardPanel.drawResourceSelection(numOfResourceWoodCarver, "Trade Up to 3 Twigs for 1 Point Each", new ArrayList<ResourceTypes>() {{
                            add(ResourceTypes.TWIG);
                        }}, game -> {
                            if(isGreenProductionEvent){

                                playCardActionWithComponentToIDConversion(state, (EverdellCard) state.getComponentById(c), cardSelection, resourceSelection).triggerCardEffect(state, (EverdellCard) state.getComponentById(c));
                            }

                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);
                            return true;
                        });

                        return true;
                    };
                    FunctionWrapper.setDescriptor(woodCarverAction, "Wood Carver Action...");
                    cardsToActivate.put(c, woodCarverAction);
                    break;

                case DOCTOR:

                    Callable doctorAction = () -> {
                        redrawPanels();
                        int numOfResourceDoctor = 3;
                        this.playerCardPanel.drawResourceSelection(numOfResourceDoctor, "Trade Up to 3 Berries for 1 Point Each", new ArrayList<ResourceTypes>() {{
                            add(ResourceTypes.BERRY);
                        }}, game -> {
                            if(isGreenProductionEvent){
                                playCardActionWithComponentToIDConversion(state, (EverdellCard) state.getComponentById(c), cardSelection, resourceSelection).triggerCardEffect(state, (EverdellCard) state.getComponentById(c));
                            }
                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);
                            return true;
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(doctorAction, "Doctor Action...");
                    cardsToActivate.put(c, doctorAction);
                    break;


                case CHIP_SWEEP:

                    Callable chipSweepAction = () -> {
                        redrawPanels();
                        CopyCard cc = (CopyCard) state.getComponentById(c);
                        //The player must select a production card from their village, which its effect will be activated.
                        //Get every card that is a green production card in the village so that we can send it for selection
                        ArrayList<EverdellCard> greenProductionCards = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(card -> card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).filter(card -> card.getCardEnumValue() != EverdellParameters.CardDetails.CHIP_SWEEP).collect(Collectors.toCollection(ArrayList::new));

                        this.villagePanel.drawVillagePanelButtons(greenProductionCards, 1, card -> {
                            cc.setCardToCopy(card);
                        });

                        JButton doneButtonChipSweep = new JButton("Done");
                        doneButtonChipSweep.addActionListener(k2 -> {
                            //If No cards were selected
                            if(cc.getCardToCopy() == null){
                                cardsToActivate.remove(c);
                                listCardsAction.accept(state);
                                return;
                            }

                            //This is what has to be run after a card has been copied
                            Callable chipSweepAction2 = () -> {
                                cardsToActivate.remove(c);
                                listCardsAction.accept(state);
                                return true;
                            };

                            Callable chipSweepAction3 = () -> {
                                if(isGreenProductionEvent){
                                    if(!checkForAdditionalStepsForCard(state, new ArrayList<>(List.of(cc.getCardToCopy())), true)){
                                        playCardActionWithComponentToIDConversion(state, cc, cardSelection, resourceSelection).triggerCardEffect(state, cc);
                                        FunctionWrapper.activateNextFunction();
                                    }
                                    cc.setCardToCopy(null);
                                }
                                else{
                                    if(!checkForAdditionalStepsForCard(state, new ArrayList<>(List.of(cc.getCardToCopy())), true)){
                                        cardsToActivate.remove(c);
                                        listCardsAction.accept(state);
                                    }
                                    cc.setCardToCopy(null);
                                }
                                return true;
                            };

                            FunctionWrapper.addAFunction(chipSweepAction2, "Chip Sweep Action 2...", 0);
                            FunctionWrapper.activateNextFunction(chipSweepAction3, "Chip Sweep Action 3...");

                        });

                        playerCardPanel.add(doneButtonChipSweep, BorderLayout.SOUTH);

                        return true;
                    };
                    FunctionWrapper.setDescriptor(chipSweepAction, "Chip Sweep Action...");
                    cardsToActivate.put(c, chipSweepAction);
                    break;

                case MONK:
                    Callable monkAction = () -> {

                        redrawPanels();
                        System.out.println("Monk Card");
                        playerCardPanel.drawResourceSelection(2, "Give up to 2 berries to a player, for 2 points each", new ArrayList<ResourceTypes>() {{
                            add(ResourceTypes.BERRY);
                        }}, game -> {
                            playerCardPanel.drawPlayerSelection(player -> {
                                MonkCard mc = (MonkCard) game.getComponentById(c);
                                mc.setSelectedPlayer(player);

                                if(isGreenProductionEvent){
                                    playCardActionWithComponentToIDConversion(game, mc, cardSelection, resourceSelection).triggerCardEffect(game, mc);
                                }
                                cardsToActivate.remove(c);
                                listCardsAction.accept(game);

                            });
                            return true;
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(monkAction, "Monk Action...");
                    cardsToActivate.put(c, monkAction);
                    break;

                case FOOL:
                    Callable foolAction = () -> {
                        redrawPanels();
                        playerCardPanel.drawPlayerSelection(player -> {
                            FoolCard fc = (FoolCard) state.getComponentById(c);
                            fc.setSelectedPlayer(player);
                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(foolAction, "Fool Action...");
                    cardsToActivate.put(c, foolAction);
                    break;

                case TEACHER:
                    Callable teacherAction = () -> {

                        redrawPanels();
                        ArrayList<EverdellCard> cTD = new ArrayList<>();
                        cTD.add(state.cardDeck.draw());
                        cTD.add(state.cardDeck.draw());

                        playerCardPanel.drawPlayerCardsButtons(1, cTD, "Draw 2 cards, Select 1 Keep, Give away the other", card -> {
                            if (card == cTD.get(0)) {
                                cardSelection.add(cTD.get(0));
                                cardSelection.add(cTD.get(1));
                            } else {
                                cardSelection.add(cTD.get(1));
                                cardSelection.add(cTD.get(0));
                            }

                            playerCardPanel.drawPlayerSelection(player -> {
                                TeacherCard tc = (TeacherCard) state.getComponentById(c);
                                tc.setSelectedPlayer(player);

                                if(isGreenProductionEvent){
                                    playCardActionWithComponentToIDConversion(state, tc, cardSelection, resourceSelection).triggerCardEffect(state, tc);
                                }
                                cardsToActivate.remove(c);
                                listCardsAction.accept(state);
                            });
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(teacherAction, "Teacher Action...");
                    cardsToActivate.put(c, teacherAction);
                    break;

                case UNDERTAKER:
                    Callable undertakerAction = () -> {
                        //The player must move 3 cards to the discard pile
                        //The meadow must replenish
                        //The player must draw 1 card from the meadow (Assuming they have space)
                        redrawPanels();

                        ArrayList<EverdellCard> meadowCardsToDiscard = new ArrayList<>();
                        meadowCardsPanel.drawMeadowPanelButtons(3, card -> {
                            meadowCardsToDiscard.add(card);
                        });

                        JButton doneButtonUndertaker = new JButton("Done");
                        doneButtonUndertaker.addActionListener(k2 -> {

                            //Remove Cards from meadow
                            for (EverdellCard card : meadowCardsToDiscard) {
                                state.meadowDeck.remove(card);
                            }

                            //Replenish the meadow
                            while (state.meadowDeck.getSize() != 8) {
                                state.meadowDeck.add(state.cardDeck.draw());
                            }

                            redrawPanels();

                            playerCardPanel.drawPlayerCardsButtons(1, state.meadowDeck.stream().collect(Collectors.toCollection(ArrayList::new)), "Select 1 card to draw from the meadow", card -> {
                                cardSelection.clear();
                                cardSelection.add(card);

                                cardsToActivate.remove(c);
                                listCardsAction.accept(state);
                            });
                        });
                        playerCardPanel.add(doneButtonUndertaker, BorderLayout.SOUTH);
                        return true;
                    };
                    FunctionWrapper.setDescriptor(undertakerAction, "Undertaker Action...");
                    cardsToActivate.put(c, undertakerAction);
                    break;

                case POSTAL_PIGEON:
                    Callable postalPigeonAction = () -> {
                        //The player must draw 2 cards from the deck, and must select 1 to play up to a cost of 3 for free
                        redrawPanels();
                        ArrayList<EverdellCard> cardsToDraw = new ArrayList<>();
                        cardsToDraw.add(state.cardDeck.draw());
                        cardsToDraw.add(state.cardDeck.draw());

                        playerCardPanel.drawPlayerCardsButtons(1, cardsToDraw, "", card -> {
                            cardSelection.add(card);
                            cardSelection.add(cardSelection.get(0) == card ? cardsToDraw.get(1) : cardsToDraw.get(0));

                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);

                            //If the selected card was valid, we must now go through the process of the placing the card
                            if (card.isCardPayedFor()) {
                                placeACard(state, card);
                            }
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(postalPigeonAction, "Postal Pigeon Action...");
                    cardsToActivate.put(c, postalPigeonAction);
                    break;


                case SHEPHERD:
                    Callable shepherdAction = () -> {
                        //The player must select another player to give 3 berries to
                        redrawPanels();
                        playerCardPanel.drawPlayerSelection(player -> {
                            ShepherdCard sc = (ShepherdCard) state.getComponentById(c);
                            sc.setBeforePR(state.PlayerResources.get(ResourceTypes.BERRY)[state.getCurrentPlayer()].getValue());
                            sc.setSelectedPlayer(player);

                            if(isGreenProductionEvent){
                                playCardActionWithComponentToIDConversion(state, sc, cardSelection, resourceSelection).triggerCardEffect(state, sc);
                            }
                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(shepherdAction, "Shepherd Action...");
                    cardsToActivate.put(c, shepherdAction);
                    break;

                case STORE_HOUSE:
                    Callable storeHouseAction = () -> {
                        redrawPanels();
                        playerCardPanel.drawResourceSelection(3, "Choose to store 3 Twigs, 2 Resin, 2 Berries or 1 Pebble", resourceSelect, game -> {
                            StorehouseCard shc = (StorehouseCard) state.getComponentById(c);

                            if(isGreenProductionEvent){
                                playCardActionWithComponentToIDConversion(state, shc, cardSelection, resourceSelection).triggerCardEffect(state, shc);
                            }
                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);
                            return true;
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(storeHouseAction, "StoreHouse Action...");
                    cardsToActivate.put(c, storeHouseAction);
                    break;

                case RANGER:
                    Callable rangerAction = () -> {
                        //The player must select a worker to move from a location to another
                        redrawPanels();

                        playerCardPanel.removeAll();

                        ArrayList<EverdellLocation> locationsToDisplayRanger = new ArrayList<>();

                        playerCardPanel.setLayout(new GridLayout(2, 4));

                        RangerCard rc = (RangerCard) state.getComponentById(c);

                        EverdellLocation locationToMoveFrom = null;

                        //Find all location in which the player has a worker on
                        for(var location : state.everdellLocations){
                            if(location.isPlayerOnLocation(state) && location.getAbstractLocation() != EverdellParameters.RedDestinationLocation.CEMETERY_DESTINATION && location.getAbstractLocation() != EverdellParameters.RedDestinationLocation.MONASTERY_DESTINATION){
                                locationsToDisplayRanger.add(location);
                            }
                        }

                        Callable rangerAction2 = () -> {
                            cardsToActivate.remove(c);
                            listCardsAction.accept(state);
                            return true;
                        };

                        FunctionWrapper.addAFunction(rangerAction2, "Ranger Action 2...",0);

                        if(locationsToDisplayRanger.isEmpty()){
                            FunctionWrapper.activateNextFunction();
                        }


                        //Display the locations for the player to remove a worker from
                        for(var location : locationsToDisplayRanger){
                            JButton locationButton = new JButton(location.getAbstractLocation().name());
                            locationButton.addActionListener(k -> {
                                rc.setLocationFrom(location);
                                state.workers[state.getCurrentPlayer()].increment();

                                playerCardPanel.activateCopyMode(copyLocationID -> {
                                    System.out.println("Copy Mode Activated");
                                    EverdellLocation copyLocation = (EverdellLocation) state.getComponentById(copyLocationID);
                                    System.out.println("Copy Location : "+copyLocation);

                                    rc.setLocationTo(copyLocation);
                                    playerCardPanel.deactivateCopyMode();
                                    redrawPanels();

                                    //This is the only location that places a card
                                    //Therefore we need to place the card first and then move to the next function
                                    if(copyLocation.getAbstractLocation() != ForestLocations.DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT && copyLocation.getAbstractLocation() != EverdellParameters.RedDestinationLocation.QUEEN_DESTINATION){
                                        FunctionWrapper.activateNextFunction();
                                    }

                                });
                                playerCardPanel.drawWorkerPlacement();
                            });
                            playerCardPanel.add(locationButton);
                        }
                        return true;
                    };
                    FunctionWrapper.setDescriptor(rangerAction, "Ranger Action...");
                    cardsToActivate.put(c, rangerAction);
                    break;


                case MINER_MOLE:
                    Callable minerMoleAction = () -> {
                        redrawPanels();

                        CopyCard cc = (CopyCard) state.getComponentById(c);
                        //The player must select a production card from their village, which its effect will be activated.
                        //Get every card that is a green production card in the village so that we can send it for selection
                        ArrayList<EverdellCard> greenProductionCards = new ArrayList<>();
                        for(int i=0; i < state.getNPlayers(); i++) {
                            if(i == state.getCurrentPlayer()){
                                continue;
                            }
                            greenProductionCards.addAll(state.playerVillage.get(i).stream().filter(card -> card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION).filter(card -> card.getCardEnumValue() != EverdellParameters.CardDetails.MINER_MOLE).collect(Collectors.toCollection(ArrayList::new)));
                        }

                        System.out.println("Green Production Cards : "+greenProductionCards);
                        this.villagePanel.drawVillagePanelButtons(greenProductionCards, 1, cc::setCardToCopy);

                        JButton doneButtonMinerMole = new JButton("Done");
                        doneButtonMinerMole.addActionListener(k2 -> {
                            //If No cards were selected
                            if(cc.getCardToCopy() == null){
                                cardsToActivate.remove(c);
                                listCardsAction.accept(state);
                                return;
                            }

                            //This is what has to be run after a card has been copied
                            Callable minerMoleAction2 = () -> {
                                cardsToActivate.remove(c);
                                listCardsAction.accept(state);
                                return true;
                            };

                            Callable minerMoleAction3 = () -> {
                                if(isGreenProductionEvent){
                                    if(!checkForAdditionalStepsForCard(state, new ArrayList<>(List.of(cc.getCardToCopy())), true)){
                                        playCardActionWithComponentToIDConversion(state, cc.getCardToCopy(), cardSelection, resourceSelection).triggerCardEffect(state, cc.getCardToCopy());
                                        FunctionWrapper.activateNextFunction();
                                    }
                                    else {
                                        if (!(cc.getCardToCopy().getCardEnumValue() == EverdellParameters.CardDetails.MINER_MOLE)) {
                                            cc.setCardToCopy(null);
                                        }
                                    }
                                }
                                else{
                                    if(!checkForAdditionalStepsForCard(state, new ArrayList<>(List.of(cc.getCardToCopy())), true)){
                                        cardsToActivate.remove(c);
                                        listCardsAction.accept(state);
                                    }
                                    else {
                                        if (!(cc.getCardToCopy().getCardEnumValue() == EverdellParameters.CardDetails.MINER_MOLE)) {
                                            cc.setCardToCopy(null);
                                        }
                                    }
                                }
                                return true;
                            };
                            FunctionWrapper.addAFunction(minerMoleAction2, "Miner Mole Action 2...",0);
                            FunctionWrapper.activateNextFunction(minerMoleAction3, "Miner Mole Action 3...");
                        });

                        playerCardPanel.add(doneButtonMinerMole, BorderLayout.SOUTH);

                        return true;
                    };
                    FunctionWrapper.setDescriptor(minerMoleAction, "Miner Mole Action...");
                    cardsToActivate.put(c, minerMoleAction);
                    break;


            }
        }
        if(cardsToActivate.isEmpty()){
            return false;
        }
        else{
            listCardsAction.accept(state);
            return true;
        }

    }

    public void afterPlayingCard(EverdellGameState state, EverdellCard c){
        //Check if the player has any cards placed that are triggered after a card is played

        EverdellParameters.CardDetails cardPlaced = c.getCardEnumValue();


        System.out.println("Checking for post card placement effects");

        HashMap<EverdellCard, Callable<Boolean>> triggeredCards = new HashMap<>();

        Consumer<EverdellGameState> listCardsAction = (state2) -> {
            if (triggeredCards.isEmpty()){
                redrawPanels();
                FunctionWrapper.activateNextFunction();
                return;
            }
            playerCardPanel.drawPlayerCardsButtons(1, new ArrayList<>(triggeredCards.keySet()), "Cards Have been Triggered, Select which one you want to trigger first", card -> {
                try {
                    FunctionWrapper.addAFunction(triggeredCards.get(card), FunctionWrapper.getDescriptor(triggeredCards.get(card)));
                    FunctionWrapper.activateNextFunction();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        };

        for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
            EverdellParameters.CardDetails cardClass = card.getCardEnumValue();

            ArrayList<ResourceTypes> resourceSelect =  new ArrayList<ResourceTypes>(){{
                add(ResourceTypes.BERRY);
                add(ResourceTypes.PEBBLE);
                add(ResourceTypes.RESIN);
                add(ResourceTypes.TWIG);}};

            resourceSelection = new HashMap<ResourceTypes, Integer>();
            resourceSelection.put(ResourceTypes.TWIG, 0);
            resourceSelection.put(ResourceTypes.PEBBLE, 0);
            resourceSelection.put(ResourceTypes.BERRY, 0);
            resourceSelection.put(ResourceTypes.RESIN, 0);

            switch (cardClass) {
                case JUDGE:
                    System.out.println("Judge Card");

                    if(cardPlaced == EverdellParameters.CardDetails.JUDGE){
                        continue;
                    }
                    Callable judgeAction = () -> {
                        //The player may swap 1 resource for another whenever a card is played
                        JudgeCard jc = (JudgeCard) card;
                        int numOfResource = 1;
                        this.playerCardPanel.drawResourceSelection(numOfResource, jc.getName()+" : Give Up to 1 of any resource, get 1 of any resource", resourceSelect, game2 -> {
                            //Select the resources to give up
                            jc.addResourcesToLose(resourceSelection);

                            //Reset it to 0
                            resetValues();
                            redrawPanels();

                            this.playerCardPanel.drawResourceSelection(numOfResource, jc.getName()+" : Select "+numOfResource+" Resources to Gain", resourceSelect, game3 -> {
                                //Select the resources to gain
                                jc.addResourcesToGain(resourceSelection);
                                jc.applyCardEffect(state);
                                redrawPanels();
                                resetValues();
                                triggeredCards.remove(card);
                                listCardsAction.accept(state);
                                //FunctionWrapper.activateNextFunction();
                                return true;
                            });
                            return true;
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(judgeAction, "Judge Action...");
                    triggeredCards.put(card, judgeAction);
                    //FunctionWrapper.addAFunction(judgeAction);
                    break;

                case COURTHOUSE:
                    System.out.println("Courthouse Card");
                    if(cardPlaced == EverdellParameters.CardDetails.COURTHOUSE || !(c instanceof ConstructionCard)){
                        continue;
                    }

                    Callable courthouseAction = () -> {
                        //The player may swap 1 resource for another whenever a card is played
                        int resourceToGain = 1;

                        resourceSelect.remove(ResourceTypes.BERRY);



                        this.playerCardPanel.drawResourceSelection(resourceToGain, card.getName()+" : Gain 1 Twig, Resin or Pebble", resourceSelect, game -> {
                            //Select the resources to gain
                            playCardActionWithComponentToIDConversion(game, card, cardSelection, resourceSelection).triggerCardEffect(game, card);
                            redrawPanels();
                            resetValues();
                            triggeredCards.remove(card);
                            listCardsAction.accept(game);
                            //FunctionWrapper.activateNextFunction();
                            return true;
                        });
                        return true;
                    };
                    FunctionWrapper.setDescriptor(courthouseAction, "Courthouse Action...");
                    triggeredCards.put(card, courthouseAction);
                    //FunctionWrapper.addAFunction(courthouseAction);
                    break;
            }
        }

        if(triggeredCards.isEmpty()){
            FunctionWrapper.activateNextFunction();
        }
        else{
            listCardsAction.accept(state);
        }

    }

    public PlayCard playCardActionWithComponentToIDConversion(EverdellGameState state, EverdellCard card, ArrayList<EverdellCard> cardSelection, HashMap<ResourceTypes, Integer> resourceSelection){
        //Check if they can pay via occupation
        int cardID = card.getComponentID();

        ArrayList<Integer> csID = new ArrayList<>();
        for(EverdellCard c : cardSelection){
            csID.add(c.getComponentID());
        }
        HashMap<ResourceTypes, Integer> rsID = new HashMap<>(resourceSelection);

        return new PlayCard(state.getCurrentPlayer(), cardID, csID, rsID);
    }


    public void redrawPanels(){
        this.playerInfoPanel.revalidate();
        this.playerInfoPanel.drawPlayerInfoPanel();

        this.playerCardPanel.revalidate();
        this.playerCardPanel.drawPlayerCards();
        this.villagePanel.revalidate();
        this.villagePanel.drawVillagePanel();
        this.meadowCardsPanel.revalidate();
        this.meadowCardsPanel.drawMeadowPanel();

        parent.repaint();
        parent.revalidate();
    }

    public void resetValues(){
        cardSelection = new ArrayList<>();
        resourceSelection = new HashMap<>();
        resourceSelection.put(ResourceTypes.BERRY, 0);
        resourceSelection.put(ResourceTypes.PEBBLE, 0);
        resourceSelection.put(ResourceTypes.RESIN, 0);
        resourceSelection.put(ResourceTypes.TWIG, 0);
    }

    protected void greenProductionEventGUI(EverdellGameState state){
        //Find all cards that are greenProduction and put it into a list
        System.out.println("Green Production Event GUI");
        ArrayList<EverdellCard> greenProductionCards = new ArrayList<>();
        for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
            if(card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION){
                greenProductionCards.add(card);
            }
        }
        if(greenProductionCards.isEmpty()){
            redrawPanels();
            return;
        }

        checkForAdditionalStepsForCard(state, greenProductionCards, true);
    }


    //THIS NEEDS TO BE UPDATED TO USE THE NEW SYSTEM
    //SUMMER GUI IS CURRENTLY NOT WORKING
    private void summerEventGUI(EverdellGameState state){
        cardSelection = new ArrayList<EverdellCard>();
        int cardsToDraw = Math.min(2, state.playerHands.get(state.getCurrentPlayer()).getCapacity() - state.playerHands.get(state.getCurrentPlayer()).getSize());

        //When it is summer, the player will be given the choice to grab 2 cards from the meadow into their hand(Depending on how many cards they have)

        if(cardsToDraw <=0){
            //Players hand is full, we do nothing
            return;
        }

        JLabel summerLabel = new JLabel("It is Summer, You may pick up to "+cardsToDraw+" cards from the meadow");
        summerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playerCardPanel.add(summerLabel, BorderLayout.NORTH);
        this.meadowCardsPanel.drawMeadowPanelButtons(cardsToDraw, card -> {
                cardSelection.add(card);
            });

        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new GridLayout(1,2));

        JButton passButton = new JButton("Pass");
        passButton.addActionListener(k -> {
            redrawPanels();
        });

        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(k -> {
            ArrayList<Integer> csID = new ArrayList<>();
            for(var c : cardSelection){
                csID.add(c.getComponentID());
            }

            new MoveSeason(csID).summerEvent(state);
            resetValues();
            redrawPanels();
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

        if(!(player instanceof HumanGUIPlayer)){
            redrawPanels();
        }
        playerInfoPanel.drawPlayerInfoPanel();
        playerInfoPanel.revalidate();
        playerInfoPanel.repaint();
    }
}
