package pandemic;

import core.GameParameters;

import java.util.HashMap;

public class PandemicParameters extends GameParameters {

    private String dataPath;
    public String getDataPath(){return dataPath;}

    long game_seed = System.currentTimeMillis(); //0;
    int lose_max_outbreak = 8;

    int max_cubes_per_city = 3;  // More cause outbreak

    int n_epidemic_cards = 4;
    int n_cubes_epidemic = 3;

    int[] infection_rate = new int[]{2, 2, 2, 3, 3, 4, 4};  // How many cards are drawn for each counter
    int n_infection_cards_setup = 3;
    int n_infections_setup = 3;
    int n_cubes_infection = 1;
    int n_initial_disease_cubes = 24;
    int n_cards_for_cure = 5;
    int n_cards_for_cure_reduced = 4;

    int n_players = 4;
    int max_cards_per_player = 7;  // Max cards in hand per player
    int n_cards_draw = 2;  // Number of cards players draw each turn

    // Number of cards each player receives.
    HashMap<Integer, Integer> n_cards_per_player = new HashMap<Integer, Integer>() {  // Mapping n_players : n_cards_per_player
        {
            put(2, 4);
            put(3, 3);
            put(4, 2);
        }
    };
    int n_actions_per_turn = 4;
    int n_research_stations = 6;

    protected PandemicParameters(int nPlayers, String dataPath) {
        super(nPlayers);
        this.dataPath = dataPath;
    }

    public PandemicParameters(PandemicParameters pandemicParameters) {
        this(pandemicParameters.nPlayers, pandemicParameters.dataPath);

        this.game_seed = pandemicParameters.game_seed;
        this.lose_max_outbreak = pandemicParameters.lose_max_outbreak;
        this.max_cubes_per_city = pandemicParameters.max_cubes_per_city;  // More cause outbreak
        this.n_epidemic_cards = pandemicParameters.n_epidemic_cards;
        this.n_cubes_epidemic = pandemicParameters.n_cubes_epidemic;
        this.n_infection_cards_setup = pandemicParameters.n_infection_cards_setup;
        this.n_infections_setup = pandemicParameters.n_infections_setup;
        this.n_cubes_infection = pandemicParameters.n_cubes_infection;
        this.n_initial_disease_cubes = pandemicParameters.n_initial_disease_cubes;
        this.n_cards_for_cure = pandemicParameters.n_cards_for_cure;
        this.n_cards_for_cure_reduced = pandemicParameters.n_cards_for_cure_reduced;
        this.n_players = pandemicParameters.n_players;
        this.max_cards_per_player = pandemicParameters.max_cards_per_player;  // Max cards in hand per player
        this.n_cards_draw = pandemicParameters.n_cards_draw;  // Number of cards players draw each turn
        this.n_actions_per_turn = pandemicParameters.n_actions_per_turn;
        this.n_research_stations = pandemicParameters.n_research_stations;

        // How many cards are drawn for each counter
        this.infection_rate = new int[infection_rate.length];
        System.arraycopy(infection_rate, 0, pandemicParameters.infection_rate, 0, infection_rate.length);

        // Number of cards each player receives.
        this.n_cards_per_player = new HashMap<>();
        for(int key : pandemicParameters.n_cards_per_player.keySet())
            this.n_cards_per_player.put(key, pandemicParameters.n_cards_per_player.get(key));
    }
}