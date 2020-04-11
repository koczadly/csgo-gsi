# CSGO-GSI
## About
A Java library for retrieving real-time game information and statistics from Counter-Strike: Global Offensive using
its built-in *game state integration* service.

This project is in active development, although has already been tested and should provide full functionality.

## Features
This library provides 3 main features:
- Automated location of the Steam and game directories
- The creation of game state service configurations
- A server which listens for updates and parses the state details

## Usage
### Maven
This project is hosted on Maven Central. To import this library, add the following dependency into your pom.xml:
```xml
<dependency>
    <groupId>uk.oczadly.karl</groupId>
    <artifactId>csgo-gsi</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Documentation
The latest Javadoc pages can be [viewed online through Javadoc.io](https://www.javadoc.io/doc/uk.oczadly.karl/csgo-gsi).

### Configuration generation
To create a configuration file, use the GSIConfig class. The SteamUtils class provides a range of static methods
which can be used to automatically locate the CSGO game directory. The example below demonstrates how to use
these utilities:

```java
GSIConfig config = new GSIConfig("http://127.0.0.1:1337")
        .setTimeoutPeriod(1.0)
        .setBufferPeriod(0.5)
        .setAuthToken("password", "Q79v5tcxVQ8u")
        .setDataComponents(
                DataComponent.PROVIDER,
                DataComponent.ROUND);

try {
    //Locate the CSGO configuration folder
    Path configPath = SteamUtils.locateCsgoConfigFolder();
    
    if (configPath != null) {
        GSIConfig.createConfig(configPath, config, "my_service");
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
listener which prints the client's logged in Steam ID to the console.
```java
//Create a new observer (anonymous class)
GSIObserver observer = new GSIObserver() {
    @Override
    public void update(GameState state, GameState previousState, Map<String, String> authTokens, InetAddress address) {
        //Access state information with the 'state' object...
        System.out.println("New state! Client SteamID: " + state.getProvider().getClientSteamId());
    }
};

GSIServer server = new GSIServer(1337); //Configure on port 1337
server.registerObserver(observer); //Register observer
server.start(); //Start the server (on the above specified port)
```

## Development
If you experience an issue or feel the library is missing functionality, submit an issue (or a pull request if you've
already resolved the problem).