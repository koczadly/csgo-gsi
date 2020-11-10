package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.config.GSIConfig;
import uk.oczadly.karl.csgsi.internal.Util;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPServer;
import uk.oczadly.karl.csgsi.state.GameState;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is used to listen for live game state information sent by the game client.
 *
 * <p>The class can be constructed using the provided {@link Builder} class, where you can set the listening port and
 * other configuration details for the {@link GSIServer} through the setter methods. The server can be started using the
 * {@link #start()} method, and listeners can be registered through the {@link #registerListener(GSIListener)} method,
 * which subscribes the listener to new game state information as it is received. Listeners may be registered while
 * the server is active.</p>
 *
 * <p>The example below demonstrates how to use this class:</p>
 * <pre>
 *  // Create a new listener (for this example, using a lambda)
 *  GSIListener listener = (state, context) -&gt; {
 *      // Access state information with the 'state' object...
 *      System.out.println("New state from game client address " + context.getAddress().getHostAddress());
 *
 *      state.getProvider().ifPresent(provider -&gt; {
 *          System.out.println("Client SteamID: " + provider.getClientSteamId());
 *      });
 *      state.getMap().ifPresent(map -&gt; {
 *          System.out.println("Current map: " + map.getName());
 *      });
 *  };
 *
 *  // Configure server
 *  GSIServer server = new GSIServer.Builder(1337)        // Port 1337, on all network interfaces
 *          .requireAuthToken("password", "Q79v5tcxVQ8u") // Require the specified password
 *          .registerListener(listener)                   // Alternatively, you can call this on the GSIServer
 *          .build();
 *
 *  // Start server
 *  try {
 *      server.start(); // Start the server (will run in a separate thread)
 *      System.out.println("Server started. Listening for state data...");
 *  } catch (IOException e) {
 *      System.out.println("Could not start server.");
 *  }
 * </pre>
 *
 * <p>If the diagnostics page is enabled (by default it is), then you can access the GSI server from a web browser
 * through the HTTP protocol (eg. <a href="http://localhost:1337/">http://localhost:1337</a>
 * for port 1337 running on the local machine). Information about the {@link GSIServer} instance, along with
 * miscellaneous state information will be displayed. If one or more authentication keys are configured, then the latest
 * state's JSON will not be viewable from the web panel (to prevent the auth keys from being publicly exposed).</p>
 *
 * @see GSIConfig
 */
public final class GSIServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GSIServer.class);
    
    final HTTPServer server;
    final ListenerRegistry listeners = new ListenerRegistry();
    final Map<String, String> requiredAuthTokens;
    final boolean diagPageEnabled;
    
    volatile Instant serverStartTimestamp;
    final ServerStats stats = new ServerStats(); // Holds statistics on the server and state
    
    
    GSIServer(InetAddress bindAddr, int port, Map<String, String> authTokens,
              Collection<GSIListener> listeners, boolean diagPageEnabled) {
        this.server = new HTTPServer(port, bindAddr, new GSIServerHTTPHandler(this));
        this.requiredAuthTokens = Collections.unmodifiableMap(authTokens);
        this.listeners.register(listeners);
        this.diagPageEnabled = diagPageEnabled;
    }
    
    
    /**
     * Constructs a new GSIServer object with pre-processed client authentication.
     *
     * @param port               the network port for the server to listen on
     * @param bindAddr           the local address to bind to
     * @param requiredAuthTokens the authentication tokens required to accept state reports
     *
     * @deprecated Use inner builder class {@link Builder} to create {@link GSIServer} instances
     * @see Builder
     */
    @Deprecated(forRemoval = true)
    public GSIServer(int port, InetAddress bindAddr, Map<String, String> requiredAuthTokens) {
        //Validate port
        if (port <= 0 || port > 65535)
            throw new IllegalArgumentException("Port number out of range");
        
        //Validate auth tokens
        if (requiredAuthTokens != null) {
            // Validate map
            for (Map.Entry<String, String> key : requiredAuthTokens.entrySet())
                if (key.getKey() == null || key.getValue() == null)
                    throw new IllegalArgumentException("Auth token key or value cannot be null");
            this.requiredAuthTokens = Collections.unmodifiableMap(new HashMap<>(requiredAuthTokens));
        } else {
            this.requiredAuthTokens = Collections.emptyMap();
        }
        
        this.server = new HTTPServer(port, bindAddr, new GSIServerHTTPHandler(this));
        this.diagPageEnabled = true;
    }
    
    /**
     * Constructs a new GSIServer object with pre-processed client authentication.
     *
     * @param port               the network port for the server to listen on
     * @param requiredAuthTokens the authentication tokens required to accept state reports
     *
     * @deprecated Use inner builder class {@link Builder} to create {@link GSIServer} instances
     * @see Builder
     */
    @Deprecated(forRemoval = true)
    public GSIServer(int port, Map<String, String> requiredAuthTokens) {
        this(port, null, requiredAuthTokens);
    }
    
    /**
     * Constructs a new GSIServer object with no pre-processed client authentication.
     *
     * @param port     the network port for the server to listen on
     * @param bindAddr the local address to bind to
     *
     * @deprecated Use inner builder class {@link Builder} to create {@link GSIServer} instances
     * @see Builder
     */
    @Deprecated(forRemoval = true)
    public GSIServer(int port, InetAddress bindAddr) {
        this(port, bindAddr, new HashMap<>());
    }
    
    /**
     * Constructs a new GSIServer object with no pre-processed client authentication.
     *
     * @param port the network port for the server to listen on
     *
     * @deprecated Use inner builder class {@link Builder} to create {@link GSIServer} instances
     * @see Builder
     */
    @Deprecated(forRemoval = true)
    public GSIServer(int port) {
        this(port, (InetAddress)null);
    }
    
    
    /**
     * @return the latest game state, or null if the server has yet to receive an update
     */
    public GameState getLatestGameState() {
        return stats.latestState;
    }
    
    /**
     * @return true if the server has received at least one valid game state since starting
     */
    public boolean hasReceivedState() {
        return stats.latestState != null;
    }
    
    
    /**
     * Subscribes a new listener to receive game state information when sent by the game client. New listeners can be
     * registered regardless of the running state of the server.
     *
     * @param listener the listener to register
     */
    public void registerListener(GSIListener listener) {
        listeners.register(listener);
    }
    
    /**
     * Removes a listener from the list, and will no longer receive updates. Listeners can be removed while the server
     * is running, although they may still receive updates for a short period while being removed.
     *
     * @param listener the listener to unsubscribe
     */
    public void removeListener(GSIListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Removes all subscribed listeners from the registry.
     */
    public void removeAllListeners() {
        listeners.clear();
    }
    
    
    /**
     * Starts the server on the configured network port and listens for game state information. This server is ran from
     * a newly issued thread, and can safely be called from the main application thread.
     *
     * @throws IllegalStateException if the server is already running
     * @throws IOException           if the configured port cannot be bound to
     */
    public void start() throws IOException {
        if (server.isRunning())
            throw new IllegalStateException("The GSI server is already running.");
        
        LOGGER.debug("Attempting to start GSI server on port {}...", server.getPort());
        
        stats.latestState = null;
        stats.latestContext = null;
        stats.stateRejectCounter.set(0);
        stats.stateCounter.set(0);
        serverStartTimestamp = Instant.now();
        
        server.start();
        LOGGER.info("GSI server successfully started.");
        LOGGER.info("Port: {}, bind IP: {}, auth required: {}, diagnostics enabled: {}.",
                server.getPort(), server.getBindAddress(), !requiredAuthTokens.isEmpty(), diagPageEnabled);
    }
    
    /**
     * Stops the server from listening for game state information, and frees the assigned network port.
     *
     * @throws IllegalStateException if the server is not currently running
     */
    public void stop() {
        LOGGER.debug("Attempting to stop GSI server running on port {}...", server.getPort());
        server.stop();
        LOGGER.info("GSI server on port {} successfully shut down.", server.getPort());
    }
    
    /**
     * @return true if the server is currently running and listening
     */
    public boolean isRunning() {
        return server.isRunning();
    }
    
    /**
     * @return the port which the server will listen on
     */
    public int getPort() {
        return server.getPort();
    }
    
    /**
     * @return the address which the server will bind to, or null if it will bind to all local addresses
     */
    public InetAddress getBindingAddress() {
        return server.getBindAddress();
    }
    
    /**
     * @return an immutable map of required authentication tokens
     */
    public Map<String, String> getRequiredAuthTokens() {
        return requiredAuthTokens;
    }
    
    
    /**
     * Handles a new JSON state and notifies the appropriate listeners.
     */
    void handleStateUpdate(String json, String path, InetAddress address) {
        LOGGER.debug("Handling new state update on server running on port {}...", getPort());
        
        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(json).getAsJsonObject();
        } catch (JsonParseException e) {
            LOGGER.warn("GSI server received invalid JSON object", e);
            return;
        }
        
        Map<String, String> authTokens = verifyStateAuth(jsonObject);
        if (authTokens == null) {
            stats.stateRejectCounter.incrementAndGet();
            LOGGER.warn("GSI state update rejected due to auth token mismatch");
            return;
        }
        
        // Parse the game state into an object
        GameState state = Util.GSON.fromJson(jsonObject, GameState.class);
    
        // Calculate information
        int counter = this.stats.stateCounter.incrementAndGet();
        Instant now = Instant.now();
        
        // Create context object
        GameStateContext context = new GameStateContext(this, path, this.stats.latestState, now,
                this.stats.latestContext != null ? this.stats.latestContext.getTimestamp() : null,
                counter, address, authTokens, jsonObject, json);
        
        // Update latest state and timestamps
        this.stats.latestState = state;
        this.stats.latestContext = context;
        
        // Notify listeners
        listeners.notify(state, context);
    }
    
    private Map<String, String> verifyStateAuth(JsonObject json) {
        // Parse auth tokens
        Map<String, String> authTokens = Util.GSON.fromJson(json.getAsJsonObject("auth"),
                new TypeToken<Map<String, String>>() {}.getType());
        authTokens = (authTokens != null) ? authTokens : Collections.emptyMap();
        
        // Verify auth tokens
        for (Map.Entry<String, String> token : requiredAuthTokens.entrySet()) {
            String val = authTokens.get(token.getKey());
            if (!token.getValue().equals(val)) {
                return null; // Invalid auth token(s), skip
            }
        }
        return authTokens;
    }
    
    
    /**
     * Used for configuring and constructing instances of {@link GSIServer} objects.
     */
    public static class Builder {
        private final int bindPort;
        private final InetAddress bindAddr;
        private final Map<String, String> authTokens = new HashMap<>();
        private final Set<GSIListener> listeners = new HashSet<>();
        private boolean diagPageEnabled = true;
    
    
        /**
         * Creates a builder with the specified port, binding to all IP interfaces.
         * @param bindPort the socket port to bind to
         */
        public Builder(int bindPort) {
            this(null, bindPort);
        }
    
        /**
         * Creates a builder with the specified port, binding to all IP interfaces.
         * @param bindAddr the socket IP address to bind to, or null to bind to all
         * @param bindPort the socket port to bind to
         */
        public Builder(InetAddress bindAddr, int bindPort) {
            if (bindPort <= 0 || bindPort > 65535)
                throw new IllegalArgumentException("Port number out of range");
            
            this.bindAddr = bindAddr;
            this.bindPort = bindPort;
        }
        
    
        /**
         * Adds a map of authentication tokens which are required by the server. Any state updates which do not
         * contain these key/value entries will be rejected.
         *
         * <p>This method will add to any existing required authentication tokens configured, and will not clear them.
         * Any overlapping key entries will be overwritten.</p>
         *
         * @param authTokens a map of auth tokens (case sensitive)
         * @return this builder
         */
        public Builder requireAuthTokens(Map<String, String> authTokens) {
            if (authTokens == null)
                throw new IllegalArgumentException("authTokens cannot be null.");
            for (Map.Entry<String, String> key : authTokens.entrySet())
                if (key.getKey() == null || key.getValue() == null)
                    throw new IllegalArgumentException("Auth token key or value cannot be null.");
            
            this.authTokens.putAll(authTokens);
            return this;
        }
    
        /**
         * Adds an authentication token which will be required by the server. Any state updates which do not contain
         * these key/value entries will be rejected.
         *
         * <p>This method may be called multiple times if multiple authentication tokens are required, and will not
         * clear existing ones. If the key has already been used, then the existing entry will be overwritten.</p>
         *
         * @param key   the key (case sensitive)
         * @param value the value/password (case sensitive)
         * @return this builder
         */
        public Builder requireAuthToken(String key, String value) {
            if (key == null || value == null)
                throw new IllegalArgumentException("Auth token key or value cannot be null.");
            
            authTokens.put(key, value);
            return this;
        }
        
        /**
         * Pre-registers a listener instance to listen to state updates.
         *
         * @param listener the listener to register
         * @return this builder
         * 
         * @see GSIServer#registerListener(GSIListener)
         */
        public Builder registerListener(GSIListener listener) {
            listeners.add(listener);
            return this;
        }
        
        /**
         * Disables the HTTP diagnostics page, instead returning an HTTP error.
         *
         * @return this builder
         */
        public Builder disableDiagnosticsPage() {
            this.diagPageEnabled = false;
            return this;
        }
    
        /**
         * Constructs a new {@link GSIServer} with the specified parameters.
         *
         * @return a new {@link GSIServer} object
         */
        public GSIServer build() {
            return new GSIServer(bindAddr, bindPort, authTokens, listeners, diagPageEnabled);
        }
    }
    
    
    static class ServerStats {
        volatile GameState latestState;
        volatile GameStateContext latestContext;
        final AtomicInteger stateCounter = new AtomicInteger();
        final AtomicInteger stateRejectCounter = new AtomicInteger();
    }
    
}
