package uk.oczadly.karl.csgsi.server;

import uk.oczadly.karl.csgsi.server.filter.AuthTokenFilter;
import uk.oczadly.karl.csgsi.server.filter.StateFilter;
import uk.oczadly.karl.csgsi.server.filter.StateFilterSet;
import uk.oczadly.karl.csgsi.server.handler.DefaultGameStateHandler;
import uk.oczadly.karl.csgsi.server.listener.GameStateListener;
import uk.oczadly.karl.csgsi.server.listener.CompositeGameStateListener;
import uk.oczadly.karl.csgsi.server.serialization.DefaultStateDeserializer;
import uk.oczadly.karl.csgsi.server.serialization.StateDeserializer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

public final class GameStateServerBuilder {

    private static final int DEFAULT_BIND_PORT = 8080; //todo: change default port?

    private InetSocketAddress bindAddress;
    private final Set<GameStateListener> listeners = new HashSet<>();
    private final Set<StateFilter> filters = new HashSet<>();
    private StateDeserializer deserializer;


    public GameStateServerBuilder() {
        bindLoopback();
    }


    public GameStateServerBuilder bindLoopback() {
        return bindLoopback(DEFAULT_BIND_PORT);
    }

    public GameStateServerBuilder bindLoopback(int port) {
        return bindInterface(InetAddress.getLoopbackAddress(), port);
    }

    public GameStateServerBuilder bindAllInterfaces() {
        return bindAllInterfaces(DEFAULT_BIND_PORT);
    }

    public GameStateServerBuilder bindAllInterfaces(int port) {
        return bindInterface(null, port);
    }

    public GameStateServerBuilder bindInterface(InetAddress address) {
        return bindInterface(address, DEFAULT_BIND_PORT);
    }

    public GameStateServerBuilder bindInterface(InetAddress address, int port) {
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException("Port out of range.");
        this.bindAddress = new InetSocketAddress(address, port);
        return this;
    }


    public GameStateServerBuilder usingDeserializer(StateDeserializer deserializer) {
        Objects.requireNonNull(deserializer, "Deserializer object cannot be null.");
        this.deserializer = deserializer;
        return this;
    }


    public GameStateServerBuilder addFilter(StateFilter filter) {
        this.filters.add(filter);
        return this;
    }

    public GameStateServerBuilder addFilters(Collection<StateFilter> filters) {
        this.filters.addAll(filters);
        return this;
    }


    public GameStateServerBuilder requireAuthToken(String key, String expectedValue) {
        return this.addFilter(new AuthTokenFilter(key, expectedValue));
    }

    public GameStateServerBuilder requireAuthTokens(Map<String, String> authTokens) {
        authTokens.forEach(this::requireAuthToken);
        return this;
    }


    public GameStateServerBuilder subscribe(GameStateListener listener) {
        this.listeners.add(listener);
        return this;
    }


    public GameStateServer build() {
        return new GameStateServer(
                this.bindAddress,
                new DefaultGameStateHandler(
                        Objects.requireNonNullElseGet(this.deserializer, DefaultStateDeserializer::new),
                        new StateFilterSet(this.filters),
                        new CompositeGameStateListener(this.listeners)
                )
        );
    }

}
