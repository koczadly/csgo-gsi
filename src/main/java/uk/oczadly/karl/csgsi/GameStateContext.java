package uk.oczadly.karl.csgsi;

import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

public class GameStateContext {
    
    private final GSIServer server;
    private final GameState previousState;
    private final InetAddress address;
    private final Map<String, String> authTokens;
    
    public GameStateContext(GSIServer server, GameState previousState, InetAddress address,
                            Map<String, String> authTokens) {
        this.server = server;
        this.previousState = previousState;
        this.address = address;
        this.authTokens = Collections.unmodifiableMap(authTokens);
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
    
}
