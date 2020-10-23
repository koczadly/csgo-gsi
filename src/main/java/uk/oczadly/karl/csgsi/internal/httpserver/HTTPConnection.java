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
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPConnection.class);
    
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
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            
            // Read start-line header
            String startLine = readLine(is);
            if (startLine == null) {
                LOGGER.warn("Socket InputStream returned null data.");
                return;
            }
            Matcher startMatcher = START_REGEX.matcher(startLine);
            if (!startMatcher.matches()) {
                LOGGER.warn("Invalid HTTP start-line header \"{}\"!", startLine);
                return;
            }
            String reqMethod = startMatcher.group(1).toUpperCase();
            String reqPath = URLDecoder.decode(startMatcher.group(2), CHARSET.name());
            
            // Read headers and body
            Map<String, String> headers = parseHeaders(is);
            LOGGER.debug("Parsed {} headers from request.", headers.size());
            String body = null;
            if (headers.containsKey("content-length")) {
                body = readBody(socket.getInputStream(), Integer.parseInt(headers.get("content-length")));
            }
            
            // Handle response
            HTTPResponse res;
            try {
                res = handler.handle(socket.getInetAddress(), reqPath, reqMethod, headers, body);
            } catch (Exception e) {
                LOGGER.error("Handler threw uncaught exception.", e);
                res = new HTTPResponse(500);
            }

            // Return header & body data
            LOGGER.debug("Writing response data, status code: {}, body len: {}...",
                    res.getStatusCode(), res.getBody() != null ? res.getBody().length() : 0);
            writeResponse(res, socket.getOutputStream());

            // Close socket
            os.flush();
        } catch (Exception e) {
            LOGGER.error("Failed to handle HTTP connection.", e);
        } finally {
            //Close socket
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
        }
    }
    
    
    /** Parse a set of headers into a map */
    private static Map<String, String> parseHeaders(InputStream is) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String s;
        while ((s = readLine(is)) != null) {
            if (s.equals("")) break; // End of headers
            
            Matcher matcher = HEADER_REGEX.matcher(s);
            if (matcher.matches()) {
                headers.put(matcher.group(1).toLowerCase(), matcher.group(2));
            }
        }
        return headers;
    }
    
    /** Read the body as a string */
    private static String readBody(InputStream is, int length) throws IOException {
        byte[] buffer = new byte[length];
        int readLen = is.read(buffer, 0, length);
        if (readLen == -1) {
            LOGGER.debug("Read 0 bytes as stream has ended.");
            return null;
        }
        return new String(buffer, 0, readLen, CHARSET);
    }
    
    /** Write response message and server information */
    private static void writeResponse(HTTPResponse res, OutputStream os) throws IOException {
        writeString(os, "HTTP/1.1 " + res.getStatusCode());
        if (res.getStatusCode() >= 200 && res.getStatusCode() < 300) {
            writeString(os, " OK\r\n"); // 200 series
        } else {
            writeString(os, " Error\r\n"); // non-200 series
        }
        if (res.getBody() != null) {
            byte[] body = res.getBody().getBytes(CHARSET);
            writeString(os, "Connection: close\r\n");
            writeString(os, "Content-length: " + body.length + "\r\n");
            String contentType = res.getContentType() != null ? res.getContentType() : "text/plain";
            writeString(os, "Content-type: " + contentType + "; charset=" + CHARSET.name() + "\r\n\r\n");
            os.write(body);
        }
        os.close();
    }
    
    /** Read a line without buffering/reading further */
    private static String readLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c;
        for (c = inputStream.read(); c != '\n' && c != -1; c = inputStream.read())
            if (c != '\r') bos.write(c);
        if (c == -1 && bos.size() == 0) return null;
        return new String(bos.toByteArray(), 0, bos.size(), CHARSET);
    }
    
    /** Write a string in the correct char encoding */
    private static void writeString(OutputStream os, String str) throws IOException {
        os.write(str.getBytes(CHARSET));
    }
    
}
