package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.Util;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPRequest;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPRequestHandler;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static uk.oczadly.karl.csgsi.internal.Util.GSON;

/**
 * Handles HTTP requests for a GSIServer.
 */
class GSIServerHTTPHandler implements HTTPRequestHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GSIServerHTTPHandler.class);

    private static final String MIME_JSON = "application/json";
    private static final String MIME_HTML = "text/html";
    private static final HTTPResponse RESPONSE_UPDATE = new HTTPResponse(200);
    private static final HTTPResponse RESPONSE_IGNORED = new HTTPResponse(503);
    private static final HTTPResponse RESPONSE_404 = new HTTPResponse(404, "text/plain",
            "This HTTP server is used to receive game state information from a Counter-Strike: Global Offensive " +
                    "game client.\nSee " + Util.REPO_URL + " for more info.");
    private static final HTTPResponse RESPONSE_WEB_REDIRECT = new HTTPResponse(404, MIME_HTML,
            "<!DOCTYPE html>\n<head><meta http-equiv=\"refresh\" content=\"0; url=/\" /></head>");

    private final GSIServer gsi;
    private final ServerStateContainer srvState;

    private volatile String diagnosticsHtml;
    
    public GSIServerHTTPHandler(GSIServer gsi) {
        this.gsi = gsi;
        this.srvState = gsi.srvState;
    }
    
    
    @Override
    public HTTPResponse handle(HTTPRequest req) {
        if (req.getMethod().equals("POST")
                && req.getHeader("user-agent", "").startsWith("Valve/Steam HTTP Client")) {
            // Received state update from client
            boolean handled = gsi.handleStateUpdate(
                    req.getBodyAsString(StandardCharsets.UTF_8),
                    req.getPath(),
                    req.getRemoteAddress());
            return handled ? RESPONSE_UPDATE : RESPONSE_IGNORED;
        } else if (gsi.diagnosticsEnabled && req.getMethod().equals("GET")) {
            // Browser requesting diagnostics info
            if (req.getPath().equals("/")) {
                // Diagnostics web app
                return new HTTPResponse(200, MIME_HTML, getDiagnosticHTML());
            } else if (req.getPath().equals("/api/diagnostics")) {
                // Diagnostics JSON
                return new HTTPResponse(200, MIME_JSON, buildDiagnosticsJson());
            } else if (req.getPath().equals("/api/state") && !gsi.requiresAuthTokens()) {
                // Raw state JSON
                synchronized (srvState.lock) {
                    return new HTTPResponse(200, MIME_JSON, srvState.getLatestContext()
                            .map(GameStateContext::getRawStateContents).orElse("{}"));
                }
            } else {
                // Redirect to root directory
                return RESPONSE_WEB_REDIRECT;
            }
        }
        // Unrecognized request
        log.warn("Unexpected HTTP request received ({} at {}) from {}!",
                req.getMethod(), req.getPath(), req.getRemoteAddress());
        return RESPONSE_404;
    }


    private String buildDiagnosticsJson() {
        JsonObject json = new JsonObject();
        synchronized (srvState.lock) {
            json.addProperty("srvTime",       System.currentTimeMillis());
            json.addProperty("startupTime",   srvState.getServerStartTimestamp().toEpochMilli());
            json.addProperty("bindAddress",   gsi.getBindAddress().getAddress().getHostAddress());
            json.addProperty("bindPort",      gsi.getBindAddress().getPort());
            json.addProperty("requiresAuth",  gsi.requiresAuthTokens());
            json.addProperty("listenerCount", gsi.listeners.size());
            json.addProperty("stateCount",    srvState.getStateCount());
            json.addProperty("discardCount",  srvState.getStateDiscardCount());
            json.addProperty("rejectCount",   srvState.getStateRejectCount());
            json.add("historicalTimestamps", GSON.toJsonTree(srvState.getHistoricalStateTimestamps()).getAsJsonArray());
            srvState.getLatestContext().ifPresent(ctx -> {
                json.addProperty("clientAddress", ctx.getClientAddress().getHostAddress());
                if (!gsi.requiresAuthTokens())
                    json.addProperty("stateContents", ctx.getRawStateContents());
            });
        }
        return json.toString();
    }

    private synchronized String getDiagnosticHTML() {
        if (diagnosticsHtml == null) {
            // Load from resource file
            InputStream resource = getClass().getClassLoader().getResourceAsStream("diagnostics.html");
            if (resource != null) {
                diagnosticsHtml = new BufferedReader(new InputStreamReader(resource))
                        .lines().collect(Collectors.joining("\n"));
            } else {
                log.error("Couldn't load diagnostics HTML from resources!");
                diagnosticsHtml = "<h1>Unable to load diagnostics page!</h1>";
            }
        }
        return diagnosticsHtml;
    }
    
}