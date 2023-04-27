package uk.oczadly.karl.csgsi.state;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class PhaseCountdownStateTest extends GameStateBaseTest {
    
    @BeforeClass
    public static void setUp() {
        GameState gameState = deserializeState("{\n" +
                "  \"phase_countdowns\": {\n" +
                "    \"phase\": \"freezetime\",\n" +
                "    \"phase_ends_in\": \"13.1\"\n" +
                "  }\n" +
                "}");
        
        assertNotNull(gameState);
        assertTrue(gameState.getPhaseCountdowns().isPresent());
        PhaseCountdownState state = gameState.getPhaseCountdowns().get();
        
        assertEquals(PhaseCountdownState.Phase.FREEZE_TIME, state.getPhase().asEnum());
        assertEquals(13.1d, state.getRemainingTime(), 1e-9);
    }
    
    
    @Test
    public void testGamePhaseEnum() {
        testEnums(MapState.GamePhase.class,
                "live", "gameover", "warmup", "intermission");
    }
    
}
