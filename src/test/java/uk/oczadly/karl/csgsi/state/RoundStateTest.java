package uk.oczadly.karl.csgsi.state;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.components.Team;
import static org.junit.Assert.*;

public class RoundStateTest extends GameStateBaseTest {

    @Test
    public void testProviderDetails() {
        GameState gameState = deserilizeState("{\n" +
                "  \"round\": {\n" +
                "    \"phase\": \"over\",\n" +
                "    \"win_team\": \"T\",\n" +
                "    \"bomb\": \"exploded\"\n" +
                "  }\n" +
                "}");
        RoundState state = gameState.getRoundState();
        
        assertNotNull(gameState);
        assertNotNull(state);
    
        assertEquals(RoundState.RoundPhase.OVER, state.getPhase());
        assertEquals(Team.TERRORIST, state.getWinningTeam());
        assertEquals(RoundState.BombPhase.EXPLODED, state.getBombPhase());
    }
    
    @Test
    public void testEmpty() {
        GameState state = deserilizeState("{}");
        assertNull(state.getRoundState());
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
