package uk.oczadly.karl.csgsi.state.json;

import com.google.gson.*;
import uk.oczadly.karl.csgsi.state.components.Coordinate;

import java.lang.reflect.Type;

public class CoordinateDeserializer implements JsonDeserializer<Coordinate> {
    
    @Override
    public Coordinate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String[] val = json.getAsString().split(",");
        return new Coordinate(
                Double.parseDouble(val[0].trim()),
                Double.parseDouble(val[1].trim()),
                Double.parseDouble(val[2].trim()));
    }
    
}
