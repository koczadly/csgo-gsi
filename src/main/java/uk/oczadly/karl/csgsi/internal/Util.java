package uk.oczadly.karl.csgsi.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.oczadly.karl.csgsi.internal.json.InstantAdapter;

import java.time.Instant;

public class Util {
    
    public static Gson createGsonObject() {
        return new GsonBuilder()
                .setLenient().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
    }
    
    public static String repeatChar(char c, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i=0; i<length; i++)
            sb.append(c);
        return sb.toString();
    }
    
}
