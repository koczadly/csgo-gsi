package uk.oczadly.karl.csgsi.config;

/**
 * This exception is thrown when the Steam installation directory cannot be found, or if an error occurs while
 * attempting to find the Steam directory.
 */
public class SteamDirectoryException extends Exception {
    
    public SteamDirectoryException() {
        super();
    }
    
    public SteamDirectoryException(String msg) {
        super(msg);
    }
    
    public SteamDirectoryException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
