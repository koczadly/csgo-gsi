package uk.oczadly.karl.csgsi.server.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.state.context.GameStateContext;
import uk.oczadly.karl.csgsi.internal.NamedThreadFactory;
import uk.oczadly.karl.csgsi.state.GameState;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class CompositeGameStateListener implements GameStateListener {

    private static final Logger log = LoggerFactory.getLogger(CompositeGameStateListener.class);

    private static final ExecutorService defaultExecutor = Executors.newCachedThreadPool(
            new NamedThreadFactory("composite-listener-pool"));
    
    private final Set<GameStateListener> delegates;
    private final boolean awaitCompletion;
    private final ExecutorService listenerExecutor;


    public CompositeGameStateListener(Collection<GameStateListener> delegates) {
        this(delegates, true);
    }

    public CompositeGameStateListener(Collection<GameStateListener> delegates, boolean awaitCompletion) {
        this(delegates, awaitCompletion, defaultExecutor);
    }

    public CompositeGameStateListener(Collection<GameStateListener> delegates, boolean awaitCompletion,
                                      ExecutorService listenerExecutor) {
        this.delegates = new HashSet<>(delegates);
        this.awaitCompletion = awaitCompletion;
        this.listenerExecutor = listenerExecutor;
    }


    public final Set<GameStateListener> getDelegates() {
        return Collections.unmodifiableSet(delegates);
    }

    public final boolean willAwaitCompletion() {
        return awaitCompletion;
    }

    public final ExecutorService getListenerExecutor() {
        return listenerExecutor;
    }


    @Override
    public void onUpdate(GameState state, GameStateContext context) {
        invokeDelegates(l -> l.onUpdate(state, context));
    }


    protected final void invokeDelegates(Consumer<GameStateListener> invoker) {
        log.debug("Notifying {} delegate listeners...", delegates.size());

        CountDownLatch completionLatch = new CountDownLatch(delegates.size());
        delegates.stream()
                .map(l -> new DelegateListenerNotifier(l, invoker, completionLatch))
                .forEach(listenerExecutor::submit);

        if (awaitCompletion) {
            log.trace("Awaiting for delegate listener to finish processing");
            try {
                completionLatch.await();
                log.debug("All delegate listeners have completed processing");
            } catch (InterruptedException e) {
                log.warn("Interruption while waiting for delegate listeners to complete.", e);
            }
        }
    }

}
