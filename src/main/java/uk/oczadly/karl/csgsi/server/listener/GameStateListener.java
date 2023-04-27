package uk.oczadly.karl.csgsi.server.listener;

import uk.oczadly.karl.csgsi.state.GameState;
import uk.oczadly.karl.csgsi.state.context.GameStateContext;

@FunctionalInterface
public interface GameStateListener {

    void onUpdate(GameState state, GameStateContext context);
    
}
