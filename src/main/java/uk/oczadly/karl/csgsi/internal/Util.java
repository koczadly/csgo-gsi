package uk.oczadly.karl.csgsi.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Util {
    
    public static Gson createGsonObject() {
        return new GsonBuilder()
                .setLenient().excludeFieldsWithoutExposeAnnotation().create();
    }
    
}
