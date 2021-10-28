package uk.oczadly.karl.csgsi.util.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class OSUtil {

    private static final Logger log = LoggerFactory.getLogger(OSUtil.class);


    private static final Pattern WIN_REG_PATTERN = Pattern.compile("^ {4}(\\S+) {4}\\S+ {4}(.+)$");

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
                Matcher matcher = WIN_REG_PATTERN.matcher(line);
                if (matcher.matches() && matcher.group(1).equalsIgnoreCase(key)) {
                    value = matcher.group(2);
                }
            }
            reader.close();
            proc.destroy();
        } catch (IOException e) {
            log.warn("Failed to read registry key {} at path {}", key, path, e);
            return null;
        }
        if (value == null)
            log.warn("Failed to read registry key {} at path {}", key, path);
        return value;
    }

    public static Family getSystemFamily() {
        String name = System.getProperty("os.name").toLowerCase(); // Obtain current OS name
        if (name.contains("windows")) {
            return Family.WINDOWS;
        } else if (name.contains("mac")) {
            return Family.MAC;
        } else if (name.contains("nux")) {
            return Family.LINUX;
        } else if (name.contains("nix") || name.contains("aix")) {
            return Family.UNIX;
        }
        return Family.UNRECOGNIZED;
    }


    enum Family { WINDOWS, MAC, LINUX, UNIX, UNRECOGNIZED }

}
