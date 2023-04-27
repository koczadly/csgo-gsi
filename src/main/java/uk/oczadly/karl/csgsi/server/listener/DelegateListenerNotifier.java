package uk.oczadly.karl.csgsi.server.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

class DelegateListenerNotifier implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DelegateListenerNotifier.class);

    private static final long TIMEOUT_WARNING = Duration.ofMillis(100).toNanos();

    private final GameStateListener listener;
    private final Consumer<GameStateListener> invoker;
    private final CountDownLatch latch;


    public DelegateListenerNotifier(GameStateListener listener, Consumer<GameStateListener> invoker, CountDownLatch latch) {
        this.listener = listener;
        this.invoker = invoker;
        this.latch = latch;
    }


    @Override
    public void run() {
        log.trace("Invoking listener handler {}", listener);
        long startTime = System.nanoTime();
        try {
            // Notify listener
            invoker.accept(listener);
        } catch (Throwable e) {
            log.error("Uncaught exception occurred in listener handler {}", listener, e);
        } finally {
            latch.countDown();
            // Log performance metrics
            long timeTaken = System.nanoTime() - startTime;
            if (timeTaken > TIMEOUT_WARNING) {
                log.warn("Took {}ms for listener {} to handle notification.", timeTaken / 1e6, listener);
            } else {
                log.debug("Took {}ms for listener {} to handle notification.", timeTaken / 1e6, listener);
            }
        }
    }

}
