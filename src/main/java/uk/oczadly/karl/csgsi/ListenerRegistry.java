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
    
    private static final Logger log = LoggerFactory.getLogger(ListenerRegistry.class);
    private static final ExecutorService HANDLER_EXECUTOR = Executors.newCachedThreadPool();
    
    final Set<GSIListener> subscribed = new CopyOnWriteArraySet<>();
    
    
    /**
     * Registers a listener.
     * @param listener the listener to register
     */
    public void subscribe(GSIListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null.");
        if (log.isDebugEnabled())
            log.debug("Registering listener {}...", Util.refVal(listener));
        subscribed.add(listener);
    }
    
    /**
     * Registers a collection of listeners.
     * @param listeners the listeners to register
     */
    public void subscribe(Collection<GSIListener> listeners) {
        if (listeners == null) throw new IllegalArgumentException("Listener collection cannot be null.");
        log.debug("Registering {} new listener...", listeners.size());
        this.subscribed.addAll(listeners);
    }
    
    /**
     * Removes a listener, if contained within the set.
     * @param listener the listener to remove
     */
    public void unsubscribe(GSIListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null.");
        if (log.isDebugEnabled())
            log.debug("Removing listener {}...", Util.refVal(listener));
        subscribed.remove(listener);
    }
    
    /**
     * Clears all listeners from the registry.
     */
    public void clear() {
        log.debug("Clearing listener registry...");
        subscribed.clear();
    }
    
    /**
     * @return the number of listeners registered
     */
    public int size() {
        return subscribed.size();
    }
    
    
    /**
     * Notifies the registered listeners of an updated state.
     *
     * @param state   the new game state information
     * @param context the game state and request context
     */
    public void notify(GameState state, GameStateContext context) {
        log.debug("Notifying {} listeners of new GSI state...", subscribed.size());

        long time = System.nanoTime();
        
        // Submit tasks and collect list of futures
        List<Future<?>> futures = new ArrayList<>(subscribed.size());
        for (GSIListener listener : subscribed) {
            futures.add(HANDLER_EXECUTOR.submit(() -> listener.update(state, context)));
        }
        
        // Wait for all tasks to complete (and log any errors)
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                log.error("Unhandled exception in listener notification task", e.getCause());
            }
        }

        long timeTaken = System.nanoTime() - time;
        if (timeTaken > 200_000_000) {
            log.warn("Took {}ms for listeners to process state update.", timeTaken / 1e6);
        } else {
            log.debug("Took {}ms for listeners to process state update.", timeTaken / 1e6);
        }
    }
    
}
