package uk.oczadly.karl.csgsi;

import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;

public interface GSIObserver {
    
    void update(GameState state, GameState previous, InetAddress address);
    
}
