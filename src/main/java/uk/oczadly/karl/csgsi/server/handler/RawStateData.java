package uk.oczadly.karl.csgsi.server.handler;

import uk.oczadly.karl.csgsi.server.GameStateServer;

import java.net.InetAddress;
import java.time.Instant;

public final class RawStateData {

    private final GameStateServer server;
    private final Instant timestamp;
    private final String stateBody;
    private final String httpPath;
    private final InetAddress clientAddress;


    public RawStateData(GameStateServer server, String stateBody, String httpPath, InetAddress clientAddress) {
        this.timestamp = Instant.now();
        this.server = server;
        this.httpPath = httpPath;
        this.clientAddress = clientAddress;
        this.stateBody = stateBody;
    }


    public GameStateServer getServer() {
        return server;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getStateBody() {
        return stateBody;
    }

    public String getHttpPath() {
        return httpPath;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

}
