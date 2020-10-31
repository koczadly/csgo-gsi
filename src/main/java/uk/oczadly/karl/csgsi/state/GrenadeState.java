package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.state.components.Coordinate;
import uk.oczadly.karl.csgsi.state.components.OptionalEnum;

public class GrenadeState {
    
    @Expose @SerializedName("owner")
    private long ownerId;
    
    @Expose @SerializedName("position")
    private Coordinate position;
    
    @Expose @SerializedName("velocity")
    private Coordinate velocity;
    
    @Expose @SerializedName("lifetime")
    private Double lifetime;
    
    @Expose @SerializedName("type")
    private OptionalEnum<Type> type;
    
    @Expose @SerializedName("effecttime")
    private Double effectTime;
    
    
    /**
     * @return the ID of the player who threw the grenade
     */
    public long getOwnerId() {
        return ownerId;
    }
    
    /**
     * @return the current position of the grenade on the map
     */
    public Coordinate getPosition() {
        return position;
    }
    
    /**
     * @return the velocity of the grenade
     */
    public Coordinate getVelocity() {
        return velocity;
    }
    
    /**
     * @return the current age of the grenade in seconds
     */
    public Double getLifetime() {
        return lifetime;
    }
    
    /**
     * @return the type of grenade
     */
    public OptionalEnum<Type> getType() {
        return type;
    }
    
    /**
     * @return the number of seconds left until the effect ends
     */
    public Double getEffectTime() {
        return effectTime;
    }
    
    
    public enum Type {
        @SerializedName("smoke") SMOKE,
        @SerializedName("decoy") DECOY,
        @SerializedName("inferno") INCENDIARY,
        @SerializedName("firebomb") MOLOTOV,
        @SerializedName("flashbang") FLASHBANG,
        @SerializedName("frag") FRAG
    }
    
}
