# CSGO-GSI
## About
A Java library for retrieving real-time game information and statistics from *Counter-Strike: Global Offensive* using
the built-in game state integration service.

This project is in active development, and will receive updates as changes are made to the game and it's state schema.

## Features
This library provides simple access to 3 main features:
- Automated location of the game directory
- The creation and deletion of game state service configurations
- A server which listens for updates and parses the game state data

## Usage
### Maven
This project is hosted on Maven Central. To import this library, add the following dependency into your pom.xml:
```xml
<dependency>
    <groupId>uk.oczadly.karl</groupId>
    <artifactId>csgo-gsi</artifactId>
    <version>1.4.2</version>
</dependency>
```

### Documentation
The latest Javadoc pages can be [viewed online through Javadoc.io](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi).

### Configuration generation
To create a configuration file, use the [`GSIConfig`](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi/latest/uk/oczadly/karl/csgsi/config/GSIConfig.html)
 class. The example below demonstrates how to use this utility:

```java
// Build the configuration for our service
GSIConfig config = new GSIConfig(1337)
        .setTimeoutPeriod(1.0)
        .setBufferPeriod(0.5)
        .setAuthToken("password", "Q79v5tcxVQ8u")
        .setAllDataComponents(); // You could also choose which using setDataComponents(...)

try {
    config.writeConfigFile("test_service");
    System.out.println("Config successfully created!");
} catch (GameNotFoundException e) {
    // Either CSGO or Steam installation directory could not be located
    System.out.println("Couldn't locate CSGO installation: " + e.getMessage());
} catch (IOException e) {
    System.out.println("Couldn't write configuration file.");
}
```

### Listening for state information
To listen for new game state information, a [`GSIServer`](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi/latest/uk/oczadly/karl/csgsi/GSIServer.html)
object must be created, and an instance which implements [`GSIObserver`](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi/latest/uk/oczadly/karl/csgsi/GSIObserver.html)
must be registered to the server object. The example below demonstrates a basic listener which prints the client's
 Steam ID and current map name (if in a game) to the console.

*Note that you **must** check that state objects are not null before accessing their member methods.*
```java
// Create a new observer (for this example, using a lambda)
GSIObserver observer = (state, context) -> {
    // Access state information with the 'state' object...
    System.out.println("New state from game client address " + context.getAddress().getHostAddress());
    
    if (state.getProviderDetails() != null) {
        System.out.println("Client SteamID: " + state.getProviderDetails().getClientSteamId());
    }
    if (state.getMapState() != null) {
        System.out.println("Current map: " + state.getMapState().getName());
    }
};

// Configure server
GSIServer server = new GSIServer.Builder(1337)        // Port 1337, on all network interfaces
        .requireAuthToken("password", "Q79v5tcxVQ8u") // Require the specified password
        .registerObserver(observer)                   // Alternatively, you can call this on the GSIServer dynamically
        .build();

// Start server
try {
    server.start(); // Start the server (runs in a separate thread)
    System.out.println("Server started. Listening for state data...");
} catch (IOException e) {
    System.out.println("Could not start server.");
}
```

## Development
If you experience an issue or think the library is missing some functionality, please submit an issue or pull request.