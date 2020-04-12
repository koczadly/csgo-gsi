package uk.oczadly.karl.csgsi.internal;

import com.google.gson.GsonBuilder;
import uk.oczadly.karl.csgsi.state.components.Coordinate;
import uk.oczadly.karl.csgsi.state.json.CoordinateDeserializer;

public class Util {
    
    public static GsonBuilder createGsonObject() {
        return new GsonBuilder()
                .setLenient().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Coordinate.class, new CoordinateDeserializer());
    }
    
}
