package uk.oczadly.karl.csgsi.state;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;

import static org.junit.Assert.*;

public class ProviderStateTest extends GameStateBaseTest {

    @Test
    public void testProviderDetails() {
        GameState gameState = deserializeState("{\n" +
                "  \"provider\": {\n" +
                "  \"name\": \"Counter-Strike: Global Offensive\",\n" +
                "  \"appid\": 730,\n" +
                "  \"version\": 13688,\n" +
                "  \"steamid\": \"76561198050830377\",\n" +
                "  \"timestamp\": 1556199071\n" +
                "  }\n" +
                "}");
        
        assertNotNull(gameState);
        assertTrue(gameState.getProvider().isPresent());
        ProviderState state = gameState.getProvider().get();
    
        assertEquals("Counter-Strike: Global Offensive", state.getName());
        assertEquals(730, state.getAppId());
        assertEquals(13688, state.getVersion());
        assertEquals(PlayerSteamID.fromId64("76561198050830377"), state.getClientSteamId());
        assertEquals(1556199071, state.getTimestamp());
    }

}
