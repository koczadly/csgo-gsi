package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.config.DataComponent;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;

import java.util.Map;
import java.util.Optional;

public class GameState {
    
    @Expose @SerializedName("provider") private ProviderState provider;
    @Expose @SerializedName("map") private MapState map;
    @Expose @SerializedName("player") private PlayerState player;
    @Expose @SerializedName("round") private RoundState round;
    @Expose @SerializedName("grenades") private GrenadeState grenades;
    @Expose @SerializedName("allplayers") private Map<PlayerSteamID, PlayerState> players;
    @Expose @SerializedName("bomb") private BombState bomb;
    @Expose @SerializedName("phase_countdowns") private PhaseCountdownState phaseCountdowns;
    
    
    /**
     * Returns a list of information about the game client which reported the state.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PROVIDER}</li>
     * </ul>
     *
     * @return the game client provider information
     */
    public Optional<ProviderState> getProvider() {
        return Optional.ofNullable(provider);
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
     * @return the current map state data
     */
    public Optional<MapState> getMap() {
        return Optional.ofNullable(map);
    }
    
    /**
     * Returns information about the current player, or the player being observed if spectating.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_ID}</li>
     *     <li>{@link DataComponent#PLAYER_MATCH_STATS}</li>
     *     <li>{@link DataComponent#PLAYER_POSITION} (spectator only)</li>
     *     <li>{@link DataComponent#PLAYER_STATE}</li>
     *     <li>{@link DataComponent#PLAYER_WEAPONS}</li>
     * </ul>
     *
     * @return the current player's state data
     */
    public Optional<PlayerState> getPlayer() {
        return Optional.ofNullable(player);
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
     * @return the current round's state data
     */
    public Optional<RoundState> getRound() {
        return Optional.ofNullable(round);
    }
    
    /**
     * Returns all the grenades which currently exist on the map.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#GRENADES} (spectator only)</li>
     * </ul>
     *
     * @return the grenades on the map
     */
    public Optional<GrenadeState> getGrenades() {
        return Optional.ofNullable(grenades);
    }
    
    /**
     * The key represents the Steam ID of the player, and the value the associated player state information. This
     * information is only available if the client is spectating.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYERS_ID} (spectator only)</li>
     *     <li>{@link DataComponent#PLAYERS_MATCH_STATS} (spectator only)</li>
     *     <li>{@link DataComponent#PLAYERS_POSITION} (spectator only)</li>
     *     <li>{@link DataComponent#PLAYERS_STATE} (spectator only)</li>
     *     <li>{@link DataComponent#PLAYERS_WEAPONS} (spectator only)</li>
     * </ul>
     *
     * @return a list of other players in the game
     */
    public Optional<Map<PlayerSteamID, PlayerState>> getAllPlayers() {
        return Optional.ofNullable(players);
    }
    
    /**
     * Returns the current bomb state and positional information. This information is only available if the client is
     * spectating.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#BOMB} (spectator only)</li>
     * </ul>
     *
     * @return the state of the bomb on the map
     */
    public Optional<BombState> getBomb() {
        return Optional.ofNullable(bomb);
    }
    
    /**
     * Returns the current phase in the round, and how long the phase will last before proceeding. This information is
     * only available if the client is spectating.
     *
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PHASE_COUNTDOWNS} (spectator only)</li>
     * </ul>
     *
     * @return the phase countdown state
     */
    public Optional<PhaseCountdownState> getPhaseCountdowns() {
        return Optional.ofNullable(phaseCountdowns);
    }
    
}
