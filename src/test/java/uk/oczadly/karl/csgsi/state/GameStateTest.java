package uk.oczadly.karl.csgsi.state;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class GameStateTest extends GameStateBaseTest {
    
    /* Component tests are performed in their individual test classes. */
    
    
    @Test
    public void testEmpty() {
        GameState state = deserializeState("{}");
        
        assertEmptyState(state.getBomb());
        assertEmptyState(state.getGrenades());
        assertEmptyState(state.getMap());
        assertEmptyState(state.getPhaseCountdowns());
        assertEmptyState(state.getPlayer());
        assertEmptyState(state.getAllPlayers());
        assertEmptyState(state.getProvider());
        assertEmptyState(state.getRound());
    }
    
    private void assertEmptyState(Optional<?> state) {
        assertNotNull(state);
        assertFalse(state.isPresent());
    }
    
}
