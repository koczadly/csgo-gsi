package uk.oczadly.karl.csgsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.NamedThreadFactory;
import uk.oczadly.karl.csgsi.internal.Util;
import uk.oczadly.karl.csgsi.state.GameState;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Handles a set of registered listeners, and notifies them.
 */
class ListenerRegistry {

    private static final long NOTIFY_WARN_TIME = 100 * 1_000_000; // in nanos
    
    private static final Logger log = LoggerFactory.getLogger(ListenerRegistry.class);
    private static final ExecutorService LISTENER_ES =
            Executors.newCachedThreadPool(new NamedThreadFactory("listener-thread-pool"));
    
    protected final Set<GSIListener> subscribers = new CopyOnWriteArraySet<>();


    /**
     * Registers a listener.
     * @param listener the listener to register
     */
    public void subscribe(GSIListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Listener cannot be null.");
        if (log.isDebugEnabled())
            log.debug("Registering listener {}...", Util.refVal(listener));
        subscribers.add(listener);
    }
    
    /**
     * Registers a collection of listeners.
     * @param listeners the listeners to register
     */
    public void subscribe(Collection<GSIListener> listeners) {
        if (listeners == null)
            throw new IllegalArgumentException("Listener collection cannot be null.");
        log.debug("Registering {} new listener...", listeners.size());
        this.subscribers.addAll(listeners);
    }
    
    /**
     * Removes a listener, if contained within the set.
     * @param listener the listener to remove
     */
    public void unsubscribe(GSIListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Listener cannot be null.");
        if (log.isDebugEnabled())
            log.debug("Removing listener {}...", Util.refVal(listener));
        subscribers.remove(listener);
    }
    
    /**
     * Clears all listeners from the registry.
     */
    public void clear() {
        log.debug("Clearing listener registry...");
        subscribers.clear();
    }
    
    /**
     * @return the number of listeners registered
     */
    public int size() {
        return subscribers.size();
    }


    /**
     * Notifies the registered listeners of an updated state.
     *
     * @param state   the new game state information
     * @param context the game state and request context
     */
    public void notifyNewState(GameState state, GameStateContext context) {
        log.debug("Notifying listeners of state update...");
        notifyListeners(l -> l.onStateUpdate(state, context));
    }


    /**
     * Notifies all listeners by applying the action consumer.
     */
    private void notifyListeners(Consumer<GSIListener> invoker) {
        Set<GSIListener> listeners = new CopyOnWriteArraySet<>(subscribers);
        CountDownLatch completionLatch = new CountDownLatch(listeners.size());
        log.debug("Notifying {} listeners...", listeners.size());
        // Invoke notification handlers
        listeners.stream()
                .map(l -> new NotifierTask(l, invoker, completionLatch))
                .forEach(LISTENER_ES::submit);
        // Block until completion
        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            log.warn("Interruption while waiting for listeners to complete.", e);
        }
    }


    private static class NotifierTask implements Runnable {
        private final GSIListener listener;
        private final Consumer<GSIListener> action;
        private final CountDownLatch latch;

        public NotifierTask(GSIListener listener, Consumer<GSIListener> action, CountDownLatch latch) {
            this.listener = listener;
            this.action = action;
            this.latch = latch;
        }

        @Override
        public void run() {
            long startTime = System.nanoTime();
            try {
                action.accept(listener); // Notify listener
            } catch (Throwable e) {
                log.error("Unhandled exception occurred in listener {}", listener, e);
            } finally {
                latch.countDown();
                // Log performance metrics
                long timeTaken = System.nanoTime() - startTime;
                if (timeTaken > NOTIFY_WARN_TIME) {
                    log.warn("Took {}ms for listener {} to process notification.", timeTaken / 1e6, listener);
                } else {
                    log.debug("Took {}ms for listener {} to process notification.", timeTaken / 1e6, listener);
                }
            }
        }
    }

    private static class ListenerThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            return null;
        }
    }
}
