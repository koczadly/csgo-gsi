package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * This class represents a player's Steam ID.
 *
 * <p>To construct an instance of this class, use one of the provided static methods (eg.
 * {@link #fromId64(String)}).</p>
 */
@JsonAdapter(PlayerSteamID.Deserializer.class)
public final class PlayerSteamID {
    
    private final long longVal;
    
    private PlayerSteamID(long longVal) {
        this.longVal = longVal;
    }
    
    
    /**
     * @return this Steam ID, as a {@code SteamID 64}, represented by an unsigned long
     */
    public long getAs64Long() {
        return longVal;
    }
    
    /**
     * @return this Steam ID, as a {@code SteamID 64} string representation
     */
    public String getAs64() {
        return Long.toUnsignedString(longVal);
    }
    
    
    @Override
    public String toString() {
        return getAs64();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerSteamID)) return false;
        PlayerSteamID that = (PlayerSteamID)o;
        return longVal == that.longVal;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(longVal);
    }
    
    
    /**
     * Constructs a new {@link PlayerSteamID} from the given {@code SteamID 64} representation.
     * @param id the Steam ID
     * @return an instance representing the given ID
     * @throws NumberFormatException if the value is not a valid 64-bit unsigned integer
     */
    public static PlayerSteamID fromId64(String id) {
        return fromId64(Long.parseUnsignedLong(id));
    }
    
    /**
     * Constructs a new {@link PlayerSteamID} from the given {@code SteamID 64} representation.
     * @param id the Steam ID
     * @return an instance representing the given ID
     */
    public static PlayerSteamID fromId64(long id) {
        return new PlayerSteamID(id);
    }
    
    
    
    static class Deserializer implements JsonDeserializer<PlayerSteamID> {
        @Override
        public PlayerSteamID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return fromId64(json.getAsString());
        }
    }
    
}
