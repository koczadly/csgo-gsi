package uk.oczadly.karl.csgsi.state.components.grenade;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents an effect grenade, with a position, velocity and effect duration.
 */
public class EffectGrenade extends ProjectileGrenade {
    
    @Expose @SerializedName("effecttime")
    private double effectTime;
    
    /**
     * @return the number of seconds the effect has been active for
     */
    public double getEffectTime() {
        return effectTime;
    }
    
}
