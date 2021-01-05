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
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
    
    private static final Pattern REG_PATTERN = Pattern.compile("^ {4}(\\S+) {4}\\S+ {4}(.+)$");
    
    
    public static final String GITHUB_URL = "https://github.com/koczadly/csgo-gsi";
    
    public static final Gson GSON = new GsonBuilder()
            .setLenient().excludeFieldsWithoutExposeAnnotation().create();
    
    
    public static String repeatChar(char c, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i=0; i<length; i++)
            sb.append(c);
        return sb.toString();
    }
    
    /**
     * Helper method to read Windows registry keys
     */
    public static String readWinRegValue(String path, String key) {
        String value = null;
        try {
            Process proc = Runtime.getRuntime().exec("reg query \"" + path + "\" /v \"" + key + "\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = REG_PATTERN.matcher(line);
                if (matcher.matches() && matcher.group(1).equalsIgnoreCase(key)) {
                    value = matcher.group(2);
                }
            }
            reader.close();
            proc.destroy();
        } catch (IOException e) {
            LOGGER.warn("Failed to read registry key {} at path {}", key, path, e);
            return null;
        }
        if (value == null)
            LOGGER.warn("Failed to read registry key {} at path {}", key, path);
        return value;
    }
    
    public static String refVal(Object o) {
        return o.getClass().getSimpleName() + '@' + Integer.toHexString(System.identityHashCode(o));
    }
    
}
