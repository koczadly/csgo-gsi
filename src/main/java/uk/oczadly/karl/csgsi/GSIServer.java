package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.config.GSIConfig;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPServer;
import uk.oczadly.karl.csgsi.state.GameState;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static uk.oczadly.karl.csgsi.internal.Util.GSON;

/**
 * This class is used to listen for live game state information sent by the game client.
 *
 * <p>The class can be constructed using the provided {@link Builder} class, where you can set the listening port and
 * other configuration details for the {@link GSIServer} through the setter methods. The server can be started using the
 * {@link #start()} method, and listeners can be registered through the {@link #subscribe(GSIListener)} method,
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
 *  GSIServer server = new GSIServer.Builder(1337)        // Port 1337
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
    
    private static final Logger log = LoggerFactory.getLogger(GSIServer.class);

    final Lock newStateLock = new ReentrantLock(); // Used when accepting and processing new states
    final Object readWriteLock = new Object(); // Used only while reading and writing state/statistic data

    final HTTPServer server;
    final ListenerRegistry listeners = new ListenerRegistry();
    final Map<String, String> requiredAuthTokens;
    final boolean diagnosticsEnabled;
    final ServerStateContainer srvState = new ServerStateContainer(this); // Holds state and statistics


    GSIServer(InetSocketAddress bindAddr, Map<String, String> authTokens, Collection<GSIListener> listeners,
              boolean diagnosticsEnabled) {
        this.server = new HTTPServer(bindAddr, new GSIServerHTTPHandler(this));
        this.requiredAuthTokens = Collections.unmodifiableMap(authTokens);
        this.listeners.subscribe(listeners);
        this.diagnosticsEnabled = diagnosticsEnabled;
    }
    
    
    /**
     * @return the latest game state, or empty if the server has yet to receive an update
     */
    public Optional<GameState> getLatestGameState() {
        return srvState.getLatestState();
    }


    /**
     * Subscribes a new listener instance to the server, which will receive future game state data sent from the game
     * client.
     *
     * <p>New listeners may be registered while the server is active, though there are no guarantees that </p>
     *
     * <p>Listeners will be executed in parallel from different threads, though the server will wait for all listeners
     * to finish processing before allowing the game client to send an update. If you wish to perform long-lasting
     * tasks and don't want to block state updates, initiate a new thread from inside the listener.</p>
     *
     * @param listener the listener to register
     */
    public void subscribe(GSIListener listener) {
        listeners.subscribe(listener);
    }

    /**
     * Removes a listener from the list, and will no longer receive updates. Listeners can be removed while the server
     * is running, although they may still receive updates for a short period while being removed.
     *
     * @param listener the listener to unsubscribe
     */
    public void unsubscribe(GSIListener listener) {
        listeners.unsubscribe(listener);
    }

    /**
     * Removes all subscribed listeners from the registry.
     */
    public void clearListeners() {
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
        synchronized (readWriteLock) {
            if (server.isRunning())
                throw new IllegalStateException("The server is already running.");

            log.debug("Attempting to start GSI server on address {}...", server.getBindAddress());
            srvState.reset(true);
            server.start();
        }
        log.info("GSI server successfully started. Interface: {}, auth required: {}, diagnostics enabled: {}.",
                getBindAddress(), !requiredAuthTokens.isEmpty(), diagnosticsEnabled);
    }
    
    /**
     * Stops the server from listening for game state information, and frees the assigned network port.
     *
     * @throws IllegalStateException if the server is not currently running
     */
    public void stop() {
        log.debug("Attempting to stop GSI server running on interface {}...", getBindAddress());
        synchronized (readWriteLock) {
            if (!server.isRunning())
                throw new IllegalStateException("The server is not currently running.");

            server.stop();
            srvState.reset(false);
        }
        log.info("GSI server on interface {} successfully shut down.", getBindAddress());
    }
    
    /**
     * @return true if the server is currently running and listening
     */
    public boolean isRunning() {
        return server.isRunning();
    }
    
    /**
     * @return the port and address which the server will bind to
     */
    public InetSocketAddress getBindAddress() {
        return server.getBindAddress();
    }
    
    /**
     * @return an immutable map of required authentication tokens
     */
    public Map<String, String> getRequiredAuthTokens() {
        return requiredAuthTokens;
    }

    /**
     * @return true if one or more auth tokens are configured and required to be sent
     */
    public boolean requiresAuthTokens() {
        return !requiredAuthTokens.isEmpty();
    }
    
    
    /**
     * Handles a new JSON state and notifies the appropriate listeners.
     */
    boolean handleStateUpdate(String rawJson, String path, InetAddress address) {
        log.debug("Handling new state update on server running on interface {}...", getBindAddress());
        Instant receiveTime = Instant.now();

        // Obtain state lock
        if (!newStateLock.tryLock()) {
            log.warn("Received new state while still processing previous one!");
            srvState.incrementStateDiscardCounter();
            return false;
        }

        try {
            long startTime = System.nanoTime();

            // Parse as JSON object
            JsonObject json;
            try {
                json = JsonParser.parseString(rawJson).getAsJsonObject();
            } catch (JsonParseException e) {
                log.warn("GSI server received invalid JSON state!", e);
                srvState.incrementStateRejectCounter();
                return false;
            }

            // Parse auth tokens
            Map<String, String> authTokens = json.has("auth")
                    ? GSON.fromJson(json.getAsJsonObject("auth"), new TypeToken<Map<String, String>>(){}.getType())
                    : Collections.emptyMap();
            // Verify auth tokens
            boolean authValid = requiredAuthTokens.entrySet().stream()
                    .allMatch(t -> t.getValue().equals(authTokens.get(t.getKey())));
            if (!authValid) {
                log.warn("GSI state update rejected due to auth token mismatch");
                srvState.incrementStateRejectCounter();
                return false;
            }

            // Parse the game state and update
            GameState gameState = GSON.fromJson(json, GameState.class);
            GameStateContext stateContext = srvState.updateState(gameState,
                    path, receiveTime, address, authTokens, json, rawJson);

            // Log performance values
            long deserializeTime = System.nanoTime() - startTime;
            if (deserializeTime > 50_000_000) {
                log.warn("Deserializing state JSON took {}ms.", deserializeTime / 1_000_000);
            } else {
                log.debug("Deserializing state JSON took {}us.", deserializeTime / 1000);
            }

            // Notify listeners of new state and handle
            listeners.notify(gameState, stateContext);
            return true;
        } finally {
            newStateLock.unlock();
        }
    }
    
    /**
     * Used for configuring and constructing instances of {@link GSIServer} objects.
     *
     * <p>By default, the server will only bind to the local loopback address. If the game client will be ran from a
     * remote computer, invoke {@link #bindToAllInterfaces()}.</p>
     */
    public static class Builder {
        private final int bindPort;
        private InetAddress bindAddr;
        private final Map<String, String> authTokens = new HashMap<>();
        private final Set<GSIListener> listeners = new HashSet<>();
        private boolean diagnosticsEnabled = true;

        /**
         * Creates a new builder instance, binding the socket to port {@code 8080}.
         */
        public Builder() {
            this(8080);
        }

        /**
         * Creates a new builder instance, specifying the port to bind the socket to.
         * @param bindPort the socket port to bind to
         */
        public Builder(int bindPort) {
            if (bindPort <= 0 || bindPort > 65535)
                throw new IllegalArgumentException("Port number out of range");
            this.bindPort = bindPort;
            this.bindToLoopback();
        }


        /**
         * Binds the server socket to the specified network interface.
         *
         * @param bindAddr the address of the interface to bind to
         * @return this builder
         */
        public Builder bindToInterface(InetAddress bindAddr) {
            if (bindAddr == null)
                throw new IllegalArgumentException("Address cannot be null.");
            this.bindAddr = bindAddr;
            return this;
        }

        /**
         * Binds the server socket to all network interfaces.
         *
         * @return this builder
         */
        public Builder bindToAllInterfaces() {
            this.bindAddr = null;
            return this;
        }

        /**
         * Binds the server socket to the local loopback interface.
         *
         * @return this builder
         */
        public Builder bindToLoopback() {
            return bindToInterface(InetAddress.getLoopbackAddress());
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
                throw new IllegalArgumentException("Token map cannot be null.");
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
         * Subscribes a new listener instance to the server, which will receive game state data sent from the game
         * client.
         *
         * <p>Listeners will be executed in parallel from different threads, though the server will wait for all
         * listeners to finish processing before allowing the game client to send an update. If you wish to perform
         * long-lasting tasks and don't want to block state updates, initiate a new thread from inside the listener.</p>
         *
         * @param listener the listener to register
         * @return this builder
         *
         * @see GSIServer#subscribe(GSIListener)
         */
        public Builder subscribe(GSIListener listener) {
            listeners.add(listener);
            return this;
        }

        /**
         * Disables the HTTP diagnostics webpage (and {@code /api/} endpoints).
         *
         * <p>Accessing the page in the browser when disabled will return a generic 404 error.</p>
         *
         * @return this builder
         */
        public Builder disableDiagnosticsPage() {
            this.diagnosticsEnabled = false;
            return this;
        }
    
        /**
         * Constructs a new {@link GSIServer} instance with the configured parameters.
         *
         * @return a new {@link GSIServer} object
         */
        public GSIServer build() {
            return new GSIServer(
                    new InetSocketAddress(bindAddr, bindPort),
                    authTokens, listeners, diagnosticsEnabled);
        }
    }



}
