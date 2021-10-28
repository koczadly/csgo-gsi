package uk.oczadly.karl.csgsi.internal.httpserver;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HTTPRequest {

    private final InetAddress remoteAddr;
    private final String path, method;
    private final Map<String, String> headers;
    private final ByteBuffer body;
    private volatile String bodyString;

    public HTTPRequest(InetAddress remoteAddr, String path, String method, Map<String, String> headers,
                       ByteBuffer body) {
        this.remoteAddr = remoteAddr;
        this.path = path;
        this.method = method.toUpperCase();
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body;
    }


    public InetAddress getRemoteAddress() {
        return remoteAddr;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Optional<String> getHeader(String key) {
        return Optional.ofNullable(headers.get(key.toLowerCase()));
    }

    public String getHeader(String key, String defaultVal) {
        return getHeader(key).orElse(defaultVal);
    }

    public boolean hasBody() {
        return getBodyLength() > 0;
    }

    public int getBodyLength() {
        return body.limit();
    }

    public ByteBuffer getBody() {
        return body.asReadOnlyBuffer();
    }

    // todo: get charset from content-type
    public synchronized String getBodyAsString() {
        if (bodyString == null) {
            bodyString = StandardCharsets.ISO_8859_1.decode(getBody()).toString();
        }
        return bodyString;
    }

}
