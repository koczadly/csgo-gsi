package uk.oczadly.karl.csgsi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>This class represents a series of parameters used in the game state profile configuration, and provides static
 * utilities for creating and deleting these profiles.</p>
 *
 * <p>The class follows a similar design to the builder pattern, whereby each optional parameter can be set by calling
 * the appropriate setter methods (which return the current instance). Mandatory parameters (callback URI) are passed
 * within the class constructor.</p>
 *
 * <p>The following example demonstrates how to configure a profile with a local receiver on port 80 with a timeout of
 * 1 second, a buffer of 500ms, an authentication value "password", and receives all supported data components:
 * <pre>
 *  GSIConfig profile = new GSIConfig("http://127.0.0.1:80")
 *          .setTimeoutPeriod(1.0)
 *          .setBufferPeriod(0.5)
 *          .setAuthToken("password", "Q79v5tcxVQ8u")
 *          .setDataComponents(
 *                 DataComponent.PROVIDER,
 *                 DataComponent.ROUND);
 * </pre>
 *
 * <p>Profiles can then be created and written to the system using the {@link #writeFile(String)} method (refer to
 * method documentation).</p>
 *
 * <p>Your applications service name should be unique, and can only contain standard english word and digit
 * characters, as well as underscores, and must be between 1 and 32 characters in length. The name must
 * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
 */
