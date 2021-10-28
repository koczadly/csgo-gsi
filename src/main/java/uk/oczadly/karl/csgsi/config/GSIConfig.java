package uk.oczadly.karl.csgsi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.internal.Util;
import uk.oczadly.karl.csgsi.util.system.CsgoUtils;
import uk.oczadly.karl.csgsi.util.system.GameNotFoundException;
import uk.oczadly.karl.csgsi.util.system.ValveConfigWriter;

import java.io.*;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>This class represents a series of parameters used in the game state profile configuration, and provides static
 * utilities for creating and deleting these profiles.</p>
 *
 * <p>The class is structured similarly to the builder pattern, whereby each parameter can be set by calling the
 * appropriate setter methods (which all return the {@code GSIConfig} instance to allow for method chaining).</p>
 *
 * <p>The following example demonstrates how to configure a profile with a local server on port 80 with a timeout of
 * 1 second, a buffer of 500ms, an authentication value "password", and receives the provider and round components:
 * <pre>
 *  GSIConfig config = new GSIConfig()
 *          .setLocalServerPort(80)  // localhost:80
 *          .setTimeoutPeriod(1.0)
 *          .setBufferPeriod(0.5)
 *          .includeAuthToken("password", "Q79v5tcxVQ8u")
 *          .subscribeComponents(DataComponent.PROVIDER, DataComponent.ROUND);
 * </pre>
 *
 * <p>Profiles can then be created and written to the system using the {@link #writeFile(String)} method (refer to
 * method documentation).</p>
 *
 * <p>Your applications service name should be unique, and can only contain standard english word and digit
 * characters, as well as underscores, and must be between 3 and 32 characters in length. The name must
 * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
 */
public final class GSIConfig {
    
    private static final Logger log = LoggerFactory.getLogger(GSIConfig.class);

    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.0###");
    private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("^\\w{3,32}$");
    private static final String DEFAULT_DESC = "Generated using " + Util.REPO_URL;
    
    private String url, description = DEFAULT_DESC;
    private Double timeout, buffer, throttle, heartbeat;
    private Integer precisionTime, precisionPosition, precisionVector;
    private final Map<String, String> authTokens = new HashMap<>();
    private EnumSet<DataComponent> dataComponents = EnumSet.noneOf(DataComponent.class);


    static {
        DOUBLE_FORMAT.setRoundingMode(RoundingMode.UP);
    }
    

    /**
     * Constructs a new GSI configuration object. Refer to class documentation and setter methods for configuring
     * properties.
     *
     * <p>The default server URL will be {@code localhost:8080} unless overridden through the setters.</p>
     */
    public GSIConfig() {
        setLocalServerPort(8080);
    }
    
    
    /**
     * Sets the URI of the server to the local machine ({@code localhost}) on the specified port, using the
     * {@code HTTP} protocol.
     *
     * @param port the port of the server
     * @return this GSIConfig instance
     *
     * @throws NullPointerException if the provided {@code uri} argument is null
     */
    public GSIConfig setLocalServerPort(int port) {
        return setServerURL("localhost", port);
    }
    
    /**
     * Sets the URL of the server to the specified host and port, using the {@code HTTP} protocol.
     *
     * @param host the hostname or address of the server (eg. "{@code 127.0.0.1}")
     * @param port the port of the server
     * @return this GSIConfig instance
     *
     * @throws NullPointerException if the provided {@code uri} argument is null
     */
    public GSIConfig setServerURL(String host, int port) {
        if (port < 1 || port > 65535)
            throw new NullPointerException("Port value is outside the allowable range.");
        return setServerURL("http://" + host + ":" + port);
    }
    
    /**
     * @param url the URL (including protocol and port) of the server
     * @return this GSIConfig instance
     *
     * @throws NullPointerException if the provided {@code uri} argument is null
     */
    public GSIConfig setServerURL(String url) {
        if (url == null) throw new NullPointerException("URL argument cannot be null.");
        this.url = url;
        return this;
    }
    
