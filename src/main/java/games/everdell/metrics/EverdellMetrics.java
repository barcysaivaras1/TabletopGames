package games.everdell.metrics;

import core.*;
import core.actions.AbstractAction;
import core.components.Deck;
import core.interfaces.IComponentContainer;
import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import evaluation.metrics.IMetricsCollection;
import evaluation.summarisers.TAGStatSummary;
import evaluation.summarisers.TAGSummariser;
import games.everdell.EverdellGameState;
import games.everdell.EverdellParameters;
import games.everdell.actions.EndGame;
import games.everdell.actions.PlayCard;
import games.everdell.components.EverdellCard;
import games.everdell.components.EverdellLocation;
import utilities.Hash;
import utilities.Pair;

import java.util.*;

import static evaluation.metrics.Event.GameEvent.*;

@SuppressWarnings("unused")
public class EverdellMetrics implements IMetricsCollection {
    public static class EndScore extends AbstractMetric {

        @Override
        public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {

            for (int i = 0; i < e.state.getNPlayers(); i++) {
                int playerId = i;
                System.out.println("Player Metrics: " + playerId);
                EverdellGameState egs = (EverdellGameState) e.state;

                System.out.println("Player " + playerId + " score: " + egs.score[playerId]);
                records.put("Player-" + playerId, egs.score[playerId]);
            }
            return true;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return new HashSet<>(Arrays.asList(GAME_OVER));
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            for (int i = 0; i < nPlayersPerGame; i++) {
                columns.put("Player-" + i, Integer.class);
            }
            return columns;
        }
    }

    public static class CardsPlayedInSeason extends AbstractMetric {

        HashMap<EverdellParameters.Seasons, HashMap<EverdellParameters.CardDetails, Integer>> cardsPlayed = new HashMap<>();
        HashMap<Integer, Deck<EverdellCard>> playerVillages = null;

        @Override
        public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {

            int playerId = e.playerID;
            EverdellGameState egs = (EverdellGameState) e.state;

            if (playerVillages == null) {
                playerVillages = new HashMap<>();
                for (int i = 0; i < e.state.getNPlayers(); i++) {
                    playerVillages.put(i, egs.playerVillage.get(i).copy());
                }
            }

            //Add all seasons to map
            if(cardsPlayed.isEmpty()) {
                for (var season : EverdellParameters.Seasons.values()) {
                    cardsPlayed.put(season, new HashMap<>());
                }
            }

            //Check if the village we have stored now is different from the one in the game state
            if(e.type == ACTION_TAKEN) {
                if (!egs.playerVillage.get(playerId).equals(playerVillages.get(playerId))) {
                    //We need to add the card to the map
                    EverdellParameters.Seasons season = egs.currentSeason[playerId];
                    for (var card : egs.playerVillage.get(playerId).getComponents()) {
                        //If the card was just played, we want to add it to the map
                        if (!playerVillages.get(playerId).contains(card)) {
                            cardsPlayed.get(season).computeIfAbsent(card.getCardEnumValue(), k -> 0);
                            cardsPlayed.get(season).put(card.getCardEnumValue(), cardsPlayed.get(season).get(card.getCardEnumValue()) + 1);
                        }
                    }
                    //If it is different, then we need to update the player village
                    playerVillages.put(playerId, egs.playerVillage.get(playerId).copy());
                }
            }
            if(e.type == GAME_OVER) {
                for (var season : EverdellParameters.Seasons.values()) {
                    StringBuilder cards = new StringBuilder("(");
                    for (var card : cardsPlayed.get(season).keySet()) {
                        cards.append(card).append(":").append(cardsPlayed.get(season).get(card)).append(", ");
                    }
                    //delete the last comma
                    if (cards.length() > 1) {
                        cards.delete(cards.length() - 2, cards.length());
                    }
                    cards.append(")");
                    records.put(season + "-CardsPlayed", cards.toString());
                }
                playerVillages = null;
                cardsPlayed = new HashMap<>();
                return true;
            }
            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return new HashSet<IGameEvent>() {{
                add(Event.GameEvent.GAME_OVER);
                add(Event.GameEvent.ACTION_TAKEN);
            }};
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            EverdellParameters.Seasons [] seasonOrder = {EverdellParameters.Seasons.WINTER, EverdellParameters.Seasons.SPRING, EverdellParameters.Seasons.SUMMER, EverdellParameters.Seasons.AUTUMN};
            for (var season : seasonOrder) {
                columns.put(season + "-CardsPlayed", String.class);
            }
            return columns;
        }
    }

    public static class CardTypesPlayedInSeason extends AbstractMetric {

        HashMap<EverdellParameters.Seasons, HashMap<EverdellParameters.CardType, Integer>> cardTypesPlayed = new HashMap<>();
        HashMap<Integer, Deck<EverdellCard>> playerVillages = null;

