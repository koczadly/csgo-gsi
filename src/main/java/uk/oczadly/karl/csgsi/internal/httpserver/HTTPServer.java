package uk.oczadly.karl.csgsi.internal.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class implements a basic HTTP server for the use of retrieving request data. The server always returns a 200 OK
 * response, complete with an empty body of data.
 */
public class HTTPServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPServer.class);
    
    
    private int port;
    private InetAddress bindAddr;
    private HTTPRequestHandler handler;
    
    private Thread thread;
    private ServerSocket socket;
    
    
    /**
     * @param port            the port number to listen on
     * @param handler         the handling class to receive HTTP requests
     */
    public HTTPServer(int port, InetAddress bindAddr, HTTPRequestHandler handler) {
        this.port = port;
        this.bindAddr = bindAddr;
        this.handler = handler;
    }
    
    
    /**
     * @return the specified port to listen on
     */
    public int getPort() {
        return port;
    }
    
    public InetAddress getBindAddress() {
        return bindAddr;
    }
    
    /**
     * @return true if the server is currently running
     */
    public boolean isRunning() {
        return thread != null && thread.isAlive();
    }
    
    
    /**
     * Starts the server inside a newly issued thread.
     *
     * @throws IOException           if the port cannot be opened
     * @throws IllegalStateException if the server is already running
     */
    public void start() throws IOException {
        if (isRunning())
            throw new IllegalStateException("Server is already running.");
        
        LOGGER.info("Starting HTTP server on port {}...", port);
        
        socket = new ServerSocket(port, 50, bindAddr);
        thread = new Thread(new ConnectionAcceptorTask());
        thread.start();
    }
    
    /**
     * Stops the server from running and frees the socket port.
     *
     * @throws IllegalStateException if the server is not currently running
     */
    public void stop() {
        if (!isRunning())
            throw new IllegalStateException("Server is not currently running.");
        
        LOGGER.info("Stopping HTTP server on port {}...", port);
        
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
    
    
    /** Accepts new client connections and handles them */
    private class ConnectionAcceptorTask implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket conn = socket.accept();
                    LOGGER.debug("Incoming HTTP request from {} on server port {}...",
                            conn.getInetAddress(), getPort());
                    new HTTPConnection(conn, handler).run();
                } catch (Exception e) {
                    LOGGER.error("Exception occured while handling HTTP connection", e);
                }
            }
        }
    }
    
}
