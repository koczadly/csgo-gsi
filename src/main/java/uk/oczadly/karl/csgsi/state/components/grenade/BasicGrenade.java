package uk.oczadly.karl.csgsi.state.components.grenade;

import com.google.gson.annotations.Expose;
import uk.oczadly.karl.csgsi.state.components.Coordinate;
import uk.oczadly.karl.csgsi.state.components.EnumValue;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;

/**
 * Represents a standard grenade.
 */
public class BasicGrenade implements Grenade {
    
    @Expose private EnumValue<Type> type;
    @Expose private PlayerSteamID owner;
    @Expose private double lifetime;
    
    
    @Override
    public final EnumValue<Type> getType() {
        return type;
    }
    
    @Override
    public final PlayerSteamID getOwner() {
        return owner;
    }
    
    @Override
    public final double getLifetime() {
        return lifetime;
    }
    
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "type=" + type +
                ", owner=" + owner +
                ", lifetime=" + lifetime + '}';
    }
    
}
