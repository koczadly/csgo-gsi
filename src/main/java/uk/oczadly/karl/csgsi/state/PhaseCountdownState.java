package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.state.components.DeserializedEnum;

public class PhaseCountdownState {
    
    @Expose
    @SerializedName("phase")
    private DeserializedEnum<Phase> phase;
    
    @Expose
    @SerializedName("phase_ends_in")
    private Double timer;
    
    
    /**
     * @return the currently ongoing countdown phase
     */
    public DeserializedEnum<Phase> getPhase() {
        return phase;
    }
    
    /**
     * @return the remaining time of the phase in seconds
     */
    public Double getRemainingTime() {
        return timer;
    }
    
    
    public enum Phase {
        /**
         * Game currently in progress.
         */
        @SerializedName("live")
        LIVE,
        
        /**
         * Game in progress and bomb has been planted.
         */
        @SerializedName("bomb")
        BOMB,
        
        /**
         * Bomb is currently being defused.
         */
        @SerializedName("defuse")
        DEFUSE,
        
        /**
         * Round is over.
         */
        @SerializedName("over")
        OVER,
        
        /**
         * Round is over.
         */
        @SerializedName("freezetime")
        FREEZE_TIME
    }
    
}
