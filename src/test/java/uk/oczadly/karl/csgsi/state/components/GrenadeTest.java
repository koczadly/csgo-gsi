package uk.oczadly.karl.csgsi.state.components;

import org.junit.Test;
import uk.oczadly.karl.csgsi.internal.Util;
import uk.oczadly.karl.csgsi.state.components.grenade.EffectGrenade;
import uk.oczadly.karl.csgsi.state.components.grenade.Grenade;
import uk.oczadly.karl.csgsi.state.components.grenade.IncendiaryGrenade;
import uk.oczadly.karl.csgsi.state.components.grenade.ProjectileGrenade;

import static org.junit.Assert.assertEquals;

/**
 * @author Karl Oczadly
 */
public class GrenadeTest {

    @Test
    public void testDeserializeSmoke() {
        String json = "{" +
                "\t\"owner\": 76561197960265729,\n" +
                "\t\"position\": \"1.00, 2.00, 3.00\",\n" +
                "\t\"velocity\": \"4.00, 5.00, 6.00\",\n" +
                "\t\"lifetime\": \"10.7\",\n" +
                "\t\"type\": \"smoke\",\n" +
                "\t\"effecttime\": \"9.8\"}";
    
        EffectGrenade grenade = (EffectGrenade)Util.GSON.fromJson(json, Grenade.class);
        assertEquals(Grenade.Type.SMOKE, grenade.getType().asEnum());
        assertEquals(PlayerSteamID.fromId64("76561197960265729"), grenade.getOwner());
        assertEquals(new Coordinate(1, 2, 3), grenade.getPosition());
        assertEquals(new Coordinate(4, 5, 6), grenade.getVelocity());
        assertEquals(10.7, grenade.getLifetime(), 1e-9);
        assertEquals(9.8, grenade.getEffectTime(), 1e-9);
    }
    
    @Test
    public void testDeserializeFirebomb() {
        String json = "{" +
                "\t\"owner\": 76561197960265729,\n" +
                "\t\"position\": \"1.00, 2.00, 3.00\",\n" +
                "\t\"velocity\": \"4.00, 5.00, 6.00\",\n" +
                "\t\"lifetime\": \"10.7\",\n" +
                "\t\"type\": \"firebomb\"}";
        
        ProjectileGrenade grenade = (ProjectileGrenade)Util.GSON.fromJson(json, Grenade.class);
        assertEquals(Grenade.Type.MOLOTOV, grenade.getType().asEnum());
        assertEquals(PlayerSteamID.fromId64("76561197960265729"), grenade.getOwner());
        assertEquals(new Coordinate(1, 2, 3), grenade.getPosition());
        assertEquals(new Coordinate(4, 5, 6), grenade.getVelocity());
        assertEquals(10.7, grenade.getLifetime(), 1e-9);
    }
    
    @Test
    public void testDeserializeFlame() {
        String json = "{" +
                "\t\"owner\": 76561197960265729,\n" +
                "\t\"lifetime\": \"10.7\",\n" +
                "\t\"type\": \"inferno\",\n" +
                "\"flames\": {\n" +
                "\t\t\"flame_p0_p0_p0\": \"-140.03, 329.38, 1.63\",\n" +
                "\t\t\"flame_p55_n21_n1\": \"-85.03, 308.38, 0.63\",\n" +
                "\t\t\"flame_p117_n29_n1\": \"-23.03, 300.38, 0.63\",\n" +
                "\t\t\"flame_n31_p41_n3\": \"-171.03, 370.38, -1.38\",\n" +
                "\t\t\"flame_p149_p2_n2\": \"8.97, 331.38, -0.38\",\n" +
                "\t\t\"flame_p120_p89_n3\": \"-20.03, 418.38, -1.38\"}}";
        
        IncendiaryGrenade grenade = (IncendiaryGrenade)Util.GSON.fromJson(json, Grenade.class);
        assertEquals(Grenade.Type.MOLOTOV_FLAMES, grenade.getType().asEnum());
        assertEquals(PlayerSteamID.fromId64("76561197960265729"), grenade.getOwner());
        assertEquals(new Coordinate(-95.53, 359.38, -1.38), grenade.getApproxPosition());
        assertEquals(6, grenade.getFlames().size());
        assertEquals(new Coordinate(-140.03, 329.38, 1.63), grenade.getFlames().get("flame_p0_p0_p0"));
        assertEquals(183.0, grenade.getApproxSize(), 1e-9);
        assertEquals(10.7, grenade.getLifetime(), 1e-9);
    }
    
