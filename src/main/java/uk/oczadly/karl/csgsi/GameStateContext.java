package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

public final class GameStateContext {
    
    private final GSIServer server;
    private final GameState previousState;
    private final InetAddress address;
    private final Map<String, String> authTokens;
    private final JsonObject rawJson;
    
    public GameStateContext(GSIServer server, GameState previousState, InetAddress address,
                            Map<String, String> authTokens, JsonObject rawJson) {
        this.server = server;
        this.previousState = previousState;
        this.address = address;
        this.authTokens = Collections.unmodifiableMap(authTokens);
        this.rawJson = rawJson;
    }
    
    
    /**
     * @return the associated server object which triggered the associated callback
     */
    public GSIServer getGsiServer() {
        return server;
    }
    
    /**
     * @return the previous game state object from the {@link GSIServer} which triggered the associated callback
     */
    public GameState getPreviousState() {
        return previousState;
    }
    
    /**
     * @return the address of the game client which sent the associated state data
     */
    public InetAddress getAddress() {
        return address;
    }
    
    /**
     * @return the map of authentication tokens (passwords) configured in the game client
     */
    public Map<String, String> getAuthTokens() {
        return authTokens;
    }
    
    /**
     * @return the raw JSON data (as a Gson {@link JsonObject} object) sent from the game client
     */
    public JsonObject getRawJsonObject() {
        return rawJson;
    }
    
}
