package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can be used to represent a set of 3 floating point values, labelled {@code X}, {@code Y} and {@code Z}.
 * <p>When used as a coordinate, the X and Y values represent a position on the map, while the Z value represents
 * the vertical height of the location.</p>
 */
@JsonAdapter(Coordinate.Deserializer.class)
public class Coordinate {
    
    private static final Pattern PATTERN = Pattern.compile("^([-.0-9]+), ([-.0-9]+), ([-.0-9]+)$");
    
    private final double x, y, z;
    
    public Coordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    
    /**
     * @return the X coordinate
     */
    public double getX() {
        return x;
    }
    
    /**
     * @return the Y coordinate
     */
    public double getY() {
        return y;
    }
    
    /**
     * @return the Z coordinate (altitude)
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
        return String.format("Coordinate{x=%,.2f, y=%,.2f, z=%,.2f}", x, y, z);
    }
    
    
    /**
     * Parses from the format "{@code x, y, z}".
     * @param str the string to parse from
     * @return the created Coordinate object
     * @throws IllegalArgumentException if not a valid string
     */
    public static Coordinate parse(String str) {
        Matcher matcher = PATTERN.matcher(str);
        if (!matcher.matches())
            throw new IllegalArgumentException("Invalid coordinate format.");
        
        return new Coordinate(
                Double.parseDouble(matcher.group(1)),
                Double.parseDouble(matcher.group(2)),
                Double.parseDouble(matcher.group(3)));
    }
    
    
    static class Deserializer implements JsonDeserializer<Coordinate> {
        @Override
        public Coordinate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Coordinate.parse(json.getAsString());
        }
    }
    
}
