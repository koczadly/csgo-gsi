# CSGO-GSI
## About
A Java library for retrieving live game information from Counter-Strike: Global Offensive.

This project is currently a work in progress, although most features are already implemented.

## Features
This library provides 3 main features:
- Automatic location of the Steam and game directories
- The creation of game state configurations
- A server which listens for updates and parses the state details

## Usage
### Configuration generation
To create a configuration file, use the GSIConfig class. The SteamUtils class provides a range of static methods
which can be used to automatically locate the CSGO game directory. The example below demonstrates how to use
these utilities:

```java
GSIConfig config = new GSIConfig("http://127.0.0.1:1337")
        .setTimeoutPeriod(1.0)
        .setBufferPeriod(0.5)
        .addAuthToken("password", "Q79v5tcxVQ8u")
        .setDataComponents(EnumSet.allOf(DataComponent.class));

try {
    Path configPath = SteamUtils.findCsgoConfigFolder();
    
    if (configPath != null) {
        GSIConfig.createConfig(configPath, config, "myservice");
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
GSIObserver observer = new GSIObserver() {
    @Override
    public void update(GameState state, GameState previousState, Map<String, String> authTokens, InetAddress address) {
        //Access state information with the 'state' object...
        System.out.println("Client SteamID: " + state.getProvider().getClientSteamId());
    }
};

GSIServer server = new GSIServer(1337); //Configure on port 1337
server.registerObserver(observer); //Register observer
server.start(); //Start the server (on the above specified port)
```