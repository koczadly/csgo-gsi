package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;

@JsonAdapter(Coordinate.Deserializer.class)
public class Coordinate {
    
    private final double x, y, z;
    
    public Coordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    
    /**
     * @return the X coordinate or vector
     */
    public double getX() {
        return x;
    }
    
    /**
     * @return the Y coordinate or vector
     */
    public double getY() {
        return y;
    }
    
    /**
     * @return the Z coordinate or vector
     */
    public double getZ() {
        return z;
    }
    
    /**
     * @return true if all {@code X}, {@code Y} and {@code Z} values are zero.
     */
    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
    }
    
    
    @Override
    public int hashCode() {
        return (int)(x * y * z);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Coordinate) {
            Coordinate cObj = (Coordinate)obj;
            return cObj.x == this.x &&
                    cObj.y == this.y &&
                    cObj.z == this.z;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "{X=" + x + ", Y=" + y + ", Z=" + z + "}";
    }
    
    
    
    static class Deserializer implements JsonDeserializer<Coordinate> {
        @Override
        public Coordinate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String[] val = json.getAsString().split(", ");
            return new Coordinate(
                    Double.parseDouble(val[0]),
                    Double.parseDouble(val[1]),
                    Double.parseDouble(val[2]));
        }
    }
    
}
