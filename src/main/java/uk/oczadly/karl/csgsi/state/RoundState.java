package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.state.components.DeserializedEnum;
import uk.oczadly.karl.csgsi.state.components.Team;

public class RoundState {
    
    @Expose
    @SerializedName("phase")
    private DeserializedEnum<RoundPhase> phase;
    
    @Expose
    @SerializedName("win_team")
    private DeserializedEnum<Team> winningTeam;
    
    @Expose
    @SerializedName("bomb")
    private DeserializedEnum<BombPhase> bomb;
    
    
    /**
     * @return the current status of the round
     */
    public DeserializedEnum<RoundPhase> getPhase() {
        return phase;
    }
    
    /**
     * @return the winning team of the round
     */
    public DeserializedEnum<Team> getWinningTeam() {
        return winningTeam;
    }
    
    /**
     * @return the current status of the bomb
     */
    public DeserializedEnum<BombPhase> getBombPhase() {
        return bomb;
    }
    
    
    public enum RoundPhase {
        /**
         * Round has ended.
         */
        @SerializedName("over")
        OVER,
        
        /**
         * Round is currently in freeze time.
         */
        @SerializedName("freezetime")
        FREEZE_TIME,
        
        /**
         * Round is currently in progress.
         */
        @SerializedName("live")
        LIVE
    }
    
    public enum BombPhase {
        /**
         * Bomb has exploded.
         */
        @SerializedName("exploded")
        EXPLODED,
        
        /**
         * Bomb has been planted.
         */
        @SerializedName("planted")
        PLANTED,
        
        /**
         * Bomb has been defused.
         */
        @SerializedName("defused")
        DEFUSED
    }
    
}
