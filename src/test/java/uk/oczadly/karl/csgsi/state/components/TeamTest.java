package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.Gson;
import org.junit.Test;
import uk.oczadly.karl.csgsi.internal.Util;
import static org.junit.Assert.*;

public class TeamTest {
    
    @Test
    public void testDeserializer() {
        assertEquals(Team.TERRORIST,
                Util.GSON.fromJson("\"t\"", Team.class));
    
        assertEquals(Team.TERRORIST,
                Util.GSON.fromJson("\"T\"", Team.class));
    
        assertEquals(Team.COUNTER_TERRORIST,
                Util.GSON.fromJson("\"ct\"", Team.class));
    
        assertEquals(Team.COUNTER_TERRORIST,
                Util.GSON.fromJson("\"CT\"", Team.class));
    }
    
}
