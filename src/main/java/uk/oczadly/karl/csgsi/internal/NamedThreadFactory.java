package uk.oczadly.karl.csgsi.internal;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger index = new AtomicInteger(0);
    private final String prefix;

    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }


    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, prefix + '-' + index.incrementAndGet());
    }

}
