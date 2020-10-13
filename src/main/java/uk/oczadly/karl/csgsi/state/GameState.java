package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.config.DataComponent;

import java.util.Map;

public class GameState {
    
    @Expose @SerializedName("provider")
    private ProviderState providerDetails;
    
    @Expose @SerializedName("map")
    private MapState mapState;
    
    @Expose @SerializedName("player")
    private PlayerState playerState;
    
    @Expose @SerializedName("round")
    private RoundState roundState;
    
    @Expose @SerializedName("grenades")
    private Map<Integer, GrenadeState> grenadeStates;
    
    @Expose @SerializedName("allplayers")
    private Map<String, PlayerState> playerStates;
    
    @Expose @SerializedName("bomb")
    private BombState bombState;
    
    @Expose @SerializedName("phase_countdowns")
    private PhaseCountdownState phaseCountdownState;
    
    
    /**
     * Returns a list of information about the game client which reported the state.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PROVIDER}</li>
     * </ul>
     *
     * @return the game client provider information, or null if not sent
     */
    public ProviderState getProviderDetails() {
        return providerDetails;
    }
    
    /**
     * Returns details and statistics on the current map.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#MAP}</li>
     *     <li>{@link DataComponent#MAP_ROUND_WINS}</li>
     * </ul>
     *
     * @return the current map state data, or null if not sent
     */
    public MapState getMapState() {
        return mapState;
    }
    
    /**
     * Returns information about the current player, or the player being observed if spectating.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_ID}</li>
     *     <li>{@link DataComponent#PLAYER_MATCH_STATS}</li>
     *     <li>{@link DataComponent#PLAYER_POSITION} (spectating only)</li>
     *     <li>{@link DataComponent#PLAYER_STATE}</li>
     *     <li>{@link DataComponent#PLAYER_WEAPONS}</li>
     * </ul>
     *
     * @return the current player's state data, or null if not sent
     */
    public PlayerState getPlayerState() {
        return playerState;
    }
    
    /**
     * Returns information about the current ongoing round.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#ROUND}</li>
     * </ul>
     *
     * @return the current round's state data, or null if not sent
     */
    public RoundState getRoundState() {
        return roundState;
    }
    
    /**
     * Returns a list of grenades on the map. The key represents a unique ID, which is generated and sent by the game
     * client. This information is only available if the client is spectating, otherwise null will be returned.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#GRENADES} (spectating only)</li>
     * </ul>
     *
     * @return the grenades on the map, or null if not sent
     */
    public Map<Integer, GrenadeState> getGrenadeStates() {
        return grenadeStates;
    }
    
    /**
     * The key represents the Steam ID of the player, and the value the associated player state information. This
     * information is only available if the client is spectating, otherwise null will be returned.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYERS_ID} (spectating only)</li>
     *     <li>{@link DataComponent#PLAYERS_MATCH_STATS} (spectating only)</li>
     *     <li>{@link DataComponent#PLAYERS_POSITION} (spectating only)</li>
     *     <li>{@link DataComponent#PLAYERS_STATE} (spectating only)</li>
     *     <li>{@link DataComponent#PLAYERS_WEAPONS} (spectating only)</li>
     * </ul>
     *
     * @return a list of other players in the game, or null if not sent
     */
    public Map<String, PlayerState> getAllPlayerStates() {
        return playerStates;
    }
    
    /**
     * Returns the current bomb state and positional information. This information is only available if the client is
     * spectating, otherwise null will be returned.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#BOMB} (spectating only)</li>
     * </ul>
     *
     * @return the state of the bomb on the map, or null if not sent
     */
    public BombState getBombState() {
        return bombState;
    }
    
    /**
     * Returns the current phase in the round, and how long the phase will last before proceeding. This information is
     * only available if the client is spectating.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PHASE_COUNTDOWNS} (spectating only)</li>
     * </ul>
     *
     * @return the phase countdown state, or null if not sent
     */
    public PhaseCountdownState getPhaseCountdownState() {
        return phaseCountdownState;
    }
    
}
