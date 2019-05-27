package uk.oczadly.karl.csgsi.state.state;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.GameState;
import uk.oczadly.karl.csgsi.state.ProviderState;

import static org.junit.Assert.*;

public class ProviderStateTest extends GameStateBaseTest {

    @Test
    public void testProviderDetails() {
        GameState gameState = deserilizeState("{\n" +
                "  \"provider\": {\n" +
                "  \"name\": \"Counter-Strike: Global Offensive\",\n" +
                "  \"appid\": 730,\n" +
                "  \"version\": 13688,\n" +
                "  \"steamid\": \"76561198050830377\",\n" +
                "  \"timestamp\": 1556199071\n" +
                "  }\n" +
                "}");
        ProviderState state = gameState.getProviderDetails();
        
        assertNotNull(gameState);
        assertNotNull(state);
    
        assertEquals(state.getName(), "Counter-Strike: Global Offensive");
        assertEquals(state.getAppId(), 730);
        assertEquals(state.getVersion(), 13688);
        assertEquals(state.getClientSteamId(), "76561198050830377");
        assertEquals(state.getTimeStamp(), 1556199071);
    }
    
    @Test
    public void testEmpty() {
        GameState state = deserilizeState("{}");
        assertNull(state.getProviderDetails());
    }

}
