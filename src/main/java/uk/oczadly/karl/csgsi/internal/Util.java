package uk.oczadly.karl.csgsi.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    public static final String REPO_URL = "https://github.com/koczadly/csgo-gsi";
    
    public static final Gson GSON = new GsonBuilder()
            .setLenient().excludeFieldsWithoutExposeAnnotation().create();
    
    
    public static String repeatChar(char c, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i=0; i<length; i++)
            sb.append(c);
        return sb.toString();
    }

    public static String refVal(Object o) {
        return o.getClass().getSimpleName() + '@' + Integer.toHexString(System.identityHashCode(o));
    }
    
}
