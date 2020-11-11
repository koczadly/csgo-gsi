package uk.oczadly.karl.csgsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.Util;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPRequestHandler;
import uk.oczadly.karl.csgsi.internal.httpserver.HTTPResponse;

import java.net.InetAddress;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Handles HTTP requests for a GSIServer.
 */
class GSIServerHTTPHandler implements HTTPRequestHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GSIServerHTTPHandler.class);
    
    private static final HTTPResponse RESPONSE_UPDATE = new HTTPResponse(200);
    
    private final GSIServer gsi;
    
    public GSIServerHTTPHandler(GSIServer gsi) {
        this.gsi = gsi;
    }
    
    
    @Override
    public HTTPResponse handle(InetAddress address, String path, String method, Map<String, String> headers,
                               String body) {
        String contentType = headers.get("content-type");
        if (method.equalsIgnoreCase("POST") && "application/json".equals(contentType)) {
            // State update from client
            if (body != null)
                gsi.handleStateUpdate(body, path, address);
            return RESPONSE_UPDATE;
        } else if (gsi.diagPageEnabled && method.equalsIgnoreCase("GET") && path.equals("/")) {
            // Browser requesting info page
            LOGGER.debug("Serving info HTML page.");
            return new HTTPResponse(200, "text/html", buildInfoHTML());
        } else if (gsi.diagPageEnabled && method.equalsIgnoreCase("GET") && (path.equalsIgnoreCase("/favicon.ico") ||
                path.equalsIgnoreCase("/robots.txt") || path.equalsIgnoreCase("/sitemap.xml"))) {
            // Ignore automatic requests
            LOGGER.debug("Ignoring automated HTTP request {} from {}.", path, address);
            return new HTTPResponse(404);
        } else {
            // Unknown request type
            LOGGER.warn("Unexpected HTTP request received ({} at {}) from {}!", method, path, address);
            return new HTTPResponse(404);
        }
    }
    
    
    /** Builds the test/info page HTML code */
    private String buildInfoHTML() {
        // Retrieve latest state information
        long now = System.currentTimeMillis();
        boolean requiresAuth = !gsi.getRequiredAuthTokens().isEmpty();
        GameStateContext latestContext = gsi.stats.latestContext;
        int stateCount = gsi.stats.stateCounter.intValue();
        int rejectCount = gsi.stats.stateRejectCounter.intValue();
        
        // Build HTML
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n\n");
        sb.append("<head><meta charset=\"UTF-8\"><meta http-equiv=\"expires\" content=\"0\" />")
                .append("<script src=\"https://cdn.jsdelivr.net/gh/google/code-prettify@master/loader/")
                .append("run_prettify.js\"></script></head>\n");
        
        sb.append("<body><h1><a style=\"color:green\" href=\"").append(Util.GITHUB_URL)
                .append("\">CSGO-GSI server is running!</a></h1>\n");
        // Listening port
        sb.append("<b>Listening on:</b> <code>http://")
                .append(gsi.getBindingAddress() != null ? gsi.getBindingAddress() : "0.0.0.0")
                .append(":").append(gsi.getPort()).append("</code><br>\n");
        // Uptime
        double uptimeMins = ((now - gsi.serverStartTimestamp.toEpochMilli()) / 1000d) / 60d;
        sb.append("<b>Server startup time:</b> ").append(DateTimeFormatter.RFC_1123_DATE_TIME.format(
                gsi.serverStartTimestamp.atZone(ZoneId.systemDefault())))
                .append(" <i>(").append(String.format("%,.2f", uptimeMins)).append(" mins uptime)</i><br>\n");
        // Auth keys
        sb.append("<b>Auth keys configured:</b> ").append(requiresAuth ? "Yes" : "No").append("<br>\n");
        // Listeners
        sb.append("<b>Subscribed listeners:</b> <span")
                .append(gsi.listeners.size() == 0 ? " style=\"color:red\">" : ">")
                .append(gsi.listeners.size()).append("</span><br>\n");
        // State counter
        sb.append("<b>State updates received:</b> ").append(String.format("%,d", stateCount))
                .append(rejectCount == 0 ? " <i>(" : " <i style=\"color:red\">(")
                .append(String.format("%,d", rejectCount)).append(" rejected)</i><br>\n");
        
        if (gsi.stats.latestContext != null) {
            // Latest TS
            sb.append("<b>Latest state timestamp:</b> ")
                    .append(DateTimeFormatter.RFC_1123_DATE_TIME.format(
                            latestContext.getTimestamp().atZone(ZoneId.systemDefault())))
                    .append(" <i>(").append(String.format("%,.3f",
                            (now - latestContext.getTimestamp().toEpochMilli()) / 1000d))
                    .append(" seconds ago)</i><br>\n");
            // Millis since last state
            sb.append("<b>Time diff between previous state: </b>");
            if (latestContext.getMillisSinceLastState() != -1) {
                sb.append(String.format("%,.3f", latestContext.getMillisSinceLastState() / 1000d))
                        .append(" seconds<br>\n");
            } else {
                sb.append("<i>N/A</i><br>\n");
            }
            // JSON dump
            if (!requiresAuth) {
                sb.append("<b>Latest state JSON:</b><br>\n<pre><code class=\"prettyprint\">")
                        .append(latestContext.getRawJsonString().replace("\t", "    ")).append("</code></pre>\n");
            }
        }
        sb.append("</body>");
        return sb.toString();
    }
    
}