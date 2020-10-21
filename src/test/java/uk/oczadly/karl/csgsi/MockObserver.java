package uk.oczadly.karl.csgsi;

import uk.oczadly.karl.csgsi.state.GameState;

import java.util.concurrent.CountDownLatch;

/**
 * @author Karl Oczadly
 */
public class MockObserver implements GSIObserver {
    
    CountDownLatch latch;
    boolean called = false;
    GameState state;
    GameStateContext context;
    
    public MockObserver() {
        this(null);
    }
    
    public MockObserver(CountDownLatch latch) {
        this.latch = latch;
    }
    
    
    @Override
    public void update(GameState state, GameStateContext context) {
        this.called = true;
        this.state = state;
        this.context = context;
        if (latch != null) latch.countDown();
    }
    
}
