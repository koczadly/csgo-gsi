package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;

public class ProviderState {
    
    @Expose @SerializedName("name")
    private String name;
    
    @Expose @SerializedName("appid")
    private int appId;
    
    @Expose @SerializedName("version")
    private int version;
    
    @Expose @SerializedName("steamid")
    private String steamId;
    
    @Expose @SerializedName("timestamp")
    private Instant timeStamp;
    
    
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
    public String getClientSteamId() {
        return steamId;
    }
    
    /**
     * @return the timestamp of the reported game state
     */
    public Instant getTimeStamp() {
        return timeStamp;
    }
    
}
