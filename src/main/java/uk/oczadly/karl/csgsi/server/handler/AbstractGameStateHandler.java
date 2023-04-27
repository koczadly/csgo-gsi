package uk.oczadly.karl.csgsi.server.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.state.GameState;
import uk.oczadly.karl.csgsi.state.context.AuthTokenMap;
import uk.oczadly.karl.csgsi.state.context.GameStateContext;

public abstract class AbstractGameStateHandler implements GameStateHandler {

    private static final Logger log = LoggerFactory.getLogger(AbstractGameStateHandler.class);

    private final ServerSessionHandler sessionHandler;


    public AbstractGameStateHandler() {
        this(new ServerSessionHandler());
    }

    public AbstractGameStateHandler(ServerSessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }


    protected final ServerSessionHandler getSessionHandler() {
        return sessionHandler;
    }


    @Override
    public final synchronized boolean processState(RawStateData stateData) {
        // Parse json string
        JsonObject json;
        try {
            json = JsonParser.parseString(stateData.getStateBody()).getAsJsonObject();
        } catch (JsonParseException e) {
            log.warn("GSI server received invalid JSON state data!", e);
            return false;
        }

        // Parse auth tokens
        AuthTokenMap authTokens = AuthTokenMap.EMPTY;
        try {
            authTokens = parseAuthTokens(json);
        } catch (Exception e) {
            log.warn("Failed to parse auth tokens from JSON; will assume none have been supplied.", e);
        }

        // Create GameStateContext
        GameStateContext stateContext = sessionHandler.createContext(authTokens, stateData, json);

        // Validate against filters
        if (!validateState(stateContext)) {
            log.debug("Game state was rejected.");
            return false;
        }

        // Parse state
        GameState state;
        try {
            state = parseState(json);
        } catch (Exception e) {
            log.warn("Failed to parse GameState object from JSON.", e);
            return false;
        }

        // Commit new state to session handler
        try {
            sessionHandler.updateContext(state, stateContext);
        } catch (ServerSessionHandler.IllegalStateContextException e) {
            log.warn("New game state was rejected by server session handler.", e);
            return false;
        }

        // Notify listener object
        try {
            handleState(state, stateContext);
        } catch (Exception e) {
            log.error("Uncaught exception occurred in listener handler.", e);
        }
        return true;
    }

    @Override
    public void resetContext() {
        log.debug("Resetting state context information");
        sessionHandler.reset();
    }


    protected abstract AuthTokenMap parseAuthTokens(JsonObject json);

    protected abstract GameState parseState(JsonObject json);

    protected boolean validateState(GameStateContext stateContext) {
        return true;
    }

    protected abstract void handleState(GameState state, GameStateContext stateContext);

}
