package uk.oczadly.karl.csgsi.state;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.components.Coordinate;
import uk.oczadly.karl.csgsi.state.components.grenade.EffectGrenade;
import uk.oczadly.karl.csgsi.state.components.grenade.Grenade;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;

import static org.junit.Assert.*;

public class GrenadeStateTest extends GameStateBaseTest {
    
    GameState gameState = deserializeState("{\n" +
            "  \"grenades\": {\n" +
            "    \"129\": {\n" +
            "      \"owner\": 76561198050830377,\n" +
            "      \"position\": \"2499.41, 49.75, 1616.00\",\n" +
            "      \"velocity\": \"1.00, 2.00, 3.00\",\n" +
            "      \"lifetime\": \"6.9\",\n" +
            "      \"type\": \"smoke\",\n" +
            "      \"effecttime\": \"5.0\"\n" +
            "    }\n" +
            "  }\n" +
            "}");
    
    @Test
    public void testDetails() {
        assertNotNull(gameState);
        assertTrue(gameState.getGrenades().isPresent());
        GrenadeState state = gameState.getGrenades().get();
        
        assertEquals(1, state.getAll().size());
        EffectGrenade grenade = (EffectGrenade)state.getById(129);
        assertEquals(new Coordinate(2499.41, 49.75, 1616.00), grenade.getPosition());
        assertEquals(new Coordinate(1d, 2d, 3d), grenade.getVelocity());
        assertEquals(6.9, grenade.getLifetime(), 1e-9);
        assertEquals(Grenade.Type.SMOKE, grenade.getType().asEnum());
        assertEquals(5d, grenade.getEffectTime(), 1e-9);
    }
    
    @Test
    public void testGetBy() {
        GrenadeState state = gameState.getGrenades().get();
        assertNull(state.getById(100));
        assertNotNull(state.getById(129));
        assertEquals(0, state.getByOwner(PlayerSteamID.fromId64("76561198050830376")).size());
        assertEquals(1, state.getByOwner(PlayerSteamID.fromId64("76561198050830377")).size());
        assertEquals(0, state.getByType(Grenade.Type.FLASHBANG).size());
        assertEquals(1, state.getByType(Grenade.Type.SMOKE).size());
    }
    
}
