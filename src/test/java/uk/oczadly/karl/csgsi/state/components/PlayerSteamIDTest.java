package uk.oczadly.karl.csgsi.state.components;

import org.junit.Test;
import uk.oczadly.karl.csgsi.internal.Util;

import static org.junit.Assert.*;

/**
 * @author Karl Oczadly
 */
public class PlayerSteamIDTest {
    
    @Test
    public void testDeserialize() {
        PlayerSteamID id = Util.GSON.fromJson("\"76561198050830377\"", PlayerSteamID.class);
        assertEquals(id.getAs64(), "76561198050830377");
        assertEquals(id.getAs64Long(), 76561198050830377L);
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