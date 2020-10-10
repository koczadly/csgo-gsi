package uk.oczadly.karl.csgsi.state;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.csgsi.state.components.Coordinate;
import uk.oczadly.karl.csgsi.state.components.EnumValue;
import uk.oczadly.karl.csgsi.state.components.PlayerInventory;
import uk.oczadly.karl.csgsi.state.components.Team;

public class PlayerState {

    @Expose @SerializedName("steamid")
    private String steamId;
    
    @Expose @SerializedName("name")
    private String name;
    
    @Expose @SerializedName("clan")
    private String groupName;
    
    @Expose @SerializedName("observer_slot")
    private Integer observerSlot;
    
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
    private String specTarget;
    
    @Expose @SerializedName("position")
    private Coordinate position;
    
    @Expose @SerializedName("forward")
    private Coordinate facing;
    
    
    /**
     * @return the Steam ID of the player, or null if not the client
     */
    public String getSteamId() {
        return steamId;
    }
    
    /**
     * @return the display name of the player
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the name of the player's nominated clan/Steam group
     */
    public String getGroupName() {
        return groupName;
    }
    
    /**
     * @return the slot this player is using (associated numerical key to spectate)
     */
    public Integer getObserverSlot() {
        return observerSlot;
    }
    
    /**
     * @return which team this player is on (terrorist or counter-terrorist)
     */
    public EnumValue<Team> getTeam() {
        return team;
    }
    
    /**
     * @return this player's activity, or null if not the client
     */
    public EnumValue<Activity> getActivity() {
        return activity;
    }
    
    /**
     * @return the current statistics for this player for this map
     */
    public MatchStats getStatistics() {
        return stats;
    }
    
    /**
     * @return state information relating to this player
     */
    public PlayerStateDetails getState() {
        return state;
    }
    
    /**
     * @return the current set of weapons and items held by the player
     */
    public PlayerInventory getInventory() {
        return inventory;
    }
    
    /**
     * @return the ID of the player being spectated, or null if not spectating
     */
    public String getSpectatorTarget() {
        return specTarget;
    }
    
    /**
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
        private int health;
        
        @Expose @SerializedName("armor")
        private int armor;
        
        @Expose @SerializedName("helmet")
        private boolean helmet;
        
        @Expose @SerializedName("defusekit")
        private boolean defuseKit;
        
        @Expose @SerializedName("flashed")
        private int flashed;
        
        @Expose @SerializedName("smoked")
        private int smoked;
        
        @Expose @SerializedName("burning")
        private int burning;
        
        @Expose @SerializedName("money")
        private int money;
        
        @Expose @SerializedName("round_kills")
        private int roundKills;
        
        @Expose @SerializedName("round_killhs")
        private int roundKillsHeadshot;
        
        @Expose @SerializedName("round_totaldmg")
        private int roundTotalDamage;
        
        @Expose @SerializedName("equip_value")
        private int equipmentValue;
        
        
        public int getHealth() {
            return health;
        }
        
        public int getArmor() {
            return armor;
        }
        
        public boolean hasHelmet() {
            return helmet;
        }
    
        public boolean hasDefuseKit() {
            return defuseKit;
        }
    
        public int getFlashed() {
            return flashed;
        }
        
        public int getSmoked() {
            return smoked;
        }
        
        public int getBurning() {
            return burning;
        }
        
        public int getMoney() {
            return money;
        }
        
        public int getRoundKills() {
            return roundKills;
        }
        
        public int getRoundKillsHeadshot() {
            return roundKillsHeadshot;
        }
        
        public int getRoundTotalDamage() {
            return roundTotalDamage;
        }
        
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
        private int kills;
        
        @Expose @SerializedName("assists")
        private int assists;
        
        @Expose @SerializedName("deaths")
        private int deaths;
        
        @Expose @SerializedName("mvps")
        private int mvps;
        
        @Expose @SerializedName("score")
        private int score;
        
        
        public int getKillCount() {
            return kills;
        }
        
        public int getAssistCount() {
            return assists;
        }
        
        public int getDeathCount() {
            return deaths;
        }
        
        public int getMvpCount() {
            return mvps;
        }
        
        public int getScore() {
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
