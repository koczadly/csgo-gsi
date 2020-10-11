package uk.oczadly.karl.csgsi.state;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.components.Coordinate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GrenadeStateTest extends GameStateBaseTest {
    
    @Test
    public void testDetails() {
        GameState state = deserializeState("{\n" +
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
        assertNotNull(state);
        
        assertNotNull(state.getGrenadeStates());
        assertEquals(1, state.getGrenadeStates().size());
        
        GrenadeState grenade = state.getGrenadeStates().get(129);
        assertEquals(new Coordinate(2499.41, 49.75, 1616.00), grenade.getPosition());
        assertEquals(new Coordinate(1d, 2d, 3d), grenade.getVelocity());
        assertEquals(6.9, grenade.getLifetime(), 1e-9);
        assertEquals(GrenadeState.Type.SMOKE, grenade.getType().val());
        assertEquals(5d, grenade.getEffectTime(), 1e-9);
    }
    
    
    @Test
    public void testTypeEnum() {
        testEnums(GrenadeState.Type.class,
                "smoke", "decoy", "inferno", "firebomb", "flashbang", "frag");
    }
    
}
