package uk.oczadly.karl.csgsi.state.context;

import com.google.gson.JsonObject;
import uk.oczadly.karl.csgsi.server.GameStateServer;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * This class contains additional contextual information accompanying a {@link GameState} state update.
 */
//todo redocument/check existing javadocs
public final class GameStateContext {

    private final GameStateServer server;
    private final GameState previousState;
    private final Instant timestamp, prevTimestamp;
    private final int sequenceIndex;
    private final InetAddress address;
    private final AuthTokenMap authTokens;
    private final JsonObject rawJson;
    private final String uriPath, rawJsonString;


    public GameStateContext(GameStateServer server, String uriPath, GameState previousState, Instant timestamp,
                            Instant prevTimestamp, int sequenceIndex, InetAddress address,
                            AuthTokenMap authTokens, JsonObject rawJson, String rawJsonString) {
        this.server = server;
        this.uriPath = uriPath;
        this.previousState = previousState;
        this.timestamp = timestamp;
        this.prevTimestamp = prevTimestamp;
        this.sequenceIndex = sequenceIndex;
        this.address = address;
        this.authTokens = authTokens;
        this.rawJson = rawJson;
        this.rawJsonString = rawJsonString;
    }
    
    
    /**
     * Gets the associated game state server ({@link GameStateServer}) which received and processed the game state.
     *
     * @return the associated {@link GameStateServer}
     */
    public GameStateServer getServer() {
        return server;
    }
    
    /**
     * Gets the URI path which the game client sent the state HTTP request to. This is typically "{@code /}", the root
     * path.
     *
     * @return the URI of the request
     */
    public String getPath() {
        return uriPath;
    }

    /**
     * Returns whether the associated state is the first one to be received by the {@link GameStateServer}.
     * @return true if this is the first state received
     */
    public boolean isFirst() {
        return previousState == null;
    }

    public boolean isHeartbeat() {
        return !isFirst() && !rawJson.has("added") && !rawJson.has("previously");
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
     * Computes and returns the current time elapsed since the server first received this state from the game client.
     *
     * <p>When reading immediately after notification of a new state this should only be a few milliseconds, but may
     * be higher on slower machines.</p>
     *
     * @return the age of this state since first received
     */
    public Duration getAge() {
        return Duration.between(getTimestamp(), Instant.now());
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
    public Optional<Duration> getInterval() {
        return getPreviousTimestamp().map(pt -> Duration.between(pt, timestamp));
    }

    /**
     * Gets the previous game state object from the {@link GameStateServer}.
     *
     * @return the previous game state, or <em>empty</em> if it's the first state
     */
    public Optional<GameState> getPreviousState() {
        return Optional.ofNullable(previousState);
    }

    /**
     * Gets the local timestamp of when the previous state was received.
     *
     * @return the timestamp of the previous state, or <em>empty</em> for the first state
     */
    public Optional<Instant> getPreviousTimestamp() {
        return Optional.ofNullable(prevTimestamp);
    }

    /**
     * Returns the sequential index of this state update. For the first state this will return a value of {@code 1},
     * with each successive state incrementing the index by {@code 1}.
     *
     * <p>This value is reset when the GSI server instance is restarted.</p>
     *
     * @return the sequential index of this state
     */
    public int getSequence() {
        return sequenceIndex;
    }
    
    /**
     * Returns the network address of the game client which sent the associated state data. For local instances, this
     * will return the local {@link InetAddress#getLoopbackAddress() loopback address}.
     *
     * @return the remote address of the game client
     */
    public InetAddress getClientAddress() {
        return address;
    }

    /**
     * Returns an immutable map of the received authentication tokens (passwords) sent by the game client.
     *
     * @return an immutable map of auth tokens sent by the game
     */
    public AuthTokenMap getAuthTokens() {
        return authTokens;
    }

    /**
     * Returns the raw state JSON data (as a Gson {@link JsonObject}) sent by the game client.
     *
     * <p><strong>Note:</strong> calls to this method will invoke {@link JsonObject#deepCopy()} on the source object. Repeat calls to this
     * method should be avoided unless necessary.</p>
     *
     * @return the raw state JSON
     */
    public JsonObject getStateJson() {
        return rawJson.deepCopy();
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
