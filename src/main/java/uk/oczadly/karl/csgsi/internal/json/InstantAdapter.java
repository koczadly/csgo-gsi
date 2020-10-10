package uk.oczadly.karl.csgsi.internal.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.Instant;

/**
 * Unix seconds
 * @author Karl Oczadly
 */
public class InstantAdapter implements JsonDeserializer<Instant> {
    
    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        long val = json.getAsLong();
        return val != 0 ? Instant.ofEpochSecond(val) : null;
    }
    
}
