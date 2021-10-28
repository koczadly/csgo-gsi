package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPRequest;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPRequestHandler;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Handles HTTP requests for a GSIServer.
 */
class GSIServerHTTPHandler implements HTTPRequestHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GSIServerHTTPHandler.class);

    private static final HTTPResponse RESPONSE_UPDATE = new HTTPResponse(200);
    private static final HTTPResponse RESPONSE_IGNORED = new HTTPResponse(503);
    private static final HTTPResponse RESPONSE_404 = new HTTPResponse(404);
    private static final HTTPResponse RESPONSE_WEB_REDIRECT = new HTTPResponse(404,
            "text/html", "<meta http-equiv=\"refresh\" content=\"0; url=/\" />");

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
            if (req.hasBody() && gsi.handleStateUpdate(
                    req.getBodyAsString(), req.getPath(), req.getRemoteAddress())) {
                return RESPONSE_UPDATE;
            } else {
                return RESPONSE_IGNORED;
            }
        } else if (gsi.diagnosticsEnabled && req.getMethod().equals("GET")) {
            // Browser requesting diagnostics info
            switch (req.getPath().toLowerCase()) {
                case "/": // Diagnostics page
                    return new HTTPResponse(200, "text/html", getDiagnosticHTML());
                case "/api/diagnostics": // Diagnostics JSON
                    return new HTTPResponse(200, "application/json", buildDiagnosticsJson());
                case "/api/state": // Raw state JSON
                    synchronized (srvState.getLock()) {
                        return new HTTPResponse(200, "application/json",
                                srvState.getLatestContext().map(GameStateContext::getRawJsonString)
                                        .orElse("{}"));
                    }
                default:
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
        synchronized (srvState.getLock()) {
            json.addProperty("startupTime",   srvState.getServerStartTimestamp().toEpochMilli());
            json.addProperty("bindAddress",   gsi.getBindAddress().getAddress().getHostAddress());
            json.addProperty("bindPort",      gsi.getBindAddress().getPort());
            json.addProperty("requiresAuth",  !gsi.getRequiredAuthTokens().isEmpty());
            json.addProperty("listenerCount", gsi.listeners.size());
            json.addProperty("stateCount",    srvState.getStateCounter());
            json.addProperty("rejectCount",   srvState.getStateRejectCounter());
            json.addProperty("discardCount",  srvState.getStateDiscardCounter());
            srvState.getLatestContext().ifPresent(ctx -> {
                json.addProperty("clientAddress", ctx.getAddress().getHostAddress());
                json.addProperty("lastStateTime", ctx.getTimestamp().toEpochMilli());
                ctx.getMillisSinceLastState().ifPresent(millis -> json.addProperty("lastStateDelay", millis));
                if (srvState.getMinStateTimeDifference() >= 0)
                    json.addProperty("lastStateDelayMin", srvState.getMinStateTimeDifference());
                if (srvState.getMaxStateTimeDifference() >= 0)
                    json.addProperty("lastStateDelayMax", srvState.getMaxStateTimeDifference());
                if (gsi.getRequiredAuthTokens().isEmpty())
                    json.addProperty("lastStateContents", ctx.getRawJsonString());
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