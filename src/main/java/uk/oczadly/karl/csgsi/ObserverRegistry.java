package uk.oczadly.karl.csgsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.state.GameState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Handles a set of registered observers, and notifies them.
 */
class ObserverRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GSIServer.class);
    
    private static final ExecutorService OBSERVER_EXECUTOR = Executors.newCachedThreadPool();
    
    final Set<GSIObserver> observers = new CopyOnWriteArraySet<>();
    
    
    /**
     * Registers an observer.
     * @param observer the observer to register
     */
    public void register(GSIObserver observer) {
        LOGGER.debug("Registering observer #{}...", Integer.toHexString(System.identityHashCode(observer)));
        observers.add(observer);
    }
    
    /**
     * Registers a collection of observers.
     * @param observers the observers to register
     */
    public void register(Collection<GSIObserver> observers) {
        LOGGER.debug("Registering multiple observers...");
        this.observers.addAll(observers);
    }
    
    /**
     * Removes an observer, if contained within the set.
     * @param observer the observer to remove
     */
    public void remove(GSIObserver observer) {
        LOGGER.debug("Removing observer #{}...", Integer.toHexString(System.identityHashCode(observer)));
        observers.remove(observer);
    }
    
    /**
     * Clears all observers from the registry.
     */
    public void clear() {
        LOGGER.debug("Clearing observers list...");
        observers.clear();
    }
    
    /**
     * @return the number of observers registered
     */
    public int size() {
        return observers.size();
    }
    
    
    /**
     * Notifies the registered observers of an updated state.
     *
     * @param state   the new game state information
     * @param context the game state and request context
     */
    protected void notify(GameState state, GameStateContext context) {
        LOGGER.debug("Notifying {} observers of new GSI state...", observers.size());
        
        // Submit tasks and collect list of futures
        List<Future<?>> futures = new ArrayList<>(observers.size());
        for (GSIObserver observer : observers) {
            futures.add(OBSERVER_EXECUTOR.submit(() -> observer.update(state, context)));
        }
        
        // Wait for all tasks to complete (and log any errors)
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                LOGGER.error("Uncaught exception in GSIServer observer notification", e.getCause());
            }
        }
        LOGGER.debug("Finished notifying state observers.");
    }
    
}
