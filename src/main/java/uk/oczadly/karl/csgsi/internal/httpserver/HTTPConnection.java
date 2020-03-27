package uk.oczadly.karl.csgsi.internal.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class HTTPConnection implements Runnable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPConnection.class);
    
    private Socket socket;
    private HTTPConnectionHandler handler;
    
    public HTTPConnection(Socket socket, HTTPConnectionHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }
    
    
    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            
            String[] requestData = reader.readLine().split(" ");
            
            //Headers
            Map<String, String> headers = parseHeaders(reader);
            if (!headers.containsKey("content-length")) {
                LOGGER.warn("No content-length header found!");
                return;
            }
            
            //Body
            String body = parseBody(reader, Integer.parseInt(headers.get("content-length")));
            
            //Return 200 OK
            writer.write("HTTP/1.1 200 OK\r\n\r\n");
            writer.flush();
            
            //Close socket
            this.socket.close();
            
            handler.handle(socket.getInetAddress(), requestData.length > 2 ? requestData[1] : "/", requestData[0],
                    headers, body);
        } catch (Exception e) {
            LOGGER.warn("Failed to handle HTTP connection", e);
        } finally {
            //Close socket
            try {
                if (!this.socket.isClosed()) this.socket.close();
            } catch (IOException ignored) {
            }
        }
    }
    
    
    private Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        
        String s;
        while ((s = reader.readLine()) != null) {
            if (s.equals(""))
                break; //End of headers
            
            String[] header = s.split(":", 2);
            headers.put(header[0].toLowerCase(), header[1].trim());
        }
        return headers;
    }
    
    private String parseBody(BufferedReader reader, int length) throws IOException {
        char[] buffer = new char[length];
        reader.read(buffer, 0, length);
        return String.valueOf(buffer);
    }
    
}
