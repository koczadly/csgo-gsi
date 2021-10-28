package uk.oczadly.karl.csgsi.internal.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages an active HTTP connection, parsing the request data, calling the handler and writing the response data.
 */
class HTTPConnection implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(HTTPConnection.class);
    
    private static final Charset CHARSET = StandardCharsets.UTF_8;
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
        try {
            boolean keepalive = false;
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
                String body = null;
                if (headers.containsKey("content-length")) {
                    body = readBody(Integer.parseInt(headers.get("content-length")));
                }

                // Check if keepalive should be disabled
                String reqConnection = headers.get("connection");
                if (reqConnection != null && reqConnection.equalsIgnoreCase("keep-alive"))
                    keepalive = true;

                // Handle response
                HTTPResponse response;
                try {
                    response = handler.handle(socket.getInetAddress(), reqPath, reqMethod, headers, body);
                } catch (Exception e) {
                    log.error("Handler threw an uncaught exception - will return 500 status.", e);
                    response = new HTTPResponse(500);
                }

                // Return header & body data
                writeResponse(response, keepalive);
            } while (keepalive && socket.isConnected());
        } catch (IOException e) {
            if (log.isDebugEnabled())
                log.debug("Network failure with HTTP connection.", e);
        } finally {
            //Close socket
            try {
                socket.close();
            } catch (IOException ignored) {}
            log.debug("HTTP exchange finished.");
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
    
    /** Read the body as a string */
    private String readBody(int length) throws IOException {
        byte[] buffer = new byte[length];
        int readLen = socket.getInputStream().read(buffer, 0, length);
        if (readLen == -1) {
            log.debug("Read 0 bytes as stream has ended.");
            return null;
        }
        return new String(buffer, 0, readLen, CHARSET);
    }
    
    /** Write response message and server information */
    private void writeResponse(HTTPResponse response, boolean keepalive) throws IOException {
        log.debug("Writing response data, status code: {}...", response.getStatusCode());

        // Status line
        OutputStream os = socket.getOutputStream();
        writeString("HTTP/1.1 " + response.getStatusCode());
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            writeString(" OK\r\n");
        } else {
            writeString(" FAILURE\r\n");
        }
        // Keepalive
        writeString("Connection: " + (keepalive ? "keep-alive" : "close") + "\r\n");
        if (keepalive && socket.getSoTimeout() > 0)
            writeString("Keep-alive: timeout=" + (socket.getSoTimeout() / 1000) + "\r\n");
        // Body
        if (response.getBody() != null) {
            byte[] body = response.getBody().getBytes(CHARSET);
            writeString("Content-length: " + body.length + "\r\n");
            String contentType = response.getContentType() != null ? response.getContentType() : "text/plain";
            writeString("Content-type: " + contentType + "; charset=" + CHARSET.name() + "\r\n\r\n");
            os.write(body);
        }
        os.flush();
    }

    /** Read a line without buffering/reading further */
    private String readLine() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        for (b = socket.getInputStream().read(); b != '\n' && b != -1; b = socket.getInputStream().read())
            if (b != '\r') bos.write(b);
        if (b == -1 && bos.size() == 0) return null;
        return new String(bos.toByteArray(), 0, bos.size(), CHARSET);
    }
    
    /** Write a string in the correct char encoding */
    private void writeString(String str) throws IOException {
        socket.getOutputStream().write(str.getBytes(CHARSET));
    }
    
}
