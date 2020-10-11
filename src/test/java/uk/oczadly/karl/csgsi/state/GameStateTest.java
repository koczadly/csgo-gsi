package uk.oczadly.karl.csgsi.state;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameStateTest extends GameStateBaseTest {
    
    /* Component tests are performed in their individual test classes. */
    
    
    @Test
    public void testEmpty() {
        GameState state = deserializeState("{}");
        
        assertNull(state.getBombState());
        assertNull(state.getGrenadeStates());
        assertNull(state.getMapState());
        assertNull(state.getPhaseCountdownState());
        assertNull(state.getPlayerState());
        assertNull(state.getAllPlayerStates());
        assertNull(state.getProviderDetails());
        assertNull(state.getRoundState());
    }
    
}
