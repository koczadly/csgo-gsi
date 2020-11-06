package uk.oczadly.karl.csgsi.state;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import uk.oczadly.karl.csgsi.config.DataComponent;
import uk.oczadly.karl.csgsi.state.components.EnumValue;
import uk.oczadly.karl.csgsi.state.components.Team;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class MapState {
    
    @Expose @SerializedName("round_wins") @JsonAdapter(RoundOutcomeDeserializer.class)
    private List<EnumValue<RoundOutcome>> rounds = Collections.emptyList();
    
    @Expose @SerializedName("mode")
    private EnumValue<GameMode> mode;
    
    @Expose @SerializedName("name")
    private String name;
    
    @Expose @SerializedName("phase")
    private EnumValue<GamePhase> phase;
    
    @Expose @SerializedName("round")
    private short roundNum;
    
    @Expose @SerializedName("team_ct")
    private TeamStats ctStats;
    
    @Expose @SerializedName("team_t")
    private TeamStats tStats;
    
    @Expose @SerializedName("num_matches_to_win_series")
    private short seriesMatchesToWin;
    
    @Expose @SerializedName("current_spectators")
    private int spectatorCount;
    
    @Expose @SerializedName("souvenirs_total")
    private int souvenirDrops;
    
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP_ROUND_WINS}</li>
     * </ul>
     *
     * @return an ordered list of round outcomes
     */
    public List<EnumValue<RoundOutcome>> getRoundResults() {
        return rounds;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     * </ul>
     *
     * @return the current game mode being played
     */
    public EnumValue<GameMode> getMode() {
        return mode;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     * </ul>
     *
     * @return the name of the map (eg. de_dust2)
     */
    public String getName() {
        return name;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     * </ul>
     *
     * @return the current phase of the match
     */
    public EnumValue<GamePhase> getPhase() {
        return phase;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     * </ul>
     *
     * @return the current round number
     */
    public short getRoundNumber() {
        return roundNum;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     * </ul>
     *
     * @return the statistics and data relating to the counter-terrorist team
     */
    public TeamStats getCounterTerroristStatistics() {
        return ctStats;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     * </ul>
     *
     * @return the statistics and data relating to the terrorist team
     */
    public TeamStats getTerroristStatistics() {
        return tStats;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     * </ul>
     *
     * @return the number of matches needed to win the series
     */
    public short getSeriesMatchesToWin() {
        return seriesMatchesToWin;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     * </ul>
     *
     * @return the number of spectators watching the match
     */
    public int getSpectatorCount() {
        return spectatorCount;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     * </ul>
     *
     * @return the number of souvenir drops this match
     */
    public int getSouvenirDrops() {
        return souvenirDrops;
    }
    
    
    static class RoundOutcomeDeserializer implements JsonDeserializer<List<EnumValue<RoundOutcome>>> {
        @Override
        public List<EnumValue<RoundOutcome>> deserialize(JsonElement json, Type typeOfT,
                                                         JsonDeserializationContext context) throws JsonParseException {
            TreeMap<Integer, EnumValue<RoundOutcome>> map = context.deserialize(json,
                    new TypeToken<TreeMap<Integer, EnumValue<RoundOutcome>>>() {}.getType());
            
            return map != null
                    ? Collections.unmodifiableList(new ArrayList<>(map.values()))
                    : Collections.emptyList();
        }
    }
    
    public enum RoundOutcome {
        /**
         * Represents a T win from the bomb exploding.
         */
        @SerializedName("t_win_bomb")         T_BOMB_EXPLODE(Team.TERRORIST),
        
        /**
         * Represents a T win from elimination of the opposing team.
         */
        @SerializedName("t_win_elimination")  T_ELIMINATION(Team.TERRORIST),
        
        /**
         * Represents a T win from the timer reaching zero.
         */
        @SerializedName("t_win_time")         T_TIME(Team.TERRORIST),
        
        /**
         * Represents a CT win from elimination of the opposing team.
         */
        @SerializedName("ct_win_elimination") CT_ELIMINATION(Team.COUNTER_TERRORIST),
        
        /**
         * Represents a CT win from the timer reaching zero.
         */
        @SerializedName("ct_win_time")        CT_TIME(Team.COUNTER_TERRORIST),
        
        /**
         * Represents a CT win from a bomb defuse.
         */
        @SerializedName("ct_win_defuse")      CT_DEFUSE(Team.COUNTER_TERRORIST),
        
        /**
         * Represents a CT win from a hostage rescue.
         */
        @SerializedName("ct_win_rescue")      CT_RESCUE(Team.COUNTER_TERRORIST),
        
        /**
         * Represents a skipped round with no winner.
         */
        @SerializedName("")                   SKIPPED(null);
        
        
        private final Team winningTeam;
        
        RoundOutcome(Team winningTeam) {
            this.winningTeam = winningTeam;
        }
        
        
        /**
         * @return the winning team, or null if the round was skipped
         */
        public Team getWinningTeam() {
            return winningTeam;
        }
    }
    
    public enum GameMode {
        /** The 5v5 ranked competitive game mode. */
        @SerializedName("competitive") COMPETITIVE,
        
        /** The casual 10v10 game mode. */
        @SerializedName("casual") CASUAL,
        
        /** The free-for-all game mode. */
        @SerializedName("deathmatch") DEATHMATCH,
        
        /** One of the 'war games' modes. */
        @SerializedName("skirmish") WAR_GAMES,
        
        /** Battle-royale danger zone mode. */
        @SerializedName("survival") DANGER_ZONE,
        
        /** A 2 vs 2 competitive game. */
        @SerializedName("scrimcomp2v2") WINGMAN_2v2
    }
    
    public enum GamePhase {
        /** Represents a currently in-progress game. */
        @SerializedName("live") LIVE,
        
        /** Represents a concluded game. */
        @SerializedName("gameover") GAME_OVER,
        
        /** Represents the pre-game warmup stage. */
        @SerializedName("warmup") WARMUP,
        
        /** Represents a paused state of the game. */
        @SerializedName("intermission") INTERMISSION
    }
    
    public static class TeamStats {
        @Expose @SerializedName("score")
        private short score;
        
        @Expose @SerializedName("consecutive_round_losses")
        private short consecutiveLosses;
        
        @Expose @SerializedName("timeouts_remaining")
        private short timeoutsRemaining;
        
        @Expose @SerializedName("matches_won_this_series")
        private short seriesMatchesWon;
        
        
        /**
         * @return the current number of round wins for this team
         */
        public short getScore() {
            return score;
        }
        
        /**
         * @return the number of consecutive losses for this team
         */
        public short getConsecutiveLosses() {
            return consecutiveLosses;
        }
        
        /**
         * @return the number of timeouts remaining which this team can use
         */
        public short getTimeoutsRemaining() {
            return timeoutsRemaining;
        }
        
        /**
         * @return the number of matches won by the team this series
         */
        public short getSeriesMatchesWon() {
            return seriesMatchesWon;
        }
    }
    
}
