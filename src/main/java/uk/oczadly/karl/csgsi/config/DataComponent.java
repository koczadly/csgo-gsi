package uk.oczadly.karl.csgsi.config;

/**
 * Represents a retrievable set of game state information which the client can report on.
 */
public enum DataComponent {
    
    /**
     * Retrieve the history of map wins for this match.
     */
    MAP_ROUND_WINS("map_round_wins", false),
    
    /**
     * Retrieve information about the current match.
     */
    MAP("map", false),
    
    /**
     * Retrieve the current player's Steam ID.
     */
    PLAYER_ID("player_id", false),
    
    /**
     * Retrieve the scoreboard statistics for the current player.
     */
    PLAYER_MATCH_STATS("player_match_stats", false),
    
    /**
     * Retrieve state information about the current player.
     */
    PLAYER_STATE("player_state", false),
    
    /**
     * Retrieve the current player's weapon loadout and their states.
     */
    PLAYER_WEAPONS("player_weapons", false),
    
    /**
     * Retrieve positional information for the current player. Game client must be an observer to report this
     * information.
     */
    PLAYER_POSITION("player_position", true),
    
    /**
     * Retrieve additional information about the reporting game client.
     */
    PROVIDER("provider", false),
    
    /**
     * Retrieve details about the current round.
     */
    ROUND("round", false),
    
    /**
     * Retrieve a list of current grenades on the map. Game client must be an observer to report this information.
     */
    GRENADES("allgrenades", true),
    
    /**
     * Retrieve the Steam ID of each player on the server. Game client must be an observer to report this information.
     */
    PLAYERS_ID("allplayers_id", true),
    
    /**
     * Retrieve the scoreboard statistics for all players on the server. Game client must be an observer to report this
     * information.
     */
    PLAYERS_MATCH_STATS("allplayers_match_stats", true),
    
    /**
     * Retrieve positional information for all players on the server. Game client must be an observer to report this
     * information.
     */
    PLAYERS_POSITION("allplayers_position", true),
    
    /**
     * Retrieve state information for all players on the server. Game client must be an observer to report this
     * information.
     */
    PLAYERS_STATE("allplayers_state", true),
    
    /**
     * Retrieve the current weapon loadout for all players on the server. Game client must be an observer to report this
     * information.
     */
    PLAYERS_WEAPONS("allplayers_weapons", true),
    
    /**
     * Retrieve state information on the bomb. Game client must be an observer to report this information.
     */
    BOMB("bomb", true),
    
    /**
     * Retrieve timing information for the current round phase. Game client must be an observer to report this
     * information.
     */
    PHASE_COUNTDOWNS("phase_countdowns", true);
    
    
    String configName;
    boolean observerOnly;
    
    DataComponent(String configName, boolean observerOnly) {
        this.configName = configName;
        this.observerOnly = observerOnly;
    }
    
    
    /**
     * @return the native name of the setting used within the game client
     */
    public String getConfigName() {
        return configName;
    }
    
    /**
     * @return whether the data is only available for spectators/observers
     */
    public boolean isObserverOnly() {
        return observerOnly;
    }
    
}