    /**
     * @param url the URL of the server
     * @return this GSIConfig instance
     *
     * @throws NullPointerException if the provided {@code uri} argument is null
     */
    public GSIConfig setServerURL(URL url) {
        if (url == null) throw new NullPointerException("URL argument cannot be null.");
        return setServerURL(url.toString());
    }
    
    /**
     * @return the current configured URL parameter
     */
    public String getURL() {
        return url;
    }


    /**
     * Clears all authentication tokens currently in the map.
     *
     * @return this GSIConfig instance
     */
    public GSIConfig clearAuthTokens() {
        this.authTokens.clear();
        return this;
    }

    /**
     * Sets the authentication tokens to a specified map of {@link String} elements.
     *
     * @param authTokens the new map of auth tokens, or null
     * @return this GSIConfig instance
     */
    public GSIConfig setAuthTokens(Map<String, String> authTokens) {
        this.authTokens.clear();
        if (authTokens != null)
            this.authTokens.putAll(authTokens);
        return this;
    }
    
    /**
     * Adds all the specified authentication tokens to the current map.
     *
     * @param authTokens the new map of auth tokens
     * @return this GSIConfig instance
     */
    public GSIConfig includeAuthTokens(Map<String, String> authTokens) {
        this.authTokens.putAll(authTokens);
        return this;
    }
    
    /**
     * Adds the authentication token to the current map of values.
     *
     * @param key   the key of the authentication token
     * @param tokenValue the associated value, or null to remove the key
     * @return this GSIConfig instance
     *
     * @throws NullPointerException if the key value is null
     */
    public GSIConfig includeAuthToken(String key, String tokenValue) {
        if (key == null)
            throw new NullPointerException("Auth token key cannot be null");
        
        if (tokenValue == null) {
            this.authTokens.remove(key);
        } else {
            this.authTokens.put(key, tokenValue);
        }
        return this;
    }
    