        @Override
        public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {

            int playerId = e.playerID;
            EverdellGameState egs = (EverdellGameState) e.state;

            if (playerVillages == null) {
                playerVillages = new HashMap<>();
                for (int i = 0; i < e.state.getNPlayers(); i++) {
                    playerVillages.put(i, egs.playerVillage.get(i).copy());
                }
            }

            //Add all seasons to map
            if(cardTypesPlayed.isEmpty()) {
                for (var season : EverdellParameters.Seasons.values()) {
                    cardTypesPlayed.put(season, new HashMap<>());
                }
            }

            //Check if the village we have stored now is different from the one in the game state
            if(e.type == ACTION_TAKEN) {
                if (!egs.playerVillage.get(playerId).equals(playerVillages.get(playerId))) {
                    //We need to add the card to the map
                    EverdellParameters.Seasons season = egs.currentSeason[playerId];
                    for (var card : egs.playerVillage.get(playerId).getComponents()) {
                        //If the card was just played, we want to add it to the map
                        if (!playerVillages.get(playerId).contains(card)) {
                            cardTypesPlayed.get(season).computeIfAbsent(card.getCardType(), k -> 0);
                            cardTypesPlayed.get(season).put(card.getCardType(), cardTypesPlayed.get(season).get(card.getCardType()) + 1);
                        }
                    }
                    //If it is different, then we need to update the player village
                    playerVillages.put(playerId, egs.playerVillage.get(playerId).copy());
                }
            }
            if(e.type == GAME_OVER) {
                for (var season : EverdellParameters.Seasons.values()) {
                    StringBuilder cards = new StringBuilder("(");
                    for (var card : cardTypesPlayed.get(season).keySet()) {
                        cards.append(card).append(":").append(cardTypesPlayed.get(season).get(card)).append(", ");
                    }
                    //delete the last comma
                    if (cards.length() > 1) {
                        cards.delete(cards.length() - 2, cards.length());
                    }
                    cards.append(")");
                    records.put(season + "-CardTypesPlayed", cards.toString());
                }
                playerVillages = null;
                cardTypesPlayed = new HashMap<>();
                return true;
            }
            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return new HashSet<IGameEvent>() {{
                add(Event.GameEvent.GAME_OVER);
                add(Event.GameEvent.ACTION_TAKEN);
            }};
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            EverdellParameters.Seasons [] seasonOrder = {EverdellParameters.Seasons.WINTER, EverdellParameters.Seasons.SPRING, EverdellParameters.Seasons.SUMMER, EverdellParameters.Seasons.AUTUMN};
            for (var season : seasonOrder) {
                columns.put(season + "-CardTypesPlayed", String.class);
            }
            return columns;
        }
    }

    public static class OrderOfPassingSeasons extends AbstractMetric {

        HashMap<EverdellParameters.Seasons, ArrayList<Integer>> orderOfPassingSeasons = new HashMap<>();
        ArrayList<EverdellParameters.Seasons> playerSeasons = new ArrayList<>();

        @Override
        public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {

            int playerId = e.playerID;
            EverdellGameState egs = (EverdellGameState) e.state;

            if (playerSeasons.isEmpty()) {
                for(int i=0; i < e.state.getNPlayers(); i++) {
                    playerSeasons.add(egs.currentSeason[i]);
                }
            }
            if(orderOfPassingSeasons.isEmpty()) {
                for (var season : EverdellParameters.Seasons.values()) {
                    if (season == EverdellParameters.Seasons.WINTER) continue;
                    orderOfPassingSeasons.put(season, new ArrayList<>());
                }
            }

            if(e.type == ACTION_TAKEN) {
                //We check if the player has passed the season
                if (egs.currentSeason[playerId] != playerSeasons.get(playerId)) {
                    //We add the player to the list of players that passed the season
                    EverdellParameters.Seasons season = egs.currentSeason[playerId];
                    orderOfPassingSeasons.get(season).add(playerId);
                    //We update the player season
                    playerSeasons.set(playerId, season);
                }
            }
            if(e.type == GAME_OVER) {
                for (var season : EverdellParameters.Seasons.values()) {
                    if (season == EverdellParameters.Seasons.WINTER) continue;
                    StringBuilder players = new StringBuilder("(");
                    for (var player : orderOfPassingSeasons.get(season)) {
                        players.append(player).append(", ");
                    }
                    //delete the last comma
                    if (players.length() > 1) {
                        players.delete(players.length() - 2, players.length());
                    }
                    players.append(")");
                    records.put(season + "-OrderOfSeasonPass", players.toString());
                }
                playerSeasons = new ArrayList<>();
                orderOfPassingSeasons = new HashMap<>();
                return true;
            }

            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return new HashSet<IGameEvent>() {{
                add(Event.GameEvent.GAME_OVER);
                add(Event.GameEvent.ACTION_TAKEN);
            }};
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            EverdellParameters.Seasons [] seasonOrder = {EverdellParameters.Seasons.SPRING, EverdellParameters.Seasons.SUMMER, EverdellParameters.Seasons.AUTUMN};
            for (var season : seasonOrder) {
                columns.put(season + "-OrderOfSeasonPass", String.class);
            }
            return columns;
        }
    }


