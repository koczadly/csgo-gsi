package uk.oczadly.karl.csgsi.state;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.components.Team;
import static org.junit.Assert.*;

public class RoundStateTest extends GameStateBaseTest {

    @Test
    public void testProviderDetails() {
        GameState gameState = deserializeState("{\n" +
                "  \"round\": {\n" +
                "    \"phase\": \"over\",\n" +
                "    \"win_team\": \"T\",\n" +
                "    \"bomb\": \"exploded\"\n" +
                "  }\n" +
                "}");
    
        assertNotNull(gameState);
        assertTrue(gameState.getRound().isPresent());
        RoundState state = gameState.getRound().get();
    
        assertEquals(RoundState.RoundPhase.OVER, state.getPhase().val());
        assertEquals(Team.TERRORIST, state.getWinningTeam().val());
        assertEquals(RoundState.BombPhase.EXPLODED, state.getBombPhase().val());
    }
    
    @Test
    public void testRoundPhaseEnum() {
        testEnums(RoundState.RoundPhase.class,
                "over", "freezetime", "live");
    }
    
    @Test
    public void testBombPhaseEnum() {
        testEnums(RoundState.BombPhase.class,
                "exploded", "planted", "defused");
    }
    
}
