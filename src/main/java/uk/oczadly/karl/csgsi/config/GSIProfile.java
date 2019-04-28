package uk.oczadly.karl.csgsi.config;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>This class represents a series of parameters used in the game state profile configuration,
 * and provides static utilities for creating and deleting these profiles.</p>
 *
 * <p>The class follows a similar model to the builder pattern, whereby each optional parameter
 * can be set by calling the appropriate setter methods (which return the current instance).
 * Mandatory parameters (callback URI) are passed within the class constructor.</p>
 *
 * <p>The following example demonstrates how to configure a profile with a local receiver on port
 * 80 with a timeout of 1s, a buffer of 500ms, an authentication value "token", and receives all
 * supported data components:
 *
 * <pre>
 *  GSIProfile profile = new GSIProfile("http://127.0.0.1:80")
 *          .setTimeoutPeriod(1.0)
 *          .setBufferPeriod(0.5)
 *          .addAuthToken("token", "Q79v5tcxVQ8u")
 *          .setDataComponents(EnumSet.allOf(DataComponent.class));
 * </pre></p>
 */
public class GSIProfile {
    
    private String uri, description;
    private Map<String, String> authData = new HashMap<>();
    private Double timeout, buffer, throttle, heartbeat;
    private Set<DataComponent> dataComponents = EnumSet.noneOf(DataComponent.class);
    
    
    /**
     * Constructs a new GSI configuration object with the specified URI.
     * Refer to class documentation and setters for setting other properties.
     *
     * @param uri the URI of the server, including port and protocol
     */
    public GSIProfile(String uri) {
        setURI(uri);
    }
    
    
    /**
     * @param uri the URI of the server to send state data to
     * @return this current object
     */
    public GSIProfile setURI(String uri) {
        this.uri = uri;
        return this;
    }
    
    /**
     * @return the current configured URI parameter
     */
    public String getURI() {
        return uri;
    }
    
    
    /**
     * Sets the auth token to a specified map of {@link String} elements. The map will be
     * cloned when calling this method, so future changes to the provided map won't change
     * the state of this object.
     *
     * If a null value is passed, the method will generate an empty {@link Map} instance.
     *
     * @param authData the new map of auth tokens, or null
     * @return this current object
     */
    public GSIProfile setAuthTokens(Map<String, String> authData) {
        this.authData = authData == null
                ? new HashMap<>() //Ensure stored value isn't null
                : new HashMap<>(authData); //Clone map
        return this;
    }
    
    /**
     * Adds the authentication token to the current map of values.
     *
     * @param key the key of the authentication token
     * @param token the associated value, or null to remove the key
     * @return this current object
     */
    public GSIProfile setAuthToken(String key, String token) {
        if(key == null)
            throw new IllegalArgumentException("Auth token key cannot be null");
        
        if(token == null) {
            this.authData.remove(key);
        } else {
            this.authData.put(key, token);
        }
        return this;
    }
    
    /**
     * @return the current map of authentication data to be sent by the client
     */
    public Map<String, String> getAuthTokens() {
        return authData;
    }
    
    
    /**
     * Sets the description of the configuration. Has no effect on the profile other than
     * being included within the file for the user's reference.
     * @param desc the service description
     * @return this current object
     */
    public GSIProfile setDescription(String desc) {
        this.description = desc;
        return this;
    }
    
    /**
     * @return the service description
     */
    public String getDescription() {
        return description;
    }
    
    
    /**
     * As defined on the Valve Developer Community documentation page:
     * <blockquote>
     *     Game expects an HTTP 2XX response code from its HTTP POST request, and game will not
     *     attempt submitting the next HTTP POST request while a previous request is still in
     *     flight. The game will consider the request as timed out if a response is not received
     *     within so many seconds, and will re-heartbeat next time with full state omitting any
     *     delta-computation. If the setting is not specified then default short timeout of 1.1
     *     sec will be used.
     * </blockquote>
     *
     * @param timeout the timeout value in seconds, or null to use client default
     * @return this current object
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve Developer Community</a>
     */
    public GSIProfile setTimeoutPeriod(Double timeout) {
        this.timeout = timeout;
        return this;
    }
    
    /**
     * @return the timeout period in seconds, or null if not set (default)
     * @see #setTimeoutPeriod(Double)
     */
    public Double getTimeoutPeriod() {
        return timeout;
    }
    
    
    /**
     * As defined on the Valve Developer Community documentation page:
     * <blockquote>
     *     Because multiple game events tend to occur one after another very quickly, it is recommended
     *     to specify a non-zero buffer. When buffering is enabled, the game will collect events for
     *     so many seconds to report a bigger delta. For localhost service integration this is less of
     *     an issue and can be tuned to match the needs of the service or set to 0.0 to disable buffering
     *     completely. If the setting is not specified then default buffer of 0.1 sec will be used.
     * </blockquote>
     *
     * @param buffer the buffer value in seconds, or null to use client default
     * @return this current object
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve Developer Community</a>
     */
    public GSIProfile setBufferPeriod(Double buffer) {
        this.buffer = buffer;
        return this;
    }
    
    /**
     * @return the buffer period in seconds, or null if not set (default)
     * @see #setBufferPeriod(Double)
     */
    public Double getBufferPeriod() {
        return buffer;
    }
    
    
    /**
     * As defined on the Valve Developer Community documentation page:
     * <blockquote>
     *     For high-traffic endpoints this setting will make the game client not send another request
     *     for at least this many seconds after receiving previous HTTP 2XX response to avoid notifying
     *     the service when game state changes too frequently. If the setting is not specified then
     *     default throttle of 1.0 sec will be used.
     * </blockquote>
     *
     * @param throttle the throttle value in seconds, or null to use client default
     * @return this current object
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve Developer Community</a>
     */
    public GSIProfile setThrottlePeriod(Double throttle) {
        this.throttle = throttle;
        return this;
    }
    
