package uk.oczadly.karl.csgsi.config;

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class GSIConfigTest {
    
    static final Pattern CONF_PATTERN = Pattern.compile(" *\"(\\w+)\" +\"(.+)\"");
    
    
    @Test
    public void testGettersAndSetters() {
        GSIConfig profile = new GSIConfig("");
        
        //URI
        profile.setURI("http://1.2.3.4:56");
        assertEquals("http://1.2.3.4:56", profile.getURI());
    
        profile.setAuthToken("token", "auth_val");
        assertEquals("auth_val", profile.getAuthTokens().get("token"));
    
        profile.setBufferPeriod(1d);
        assertEquals(1d, profile.getBufferPeriod(), 0.001d);
    
        profile.setTimeoutPeriod(2d);
        assertEquals(2d, profile.getTimeoutPeriod(), 0.001d);
    
        profile.setHeartbeatPeriod(3d);
        assertEquals(3d, profile.getHeartbeatPeriod(), 0.001d);
    
        profile.setThrottlePeriod(4d);
        assertEquals( 4d, profile.getThrottlePeriod(), 0.001d);
    
        profile.setPrecisionPosition(421);
        assertEquals((Integer)421, profile.getPrecisionPosition());
    
        profile.setPrecisionTime(422);
        assertEquals((Integer)422, profile.getPrecisionTime());
    
        profile.setPrecisionVector(423);
        assertEquals((Integer)423, profile.getPrecisionVector());
    
        profile.addDataComponent(DataComponent.BOMB);
        assertEquals(1, profile.getDataComponents().size());
        assertTrue(profile.getDataComponents().contains(DataComponent.BOMB));
        assertFalse(profile.getDataComponents().contains(DataComponent.MAP));
    }
    
    
    /* Does not actually check for conformity of the output file, only
       that the specified elements are present in the string. */
    @Test
    public void testProfileCreation() {
        //Profile
        GSIConfig profile = new GSIConfig("http://1.2.3.4:567")
                .setAuthToken("token", "42A")
                .addDataComponent(DataComponent.BOMB)
                .setBufferPeriod(20.1)
                .setTimeoutPeriod(30.2)
                .setHeartbeatPeriod(40.3)
                .setThrottlePeriod(50.4)
                .setDescription("Desc")
                .setPrecisionPosition(421)
                .setPrecisionTime(422)
                .setPrecisionVector(423);
        
        //Generate string
        String config = genConfig(profile);
        
        //Tests
        assertConfigValue(config, "uri", "http://1.2.3.4:567");
        assertConfigValue(config, "token", "42A");
        assertConfigValue(config, "buffer", "20.1");
        assertConfigValue(config, "timeout", "30.2");
        assertConfigValue(config, "heartbeat", "40.3");
        assertConfigValue(config, "throttle", "50.4");
        assertConfigValue(config, DataComponent.BOMB.getConfigName(), "1");
        assertConfigValue(config, DataComponent.MAP.getConfigName(), "0");
        assertConfigValue(config, "precision_position", "421");
        assertConfigValue(config, "precision_time", "422");
        assertConfigValue(config, "precision_vector", "423");
    }
    
    
    // TODO: doesn't test for nested values correctly, only that the key-value pair is in the object.
    private static void assertConfigValue(String conf, String key, String expectedValue) {
        for (String line : conf.split("[\\r\\n]+")) {
            Matcher matcher = CONF_PATTERN.matcher(line);
            if (matcher.matches()) {
                String curKey = matcher.group(1), curVal = matcher.group(2);
                if (curKey.equals(key)) {
                    assertEquals(expectedValue, curVal);
                    return;
                }
            }
        }
        fail("Key \"" + key + "\" not found in exported config.");
    }
    
    private static String genConfig(GSIConfig profile) {
        StringWriter sw = new StringWriter();
        profile.export(new PrintWriter(sw));
        return sw.toString();
    }

}