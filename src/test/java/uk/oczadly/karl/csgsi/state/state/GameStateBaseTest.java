package uk.oczadly.karl.csgsi.state.state;

import com.google.gson.Gson;
import uk.oczadly.karl.csgsi.GSIUtil;
import uk.oczadly.karl.csgsi.state.GameState;

public class GameStateBaseTest {
    
    private final static Gson GSON = GSIUtil.createGsonObject().create();
    
    
    protected GameState deserilizeState(String json) {
        return GSON.fromJson(json, GameState.class);
    }
    
}
