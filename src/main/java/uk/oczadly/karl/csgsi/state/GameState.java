package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class GameState {
    
    @Expose
    @SerializedName("provider")
    private ProviderState providerDetails;

    @Expose
    @SerializedName("auth")
    private Map<String, String> authTokens;
    
    @Expose
    @SerializedName("map")
    private MapState mapState;
    
    @Expose
    @SerializedName("player")
    private PlayerState playerState;
    
    @Expose
    @SerializedName("round")
    private RoundState roundState;
    
    @Expose
    @SerializedName("grenades")
    private Map<Integer, GrenadeState> grenadeStates;

    @Expose
    @SerializedName("allplayers")
    private Map<String, PlayerState> playerStates;

    @Expose
    @SerializedName("bomb")
    private BombState bombState;

    @Expose
    @SerializedName("phase_countdowns")
    private PhaseCountdownState phaseCountdownState;
    
    
    /**
     * Returns a list of information about the game client which reported the state.
     * @return the game client provider information, or null if not sent
     */
    public ProviderState getProviderDetails() {
        return providerDetails;
    }
    
    /**
     * Returns a map of authentication tokens sent by the game client. These are defined in the service's game state
     * configuration file, and can be used to authenticate the sending game client.
     * @return the authentication tokens, or null if not sent
     */
    public Map<String, String> getAuthenticationTokens() {
        return authTokens;
    }
    
    /**
     * Returns details and statistics on the current map.
     * @return the current map state data, or null if not sent
     */
    public MapState getMapState() {
        return mapState;
    }
    
    /**
     * Returns information about the current player or the player being observed if spectating.
     * @return the current player's state data, or null if not sent
     */
    public PlayerState getPlayerState() {
        return playerState;
    }
    
    /**
     * Returns information about the current ongoing round.
     * @return the current round's state data, or null if not sent
     */
    public RoundState getRoundState() {
        return roundState;
    }
    
    /**
     * Returns a list of grenades on the map. The key represents a unique ID, which is generated and sent by the
     * game client. This information is only available if the client is spectating, otherwise null will be returned.
     * @return the grenades on the map, or null if not sent
     */
    public Map<Integer, GrenadeState> getGrenadeStates() {
        return grenadeStates;
    }
    
    /**
     * The key represents the Steam ID of the player, and the value the associated player state information. This
     * information is only available if the client is spectating, otherwise null will be returned.
     * @return a list of other players in the game, or null if not sent
     */
    public Map<String, PlayerState> getPlayerStates() {
        return playerStates;
    }
    
    /**
     * Returns the current bomb state and positional information. This information is only available if the client is
     * spectating, otherwise null will be returned.
     * @return the state of the bomb on the map, or null if not sent
     */
    public BombState getBombState() {
        return bombState;
    }
    
    /**
     * Returns the current phase in the round, and how long the phase will last before proceeding. This information is
     * only available if the client is spectating.
     * @return the phase countdown state, or null if not sent
     */
    public PhaseCountdownState getPhaseCountdownState() {
        return phaseCountdownState;
    }
    
}
