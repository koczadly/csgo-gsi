package uk.oczadly.karl.csgsi.state.components;

import org.junit.Test;
import uk.oczadly.karl.csgsi.internal.Util;

import static org.junit.Assert.*;

/**
 * @author Karl Oczadly
 */
public class PlayerSteamIDTest {
    
    @Test
    public void testConvert() {
        PlayerSteamID id = PlayerSteamID.fromId64("76561198050830377");
        assertEquals("76561198050830377", id.getAsID64());
        assertEquals(76561198050830377L, id.getAsID64Long());
        assertEquals("STEAM_1:1:45282324", id.getAsID());
        assertEquals("[U:1:90564649]", id.getAsID3());
    }
    
    @Test
    public void testDeserialize() {
        PlayerSteamID id = Util.GSON.fromJson("\"76561198050830377\"", PlayerSteamID.class);
        assertEquals(id.getAsID64(), "76561198050830377");
        assertEquals(id.getAsID64Long(), 76561198050830377L);
    }
    
    @Test
    public void testEquality() {
        PlayerSteamID id = PlayerSteamID.fromId64("76561198050830377");
        assertEquals(id, id); // Self equality
        assertEquals(id, PlayerSteamID.fromId64("76561198050830377")); // Same values
        assertNotEquals(id, PlayerSteamID.fromId64("76561198050830378")); // Different vals
    }
    
    @Test
    public void testHashcode() {
        assertEquals(PlayerSteamID.fromId64("76561198050830377").hashCode(),
                PlayerSteamID.fromId64("76561198050830377").hashCode());
    }

}