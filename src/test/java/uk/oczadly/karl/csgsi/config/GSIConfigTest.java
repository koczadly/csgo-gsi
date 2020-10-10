package uk.oczadly.karl.csgsi.config;

import org.junit.Test;
import uk.oczadly.karl.csgsi.config.DataComponent;
import uk.oczadly.karl.csgsi.config.GSIConfig;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class GSIConfigTest {
    
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
                .setAuthToken("token", "42")
                .addDataComponent(DataComponent.BOMB)
                .setBufferPeriod(20d)
                .setTimeoutPeriod(30d)
                .setHeartbeatPeriod(40d)
                .setThrottlePeriod(50d)
                .setDescription("Desc")
                .setPrecisionPosition(421)
                .setPrecisionTime(422)
                .setPrecisionVector(423);
        
        //Generate string
        String config = genConfig(profile);
        
        //Tests
        testSet(config, "uri", "http://1.2.3.4:567");
        testSet(config, "token", "42");
        testSet(config, "buffer", 20d);
        testSet(config, "timeout", 30d);
        testSet(config, "heartbeat", 40d);
        testSet(config, "throttle", 50d);
        testSet(config, DataComponent.BOMB.getConfigName(), "1");
        testSet(config, DataComponent.MAP.getConfigName(), "0");
        testSet(config, "precision_position", 421);
        testSet(config, "precision_time", 422);
        testSet(config, "precision_vector", 423);
    }
    
    
    /*
    TODO: doesn't test for nested values correctly, only that the key-value pair is in the object.
    Also relies heavily on the exact formatting of the file (spaces, etc), rather than the structure.
     */
    private static void testSet(String conf, String key, Object expectedValue) {
        assertTrue("Key \"" + key + "\" and value \"" + expectedValue.toString() + "\" not apparent in exported configuration",
                conf.contains("\"" + key + "\"\t\"" + expectedValue.toString() + "\""));
    }
    
    private static String genConfig(GSIConfig profile) {
        StringWriter sw = new StringWriter();
        profile.generate(new PrintWriter(sw));
        return sw.toString();
    }

}