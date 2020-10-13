package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

/**
 * This class contains additional contextual information accompanying a {@link GameState} state update.
 */
public final class GameStateContext {
    
    private final GSIServer server;
    private final GameState previousState;
    private final int millisSinceLast;
    private final InetAddress address;
    private final Map<String, String> authTokens;
    private final JsonObject rawJson;
    private final String rawJsonString;
    
    GameStateContext(GSIServer server, GameState previousState, int millisSinceLast, InetAddress address,
                            Map<String, String> authTokens, JsonObject rawJson, String rawJsonString) {
        this.server = server;
        this.previousState = previousState;
        this.millisSinceLast = millisSinceLast;
        this.address = address;
        this.authTokens = Collections.unmodifiableMap(authTokens);
        this.rawJson = rawJson;
        this.rawJsonString = rawJsonString;
    }
    
    
    /**
     * Returns the game state server which triggered the callback.
     *
     * @return the {@link GSIServer} that triggered the callback
     */
    public GSIServer getGsiServer() {
        return server;
    }
    
    /**
     * Gets the previous game state object from the {@link GSIServer}.
     *
     * @return the previous game state
     */
    public GameState getPreviousState() {
        return previousState;
    }
    
    /**
     * Gets the number of milliseconds elapsed since the last state update.
     *
     * <p>This value is based on the local timestamps when the data was parsed, and <em>not</em> on the timestamp
     * included in the provider state. The first received game state will return {@code -1}.</p>
     *
     * @return the number of milliseconds since the last received state, or {@code -1} for the first state
     */
    public int getMillisSinceLastState() {
        return millisSinceLast;
    }
    
    /**
     * Gets the network address of the game client which sent the associated state data.
     *
     * @return the address of the game client
     */
    public InetAddress getAddress() {
        return address;
    }
    
    /**
     * Gets a map of the received authentication tokens (passwords) configured and received from the game client.
     *
     * @return a map of received authentication tokens
     */
    public Map<String, String> getAuthTokens() {
        return authTokens;
    }
    
    /**
     * Returns the raw JSON data (as a Gson {@link JsonObject}) received from the game client.
     *
     * @return the raw JSON data
     */
    public JsonObject getRawJsonObject() {
        return rawJson;
    }
    
    /**
     * Returns the raw JSON data (in String form) received from the game client.
     *
     * @return the raw JSON data
     */
    public String getRawJsonString() {
        return rawJsonString;
    }
    
}
