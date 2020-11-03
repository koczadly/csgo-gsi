package uk.oczadly.karl.csgsi.state;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.oczadly.karl.csgsi.state.components.Coordinate;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;
import uk.oczadly.karl.csgsi.state.components.Team;

import static org.junit.Assert.*;

public class PlayerStateTest extends GameStateBaseTest {
    
    static PlayerState playerState;
    
    @BeforeClass
    public static void setUp() {
        GameState gameState = deserializeState("{\n" +
                "  \"player\": {\n" +
                "    \"steamid\": \"76561198050830377\",\n" +
                "    \"name\": \"PlayerName\",\n" +
                "    \"clan\": \"GroupName\",\n" +
                "    \"observer_slot\": 6,\n" +
                "    \"team\": \"t\",\n" +
                "    \"activity\": \"playing\",\n" +
                "    \"match_stats\": {\n" +
                "      \"kills\": 10,\n" +
                "      \"assists\": 11,\n" +
                "      \"deaths\": 12,\n" +
                "      \"mvps\": 13,\n" +
                "      \"score\": 14\n" +
                "    },\n" +
                "    \"state\": {\n" +
                "      \"health\": 20,\n" +
                "      \"armor\": 21,\n" +
                "      \"helmet\": true,\n" +
                "      \"flashed\": 22,\n" +
                "      \"smoked\": 23,\n" +
                "      \"burning\": 24,\n" +
                "      \"money\": 25,\n" +
                "      \"round_kills\": 26,\n" +
                "      \"round_killhs\": 27,\n" +
                "      \"round_totaldmg\": 28,\n" +
                "      \"equip_value\": 29\n" +
                "    },\n" +
                "    \"weapons\": {\n" +
                "      \"weapon_0\": {\n" +
                "        \"name\": \"weapon_knife_t\",\n" +
                "        \"paintkit\": \"default\",\n" +
                "        \"type\": \"Knife\",\n" +
                "        \"state\": \"holstered\"\n" +
                "      },\n" +
                "      \"weapon_1\": {\n" +
                "        \"name\": \"weapon_glock\",\n" +
                "        \"paintkit\": \"default\",\n" +
                "        \"type\": \"Pistol\",\n" +
                "        \"ammo_clip\": 20,\n" +
                "        \"ammo_clip_max\": 20,\n" +
                "        \"ammo_reserve\": 120,\n" +
                "        \"state\": \"holstered\"\n" +
                "      },\n" +
                "      \"weapon_2\": {\n" +
                "        \"name\": \"weapon_c4\",\n" +
                "        \"paintkit\": \"default\",\n" +
                "        \"type\": \"C4\",\n" +
                "        \"state\": \"active\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"spectarget\": \"76561198050830377\",\n" +
                "    \"position\": \"-1382.89, 191.24, 1769.19\",\n" +
                "    \"forward\": \"0.95, -0.30, 0.08\"\n" +
                "  }\n" +
                "}");
        
        assertNotNull(gameState);
        assertTrue(gameState.getPlayer().isPresent());
        playerState = gameState.getPlayer().get();
    }
    
    
    @Test
    public void testSteamId() {
        assertEquals(PlayerSteamID.fromId64("76561198050830377"), playerState.getSteamId());
    }
    
    @Test
    public void testName() {
        assertEquals("PlayerName", playerState.getName());
    }
    
    @Test
    public void testGroupName() {
        assertEquals("GroupName", playerState.getGroupName());
    }
    
    @Test
    public void testObserverSlot() {
        assertEquals(6, (byte)playerState.getObserverSlot());
    }
    
    @Test
    public void testTeam() {
        assertEquals(Team.TERRORIST, playerState.getTeam().get());
    }
    
    @Test
    public void testActivity() {
        assertEquals(PlayerState.Activity.PLAYING, playerState.getActivity().get());
    }
    
    @Test
    public void testStats() {
        assertNotNull(playerState.getStatistics());
        
        assertEquals(10, playerState.getStatistics().getKillCount());
        assertEquals(11, playerState.getStatistics().getAssistCount());
        assertEquals(12, playerState.getStatistics().getDeathCount());
        assertEquals(13, playerState.getStatistics().getMvpCount());
        assertEquals(14, playerState.getStatistics().getScore());
    }
    
    @Test
    public void testState() {
        assertNotNull(playerState.getState());
        
        assertEquals(20, playerState.getState().getHealth());
        assertEquals(21, playerState.getState().getArmor());
        assertTrue(playerState.getState().hasHelmet());
        assertEquals(22, playerState.getState().getFlashed());
        assertEquals(23, playerState.getState().getSmoked());
        assertEquals(24, playerState.getState().getBurning());
        assertEquals(25, playerState.getState().getMoney());
        assertEquals(26, playerState.getState().getRoundKills());
        assertEquals(27, playerState.getState().getRoundKillsHeadshot());
        assertEquals(28, playerState.getState().getRoundTotalDamage());
        assertEquals(29, playerState.getState().getEquipmentValue());
    }
    
    @Test
    public void testWeapons() {
        // Additional info is tested in PlayerInventoryTest
        assertNotNull(playerState.getInventory());
    }
    
    @Test
    public void testSpecTarget() {
        assertNotNull(playerState.getSpectatorTarget());
        assertEquals(PlayerSteamID.fromId64("76561198050830377"), playerState.getSpectatorTarget());
    }
    
    @Test
    public void testPosition() {
        assertNotNull(playerState.getPosition());
        assertEquals(new Coordinate(-1382.89, 191.24, 1769.19), playerState.getPosition());
    }
    
    @Test
    public void testDirection() {
        assertNotNull(playerState.getDirection());
        assertEquals(new Coordinate(0.95, -0.30, 0.08), playerState.getDirection());
    }
    
    
    
    @Test
    public void testActivityEnum() {
        testEnums(PlayerState.Activity.class,
                "playing", "textinput", "menu");
    }
    
}
