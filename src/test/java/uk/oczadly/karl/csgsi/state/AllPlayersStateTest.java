package uk.oczadly.karl.csgsi.state;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AllPlayersStateTest extends GameStateBaseTest {
    
    @Test
    public void testSize() {
        GameState state = deserializeState("{\n" +
                "  \"allplayers\": {\n" +
                "    \"76561197960265729\": {\n" +
                "      \"name\": \"Wesley\",\n" +
                "      \"observer_slot\": 1,\n" +
                "      \"team\": \"CT\",\n" +
                "      \"match_stats\": {\n" +
                "        \"kills\": 0,\n" +
                "        \"assists\": 0,\n" +
                "        \"deaths\": 0,\n" +
                "        \"mvps\": 0,\n" +
                "        \"score\": 0\n" +
                "      },\n" +
                "      \"position\": \"-1353.00, 645.00, 1614.03\",\n" +
                "      \"forward\": \"0.29, -0.96, -0.03\",\n" +
                "      \"state\": {\n" +
                "        \"health\": 100,\n" +
                "        \"armor\": 0,\n" +
                "        \"helmet\": false,\n" +
                "        \"flashed\": 0,\n" +
                "        \"burning\": 0,\n" +
                "        \"money\": 800,\n" +
                "        \"round_kills\": 0,\n" +
                "        \"round_killhs\": 0,\n" +
                "        \"round_totaldmg\": 0,\n" +
                "        \"equip_value\": 200\n" +
                "      },\n" +
                "      \"weapons\": {\n" +
                "        \"weapon_0\": {\n" +
                "          \"name\": \"weapon_knife\",\n" +
                "          \"paintkit\": \"default\",\n" +
                "          \"type\": \"Knife\",\n" +
                "          \"state\": \"holstered\"\n" +
                "        },\n" +
                "        \"weapon_1\": {\n" +
                "          \"name\": \"weapon_hkp2000\",\n" +
                "          \"paintkit\": \"default\",\n" +
                "          \"type\": \"Pistol\",\n" +
                "          \"ammo_clip\": 13,\n" +
                "          \"ammo_clip_max\": 13,\n" +
                "          \"ammo_reserve\": 52,\n" +
                "          \"state\": \"active\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}");
        assertNotNull(state);
        
        assertEquals(1, state.getAllPlayerStates().size());
        assertNotNull(state.getAllPlayerStates().get("76561197960265729"));
        
        assertEquals("Wesley", state.getAllPlayerStates().get("76561197960265729").getName());
    }
    
}
