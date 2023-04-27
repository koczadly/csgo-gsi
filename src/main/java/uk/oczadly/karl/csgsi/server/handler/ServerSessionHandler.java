package uk.oczadly.karl.csgsi.server.handler;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.state.GameState;
import uk.oczadly.karl.csgsi.state.context.AuthTokenMap;
import uk.oczadly.karl.csgsi.state.context.GameStateContext;

import java.util.Optional;

public final class ServerSessionHandler {

    private static final Logger log = LoggerFactory.getLogger(ServerSessionHandler.class);

    private volatile GameState latestState;
    private volatile GameStateContext latestContext;


    private Optional<GameStateContext> getLatestContext() {
        return Optional.ofNullable(latestContext);
    }


    public synchronized GameStateContext createContext(AuthTokenMap authTokens, RawStateData stateData,
                                                       JsonObject stateJson) {
        log.trace("Creating new game state context.");
        return new GameStateContext(
                stateData.getServer(),
                stateData.getHttpPath(),
                latestState,
                stateData.getTimestamp(),
                getLatestContext().map(GameStateContext::getTimestamp).orElse(null),
                getLatestContext().map(GameStateContext::getSequence).orElse(0) + 1,
                stateData.getClientAddress(),
                authTokens,
                stateJson,
                stateData.getStateBody()
        );
    }

    public synchronized void updateContext(GameState state, GameStateContext context) {
        // Validate against previous context
        if (latestContext != null) {
            // Ensure context matches same server as previous
            if (latestContext.getServer() != context.getServer()) {
                throw new IllegalStateContextException(
                        "Trying to commit a game state which does not match the previous");
            }
            // Check sequence comes after previous
            if (latestContext.getSequence() >= context.getSequence()) {
                throw new IllegalStateContextException("Trying to commit an out-of-date game state");
            }

            // Log warning if client address changed
            if (!latestContext.getClientAddress().equals(context.getClientAddress())) {
                log.warn("Game state came from a different client than previously!");
            }
        }

        this.latestState = state;
        this.latestContext = context;
        log.trace("Committed game state context.");
    }

    public synchronized void reset() {
        this.latestState = null;
        this.latestContext = null;
    }


    public static class IllegalStateContextException extends IllegalStateException {
        public IllegalStateContextException(String message) {
            super(message);
        }
    }

}
