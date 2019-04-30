package uk.oczadly.karl.csgsi.config;

public class SteamLibraryException extends Exception {
    
    public SteamLibraryException() {
        super();
    }
    
    public SteamLibraryException(String msg) {
        super(msg);
    }
    
    public SteamLibraryException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
