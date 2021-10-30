package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;

class ServerStateContainer {

    private final static int TIMESTAMP_HISTORY_COUNT = 25;

    public final Object lock;
    private final GSIServer gsiServer;
    private volatile Instant serverStartTimestamp;
    private volatile GameState latestState;
    private volatile GameStateContext latestContext;
    private volatile int stateCount, stateRejectCount, stateDiscardCount;
    private final List<Long> historicalStateTimestamps = new LinkedList<>();

    ServerStateContainer(GSIServer gsiServer) {
        this.gsiServer = gsiServer;
        this.lock = gsiServer.readWriteLock;
        reset(false);
    }


    public Optional<GameState> getLatestState() {
        return Optional.ofNullable(latestState);
    }

    public Optional<GameStateContext> getLatestContext() {
        return Optional.ofNullable(latestContext);
    }

    public GameStateContext updateState(GameState state, String path, Instant receivedTime, InetAddress remoteAddr,
                            Map<String, String> authTokens, JsonObject json, String rawJson) {
        synchronized (lock) {
            // Update state and context
            latestState = state;
            latestContext = new GameStateContext(
                    gsiServer, path, getLatestState().orElse(null), receivedTime,
                    getLatestContext().map(GameStateContext::getTimestamp).orElse(null),
                    incrementStateCounter() - 1, remoteAddr, authTokens, json, rawJson);

            // Update statistics
            this.recordHistoricalStateTimestamp(receivedTime.toEpochMilli());

            return latestContext;
        }
    }

    public int getStateCount() {
        return stateCount;
    }

    public int incrementStateCounter() {
        synchronized (lock) {
            return ++stateCount;
        }
    }

    public int getStateRejectCount() {
        return stateRejectCount;
    }

    public int incrementStateRejectCounter() {
        synchronized (lock) {
            return ++stateRejectCount;
        }
    }

    public int getStateDiscardCount() {
        return stateDiscardCount;
    }

    public int incrementStateDiscardCounter() {
        synchronized (lock) {
            return ++stateDiscardCount;
        }
    }

    public Instant getServerStartTimestamp() {
        return serverStartTimestamp;
    }

    public List<Long> getHistoricalStateTimestamps() {
        return historicalStateTimestamps;
    }

    private void recordHistoricalStateTimestamp(long tsMillis) {
        synchronized (lock) {
            if (historicalStateTimestamps.size() == TIMESTAMP_HISTORY_COUNT) {
                historicalStateTimestamps.remove(TIMESTAMP_HISTORY_COUNT - 1);
            }
            historicalStateTimestamps.add(0, tsMillis);
        }
    }

    public void reset(boolean started) {
        synchronized (lock) {
            serverStartTimestamp = started ? Instant.now() : null;
            historicalStateTimestamps.clear();
            latestState = null;
            latestContext = null;
            stateCount = 0;
            stateRejectCount = 0;
            stateDiscardCount = 0;
        }
    }
}
