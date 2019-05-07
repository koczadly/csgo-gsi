package uk.oczadly.karl.csgsi.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements a basic HTTP server for the use of retrieving request data. The server always returns a 200
 * OK response, complete with an empty body of data.
 */
public class HTTPServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPServer.class);
    
    
    private int port;
    private ExecutorService executorService;
    private HTTPConnectionHandler handler;
    
    private Thread thread;
    private ServerSocket socket;
    
    
    /**
     * @param port              the port number to listen on
     * @param executorService   the ExecutorService used to handle HTTP requests
     * @param handler           the handling class to receive HTTP requests
     */
    public HTTPServer(int port, ExecutorService executorService, HTTPConnectionHandler handler) {
        this.port = port;
        this.executorService = executorService;
        this.handler = handler;
    }
    
    /**
     * @param port              the port number to listen on
     * @param maxConnections    the maximum number of connections to be processed at one time
     * @param handler           the handling class to receive HTTP requests
     */
    public HTTPServer(int port, int maxConnections, HTTPConnectionHandler handler) {
        this(port, Executors.newFixedThreadPool(maxConnections), handler);
    }
    
    
    /**
     * @return the specified port to listen on
     */
    public int getPort() {
        return port;
    }
    
    /**
     * @return true if the server is currently running
     */
    public boolean isRunning() {
        return thread != null && thread.isAlive();
    }
    
    
    /**
     * Starts the server inside a newly issued thread.
     * @throws IOException              if the port cannot be opened
     * @throws IllegalStateException    if the server is already running
     */
    public void start() throws IOException {
        if (isRunning())
            throw new IllegalStateException("Server is already running.");
        
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Starting HTTP server on port {}...", port);
        
        
        socket = new ServerSocket(port);
        thread = new Thread(new ConnectionAcceptor());
        thread.start();
    }
    
    /**
     * Stops the server from running and frees the socket port.
     * @throws IllegalStateException if the server is not currently running
     */
    public void stop() {
        if(!isRunning())
            throw new IllegalStateException("Server is not currently running.");
    
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Stopping HTTP server on port {}...", port);
        
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
    
    
    
    private class ConnectionAcceptor implements Runnable {
        
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket conn = socket.accept();
                    executorService.submit(new HTTPConnection(conn, handler));
                } catch (IOException e) {
                    LOGGER.warn("Exception occured while handling HTTP connection", e);
                }
            }
        }
        
    }
    
}
