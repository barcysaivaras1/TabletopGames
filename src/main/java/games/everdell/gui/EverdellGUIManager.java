package games.everdell.gui;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.CoreConstants;
import core.Game;
import core.components.Deck;
import games.catan.CatanGameState;
import games.catan.CatanParameters;
import games.catan.gui.CatanGUI;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.actions.MoveSeason;
import games.everdell.actions.PlaceWorker;
import games.everdell.actions.PlayCard;
import games.everdell.components.*;
import games.everdell.EverdellParameters.ResourceTypes;
import games.everdell.EverdellParameters.ForestLocations;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.EverdellParameters.BasicEvent;

import gui.AbstractGUIManager;
import gui.GamePanel;

import core.components.Counter;
import gui.IScreenHighlight;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;
import org.apache.log4j.Layout;
import org.w3c.dom.css.RGBColor;
import players.human.ActionController;
import scala.collection.immutable.Stream;

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


    private TopPanel meadowCardsPanel;
    private MiddlePanel villagePanel;
    private BottomPanel playerCardPanel;
    private SidePanel playerInfoPanel;
    private JPanel playerActionsPanel;

    public ArrayList<EverdellCard> cardSelection;
    public HashMap<EverdellParameters.ResourceTypes, Counter> resourceSelection;


    public EverdellGUIManager(GamePanel parent, Game game, ActionController ac, Set<Integer> human) {
        super(parent, game, ac, human);
        if (game == null) {
            return;
        }

        EverdellGameState state = (EverdellGameState) game.getGameState();

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
        playerCardPanel = new BottomPanel(this,state);
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
            if(resourceSelection.get(resourceType).getValue() == amountOfResources){
                return;
            }
            resourceSelection.get(resourceType).increment();
            redrawButton.accept(state);
        });
        JButton resourceButtonMinus = new JButton(resourceType+" -");
        resourceButtonMinus.addActionListener(k -> {
            if(resourceSelection.get(resourceType).getValue() == 0){
                return;
            }
            resourceSelection.get(resourceType).decrement();
            redrawButton.accept(state);
        });

        JLabel resourceLabel = new JLabel("" + resourceSelection.get(resourceType).getValue());

        panelToDrawOn.setBackground(resourceColour);
        panelToDrawOn.add(resourceButtonMinus);
        panelToDrawOn.add(resourceLabel);
        panelToDrawOn.add(resourceButtonPlus);

    }

    private void createPaymentChoicePanel(EverdellGameState state, EverdellCard cardToPlace){
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
        ArrayList<EverdellCard> cardsThatCanOccupy = new PlayCard(cardToPlace,cardSelection,resourceSelection).canPayWithOccupation(state, cardToPlace);
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
            });
            paymentPanel.add(occupationButton);
        }

        //Check if there are special cards that can apply a discount {Crane, Innkeeper}

        for(var sCard : state.playerVillage.get(state.getCurrentPlayer())){
            if(sCard.getCardEnumValue() == EverdellParameters.CardDetails.CRANE & cardToPlace instanceof ConstructionCard){
                //Give player the choice to pay via Crane
                JButton craneButton = new JButton("Pay via Crane");

                craneButton.addActionListener(k -> {
                    //They must select resources they want to discount

                    ArrayList<ResourceTypes> r = new ArrayList<>();
                    r.add(ResourceTypes.RESIN);
                    r.add(ResourceTypes.TWIG);
                    r.add(ResourceTypes.PEBBLE);

                    playerCardPanel.drawResourceSelection(3, "Select 3 Resources to discount", r, (s) -> {
                        //Crane Card is sCard in this instance
                        //Trigger Crane Card Effect
                        new PlayCard(cardToPlace, cardSelection, resourceSelection).triggerCardEffect(s, sCard);

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
                JButton innkeeperButton = new JButton("Pay via Innkeeper");


                innkeeperButton.addActionListener(k -> {
                    //They must select resources they want to discount

                    ArrayList<ResourceTypes> r = new ArrayList<>();
                    r.add(ResourceTypes.BERRY);

                    playerCardPanel.drawResourceSelection(3, "Select 3 Resources to discount", r, (s) -> {
                        //Innkeeper Card is sCard in this instance
                        //Trigger Innkeeper Card Effect
                        new PlayCard(cardToPlace, cardSelection, resourceSelection).triggerCardEffect(s, sCard);

                        //Card needs to be removed from the village
                        state.playerVillage.get(state.getCurrentPlayer()).remove(sCard);

                        //Place the now paid for card
                        placeACard(state, cardToPlace);
                        return true;
                    });
                });
                paymentPanel.add(innkeeperButton);
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
                System.out.println("WE ARE WORKING!");
                //If the card is already selected, unselect it
                if(cardButton.getBackground() == Color.GRAY){
                    System.out.println("1");
                    cardButton.setBackground(params.cardColour.get(card.getCardType()));
                    ForestLocations.cardChoices.remove(card);
                    cardSelection.remove(card);
                }
                //Limit the number of selections
                else if(numberOfSelections == ForestLocations.cardChoices.size() || numberOfSelections == cardSelection.size()){
                    System.out.println("2");
                    return;
                }
                //Select the card
                else {
                    System.out.println("3");
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

    public void placeACard(EverdellGameState state, EverdellCard card){

        System.out.println("Placing a card");
        //We check if the player can afford the card
        if(new PlayCard(card, cardSelection, resourceSelection).checkIfPlayerCanBuyCard(state)){
            if(!checkForAdditionalStepsForCard(state, card)){
                //Place the card
                new PlayCard(card, cardSelection, resourceSelection).execute(state);
                redrawPanels();
                checkForTriggeredCardEffects(state, card);
            }
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

        JButton placeWorkerButton = new JButton("Place Worker");
        placeWorkerButton.addActionListener(k -> {
            redrawPanels();
            this.playerCardPanel.drawWorkerPlacement();
        });
        workerActionPanel.add(placeWorkerButton);

        JButton playCardButton = new JButton("Play Card");
        playCardButton.addActionListener(k -> {
            //Make player cards available for selection via buttons
            this.playerCardPanel.drawPlayerCardsButtons(1, card -> {
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

            new MoveSeason(cardSelection).execute(state);

            EverdellParameters.Seasons currentSeason = state.currentSeason[state.getCurrentPlayer()];

            //If it is summer, the player must draw 2 cards from the meadow
            if(currentSeason == EverdellParameters.Seasons.SUMMER){
                summerEventGUI(state, playerCardPanel, playerInfoPanel);
            }
            //If it is Spring or Autumn, we must trigger the green production event and see if any additional actions
            // need to be taken
            if(currentSeason == EverdellParameters.Seasons.AUTUMN || currentSeason == EverdellParameters.Seasons.SPRING){
                greenProductionEventGUI(state);
            }
            this.playerInfoPanel.drawPlayerInfoPanel();
        });
        seasonActionPanel.add(moveSeasonButton);

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
    }

    //This is in charge of providing CARD specific GUI so that the player can make decisions
    private Boolean checkForAdditionalStepsForCard(EverdellGameState state, EverdellCard c){

        JButton doneButton;
        int numOfResource;

        System.out.println("Checking for additional steps for card");

        ArrayList<EverdellCard> cardsToDisplay = new ArrayList<>();


        EverdellParameters.CardDetails cardClass = c.getCardEnumValue();
        ArrayList<EverdellCard> selectedCards = new ArrayList<>();

        ArrayList<ResourceTypes> resourceSelect =  new ArrayList<ResourceTypes>(){{
            add(ResourceTypes.BERRY);
            add(ResourceTypes.PEBBLE);
            add(ResourceTypes.RESIN);
            add(ResourceTypes.TWIG);}};

        resourceSelection = new HashMap<ResourceTypes, Counter>();
        resourceSelection.put(ResourceTypes.TWIG, new Counter());
        resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
        resourceSelection.put(ResourceTypes.BERRY, new Counter());
        resourceSelection.put(ResourceTypes.RESIN, new Counter());

        switch (cardClass) {
            case BARD:
                //The player can discard up to 5 cards
                cardsToDisplay = state.playerHands.get(state.getCurrentPlayer()).getComponents().stream().filter(card -> card != c).collect(Collectors.toCollection(ArrayList::new));


                this.playerCardPanel.drawPlayerCardsButtons(5, cardsToDisplay, selectedCards::add);

                doneButton = new JButton("Discard Selected Cards");
                doneButton.addActionListener(k2 -> {
                    if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                        CritterCard cc = (CritterCard) c;
                        cc.applyCardEffect(state);
                    }
                    else{
                        new PlayCard(c,selectedCards,new HashMap<>()).execute(state);
                    }
                    redrawPanels();
                });
                playerCardPanel.add(doneButton, BorderLayout.SOUTH);

                return true;

            case RUINS:
                //Get every card that is a construction in the village so that we can send it for selection
                ArrayList<EverdellCard> constructionCards = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(card -> card instanceof ConstructionCard).filter(card -> card.getCardEnumValue() != EverdellParameters.CardDetails.RUINS).collect(Collectors.toCollection(ArrayList::new));

                this.villagePanel.drawVillagePanelButtons(constructionCards,1, card ->{
                    System.out.println("Village card Selected");
                    cardSelection.add(card);
                });

                doneButton = new JButton("Discard Selected Card, Refund Resources and Draw 2 Cards");
                doneButton.addActionListener(k2 -> {
                    System.out.println("Done Button Pressed, Card selection is : "+cardSelection);
                    new PlayCard(c,cardSelection,resourceSelection).execute(state);
                    redrawPanels();
                });

                playerCardPanel.add(doneButton, BorderLayout.SOUTH);

                return true;

            case HUSBAND:
                //THIS CAN EASILY BE SIMPLIFIED
                HusbandCard hc = (HusbandCard) c;

                //Check for wife
                for(var card : state.playerVillage.get(state.getCurrentPlayer())){
                    if(card.getCardEnumValue() == EverdellParameters.CardDetails.WIFE){
                        if(((WifeCard) card).getHusband() == null){
                            ((WifeCard) card).setHusband(hc);
                            hc.setWife((WifeCard) card);
                        }
                    }
                }
                if(hc.getWife() != null){
                    //Check for farm
                    for(var card : state.playerVillage.get(state.getCurrentPlayer())){
                        if(card.getCardEnumValue() == EverdellParameters.CardDetails.FARM){
                            playerCardPanel.drawResourceSelection(1, "Select 1 Resource to Gain", resourceSelect, game -> {
                                if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                                    CritterCard cc = (CritterCard) c;
                                    cc.applyCardEffect(state);
                                }
                                else{
                                    new PlayCard(c, cardSelection, resourceSelection).execute(state);
                                }
                                redrawPanels();
                                return true;
                            });
                            return true;
                        }
                        else{
                            if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                                CritterCard cc = (CritterCard) c;
                                cc.applyCardEffect(state);
                            }
                            else{
                                new PlayCard(c, cardSelection, resourceSelection).execute(state);
                            }
                            redrawPanels();
                        }

                    }

                }
                else{
                    if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                        CritterCard cc = (CritterCard) c;
                        cc.applyCardEffect(state);
                    }
                    else {
                        new PlayCard(c, cardSelection, resourceSelection).execute(state);
                    }
                    redrawPanels();
                }

                return true;

            case WOOD_CARVER:
                numOfResource = 3;

                this.playerCardPanel.drawResourceSelection(numOfResource, "Trade Up to 3 Twigs for 1 Point Each",new ArrayList<ResourceTypes>(){{
                    add(ResourceTypes.TWIG);
                }}, game -> {
                    if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                        new PlayCard(c, cardSelection, resourceSelection).triggerCardEffect(state, c);
                    }
                    else{
                        new PlayCard(c, cardSelection, resourceSelection).execute(state);
                    }

                    redrawPanels();
                    return true;
                });

                return true;

            case DOCTOR:
                numOfResource = 3;
                this.playerCardPanel.drawResourceSelection(numOfResource, "Trade Up to 3 Berries for 1 Point Each",new ArrayList<ResourceTypes>(){{
                    add(ResourceTypes.BERRY);
                }}, game -> {
                    if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                        new PlayCard(c, cardSelection, resourceSelection).triggerCardEffect(state, c);
                    }
                    else{
                        new PlayCard(c, cardSelection, resourceSelection).execute(state);
                    }
                    redrawPanels();
                    return true;
                });
                return true;

            case PEDDLER:

                PeddlerCard pc = (PeddlerCard) c;
                numOfResource = 2;
                this.playerCardPanel.drawResourceSelection(numOfResource, "Give Up to 2 of any resource, get 1 of any resource for each one given", resourceSelect, game -> {
                    //Select the resources to give up
                    pc.addResourcesToLose(resourceSelection);

                    //Reset it to 0
                    resetValues();
                    redrawPanels();

                    this.playerCardPanel.drawResourceSelection(numOfResource, "Select "+numOfResource+" Resources to Gain", resourceSelect, game2 -> {
                        //Select the resources to gain
                        pc.addResourcesToGain(resourceSelection);
                        if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                            new PlayCard(pc, cardSelection, resourceSelection).triggerCardEffect(state, c);
                        }
                        else{
                            new PlayCard(pc, cardSelection, resourceSelection).execute(state);
                        }
                        redrawPanels();
                        return true;
                    });
                    return true;
                });
                return true;

            case CHIP_SWEEP:
                //The player must select a production card from their village, which its effect will be activated.
                //Get every card that is a green production card in the village so that we can send it for selection
                ArrayList<EverdellCard> greenProductionCards = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(card -> card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION ).filter(card -> card.getCardEnumValue() != EverdellParameters.CardDetails.CHIP_SWEEP).collect(Collectors.toCollection(ArrayList::new));

                cardSelection.clear();

                this.villagePanel.drawVillagePanelButtons(greenProductionCards, 1, card -> {
                    cardSelection.clear();
                    cardSelection.add(card);
                });

                doneButton = new JButton("Done");
                doneButton.addActionListener(k2 -> {
                    //If nothing is selected
                    if(cardSelection.isEmpty()){
                        if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                            new PlayCard(c, cardSelection, resourceSelection).triggerCardEffect(state, c);
                        }
                        else{
                            new PlayCard(c, cardSelection, resourceSelection).execute(state);
                        }
                        redrawPanels();
                    }
                    //If it is triggered by a green production event
                    else if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                        System.out.println("EMPTY CHECK "+cardSelection);
                        //redrawPanels();
                        if(!checkForAdditionalStepsForCard(state, cardSelection.get(0))){
                            new PlayCard(c, cardSelection, resourceSelection).triggerCardEffect(state,c);
                            redrawPanels();
                        }
                        else{
                            new PlayCard(c, cardSelection, resourceSelection).triggerCardEffect(state,c);
                        }

                    }
                    //If we are placing the card
                    else{
                        if(!checkForAdditionalStepsForCard(state, cardSelection.get(0))){
                            new PlayCard(c, cardSelection, resourceSelection).execute(state);
                            redrawPanels();
                        }
                        else{
                            new PlayCard(c, cardSelection, resourceSelection).execute(state);
                        }
                    }
                });

                playerCardPanel.add(doneButton, BorderLayout.SOUTH);

                return true;


            case MONK:
                System.out.println("Monk Card");
                playerCardPanel.drawResourceSelection(2, "Give up to 2 berries to a player, for 2 points each", new ArrayList<ResourceTypes>(){{
                    add(ResourceTypes.BERRY);
                }}, game -> {
                    playerCardPanel.drawPlayerSelection(player -> {
                        MonkCard mc = (MonkCard) c;
                        mc.setSelectedPlayer(player);

                        if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                            new PlayCard(mc, cardSelection, resourceSelection).triggerCardEffect(state, c);
                        }
                        else{
                            new PlayCard(mc, cardSelection, resourceSelection).execute(state);
                        }
                        redrawPanels();
                    });
                    return true;
                });
                return true;

            case FOOL:
                playerCardPanel.drawPlayerSelection(player -> {
                    FoolCard fc = (FoolCard) c;
                    fc.setSelectedPlayer(player);
                    new PlayCard(c, cardSelection, resourceSelection).execute(state);
                    redrawPanels();
                });
                return true;

            case TEACHER:
                ArrayList<EverdellCard> cTD = new ArrayList<>();
                cTD.add(state.cardDeck.draw());
                cTD.add(state.cardDeck.draw());

                playerCardPanel.drawPlayerCardsButtons( 1, cTD, card -> {
                    if(card == cTD.get(0)){
                        cardSelection.add(cTD.get(0));
                        cardSelection.add(cTD.get(1));
                    }
                    else{
                        cardSelection.add(cTD.get(1));
                        cardSelection.add(cTD.get(0));
                    }
                });

                doneButton = new JButton("Done");
                doneButton.addActionListener(k2 -> {
                    playerCardPanel.drawPlayerSelection(player -> {
                        TeacherCard tc = (TeacherCard) c;
                        tc.setSelectedPlayer(player);
                        new PlayCard(c, cardSelection, resourceSelection).execute(state);
                        redrawPanels();
                    });
                });

                playerCardPanel.add(doneButton, BorderLayout.SOUTH);
                return true;

            case UNDERTAKER:
                //The player must move 3 cards to the discard pile
                //The meadow must replenish
                //The player must draw 1 card from the meadow (Assuming they have space)
                redrawPanels();

                ArrayList<EverdellCard> meadowCardsToDiscard = new ArrayList<>();
                meadowCardsPanel.drawMeadowPanelButtons(3, card -> {
                    meadowCardsToDiscard.add(card);
                });

                doneButton = new JButton("Done");
                doneButton.addActionListener(k2 -> {

                    //Remove Cards from meadow
                    for(EverdellCard card : meadowCardsToDiscard){
                        state.meadowDeck.remove(card);
                    }

                    //Replenish the meadow
                    while(state.meadowDeck.getSize() !=8 ){
                        state.meadowDeck.add(state.cardDeck.draw());
                    }

                    redrawPanels();

                    playerCardPanel.drawPlayerCardsButtons(1, state.meadowDeck.stream().collect(Collectors.toCollection(ArrayList::new)), card -> {
                        cardSelection.clear();
                        cardSelection.add(card);

                        new PlayCard(c, cardSelection, resourceSelection).execute(state);
                        redrawPanels();
                    });
                });

                playerCardPanel.add(doneButton, BorderLayout.SOUTH);
                return true;

            case POSTAL_PIGEON:
                //The player must draw 2 cards from the deck, and must select 1 to play up to a cost of 3 for free
                redrawPanels();
                ArrayList<EverdellCard> cardsToDraw = new ArrayList<>();
                cardsToDraw.add(state.cardDeck.draw());
                cardsToDraw.add(state.cardDeck.draw());

                playerCardPanel.drawPlayerCardsButtons(1, cardsToDraw, card -> {
                    cardSelection.add(card);
                    cardSelection.add(cardSelection.get(0) == card ? cardsToDraw.get(1) : cardsToDraw.get(0));

                    new PlayCard(c, cardSelection, resourceSelection).execute(state);
                    redrawPanels();

                    //If the selected card was valid, we must now go through the process of the placing the card
                    if(card.isCardPayedFor()){
                        placeACard(state, card);
                    }
                });
                return true;

            default:
                redrawPanels();
                return false;

        }
    }

    public void checkForTriggeredCardEffects(EverdellGameState state, EverdellCard c){

        EverdellParameters.CardDetails cardPlaced = c.getCardEnumValue();



        System.out.println("Checking for triggered card effects");
        for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
            EverdellParameters.CardDetails cardClass = card.getCardEnumValue();

            ArrayList<ResourceTypes> resourceSelect =  new ArrayList<ResourceTypes>(){{
                add(ResourceTypes.BERRY);
                add(ResourceTypes.PEBBLE);
                add(ResourceTypes.RESIN);
                add(ResourceTypes.TWIG);}};

            resourceSelection = new HashMap<ResourceTypes, Counter>();
            resourceSelection.put(ResourceTypes.TWIG, new Counter());
            resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
            resourceSelection.put(ResourceTypes.BERRY, new Counter());
            resourceSelection.put(ResourceTypes.RESIN, new Counter());

            System.out.println("Card Class : "+cardClass);

            switch (cardClass) {
                case JUDGE:
                    System.out.println("Judge Card");
                    if(cardPlaced == EverdellParameters.CardDetails.JUDGE){
                        continue;
                    }
                    //The player may swap 1 resource for another whenever a card is played
                    JudgeCard jc = (JudgeCard) card;
                    int numOfResource = 1;
                    this.playerCardPanel.drawResourceSelection(numOfResource, "Give Up to 1 of any resource, get 1 of any resource", resourceSelect, game -> {
                        //Select the resources to give up
                        jc.addResourcesToLose(resourceSelection);

                        //Reset it to 0
                        resetValues();
                        redrawPanels();

                        this.playerCardPanel.drawResourceSelection(numOfResource, "Select "+numOfResource+" Resources to Gain", resourceSelect, game2 -> {
                            //Select the resources to gain
                            jc.addResourcesToGain(resourceSelection);
                            if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                                CritterCard cc = (CritterCard) c;
                                cc.applyCardEffect(state);
                            }
                            else{
                                new PlayCard(c, cardSelection, resourceSelection).execute(state);
                            }
                            redrawPanels();
                            return true;
                        });
                        return true;
                    });
            }
        }

    }


    public void redrawPanels(){
        this.playerInfoPanel.drawPlayerInfoPanel();
        this.playerCardPanel.drawPlayerCards();
        this.villagePanel.drawVillagePanel();
        this.meadowCardsPanel.drawMeadowPanel();
    }

    public void resetValues(){
        cardSelection = new ArrayList<>();
        resourceSelection = new HashMap<>();
        resourceSelection.put(ResourceTypes.BERRY, new Counter());
        resourceSelection.put(ResourceTypes.PEBBLE, new Counter());
        resourceSelection.put(ResourceTypes.RESIN, new Counter());
        resourceSelection.put(ResourceTypes.TWIG, new Counter());
    }

    private void greenProductionEventGUI(EverdellGameState state){
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


        JButton nextButton = new JButton("Next");
        int counter = 0;
        playerActionsPanel.removeAll();
        nextButton.addActionListener(k -> {
            if(greenProductionCards.isEmpty()){
                System.out.println("No more cards to check");
                this.playerActionsPanel.remove(nextButton);
                drawPlayerActionsPanel(state);
                redrawPanels();
            }
            else {

                if(!checkForAdditionalStepsForCard(state, greenProductionCards.get(counter))){
                    greenProductionCards.remove(counter);
                }
                else{
                    greenProductionCards.remove(counter);
                }
            }
        });
        playerActionsPanel.add(nextButton);

        checkForAdditionalStepsForCard(state, greenProductionCards.get(counter));
        greenProductionCards.remove(counter);
    }


    //THIS NEEDS TO BE UPDATED TO USE THE NEW SYSTEM
    //SUMMER GUI IS CURRENTLY NOT WORKING
    private void summerEventGUI(EverdellGameState state,JPanel playerCardPanel, JPanel playerInfoPanel){
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
            new MoveSeason(cardSelection).summerEvent(state);
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

        playerInfoPanel.drawPlayerInfoPanel();

    }
}
