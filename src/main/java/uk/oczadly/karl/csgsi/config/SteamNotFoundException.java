package uk.oczadly.karl.csgsi.config;

/**
 * This exception is thrown when the Steam installation directory cannot be found, or if an error occurs while
 * attempting to find the Steam directory.
 */
public class SteamNotFoundException extends GameNotFoundException {
    
    public SteamNotFoundException() {
        super();
    }
    
    public SteamNotFoundException(String msg) {
        super(msg);
    }
    
    public SteamNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
