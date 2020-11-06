package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.state.components.Coordinate;
import uk.oczadly.karl.csgsi.state.components.EnumValue;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;

public class BombState {
    
    @Expose @SerializedName("state")
    private EnumValue<BombStatus> phase;
    
    @Expose @SerializedName("position")
    private Coordinate position;
    
    @Expose @SerializedName("countdown")
    private Double countdown;
    
    @Expose @SerializedName("player")
    private PlayerSteamID playerId;
    
    
    /**
     * @return the phase of the bomb
     */
    public EnumValue<BombStatus> getPhase() {
        return phase;
    }
    
    /**
     * @return the current position of the bomb on the map
     */
    public Coordinate getPosition() {
        return position;
    }
    
    /**
     * @return the number of seconds for the current phase
     */
    public Double getCountdown() {
        return countdown;
    }
    
    /**
     * @return the ID of the player interacting with the bomb, or null if no player is interacting with the bomb
     */
    public PlayerSteamID getPlayerId() {
        return playerId;
    }
    
    
    public enum BombStatus {
        /**
         * Bomb is dropped on the ground.
         */
        @SerializedName("dropped") DROPPED,
        
        /**
         * Bomb is being carried in a player's inventory.
         */
        @SerializedName("carried") CARRIED,
        
        /**
         * Bomb is currently being planted.
         */
        @SerializedName("planting") PLANTING,
        
        /**
         * Bomb is planted.
         */
        @SerializedName("planted") PLANTED,
        
        /**
         * Bomb is currently being defused.
         */
        @SerializedName("defusing") DEFUSING,
        
        /**
         * Bomb has been defused and round is resetting.
         */
        @SerializedName("defused") DEFUSED,
        
        /**
         * Bomb has exploded and round is resetting.
         */
        @SerializedName("exploded") EXPLODED
    }
    
}
