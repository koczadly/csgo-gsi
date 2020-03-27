package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.annotations.SerializedName;

public enum Team {
    
    @SerializedName(value = "t", alternate = {"T"})
    TERRORIST,
    
    @SerializedName(value = "ct", alternate = {"CT"})
    COUNTER_TERRORIST
    
}
