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
     * @param address the address of the client connection
     * @param path    the requested URL path
     * @param method  the HTTP request method (eg. POST, GET)
     * @param headers a map of headers
     * @param body    the payload body contents
     */
    HTTPResponse handle(InetAddress address, String path, String method, Map<String, String> headers, String body);
    
}
