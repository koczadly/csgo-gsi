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
     * @return this Steam ID, as a {@code SteamID64}, represented by an unsigned long
     */
    public long getAsID64Long() {
        return longVal;
    }
    
    /**
     * @return this Steam ID, as a {@code SteamID64} string representation (eg. {@code 76561198050830377})
     */
    public String getAsID64() {
        return Long.toUnsignedString(longVal);
    }
    
    /**
     * @return this Steam ID, as a standard {@code SteamID} string representation (eg. {@code STEAM_1:1:45282324})
     */
    public String getAsID() {
        return "STEAM_" + (longVal >> 56) +             // Universe
                ':' + (longVal & 0b1L) +                // Account digit
                ':' + ((longVal >> 1) & 0x7FFFFFFFL);   // Account no.
    }
    
    /**
     * @return this Steam ID, as a {@code SteamID3} string representation (eg. {@code [U:1:90564649]})
     */
    public String getAsID3() {
        return "[U:1:" + (longVal - 76561197960265728L) + "]";
    }
    
    
    @Override
    public String toString() {
        return getAsID64();
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
     * Constructs a new {@link PlayerSteamID} from the given {@code SteamID64} representation.
     * @param id the Steam ID
     * @return an instance representing the given ID
     * @throws NumberFormatException if the value is not a valid 64-bit unsigned integer
     */
    public static PlayerSteamID fromId64(String id) {
        return fromId64(Long.parseUnsignedLong(id));
    }
    
    /**
     * Constructs a new {@link PlayerSteamID} from the given {@code SteamID64} representation.
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
