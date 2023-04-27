package uk.oczadly.karl.csgsi.server.handler;

import com.google.gson.JsonObject;
import uk.oczadly.karl.csgsi.server.filter.StateFilterSet;
import uk.oczadly.karl.csgsi.server.listener.GameStateListener;
import uk.oczadly.karl.csgsi.server.serialization.StateDeserializer;
import uk.oczadly.karl.csgsi.state.GameState;
import uk.oczadly.karl.csgsi.state.context.AuthTokenMap;
import uk.oczadly.karl.csgsi.state.context.GameStateContext;

public final class DefaultGameStateHandler extends AbstractGameStateHandler {

    private final StateDeserializer stateDeserializer;
    private final StateFilterSet filters;
    private final GameStateListener listener;


    public DefaultGameStateHandler(StateDeserializer stateDeserializer, StateFilterSet filters, GameStateListener listener) {
        this.stateDeserializer = stateDeserializer;
        this.filters = filters;
        this.listener = listener;
    }


    public StateDeserializer getStateParser() {
        return stateDeserializer;
    }

    public StateFilterSet getFilters() {
        return filters;
    }

    public GameStateListener getListener() {
        return listener;
    }


    @Override
    protected AuthTokenMap parseAuthTokens(JsonObject json) {
        return stateDeserializer.parseAuthTokens(json);
    }

    @Override
    protected GameState parseState(JsonObject json) {
        return stateDeserializer.parseState(json);
    }

    @Override
    protected boolean validateState(GameStateContext stateContext) {
        return filters.isPermitted(stateContext);
    }

    @Override
    protected void handleState(GameState state, GameStateContext stateContext) {
        listener.onUpdate(state, stateContext);
    }

}
