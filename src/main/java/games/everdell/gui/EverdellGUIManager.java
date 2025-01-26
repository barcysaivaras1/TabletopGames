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
        playerActionsPanel = playerActionsPanel(state);

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

    private void createPaymentChoicePanel(EverdellGameState gameState, JPanel panel,int numberOfOccupation, ConstructionCard constructionCard, EverdellCard cardToPlace){
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(147, 136, 40));
        panel.add(new JLabel("Worker Placement"), BorderLayout.NORTH);
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            panel.removeAll();
            this.playerCardPanel.drawPlayerCards();
        });
        panel.add(back,BorderLayout.SOUTH);

        JPanel paymentPanel = new JPanel();

        JButton resourcesButton = new JButton("Pay With Resources");
        resourcesButton.addActionListener(k -> {
            //Place the card
            placeACard(gameState, cardToPlace);
        });
        paymentPanel.add(resourcesButton);

        JButton occupationButton = new JButton("Pay With Occupation");
        occupationButton.addActionListener(k -> {
            if(numberOfOccupation == 1) {

                constructionCard.occupyConstruction((CritterCard) cardToPlace);

                //Place the card
                placeACard(gameState, cardToPlace);
            }

        });
        paymentPanel.add(occupationButton);

        panel.add(paymentPanel,BorderLayout.CENTER);


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

        //We check if the player can afford the card
        if(new PlayCard(card, cardSelection, resourceSelection).checkIfPlayerCanBuyCard(state)){
            if(!checkForAdditionalStepsForCard(state, card)){
                //Place the card
                new PlayCard(card, cardSelection, resourceSelection).execute(state);
                redrawPanels();
            }
        }
        else{
            redrawPanels();
        }
    }

    //Displays the possible actions that the player can take
    private JPanel playerActionsPanel(EverdellGameState state){
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

                //Can the card occupy a Construction Card
                for(EverdellCard c : state.playerVillage.get(state.getCurrentPlayer())) {
                    System.out.println("We are checking if the card can occupy a construction card");
                    if (c instanceof ConstructionCard) {
                        if(((ConstructionCard) c).canCardOccupyThis(state, card)){
                            System.out.println("A card can occupy this");
                            createPaymentChoicePanel(state, playerCardPanel, ((ConstructionCard) c).getCardsThatCanOccupy().size(),(ConstructionCard) c, card);
                            return;
                        }
                    }
                }
                //If not we make them pay with resources
                //Place the card
                placeACard(state, card);
            });

            //Make meadow cards available for selection via buttons
            this.meadowCardsPanel.drawMeadowPanelButtons( 1, card ->{

                //Can the card occupy a Construction Card
                for(EverdellCard c : state.playerVillage.get(state.getCurrentPlayer())) {
                    if (c instanceof ConstructionCard) {
                        System.out.println("Construction Card");
                        System.out.println(((ConstructionCard) c).getCardsThatCanOccupy());
                        System.out.println(card.getCardEnumValue());
                        if(((ConstructionCard) c).canCardOccupyThis(state,card)){
                            createPaymentChoicePanel(state, playerCardPanel, ((ConstructionCard) c).getCardsThatCanOccupy().size(),(ConstructionCard) c, card);
                            return;
                        }
                    }
                }

                //Place the card
                placeACard(state, card);
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
    private Boolean checkForAdditionalStepsForCard(EverdellGameState state, EverdellCard c){

        JButton doneButton;
        int numOfResource;

        System.out.println("Checking for additional steps for card");


        EverdellParameters.CardDetails cardClass = c.getCardEnumValue();
        ArrayList<EverdellCard> selectedCards = new ArrayList<>();


        switch (cardClass) {
            case BARD:
                //The player can discard up to 5 cards
                ArrayList<EverdellCard> cardsToDisplay = state.playerHands.get(state.getCurrentPlayer()).getComponents().stream().filter(card -> card != c).collect(Collectors.toCollection(ArrayList::new));


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

                this.villagePanel.drawVillagePanelButtons(constructionCards,1, selectedCards::add);

                doneButton = new JButton("Discard Selected Card, Refund Resources and Draw 2 Cards");
                doneButton.addActionListener(k2 -> {
                    if(state.playerVillage.get(state.getCurrentPlayer()).contains(c)){
                        ConstructionCard cc = (ConstructionCard) c;
                        cc.applyCardEffect(state);
                    }
                    else{
                        new PlayCard(c,selectedCards,new HashMap<>()).execute(state);
                    }
                    redrawPanels();
                });

                playerCardPanel.add(doneButton, BorderLayout.SOUTH);

                return true;

            case WOOD_CARVER:
                numOfResource = 3;

                this.playerCardPanel.drawResourceSelection(numOfResource, "Trade Up to 3 Twigs for 1 Point Each",new ArrayList<ResourceTypes>(){{
                    add(ResourceTypes.TWIG);
                }}, game -> {
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

            case DOCTOR:
                numOfResource = 3;
                this.playerCardPanel.drawResourceSelection(numOfResource, "Trade Up to 3 Berries for 1 Point Each",new ArrayList<ResourceTypes>(){{
                    add(ResourceTypes.BERRY);
                }}, game -> {
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

            case PEDDLER:

                PeddlerCard pc = (PeddlerCard) c;
                numOfResource = 2;
                this.playerCardPanel.drawResourceSelection(numOfResource, "Give Up to 2 of any resource, get 1 of any resource for each one given",new ArrayList<ResourceTypes>(){{
                    add(ResourceTypes.BERRY);
                    add(ResourceTypes.PEBBLE);
                    add(ResourceTypes.RESIN);
                    add(ResourceTypes.TWIG);
                }}, game -> {
                    //Select the resources to give up
                    pc.addResourcesToLose(resourceSelection);

                    //Reset it to 0
                    resetValues();
                    redrawPanels();

                    this.playerCardPanel.drawResourceSelection(numOfResource, "Select "+numOfResource+" Resources to Gain",new ArrayList<ResourceTypes>(){{
                        add(ResourceTypes.BERRY);
                        add(ResourceTypes.PEBBLE);
                        add(ResourceTypes.RESIN);
                        add(ResourceTypes.TWIG);
                    }}, game2 -> {
                        //Select the resources to gain
                        pc.addResourcesToGain(resourceSelection);
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
                return true;

            case CHIP_SWEEP:
                //The player must select a production card from their village, which its effect will be activated.
                //Get every card that is a green production card in the village so that we can send it for selection
                ArrayList<EverdellCard> greenProductionCards = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(card -> card.getCardType() == EverdellParameters.CardType.GREEN_PRODUCTION ).filter(card -> card.getCardEnumValue() != EverdellParameters.CardDetails.CHIP_SWEEP).collect(Collectors.toCollection(ArrayList::new));

                this.villagePanel.drawVillagePanelButtons(greenProductionCards, 1, card -> {
                    cardSelection.clear();
                    cardSelection.add(card);
                });
                doneButton = new JButton("Done");
                doneButton.addActionListener(k2 -> {
                    if(!checkForAdditionalStepsForCard(state, cardSelection.get(0))){
                        new PlayCard(c, cardSelection, resourceSelection).execute(state);
                        redrawPanels();
                    }
                    else{
                        new PlayCard(c, cardSelection, resourceSelection).execute(state);
                    }
                });

                playerCardPanel.add(doneButton, BorderLayout.SOUTH);

                return true;


            default:
                redrawPanels();
                return false;

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
                this.playerActionsPanel.removeAll();
                this.playerActionsPanel.add(playerActionsPanel((EverdellGameState) game.getGameState()));
                redrawPanels();
            }
            else {
                while(!checkForAdditionalStepsForCard(state, greenProductionCards.get(counter))){
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
    }
}
