package uk.oczadly.karl.csgsi.state.json;

import com.google.gson.Gson;
import org.junit.Test;
import uk.oczadly.karl.csgsi.state.components.Coordinate;

import static org.junit.Assert.assertEquals;

public class CoordinateDeserializerTest {
    
    public Gson gson = new Gson();
    
    class Container {
        Coordinate val;
    }
    

    @Test
    public void testDeserialization() {
        //Integer
        Coordinate c1 = gson.fromJson("{\"val\": \"42, 80, 480\"}",
                Container.class).val;
        assertEquals(c1.getX(), 42d, 1e-10);
        assertEquals(c1.getY(), 80d, 1e-10);
        assertEquals(c1.getZ(), 480d, 1e-10);
    
        //Double
        Coordinate c2 = gson.fromJson("{\"val\": \"12.34, 56.78, 91.01234\"}",
                Container.class).val;
        assertEquals(c2.getX(), 12.34d, 1e-10);
        assertEquals(c2.getY(), 56.78d, 1e-10);
        assertEquals(c2.getZ(), 91.01234d, 1e-10);
    }

}