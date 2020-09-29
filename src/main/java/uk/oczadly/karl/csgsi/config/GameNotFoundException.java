package uk.oczadly.karl.csgsi.config;

/**
 * This exception is thrown when the a game directory could not be found.
 */
public class GameNotFoundException extends Exception {
    
    public GameNotFoundException() {
        super();
    }
    
    public GameNotFoundException(String msg) {
        super(msg);
    }
    
    public GameNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
