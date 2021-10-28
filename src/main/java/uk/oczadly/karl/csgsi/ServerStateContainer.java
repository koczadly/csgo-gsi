package uk.oczadly.karl.csgsi;

import uk.oczadly.karl.csgsi.state.GameState;

import java.time.Instant;
import java.util.Optional;

class ServerStateContainer {

    final GSIServer gsiServer;
    volatile Instant serverStartTimestamp;
    volatile GameState latestState;
    volatile GameStateContext latestContext;
    volatile int minStateTimeDiff, maxStateTimeDiff;
    volatile int stateCounter, stateRejectCounter, stateDiscardCounter;

    ServerStateContainer(GSIServer gsiServer) {
        this.gsiServer = gsiServer;
        reset(false);
    }


    public Object getLock() {
        return gsiServer.readWriteLock;
    }

    public Optional<GameState> getLatestState() {
        return Optional.ofNullable(latestState);
    }

    public Optional<GameStateContext> getLatestContext() {
        return Optional.ofNullable(latestContext);
    }

    public int getStateCounter() {
        return stateCounter;
    }

    public int getStateRejectCounter() {
        return stateRejectCounter;
    }

    public int getStateDiscardCounter() {
        return stateDiscardCounter;
    }

    public Instant getServerStartTimestamp() {
        return serverStartTimestamp;
    }

    public int getMinStateTimeDifference() {
        return minStateTimeDiff;
    }

    public int getMaxStateTimeDifference() {
        return maxStateTimeDiff;
    }

    public void reset(boolean started) {
        synchronized (getLock()) {
            serverStartTimestamp = started ? Instant.now() : null;
            latestState = null;
            latestContext = null;
            minStateTimeDiff = -1;
            maxStateTimeDiff = -1;
            stateCounter = 0;
            stateRejectCounter = 0;
            stateDiscardCounter = 0;
        }
    }
}
