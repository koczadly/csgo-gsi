package uk.oczadly.karl.csgsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.Util;
import uk.oczadly.karl.csgsi.state.GameState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Handles a set of registered listeners, and notifies them.
 */
class ListenerRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerRegistry.class);
    private static final ExecutorService HANDLER_EXECUTOR = Executors.newCachedThreadPool();
    
    final Set<GSIListener> listeners = new CopyOnWriteArraySet<>();
    
    
    /**
     * Registers a listener.
     * @param listener the listener to register
     */
    public void register(GSIListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null.");
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Registering listener {}...", Util.refVal(listener));
        listeners.add(listener);
    }
    
    /**
     * Registers a collection of listeners.
     * @param listeners the listeners to register
     */
    public void register(Collection<GSIListener> listeners) {
        if (listeners == null) throw new IllegalArgumentException("Listener collection cannot be null.");
        LOGGER.debug("Registering {} new listener...", listeners.size());
        this.listeners.addAll(listeners);
    }
    
    /**
     * Removes a listener, if contained within the set.
     * @param listener the listener to remove
     */
    public void remove(GSIListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null.");
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Removing listener {}...", Util.refVal(listener));
        listeners.remove(listener);
    }
    
    /**
     * Clears all listeners from the registry.
     */
    public void clear() {
        LOGGER.debug("Clearing listener registry...");
        listeners.clear();
    }
    
    /**
     * @return the number of listeners registered
     */
    public int size() {
        return listeners.size();
    }
    
    
    /**
     * Notifies the registered listeners of an updated state.
     *
     * @param state   the new game state information
     * @param context the game state and request context
     */
    public void notify(GameState state, GameStateContext context) {
        LOGGER.debug("Notifying {} listeners of new GSI state...", listeners.size());
        
        // Submit tasks and collect list of futures
        List<Future<?>> futures = new ArrayList<>(listeners.size());
        for (GSIListener listener : listeners) {
            futures.add(HANDLER_EXECUTOR.submit(() -> listener.update(state, context)));
        }
        
        // Wait for all tasks to complete (and log any errors)
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                LOGGER.error("Unhandled exception in listener notification task", e.getCause());
            }
        }
        LOGGER.debug("Finished notifying state listeners.");
    }
    
}
