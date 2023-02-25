package uk.oczadly.karl.csgsi.internal.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements a basic HTTP server for the use of retrieving request data. The server always returns a 200 OK
 * response, complete with an empty body of data.
 */
public class HTTPServer {
    
    private static final Logger log = LoggerFactory.getLogger(HTTPServer.class);

    private static final int TIMEOUT = 120000; // Timeout for read & keepalive

    private final InetSocketAddress bindAddr;
    private final HTTPRequestHandler handler;
    private final ExecutorService executorService = Executors.newFixedThreadPool(250);
    
    private volatile Thread thread;
    private volatile ServerSocket socket;
    

    public HTTPServer(InetSocketAddress bindAddr, HTTPRequestHandler handler) {
        this.bindAddr = bindAddr;
        this.handler = handler;
    }
    
    public InetSocketAddress getBindAddress() {
        return bindAddr;
    }
    
    /**
     * @return true if the server is currently running
     */
    public synchronized boolean isRunning() {
        return thread != null && thread.isAlive();
    }
    
    
    /**
     * Starts the server inside a newly issued thread.
     *
     * @throws IOException           if the port cannot be opened
     * @throws IllegalStateException if the server is already running
     */
    public synchronized void start() throws IOException {
        if (isRunning())
            throw new IllegalStateException("Server is already running.");
        
        log.info("Starting HTTP server on interface {}...", bindAddr);
        socket = new ServerSocket(bindAddr.getPort(), 50, bindAddr.getAddress());
        socket.setSoTimeout(0);
        thread = new Thread(new ConnectionAcceptorTask());
        thread.start();
    }
    
    /**
     * Stops the server from running and frees the socket port.
     *
     * @throws IllegalStateException if the server is not currently running
     */
    public synchronized void stop() {
        if (!isRunning())
            throw new IllegalStateException("Server is not currently running.");
        
        log.info("Stopping HTTP server on interface {}...", bindAddr);
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
    
    
    /** Accepts new client connections and handles them */
    private class ConnectionAcceptorTask implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    log.trace("Awaiting HTTP connection...");
                    Socket conn = socket.accept();
                    conn.setSoTimeout(TIMEOUT);
                    conn.setKeepAlive(true);
                    log.debug("Incoming HTTP request from {} on server interface {}...",
                            conn.getInetAddress(), getBindAddress());
                    executorService.submit(new HTTPConnection(conn, handler));
                } catch (IOException e) {
                    log.debug("Error/timeout while waiting for HTTP connection.", e);
                }
            }
        }
    }
    
}