    //This aims to track the number of instances a resource was gathered, NOT the amount of resources
    //An example is : Visiting Location 3 WOOD would count as 1 instance. Using location TWO_ANY, if they select 1 WOOD and 1 PEBBLE
    //it would count as 1 instance of WOOD and 1 instance of PEBBLE
    public static class NumberOfInstancesOfGatheringResourcesPerSeason extends AbstractMetric {

        HashMap<EverdellParameters.Seasons, HashMap<EverdellParameters.ResourceTypes, Integer>> resourcesGatheredEachSeason = new HashMap<>();
        HashMap<EverdellParameters.ResourceTypes, ArrayList<Integer>> playerResourcesPrev = new HashMap<>();

        @Override
        public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {

            int playerId = e.playerID;
            EverdellGameState egs = (EverdellGameState) e.state;

            if (playerResourcesPrev.isEmpty()) {
                for (var resource : EverdellParameters.ResourceTypes.values()) {
                    playerResourcesPrev.put(resource, new ArrayList<>());
                    for (int i = 0; i < egs.getNPlayers(); i++) {
                        playerResourcesPrev.get(resource).add(0);
                    }
                }
            }

            //Add all seasons to map
            if(resourcesGatheredEachSeason.isEmpty()) {
                for (var season : EverdellParameters.Seasons.values()) {
                    resourcesGatheredEachSeason.put(season, new HashMap<>());
                    //Add all resources to map
                    for (var resource : EverdellParameters.ResourceTypes.values()) {
                        resourcesGatheredEachSeason.get(season).put(resource, 0);
                    }
                }
            }

            if(e.type == ACTION_TAKEN){
                //Check if a resource has been gathered this action, if so, update the map
                for (var resource : EverdellParameters.ResourceTypes.values()) {
                    //Check if the resource has been gathered
                    if (egs.PlayerResources.get(resource)[playerId].getValue() > playerResourcesPrev.get(resource).get(playerId)) {
                        //Increment the counter in the map for that resource
                        EverdellParameters.Seasons season = egs.currentSeason[playerId];
                        resourcesGatheredEachSeason.get(season).put(resource, resourcesGatheredEachSeason.get(season).get(resource) + 1);

                        //Update the previous resources list
                        playerResourcesPrev.get(resource).set(playerId, egs.PlayerResources.get(resource)[playerId].getValue());
                    }
                }
            }

            if(e.type == GAME_OVER) {
                for (var season : EverdellParameters.Seasons.values()) {
                    StringBuilder resources = new StringBuilder("(");
                    for (var resource : resourcesGatheredEachSeason.get(season).keySet()) {
                        resources.append(resource).append(":").append(resourcesGatheredEachSeason.get(season).get(resource)).append(", ");
                    }
                    //delete the last comma
                    if (resources.length() > 1) {
                        resources.delete(resources.length() - 2, resources.length());
                    }
                    resources.append(")");
                    records.put(season + "-ResourcesGathered", resources.toString());
                }
                playerResourcesPrev = new HashMap<>();
                resourcesGatheredEachSeason = new HashMap<>();
                return true;
            }


            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return new HashSet<IGameEvent>() {{
                add(Event.GameEvent.GAME_OVER);
                add(Event.GameEvent.ACTION_TAKEN);
            }};
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            for (var season : EverdellParameters.Seasons.values()) {
                columns.put(season + "-ResourcesGathered", String.class);
            }
            return columns;
        }
    }

    public static class LocationsPlayedInSeason extends AbstractMetric {

        HashMap<EverdellParameters.Seasons, HashMap<EverdellParameters.AbstractLocations, Integer>> locationsPlayed = new HashMap<>();
        ArrayList<EverdellLocation> everdellLocationsPrev = new ArrayList<>();

