package uk.oczadly.karl.csgsi.state.components.grenade;

import com.google.gson.annotations.Expose;
import uk.oczadly.karl.csgsi.state.components.Coordinate;

/**
 * Represents a standard projectile grenade, with a position and velocity.
 */
public class ProjectileGrenade extends Grenade {
    
    @Expose private Coordinate position;
    @Expose private Coordinate velocity;
    
    
    /**
     * @return the current position on the map
     */
    public Coordinate getPosition() {
        return position;
    }
    
    /**
     * @return the current velocity of the grenade
     */
    public Coordinate getVelocity() {
        return velocity;
    }
    
}
