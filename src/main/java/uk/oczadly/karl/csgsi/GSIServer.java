package uk.oczadly.karl.csgsi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.httpserver.HTTPConnectionHandler;
import uk.oczadly.karl.csgsi.httpserver.HTTPServer;
import uk.oczadly.karl.csgsi.state.GameState;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GSIServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GSIServer.class);
    
    private final HTTPServer server;
    private final HTTPConnectionHandler handler = new Handler();
    
    private final Set<GSIObserver> observers = new CopyOnWriteArraySet<>();
    private final ExecutorService observerExecutor;
    
    private final Gson gson;
    
    private volatile GameState latestGameState;
    
    
    /**
     * @param port              the network port for the server to listen on
     * @param observerExecutor  the executor used to notify observers
     */
    public GSIServer(int port, ExecutorService observerExecutor) {
        this.server = new HTTPServer(port, 1, handler);
        this.observerExecutor = observerExecutor;
        this.gson = new Gson(); //TODO?
    }
    
    /**
     * @param port the network port for the server to listen on
     */
    public GSIServer(int port) {
        this(port, Executors.newCachedThreadPool());
    }
    
    
    /**
     * @return the latest game state, or null if the server has yet to receive an update
     */
    public GameState getLatestGameState() {
        return latestGameState;
    }
    
    
    /**
     * Subscribes a new observer to receive game state information when sent by the game client.
     * @param observer the observer to register
     */
    public void registerObserver(GSIObserver observer) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("New observer #{} registered for GSI server on port {}", Integer.toHexString(observer.hashCode()), server.getPort());
        
        observers.add(observer);
    }
    
    /**
     * Removes an observer from the list, and will no longer receive updates.
     * @param observer the observer to unsubscribe
     */
    public void removeObserver(GSIObserver observer) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Removing observer #{} from GSI server on port {}", Integer.toHexString(observer.hashCode()), server.getPort());
        
        observers.remove(observer);
    }
    
    /**
     * Notifies the registered observers of an updated state.
     * @param state         the new game state information
     * @param previousState the previous game state information
     * @param addr          the network address of the client
     */
    protected void notifyObservers(GameState state, GameState previousState, InetAddress addr) {
        LOGGER.debug("Notifying {} observers of new GSI state from server on port {}", observers.size(), server.getPort());
        
        for (GSIObserver observer : observers) {
            observerExecutor.submit(() -> observer.update(state, previousState, addr));
        }
    }
    
    
    /**
     * Starts the server on the configured network port and listens for game state information.
     * @throws IllegalStateException if the server is already running
     * @throws IOException if the configured port cannot be bound to
     */
    public void startServer() throws IOException {
        LOGGER.debug("Attempting to start GSI server on port {}...", server.getPort());
        
        if (server.isRunning())
            throw new IllegalStateException("The GSI server is already running.");
        
        latestGameState = null;
        server.start();
        LOGGER.info("GSI server on port {} successfully started", server.getPort());
    }
    
    /**
     * Stops the server from listening for game state information, and frees the assigned network port.
     * @throws IllegalStateException if the server is not currently running
     */
    public void stopServer() {
        LOGGER.debug("Attempting to stop GSI server running on port {}...", server.getPort());
        server.stop();
        LOGGER.info("GSI server on port {} successfully shut down", server.getPort());
    }
    
    /**
     * @return true if the server is currently running and listening
     */
    public boolean isServerRunning() {
        return server.isRunning();
    }
    
    
    
    /** Handles HTTP connection requests */
    private class Handler implements HTTPConnectionHandler {
        @Override
        public void handle(InetAddress address, String path, String method, Map<String, String> headers, String body) {
            GameState state = gson.fromJson(body, GameState.class);
            notifyObservers(state, latestGameState, address);
            latestGameState = state;
        }
    }
    
}