    /**
     * @return an immutable map of configured auth tokens to be sent by the client
     */
    public Map<String, String> getAuthTokens() {
        return Collections.unmodifiableMap(authTokens);
    }
    
    
    /**
     * Sets the description of the configuration. Has no effect on the profile other than being included within the file
     * for the user's reference.
     *
     * @param desc the service description
     * @return this GSIConfig instance
     */
    public GSIConfig setDescription(String desc) {
        this.description = Objects.requireNonNullElse(desc, DEFAULT_DESC);
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
     * Game expects an HTTP 2XX response code from its HTTP POST request, and game will not attempt submitting the next
     * HTTP POST request while a previous request is still in flight. The game will consider the request as timed out if
     * a response is not received within so many seconds, and will re-heartbeat next time with full state omitting any
     * delta-computation. If the setting is not specified then default short timeout of 1.1 sec will be used.
     * </blockquote>
     *
     * @param timeout the timeout value in seconds, or null to use client default
     * @return this GSIConfig instance
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve
     * Developer Community</a>
     */
    public GSIConfig setTimeoutPeriod(Double timeout) {
        if (timeout != null && timeout < 0)
            throw new IllegalArgumentException("Timeout duration cannot be negative.");
        this.timeout = timeout;
        return this;
    }
    
    /**
     * @return the timeout period in seconds, or empty if not set (default)
     *
     * @see #setTimeoutPeriod(Double)
     */
    public Optional<Double> getTimeoutPeriod() {
        return Optional.ofNullable(timeout);
    }
    
    
    /**
     * As defined on the Valve Developer Community documentation page:
     * <blockquote>
     * Because multiple game events tend to occur one after another very quickly, it is recommended to specify a
     * non-zero buffer. When buffering is enabled, the game will collect events for so many seconds to report a bigger
     * delta. For localhost service integration this is less of an issue and can be tuned to match the needs of the
     * service or set to 0.0 to disable buffering completely. If the setting is not specified then default buffer of 0.1
     * sec will be used.
     * </blockquote>
     *
     * @param buffer the buffer value in seconds, or null to use client default
     * @return this GSIConfig instance
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve
     * Developer Community</a>
     */
    public GSIConfig setBufferPeriod(Double buffer) {
        if (buffer != null && buffer < 0)
            throw new IllegalArgumentException("Buffer period cannot be negative.");
        this.buffer = buffer;
        return this;
    }
    
    /**
     * Disables game state buffering by setting the buffer period to {@code 0}.
     *
     * <p>As defined on the Valve Developer Community documentation page:</p>
     * <blockquote>
     * Because multiple game events tend to occur one after another very quickly, it is recommended to specify a
     * non-zero buffer. When buffering is enabled, the game will collect events for so many seconds to report a bigger
     * delta. For localhost service integration this is less of an issue and can be tuned to match the needs of the
     * service or set to 0.0 to disable buffering completely. If the setting is not specified then default buffer of 0.1
     * sec will be used.
     * </blockquote>
     *
     * @return this GSIConfig instance
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve
     * Developer Community</a>
     */
    public GSIConfig disableBuffering() {
        return setBufferPeriod(0d);
    }
    
    /**
     * @return the buffer period in seconds, or empty if not set (default)
     *
     * @see #setBufferPeriod(Double)
     */
    public Optional<Double> getBufferPeriod() {
        return Optional.ofNullable(buffer);
    }
    
    
    /**
     * As defined on the Valve Developer Community documentation page:
     * <blockquote>
     * For high-traffic endpoints this setting will make the game client not send another request for at least this many
     * seconds after receiving previous HTTP 2XX response to avoid notifying the service when game state changes too
     * frequently. If the setting is not specified then default throttle of 1.0 sec will be used.
     * </blockquote>
     *
     * @param throttle the throttle value in seconds, or null to use client default
     * @return this GSIConfig instance
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">
     *     Valve Developer Community</a>
     */
    public GSIConfig setThrottlePeriod(Double throttle) {
        if (throttle != null && throttle < 0)
            throw new IllegalArgumentException("Throttle period cannot be negative.");
        this.throttle = throttle;
        return this;
    }
    
    /**
     * Disables game state throttling by setting the throttle period to {@code 0}.
     *
     * <p>As defined on the Valve Developer Community documentation page:</p>
     * <blockquote>
     * For high-traffic endpoints this setting will make the game client not send another request for at least this many
     * seconds after receiving previous HTTP 2XX response to avoid notifying the service when game state changes too
     * frequently. If the setting is not specified then default throttle of 1.0 sec will be used.
     * </blockquote>
     *
     * @return this GSIConfig instance
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">
     *     Valve Developer Community</a>
     */
    public GSIConfig disableThrottling() {
        return setBufferPeriod(0d);
    }
    
    /**
     * @return the timeout period in seconds, or empty if not set (default)
     *
     * @see #setThrottlePeriod(Double)
     */
    public Optional<Double> getThrottlePeriod() {
        return Optional.ofNullable(throttle);
    }
    
    
    /**
     * As defined on the Valve Developer Community documentation page:
     * <blockquote>
     * Even if no game state change occurs, this setting instructs the game to send a request so many seconds after
     * receiving previous HTTP 2XX response. The service can be configured to consider game as offline or disconnected
     * if it didn't get a notification for a significant period of time exceeding the heartbeat interval.
     * </blockquote>
     *
     * @param heartbeat the heartbeat period in seconds, or null to use client default
     * @return this GSIConfig instance
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve
     * Developer Community</a>
     */
    public GSIConfig setHeartbeatPeriod(Double heartbeat) {
        if (heartbeat != null && heartbeat < 0)
            throw new IllegalArgumentException("Heartbeat period cannot be negative.");
        this.heartbeat = heartbeat;
        return this;
    }
    
    /**
     * @return the heartbeat period in seconds, or empty if not set (default)
     *
     * @see #setHeartbeatPeriod(Double)
     */
    public Optional<Double> getHeartbeatPeriod() {
        return Optional.ofNullable(heartbeat);
    }
    

    /**
     * @return the number of decimal places for time units, or empty for default
     * @see #setPrecisionTime(Integer)
     */
    public Optional<Integer> getPrecisionTime() {
        return Optional.ofNullable(precisionTime);
    }
    
    /**
     * Sets the number of decimal places to be sent for values containing a measurement of time. Use a value of null
     * if you wish to use the default client value.
     *
     * @param precisionTime the number of decimal places for time units, or null for default
     * @return this GSIConfig instance
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">
     * Valve Developer Community</a>
     */
    public GSIConfig setPrecisionTime(Integer precisionTime) {
        if (precisionTime != null && precisionTime < 0)
            throw new IllegalArgumentException("Precision cannot be less than zero.");
        this.precisionTime = precisionTime;
        return this;
    }
    
    /**
     * @return the number of decimal places for positional units, or empty for default
     * @see #setPrecisionPosition(Integer)
     */
    public Optional<Integer> getPrecisionPosition() {
        return Optional.ofNullable(precisionPosition);
    }
    
    /**
     * Sets the number of decimal places to be sent for values containing a units of position (coordinates). Use a value
     * of null if you wish to use the default client value.
     *
     * @param precisionPosition the number of decimal places for position units, or null for default
     * @return this GSIConfig instance
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">
     * Valve Developer Community</a>
     */
    public GSIConfig setPrecisionPosition(Integer precisionPosition) {
        if (precisionPosition != null && precisionPosition < 0)
            throw new IllegalArgumentException("Precision cannot be less than zero.");
        this.precisionPosition = precisionPosition;
        return this;
    }
    
    /**
     * @return the number of decimal places for vector units, or empty for default
     * @see #setPrecisionVector(Integer)
     */
    public Optional<Integer> getPrecisionVector() {
        return Optional.ofNullable(precisionVector);
    }
    
    /**
     * Sets the number of decimal places to be sent for values containing a vector. Use a value of null if you wish to
     * use the default client value.
     *
     * @param precisionVector the number of decimal places for vector units, or null for default
     * @return this GSIConfig instance
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">
     * Valve Developer Community</a>
     */
    public GSIConfig setPrecisionVector(Integer precisionVector) {
        if (precisionVector != null && precisionVector < 0)
            throw new IllegalArgumentException("Precision cannot be less than zero.");
        this.precisionVector = precisionVector;
        return this;
    }


    /**
     * Clears all currently subscribed data components, emptying the set.
     *
     * @return this GSIConfig instance
     */
    public GSIConfig clearSubscribedComponents() {
        this.dataComponents.clear();
        return this;
    }

    /**
     * Replaces the contents of the current data component subscription set with the given set.
     *
     * <p>Subscribed components will be sent from the game client when available. If a component is not subscribed to
     * in the configuration, the game client will never send any data.</p>
     *
     * @param components the new set of data components to be sent
     * @return this GSIConfig instance
     */
    public GSIConfig setSubscribedComponents(Set<DataComponent> components) {
        if (components == null)
            throw new IllegalArgumentException("Data components cannot be null.");
        this.dataComponents.clear();
        this.dataComponents.addAll(components);
        return this;
    }

    /**
     * Appends the specified data components to the current component subscription set.
     *
     * <p>Subscribed components will be sent from the game client when available. If a component is not subscribed to
     * in the configuration, the game client will never send any data.</p>
     *
     * @param components the data components to subscribe to
     * @return this GSIConfig instance
     */
    public GSIConfig subscribeComponents(DataComponent... components) {
        if (components == null)
            throw new IllegalArgumentException("Data components cannot be null.");
        this.dataComponents.addAll(Arrays.asList(components));
        return this;
    }
    
    /**
     * Sets the configuration so that all available data components will be sent by the client.
     *
     * @return this GSIConfig instance
     */
    public GSIConfig subscribeAllComponents() {
        this.dataComponents = EnumSet.allOf(DataComponent.class);
        return this;
    }
    
    /**
     * @return an immutable set of data components to be sent by the client
     */
    public Set<DataComponent> getDataComponents() {
        return Collections.unmodifiableSet(dataComponents);
    }
    
    
    private void validateState() {
        if (url == null)
            throw new IllegalStateException("No URL is set.");
        if (dataComponents.isEmpty())
            throw new IllegalStateException("No data components are specified.");
    }
    
    /**
     * Generates a configuration profile from the current set parameter values and returns it as a String value.
     *
     * <p>This method uses the built-in {@link ValveConfigWriter} class to generate and write the configuration file
     * contents. New lines will be separated by the value returned by {@link System#lineSeparator()}.</p>
     *
     * @throws IllegalStateException if one or more configuration values aren't set or are invalid
     *
     * @see #export(Writer)
     */
    public String export() {
        try {
            Writer sw = new StringWriter();
            export(sw);
            return sw.toString();
        } catch (IOException e) {
           throw new AssertionError(e);
        }
    }
    
    /**
     * Generates a configuration profile from the current set parameter values and writes it to the provided
     * {@link Writer} object.
     *
     * <p>This method uses the built-in {@link ValveConfigWriter} class to generate and write the configuration file
     * contents. New lines will be separated by the value returned by {@link System#lineSeparator()}.</p>
     *
     * @param writer the writer instance to write the configuration data to
     * @throws IOException if an I/O error occurs when writing to the writer
     * @throws IllegalStateException if one or more configuration values aren't set or are invalid
     *
     * @see #writeFile(String)
     */
    public void export(Writer writer) throws IOException {
        validateState();
        
        ValveConfigWriter conf = new ValveConfigWriter(writer);
        conf.key(description != null ? description : DEFAULT_DESC).beginObject();
    
        // Values
        conf.key("uri").value(getURL())
                .key("timeout").value(getTimeoutPeriod().map(DOUBLE_FORMAT::format).orElse(null))
                .key("buffer").value(getBufferPeriod().map(DOUBLE_FORMAT::format).orElse(null))
                .key("throttle").value(getThrottlePeriod().map(DOUBLE_FORMAT::format).orElse(null))
                .key("heartbeat").value(getHeartbeatPeriod().map(DOUBLE_FORMAT::format).orElse(null));
    
        // Output precision
        conf.key("output").beginObject()
                .key("precision_time").value(getPrecisionTime().orElse(null))
                .key("precision_position").value(getPrecisionPosition().orElse(null))
                .key("precision_vector").value(getPrecisionVector().orElse(null))
                .endObject();
    
        // Auth tokens
        conf.key("auth").beginObject();
        for (Map.Entry<String, String> token : authTokens.entrySet()) {
            conf.key(token.getKey()).value(token.getValue());
        }
        conf.endObject();
    
        // Data components
        conf.key("data").beginObject();
        for (DataComponent type : DataComponent.values()) {
            conf.key(type.getConfigName()).value(dataComponents.contains(type) ? "1" : "0");
        }
        conf.endObject().endObject().close();
    }
    
    
    /**
     * Creates or replaces an existing configuration file within the located game directory.
     *
     * <p>This method automatically locates the game directory using the {@link CsgoUtils#locateConfigDirectory()}
     * utility method. If neither the Steam or game directory can be identified, then a {@link GameNotFoundException}
     * will be raised.</p>
     *
     * <p>Your applications service name should be unique, and can only contain standard english word and digit
     * characters, as well as underscores, and must be between 3 and 32 characters in length. The name must
     * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
     *
     * <pre>
     *  GSIConfig config = ... // Create config here
     *
     *  try {
     *      config.writeFile("my_service");
     *      System.out.println("config successfully created!");
     *  } catch (GameNotFoundException e) {
     *      System.out.println("Couldn't locate CSGO or Steam installation directories.");
     *  } catch (IOException e) {
     *      System.out.println("Couldn't write configuration file");
     *  }
     * </pre>
     *
     * @param serviceName the identifying name of your application or service, eg: {@code test_service}
     * @return the configuration file path which was written
     *
     * @throws IOException           if the file cannot be written to
     * @throws GameNotFoundException if the Steam or CSGO installation could not be located
     * @throws SecurityException     if the security manager doesn't permit access to the file
     * @throws IllegalStateException if one or more configuration values aren't set or are invalid
     */
    public Path writeFile(String serviceName) throws GameNotFoundException, IOException {
        Path file = getFile(serviceName);
        writeFile(file);
        return file;
    }
    
    /**
     * Creates or replaces an existing configuration file within the specified game directory.
     *
     * <p>Your applications service name should be unique, and can only contain standard english word and digit
     * characters, as well as underscores, and must be between 3 and 32 characters in length. The name must
     * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
     *
     * <p>The {@code dir} parameter can be passed the value returned from {@link CsgoUtils#locateConfigDirectory()},
     * which will automatically locate this folder on the current system for you. Be aware that the utility method can
     * return null if no CS:GO directory is found, and throw a {@link GameNotFoundException} if no valid Steam
     * installation can be found on the system. The following example demonstrates how to create and write a
     * configuration file to the system:</p>
     * <pre>
     *  GSIConfig config = ... // Create config here
     *
     *  try {
     *      Path configPath = CsgoUtils.locateConfigDirectory();
     *
     *      config.writeFile("my_service", configPath);
     *      System.out.println("Config successfully created!");
     *  } catch (GameNotFoundException e) {
     *      System.out.println("Couldn't locate CSGO or Steam installation directories.");
     *  } catch (IOException e) {
     *      System.out.println("Couldn't write configuration file");
     *  }
     * </pre>
     *
     * @param serviceName the identifying name of your application or service, eg: {@code test_service}
     * @param dir         the directory in which the file is created
     *
     * @throws IOException           if the file cannot be written to
     * @throws NotDirectoryException if the given path argument is not a directory
     * @throws SecurityException     if the security manager doesn't permit access to the file
     * @throws IllegalStateException if one or more configuration values aren't set or are invalid
     *
     * @see CsgoUtils#locateConfigDirectory()
     * @see #writeFile(String)
     */
    public void writeFile(String serviceName, Path dir) throws IOException {
        if (!Files.isDirectory(dir))
            throw new NotDirectoryException("Path must be a directory.");
        writeFile(getFile(dir, serviceName));
    }
    
    /**
     * Creates or replaces an existing configuration file.
     *
     * @param file the configuration file to write to
     *
     * @throws IOException       if the file cannot be written to
     * @throws SecurityException if the security manager doesn't permit access to the file
     * @throws IllegalStateException if one or more configuration values aren't set or are invalid
     *
     * @see #writeFile(String)
     */
    public void writeFile(Path file) throws IOException {
        validateState();
        if (Files.isDirectory(file))
            throw new IllegalArgumentException("Path was an existing directory, and not a file.");
        
        log.debug("Attempting to write config file {}...", file);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            export(writer);
        }
        log.info("Written game state config file {}", file);
    }
    
    
    /**
     * Removes a configuration file in the located game directory, if it exists.
     *
     * <p>This method automatically locates the game directory using the {@link CsgoUtils#locateConfigDirectory()} ()}
     * utility method. If neither the Steam or game directory can be identified, then a {@link GameNotFoundException}
     * will be raised.</p>
     *
     * @param serviceName the identifying name of your application or service, eg: {@code test_service}
     *
     * @return true if the file was successfully removed, false if it didn't exist
     *
     * @throws GameNotFoundException if the Steam or CSGO installation could not be located
     * @throws IOException           if the file could not be removed
     * @throws SecurityException     if the security manager disallows access to the file
     * @throws FileNotFoundException if the given path argument is not an existing directory
     */
    public static boolean removeFile(String serviceName) throws GameNotFoundException, IOException {
        return removeFile(CsgoUtils.locateConfigDirectory(), serviceName);
    }
    
