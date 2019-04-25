package uk.oczadly.karl.csgsi.config;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GSIConfig {
    
    private String uri, description;
    private Map<String, String> authData = new HashMap<>();
    private Double timeout, buffer, throttle, heartbeat;
    private Set<DataComponent> dataComponents = EnumSet.noneOf(DataComponent.class);
    
    
    public GSIConfig(URI uri) {
        this(uri.toString());
    }
    
    public GSIConfig(String uri) {
        this.uri = uri;
    }
    
    
    public String getURI() {
        return uri;
    }
    
    
    public GSIConfig setAuthToken(Map<String, String> authData) {
        this.authData = authData == null ? new HashMap<>() : new HashMap<>(authData);
        return this;
    }
    
    public GSIConfig addAuthToken(String key, String token) {
        if(key == null)
            throw new IllegalArgumentException("Auth token key cannot be null");
        
        this.authData.put(key, token);
        return this;
    }
    
    public Map<String, String> getAuthData() {
        return authData;
    }
    
    
    public GSIConfig setDescription(String desc) {
        this.description = desc;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    
    public GSIConfig setTimeoutPeriod(Double timeout) {
        this.timeout = timeout;
        return this;
    }
    
    public Double getTimeoutPeriod() {
        return timeout;
    }
    
    
    public GSIConfig setBufferPeriod(Double buffer) {
        this.buffer = buffer;
        return this;
    }
    
    public Double getBufferPeriod() {
        return buffer;
    }
    
    
    public GSIConfig setThrottlePeriod(Double throttle) {
        this.throttle = throttle;
        return this;
    }
    
    public Double getThrottlePeriod() {
        return throttle;
    }
    
    
    public GSIConfig setHeartbeatPeriod(Double heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }
    
    public Double getHeartbeatPeriod() {
        return heartbeat;
    }
    
    
    public GSIConfig setDataComponents(Set<DataComponent> reportedData) {
        this.dataComponents = EnumSet.copyOf(reportedData);
        return this;
    }
    
    public GSIConfig addDataComponent(DataComponent reportedData) {
        this.dataComponents.add(reportedData);
        return this;
    }
    
    public Set<DataComponent> getDataComponents() {
        return dataComponents;
    }
    
    
    public void generate(PrintWriter writer) throws IOException {
        writer.println("\"" + (this.getDescription() != null ? this.getDescription().replace("\"", "\\\"") : "") + "\" {");
        
        appendParameter(writer, "uri", this.getURI());
        appendParameter(writer, "timeout", this.getTimeoutPeriod());
        appendParameter(writer, "buffer", this.getBufferPeriod());
        appendParameter(writer, "throttle", this.getThrottlePeriod());
        appendParameter(writer, "heartbeat", this.getHeartbeatPeriod());
        
        if(!this.getAuthData().isEmpty()) {
            writer.println("\"auth\" {");
            for(Map.Entry<String, String> token : this.getAuthData().entrySet()) {
                appendParameter(writer, token.getKey(), token.getValue());
            }
            writer.println("}");
        }
        
        writer.println("\"data\" {");
        for(DataComponent type : DataComponent.values()) {
            //Append value if set or ALL components are currently excluded (assume user wants all)
            appendParameter(writer, type.getConfigName(),
                    (this.getDataComponents().isEmpty() || this.getDataComponents().contains(type)) ? "1" : "0");
        }
        writer.println("}}");
    }
    
    private void appendParameter(PrintWriter writer, String name, Object value) {
        if(value == null) return; //Dont write empty values
        
        writer.print("\"");
        writer.print(name);
        writer.print("\" \"");
        writer.print(value.toString());
        writer.println("\"");
    }
    
    
    public static void createConfig(Path dir, GSIConfig config, String serviceName) throws IOException {
        if(!Files.exists(dir))
            throw new FileNotFoundException("Path argument is not an existing directory");
        if(!Files.isDirectory(dir))
            throw new NotDirectoryException("Path must be a directory");
        
        Path file = dir.resolve(generateConfigName(serviceName));
        try(PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file, Charset.forName("UTF-8")))) {
            config.generate(writer);
        }
    }
    
    public static void removeConfig(Path dir, String serviceName) throws IOException {
        if(!Files.exists(dir))
            throw new FileNotFoundException("Path argument is not an existing directory");
        if(!Files.isDirectory(dir))
            throw new NotDirectoryException("Path must be a directory");
        
        Files.delete(dir.resolve(generateConfigName(serviceName)));
    }
    
    
    private static String generateConfigName(String service) {
        return "gamestate_integration_" + service.toLowerCase().replace(' ', '_') + ".cfg";
    }
    
}
