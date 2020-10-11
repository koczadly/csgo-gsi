package uk.oczadly.karl.csgsi.state.components;

import org.junit.Test;
import uk.oczadly.karl.csgsi.internal.Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CoordinateTest {
    
    @Test
    public void testVals() {
        Coordinate c1 = new Coordinate(12.34, 56.78, 91.0123);
    
        assertEquals(c1.getX(), 12.34, 1e-10);
        assertEquals(c1.getY(), 56.78, 1e-10);
        assertEquals(c1.getZ(), 91.0123, 1e-10);
    }

    @Test
    public void testEquality() {
        Coordinate c1 = new Coordinate(12.34, 56.78, 91.0123);
        
        //Self equality
        assertEquals(c1, c1);
        
        //Same values
        assertEquals(c1, new Coordinate(12.34, 56.78, 91.0123));
        
        //Different vals
        assertNotEquals(c1, new Coordinate(1, 56.78, 91.0123)); //X
        assertNotEquals(c1, new Coordinate(12.34, 1, 91.0123)); //Y
        assertNotEquals(c1, new Coordinate(12.34, 56.78, 1)); //Z
    }
    
    @Test
    public void testHashcode() {
        Coordinate c1 = new Coordinate(12.34, 56.78, 91.0123);
        Coordinate c2 = new Coordinate(12.34, 56.78, 91.0123);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
    
    @Test
    public void testDeserialize() {
        Coordinate coord = Util.GSON.fromJson("\"-1.1, -2.2, -3.3\"", Coordinate.class);
        assertEquals(coord.getX(), -1.1, 1e-10);
        assertEquals(coord.getY(), -2.2, 1e-10);
        assertEquals(coord.getZ(), -3.3, 1e-10);
    }
    
}