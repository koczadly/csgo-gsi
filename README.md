# CSGO-GSI
## About
A Java library for retrieving real-time game information and statistics from *Counter-Strike: Global Offensive* using
the built-in game state integration service.

## Features
This library provides access to 3 main features:
- Automated location of the game's installation directory
- The creation and deletion of game state service configurations
- A server which listens for updates and parses the game state data

## Usage
### Maven
This project is hosted on Maven Central. To import this library, add the following dependency into your pom.xml:
```xml
<dependency>
    <groupId>uk.oczadly.karl</groupId>
    <artifactId>csgo-gsi</artifactId>
    <version>1.6.0</version>
</dependency>
```

### Documentation
The latest Javadoc pages can be [viewed online through Javadoc.io](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi).

### Configuration generation
Creating the game state configuration file for the game client is an easy and automated process. This can be 
accomplished using the provided [`GSIConfig`](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi/latest/uk/oczadly/karl/csgsi/config/GSIConfig.html)
 class. The example below demonstrates how to use this utility:

```java
// Build the configuration for our service
GSIConfig config = new GSIConfig()
        .setLocalURI(1337)  // Server on localhost:1337
        .setTimeoutPeriod(1.0)
        .setBufferPeriod(0.5)
        .includeAuthToken("password", "Q79v5tcxVQ8u")
        .subscribeAll();  // or specify which components using subscribe(...)

try {
    // Automatically locates the game directory and creates the configuration file
    Path output = config.writeFile("test_service");
    System.out.println("Written config file: " + output);
} catch (GameNotFoundException e) {
    // Either CSGO or Steam installation directory could not be located
    System.err.println("Couldn't locate CSGO installation: " + e.getMessage());
} catch (IOException e) {
    System.err.println("Couldn't write to configuration file.");
}
```

<details>
<summary>Click to view data written to the file <code>gamestate_integration_test_service.cfg</code></summary>

```text
"Sample test service" {
    "uri"     "http://localhost:1337"
    "timeout" "1.0"
    "buffer"  "0.5"
    "auth" {
        "password" "Q79v5tcxVQ8u"
    }
    "data" {
        "map_round_wins"         "1"
        "map"                    "1"
        "player_id"              "1"
        "player_match_stats"     "1"
        "player_state"           "1"
        "player_weapons"         "1"
        "player_position"        "1"
        "provider"               "1"
        "round"                  "1"
        "allgrenades"            "1"
        "allplayers_id"          "1"
        "allplayers_match_stats" "1"
        "allplayers_position"    "1"
        "allplayers_state"       "1"
        "allplayers_weapons"     "1"
        "bomb"                   "1"
        "phase_countdowns"       "1"
    }
}
```
</details>

### Listening for state information
To listen for new game state information, a [`GSIServer`](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi/latest/uk/oczadly/karl/csgsi/GSIServer.html)
object must be created, and an instance which implements [`GSIListener`](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi/latest/uk/oczadly/karl/csgsi/GSIListener.html)
must be registered to the server object. The example below demonstrates a basic listener which prints the client's
 Steam ID and current map name (if in a game) to the console.

```java
// Create a new listener (using a lambda for this example)
GSIListener listener = (state, context) -> {
    // Access state information with the 'state' object...
    System.out.println("New state from game client address " + context.getAddress().getHostAddress());
    
    state.getProvider().ifPresent(provider -> {
        System.out.println("Client SteamID: " + provider.getClientSteamId());
    });
    state.getMap().ifPresent(map -> {
        System.out.println("Current map: " + map.getName());
    });
};

// Configure server
GSIServer server = new GSIServer.Builder(1337)        // Port 1337, on all network interfaces
        .requireAuthToken("password", "Q79v5tcxVQ8u") // Require the specified password
        .registerListener(listener)                   // Alternatively, you can call this dynamically on the GSIServer
        .build();

// Start server
try {
    server.start(); // Start the server (runs in a separate thread)
    System.out.println("Server started. Listening for state data...");
} catch (IOException e) {
    System.err.println("Couldn't start server.");
}
```

### Accessing the diagnostics page
If not disabled (using the GSIServer builder), you can access the server as a standard webpage, revealing information
 about the server and the game state information being received.
<details>
<summary>Sample output screenshot</summary>
<img src="https://user-images.githubusercontent.com/1368580/98445604-686b7c00-2110-11eb-9cc9-44886371eae2.png">
</details>

## Development
If you experience a bug or think the library is missing some functionality, please submit an issue or pull request.