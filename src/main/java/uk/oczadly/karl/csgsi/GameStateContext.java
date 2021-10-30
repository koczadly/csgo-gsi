package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * This class contains additional contextual information accompanying a {@link GameState} state update.
 */
public final class GameStateContext {
    
    private final GSIServer server;
    private final GameState previousState;
    private final Instant timestamp, prevTimestamp;
    private final int sequenceIndex;
    private final InetAddress address;
    private final Map<String, String> authTokens;
    private final JsonObject rawJson;
    private final String uriPath, rawJsonString;
    
    GameStateContext(GSIServer server, String uriPath, GameState previousState, Instant timestamp,
                     Instant prevTimestamp, int sequenceIndex, InetAddress address, Map<String, String> authTokens,
                     JsonObject rawJson, String rawJsonString) {
        this.server = server;
        this.uriPath = uriPath;
        this.previousState = previousState;
        this.timestamp = timestamp;
        this.prevTimestamp = prevTimestamp;
        this.sequenceIndex = sequenceIndex;
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
     * Gets the elapsed time duration between this state and the previous, returning empty if this is the first
     * received state.
     *
     * <p>This value is based on local timestamps of when the data was received and parsed, and <em>not</em> on the
     * timestamp provided by the client in the {@code provider} component.</p>
     *
     * @return the time interval between this state and the previous, or <em>empty</em> for the first state
     */
    public Optional<Duration> getUpdateTimeInterval() {
        return getPreviousStateTimestamp().map(pt -> Duration.between(pt, timestamp));
    }
    
    /**
     * Gets the local timestamp of when this state update was received.
     *
     * @return the timestamp of this state
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the local timestamp of when the previous state was received.
     *
     * @return the timestamp of the previous state, or <em>empty</em> for the first state
     */
    public Optional<Instant> getPreviousStateTimestamp() {
        return Optional.ofNullable(prevTimestamp);
    }
    
    /**
     * Returns the sequential index of this state update. The first state will return a value of {@code 0}, with each
     * additional state incrementing the index by {@code 1}.
     *
     * @return the sequential index of this state
     */
    public int getSequenceIndex() {
        return sequenceIndex;
    }
    
    /**
     * Gets the network address of the game client which sent the associated state data. For local instances, this will
     * return the local {@link InetAddress#getLoopbackAddress() loopback address} (typically {@code 127.0.0.1}).
     *
     * @return the address of the game client
     */
    public InetAddress getClientAddress() {
        return address;
    }
    
    /**
     * Returns an immutable map of the received authentication tokens (passwords) sent by the game client.
     *
     * @return an immutable map of auth tokens sent by the game
     */
    public Map<String, String> getAuthTokens() {
        return authTokens;
    }
    
    /**
     * Returns the raw state JSON data (as a Gson {@link JsonObject}) sent by the game client.
     *
     * @return the raw state JSON
     */
    public JsonObject getStateJson() {
        return rawJson;
    }
    
    /**
     * Returns the unmodified JSON state data sent from the client. This will preserve any formatting and indentation
     * sent from the game.
     *
     * @return the raw data sent from the game client
     */
    public String getRawStateContents() {
        return rawJsonString;
    }
    
}
