package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * This class contains additional contextual information accompanying a {@link GameState} state update.
 */
public final class GameStateContext {
    
    private final GSIServer server;
    private final GameState previousState;
    private final Instant timestamp, prevTimestamp;
    private final int counter;
    private final InetAddress address;
    private final Map<String, String> authTokens;
    private final JsonObject rawJson;
    private final String uriPath, rawJsonString;
    
    GameStateContext(GSIServer server, String uriPath, GameState previousState, Instant timestamp,
                     Instant prevTimestamp, int counter, InetAddress address, Map<String, String> authTokens,
                     JsonObject rawJson, String rawJsonString) {
        this.server = server;
        this.uriPath = uriPath;
        this.previousState = previousState;
        this.timestamp = timestamp;
        this.prevTimestamp = prevTimestamp;
        this.counter = counter;
        this.address = address;
        this.authTokens = Collections.unmodifiableMap(authTokens);
        this.rawJson = rawJson;
        this.rawJsonString = rawJsonString;
    }
    
    
    /**
     * Gets the game state server which triggered this callback.
     *
     * @return the {@link GSIServer} that triggered the callback
     */
    public GSIServer getGsiServer() {
        return server;
    }
    
    /**
     * Gets the URI path which the game client sent the state HTTP request to.
     *
     * @return the URI of the request
     */
    public String getUriPath() {
        return uriPath;
    }
    
    /**
     * Gets the previous game state object from the {@link GSIServer}.
     *
     * @return the previous game state, or <em>empty</em> if it's the first state
     */
    public Optional<GameState> getPreviousState() {
        return Optional.ofNullable(previousState);
    }
    
    /**
     * Gets the number of milliseconds elapsed since the last state update.
     *
     * <p>This value is based on the local timestamps when the data was parsed, and <em>not</em> on the timestamp
     * included in the provider state.</p>
     *
     * @return the number of milliseconds since the last received state, or <em>empty</em> for the first state
     */
    public OptionalInt getMillisSinceLastState() {
        if (prevTimestamp == null)
            return OptionalInt.empty();
        return OptionalInt.of((int)Duration.between(prevTimestamp, timestamp).toMillis());
    }
    
    /**
     * Gets the local timestamp of when this state update was received (now).
     *
     * @return the timestamp of this state
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the local timestamp of when the previous state was received.
     *
     * @return the timestamp of the previous state, or <em>empty</em> if it's the first state
     */
    public Optional<Instant> getPreviousTimestamp() {
        return Optional.ofNullable(prevTimestamp);
    }
    
    /**
     * Gets the current state counter, where each new state increases the value by one, starting at {@code 1}.
     *
     * @return the index counter of this state
     */
    public int getSequentialCounter() {
        return counter;
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
     * Gets a map of the received authentication tokens (passwords) received from the game client.
     *
     * @return a map of received authentication tokens
     */
    public Map<String, String> getAuthTokens() {
        return authTokens;
    }
    
    /**
     * Returns the raw JSON data (as a Gson {@link JsonObject}) sent by the game client.
     *
     * @return the raw JSON data
     */
    public JsonObject getRawJsonObject() {
        return rawJson;
    }
    
    /**
     * Returns the raw string data sent by the game client.
     *
     * @return the raw JSON data
     */
    public String getRawJsonString() {
        return rawJsonString;
    }
    
}
