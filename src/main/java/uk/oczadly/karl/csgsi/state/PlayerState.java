package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.config.DataComponent;
import uk.oczadly.karl.csgsi.state.components.*;

public class PlayerState {

    @Expose @SerializedName("steamid")
    private PlayerSteamID steamId;
    
    @Expose @SerializedName("name")
    private String name;
    
    @Expose @SerializedName("clan")
    private String groupName;
    
    @Expose @SerializedName("observer_slot")
    private Byte observerSlot;
    
    @Expose @SerializedName("team")
    private EnumValue<Team> team;
    
    @Expose @SerializedName("activity")
    private EnumValue<Activity> activity;
    
    @Expose @SerializedName("match_stats")
    private MatchStats stats;
    
    @Expose @SerializedName("state")
    private PlayerStateDetails state;
    
    @Expose @SerializedName("weapons")
    private PlayerInventory inventory;
    
    @Expose @SerializedName("spectarget")
    private PlayerSteamID specTarget;
    
    @Expose @SerializedName("position")
    private Coordinate position;
    
    @Expose @SerializedName("forward")
    private Coordinate facing;
    
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_ID} (when accessing from {@link GameState#getPlayer()})</li>
     *     <li>{@link DataComponent#PLAYERS_ID} (when accessing from {@link GameState#getAllPlayers()})</li>
     * </ul>
     *
     * @return the Steam ID of the player
     */
    public PlayerSteamID getSteamId() {
        return steamId;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_ID} (when accessing from {@link GameState#getPlayer()})</li>
     *     <li>{@link DataComponent#PLAYERS_ID} (when accessing from {@link GameState#getAllPlayers()})</li>
     * </ul>
     *
     * @return the display name of the player
     */
    public String getName() {
        return name;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_ID} (when accessing from {@link GameState#getPlayer()})</li>
     *     <li>{@link DataComponent#PLAYERS_ID} (when accessing from {@link GameState#getAllPlayers()})</li>
     * </ul>
     *
     * @return the name of the player's nominated clan/Steam group
     */
    public String getGroupName() {
        return groupName;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_ID} (when accessing from {@link GameState#getPlayer()})</li>
     *     <li>{@link DataComponent#PLAYERS_ID} (when accessing from {@link GameState#getAllPlayers()})</li>
     * </ul>
     *
     * @return the slot this player is using (associated numerical key to spectate), or null if they aren't a player
     */
    public Byte getObserverSlot() {
        return observerSlot;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_ID} (when accessing from {@link GameState#getPlayer()})</li>
     *     <li>{@link DataComponent#PLAYERS_ID} (when accessing from {@link GameState#getAllPlayers()})</li>
     * </ul>
     *
     * @return which team this player is on (terrorist or counter-terrorist)
     */
    public EnumValue<Team> getTeam() {
        return team;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_ID} (when accessing from {@link GameState#getPlayer()})</li>
     * </ul>
     *
     * @return this player's activity, or null if not the client
     */
    public EnumValue<Activity> getActivity() {
        return activity;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_MATCH_STATS} (when accessing from {@link GameState#getPlayer()})</li>
     *     <li>{@link DataComponent#PLAYERS_MATCH_STATS} (when accessing from {@link
     *     GameState#getAllPlayers()})</li>
     * </ul>
     *
     * @return the current statistics for this player for this map
     */
    public MatchStats getStatistics() {
        return stats;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_STATE} (when accessing from {@link GameState#getPlayer()})</li>
     *     <li>{@link DataComponent#PLAYERS_STATE} (when accessing from {@link GameState#getAllPlayers()})</li>
     * </ul>
     *
     * @return state information relating to this player
     */
    public PlayerStateDetails getState() {
        return state;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_WEAPONS} (when accessing from {@link GameState#getPlayer()})</li>
     *     <li>{@link DataComponent#PLAYERS_WEAPONS} (when accessing from {@link GameState#getAllPlayers()},
     *     spectating only)</li>
     * </ul>
     *
     * @return the current set of weapons and items held by the player
     */
    public PlayerInventory getInventory() {
        return inventory;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_ID} (when accessing from {@link GameState#getPlayer()},
     *     spectating only)</li>
     * </ul>
     *
     * @return the ID of the player being spectated, or null if not spectating
     */
    public PlayerSteamID getSpectatorTarget() {
        return specTarget;
    }
    
    /**
     * <p>This set of state information requires one or more of the following data components to be enabled in the
     * configuration:</p>
     * <ul>
     *     <li>{@link DataComponent#PLAYER_POSITION} (when accessing from {@link GameState#getPlayer()},
     *     spectating only)
     *     </li>
     *     <li>{@link DataComponent#PLAYERS_POSITION} (when accessing from {@link GameState#getPlayer()},
     *     spectating only)</li>
     * </ul>
     *
     * @return the position of this player on the map
     */
    public Coordinate getPosition() {
        return position;
    }
    
    /**
     * @return the direction this player is facing
     */
    public Coordinate getDirection() {
        return facing;
    }
    
    @Override
    public String toString() {
        return getName() + " (" + getSteamId() + ")";
    }
    
    
    public enum Activity {
        /**
         * Currently playing/watching the game.
         */
        @SerializedName("playing") PLAYING,
        
        /**
         * Currently typing in the chat.
         */
        @SerializedName("textinput") TEXT_INPUT,
        
        /**
         * Currently navigating a game menu.
         */
        @SerializedName("menu") MENU
    }
    
    public static class PlayerStateDetails {
        
        @Expose @SerializedName("health")
        private short health;
        
        @Expose @SerializedName("armor")
        private short armor;
        
        @Expose @SerializedName("helmet")
        private boolean helmet;
        
        @Expose @SerializedName("defusekit")
        private boolean defuseKit;
        
        @Expose @SerializedName("flashed")
        private short flashed;
        
        @Expose @SerializedName("smoked")
        private short smoked;
        
        @Expose @SerializedName("burning")
        private short burning;
        
        @Expose @SerializedName("money")
        private int money;
        
        @Expose @SerializedName("round_kills")
        private short roundKills;
        
        @Expose @SerializedName("round_killhs")
        private short roundKillsHeadshot;
        
        @Expose @SerializedName("round_totaldmg")
        private short roundTotalDamage;
        
        @Expose @SerializedName("equip_value")
        private int equipmentValue;
    
    
        /**
         * @return the players health value (typically {@code 0–100})
         */
        public short getHealth() {
            return health;
        }
    
        /**
         * @return the players armor value (typically {@code 0–100})
         */
        public short getArmor() {
            return armor;
        }
    
        /**
         * @return true if the player has armor
         */
        public boolean hasArmor() {
            return getArmor() > 0;
        }
    
        /**
         * @return true if the player has head armor
         */
        public boolean hasHelmet() {
            return helmet;
        }
    
        /**
         * @return true if the player has a defuse kit
         */
        public boolean hasDefuseKit() {
            return defuseKit;
        }
    
        /**
         * @return how flashed the player's screen is (from {@code 0—255})
         */
        public short getFlashed() {
            return flashed;
        }
    
        /**
         * @return true if the player is flashed
         */
        public boolean isFlashed() {
            return getFlashed() > 0;
        }
    
        /**
         * @return how smoked the player's vision is (from {@code 0—255})
         */
        public short getSmoked() {
            return smoked;
        }
    
        /**
         * @return true if the player is standing in smoke
         */
        public boolean isSmoked() {
            return getSmoked() > 0;
        }
    
        /**
         * @return how much the player is burning (from {@code 0—255})
         */
        public short getBurning() {
            return burning;
        }
    
        /**
         * @return true if the player is burning or on fire
         */
        public boolean isBurning() {
            return getBurning() > 0;
        }
    
        /**
         * @return the amount of money the player has
         */
        public int getMoney() {
            return money;
        }
    
        /**
         * @return the number of kills the player has during the ongoing round
         */
        public short getRoundKills() {
            return roundKills;
        }
    
        /**
         * @return the number of headshot kills the player has during the ongoing round
         */
        public short getRoundKillsHeadshot() {
            return roundKillsHeadshot;
        }
    
        /**
         * @return the amount of damage the player has given during the ongoing round
         */
        public short getRoundTotalDamage() {
            return roundTotalDamage;
        }
    
        /**
         * @return the value of all the player's current equipment
         */
        public int getEquipmentValue() {
            return equipmentValue;
        }
    
    
        @Override
        public String toString() {
            return "PlayerStateDetails{" +
                    "health=" + health +
                    ", armor=" + armor +
                    ", helmet=" + helmet +
                    ", defuseKit=" + defuseKit +
                    ", flashed=" + flashed +
                    ", smoked=" + smoked +
                    ", burning=" + burning +
                    ", money=" + money +
                    ", roundKills=" + roundKills +
                    ", roundKillsHeadshot=" + roundKillsHeadshot +
                    ", roundTotalDamage=" + roundTotalDamage +
                    ", equipmentValue=" + equipmentValue +
                    '}';
        }
    }
    
    public static class MatchStats {
        
        @Expose @SerializedName("kills")
        private short kills;
        
        @Expose @SerializedName("assists")
        private short assists;
        
        @Expose @SerializedName("deaths")
        private short deaths;
        
        @Expose @SerializedName("mvps")
        private short mvps;
        
        @Expose @SerializedName("score")
        private short score;
    
    
        /**
         * @return the number of kills the player has during the current game
         */
        public short getKillCount() {
            return kills;
        }
    
        /**
         * @return the number of assists the player has during the current game
         */
        public short getAssistCount() {
            return assists;
        }
    
        /**
         * @return the number of deaths the player has during the current game
         */
        public short getDeathCount() {
            return deaths;
        }
    
        /**
         * @return the kill-to-death ratio of the player
         */
        public float getKDR() {
            return (float)getKillCount() / getDeathCount();
        }
    
        /**
         * @return the number of MVP awards the player has achieved during the current game
         */
        public short getMvpCount() {
            return mvps;
        }
    
        /**
         * @return the score of the player shown on the scoreboard
         */
        public short getScore() {
            return score;
        }
    
    
        @Override
        public String toString() {
            return "MatchStats{" +
                    "kills=" + kills +
                    ", assists=" + assists +
                    ", deaths=" + deaths +
                    ", mvps=" + mvps +
                    ", score=" + score +
                    '}';
        }
    }

}
