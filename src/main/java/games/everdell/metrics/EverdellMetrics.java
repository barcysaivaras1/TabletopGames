package games.everdell.metrics;

import core.*;
import core.actions.AbstractAction;
import core.interfaces.IComponentContainer;
import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import evaluation.metrics.IMetricsCollection;
import evaluation.summarisers.TAGStatSummary;
import evaluation.summarisers.TAGSummariser;
import games.everdell.EverdellGameState;
import games.everdell.actions.EndGame;
import utilities.Pair;

import java.util.*;

import static evaluation.metrics.Event.GameEvent.*;

@SuppressWarnings("unused")
public class EverdellMetrics implements IMetricsCollection {
    public static class Score extends AbstractMetric {

        private HashMap<Integer, Integer> score = new HashMap<>();

        @Override
        public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {

            int playerId = e.playerID;

            if(e.action instanceof EndGame) {
                System.out.println("Player Metrics: " + playerId);
                EverdellGameState egs = (EverdellGameState) e.state;
                score.put(playerId, egs.score[playerId]);

                System.out.println("Player " + playerId + " score: " + egs.score[playerId]);


                records.put("Player-" + playerId, score.get(playerId));
                return true;
            }

            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return new HashSet<>(Arrays.asList(ACTION_TAKEN));
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            for (int i = 0; i < nPlayersPerGame; i++) {
                columns.put("Player-" + i, Integer.class);
            }
            columns.put("Score", Integer.class);
            return columns;
        }
    }

}