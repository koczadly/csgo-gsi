package uk.oczadly.karl.csgsi;

import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;

/**
 * This interface must be implemented by classes that wish to subscribe to game state updates. Objects should be
 * registered to a {@link GSIServer} instance through the {@link GSIServer#registerObserver(GSIObserver)} method.
 */
public interface GSIObserver {
    
    /**
     * Called when a new updated state is received from the game client.
     *
     * @param state     the new game state
     * @param previous  the previous game state object, or null if this is the first update
     * @param address   the network address of the game client which sent the update
     */
    void update(GameState state, GameState previous, InetAddress address);
    
}
