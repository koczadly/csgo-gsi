package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;

import java.time.Instant;

public class ProviderState {
    
    @Expose @SerializedName("name")
    private String name;
    
    @Expose @SerializedName("appid")
    private int appId;
    
    @Expose @SerializedName("version")
    private int version;
    
    @Expose @SerializedName("steamid")
    private PlayerSteamID steamId;
    
    @Expose @SerializedName("timestamp")
    private long timestamp;
    
    
    /**
     * @return the name of the game (expected "Counter-Strike: Global Offensive")
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the Steam app ID of the game (expected 730)
     */
    public int getAppId() {
        return appId;
    }
    
    /**
     * @return the version of the reporting game client
     */
    public int getVersion() {
        return version;
    }
    
    /**
     * @return the Steam ID of the player logged into the game client
     */
    public PlayerSteamID getClientSteamId() {
        return steamId;
    }
    
    /**
     * @return the Unix timestamp reported by the game client
     */
    public long getTimestamp() {
        return timestamp;
    }
    
}
