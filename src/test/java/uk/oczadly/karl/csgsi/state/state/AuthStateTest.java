package uk.oczadly.karl.csgsi.state.state;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.GameState;
import uk.oczadly.karl.csgsi.state.RoundState;
import uk.oczadly.karl.csgsi.state.components.Team;

import java.util.Map;

import static org.junit.Assert.*;

public class AuthStateTest extends GameStateBaseTest {

    @Test
    public void testProviderDetails() {
        GameState gameState = deserilizeState("{\n" +
                "  \"auth\": {\n" +
                "    \"token1\": \"CCWJu64ZV3JHDT8hZc\",\n" +
                "    \"token2\": \"abc123\"\n" +
                "  }\n" +
                "}");
        Map<String, String> auth = gameState.getAuthenticationTokens();
        
        assertNotNull(gameState);
        assertNotNull(auth);
    
        assertEquals(2, auth.size());
        assertEquals("CCWJu64ZV3JHDT8hZc", auth.get("token1"));
        assertEquals("abc123", auth.get("token2"));
    }
    
    @Test
    public void testEmpty() {
        GameState state = deserilizeState("{}");
        assertNotNull(state.getAuthenticationTokens());
    }
    
}
