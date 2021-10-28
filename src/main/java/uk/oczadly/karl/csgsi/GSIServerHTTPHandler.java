package uk.oczadly.karl.csgsi;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPRequestHandler;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles HTTP requests for a GSIServer.
 */
class GSIServerHTTPHandler implements HTTPRequestHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GSIServerHTTPHandler.class);

    private static final HTTPResponse RESPONSE_UPDATE = new HTTPResponse(200);
    private static final HTTPResponse RESPONSE_404 = new HTTPResponse(404);
    private static final HTTPResponse RESPONSE_404_REDIRECT = new HTTPResponse(404,
            "text/html", "<meta http-equiv=\"refresh\" content=\"0; url=/\" />");

    private final GSIServer gsi;

    private volatile String diagnosticHtml;
    
    public GSIServerHTTPHandler(GSIServer gsi) {
        this.gsi = gsi;
    }
    
    
    @Override
    public HTTPResponse handle(InetAddress address, String path, String method, Map<String, String> headers,
                               String body) {
        String userAgent = headers.get("user-agent");
        if (userAgent.startsWith("Valve/Steam HTTP Client") && method.equalsIgnoreCase("POST")) {
            // Received state update from client
            if (body != null)
                gsi.handleStateUpdate(body, path, address);
            return RESPONSE_UPDATE;
        } else if (gsi.diagPageEnabled && method.equalsIgnoreCase("GET")) {
            // Browser requesting diagnostics info
            switch (path.toLowerCase()) {
                case "/":
                    // Diagnostics page
                    return new HTTPResponse(200, "text/html", getDiagnosticHTML());
                case "/api/diagnostics":
                    // Diagnostics JSON
                    return new HTTPResponse(200, "application/json", buildDiagnosticsJson());
                case "/api/state":
                    // Raw state JSON
                    return new HTTPResponse(200, "application/json", gsi.stats.getLatestContext()
                            .map(GameStateContext::getRawJsonString).orElse("{}"));
                default:
                    // Redirect to root directory
                    return RESPONSE_404_REDIRECT;
            }
        }
        // Unrecognized request
        log.warn("Unexpected HTTP request received ({} at {}) from {}!", method, path, address);
        return RESPONSE_404;
    }


    private String buildDiagnosticsJson() {
        JsonObject json = new JsonObject();
        json.addProperty("startupTime",   gsi.serverStartTimestamp.toEpochMilli());
        json.addProperty("bindAddress",   gsi.getBindAddress().getAddress().getHostAddress());
        json.addProperty("bindPort",      gsi.getBindAddress().getPort());
        json.addProperty("hasAuthKeys",   !gsi.getRequiredAuthTokens().isEmpty());
        json.addProperty("listenerCount", gsi.listeners.size());
        json.addProperty("stateCount",    gsi.stats.getStateCounter());
        json.addProperty("rejectCount",   gsi.stats.getStateRejectCounter());
        gsi.stats.getLatestContext().ifPresent(context -> {
            json.addProperty("lastStateTime", context.getTimestamp().toEpochMilli());
            context.getPreviousTimestamp().ifPresent(pts ->
                    json.addProperty("lastStateDelay",
                            Duration.between(pts, context.getTimestamp()).toMillis()));
            json.addProperty("lastStateContents", context.getRawJsonString());
        });
        return json.toString();
    }

    private synchronized String getDiagnosticHTML() {
        if (diagnosticHtml == null) {
            // Load from resource file
            InputStream resource = getClass().getClassLoader().getResourceAsStream("diagnostics.html");
            diagnosticHtml = new BufferedReader(new InputStreamReader(resource))
                    .lines().collect(Collectors.joining("\n"));
        }
        return diagnosticHtml;
    }
    
}