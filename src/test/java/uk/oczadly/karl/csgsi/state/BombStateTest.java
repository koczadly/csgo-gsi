package uk.oczadly.karl.csgsi.state;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.oczadly.karl.csgsi.state.components.Coordinate;

import static org.junit.Assert.*;

public class BombStateTest extends GameStateBaseTest {
    
    @BeforeClass
    public static void setUp() {
        GameState gameState = deserializeState("{\n" +
                "  \"bomb\": {\n" +
                "    \"state\": \"carried\",\n" +
                "    \"position\": \"3084.00, 127.00, 1613.03\",\n" +
                "    \"player\": 76561197960265734\n" +
                "  }\n" +
                "}");
        
        assertNotNull(gameState);
        assertTrue(gameState.getBomb().isPresent());
        BombState state = gameState.getBomb().get();
        
        assertEquals(BombState.BombStatus.CARRIED, state.getPhase().val());
        assertEquals(new Coordinate(3084.00, 127.00, 1613.03), state.getPosition());
        assertEquals(76561197960265734L, state.getPlayerId());
    }
    
    
    @Test
    public void testPhaseEnum() {
        testEnums(BombState.BombStatus.class,
                "dropped", "carried", "planting", "planted", "defusing", "defused", "exploded");
    }
    
}
