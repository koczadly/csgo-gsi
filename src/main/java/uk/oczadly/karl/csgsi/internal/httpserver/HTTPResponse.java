package uk.oczadly.karl.csgsi.internal.httpserver;

/**
 * @author Karl Oczadly
 */
public class HTTPResponse {

    private final int statusCode;
    private final String contentType, body;
    
    public HTTPResponse(int statusCode) {
        this(statusCode, null, null);
    }
    
    public HTTPResponse(int statusCode, String contentType, String body) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
    }
    
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public String getBody() {
        return body;
    }
    
}
