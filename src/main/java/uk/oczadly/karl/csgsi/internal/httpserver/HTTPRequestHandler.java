package uk.oczadly.karl.csgsi.internal.httpserver;

import java.net.InetAddress;
import java.util.Map;

/**
 * This interface is used by the {@link HTTPServer} class to listen for new incoming requests. Data can only be
 * retrieved from the incoming request, and cannot be sent back to the client.
 */
public interface HTTPRequestHandler {
    
    /**
     * Handles the data from an incoming HTTP request.
     *
     * @param request the HTTP request content
     */
    HTTPResponse handle(HTTPRequest request);
    
}
