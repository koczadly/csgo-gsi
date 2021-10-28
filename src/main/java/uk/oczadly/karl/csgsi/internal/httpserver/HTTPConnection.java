package uk.oczadly.karl.csgsi.internal.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
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
    private static final Pattern START_REGEX = Pattern.compile("^(\\w+) (.+) (HTTP/[0-9.]+)$");
    
    private final Socket socket;
    private final HTTPRequestHandler handler;
    
    public HTTPConnection(Socket socket, HTTPRequestHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }
    
    
    @Override
    public void run() {
        try {
            // Read start-line header
            String startLine = readLine();
            if (startLine == null) {
                log.warn("Socket InputStream returned null data.");
                return;
            }
            Matcher startMatcher = START_REGEX.matcher(startLine);
            if (!startMatcher.matches()) {
                log.warn("Invalid HTTP start-line header \"{}\"!", startLine);
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

            // Handle response
            HTTPResponse response;
            try {
                response = handler.handle(socket.getInetAddress(), reqPath, reqMethod, headers, body);
            } catch (Exception e) {
                log.error("Handler threw uncaught exception.", e);
                response = new HTTPResponse(500);
            }

            // Return header & body data
            writeResponse(response);
        } catch (IOException e) {
            log.error("Failed to handle HTTP connection.", e);
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
    private void writeResponse(HTTPResponse response) throws IOException {
        log.debug("Writing response data, status code: {}...", response.getStatusCode());

        OutputStream os = socket.getOutputStream();
        writeString("HTTP/1.1 " + response.getStatusCode());
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            writeString(" OK\r\n"); // 200 series
        } else {
            writeString(" Error\r\n"); // non-200 series
        }
        writeString("Connection: close\r\n");

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
        int c;
        for (c = socket.getInputStream().read(); c != '\n' && c != -1; c = socket.getInputStream().read())
            if (c != '\r') bos.write(c);
        if (c == -1 && bos.size() == 0) return null;
        return new String(bos.toByteArray(), 0, bos.size(), CHARSET);
    }
    
    /** Write a string in the correct char encoding */
    private void writeString(String str) throws IOException {
        socket.getOutputStream().write(str.getBytes(CHARSET));
    }
    
}
