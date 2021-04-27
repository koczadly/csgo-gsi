package uk.oczadly.karl.csgsi.state.components.grenade;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.internal.Util;
import uk.oczadly.karl.csgsi.state.components.EnumValue;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;

/**
 * This class represents a single grenade.
 * <p>Available types:</p>
 * <ul>
 *     <li>{@link Grenade} (base type)</li>
 *     <li>{@link ProjectileGrenade}</li>
 *     <li>{@link EffectGrenade}</li>
 *     <li>{@link IncendiaryGrenade}</li>
 * </ul>
 */
@JsonAdapter(Grenade.Adapter.class)
public class Grenade {
    
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
     * Represents a grenade type.
     */
    public enum Type {
        /** A decoy grenade. */
        @SerializedName("decoy")     DECOY          (EffectGrenade.class),
        /** A flashbang grenade. */
        @SerializedName("flashbang") FLASHBANG      (ProjectileGrenade.class),
        /** An HE grenade. */
        @SerializedName("frag")      FRAG           (ProjectileGrenade.class),
        /** A smoke grenade. */
        @SerializedName("smoke")     SMOKE          (EffectGrenade.class),
        /** A molotov/incendiary grenade projectile. */
        @SerializedName("firebomb")  MOLOTOV        (ProjectileGrenade.class),
        /** A molotov/incendiary grenade on the ground in flames. */
        @SerializedName("inferno")   MOLOTOV_FLAMES (IncendiaryGrenade.class);
        
        
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
        public Grenade deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                   JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            EnumValue<Grenade.Type> gType = EnumValue.of(obj.get("type").getAsString(), Grenade.Type.class, Util.GSON);
            Class<? extends Grenade> classType = gType.isResolved() ? gType.get().getObjectClass() : Grenade.class;
            Grenade grenade = context.deserialize(obj, classType);
            grenade.type = gType;
            return grenade;
        }
    }
    
}
