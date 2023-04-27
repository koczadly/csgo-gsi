package uk.oczadly.karl.csgsi.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPRequest;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPRequestHandler;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPResponse;
import uk.oczadly.karl.csgsi.server.handler.GameStateHandler;
import uk.oczadly.karl.csgsi.server.handler.RawStateData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

class GameStateHttpHandler implements HTTPRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(GameStateHttpHandler.class);

    private static final HTTPResponse RESPONSE_STATE_PROCESSED = new HTTPResponse(200);
    private static final HTTPResponse RESPONSE_STATE_REJECTED = new HTTPResponse(403);
    private static final HTTPResponse RESPONSE_NOT_FOUND = new HTTPResponse(404, "text/plain", "CSGO-GSI server");
    private static final HTTPResponse RESPONSE_ERROR = new HTTPResponse(500);

    private static volatile String landingHtml;


    private final GameStateServer server;
    private final GameStateHandler stateHandler;


    public GameStateHttpHandler(GameStateServer server, GameStateHandler stateHandler) {
        this.server = server;
        this.stateHandler = stateHandler;
    }


    @Override
    public HTTPResponse handle(HTTPRequest httpRequest) {
        if (httpRequest.hasBody() && httpRequest.getMethod().equalsIgnoreCase("POST")) {
            log.debug("Handling incoming state request");
            return handleState(httpRequest);
        } else if (httpRequest.getMethod().equalsIgnoreCase("GET")) {
            log.debug("Serving HTTP request from web browser");
            return generateLandingPage();
        } else {
            log.debug("Serving unrecognized HTTP request");
            return RESPONSE_NOT_FOUND;
        }
    }


    private HTTPResponse handleState(HTTPRequest httpRequest) {
        // Prepare state data object
        RawStateData stateData = new RawStateData(
                this.server,
                httpRequest.getBodyAsString(StandardCharsets.UTF_8),
                httpRequest.getPath(),
                httpRequest.getRemoteAddress()
        );

        // Try to process state
        boolean handled;
        try {
            handled = stateHandler.processState(stateData);
        } catch (Exception e) {
            log.error("An unexpected exception occurred when handling incoming game state.", e);
            return RESPONSE_ERROR;
        }
        if (handled) {
            log.debug("State handler execution completed, serving OK response to game client.");
            return RESPONSE_STATE_PROCESSED;
        } else {
            log.debug("State handler rejected incoming state, serving reject response.");
            return RESPONSE_STATE_REJECTED;
        }
    }


    private static synchronized HTTPResponse generateLandingPage() {
        if (landingHtml == null) {
            landingHtml = loadPageContents("landing");
        }
        if (landingHtml != null) {
            return new HTTPResponse(200, "text/html", landingHtml);
        } else {
            return new HTTPResponse(200, "text/html", "<!DOCTYPE html>\n\n<h1>CSGO-GSI</h1>");
        }
    }

    private static String loadPageContents(String fileName) {
        InputStream resource = GameStateHttpHandler.class.getClassLoader()
                .getResourceAsStream("web/" + fileName + ".html");
        if (resource != null) {
            try (resource) {
                return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
            } catch (IOException e) {
                log.warn("Failed to load page contents for file {}", fileName, e);
            }
        } else {
            log.warn("Couldn't find page content for file {}", fileName);
        }
        return null;
    }

}
