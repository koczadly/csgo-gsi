package uk.oczadly.karl.csgsi.state;

import com.google.gson.Gson;
import uk.oczadly.karl.csgsi.internal.Util;

import static org.junit.Assert.*;

public class GameStateBaseTest {
    
    public static GameState deserializeState(String json) {
        return Util.GSON.fromJson(json, GameState.class);
    }
    
    public static <T extends Enum> void testEnums(Class<T> clazz, String... values) {
        //Test values
        for (String name : values) {
            T enumVal = Util.GSON.fromJson("\"" + name + "\"", clazz);
            
            assertNotNull("Enum value \"" + name + "\" of " + clazz.getCanonicalName() + " couldn't be deserialized",
                    enumVal);
        }
        
        //Test null
        T nullVal = Util.GSON.fromJson("\"slugs8984328942\"", clazz);
        assertNull("Unknown enum value was deserialized for " + clazz.getCanonicalName(),
                nullVal);
    }
    
}