    /**
     * @return the timeout period in seconds, or null if not set (default)
     * @see #setThrottlePeriod(Double)
     */
    public Double getThrottlePeriod() {
        return throttle;
    }
    
    
    /**
     * As defined on the Valve Developer Community documentation page:
     * <blockquote>
     *     Even if no game state change occurs, this setting instructs the game to send a request so many
     *     seconds after receiving previous HTTP 2XX response. The service can be configured to consider
     *     game as offline or disconnected if it didn't get a notification for a significant period of
     *     time exceeding the heartbeat interval.
     * </blockquote>
     *
     * @param heartbeat the heartbeat period in seconds, or null to use client default
     * @return this current object
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve Developer Community</a>
     */
    public GSIProfile setHeartbeatPeriod(Double heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }
    
    /**
     * @return the heartbeat period in seconds, or null if not set (default)
     * @see #setHeartbeatPeriod(Double)
     */
    public Double getHeartbeatPeriod() {
        return heartbeat;
    }
    
    
    /**
     * Sets the which data values will be sent by the client. The set will be
     * cloned when calling this method, so future changes to the provided set won't change
     * the state of this object.
     *
     * If a null value is passed, the method will generate an empty {@link EnumSet} instance.
     *
     * @param reportedData the new set of data components to be sent, or null
     * @return this current object
     */
    public GSIProfile setDataComponents(Set<DataComponent> reportedData) {
        this.dataComponents = reportedData != null
                ? EnumSet.copyOf(reportedData)
                : EnumSet.noneOf(DataComponent.class);
        return this;
    }
    
    /**
     * Adds the specified data component to the current list, which will be sent by the client.
     * @param reportedData the data component
     * @return this current object
     */
    public GSIProfile addDataComponent(DataComponent reportedData) {
        this.dataComponents.add(reportedData);
        return this;
    }
    
    /**
     * @return a set of data components to be sent by the client
     */
    public Set<DataComponent> getDataComponents() {
        return dataComponents;
    }
    
    
    /**
     * Generates a valid profile configuration from the current set parameter values and outputs
     * it to the provided {@link PrintWriter} object.
     *
     * @param writer the {@link PrintWriter} to write the configuration to
     */
    public void generate(PrintWriter writer) {
        writer.println("\"" + (this.getDescription() != null ? this.getDescription().replace("\"", "\\\"") : "") + "\" {");
        
        appendParameter(writer, "uri", this.getURI());
        appendParameter(writer, "timeout", this.getTimeoutPeriod());
        appendParameter(writer, "buffer", this.getBufferPeriod());
        appendParameter(writer, "throttle", this.getThrottlePeriod());
        appendParameter(writer, "heartbeat", this.getHeartbeatPeriod());
        
        //Authentication tokens
        if(!this.getAuthTokens().isEmpty()) {
            writer.println("\"auth\" {");
            for(Map.Entry<String, String> token : this.getAuthTokens().entrySet()) {
                appendParameter(writer, token.getKey(), token.getValue());
            }
            writer.println("}");
        }
        
        //Data components to retrieve
        writer.println("\"data\" {");
        for(DataComponent type : DataComponent.values()) {
            appendParameter(writer, type.getConfigName(),
                    this.getDataComponents().contains(type) ? "1" : "0");
        }
        writer.println("}}");
    }
    
    /** Helper method for {@link #generate(PrintWriter)} */
    private static void appendParameter(PrintWriter writer, String name, Object value) {
        if(value == null) return; //Dont write empty values
        
        writer.print("\"");
        writer.print(name);
        writer.print("\" \"");
        writer.print(value.toString());
        writer.println("\"");
    }
    
    
    /**
     * <p>Creates or replaces a configuration file within the specified directory for the provided
     * {@link GSIProfile} object.</p>
     *
     * <p>The provided service name should be unique and represent your application or organisation,
     * and must conform with file naming standards.</p>
     *
     * @param dir           the directory in which the file is created
     * @param config        the profile configuration object
     * @param serviceName   the name of the service
     * @throws IOException  if the file cannot be written to
     */
    public static void createConfig(Path dir, GSIProfile config, String serviceName) throws IOException {
        if(!Files.exists(dir))
            throw new FileNotFoundException("Path argument is not an existing directory");
        if(!Files.isDirectory(dir))
            throw new NotDirectoryException("Path must be a directory");
        
        Path file = dir.resolve(generateConfigName(serviceName));
        try(PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file, Charset.forName("UTF-8")))) {
            config.generate(writer);
        }
    }
    
    /**
     * Removes a configuration file in the provided directory, if it exists.
     *
     * @param dir           the directory of the profile configuration
     * @param serviceName   the identifying service name of the profile
     * @return true if the file was successfully removed
     * @throws IOException if the file could not be removed
     */
    public static boolean removeConfig(Path dir, String serviceName) throws IOException {
        if(!Files.exists(dir))
            throw new FileNotFoundException("Path argument is not an existing directory");
        if(!Files.isDirectory(dir))
            throw new NotDirectoryException("Path must be a directory");
        
        return Files.deleteIfExists(dir.resolve(generateConfigName(serviceName)));
    }
    
    
    /** Generates the name of the configuration file based on the provided service name. */
    private static String generateConfigName(String service) {
        return "gamestate_integration_" + service.toLowerCase().replace(' ', '_') + ".cfg";
    }
    
}