    @Test
    public void testDeserializeFlashbang() {
        String json = "{" +
                "\t\"owner\": 76561197960265729,\n" +
                "\t\"position\": \"1.00, 2.00, 3.00\",\n" +
                "\t\"velocity\": \"4.00, 5.00, 6.00\",\n" +
                "\t\"lifetime\": \"10.7\",\n" +
                "\t\"type\": \"flashbang\"}";
        
        ProjectileGrenade grenade = (ProjectileGrenade)Util.GSON.fromJson(json, Grenade.class);
        assertEquals(Grenade.Type.FLASHBANG, grenade.getType().asEnum());
        assertEquals(PlayerSteamID.fromId64("76561197960265729"), grenade.getOwner());
        assertEquals(new Coordinate(1, 2, 3), grenade.getPosition());
        assertEquals(new Coordinate(4, 5, 6), grenade.getVelocity());
        assertEquals(10.7, grenade.getLifetime(), 1e-9);
    }
    
    @Test
    public void testDeserializeDecoy() {
        String json = "{" +
                "\t\"owner\": 76561197960265729,\n" +
                "\t\"position\": \"1.00, 2.00, 3.00\",\n" +
                "\t\"velocity\": \"4.00, 5.00, 6.00\",\n" +
                "\t\"lifetime\": \"10.7\",\n" +
                "\t\"type\": \"decoy\",\n" +
                "\t\"effecttime\": \"9.8\"}";
        
        EffectGrenade grenade = (EffectGrenade)Util.GSON.fromJson(json, Grenade.class);
        assertEquals(Grenade.Type.DECOY, grenade.getType().asEnum());
        assertEquals(PlayerSteamID.fromId64("76561197960265729"), grenade.getOwner());
        assertEquals(new Coordinate(1, 2, 3), grenade.getPosition());
        assertEquals(new Coordinate(4, 5, 6), grenade.getVelocity());
        assertEquals(10.7, grenade.getLifetime(), 1e-9);
        assertEquals(9.8, grenade.getEffectTime(), 1e-9);
    }
    
    @Test
    public void testDeserializeFrag() {
        String json = "{" +
                "\t\"owner\": 76561197960265729,\n" +
                "\t\"position\": \"1.00, 2.00, 3.00\",\n" +
                "\t\"velocity\": \"4.00, 5.00, 6.00\",\n" +
                "\t\"lifetime\": \"10.7\",\n" +
                "\t\"type\": \"frag\"}";
        
        ProjectileGrenade grenade = (ProjectileGrenade)Util.GSON.fromJson(json, Grenade.class);
        assertEquals(Grenade.Type.FRAG, grenade.getType().asEnum());
        assertEquals(PlayerSteamID.fromId64("76561197960265729"), grenade.getOwner());
        assertEquals(new Coordinate(1, 2, 3), grenade.getPosition());
        assertEquals(new Coordinate(4, 5, 6), grenade.getVelocity());
        assertEquals(10.7, grenade.getLifetime(), 1e-9);
    }
    
    @Test
    public void testDeserializeUnknown() {
        String json = "{" +
                "\t\"owner\": 76561197960265729,\n" +
                "\t\"lifetime\": \"10.7\",\n" +
                "\t\"type\": \"slugs\"}";
        
        Grenade grenade = Util.GSON.fromJson(json, Grenade.class);
        assertEquals("slugs", grenade.getType().asString());
        assertEquals(PlayerSteamID.fromId64("76561197960265729"), grenade.getOwner());
        assertEquals(10.7, grenade.getLifetime(), 1e-9);
    }

}