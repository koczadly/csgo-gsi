package uk.oczadly.karl.csgsi.internal.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages an active HTTP connection, parsing the request data, calling the handler and writing the response data.
 */
class HTTPConnection implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(HTTPConnection.class);

    static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final String NL = "\r\n";
    private static final Pattern HEADER_REGEX = Pattern.compile("^([\\w-]+)\\s*:\\s*(.+)$");
    private static final Pattern REQUEST_LINE_REGEX = Pattern.compile("^(\\w+) (.+) (HTTP/[0-9.]+)$");
    
    private final Socket socket;
    private final HTTPRequestHandler handler;
    
    public HTTPConnection(Socket socket, HTTPRequestHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }
    
    
    @Override
    public void run() {
        boolean keepalive = true;
        try {
            do {
                // Read start-line header
                String requestLine = readLine();
                if (requestLine == null) {
                    log.warn("Socket InputStream returned null data.");
                    return;
                }
                Matcher startMatcher = REQUEST_LINE_REGEX.matcher(requestLine);
                if (!startMatcher.matches()) {
                    log.warn("Invalid HTTP start-line header \"{}\"!", requestLine);
                    return;
                }
                String reqMethod = startMatcher.group(1).toUpperCase();
                String reqPath = URLDecoder.decode(startMatcher.group(2), CHARSET.name());

                // Read headers and body
                Map<String, String> headers = parseHeaders();
                log.debug("Parsed {} headers from request.", headers.size());
                ByteBuffer body = readBody(headers);
                log.debug("Parsed body (length: {} bytes).", body.limit());
                HTTPRequest request = new HTTPRequest(socket.getInetAddress(), reqPath, reqMethod, headers, body);

                // Check if keepalive should be disabled
                if (!request.getHeader("connection", "keep-alive").equalsIgnoreCase("keep-alive"))
                    keepalive = false;

                // Handle request and generate response
                HTTPResponse response = handleRequest(request);

                // Return header & body data
                writeResponse(response, keepalive);
            } while (keepalive && socket.isConnected());
        } catch (SocketTimeoutException e) {
            if (log.isDebugEnabled())
                log.debug("Socket timeout (keepalive: {}).", keepalive, e);
        } catch (IOException e) {
            log.warn("Network failure with HTTP connection.", e);
        } finally {
            //Close socket
            try {
                socket.close();
            } catch (IOException ignored) {}
            log.debug("HTTP exchange finished.");
        }
    }


    private HTTPResponse handleRequest(HTTPRequest request) {
        try {
            return handler.handle(request);
        } catch (Exception e) {
            log.error("Handler threw an uncaught exception - will return 500 status.", e);
            return new HTTPResponse(500);
        }
    }
    
    /** Parse a set of headers into a map */
    private Map<String, String> parseHeaders() throws IOException {
        Map<String, String> headers = new HashMap<>();
        String s;
        while ((s = readLine()) != null) {
            if (s.equals("")) break; // End of headers
            
            Matcher matcher = HEADER_REGEX.matcher(s);
            if (matcher.matches()) {
                headers.put(matcher.group(1).toLowerCase(), matcher.group(2));
            }
        }
        return headers;
    }

    private ByteBuffer readBody(Map<String, String> headers) throws IOException {
        ByteBuffer buffer;
        if (headers.containsKey("content-length")) {
            buffer = ByteBuffer.allocate(Integer.parseInt(headers.get("content-length")));
            Channels.newChannel(socket.getInputStream()).read(buffer);
        } else {
            buffer = ByteBuffer.allocate(0);
        }
        return buffer.flip();
    }
    
    /** Write response message and server information */
    private void writeResponse(HTTPResponse response, boolean keepalive) throws IOException {
        log.debug("Writing response data, status code: {}...", response.getStatusCode());

        // Status line
        writeString("HTTP/1.1 " + response.getStatusCode());
        writeString(response.getStatusCode() >= 200 && response.getStatusCode() < 300 ? " OK" : " FAILURE");
        writeString(NL);
        // Keepalive
        writeHeader("Connection", (keepalive ? "keep-alive" : "close"));
        if (keepalive && socket.getSoTimeout() > 0)
            writeHeader("Keep-alive", "timeout=" + (socket.getSoTimeout() / 1000));
        // Content
        if (response.getBody() != null && !response.getBody().isEmpty()) {
            byte[] body = response.getBody().getBytes(CHARSET);
            writeHeader("Content-length", body.length);
            String contentType = Objects.requireNonNullElse(response.getContentType(), "text/plain");
            writeHeader("Content-type", contentType + "; charset=" + CHARSET.name());
            writeString(NL);
            // Write body
            socket.getOutputStream().write(body);
        } else {
            writeHeader("Content-length", 0);
            writeString(NL);
        }
        socket.getOutputStream().flush();
    }

    /** Read a line without buffering/reading further */
    private String readLine() throws IOException {
        InputStream is = socket.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(64);
        int b;
        for (b = is.read(); b != '\n' && b != -1; b = is.read())
            if (b != '\r') bos.write(b);
        if (b == -1 && bos.size() == 0) return null;
        return new String(bos.toByteArray(), 0, bos.size(), CHARSET);
    }

    /** Write a string in the correct char encoding */
    private void writeString(String str) throws IOException {
        socket.getOutputStream().write(str.getBytes(CHARSET));
    }

    /** Write a string in the correct char encoding */
    private void writeHeader(String key, Object value) throws IOException {
        writeString(key + ": " + value.toString() + NL);
    }
    
}
