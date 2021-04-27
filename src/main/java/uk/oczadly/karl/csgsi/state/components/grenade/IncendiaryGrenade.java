package uk.oczadly.karl.csgsi.state.components.grenade;

import com.google.gson.annotations.Expose;
import uk.oczadly.karl.csgsi.state.components.Coordinate;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a set of flames on the ground, from a thrown molotov or incendiary grenade.
 */
public class IncendiaryGrenade extends Grenade {
    
    @Expose private Map<String, Coordinate> flames;
    private volatile Coordinate approxPos = null;
    private volatile double approxSize = 0;
    
    private IncendiaryGrenade() {}
    
    /**
     * Returns a map of all the individual flames. The key is a unique identifier of the flame, and the value is the
     * positional coordinate of the flame effect.
     *
     * @return the flames from this grenade
     */
    public Map<String, Coordinate> getFlames() {
        return Collections.unmodifiableMap(flames);
    }
    
    /**
     * @return the <em>approximate</em> position of the flames
     */
    public Coordinate getApproxPosition() {
        calcApproxVals();
        return approxPos;
    }
    
    /**
     * @return the <em>approximate</em> size of the flames, in units
     */
    public double getApproxSize() {
        calcApproxVals();
        return approxSize;
    }
    
    private void calcApproxVals() {
        if (this.approxPos == null && !flames.isEmpty()) {
            synchronized (this) {
                double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE,
                        minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE,
                        minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;
                for (Coordinate c : flames.values()) {
                    minX = Math.min(c.getX(), minX);
                    maxX = Math.max(c.getX(), minX);
                    minY = Math.min(c.getY(), minY);
                    maxY = Math.max(c.getY(), minY);
                    minZ = Math.min(c.getZ(), minZ);
                    maxZ = Math.max(c.getZ(), minZ);
                }
                // Approx pos = center of outer boundaries
                this.approxPos = new Coordinate(
                        minX + ((maxX - minX) / 2),
                        minY + ((maxY - minY) / 2),
                        minZ + ((maxZ - minZ) / 2));
                // Approx size = diameter of boundaries
                this.approxSize = Math.max(maxX - minX, maxY - minY) + 32;
            }
        }
    }
    
}