        @Override
        public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {

            int playerId = e.playerID;
            EverdellGameState egs = (EverdellGameState) e.state;

            if (everdellLocationsPrev.isEmpty()) {
                for (var location : egs.everdellLocations) {
                    everdellLocationsPrev.add(location.copy());
                }
            }

            //Add all seasons to map
            if(locationsPlayed.isEmpty()) {
                for (var season : EverdellParameters.Seasons.values()) {
                    locationsPlayed.put(season, new HashMap<>());
                }
            }

            //Check if a worker has been placed this action, if so, update the map
            if(e.type == ACTION_TAKEN) {
                EverdellParameters.Seasons season = egs.currentSeason[playerId];
                for (var location : egs.everdellLocations) {
                    EverdellLocation locationInPrev = getLocationFromPrevById(location);

                    if(locationInPrev == null)continue;

                    //Check if a worker has been placed on a location
                    if(locationInPrev.getPlayersOnLocation().size() < location.getPlayersOnLocation().size()) {
                        //Increment the counter in the map for that location
                        locationsPlayed.get(season).computeIfAbsent(location.getAbstractLocation(), k -> 0);
                        locationsPlayed.get(season).put(location.getAbstractLocation(), locationsPlayed.get(season).get(location.getAbstractLocation()) + 1);

                        //Update the previous locations list
                        everdellLocationsPrev.remove(locationInPrev);
                        everdellLocationsPrev.add(location.copy());
                    }
                    else{
                        everdellLocationsPrev.remove(locationInPrev);
                        everdellLocationsPrev.add(location.copy());
                    }
                }
            }
            if(e.type == GAME_OVER) {
//                System.out.println("Locations played in each season in GAMEOVER");
//                //Printout Locations played in each season
//                for(var season : EverdellParameters.Seasons.values()) {
//                    System.out.println("Season: " + season);
//                    for (var location : locationsPlayed.get(season).keySet()) {
//                        System.out.println("Location: " + location + " - " + locationsPlayed.get(season).get(location));
//                    }
//                }
                for (var season : EverdellParameters.Seasons.values()) {
                    StringBuilder locString = new StringBuilder("(");
                    for (var location : locationsPlayed.get(season).keySet()) {
                        locString.append(location).append(":").append(locationsPlayed.get(season).get(location)).append(", ");
                    }
                    //delete the last comma
                    if (locString.length() > 1) {
                        locString.delete(locString.length() - 2, locString.length());
                    }
                    locString.append(")");
                    records.put(season + "-LocationsPlayed", locString.toString());
                }
                locationsPlayed = new HashMap<>();
                everdellLocationsPrev = new ArrayList<>();
                return true;
            }
            return false;
        }

        public EverdellLocation getLocationFromPrevById(EverdellLocation location) {
            int id = location.getComponentID();
            for (var locationPrev : everdellLocationsPrev) {
                if (locationPrev.getComponentID() == id) {
                    return locationPrev;
                }
            }

            //If we don't find the location, it must be a red destination location
            //We add it to the list and return it
            EverdellLocation locationCopy = location.copy();
            everdellLocationsPrev.add(locationCopy);

            return locationCopy;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return new HashSet<IGameEvent>() {{
                add(Event.GameEvent.GAME_OVER);
                add(Event.GameEvent.ACTION_TAKEN);
            }};
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            EverdellParameters.Seasons [] seasonOrder = {EverdellParameters.Seasons.WINTER, EverdellParameters.Seasons.SPRING, EverdellParameters.Seasons.SUMMER, EverdellParameters.Seasons.AUTUMN};
            for (var season : seasonOrder) {
                columns.put(season + "-LocationsPlayed", String.class);
            }
            return columns;
        }
    }


    public static class VillageAtEnd extends AbstractMetric {

        @Override
        public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {


            for(int i = 0; i < e.state.getNPlayers(); i++) {
                int playerId = i;
                System.out.println("Player Metrics: " + playerId);
                EverdellGameState egs = (EverdellGameState) e.state;

                StringBuilder village = new StringBuilder("(");

                for (var card : egs.playerVillage.get(playerId).getComponents()) {
                    village.append("'");
                    village.append(card.getCardEnumValue()).append("'").append(", ");
                }
                //delete the last comma
                if (village.length() > 1) {
                    village.delete(village.length() - 2, village.length());
                }
                village.append(")");
                records.put("Village-"+playerId, village.toString());
            }
            return true;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return new HashSet<>(Arrays.asList(GAME_OVER));
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            for (int i = 0; i < nPlayersPerGame; i++) {
                columns.put("Village-"+ i, String.class);
            }
            return columns;
        }
    }

    public static class LocationsAtEnd extends AbstractMetric {


        @Override
        public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {


            EverdellGameState egs = (EverdellGameState) e.state;
            StringBuilder locations = new StringBuilder("(");
            for (var location : egs.everdellLocations) {
                locations.append(location.getAbstractLocation()).append("-");
                locations.append(location.getPlayersOnLocation()).append(", ");
            }
            //delete the last comma
            if (locations.length() > 1) {
                locations.delete(locations.length() - 2, locations.length());
            }
            records.put("Locations", locations.toString());
            return true;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return new HashSet<>(Arrays.asList(GAME_OVER));
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            columns.put("Locations", String.class);
            return columns;
        }
    }

}