    /**
     * Removes a configuration file in the provided directory, if it exists.
     *
     * <p>The directory parameter can be passed the result from the {@link CsgoUtils#locateConfigDirectory()} method,
     * which will attempt to automatically locate the directory for you.</p>
     *
     * @param dir         the directory which the configuration file resides in
     * @param serviceName the identifying name of your application or service, eg: {@code test_service}
     *
     * @return true if the file was successfully removed, false if it didn't exist
     *
     * @throws IOException           if the file could not be removed
     * @throws SecurityException     if the security manager disallows access to the file
     * @throws FileNotFoundException if the given path argument is not an existing directory
     * @throws NotDirectoryException if the given path argument is not a directory
     *
     * @see CsgoUtils#locateConfigDirectory()
     * @see #removeFile(String)
     */
    public static boolean removeFile(Path dir, String serviceName) throws IOException {
        if (!Files.exists(dir))
            throw new FileNotFoundException("Path argument is not an existing directory.");
        if (!Files.isDirectory(dir))
            throw new NotDirectoryException("Path must be a directory.");
        
        Path file = getFile(dir, serviceName);
        log.debug("Attempting to remove config file {}...", file);
        return Files.deleteIfExists(file);
    }
    
    
    /**
     * Checks whether a configuration file currently exists with the specified service name.
     *
     * <p>This method automatically locates the game directory using the {@link CsgoUtils#locateConfigDirectory()}
     * utility method. If neither the Steam or game directory can be identified, then a {@link GameNotFoundException}
     * will be raised.</p>
     *
     * @param serviceName the identifying name of your application or service, eg: {@code test_service}
     *
     * @return true if a configuration file already exists
     *
     * @throws GameNotFoundException if the Steam or CSGO installation could not be located
     * @throws SecurityException     if the security manager disallows access to the file
     */
    public static boolean fileExists(String serviceName) throws GameNotFoundException {
        return Files.isRegularFile(getFile(serviceName));
    }
    
