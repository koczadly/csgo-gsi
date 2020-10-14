package uk.oczadly.karl.csgsi.internal.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HTTPConnection implements Runnable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPConnection.class);
    
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Pattern HEADER_REGEX = Pattern.compile("^([\\w-]+)\\s*:\\s*(.+)$");
    private static final Pattern START_REGEX = Pattern.compile("^(.+) (.+) (.+)$");
    
    private Socket socket;
    private HTTPRequestHandler handler;
    
    public HTTPConnection(Socket socket, HTTPRequestHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }
    
    
    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            
            String startLine = readLine(is);
            if (startLine == null) {
                LOGGER.warn("HTTP stream was null");
                return;
            }
            Matcher startMatcher = START_REGEX.matcher(startLine);
            if (!startMatcher.matches()) {
                LOGGER.warn("Invalid HTTP start-line header!");
                return;
            }

            // Headers
            Map<String, String> headers = parseHeaders(is);
            String body = null;
            if (headers.containsKey("content-length")) {
                body = readBody(socket.getInputStream(), Integer.parseInt(headers.get("content-length")));
            }
    
            // Handle response
            HTTPResponse res;
            try {
                res = handler.handle(socket.getInetAddress(),
                        startMatcher.group(2), startMatcher.group(1), headers, body);
            } catch (Exception e) {
                LOGGER.warn("HTTP handler threw uncaught exception", e);
                e.printStackTrace();
                res = new HTTPResponse(500, null, null);
            }

            // Return header & body data
            writeResponse(res, socket.getOutputStream());

            // Close socket
            os.flush();
        } catch (Exception e) {
            LOGGER.warn("Failed to handle HTTP connection", e);
            e.printStackTrace();
        } finally {
            //Close socket
            try {
                if (!socket.isClosed())
                    socket.close();
            } catch (IOException ignored) {}
        }
    }
    
    
    private Map<String, String> parseHeaders(InputStream is) throws IOException {
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
    
    private String readBody(InputStream is, int length) throws IOException {
        byte[] buffer = new byte[length];
        is.read(buffer, 0, length);
        return new String(buffer, CHARSET);
    }
    
    private void writeResponse(HTTPResponse res, OutputStream os) throws IOException {
        writeString(os, "HTTP/1.1 " + res.getStatusCode() + " OK\r\n");
        if (res.getBody() != null) {
            byte[] body = res.getBody().getBytes(CHARSET);
            writeString(os, "Content-length: " + body.length + "\r\n");
            String contentType = res.getContentType() != null ? res.getContentType() : "text/plain";
            writeString(os, "Content-type: " + contentType + "\r\n\r\n");
            writeString(os, res.getBody());
        }
        os.flush();
    }
    
    private String readLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c;
        for (c = inputStream.read(); c != '\n' && c != -1; c = inputStream.read())
            if (c != '\r') bos.write(c);
        if (c == -1 && bos.size() == 0) return null;
        return bos.toString("UTF-8");
    }
    
    private void writeString(OutputStream os, String str) throws IOException {
        os.write(str.getBytes(CHARSET));
    }
    
}
