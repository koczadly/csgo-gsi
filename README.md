# CSGO-GSI
## About
A Java library for retrieving real-time game information and statistics from Counter-Strike: Global Offensive using
its built-in *game state integration* service.

This project is in active development, and will receive updates as changes are made to the underlying game state
 schema supplied by the game.

## Features
This library provides simple access to 3 main features:
- Automated location of the Steam and game directories (even on externally configured drives)
- The creation and deletion of game state service configurations
- A server which listens for updates and parses the game state data

## Usage
### Maven
This project is hosted on Maven Central. To import this library, add the following dependency into your pom.xml:
```xml
<dependency>
    <groupId>uk.oczadly.karl</groupId>
    <artifactId>csgo-gsi</artifactId>
    <version>1.3.3</version>
</dependency>
```

### Documentation
The latest Javadoc pages can be [viewed online through Javadoc.io](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi).

### Configuration generation
To create a configuration file, use the GSIConfig class. The SteamUtils class provides a range of static methods
which can be used to automatically locate the CSGO game directory. The example below demonstrates how to use
these utilities:

```java
// Build the configuration for our service
GSIConfig config = new GSIConfig(1337)
        .setDescription("Test service for CSGO-GSI") // Not necessary, but adds a useful comment to the file
        .setTimeoutPeriod(1.0)
        .setBufferPeriod(0.5)
        .setAuthToken("password", "Q79v5tcxVQ8u")
        .setDataComponents(
                DataComponent.PROVIDER,
                DataComponent.MAP); // Alternatively, you can call setAllDataComponents()

try {
    // Locate the CSGO configuration folder
    Path configPath = SteamUtils.locateCsgoConfigFolder();
    
    if (configPath != null) {
        // Create the service config file
        GSIConfig.createConfig(configPath, config, "my_service_name");
        System.out.println("Config successfully created!");
    } else {
        System.out.println("Couldn't locate CS:GO directory");
    }
} catch (SteamDirectoryException e) {
    System.out.println("Couldn't locate Steam installation directory");
} catch (IOException e) {
    System.out.println("Couldn't write configuration file");
}
```

### Listening for state information
To listen for new game state information, a GSIServer object must be created and an instance of a class
implementing GSIObserver must be registered to the server object. The example below demonstrates a basic
listener which prints the client's Steam ID and current map name (if in a game) to the console.
```java
// Create a new observer (for this example, using a lambda)
GSIObserver observer = (state, context) -> {
    // Access state information with the 'state' object...
    System.out.println("New state received from game client at address " + context.getAddress().getHostAddress());
    
    System.out.println("  Client SteamID: " + state.getProviderDetails().getClientSteamId());
    if (state.getMapState() != null) {
        System.out.println("  Current map: " + state.getMapState().getName());
    }
};

// Configure server on port 1337, requiring the specified "password" auth token
GSIServer server = new GSIServer(1337, Map.of("password", "Q79v5tcxVQ8u"));
server.registerObserver(observer); // Register our observer object
server.startServer(); // Start the server (runs in a separate thread)

System.out.println("Server started. Listening for state data...");
```

## Development
If you experience an issue or feel the library is missing functionality, submit an issue (or a pull request if you've
already resolved the problem).