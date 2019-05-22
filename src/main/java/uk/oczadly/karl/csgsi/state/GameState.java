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
     * @return the game client provider information
     */
    public ProviderState getProviderDetails() {
        return providerDetails;
    }
    
    /**
     * @return the authentication tokens sent by the client
     */
    public Map<String, String> getAuthenticationTokens() {
        return authTokens;
    }
    
    /**
     * @return the current map state data
     */
    public MapState getMapState() {
        return mapState;
    }
    
    /**
     * @return the current player's state data
     */
    public PlayerState getPlayerState() {
        return playerState;
    }
    
    /**
     * @return the current round's state data
     */
    public RoundState getRoundState() {
        return roundState;
    }
    
    /**
     * Returns a list of grenades on the map. The key represents a unique ID, which is generated and sent by the
     * game client.
     * @return the grenades on the map
     */
    public Map<Integer, GrenadeState> getGrenadeStates() {
        return grenadeStates;
    }
    
    /**
     * The key represents the Steam ID of the player, and the value the associated player state information.
     * @return a list of other players in the game
     */
    public Map<String, PlayerState> getPlayerStates() {
        return playerStates;
    }
    
    /**
     * @return the state of the bomb on the map
     */
    public BombState getBombState() {
        return bombState;
    }
    
    /**
     * Returns the current phase in the round, and how long the phase will last before proceeding.
     * @return the phase countdown state
     */
    public PhaseCountdownState getPhaseCountdownState() {
        return phaseCountdownState;
    }
    
}