    /**
     * Checks whether a configuration file currently exists with the specified service name in the given directory.
     *
     * @param dir         the directory containing the potential configuration file
     * @param serviceName the identifying name of your application or service, eg: {@code test_service}
     *
     * @return true if a configuration file already exists
     *
     * @throws SecurityException     if the security manager disallows access to the file
     *
     * @see CsgoUtils#locateConfigDirectory()
     * @see #fileExists(String)
     */
    public static boolean fileExists(Path dir, String serviceName) {
        return Files.isRegularFile(getFile(dir, serviceName));
    }
    
    
    /**
     * Returns the path of the configuration file with the given service name. This method will not create any files,
     * nor will it perform any checks as to whether the configuration file actually exists on the system.
     *
     * <p>This method automatically locates the game directory using the {@link CsgoUtils#locateConfigDirectory()}
     * utility method. If neither the Steam or game directory can be identified, then a {@link GameNotFoundException}
     * will be raised.</p>
     *
     * <p>Your applications service name should be unique, and can only contain standard english word and digit
     * characters, as well as underscores, and must be between 3 and 32 characters in length. The name must
     * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
     *
     * @param serviceName the identifying name of your application or service, eg: {@code test_service}
     *
     * @return the configuration file for the given service
     *
     * @throws GameNotFoundException if the Steam or CSGO installation could not be located
     */
    public static Path getFile(String serviceName) throws GameNotFoundException {
        return getFile(CsgoUtils.locateConfigDirectory(), serviceName);
    }
    
    /**
     * Returns the path of the configuration file with the given service name. This method will not create any files,
     * nor will it perform any checks as to whether the configuration file actually exists on the system.
     *
     * <p>Your applications service name should be unique, and can only contain standard english word and digit
     * characters, as well as underscores, and must be between 3 and 32 characters in length. The name must
     * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
     *
     * @param dir         the directory containing the configuration file
     * @param serviceName the identifying name of your application or service, eg: {@code test_service}
     *
     * @return the configuration file for the given service
     *
     * @see #getFile(String)
     */
    public static Path getFile(Path dir, String serviceName) {
        if (!SERVICE_NAME_PATTERN.matcher(serviceName).matches())
            throw new IllegalArgumentException("Invalid service name.");
        return dir.resolve("gamestate_integration_" + serviceName.toLowerCase() + ".cfg").toAbsolutePath();
    }
    
}
