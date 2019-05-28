package uk.oczadly.karl.csgsi;

import org.junit.Before;
import org.junit.Test;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class GSIServerTest {
    
    private static final int TIMEOUT_SECONDS = 5;
    private static final String AUTH_TOKEN_JSON = "{\n" +
            "  \"auth\": {\n" +
            "    \"token1\": \"abc123\",\n" +
            "    \"token2\": \"def456\"\n" +
            "  }\n" +
            "}";
    
    
    @Test
    public void testAuthTokensParse() throws Exception {
        TestObserver observer = new TestObserver();
        GSIServer server = new GSIServer(1337);
        server.registerObserver(observer);
        
        server.handleStateUpdate(AUTH_TOKEN_JSON, null);
        
        //Wait for observer
        server.getObserverExecutorService().shutdown();
        server.getObserverExecutorService().awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        assertNotNull(observer.authTokens);
        assertEquals(2, observer.authTokens.size());
        assertEquals("abc123", observer.authTokens.get("token1"));
        assertEquals("def456", observer.authTokens.get("token2"));
    }
    
    @Test
    public void testAuthTokenValidationAllValid() throws Exception {
        Map<String, String> expectedTokens = new HashMap<>();
        expectedTokens.put("token1", "abc123"); //Valid
        expectedTokens.put("token2", "def456"); //Valid
        assertTrue("Observer wasn't notified despite valid auth", checkAuthValidation(expectedTokens));
    }
    
    @Test
    public void testAuthTokenValidationSomeValid() throws Exception {
        Map<String, String> expectedTokens = new HashMap<>();
        expectedTokens.put("token1", "abc123"); //Valid
        assertTrue("Observer wasn't notified despite valid auth", checkAuthValidation(expectedTokens));
    }
    
    @Test
    public void testAuthTokenValidationInvalidValue() throws Exception {
        Map<String, String> expectedTokens = new HashMap<>();
        expectedTokens.put("token1", "abc123"); //Valid
        expectedTokens.put("token2", "slugs"); //Invalid (wrong value)
        assertFalse("Observer was notified despite invalid auth", checkAuthValidation(expectedTokens));
    }
    
    @Test
    public void testAuthTokenValidationInvalidKey() throws Exception {
        Map<String, String> expectedTokens = new HashMap<>();
        expectedTokens.put("token1", "abc123"); //Valid
        expectedTokens.put("token42", "def456"); //Invalid (key unspecified)
        assertFalse("Observer was notified despite invalid auth", checkAuthValidation(expectedTokens));
    }
    
    @Test
    public void testEmptyState() { //Ensure no exception
        new GSIServer(1337).handleStateUpdate("{}", null);
    }
    
    @Test
    public void testObserverNotification() throws Exception {
        TestObserver observer1 = new TestObserver(), observer2 = new TestObserver();
        GSIServer server = new GSIServer(1337);
        server.registerObserver(observer1);
        server.registerObserver(observer2);
        
        //Create mock objects
        GameState state = new GameState(), previous = new GameState();
        Map<String, String> authTokens = new HashMap<>();
        InetAddress address = InetAddress.getLoopbackAddress();
        
        //Notify observing object
        server.notifyObservers(state, previous, authTokens, address);
        
        //Await observer notification...
        server.getObserverExecutorService().shutdown();
        server.getObserverExecutorService().awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        //Verify objects match
        assertSame(state, observer1.state);
        assertSame(state, observer2.state);
        assertSame(previous, observer1.previous);
        assertSame(previous, observer2.previous);
        assertSame(authTokens, observer1.authTokens);
        assertSame(authTokens, observer2.authTokens);
        assertSame(address, observer1.address);
        assertSame(address, observer2.address);
    }
    
    
    
    public boolean checkAuthValidation(Map<String, String> expectedTokens) throws Exception {
        TestObserver observer = new TestObserver();
        GSIServer server = new GSIServer(1337, expectedTokens);
        server.registerObserver(observer);
        
        server.handleStateUpdate(AUTH_TOKEN_JSON, null);
        
        //Wait for observer
        server.getObserverExecutorService().shutdown();
        server.getObserverExecutorService().awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        return observer.authTokens != null;
    }
    
    private class TestObserver implements GSIObserver {
        GameState state;
        GameState previous;
        Map<String, String> authTokens;
        InetAddress address;
        
        @Override
        public void update(GameState state, GameState previous, Map<String, String> authTokens, InetAddress address) {
            this.state = state;
            this.previous = previous;
            this.authTokens = authTokens;
            this.address = address;
        }
    }

}