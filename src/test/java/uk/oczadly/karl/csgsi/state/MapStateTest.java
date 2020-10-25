package uk.oczadly.karl.csgsi.state;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class MapStateTest extends GameStateBaseTest {
    
    static MapState mapState;
    
    @BeforeClass
    public static void setUp() {
        GameState gameState = deserializeState("{\n" +
                "  \"map\": {\n" +
                "    \"round_wins\": {\n" +
                "      \"1\": \"t_win_bomb\",\n" +
                "      \"2\": \"t_win_elimination\",\n" +
                "      \"3\": \"t_win_time\",\n" +
                "      \"4\": \"ct_win_elimination\"\n" +
                "    },\n" +
                "    \"mode\": \"competitive\",\n" +
                "    \"name\": \"de_cache\",\n" +
                "    \"phase\": \"live\",\n" +
                "    \"round\": 101,\n" +
                "    \"team_ct\": {\n" +
                "      \"score\": 201,\n" +
                "      \"consecutive_round_losses\": 202,\n" +
                "      \"timeouts_remaining\": 203,\n" +
                "      \"matches_won_this_series\": 204\n" +
                "    },\n" +
                "    \"team_t\": {\n" +
                "      \"score\": 205,\n" +
                "      \"consecutive_round_losses\": 206,\n" +
                "      \"timeouts_remaining\": 207,\n" +
                "      \"matches_won_this_series\": 208\n" +
                "    },\n" +
                "    \"num_matches_to_win_series\": 102,\n" +
                "    \"current_spectators\": 103,\n" +
                "    \"souvenirs_total\": 104\n" +
                "  }\n" +
                "}");
        
        assertNotNull(gameState);
        assertTrue(gameState.getMap().isPresent());
        mapState = gameState.getMap().get();
    }
    
    
    @Test
    public void testRoundResults() {
        assertNotNull(mapState.getRoundResults());
        assertEquals(4, mapState.getRoundResults().size());
        assertEquals(MapState.RoundOutcome.T_BOMB_EXPLODE, mapState.getRoundResults().get(0).val());
        assertEquals(MapState.RoundOutcome.T_ELIMINATION, mapState.getRoundResults().get(1).val());
        assertEquals(MapState.RoundOutcome.T_TIME, mapState.getRoundResults().get(2).val());
        assertEquals(MapState.RoundOutcome.CT_ELIMINATION, mapState.getRoundResults().get(3).val());
    }
    
    @Test
    public void testGameMode() {
        assertNotNull(mapState.getMode());
        assertEquals(MapState.GameMode.COMPETITIVE, mapState.getMode().val());
    }
    
    @Test
    public void testName() {
        assertNotNull(mapState.getName());
        assertEquals("de_cache", mapState.getName());
    }
    
    @Test
    public void testPhase() {
        assertNotNull(mapState.getPhase());
        assertEquals(MapState.GamePhase.LIVE, mapState.getPhase().val());
    }
    
    @Test
    public void testRoundNumber() {
        assertEquals(101, mapState.getRoundNumber());
    }
    
    @Test
    public void testTeamStats() {
        assertNotNull(mapState.getCounterTerroristStatistics());
        assertNotNull(mapState.getTerroristStatistics());
        
        assertEquals(201, mapState.getCounterTerroristStatistics().getScore());
        assertEquals(202, mapState.getCounterTerroristStatistics().getConsecutiveLosses());
        assertEquals(203, mapState.getCounterTerroristStatistics().getTimeoutsRemaining());
        assertEquals(204, mapState.getCounterTerroristStatistics().getSeriesMatchesWon());
        
        assertEquals(205, mapState.getTerroristStatistics().getScore());
        assertEquals(206, mapState.getTerroristStatistics().getConsecutiveLosses());
        assertEquals(207, mapState.getTerroristStatistics().getTimeoutsRemaining());
        assertEquals(208, mapState.getTerroristStatistics().getSeriesMatchesWon());
    }
    
    @Test
    public void testSeriesMatches() {
        assertEquals(102, mapState.getSeriesMatchesToWin());
    }
    
    @Test
    public void testSpectatorCount() {
        assertEquals(103, mapState.getSpectatorCount());
    }
    @Test
    public void testSouvenirDrops() {
        assertEquals(104, mapState.getSouvenirDrops());
    }
    
    
    @Test
    public void testRoundOutcomeEnum() {
        testEnums(MapState.RoundOutcome.class,
                "t_win_bomb", "t_win_elimination", "t_win_time", "ct_win_elimination", "ct_win_time", "ct_win_defuse",
                "ct_win_rescue", "");
    }
    
    @Test
    public void testGameModeEnum() {
        testEnums(MapState.GameMode.class,
                "competitive", "casual", "deathmatch", "skirmish", "survival", "scrimcomp2v2");
    }
    
    @Test
    public void testGamePhaseEnum() {
        testEnums(MapState.GamePhase.class,
                "live", "gameover", "warmup", "intermission");
    }
    
}
