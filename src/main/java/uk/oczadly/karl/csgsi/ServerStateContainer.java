package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;

class ServerStateContainer {

    private final static int STATE_INTERVAL_COUNT = 20;

    public final Object lock;
    private final GSIServer gsiServer;
    private volatile Instant serverStartTimestamp;
    private volatile GameState latestState;
    private volatile GameStateContext latestContext;
    private volatile int stateCounter, stateRejectCounter, stateDiscardCounter;
    private final List<Integer> stateIntervalHistory = new LinkedList<>();

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
                    incrementStateCounter(), remoteAddr, authTokens, json, rawJson);

            // Update statistics
            latestContext.getMillisSinceLastState().ifPresent(this::addStateIntervalHistory);

            return latestContext;
        }
    }

    public int getStateCounter() {
        return stateCounter;
    }

    public int incrementStateCounter() {
        synchronized (lock) {
            return ++stateCounter;
        }
    }

    public int getStateRejectCounter() {
        return stateRejectCounter;
    }

    public int incrementStateRejectCounter() {
        synchronized (lock) {
            return ++stateRejectCounter;
        }
    }

    public int getStateDiscardCounter() {
        return stateDiscardCounter;
    }

    public int incrementStateDiscardCounter() {
        synchronized (lock) {
            return ++stateDiscardCounter;
        }
    }

    public Instant getServerStartTimestamp() {
        return serverStartTimestamp;
    }

    public List<Integer> getStateIntervalHistory() {
        return stateIntervalHistory;
    }

    public void addStateIntervalHistory(int intervalMillis) {
        synchronized (lock) {
            if (stateIntervalHistory.size() == STATE_INTERVAL_COUNT) {
                stateIntervalHistory.remove(STATE_INTERVAL_COUNT - 1);
            }
            stateIntervalHistory.add(0, intervalMillis);
        }
    }

    public void reset(boolean started) {
        synchronized (lock) {
            serverStartTimestamp = started ? Instant.now() : null;
            stateIntervalHistory.clear();
            latestState = null;
            latestContext = null;
            stateCounter = 0;
            stateRejectCounter = 0;
            stateDiscardCounter = 0;
        }
    }
}