public class GSIConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GSIConfig.class);
    
    private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("^\\w{1,32}$");
    private static final String DEFAULT_DESC = "Created using https://github.com/koczadly/csgo-gsi";
    
    
    private String uri, description = DEFAULT_DESC;
    private Map<String, String> authData = new HashMap<>();
    private Double timeout, buffer, throttle, heartbeat;
    private Integer precisionTime, precisionPosition, precisionVector;
    private Set<DataComponent> dataComponents = EnumSet.noneOf(DataComponent.class);
    
    
    /**
     * Constructs a new GSI configuration object with the URI as localhost on the specified port. Refer to class
     * documentation and setter methods for configuring other properties.
     *
     * @param port the port of the server
     */
    public GSIConfig(int port) {
        setURI(port);
    }
    
    /**
     * Constructs a new GSI configuration object with the URI as localhost on the specified port. Refer to class
     * documentation and setter methods for configuring other properties.
     *
     * @param host the hostname or address of the server (eg. "{@code 127.0.0.1}")
     * @param port the port of the server
     */
    public GSIConfig(String host, int port) {
        setURI(host, port);
    }
    
    /**
     * Constructs a new GSI configuration object with the specified URI. Refer to class documentation and setter methods
     * for configuring other properties.
     *
     * @param uri the URI of the server, including port and protocol
     * @throws NullPointerException if the provided {@code uri} argument is null
     */
    public GSIConfig(String uri) {
        setURI(uri);
    }
    
    /**
     * Constructs a new GSI configuration object. Refer to class documentation and setter methods for configuring
     * properties.
     */
    public GSIConfig() {}
    
    
    /**
     * Sets the URI of the server to the local machine ({@code 127.0.0.1}) on the specified port, using the {@code
     * HTTP} URI.
     *
     * @param port the port of the server
     * @return this current object
     *
     * @throws NullPointerException if the provided {@code uri} argument is null
     */
    public GSIConfig setURI(int port) {
        return setURI("127.0.0.1", port);
    }
    
    /**
     * Sets the URI of the server to the specified host and port, using the {@code HTTP} URI.
     *
     * @param host the hostname or address of the server (eg. "{@code 127.0.0.1}")
     * @param port the port of the server
     * @return this current object
     *
     * @throws NullPointerException if the provided {@code uri} argument is null
     */
    public GSIConfig setURI(String host, int port) {
        if (port < 1 || port > 65535)
            throw new NullPointerException("Port value is outside the allowable range.");
        return setURI("http://" + host + ":" + port);
    }
    
    /**
     * @param uri the URI (including protocol and port) of the server
     * @return this current object
     *
     * @throws NullPointerException if the provided {@code uri} argument is null
     */
    public GSIConfig setURI(String uri) {
        if (uri == null)
            throw new NullPointerException("URI argument cannot be null.");
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
     * Sets the auth token to a specified map of {@link String} elements. The map will be copied when calling this
     * method, so future changes to the provided map won't change the state of this object. If a null value is passed,
     * the method will generate an empty {@link Map} instance.
     *
     * @param authData the new map of auth tokens, or null
     * @return this current object
     */
    public GSIConfig setAuthTokens(Map<String, String> authData) {
        this.authData = authData == null
                ? new HashMap<>() //Ensure stored value isn't null
                : new HashMap<>(authData); //Clone map
        return this;
    }
    
    /**
     * Adds the authentication token to the current map of values.
     *
     * @param key   the key of the authentication token
     * @param token the associated value, or null to remove the key
     * @return this current object
     *
     * @throws NullPointerException if the key value is null
     */
    public GSIConfig setAuthToken(String key, String token) {
        if (key == null)
            throw new NullPointerException("Auth token key cannot be null");
        
        if (token == null) {
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
     * Sets the description of the configuration. Has no effect on the profile other than being included within the file
     * for the user's reference.
     *
     * @param desc the service description
     * @return this current object
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
     * @return this current object
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve
     * Developer Community</a>
     */
    public GSIConfig setTimeoutPeriod(Double timeout) {
        this.timeout = timeout;
        return this;
    }
    
    /**
     * @return the timeout period in seconds, or null if not set (default)
     *
     * @see #setTimeoutPeriod(Double)
     */
    public Double getTimeoutPeriod() {
        return timeout;
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
     * @return this current object
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve
     * Developer Community</a>
     */
    public GSIConfig setBufferPeriod(Double buffer) {
        this.buffer = buffer;
        return this;
    }
    
    /**
     * @return the buffer period in seconds, or null if not set (default)
     *
     * @see #setBufferPeriod(Double)
     */
    public Double getBufferPeriod() {
        return buffer;
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
     * @return this current object
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve
     * Developer Community</a>
     */
    public GSIConfig setThrottlePeriod(Double throttle) {
        this.throttle = throttle;
        return this;
    }
    
    /**
     * @return the timeout period in seconds, or null if not set (default)
     *
     * @see #setThrottlePeriod(Double)
     */
    public Double getThrottlePeriod() {
        return throttle;
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
     * @return this current object
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">Valve
     * Developer Community</a>
     */
    public GSIConfig setHeartbeatPeriod(Double heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }
    
    /**
     * @return the heartbeat period in seconds, or null if not set (default)
     *
     * @see #setHeartbeatPeriod(Double)
     */
    public Double getHeartbeatPeriod() {
        return heartbeat;
    }
    

    /**
     * @return the number of decimal places for time units, or null for default
     * @see #setPrecisionTime(Integer)
     */
    public Integer getPrecisionTime() {
        return precisionTime;
    }
    
    /**
     * Sets the number of decimal places to be sent for values containing a measurement of time. Use a value of null
     * if you wish to use the default client value.
     *
     * @param precisionTime the number of decimal places for time units, or null for default
     * @return this current object
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">
     * Valve Developer Community</a>
     */
    public GSIConfig setPrecisionTime(Integer precisionTime) {
        this.precisionTime = precisionTime;
        return this;
    }
    
    /**
     * @return the number of decimal places for positional units, or null for default
     * @see #setPrecisionPosition(Integer)
     */
    public Integer getPrecisionPosition() {
        return precisionPosition;
    }
    
    /**
     * Sets the number of decimal places to be sent for values containing a units of position (coordinates). Use a value
     * of null if you wish to use the default client value.
     *
     * @param precisionPosition the number of decimal places for position units, or null for default
     * @return this current object
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">
     * Valve Developer Community</a>
     */
    public GSIConfig setPrecisionPosition(Integer precisionPosition) {
        this.precisionPosition = precisionPosition;
        return this;
    }
    
    /**
     * @return the number of decimal places for vector units, or null for default
     * @see #setPrecisionVector(Integer)
     */
    public Integer getPrecisionVector() {
        return precisionVector;
    }
    
    /**
     * Sets the number of decimal places to be sent for values containing a vector. Use a value of null if you wish to
     * use the default client value.
     *
     * @param precisionVector the number of decimal places for vector units, or null for default
     * @return this current object
     *
     * @see <a href="https://developer.valvesoftware.com/wiki/Counter-Strike:_Global_Offensive_Game_State_Integration">
     * Valve Developer Community</a>
     */
    public GSIConfig setPrecisionVector(Integer precisionVector) {
        this.precisionVector = precisionVector;
        return this;
    }
    
    /**
     * Sets the which data values will be sent by the client. The set will be copied when calling this method, so future
     * changes to the provided set won't change the state of this object. If a null value is passed, the method will
     * generate an empty {@link EnumSet} instance.
     *
     * @param reportedData the new set of data components to be sent, or null
     * @return this current object
     */
    public GSIConfig setDataComponents(Set<DataComponent> reportedData) {
        this.dataComponents = reportedData != null
                ? EnumSet.copyOf(reportedData)
                : EnumSet.noneOf(DataComponent.class);
        return this;
    }
    
    /**
     * Sets the which data values will be sent by the client. If a null value is passed, the method will generate an
     * empty {@link EnumSet} instance.
     *
     * @param reportedData the new set of data components to be sent, or null
     * @return this current object
     */
    public GSIConfig setDataComponents(DataComponent... reportedData) {
        Set<DataComponent> set = EnumSet.noneOf(DataComponent.class);
        if (reportedData != null)
            Collections.addAll(set, reportedData);
        this.dataComponents = set;
        return this;
    }
    
    /**
     * Sets the configuration so that all the data components will be sent by the client.
     *
     * @return this current object
     */
    public GSIConfig setAllDataComponents() {
        this.dataComponents = EnumSet.allOf(DataComponent.class);
        return this;
    }
    
    /**
     * Adds the specified data component to the current list, which will be sent by the client.
     *
     * @param reportedData the data component
     * @return this current object
     */
    public GSIConfig addDataComponent(DataComponent reportedData) {
        this.dataComponents.add(reportedData);
        return this;
    }
    
    /**
     * @return an immutable set of data components to be sent by the client
     */
    public Set<DataComponent> getDataComponents() {
        return Collections.unmodifiableSet(dataComponents);
    }
    
    
    /**
     * Generates a configuration profile from the current set parameter values and returns it as a String value.
     *
     * <p>This method uses the built-in {@link ValveConfigWriter} class to generate and write the configuration file
     * contents. New lines will be separated by the value returned by {@link System#lineSeparator()}.</p>
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
     *
     * @see #writeFile(String)
     */
    public void export(Writer writer) throws IOException {
        ValveConfigWriter conf = new ValveConfigWriter(writer);
        conf.key(this.getDescription()).beginObject();
    
        // Values
        conf.key("uri").value(getURI())
                .key("timeout").value(getTimeoutPeriod())
                .key("buffer").value(getBufferPeriod())
                .key("throttle").value(getThrottlePeriod())
                .key("heartbeat").value(getHeartbeatPeriod());
    
        // Output precision
        conf.key("output").beginObject()
                .key("precision_time").value(getPrecisionTime())
                .key("precision_position").value(getPrecisionPosition())
                .key("precision_vector").value(getPrecisionVector())
                .endObject();
    
        // Auth tokens
        conf.key("auth").beginObject();
        for (Map.Entry<String, String> token : getAuthTokens().entrySet()) {
            conf.key(token.getKey()).value(token.getValue());
        }
        conf.endObject();
    
        // Data components
        conf.key("data").beginObject();
        for (DataComponent type : DataComponent.values()) {
            conf.key(type.getConfigName())
                    .value(getDataComponents().contains(type) ? "1" : "0");
        }
        conf.endObject().endObject().close();
    }
    
    
    /**
     * Creates or replaces an existing configuration file within the located game directory.
     *
     * <p>This method automatically locates the game directory using the {@link SteamUtils#locateCsgoConfigFolder()}
     * utility method. If neither the Steam or game directory can be identified, then a {@link GameNotFoundException}
     * will be raised.</p>
     *
     * <p>Your applications service name should be unique, and can only contain standard english word and digit
     * characters, as well as underscores, and must be between 1 and 32 characters in length. The name must
     * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
     *
     * <pre>
     *  GSIConfig profile = ... // Create profile here
     *
     *  try {
     *      profile.writeConfigFile("my_service");
     *      System.out.println("Profile successfully created!");
     *  } catch (GameNotFoundException e) {
     *      System.out.println("Couldn't locate CSGO or Steam installation directories.");
     *  } catch (IOException e) {
     *      System.out.println("Couldn't write configuration file");
     *  }
     * </pre>
     *
     * @param serviceName the identifying name of your application/service (eg: {@code test_service})
     *
     * @throws IOException           if the file cannot be written to
     * @throws GameNotFoundException if the Steam or CSGO installation could not be located
     * @throws SecurityException     if the security manager doesn't permit access to the file
     */
    public void writeFile(String serviceName) throws GameNotFoundException, IOException {
        writeFile(getFile(serviceName));
    }
    
    /**
     * Creates or replaces an existing configuration file within the specified game directory.
     *
     * <p>Your applications service name should be unique, and can only contain standard english word and digit
     * characters, as well as underscores, and must be between 1 and 32 characters in length. The name must
     * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
     *
     * <p>The {@code dir} parameter can be passed the value returned from {@link SteamUtils#locateCsgoConfigFolder()},
     * which will automatically locate this folder on the current system for you. Be aware that the utility method can
     * return null if no CS:GO directory is found, and throw a {@link GameNotFoundException} if no valid Steam
     * installation can be found on the system. The following example demonstrates how to create and write a
     * configuration file to the system:</p>
     * <pre>
     *  GSIConfig profile = ... // Create profile here
     *
     *  try {
     *      Path configPath = SteamUtils.locateCsgoConfigFolder();
     *
     *      profile.writeConfigFile("my_service", configPath);
     *      System.out.println("Profile successfully created!");
     *  } catch (GameNotFoundException e) {
     *      System.out.println("Couldn't locate CSGO or Steam installation directories.");
     *  } catch (IOException e) {
     *      System.out.println("Couldn't write configuration file");
     *  }
     * </pre>
     *
     * @param serviceName the identifying name of your application/service (eg: {@code test_service})
     * @param dir         the directory in which the file is created
     *
     * @throws IOException           if the file cannot be written to
     * @throws NotDirectoryException if the given path argument is not a directory
     * @throws SecurityException     if the security manager doesn't permit access to the file
     *
     * @see SteamUtils#locateCsgoConfigFolder()
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
     *
     * @see #writeFile(String)
     */
    public void writeFile(Path file) throws IOException {
        if (Files.isDirectory(file))
            throw new IllegalArgumentException("Path was an existing directory, and not a file.");
        
        LOGGER.debug("Attempting to create config file {}...", file.toString());
        if (dataComponents.isEmpty())
            LOGGER.warn("No data components are enabled in the profile! The client won't return any information.");
        
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            export(writer);
        }
        LOGGER.info("Written game state config file {}", file.toString());
    }
    
    
    /**
     * Removes a configuration file in the located game directory, if it exists.
     *
     * <p>This method automatically locates the game directory using the {@link SteamUtils#locateCsgoConfigFolder()}
     * utility method. If neither the Steam or game directory can be identified, then a {@link GameNotFoundException}
     * will be raised.</p>
     *
     * @param serviceName the identifying name of your application/service (eg: {@code test_service})
     *
     * @return true if the file was successfully removed, false if it didn't exist
     *
     * @throws GameNotFoundException if the Steam or CSGO installation could not be located
     * @throws IOException           if the file could not be removed
     * @throws SecurityException     if the security manager disallows access to the file
     * @throws FileNotFoundException if the given path argument is not an existing directory
     */
    public static boolean removeFile(String serviceName) throws GameNotFoundException, IOException {
        return removeFile(SteamUtils.locateCsgoConfigFolder(), serviceName);
    }
    
    /**
     * Removes a configuration file in the provided directory, if it exists.
     *
     * <p>The directory parameter can be passed the result from the {@link SteamUtils#locateCsgoConfigFolder()} method,
     * which will attempt to automatically locate the directory for you.</p>
     *
     * @param dir         the directory which the configuration file resides in
     * @param serviceName the identifying name of your application/service (eg: {@code test_service})
     *
     * @return true if the file was successfully removed, false if it didn't exist
     *
     * @throws IOException           if the file could not be removed
     * @throws SecurityException     if the security manager disallows access to the file
     * @throws FileNotFoundException if the given path argument is not an existing directory
     * @throws NotDirectoryException if the given path argument is not a directory
     *
     * @see SteamUtils#locateCsgoConfigFolder()
     * @see #removeFile(String)
     */
    public static boolean removeFile(Path dir, String serviceName) throws IOException {
        if (!Files.exists(dir))
            throw new FileNotFoundException("Path argument is not an existing directory.");
        if (!Files.isDirectory(dir))
            throw new NotDirectoryException("Path must be a directory.");
        
        Path file = getFile(dir, serviceName);
        LOGGER.debug("Attempting to remove config file {}...", file.toString());
        return Files.deleteIfExists(file);
    }
    
    
    /**
     * Checks whether a configuration file currently exists with the specified service name.
     *
     * <p>This method automatically locates the game directory using the {@link SteamUtils#locateCsgoConfigFolder()}
     * utility method. If neither the Steam or game directory can be identified, then a {@link GameNotFoundException}
     * will be raised.</p>
     *
     * @param serviceName the identifying name of your application/service (eg: {@code test_service})
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
     * @param serviceName the identifying name of your application/service (eg: {@code test_service})
     *
     * @return true if a configuration file already exists
     *
     * @throws SecurityException     if the security manager disallows access to the file
     *
     * @see SteamUtils#locateCsgoConfigFolder()
     * @see #fileExists(String)
     */
    public static boolean fileExists(Path dir, String serviceName) {
        return Files.isRegularFile(getFile(dir, serviceName));
    }
    
    
    /**
     * Returns the path of the configuration file with the given service name. This method will not create any files,
     * nor will it perform any checks as to whether the configuration file actually exists on the system.
     *
     * <p>This method automatically locates the game directory using the {@link SteamUtils#locateCsgoConfigFolder()}
     * utility method. If neither the Steam or game directory can be identified, then a {@link GameNotFoundException}
     * will be raised.</p>
     *
     * <p>Your applications service name should be unique, and can only contain standard english word and digit
     * characters, as well as underscores, and must be between 1 and 32 characters in length. The name must
     * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
     *
     * @param serviceName the identifying name of your application/service (eg: {@code test_service})
     *
     * @return the configuration file for the given service
     *
     * @throws GameNotFoundException if the Steam or CSGO installation could not be located
     */
    public static Path getFile(String serviceName) throws GameNotFoundException {
        return getFile(SteamUtils.locateCsgoConfigFolder(), serviceName);
    }
    
    /**
     * Returns the path of the configuration file with the given service name. This method will not create any files,
     * nor will it perform any checks as to whether the configuration file actually exists on the system.
     *
     * <p>Your applications service name should be unique, and can only contain standard english word and digit
     * characters, as well as underscores, and must be between 1 and 32 characters in length. The name must
     * <em>not</em> include the {@code gamestate_integration_} prefix or the {@code .cfg} file extension suffix.</p>
     *
     * @param dir         the directory containing the configuration file
     * @param serviceName the identifying name of your application/service (eg: {@code test_service})
     *
     * @return the configuration file for the given service
     *
     * @see #getFile(String)
     */
    public static Path getFile(Path dir, String serviceName) {
        if (!SERVICE_NAME_PATTERN.matcher(serviceName).matches())
            throw new IllegalArgumentException("Invalid service name.");
        return dir.resolve("gamestate_integration_" + serviceName.toLowerCase() + ".cfg");
    }
    
}
