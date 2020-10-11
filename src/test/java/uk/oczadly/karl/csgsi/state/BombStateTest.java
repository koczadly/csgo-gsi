package uk.oczadly.karl.csgsi.state;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.oczadly.karl.csgsi.state.components.Coordinate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BombStateTest extends GameStateBaseTest {
    
    static BombState bombState;
    
    @BeforeClass
    public static void setUp() {
        GameState state = deserializeState("{\n" +
                "  \"bomb\": {\n" +
                "    \"state\": \"carried\",\n" +
                "    \"position\": \"3084.00, 127.00, 1613.03\",\n" +
                "    \"player\": 76561197960265734\n" +
                "  }\n" +
                "}");
        assertNotNull(state);
    
        bombState = state.getBombState();
        assertNotNull(bombState);
    }
    
    
    @Test
    public void testStatus() {
        assertEquals(BombState.BombStatus.CARRIED, bombState.getPhase().val());
    }
    
    @Test
    public void testPosition() {
        assertEquals(new Coordinate(3084.00, 127.00, 1613.03), bombState.getPosition());
    }
    
    @Test
    public void testPlayer() {
        assertEquals("76561197960265734", bombState.getPlayerId());
    }
    
    
    @Test
    public void testPhaseEnum() {
        testEnums(BombState.BombStatus.class,
                "dropped", "carried", "planting", "planted", "defusing", "defused", "exploded");
    }
    
}
