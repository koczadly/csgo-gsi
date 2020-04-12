package uk.oczadly.karl.csgsi.state;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.oczadly.karl.csgsi.state.MapState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PhaseCountdownStateTest extends GameStateBaseTest {
    
    static PhaseCountdownState phaseState;
    
    @BeforeClass
    public static void setUp() {
        GameState state = deserilizeState("{\n" +
                "  \"phase_countdowns\": {\n" +
                "    \"phase\": \"freezetime\",\n" +
                "    \"phase_ends_in\": \"13.1\"\n" +
                "  }\n" +
                "}");
        assertNotNull(state);
    
        phaseState = state.getPhaseCountdownState();
        assertNotNull(phaseState);
    }
    
    
    @Test
    public void testPhase() {
        assertEquals(PhaseCountdownState.Phase.FREEZE_TIME, phaseState.getPhase().getEnum());
    }
    
    @Test
    public void testTime() {
        assertEquals(13.1d, phaseState.getRemainingTime(), 1e-9);
    }
    
    
    @Test
    public void testGamePhaseEnum() {
        testEnums(MapState.GamePhase.class,
                "live", "gameover", "warmup", "intermission");
    }
    
}
