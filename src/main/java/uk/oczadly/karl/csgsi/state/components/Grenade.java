package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.internal.Util;

import java.util.Collections;
import java.util.Map;

/**
 * This class represents a single grenade.
 * <p>Available types:</p>
 * <ul>
 *     <li>{@link BasicGrenade} (adds no additional attributes)</li>
 *     <li>{@link ProjectileGrenade}</li>
 *     <li>{@link EffectGrenade}</li>
 *     <li>{@link IncendiaryGrenade}</li>
 * </ul>
 */
@JsonAdapter(Grenade.Adapter.class)
public abstract class Grenade {
    
    private EnumValue<Type> type;
    @Expose private PlayerSteamID owner;
    @Expose private double lifetime;
    
    /**
     * @return the type of grenade
     */
    public EnumValue<Type> getType() {
        return type;
    }
    
    /**
     * @return the Steam ID of the player who threw this grenade
     */
    public PlayerSteamID getOwner() {
        return owner;
    }
    
    /**
     * @return how many seconds have elapsed since the grenade was thrown
     */
    public double getLifetime() {
        return lifetime;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "type=" + type +
                ", owner=" + owner +
                ", lifetime=" + lifetime +
                '}';
    }
    
    
    /**
     * Represents an unknown grenade type.
     */
    public static class BasicGrenade extends Grenade {
        private BasicGrenade() {}
    }
    
    /**
     * Represents a standard projectile grenade, with a position and velocity.
     */
    public static class ProjectileGrenade extends Grenade {
        @Expose private Coordinate position;
        @Expose private Coordinate velocity;
    
        private ProjectileGrenade() {}
    
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
    
    /**
     * Represents an effect grenade, with a position, velocity and effect duration.
     */
    public static class EffectGrenade extends ProjectileGrenade {
        @Expose @SerializedName("effecttime") private double effectTime;
    
        private EffectGrenade() {}
        
        /**
         * @return the number of seconds the effect has been active for
         */
        public double getEffectTime() {
            return effectTime;
        }
    }
    
    /**
     * Represents a set of flames on the ground, from a thrown molotov or incendiary grenade.
     */
    public static class IncendiaryGrenade extends Grenade {
        @Expose private Map<String, Coordinate> flames;
        
        private volatile Coordinate approxPos = null;
        private volatile double approxSize = 0;
    
        private IncendiaryGrenade() {}
    
        /**
         * Returns a map of all the individual flames. The key is a unique identifier of the flame, and the
         * value is the positional coordinate of the flame effect.
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
                    this.approxPos = new Coordinate( // Center of outer boundaries
                            minX + ((maxX - minX) / 2),
                            minY + ((maxY - minY) / 2),
                            minZ + ((maxZ - minZ) / 2));
                    this.approxSize = Math.max(maxX - minX, maxY - minY) + 32; // Diameter of outer boundaries
                }
            }
        }
    }
    
    
    /**
     * Represents a grenade type.
     */
    public enum Type {
        /** A decoy grenade. */
        @SerializedName("decoy")     DECOY                  (EffectGrenade.class),
        /** A flashbang grenade. */
        @SerializedName("flashbang") FLASHBANG              (ProjectileGrenade.class),
        /** An HE grenade. */
        @SerializedName("frag")      FRAG                   (ProjectileGrenade.class),
        /** A smoke grenade. */
        @SerializedName("smoke")     SMOKE                  (EffectGrenade.class),
        /** A molotov/incendiary grenade projectile. */
        @SerializedName("firebomb")  MOLOTOV                (ProjectileGrenade.class),
        /** A molotov/incendiary grenade on the ground in flames. */
        @SerializedName("inferno")   MOLOTOV_FLAMES         (IncendiaryGrenade.class);
        
        
        private final Class<? extends Grenade> objClass;
        
        Type(Class<? extends Grenade> objClass) {
            this.objClass = objClass;
        }
        
        public Class<? extends Grenade> getObjectClass() {
            return objClass;
        }
    }
    
    
    static class Adapter implements JsonDeserializer<Grenade> {
        @Override
        public Grenade deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            
            // Get type
            EnumValue<Grenade.Type> gType = EnumValue.of(
                    obj.get("type").getAsString(), Grenade.Type.class, Util.GSON);
            
            // Deserialize grenade
            Class<? extends Grenade> classType =
                    gType.isResolved() ? gType.get().getObjectClass() : BasicGrenade.class;
            Grenade grenade = context.deserialize(obj, classType);
            grenade.type = gType; // Fill type field
            return grenade;
        }
    }
    
}
