package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import org.junit.Test;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class GSIServerTest {
    
    private static final int OBSERVER_TIMEOUT = 500; // in millis
    private static final String AUTH_TOKEN_JSON = "{\n" +
            "  \"auth\": {\n" +
            "    \"token1\": \"abc123\",\n" +
            "    \"token2\": \"def456\"\n" +
            "  }\n" +
            "}";
    private static final InetAddress ADDRESS = InetAddress.getLoopbackAddress();
    
    
    @Test
    public void testBuilder() throws Exception {
        InetAddress addr = InetAddress.getByAddress(new byte[] {1, 2, 3, 4});
        GSIListener observer = new MockListener(null);
        GSIServer srv = new GSIServer.Builder(1337)
                .bindToInterface(addr)
                .requireAuthToken("t3", "v3") // Single add
                .requireAuthTokens(Map.of("t1", "v1", "t2", "v2")) // Multi add
                .disableDiagnosticsPage()
                .registerListener(observer).build();
        
        assertFalse(srv.diagnosticsEnabled);
        assertEquals(1337, srv.getBindAddress().getPort());
        assertSame(addr, srv.getBindAddress().getAddress());
        assertEquals(Map.of("t1", "v1", "t2", "v2", "t3", "v3"), srv.getRequiredAuthTokens());
        assertEquals(Set.of(observer), srv.listeners.listeners);
    }
    
    @Test
    public void testAuthTokensParse() throws Exception {
        CountDownLatch observerLatch = new CountDownLatch(1);
        MockListener observer = new MockListener(observerLatch);
        GSIServer server = new GSIServer.Builder(1337).build();
        server.registerListener(observer);
        
        server.handleStateUpdate(AUTH_TOKEN_JSON, "/", ADDRESS);
        assertTrue(observerLatch.await(OBSERVER_TIMEOUT, TimeUnit.MILLISECONDS)); // Wait for observer
        
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
        new GSIServer.Builder(1337).build()
                .handleStateUpdate("{}", "/", ADDRESS);
    }
    
    @Test
    public void testObserverNotification() throws Exception {
        CountDownLatch observerLatch = new CountDownLatch(2);
        MockListener observer1 = new MockListener(observerLatch), observer2 = new MockListener(observerLatch);
        GSIServer server = new GSIServer.Builder(1337)
                .registerListener(observer1)
                .registerListener(observer2).build();
        
        // Create mock objects
        GameState state = new GameState(), previous = new GameState();
        Map<String, String> authTokens = new HashMap<>();
        InetAddress address = InetAddress.getLoopbackAddress();
        JsonObject jsonObject = new JsonObject();
        String uriPath = "/", jsonString = "{}";
        Instant i1 = Instant.ofEpochMilli(500), i2 = Instant.ofEpochMilli(200);
        GameStateContext context = new GameStateContext(
                server, uriPath, previous, i1, i2, 43, address, authTokens, jsonObject, jsonString);
        
        // Notify observing object
        server.listeners.notify(state, context);
        assertTrue(observerLatch.await(OBSERVER_TIMEOUT, TimeUnit.MILLISECONDS)); // Wait for observers
        
        // Verify objects match
        assertSame(state, observer1.state);
        assertSame(state, observer2.state);
        assertSame(uriPath, observer1.context.getUriPath());
        assertSame(uriPath, observer2.context.getUriPath());
        assertSame(i1, observer1.context.getTimestamp());
        assertSame(i1, observer2.context.getTimestamp());
        assertSame(previous, observer1.context.getPreviousState().get());
        assertSame(previous, observer2.context.getPreviousState().get());
        assertSame(i2, observer1.context.getPreviousTimestamp().get());
        assertSame(i2, observer2.context.getPreviousTimestamp().get());
        assertEquals(300, observer1.context.getMillisSinceLastState().getAsInt());
        assertEquals(300, observer2.context.getMillisSinceLastState().getAsInt());
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
        CountDownLatch observerLatch = new CountDownLatch(1);
        MockListener observer = new MockListener(observerLatch);
        GSIServer server = new GSIServer.Builder(1337)
                .requireAuthTokens(expectedTokens)
                .registerListener(observer).build();
        server.handleStateUpdate(AUTH_TOKEN_JSON, "/", ADDRESS);
        return observerLatch.await(OBSERVER_TIMEOUT, TimeUnit.MILLISECONDS) && observer.called;
    }

}