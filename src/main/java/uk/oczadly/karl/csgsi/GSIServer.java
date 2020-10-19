package uk.oczadly.karl.csgsi;

import com.google.gson.Gson;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is used to listen for live game state information sent by the game client.
 *
 * <p>The class can be constructed using the provided {@link Builder} class, where you can set the listening port and
 * other configuration details for the {@link GSIServer} through the setter methods. The server can be started using the
 * {@link #start()} method, and observers can be registered through the {@link #registerObserver(GSIObserver)} method,
 * which subscribes the observer to new game state information as it is received. Observers may be registered while
 * the server is active.</p>
 *
 * <p>The example below demonstrates how to use this class:</p>
 * <pre>
 *     // Create observer
 *     GSIObserver observer = (state, context) -> {
 *         System.out.println("New state from game client address " + context.getAddress().getHostAddress());
 *         if (state.getProviderDetails() != null) {
 *             System.out.println("Client SteamID: " + state.getProviderDetails().getClientSteamId());
 *         }
 *         if (state.getMapState() != null) {
 *             System.out.println("Current map: " + state.getMapState().getName());
 *         }
 *     };
 *
 *     // Configure server (port 1337, requiring password)
 *     GSIServer server = new GSIServer.Builder(1337)
 *             .addRequiredAuthToken("password", "Q79v5tcxVQ8u")
 *             .registerObserver(observer) // Alternatively, you can call this on the GSIServer
 *             .build();
 *
 *     // Start server
 *     try {
 *         server.start(); // Start the server (runs in a separate thread)
 *         System.out.println("Server started. Listening for state data...");
 *     } catch (IOException e) {
 *         System.out.println("Could not start server.");
 *     }
 * </pre>
 *
 * @see GSIConfig
 */
public final class GSIServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GSIServer.class);
    private static final Gson GSON = Util.GSON;
    private static final ExecutorService OBS_EXECUTOR = Executors.newCachedThreadPool();
    
    final HTTPServer server;
    final Set<GSIObserver> observers = new CopyOnWriteArraySet<>();
    final Map<String, String> requiredAuthTokens;
    
    volatile Instant serverStartTimestamp;
    volatile GameState latestGameState;
    volatile GameStateContext latestStateContext;
    final AtomicInteger stateCounter = new AtomicInteger();
    final AtomicInteger stateRejectCounter = new AtomicInteger();
    final Object stateLock = new Object(); // Used to synchronize state updates
    
    
    GSIServer(InetAddress bindAddr, int port, Map<String, String> authTokens,
              Collection<GSIObserver> observers) {
        this.requiredAuthTokens = Collections.unmodifiableMap(authTokens);
        this.observers.addAll(observers);
        this.server = new HTTPServer(port, bindAddr, new GSIServerHTTPHandler(this));
    }
    
    
    /**
     * Constructs a new GSIServer object with pre-processed client authentication.
     *
     * @param port               the network port for the server to listen on
     * @param bindAddr           the local address to bind to
     * @param requiredAuthTokens the authentication tokens required to accept state reports
     *
     * @deprecated Use inner builder class {@link Builder} to create {@link GSIServer} instances
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
    }
    
    /**
     * Constructs a new GSIServer object with pre-processed client authentication.
     *
     * @param port               the network port for the server to listen on
     * @param requiredAuthTokens the authentication tokens required to accept state reports
     *
     * @deprecated Use inner builder class {@link Builder} to create {@link GSIServer} instances
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
     */
    @Deprecated(forRemoval = true)
    public GSIServer(int port) {
        this(port, (InetAddress)null);
    }
    
    
    /**
     * @return the latest game state, or null if the server has yet to receive an update
     */
    public GameState getLatestGameState() {
        return latestGameState;
    }
    
    /**
     * @return true if the server has received at least one valid game state since starting
     */
    public boolean hasReceivedState() {
        return latestGameState != null;
    }
    
    
    /**
     * Subscribes a new observer to receive game state information when sent by the game client. New observers can be
     * registered regardless of the running state of the server.
     *
     * @param observer the observer to register
     */
    public void registerObserver(GSIObserver observer) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("New observer #{} registered for GSI server on port {}",
                    Integer.toHexString(System.identityHashCode(observer)), server.getPort());
        
        observers.add(observer);
    }
    
    /**
     * Removes an observer from the list, and will no longer receive updates. Observers can be removed while the server
     * is running, although they may still receive updates for a short period while being removed.
     *
     * @param observer the observer to unsubscribe
     */
    public void removeObserver(GSIObserver observer) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Removing observer #{} from GSI server on port {}",
                    Integer.toHexString(System.identityHashCode(observer)), server.getPort());
        
        observers.remove(observer);
    }
    
    /**
     * Removes all subscribed observers from the registry.
     */
    public void clearObservers() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Removing all observers from GSI server on port {}", server.getPort());
        observers.clear();
    }
    
    /**
     * Notifies the registered observers of an updated state.
     *
     * @param state         the new game state information
     * @param context       the game state and request context
     */
    protected void notifyObservers(GameState state, GameStateContext context) {
        LOGGER.debug("Notifying {} observers of new GSI state from server on port {}",
                observers.size(), server.getPort());
        
        List<Future<?>> futures = new ArrayList<>(observers.size());
        for (GSIObserver observer : observers) {
            futures.add(OBS_EXECUTOR.submit(() -> observer.update(state, context)));
        }
        
        // Wait for all tasks to complete (and log any errors)
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                LOGGER.error("Uncaught exception in GSIServer observer notification", e.getCause());
            }
        }
        LOGGER.debug("Finished notifying state observers.");
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
        
        synchronized (stateLock) {
            latestGameState = null;
            latestStateContext = null;
            stateRejectCounter.set(0);
            stateCounter.set(0);
        }
        serverStartTimestamp = Instant.now();
        
        server.start();
        LOGGER.info("GSI server on port {} successfully started.", server.getPort());
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
     * Used for unit tests
     */
    ExecutorService getObserverExecutorService() {
        return OBS_EXECUTOR;
    }
    
    
    /**
     * Handles a new JSON state and notifies the appropriate observers.
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
            stateRejectCounter.incrementAndGet();
            LOGGER.warn("GSI state update rejected due to auth token mismatch");
            return;
        }
        
        // Parse the game state into an object
        GameState state = GSON.fromJson(jsonObject, GameState.class);
    
        // Calculate information
        int counter;
        synchronized (stateLock) {
            counter = stateCounter.incrementAndGet();
        }
        Instant now = Instant.now();
        
        // Create context object
        GameStateContext context = new GameStateContext(this, path, latestGameState, now,
                latestStateContext != null ? latestStateContext.getTimestamp() : null,
                counter, address, authTokens, jsonObject, json);
        
        // Update latest state and timestamps
        synchronized (stateLock) {
            latestGameState = state;
            latestStateContext = context;
        }
        
        // Notify observers
        notifyObservers(state, context);
    }
    
    private Map<String, String> verifyStateAuth(JsonObject json) {
        // Parse auth tokens
        Map<String, String> authTokens = GSON.fromJson(json.getAsJsonObject("auth"),
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
        private final Set<GSIObserver> observers = new HashSet<>();
    
    
        /**
         * Creates a builder with the specified port, binding to all IP interfaces.
         * @param bindPort the socket port to bind to
         */
        public Builder(int bindPort) {
            this(null, bindPort);
        }
    
        /**
         * Creates a builder with the specified port, binding to all IP interfaces.
         * @param bindAddr the socket IP address to bind to
         * @param bindPort the socket port to bind to
         */
        public Builder(InetAddress bindAddr, int bindPort) {
            this.bindAddr = bindAddr;
            this.bindPort = bindPort;
        }
    
    
        /**
         * @return the port which the server will listen on
         */
        public int getBindPort() {
            return bindPort;
        }
    
        /**
         * @return the IP address the server will bind to
         */
        public InetAddress getBindAddress() {
            return bindAddr;
        }
    
        /**
         * @return an immutable map of authentication tokens required
         */
        public Map<String, String> getRequiredAuthTokens() {
            return Collections.unmodifiableMap(authTokens);
        }
    
        /**
         * Sets the map of authentication tokens which are required by the server. Any state updates which do not
         * contain these key/value entries will be rejected.
         *
         * @param authTokens a map of auth tokens (case sensitive)
         * @return this builder
         */
        public Builder setRequiredAuthTokens(Map<String, String> authTokens) {
            if (authTokens != null) {
                for (Map.Entry<String, String> key : authTokens.entrySet())
                    if (key.getKey() == null || key.getValue() == null)
                        throw new IllegalArgumentException("Auth token key or value cannot be null.");
                this.authTokens.clear();
                this.authTokens.putAll(authTokens);
            } else {
                this.authTokens.clear();
            }
            return this;
        }
    
        /**
         * Adds an authentication token which will be required by the server. Any state updates which do not contain
         * these key/value entries will be rejected.
         *
         * @param key   the key (case sensitive)
         * @param value the value/password (case sensitive)
         * @return this builder
         */
        public Builder addRequiredAuthToken(String key, String value) {
            if (key == null || value == null)
                throw new IllegalArgumentException("Auth token key or value cannot be null.");
            authTokens.put(key, value);
            return this;
        }
    
        /**
         * @return a collection of pre-registered observer instances
         */
        public Collection<GSIObserver> getObservers() {
            return Collections.unmodifiableSet(observers);
        }
    
        /**
         * Pre-registers an observer instance to listen to state updates.
         *
         * @param observer the observer to register
         * @return this builder
         */
        public Builder registerObserver(GSIObserver observer) {
            observers.add(observer);
            return this;
        }
    
        /**
         * Removes all pre-registered observers.
         * @return this builder
         */
        public Builder clearObservers() {
            observers.clear();
            return this;
        }
    
        /**
         * Constructs a new {@link GSIServer} with the specified parameters.
         *
         * @return a new {@link GSIServer} object
         */
        public GSIServer build() {
            return new GSIServer(bindAddr, bindPort, authTokens, observers);
        }
    }
    
}
