package uk.oczadly.karl.csgsi.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPServer;
import uk.oczadly.karl.csgsi.server.handler.GameStateHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Optional;

public final class GameStateServer {

    private static final Logger log = LoggerFactory.getLogger(GameStateServer.class);

    private final HTTPServer httpServer;
    private final GameStateHandler stateHandler;
    private volatile Instant startupTime;


    public GameStateServer(InetSocketAddress bindAddress, GameStateHandler stateHandler) {
        this.httpServer = new HTTPServer(bindAddress, new GameStateHttpHandler(this, stateHandler));
        this.stateHandler = stateHandler;
    }


    public synchronized void start() throws IOException {
        if (isRunning())
            throw new IllegalStateException("The server is already running.");

        log.debug("Starting GSI server on {}...", getBindAddress());
        startupTime = Instant.now();
        stateHandler.resetContext();
        httpServer.start();
        log.info("GSI server successfully started on {}.", getBindAddress());
    }

    public synchronized void stop() {
        if (!isRunning())
            throw new IllegalStateException("The server is not currently running.");

        log.debug("Stopping GSI server on {}...", getBindAddress());
        startupTime = null;
        httpServer.stop();
        log.info("GSI server on interface {} successfully shut down.", getBindAddress());
    }

    public synchronized boolean isRunning() {
        return httpServer.isRunning();
    }


    public InetSocketAddress getBindAddress() {
        return httpServer.getBindAddress();
    }

    public Optional<Instant> getStartupTime() {
        return Optional.ofNullable(startupTime);
    }


    @Override
    public String toString() {
        return "GameStateServer{" +
                "bindAddress=" + this.getBindAddress() +
                '}';
    }


    public static GameStateServerBuilder builder() {
        return new GameStateServerBuilder();
    }

}
