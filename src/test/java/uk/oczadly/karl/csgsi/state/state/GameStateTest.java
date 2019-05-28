package uk.oczadly.karl.csgsi.state.state;

import org.junit.Test;
import static org.junit.Assert.*;
import uk.oczadly.karl.csgsi.state.GameState;

public class GameStateTest extends GameStateBaseTest {
    
    /* Component tests are performed in their individual test classes. */
    
    
    @Test
    public void testEmpty() {
        GameState state = deserilizeState("{}");
        
        assertNull(state.getBombState());
        assertNull(state.getGrenadeStates());
        assertNull(state.getMapState());
        assertNull(state.getPhaseCountdownState());
        assertNull(state.getPlayerState());
        assertNull(state.getPlayerStates());
        assertNull(state.getProviderDetails());
        assertNull(state.getRoundState());
    }
    
}
