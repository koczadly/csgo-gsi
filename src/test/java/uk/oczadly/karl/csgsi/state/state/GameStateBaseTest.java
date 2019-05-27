package uk.oczadly.karl.csgsi.state.state;

import com.google.gson.Gson;
import org.junit.Test;
import uk.oczadly.karl.csgsi.GSIUtil;
import uk.oczadly.karl.csgsi.state.GameState;
import uk.oczadly.karl.csgsi.state.RoundState;

import static org.junit.Assert.*;

public class GameStateBaseTest {
    
    public final static Gson GSON = GSIUtil.createGsonObject().create();
    
    
    public static GameState deserilizeState(String json) {
        return GSON.fromJson(json, GameState.class);
    }
    
    @Test
    public void test() {
        testEnums(RoundState.RoundPhase.class, "over", "freezetime", "live");
    }
    
    public static <T extends Enum> void testEnums(Class<T> clazz, String... values) {
        //Test values
        for (String name : values) {
            T enumVal = GSON.fromJson("\"" + name + "\"", clazz);
            
            assertNotNull("Enum value \"" + name + "\" of " + clazz.getCanonicalName() + " couldn't be deserialized",
                    enumVal);
        }
        
        //Test null
        T nullVal = GSON.fromJson("\"\"", clazz);
        assertNull("Unknown enum value was deserialized for " + clazz.getCanonicalName(),
                nullVal);
    }
    
}
