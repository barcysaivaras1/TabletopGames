package games.everdell.gui;

import core.components.Counter;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.EverdellParameters.BasicLocations;
import games.everdell.EverdellParameters.BasicEvent;
import games.everdell.EverdellParameters.SpecialEvent;
import games.everdell.EverdellParameters.HavenLocation;
import games.everdell.EverdellParameters.ForestLocations;
import games.everdell.EverdellParameters.RedDestinationLocation;
import games.everdell.EverdellParameters.ResourceTypes;
import games.everdell.FunctionWrapper;
import games.everdell.actions.MoveSeason;
import games.everdell.actions.PlaceWorker;
import games.everdell.actions.PlayCard;
import games.everdell.components.*;
import org.apache.spark.sql.sources.In;
import scala.collection.immutable.Stream;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BottomPanel extends JPanel {

    EverdellGUIManager everdellGUIManager;
    EverdellGameState state;

    private ArrayList<EverdellCard> cardsToDisplay;
    private Consumer<EverdellCard> cardButtonAction;
    private Function<EverdellGameState,Boolean> resourceButtonAction;
    private Consumer<Integer> playerSelectionAction;

    private String labelText;
    private ArrayList<ResourceTypes> allowedResources;

    private int numberOfCardSelections;
    private int numberOfResourceSelections;


    //Which Panels should we display
    private Boolean displayCards;
    private Boolean makeCardsButtons;
    private Boolean displayWorkerPlacement;
    private Boolean displayBasicLocations;
    private Boolean displayForestLocations;
    private Boolean displayBasicEvents;
    private Boolean displaySpecialEvents;
    private Boolean displayJourneyLocations;
    private Boolean displayHavenLocation;
    private Boolean displayRedDestinations;
    private Boolean displayResourceSelection;
    private Boolean displayPlayerSelection;


    //Modes
    private Boolean copyMode;
    private Consumer<EverdellParameters.AbstractLocations> copyAction;

    BottomPanel(EverdellGUIManager EverdellGUIManager, EverdellGameState state){
        super();
        this.everdellGUIManager = EverdellGUIManager;
        this.state = state;

        displayCards = false;
        makeCardsButtons = false;
        displayWorkerPlacement = false;
        displayBasicLocations = false;
        displayForestLocations = false;
        displayBasicEvents = false;
        displayRedDestinations = false;

        copyMode = false;


    }

    private void draw(){
        this.removeAll();
        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);



        if(displayCards){
            drawCards();
        }

        else if(displayWorkerPlacement){
            drawWorkerNavigation();
        }
        else if(displayResourceSelection){
            drawResourceSelectionPanel();
        }
        else if(displayPlayerSelection){
            drawPlayerSelectionPanel();
        }
    }

    private void drawCards(){
        JLabel playerCardLabel = new JLabel(labelText);
        playerCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(playerCardLabel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel();
        cardsPanel.setBackground(Color.BLACK);
        cardsPanel.setLayout(new GridLayout(2,4));

        if(makeCardsButtons){
            this.add(everdellGUIManager.drawCardButtons(state, Objects.requireNonNullElseGet(cardsToDisplay, () -> new ArrayList<>(state.playerHands.get(state.getCurrentPlayer()).getComponents())), cardsPanel, cardButtonAction, numberOfCardSelections), BorderLayout.CENTER);
        }
        else{
            this.add(everdellGUIManager.drawCards(state,state.playerHands.get(0), cardsPanel), BorderLayout.CENTER);
        }
    }

    private void drawWorkerNavigation(){
        JLabel playerCardLabel = new JLabel("Worker Placement");
        playerCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(playerCardLabel, BorderLayout.NORTH);

        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            drawPlayerCards();
        });
        this.add(back,BorderLayout.SOUTH);

        JPanel workerOptionsPanel = new JPanel();

        drawBasicLocationsButton(workerOptionsPanel);

        drawForestLocationsButton(workerOptionsPanel);


        JButton havenButton = new JButton("Haven Location");
        havenButton.addActionListener(k -> {
            resetNavigation();
            displayHavenLocation = true;
            displayWorkerPlacement = true;
            draw();
        });
        workerOptionsPanel.add(havenButton);

        JButton journeyButton = new JButton("Journey Location");
        journeyButton.addActionListener(k -> {
            resetNavigation();
            displayJourneyLocations = true;
            displayWorkerPlacement = true;
            draw();
        });
        if(state.currentSeason[state.getCurrentPlayer()] == EverdellParameters.Seasons.AUTUMN) {
            workerOptionsPanel.add(journeyButton);
        }

        JButton basicEventsButton = new JButton("Basic Events");
        basicEventsButton.addActionListener(k -> {
            resetNavigation();
            displayBasicEvents = true;
            displayWorkerPlacement = true;

            draw();
        });
        workerOptionsPanel.add(basicEventsButton);

        JButton specialEventsButton = new JButton("Special Events");
        specialEventsButton.addActionListener(k -> {
            resetNavigation();
            displaySpecialEvents = true;
            displayWorkerPlacement = true;

            draw();
        });
        workerOptionsPanel.add(specialEventsButton);



        JButton redDestinationButton = new JButton("Red Destinations");
        redDestinationButton.addActionListener(k -> {
            resetNavigation();
            displayRedDestinations = true;
            displayWorkerPlacement = true;

            draw();
        });
        workerOptionsPanel.add(redDestinationButton);

        this.add(workerOptionsPanel,BorderLayout.CENTER);


        if(displayBasicLocations){
            drawBasicLocations(location -> {
                  new PlaceWorker(location,everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                  everdellGUIManager.redrawPanels();
            });
        }

        if(displayForestLocations){
            drawForestLocations();
        }

        if(displayBasicEvents){
            drawBasicEvents();
        }

        if(displayRedDestinations){
            drawRedDestinations();
        }

        if(displayHavenLocation){
            drawHavenLocation();
        }

        if (displayJourneyLocations){
            drawJourneyLocations();
        }
        if(displaySpecialEvents){
            drawSpecialEvents();
        }
    }

    private void drawBasicLocationsButton(JPanel panelToDrawOn){
        JButton basicLocationsButton = new JButton("Basic Locations");
        basicLocationsButton.addActionListener(k -> {
            resetNavigation();
            displayBasicLocations = true;
            displayWorkerPlacement = true;

            draw();
        });
        panelToDrawOn.add(basicLocationsButton);
    }

    private void drawForestLocationsButton(JPanel panelToDrawOn){
        JButton forestLocationsButton = new JButton("Forest Locations");
        forestLocationsButton.addActionListener(k -> {
            resetNavigation();
            displayForestLocations = true;
            displayWorkerPlacement = true;

            draw();
        });
        panelToDrawOn.add(forestLocationsButton);
    }



    private void drawBasicLocations(Consumer<EverdellParameters.AbstractLocations> buttonAction){
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            displayBasicLocations = false;
            draw();
        });
        this.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,state.Locations.size()));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : state.Locations.keySet()){
            //Select only Basic Locations
            if(!(location instanceof BasicLocations)){
                continue;
            }
            JButton button = new JButton(location.name());

            //If the location is not free for the player, change the background color
            if(!state.Locations.get(location).isLocationFreeForPlayer(state)) {
                button.setBackground(Color.LIGHT_GRAY);
            }

            button.addActionListener(k -> {
                if(copyMode){
                    copyAction.accept(location);
                    return;
                }
                buttonAction.accept(location);
            });
            locationPanel.add(button);
        }
        this.add(locationPanel,BorderLayout.CENTER);
    }

    private void drawJourneyLocations(){
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            displayJourneyLocations = false;
            draw();
        });
        this.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,state.Locations.size()));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : state.Locations.keySet()){
            //Select only Haven Locations
            if(!(location instanceof EverdellParameters.JourneyLocations)){
                continue;
            }
            JButton button = new JButton(location.name());

            //If the location is not free for the player, change the background color
            if(!state.Locations.get(location).isLocationFreeForPlayer(state)) {
                button.setBackground(Color.LIGHT_GRAY);
            }

            button.addActionListener(k -> {
                if(copyMode){
                    copyAction.accept(location);
                    return;
                }
                //Discard cards from hand
                System.out.println("Size : "+state.playerHands.get(state.getCurrentPlayer()).getSize());
                drawPlayerCardsButtons(state.playerHands.get(state.getCurrentPlayer()).getSize(), "Discard cards equal to the number of the selected Journey : "+location , card -> {
                    everdellGUIManager.cardSelection.add(card);
                });

                JButton doneButton = new JButton("Done");
                doneButton.addActionListener(k2 -> {
                    new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                    everdellGUIManager.redrawPanels();
                });

                this.add(doneButton, BorderLayout.SOUTH);

            });
            locationPanel.add(button);
        }
        this.add(locationPanel,BorderLayout.CENTER);
    }

    private void drawHavenLocation(){
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            displayHavenLocation = false;
            draw();
        });
        this.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,state.Locations.size()));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : state.Locations.keySet()){
            //Select only Haven Locations
            if(!(location instanceof HavenLocation)){
                continue;
            }
            JButton button = new JButton(location.name());

            button.addActionListener(k -> {
                if(copyMode){
                    copyAction.accept(location);
                    return;
                }
                //Discard cards from hand
                System.out.println("Size : "+state.playerHands.get(state.getCurrentPlayer()).getSize());
                drawPlayerCardsButtons(state.playerHands.get(state.getCurrentPlayer()).getSize(), "Discard as many cards as you want, +1 Resource for every 2 discarded", card -> {
                    everdellGUIManager.cardSelection.add(card);
                });

                JButton doneButton = new JButton("Done");
                doneButton.addActionListener(k2 -> {
                    //The player must select resources
                    drawResourceSelection(everdellGUIManager.cardSelection.size()/2,"Choose "+everdellGUIManager.cardSelection.size()/2+" Resources to gain", new ArrayList<>(List.of(ResourceTypes.values())), state -> {
                        new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                        everdellGUIManager.redrawPanels();
                        return true;
                    });
                });

                this.add(doneButton, BorderLayout.SOUTH);

            });
            locationPanel.add(button);
        }
        this.add(locationPanel,BorderLayout.CENTER);
    }
    private void drawRedDestinations(){
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            displayRedDestinations = false;
            draw();
        });
        this.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,state.Locations.size()));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : state.Locations.keySet()){
            //Select only Basic Locations
            if(!(location instanceof RedDestinationLocation)){
                continue;
            }
            JButton button = new JButton(location.name());

            //If the location is not free for the player, change the background color
            if(!state.Locations.get(location).isLocationFreeForPlayer(state)) {
                button.setBackground(Color.LIGHT_GRAY);
            }

            if(location == RedDestinationLocation.LOOKOUT_DESTINATION){
                //Copy Basic or Forest Location
                button.addActionListener(k ->{
                    locationPanel.removeAll();
                    activateCopyMode(copyLocation -> {
                        RedDestinationLocation.copyLocationChoice = copyLocation;

                        new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                        deactivateCopyMode();
                        everdellGUIManager.redrawPanels();
                        FunctionWrapper.activateNextFunction();
                    });
                    drawBasicLocationsButton(locationPanel);
                    drawForestLocationsButton(locationPanel);
                });
            }
            if(location == RedDestinationLocation.QUEEN_DESTINATION){
                //Place a card with 3 or less points
                button.addActionListener(k ->{
                   ArrayList<EverdellCard> cards = state.playerHands.get(state.getCurrentPlayer()).stream().filter(card -> card.getPoints() <= 3).collect(Collectors.toCollection(ArrayList::new));
                   cards.addAll(state.meadowDeck.stream().filter(card -> card.getPoints() <= 3).collect(Collectors.toCollection(ArrayList::new)));
                   drawPlayerCardsButtons(1,cards, "Play a card worth 3 points or less for free", card -> {
                       everdellGUIManager.cardSelection.add(card);
                       if(copyMode){
                           copyAction.accept(location);
                           everdellGUIManager.redrawPanels();
                           FunctionWrapper.addAFunction(() -> {
                               everdellGUIManager.placeACard(state, card);
                               return true;
                           }, "Queen Destination Action...", 0);
                           FunctionWrapper.activateNextFunction();
                       }
                       else {
                           new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                           everdellGUIManager.redrawPanels();
                           everdellGUIManager.placeACard(state, card);
                       }
                   });

                });
            }

            if(location == RedDestinationLocation.INN_DESTINATION){
                //Play a meadow card for 3 less resources
                button.addActionListener(k ->{
                    ArrayList<EverdellCard> cards = state.meadowDeck.stream().collect(Collectors.toCollection(ArrayList::new));
                    drawPlayerCardsButtons(1,cards, "Select a meadow card to play for 3 less resources", card -> {

                        int discountAmount = 3;

                        drawResourceSelection(discountAmount,"Select 3 Resources to Discount from the card", new ArrayList<>(List.of(ResourceTypes.values())), state -> {
                            everdellGUIManager.cardSelection.add(0, card);
                            EverdellLocation loc = state.Locations.get(location);
                            //Find the card that aligns with the location
                            for(var playerDeck : state.playerVillage){
                                for(var c : playerDeck.getComponents()){
                                    if(c instanceof InnCard ic){
                                        if(ic.location == loc){
                                            ic.setPlayers(state.getCurrentPlayer());
                                            break;
                                        }
                                    }
                                }
                            }

                            if(copyMode){
                                copyAction.accept(location);
                                everdellGUIManager.redrawPanels();
                                FunctionWrapper.addAFunction(() -> {
                                    everdellGUIManager.placeACard(state, card);
                                    return true;
                                },"Inn Destination Action...", 0);
                                FunctionWrapper.activateNextFunction();
                            }
                            else {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                                everdellGUIManager.placeACard(state, card);
                            }
                            return true;
                            });
                    });

                });
            }

            if(location == RedDestinationLocation.POST_OFFICE_DESTINATION){
                //Give 2 cards, discard any and draw to the limit
                button.addActionListener(k -> {

                    drawPlayerCardsButtons(state.playerHands.get(state.getCurrentPlayer()).getSize(), "Give 2 cards, discard any and draw to the limit", card -> {
                        everdellGUIManager.cardSelection.add(card);
                    });


                    JButton doneButton = new JButton("Done");
                    doneButton.addActionListener(k2 -> {
                        drawPlayerSelection(player -> {
                            EverdellLocation loc = state.Locations.get(location);
                            //Find the card that aligns with the location
                            for(var playerDeck : state.playerVillage){
                                for(var card : playerDeck.getComponents()){
                                    if(card instanceof PostOfficeCard poc){
                                        if(poc.location == loc){
                                            poc.setPlayers(player,state.getCurrentPlayer());
                                            break;
                                        }
                                    }
                                }
                            }
                            System.out.println("Location is "+loc.getLocation());

                            if(copyMode){
                                copyAction.accept(location);
                            }
                            else {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                            }
                        });
                    });

                    this.add(doneButton, BorderLayout.SOUTH);
                    });
                }
            if(location == RedDestinationLocation.MONASTERY_DESTINATION){
                //Give 2 resources, gain 4 points
                button.addActionListener(k -> {
                    drawResourceSelection(2,"Give 2 Resources, Gain 4 points", new ArrayList<>(List.of(ResourceTypes.values())), state -> {
                        drawPlayerSelection(player -> {
                            EverdellLocation loc = state.Locations.get(location);
                            //Find the card that aligns with the location
                            for(var playerDeck : state.playerVillage){
                                for(var card : playerDeck.getComponents()){
                                    if(card instanceof MonasteryCard mc){
                                        if(mc.location == loc){
                                            mc.setPlayers(player);
                                            break;
                                        }
                                    }
                                }
                            }

                            if(copyMode){
                                copyAction.accept(location);
                            }
                            else {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                            }
                        });
                        return true;
                    });

                });
            }
            if(location == RedDestinationLocation.CEMETERY_DESTINATION){
                //Reveal 4 Cards from the deck or the discard pile. Play 1 of them 1 free
                button.addActionListener(k -> {
                    ArrayList<EverdellCard> drawnCards = new ArrayList<>();

                    this.removeAll();

                    JPanel drawSelectionPanel = new JPanel();
                    drawSelectionPanel.setLayout(new GridLayout(1,2));
                    JButton drawFromDiscard = new JButton("Reveal from Discard");

                    Consumer<EverdellGameState> selectionAction = state -> {
                        drawPlayerCardsButtons(1, drawnCards, "Select 1 card to play for free",card -> {
                            everdellGUIManager.cardSelection.add(card);

                            //Add the rest of the cards to drawnCards
                            for(int i = 0; i < drawnCards.size(); i++){
                                if(everdellGUIManager.cardSelection.get(0) != drawnCards.get(i)){
                                    everdellGUIManager.cardSelection.add(drawnCards.get(i));
                                }
                            }

                            if(copyMode){
                                copyAction.accept(location);
                                everdellGUIManager.redrawPanels();
                                FunctionWrapper.addAFunction(() -> {
                                    everdellGUIManager.placeACard(state, card);
                                    return true;
                                }, "Cemetery Destination Action...", 0);
                                FunctionWrapper.activateNextFunction();
                            }
                            else {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                                everdellGUIManager.placeACard(state, card);
                            }
                        });
                    };

                    drawFromDiscard.addActionListener(k2->{
                        //Draw 4 cards
                        for(int i = 0; i < 4; i++){
                            if(state.discardDeck.getSize() > 0){
                                drawnCards.add(state.discardDeck.get(0));
                                state.discardDeck.remove(0);
                            }
                        }

                        selectionAction.accept(state);
                    });

                    JButton drawFromDeck = new JButton("Reveal from Deck");
                    drawFromDeck.addActionListener(k2 ->{
                        //Draw 4 cards
                        for(int i = 0; i < 4; i++){
                            if(state.cardDeck.getSize() > 0){
                                drawnCards.add(state.cardDeck.get(0));
                                state.cardDeck.remove(0);
                            }
                        }

                        selectionAction.accept(state);
                    });

                    drawSelectionPanel.add(drawFromDiscard);
                    drawSelectionPanel.add(drawFromDeck);

                    this.add(drawSelectionPanel, BorderLayout.CENTER);

                });
            }

            if(location == RedDestinationLocation.UNIVERSITY_DESTINATION){
                //Discard a village card, refund it, gain a resource and 1 point
                button.addActionListener(k -> {
                    //They must select a card that they want to discard

                    ArrayList<EverdellCard> villageCards = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(card -> card.getCardEnumValue() != EverdellParameters.CardDetails.UNIVERSITY).collect(Collectors.toCollection(ArrayList::new));

                    drawPlayerCardsButtons(1, villageCards, "Discard a village card, refund it, gain a resource and 1 point", card -> {
                        everdellGUIManager.cardSelection.add(card);

                        drawResourceSelection(1, "Select 1 Resource to Gain", new ArrayList<>(List.of(ResourceTypes.values())), state -> {
                            //Find University Card
                            boolean found = false;
                            for(var cardInDeck : state.playerVillage.get(state.getCurrentPlayer()).getComponents()){
                                if(cardInDeck.getCardEnumValue() == EverdellParameters.CardDetails.UNIVERSITY){
                                    found = true;
                                    if(copyMode){
                                        copyAction.accept(location);
                                    }
                                    else {
                                        new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                        everdellGUIManager.redrawPanels();
                                    }
                                    break;
                                }
                            }

                            //If the card is not found, there is a very big problem. The card HAS to be in the village
                            if(!found){
                                try {
                                    throw new Exception("University Card not found");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            return true;

                        });
                    });
                });
            }

            if(location == RedDestinationLocation.CHAPEL_DESTINATION){
                button.addActionListener(k -> {
                    if(copyMode){
                        copyAction.accept(location);
                    }
                    else {
                        new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                        everdellGUIManager.redrawPanels();
                    }
                });
            }


            locationPanel.add(button);
        }
        this.add(locationPanel,BorderLayout.CENTER);
    }

    private void drawForestLocations(){
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            displayForestLocations = false;
            draw();
        });
        this.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,state.Locations.size()));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : state.Locations.keySet()){
            //Only Taking in Forest Locations
            if(!(location instanceof ForestLocations)){
                continue;
            }


            JButton button = new JButton(location.name());
            //Is the location free
            if(!state.Locations.get(location).isLocationFreeForPlayer(state)) {
                button.setBackground(Color.LIGHT_GRAY);
            }

            ArrayList<EverdellParameters.ResourceTypes> rt = new ArrayList<>();
            rt.add(ResourceTypes.BERRY);
            rt.add(ResourceTypes.PEBBLE);
            rt.add(ResourceTypes.RESIN);
            rt.add(ResourceTypes.TWIG);

            if(location == ForestLocations.TWO_ANY || location == ForestLocations.TWO_CARDS_ONE_ANY){
                button.addActionListener(k -> {
                    //If the player is already on the location, return
                    if(state.Locations.get(location).playersOnLocation.contains(state.getCurrentPlayer()) && !copyMode) {return;}

                    int numOfResource = (location == ForestLocations.TWO_ANY) ? 2 : 1;

                    this.drawResourceSelection(numOfResource, "Select "+numOfResource+" Resources", rt, state -> {
                        if(copyMode){
                            copyAction.accept(location);
                            return true;
                        }
                        new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                        everdellGUIManager.redrawPanels();
                        return true;
                    });
                });
            }
            else if (location == ForestLocations.DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED || location == ForestLocations.DISCARD_UP_TO_THREE_GAIN_ONE_ANY_FOR_EACH_CARD_DISCARDED){
                button.addActionListener(k -> {
                    //If the player is already on the location, return
                    if(state.Locations.get(location).playersOnLocation.contains(state.getCurrentPlayer()) && !copyMode) {return;}

                    ForestLocations.cardChoices = new ArrayList<>();

                    JButton doneButton = new JButton("Discard Selected Cards");

                    if(location == ForestLocations.DISCARD_CARD_DRAW_TWO_FOR_EACH_DISCARDED){
                        this.drawPlayerCardsButtons(state.playerHands.get(state.getCurrentPlayer()).getSize(), "Discard Cards, Draw two for each one discarded", card -> {
                            ForestLocations.cardChoices.add(card);
                        });
                        doneButton.addActionListener(k2 -> {
                            if(copyMode){
                                copyAction.accept(location);
                            }
                            else{
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                            }
                        });
                    }
                    else{
                        this.drawPlayerCardsButtons(3, "Discard up to 3 cards", card -> {
                            ForestLocations.cardChoices.add(card);
                        });

                        doneButton.addActionListener(k2 -> {
                            drawResourceSelection(ForestLocations.cardChoices.size(),"Select "+ForestLocations.cardChoices.size()+" Resources", rt,
                                    game -> {
                                        if(copyMode){
                                            copyAction.accept(location);
                                            return true;
                                        }
                                        new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(game);
                                        everdellGUIManager.redrawPanels();
                                        return true;
                            });
                        });
                    }
                    this.add(doneButton, BorderLayout.SOUTH);
                });
            }
            else if (location == ForestLocations.COPY_BASIC_LOCATION_DRAW_CARD){
                button.addActionListener(k -> {
                    //If the player is already on the location, return
                    if(state.Locations.get(location).playersOnLocation.contains(state.getCurrentPlayer()) && !copyMode){return;}

                    this.remove(locationPanel);

                    drawBasicLocations(basicLocation -> {
                        if(copyMode){
                            copyAction.accept(location);
                        }
                        else{
                            ForestLocations.basicLocationChoice = (BasicLocations) basicLocation;
                            new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                            everdellGUIManager.redrawPanels();
                        }
                    });
                });
            }
            else if (location == ForestLocations.DRAW_TWO_MEADOW_CARDS_PLAY_ONE_DISCOUNT){
                button.addActionListener(k -> {
                    //If the player is already on the location, return
                    if(state.Locations.get(location).playersOnLocation.contains(state.getCurrentPlayer()) && !copyMode){return;}

                    this.remove(locationPanel);

                    ArrayList<EverdellCard> cards = state.meadowDeck.stream().collect(Collectors.toCollection(ArrayList::new));
                    drawPlayerCardsButtons(2,cards,"Select 2 Meadow Cards", card -> {

                        everdellGUIManager.cardSelection.add(card);
                    });

                    JButton doneButton = new JButton("Done");
                    doneButton.addActionListener(k2 -> {

                        drawPlayerCardsButtons(1, everdellGUIManager.cardSelection, "Select 1 Card to Play at a Discount",card -> {
                            int discountAmount = 1;
                            drawResourceSelection(discountAmount,"Select 1 Resource to Discount from the card", new ArrayList<>(List.of(ResourceTypes.values())), state -> {

                                System.out.println("Resource Selection: "+everdellGUIManager.resourceSelection);
                                ForestLocations.cardChoices.clear();
                                if(everdellGUIManager.cardSelection.get(0) == card){
                                    ForestLocations.cardChoices.add(everdellGUIManager.cardSelection.get(1));
                                }else{
                                    ForestLocations.cardChoices.add(everdellGUIManager.cardSelection.get(0));
                                }
                                everdellGUIManager.cardSelection.clear();
                                everdellGUIManager.cardSelection.add(0, card);
                                if(copyMode){
                                    copyAction.accept(location);
                                    everdellGUIManager.redrawPanels();
                                    FunctionWrapper.addAFunction(() -> {
                                        everdellGUIManager.placeACard(state, card);
                                        return true;
                                    },"Forest Location - Draw Two Cards, Play 1 Discount Action...", 0);
                                    FunctionWrapper.activateNextFunction();
                                }
                                else {
                                    new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                    everdellGUIManager.redrawPanels();
                                    everdellGUIManager.placeACard(state, card);
                                }

                                ForestLocations.cardChoices.clear();
                                return true;
                            });
                        });
                    });
                    this.add(doneButton, BorderLayout.SOUTH);

                });
            }
            else {
                button.addActionListener(k -> {
                    if(copyMode){
                        copyAction.accept(location);
                    }
                    else {
                        new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                        everdellGUIManager.redrawPanels();
                    }
                });
            }
            locationPanel.add(button);

        }
        this.add(locationPanel,BorderLayout.CENTER);
    }

    private void drawBasicEvents(){
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            displayBasicEvents = false;
            draw();
        });
        this.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,state.Locations.size()));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : state.Locations.keySet()){
            if(!(location instanceof BasicEvent)){
                continue;
            }
            JButton button = new JButton(location.name());
            if(!state.Locations.get(location).isLocationFreeForPlayer(state)) {
                button.setBackground(EverdellParameters.playerColour.get(state.Locations.get(location).playersOnLocation.get(0)));
            }

            button.addActionListener(k -> {
                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);

                everdellGUIManager.redrawPanels();
            });
            locationPanel.add(button);
        }
        this.add(locationPanel,BorderLayout.CENTER);
    }

    private void drawSpecialEvents(){
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            displaySpecialEvents = false;
            draw();
        });
        this.add(back,BorderLayout.SOUTH);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new GridLayout(2,2));

        //Adds a listener to each button that will run the function assigned to it
        for(var location : state.Locations.keySet()){
            if(!(location instanceof SpecialEvent)){
                continue;
            }

            JButton button = new JButton(location.name());
            if(!state.Locations.get(location).isLocationFreeForPlayer(state)) {
                button.setBackground(EverdellParameters.playerColour.get(state.Locations.get(location).playersOnLocation.get(0)));
            }
            //Button should not have any effect if the player is on the location
            if(!state.Locations.get(location).isPlayerOnLocation(state)) {
                if (location == SpecialEvent.CROAK_WARE_CURE) {
                    button.addActionListener(k -> {
                        if (SpecialEvent.CROAK_WARE_CURE.checkIfConditionMet.apply(state)) {
                            //Allow the player to select 2 cards to discard
                            drawPlayerCardsButtons(2, "Lose 2 Berries, Discard 2 Cards and Gain 6 points", card -> {
                                everdellGUIManager.cardSelection.add(card);
                            });
                            JButton doneButton = new JButton("Done");
                            doneButton.addActionListener(k2 -> {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                            });
                            this.add(doneButton, BorderLayout.SOUTH);
                        }

                    });
                }
                else if (location == SpecialEvent.TAX_RELIEF) {
                    button.addActionListener(k -> {
                        if (SpecialEvent.TAX_RELIEF.checkIfConditionMet.apply(state)) {
                            //This will trigger the green production event for all cards that require NO GUI interaction
                            new MoveSeason(new ArrayList<>()).productionEvent(state);
                            FunctionWrapper.addAFunction(() -> {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                                return false;
                            }, "Tax Relief Action...", 0);
                            FunctionWrapper.activateNextFunction(() -> {
                                everdellGUIManager.greenProductionEventGUI(state);
                                return true;
                            }, "Green Production Event...");
                        }

                    });
                }

                else if (location == SpecialEvent.CAPTURE_OF_THE_ACORN_THIEVES) {
                    button.addActionListener(k -> {
                        if (SpecialEvent.CAPTURE_OF_THE_ACORN_THIEVES.checkIfConditionMet.apply(state)) {
                            //Let the player select 2 critters from their city to remove
                            ArrayList<EverdellCard> critters = state.playerVillage.get(state.getCurrentPlayer()).stream().filter(card -> card instanceof CritterCard).collect(Collectors.toCollection(ArrayList::new));

                            drawPlayerCardsButtons(2, critters, "Select 2 Critters to Remove from your village, Gain 3 points for each 1", card -> {
                                everdellGUIManager.cardSelection.add(card);
                            });
                            JButton doneButton = new JButton("Done");
                            doneButton.addActionListener(k2 -> {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                            });
                            this.add(doneButton, BorderLayout.SOUTH);
                        }

                    });
                }

                else if (location == SpecialEvent.GRADUATION_OF_SCHOLARS) {
                    button.addActionListener(k -> {
                        if (SpecialEvent.GRADUATION_OF_SCHOLARS.checkIfConditionMet.apply(state)) {
                            //Let the player select 3 critters from their city to remove
                            ArrayList<EverdellCard> critters = state.playerHands.get(state.getCurrentPlayer()).stream().filter(card -> card instanceof CritterCard).collect(Collectors.toCollection(ArrayList::new));

                            drawPlayerCardsButtons(3, critters, "Select 3 Critters to Remove from your HAND, Gain 2 points for each 1", card -> {
                                everdellGUIManager.cardSelection.add(card);
                            });
                            JButton doneButton = new JButton("Done");
                            doneButton.addActionListener(k2 -> {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                            });
                            this.add(doneButton, BorderLayout.SOUTH);
                        }

                    });
                }

                else if (location == SpecialEvent.AN_EVENING_OF_FIREWORKS) {
                    button.addActionListener(k -> {
                        if (SpecialEvent.AN_EVENING_OF_FIREWORKS.checkIfConditionMet.apply(state)) {
                            //The player must select how many resources they want to give up
                            drawResourceSelection(3, "Give up 3 Twigs, Gain 2 points for each twig", new ArrayList<>(List.of(ResourceTypes.TWIG)), state -> {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                                return true;
                            });
                        }
                    });
                }
                else if (location == SpecialEvent.PERFORMER_IN_RESIDENCE) {
                    button.addActionListener(k -> {
                        if (SpecialEvent.PERFORMER_IN_RESIDENCE.checkIfConditionMet.apply(state)) {
                            //The player must select how many resources they want to give up
                            drawResourceSelection(3, "Give up 3 Berry, Gain 2 points for each Berry", new ArrayList<>(List.of(ResourceTypes.BERRY)), state -> {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                                return true;
                            });
                        }
                    });
                }
                else if (location == SpecialEvent.ANCIENT_SCROLLS_DISCOVERED) {
                    button.addActionListener(k -> {
                        if (SpecialEvent.ANCIENT_SCROLLS_DISCOVERED.checkIfConditionMet.apply(state)) {
                            int nCards = 5;
                            ArrayList<EverdellCard> drawnCards = new ArrayList<>();
                            for(int i = 0; i < nCards; i++) {
                                drawnCards.add(state.cardDeck.draw());
                            }
                            //Allow the player to select cards to keep, up to 5
                            drawPlayerCardsButtons(nCards, drawnCards, "Select up to 5 cards to keep, The rest are placed under the event, gain 1 point for each card under the event", card -> {
                                everdellGUIManager.cardSelection.add(card);
                            });
                            JButton doneButton = new JButton("Done");
                            doneButton.addActionListener(k2 -> {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                            });
                            this.add(doneButton, BorderLayout.SOUTH);
                        }
                    });
                }
                else if (location == SpecialEvent.UNDER_NEW_MANAGEMENT) {
                    button.addActionListener(k -> {
                        if (SpecialEvent.UNDER_NEW_MANAGEMENT.checkIfConditionMet.apply(state)) {
                            drawResourceSelection(3, "Give up 3 of any resource, Gain 1 points for each Twig/Berry, Gain 2 Points for each Pebble/Resin", new ArrayList<>(List.of(ResourceTypes.values())), state -> {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                                return true;
                            });
                        }
                    });
                }
                else if (location == SpecialEvent.PRISTINE_CHAPEL_CEILING) {
                    button.addActionListener(k -> {
                        if (SpecialEvent.PRISTINE_CHAPEL_CEILING.checkIfConditionMet.apply(state)) {
                            //Find Chapel Card
                            int numberOfPointsPlaced = 0;
                            int pointsChapelCardsStartWith = 2;
                            for(EverdellCard card : state.playerVillage.get(state.getCurrentPlayer())){
                                if(card.getCardEnumValue() == EverdellParameters.CardDetails.CHAPEL){
                                    numberOfPointsPlaced = card.getPoints()-pointsChapelCardsStartWith;
                                    break;
                                }
                            }
                            drawResourceSelection(numberOfPointsPlaced, "Gain "+numberOfPointsPlaced+" due to the number of points placed on the Chapel card", new ArrayList<>(List.of(ResourceTypes.values())), state -> {
                                new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                everdellGUIManager.redrawPanels();
                                return true;
                            });
                        }
                    });
                }
                else if (location == SpecialEvent.A_WELL_RUN_CITY) {
                    button.addActionListener(k -> {
                        SpecialEvent event = (SpecialEvent) location;
                        if (event.checkIfConditionMet.apply(state)) {
                            //Display all Locations that the player has occupied so that they can select 1 worker to bring back
                            ArrayList<EverdellLocation> locations = new ArrayList<>();
                            for (EverdellLocation loc : state.Locations.values()) {
                                if (loc.isPlayerOnLocation(state) && loc.getLocation() != RedDestinationLocation.CEMETERY_DESTINATION && loc.getLocation() != RedDestinationLocation.MONASTERY_DESTINATION) {
                                    locations.add(loc);
                                }
                            }
                            this.removeAll();
                            this.setLayout(new BorderLayout());
                            JLabel label = new JLabel("Select a location to bring a worker back from");
                            label.setHorizontalAlignment(SwingConstants.CENTER);
                            this.add(label, BorderLayout.NORTH);
                            JPanel workerSelectionPanel = new JPanel();
                            workerSelectionPanel.setLayout(new GridLayout(2, locations.size()/2));
                            for (EverdellLocation loc : locations) {
                                JButton workerButton = new JButton(loc.getLocation().name());
                                workerButton.addActionListener(k2 -> {
                                    event.selectedLocation = loc;
                                    new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                                    everdellGUIManager.redrawPanels();
                                });
                                workerSelectionPanel.add(workerButton);
                            }
                            this.add(workerSelectionPanel, BorderLayout.CENTER);


                        }
                    });
                }
                else if (location == SpecialEvent.A_BRILLIANT_MARKETING_PLAN) {
                    button.addActionListener(k -> {
                        SpecialEvent event = (SpecialEvent) location;
                        if (event.checkIfConditionMet.apply(state)) {
                            event.playersToGiveResources = new HashMap<>();
                            drawSelectingMultiplePlayersToGiveResourcesTo(location);
                        }
                    });
                }

                else if (location == SpecialEvent.PATH_OF_THE_PILGRIMS || location == SpecialEvent.MINISTERING_TO_MISCREANTS || location == SpecialEvent.REMEMBERING_THE_FALLEN || location == SpecialEvent.FLYING_DOCTOR_SERVICE || location == SpecialEvent.THE_EVERDELL_GAMES) {
                    button.addActionListener(k -> {
                        SpecialEvent event = (SpecialEvent) location;
                        if (event.checkIfConditionMet.apply(state)) {
                            new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
                            everdellGUIManager.redrawPanels();
                        }
                    });
                }

            }

            locationPanel.add(button);
        }
        this.add(locationPanel,BorderLayout.CENTER);
    }


    //This is specifically made for the Special Event A Brilliant Marketing Plan
    //This can be altered to be more general
    //But this is not required within the scope of this project
    private void drawSelectingMultiplePlayersToGiveResourcesTo(EverdellParameters.AbstractLocations location){
        //Need a menu to select a player
        this.removeAll();
        SpecialEvent event = (SpecialEvent) location;
        drawPlayerSelection(player -> {
            if(!event.playersToGiveResources.containsKey(player)){
                event.playersToGiveResources.put(player, new HashMap<>());
            }

            //The player must select a player to give resources to
            //
            drawResourceSelection(3, "Choose UP TO 3 TOTAL Resources to give to ANY player", new ArrayList<>(List.of(ResourceTypes.values())), state -> {
                if(event.playersToGiveResources.get(player).isEmpty()){
                    for(ResourceTypes resource : everdellGUIManager.resourceSelection.keySet()){
                        //event.playersToGiveResources.get(player).put(resource, everdellGUIManager.resourceSelection.get(resource).copy());
                        Counter c = new Counter();
                        c.increment(everdellGUIManager.resourceSelection.get(resource).getValue());
                        event.playersToGiveResources.get(player).put(resource, c);
                    }
                }
                everdellGUIManager.resetValues();
                drawSelectingMultiplePlayersToGiveResourcesTo(location);
                return true;
            });


        });
        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new GridLayout(1,2));
        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(k -> {
            System.out.println("EVENT TWIG : "+ event.playersToGiveResources.get(1).get(ResourceTypes.TWIG).getValue());
            new PlaceWorker(location, everdellGUIManager.cardSelection, everdellGUIManager.resourceSelection).execute(state);
            everdellGUIManager.redrawPanels();
        });
        JButton backButton = new JButton("Back");
        backButton.addActionListener(k -> {
            drawSpecialEvents();
        });
        navigationPanel.add(backButton);
        navigationPanel.add(doneButton);
        this.add(navigationPanel, BorderLayout.SOUTH);
    }



    private void drawPlayerSelectionPanel(){
        System.out.println("DRAWING PLAYER SELECTION PANEL");

        JButton playerButton;

        JPanel playerSelectionPanel = new JPanel();
        playerSelectionPanel.setLayout(new GridLayout(1,state.getNPlayers()));

        for(int i = 0; i < state.getNPlayers(); i++){
            if(i == state.getCurrentPlayer()){
                continue;
            }

            playerButton = new JButton("Player "+(i+1));
            int currentPlayer = i;
            playerButton.addActionListener(k -> {
                playerSelectionAction.accept(currentPlayer);
            });
            playerSelectionPanel.add(playerButton);
        }
        this.add(playerSelectionPanel,BorderLayout.CENTER);
    }

    private void drawResourceSelectionPanel(){
        JButton back = new JButton("Back");
        back.addActionListener(k -> {
            drawPlayerCards();
        });

        JPanel traversalPanel = new JPanel();
        traversalPanel.setLayout(new GridLayout(1,2));

        JPanel resourcePanel = new JPanel();


        JLabel resourceLabel = new JLabel(labelText);
        resourceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resourceLabel.setBackground(Color.WHITE);
        resourceLabel.setOpaque(true);

        Consumer<EverdellGameState> redraw = game -> {
            draw();
        };

        JPanel berryPanel = new JPanel();
        everdellGUIManager.drawResourceButtons(state,berryPanel, ResourceTypes.BERRY,new Color(160, 66, 239),redraw, numberOfResourceSelections);

        JPanel pebblePanel = new JPanel();
        everdellGUIManager.drawResourceButtons(state,pebblePanel,ResourceTypes.PEBBLE,new Color(162, 162, 151),redraw, numberOfResourceSelections);

        JPanel resinPanel = new JPanel();
        everdellGUIManager.drawResourceButtons(state,resinPanel,ResourceTypes.RESIN,new Color(250, 168, 79),redraw, numberOfResourceSelections);

        JPanel twigPanel = new JPanel();
        everdellGUIManager.drawResourceButtons(state,twigPanel,ResourceTypes.TWIG,new Color(145, 92, 52),redraw, numberOfResourceSelections);

        JButton doneButton = new JButton("Done");

        //Could make it more accessible by making this function work differently.
        //Could pass a boolean value to say whether selection is strict or not
        //Aka if the player can select less or if it has to be the exact amount
        doneButton.addActionListener(k -> {
            if(everdellGUIManager.resourceSelection.values().stream().mapToInt(Counter::getValue).sum() <= numberOfResourceSelections) {
                resourceButtonAction.apply(state);
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

        this.add(resourceLabel,BorderLayout.NORTH);
        this.add(resourcePanel,BorderLayout.CENTER);

        traversalPanel.add(back);
        traversalPanel.add(doneButton);
        this.add(traversalPanel,BorderLayout.SOUTH);
    }

    private void resetNavigation(){
        displayBasicLocations = false;
        displayForestLocations = false;
        displayBasicEvents = false;
        displayWorkerPlacement = false;
        displayResourceSelection = false;
        makeCardsButtons = false;
        displayCards = false;
        displayRedDestinations = false;
        displayPlayerSelection = false;
        displayHavenLocation = false;
        displayJourneyLocations = false;
        displaySpecialEvents = false;
    }

    //PUBLIC ACCESS METHODS

    public void drawResourceSelection(int numberOfResources, String labelText, ArrayList<ResourceTypes> allowedResources, Function<EverdellGameState,Boolean> resourceButtonAction){
        resetNavigation();
        displayResourceSelection = true;
        this.resourceButtonAction = resourceButtonAction;
        this.numberOfResourceSelections = numberOfResources;
        this.labelText = labelText;
        this.allowedResources = allowedResources;

        draw();
    }

    public void drawPlayerSelection(Consumer<Integer> playerSelectionAction){
        resetNavigation();
        displayPlayerSelection = true;

        this.playerSelectionAction = playerSelectionAction;

        draw();
    }

    public void drawWorkerPlacement(){
        resetNavigation();
        everdellGUIManager.resetValues();
        displayWorkerPlacement = true;

        draw();
    }

    public void drawPlayerCards(){
        resetNavigation();
        displayCards = true;
        labelText = "Player Cards";

        draw();
    }

    public void drawPlayerCardsButtons(int numberOfCardSelections, String title, Consumer<EverdellCard> cardButtonAction){
        this.cardButtonAction = cardButtonAction;
        this.numberOfCardSelections = numberOfCardSelections;
        this.cardsToDisplay = null;
        this.labelText = title;

        resetNavigation();

        displayCards = true;
        makeCardsButtons = true;
        draw();
    }
    public void drawPlayerCardsButtons(int numberOfCardSelections, ArrayList<EverdellCard> cardsToDisplay, String title, Consumer<EverdellCard> cardButtonAction){
        this.cardButtonAction = cardButtonAction;
        this.numberOfCardSelections = numberOfCardSelections;
        this.cardsToDisplay = cardsToDisplay;
        this.labelText = title;

        resetNavigation();

        displayCards = true;
        makeCardsButtons = true;
        draw();
    }

    public void activateCopyMode(Consumer<EverdellParameters.AbstractLocations> copyAction){
        copyMode = true;
        this.copyAction = copyAction;
    }

    public void deactivateCopyMode(){
        copyMode = false;
        copyAction = null;
    }

    public void redraw(){
        draw();
    }



}
