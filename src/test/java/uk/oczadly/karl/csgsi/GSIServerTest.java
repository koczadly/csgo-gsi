package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.time.Instant;
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
    private static final InetAddress ADDRESS = InetAddress.getLoopbackAddress();
    
    
    @Test
    public void testBuilder() {
        InetAddress addr = InetAddress.getLoopbackAddress();
        GSIServer srv = new GSIServer.Builder(addr, 1337)
                .requireAuthToken("t3", "v3") // Single add
                .requireAuthTokens(Map.of("t1", "v1", "t2", "v2")) // Multi add
                .disableDiagnosticsPage()
                .build();
        
        assertFalse(srv.diagPageEnabled);
        assertEquals(1337, srv.getPort());
        assertSame(addr, srv.getBindingAddress());
        assertEquals(Map.of("t1", "v1", "t2", "v2", "t3", "v3"), srv.getRequiredAuthTokens());
    }
    
    @Test
    public void testAuthTokensParse() throws Exception {
        TestObserver observer = new TestObserver();
        GSIServer server = new GSIServer.Builder(1337).build();
        server.registerObserver(observer);
        
        server.handleStateUpdate(AUTH_TOKEN_JSON, "/", ADDRESS);
        
        //Wait for observer
        server.getObserverExecutorService().shutdown();
        server.getObserverExecutorService().awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        assertNotNull(observer.context.getAuthTokens());
        assertEquals(2, observer.context.getAuthTokens().size());
        assertEquals("abc123", observer.context.getAuthTokens().get("token1"));
        assertEquals("def456", observer.context.getAuthTokens().get("token2"));
    }
    
    @Test
    public void testAuthTokenValidationAllValid() throws Exception {
        Map<String, String> expectedTokens = new HashMap<>();
        expectedTokens.put("token1", "abc123"); //Valid
        expectedTokens.put("token2", "def456"); //Valid
        
        assertTrue("Observer wasn't notified despite valid auth", checkAuthValidation(expectedTokens));
    }
    
    @Test
    public void testAuthTokenValidationOnlyRequiredValid() throws Exception {
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
    public void testAuthTokenValidationUnspecifiedKey() throws Exception {
        Map<String, String> expectedTokens = new HashMap<>();
        expectedTokens.put("token1", "abc123"); //Valid
        expectedTokens.put("token42", "def456"); //Invalid (key unspecified)
        
        assertFalse("Observer was notified despite invalid auth", checkAuthValidation(expectedTokens));
    }
    
    @Test
    public void testEmptyState() { //Ensure no exception
        new GSIServer.Builder(1337).build().handleStateUpdate("{}", "/", ADDRESS);
    }
    
    @Test
    public void testObserverNotification() throws Exception {
        TestObserver observer1 = new TestObserver(), observer2 = new TestObserver();
        GSIServer server = new GSIServer.Builder(1337).build();
        server.registerObserver(observer1);
        server.registerObserver(observer2);
        
        //Create mock objects
        GameState state = new GameState(), previous = new GameState();
        Map<String, String> authTokens = new HashMap<>();
        InetAddress address = InetAddress.getLoopbackAddress();
        JsonObject jsonObject = new JsonObject();
        String uriPath = "/", jsonString = "{}";
        Instant i1 = Instant.ofEpochMilli(500), i2 = Instant.ofEpochMilli(200);
        GameStateContext context = new GameStateContext(
                server, uriPath, previous, i1, i2, 43, address, authTokens, jsonObject, jsonString);
        
        //Notify observing object
        server.notifyObservers(state, context);
        
        //Await observer notification...
        server.getObserverExecutorService().shutdown();
        server.getObserverExecutorService().awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        //Verify objects match
        assertSame(state, observer1.state);
        assertSame(state, observer2.state);
        assertSame(uriPath, observer1.context.getUriPath());
        assertSame(uriPath, observer2.context.getUriPath());
        assertSame(i1, observer1.context.getTimestamp());
        assertSame(i1, observer2.context.getTimestamp());
        assertSame(previous, observer1.context.getPreviousState());
        assertSame(previous, observer2.context.getPreviousState());
        assertSame(i2, observer1.context.getPreviousTimestamp());
        assertSame(i2, observer2.context.getPreviousTimestamp());
        assertEquals(300, observer1.context.getMillisSinceLastState());
        assertEquals(300, observer2.context.getMillisSinceLastState());
        assertEquals(43, observer1.context.getSequentialCounter());
        assertEquals(43, observer2.context.getSequentialCounter());
        assertEquals(authTokens, observer1.context.getAuthTokens());
        assertEquals(authTokens, observer2.context.getAuthTokens());
        assertSame(address, observer1.context.getAddress());
        assertSame(address, observer2.context.getAddress());
        assertSame(server, observer1.context.getGsiServer());
        assertSame(server, observer2.context.getGsiServer());
        assertSame(jsonObject, observer1.context.getRawJsonObject());
        assertSame(jsonObject, observer2.context.getRawJsonObject());
        assertSame(jsonString, observer1.context.getRawJsonString());
        assertSame(jsonString, observer2.context.getRawJsonString());
    }
    
    
    
    public boolean checkAuthValidation(Map<String, String> expectedTokens) throws Exception {
        TestObserver observer = new TestObserver();
        GSIServer server = new GSIServer.Builder(1337).requireAuthTokens(expectedTokens).build();
        server.registerObserver(observer);
        
        server.handleStateUpdate(AUTH_TOKEN_JSON, "/", ADDRESS);
        
        //Wait for observer
        server.getObserverExecutorService().shutdown();
        server.getObserverExecutorService().awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        return observer.called;
    }
    
    private static class TestObserver implements GSIObserver {
        boolean called = false;
        GameState state;
        GameStateContext context;
        
        @Override
        public void update(GameState state, GameStateContext context) {
            this.called = true;
            this.state = state;
            this.context = context;
        }
    }

}