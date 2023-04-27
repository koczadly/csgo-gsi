package uk.oczadly.karl.csgsi.server.handler;

public interface GameStateHandler {

    boolean processState(RawStateData stateData);

    void resetContext();

}
